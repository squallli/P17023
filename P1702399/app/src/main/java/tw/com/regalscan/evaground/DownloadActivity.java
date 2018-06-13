package tw.com.regalscan.evaground;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.device.DeviceManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.security.KeyChain;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.jess.arms.utils.ArmsUtils;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

import timber.log.Timber;
import tw.com.regalscan.FTP.FTPFunction;
import tw.com.regalscan.HttpRequest.CallBack;
import tw.com.regalscan.HttpRequest.Http_Post;
import tw.com.regalscan.HttpRequest.UrlConfig;
import tw.com.regalscan.R;
import tw.com.regalscan.evaground.Models.JsonPostObject;
import tw.com.regalscan.evaground.Models.RtnObject;
import tw.com.regalscan.utils.EvaUtils;

public class DownloadActivity extends AppCompatActivity {

    private final String TAG = DownloadActivity.class.getSimpleName();

    private Button mbtnDownload, mbtnCancel;
    private EditText medittxtDatePicker, medittxtFlightNum;
    private Spinner mSpinner;
    private RadioButton radioButton;
    private DrawerLayout drawerLayout;

    private boolean state = true, cupDownload = false, imgDownload = false, cerDownload = false;

    private String UDID, FlightNo;
    private downloadFile mDownloadFile;
    private TextView downLoadProgressText, downLoadProgressDone;
    private FTPFunction mFTPFunction;
    private DeviceManager deviceManager = new DeviceManager();
    private JSONObject mJSONObject = new JSONObject();
    public List<String> mStrings = new ArrayList<>();
    private String progressMsg;
    private ProgressBar progressDownload;
    private String cert_name;
    private Context mContext;
    private String SSID;

    private Calendar cal = Calendar.getInstance();

    private void InitializeComponent() {
        mbtnDownload = findViewById(R.id.btnDownload);
        mbtnDownload.setOnClickListener(downloadBtnOnClick);

        mbtnCancel = findViewById(R.id.btnCancel);
        mbtnCancel.setOnClickListener(cancelBtnOnClick);

        medittxtDatePicker = findViewById(R.id.edittxtDatePicker);
        medittxtDatePicker.setInputType(InputType.TYPE_NULL);
        medittxtDatePicker.setOnClickListener(edittxtDatePickerOnClick);
        medittxtDatePicker.setText(
                String.valueOf(cal.get(Calendar.YEAR)) + String.format("%02d", cal.get(Calendar.MONTH) + 1) + String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)));

        medittxtFlightNum = findViewById(R.id.edittxtFlightNum);
        medittxtFlightNum.setOnKeyListener(getCartNo);

        mSpinner = findViewById(R.id.spinnerCartNum);
        mSpinner.setVisibility(View.INVISIBLE);

        // 設定側邊選單抽屜
        drawerLayout = findViewById(R.id.drawer_layout);

        downLoadProgressText = findViewById(R.id.tv_downloadProgress);
        downLoadProgressDone = findViewById(R.id.tv_downloadProgressDone);

        mFTPFunction = new FTPFunction(DownloadActivity.this);

        progressDownload = findViewById(R.id.progressDownload);

        enableExpandableList();
        addListenerOnButton();

        radioButton = findViewById(R.id.rbtn_br);
    }

    private void enableExpandableList() {
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ExpandableListView expandableListView = findViewById(R.id.expendlist);

        new NavigationDrawer(this, this, drawerLayout, toolbar, expandableListView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avtivity_download);

        InitializeComponent();

        UDID = deviceManager.getDeviceId();

        mContext = this;
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private View.OnClickListener downloadBtnOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            if (medittxtDatePicker.getText().toString().trim().equals("")) {
                MessageBox.show("", "Pleas enter departure time.", DownloadActivity.this, "Return");
                medittxtDatePicker.requestFocus();
                return;
            }

            if (medittxtFlightNum.getText().toString().trim().equals("")) {
                MessageBox.show("", "Please enter flight number.", DownloadActivity.this, "Return");
                medittxtFlightNum.requestFocus();
                return;
            }

            if (mSpinner.getSelectedItem() == null) {
                MessageBox.show("", "Please select cart number.", DownloadActivity.this, "Return");
                return;
            }

            EvaUtils.deleteAllFileInFolder(Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator, "EVAPosDownloadText");
            EvaUtils.deleteAllFileInFolder(Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_PICTURES + File.separator, "Screenshots");

            mStrings.clear();
            downLoadProgressText.setText("");
            downLoadProgressDone.setText("");
            progressDownload.setProgress(0);
            mbtnDownload.setOnClickListener(null);

            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            downLoadProgressText.setText("Waiting server create file.");

            JsonPostObject.getInstance().setUDID(UDID);
            JsonPostObject.getInstance().setDEPT_DATE(medittxtDatePicker.getText().toString());
            JsonPostObject.getInstance().setDEPT_FLT_NO(FlightNo);
            JsonPostObject.getInstance().setEMPLOYEE_ID(RtnObject.getInstance().getEmployeeID());
            JsonPostObject.getInstance().setDOC_NO(mSpinner.getSelectedItem().toString());

            try {
                mJSONObject.put("UDID", UDID);
                mJSONObject.put("DEPT_DATE", medittxtDatePicker.getText().toString());
                mJSONObject.put("DEPT_FLT_NO", FlightNo);
                mJSONObject.put("EMPLOYEE_ID", RtnObject.getInstance().getEmployeeID());
                mJSONObject.put("DOC_NO", mSpinner.getSelectedItem().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            chkDownloadable();
        }
    };

    private View.OnClickListener cancelBtnOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            state = false;
            DownloadActivity.this.runOnUiThread(() -> {
                if (MessageBox.show("", "Download canceled!", DownloadActivity.this, "Return")) {
                    if (mDownloadFile != null) {
                        mDownloadFile.cancel(true);
                        DownloadActivity.this.finish();
                    }
                    DownloadActivity.this.finish();
                }
            });
        }
    };

    private View.OnClickListener edittxtDatePickerOnClick = v -> showDatePickerDialog();

    private void showDatePickerDialog() {

        final DatePickerDialog mDialog = new DatePickerDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT, null,
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        mDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes",
                (dialog, which) -> {
                    //通過mDialog.getDatePicker獲得dialog上的DatePicker組件，然後可以獲取日期信息
                    DatePicker datePicker = mDialog.getDatePicker();
                    int year = datePicker.getYear();
                    int month = datePicker.getMonth() + 1;
                    int day = datePicker.getDayOfMonth();
                    medittxtDatePicker.setText(String.valueOf(year) + String.format("%02d", month) + String.format("%02d", day));
                    medittxtFlightNum.setText("");
                    mSpinner.setAdapter(null);
                });
        //取消按鈕，如果不需要直接不設置即可
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No",
                (dialog, which) -> System.out.println("BUTTON_NEGATIVE~~"));
        mDialog.show();
    }

    private void addListenerOnButton() {

        RadioGroup radioGroup = findViewById(R.id.rgroup);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rbtn_br:
                    radioButton = findViewById(checkedId);
                    break;
                case R.id.rbtn_b7:
                    radioButton = findViewById(checkedId);
                    break;
            }

            medittxtFlightNum.setText("");
            mSpinner.setAdapter(null);
        });
    }

    private View.OnKeyListener getCartNo = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                boolean flag = true;

                if (medittxtDatePicker.getText().toString().trim().equals("")) {
                    MessageBox.show("", "Please enter departure time.", DownloadActivity.this, "Return");
                    medittxtDatePicker.requestFocus();
                    flag = false;
                } else if (radioButton == null) {
                    MessageBox.show("", "Please select BR or B7.", DownloadActivity.this, "Return");
                    flag = false;
                } else if (medittxtFlightNum.getText().toString().equals("")) {
                    MessageBox.show("", "Please enter flight number.", DownloadActivity.this, "Return");
                    flag = false;
                }

                if (flag) {

                    FlightNo = radioButton.getText().toString() + medittxtFlightNum.getText().toString();

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("UDID", UDID);
                        jsonObject.put("DEPT_DATE", medittxtDatePicker.getText().toString());
                        jsonObject.put("DEPT_FLT_NO", FlightNo);
                        jsonObject.put("EMPLOYEE_ID", RtnObject.getInstance().getEmployeeID());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Http_Post.Post(new CallBack() {
                        @Override
                        public void onSuccess(final JSONObject jsonObject) {
                            try {
                                final String msg = jsonObject.getString("MSG");
                                if (jsonObject.getString("IS_VALID").equals("Y")) {
                                    JSONArray jsonArray = jsonObject.getJSONArray("DOC_LIST");

                                    final List<String> docNos = new ArrayList<>();

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        docNos.add(jsonArray.getJSONObject(i).getString("DOC_NO"));
                                    }

                                    runOnUiThread(() -> {
                                        ArrayAdapter arrayAdapter = new ArrayAdapter(DownloadActivity.this, android.R.layout.simple_spinner_item, docNos);
                                        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        mSpinner.setAdapter(arrayAdapter);
                                        mSpinner.setVisibility(View.VISIBLE);
                                    });
                                } else {
                                    runOnUiThread(() -> MessageBox.show("", msg, DownloadActivity.this, "Return"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(final String error) {
                            runOnUiThread(() -> MessageBox.show("", error, mContext, "Return"));

                        }
                    }, jsonObject, UrlConfig.getInstance().getUrl(DownloadActivity.this) + UrlConfig.getInstance().url_GETDOCNO, mContext);
                }
            }
            return false;
        }
    };

    /**
     * Notify API for creating download file
     */
    private void notifyDownload() {

        Http_Post.Post(new CallBack() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                try {
                    if (jsonObject.getString("IS_VALID").equals("Y")) {
                        runOnUiThread(() -> downLoadProgressText.setText(""));
                        mFTPFunction.mStrings.add(0, "Server create file success!\n");
                        mDownloadFile = new downloadFile(medittxtDatePicker.getText().toString(), FlightNo, mSpinner.getSelectedItem().toString());
                        mDownloadFile.execute();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {

            }
        }, mJSONObject, UrlConfig.getInstance().getUrl(DownloadActivity.this) + UrlConfig.getInstance().url_DOWNLOAD, mContext);
    }

    /**
     * Check the file is already downloaded or not
     */
    private void chkDownloadable() {

        Http_Post.Post(new CallBack() {
            @Override
            public void onSuccess(final JSONObject jsonObject) {
                try {
                    final String is_valid = jsonObject.getString("IS_VALID");
                    final String msg = jsonObject.getString("MSG");
                    final String is_downloadable = jsonObject.getString("IS_DOWNLOADABLE");
                    final String is_outOf24HR = jsonObject.getString("IS_OUTOF24HR");
                    final String is_trans = jsonObject.getString("IS_TRANS");
                    final String is_cup_download = jsonObject.getString("IS_CUP_DOWNLOAD");
                    final String is_update_image = jsonObject.getString("IS_UPDATE_IMAGE");
                    final String is_update_cer = jsonObject.getString("IS_UPDATE_CER");
                    cert_name = jsonObject.optString("CERT_NAME");
                    if (is_valid.equals("Y")) {

                        int count = 0;
                        if (is_cup_download.equals("Y")) {
                            cupDownload = true;
                            count += 1;
                        }

                        if (is_update_image.equals("Y")) {
                            imgDownload = true;
                            count += 1;
                        }

                        if (is_update_cer.equals("Y")) {
                            cerDownload = true;
                            count += 1;
                        }

                        if (count == 1) {
                            progressDownload.setMax(60);
                        } else if (count == 2) {
                            progressDownload.setMax(80);
                        } else if (count == 3) {
                            progressDownload.setMax(100);
                        } else {
                            progressDownload.setMax(40);
                        }

                        //檢查航班可否下載
                        if (is_downloadable.equals("N")) {
                            runOnUiThread(() -> MessageBox.show("", "Can't download this Flight!!", DownloadActivity.this, "Return"));
                        } else {

                            //檢查該車輛是否為24hr以後的車次
                            if (is_outOf24HR.equals("Y")) {
                                runOnUiThread(() -> {
                                    if (MessageBox.show("", "Dept time is out of 24hr. Still want to download?", DownloadActivity.this, "Yes", "No")) {
                                        notifyDownload();
                                    } else {
                                        mbtnDownload.setOnClickListener(downloadBtnOnClick);
                                    }
                                });
                            }

                            //檢查該車輛是否被下載過
                            if (is_trans.equals("Y")) {
                                runOnUiThread(() -> {
                                    if (MessageBox.show("", "This Flight already download, is download again?",
                                            DownloadActivity.this, "Yes", "No")) {
                                        notifyDownload();
                                    } else {
                                        mbtnDownload.setOnClickListener(downloadBtnOnClick);
                                    }
                                });
                            }

                            if (is_trans.equals("N") && is_outOf24HR.equals("N")) {
                                runOnUiThread(() -> notifyDownload());
                            }
                        }
                    } else {
                        runOnUiThread(() -> MessageBox.show("", msg, DownloadActivity.this, "Return"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                downLoadProgressText.setText("");
                MessageBox.show("", error, mContext, "Return");
            }
        }, mJSONObject, UrlConfig.getInstance().getUrl(DownloadActivity.this) + UrlConfig.getInstance().url_DOWNLOADABLE, mContext);
    }


    /**
     * Download and show download progress
     */
    class downloadFile extends AsyncTask<Void, Void, Boolean> {

        private final String mDate, mFlightNo, mCartNo;

        downloadFile(String date, String flightNo, String cartNo) {
            mDate = date;
            mFlightNo = flightNo;
            mCartNo = cartNo;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(final Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            DownloadActivity.this.runOnUiThread(() -> {
                if (aBoolean) {
                    if (cerDownload) {
                        if (!ImportCa("EVAPOS.cer")) {
                            MessageBox.show("", "CA install failed!", mContext, "Return");
                        }
                    } else {
                        if (MessageBox.show("", "Download success!", DownloadActivity.this, "Ok")) {
                            Http_Post.Post(
                                    new CallBack() {
                                        @Override
                                        public void onSuccess(JSONObject result) {
                                            Timber.tag(TAG).d("Update status success!");

                                            mFTPFunction.deleteAll(
                                                    new File(Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator + "EVAPOSDownloadText"));

                                            ArmsUtils.startActivity(DownloadActivity.this, ReportActivity.class);

                                            DownloadActivity.this.finish();
                                        }

                                        @Override
                                        public void onError(String error) {
                                            Timber.tag(TAG).d(error);

                                        }
                                    }, mJSONObject, UrlConfig.getInstance().getUrl(mContext) + UrlConfig.getInstance().url_UPDATESTATUS, mContext);
                        }
                    }
                } else {
                    if (MessageBox.show("", "Download fail, please download again!", DownloadActivity.this, "Return")) {
                        mbtnDownload.setOnClickListener(downloadBtnOnClick);
                    }
                }
            });
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                progressMsg = mDate + "_" + mFlightNo + "_" + mCartNo + ".zip";
                sendMsg(0);
                if (!mFTPFunction.DownloadFlightInfo(mDate, mFlightNo, mCartNo)) {
                    sendMsg(-1);
                    return false;
                } else {
                    sendMsg(3);
                }

                Thread.sleep(1000);

                sendMsg(1);
                if (!mFTPFunction.UnZipFlightInfo(mDate, mFlightNo, mCartNo)) {
                    return false;
                } else {
                    sendMsg(4);
                }

                Thread.sleep(1000);

                progressMsg = "BLACK.zip";
                sendMsg(0);
                if (!mFTPFunction.DownloadBlackList()) {
                    sendMsg(-1);
                    return false;
                } else {
                    sendMsg(3);
                }

                Thread.sleep(1000);

                sendMsg(1);
                if (!mFTPFunction.UnZipBlackList()) {
                    return false;
                } else {
                    sendMsg(4);
                }

                if (cupDownload) {
                    Thread.sleep(1000);
                    progressMsg = "CUP.zip";
                    sendMsg(0);
                    if (!mFTPFunction.DownloadCUP()) {
                        sendMsg(-1);
                        return false;
                    } else {
                        sendMsg(3);
                    }

                    Thread.sleep(1000);

                    sendMsg(1);
                    if (!mFTPFunction.UnZipCUP()) {
                        return false;
                    } else {
                        sendMsg(4);
                    }
                }

                if (imgDownload) {
                    Thread.sleep(1000);
                    progressMsg = "IMAGE.zip";
                    sendMsg(0);
                    if (!mFTPFunction.DownloadImg()) {
                        sendMsg(-1);
                        return false;
                    } else {
                        sendMsg(3);
                    }

                    Thread.sleep(1000);

                    sendMsg(1);
                    if (!mFTPFunction.UnZipImg()) {
                        return false;
                    } else {
                        sendMsg(4);
                    }
                }

                if (cerDownload) {
                    Thread.sleep(1000);
                    progressMsg = "EVAPOS.cer";
                    sendMsg(0);
                    if (!mFTPFunction.DownloadCA(cert_name)) {
                        sendMsg(-1);
                        return false;
                    } else {
                        sendMsg(3);
                    }
                }

                mFTPFunction.DownloadFont();

                Thread.sleep(1000);

                progressMsg = "DataBase";
                sendMsg(2);
                if (!mFTPFunction.DBArchB().getBoolean("Result")) {
                    sendMsg(-3);
                    return false;
                } else {
                    sendMsg(5);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            if (!ImportCa(cert_name)) {
                MessageBox.show("", "P12 install failed!", mContext, "Return");
            }
        } else if (requestCode == 200) {
            if (MessageBox.show("", "Download success!", DownloadActivity.this, "Ok")) {
                Http_Post.Post(
                        new CallBack() {
                            @Override
                            public void onSuccess(JSONObject result) {
                                Timber.tag(TAG).d("Update status success!");

                                mFTPFunction.deleteAll(
                                        new File(Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator + "EVAPOSDownloadText"));

                                ArmsUtils.startActivity(DownloadActivity.this, ReportActivity.class);

                                DownloadActivity.this.finish();
                            }

                            @Override
                            public void onError(String error) {
                                Timber.tag(TAG).d(error);
                            }
                        }, mJSONObject, UrlConfig.getInstance().getUrl(mContext) + UrlConfig.getInstance().url_UPDATESTATUS, mContext);
            }
        }
    }

    private boolean ImportCa(String fileName) {

        String CERT_FILE = Environment.getExternalStorageDirectory() + "/Download/" + fileName;
        Intent intent = KeyChain.createInstallIntent();

        try {
            if (fileName.equals("EVAPOS.cer")) {
                InputStream inStream = new FileInputStream(CERT_FILE);
                javax.security.cert.X509Certificate x509 = javax.security.cert.X509Certificate.getInstance(inStream);
                inStream.close();

                intent.putExtra(KeyChain.EXTRA_CERTIFICATE, x509.getEncoded());
                startActivityForResult(intent, 100);
            } else {
                File file = new File(CERT_FILE);
                byte bytes[] = FileUtils.readFileToByteArray(file);

                intent.putExtra(KeyChain.EXTRA_PKCS12, bytes);
                startActivityForResult(intent, 200);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    downLoadProgressText.setText("Wait for download " + progressMsg);
                    break;
                case 1:
                    downLoadProgressText.setText("Wait for unzip " + progressMsg);
                    break;
                case 2:
                    downLoadProgressText.setText("Wait for create " + progressMsg);
                    break;
                case 3:
                    progressDownload.incrementProgressBy(10);
                    mStrings.add(0, "Download " + progressMsg + " ... OK\n");
                    downLoadProgressDone.setText("");
                    for (int i = 0; i < mStrings.size(); i++) {
                        downLoadProgressDone.append(mStrings.get(i).toString());
                    }
                    downLoadProgressText.setText("");
                    break;
                case 4:
                    progressDownload.incrementProgressBy(10);
                    mStrings.add(0, "Unzip " + progressMsg + " ... OK\n");
                    downLoadProgressDone.setText("");
                    for (int i = 0; i < mStrings.size(); i++) {
                        downLoadProgressDone.append(mStrings.get(i).toString());
                    }
                    downLoadProgressText.setText("");
                    break;
                case 5:
                    progressDownload.incrementProgressBy(14);
                    mStrings.add(0, "Create " + progressMsg + " ... OK\n");
                    downLoadProgressDone.setText("");
                    for (int i = 0; i < mStrings.size(); i++) {
                        downLoadProgressDone.append(mStrings.get(i).toString());
                    }
                    downLoadProgressText.setText("");
                    break;

                case -2:
                    mStrings.add(0, "Unzip " + progressMsg + " fail, please download again.\n");
                    downLoadProgressDone.setText("");
                    progressDownload.setProgress(0);
                    for (int i = 0; i < mStrings.size(); i++) {
                        downLoadProgressDone.append(mStrings.get(i).toString());
                    }
                    break;

                case -3:
                    mStrings.add(0, "Create " + progressMsg + " fail, please download again.\n");
                    downLoadProgressDone.setText("");
                    progressDownload.setProgress(0);
                    for (int i = 0; i < mStrings.size(); i++) {
                        downLoadProgressDone.append(mStrings.get(i).toString());
                    }
                    break;

                case -4:
                    mStrings.add(0, progressMsg + ", please download again.\n");
                    downLoadProgressDone.setText("");
                    progressDownload.setProgress(0);
                    for (int i = 0; i < mStrings.size(); i++) {
                        downLoadProgressDone.append(mStrings.get(i).toString());
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void sendMsg(int flag) {
        Message message = new Message();
        message.what = flag;
//    Handler printerHandler = new PrinterHandler(DownloadActivity.this);
//    printerHandler.sendMessage(message);
        mHandler.sendMessage(message);
    }
}
