package tw.com.regalscan.evaground;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.device.DeviceManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.github.pwittchen.reactivenetwork.library.rx2.ConnectivityPredicate;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.jess.arms.utils.ArmsUtils;
import com.regalscan.sqlitelibrary.TSQL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import tw.com.regalscan.BuildConfig;
import tw.com.regalscan.HttpRequest.CallBack;
import tw.com.regalscan.HttpRequest.Http_Post;
import tw.com.regalscan.HttpRequest.UrlConfig;
import tw.com.regalscan.MainActivity;
import tw.com.regalscan.R;
import tw.com.regalscan.app.entity.Setting;
import tw.com.regalscan.component.FlightInfoManager;
import tw.com.regalscan.component.RFIDReaderService;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.PublicFunctions;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.evaground.Models.NewsInfo;
import tw.com.regalscan.evaground.Models.RtnObject;
import tw.com.regalscan.utils.Cursor;
import tw.com.regalscan.utils.Tools;
import tw.com.regalscan.wifi.WifiConnector;
import tw.com.regalscan.wifi.WifiUtil;

/**
 * Created by tp00175 on 2017/5/15.
 */

public class LoginActivity extends AppCompatActivity {
    SharedPreferences settings;
    private Button btnExit, btnLogin;
    public Context mContext;
    public Activity mActivity;
    private EditText loginID, loginPsw;

    public static String SecSeq;
    private ProgressDialog progressDialog;
    private RFIDReaderService mRFIDReaderService;
    private AlertDialog mAlertDialog;
    private PublicFunctions mPublicFunctions;
    private TSQL mTSQL;
    private Setting mSetting;
    private Disposable networkDisposable;
    private WifiUtil mWifiUtil;
    private DeviceManager deviceManager;

    private void init() {
        mContext = this;
        mActivity = this;
        settings = getSharedPreferences("PrintTag",0);
        settings.edit().clear().apply();
        mRFIDReaderService = new RFIDReaderService(mActivity, mHandler);
        deviceManager = new DeviceManager();
        mPublicFunctions = new PublicFunctions(mContext, FlightData.SecSeq);
        mWifiUtil = new WifiUtil(mContext);

        mTSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
        mSetting = (Setting) ArmsUtils.obtainAppComponentFromContext(mContext).extras().get("Setting");


        // 帳號密碼
        RelativeLayout rlID = findViewById(R.id.rl_groundID);
        rlID.setVisibility(View.VISIBLE);
        RelativeLayout rlPsw = findViewById(R.id.rl_groundPsw);
        rlPsw.setVisibility(View.VISIBLE);

        loginID = findViewById(R.id.et_loginID);
        loginPsw = findViewById(R.id.et_groundPsw);
        loginID.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {

                String CAId = loginID.getText().toString().trim();
                if (CAId.equals("")) {
                    InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    mInputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    MessageBox.show("", "Please input user ID.", LoginActivity.this, "Return");
                    return true;
                }

                loginPsw.requestFocus();
            }
            return false;
        });

        loginPsw.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {

                String CPPw = loginPsw.getText().toString().trim();
                if (CPPw.equals("")) {
                    MessageBox.show("", "Please input user password.", LoginActivity.this, "Return");
                    return true;
                }

                InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                mInputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                ChkLoginInfo(false);
            }
            return false;
        });

        //隱藏AIR登入介面
        RelativeLayout layout = findViewById(R.id.row01);
        layout.setVisibility(View.INVISIBLE);
        RelativeLayout layout1 = findViewById(R.id.row02);
        layout1.setVisibility(View.INVISIBLE);
        RelativeLayout layout2 = findViewById(R.id.row03);
        layout2.setVisibility(View.INVISIBLE);
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setVisibility(View.INVISIBLE);

        //btn
        btnExit = findViewById(R.id.btnReLogin);
        btnExit.setOnClickListener(BtnOnClick);
        btnExit.setText("Exit");
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(BtnOnClick);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        try {
            Boolean isDocker = deviceManager.getDockerState();
            if (!isDocker) {
                connectToWifi();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onStart() {
        try {
            mRFIDReaderService.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }


    private Button.OnClickListener BtnOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == btnLogin) {
                String CPPw = loginPsw.getText().toString().trim();
                String CAId = loginID.getText().toString().trim();
                if (CAId.equals("")) {
                    MessageBox.show("", "Please input user ID.", LoginActivity.this, "Return");
                    return;
                }
                if (CPPw.equals("")) {
                    MessageBox.show("", "Please input user password.", LoginActivity.this, "Return");
                    return;
                } else if (!BuildConfig.DEBUG && !CPPw.equals("995996") && !CPPw.equals("123")) {
                    MessageBox.show("", "Password error...", LoginActivity.this, "Return");
                    return;
                }

                JSONObject jsonObject = mPublicFunctions.CheckEmployee(CAId);
                String retCode = null;
                try {
                    retCode = jsonObject.getString("ReturnCode");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                switch (CPPw) {
                    case "995996":
                        if (!retCode.equals("0")) {
                            MessageBox.show("", "Password error...", mContext, "OK");
                        } else {
                            if (MessageBox.show("", "Login Success", mContext, "OK")) {
                                mTSQL.WriteLog("", "Ground", "Login", "", "Login success. User:" + CAId);
                                RtnObject.getInstance().setCompany("EVA");
                                RtnObject.getInstance().setEmployeeID(loginID.getText().toString().trim());
                                Intent intent = new Intent(mActivity, MenuActivity.class);

                                mActivity.startActivity(intent);
                                mActivity.finish();
                            }
                        }
                        break;
                    case "123":
                        if (MessageBox.show("", "Login Success", mContext, "OK")) {
                            mTSQL.WriteLog("", "Ground", "Login", "", "Login success. User:" + CAId);
                            RtnObject.getInstance().setCompany("EGAS");
                            RtnObject.getInstance().setEmployeeID(loginID.getText().toString().trim());
                            Intent intent = new Intent(mActivity, MenuActivity.class);
                            mActivity.startActivity(intent);
                            mActivity.finish();
                        }
                        break;
                    default:
                        ChkLoginInfo(false);
                        break;
                }
            } else if (v == btnExit) {
                finish();
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);

        init();

        getLatestSecSeq();
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RFIDReaderService.MSG_SHOW_BLOCK_DATA:
                    String UID = msg.getData().getString(RFIDReaderService.CARD_UID);
                    //員工證號
                    String BlockData = msg.getData().getString(RFIDReaderService.CARD_BLOCK_DATA);
                    //"EVA" or "EGAS", UID和BlockData為null的話就是ErrorString
                    String EMPLOYEE_TYPE = msg.getData().getString(RFIDReaderService.EMPLOYEE_TYPE);

                    if (UID != null && BlockData != null) {
                        loginID.setText(BlockData);
                        mRFIDReaderService.stop();
                        if (EMPLOYEE_TYPE.equals("EVA")) {
                            ChkLoginInfo(true);
                        } else {
                            if (MessageBox.show("", "Login Success", mContext, "OK")) {
                                mTSQL.WriteLog("", "Ground", "Login", "", "Login success. User:" + BlockData);
                                RtnObject.getInstance().setCompany("EGAS");
                                RtnObject.getInstance().setEmployeeID(loginID.getText().toString().trim());
                                Intent intent = new Intent(mActivity, MenuActivity.class);
                                mActivity.startActivity(intent);
                                mActivity.finish();
                            }
                        }
                    } else {
                        // EMPLOYEE_TYPE為 Error String
                        MessageBox.show("", EMPLOYEE_TYPE, mActivity, "Return");
                    }
                    break;

                case RFIDReaderService.MSG_OPEN_FAILED:
                    MessageBox.show("", "Please try again", mActivity, "Return");
                    break;
            }
        }
    };

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

    //鎖返回和menu
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

    /**
     * 連接API進行帳號驗證
     *
     * @param rfidMark 是否使用RFID登入，true為是、false為否
     */
    private void ChkLoginInfo(boolean rfidMark) {

        DeviceManager deviceManager = new DeviceManager();

        showDialog();

        String pInfo = null;

        String UDID = deviceManager.getDeviceId();

        String userId, psw, rfid, company;

        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        if (rfidMark) {
            userId = loginID.getText().toString().toUpperCase();
            psw = "";
            rfid = "Y";
            company = "EVA";
        } else {
            userId = loginID.getText().toString();
            psw = loginPsw.getText().toString();
            rfid = "N";
            company = "EVA";
        }

        //region 開發用登入資料
//    String result = "{\"IS_VALID\":\"Y\",\"MSG\":null,\"COMPANY\":\"EVA\",\"DEPARTMENT\":\"I503\",\"EMPLOYEE_ID\":\"E70977\","
//        + "\"NEWS_LIST\":[{\"TITLE\":\"手提箱堆疊不可超高\",\"CONTENT\":\"手提箱堆疊不可超高\"},{\"TITLE\":\"TEST\",\"CONTENT\":\"POS - TEST\"}],"
//        + "\"FULLNAME\":\"賴信榕\",\"VERSION\":\"v1.0.6\",\"DOWNLOAD_URL\":\"https://qasmasbt01.evaair.com\",\"FORCE_UPDATE\":\"N\","
//        + "\"FTP_IP\":\"QASMASBT01\",\"SYSTEM_TIME\":\"20170417120000\"}";
//
//    try {
//      JSONObject jsonObject = new JSONObject(result);
//      RtnObject.getInstance().setIsValid(jsonObject.getString("IS_VALID"));
//      RtnObject.getInstance().setMsg(jsonObject.getString("MSG"));
//      RtnObject.getInstance().setCompany(jsonObject.getString("COMPANY"));
//      RtnObject.getInstance().setDepartment(jsonObject.getString("DEPARTMENT"));
//      RtnObject.getInstance().setEmployeeID(jsonObject.getString("EMPLOYEE_ID"));
//      RtnObject.getInstance().setFullName(jsonObject.getString("FULLNAME"));
//      RtnObject.getInstance().setVersion(jsonObject.getString("VERSION"));
//      RtnObject.getInstance().setDownloadURL(jsonObject.getString("DOWNLOAD_URL"));
//      RtnObject.getInstance().setForceUpdate(jsonObject.getString("FORCE_UPDATE"));
//      RtnObject.getInstance().setFtpIP(jsonObject.getString("FTP_IP"));
//      RtnObject.getInstance().setSystemTime(jsonObject.getString("SYSTEM_TIME"));
//
//      JSONArray jsonArray = jsonObject.getJSONArray("NEWS_LIST");
//      ArrayList<NewsInfo> newsInfos = new ArrayList<NewsInfo>();
//
//      for (int i = 0; i < jsonArray.length(); i++) {
//        String title = jsonArray.getJSONObject(i).getString("TITLE");
//        String content = jsonArray.getJSONObject(i).getString("CONTENT");
//        newsInfos.add(new NewsInfo(title, content));
//      }
//
//      RtnObject.getInstance().setNewsInfo(newsInfos);
//
//      createAlertDialog();
//
//      CheckAppVersion();
//
//    } catch (JSONException e) {
//      e.printStackTrace();
//    }
        //endregion

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("OS_VERSION", "5.1.1");
            jsonObject.put("APP_VERSION", pInfo);
            jsonObject.put("UDID", UDID);
            jsonObject.put("EMPLOYEE_ID", userId);
            jsonObject.put("PASSWORD", psw);
            jsonObject.put("RFID_MARK", rfid);
            jsonObject.put("COMPANY", company);
        } catch (Exception e) {
            Timber.e(e);
        }

        //region 開發中請屏蔽此區
        Http_Post.Post(new CallBack() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    final String msg = jsonObject.getString("MSG");
                    if (jsonObject.getString("IS_VALID").equals("Y")) {
                        RtnObject.getInstance().setMsg(jsonObject.getString("MSG"));
                        RtnObject.getInstance().setCompany(jsonObject.getString("COMPANY"));
                        RtnObject.getInstance().setDepartment(jsonObject.getString("DEPARTMENT"));
                        RtnObject.getInstance().setEmployeeID(jsonObject.getString("EMPLOYEE_ID"));
                        RtnObject.getInstance().setFullName(jsonObject.getString("FULLNAME"));
                        RtnObject.getInstance().setVersion(jsonObject.getString("VERSION"));
                        RtnObject.getInstance().setDownloadURL(jsonObject.getString("DOWNLOAD_URL"));
                        RtnObject.getInstance().setForceUpdate(jsonObject.getString("FORCE_UPDATE"));
                        RtnObject.getInstance().setFtpIP(jsonObject.getString("FTP_IP"));
                        RtnObject.getInstance().setSystemTime(jsonObject.getString("SYSTEM_TIME"));

                        JSONArray jsonArray = jsonObject.getJSONArray("NEWS_LIST");
                        ArrayList<NewsInfo> newsInfos = new ArrayList<>();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            String title = jsonArray.getJSONObject(i).getString("TITLE");
                            String content = jsonArray.getJSONObject(i).getString("CONTENT");
                            newsInfos.add(new NewsInfo(title, content));
                        }

                        RtnObject.getInstance().setNewsInfo(newsInfos);

                        runOnUiThread(() -> {
                            createAlertDialog();

                            CheckAppVersion();
                        });

                    } else {
                        runOnUiThread(() -> {
                            hideDialog();
                            MessageBox.show("", msg, LoginActivity.this, "Return");
                            mRFIDReaderService.start();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                hideDialog();
            }

            @Override
            public void onError(final String error) {
                runOnUiThread(() -> {
                    hideDialog();
                    MessageBox.show("", error, LoginActivity.this, "Return");
                    mRFIDReaderService.start();
                });
            }
        }, jsonObject, UrlConfig.getInstance().getUrl(mContext) + UrlConfig.getInstance().url_LOGIN, mContext);
        //endregion
    }

    private void CheckAppVersion() {

        PackageInfo pInfo;

        String oldVer = null;
        String newVer = RtnObject.getInstance().getVersion();
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            oldVer = "V" + pInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        //版本不相同做更新
        if (!oldVer.equals(newVer)) {
            mAlertDialog.show();
        } else {
            hideDialog();
            LoginActivity.this.runOnUiThread(() -> {
                if (MessageBox.show("", "Login Success", mContext, "Ok")) {
                    mTSQL.WriteLog("", "Ground", "Login", "", "Login success. User:" + loginID.getText().toString());
                    Intent intent = new Intent(mActivity, MenuActivity.class);
                    mActivity.startActivity(intent);
                    mActivity.finish();
                }
            });
        }
    }

    private void UpdateApk(byte[] fileBuffer) {

        String downloadURL = RtnObject.getInstance().getDownloadURL();
        getUpdateFile getUpdateFile = new getUpdateFile(downloadURL);
        getUpdateFile.execute();
//    byte[] fileBuffer = rtnObj;

        if (fileBuffer != null) {

            String SDCARD_PATH = Environment.getExternalStorageDirectory() + File.separator + "download";
            File tFile = new File(SDCARD_PATH + File.separator + "evapos.apk");

            if (tFile.exists()) {
                tFile.delete();
            }

            try {
                tFile.createNewFile();
                OutputStream mOutput = new FileOutputStream(tFile);

                mOutput.write(fileBuffer, 0, fileBuffer.length);

                mOutput.flush();
                mOutput.close();

                String fileName = Environment.getExternalStorageDirectory() + "/download/evapos.apk";
                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(install);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class getUpdateFile extends AsyncTask<Void, Void, Boolean> {

        private String url;
        private StringBuilder error = new StringBuilder();

        byte[] rtndata;

        getUpdateFile(String SERVER_URL) {
            url = SERVER_URL;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            int retCode = -1;

            HttpClient httpclient = new DefaultHttpClient();
            // Prepare a request object
            HttpGet httpget = new HttpGet(url);
            // Execute the request
            HttpResponse response;

            try {

                response = httpclient.execute(httpget);

                if (response.getStatusLine().getStatusCode() != 200) {
                    error.append("與遠端連線失敗!!");
                    hideDialog();
                    return false;
                }

                // Get the response entity
                HttpEntity entity = response.getEntity();
                // If response entity is not null
                if (entity != null) {

                    InputStream is = entity.getContent();
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                    int nRead;
                    byte[] data = new byte[1024];

                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }

                    buffer.flush();

                    rtndata = buffer.toByteArray();
                    return true;
                }

            } catch (ClientProtocolException e) {
                error.append("ClientProtocolException | " + e.getMessage());
                return false;
            } catch (IOException e) {
                error.append("IOException | " + e.getMessage());
                return false;
            } catch (Exception e) {
                error.append("Exception | " + e.getMessage());
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean) {
                UpdateApk(rtndata);
            } else {
                runOnUiThread(() -> {
                    hideDialog();
                    MessageBox.show("", "Download fail, please download again.", mContext, "Return");
                });
            }
        }
    }

    private void showDialog() {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void hideDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void createAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater layoutInflater = this.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_download, null);
        builder.setView(view);

        LinearLayout linearLayout = view.findViewById(R.id.ll_btnlayout);
        Button update = view.findViewById(R.id.btn_update);
        Button update1 = view.findViewById(R.id.btn_update1);
        Button cancel = view.findViewById(R.id.btn_cancel);

        if (RtnObject.getInstance().getForceUpdate().equals("Y")) {
            update1.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.INVISIBLE);
        }

        View.OnClickListener btnOnClick = v -> {
            mAlertDialog.cancel();
            showDialog();
            getUpdateFile getUpdateFile = new getUpdateFile(RtnObject.getInstance().getDownloadURL());
            getUpdateFile.execute();
        };

        update.setOnClickListener(btnOnClick);
        update1.setOnClickListener(btnOnClick);

        cancel.setOnClickListener((View v) -> {
            mAlertDialog.cancel();
            hideDialog();
            LoginActivity.this.runOnUiThread(() -> {
                if (tw.com.regalscan.component.MessageBox.show("", "Login Success", mContext, "OK")) {
                    Intent intent = new Intent(mActivity, MenuActivity.class);

                    mActivity.startActivity(intent);
                    mActivity.finish();
                }
            });
        });
        mAlertDialog = builder.create();
    }

    private void getLatestSecSeq() {

        StringBuilder errMsg = new StringBuilder();

        DBQuery.CurrentOpenFlightPack openFlightPack = DBQuery.getCurrentOpenFlightList(this, errMsg);

        if (openFlightPack != null) {
            if (openFlightPack.openFlights != null) {
                for (int i = 0; i < openFlightPack.openFlights.length; i++) {
                    if (openFlightPack.openFlights[i].Status.equals("Closed") && i != openFlightPack.openFlights.length - 1) {
                        if (openFlightPack.openFlights[i + 1].Status.equals("")) {
                            SecSeq = openFlightPack.openFlights[i].SecSeq;
                            FlightInfoManager.getInstance().setCurrentSecSeq(SecSeq);
                            break;
                        }
                    } else {
                        SecSeq = openFlightPack.openFlights[i].SecSeq;
                        FlightInfoManager.getInstance().setCurrentSecSeq(SecSeq);
                        break;
                    }
                }
            } else {
                FlightInfoManager.getInstance().setCurrentSecSeq("0");
            }
        } else {
            FlightInfoManager.getInstance().setCurrentSecSeq("0");
        }
    }

    private void connectToWifi() {
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

                        MessageBox.show("", "Wifi connect success!", mContext, "Ok");
                    } else {
                        Executor executor = Executors.newCachedThreadPool();
                        executor.execute(() -> {
                            try {
                                mWifiUtil.connect(mSetting.getGroundSSID(), null, 2);
                            } catch (WifiConnector.WifiException e) {
                                runOnUiThread(() -> {
                                    Cursor.Normal();
                                    if (MessageBox.show("", "Wifi connect is close, you can retry.", mContext, "Yes", "No")) {
                                        connectToWifi();
                                    } else {
                                        mWifiUtil.disconnect();
                                    }
                                });
                            }
                        });
                    }
                });
    }

    @Override
    protected void onDestroy() {
        mRFIDReaderService.Dispose();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        try {
            mRFIDReaderService.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onPause();
    }

}
