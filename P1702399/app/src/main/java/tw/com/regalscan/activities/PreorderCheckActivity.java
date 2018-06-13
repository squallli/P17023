package tw.com.regalscan.activities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.gson.Gson;
import com.regalscan.sqlitelibrary.TSQL;
import tw.com.regalscan.R;
import tw.com.regalscan.component.AESEncrypDecryp;
import tw.com.regalscan.component.MagReadService;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.MagReader;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.utils.Tools;

public class PreorderCheckActivity extends Activity {
    private Button btnReturn, btnOk;
    private EditText editCreditCard, editTravelNum;
    private RadioGroup rgroup;
    private Context mContext;
    private Activity mActivity;
    private RadioButton radioCredit, radioTravel;
    private DBQuery.PreorderInformation listDetail;
    private ArrayList<String> cardData;
    //鍵盤
    private InputMethodManager imm;
    private String track1 = "";
    private String track2 = "";
    private MagReadService mReadService;


    private static class MagnetHandler extends Handler {
        private WeakReference<PreorderCheckActivity> weakActivity;

        MagnetHandler(PreorderCheckActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            PreorderCheckActivity handlerActivity = weakActivity.get();

            switch (msg.what) {
                case MagReadService.MESSAGE_READ_MAG:
                    handlerActivity.track1 = msg.getData().getString(MagReadService.CARD_TRACK1);
                    handlerActivity.track2 = msg.getData().getString(MagReadService.CARD_TRACK2);

                    try {
                        // 1. 偵測VISA等卡 (Credit Card)
                        MagReader magReader = new MagReader(FlightData.FlightDate);

                        // CardType, CardNo, 持卡人名稱, 到期日, ServiceCode
                        handlerActivity.cardData = magReader.GetCardData(false, handlerActivity.track1, handlerActivity.track2);

                        // cardData.get(0): 0為失敗, 1為成功
                        // size>5: 有資料, size==2: cardData.get(1)為錯誤訊息
                        if (handlerActivity.cardData.size() < 5) {
                            MessageBox.show("", handlerActivity.cardData.get(1), handlerActivity, "Return");
                            return;
                        }

                        // 確認黑名單卡
                        StringBuilder err = new StringBuilder();
                        if (DBQuery.checkBlackCard(handlerActivity, err, handlerActivity.cardData.get(0), handlerActivity.cardData.get(1))) {
                            MessageBox.show("", err.toString(), handlerActivity, "Return");
                            return;
                        }

                        // 將卡號填入輸入框
                        if (handlerActivity.radioCredit.isChecked()) {
                            handlerActivity.editCreditCard.setText(handlerActivity.cardData.get(1));
                        } else if (handlerActivity.radioTravel.isChecked()) {
                            handlerActivity.editTravelNum.setText(handlerActivity.cardData.get(1));
                        } else {
                            MessageBox.show("", "Please choose a verify type", handlerActivity, "Return");
                            return;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        MessageBox.show("", "Please retry", handlerActivity, "Return");
                        TSQL _TSQL = TSQL.getINSTANCE(handlerActivity, FlightData.SecSeq, "P17023");
                        _TSQL.WriteLog(FlightData.SecSeq, "System", "PreorderCheckActivity", "MagReadService", e.getMessage());
                        return;
                    }
                    break;
                case MagReadService.MESSAGE_OPEN_MAG:
                    break;
                case MagReadService.MESSAGE_CHECK_FAILE:
                    break;
                case MagReadService.MESSAGE_CHECK_OK:
                    break;
            }
        }
    }


//    private final Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MagReadService.MESSAGE_READ_MAG:
//                    track1 = msg.getData().getString(MagReadService.CARD_TRACK1);
//                    track2 = msg.getData().getString(MagReadService.CARD_TRACK2);
//
//                    try {
//                        // 1. 偵測VISA等卡 (Credit Card)
//                        MagReader magReader = new MagReader(FlightData.FlightDate);
//
//                        // CardType, CardNo, 持卡人名稱, 到期日, ServiceCode
//                        cardData = magReader.GetCardData(false, track1, track2);
//
//                        // cardData.get(0): 0為失敗, 1為成功
//                        // size>5: 有資料, size==2: cardData.get(1)為錯誤訊息
//                        if (cardData.size() < 5) {
//                            MessageBox.show("", cardData.get(1), mActivity, "Return");
//                            return;
//                        }
//
//                        // 確認黑名單卡
//                        StringBuilder err = new StringBuilder();
//                        if (DBQuery.checkBlackCard(mContext, err, cardData.get(0), cardData.get(1))) {
//                            MessageBox.show("", err.toString(), mActivity, "Return");
//                            return;
//                        }
//
//                        // 將卡號填入輸入框
//                        if(radioCredit.isChecked()){
//                            editCreditCard.setText(cardData.get(1));
//                        }else if(radioTravel.isChecked()){
//                            editTravelNum.setText(cardData.get(1));
//                        }else{
//                            MessageBox.show("", "Please choose a verify type", PreorderCheckActivity.this, "Return");
//                            return;
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        MessageBox.show("", "Please retry", mActivity, "Return");
//                        TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
//                        _TSQL.WriteLog(FlightData.SecSeq,
//                            "System", "PreorderCheckActivity", "MagReadService", e.getMessage());
//                        return;
//                    }
//
//                    break;
//                case MagReadService.MESSAGE_OPEN_MAG:
//                    break;
//                case MagReadService.MESSAGE_CHECK_FAILE:
//                    break;
//                case MagReadService.MESSAGE_CHECK_OK:
//                    break;
//            }
//        }
//    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_preorder_check);
        this.setFinishOnTouchOutside(false);


        mContext = this;
        mActivity = this;
        imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
        Handler magnetHandler = new MagnetHandler(this);
        mReadService = new MagReadService(mActivity, magnetHandler, mActivity);

        //整個購物車item
        Bundle argument = getIntent().getExtras();
        if (argument != null) {
            String itemString = argument.getString("ListDetail");
            Gson gson = new Gson();
            listDetail = gson.fromJson(itemString, DBQuery.PreorderInformation.class);
        }

        editCreditCard = findViewById(R.id.editCreditCard);
        editCreditCard.setInputType(InputType.TYPE_NULL);
        editTravelNum = findViewById(R.id.editTravelNum);
        radioCredit = findViewById(R.id.radioCreditCard);
        radioTravel = findViewById(R.id.radioTravelNum);

        rgroup = findViewById(R.id.rgroup);
        rgroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radioCreditCard:
                    editTravelNum.setEnabled(false);
                    editTravelNum.setText("");
                    editCreditCard.setEnabled(true);
                    editCreditCard.requestFocus();
                    mReadService = new MagReadService(mActivity, magnetHandler, mActivity);
                    break;

                case R.id.radioTravelNum:
                    editCreditCard.setEnabled(false);
                    editCreditCard.setText("");
                    editTravelNum.setEnabled(true);
                    editTravelNum.requestFocus();
                    mReadService = new MagReadService(mActivity, magnetHandler, mActivity);
                    break;
            }
//                setMagnetListener();
        });

        if (listDetail != null) {
            // pre-order卡號為空=銀聯 or 台胞證+護照驗證
            if (listDetail.CardNo.equals("")) {
                editCreditCard.setEnabled(false);
                radioCredit.setEnabled(false);
            } else {
                // 預設帶出客戶預購免稅品時登記之信用卡號，空服人員需刷讀卡號驗證
                editCreditCard.setEnabled(true);
                radioCredit.setEnabled(true);
                radioCredit.setChecked(true);
            }
        }

        // 卡片號碼
//        if(listDetail.PreorderNO.equals("2017051200001")){
//            editCreditCard.setText("5430451011014002");
//        }else if(listDetail.PreorderNO.equals("2017051200002")){
//            editCreditCard.setText("4938171010113304");
//        }

        //確定與取消
        btnOk = findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> {

            //沒選擇選項
            if (!radioCredit.isChecked() && !radioTravel.isChecked()) {
                MessageBox.show("", "Please choose a payment", PreorderCheckActivity.this, "Return");
                return;
            }

            // 1. 讀信用卡, 驗證刷的卡號是否和預購時的卡號一樣
            if (radioCredit.isChecked()) {
                imm.hideSoftInputFromWindow(editCreditCard.getWindowToken(), 0);
                if (editCreditCard.getText().toString().equals("")) {
                    MessageBox.show("", "Please input credit card number", PreorderCheckActivity.this, "Return");
                    return;
                }
                try {
                    // 解密資料庫卡號, 比對
                    if (!AESEncrypDecryp.getDectyptData(listDetail.CardNo, FlightData.AESKey).equals(editCreditCard.getText().toString())) {
                        MessageBox.show("", "Credit card number error", PreorderCheckActivity.this, "Return");
                        return;
                    }
                    Intent backIntent = getIntent();
                    backIntent.putExtra("VerifyType", "C");
                    setResult(RESULT_OK, backIntent);
                    PreorderCheckActivity.this.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageBox.show("", "Get data error", PreorderCheckActivity.this, "Return");
                }
            }

            // 2. 驗證護照號碼和銀聯卡
            else if (radioTravel.isChecked()) {
                imm.hideSoftInputFromWindow(editTravelNum.getWindowToken(), 0);
                if (editTravelNum.getText().toString().equals("")) {
                    MessageBox.show("", "Please input travel document number", PreorderCheckActivity.this, "Return");
                    return;
                }
                // 解密資料庫卡號, 比對
                try {
                    if (!AESEncrypDecryp.getDectyptData(listDetail.TravelDocument, FlightData.AESKey).equals(
                        editTravelNum.getText().toString())) {
                        MessageBox.show("", "Travel document number error", PreorderCheckActivity.this, "Return");
                        return;
                    }
                    Intent backIntent = getIntent();
                    backIntent.putExtra("VerifyType", "P");
                    setResult(RESULT_OK, backIntent);
                    PreorderCheckActivity.this.finish();
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageBox.show("", "Get data error", PreorderCheckActivity.this, "Return");
                }
            } else {
                MessageBox.show("", "Please choose a verify type", PreorderCheckActivity.this, "Return");
            }
        });

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> PreorderCheckActivity.this.finish());
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

    //鎖返回鍵和menu
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
    public void onResume() {
        super.onResume();
        if (mReadService != null)
            mReadService.start();
    }

    @Override

    protected void onDestroy() {
        if (mReadService != null)
            mReadService.stop();
        super.onDestroy();
    }

}
