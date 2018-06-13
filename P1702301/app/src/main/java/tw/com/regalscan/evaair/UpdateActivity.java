package tw.com.regalscan.evaair;

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
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import timber.log.Timber;
import tw.com.regalscan.R;
import tw.com.regalscan.activities.ItemDetailActivity;
import tw.com.regalscan.activities.ItemPictureActivity;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.utils.Tools;


/* 根據資料庫分類分drawer & 載入
* 輸入雜誌編號或scan條碼去搜尋 DB
* scan 條碼後切換到此商品所在的抽屜+ 物品清單
* 修改後的物件裝入HashMap, 並在每次切換抽屜時檢查, 如果在Map內的話就修改顏色 */


public class UpdateActivity extends AppCompatActivity {
    //Scan
    private final String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private int soundid;
    private String barcodeStr;
    //判斷載入抽屜時是否是scan的觸發
    private boolean isScan = false;

    private Button btnReturn, btnNext;
    public Context mContext;
    public Activity mActivity;
    private EditText editItemNum;
    private Spinner spinnerDrawer;
    private ImageView imageViewSearch;

    //商品清單
    public ItemListPictureModifyAdapter adapter;
    private ListView itemListView;
    // 經過調整的商品清單
    private HashMap<String, ItemInfo> modifiedList = new HashMap<String, ItemInfo>();
    //調整數量視窗
    public final int ITEMS_DETAIL = 500;
    private boolean isItemClickModified = false;
    //鍵盤
    private InputMethodManager imm;
    // 抽屜清單
    private ArrayAdapter<String> drawerList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        init();

        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    }


    private void init() {
        mContext = this;
        mActivity = this;

        imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
        });

        //進下一頁時傳遞修改過的物品清單
        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(view -> {
            try {
                if (modifiedList.size() == 0) {
                    MessageBox.show("", "Adjust list can't be null", UpdateActivity.this, "Return");
                    return;
                }
                Bundle argument = new Bundle();
                argument.putSerializable("UpdateList", modifiedList);
                Intent intent = new Intent(mActivity, UpdateCpCheckActivity.class);
                intent.putExtras(argument);
                mActivity.startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Get update data error", mContext, "Return");
            }
        });

        //輸入文字搜尋
        editItemNum = findViewById(R.id.editItemNum);
        editItemNum.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String searchString = editItemNum.getText().toString();
                if (!searchString.equals("")) {
                    searchItem(searchString);
                    editItemNum.setText("");
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

        //物品清單
        itemListView = findViewById(R.id.lvItemList);
        adapter = new ItemListPictureModifyAdapter(mContext, itemListView);
        adapter.setIsMoneyVisible(false);
        adapter.setFilpperFunctionClickListener(itemPictureClickListener);
        adapter.setItemInfoClickListener(itemInformationClickListener);
        itemListView.setAdapter(adapter);


        // 取得所有抽屜號碼
        spinnerDrawer = findViewById(R.id.spinner01);
        StringBuilder err = new StringBuilder();
        DBQuery.DrawNoPack drawerPack = DBQuery.getAllDrawerNo(mContext, err, FlightData.SecSeq, null, false);
        if (drawerPack == null) {
            UpdateActivity.this.runOnUiThread(() -> MessageBox.show("", "Get drawer error", mContext, "Return"));
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
            return;
        }
        drawerList = new ArrayAdapter<>(this, R.layout.spinner_item);
        for (int i = 0; i < drawerPack.drawers.length; i++) {
            drawerList.add(drawerPack.drawers[i].DrawNo);
        }
        spinnerDrawer.setAdapter(drawerList);
        spinnerDrawer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //用抽屜分類來重載Drawers
                refreshAdapter(drawerList.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }


    //使用雜誌編號或條碼等進行search
    private void searchItem(String itemNum) {
        try {
            imm.hideSoftInputFromWindow(editItemNum.getWindowToken(), 0);

            StringBuilder err = new StringBuilder();
            DBQuery.ItemDataPack itemPack = DBQuery.getProductInfo(
                mContext, err, FlightData.SecSeq, itemNum, null, 2);

            //航段編號, 商品編號、雜誌編號或商品條碼, 抽屜編號
            if (itemPack == null) {
                MessageBox.show("", "Pno code error", mContext, "Return");
                return;
            }

            //將商品加入adapter, update的stock抓endQty
            adapter.clear();
            // 比對有沒有在修改清單內,
            // 沒有的話用庫存原始數量
            if (modifiedList.get(itemPack.items[0].ItemCode) != null) {
                // 有的話拿修改清單的數量放入，和顏色
                adapter.addItem(
                    itemPack.items[0].ItemCode,
                    itemPack.items[0].DrawNo + "-" + itemPack.items[0].SerialCode, "US", itemPack.items[0].ItemPriceUS,
                    itemPack.items[0].ItemName, itemPack.items[0].StartQty, modifiedList.get(itemPack.items[0].ItemCode).getIntegerQty()
                );
                adapter.modifiedItemColorChange(0, true);
            }
            // 或以前有沒有修改過(end==start為沒修改)
            else if (itemPack.items[0].EndQty != itemPack.items[0].StartQty) {
                adapter.addItem(
                    itemPack.items[0].ItemCode,
                    itemPack.items[0].DrawNo + "-" + itemPack.items[0].SerialCode, "US", itemPack.items[0].ItemPriceUS,
                    itemPack.items[0].ItemName, itemPack.items[0].StartQty, itemPack.items[0].EndQty
                );
                adapter.modifiedItemColorChange(0, true);
            }
            // 都沒修改過: 抓Start
            else {
                //String SerialCode, String moneyType, int price, String itemName, int stock, int qt
                adapter.addItem(
                    itemPack.items[0].ItemCode,
                    itemPack.items[0].DrawNo + "-" + itemPack.items[0].SerialCode, "US", itemPack.items[0].ItemPriceUS,
                    itemPack.items[0].ItemName, itemPack.items[0].StartQty, itemPack.items[0].StartQty
                );
            }
            adapter.notifyDataSetChanged();

            isScan = true;
            for (int i = 0; i < drawerList.getCount(); i++) {
                if (drawerList.getItem(i).equals(itemPack.items[0].DrawNo)) {
                    //切換到該商品的抽屜
                    spinnerDrawer.setSelection(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Search item error", mContext, "Return");
        }
    }


    // 重整item ListView, 以抽屜分類query DB, 載入商品
    // 1. 如果是Scan商品帶出的切換抽屜就載入單項商品
    // 2. 如果是手動選擇抽屜就載入該抽屜所有商品
    private void refreshAdapter(String drawerName) {
        StringBuilder err = new StringBuilder();
        DBQuery.ItemDataPack itemPack;
        try {
            if (isScan) // Scan商品
                isScan = false; //將單一商品顯示在adapter上

            else { // spinner選擇商品
                // default在all draw時呼叫12, 傳全null
                if (drawerName.equals("All Drawer")) {
                    itemPack = DBQuery.getProductInfo(mContext, err, FlightData.SecSeq, null, null, 2);
                } else { //其餘的就傳抽屜編號
                    itemPack = DBQuery.getProductInfo(mContext, err, FlightData.SecSeq, null, drawerName, 2);
                }

                if (itemPack == null) {
                    MessageBox.show("", "Get drawer info error", mContext, "Return");
                } else {
                    adapter.clear();

                    //將查詢到的itemPack丟進adapter內顯示, update的stock抓endQty
                    for (int i = 0; i < itemPack.items.length; i++) {
                        if (itemPack.items[i].EndQty != itemPack.items[i].StartQty) {
                            adapter.addItem(
                                itemPack.items[i].ItemCode,
                                itemPack.items[i].DrawNo + "-" + itemPack.items[i].SerialCode, "US", itemPack.items[i].ItemPriceUS,
                                itemPack.items[i].ItemName, itemPack.items[i].StartQty, itemPack.items[i].EndQty
                            );
                            adapter.modifiedItemColorChange(i, true);
                        }
                        // 都沒修改過: 抓Start
                        else {
                            //String SerialCode, String monyType, int price, String itemName, int stock, int qt
                            adapter.addItem(
                                itemPack.items[i].ItemCode,
                                itemPack.items[i].DrawNo + "-" + itemPack.items[i].SerialCode, "US", itemPack.items[i].ItemPriceUS,
                                itemPack.items[i].ItemName, itemPack.items[i].StartQty, itemPack.items[i].StartQty
                            );
                        }
                    }

                    //載入物品清單後用雜誌編號尋找修改清單，有match就更改顏色和數量
                    for (int i = 0; i < adapter.getCount(); i++) {
                        ItemInfo modifiedItem = modifiedList.get(adapter.getItem(i).getItemCode());

                        if (modifiedItem != null) {
                            adapter.modifiedItemChange(
                                i,
                                (Integer)modifiedItem.getQty(),
                                modifiedItem.getStock());
                            adapter.modifiedItemColorChange(i, true);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Set item error", mContext, "Return");
        }
    }


    //點選 Item 項目修改內容
    private ItemListPictureModifyAdapter.ItemInfoClickListener itemInformationClickListener
        = new ItemListPictureModifyAdapter.ItemInfoClickListener() {

        @Override
        public void txtItemInfoClickListener(int position) {
            try {
                //如果正在編輯就不能觸發第二次的activity彈跳視窗
                if (!isItemClickModified) {
                    isItemClickModified = true;

                    ItemInfo item = adapter.getItem(position);
                    Intent intent = new Intent();
                    intent.setClass(mActivity, ItemDetailActivity.class);

                    //String itemCode, String monyType, int price, String itemName, int stock, int qt
                    intent.putExtra("item", item);
                    intent.putExtra("index", String.valueOf(position));
                    intent.putExtra("canModifiedToZero", "true");
                    intent.putExtra("fromWhere", "UpdateActivity");
                    startActivityForResult(intent, ITEMS_DETAIL);
                }
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Modify item error", mContext, "Return");
            }
        }
    };


    //點選 Item 圖片放大圖片
    private ItemListPictureModifyAdapter.ItemListFunctionClickListener itemPictureClickListener
        = new ItemListPictureModifyAdapter.ItemListFunctionClickListener() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //調整數量
        if ((requestCode == ITEMS_DETAIL) && resultCode == RESULT_OK) {

            try {
                int index = Integer.valueOf((String)data.getExtras().get("index"));
                String newQty = (String)data.getExtras().get("newQty");

                ItemInfo modifiedItem = adapter.getItem(index);

                adapter.modifiedItemChange(index, Integer.valueOf(newQty), modifiedItem.getStock());

                adapter.modifiedItemColorChange(index, true);
                //修改過的item加入清單
                modifiedList.put(modifiedItem.getItemCode(), modifiedItem);

                // 如果調整後的數量不等於stock，紅字顯示
//                if( Integer.valueOf(newQty) != MODIFIED_ITEM.getStock() ){
//                    adapter.modifiedItemColorChange(index, true);
//                    //修改過的item加入清單
//                    modifiedList.put( MODIFIED_ITEM.getItemCode(), MODIFIED_ITEM );
//                }
//                //調整回來就改回黑字，從modifiedList刪掉
//                else{
//                    adapter.modifiedItemColorChange(index, false);
//                    if(modifiedList.get( MODIFIED_ITEM.getItemCode() )!=null){
//                        modifiedList.remove( MODIFIED_ITEM.getItemCode() );
//                    }
//                }
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Modify item error", mContext, "Return");
                return;
            }
        }
        isItemClickModified = false;
        super.onActivityResult(requestCode, resultCode, data);
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
            barcodeStr = new String(barcode, 0, barcodelen);

            //用掃到的條碼搜尋物品
            searchItem(barcodeStr);

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


//    private void enableExpandableList() {
//        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle("");
//        setSupportActionBar(toolbar);
//        ExpandableListView expandableListView = findViewById(R.id.expendlist);
//        NavigationDrawer navigationDrawer = new NavigationDrawer(mActivity, mContext, drawerLayout, toolbar, expandableListView);
//    }

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

    //鎖返回
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
