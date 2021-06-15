package com.secrethq.ads;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class PTAdAdMobBridge {
	private static final String TAG = "PTAdAdMobBridge";
    private static WeakReference<Activity> activity;

	private static AdView banner;
	private static InterstitialAd interstitial;
	private static RewardedAd rewardedVideo;

    private static boolean userConsent;
	
	private static native String appId();
	private static native String bannerId();
	private static native String interstitialId();
	private static native String rewardedVideoId();

    private static native void bannerDidFail();
    private static native void interstitialDidFail();
    private static native void rewardedVideoDidEnd();
    
    private static boolean isBannerScheduledForShow = false;
    private static boolean isInterstitialScheduledForShow = false;
    private static boolean isRewardedVideoLoaded = false;

    public static void initBridge(Activity act){
        Log.v(TAG, "PTAdAdMobBridge  -- INIT");
        activity = new WeakReference<>(act);

        //TODO: Set up consent stuff
        // SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.get());
        // userConsent = sharedPreferences.getBoolean(ConsentHelper.getConsentKey(sdkId), false);

        PTAdAdMobBridge.initBanner();
        PTAdAdMobBridge.initInterstitial();
        PTAdAdMobBridge.initRewardedVideo();
    }

    public static void setUserConsent(boolean consentGiven) {
            userConsent = consentGiven;
    }

    public static void initBanner(){
        Log.v(TAG, "PTAdAdMobBridge  -- initBanner");
        Activity activityRef = activity.get();

        activityRef.runOnUiThread( new Runnable() {
            public void run() {
                if(banner == null) {
                    banner = new AdView(activityRef);
                    banner.setAdUnitId(bannerId());
                    banner.setAdSize(AdSize.SMART_BANNER);
                    banner.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            Log.d(TAG, "Banner - onAdLoaded");
                            //TODO: Setup event tracking
                            // bannerLoaded();
                        }

                        @Override
                        public void onAdFailedToLoad(LoadAdError error) {
                            Log.d(TAG, "Banner - onAdFailedToLoad: " + error);
                            banner = null;
                            //TODO: Setup event tracking
                            // bannerFailed();
                        }

                        @Override
                        public void onAdImpression() {
                            Log.d(TAG, "Banner - onAdImpression");
                            //TODO: Setup Impressions
                            //AdIntegratorManager.bannerImpression(adNetworkId);
                        }

                        @Override
                        public void onAdOpened() {
                            Log.d(TAG, "Banner - onAdOpened");
                        }

                        @Override
                        public void onAdClicked() {
                            Log.d(TAG, "Banner - onAdClicked");
                        }

                        @Override
                        public void onAdClosed() {
                            Log.d(TAG, "Banner - onAdClosed");
                        }
                    });

                    FrameLayout frameLayout = (FrameLayout)activity.get().findViewById(android.R.id.content);
                    RelativeLayout bannerContainer = new RelativeLayout(activityRef);
                    frameLayout.addView(bannerContainer);
                    
                    RelativeLayout.LayoutParams bannerLayoutParams = new RelativeLayout.LayoutParams(
                            AdView.LayoutParams.WRAP_CONTENT,
                            AdView.LayoutParams.WRAP_CONTENT);
                    bannerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    bannerLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

                    banner.setLayoutParams(bannerLayoutParams);
                    bannerContainer.addView(banner);
                    banner.setVisibility(View.GONE);
                }

                banner.loadAd(getAdRequest());
            }
        });
    }

    public static void showBannerAd() {
        Log.d(TAG, "showBanner");

        if (banner != null) {
            activity.get().runOnUiThread( new Runnable() {
                public void run() {
                    banner.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public static void hideBannerAd() {
        Log.d(TAG, "hideBanner");

        if (banner != null) {
            activity.get().runOnUiThread( new Runnable() {
                public void run() {
                    banner.setVisibility(View.GONE);
                }
            });
        }
    }

    public boolean isBannerVisible() {
        return banner != null && banner.getVisibility() == View.VISIBLE;
    }

    public static void initInterstitial() {
        Log.v(TAG, "PTAdAdMobBridge  -- initInterstitial");
        Activity activityRef = activity.get();

        activityRef.runOnUiThread( new Runnable() {
            public void run() {

                if(interstitial != null){
                    return;
                }

                InterstitialAd.load(activity.get(), interstitialId(), getAdRequest(), new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        Log.d(TAG, "Interstitial - onAdLoaded");

                        interstitial = interstitialAd;
                        interstitial.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError error) {
                                Log.d(TAG, "Interstitial - onAdFailedToShowFullScreenContent: " + error);
                                interstitial = null;
                                //TODO: Implement tracking for events
                                // interstitialClosed();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                Log.d(TAG, "Interstitial - onAdShowedFullScreenContent");
                                //TODO: Implement impressions
                                //AdIntegratorManager.interstitialImpression(adNetworkId);
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Interstitial - onAdDismissedFullScreenContent");
                                interstitial = null;
                                //TODO: Implement tracking for events
                                // interstitialClosed();
                            }
                        });

                        //TODO: Implement tracking for events
                        // interstitialLoaded();
                    }

                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        Log.d(TAG, "Interstitial - onAdFailedToLoad: " + error);
                        interstitial = null;
                        //TODO: Implement tracking for events
                        // interstitialFailed();
                    }
                });
            }
        });
    }

    //TODO: Refactor to showInterstitial
    public static void showFullScreen(){
        Log.v(TAG, "showInterstitial");
        Activity activityRef = activity.get();

        if(interstitial != null){
            activityRef.runOnUiThread( new Runnable() {
                public void run() {
                    interstitial.show(activityRef);
                }
            });
        }
    }

    public static void initRewardedVideo() {
        Log.v(TAG, "PTAdAdMobBridge  -- initRewardedVideo");
        Activity activityRef = activity.get();

        activityRef.runOnUiThread( new Runnable() {
            public void run() {
                if (rewardedVideo == null) {
                    RewardedAd.load(activityRef, rewardedVideoId(), getAdRequest(), new RewardedAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                            Log.d(TAG, "Rewarded - onAdLoaded");

                            rewardedVideo = rewardedAd;
                            rewardedVideo.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdFailedToShowFullScreenContent(AdError error) {
                                    Log.d(TAG, "Rewarded - onAdFailedToShowFullScreenContent: " + error);
                                    rewardedVideo = null;
                                    //TODO: Implement tracking for events
                                    // rewardedVideoDidEnd(false);
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    Log.d(TAG, "Rewarded - onAdShowedFullScreenContent");
                                    //TODO: Implement tracking for events
                                    // AdIntegratorManager.rewardedVideoImpression(adNetworkId);
                                }

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    Log.d(TAG, "Rewarded - onAdDismissedFullScreenContent");
                                    rewardedVideo = null;
                                    //TODO: Implement tracking for events
                                    // rewardedVideoDidEnd(true);
                                }
                            });

                            //TODO: Implement tracking for events
                            // rewardedVideoLoaded();
                        }

                        public void onAdFailedToLoad(@NonNull LoadAdError error) {
                            Log.d(TAG, "Rewarded - onAdFailedToLoad: " + error);
                            rewardedVideo = null;
                            //TODO: Implement tracking for events
                            // rewardedVideoFailed();
                        }
                    });
                }
            }
        });
    }

    public static boolean isRewardedVideoAvialable() {
        return rewardedVideo != null;
    }

    public static void showRewardedVideo() {
        Log.v(TAG, "showRewardedVideo");
        Activity activityRef = activity.get();

        if(rewardedVideo != null) {
            activityRef.runOnUiThread(new Runnable() {
                public void run() {
                    rewardedVideo.show(activityRef, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            Log.d(TAG, "Rewarded - onUserEarnedReward");
                            //TODO: Implement tracking for events
                            // rewardedVideoDidReward(true);
                        }
                    });
                }
            });
        }
    }

    public boolean isRewardedVideoAvailable() {
        return rewardedVideo != null;
    }

    private static AdRequest getAdRequest() {
        if (userConsent) {
            return new AdRequest.Builder().build();
        }
        else {
            Bundle extras = new Bundle();
            extras.putString("npa", "1");
            return new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();
        }
    }
}
