package tw.com.regalscan.activities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import com.regalscan.sqlitelibrary.TSQL;
import timber.log.Timber;
import tw.com.regalscan.R;
import tw.com.regalscan.adapters.DiscountAdapter;
import tw.com.regalscan.component.MagReadService;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.RFIDReaderService;
import tw.com.regalscan.component.SpinnerHideItemAdapter;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.MagReader;
import tw.com.regalscan.db02.DBQuery;

public class DiscountCheckActivity extends Activity {

    private final String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action

    private Spinner spinner;
    private EditText editCardInfo;

    //折扣list view
    private DiscountAdapter discountAdapter;
    private SpinnerHideItemAdapter identityList;

    // 所有的被動式折扣類別
    DBQuery.AllDiscountTypePack discountPack;
    private Context mContext;
    private String fromWhere;
    private Bundle argument;
    // private MagnetReader magReader;
    private RFIDReaderService mRFIDReaderService;
    private InputMethodManager imm;
    private String track1 = "";
    private String track2 = "";
    private MagReadService mReadService;

    // 暫存身分折扣清單
    private HashMap<String, String> discountHashmap;

    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private int soundid;


    private static class RFIDHandler extends Handler {

        private WeakReference<DiscountCheckActivity> weakActivity;

        RFIDHandler(DiscountCheckActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            DiscountCheckActivity handlerActivity = weakActivity.get();
            Context handlerContext = handlerActivity.getApplicationContext();

            switch (msg.what) {
                case RFIDReaderService.MSG_SHOW_BLOCK_DATA:
                    String UID = msg.getData().getString(RFIDReaderService.CARD_UID);
                    //員工證號
                    String BlockData = msg.getData().getString(RFIDReaderService.CARD_BLOCK_DATA);
                    //"EVA" or "EGAS", UID和BlockData為null的話就是ErrorString
                    String EMPLOYEE_TYPE = msg.getData().getString(RFIDReaderService.EMPLOYEE_TYPE);

                    if (UID != null && BlockData != null) {
                        handlerActivity.editCardInfo.setText(BlockData);
                        handlerActivity.authCard(BlockData);
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
        setContentView(R.layout.activity_discount);
        this.setFinishOnTouchOutside(false);
        Activity activity = this;
        mContext = this;
        Handler magnetHandler = new MagnetHandler(this);
        mReadService = new MagReadService(activity, magnetHandler, activity);
        Handler rfidHandler = new RFIDHandler(this);
        mRFIDReaderService = new RFIDReaderService(activity, rfidHandler);

        argument = getIntent().getExtras();
        if (argument != null) {
            fromWhere = argument.getString("fromWhere");
        }

        imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
        discountHashmap = new HashMap<>();

        //將現有的折扣列表放入清單
        discountAdapter = new DiscountAdapter(this, getIntent().getStringArrayListExtra("discountArrayList"));
        ListView discountListView = findViewById(R.id.listView);
        discountListView.setAdapter(discountAdapter);

        //discount取得所有的被動式折扣類別, 19
        StringBuilder err = new StringBuilder();
        discountPack = DBQuery.getAllDiscountType(this, err);
        if (discountPack == null) {
            MessageBox.show("", "Get discount list error", mContext, "Return");
            DiscountCheckActivity.this.finish();
        }
        // 將可使用的被動式折扣放入spinner
        ArrayList<String> list = new ArrayList<>();
        list.add("Choose identity");
        for (int i = 0; i < discountPack.discounts.length; i++) {
            list.add(discountPack.discounts[i].Type);
        }

        // 刷卡或感應折扣證件
        editCardInfo = findViewById(R.id.editCardInfo);

        // 下拉選單
        spinner = findViewById(R.id.spinner2);
        spinner.setAdapter(identityList);

        identityList = new SpinnerHideItemAdapter(this, R.layout.spinner_item, list, 0);
        identityList.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(identityList);

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String identity = spinner.getSelectedItem().toString();
                    switch (identity) {
                        // 聯名卡
                        case "AE":
                        case "AEG":
                        case "CUB":
                            // 會員卡
                        case "EC":
                        case "EP":
                        case "CD":
                        case "CE":
                        case "CG":
                            editCardInfo.setHint(getString(R.string.Please_Swipe_or_Tag_Card));
                            mRFIDReaderService.stop();
                            break;
                        case "CUSS":
                            editCardInfo.setHint("");
                            mRFIDReaderService.stop();
                            break;
                        case "PRE":
                            editCardInfo.setHint(getString(R.string.PRE));
                            mRFIDReaderService.stop();
                            break;
                        default:
                            if (identity.matches("^STAFF.*$")) {
                                // 員工卡: 於Staff table比對 (RFID)
                                editCardInfo.setHint(getString(R.string.Please_Swipe_or_Tag_Card));
                                mRFIDReaderService.start();
                            } else {
                                editCardInfo.setHint(getString(R.string.Please_Swipe_or_Tag_Card));
                                mRFIDReaderService.stop();
                            }
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageBox.show("", "Set identity error", mContext, "Return");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //確定與取消
        Button btnAccept = findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(v -> {
            imm.hideSoftInputFromWindow(editCardInfo.getWindowToken(), 0);

            // 手動輸入折扣身分
            String searchString = editCardInfo.getText().toString();
            if (!searchString.equals("") || spinner.getSelectedItem().toString().equals("CUSS")) {
                if (!authCard(searchString)) {
                    return;
                }
            } else {
                imm.hideSoftInputFromWindow(editCardInfo.getWindowToken(), 0);
            }

            //回傳discount No (卡號)和discount Type
            Intent backIntent = getIntent();
            if (fromWhere.equals("FragmentVIPPayCard") || fromWhere.equals("FragmentPayCard")) {
                backIntent.putExtra("discountHashMap", discountHashmap);
            } else {
                backIntent.putStringArrayListExtra("discountArrayList", discountAdapter.getCurrentDiscountList());
                backIntent.putExtra("discountHashMap", discountHashmap);
            }
            setResult(RESULT_OK, backIntent);
            DiscountCheckActivity.this.finish();
        });

        Button btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(v -> {
            imm.hideSoftInputFromWindow(editCardInfo.getWindowToken(), 0);
            DiscountCheckActivity.this.finish();
        });

        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        initScan();

        //Scanner處理
        IntentFilter filter = new IntentFilter();
        int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
        String[] value_buf = mScanManager.getParameterString(idbuf);
        if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
            filter.addAction(value_buf[0]);
        } else {
            filter.addAction(SCAN_ACTION);
        }

        registerReceiver(mScanReceiver, filter);
    }


    private static class MagnetHandler extends Handler {

        private WeakReference<DiscountCheckActivity> weakActivity;

        MagnetHandler(DiscountCheckActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            DiscountCheckActivity handlerActivity = weakActivity.get();

            switch (msg.what) {
                case MagReadService.MESSAGE_READ_MAG:
                    handlerActivity.track1 = msg.getData().getString(MagReadService.CARD_TRACK1);
                    handlerActivity.track2 = msg.getData().getString(MagReadService.CARD_TRACK2);
                    String identity = handlerActivity.spinner.getSelectedItem().toString();

                    try {
                        // 1. 會員卡
                        if (identity.equals("EC") || identity.equals("EP")
                            || identity.equals("CD") || identity.equals("CE") || identity.equals("CG")) {
                            if (handlerActivity.track2.length() < 10) {
                                MessageBox.show("", "Member card detect error", handlerActivity, "Return");
                                return;
                            }
                            handlerActivity.authCard(handlerActivity.track2.substring(0, 10));
                        }

                        // 2. 聯名卡
                        else {
                            MagReader magReader = new MagReader(FlightData.FlightDate);
                            ArrayList<String> memberCardData;
                            // CardType, CardNo, 持卡人名稱, 到期日, ServiceCode
                            if (identity.equals("UNIONP")) {
                                memberCardData = magReader.GetCardData(true, handlerActivity.track1, handlerActivity.track2);
                            } else {
                                memberCardData = magReader.GetCardData(false, handlerActivity.track1, handlerActivity.track2);
                            }

                            if (memberCardData.size() < 1) {
                                MessageBox.show("", "Credit card detect error", handlerActivity, "Return");
                                return;
                            }
                            // 加入折扣
                            handlerActivity.authCard(memberCardData.get(1));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        TSQL _TSQL = TSQL.getINSTANCE(handlerActivity, FlightData.SecSeq, "P17023");
                        _TSQL.WriteLog(FlightData.SecSeq,
                            "System", "DiscountCheckActivity", "MagReadService", e.getMessage());
                        MessageBox.show("", "Please retry", handlerActivity, "Return");
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

    // 驗證被動式身份折扣
    private boolean authCard(String cardNumber) {
        editCardInfo.setText("");
        StringBuilder err = new StringBuilder();
        StringBuilder err1 = new StringBuilder();
        String identity = spinner.getSelectedItem().toString();

        if (!identity.equals("PRE") && !identity.equals("CUSS")) {

            DBQuery.DiscountCheck coBrandedCard = DBQuery.checkCoBrandedCard(mContext, err1, cardNumber);
            DBQuery.DiscountCheck memberCard = DBQuery.checkMemnerCard(mContext, err, identity, cardNumber);

            if (identity.matches("^STAFF.*$")) {
                if (DBQuery.checkEmployee(mContext, err, cardNumber) == null && MessageBox.show("", "Please check " + identity + " ID", mContext, "Return")) {
                    return false;
                }
            } else {
                if (identity.equals("EC") || identity.equals("EP") || identity.equals("CD") || identity.equals("CE") || identity.equals("CG")) {
                    if (memberCard == null) {
                        MessageBox.show("", "Please check " + identity + " ID", mContext, "Return");
                        return false;
                    }
                } else {
                    if (coBrandedCard == null) {
                        MessageBox.show("", "Please check " + identity + " card", mContext, "Return");
                        return false;
                    }
                }
            }
        } else if (identity.equals("PRE")) {
            if (!cardNumber.contains(FlightData.FlightDate)) {
                MessageBox.show("", "Please check " + identity + " discount info", mContext, "Return");
                return false;
            }
        }

        if (discountHashmap == null) {
            discountHashmap = new HashMap<>();
        }
        discountHashmap.put(identity, cardNumber);

        // 將主動式折扣 insert在第一個index
        discountAdapter.insertItemToFirst(identity + " - " + cardNumber);
        discountAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public void onBackPressed() {
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mScanReceiver != null) {
            //Scanner處理
            IntentFilter filter = new IntentFilter();
            int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
            String[] value_buf = mScanManager.getParameterString(idbuf);
            if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
                filter.addAction(value_buf[0]);
            } else {
                filter.addAction(SCAN_ACTION);
            }

            registerReceiver(mScanReceiver, filter);
        }

        mRFIDReaderService.start();
        if (mReadService != null) {
            mReadService.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRFIDReaderService.Dispose();

        if (mReadService != null) {
            mReadService.stop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRFIDReaderService.Dispose();

        if (mReadService != null) {
            mReadService.stop();
        }

        unregisterReceiver(mScanReceiver);
    }

    // Scan init
    private void initScan() {

        mScanManager = new ScanManager();
        mScanManager.openScanner();

        mScanManager.switchOutputMode(0);
        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
        soundid = soundpool.load("/etc/Scan_new.ogg", 1);
    }


    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            soundpool.play(soundid, 1, 1, 0, 0, 1);
            mVibrator.vibrate(100);

            byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
            int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
            byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte)0);
            Timber.tag("debug").i("----codetype--" + temp);

            String barcodeStr = new String(barcode, 0, barcodelen);

            editCardInfo.setText(barcodeStr);
        }

    };
}
