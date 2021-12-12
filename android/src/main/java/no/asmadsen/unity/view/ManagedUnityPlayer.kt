package no.asmadsen.unity.view

import com.unity3d.player.UnityPlayer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author dhleong
 */
class ManagedUnityPlayer(val player: UnityPlayer) {
    private val isValid = AtomicBoolean(true)
    private val isResumed = AtomicBoolean(false)

    val valid: Boolean
        get() = isValid.get()

    fun resume() {
        if (!valid) return
        if (!isResumed.getAndSet(true)) {
            player.onWindowFocusChanged(true)
            player.requestFocus()
            player.resume()
        }
    }

    fun pause() {
        if (!valid) return
        if (isResumed.getAndSet(true)) {
            player.pause()
        }
    }

    fun destroy() {
        pause()
        if (isValid.getAndSet(false)) {
            player.quit()
        }
    }
}