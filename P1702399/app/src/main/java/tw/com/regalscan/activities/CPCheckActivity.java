package tw.com.regalscan.activities;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import tw.com.regalscan.R;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.RFIDReaderService;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.CrewInfo;
import tw.com.regalscan.db02.DBQuery.ReceiptList;
import tw.com.regalscan.evaair.ife.DealInProgressActivity;
import tw.com.regalscan.evaair.ife.IFEActivity01;
import tw.com.regalscan.evaair.upgrade.UpgradeActivity01;
import tw.com.regalscan.evaair.upgrade.UpgradePayActivity;
import tw.com.regalscan.evaair.upgrade.UpgradeRefundActivity01;

public class CPCheckActivity extends Activity {
    private Button btnReturn, btnOk;
    private Context mContext;
    public static Activity mActivity;
    private String fromWhere;
    private EditText editId, editPw;
    //鍵盤
    private InputMethodManager imm;
    private RFIDReaderService mRFIDReaderService;

    private Bundle argument;


    private static class RFIDHandler extends Handler {
        private WeakReference<CPCheckActivity> weakActivity;

        RFIDHandler(CPCheckActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            CPCheckActivity handlerActivity = weakActivity.get();
            Context handlerContext = handlerActivity.getApplicationContext();

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
                            MessageBox.show("", "Please check ID", handlerContext, "Return");
                            return;
                        }
                        handlerActivity.editId.setText(BlockData);
                        handlerActivity.editPw.setText(CP.Password);

                        handlerActivity.certificateCP();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_cp_check);
        this.setFinishOnTouchOutside(false);
        mContext = this;
        mActivity = this;
        imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);

        argument = getIntent().getExtras();
        fromWhere = argument.getString("fromWhere");
        this.setFinishOnTouchOutside(false);

        editId = findViewById(R.id.editId);
        editPw = findViewById(R.id.editPassword);

        Handler rfidHandler = new RFIDHandler(this);
        mRFIDReaderService = new RFIDReaderService(mActivity, rfidHandler);

        //確定與取消
        btnOk = findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> certificateCP());

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> {
            CPCheckActivity.this.finish();

            //開Upgrade畫面
            if (fromWhere.equals("UpgradeActivity01") || fromWhere.equals("MenuUpgradeRefund")) {
                if (!ActivityManager.isActivityInStock("UpgradeActivity01")) {
                    Intent mIntent03 = new Intent(mActivity, UpgradeActivity01.class);
                    mActivity.startActivity(mIntent03);
                }
            }

            //開IFE畫面
            else if (fromWhere.equals("IFEActivity01") || fromWhere.equals("MenuDealInProgress")) {
                if (!ActivityManager.isActivityInStock("IFEActivity01")) {
                    Intent mIntent03 = new Intent(mActivity, IFEActivity01.class);
                    mActivity.startActivity(mIntent03);
                }
            }

            //EX3
            else if (fromWhere.equals("IFEEX3Activity")) {
                this.finish();
            }
        });
    }

    private void certificateCP() {
        imm.hideSoftInputFromWindow(editPw.getWindowToken(), 0);

        //                editId.setText("150790");
        //                editPw.setText("0222");

        String CPId = editId.getText().toString().trim();
        String CPPw = editPw.getText().toString().trim();
        StringBuilder err = new StringBuilder();

        if (CPId.equals("")) {
            MessageBox.show("", "Please input CP ID", CPCheckActivity.this, "Return");
            return;
        }

        if (CPId.equals("")) {
            MessageBox.show("", "Please input CP password", CPCheckActivity.this, "Return");
            return;
        }

        //驗證CP ID和PW
        //驗證CP帳號密碼, 5
        if (DBQuery.getGetCrewInfo(mContext, err, CPId, CPPw) == null) {
            MessageBox.show("", "Please check ID and password", mContext, "Return");
            return;
        }

        if (DBQuery.checkEmployee(mContext, err, CPId) == null) {
            MessageBox.show("", "Not employee", mContext, "Return");
            return;
        }

        if (!FlightData.PurserID.equals(CPId)) {
            MessageBox.show("", "Wrong CP", CPCheckActivity.this, "Return");
            return;
        }

        // Upgrade Menu的CP Check
        // 與從其他地方用 Drawer 跳轉過來的 CP Check
        switch (fromWhere) {
            case "UpgradeActivity01":
            case "MenuUpgradeRefund": {

                // 取得所有單據號碼
                ReceiptList receiptNoList = DBQuery.getAllUpgradeRceciptNoList(mContext, err, null, "Sale");
                if (receiptNoList == null) {
                    MessageBox.show("", "Query order data error", mContext, "Return");
                    mActivity.finish();
                    return;
                }
                if (receiptNoList.rececipts == null) {
                    MessageBox.show("", "No upgrade receipt can be refund", mContext, "Return");
                    mActivity.finish();
                    return;
                }
                // 轉json傳入
                Gson gson = new Gson();
                String jsonPack = gson.toJson(receiptNoList);
                argument.putString("jsonPack", jsonPack);
                Intent intent = new Intent(mContext, UpgradeRefundActivity01.class);
                intent.putExtras(argument);
                mContext.startActivity(intent);
                mActivity.finish();
                break;
            }

            // Upgrade 購物車的CP Check
            case "UpgradeBasketActivity": {
                //傳遞物品內容的Bundle
                Intent intent = new Intent(mContext, UpgradePayActivity.class);
                intent.putExtras(argument);
                mContext.startActivity(intent);
                mActivity.finish();
                break;
            }
            case "IFEActivity01":
            case "MenuDealInProgress": {
                Intent intent = new Intent(mContext, DealInProgressActivity.class);
                mContext.startActivity(intent);
                mActivity.finish();
                break;
            }
            case "IFEEX3Activity":
                Intent intent = getIntent();
                intent.putExtra("isVerify", true);
                setResult(-1, intent);
                this.finish();
                break;
        }

    }

    @Override
    public void onBackPressed() {
    }


    @Override
    public void onStart() {
        mRFIDReaderService.start();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        mRFIDReaderService.Dispose();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mRFIDReaderService.Dispose();
        super.onPause();
    }

}
