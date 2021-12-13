package no.asmadsen.unity.view

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.PixelFormat
import android.util.Log
import com.unity3d.player.UnityPlayer
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

const val DEFAULT_WARMUP_DURATION_MS = 1000L

/**
 * @author dhleong
 */
@SuppressLint("StaticFieldLeak")
object UnityPlayerManager {
    private var lastActivity: Activity? = null
    private var lastPlayer: ManagedUnityPlayer? = null

    val activeRequests = AtomicInteger(0)
    private val requests = CopyOnWriteArrayList<(ManagedUnityPlayer) -> Unit>()

    fun get(activity: Activity?): ManagedUnityPlayer? {
        val existing = lastPlayer
        if (existing != null && existing.valid) {
            if (lastActivity === activity) {
                return existing
            }

            this.lastPlayer = null
            existing.destroy()
        }

        return null
    }

    fun acquire(
        activity: Activity,
        warmupDurationMs: Long = DEFAULT_WARMUP_DURATION_MS,
        onReady: (ManagedUnityPlayer) -> Unit,
    ) {
        val existing = get(activity)
        if (existing != null) {
            return onReady(existing)
        }

        val hasRequests = requests.isNotEmpty()
        requests += onReady
        lastActivity = activity

        if (!hasRequests) {
            Log.v("UnityView", "createPlayer")
            createPlayer(activity, warmupDurationMs) { player ->
                lastPlayer = player
                for (request in requests) {
                    request(player)
                }
                requests.clear()
            }
        } else {
            Log.v("UnityView", "player creation pending")
        }
    }

    fun pause() {
        lastPlayer?.pause()
    }

    fun destroy() {
        lastPlayer?.destroy()
        lastPlayer = null
    }

    fun hasPlayer(): Boolean = lastPlayer?.valid == true

    private fun createPlayer(
        activity: Activity,
        warmupDurationMs: Long,
        callback: (ManagedUnityPlayer) -> Unit,
    ) {
        activity.runOnUiThread {
            activity.window.setFormat(PixelFormat.RGBA_8888)
            val unity = ManagedUnityPlayer(UnityPlayer(activity))

            // Start unity
            UnityUtils.addUnityViewToBackground(unity)
            unity.resume()

            if (activeRequests.get() == 0) {
                Log.v("UnityView", "no active requests")
                unity.pauseAfterWarmup(warmupDurationMs)
            }

            callback(unity)
        }
    }

    private fun ManagedUnityPlayer.pauseAfterWarmup(warmupDurationMs: Long) {
        // NOTE: We delay the pause to give Unity a chance to "warm up"
        player.view.postDelayed({
            if (activeRequests.get() == 0) {
                pause()
            }
        }, warmupDurationMs)
    }
}
