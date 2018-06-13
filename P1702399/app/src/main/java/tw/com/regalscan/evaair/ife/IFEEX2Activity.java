package tw.com.regalscan.evaair.ife;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.github.pwittchen.reactivenetwork.library.rx2.ConnectivityPredicate;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.jess.arms.utils.ArmsUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import tw.com.regalscan.R;
import tw.com.regalscan.app.entity.Setting;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.utils.Cursor;
import tw.com.regalscan.utils.Tools;
import tw.com.regalscan.wifi.WifiConnector;
import tw.com.regalscan.wifi.WifiUtil;

public class IFEEX2Activity extends AppCompatActivity {

    public Context mContext;
    public Activity mActivity;
    private Switch switchTransaction;
    private IFEDBFunction mIFEDBFunction;
    private Disposable networkDisposable;
    private WifiUtil mWifiUtil;
    private Setting mSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ife_ex2);

        init();
    }

    private void init() {
        mContext = this;
        mActivity = this;

        mIFEDBFunction = new IFEDBFunction(mContext, FlightData.SecSeq);
        mWifiUtil = new WifiUtil(mContext);
        mSetting = (Setting)ArmsUtils.obtainAppComponentFromContext(mContext).extras().get("Setting");

        //btn
        Button btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();

            if (!ActivityManager.isActivityInStock("IFEActivity01")) {
                Intent mIntent02 = new Intent(mActivity, IFEActivity01.class);
                mActivity.startActivity(mIntent02);
            }
        });

        switchTransaction = findViewById(R.id.switchOpenTransaction);

        if (FlightData.OnlineAuthorize) {
            switchTransaction.setChecked(true);
        }

        switchTransaction.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // true if the switch is in the On position
            if (isChecked) {
//                networkDisposable = ReactiveNetwork.observeNetworkConnectivity(getApplicationContext())
//                    .subscribeOn(Schedulers.io())
//                    .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
//                    .filter(ConnectivityPredicate.hasType(ConnectivityManager.TYPE_WIFI))
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .doFinally(Cursor::Normal)
//                    .subscribe(isConnectedToInternet -> {
//                        if (isConnectedToInternet.getState().equals(State.CONNECTED)) {
//                            while (!networkDisposable.isDisposed()) {
//                                networkDisposable.dispose();
//                            }
                            FlightData.OnlineAuthorize = true;
//                        } else {
//                            Executor executor = Executors.newCachedThreadPool();
//                            executor.execute(() -> {
//                                try {
//                                    mWifiUtil.connect(mSetting.getIFESSID(), mSetting.getIFEKEY(), 0);
//                                } catch (WifiConnector.WifiException e) {
//                                    runOnUiThread(() -> {
//                                        Cursor.Normal();
//                                        if (MessageBox.show("", "Wifi connect is close, you can retry.", mContext, "Yes", "No")) {
//                                            switchTransaction.setChecked(false);
//                                            switchTransaction.setChecked(true);
//                                        } else {
//                                            switchTransaction.setChecked(false);
//                                        }
//                                    });
//                                }
//                            });
//                        }
//                    });
            } else {
                if (MessageBox.show("", "Offline Transaction?", mContext, "Yes", "No")) {
                    //關閉線上取授權
                    FlightData.OnlineAuthorize = false;
//                    if (!FlightData.IFEConnectionStatus) {
//                        mWifiUtil.disconnect();
//                    }
                } else {
                    switchTransaction.setChecked(true);
                }
            }
        });
        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }

    //點空白處自動隱藏鍵盤
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View view = getCurrentFocus();
                Tools.hideSoftKeyboard(ev, view, this);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    //鎖返回
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result;

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                result = false;
                break;
            case KeyEvent.KEYCODE_MENU:
                result = false;
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                result = false;
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                result = false;
                break;
            default:
                result = super.onKeyDown(keyCode, event);
                break;
        }

        return result;
    }
}