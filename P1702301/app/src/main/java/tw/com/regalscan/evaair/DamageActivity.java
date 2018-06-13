package tw.com.regalscan.evaair;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.jess.arms.utils.ArmsUtils;
import com.regalscan.sqlitelibrary.TSQL;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import org.json.JSONArray;
import org.json.JSONObject;
import timber.log.Timber;
import tw.com.regalscan.R;
import tw.com.regalscan.activities.ItemDetailActivity;
import tw.com.regalscan.activities.ItemPictureActivity;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.SwipeListView;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.DamageItem;
import tw.com.regalscan.evaair.ife.IFEActivity01;
import tw.com.regalscan.evaair.ife.IFEDBFunction;
import tw.com.regalscan.evaair.ife.IFEFunction;
import tw.com.regalscan.utils.Cursor;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;


public class DamageActivity extends AppCompatActivity {


    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private int soundid;
    private String barcodeStr;
    private EditText editItemNum;
    private ImageView imageViewSearch;
    private Button btnReturn, btnSave;
    public Context mContext;
    public Activity mActivity;

    private static ItemListPictureModifyAdapter adapter;
    private SwipeListView itemListView;
    //調整數量視窗
    public final int ITEMS_DETAIL = 500;
    private boolean isItetmClickModified = false;

    //原始Query的Damage清單
    private static DBQuery.DamageItemPack originDamagePack;

    //鍵盤
    private InputMethodManager imm;
    private ProgressDialog mloadingDialog;

    //IFE
    private IFEDBFunction mIFEDBFunction;
    private IFEFunction mIFEFunction;

    private String modifiedItem = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_damage);

        init();
    }

    private void init() {
        mContext = this;
        mActivity = this;

        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
        mIFEDBFunction = new IFEDBFunction(mContext, FlightData.SecSeq);
        mIFEFunction = new IFEFunction(mContext);

        //物品清單
        itemListView = findViewById(R.id.lvItemList);
        adapter = new ItemListPictureModifyAdapter(mContext, itemListView, itemListView.getRightViewWidth());
        adapter.setIsModifiedItem(true);
        adapter.setItemInfoClickListener(itemInformationClickListener);
        adapter.setIsPictureZoomIn(true);
        adapter.setIsRightTwoVisible(false);
        adapter.setFilpperFunctionClickListener(itemPictureClickListener);

        //滑動刪除Button onClick
        adapter.setItemSwipeListener(position -> {

            if (modifiedItem.isEmpty()){
                if(!isDamageListModified()) {
                    deleteItem(position);
                }
                else
                {
                    MessageBox.show("", "Press save!", mContext, "Return");
                }
            }
            else
            {
                if (adapter.getItem(position).getItemCode().equals(modifiedItem)) {
                    deleteItem(position);
                }
                else {
                    MessageBox.show("", "Press save!", mContext, "Return");
                }
            }

//            if (modifiedItem.isEmpty()) {
//                modifiedItem = adapter.getItem(position).getItemCode();
//                deleteItem(position);
//            } else {
//                if (adapter.getItem(position).getItemCode().equals(modifiedItem)) {
//                    deleteItem(position);
//                } else {
//                    MessageBox.show("", "Press save!", mContext, "Return");
//                }
//            }
        });

        itemListView.setAdapter(adapter);

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            // 如有異動Damage商品，尚未save就按Return或樹狀選單欲跳出此畫面
            if (isDamageListModified()) {
                MessageBox.show("", "Press save!", mContext, "Return");
                return;
            }
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
        });

        //取得所有Damage清單
        StringBuilder err = new StringBuilder();
        originDamagePack = DBQuery.damageItemQty(mContext, err, null);
        if (originDamagePack == null) {
            if (MessageBox.show("", "Query damage data error", mContext, "Return")) {
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
                return;
            }
        }

        adapter.clear();
        //回傳的清單有Damage商品則存入, 沒有則不動作
        if (originDamagePack.damages != null) {
            for (DamageItem item : originDamagePack.damages) {
                //String SerialCode, String moneyType, int price, String itemName, int stock, int qt
                adapter.addItem(
                    item.ItemCode,
                    item.DrawNo + "-" + item.SerialCode, "US", item.ItemPriceUS, item.ItemName,
                    item.EndQty, item.DamageQty
                );
            }
        }
        adapter.notifyDataSetChanged();

        btnSave = findViewById(R.id.btnSave);
        btnSave.setEnabled(false);
        btnSave.setOnClickListener(view -> {

            if (adapter.isQtyMoreThanStock(false)) {
                MessageBox.show("", "Damage Qty more than Stock!", DamageActivity.this, "Return");
                return;
            }

            if (FlightData.IFEConnectionStatus) {
                ArrayList<DBQuery.ItemCodeAndNumber> array = countDamageList();
                String itemCode = array.get(0).getItemCode();
                int damageQty = array.get(0).getNumber();
                if (!mIFEDBFunction.getItemID(itemCode).equals("")) {
                    mIFEFunction.adjustItem(mIFEDBFunction.getIFEID(itemCode), 0 - damageQty)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally(Cursor::Normal)
                        .subscribe(ifeReturnData -> {
                            if (ifeReturnData.isSuccess()) {
                                saveData();
                            } else {
                                if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                                    if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                                        ArmsUtils.startActivity(IFEActivity01.class);
                                        ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                                        finish();
                                    }
                                }
                            }
                        });
                } else {
                    saveData();
                }
            } else {
                saveData();
            }
        });

        //輸入文字搜尋
        editItemNum = findViewById(R.id.editItemNum);
        editItemNum.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String searchString = editItemNum.getText().toString();
                if (!searchString.equals("")) {
                    //search item
                    searchItem(searchString);
                    editItemNum.setText("");

                    this.modifiedItem = mIFEDBFunction.getItemCode(searchString);
                } else {
                    imm.hideSoftInputFromWindow(editItemNum.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        //放大鏡搜尋
        imageViewSearch = findViewById(R.id.imageViewSearch);
        imageViewSearch.setOnClickListener(view -> {
            editItemNum.requestFocus();
            imm.showSoftInput(editItemNum, 0);
        });
        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }
    boolean isDel=false;
    private void deleteItem(int position) {
        adapter.removeItem(position);
        adapter.notifyDataSetChanged();
        itemListView.hiddenRight(itemListView.mPreItemView);
        btnSave.setEnabled(isDamageListModified());
        editItemNum.setEnabled(!isDamageListModified());
    }

    private static class PrinterHandler extends Handler {

        private WeakReference<DamageActivity> weakActivity;

        PrinterHandler(DamageActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            DamageActivity handlerActivity = weakActivity.get();
            Context handlerContext = handlerActivity.mContext;
            handlerActivity.mloadingDialog.dismiss();
            switch (msg.what) {
                case 1: // 沒紙
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData();
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 2: //Print error
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData();
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 3: //成功
                    handlerActivity.doPrintFinal();
                    break;
            }
        }
    }

    private void saveData() {
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        try {
            //儲存Damage, 建立Json Array回傳DB
            ArrayList<DBQuery.ItemCodeAndNumber> array = countDamageList();
            if (array == null) {
                DamageActivity.this.runOnUiThread(() -> {
                    mloadingDialog.dismiss();
                    MessageBox.show("", "Get damage list error", mContext, "Return");
                });
                return;
            }
            JSONArray returnPack = new JSONArray();
            for (DBQuery.ItemCodeAndNumber item : array) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("ItemCode", item.getItemCode());
                jsonObj.put("UpdateDamageQty", item.getNumber());
                returnPack.put(jsonObj);
            }

            StringBuilder err1 = new StringBuilder();
            originDamagePack = DBQuery.damageItemQty(mContext, err1, returnPack);
            if (originDamagePack == null) {
                DamageActivity.this.runOnUiThread(() -> {
                    mloadingDialog.dismiss();
                    MessageBox.show("", "Save damage error", mContext, "Return");
                });
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            DamageActivity.this.runOnUiThread(() -> {
                mloadingDialog.dismiss();
                MessageBox.show("", "Save damage error", mContext, "Return");
            });
            return;
        }

        if (originDamagePack.damages == null) {
            if (MessageBox.show("", "No damage list can print", mContext, "Ok")) {
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            }
        } else {
            printData();
        }
    }

    private void doPrintFinal() {
        if (MessageBox.show("", "Damage finished", mContext, "Ok")) {
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
        }
    }

    private void printData() {
        mloadingDialog.dismiss();
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        new Thread() {
            public void run() {
                // 列印目前所有Damage商品
                PrintAir printer = new PrintAir(mContext, Integer.valueOf(FlightData.SecSeq));
                try {
                    if (printer.printDamageList() == -1) {
                        printerHandler.sendMessage(Tools.createMsg(1));
                    } else {
                        printerHandler.sendMessage(Tools.createMsg(3));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                    _TSQL.WriteLog(FlightData.SecSeq, "System", "DamageActivity", "printDamageList", e.getMessage());
                    printerHandler.sendMessage(Tools.createMsg(2));
                }
            }
        }.start();
    }

    public static boolean isDamageListModified() {

        int adapterCount;
        int damageListCount;

        // 1. 查詢舊的損壞清單錯誤
        if (originDamagePack == null) {
            return false;
        }
        // 2. 取得舊的商品清單物品數量
        if (originDamagePack.damages == null) {
            damageListCount = 0;
        } else {
            damageListCount = originDamagePack.damages.length;
        }
        // 3, 取得adapter商品數量
        if (adapter == null) {
            adapterCount = 0;
        } else {
            adapterCount = adapter.getCount();
        }

        // 比對1: 舊的清單與adapter清單的數量不符合
        if (damageListCount != adapterCount) {
            return true;
        }

        // 比對2: 舊的清單是空的, 且與adapter的數量相同 (damage=0, adapter=0)
        if (originDamagePack.damages == null || adapter == null) {
            return false;
        }

        // 比對2: 兩個清單商品數量相同, 判斷itemCode和個別數量是否相同 (damage>0, adapter>0)
        // 全部走過一圈(可能順序有差)
        for (DamageItem oriItem : originDamagePack.damages) {
            boolean isFind = false;
            for (int i = 0; i < adapter.getCount(); i++) {
                if (oriItem.ItemCode.equals(adapter.getItem(i).getItemCode()) && oriItem.DamageQty == ((Integer)adapter.getItem(i).getQty())) {
                    isFind = true;
                    break;
                }
            }
            if (!isFind) {
                return true;
            }
        }
        return false;
    }

    //使用雜誌編號或條碼等進行search
    private void searchItem(String itemNum) {
        try {
            imm.hideSoftInputFromWindow(editItemNum.getWindowToken(), 0);

            StringBuilder err = new StringBuilder();
            DBQuery.ItemDataPack itemPack = DBQuery.getProductInfo(mContext, err, FlightData.SecSeq, itemNum, null, 2);

            //查無此商品
            if (itemPack == null) {
                DamageActivity.this.runOnUiThread(() -> MessageBox.show("", "Pno code error", mContext, "Return"));
                return;
            }

            //Scan到原有的商品就不動作
            if (adapter.getItem(itemPack.items[0].ItemCode) == null) {
                //String SerialCode, String moneyType, int price, String itemName,
                // int stock, int qt, boolean canDiscount
                adapter.addItem(
                    itemPack.items[0].ItemCode, itemPack.items[0].DrawNo + "-" + itemPack.items[0].SerialCode,
                    "US", itemPack.items[0].ItemPriceUS,
                    itemPack.items[0].ItemName,
                    itemPack.items[0].EndQty, 1
                );
                adapter.notifyDataSetChanged();
                btnSave.setEnabled(isDamageListModified());
                editItemNum.setEnabled(!isDamageListModified());


                isDel=false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Search item error", mContext, "Return");
        }
    }

    // 計算結算的Damage清單
    private ArrayList<DBQuery.ItemCodeAndNumber> countDamageList() {

        ArrayList<DBQuery.ItemCodeAndNumber> endDamagePack = new ArrayList<DBQuery.ItemCodeAndNumber>();
        try {
            // 1. adapter有東西
            if (adapter.getCount() > 0) {

                // 一、以adapter為主體比對damage pack
                for (int i = 0; i < adapter.getCount(); i++) {
                    boolean isMatch = false;

                    //(1) damage pack有東西, 比對
                    if (originDamagePack.damages != null) {
                        for (int j = 0; j < originDamagePack.damages.length; j++) {

                            // I. 有找到
                            if (adapter.getItem(i).getItemCode().equals(originDamagePack.damages[j].ItemCode)) {
                                // 數量不同
                                if ((Integer)adapter.getItem(i).getQty() != originDamagePack.damages[j].DamageQty) {
                                    //"差異量"= newQty-stock
                                    endDamagePack.add(new DBQuery.ItemCodeAndNumber(adapter.getItem(i).getItemCode(),
                                        (Integer)adapter.getItem(i).getQty() - originDamagePack.damages[j].DamageQty));
                                    isMatch = true;
                                }
                                //數量相同
                                else {
                                    isMatch = true;
                                }
                                break;
                            }
                        }

                        //II. 沒找到, 加入回傳清單
                        if (!isMatch) {
                            endDamagePack.add(new DBQuery.ItemCodeAndNumber(adapter.getItem(i).getItemCode(),
                                (Integer)adapter.getItem(i).getQty()));
                        }
                    }
                    // (2) damage pack沒東西, 將adapter加入回傳清單
                    else {
                        endDamagePack.add(new DBQuery.ItemCodeAndNumber(adapter.getItem(i).getItemCode(),
                            (Integer)adapter.getItem(i).getQty()));
                    }
                }

                // 二、以damage pack為主體比對adapter
                if (originDamagePack.damages != null) {
                    for (int j = 0; j < originDamagePack.damages.length; j++) {
                        boolean isMatch = false;
                        for (int i = 0; i < adapter.getCount(); i++) {
                            if (adapter.getItem(i).getItemCode().equals(originDamagePack.damages[j].ItemCode)) {
                                isMatch = true;
                            }
                        }
                        // adapter裡面沒找到, 刪掉
                        if (!isMatch) {
                            //"差異量"= newQty-stock
                            endDamagePack.add(new DBQuery.ItemCodeAndNumber(originDamagePack.damages[j].ItemCode,
                                0 - originDamagePack.damages[j].DamageQty));
                        }
                    }
                }

            }
            // 2. adapter沒東西
            else {
                // (1) damage pack有東西
                if (originDamagePack.damages != null) {
                    for (int j = 0; j < originDamagePack.damages.length; j++) {
                        //"差異量"= newQty-stock
                        endDamagePack.add(new DBQuery.ItemCodeAndNumber(originDamagePack.damages[j].ItemCode,
                            0 - originDamagePack.damages[j].DamageQty));
                    }
                }
                // (2) damage pack沒東西
            }

            return endDamagePack;
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get damage list error", mContext, "Return");
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //調整數量
        if ((requestCode == ITEMS_DETAIL) && resultCode == RESULT_OK) {

            int index = Integer.valueOf((String)data.getExtras().get("index"));
            String newQty = (String)data.getExtras().get("newQty");

            ItemInfo modifiedItem = adapter.getItem(index);

            this.modifiedItem = modifiedItem.getItemCode();

            //修改adapter的數量
            adapter.modifiedItemChange(
                index,
                Integer.valueOf(newQty),
                modifiedItem.getStock());
            adapter.notifyDataSetChanged();

            btnSave.setEnabled(isDamageListModified());
            editItemNum.setEnabled(!isDamageListModified());
        }
        isItetmClickModified = false;

        super.onActivityResult(requestCode, resultCode, data);
    }


    //點選 Item 項目修改內容
    private ItemListPictureModifyAdapter.ItemInfoClickListener itemInformationClickListener = new ItemListPictureModifyAdapter.ItemInfoClickListener() {

        @Override
        public void txtItemInfoClickListener(int position) {

            try {
                if (modifiedItem.isEmpty()) {
                    ItemInfo item = adapter.getItem(position);
                    //如果正在編輯就不能觸發第二次的activity彈跳視窗
                    if (!isItetmClickModified) {
                        isItetmClickModified = true;
                        Intent intent = new Intent();
                        intent.setClass(mActivity, ItemDetailActivity.class);

                        //String itemCode, String moneyType, int price, String itemName, int stock, int qt
                        intent.putExtra("index", String.valueOf(position));
                        intent.putExtra("item", item);
                        intent.putExtra("canModifiedToZero", "false");
                        intent.putExtra("fromWhere", "DamageActivity");
                        startActivityForResult(intent, ITEMS_DETAIL);
                    }
                } else {
                    if (adapter.getItem(position).getItemCode().equals(modifiedItem)) {
                        ItemInfo item = adapter.getItem(position);
                        //如果正在編輯就不能觸發第二次的activity彈跳視窗
                        if (!isItetmClickModified) {
                            isItetmClickModified = true;
                            Intent intent = new Intent();
                            intent.setClass(mActivity, ItemDetailActivity.class);

                            //String itemCode, String moneyType, int price, String itemName, int stock, int qt
                            intent.putExtra("index", String.valueOf(position));
                            intent.putExtra("item", item);
                            intent.putExtra("canModifiedToZero", "false");
                            intent.putExtra("fromWhere", "DamageActivity");
                            startActivityForResult(intent, ITEMS_DETAIL);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Modify item error", mContext, "Return");
            }
        }
    };


    //點選 Item 圖片放大圖片
    private ItemListPictureModifyAdapter.ItemListFunctionClickListener itemPictureClickListener = new ItemListPictureModifyAdapter.ItemListFunctionClickListener() {

        @Override
        public void toolItemFunctionClickListener(String itemInfo) {

            try {
                ItemInfo item = adapter.getItem(itemInfo);
                Intent intent = new Intent();
                intent.setClass(mActivity, ItemPictureActivity.class);
                intent.putExtra("itemCode", item.getItemCode());
                mActivity.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Zoom in item image error", mContext, "Return");
            }
        }
    };


    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            soundpool.play(soundid, 1, 1, 0, 0, 1);
            mVibrator.vibrate(100);

            byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
            int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
            byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte)0);
            Timber.tag("debug").i("----codeType--%s", temp);
            barcodeStr = new String(barcode, 0, barcodelen);

            searchItem(barcodeStr);
            modifiedItem = mIFEDBFunction.getItemCode(barcodeStr);
            barcodeStr = null;
        }

    };


    // Scan init
    private void initScan() {
        mScanManager = new ScanManager();
        mScanManager.openScanner();

        mScanManager.switchOutputMode(0);
        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
        soundid = soundpool.load("/etc/Scan_new.ogg", 1);
    }


    @Override
    protected void onResume() {
        super.onResume();
        initScan();

        editItemNum.setText("");

        IntentFilter filter = new IntentFilter();
        int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
        String[] value_buf = mScanManager.getParameterString(idbuf);
        if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
            filter.addAction(value_buf[0]);
        } else {
            String SCAN_ACTION = ScanManager.ACTION_DECODE;
            filter.addAction(SCAN_ACTION);
        }

        registerReceiver(mScanReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScanManager != null) {
            mScanManager.stopDecode();
        }
        unregisterReceiver(mScanReceiver);
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
}