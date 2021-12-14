package no.asmadsen.unity.view

import android.app.Activity
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import com.unity3d.player.UnityPlayer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * @author dhleong
 */
class ManagedUnityPlayer(
    val player: UnityPlayer,
    private val isFullScreen: Boolean,
) {
    private enum class State(
        val isPause: Boolean,
    ) {
        PAUSING(true),
        PAUSED(true),
        RESUMING(false),
        RESUMED(false),
    }

    init {
        log("create unity")
    }

    private val isValid = AtomicBoolean(true)
    private val currentState = AtomicReference(State.PAUSED)

    val valid: Boolean
        get() = isValid.get()

    fun removeFromParent() {
        (player.parent as? ViewGroup)?.removeView(player)
    }

    fun resume() {
        if (!valid) return log("resume: not valid")
        if (player.parent == null) {
            log("resume: not attached")
            return
        }

        val state = currentState.get()
        if (state == State.RESUMED) {
            log("resume: already RESUMED")
            return
        }
        currentState.set(State.RESUMING)
        if (state != State.PAUSED) {
            log("resume: was in $state")
        }

        player.windowFocusChanged(true)
        if (Looper.myLooper() === Looper.getMainLooper()) {
            player.requestFocus()
        }
        player.resume()
        applyFullscreen()

        queueGLThreadEvent {
            log("completed resume")
            val requested = currentState.get()
            if (!currentState.compareAndSet(State.RESUMING, State.RESUMED)) {
                log("resume: $requested requested in between")
                if (requested.isPause) {
                    pause()
                }
            }
        }
    }

    fun pause() {
        if (!valid) return log("pause: not valid")
        val state = currentState.get()
        if (state == State.PAUSED) {
            log("pause: already PAUSED")
            return
        }
        currentState.set(State.PAUSING)
        if (state != State.RESUMED) {
            log("pause: was in $state")
        }

        player.pause()

        queueGLThreadEvent {
            log("completed pause")
            val requested = currentState.get()
            if (!currentState.compareAndSet(State.PAUSING, State.PAUSED)) {
                log("pause: $requested requested in between")
                if (!requested.isPause) {
                    resume()
                }
            }
        }
    }

    fun destroy() {
        pause()
        if (isValid.getAndSet(false)) {
            log("destroy")
            player.quit()
        } else {
            log("destroy: already destroyed")
        }
    }

    fun applyFullscreen() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            setFullscreen(isFullScreen)
        } else {
            (player.context as? Activity)?.runOnUiThread {
                setFullscreen(isFullScreen)
            }
        }
    }

    private fun queueGLThreadEvent(callback: () -> Unit) {
        queueGLThreadEventMethod.invoke(player, Runnable(callback))
    }

    @Suppress("DEPRECATION")
    private fun setFullscreen(isFullScreen: Boolean) {
        val activity = player.context as? Activity ?: return
        if (!isFullScreen) {
            Log.v("UnityView", "Not fullscreen")
            activity.window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
                clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        } else {
            Log.v("UnityView", "Fullscreen")
        }
    }

    companion object {
        private fun log(message: String) {
            if (BuildConfig.DEBUG) Log.v("UnityView", message)
        }

        private val queueGLThreadEventMethod by lazy {
            try {
                UnityPlayer::class.java.getDeclaredMethod(
                    "queueGLThreadEvent",
                    Runnable::class.java
                ).apply {
                    isAccessible = true
                }
            } catch (e: Throwable) {
                Log.w("UnityView", "ERROR resolving queueGLThreadEvent", e)
                requireNotNull(
                    UnityPlayer::class.java.declaredMethods.find {
                        log("examine: ${it.name} / ${it.parameterTypes.toList()}")
                        it.parameterTypes.size == 1 && Runnable::class.java == it.parameterTypes[0]
                    }?.apply {
                        log("found: $name")
                        isAccessible = true
                    }
                ) { "Couldn't find queueGLThreadEvent" }
            }
        }
    }
}