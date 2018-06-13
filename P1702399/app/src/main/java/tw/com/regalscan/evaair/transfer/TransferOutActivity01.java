package tw.com.regalscan.evaair.transfer;

import java.util.ArrayList;
import java.util.HashMap;

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
import android.os.Vibrator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;
import timber.log.Timber;
import tw.com.regalscan.R;
import tw.com.regalscan.activities.ItemDetailActivity;
import tw.com.regalscan.activities.ItemPictureActivity;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.component.SwipeListView;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.Transfer;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.TransferOutItem;
import tw.com.regalscan.db02.DBQuery.TransferOutItemPack;
import tw.com.regalscan.utils.Tools;

public class TransferOutActivity01 extends AppCompatActivity {
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
    private TextView txtFrom;
    private ItemListPictureModifyAdapter adapter;
    private Button btnReturn, btnTransfer;
    public Context mContext;
    public Activity mActivity;

    //調整數量視窗
    public static final int ITEMS_DETAIL = 500;
    private boolean isitetmClickModified = false;
    private SwipeListView itemListView;

    //鍵盤
    private InputMethodManager imm;
    private String cartNo = "";

    private Transfer _TransferTranscation;

    //local紀錄的應Scan贈品清單
    private HashMap<String, ItemInfo> giftList = new HashMap<String, ItemInfo>();

    // 贈品提醒視窗有沒有顯示過
    private boolean isGiftMagShow = false;

    //local紀錄的adapter內所有商品清單
    private TransferOutItemPack transferOutItemPack;


    // Transfer QR code內格式，
    // 一次最多可以轉移六種Item，每種Item上限99個


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_out_01);


        init();
        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
    }

    private void init() {
        mContext = this;
        mActivity = this;
        enableExpandableList();
        imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();

            if (!ActivityManager.isActivityInStock("TransferActivity01")) {
                Intent mIntent = new Intent(mActivity, TransferActivity01.class);
                mActivity.startActivity(mIntent);
            }
        });

        //取得自己的車號
        cartNo = FlightData.CartNo;
        txtFrom = findViewById(R.id.txtFrom);
        txtFrom.setText("From: " + cartNo);


        // 下拉選單, 取得所有其他的車號塞進去
        StringBuilder err = new StringBuilder();
        spinnerDrawer = findViewById(R.id.spinner01);
        DBQuery.CartNoPack numbers = DBQuery.getAllCartList(mContext, err);
        if (numbers == null) {
            MessageBox.show("", "Get cart number error", mContext, "Return");
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
            return;
        }
        ArrayAdapter<String> drawerList = new ArrayAdapter<String>(this, R.layout.spinner_item);
        for (int i = 0; i < numbers.cartList.length; i++) {
            if (!cartNo.equals(numbers.cartList[i].CartNo)) {
                drawerList.add(numbers.cartList[i].CartNo);
            }
        }
        spinnerDrawer.setAdapter(drawerList);
        spinnerDrawer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != -1) {
                    try {
                        _TransferTranscation.SetCarTo(spinnerDrawer.getSelectedItem().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        MessageBox.show("", "Choose cart error", mContext, "Return");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        //取得要轉成 QR Code的String
        btnTransfer = findViewById(R.id.btnTransfer);
        btnTransfer.setOnClickListener(view -> {

            if (adapter.getCount() == 0) {
                MessageBox.show("", "Transfer list can't null", TransferOutActivity01.this, "Return");
                return;
            }
            if (spinnerDrawer.getSelectedItem().toString().equals(FlightData.CartNo)) {
                MessageBox.show("", "Please check cart number", TransferOutActivity01.this, "Return");
                return;
            }

            //商品訂購量大於可販售量(POS庫存量)，不可進行付款
            if (adapter.isQtyMoreThanStock(false)) {
                MessageBox.show("", "Qty not enough", mContext, "Return");
                return;
            }

            // 2. 檢查贈品未scan
            StringBuilder giftNotScanString = new StringBuilder();
            for (TransferOutItem singleItem : transferOutItemPack.items) {
                if (singleItem.GiftFlag.equals("Y") &&
                    singleItem.TransferQty <= singleItem.POSStock &&
                    singleItem.TransferQty > giftList.get(singleItem.ItemCode).getGiftScan()) {
                    giftNotScanString.append("Scan gift no. ").append(singleItem.SerialCode).append(" * ").append(singleItem.TransferQty - giftList.get(singleItem.ItemCode).getGiftScan()).append("\n");
                }
            }

            // 贈品有貨未scan的確認視窗
            if (!giftNotScanString.toString().equals("")) {
                if (isGiftMagShow) {
                    MessageBox.show("", giftNotScanString.toString(), mContext, "Ok");
                    isGiftMagShow = false;
                    return;
                }
                if (MessageBox.show("", "To transfer out?", mContext, "Yes", "No")) {
                    successTransfer();
                }
            } else {
                if (MessageBox.show("", "To transfer out?", mContext, "Yes", "No")) {
                    successTransfer();
                }
            }
        });

        //物品清單
        itemListView = findViewById(R.id.lvItemList);
        adapter = new ItemListPictureModifyAdapter(mContext, itemListView, itemListView.getRightViewWidth());
        adapter.setIsModifiedItem(true);
        adapter.setIsPictureZoomIn(true);
        adapter.setIsRightTwoVisible(false);
        adapter.setIsMoneyVisible(false);
        adapter.setFilpperFunctionClickListener(itemPictureClickListener);
        adapter.setItemInfoClickListener(itemInformationClickListener);
        //滑動刪除Button onClick
        adapter.setItemSwipeListener(position -> {
            try {
                //GiftFlag 贈品，不可單獨刪除
                if (adapter.getItem(position).getPrice() == 0) {
                    itemListView.hiddenRight(itemListView.mPreItemView);
                    MessageBox.show("", getString(R.string.Gift_Cannot_Removed), mContext, "Return");
                    return;
                }
                String itemCode = adapter.getItem(position).getItemCode();
                int count = 0 - (int)adapter.getItem(position).getQty();

                getDBData(_TransferTranscation.ModifyTransferList(itemCode, count), false);

                itemListView.hiddenRight(itemListView.mPreItemView);
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Delete item error", mContext, "Return");
            }
        });
        itemListView.setAdapter(adapter);

        //輸入文字搜尋
        editItemNum = findViewById(R.id.editItemNum);
        editItemNum.setOnKeyListener((v, keyCode, event) -> {
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
        });

        //放大鏡搜尋
        imageViewSearch = findViewById(R.id.imageViewSearch);
        imageViewSearch.setOnClickListener(view -> {
            editItemNum.requestFocus();
            imm.showSoftInput(editItemNum, 0);
        });

        try {
            // 設定 Transaction
            _TransferTranscation = new Transfer(mContext, FlightData.SecSeq,
                cartNo, spinnerDrawer.getSelectedItem().toString());
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get transfer info error", mContext, "Return");
            return;
        }

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }

    private void successTransfer() {
        final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        new Thread() {
            public void run() {
                try {

                    // 將贈品列表丟進去
                    for (String key : giftList.keySet()) {
                        ItemInfo item = giftList.get(key);
                        if (item.getGiftScan() > 0) {
                            _TransferTranscation.SetGiftScanQty(item.getItemCode(), item.getGiftScan());
                        }
                    }

                    JSONObject jsonout = _TransferTranscation.SaveTransferInfo();
                    if (jsonout.getString("ReturnCode").equals("0")) {
                        JSONArray jsonArray = jsonout.getJSONArray("ResponseData");
                        String TransferNo = jsonArray.getJSONObject(0).getString("QRData");
                        Bundle argument = new Bundle();
                        argument.putString("TransferList", TransferNo);
                        Intent intent = new Intent(mActivity, TransferOutActivity02.class);
                        intent.putExtras(argument);
                        mActivity.startActivity(intent);
                    } else {
                        TransferOutActivity01.this.runOnUiThread(() -> {
                            mloadingDialog.dismiss();
                            MessageBox.show("", "Save transfer out error", mContext, "Return");
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    TransferOutActivity01.this.runOnUiThread(() -> {
                        mloadingDialog.dismiss();
                        MessageBox.show("", "Save transfer out error", mContext, "Return");
                    });
                }
            }
        }.start();
    }


    private void enableExpandableList() {
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ExpandableListView expandableListView = findViewById(R.id.expendlist);
        NavigationDrawer navigationDrawer = new NavigationDrawer(mActivity, mContext, drawerLayout, toolbar, expandableListView);
    }

    //使用雜誌編號或條碼等進行search
    private void searchItem(String itemNum) {
        try {
            imm.hideSoftInputFromWindow(editItemNum.getWindowToken(), 0);

            StringBuilder err = new StringBuilder();
            DBQuery.ItemDataPack itemPack = DBQuery.getProductInfo(mContext, err, FlightData.SecSeq, itemNum, null, 0);
            //查無此商品
            if (itemPack == null) {
                MessageBox.show("", "Pno Code error", mContext, "Return");
                return;
            }


            // Scan到贈品
            // 1. 檢查local清單內是否存在此贈品
            // 2. 是否有被scan過, 有的話就return
            if (itemPack.items[0].ItemPriceUS == 0.0) {
                ItemInfo giftItem = giftList.get(itemPack.items[0].ItemCode);

                //gift不可單獨scan
                // 購物車是空的: 贈品不能單獨scan
                // 購物車有東西: Wrong gift
                if (adapter.getCount() > 0) {
                    if (giftItem == null) {
                        MessageBox.show("", "Wrong gift", mContext, "Return");
                        return;
                    }
                } else {
                    if (giftItem == null) {
                        MessageBox.show("", "Gift can't be transferred individually", mContext, "Return");
                        return;
                    }
                }

                //如果清單內存在此贈品，表示為可以調整數量的item
                //如果scan過的數量大於應贈送總數的話, 返回
                if ((Integer)giftItem.getQty() <= giftItem.getGiftScan()) {
                    MessageBox.show("", "Gift already scan", mContext, "Return");
                    return;
                }

                // 如果清單內存在此贈品, 但贈送量大於庫存量
                // 1. 庫存0, 應販售數量1, 已scan 0 // 要擋
                // 2. 庫存1, 應販售數量2, 已scan 0 // 不擋
                // 3. 庫存1, 應販售數量2, 已scan 1 // 不擋

                // 庫存=0要擋
                // 已scan數量>庫存 要擋
                if (giftItem.getStock() == 0 || giftItem.getStock() <= giftItem.getGiftScan()) {
                    MessageBox.show("", "Stock not enough", mContext, "Return");
                    return;
                }

                try {
                    _TransferTranscation.SetGiftScanQty(giftItem.getItemCode(), giftItem.getGiftScan() + 1);
                } catch (Exception e) {
                    MessageBox.show("", "Please try again", mContext, "Return");
                    return;
                }

                //小於等於應贈送總數的話就增加adapter內的數量, 並將對應物件數量存回去
                giftItem.setGiftScan(giftItem.getGiftScan() + 1);
                giftList.put(itemPack.items[0].ItemCode, giftItem);

                // 如果adapter內沒有此贈品, add
                if (adapter.getItem(giftItem.getItemCode()) == null) {
                    // 販售量大於庫存量
                    if ((int)giftItem.getQty() > giftItem.getStock()) {
                        //String itemCode, String monyType, int price, String itemName,
                        // int stock, int qty, boolan canDisCount, boolean isModified
                        adapter.addItem(
                            giftItem.getItemCode(), giftItem.getSerialNo(), "US",
                            giftItem.getPrice(), giftItem.getItemName(),
                            giftItem.getStock(), giftItem.getGiftScan(),
                            true, true);
                    } else {
                        adapter.addItem(
                            giftItem.getItemCode(), giftItem.getSerialNo(), "US",
                            giftItem.getPrice(), giftItem.getItemName(),
                            giftItem.getStock(), giftItem.getGiftScan(),
                            true, false);
                    }
                }
                // 有此贈品, 增加數量
                else {
                    //int position, int qty, int stock
                    adapter.modifiedItemChange(
                        adapter.getItemId(giftItem.getItemCode()),
                        giftItem.getGiftScan(), giftItem.getStock());

                    // 販售量大於庫存量
                    if ((int)giftItem.getQty() > giftItem.getStock()) {
                        adapter.modifiedItemColorChange(
                            adapter.getItemId(giftItem.getItemCode()), true);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            //Scan到普通item，包起來
            else {
                // 如果非小視窗調整商品數量
                // 是Scan或輸入ItemNo的話，在adapter內的商品就不動作
                if (adapter.getItem(itemPack.items[0].ItemCode) != null) {
                    return;
                }

                try {
                    getDBData(_TransferTranscation.ModifyTransferList(itemPack.items[0].ItemCode, 1), false);
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageBox.show("", "Get sales info error", mContext, "Return");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Search item error", mContext, "Return");
        }
    }

    private void getDBData(final JSONObject jsonInput, final boolean resetGiftMsg) {
        final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        new Thread() {
            public void run() {

                isGiftMagShow = !resetGiftMsg;

                final StringBuilder err = new StringBuilder(); // 解Jason

                TransferOutItemPack tmpPack = DBQuery.modifyTransfer(err, jsonInput);
                if (tmpPack == null) {
                    TransferOutActivity01.this.runOnUiThread(() -> {
                        mloadingDialog.dismiss();
                        MessageBox.show("", err.toString(), mContext, "Return");
                    });
                    return;
                }
                transferOutItemPack = tmpPack;

                /*-------- 將所有item放入adapter --------*/
                adapter.clear();

                //收到的贈品清單
                HashMap<String, TransferOutItem> newGiftList = new HashMap<String, TransferOutItem>();

                //分辨是否贈品
                for (int i = 0; i < transferOutItemPack.items.length; i++) {
                    int _Price = 100;
                    if (transferOutItemPack.items[i].GiftFlag.equals("N")) {
                        if (transferOutItemPack.items[i].TransferQty > transferOutItemPack.items[i].POSStock) {
                            adapter.addItem(
                                transferOutItemPack.items[i].ItemCode,
                                transferOutItemPack.items[i].DrawerNo + "-" + transferOutItemPack.items[i].SerialCode, "US",
                                _Price, transferOutItemPack.items[i].ItemName,
                                transferOutItemPack.items[i].POSStock, transferOutItemPack.items[i].TransferQty,
                                true, true);
                        } else {
                            adapter.addItem(
                                transferOutItemPack.items[i].ItemCode,
                                transferOutItemPack.items[i].DrawerNo + "-" + transferOutItemPack.items[i].SerialCode, "US",
                                _Price, transferOutItemPack.items[i].ItemName,
                                transferOutItemPack.items[i].POSStock, transferOutItemPack.items[i].TransferQty,
                                true, false);
                        }
                    }

                    // 贈品
                    else {
                        //先把整個回傳的贈品清單存起來，再一個個和現有的贈品清單比對
                        newGiftList.put(transferOutItemPack.items[i].ItemCode, transferOutItemPack.items[i]);
                    }
                }

                StringBuilder giftHindString = new StringBuilder();
                // 要移除的回傳贈品key
                ArrayList<String> removeList = new ArrayList<>();

                // 1. 如果有回傳的贈品
                if (newGiftList.size() > 0) {
                    // (1) 如果local清單有東西
                    if (giftList.size() > 0) {

                        //將現有清單比對新的清單
                        for (String oldKey : giftList.keySet()) {
                            TransferOutItem newItem = newGiftList.get(oldKey);

                            // 1. 如果回傳值無此贈品，直接remove
                            if (newItem == null) {
//                                giftList.remove(oldKey);
                                removeList.add(oldKey);
                            }

                            // 2. 有的話比對新的贈送數量和已scan的贈送數量
                            else {
                                ItemInfo oldItem = giftList.get(oldKey);

                                // (1) 如果新贈送數量大於舊數量, 顯示msg
                                // 庫存足夠
                                if (newItem.TransferQty > (Integer)oldItem.getQty() &&
                                    newItem.POSStock > (Integer)oldItem.getQty()) {
                                    giftHindString.append("Scan gift no. ").append(newItem.SerialCode).append("\n");
                                }

                                // (1) 如果新的應贈送數量與舊的不同，取代
                                if (newItem.TransferQty != (Integer)oldItem.getQty()) {
                                    oldItem.setQty(newItem.TransferQty);
                                }
                                // (2) 如果已Scan數量大於應贈送數量，取代
                                if (oldItem.getGiftScan() > newItem.TransferQty) {
                                    oldItem.setGiftScan(newItem.TransferQty);
                                }
                                // (3) 如果新的stock數量與舊的不同，取代
                                if (newItem.POSStock != oldItem.getStock()) {
                                    oldItem.setStock(newItem.POSStock);
                                }

                                // (5) 將已scan數量大於0的贈品加入adapter
                                if (oldItem.getGiftScan() > 0) {
                                    // 將所有local有Scan數量的贈品放到adapter內
                                    // 應贈送量大於庫存量, 紅色細體字
                                    // 贈品的 canDisCount= true;
                                    if ((Integer)oldItem.getQty() > oldItem.getStock() || oldItem.getStock() == 0) {
//                                        adapter.addItem(
//                                            oldItem.getItemCode(), oldItem.getSerialNo(), "US",
//                                            oldItem.getPrice(), oldItem.getItemName(),
//                                            oldItem.getStock(), oldItem.getGiftScan(),
//                                            true, true);
                                    } else {
                                        adapter.addItem(
                                            oldItem.getItemCode(), oldItem.getSerialNo(), "US",
                                            oldItem.getPrice(), oldItem.getItemName(),
                                            oldItem.getStock(), oldItem.getGiftScan(),
                                            true, false);
                                    }
                                }
                                // (6) 贈品庫存小於販售量, 且庫存為0, 無法scan
                                //     自動加入畫面上, 紅字
                                else {
                                    if ((Integer)oldItem.getQty() > oldItem.getStock() || oldItem.getStock() == 0) {
//                                            adapter.addItem(
//                                                oldItem.getItemCode(), oldItem.getSerialNo(), "US",
//                                                oldItem.getPrice(), oldItem.getItemName(),
//                                                oldItem.getStock(), 0,
//                                                true, true);
                                    }
                                }
                                // (7) 更新giftList
                                giftList.put(oldKey, oldItem);
                            }
                        }

                        // 將新的清單再一次比對local清單
                        for (String newKey : newGiftList.keySet()) {
                            ItemInfo oldItem = giftList.get(newKey);

                            // 如果有新的贈品, 加入local清單
                            if (oldItem == null) {
                                TransferOutItem newItem = newGiftList.get(newKey);
                                giftList.put(newItem.ItemCode,
                                    new ItemInfo(newItem.ItemCode,
                                        newItem.DrawerNo + "-" + newItem.SerialCode, "US", 0, newItem.ItemName,
                                        newItem.POSStock, newItem.TransferQty, 0, false, false));

                                // 庫存不足
                                if (newItem.TransferQty > newItem.POSStock || newItem.POSStock == 0) {
//                                    adapter.addItem(
//                                        newItem.ItemCode, newItem.SerialCode, "US", 0, newItem.ItemName,
//                                        newItem.POSStock, 0, true, true);
//                                    giftHindString.append("Qty not enough: " + newItem.SerialCode + "\n");
                                }
                                // 庫存足夠
                                else {
                                    // 加入Gift msg
                                    giftHindString.append("Scan gift no. " + newItem.SerialCode + "\n");
                                }
                            }
                        }

                        // 移除現有清單內多的贈品
                        for (String removeKey : removeList) {
                            giftList.remove(removeKey);
                        }
                    }

                    // (2) 如果local清單沒東西
                    else {
                        for (String key : newGiftList.keySet()) {
                            TransferOutItem item = newGiftList.get(key);
                            //String itemCode, String serialNo, String monyType, Double price, String itemName
                            //int stock, int qty, int ifeStock,int giftScan, boolean canDiscount, boolean isModified

                            giftList.put(item.ItemCode,
                                new ItemInfo(item.ItemCode,
                                    item.DrawerNo + "-" + item.SerialCode, "US", 0, item.ItemName,
                                    item.POSStock, item.TransferQty, 0,
                                    false, false));

                            // 庫存不足
                            if (item.TransferQty > item.POSStock || item.POSStock == 0) {
//                                adapter.addItem(
//                                    item.ItemCode, item.SerialCode, "US", 0, item.ItemName,
//                                    item.POSStock, 0, true, true);
//                                giftHindString.append("Qty not enough: " + item.SerialCode + "\n");
                            } else {
                                // 加入Gift msg
                                giftHindString.append("Scan gift no. ").append(item.SerialCode).append("\n");
                            }
                        }
                    }
                }
                //如果回傳無贈品, 清空giftList
                else {
                    giftList = new HashMap<>();
                }

                TransferOutActivity01.this.runOnUiThread(() -> adapter.notifyDataSetChanged());

                if (!giftHindString.toString().equals("")) {
                    // 顯示贈品資訊
                    final String msg = giftHindString.toString();
                    TransferOutActivity01.this.runOnUiThread(() -> MessageBox.show("", msg, mContext, "Ok"));
                }

                mloadingDialog.dismiss();

            }
        }.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //調整數量
        if ((requestCode == ITEMS_DETAIL) && resultCode == RESULT_OK) {
            int index = Integer.valueOf((String)data.getExtras().get("index"));

            String itemCode = adapter.getItem(index).getItemCode();
            int modiQty = Integer.valueOf((String)data.getExtras().get("newQty")) -
                (int)adapter.getItem(index).getQty();

            try {
                if (modiQty != 0) {
                    getDBData(_TransferTranscation.ModifyTransferList(itemCode, modiQty), true);
                }

            } catch (Exception e) {
                MessageBox.show("", "Get sales info error", mContext, "Return");
                return;
            }

        }
        isitetmClickModified = false;
        super.onActivityResult(requestCode, resultCode, data);
    }


    //點選 Item 項目修改內容
    private ItemListPictureModifyAdapter.ItemInfoClickListener itemInformationClickListener
        = new ItemListPictureModifyAdapter.ItemInfoClickListener() {

        @Override
        public void txtItemInfoClickListener(int position) {
            try {
                ItemInfo item = adapter.getItem(position);

                //贈品不能手動調整數量
                if (item.getPrice() == 0) {
                    MessageBox.show("", "Gift can not modify", mContext, "Return");
                    return;
                }

                //如果正在編輯就不能觸發第二次的activity彈跳視窗
                if (!isitetmClickModified) {
                    isitetmClickModified = true;

                    Intent intent = new Intent();
                    intent.setClass(mActivity, ItemDetailActivity.class);

                    //String itemCode, String monyType, int price, String itemName, int stock, int qt
                    intent.putExtra("item", item);
                    intent.putExtra("index", String.valueOf(position));
                    intent.putExtra("canModifiedToZero", "false");
                    intent.putExtra("fromWhere", "TransferOutActivity01");
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

        isGiftMagShow = true;
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