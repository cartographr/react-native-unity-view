package no.asmadsen.unity.view

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.widget.FrameLayout

class UnityView(context: Context) : FrameLayout(context) {
    private var unity: ManagedUnityPlayer? = null

    fun setUnityPlayer(player: ManagedUnityPlayer) {
        unity = player
        Log.v("UnityView", "setUnityPlayer")
        UnityUtils.addUnityViewToGroup(this, player)
        player.resume()
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        unity?.player?.windowFocusChanged(hasWindowFocus)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        unity?.player?.configurationChanged(newConfig)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        unity?.let { unity ->
            if (unity.player.parent !== this) {
                UnityUtils.addUnityViewToGroup(this, unity)
            }
            unity.resume()
        }
    }

    override fun onDetachedFromWindow() {
        unity?.let { unity ->
            UnityUtils.addUnityViewToBackground(unity)
            unity.pause()
        }
        super.onDetachedFromWindow()
    }
}