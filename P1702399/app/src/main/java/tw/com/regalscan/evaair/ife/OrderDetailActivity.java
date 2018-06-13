package tw.com.regalscan.evaair.ife;

import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jess.arms.utils.ArmsUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import aero.panasonic.inflight.crew.services.ordermanagement.v1.CrewOrder;
import aero.panasonic.inflight.crew.services.ordermanagement.v1.Item;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import tw.com.regalscan.R;
import tw.com.regalscan.activities.DiscountCheckActivity;
import tw.com.regalscan.activities.ItemPictureActivity;
import tw.com.regalscan.adapters.DiscountAdapter;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.Transaction;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.AllCurrencyListPack;
import tw.com.regalscan.db02.DBQuery.AllDiscountType;
import tw.com.regalscan.db02.DBQuery.BasketItem;
import tw.com.regalscan.db02.DBQuery.BasketItemPack;
import tw.com.regalscan.db02.DBQuery.ItemData;
import tw.com.regalscan.db02.DBQuery.ItemDataPack;
import tw.com.regalscan.evaair.basket.PayActivity;
import tw.com.regalscan.utils.Cursor;
import tw.com.regalscan.utils.Tools;

public class OrderDetailActivity extends AppCompatActivity {

    private static final String TAG = OrderDetailActivity.class.getSimpleName();

    //Scan
    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private int soundid;
    private String barcodeStr;

    public Context mContext;
    public Activity mActivity;
    private TextView txtMoney;

    private Button btnReturn;

    //商品清單
    private ItemListPictureModifyAdapter adapter;
    private Spinner spinnerCurrency;

    //local紀錄的adapter內所有商品清單
    private BasketItemPack basketItemPack;
    private ArrayAdapter<String> listCurrency;
    public static Transaction _Transaction;

    // 每個商品的Remark
    private Set<String> itemRemark;
    public static HashMap<String, ItemInfo> giftList = new HashMap<>();
    public static ArrayList<AllDiscountType> discountHintList = new ArrayList<>();
    public static HashMap<String, Integer> giftHintList = new HashMap<>();
    private boolean isGiftMagShow = false;

    //折扣list view
    private DiscountAdapter discountAdapter;
    private ImageView imageViewDiscount;
    public final int DISCOUNT_INFO = 600;

    //IFE
    private CrewOrder mCrewOrder;
    private IFEDBFunction mIFEDBFunction;
    private String fromWhere = "";
    private IFEFunction mIFEFunction;

    private List<Integer> isCheck = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            mCrewOrder = b.getParcelable("CrewOrder");
            fromWhere = b.getString("fromWhere");
        } else {
            MessageBox.show("", "Get order info error", mContext, "Return");
        }

        init();

        createItemList();
    }

    private void init() {

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mContext = this;
        mActivity = this;

        mIFEDBFunction = new IFEDBFunction(mContext, FlightData.SecSeq);
        mIFEFunction = new IFEFunction(mContext);

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener((View view) -> {
            if (!fromWhere.equals("dealInProgress") && mCrewOrder != null) {
                mIFEFunction.revertOrder(mCrewOrder)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete(Cursor::Normal)
                        .subscribe(ifeReturnData -> {
                            if (ifeReturnData.isSuccess()) {
                                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                                ArmsUtils.startActivity(OrderListActivity.class);
                                finish();
                            } else {
                                if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                                    processIFEError();
                                }
                            }
                        });
            } else {
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                ArmsUtils.startActivity(ProcessingOrderListActivity.class);
                finish();
            }
        });

        TextView txtToolbarTitle = findViewById(R.id.toolbar_title);
        if (fromWhere.equals("dealInProgress")) {
            txtToolbarTitle.setText("Processing Order Details");
        } else {
            txtToolbarTitle.setText("Order Details");
        }

        try {
            //建立交易
            _Transaction = new Transaction(mContext, FlightData.SecSeq);
            _Transaction.setCanUseIFEDiscount(true);
            basketItemPack = DBQuery.modifyBasket(new StringBuilder(), _Transaction.GetBasketInfo());
            if (basketItemPack == null) {
                MessageBox.show("", "Get sales info error", mContext, "Return");
                return;
            }
            giftList = new HashMap<>();
            _Transaction.SetSeatNo(mCrewOrder.getSeat());

        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get sales info error", mContext, "Return");
            return;
        }

        TextView txtSeatNum = findViewById(R.id.txtSeatNum);
        txtSeatNum.setText("Seat No.: " + mCrewOrder.getSeat());

        // items
        ListView itemListView = findViewById(R.id.lvItemList);
        adapter = new ItemListPictureModifyAdapter(mContext, itemListView);
        adapter.setIsModifiedItem(false);
        adapter.setIsCheckLayout(true);
        adapter.setIsOnlineSale(true);
        itemListView.setAdapter(adapter);
        // 點選item放大圖片
        adapter.setFilpperFunctionClickListener((String itemInfo) -> {
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
        });

        // 點選item做check // X
        // Scan到的商品才幫他自動做Check, 目前全部設為disable
        adapter.setCheckBoxCheckedListener((int position, boolean isChecked) -> {
            adapter.itemChecked(position, isChecked);

            if (isChecked) {
                isCheck.add(position);

                if (adapter.getItem(position).getPrice() == 0.0) {
                    ItemDataPack itemPack = DBQuery.getProductInfo(mContext, new StringBuilder(), FlightData.SecSeq, adapter.getItem(position).getItemCode(), null, 0);
                    newGiftItemModi(itemPack.items[0], 1, false);
                }

            } else {
                for (int i = 0; i < isCheck.size(); i++) {
                    if (isCheck.get(i) == position) {
                        isCheck.remove(i);
                    }
                }
            }

        });

        //取得可以使用的幣別, 20
        final StringBuilder err = new StringBuilder();
        AllCurrencyListPack allCurrencyPack = DBQuery.getAllCurrencyList(mContext, err);
        if (allCurrencyPack == null) {
            MessageBox.show("", "Get currency error", mContext, "Return");
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
        }

        // 下拉幣別選單
        spinnerCurrency = findViewById(R.id.spinner01);
        listCurrency = new ArrayAdapter<>(this, R.layout.spinner_item);
        for (DBQuery.AllCurrencyList currency : allCurrencyPack.currencyList) {
            //判斷是否要顯示在購物車上
            if (currency.BasketCurrency.equals("Y")) {
                listCurrency.add(currency.CurDvr);
            }
        }
        spinnerCurrency.setAdapter(listCurrency);
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                txtMoney.setText(setTotalMoneyTextView());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        txtMoney = findViewById(R.id.txtMoney);

        //折扣ListView
        discountAdapter = new DiscountAdapter(mContext);
        ListView discountListView = findViewById(R.id.discountList);
        discountListView.setAdapter(discountAdapter);

        Button btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener((View view) -> {
            // order轉cancel
            if (MessageBox.show("", "Confirm cancel order?", OrderDetailActivity.this, "Yes", "No")) {
                // 將open order轉成cancel
                mIFEFunction.cancelOrder(mCrewOrder)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnComplete(Cursor::Normal)
                        .subscribe(ifeReturnData -> {
                            if (ifeReturnData.isSuccess()) {
                                ArmsUtils.startActivity(OrderListActivity.class);
                                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                                finish();
                            } else {
                                if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                                    processIFEError();
                                }
                            }
                        });
            }
        });

        Button btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener((View view) -> {
            // 將open order轉成crew cart
            mIFEFunction.convertToCart(mCrewOrder)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ifeReturnData -> {
                        if (ifeReturnData.isSuccess()) {
                            Cursor.Normal();
                            Bundle extras = new Bundle();
                            extras.putString("SeatNo", mCrewOrder.getSeat());
                            extras.putString("orderNo", mCrewOrder.getOrderId());

                            if (fromWhere.equals("dealInProgress")) {
                                extras.putString("fromWhere", "ProcessingOrder");
                            } else {
                                extras.putString("fromWhere", "OrderDetail");
                            }

                            // 跳轉頁面
                            Intent intent = new Intent(mContext, OnlineBasketActivity.class);
                            intent.putExtras(extras);
                            mActivity.startActivity(intent);
                            finish();
                        } else {
                            Cursor.Normal();
                            if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                                processIFEError();
                            }
                        }
                    });
        });

        Button btnPay = findViewById(R.id.btnPay);
        btnPay.setOnClickListener((View view) -> {
            if (adapter.getCount() == 0) {
                MessageBox.show("", "No item, can't pay", mContext, "Return");
                return;
            }

            for (ItemInfo itemInfo : adapter.getItemList()) {
                if (!itemInfo.isCheck() && itemInfo.getPrice() != 0.0) {
                    MessageBox.show("", "Please check the item.", mContext, "Return");
                    return;
                }
            }

            //商品訂購量大於可販售量，不可進行付款
            if (adapter.isQtyMoreThanStock(false)) {
                MessageBox.show("", "Qty not enough", mContext, "Return");
                return;
            }

            // 1. 顯示商品Remark
            // 商品Remark
            StringBuilder itemRemarkBuilder = new StringBuilder();
            for (String s : itemRemark) {
                itemRemarkBuilder.append(s).append("\n");
            }

            // 2. 檢查贈品未scan
            StringBuilder giftNotScanString = new StringBuilder();
            for (BasketItem singleItem : basketItemPack.items) {
                for (ItemInfo iteminfo : adapter.getItemList()) {
                    if (singleItem.ItemCode.equals(iteminfo.getItemCode()) && !iteminfo.isCheck()) {
                        if (singleItem.GiftFlag.equals("Y") &&
                                (singleItem.SalesQty <= singleItem.POSStock || singleItem.SalesQty <= singleItem.IFEStock) &&
                                singleItem.SalesQty > giftList.get(singleItem.ItemCode).getGiftScan()) {
                            giftNotScanString.append("Scan gift no. ").append(singleItem.SerialCode).append(" * ").append(
                                    singleItem.SalesQty - giftList.get(singleItem.ItemCode).getGiftScan()).append("\n");
                        }
                    }
                }
            }

            // 贈品有貨未scan的確認視窗
            if (!giftNotScanString.toString().equals("")) {
                if (isGiftMagShow) {
                    MessageBox.show("", giftNotScanString.toString(), mContext, "Ok");
                    isGiftMagShow = false;
                    return;
                }

                if (MessageBox.show("", "Does PAX agree to pay without gift?", mContext, "Yes", "No")) {
                    successPay(itemRemarkBuilder.toString());
                }
            }

            // 是否有贈品訂購量大於庫存量
            else if (adapter.isQtyMoreThanStock(true)) {
                if (MessageBox.show("", "Does PAX agree to pay without gift?", mContext, "Yes", "No")) {
                    successPay(itemRemarkBuilder.toString());
                }
            } else {
                successPay(itemRemarkBuilder.toString());
            }
        });

        //discount清單
        imageViewDiscount = findViewById(R.id.imgDiscountPlus);
        imageViewDiscount.setOnClickListener(view -> {
            try {
                //傳遞現有的折扣清單
                Bundle argument = new Bundle();
                argument.putString("fromWhere", "BasketActivity");
                Intent intent = new Intent();
                intent.putStringArrayListExtra("discountArrayList", discountAdapter.getCurrentDiscountList());
                intent.putExtras(argument);
                intent.setClass(mActivity, DiscountCheckActivity.class);

                startActivityForResult(intent, DISCOUNT_INFO);
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Error", mContext, "Return");
            }
        });

        //折扣ListView
        discountAdapter = new DiscountAdapter(mContext);
        discountListView = findViewById(R.id.discountList);
        discountListView.setAdapter(discountAdapter);

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == DISCOUNT_INFO) && resultCode == RESULT_OK) {
            try {
                HashMap<String, String> discountHashMap = (HashMap) data.getExtras().get("discountHashMap");
                if (discountHashMap != null && discountHashMap.size() > 0) {
                    for (String key : discountHashMap.keySet()) {
                        JSONObject json = _Transaction.AddDiscountList(key, discountHashMap.get(key));
                        String retCode = json.getString("ReturnCode");
                        if (!retCode.equals("0")) {
                            MessageBox.show("", json.getString("ReturnMessage"), mContext, "Return");
                            return;
                        }
                    }
                }
                getDBData(_Transaction.GetBasketInfo(), false);
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Get sales data error", mContext, "Return");
            }
        }
    }

    // 將Scan到的商品打勾
    private void createCurrentItemJson(final String itemNum) {
        StringBuilder err = new StringBuilder();
        ItemDataPack itemPack = DBQuery.getProductInfo(mContext, err, FlightData.SecSeq, itemNum, null, 0);
        //查無此商品
        if (itemPack == null) {
            MessageBox.show("", "Pno Code error", mContext, "Return");
            return;
        }
        int itemIndex = adapter.getItemId(itemPack.items[0].ItemCode);
        if (itemIndex == -1) {
            if (itemPack.items[0].ItemPriceUS == 0.0) {
                MessageBox.show("", "Wrong gift", mContext, "Return");
            } else {
                MessageBox.show("", "Wrong item", mContext, "Return");
            }
        } else {

            if (itemPack.items[0].ItemPriceUS == 0.0) {
                newGiftItemModi(itemPack.items[0], 1, false);
            }

            // 將對應的item打勾
            adapter.itemChecked(itemIndex, true);
            adapter.notifyDataSetChanged();
        }
    }

    // Scan到贈品
    // 1. 檢查local清單內是否存在此贈品
    // 2. 是否有被scan過, 有的話就return
    private void newGiftItemModi(ItemData item, int addCount, boolean isTransfer) {
        ItemInfo giftItem = giftList.get(item.ItemCode);

        //gift不可單獨scan
        // 購物車是空的: 贈品不能單獨scan
        // 購物車有東西: Wrong gift
        if (adapter.getCount() > 0) {
            if (giftItem == null) {
                runOnUiThread(() -> MessageBox.show("", "Wrong gift", mContext, "Return"));
                return;
            }
        } else {
            if (giftItem == null) {
                final String ss;
                if (isTransfer) {
                    ss = "transferred";
                } else {
                    ss = "sold";
                }
                runOnUiThread(() -> MessageBox.show("", "Gift can't be " + ss + " individually", mContext, "Return"));
                return;
            }
        }

        //如果清單內存在此贈品，表示為可以調整數量的item
        //如果scan過的數量大於應贈送總數的話, 返回
        if ((Integer) giftItem.getQty() <= giftItem.getGiftScan()) {
            runOnUiThread(() -> MessageBox.show("", "Gift already scan", mContext, "Return"));
            return;
        }

        // 已scan數量>=庫存 要擋
        // Transfer收據上買A送B，有可能為A一個B零個，這時若庫存不足，不要顯示 Stock not enough
        if (!isTransfer
                && (giftItem.getStock() == 0 || giftItem.getStock() <= giftItem.getGiftScan())) {
            runOnUiThread(() -> MessageBox.show("", "Qty not enough", mContext, "Return"));
            return;
        }

        //小於等於應贈送總數的話就增加adapter內的數量, 並將對應物件數量存回去
        giftItem.setGiftScan(giftItem.getGiftScan() + addCount);
        giftList.put(item.ItemCode, giftItem);

        // 如果adapter內沒有此贈品, add
        if (adapter.getItem(giftItem.getItemCode()) == null) {
            // 販售量大於庫存量
            if ( // 連線
                    (adapter.isOnlineSale() && ((int) giftItem.getQty() > giftItem.getStock() || (int) giftItem.getQty() > giftItem.getIfeStock()))
                            // 離線
                            || (!adapter.isOnlineSale() && (int) giftItem.getQty() > giftItem.getStock())) {
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
            if ( // 連線
                    (adapter.isOnlineSale() &&
                            ((int) giftItem.getQty() > giftItem.getStock() || (int) giftItem.getQty() > giftItem.getIfeStock()))
                            // 離線
                            || (!adapter.isOnlineSale() && (int) giftItem.getQty() > giftItem.getStock())) {
                adapter.modifiedItemColorChange(
                        adapter.getItemId(giftItem.getItemCode()), true);
            }
        }

        runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    private void getDBData(final JSONObject jsonInput, final boolean resetGiftMsg) {

        isGiftMagShow = !resetGiftMsg;

        final StringBuilder err = new StringBuilder();
        // 解Jason
        BasketItemPack tmpItemPack = DBQuery.modifyBasket(err, jsonInput);
        if (tmpItemPack == null) {
            OrderDetailActivity.this.runOnUiThread(() -> {
                Cursor.Normal();
                if (MessageBox.show("", err.toString(), mContext, "Return")) {
                    btnReturn.performClick();
                }
            });
            return;
        }
        basketItemPack = tmpItemPack;

        // 顯示的所有訊息
        StringBuilder msgShow = new StringBuilder();

        //收到的所有商品, 折扣備註視窗
        HashMap<String, Integer> newGiftHintList = new HashMap<>();

        // 將所有主動被動折扣放到discount內, 回傳贈品折扣訊息
        newGiftHintList = discountMsgProcessing(msgShow, newGiftHintList);

        // 處理贈品折扣訊息
        discountGiftHindProcessing(msgShow, newGiftHintList);

        // 將所有item放入adapter,  回傳收到的贈品清單
        HashMap<String, BasketItem> newGiftList = itemShowProcessing();

        // 處理贈品的顯示, 回傳贈品顯示訊息 (庫存不足的顯示)
        giftShowProcessing(newGiftList);

        // 贈品拿已存清單另外加在畫面上
//        for (String oldKey : newGiftList.keySet()) {
//            BasketItem item = newGiftList.get(oldKey);
//            if ((item.SalesQty > item.POSStock || item.SalesQty > item.IFEStock)) {
//                adapter.addItem(
//                    item.ItemCode, item.SerialCode, "US",
//                    0, item.ItemName,
//                    item.POSStock, item.SalesQty,
//                    true, true);
//            }
//        }

        for (int i = 0; i < isCheck.size(); i++) {
            adapter.itemChecked(isCheck.get(i), true);
        }

        OrderDetailActivity.this.runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            txtMoney.setText(setTotalMoneyTextView());
        });

        runOnUiThread(Cursor::Normal);
    }

    // 主動被動折扣的處理
    private HashMap<String, Integer> discountMsgProcessing(StringBuilder discountMsg, HashMap<String, Integer> newGiftHintList) {
        discountAdapter.clear();
        ArrayList<AllDiscountType> discountPassiveType = new ArrayList<>();
        ArrayList<AllDiscountType> discountActiveType = new ArrayList<>();

        // 要移除的回傳折扣index
        ArrayList<Integer> removeList = new ArrayList<>();
        // 要增加的回傳折扣物件
        ArrayList<AllDiscountType> appendList = new ArrayList<>();

        // 回傳的新主動式折扣清單
        ArrayList<AllDiscountType> newDiscountList = new ArrayList<>();

        for (int i = 0; i < basketItemPack.types.length; i++) {
            // type和描述相同, 為被動式折扣
            // 被動式折扣: 加到adapter, 不顯示在小視窗
//            if (basketItemPack.types[i].Type.equals(basketItemPack.types[i].Description)) {
//                discountPassiveType.add(new AllDiscountType(
//                    basketItemPack.types[i].Type, basketItemPack.types[i].Description));
//            }
            if (basketItemPack.types[i].FuncID.equals("")) {
                discountPassiveType.add(new AllDiscountType(
                        basketItemPack.types[i].Type, basketItemPack.types[i].Description));
            }
            // 主動式折扣
            else {
                // Description前面有@@, 則為贈品訊息, 另存起來比對
                // 贈品訊息
//                if (!basketItemPack.types[i].Description.equals("")) {
                if (basketItemPack.types[i].Description.substring(0, 2).equals("@@")) {
                    newGiftHintList.put(basketItemPack.types[i].Type + basketItemPack.types[i].Description, basketItemPack.types[i].DiscountCount);
                }
//                }

                // 主動式折扣: 加到adapter, 加到訊息比對list過濾
                else {
                    AllDiscountType tmp = new AllDiscountType
                            (basketItemPack.types[i].Type, basketItemPack.types[i].Description);
                    discountActiveType.add(tmp);
                    newDiscountList.add(tmp);
                }
            }
        }

        // 本來沒折扣, 回傳有折扣: 全部塞進去, 全部顯示
        if (discountHintList.size() == 0 && newDiscountList.size() > 0) {
            discountHintList = new ArrayList<>(newDiscountList);
            for (AllDiscountType discount : discountHintList) {
                if (!discount.Description.equals("")) {
                    discountMsg.append(discount.Type).append(" - ").append(discount.Description).append("\n");
                } else {
                    discountMsg.append(discount.Type).append("\n");
                }
            }
        }

        // 本來有折扣, 回傳有折扣
        if (discountHintList.size() > 0 && newDiscountList.size() > 0) {

            // 將現有清單比對新的清單
            for (int i = 0; i < discountHintList.size(); i++) {
                boolean isExit = false;
                AllDiscountType oldMsg = discountHintList.get(i);
                for (AllDiscountType newMsg : newDiscountList) {
                    if (oldMsg.Type.equals(newMsg.Type) && oldMsg.Description.equals(newMsg.Description)) {
                        isExit = true;
                        break;
                    }
                }
                // 如果回傳值沒有此折扣, remove, 不顯示
                if (!isExit) {
                    removeList.add(i);
                }
                // 如果回傳有此折扣, 不顯示
            }

            // 刪掉現有清單多的折扣
            for (int i : removeList) {
                discountHintList.remove(i);
            }

            // 將新的清單比對現有清單
            // 如果有新增的折扣, 塞進去, 顯示
            for (int i = 0; i < newDiscountList.size(); i++) {
                boolean isExit = false;
                AllDiscountType newMsg = newDiscountList.get(i);

                for (AllDiscountType oldMsg : discountHintList) {
                    if (oldMsg.Type.equals(newMsg.Type) && oldMsg.Description.equals(newMsg.Description)) {
                        isExit = true;
                        break;
                    }
                }
                // 如果現有清單沒有此折扣, 加進去
                if (!isExit) {
                    appendList.add(newMsg);
                }
            }

            // 將新的折扣加入現有清單, 顯示
            for (AllDiscountType newDiscount : appendList) {
                discountHintList.add(newDiscount);
                discountMsg.append(newDiscount.Type).append(" - ").append(newDiscount.Description).append("\n");
            }
        }

        // 本來有折扣, 回傳沒折扣: 全部清掉, 全部不顯示
        if (discountHintList.size() > 0 && newDiscountList.size() == 0) {
            discountHintList = new ArrayList<>();
        }

        /*-------------將兩種折扣放入adapter-------------*/
        for (AllDiscountType tmp : discountPassiveType) {
            discountAdapter.addItem(tmp.Type + " - " + tmp.Description);
        }
        for (AllDiscountType tmp : discountActiveType) {
            discountAdapter.addItem(tmp.Type + " - " + tmp.Description);
        }
        OrderDetailActivity.this.runOnUiThread(() -> discountAdapter.notifyDataSetChanged());

        return newGiftHintList;
    }

    // 贈品贈送訊息的處理
    private void discountGiftHindProcessing(StringBuilder discountGiftString, HashMap<String, Integer> newGiftHintList) {
        // 本來沒贈品, 回傳有贈品: 全部塞進去, 全部顯示
        if (giftHintList.size() == 0 && newGiftHintList.size() > 0) {
            giftHintList = new HashMap<>(newGiftHintList);
            for (String key : giftHintList.keySet()) {
                String[] tmpSplit = key.split("@@");
                if (tmpSplit.length >= 2) {
                    discountGiftString.append(tmpSplit[1]).append("\n");
                }
            }
        }
    }

    // 主商品的增加與顯示
    private HashMap<String, BasketItem> itemShowProcessing() {
        adapter.clear();

        HashMap<String, BasketItem> newGiftList = new HashMap<>();
        //商品備註視窗
        itemRemark = new HashSet<>();

        //分辨是否贈品
        for (int i = 0; i < basketItemPack.items.length; i++) {

            if (basketItemPack.items[i].USDPrice != 0.0) {
                // String itemCode, String SerialCode, String moneyType, int price, String itemName,
                // int stock, int qty, boolean canDisCount, boolean isModified
                // 可打折商品
                if (basketItemPack.items[i].DiscountFlag.equals("Y")) {
                    // 商品訂購量大於可販售量, 紅色細體字
                    if ((adapter.isOnlineSale() && (basketItemPack.items[i].SalesQty > basketItemPack.items[i].POSStock
                            || basketItemPack.items[i].SalesQty > basketItemPack.items[i].IFEStock))

                            || (!adapter.isOnlineSale() && basketItemPack.items[i].SalesQty > basketItemPack.items[i].POSStock)) {
                        adapter.addItem(
                                basketItemPack.items[i].ItemCode,
                                basketItemPack.items[i].DrawerNo + "-" + basketItemPack.items[i].SerialCode, "US",
                                basketItemPack.items[i].USDPrice, basketItemPack.items[i].ItemName,
                                basketItemPack.items[i].POSStock, basketItemPack.items[i].SalesQty,
                                basketItemPack.items[i].IFEStock,
                                true, true);
                    } else {
                        adapter.addItem(
                                basketItemPack.items[i].ItemCode,
                                basketItemPack.items[i].DrawerNo + "-" + basketItemPack.items[i].SerialCode, "US",
                                basketItemPack.items[i].USDPrice, basketItemPack.items[i].ItemName,
                                basketItemPack.items[i].POSStock, basketItemPack.items[i].SalesQty,
                                basketItemPack.items[i].IFEStock,
                                true, false);
                    }
                }
                // 不打折商品, 黑色粗體字
                else {
                    if ((adapter.isOnlineSale() && (basketItemPack.items[i].SalesQty > basketItemPack.items[i].POSStock
                            || basketItemPack.items[i].SalesQty > basketItemPack.items[i].IFEStock))

                            || (!adapter.isOnlineSale() && basketItemPack.items[i].SalesQty > basketItemPack.items[i].POSStock)) {
                        adapter.addItem(
                                basketItemPack.items[i].ItemCode,
                                basketItemPack.items[i].DrawerNo + "-" + basketItemPack.items[i].SerialCode, "US",
                                basketItemPack.items[i].USDPrice, basketItemPack.items[i].ItemName,
                                basketItemPack.items[i].POSStock, basketItemPack.items[i].SalesQty,
                                basketItemPack.items[i].IFEStock,
                                false, true);
                    } else {
                        adapter.addItem(
                                basketItemPack.items[i].ItemCode,
                                basketItemPack.items[i].DrawerNo + "-" + basketItemPack.items[i].SerialCode, "US",
                                basketItemPack.items[i].USDPrice, basketItemPack.items[i].ItemName,
                                basketItemPack.items[i].POSStock, basketItemPack.items[i].SalesQty,
                                basketItemPack.items[i].IFEStock,
                                false, false);
                    }
                }
                //備註, remark比對字串
                if (!basketItemPack.items[i].Remark.equals("")) {
                    itemRemark.add(basketItemPack.items[i].Remark);
                }
            }

            //金額=0的是贈品
            else {
                //先把整個回傳的贈品清單存起來，再一個個和現有的贈品清單比對
                newGiftList.put(basketItemPack.items[i].ItemCode, basketItemPack.items[i]);
            }
        }

        return newGiftList;
    }

    // 處理贈品的增加與顯示、提醒視窗的顯示
    private void giftShowProcessing(HashMap<String, BasketItem> newGiftList) {
        // 要移除的回傳贈品key
        ArrayList<String> removeList = new ArrayList<>();

        //如果有回傳的贈品
        if (newGiftList.size() > 0) {

            //如果local清單有東西
            if (OrderDetailActivity.giftList.size() > 0) {

                //將現有清單比對新的清單
                for (String oldKey : OrderDetailActivity.giftList.keySet()) {
                    BasketItem newItem = newGiftList.get(oldKey);

                    // 1. 如果回傳值無此贈品，直接remove
                    if (newItem == null) {
//                        giftList.remove(oldKey);
                        removeList.add(oldKey);
                    }
                    // 2. 有的話比對新的贈送數量和已scan的贈送數量
                    else {
                        ItemInfo oldItem = OrderDetailActivity.giftList.get(oldKey);

                        // 如果新贈送數量大於舊數量, 且新數量大於庫存量
//                        if (newItem.SalesQty > (Integer)oldItem.getQty() &&
//                            newItem.POSStock< newItem.SalesQty){
//                            giftHindString.append("Qty not enough: " + newItem.SerialCode + "\n");
//                        }

                        // (1) 如果新的應贈送數量與舊的不同，取代
                        if (newItem.SalesQty != (Integer) oldItem.getQty()) {
                            oldItem.setQty(newItem.SalesQty);
                        }
                        // (2) 如果已Scan數量大於應贈送數量，取代
                        if (oldItem.getGiftScan() > newItem.SalesQty) {
                            oldItem.setGiftScan(newItem.SalesQty);
                        }
                        // (3) 如果新的stock數量與舊的不同，取代
                        if (newItem.POSStock != oldItem.getStock()) {
                            oldItem.setStock(newItem.POSStock);
                        }
                        // (4) 如果新的IFE數量與舊的不同，取代
                        if (newItem.IFEStock != oldItem.getIfeStock()) {
                            oldItem.setStock(newItem.IFEStock);
                        }

                        // (5) 將已scan數量大於0的贈品加入adapter
                        if (oldItem.getGiftScan() > 0) {
                            // 連線庫存 & 離線庫存
                            // 將所有local有Scan數量的贈品放到adapter內
                            // 應贈送量大於庫存量, 紅色細體字
                            if ((adapter.isOnlineSale() &&
                                    ((Integer) oldItem.getQty() > oldItem.getStock() || (Integer) oldItem.getQty() > oldItem.getIfeStock()))
                                    || (!adapter.isOnlineSale()) && (Integer) oldItem.getQty() > oldItem.getStock()) {
                                adapter.addItem(
                                        oldItem.getItemCode(), oldItem.getSerialNo(), "US",
                                        oldItem.getPrice(), oldItem.getItemName(),
                                        oldItem.getStock(), oldItem.getGiftScan(),
                                        true, true);
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
                            //連線庫存
                            if (((Integer) oldItem.getQty() > oldItem.getStock() || (Integer) oldItem.getQty() > oldItem.getIfeStock())) {
                                if ((Integer) oldItem.getQty() > oldItem.getStock()) {
                                    adapter.addItem(
                                            oldItem.getItemCode(), oldItem.getSerialNo(), "US",
                                            oldItem.getPrice(), oldItem.getItemName(),
                                            oldItem.getStock(), 0,
                                            true, true);
                                }
                            }
                        }
                        // (7) 更新giftList
                        OrderDetailActivity.giftList.put(oldKey, oldItem);
                    }
                }

                // 將新的清單再一次比對local清單
                for (String newKey : newGiftList.keySet()) {
                    ItemInfo oldItem = OrderDetailActivity.giftList.get(newKey);

                    // 如果有新的贈品, 加入local清單
                    if (oldItem == null) {
                        BasketItem newItem = newGiftList.get(newKey);
                        OrderDetailActivity.giftList.put(newItem.ItemCode,
                                new ItemInfo(newItem.ItemCode,
                                        newItem.DrawerNo + "-" + newItem.SerialCode, "US", newItem.USDPrice, newItem.ItemName,
                                        newItem.POSStock, newItem.SalesQty, newItem.IFEStock, 0, false, false));

                        // 庫存不足
                        if ((adapter.isOnlineSale() &&
                                (newItem.SalesQty > newItem.POSStock || newItem.SalesQty > newItem.IFEStock))
                                || (!adapter.isOnlineSale()) && newItem.SalesQty > newItem.POSStock) {
                            adapter.addItem(
                                    newItem.ItemCode, newItem.SerialCode, "US", newItem.USDPrice, newItem.ItemName,
                                    newItem.POSStock, 0, true, true);
                        }
                    }
                }

                // 移除現有清單內多的贈品
                for (String removeKey : removeList) {
                    OrderDetailActivity.giftList.remove(removeKey);
                }
            }
            // local為空的話就將所有newGift放入local內
            else {
                for (String key : newGiftList.keySet()) {
                    BasketItem item = newGiftList.get(key);
                    //String itemCode, String serialNo, String moneyType, Double price, String itemName
                    //int stock, int qty, int ifeStock,int giftScan, boolean canDiscount, boolean isModified

                    OrderDetailActivity.giftList.put(item.ItemCode,
                            new ItemInfo(item.ItemCode,
                                    item.DrawerNo + "-" + item.SerialCode, "US", item.USDPrice, item.ItemName,
                                    item.POSStock, item.SalesQty, item.IFEStock, 0, false, false));

                    if ((adapter.isOnlineSale() && (item.SalesQty > item.POSStock || item.SalesQty > item.IFEStock))
                            || (!adapter.isOnlineSale() && item.SalesQty > item.POSStock)) {
                        // 庫存不足
                        adapter.addItem(
                                item.ItemCode, item.SerialCode, "US", item.USDPrice, item.ItemName,
                                item.POSStock, 0, true, true);
                    } else {
                        adapter.addItem(
                                item.ItemCode, item.SerialCode, "US", item.USDPrice, item.ItemName,
                                item.POSStock, item.SalesQty, true, false);
                    }
                }
            }
        }
        //如果回傳無贈品, 清空giftList
        else {
            OrderDetailActivity.giftList = new HashMap<>();
        }
    }

    // 取得應付款總額
    private String setTotalMoneyTextView() {
        try {
            if (basketItemPack != null && listCurrency != null) {
                // 先呼叫一次，取得總應付金額，這樣 GetCurrencyMaxAmount 才取得到值
                _Transaction.GetPaymentMode();

                String currency = spinnerCurrency.getSelectedItem().toString();
                DBQuery.ShouldPayMoney payItem = DBQuery.getPayMoneyNow(new StringBuilder(),
                        _Transaction.GetCurrencyMaxAmount(currency));
                if (payItem == null || payItem.Currency == null) {
                    MessageBox.show("", "Get pay info error", mActivity, "Return");
                    return "";
                }
                if (adapter.getCount() != 0) {
                    return (currency + " " + String.valueOf(payItem.MaxPayAmount));
                } else {
                    return (currency + " 0");
                }
            } else {
                return "USD 0";
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Set currency error", mContext, "Return");
            return "";
        }
    }

    private void successPay(String LAGRemark) {
        //傳遞整包BasketItemPack
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        String jsonBasketItemPack = gson.toJson(basketItemPack);
        bundle.putString("Currency", spinnerCurrency.getSelectedItem().toString());
        bundle.putString("BasketItemPack", jsonBasketItemPack);
        bundle.putString("RemarkMsg", LAGRemark);
        if (!fromWhere.equals("dealInProgress")) {
            bundle.putString("fromWhere", "OrderDetailActivity");
        } else {
            bundle.putString("fromWhere", "ProcessingOrderDetailActivity");
        }
        bundle.putParcelable("CrewOrder", mCrewOrder);

        Intent intent = new Intent(mActivity, PayActivity.class);
        intent.putExtras(bundle);
        mActivity.startActivity(intent);

        finish();
    }

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            soundpool.play(soundid, 1, 1, 0, 0, 1);
            mVibrator.vibrate(100);

            byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
            int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
            byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte) 0);
            Timber.tag("debug").i("----codetype--" + temp);
            barcodeStr = new String(barcode, 0, barcodelen);

            if (barcodeStr.length() >= 33) {
                MessageBox.show("", "Press Edit", mContext, "Ok");
                return;
            }

            //依照輸入的barcode作search
            createCurrentItemJson(barcodeStr);

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

        try {
            _Transaction.GetBasketInfo();
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get basket info error", mContext, "Return");
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        //Scanner處理
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void createItemList() {
        if (fromWhere.equals("OrderListActivity") || fromWhere.equals("OrderEditActivity") || fromWhere.equals("dealInProgress") || fromWhere.equals("PayActivity")) {

            Cursor.Busy("Processing...", mContext);
            new Thread() {
                public void run() {
                    // 將取得的Order Data放到畫面上
                    // IFE訂單上只會有主商品, 贈品由Transaction進行計算
                    try {
                        JSONObject json = new JSONObject();

                        for (Item item : mCrewOrder.getOrderItems()) {
                            String itemID = mIFEDBFunction.getItemCode(item.getSku());

                            _Transaction.setIFEStock(Integer.valueOf(item.getQuantity()));
                            json = _Transaction.ModifyItemList(itemID, Integer.valueOf(item.getQuantity()));
                        }

                        getDBData(json, false);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Timber.tag(TAG).d(e.getMessage());

                        runOnUiThread(() -> {
                            Cursor.Normal();
                            if (MessageBox.show("", "Get sales info error", mContext, "Return")) {
                                btnReturn.performClick();
                            }
                        });
                    }
                }
            }.start();
        }
    }

    private void processIFEError() {
        if (!fromWhere.equals("dealInProgress")) {
            if (MessageBox.show("", getString(R.string.IFE_Offline_Processing_Order_List), mContext, "Ok")) {
                ArmsUtils.startActivity(IFEActivity01.class);
                finish();
            }
        } else {
            if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                ArmsUtils.startActivity(IFEActivity01.class);
                finish();
            }
        }
    }
}
