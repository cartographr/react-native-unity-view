package no.asmadsen.unity.view;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.ViewGroup;

import com.unity3d.player.UnityPlayer;

import java.util.concurrent.CopyOnWriteArraySet;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class UnityUtils {
    private static final CopyOnWriteArraySet<UnityEventListener> mUnityEventListeners =
            new CopyOnWriteArraySet<>();

    public static void createPlayer(final Activity activity, final CreateCallback callback) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getWindow().setFormat(PixelFormat.RGBA_8888);
//                int flag = activity.getWindow().getAttributes().flags;
//                boolean fullScreen = false;
//                if ((flag & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
//                    fullScreen = true;
//                }

                final ManagedUnityPlayer unity =
                    new ManagedUnityPlayer(new UnityPlayer(activity));

//                try {
//                    // wait a moment. fix unity cannot start when startup.
//                    Thread.sleep(1000);
//                } catch (Exception e) {
//                }

                // start unity
                addUnityViewToBackground(unity);
                unity.resume();

                if (UnityPlayerManager.INSTANCE.getActiveRequests().get() == 0) {
                    Log.v("UnityView", "no active requests");
                    unity.pause();
                }

//                // restore window layout
//                if (!fullScreen) {
//                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                }
//                _isUnityReady = true;
//                callback.onReady();

                callback.onReady(unity);
            }
        });
    }

    public static void postMessage(String gameObject, String methodName, String message) {
        if (!UnityPlayerManager.INSTANCE.hasPlayer()) {
            return;
        }
        UnityPlayer.UnitySendMessage(gameObject, methodName, message);
    }

    /**
     * Invoked by unity C#
     */
    @SuppressWarnings("unused")
    public static void onUnityMessage(String message) {
        for (UnityEventListener listener : mUnityEventListeners) {
            try {
                listener.onMessage(message);
            } catch (Exception e) {
                Log.v("RNUnityView", "Error dispatching message to listener", e);
            }
        }
    }

    public static void addUnityEventListener(UnityEventListener listener) {
        mUnityEventListeners.add(listener);
    }

    public static void removeUnityEventListener(UnityEventListener listener) {
        mUnityEventListeners.remove(listener);
    }

    public static void addUnityViewToBackground(ManagedUnityPlayer unity) {
        if (unity == null) {
            return;
        }
        unity.removeFromParent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            unity.getPlayer().setZ(-1f);
        }
        final Activity activity = ((Activity) unity.getPlayer().getContext());
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(1, 1);
        activity.addContentView(unity.getPlayer(), layoutParams);
    }

    public static void addUnityViewToGroup(ViewGroup group, ManagedUnityPlayer unity) {
        if (unity == null) {
            return;
        }
        unity.removeFromParent();
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        group.addView(unity.getPlayer(), 0, layoutParams);
    }

    public interface CreateCallback {
        void onReady(ManagedUnityPlayer unityPlayer);
    }
}
