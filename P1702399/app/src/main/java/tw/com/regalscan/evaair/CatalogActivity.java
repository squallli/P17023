package tw.com.regalscan.evaair;

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
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

import tw.com.regalscan.R;
import tw.com.regalscan.activities.ItemPictureActivity;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.utils.Tools;


public class CatalogActivity extends AppCompatActivity {


    //Scan
    private final String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private int soundid;
    private String barcodeStr;

    private EditText editItemNum;
    private Spinner spinnerDrawer;
    private ImageView imageViewSearch;

    private Button btnReturn;
    public Context mContext;
    public Activity mActivity;

    private ItemListPictureModifyAdapter adapter;
    private ListView itemListView;
    //鍵盤
    private InputMethodManager imm;

    //判斷載入抽屜時是否是scan的觸發
    private boolean isScan = false;

    //所有抽屜號碼
    private ArrayAdapter<String> listDrawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        init();



        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void init() {
        mContext = this;
        mActivity = this;
        enableExpandableList();
        imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
            }
        });

        //輸入文字搜尋
        editItemNum = findViewById(R.id.editItemNum);
        editItemNum.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    String searchString = editItemNum.getText().toString();
                    if (!searchString.equals("")) {
                        //search item
                        searchItem(searchString);
                        editItemNum.setText("");
                    } else {
                        imm.hideSoftInputFromWindow(editItemNum.getWindowToken(), 0);
                    }
                    return true;
                }
                return false;
            }
        });

        //放大鏡搜尋
        imageViewSearch = findViewById(R.id.imageViewSearch);
        imageViewSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editItemNum.requestFocus();
                imm.showSoftInput(editItemNum, 0);
            }
        });

        //物品清單
        itemListView = findViewById(R.id.lvItemList);
        adapter = new ItemListPictureModifyAdapter(mContext, itemListView);
        adapter.setIsModifiedItem(false);
        adapter.setIsPictureZoomIn(true);
        adapter.setIsRightTwoVisible(false);
        //點選Item圖片放大圖片
        adapter.setFilpperFunctionClickListener(new ItemListPictureModifyAdapter.ItemListFunctionClickListener() {
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
        });
        itemListView.setAdapter(adapter);

        // 取得所有抽屜號碼
        spinnerDrawer = findViewById(R.id.spinner01);
        StringBuilder err = new StringBuilder();
        DBQuery.DrawNoPack drawerPack = DBQuery.getAllDrawerNo(
            mContext, err, FlightData.SecSeq, null, false);
        if (drawerPack == null) {
            CatalogActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MessageBox.show("", "Get drawer error", mContext, "Return");
                }
            });
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
            return;
        }
        listDrawer = new ArrayAdapter<String>(this, R.layout.spinner_item);
        for (int i = 0; i < drawerPack.drawers.length; i++) {
            listDrawer.add(drawerPack.drawers[i].DrawNo);
        }
        spinnerDrawer.setAdapter(listDrawer);
        spinnerDrawer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //用抽屜分類來重載Drawers
                refreshAdapter(listDrawer.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }

    //使用雜誌編號或條碼等進行search
    private void searchItem(String itemNum) {
        imm.hideSoftInputFromWindow(editItemNum.getWindowToken(), 0);

        StringBuilder err = new StringBuilder();
        DBQuery.ItemDataPack itemPack = DBQuery.getProductInfo(
            mContext, err, FlightData.SecSeq, itemNum, null, 2);

        //航段編號, 商品編號、雜誌編號或商品條碼, 抽屜編號
        if (itemPack == null) {
            CatalogActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MessageBox.show("", "Pno code error", mContext, "Return");
                }
            });
            return;
        }

        //將商品加入adapter, update的stock抓endQty
        adapter.clear();
        //String SerialCode, String monyType, int price, String itemName, int stock, int qt
        adapter.addItem(
            itemPack.items[0].ItemCode, itemPack.items[0].DrawNo + "-" + itemPack.items[0].SerialCode,
            "US", itemPack.items[0].ItemPriceUS,
            itemPack.items[0].ItemName, itemPack.items[0].EndQty, itemPack.items[0].EndQty
        );
        adapter.notifyDataSetChanged();
        isScan = true;

        for (int i = 0; i < listDrawer.getCount(); i++) {
            if (listDrawer.getItem(i).equals(itemPack.items[0].DrawNo)) {
                //切換到該商品的抽屜
                spinnerDrawer.setSelection(i);
            }
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
                    itemPack = DBQuery.getProductInfo(mContext, err,
                        FlightData.SecSeq, null, null, 2);
                } else { //其餘的就傳抽屜編號
                    itemPack = DBQuery.getProductInfo(mContext, err,
                        FlightData.SecSeq, null, drawerName, 2);
                }

                if (itemPack == null) {
                    CatalogActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MessageBox.show("", "Get drawer info error", mContext, "Return");
                        }
                    });
                } else {
                    adapter.clear();

                    //將查詢到的itemPack丟進adapter內顯示, update的stock抓endQty
                    for (int i = 0; i < itemPack.items.length; i++) {
                        //String SerialCode, String monyType, int price, String itemName, int stock, int qt
                        adapter.addItem(
                            itemPack.items[i].ItemCode, itemPack.items[i].DrawNo + "-" + itemPack.items[i].SerialCode,
                            "US", itemPack.items[i].ItemPriceUS,
                            itemPack.items[i].ItemName, itemPack.items[i].EndQty, itemPack.items[i].EndQty
                        );
                    }
                    adapter.notifyDataSetChanged();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            soundpool.play(soundid, 1, 1, 0, 0, 1);
            mVibrator.vibrate(100);

            byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
            int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
            byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte) 0);
            android.util.Log.i("debug", "----codetype--" + temp);
            barcodeStr = new String(barcode, 0, barcodelen);

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


    private void enableExpandableList() {
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ExpandableListView expandableListView = findViewById(R.id.expendlist);
        NavigationDrawer navigationDrawer = new NavigationDrawer(mActivity, mContext, drawerLayout, toolbar, expandableListView);
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
