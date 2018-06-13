package tw.com.regalscan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import com.jess.arms.utils.ArmsUtils;

import tw.com.regalscan.app.CacheDataTags;
import tw.com.regalscan.app.entity.Setting;
import tw.com.regalscan.component.FlightInfoManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBFunctions;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.evaair.MenuActivity;
import tw.com.regalscan.evaair.OpenActivity;
import tw.com.regalscan.evaground.LoginActivity;
import tw.com.regalscan.wifi.WifiUtil;
import zmq.Config;


public class MainActivity extends AppCompatActivity {

    private Button btnCrew, btnGround;
    public Context mContext;
    public Activity mActivity;
    private TextView txtVersion, txtPreOrder, txtVIPOrder,
        txtCartNo, txtflightInfo01;
    //可以繼續Open狀態的航段
    private String canContinueSecSeq;

    // ShardPreference的密碼儲存檔案
    private final String PASSWORD_FILE = "PASSWORD_FILE.txt";
    // 音量調整
    private AudioManager mAudioManager;

    private WifiUtil mWifiUtil;

    private DBFunctions mDBFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        if (BuildConfig.DEBUG) {
//            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                    .detectCustomSlowCalls() //API等級11，使用StrictMode.noteSlowCode
//                    .detectDiskReads()
//                    .detectDiskWrites()
//                    .detectNetwork()   // or .detectAll() for all detectable problems
//                    //.penaltyDialog() //彈出違規提示對話框
//                    .penaltyLog() //在Logcat 中打印違規異常信息
//                    //.penaltyFlashScreen() //API等級11
//                    .build());
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects() //API等級11
//                    .penaltyLog()
//                    .penaltyDeath()
//                    .build());
//        }

        init();


    }

    @Override
    protected void onStart() {
        super.onStart();

        prepareData();
    }


    private void init() {

        mContext = this;
        mActivity = this;

        new Thread() {
            public void run() {
                mWifiUtil = new WifiUtil(mContext);
                if (!BuildConfig.DEBUG) {
                    mWifiUtil.disconnect();
                }

                // 設定關閉螢幕的時間
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 1800000);

                // 設定媒體音量預設大小
                mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); //最大音量, 15
                int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC); //當前音量
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(maxVolume * 0.1), 0); //tempVolume:音量絕對值


                setGlobalData();
            }}.start();




        txtPreOrder = findViewById(R.id.txtPreOrder);
        txtVIPOrder = findViewById(R.id.txtVIPOrder);
        txtCartNo = findViewById(R.id.txtCartNo);
        txtflightInfo01 = findViewById(R.id.flightInfo01);

        //版本號碼
        txtVersion = findViewById(R.id.txtVersion);

        //btn
        btnCrew = findViewById(R.id.btnCrew);
        btnCrew.setOnClickListener(view -> {
            Intent intent = new Intent(mActivity, OpenActivity.class);
            mActivity.startActivity(intent);
            mActivity.finish();
        });
        btnCrew.setEnabled(false);

        btnGround = findViewById(R.id.btnGround);
        btnGround.setOnClickListener(view -> {
            //開Ground頁面
            Intent intent = new Intent(mActivity, LoginActivity.class);
            mActivity.startActivity(intent);
//            ArmsUtils.startActivity(tw.com.regalscan.evaground.mvp.ui.activity.LoginActivity.class);
            mActivity.finish();

            //region 產生資料庫，不使用時請註解
//            final ProgressDialog progressDialog;
//            progressDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
//            new Thread(() -> {
//                try {
//                    final JSONObject jsonObject = new FTPFunction(mContext).DBArchB();
//                    if (!jsonObject.getBoolean("Result")) {
//                        runOnUiThread(() -> {
//                            try {
//                                MessageBox.show("", jsonObject.getString("ErrMsg"), mContext, "Return");
//                                progressDialog.dismiss();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        });
//                    } else {
//                        runOnUiThread(() -> MessageBox.show("", "Database Create success!", mContext, "Return"));
//                        progressDialog.dismiss();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                progressDialog.dismiss();
//            }).start();
            //endregion
        });
        btnGround.setEnabled(true);

        //設定系統時間
//        String[] strings = mDBFunctions.getFlightDate();
//        String date = strings[0];
//        Calendar c = Calendar.getInstance();
//        c.set(Integer.valueOf(date.substring(0,4)), Integer.valueOf(date.substring(5,7)), Integer.valueOf(date.substring(7, 8)), 00, 00, 00);
//        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
//        alarmManager.setTime(c.getTimeInMillis());

    }

    private void prepareData() {

        final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Check Flight...", true, false);
        new Thread() {
            public void run() {

                //取得軟體版本號
                try {
                    PackageManager packageManager = MainActivity.this.getPackageManager();
                    PackageInfo pInfo = packageManager.getPackageInfo(MainActivity.this.getPackageName(), 0);
                    final String[] versionSplit = pInfo.versionName.split("\\.");

                    MainActivity.this.runOnUiThread(() -> txtVersion.setText("V" + versionSplit[0] + "." + versionSplit[1] + "." + versionSplit[2]));

                    //確認基本銷售資訊
                    StringBuilder err = new StringBuilder();
                    final DBQuery.BasicSalesInfo saleInfo = DBQuery.checkBasicSalesInfoIsReady(mContext, err);

                    if (saleInfo == null) {
                        MainActivity.this.runOnUiThread(() -> {
                            mloadingDialog.dismiss();
                            MessageBox.show("", "Get basic sales info error", mContext, "Return");
                        });
                        return;
                    }

                    MainActivity.this.runOnUiThread(() -> {
                        txtPreOrder.setText("Preorder: " + String.valueOf(saleInfo.PreorderCount));
                        txtVIPOrder.setText("VIP Order: " + String.valueOf(saleInfo.VIPCount));
                    });

                    //取得全部航段資訊
                    //畫面預留四段空間
                    err = new StringBuilder();
                    final DBQuery.FlightInfoPack flightPack = DBQuery.getFlightInfo(mContext, err);

                    if (flightPack == null) {
                        MainActivity.this.runOnUiThread(() -> {
                            mloadingDialog.dismiss();
                            MessageBox.show("", "Get Flight info error", mContext, "Return");
                        });
                        return;
                    }
                    //存入FlightInfoManager
//          FlightInfoManager.getInstance().setFlightInfoPack(flightPack);

                    //設定車櫃編號與航段資訊
                    MainActivity.this.runOnUiThread(() -> {
                        txtflightInfo01.setText("");

                        for (int i = 0; i < flightPack.flights.length; i++) {

                            //車櫃號, 文字加底線
                            SpannableString txtUnderlineSector = new SpannableString(flightPack.flights[i].CarNo);
                            txtUnderlineSector.setSpan(new UnderlineSpan(), 0, flightPack.flights[i].CarNo.length(), 0);
                            txtCartNo.setText(txtUnderlineSector);

                            //航段編號
                            txtflightInfo01.append(
                                flightPack.flights[i].FlightNo + " - " +
                                    flightPack.flights[i].DepStn + " to " +
                                    flightPack.flights[i].ArivStn
                            );

                            if (i != flightPack.flights.length - 1) {
                                txtflightInfo01.append("\n");
                            }
                        }
                    });

                    //如果有沒有關櫃的航段，就直接進入Air，提醒組員要關櫃
                    checkOpenedFlightExit();

                    MainActivity.this.runOnUiThread(() -> {
                        btnCrew.setEnabled(true);
                        btnGround.setEnabled(true);
                    });

                    mloadingDialog.dismiss();

                } catch (Exception e) {
                    e.printStackTrace();
                    mloadingDialog.dismiss();
                    MainActivity.this.runOnUiThread(() -> MessageBox.show("", "Get flight info error", mContext, "Return"));
                }
            }
        }.start();
    }

    private void checkOpenedFlightExit() {

        StringBuilder err = new StringBuilder();
        final DBQuery.CurrentOpenFlightPack currentFlightPack = DBQuery.getCurrentOpenFlightList(mContext, err);

        if (currentFlightPack == null) {
            MainActivity.this.runOnUiThread(() -> MessageBox.show("", "Get flight info error", mContext, "Return"));
            return;
        }

        for (int i = 0; i < currentFlightPack.openFlights.length; i++) {
            if (currentFlightPack.openFlights[i].Status.equals("Open")) {
                //可重新開櫃的航段編號
                canContinueSecSeq = currentFlightPack.openFlights[i].SecSeq;

                //如果有Open狀態, 直接進Menu
                DBQuery.FlightInfo flight = DBQuery.getFlightInfo(mContext, err, canContinueSecSeq);

                if (flight == null) {
                    MainActivity.this.runOnUiThread(() -> MessageBox.show("", "Open flight failed.", mContext, "Return"));
                } else {
                    //取得特定組員資訊, 7
                    DBQuery.CrewInfo crewInfo = DBQuery.getGetCrewInfo(mContext, err, flight.CrewID); //CA
                    if (crewInfo == null) {
                        MainActivity.this.runOnUiThread(() -> MessageBox.show("", "Get crew failed.", mContext, "Return"));
                    } else {
                        DBQuery.CrewInfo CA = DBQuery.getGetCrewInfo(mContext, err, flight.CrewID);

                        //紀錄先前登入之航段編號, CA資訊, CP資訊
                        FlightInfoManager.getInstance().setCurrentSecSeq(canContinueSecSeq);
                        FlightInfoManager.getInstance().setCAName(CA.Name); // CA Name

                        MainActivity.this.runOnUiThread(() -> {
                            if (MessageBox.show("", "Remember to close", mContext, "Yes")) {
                                Intent intent = new Intent(mActivity, MenuActivity.class);
                                mActivity.startActivity(intent);
                                mActivity.finish();
                            }
                        });
                    }
                }
            }
        }
    }


    // (1) 讀取密碼設定檔
    private String getOldPassword() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        return prefs.getString(PASSWORD_FILE, "");
    }

    // (2) 設定新密碼
    private void setNewPassword(String newPassword) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PASSWORD_FILE, newPassword);
        editor.apply();
    }


    //menu內輸入密碼進入admin mode (增刪icon、返回預設home)
    public boolean onOptionsItemSelected(final MenuItem item) {

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        //彈跳視窗的View和Edittext
        View mView = layoutInflater.inflate(R.layout.user_input_dialog_box, null);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext).setView(mView);
        final EditText editPassword = mView.findViewById(R.id.userInputDialog);

        //兩個輸入框的視窗
        View mView02 = layoutInflater.inflate(R.layout.user_input_dialog_box_two, null);
        final AlertDialog.Builder dialogBuilder02 = new AlertDialog.Builder(mContext).setView(mView02);
        final EditText editPassword01 = mView02.findViewById(R.id.userInputDialog01);
        final EditText editPassword02 = mView02.findViewById(R.id.userInputDialog02);

        switch (item.getItemId()) {
            // 修改密碼
            case 0:
                dialogBuilder02
                    .setCancelable(false)
                    .setPositiveButton("Send", (dialogBox, id) -> {
                        if (editPassword01.getText().toString().equals(getOldPassword())) {
                            // 取得文字修改密碼
                            setNewPassword(editPassword02.getText().toString());
                            Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialogBox, id) -> dialogBox.cancel());

                //輸入文字完按 enter 會 onFocus 到 Send位置
                final AlertDialog dialog02 = dialogBuilder02.create();
                dialog02.setOnShowListener(dialogInterface -> {
                    final Button positive = dialog02.getButton(AlertDialog.BUTTON_POSITIVE);
                    editPassword02.setOnKeyListener((v, keyCode, event) -> {
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            positive.setFocusable(true);
                            positive.setFocusableInTouchMode(true);
                            positive.requestFocus();
                            return true;
                        }
                        return false;
                    });
                });
                dialog02.show();
                break;

            //離開Launcher
            case 1:
                dialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Send", (dialogBox, id) -> {
                        if (editPassword.getText().toString().equals(getOldPassword())) {
                            MainActivity.this.finish();
                            Process.killProcess(Process.myPid());
                            System.exit(1);
                        }
                    })
                    .setNegativeButton("Cancel", (dialogBox, id) -> dialogBox.cancel());
//                dialogBuilder.create().show();

                //輸入文字完按 enter 會 onFocus 到 Send位置
                final AlertDialog dialog = dialogBuilder.create();
                dialog.setOnShowListener(dialogInterface -> {
                    final Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    editPassword.setOnKeyListener((v, keyCode, event) -> {
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            positive.setFocusable(true);
                            positive.setFocusableInTouchMode(true);
                            positive.requestFocus();
                            return true;
                        }
                        return false;
                    });
                });
                dialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //menu鍵
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //群組id, itemId, item順序, item名稱
        menu.add(0, 0, 0, "Modify Password");
        menu.add(0, 1, 1, "Exit");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();//註釋掉這行,back鍵不退出activity
    }

    /*
    ** Key設定 **
    * 1. Search: onKeyDown 或onKeyUp
    * 2. Menu: onCreateOptionsMenu、onKeyDown 或 onKeyUp
    * 3. Back: onBackPressed、onKeyDown 或 onKeyUp
    * 4. Home: 點下Home鍵系統會發出 Intent.ACTION_CLOSE_SYSTEM_DIALOGS 廣播 (關閉系統Dialog的廣播)
    * */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return false;

            case KeyEvent.KEYCODE_HOME:
                // home鍵的消息在framework層就已經被攔截
                // 無法在應用中通過此方法監聽到home鍵的消息
                return false;

            case KeyEvent.KEYCODE_MENU:
                return super.onKeyDown(keyCode, event);

            case KeyEvent.KEYCODE_SEARCH:
                return false;
            case KeyEvent.KEYCODE_APP_SWITCH:
                return false;
            case KeyEvent.KEYCODE_VOLUME_UP:
                return false;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                return false;
            case KeyEvent.KEYCODE_CAMERA:
                return false;
            case KeyEvent.KEYCODE_DPAD_UP:
                return false;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return false;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return false;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 設定公用數據
     */
    private void setGlobalData() {

        //取得設定檔數據
        mDBFunctions = new DBFunctions(mContext, FlightData.SecSeq);
        Setting setting = mDBFunctions.getSettings();
        ArmsUtils.obtainAppComponentFromContext(mContext).extras().put(CacheDataTags.SETTING, setting);
    }
}