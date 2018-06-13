package tw.com.regalscan.evaground.mvp.ui.activity;

import java.lang.ref.WeakReference;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import butterknife.BindView;
import butterknife.OnClick;
import com.jess.arms.base.BaseActivity;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.DeviceUtils;
import com.jess.arms.utils.Preconditions;
import io.reactivex.disposables.Disposable;
import tw.com.regalscan.MainActivity;
import tw.com.regalscan.R;
import tw.com.regalscan.component.RFIDReaderService;
import tw.com.regalscan.evaground.MessageBox;
import tw.com.regalscan.evaground.di.component.DaggerLoginComponent;
import tw.com.regalscan.evaground.di.module.LoginModule;
import tw.com.regalscan.evaground.mvp.contract.LoginContract;
import tw.com.regalscan.evaground.mvp.presenter.LoginPresenter;
import tw.com.regalscan.utils.Cursor;


public class LoginActivity extends BaseActivity<LoginPresenter> implements LoginContract.View {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.titleText) RelativeLayout mTitleText;
    @BindView(R.id.titleLine) View mTitleLine;
    @BindView(R.id.spinner) Spinner mSpinner;
    @BindView(R.id.image01) ImageView mImage01;
    @BindView(R.id.txtCaId) EditText mTxtCaId;
    @BindView(R.id.row01) RelativeLayout mRow01;
    @BindView(R.id.image02) ImageView mImage02;
    @BindView(R.id.txtCpId) EditText mTxtCpId;
    @BindView(R.id.row02) RelativeLayout mRow02;
    @BindView(R.id.image03) ImageView mImage03;
    @BindView(R.id.txtPassword) EditText mTxtPassword;
    @BindView(R.id.row03) RelativeLayout mRow03;
    @BindView(R.id.iv_loginicon) ImageView mIvLoginIcon;
    @BindView(R.id.et_loginID) EditText mEtLoginID;
    @BindView(R.id.rl_groundID) RelativeLayout mRlGroundID;
    @BindView(R.id.iv_groundPsw) ImageView mIvGroundPsw;
    @BindView(R.id.et_groundPsw) EditText mEtGroundPsw;
    @BindView(R.id.rl_groundPsw) RelativeLayout mRlGroundPsw;
    @BindView(R.id.btnReturn) Button mBtnReturn;
    @BindView(R.id.line_01) ImageView mLine01;
    @BindView(R.id.btnReLogin) Button mBtnReLogin;
    @BindView(R.id.btnLogin) Button mBtnLogin;
    @BindView(R.id.rowBtn03) LinearLayout mRowBtn03;

    private final handler mHandler = new handler(this);

    private RFIDReaderService mRFIDReaderService = new RFIDReaderService(this, mHandler);

//    private WifiUtil mWifiUtil = new WifiUtil(this);

    private Disposable networkDisposable;

    @Override
    public void setupActivityComponent(AppComponent appComponent) {
        DaggerLoginComponent //如找不到该类,请编译一下项目
            .builder()
            .appComponent(appComponent)
            .loginModule(new LoginModule(this))
            .build()
            .inject(this);
    }

    @Override
    public int initView(Bundle savedInstanceState) {
        return R.layout.activity_open; //如果你不需要框架帮你设置 setContentView(id) 需要自行设置,请返回 0
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        mRlGroundID.setVisibility(View.VISIBLE);
        mRlGroundPsw.setVisibility(View.VISIBLE);
        mEtLoginID.setOnKeyListener(mPresenter.mOnKeyListener);
        mEtGroundPsw.setOnKeyListener(mPresenter.mOnKeyListener);

        mRow01.setVisibility(View.INVISIBLE);
        mRow02.setVisibility(View.INVISIBLE);
        mRow03.setVisibility(View.INVISIBLE);
        mSpinner.setVisibility(View.INVISIBLE);

        mBtnReLogin.setText(getString(R.string.Exit));
    }

    /**
     * 處理RFID讀取
     */
    private static class handler extends Handler {
        private final WeakReference<LoginActivity> mLoginActivityWeakReference;

        public handler(LoginActivity activity) {
            mLoginActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginActivity activity = mLoginActivityWeakReference.get();
            if (activity != null) {
                activity.mPresenter.handleRFID(msg);
            }
        }
    }

    @Override
    public void hideSoftKeyboard(View view) {
        DeviceUtils.hideSoftKeyboard(this, view);
    }

    @Override
    protected void onStart() {
//        networkDisposable = ReactiveNetwork.observeNetworkConnectivity(this)
//            .subscribeOn(Schedulers.io())
//            .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), this))
//            .filter(ConnectivityPredicate.hasType(ConnectivityManager.TYPE_WIFI))
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe(isConnectedToInternet -> {
//                if (isConnectedToInternet.getState().equals(NetworkInfo.State.CONNECTED)) {
//
//                    while (!networkDisposable.isDisposed()) {
//                        networkDisposable.dispose();
//                    }
//
//                } else {
//                    Executor executor = Executors.newCachedThreadPool();
//                    executor.execute(() -> {
//                        try {
//                            mWifiUtil.connect("", "");
//                        } catch (WifiConnector.WifiException e) {
//                            runOnUiThread(() -> {
//                                Cursor.Normal();
//                                MessageBox.show("", "Wifi connect is close!\r\nError message: " + e.toString(), this, "Ok");
//                            });
//                        }
//                    });
//                }
//            });

        ArmsUtils.obtainAppComponentFromContext(this).extras().put("isOnline", false);

        openRFID();

        super.onStart();
    }

    @Override
    public void showLoading() {
        Cursor.Busy(getString(R.string.Processing_Msg), this);
    }

    @Override
    public void hideLoading() {
        Cursor.Normal();
    }

    @Override
    public void showMessage(@NonNull String message) {
        Preconditions.checkNotNull(message);
        MessageBox.show("", message, this, "Ok");
        openRFID();
    }

    @Override
    public void launchActivity(@NonNull Intent intent) {
        Preconditions.checkNotNull(intent);
        ArmsUtils.startActivity(intent);
    }

    @Override
    public void killMyself() {
        mRFIDReaderService.stop();
        finish();
    }

    /**
     * 取消返回鍵功能
     */
    @Override
    public void onBackPressed() {
    }

    /**
     * 點擊空白處隱藏鍵盤
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.getCurrentFocus() != null) {
            DeviceUtils.hideSoftKeyboard(this, this.getWindow().getCurrentFocus());
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void setUserID(String userID) {
        mEtLoginID.setText(userID);
    }

    @Override
    public void openRFID() {
        mRFIDReaderService.start();
    }

    @Override
    public void closeRFID() {
        mRFIDReaderService.stop();
    }

    @OnClick(R.id.btnLogin)
    void onLoginClick() {
        String userID = mEtLoginID.getText().toString();
        String password = mEtGroundPsw.getText().toString();
        mPresenter.validCredential(userID, password);
    }

    @OnClick(R.id.btnReLogin)
    void onReturnClick() {
        ArmsUtils.startActivity(MainActivity.class);
        killMyself();
    }
}
