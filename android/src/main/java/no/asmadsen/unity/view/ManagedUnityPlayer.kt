package no.asmadsen.unity.view

import android.util.Log
import com.unity3d.player.UnityPlayer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author dhleong
 */
class ManagedUnityPlayer(val player: UnityPlayer) {

    init {
        log("create unity")
    }

    private val isValid = AtomicBoolean(true)
    private val isResumed = AtomicBoolean(false)

    val valid: Boolean
        get() = isValid.get()

    fun resume() {
        if (!valid) return log("resume: not valid")
        if (player.parent == null) {
            log("resume: not attached")
            return
        }
        if (!isResumed.getAndSet(true)) {
            log("resume")
            player.onWindowFocusChanged(true)
            player.requestFocus()
            player.resume()
        } else {
            log("resume: already resumed")
        }
    }

    fun pause() {
        if (!valid) return log("pause: not valid")
        if (isResumed.getAndSet(false)) {
            log("pause")
            player.pause()
        } else {
            log("pause: already paused")
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

    private fun log(message: String) {
        if (BuildConfig.DEBUG) Log.v("UnityView", message)
    }
}