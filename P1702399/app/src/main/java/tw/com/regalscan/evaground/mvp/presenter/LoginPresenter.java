package tw.com.regalscan.evaground.mvp.presenter;

import java.io.File;
import javax.inject.Inject;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.http.imageloader.ImageLoader;
import com.jess.arms.integration.AppManager;
import com.jess.arms.mvp.BasePresenter;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.RxLifecycleUtils;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import me.jessyan.rxerrorhandler.handler.ErrorHandleSubscriber;
import timber.log.Timber;
import tw.com.regalscan.BuildConfig;
import tw.com.regalscan.R;
import tw.com.regalscan.app.CacheDataTags;
import tw.com.regalscan.app.entity.UserInfo;
import tw.com.regalscan.component.RFIDReaderService;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.evaground.MessageBox;
import tw.com.regalscan.evaground.mvp.contract.LoginContract;
import tw.com.regalscan.evaground.mvp.ui.activity.MenuActivity;


@ActivityScope
public class LoginPresenter extends BasePresenter<LoginContract.Model, LoginContract.View> {

    public static final String TAG = LoginPresenter.class.getSimpleName();

    private RxErrorHandler mErrorHandler;
    private Application mApplication;
    private ImageLoader mImageLoader;
    private AppManager mAppManager;

    @Inject
    LoginPresenter(LoginContract.Model model, LoginContract.View rootView
        , RxErrorHandler handler, Application application
        , ImageLoader imageLoader, AppManager appManager) {
        super(model, rootView);
        this.mErrorHandler = handler;
        this.mApplication = application;
        this.mImageLoader = imageLoader;
        this.mAppManager = appManager;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mErrorHandler = null;
        this.mAppManager = null;
        this.mImageLoader = null;
        this.mApplication = null;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void onCreate() {
        setSecSeq();
    }

    public View.OnKeyListener mOnKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int i, KeyEvent keyEvent) {
            if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                switch (view.getId()) {
                    case R.id.et_loginID:
                        if (((EditText)view).getText().toString().equals("")) {
                            mRootView.hideSoftKeyboard(view);
                            MessageBox.show("", "Please input user ID.", mAppManager.getCurrentActivity(), "Return");
                            return true;
                        }
                        break;
                    case R.id.et_groundPsw:
                        if (((EditText)view).getText().toString().equals("")) {
                            mRootView.hideSoftKeyboard(view);
                            MessageBox.show("", "Please input user password.", mAppManager.getCurrentActivity(), "Return");
                            return true;
                        }
                        break;
                }
            }
            return false;
        }
    };

    public void handleRFID(Message msg) {
        switch (msg.what) {
            case RFIDReaderService.MSG_SHOW_BLOCK_DATA:
                String UID = msg.getData().getString(RFIDReaderService.CARD_UID);
                //員工證號
                String BlockData = msg.getData().getString(RFIDReaderService.CARD_BLOCK_DATA);
                //"EVA" or "EGAS", UID和BlockData為null的話就是ErrorString
                String EMPLOYEE_TYPE = msg.getData().getString(RFIDReaderService.EMPLOYEE_TYPE);

                if (UID != null && BlockData != null) {
                    mRootView.setUserID(BlockData);
                    mRootView.closeRFID();
                    login(BlockData, "", "Y", EMPLOYEE_TYPE);
                } else {
                    // EMPLOYEE_TYPE為 Error String
                    mRootView.showMessage(EMPLOYEE_TYPE);
                    mRootView.openRFID();
                }
                break;

            case RFIDReaderService.MSG_OPEN_FAILED:
                mRootView.showMessage("Please try again!");
                mRootView.openRFID();
                break;
        }
    }

    /**
     * 驗證帳號密碼格式
     *
     * @param userID   帳號
     * @param password 密碼
     */
    public void validCredential(String userID, String password) {
        if (userID.equals("")) {
            mRootView.showMessage("Please input user ID!");
            return;
        }

        if (password.equals("")) {
            mRootView.showMessage("Please input password!");
            return;
        } else if (((boolean)ArmsUtils.obtainAppComponentFromContext(mAppManager.getCurrentActivity()).extras().get("isOnline")) && (password.equals("995996") || password.equals("123"))) {
            mRootView.showMessage("Cannot use Offline mode when Wifi is connected!");
            return;
        }

        switch (password) {
            case "995996":
                login(userID, password, "N", "EVA");
                break;
            case "123":
                login(userID, password, "N", "EGAS");
                break;
            default:
                login(userID, password, "Y", "");
                break;
        }
    }

    /**
     * 驗證帳號密碼正確性
     *
     * @param userId   帳號
     * @param password 密碼
     * @param rfidMark 是否使用卡片登入
     * @param company  公司類別 EVA/EGAS
     */
    private void login(String userId, String password, String rfidMark, String company) {

        if (password.equals("995996")) {

            UserInfo userInfo = new UserInfo();
            userInfo.setCOMPANY("EVA");

//            String jsonStr = "{\"IS_VALID\":\"Y\",\"MSG\":null,\"COMPANY\":\"EVA\",\"DEPARTMENT\":\"Y103\",\"EMPLOYEE_ID\":\"E70977\",\"NEWS_LIST\":[{\"TITLE\":\"手提箱堆疊不可超高\",\"CONTENT\":\"手提箱堆疊不可超高\"},{\"TITLE\":\"TEST\",\"CONTENT\":\"POS - TEST\"}],\"FULLNAME\":\"賴信榕\",\"VERSION\":\"v1.0.1\",\"DOWNLOAD_URL\":\"https://qasmasbt01.evaair.com\",\"FORCE_UPDATE\":\"Y\",\"FTP_IP\":\"QASMASBT01\",\"SYSTEM_TIME\":\"20170531120000\"}";
//            UserInfo userInfo = JSON.parseObject(jsonStr, UserInfo.class);

            Intent intent = new Intent(mAppManager.getCurrentActivity(), MenuActivity.class);
            intent.putExtra("UserInfo", userInfo);

            mRootView.launchActivity(intent);
            mRootView.killMyself();
            return;
        }

        if (password.equals("123")) {
            UserInfo userInfo = new UserInfo();
            userInfo.setCOMPANY("EGAS");

            Intent intent = new Intent(mAppManager.getCurrentActivity(), MenuActivity.class);
            intent.putExtra("UserInfo", userInfo);

            mRootView.launchActivity(intent);
            mRootView.killMyself();
            return;
        }

        mModel.login(userId, password, rfidMark, company)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(disposable -> mRootView.showLoading())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally(() -> mRootView.hideLoading())
            .compose(RxLifecycleUtils.bindToLifecycle(mRootView))
            .subscribe(new ErrorHandleSubscriber<UserInfo>(mErrorHandler) {
                @Override
                public void onNext(UserInfo userInfo) {
                    if (userInfo.isValid()) {
                        if (checkAppVersion(userInfo.getVERSION(), userInfo.getFORCE_UPDATE())) {
                            downLoadAPK(userInfo.getDOWNLOAD_URL());
                        } else {
                            Intent intent = new Intent(mAppManager.getCurrentActivity(), MenuActivity.class);
                            intent.putExtra("UserInfo", userInfo);
                            mRootView.launchActivity(intent);
                        }
                    } else {
                        mRootView.showMessage(userInfo.getMSG());
                    }
                }

                @Override
                public void onComplete() {
                    super.onComplete();
                }

                @Override
                public void onError(Throwable t) {
                    super.onError(t);
                    Timber.tag(TAG).w(t.getMessage());
                    mRootView.showMessage(t.getMessage());
                }
            });
    }

    /**
     * 檢查APP是否需要更新
     *
     * @param version       APP版本
     * @param isForceUpdate 是否強制更新
     * @return true-更新/false-不更新
     */
    private boolean checkAppVersion(String version, String isForceUpdate) {
        if (!("V" + BuildConfig.VERSION_NAME).equals(version)) {
            if (isForceUpdate.equals("Y")) {
                return MessageBox.show("", mAppManager.getCurrentActivity().getString(R.string.update_msg), mAppManager.getCurrentActivity(), "Ok");
            } else {
                return MessageBox.show("", mAppManager.getCurrentActivity().getString(R.string.update_msg), mAppManager.getCurrentActivity(), "Yes", "No");
            }
        }
        return false;
    }

    /**
     * 下載APK
     *
     * @param fileUrl APK下載網址
     */
    private void downLoadAPK(String fileUrl) {
        mModel.downloadApk(fileUrl)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(disposable -> mRootView.showLoading())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally(() -> mRootView.hideLoading())
            .compose(RxLifecycleUtils.bindToLifecycle(mRootView))
            .subscribe(new ErrorHandleSubscriber<File>(mErrorHandler) {
                @Override
                public void onNext(File file) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mRootView.launchActivity(intent);
                }

                @Override
                public void onComplete() {
                    super.onComplete();
                }

                @Override
                public void onError(Throwable t) {
                    super.onError(t);
                    mRootView.showMessage(t.getMessage());
                }
            });

    }

    /**
     * 設定航段
     */
    private void setSecSeq() {

        DBQuery.CurrentOpenFlightPack currentFlightPack = DBQuery.getCurrentOpenFlightList(mAppManager.getCurrentActivity(), new StringBuilder());

        if ((currentFlightPack != null ? currentFlightPack.openFlights.length : 0) > 0) {
            for (DBQuery.CurrentOpenFlight openFlight : currentFlightPack.openFlights) {
                if (openFlight.Status.equals("Closed")) {
                    ArmsUtils.obtainAppComponentFromContext(mAppManager.getCurrentActivity()).extras().put(CacheDataTags.SecSeq, openFlight.SecSeq);
                    return;
                }
            }

            ArmsUtils.obtainAppComponentFromContext(mAppManager.getCurrentActivity()).extras().put(CacheDataTags.SecSeq, 0);
        } else {
            ArmsUtils.obtainAppComponentFromContext(mAppManager.getCurrentActivity()).extras().put(CacheDataTags.SecSeq, 0);
        }
    }
}
