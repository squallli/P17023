/*
  * Copyright 2017 JessYan
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package tw.com.regalscan.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ExpandableListView;

import com.jess.arms.utils.ArmsUtils;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import timber.log.Timber;
import tw.com.regalscan.BuildConfig;
import tw.com.regalscan.R;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.evaair.DamageActivity;
import tw.com.regalscan.evaair.RefundDFSActivity;
import tw.com.regalscan.evaair.basket.PayActivity;
import tw.com.regalscan.evaair.ife.*;
import tw.com.regalscan.evaground.*;
import tw.com.regalscan.evaground.mvp.ui.activity.OfflineDownloadActivity;
import tw.com.regalscan.utils.NetworkStatusReceiver;

/**
 * ================================================
 * Created by JessYan on 04/09/2017 17:14
 * Contact with jess.yan.effort@gmail.com
 * Follow me on https://github.com/JessYanCoding
 * ================================================
 */

public class ActivityLifecycleCallbacksImpl implements Application.ActivityLifecycleCallbacks {

    private PowerManager.WakeLock mWakeLock;

    private NetworkStatusReceiver mStatusReceiver;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Timber.w("%s - onActivityCreated", activity);
        if (BuildConfig.DEBUG) {
            //leakCanary内存泄露检查
            ArmsUtils.obtainAppComponentFromContext(activity.getApplication()).extras().put(RefWatcher.class.getName(), BuildConfig.USE_CANARY ? LeakCanary.install(activity.getApplication()) : RefWatcher.DISABLED);
        }

        if (FlightData.IFEConnectionStatus) {
            final PowerManager pm = (PowerManager)activity.getSystemService(Context.POWER_SERVICE);
            this.mWakeLock = pm != null ? pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag") : null;
            if (this.mWakeLock != null) {
                this.mWakeLock.acquire();
            }
        } else {
            if (mWakeLock != null) {
                if (mWakeLock.isHeld()) {
                    this.mWakeLock.release();
                }
            }
        }

        UnCatchHandler.addActivity(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Timber.w("%s - onActivityStarted", activity);

        //強制豎屏
        ArmsUtils.obtainAppComponentFromContext(activity.getApplication()).appManager().getTopActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (!activity.getIntent().getBooleanExtra("isInitToolbar", false)) {
            //由于加强框架的兼容性,故将 setContentView 放到 onActivityCreated 之后,onActivityStarted 之前执行
            //而 findViewById 必须在 Activity setContentView() 后才有效,所以将以下代码从之前的 onActivityCreated 中移动到 onActivityStarted 中执行
            activity.getIntent().putExtra("isInitToolbar", true);
            //这里全局给Activity设置toolbar和title,你想象力有多丰富,这里就有多强大,以前放到BaseActivity的操作都可以放到这里
            if (activity instanceof AppCompatActivity) {
                if (activity.findViewById(R.id.toolbar) != null) {
                    DrawerLayout drawerLayout = activity.findViewById(R.id.drawer_layout);
                    Toolbar toolbar = activity.findViewById(R.id.toolbar);
                    toolbar.setTitle("");

                    ((AppCompatActivity)activity).setSupportActionBar(toolbar);
                    ((AppCompatActivity)activity).getSupportActionBar().setDisplayShowTitleEnabled(false);

                    ExpandableListView expandableListView = activity.findViewById(R.id.expendlist);

                    if (expandableListView != null) {
                        if (activity instanceof MenuActivity || activity instanceof ECheckUpdateActivity
                            || activity instanceof DownloadActivity || activity instanceof UploadActivity
                            || activity instanceof ReportActivity || activity instanceof OfflineDownloadActivity) {
                            new NavigationDrawer(activity, activity.getApplicationContext(), drawerLayout, toolbar, expandableListView);
                        } else {
                            new tw.com.regalscan.component.NavigationDrawer(activity, activity.getApplicationContext(), drawerLayout, toolbar, expandableListView);
                        }
                    }
                }
            }
        }

        if (activity instanceof OnlineBasketActivity || activity instanceof OrderListActivity || activity instanceof OrderDetailActivity
            || activity instanceof ProcessingOrderListActivity || activity instanceof CrewCartActivity || activity instanceof PayActivity
            || activity instanceof DamageActivity || activity instanceof RefundDFSActivity) {
            if (!FlightData.IFEConnectionStatus && FlightData.OnlineAuthorize) {
                IntentFilter filter = new IntentFilter();
                filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                mStatusReceiver = new NetworkStatusReceiver();
                activity.registerReceiver(mStatusReceiver, filter);
                activity.getIntent().putExtra("isRegisterReceiver", true);
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Timber.w("%s - onActivityResumed", activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Timber.w("%s - onActivityPaused", activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Timber.w("%s - onActivityStopped", activity);

        if (mStatusReceiver != null && activity.getIntent().getBooleanExtra("isRegisterReceiver", false)) {
            activity.unregisterReceiver(mStatusReceiver);
            mStatusReceiver = null;
        }

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Timber.w("%s - onActivitySaveInstanceState", activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Timber.w("%s - onActivityDestroyed", activity);
        UnCatchHandler.removeActivity(activity);
    }
}