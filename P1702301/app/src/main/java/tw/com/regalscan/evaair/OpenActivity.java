package tw.com.regalscan.evaair;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.regalscan.sqlitelibrary.TSQL;
import tw.com.regalscan.MainActivity;
import tw.com.regalscan.R;
import tw.com.regalscan.component.IMsgBoxOnClick;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.RFIDReaderService;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.LogType;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.CrewInfo;
import tw.com.regalscan.db02.DBQuery.FlightInfoPack;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;

public class OpenActivity extends AppCompatActivity {


    private ImageView lineImage;
    private Button btnRelogin, btnLogin, btnReturn;
    public Context mContext;
    public Activity mActivity;
    private Spinner spinnerSegment;
    private EditText editCAID, editCPID, editCPPw;

    //RFID
    private boolean setCAText = false; //設定自動填入CP或CA輸入框

    //紀錄所選擇的航段SecSeq
    private String mSecSeq = "";
    //目前可開櫃與可重開櫃的航段
    private String canReOpenSecSeq = "";
    private ArrayList<String> canOpenSecSeq = new ArrayList<>();
    private FlightInfoPack flightPack;
    private ArrayAdapter<String> flightArray;
    private PrintAir printer;
    private ProgressDialog mloadingDialog;
    private RFIDReaderService mRFIDReaderService;


    private static class RFIDHandler extends Handler {
        private WeakReference<OpenActivity> weakActivity;

        RFIDHandler(OpenActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            OpenActivity handlerActivity = weakActivity.get();
            Context handlerContext = handlerActivity.getApplication();

            switch (msg.what) {
                case RFIDReaderService.MSG_SHOW_BLOCK_DATA:
                    String UID = msg.getData().getString(RFIDReaderService.CARD_UID);
                    //員工證號
                    String BlockData = msg.getData().getString(RFIDReaderService.CARD_BLOCK_DATA);
                    //"EVA" or "EGAS", UID和BlockData為null的話就是ErrorString
                    String EMPLOYEE_TYPE = msg.getData().getString(RFIDReaderService.EMPLOYEE_TYPE);

                    if (UID != null && BlockData != null) {

                        StringBuilder err = new StringBuilder();
                        CrewInfo CP = DBQuery.getGetCrewPassword(handlerContext, err, BlockData);
                        if (CP == null) {
                            MessageBox.show("", "Please check ID", handlerActivity, "Return");
                            return;
                        }

                        if (handlerActivity.editCAID.getText().toString().trim().equals("")) {
                            handlerActivity.editCAID.setText(BlockData);
                            handlerActivity.setCAText = false;
                        } else if (handlerActivity.editCPID.getText().toString().trim().equals("")) {
                            handlerActivity.editCPID.setText(BlockData);
                            handlerActivity.editCPPw.setText(CP.Password);
                            handlerActivity.setCAText = true;
                        } else if (handlerActivity.setCAText) {
                            handlerActivity.editCAID.setText(BlockData);
                            handlerActivity.setCAText = false;
                        } else {
                            handlerActivity.editCPID.setText(BlockData);
                            handlerActivity.editCPPw.setText(CP.Password);
                            handlerActivity.setCAText = true;
                        }
                    } else {
                        // EMPLOYEE_TYPE為 Error String
                        MessageBox.show("", EMPLOYEE_TYPE, handlerContext, "Return");
                    }
                    break;

                case RFIDReaderService.MSG_OPEN_FAILED:
                    MessageBox.show("", "Please try again", handlerContext, "Return");
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);


        init();

        new Thread(this::checkCanOpenFlight).start();
        Handler rfidHandler = new RFIDHandler(this);
        mRFIDReaderService = new RFIDReaderService(mActivity, rfidHandler);
    }


    @Override
    public void onStart() {
        mRFIDReaderService.start();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MessageBox.drawerShow("", "Please check route!", mContext, "Ok", new IMsgBoxOnClick() {
            @Override
            public void onYesClick() {

            }

            @Override
            public void onNoClick() {

            }
        });
    }

    private void init() {
        mContext = this;
        mActivity = this;

        //取得儲存的所有航段資訊放入spinner
        spinnerSegment = findViewById(R.id.spinner);

        flightPack = DBQuery.getFlightInfo(mContext, new StringBuilder());
        if (flightPack == null) {
            OpenActivity.this.runOnUiThread(() -> {
                mloadingDialog.dismiss();
                MessageBox.show("", "Get Flight info error", mContext, "Return");
            });
            return;
        }
        flightArray = new ArrayAdapter<>(this, R.layout.spinner_item);
        for (DBQuery.FlightInfo info : flightPack.flights) {
            flightArray.add(info.DepStn + "-" + info.ArivStn);
        }
        spinnerSegment.setAdapter(flightArray);
        spinnerSegment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSecSeq = flightPack.flights[position].SecSeq;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 帳號密碼
        editCAID = findViewById(R.id.txtCaId);
        editCPID = findViewById(R.id.txtCpId);
        editCPPw = findViewById(R.id.txtPassword);


        // Ground帳號密碼
        RelativeLayout rlID = findViewById(R.id.rl_groundID);
        rlID.setVisibility(View.INVISIBLE);
        RelativeLayout rlPsw = findViewById(R.id.rl_groundPsw);
        rlPsw.setVisibility(View.INVISIBLE);

        //btn
        btnRelogin = findViewById(R.id.btnReLogin);
        btnRelogin.setOnClickListener(openBtnOnClick);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(openBtnOnClick);
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setVisibility(View.VISIBLE);
        btnReturn.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, MainActivity.class);
            mActivity.startActivity(intent);
            mActivity.finish();
        });
        lineImage = findViewById(R.id.line_01);
        lineImage.setVisibility(View.VISIBLE);
    }

    private void checkCanOpenFlight() {
        //確認目前選擇之航段編號是否可以開櫃, 4
        StringBuilder err = new StringBuilder();
        final DBQuery.CurrentOpenFlightPack currentFlightPack = DBQuery.getCurrentOpenFlightList(
            mContext, err);

        if (currentFlightPack == null) {
            OpenActivity.this.runOnUiThread(() -> MessageBox.show("", "Get flight info error", mContext, "Return"));
            return;
        }

        int firstCanOpenIndex = -1;
        for (int i = 0; i < currentFlightPack.openFlights.length; i++) {
            // 最後一筆Closed: 可重開櫃
            if (currentFlightPack.openFlights[i].Status.equals("Closed")) {
                canReOpenSecSeq = currentFlightPack.openFlights[i].SecSeq;
            }
            // 所有狀態為者""皆可開櫃
            if (currentFlightPack.openFlights[i].Status.equals("")) {
                if (canOpenSecSeq.size() == 0) {
                    firstCanOpenIndex = i;
                }
                canOpenSecSeq.add(currentFlightPack.openFlights[i].SecSeq);
            }
        }

        // 將可開櫃的第一個航段設為預設開櫃航段
        if (canOpenSecSeq.size() > 0) {
            for (int i = 0; i < flightPack.flights.length; i++) {
                if (flightPack.flights[i].DepStn.equals(currentFlightPack.openFlights[firstCanOpenIndex].DepStn) &&
                    flightPack.flights[i].ArivStn.equals(currentFlightPack.openFlights[firstCanOpenIndex].ArivStn)) {
                    final int position = i;
                    OpenActivity.this.runOnUiThread(() -> spinnerSegment.setSelection(position));
                }
            }
        } else if (!canReOpenSecSeq.equals("")) {
            // 沒有可開櫃的航段就設定最後一個航段
            OpenActivity.this.runOnUiThread(() -> spinnerSegment.setSelection(flightArray.getCount() - 1));
        }
    }


    private Button.OnClickListener openBtnOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

//            editCAID.setText("D91146");
//            editCPID.setText("150790");
//            editCPPw.setText("0222");

            String CPId = editCPID.getText().toString().trim();
            String CPPw = editCPPw.getText().toString().trim();
            String CAId = editCAID.getText().toString().trim();

            if (CAId.equals("") || CPId.equals("") || CPPw.equals("")) {
                MessageBox.show("", "ID or password can't be null", OpenActivity.this, "Return");
                return;
            }

            switch (v.getId()) {
                //新開櫃則點選Login按鈕
                case R.id.btnLogin:
                    if (canOpenSecSeq.size() == 0 || !canOpenSecSeq.contains(mSecSeq)) {
                        MessageBox.show("", "Can't open", mContext, "Return");
                        return;
                    }
                    loginOpen(CAId, CPId, CPPw);
                    break;

                // 重新開啟上一個航段作業則選擇Relogin按鈕
                case R.id.btnReLogin:
                    if (canReOpenSecSeq == null || !canReOpenSecSeq.equals(mSecSeq)) {
                        MessageBox.show("", "Can't reopen", mContext, "Return");
                        return;
                    }
                    loginReopen(CAId, CPId, CPPw);
                    break;
            }
        }
    };

    private void loginReopen(final String CAId, final String CPId, final String CPPw) {
        mloadingDialog = ProgressDialog.show(mContext, "", "Relogin...", true, false);

        new Thread() {
            public void run() {
                try {
                    StringBuilder err = new StringBuilder();
                    DBQuery.FlightInfo flightInfo = DBQuery.getFlightInfo(mContext, err, mSecSeq);

                    //由航段編號取得航段資訊, 2
                    if (flightInfo == null) {
                        OpenActivity.this.runOnUiThread(() -> {
                            mloadingDialog.dismiss();
                            MessageBox.show("", "Please check flight info", mContext, "Return");
                        });
                        return;
                    }

                    CrewInfo CA = DBQuery.getGetCrewInfo(mContext, err, CAId);
                    CrewInfo CP;
                    //驗證CA, 7
                    if (CA == null) {
                        OpenActivity.this.runOnUiThread(() -> {
                            mloadingDialog.dismiss();
                            MessageBox.show("", "Please check CA ID", mContext, "Return");
                        });
                        return;
                    }

                    //驗證CP帳號密碼, 5
                    CP = DBQuery.getGetCrewInfo(mContext, err, CPId, CPPw, "CP");
                    if (CP == null) {
                        OpenActivity.this.runOnUiThread(() -> {
                            mloadingDialog.dismiss();
                            MessageBox.show("", "Please check CP ID and password", mContext,
                                "Return");
                        });
                        return;
                    }

                    if (DBQuery.checkEmployee(mContext, err, CAId) == null || DBQuery.checkEmployee(mContext, err, CPId) == null) {
                        OpenActivity.this.runOnUiThread(() -> {
                            mloadingDialog.dismiss();
                            MessageBox.show("", "Not employee", mContext, "Return");
                        });
                        return;
                    }

                    //核對CP和CA是否與先前開櫃的人相同
                    if (!flightInfo.CrewID.equals(CAId)) { //CA
                        OpenActivity.this.runOnUiThread(() -> {
                            mloadingDialog.dismiss();
                            MessageBox.show("", "Wrong CA ID", mContext, "Return");
                        });
                        return;
                    }
                    if (!flightInfo.PurserID.equals(CPId)) { //CP
                        OpenActivity.this.runOnUiThread(() -> {
                            mloadingDialog.dismiss();
                            MessageBox.show("", "Wrong CP ID", mContext, "Return");
                        });
                        return;
                    }

                    //進行重新開櫃, 8
                    if (!(DBQuery.reopenFlightSecSeq(mContext, err, mSecSeq, CPId, CPPw, CAId))) {
                        OpenActivity.this.runOnUiThread(() -> {
                            mloadingDialog.dismiss();
                            MessageBox.show("", "Reopen flight failed", mContext, "Return");
                        });
                        return;
                    }

                    OpenActivity.this.runOnUiThread(() -> {
                        mloadingDialog.dismiss();
                        if (MessageBox.show("", "Relogin successful", mContext, "Ok")) {
                            Intent intent = new Intent(mActivity, MenuActivity.class);
                            mActivity.startActivity(intent);
                            mActivity.finish();
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    OpenActivity.this.runOnUiThread(() -> {
                        mloadingDialog.dismiss();
                        MessageBox.show("", "Reopen flight failed", mContext, "Return");
                    });
                }
            }
        }.start();
    }

    private void loginOpen(final String CAId, final String CPId, final String CPPw) {

        mloadingDialog = ProgressDialog.show(mContext, "", "Login...", true, false);
        try {
            StringBuilder err = new StringBuilder();
            CrewInfo CA = DBQuery.getGetCrewInfo(mContext, err, CAId);
            CrewInfo CP;
            //驗證CA, 7
            if (CA == null) {
                OpenActivity.this.runOnUiThread(() -> {
                    mloadingDialog.dismiss();
                    MessageBox.show("", "Please check CA ID", mContext, "Return");
                });
                return;
            }


            //驗證CP帳號密碼, 5
            CP = DBQuery.getGetCrewInfo(mContext, err, CPId, CPPw);
            if (CP == null) {
                OpenActivity.this.runOnUiThread(() -> {
                    mloadingDialog.dismiss();
                    MessageBox.show("", "Please check CP ID and password", mContext, "Return");
                });
                return;
            }


            if (DBQuery.checkEmployee(mContext, err, CAId) == null || DBQuery.checkEmployee(mContext, err, CPId) == null) {
                OpenActivity.this.runOnUiThread(() -> {
                    mloadingDialog.dismiss();
                    MessageBox.show("", "Not employee", mContext, "Return");
                });
                return;
            }

            //進行開櫃, 9
            if (!DBQuery.openFlightSecSeq(mContext, err, mSecSeq, CPId, CPPw, CAId)) {
                OpenActivity.this.runOnUiThread(() -> {
                    mloadingDialog.dismiss();
                    MessageBox.show("", "Open flight failed", mContext, "Return");
                });
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            OpenActivity.this.runOnUiThread(() -> {
                mloadingDialog.dismiss();
                MessageBox.show("", "Open flight failed", mContext, "Return");
            });
            return;
        }
        printData();
    }


    private static class PrinterHandler extends Handler {
        private WeakReference<OpenActivity> weakActivity;

        PrinterHandler(OpenActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            OpenActivity handlerActivity = weakActivity.get();
            Context handlerContext = handlerActivity.mContext;
            handlerActivity.mloadingDialog.dismiss();
            switch (msg.what) {
                case 1: // 沒紙
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No"))
                        handlerActivity.printData();
                    else handlerActivity.doPrintFinal();
                    break;

                case 2: //Print error
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No"))
                        handlerActivity.printData();
                    else handlerActivity.doPrintFinal();
                    break;

                case 3: //成功
                    handlerActivity.doPrintFinal();
                    break;
            }
        }
    }

//    private Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            mloadingDialog.dismiss();
//            switch(msg.what){
//                case 1: // 沒紙
//                    if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData();
//                    else doPrintFinal();
//                    break;
//
//                case 2: //Print error
//                    if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData();
//                    else doPrintFinal();
//                    break;
//
//                case 3: //成功
//                    doPrintFinal();
//                    break;
//            }
//        }
//    };

    private void doPrintFinal() {
        if (MessageBox.show("", "Login successful", mContext, "Ok")) {

            TSQL tsql = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
            tsql.WriteLog(FlightData.SecSeq, LogType.LOGIN, "", "", "Crew Login, SecSeq:" + FlightData.SecSeq + ", " + spinnerSegment.getSelectedItem().toString());

            Intent intent = new Intent(mActivity, MenuActivity.class);
            mActivity.startActivity(intent);
            mActivity.finish();
        }
    }

    private void printData() {
        mloadingDialog.dismiss();
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        new Thread() {
            public void run() {
                try {
                    // 列印開櫃報表
                    printer = new PrintAir(mContext, Integer.valueOf(mSecSeq));
                    if (printer.printBeginInventory() == -1) {
                        printerHandler.sendMessage(Tools.createMsg(1));
                    } else {
                        printerHandler.sendMessage(Tools.createMsg(3));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                    _TSQL.WriteLog(FlightData.SecSeq,
                        "System", "OpenActivity", "printBeginInventory", e.getMessage());
                    printerHandler.sendMessage(Tools.createMsg(2));
                }
            }
        }.start();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRFIDReaderService.Dispose();
    }

}