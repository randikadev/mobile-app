package com.buildbox;

import java.lang.ref.WeakReference;
import android.app.Activity;

public class AdIntegrator {
    public static native boolean rewardedVideoDidEnd();

    private static WeakReference<Activity> activity;

    public static void initBridge(Activity act){
        activity = new WeakReference<>(act);
    }

    public static void initAds(){

    }

    public static void showBanner(){

    }

    public static void hideBanner(){

    }

    public static boolean isBannerVisible(){
        return true;
    }

    public static boolean isRewardedVideoAvialable(){
        return true;
    }

    public static void showInterstitial(){

    }

    public static void showRewardedVideo(){
        rewardedVideoDidEnd();
    }

    public static void buttonActivated(){

    }
    
    public static boolean buttonVisible(){
        return true;
    }
}
