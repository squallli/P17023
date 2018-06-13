package tw.com.regalscan.evaground;

import android.content.Context;
import android.device.DeviceManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import tw.com.regalscan.FTP.FTPFunction;
import tw.com.regalscan.HttpRequest.CallBack;
import tw.com.regalscan.HttpRequest.Http_Post;
import tw.com.regalscan.HttpRequest.UrlConfig;
import tw.com.regalscan.R;
import tw.com.regalscan.db02.DBFunctions;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.FlightInfo;
import tw.com.regalscan.db02.SalesDataSummary;
import tw.com.regalscan.evaground.Models.RtnObject;
import tw.com.regalscan.utils.Compress;
import tw.com.regalscan.utils.Cursor;
import tw.com.regalscan.utils.EvaUtils;

public class UploadActivity extends AppCompatActivity {

    private final String TAG = UploadActivity.class.getSimpleName();

    private Button btnUpload;

    private ProgressBar progressUpload;
    private TextView txtUploadProgress;

    private DrawerLayout drawerLayout;
    private Context mContext;

    private boolean IsStop = false;

    private JSONObject jsonObject = new JSONObject();
    private JSONArray mJSONArray = new JSONArray();

    private List<String> mStrings = new ArrayList<>();

    private String errMsg = "";

    private String UDID = "";

    private DeviceManager mDeviceManager = new DeviceManager();
    private StringBuilder errorMsg = new StringBuilder();

    private JSONObject uploadJson = new JSONObject();

    private void InitializeComponent() {
        btnUpload = findViewById(R.id.btnUpload);
        btnUpload.setOnClickListener(uploadBtnOnClick);

        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(cancelBtnOnClick);

        progressUpload = findViewById(R.id.progressUpload);

        txtUploadProgress = findViewById(R.id.txtprogressUpload);

        // 設定側邊選單抽屜
        drawerLayout = findViewById(R.id.drawer_layout);
        enableExpandableList();

        UDID = mDeviceManager.getDeviceId();
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
        setContentView(R.layout.activity_upload);

        mContext = this;

        InitializeComponent();

    }

    private View.OnClickListener uploadBtnOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btnUpload.setEnabled(false);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mStrings.clear();
            progressUpload.setProgress(0);
            new Thread(() -> {
                if (!IsStop) {
                    try {
                        SalesDataSummary salesData = new SalesDataSummary(mContext, "0", mHandler);
                        jsonObject = salesData.SalesDataConvertToJson();
                        mJSONArray = jsonObject.getJSONArray("ResponseData");
                        sendMsg(20);

                    } catch (Exception e) {
                        runOnUiThread(() -> MessageBox.show("", e.getMessage(), mContext, "Return"));
                        errMsg = e.getMessage();
                        sendMsg(-1);
                        btnUpload.setEnabled(true);
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    };


    private void fileCovertSuccess() {

        FlightInfo flightInfo = DBQuery.getFlightInfo(mContext, errorMsg, "0");

        try {
            uploadJson.put("UDID", UDID);
            uploadJson.put("DEPT_DATE", flightInfo.FlightDate);
            uploadJson.put("DEPT_FLT_NO", flightInfo.FlightNo);
            uploadJson.put("EMPLOYEE_ID", RtnObject.getInstance().getEmployeeID());
            uploadJson.put("DOC_NO", flightInfo.CarNo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Http_Post.Post(new CallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    if (result.getString("IS_TRANS").equals("Y")) {
                        runOnUiThread(() -> {
                            if (MessageBox.show("", "This flight is already upload... Upload again??", mContext, "Yes", "No")) {
                                Cursor.Busy("Data uploading...", mContext);
                                new Thread(() -> UploadData()).start();
                            } else {
                                btnUpload.setEnabled(true);
                            }
                        });
                    } else {
                        runOnUiThread(() -> Cursor.Busy("Data uploading...", mContext));
                        UploadData();
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        MessageBox.show("", e.getMessage(), mContext, "Return");
                        btnUpload.setEnabled(true);
                    });
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    MessageBox.show("", error, mContext, "Return");
                    btnUpload.setEnabled(true);
                });
            }
        }, uploadJson, UrlConfig.getInstance().getUrl(mContext) + UrlConfig.getInstance().url_TRANS, mContext);
    }

    private void UploadData() {
        try {

            FlightInfo flightInfo = DBQuery.getFlightInfo(mContext, errorMsg, "0");

            //上傳照片
            uploadPic(flightInfo.FlightDate + "_" + flightInfo.FlightNo + "_" + flightInfo.CarNo + ".zip");

            String jsonString = mJSONArray.toString().substring(1, mJSONArray.toString().length() - 1);

            JSONObject JSONObject = new JSONObject(jsonString);

            JSONObject.put("UDID", UDID);
            JSONObject.put("DEPT_DATE", flightInfo.FlightDate);
            JSONObject.put("DEPT_FLT_NO", flightInfo.FlightNo);
            JSONObject.put("EMPLOYEE_ID", RtnObject.getInstance().getEmployeeID());
            JSONObject.put("DOC_NO", flightInfo.CarNo);

            Timber.tag(TAG).d(JSONObject.toString());

            Http_Post.Post(new CallBack() {
                @Override
                public void onSuccess(JSONObject result) {

                    runOnUiThread(() -> {
                        try {
                            Cursor.Normal();
                            if (result.getString("IS_VALID").equals("Y")) {
                                Timber.tag(TAG).d("Upload success!");
                                MessageBox.show("", "Upload success!", mContext, "Ok");

                                EvaUtils.deleteFile(Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator,
                                        flightInfo.FlightDate + "_" + flightInfo.FlightNo + "_" + flightInfo.CarNo + ".zip");

                                EvaUtils.deleteAllFileInFolder(Environment.getExternalStorageDirectory() +
                                        File.separator + Environment.DIRECTORY_PICTURES + File.separator, "Screenshots");

                                btnUpload.setEnabled(true);

                                DBFunctions dbFunctions = new DBFunctions(mContext, "");

                                dbFunctions.updateUploadStatus();

                                sendMsg(21);
                            } else {
                                Timber.tag(TAG).d("Upload error!");
                                MessageBox.show("", result.getString("MSG"), mContext, "Return");
                                btnUpload.setEnabled(true);
                                Timber.tag("UPLOAD_ERROR").d(result.getString("MSG"));
                                errMsg = result.getString("MSG");
                                sendMsg(-2);
                            }
                        } catch (Exception e) {
                            Timber.tag(TAG).d("Upload error!");
                            MessageBox.show("", e.getMessage(), mContext, "Return");
                            Timber.tag("UPLOAD_ERROR").d(e);
                            btnUpload.setEnabled(true);
                            errMsg = e.getMessage();
                            sendMsg(-2);
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        Cursor.Normal();
                        MessageBox.show("", error, mContext, "Return");
                        Timber.tag("UPLOAD_ERROR").d(error);
                        btnUpload.setOnClickListener(uploadBtnOnClick);
                    });
                }
            }, JSONObject, UrlConfig.getInstance().getUrl(mContext) + UrlConfig.getInstance().url_UPLOAD, mContext);
        } catch (Exception e) {
            runOnUiThread(() -> {
                Cursor.Normal();
                MessageBox.show("", e.getMessage(), mContext, "Return");
                Timber.tag(TAG).d(e);
                btnUpload.setOnClickListener(uploadBtnOnClick);
            });
            e.printStackTrace();
        }
    }

    private View.OnClickListener cancelBtnOnClick = view -> {
        IsStop = true;
        UploadActivity.this.runOnUiThread(() -> {
            if (MessageBox.show("", "Upload canceled!", UploadActivity.this, "Return")) {
                UploadActivity.this.finish();
            }
        });
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mStrings.add(0, "\r\nExport IP...");
//          txtUploadProgress.append("\r\nExport IP...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 2:
                    mStrings.add(0, "\r\nExport IM...");
//          txtUploadProgress.append("\r\nExport IM...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 3:
                    mStrings.add(0, "\r\nExport LC...");
//          txtUploadProgress.append("\r\nExport LC...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 4:
                    mStrings.add(0, "\r\nExport RR...");
//          txtUploadProgress.append("\r\nExport RR...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 5:
                    mStrings.add(0, "\r\nExport RS...");
//          txtUploadProgress.append("\r\nExport RS...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 6:
                    mStrings.add(0, "\r\nExport RD...");
//          txtUploadProgress.append("\r\nExport RD...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 7:
                    mStrings.add(0, "\r\nExport RC...");
//          txtUploadProgress.append("\r\nExport RC...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 8:
                    mStrings.add(0, "\r\nExport RZ...");
//          txtUploadProgress.append("\r\nExport RZ...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 9:
                    mStrings.add(0, "\r\nExport RA...");
//          txtUploadProgress.append("\r\nExport RA...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 10:
                    mStrings.add(0, "\r\nExport RV1...");
//          txtUploadProgress.append("\r\nExport RV1...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 11:
                    mStrings.add(0, "\r\nExport RV3...");
//          txtUploadProgress.append("\r\nExport RV3...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 12:
                    mStrings.add(0, "\r\nExport RC1...");
//          txtUploadProgress.append("\r\nExport RC1...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 13:
                    mStrings.add(0, "\r\nExport RA1..");
//          txtUploadProgress.append("\r\nExport RA1..");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 14:
                    mStrings.add(0, "\r\nExport LOG...");
//          txtUploadProgress.append("\r\nExport LOG...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 15:
                    mStrings.add(0, "\r\nExport PODS...");
//          txtUploadProgress.append("\r\nExport PODS...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 16:
                    mStrings.add(0, "\r\nExport PODR...");
//          txtUploadProgress.append("\r\nExport PODR...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 17:
                    mStrings.add(0, "\r\nExport COU...");
//          txtUploadProgress.append("\r\nExport COU...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 18:
                    mStrings.add(0, "\r\nExport RU1...");
//          txtUploadProgress.append("\r\nExport RU1...");
                    progressUpload.incrementProgressBy(5);
                    break;
                case 19:
                    mStrings.add(0, "\r\nExport RU3...");
//          txtUploadProgress.append("\r\nExport RU3...");
                    progressUpload.incrementProgressBy(10);
                    break;
                case 20:
                    mStrings.add(0, "\r\nExport success!");
//          txtUploadProgress.append("\r\nExport Success...");
                    break;
                case 22:
                    mStrings.add(0,"\r\nExport Carousel");
                    break;
                case 21:
                    mStrings.add(0, "\r\nUpload success!");
                    finish();
                    break;
                case -1:
                    mStrings.add(0, errMsg + ", please upload again.\r\n");
                    break;
                case -2:
                    mStrings.add(0, errMsg + ", please upload again.\r\n");
                    break;
            }

            if (mStrings != null) {
                if (mStrings.size() != 0) {
                    txtUploadProgress.setText("");
                    for (String string : mStrings) {
                        txtUploadProgress.append(string);
                    }
                }
            }

            if (mStrings.get(0).contains("Export success")) {
                fileCovertSuccess();
            }

            super.handleMessage(msg);
        }
    };

    private void sendMsg(int flag) {
        Message message = new Message();
        message.what = flag;
        mHandler.sendMessage(message);
    }

    private void uploadCarousel()
    {

    }

    private void uploadPic(String zipFileName) {

        List<String> file = new ArrayList<>();

        String path = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_PICTURES + File.separator + "Screenshots";
        String savePath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + zipFileName;
        File directory = new File(path);
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file1 : files) {
                file.add(path + File.separator + file1.getName());
            }
        }

        path = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "P17023.db3";
        file.add(path);

        if (file.size() != 0) {
            Compress compress = new Compress(file, savePath);
            compress.zip();

//            try {
//                String[] strings = zipFileName.split("_");
//                AESEncrypter aesEncrypter = new AESEncrypterBC();
//                aesEncrypter.init(strings[0] + strings[2].split("\\.")[0], 0);
//                File aesZip = new File(Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + zipFileName);
//                AesZipFileEncrypter aesZipFileEncrypter = new AesZipFileEncrypter(aesZip, aesEncrypter);
//                aesZipFileEncrypter.add(new File(savePath), strings[0] + strings[2].split("\\.")[0]);
//                aesZipFileEncrypter.close();
//                new File(savePath).delete();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            FTPFunction ftpFunction = new FTPFunction(this);
            //region 上傳RS
            FilenameFilter namefilter =new FilenameFilter(){
                private String[] filter={"RS"};
                @Override
                public boolean accept(File dir, String filename){
                    for(int i=0;i<filter.length;i++){
                        if(filename.indexOf(filter[i])!=-1)
                            return true;
                    }
                    return false;
                }
            };
            try{
                File filePath = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "Upload");
                File[] fileList=filePath.listFiles(namefilter);
                CharSequence[] list =new CharSequence[fileList.length];
                for(int i=0;i<list.length;i++){
                    list[i]=fileList[i].getName();
                }
                ftpFunction.uploadRSFile(list[0].toString());
            }catch(Exception e){
                e.printStackTrace();
            }

            //endregion
            ftpFunction.uploadPicture(zipFileName);

//            Executor executor = Executors.newCachedThreadPool();
//            executor.execute(() -> ftpFunction.uploadPicture(zipFileName));
        }
    }

}
