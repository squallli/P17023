package tw.com.regalscan.utils;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

import com.github.pwittchen.reactivenetwork.library.rx2.ConnectivityPredicate;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.jess.arms.utils.ArmsUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import tw.com.regalscan.R;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.wifi.WifiUtil;

/**
 * Created by tp00175 on 2017/10/21.
 */

public class NetworkStatusReceiver extends BroadcastReceiver {

    Disposable networkDisposable = null;

    WifiUtil mWifiUtil;

    boolean isProcessing = false;

    @Override
    public void onReceive(Context context, Intent intent) {

        WifiManager mWifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        Activity activity = ArmsUtils.obtainAppComponentFromContext(context).appManager().getCurrentActivity();

        mWifiUtil = new WifiUtil(context);

        if (FlightData.OnlineAuthorize && !FlightData.IFEConnectionStatus && WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            Parcelable parcelable = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (parcelable != null) {
                NetworkInfo networkInfo = (NetworkInfo)parcelable;
                NetworkInfo.State state = networkInfo.getState();
                boolean isConnected = state == NetworkInfo.State.CONNECTED;
                if (!isConnected && !isProcessing) {

                    isProcessing = true;

                    if (MessageBox.show("", "Wifi is disconnected!\r\nReconnect now?", activity, "Yes", "No")) {
                        networkDisposable = ReactiveNetwork.observeNetworkConnectivity(activity)
                            .subscribeOn(Schedulers.io())
                            .doOnSubscribe(disposable -> Cursor.Busy(activity.getString(R.string.Processing_Msg), activity))
                            .filter(ConnectivityPredicate.hasType(ConnectivityManager.TYPE_WIFI))
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally(Cursor::Normal)
                            .subscribe(isConnectedToInternet -> {
                                if (isConnectedToInternet.getState().equals(NetworkInfo.State.CONNECTED)) {

                                    while (!networkDisposable.isDisposed()) {
                                        networkDisposable.dispose();
                                    }

                                    MessageBox.show("", activity.getString(R.string.WiFi_reconnect_success), activity, "Ok");

                                    FlightData.OnlineAuthorize = true;
                                    isProcessing = false;
                                } else {

                                    while (mWifiManager.isWifiEnabled()) {
                                        mWifiManager.setWifiEnabled(false);
                                    }

                                    while (!mWifiManager.isWifiEnabled()) {
                                        mWifiManager.setWifiEnabled(true);
                                    }

                                    List<WifiConfiguration> wifiConfigurations = mWifiManager.getConfiguredNetworks();

                                    mWifiManager.enableNetwork(wifiConfigurations.get(0).networkId, true);
                                }
                            });
                    } else {
                        if (MessageBox.show("", activity.getString(R.string.POS_is_now_offline), activity, "Ok")) {
                            mWifiUtil.disconnect();
                            mWifiManager.setWifiEnabled(false);
                            FlightData.OnlineAuthorize = false;
                            isProcessing = false;
                        }
                    }
                }
            }
        }
    }
}
