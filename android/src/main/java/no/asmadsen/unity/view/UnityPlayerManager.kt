package no.asmadsen.unity.view

import android.annotation.SuppressLint
import android.app.Activity
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author dhleong
 */
@SuppressLint("StaticFieldLeak")
object UnityPlayerManager {
    private var lastActivity: Activity? = null
    private var lastPlayer: ManagedUnityPlayer? = null

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

    fun acquire(activity: Activity, onReady: (ManagedUnityPlayer) -> Unit) {
        val existing = get(activity)
        if (existing != null) {
            return onReady(existing)
        }

        val hasRequests = requests.isNotEmpty()
        requests += onReady
        lastActivity = activity

        if (!hasRequests) {
            UnityUtils.createPlayer(activity) { player ->
                lastPlayer = player
                for (request in requests) {
                    request(player)
                }
                requests.clear()
            }
        }
    }

    fun hasPlayer(): Boolean = lastPlayer?.valid == true
}