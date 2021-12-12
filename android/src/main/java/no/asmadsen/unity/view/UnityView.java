package no.asmadsen.unity.view;

import android.content.Context;
import android.content.res.Configuration;
import android.widget.FrameLayout;

public class UnityView extends FrameLayout {

    private ManagedUnityPlayer unity;

    protected UnityView(Context context) {
        super(context);
    }

    public void setUnityPlayer(ManagedUnityPlayer player) {
        this.unity = player;
        UnityUtils.addUnityViewToGroup(this, player);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (unity != null) {
            unity.getPlayer().windowFocusChanged(hasWindowFocus);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (unity != null) {
            unity.getPlayer().configurationChanged(newConfig);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (unity != null) {
            UnityUtils.addUnityViewToBackground(unity);
        }
        super.onDetachedFromWindow();
    }
}
