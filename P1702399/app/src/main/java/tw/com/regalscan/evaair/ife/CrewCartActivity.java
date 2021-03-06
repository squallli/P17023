package tw.com.regalscan.evaair.ife;

import java.util.*;

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
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import aero.panasonic.inflight.crew.services.cartmanagement.v1.model.CrewCart;
import aero.panasonic.inflight.crew.services.cartmanagement.v1.model.Item;
import aero.panasonic.inflight.crew.services.ordermanagement.v1.CrewOrder;
import com.google.gson.Gson;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.DeviceUtils;
import com.regalscan.sqlitelibrary.TSQL;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import org.json.JSONObject;
import tw.com.regalscan.R;
import tw.com.regalscan.activities.DiscountCheckActivity;
import tw.com.regalscan.activities.ItemDetailOnlineActivity;
import tw.com.regalscan.activities.ItemPictureActivity;
import tw.com.regalscan.adapters.DiscountAdapter;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.component.*;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.LogType;
import tw.com.regalscan.db.Transaction;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.*;
import tw.com.regalscan.evaair.basket.PayActivity;
import tw.com.regalscan.evaair.ife.entity.Catalog;
import tw.com.regalscan.utils.Cursor;
import tw.com.regalscan.utils.Tools;


public class CrewCartActivity extends AppCompatActivity {

    private static final String TAG = CrewCartActivity.class.getSimpleName();

    //Scan
    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private int soundid;
    private String barcodeStr;

    private StringBuilder errMsg = new StringBuilder();
    public ItemListPictureModifyAdapter adapter;
    private SwipeListView itemListView;
    private ListView discountListView;
    private Spinner spinnerCurrency, spinnerCar;
    private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    private Button btnReturn, btnPay;
    public Context mContext;
    public Activity mActivity;
    private EditText editSeatNum, editItemNum;
    private CheckBox checkCrew;
    private TextView txtMoney;
    //當前車櫃編號
    private String cartNum = "";

    public static final int ITEMS_DETAIL = 500;
    private boolean isItemClickModified = false;
    private ArrayAdapter<String> listCurrency;

    //被動式折扣(自己刷卡)和主動式折扣(DB算的)清單
    private ArrayList<AllDiscountType> discountHintList = new ArrayList<>();

    //local紀錄的應Scan贈品清單
    private static HashMap<String, ItemInfo> giftList = new HashMap<>();

    // 加入商品時的贈品提醒訊息
    private HashMap<String, Integer> giftHintList = new HashMap<>();

    //折扣list view
    private DiscountAdapter discountAdapter;
    ArrayList<String> discountList = new ArrayList<String>();
    public static final int DISCOUNT_INFO = 600;

    //鍵盤
    private InputMethodManager imm;
    private ImageView imageViewSearch, imageViewDiscount;

    public static Transaction s_Transaction;
    private String seatNo = "";

    // 每個商品的Remark
    private Set<String> itemRemark;

    // 結帳時的未scan贈品提醒視窗有沒有顯示過
    private boolean isGiftMagShow = false;

    // Transfer的物品編號暫存, 數量暫存
    ArrayList<ItemData> transferItemTmp;
    ArrayList<String> transferItemTmpCount;

    //IFE
    private IFEDBFunction mIFEDBFunction;
    private CrewOrder mCrewOrder;
    private CrewCart mCrewCart;
    private String fromWhere = "";
    private Map<Item, Integer> itemQuantityMap = new HashMap<>();

    private IFEFunction mIFEFunction;

    //local紀錄的adapter內所有商品清單
    private DBQuery.BasketItemPack basketItemPack;

    private TSQL mTSQL;

    public static Transaction getBasketTransaction() {
        return s_Transaction;
    }

    public static HashMap<String, ItemInfo> getBasketGiftList() {
        return giftList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew_cart);

        init();
    }

    private void init() {
        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
//    cartNum = FlightData.CartNo;
        mContext = this;
        mActivity = this;

        mIFEDBFunction = new IFEDBFunction(mContext, FlightData.SecSeq);
        mIFEFunction = new IFEFunction(mContext);
        mTSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            if (mCrewCart != null) {
                MessageBox.threeBtnShow("",
                    "Press 'YES', turn the CrewCart into Order;\n'NO' to stay at current page;\n'Cancel' to cancel this CrewCart.",
                    mContext, "Yes", "No", "Cancel", new IMsgBoxThreeBtnClick() {
                        @Override
                        public void onYesClick() {
                            mIFEFunction.submitCart(mCrewCart)
                                .subscribeOn(Schedulers.io())
                                .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                                .observeOn(AndroidSchedulers.mainThread())
                                .doFinally(Cursor::Normal)
                                .subscribe(ifeReturnData -> {
                                    if (ifeReturnData.isSuccess()) {
                                        ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                                        giftList = new HashMap<>();
                                        finish();
                                    } else {
                                        if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                                            if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                                                ArmsUtils.startActivity(IFEActivity01.class);
                                                finish();
                                            }
                                        }
                                    }
                                });
                        }

                        @Override
                        public void onNoClick() {

                        }

                        @Override
                        public void onCancelClick() {
                            mIFEFunction.emptyCart(mCrewCart)
                                .subscribeOn(Schedulers.io())
                                .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnComplete(Cursor::Normal)
                                .subscribe(ifeReturnData -> {
                                    if (ifeReturnData.isSuccess()) {
                                        ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                                        giftList = new HashMap<>();
                                        finish();
                                    } else {
                                        if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                                            if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                                                ArmsUtils.startActivity(IFEActivity01.class);
                                                finish();
                                            }
                                        }
                                    }
                                });
                        }
                    });
            } else {
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
            }
        });

        try {
            //建立交易
            s_Transaction = new Transaction(mContext, FlightData.SecSeq);
            basketItemPack = DBQuery.modifyBasket(new StringBuilder(), s_Transaction.GetBasketInfo());
            if (basketItemPack == null) {
                MessageBox.show("", "Get sales info error", mContext, "Return");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get sales info error", mContext, "Return");
            return;
        }

        editSeatNum = findViewById(R.id.editTextSeatNum);
        editItemNum = findViewById(R.id.editItemNum);
        editItemNum.setEnabled(false);
        checkCrew = findViewById(R.id.checkBox);

        checkCrew.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkCrew.isChecked()) {
                // 打勾: 隱藏crew文字，顯示下拉選單，將下拉選單的選項填入editText
                editSeatNum.setEnabled(false);
                editSeatNum.setText(spinnerCar.getSelectedItem().toString());
                checkCrew.setText("");
                spinnerCar.setVisibility(View.VISIBLE);
            } else {
                // 未打勾: 顯示crew文字，隱藏下拉選單
                editSeatNum.setEnabled(true);
                editSeatNum.setText("");
                checkCrew.setText("crew");
                spinnerCar.setVisibility(View.GONE);
            }
        });

        //設定手動輸入商品號的動作
        editItemNum.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                String searchString = editItemNum.getText().toString();
                if (!searchString.equals("")) {

                    //search item
                    final String itemCode;
                    if (searchString.length() < 3) {
                        String sku = String.format("%03d", Integer.valueOf(searchString));
                        itemCode = mIFEDBFunction.getItemCode(sku);
                    } else {
                        itemCode = mIFEDBFunction.getItemCode(searchString);
                    }

                    ItemDataPack itemPack = DBQuery.getProductInfo(mContext, errMsg, FlightData.SecSeq, searchString, null, 0);
                    //查無此商品
                    if (itemPack == null) {
                        MessageBox.show("", "Pno Code error", mContext, "Return");
                        return true;
                    } else {
                        if (!mIFEDBFunction.getItemID(searchString).equals("")) {
                            if (mCrewCart == null) {
                                mIFEFunction.createCart(seatNo)
                                    .subscribeOn(Schedulers.io())
                                    .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(ifeReturnData -> {
                                        Cursor.Normal();
                                        if (ifeReturnData.isSuccess()) {
                                            mCrewCart = (CrewCart)ifeReturnData.getData();

                                            addItemToCart(itemCode, false);

                                        } else {
                                            Cursor.Normal();
                                            if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                                                if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                                                    ArmsUtils.startActivity(IFEActivity01.class);
                                                    finish();
                                                }
                                            }
                                        }
                                    });
                            } else {
                                addItemToCart(itemCode, false);
                            }
                        } else {
                            createCurrentItemJson(searchString, 1);
                            editItemNum.setText("");
                        }
                    }
                } else {
                    imm.hideSoftInputFromWindow(editItemNum.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        //輸入座位號 or 選擇車號後按下enter, 搜尋訂單
        editSeatNum.setOnKeyListener((v, keyCode, event) -> {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                String searchSeatNo = editSeatNum.getText().toString();
                if (!searchSeatNo.equals("")) {
                    // 取得對應座位號的Crew Cart
                    getCrewCart();
                } else {
                    MessageBox.show("", "Please input seat number", mContext, "Return");
                }

                imm.hideSoftInputFromWindow(editSeatNum.getWindowToken(), 0);
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

        //discount清單
        imageViewDiscount = findViewById(R.id.imgDiscountPlus);
        imageViewDiscount.setOnClickListener(view -> {
            try {
                //傳遞現有的折扣清單
                Bundle argument = new Bundle();
                argument.putString("fromWhere", "CrewCartActivity");
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

        btnPay = findViewById(R.id.btnPay);
        btnPay.setOnClickListener(view -> {
            if (adapter.getCount() == 0) {
                MessageBox.show("", "No item, can't pay", mContext, "Return");
                return;
            }

            //檢查SeatNumber有無輸入
            if (editSeatNum.getText().toString().trim().equals("")) {
                MessageBox.show("", "Please input seat number.", mContext, "Return");
                return;
            }

            //使用座位號碼
            if (!checkCrew.isChecked() && !editSeatNum.getText().toString().trim().matches("[0-9]{1,2}[a-zA-Z]{1}")) {
                MessageBox.show("", "Please check seat number", mContext, "Return");
                return;
            }

            // IFE數量要算進去?
            //商品訂購量大於可販售量(POS庫存量)，不可進行付款
            if (adapter.isQtyMoreThanStock(false)) {
                MessageBox.show("", "Qty not enough", mContext, "Return");
                return;
            }

            // 1. 顯示商品Remark
            // 商品Remark
            StringBuilder itemRemarkBuilder = new StringBuilder();
            for (String s : itemRemark) {
                itemRemarkBuilder.append(s + "\n");
            }

            // 2. 檢查贈品有貨未scan
            StringBuilder giftNotScanString = new StringBuilder();
            if (adapter.isOnlineSale()) {
                for (BasketItem singleItem : basketItemPack.items) {
                    if (singleItem.GiftFlag.equals("Y") &&
                        (singleItem.POSStock > 0 || singleItem.IFEStock > 0) &&
                        singleItem.SalesQty > giftList.get(singleItem.ItemCode).getGiftScan()) {
                        giftNotScanString.append("Scan gift no. " + singleItem.SerialCode + " * "
                            + (singleItem.SalesQty - giftList.get(singleItem.ItemCode).getGiftScan()) + "\n");
                    }
                }
            } else {
                // 提示Scan贈品:
                // 有應售數量且庫存不為零
                for (BasketItem singleItem : basketItemPack.items) {
                    if (singleItem.GiftFlag.equals("Y") &&
                        (singleItem.POSStock > 0 || singleItem.SalesQty <= singleItem.POSStock) &&
                        singleItem.SalesQty > giftList.get(singleItem.ItemCode).getGiftScan()) {

                        if (singleItem.SalesQty <= singleItem.POSStock) {
                            giftNotScanString.append("Scan gift no. ").append(singleItem.SerialCode).append(" * ").append(singleItem.SalesQty - giftList.get(singleItem.ItemCode).getGiftScan()).append("\n");
                        } else {
                            int qty = singleItem.POSStock - giftList.get(singleItem.ItemCode).getGiftScan();
                            if (qty > 0) {
                                giftNotScanString.append("Scan gift no. ").append(singleItem.SerialCode).append(" * ").append(qty).append("\n");
                            }
                        }
                    }
                }
            }

            // 3. 檢查贈品缺貨未scan
            boolean flag = false;
            for (String key : giftList.keySet()) {
                ItemInfo item = giftList.get(key);
                if ((Integer)item.getQty() > item.getGiftScan()) {
                    flag = true;
                    break;
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

            // 贈品沒貨未scan的確認視窗
            else if (flag) {
                if (MessageBox.show("", "Does PAX agree to pay without gift?", mContext, "Yes", "No")) {
                    successPay(itemRemarkBuilder.toString());
                }
            }

            // pay
            else {
                successPay(itemRemarkBuilder.toString());
            }
        });

        //items
        itemListView = findViewById(R.id.lvItemList);
        adapter = new ItemListPictureModifyAdapter(mContext, itemListView, itemListView.getRightViewWidth());
        //點選Item圖片放大圖片
        adapter.setFilpperFunctionClickListener(itemInfo -> {
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
        adapter.setItemInfoClickListener(itemInformationClickListener);

        //滑動刪除, 直接刪除adapter內的物品，將adapter回傳
        adapter.setItemSwipeListener(position -> {
            try {
                //GiftFlag 贈品，不可單獨刪除
                if (adapter.getItem(position).getPrice() == 0) {
                    itemListView.hiddenRight(itemListView.mPreItemView);
                    MessageBox.show("", getString(R.string.Gift_Cannot_Removed), mContext, "Return");
                    return;
                }

                if (mCrewCart.getItems().size() != 1) {
                    Item item = new Item(mIFEDBFunction.getItemID(adapter.getItem(position).getItemCode()), 0);
                    itemQuantityMap = new HashMap<>();
                    itemQuantityMap.put(item, 0);
                    mIFEFunction.updateCart(mCrewCart, itemQuantityMap)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(ifeReturnData -> {
                            if (ifeReturnData.isSuccess()) {
                                mCrewCart = (CrewCart)ifeReturnData.getData();

                                String itemCode = adapter.getItem(position).getItemCode();
                                int count = 0 - (int)adapter.getItem(position).getQty();
                                adapter.removeItem(position);

                                if (adapter.getCount() == 0) {
                                    for (AllDiscountType allDiscountType : discountHintList) {
                                        s_Transaction.DeleteDiscountList(allDiscountType.Type);
                                    }
                                }

                                for (AllDiscountType allDiscountType : discountHintList) {
                                    if (allDiscountType.Type.contains("STAFF")) {
                                        s_Transaction.DeleteDiscountList(allDiscountType.Type);
                                    }
                                }

                                try {
                                    getDBData(s_Transaction.ModifyItemList(itemCode, count), false, false);
                                } catch (Exception e) {
                                    MessageBox.show("", "Get sales info error", mContext, "Return");
                                    return;
                                }

                                itemListView.hiddenRight(itemListView.mPreItemView);

                                Cursor.Normal();

                            } else {
                                Cursor.Normal();
                                if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                                    if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                                        ArmsUtils.startActivity(IFEActivity01.class);
                                        finish();
                                    }
                                }
                            }
                        });
                } else {
                    mIFEFunction.emptyCart(mCrewCart)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(ifeReturnData -> {
                            mCrewCart = null;
                            mCrewOrder = null;

                            String itemCode = adapter.getItem(position).getItemCode();
                            int count = 0 - (int)adapter.getItem(position).getQty();
                            mTSQL.WriteLog(FlightData.SecSeq, LogType.ACTION, "Online Sale", "", "Delete item - " + itemCode + " - ItemQty: " + adapter.getItem(position).getQty());
                            adapter.removeItem(position);

                            if (adapter.getCount() == 0) {
                                for (AllDiscountType allDiscountType : discountHintList) {
                                    s_Transaction.DeleteDiscountList(allDiscountType.Type);
                                }
                            }

                            for (AllDiscountType allDiscountType : discountHintList) {
                                if (allDiscountType.Type.contains("STAFF")) {
                                    s_Transaction.DeleteDiscountList(allDiscountType.Type);
                                }
                            }

                            try {
                                getDBData(s_Transaction.ModifyItemList(itemCode, count), false, false);
                            } catch (Exception e) {
                                MessageBox.show("", "Get sales info error", mContext, "Return");
                                return;
                            }

                            itemListView.hiddenRight(itemListView.mPreItemView);
                        });
                }
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Delete item error", mContext, "Return");
            }
        });
        adapter.setIsOnlineSale(true);
        itemListView.setAdapter(adapter);

        //取得可以使用的幣別, 20
        final StringBuilder err = new StringBuilder();
        DBQuery.AllCurrencyListPack allCurrencyPack = DBQuery.getAllCurrencyList(mContext, err);
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

        //商品總額
        txtMoney = findViewById(R.id.txtMoney);

        discountAdapter = new DiscountAdapter(mContext, discountList);
        discountListView = findViewById(R.id.discountList);
        discountListView.setAdapter(discountAdapter);

        // 下拉選單, 取得所有其他的車號塞進去
        spinnerCar = findViewById(R.id.spinner02);
        DBQuery.CartNoPack numbers = DBQuery.getAllCartList(mContext, err);
        if (numbers == null) {
            MessageBox.show("", "Get cart number error", mContext, "Return");
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
            return;
        }
        ArrayAdapter<String> drawerList = new ArrayAdapter<String>(this, R.layout.spinner_item);
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < numbers.cartList.length; i++) {
            drawerList.add(numbers.cartList[i].CartNo);
            list.add(numbers.cartList[i].CartNo);
        }
        list.add(0, "Cart");
        SpinnerHideItemAdapter spinnerHideItemAdapter = new SpinnerHideItemAdapter(this, R.layout.spinner_item, list, 0);

        spinnerCar.setAdapter(spinnerHideItemAdapter);
        spinnerCar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (checkCrew.isChecked()) {
                    // 將所選車號塞到edittext內
                    editSeatNum.setText(spinnerCar.getSelectedItem().toString());

                    if (!editSeatNum.getText().toString().equals("Cart")) {
                        // 取得對應座位號的Crew Cart
                        getCrewCart();
                    }
                } else {
                    editSeatNum.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerCar.setVisibility(View.GONE);

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }

    // 一、將輸入的transfer in/ 掃描的單品包成JsonObj
    // 未在購物車的單品: itemQty= 1
    // 已在購物車的單品: itemQty傳差異量 (adapter內不要動)
    // 贈品: itemQty=1
    // Transfer: itemQty=1 (不使用)
    // 已在購物車內的item要自己過濾掉
    private void createCurrentItemJson(final String itemNum, int itemQty) {
        imm.hideSoftInputFromWindow(editItemNum.getWindowToken(), 0);
        StringBuilder err = new StringBuilder();

        // true -> 先call setSeatNo再加入商品
        // false -> 直接加入商品
        if (adapter.getCount() == 0 && s_Transaction.SeatNo.equals("")) {
            try {
                JSONObject json = s_Transaction.SetSeatNo(editSeatNum.getText().toString());
                String retCode = json.getString("ReturnCode");
                // String retMsg= json.getString("ReturnMessage");
                if (!retCode.equals("0")) {
                    MessageBox.show("", "Please check seat No.", mContext, "Return");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Please check seat No.", mContext, "Return");
                return;
            }
        }

        /*------------- 1. transfer in -------------*/

        if (itemNum.length() >= 33) {
            final String splitString[] = itemNum.split(",");

            // 掃讀Transfer In QR Code 目的車號錯誤
            if (!splitString[2].equals(FlightData.CartNo)) {
                MessageBox.show("", "Wrong cart", mContext, "Return");
                return;
            }
            try {
                // Transfer不可In Out同車
                TransferItemPack transItemPack_01 = DBQuery.queryTransferItemQty(mContext, err, splitString[0], "OUT");
                if (transItemPack_01 == null) {
                    MessageBox.show("", "Query transfer list error", mContext, "Return");
                    return;
                }
                if (transItemPack_01.transfers != null) {
                    MessageBox.show("", "Can't transfer to origin cart!", mContext, "Return");
                    return;
                }

                //  車櫃號後三碼 + 時分秒(六碼) + , + 來源車櫃(六碼) + , + 目的車櫃(七碼) + , +  數量(二碼) + Code(七碼) ....
                //  EX: 2A1 095735 , 882957 , 102809 , 01 5001050, 01 5001052
                //  Transfer No.: 2A1095735
                // 若有傳回商品列表, 則這個transfer no 就已經是用過的
                TransferItemPack transItemPack = DBQuery.queryTransferItemQty(mContext, err, splitString[0], null);
                if (transItemPack == null) {
                    MessageBox.show("", "Query transfer list error", mContext, "Return");
                    return;
                }
                if (transItemPack.transfers != null) {
                    MessageBox.show("", "Transfer No. was used!", mContext, "Return");
                    return;
                }

            } catch (Exception e) {
                MessageBox.show("", "Please retry", mContext, "Return");
                return;
            }

            //暫存 Transfer 商品內容
            StringBuilder transferInfo = new StringBuilder();
            transferItemTmp = new ArrayList<>();
            transferItemTmpCount = new ArrayList<>();

            for (int j = 3; j < splitString.length; j++) {
                //用Transfer Out內的編號查詢DB
                ItemDataPack itemPack = DBQuery.getProductInfo(mContext, err, FlightData.SecSeq, splitString[j].substring(2), null, 2);

                //查無此商品
                if (itemPack == null) {
                    MessageBox.show("", "Pno code error", mContext, "Return");
                    return;
                }
                //加入暫存清單
                else {
                    transferItemTmp.add(itemPack.items[0]);
                    transferItemTmpCount.add(splitString[j].substring(0, 2));
                }
            }

            //詢問是否執行Transfer In：
            //        No 361 X 2
            //        No 014 X 1
            //        To Transfer?
            for (int i = 0; i < transferItemTmp.size(); i++) {
                transferInfo.append("No. " +
                    transferItemTmp.get(i).SerialCode + " * " + Integer.parseInt(transferItemTmpCount.get(i)) + "\n");
            }
            transferInfo.append("To transfer in?");

            if (MessageBox.show("", transferInfo.toString(), mContext, "Yes", "No")) {
                try {
                    // 建立tranfer單據
                    DBQuery.TransferAdjItem[] returnItems = new DBQuery.TransferAdjItem[splitString.length - 3];
                    for (int j = 3; j < splitString.length; j++) {
                        returnItems[j - 3] = new DBQuery.TransferAdjItem();
                        returnItems[j - 3].ItemCode = splitString[j].substring(2);
                        returnItems[j - 3].TransferQty = Integer.valueOf(splitString[j].substring(0, 2));
                    }
                    // transferNo, carFrom ,carTo, in or out, items
                    DBQuery.TransferAdjItemPack returnPack = new DBQuery.TransferAdjItemPack(
                        splitString[0], splitString[1], splitString[2], "IN", returnItems
                    );
                    // class轉 json Obj
                    Gson gson = new Gson();
                    String jsonString = gson.toJson(returnPack);
                    JSONObject jsonObj = new JSONObject(jsonString);

                    // 回傳tranfer單據
                    if (!(DBQuery.transferItemQty(mContext, err, jsonObj))) {
                        CrewCartActivity.this.runOnUiThread(() -> MessageBox.show("", "Transferred in repeatly", mContext, "Return"));
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageBox.show("", "Transfer error", mContext, "Return");
                    return;
                }

                // 將轉入商品加入購物車
                try {
                    // 先加完全部的主商品, 再在getDBData內一個個scan贈品起來
                    JSONObject json = new JSONObject();
                    for (int i = 0; i < transferItemTmp.size(); i++) {
                        if (transferItemTmp.get(i).ItemPriceUS > 0) {
                            json = s_Transaction.ModifyItemList(transferItemTmp.get(i).ItemCode, Integer.valueOf(transferItemTmpCount.get(i)));
                        }
                    }
                    getDBData(json, false, true);

                } catch (Exception e) {
                    e.printStackTrace();
                    MessageBox.show("", "Get sales info error", mContext, "Return");
                }
            }
        }

        /*------------- 2. Item Number -------------*/
        else {

            ItemDataPack itemPack = DBQuery.getProductInfo(mContext, err, FlightData.SecSeq, itemNum, null, 0);
            //查無此商品
            if (itemPack == null) {
                MessageBox.show("", "Pno Code error", mContext, "Return");
                return;
            }

            // Scan到贈品
            // 1. 檢查local清單內是否存在此贈品
            // 2. 是否有被scan過, 有的話就return
            if (itemPack.items[0].ItemPriceUS == 0.0) {
                newGiftItemModi(itemPack.items[0], itemQty, false);
            }

            //Scan到普通item，包起來
            else {
                // 如果非小視窗調整商品數量
                // 是Scan或輸入ItemNo的話，在adapter內的商品就不動作
                if (adapter.getItem(itemPack.items[0].ItemCode) != null) {
                    return;
                }
                try {
                    getDBData(s_Transaction.ModifyItemList(itemPack.items[0].ItemCode, Integer.valueOf(itemQty)), false, false);
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageBox.show("", "Get sales info error", mContext, "Return");
                }
            }
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
                CrewCartActivity.this.runOnUiThread(() -> MessageBox.show("", "Wrong gift", mContext, "Return"));
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
                CrewCartActivity.this.runOnUiThread(() -> MessageBox.show("", "Gift can't be " + ss + " individually", mContext, "Return"));
                return;
            }
        }

        //如果清單內存在此贈品，表示為可以調整數量的item
        //如果scan過的數量大於應贈送總數的話, 返回
        if ((Integer)giftItem.getQty() <= giftItem.getGiftScan()) {
            CrewCartActivity.this.runOnUiThread(() -> MessageBox.show("", "Gift already scan", mContext, "Return"));
            return;
        }

        // 已scan數量>=庫存 要擋
        // Transfer收據上買A送B，有可能為A一個B零個，這時若庫存不足，不要顯示 Stock not enough
        if (!isTransfer && (giftItem.getStock() == 0 || giftItem.getStock() <= giftItem.getGiftScan())) {
            CrewCartActivity.this.runOnUiThread(() -> MessageBox.show("", "Qty not enough", mContext, "Return"));
            return;
        }

        //小於等於應贈送總數的話就增加adapter內的數量, 並將對應物件數量存回去
        giftItem.setGiftScan(giftItem.getGiftScan() + addCount);
        giftList.put(item.ItemCode, giftItem);

        // 如果adapter內沒有此贈品, add
        if (adapter.getItem(giftItem.getItemCode()) == null) {
            // 販售量大於庫存量
            if ( // 連線
                (adapter.isOnlineSale() &&
                    ((int)giftItem.getQty() > giftItem.getStock() || (int)giftItem.getQty() > giftItem.getIfeStock()))
                    // 離線
                    || (!adapter.isOnlineSale() && (int)giftItem.getQty() > giftItem.getStock())) {
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
                    ((int)giftItem.getQty() > giftItem.getStock() || (int)giftItem.getQty() > giftItem.getIfeStock()))
                    // 離線
                    || (!adapter.isOnlineSale() && (int)giftItem.getQty() > giftItem.getStock())) {
                adapter.modifiedItemColorChange(
                    adapter.getItemId(giftItem.getItemCode()), true);
            }
        }

        CrewCartActivity.this.runOnUiThread(() -> adapter.notifyDataSetChanged());
    }

    // 二、顯示收到的物品
    private void getDBData(final JSONObject jsonInput, final boolean resetGiftMsg, final boolean isTransfer) {

        /*---------------------------------------------*/
        final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        new Thread() {
            public void run() {
                isGiftMagShow = !resetGiftMsg;

                final StringBuilder err = new StringBuilder();
                // 解Jason
                BasketItemPack tmpItemPack = DBQuery.modifyBasket(err, jsonInput);
                if (tmpItemPack == null) {
                    CrewCartActivity.this.runOnUiThread(() -> {
                        mloadingDialog.dismiss();
                        MessageBox.show("", err.toString(), mContext, "Return");
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
                giftShowProcessing(msgShow, newGiftList);

                // 如果是transfer進來, 處理有贈品的情況
                if (isTransfer && transferItemTmp != null && transferItemTmpCount != null) {
                    for (int i = 0; i < transferItemTmp.size(); i++) {
                        if (transferItemTmp.get(i).ItemPriceUS == 0) {
                            newGiftItemModi(transferItemTmp.get(i), Integer.valueOf(transferItemTmpCount.get(i)), isTransfer);
                        }
                    }
                }

                CrewCartActivity.this.runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    txtMoney.setText(setTotalMoneyTextView());
                });

                // 顯示提醒視窗
                final String finalHint = msgShow.toString();
                if (!finalHint.equals("")) {
                    CrewCartActivity.this.runOnUiThread(() -> MessageBox.show("", finalHint, CrewCartActivity.this, "Ok"));
                }

                // 如果沒有商品清單就允許更改座位號碼
                CrewCartActivity.this.runOnUiThread(() -> {
                    if (adapter.getCount() == 0) {
                        editSeatNum.setEnabled(true);
                        checkCrew.setEnabled(true);
                    } else {
                        editSeatNum.setEnabled(false);
                        checkCrew.setEnabled(false);
                    }
                });

                mloadingDialog.dismiss();

            }
        }.start();
    }

    // 主動被動折扣的處理
    private HashMap<String, Integer> discountMsgProcessing(StringBuilder discountMsg, HashMap<String, Integer> newGiftHintList) {
        discountAdapter.clear();
        ArrayList<AllDiscountType> discountPassiveType = new ArrayList<>();
        ArrayList<AllDiscountType> discontActiveType = new ArrayList<>();

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
                    newGiftHintList.put(basketItemPack.types[i].Type + basketItemPack.types[i].Description,
                        basketItemPack.types[i].DiscountCount);
//                    }
                }

                // 主動式折扣: 加到adapter, 加到訊息比對list過濾
                else {
                    AllDiscountType tmp = new AllDiscountType
                        (basketItemPack.types[i].Type, basketItemPack.types[i].Description);
                    discontActiveType.add(tmp);
                    newDiscountList.add(tmp);
                }
            }
        }

        // 本來沒折扣, 回傳有折扣: 全部塞進去, 全部顯示
        if (discountHintList.size() == 0 && newDiscountList.size() > 0) {
            discountHintList = new ArrayList<>(newDiscountList);
            for (AllDiscountType discount : discountHintList) {
                discountMsg.append(discount.Type + " - " + discount.Description + "\n");
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
                discountMsg.append(newDiscount.Type + " - " + newDiscount.Description + "\n");
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
        for (AllDiscountType tmp : discontActiveType) {
            discountAdapter.addItem(tmp.Type + " - " + tmp.Description);
        }
        CrewCartActivity.this.runOnUiThread(() -> discountAdapter.notifyDataSetChanged());

        return newGiftHintList;
    }

    // 贈品贈送訊息的處理
    private void discountGiftHindProcessing(StringBuilder discountGiftString, HashMap<String, Integer> newGiftHintList) {
        // 要移除的回傳贈品key
        ArrayList<String> removeList = new ArrayList<>();

        // 1. 本來沒贈品, 回傳沒贈品: 沒反應, 只是個清單

        // 2. 本來沒贈品, 回傳有贈品: 全部塞進去, 全部顯示
        if (giftHintList.size() == 0 && newGiftHintList.size() > 0) {
            giftHintList = new HashMap<>(newGiftHintList);
            for (String key : giftHintList.keySet()) {
                String[] tmpSplit = key.split("@@");
                if (tmpSplit.length >= 2) {
                    discountGiftString.append(tmpSplit[1] + "\n");
                }
            }
        }

        // 3. 本來有贈品, 回傳有贈品:
        if (giftHintList.size() > 0 && newGiftHintList.size() > 0) {

            // (1) 將現有清單比對新的清單
            for (String oldKey : giftHintList.keySet()) {
                // 如果回傳值沒有贈品, remove, 不顯示
                if (newGiftHintList.get(oldKey) == null) {
//                    giftHintList.remove(oldKey);
                    removeList.add(oldKey);
                }

                // 如果回傳有此贈品
                else {
                    int newCount = newGiftHintList.get(oldKey);
                    // 比對新舊數量
                    int oldCount = giftHintList.get(oldKey);
                    // 數量小於舊的: 塞回去, 不顯示
                    if (newCount < oldCount) {
                        giftHintList.put(oldKey, newCount);
                    }
                    // 數量大於舊的: 塞回去, 顯示
                    else if (newCount > oldCount) {
                        giftHintList.put(oldKey, newCount);
                        String[] tmpSplit = oldKey.split("@@");
                        if (tmpSplit.length >= 2) {
                            discountGiftString.append(tmpSplit[1] + "\n");
                        }
                    }
                    // 數量等於舊的: 不塞回去, 不顯示
                }
            }

            // (2) 將新的清單比對現有清單
            for (String newKey : newGiftHintList.keySet()) {
                // 如果有新增的贈品, 塞進去, 顯示
                if (giftHintList.get(newKey) == null) {
                    giftHintList.put(newKey, newGiftHintList.get(newKey));
                    String[] tmpSplit = newKey.split("@@");
                    if (tmpSplit.length >= 2) {
                        discountGiftString.append(tmpSplit[1] + "\n");
                    }
                }
            }

            // (3) 移除現有清單所有多的贈品
            for (String removeKey : removeList) {
                giftHintList.remove(removeKey);
            }
        }

        // 4. 本來有贈品, 回傳沒贈品: 全部清掉, 全部不顯示
        if (giftHintList.size() > 0 && newGiftHintList.size() == 0) {
            giftHintList = new HashMap<>();
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
                // String itemCode, String SerialCode, String monyType, int price, String itemName,
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
    private void giftShowProcessing(StringBuilder giftHindString, HashMap<String, BasketItem> newGiftList) {
        // 要移除的回傳贈品key
        ArrayList<String> removeList = new ArrayList<>();

        //如果有回傳的贈品
        if (newGiftList.size() > 0) {

            //如果local清單有東西
            if (giftList.size() > 0) {

                //將現有清單比對新的清單
                for (String oldKey : giftList.keySet()) {
                    BasketItem newItem = newGiftList.get(oldKey);

                    // 1. 如果回傳值無此贈品，直接remove
                    if (newItem == null) {
//                        giftList.remove(oldKey);
                        removeList.add(oldKey);
                    }
                    // 2. 有的話比對新的贈送數量和已scan的贈送數量
                    else {
                        ItemInfo oldItem = giftList.get(oldKey);

                        // 如果新贈送數量大於舊數量, 且新數量大於庫存量
                        if (newItem.SalesQty > (Integer)oldItem.getQty() &&
                            newItem.POSStock < newItem.SalesQty) {
                            giftHindString.append("Qty not enough: no." + newItem.SerialCode + " * 1\n");
                        }

                        // (1) 如果新的應贈送數量與舊的不同，取代
                        if (newItem.SalesQty != (Integer)oldItem.getQty()) {
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
                                ((Integer)oldItem.getQty() > oldItem.getStock() || (Integer)oldItem.getQty() > oldItem.getIfeStock()))
                                || (!adapter.isOnlineSale()) && (Integer)oldItem.getQty() > oldItem.getStock()) {
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
                            if ((adapter.isOnlineSale() &&
                                ((Integer)oldItem.getQty() > oldItem.getStock() || (Integer)oldItem.getQty() > oldItem.getIfeStock())
                                || (!adapter.isOnlineSale()) && (Integer)oldItem.getQty() > oldItem.getStock())) {
                                if ((Integer)oldItem.getQty() > oldItem.getStock()) {
                                    adapter.addItem(
                                        oldItem.getItemCode(), oldItem.getSerialNo(), "US",
                                        oldItem.getPrice(), oldItem.getItemName(),
                                        oldItem.getStock(), 0,
                                        true, true);
                                }
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
                        BasketItem newItem = newGiftList.get(newKey);
                        giftList.put(newItem.ItemCode,
                            new ItemInfo(newItem.ItemCode,
                                newItem.DrawerNo + "-" + newItem.SerialCode, "US", newItem.USDPrice, newItem.ItemName,
                                newItem.POSStock, newItem.SalesQty, newItem.IFEStock, 0, false, false));

                        if (adapter.isOnlineSale()) {

                        } else {
                            // 庫存不足
                            if ((adapter.isOnlineSale() &&
                                (newItem.SalesQty > newItem.POSStock || newItem.SalesQty > newItem.IFEStock))
                                || (!adapter.isOnlineSale()) && newItem.SalesQty > newItem.POSStock) {
                                adapter.addItem(
                                    newItem.ItemCode,
                                    newItem.DrawerNo + "-" + newItem.SerialCode, "US", newItem.USDPrice, newItem.ItemName,
                                    newItem.POSStock, 0, true, true);
                                giftHindString.append("Qty not enough: no." + newItem.SerialCode + " * 1\n");
                            }
                        }
                    }
                }

                // 移除現有清單內多的贈品
                for (String removeKey : removeList) {
                    giftList.remove(removeKey);
                }
            }
            // local為空的話就將所有newGift放入local內
            else {
                for (String key : newGiftList.keySet()) {
                    BasketItem item = newGiftList.get(key);
                    //String itemCode, String serialNo, String monyType, Double price, String itemName
                    //int stock, int qty, int ifeStock,int giftScan, boolean canDiscount, boolean isModified

                    giftList.put(item.ItemCode,
                        new ItemInfo(item.ItemCode,
                            item.DrawerNo + "-" + item.SerialCode, "US", item.USDPrice, item.ItemName,
                            item.POSStock, item.SalesQty, item.IFEStock, 0, false, false));

                    if ((adapter.isOnlineSale() && (item.SalesQty > item.POSStock || item.SalesQty > item.IFEStock))
                        || (!adapter.isOnlineSale() && item.SalesQty > item.POSStock)) {
                        // 庫存不足
                        adapter.addItem(
                            item.ItemCode,
                            item.DrawerNo + "-" + item.SerialCode, "US", item.USDPrice, item.ItemName,
                            item.POSStock, 0, true, true);
                        giftHindString.append("Qty not enough: no." + item.SerialCode + " * 1\n");
                    }
                }
            }
        }
        //如果回傳無贈品, 清空giftList
        else {
            giftList = new HashMap<>();
        }
    }

    // 取得應付款總額
    private String setTotalMoneyTextView() {
        try {
            if (basketItemPack != null && listCurrency != null) {
                // 先呼叫一次，取得總應付金額，這樣 GetCurrencyMaxAmount 才取得到值
                s_Transaction.GetPaymentMode();

                String currency = spinnerCurrency.getSelectedItem().toString();
                DBQuery.ShouldPayMoney payItem = DBQuery.getPayMoneyNow(new StringBuilder(),
                    s_Transaction.GetCurrencyMaxAmount(currency));
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
        if (mCrewCart != null) {
            mIFEFunction.submitCart(mCrewCart)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                .flatMap(ifeReturnData -> {
                    if (ifeReturnData.isSuccess()) {
                        return mIFEFunction.getOrders((String)ifeReturnData.getData(), null);
                    } else {
                        return io.reactivex.Observable.just(ifeReturnData);
                    }
                })
                .flatMap(ifeReturnData -> {
                    if (ifeReturnData.isSuccess()) {
                        mCrewOrder = ((List<CrewOrder>)ifeReturnData.getData()).get(0);
                        return mIFEFunction.initOrder(mCrewOrder);
                    } else {
                        return io.reactivex.Observable.just(ifeReturnData);
                    }
                }).subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(Cursor::Normal)
                .subscribe(ifeReturnData -> {
                    if (ifeReturnData.isSuccess()) {
                        //傳遞整包BasketItemPack
                        Bundle bundle = new Bundle();
                        Gson gson = new Gson();
                        String jsonOrderItemPack = gson.toJson(basketItemPack);
                        bundle.putString("Currency", spinnerCurrency.getSelectedItem().toString());
                        bundle.putString("BasketItemPack", jsonOrderItemPack);
                        bundle.putString("fromWhere", "CrewCartActivity");
                        bundle.putString("RemarkMsg", LAGRemark);

                        bundle.putParcelable("CrewOrder", mCrewOrder);

                        Cursor.Normal();

                        Intent intent = new Intent(mActivity, PayActivity.class);
                        intent.putExtras(bundle);
                        mActivity.startActivity(intent);
                        finish();
                    } else {
                        if (ifeReturnData.getErrMsg().contains("General Server Side error")) {
                            MessageBox.show("", getString(R.string.Error_SeatNotAvailable), mContext, "Return");
                            return;
                        } else {
                            if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                                if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                                    ArmsUtils.startActivity(IFEActivity01.class);
                                    finish();
                                }
                            }
                        }
                    }
                });
        } else {
            Cursor.Normal();
            //傳遞整包BasketItemPack
            Bundle bundle = new Bundle();
            Gson gson = new Gson();
            String jsonOrderItemPack = gson.toJson(basketItemPack);
            bundle.putString("Currency", spinnerCurrency.getSelectedItem().toString());
            bundle.putString("BasketItemPack", jsonOrderItemPack);
            bundle.putString("fromWhere", "CrewCartActivity");
            bundle.putString("RemarkMsg", LAGRemark);

            Cursor.Normal();

            Intent intent = new Intent(mActivity, PayActivity.class);
            intent.putExtras(bundle);
            mActivity.startActivity(intent);
            finish();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //調整數量
        if ((requestCode == ITEMS_DETAIL) && resultCode == RESULT_OK) {
            int index = Integer.valueOf((String)data.getExtras().get("index"));

            String itemCode = adapter.getItem(index).getItemCode();
            int modifyQty = Integer.valueOf((String)data.getExtras().get("newQty")) - (int)adapter.getItem(index).getQty();

            mTSQL.WriteLog(FlightData.SecSeq, LogType.ACTION, "Online Sale", "", "Modify item qty - " + itemCode + " - qty: " + data.getExtras().get("newQty"));

            try {
                if (modifyQty != 0) {
                    if (!mIFEDBFunction.getItemID(itemCode).equals("")) {
                        Item item = new Item(mIFEDBFunction.getItemID(itemCode), Integer.valueOf((String)data.getExtras().get("newQty")));
                        itemQuantityMap = new HashMap<>();
                        itemQuantityMap.put(item, Integer.valueOf((String)data.getExtras().get("newQty")));
                        mIFEFunction.updateCart(mCrewCart, itemQuantityMap)
                            .subscribeOn(Schedulers.io())
                            .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally(Cursor::Normal)
                            .subscribe(ifeReturnData -> {
                                if (ifeReturnData.isSuccess()) {
                                    mCrewCart = (CrewCart)ifeReturnData.getData();
                                    s_Transaction.setIFEStock((Integer)data.getExtras().get("IFEStock"));
                                    getDBData(s_Transaction.ModifyItemList(itemCode, modifyQty), true, false);
                                    Cursor.Normal();
                                } else {
                                    Cursor.Normal();
                                    if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                                        if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                                            ArmsUtils.startActivity(IFEActivity01.class);
                                            finish();
                                        }
                                    }
                                }
                            });
                    } else {
                        s_Transaction.setIFEStock((Integer)data.getExtras().get("IFEStock"));
                        getDBData(s_Transaction.ModifyItemList(itemCode, modifyQty), true, false);
                    }
                }
            } catch (Exception e) {
                MessageBox.show("", "Get sales info error", mContext, "Return");
                return;
            }
        }

        //折扣資訊
        else if ((requestCode == DISCOUNT_INFO) && resultCode == RESULT_OK) {
            try {
                HashMap<String, String> discountHashmap = (HashMap)data.getExtras().get("discountHashMap");
                if (discountHashmap != null && discountHashmap.size() > 0) {
                    Iterator it = discountHashmap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        JSONObject json = s_Transaction.AddDiscountList((String)pair.getKey(), (String)pair.getValue());
                        String retCode = json.getString("ReturnCode");
                        if (!retCode.equals("0")) {
                            MessageBox.show("", json.getString("ReturnMessage"), mContext, "Return");
                            return;
                        }
                    }
                }
                getDBData(s_Transaction.GetBasketInfo(), false, false);
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Get sales data error", mContext, "Return");
            }
        }

        isItemClickModified = false;
        super.onActivityResult(requestCode, resultCode, data);
    }


    //點選 Item 項目修改內容
    private ItemListPictureModifyAdapter.ItemInfoClickListener itemInformationClickListener = new ItemListPictureModifyAdapter.ItemInfoClickListener() {

        @Override
        public void txtItemInfoClickListener(int position) {

            try {
                ItemInfo item = adapter.getItem(position);

                mIFEFunction.requestCatalogItem(mIFEDBFunction.getIFEID(item.getItemCode()))
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ifeReturnData -> {
                        Cursor.Normal();
                        if (ifeReturnData.isSuccess()) {
                            Catalog catalog = (Catalog)ifeReturnData.getData();
                            item.setIfeStock(catalog.getCurrentCount());

                            //贈品不能手動調整數量
                            if (item.getPrice() == 0) {
                                MessageBox.show("", "Gift can not modify", mContext, "Return");
                                return;
                            }

                            //如果正在編輯就不能觸發第二次的activity彈跳視窗
                            if (!isItemClickModified) {
                                isItemClickModified = true;
                                Intent intent = new Intent();

                                intent.setClass(mActivity, ItemDetailOnlineActivity.class);

                                //String itemCode, String moneyType, int price, String itemName, int stock, int qt
                                intent.putExtra("index", String.valueOf(position));
                                intent.putExtra("item", item);
                                intent.putExtra("canModifiedToZero", "false");
                                intent.putExtra("fromWhere", "OnlineBasketActivity");
                                startActivityForResult(intent, ITEMS_DETAIL);
                            }
                        } else {
                            if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                                if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                                    ArmsUtils.startActivity(IFEActivity01.class);
                                    finish();
                                }
                            }
                        }
                    });
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Modify item error", mContext, "Return");
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
            android.util.Log.i("debug", "----codetype--" + temp);
            barcodeStr = new String(barcode, 0, barcodelen);

            if (editSeatNum.getText().toString().equals("")) {
                MessageBox.show("", "Please check seat No.", mContext, "Return");
                return;
            }

            mTSQL.WriteLog(FlightData.SecSeq, LogType.ACTION, "Online Sale", "", "Scan item code for sale - " + barcodeStr);

            if (mCrewCart == null) {
                mIFEFunction.createCart(editSeatNum.getText().toString())
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ifeReturnData -> {
                        Cursor.Normal();
                        if (ifeReturnData.isSuccess()) {
                            mCrewCart = (CrewCart)ifeReturnData.getData();

                            addItemToCart(barcodeStr, true);

                        } else {
                            Cursor.Normal();
                            if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                                if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                                    ArmsUtils.startActivity(IFEActivity01.class);
                                    finish();
                                }
                            }
                            editItemNum.requestFocus();
                        }
                    });
            } else {
                addItemToCart(barcodeStr, true);
            }
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

    private void addItemToCart(String sku, boolean isScan) {
        if (adapter.getCount() == 0) {
            try {
                JSONObject json = s_Transaction.SetSeatNo(editSeatNum.getText().toString());
                String retCode = json.getString("ReturnCode");
                if (!retCode.equals("0")) {
                    Cursor.Normal();
                    MessageBox.show("", "Please check seat No.", mContext, "Return");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Cursor.Normal();
                MessageBox.show("", "Please check seat No.", mContext, "Return");
                return;
            }
        }

        if (isScan) {
            if (sku.length() >= 33) {
                String s[] = sku.split(",");
                itemQuantityMap = new HashMap<>();

                for (int i = 3; i < s.length; i++) {
                    if (!mIFEDBFunction.getItemID(s[i].substring(2, 9)).equals("")) {
                        if (adapter.getItem(s[i].substring(2, 9)) != null) {
                            Item item = new Item(mIFEDBFunction.getItemID(s[i].substring(2, 9)), 1 + (Integer)adapter.getItem(s[i].substring(2, 9)).getQty());
                            itemQuantityMap.put(item, Integer.valueOf(s[i].substring(0, 2)) + (Integer)adapter.getItem(s[i].substring(2, 9)).getQty());
                        } else {
                            Item item = new Item(mIFEDBFunction.getItemID(s[i].substring(2, 9)), Integer.valueOf(s[i].substring(0, 2)));
                            itemQuantityMap.put(item, Integer.valueOf(s[i].substring(0, 2)));
                        }
                    }
                }
            } else {
                if (!mIFEDBFunction.getItemID(sku).equals("")) {
                    if (checkItemExist(sku)) {
                        if (checkIsGift(sku)) {
                            MessageBox.show("", "Gift can't be sold individually", mContext, "Return");
                            return;
                        } else {
                            Item item = new Item(mIFEDBFunction.getItemID(sku), 1);
                            itemQuantityMap.put(item, 1);
                        }
                    } else {
                        MessageBox.show("", "Pno code error", mContext, "Return");
                        return;
                    }
                }
            }
        } else {
            Item item = new Item(mIFEDBFunction.getItemID(sku), 1);
            itemQuantityMap = new HashMap<>();
            itemQuantityMap.put(item, 1);
        }

        mIFEFunction.updateCart(mCrewCart, itemQuantityMap)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ifeReturnData -> {
                if (ifeReturnData.isSuccess()) {
                    mCrewCart = (CrewCart)ifeReturnData.getData();

                    if (isScan) {
                        createCurrentItemJson(barcodeStr, 1);
                    } else {
                        createCurrentItemJson(mIFEDBFunction.getItemCode(sku), 1);
                    }

                    editItemNum.setText("");
                    Cursor.Normal();
                } else {
                    if (ifeReturnData.getErrMsg().contains("IFE Qty not enough")) {
                        if (MessageBox.show("", ifeReturnData.getErrMsg(), mContext, "Ok")) {

                            if (mCrewCart.getItems().size() == 0) {
                                mCrewCart = null;
                            }

                            editItemNum.setText("");
                            editItemNum.requestFocus();
                            DeviceUtils.hideSoftKeyboard(mContext, editItemNum);

                            editItemNum.requestFocus();
                            DeviceUtils.hideSoftKeyboard(this, editItemNum);
                        }
                    } else {
                        if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                            if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                                ArmsUtils.startActivity(IFEActivity01.class);
                                finish();
                            }
                        }
                    }
                }
            });
    }

    private void getCrewCart() {
        mIFEFunction.getCart(editSeatNum.getText().toString().trim())
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally(Cursor::Normal)
            .subscribe(ifeReturnData -> {
                if (ifeReturnData.isSuccess()) {

                    s_Transaction = new Transaction(mContext, FlightData.SecSeq);

                    mCrewCart = (CrewCart)ifeReturnData.getData();
                    adapter.clear();

                    try {
                        s_Transaction.SetSeatNo(editSeatNum.getText().toString().trim());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    for (Item item : mCrewCart.getItems()) {
                        s_Transaction.setIFEStock(item.getQuantity());
                        createCurrentItemJson(mIFEDBFunction.getItemCodeByItemID(item.getItemId()), item.getQuantity());
                    }

                    editItemNum.setEnabled(true);
                } else {
                    if (ifeReturnData.getErrMsg().contains("Catalog not found")) {
                        MessageBox.show("", "There is no item in crew cart", mContext, "Return");
                    } else {
                        if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                            if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                                ArmsUtils.startActivity(IFEActivity01.class);
                                finish();
                            }
                        }
                    }
                }
            });
    }

    private boolean checkItemExist(String sku) {
        ItemDataPack itemPack = DBQuery.getProductInfo(mContext, new StringBuilder(), FlightData.SecSeq, sku, null, 0);
        //查無此商品
        return itemPack != null;
    }

    private boolean checkIsGift(String sku) {
        ItemDataPack itemPack = DBQuery.getProductInfo(mContext, new StringBuilder(), FlightData.SecSeq, sku, null, 0);
        return itemPack.items[0].ItemPriceUS == 0.0;
    }
}
