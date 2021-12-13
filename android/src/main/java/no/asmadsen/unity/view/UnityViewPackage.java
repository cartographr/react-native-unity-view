package no.asmadsen.unity.view;

import android.util.Log;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class UnityViewPackage implements ReactPackage {
    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        Log.v("UnityView", "createNativeModules");
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new UnityNativeModule(reactContext));
        return modules;
    }

    @NonNull
    @Override
    @SuppressWarnings("rawtypes")
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        Log.v("UnityView", "createViewManagers");
        List<ViewManager> viewManagers = new ArrayList<>();
        viewManagers.add(new UnityViewManager(reactContext));
        return viewManagers;
    }
}
