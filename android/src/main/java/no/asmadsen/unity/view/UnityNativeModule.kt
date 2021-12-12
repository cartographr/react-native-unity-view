package no.asmadsen.unity.view

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter

class UnityNativeModule(
    reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext), UnityEventListener {

    init {
        UnityUtils.addUnityEventListener(this)
    }

    override fun getName(): String = "UnityNativeModule"

    @ReactMethod
    fun isReady(promise: Promise) {
        promise.resolve(UnityPlayerManager.get(currentActivity) != null)
    }

    @ReactMethod
    fun createUnity(promise: Promise) {
        val activity = currentActivity
        if (activity == null) {
            promise.resolve(false)
        } else {
            UnityPlayerManager.acquire(activity) {
                promise.resolve(true)
            }
        }
    }

    @ReactMethod
    fun postMessage(gameObject: String?, methodName: String?, message: String?) {
        UnityUtils.postMessage(gameObject, methodName, message)
    }

    @ReactMethod
    fun pause() {
        UnityPlayerManager.get(currentActivity)?.pause()
    }

    @ReactMethod
    fun resume() {
        UnityPlayerManager.get(currentActivity)?.resume()
    }

    override fun onMessage(message: String) {
        val context: ReactContext = reactApplicationContext
        context.getJSModule(RCTDeviceEventEmitter::class.java).emit("onUnityMessage", message)
    }
}