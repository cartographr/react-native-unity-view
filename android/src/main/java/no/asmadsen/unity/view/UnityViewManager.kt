package no.asmadsen.unity.view

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.uimanager.ThemedReactContext

class UnityViewManager internal constructor(
    private val context: ReactApplicationContext
) : SimpleViewManager<UnityView>(), LifecycleEventListener {

    override fun getName(): String = "RNUnityView"

    override fun initialize() {
        context.addLifecycleEventListener(this)
    }

    override fun createViewInstance(reactContext: ThemedReactContext): UnityView {
        val view = UnityView(reactContext)

        val activity = reactContext.currentActivity ?: return view
        UnityPlayerManager.activeRequests.incrementAndGet()
        UnityPlayerManager.acquire(activity) { player ->
            val activeRequests = UnityPlayerManager.activeRequests.get()
            Log.v("UnityView", "Acquired player; activeRequests=$activeRequests")
            if (activeRequests > 0) {
                view.setUnityPlayer(player)
            }
        }

        return view
    }

    override fun onDropViewInstance(view: UnityView) {
        super.onDropViewInstance(view)
        UnityPlayerManager.activeRequests.decrementAndGet()
        UnityPlayerManager.get(context.currentActivity)?.pause()
    }

    override fun onHostResume() {
        UnityPlayerManager.get(context.currentActivity)?.resume()
    }

    override fun onHostPause() {
        UnityPlayerManager.get(context.currentActivity)?.pause()
    }

    override fun onHostDestroy() {
        UnityPlayerManager.destroy()
    }
}