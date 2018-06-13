package tw.com.regalscan.evaair.ife;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.github.pwittchen.reactivenetwork.library.rx2.ConnectivityPredicate;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.jess.arms.utils.ArmsUtils;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import tw.com.regalscan.BuildConfig;
import tw.com.regalscan.R;
import tw.com.regalscan.activities.CPCheckActivity;
import tw.com.regalscan.app.entity.Setting;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.IMsgBoxOnClick;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.evaair.ife.entity.Catalog;
import tw.com.regalscan.utils.Cursor;
import tw.com.regalscan.utils.Tools;
import tw.com.regalscan.wifi.WifiConnector;
import tw.com.regalscan.wifi.WifiUtil;

public class IFEEX3Activity extends AppCompatActivity {

    private static final String TAG = IFEEX3Activity.class.getSimpleName();

    private Context mContext;
    private Activity mActivity;
    private Switch switchTransaction, switchSync, switchInit;
    private IFEDBFunction mIFEDBFunction;
    private Disposable networkDisposable;
    private IFEFunction mIFEFunction;
    private WifiUtil mWifiUtil;
    private Setting mSetting;
    private int type;

    public IFEEX3Activity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ife_ex3);

        type = getIntent().getExtras().getInt("type");

        init();

        if (!mIFEDBFunction.chkPushInventory(FlightData.SecSeq)) {
            MessageBox.show("", "Don't forget to check the Inventory!!", mContext, "Ok");
        }
    }

    private void init() {
        mContext = this;
        mActivity = this;

        mIFEDBFunction = new IFEDBFunction(mContext, FlightData.SecSeq);
        mWifiUtil = new WifiUtil(mContext);
        mSetting = (Setting) ArmsUtils.obtainAppComponentFromContext(mContext).extras().get("Setting");

        //btn
        Button btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {

//            if (!FlightData.IFEPushInitialize) {
//                MessageBox.show("", getString(R.string.Please_Initialize), mContext, "Ok");
//            }

            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();

            if (!ActivityManager.isActivityInStock("IFEActivity01")) {
                Intent mIntent02 = new Intent(mActivity, IFEActivity01.class);
                mActivity.startActivity(mIntent02);
            }
        });

        //啟動或關閉POS Wifi以及IFE連線
        switchSync = findViewById(R.id.switchSync);

        if (FlightData.IFEConnectionStatus) {
            switchSync.setChecked(true);
        }

        switchSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                connectWithWifi(0);
//                createWiFiProfile();
            } else {
                // 關閉 wifi 與 ife 連線
                FlightData.IFEConnectionStatus = false;
                FlightData.OnlineAuthorize = false;
                switchTransaction.setChecked(false);
                mWifiUtil.disconnect();
            }
        });

        //啟動或關閉線上取授權功能，當Sync with IFE開啟時，此功能預設啟動
        switchTransaction = findViewById(R.id.switchOpenTransaction);
        if (FlightData.OnlineAuthorize) {
            switchTransaction.setChecked(true);
        }

        switchTransaction.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // true if the switch is in the On position
            if (isChecked) {
                if (FlightData.IFEConnectionStatus) {
                    //啟動線上取授權
                    FlightData.OnlineAuthorize = true;
                } else {
                    connectWithWifi(1);
                }
            } else {
                if (MessageBox.show("", "Offline Transaction?", mContext, "Yes", "No")) {
                    //關閉線上取授權
                    FlightData.OnlineAuthorize = false;
                    if (!FlightData.IFEConnectionStatus) {
                        mWifiUtil.disconnect();
                    }
                } else {
                    switchTransaction.setChecked(true);
                }
            }
        });

        // 同步庫存到IFE動作，每個航段僅可同步一次，且須CP確認後方可開啟，該航段同步後，此按鈕反灰
        switchInit = findViewById(R.id.switchInit);

        if (mIFEDBFunction.chkPushInventory(FlightData.SecSeq)) {
            switchInit.setChecked(true);
            switchInit.setEnabled(false);
            FlightData.IFEPushInitialize = true;
        }

        switchInit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (FlightData.IFEConnectionStatus) {

                    Bundle bundle = new Bundle();
                    bundle.putString("fromWhere", "IFEEX3Activity");
                    Intent intent = new Intent(mActivity, CPCheckActivity.class);
                    intent.putExtras(bundle);
                    mActivity.startActivityForResult(intent, 123);

                } else {
                    MessageBox.show("", getString(R.string.Please_Sync_IFE), mContext, "Return");
                    switchInit.setChecked(false);
                }
            }
        });

        TextView flightInfo = findViewById(R.id.txtFlightInfo);
        TextView textView = findViewById(R.id.tv_Sync);

        switch (type) {
            // 77A/77B
            case 0:
                flightInfo.setText("77A/77B");
                textView.setTextColor(getResources().getColor(R.color.colorGray));
                switchSync.setEnabled(false);
                break;
            // 787
            case 1:
                flightInfo.setText("787");
                break;
        }

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            if (mIFEFunction == null) {
                mIFEFunction = new IFEFunction(mContext);
            }

            // 同步庫存
            initialize();

        } else {
            switchInit.setChecked(false);
        }
    }

    /**
     * 設定wifi連線
     *
     * @param type 0 - IFE連線 / 1 - 取授權連線
     */
    private void connectWithWifi(final int type) {
        networkDisposable = ReactiveNetwork.observeNetworkConnectivity(mContext)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                .filter(ConnectivityPredicate.hasType(ConnectivityManager.TYPE_WIFI))
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(Cursor::Normal)
                .subscribe(isConnectedToInternet -> {
                    if (isConnectedToInternet.getState().equals(NetworkInfo.State.CONNECTED)) {

                        while (!networkDisposable.isDisposed()) {
                            networkDisposable.dispose();
                        }

                        if (type == 0) {
                            if (!mIFEDBFunction.chkPushInventory(FlightData.SecSeq)) {
                                if (MessageBox.show("", "For long haul flight only.\r\nConnect to IFE?", mContext, "Yes", "No")) {
                                    FlightData.OnlineAuthorize = true;
                                    FlightData.IFEConnectionStatus = true;
                                    switchTransaction.setChecked(true);

                                    if (!mIFEDBFunction.chkPushInventory(FlightData.SecSeq)) {
                                        switchInit.setChecked(true);
                                    }

                                } else {
                                    switchSync.setChecked(false);
                                }
                            } else {
                                FlightData.OnlineAuthorize = true;
                                FlightData.IFEConnectionStatus = true;
                                switchTransaction.setChecked(true);

                                if (!mIFEDBFunction.chkPushInventory(FlightData.SecSeq)) {
                                    switchInit.setChecked(true);
                                }
                            }
                        } else {
                            FlightData.OnlineAuthorize = true;
                            switchTransaction.setChecked(true);
                        }
                    } else {
                        Executor executor = Executors.newCachedThreadPool();
                        executor.execute(() -> {
                            try {
                                if (BuildConfig.DEBUG) {
                                    mWifiUtil.connect("InFlight Emulator Stick EVA", "vqNC88r14", 2);
                                } else {
                                    switch (this.type) {
                                        case 0:
                                            mWifiUtil.connect(mSetting.getIFESSID(), mSetting.getIFEKEY(), 0);
                                            break;
                                        case 1:
                                            mWifiUtil.connect(mSetting.getIFESSID(), null, 1);
                                            break;
                                    }
                                }
                            } catch (WifiConnector.WifiException e) {
                                runOnUiThread(() -> {
                                    Cursor.Normal();
                                    if (MessageBox.show("", "Wifi connect is close, you can retry.", mContext, "Yes", "No")) {
                                        if (type == 0) {
                                            switchSync.setChecked(false);
                                            switchSync.setChecked(true);
                                        } else {
                                            switchTransaction.setChecked(false);
                                            switchTransaction.setChecked(true);
                                        }
                                    } else {
                                        if (type == 0) {
                                            switchSync.setChecked(false);
                                        } else {
                                            switchTransaction.setChecked(false);
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
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
                result = true;
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                result = true;
                break;
            default:
                result = super.onKeyDown(keyCode, event);
                break;
        }

        return result;
    }

    private void initialize() {
        mIFEFunction.getEnableItem()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                .observeOn(Schedulers.io())
                .flatMap(ifeReturnData -> {
                    if (ifeReturnData.isSuccess()) {
                        if (!FlightData.IFEGetEnabledItemCode) {
                            if (mIFEDBFunction.UpdateEnableItem((List<Catalog>) ifeReturnData.getData())) {

                                FlightData.IFEGetEnabledItemCode = true;
                                return mIFEFunction.pushInventory(mIFEDBFunction.getEnableItem());
//                            return Observable.just(ifeReturnData);
                            } else {
                                ifeReturnData.setSuccess(false);
                                ifeReturnData.setErrMsg("Please check the catalog!");
                                return Observable.just(ifeReturnData);
                            }
                        } else {
                            return mIFEFunction.pushInventory(mIFEDBFunction.getEnableItem());
//                        return Observable.just(ifeReturnData);
                        }
                    } else {
                        return Observable.just(ifeReturnData);
                    }
                }).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate(Cursor::Normal)
                .subscribe(ifeReturnData -> {
                    if (ifeReturnData.isSuccess()) {
                        mIFEDBFunction.inventoryPushed();
                        MessageBox.drawerShow("", "POS inventory sync with IFE finished.", mContext, "Ok", new IMsgBoxOnClick() {
                            @Override
                            public void onYesClick() {
                                FlightData.IFEPushInitialize = true;

                                //反灰不可按
                                switchInit.setEnabled(false);
                            }

                            @Override
                            public void onNoClick() {

                            }
                        });
                    } else {
                        if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                            if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                                ArmsUtils.startActivity(IFEActivity01.class);
                                finish();
                            }
                        }
                        switchInit.setChecked(false);
                    }
                });
    }
}