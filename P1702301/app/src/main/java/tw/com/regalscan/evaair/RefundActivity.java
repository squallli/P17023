package tw.com.regalscan.evaair;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.google.gson.Gson;
import com.regalscan.sqlitelibrary.TSQL;
import org.json.JSONException;
import org.json.JSONObject;
import tw.com.regalscan.R;
import tw.com.regalscan.activities.ItemPictureActivity;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.component.SpinnerHideItemAdapter;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.RefundTransaction;
import tw.com.regalscan.db.VipSaleRefundTransaction;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.*;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;

public class RefundActivity extends AppCompatActivity {


    private Button btnReturn, btnRefund;
    public Context mContext;
    public Activity mActivity;
    private Spinner spinnerOrder;
    private TextView txtOrderListKind, txtTotalMoney, txtTag2, txtTag3;
    public ItemListPictureModifyAdapter adapter;
    private ListView itemListView;

    // preorder list pack
    private DBQuery.PreorderInfoPack preorderPack;

    private SpinnerHideItemAdapter orderList;

    // 所有單據號碼
    private ReceiptList receiptNoList, tmp_receiptNoList;
    // DFS的退款Function
    public static RefundTransaction _RefundTransaction;
    public static VipSaleRefundTransaction _VipSaleRefundTransaction;
    private ProgressDialog mloadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refund_01);

        // 整個pack
        Bundle argument = getIntent().getExtras();
        if (argument != null) {
            String itemString = argument.getString("jsonPack");
            Gson gson = new Gson();
            tmp_receiptNoList = gson.fromJson(itemString, ReceiptList.class);
            init();
        } else {
            if (MessageBox.show("", "No refund list", this, "Ok")) {
                finish();
            }
        }
    }

    private void init() {
        mContext = this;
        mActivity = this;
        enableExpandableList();

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
        });

        txtTag2 = findViewById(R.id.tag2);
        txtTag3 = findViewById(R.id.tag3);

        // VIP和Preorder
        txtTag2.setVisibility(View.VISIBLE);
        txtTag2.setText("Qty");
        txtTag3.setText("Price");

        //items
        itemListView = findViewById(R.id.lvItemList);
        adapter = new ItemListPictureModifyAdapter(mContext, itemListView);
        adapter.setFilpperFunctionClickListener(itemPictureClickListener);
        adapter.setIsModifiedItem(false);
        adapter.setIsRightTwoVisible(false);
        adapter.setQtyPutPrice(true);
        itemListView.setAdapter(adapter);

        //VIP和preorder單號
        txtOrderListKind = findViewById(R.id.txtVipNo);

        // 所有可退款單據號碼，列表排序為DFS、VIP Sale、Pre-order、VIP Paid
        spinnerOrder = findViewById(R.id.spinner01);

        try {
            //建立交易
            _RefundTransaction = new RefundTransaction(mContext, FlightData.SecSeq);
            _VipSaleRefundTransaction = new VipSaleRefundTransaction(mContext, FlightData.SecSeq);
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get sales info error", mContext, "Return");
            return;
        }

        //回傳的清單有商品
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < tmp_receiptNoList.rececipts.length; i++) {
            list.add(tmp_receiptNoList.rececipts[i].ReceiptNo);
        }
        // 回傳的排序為 SerialNo, PreorderNo (普通單為""), ReceiptNo
        list = Tools.resortListNo(list);
        list.add(0, "Choose receipt");
        orderList = new SpinnerHideItemAdapter(this, R.layout.spinner_item, list, 0);

        // 複製一份重新排列
        receiptNoList = new ReceiptList();
        receiptNoList.rececipts = Arrays.copyOf(tmp_receiptNoList.rececipts, tmp_receiptNoList.rececipts.length);
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < tmp_receiptNoList.rececipts.length; j++) {
                if (list.get(i).equals(tmp_receiptNoList.rececipts[j].ReceiptNo)) {
                    receiptNoList.rececipts[i - 1] = tmp_receiptNoList.rececipts[j];
                    break;
                }
            }
        }
        spinnerOrder.setAdapter(orderList);
        spinnerOrder.setVisibility(View.VISIBLE);

        //依照spinner選擇的單號判斷是否要顯示Order No.
        spinnerOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    loadOrderItems(position - 1);
                } else {
                    txtTotalMoney.setText("USD 0");
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    btnRefund.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        txtTotalMoney = findViewById(R.id.txtTotalMoney);

        btnRefund = findViewById(R.id.btnRefund);
        btnRefund.setEnabled(false);
        //依照spinner選擇的單號判斷單子種類
        btnRefund.setOnClickListener(view -> {

            final StringBuilder err = new StringBuilder();
            if (receiptNoList == null) {
                return;
            }

            if (spinnerOrder.getSelectedItemPosition() == 0) {
                RefundActivity.this.runOnUiThread(() -> MessageBox.show("", "Please choose receipt", mContext, "Return"));
                return;
            }

            Receipt receiptNo = receiptNoList.rececipts[spinnerOrder.getSelectedItemPosition() - 1];
            try {
                DBQuery.PaymentModePack payPack;
                Gson gson = new Gson();
                Bundle bundle = new Bundle();
                String jsonItem;
                Intent intent;

                switch (receiptNo.Type) {
                    case "DFS":
                        //將此張單購買細節放入Json String傳遞
                        payPack = DBQuery.getPayMode(new StringBuilder(), _RefundTransaction.GetOriginalPaymentMold());

                        if (payPack == null) {
                            MessageBox.show("", "Get pay info error.", mContext, "Return");
                            return;
                        }
                        jsonItem = gson.toJson(payPack);
                        bundle.putString("order", jsonItem);
                        bundle.putString("ReceiptNo", receiptNo.ReceiptNo);
                        bundle.putString("IFEOrderNo", _RefundTransaction.getIFEOrderNO());
                        intent = new Intent(mActivity, RefundDFSActivity.class);
                        intent.putExtras(bundle);
                        mActivity.startActivity(intent);
                        break;

                    case "VS":
                        payPack = DBQuery.getPayMode(new StringBuilder(), _VipSaleRefundTransaction.GetOriginalPaymentMold());
                        if (payPack == null) {
                            MessageBox.show("", "Get pay info error.", mContext, "Return");
                            return;
                        }
                        jsonItem = gson.toJson(payPack);
                        bundle.putString("order", jsonItem);
                        bundle.putString("ReceiptNo", receiptNo.ReceiptNo);
                        intent = new Intent(mActivity, RefundVSActivity.class);
                        intent.putExtras(bundle);
                        mActivity.startActivity(intent);
                        break;

                    // 直接退, 彈跳視窗確認
                    case "PR":
                        if (MessageBox.show("", "Refund?", RefundActivity.this, "Yes", "No")) {
                            mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
                            //更新訂單狀態
                            try {
                                JSONObject request = new JSONObject();
                                request.put("PreorderNo", preorderPack.info[0].PreorderNO);
                                request.put("PreorderType", "PR");
                                request.put("HistoryRefund", null);
                                request.put("PaymentList", null);
                                if (!DBQuery.refundPreorderInfo(mContext, err, request)) {
                                    RefundActivity.this.runOnUiThread(() -> {
                                        mloadingDialog.dismiss();
                                        MessageBox.show("", "Save pre-order error, please retry", mContext, "Return");
                                    });
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                RefundActivity.this.runOnUiThread(() -> {
                                    mloadingDialog.dismiss();
                                    MessageBox.show("", "Save error", mContext, "Return");
                                });
                                return;
                            }
                            printData(true);
                        }
                        break;

                    case "VP":
                        if (MessageBox.show("", "Refund?", RefundActivity.this, "Yes", "No")) {
                            mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
                            try {
                                //更新訂單狀態
                                JSONObject request = new JSONObject();
                                request.put("PreorderNo", preorderPack.info[0].PreorderNO);
                                request.put("PreorderType", "VP");
                                request.put("HistoryRefund", null);
                                request.put("PaymentList", null);
                                if (!DBQuery.refundPreorderInfo(mContext, err, request)) {
                                    RefundActivity.this.runOnUiThread(() -> MessageBox.show("", "Save pre-order error, please retry", mContext, "Return"));
                                    return;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                RefundActivity.this.runOnUiThread(() -> {
                                    mloadingDialog.dismiss();
                                    MessageBox.show("", "Save error", mContext, "Return");
                                });
                                return;
                            }
                            printData(false);
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                RefundActivity.this.runOnUiThread(() -> {
                    mloadingDialog.dismiss();
                    MessageBox.show("", "Refund error", mContext, "Return");
                });
                return;
            }
        });
        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }

    private static class PrinterHandler extends Handler {

        private WeakReference<RefundActivity> weakActivity;

        PrinterHandler(RefundActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            RefundActivity handlerActivity = weakActivity.get();
            Context handlerContext = handlerActivity.mContext;

            handlerActivity.mloadingDialog.dismiss();
            switch (msg.what) {
                case 1: // 沒紙 (PR)
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(true);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 2: // 沒紙 (VP)
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(false);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 3: //Print error (PR)
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(true);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 4: //Print error (VP)
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(false);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 5: //成功
                    handlerActivity.doPrintFinal();
                    break;
            }
        }
    }

    private void doPrintFinal() {
        mloadingDialog.dismiss();
        if (MessageBox.show("", "Success", mContext, "Ok")) {
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
        }
    }

    private void printData(final boolean isPreorder) {
        mloadingDialog.dismiss();
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        new Thread() {
            public void run() {
                PrintAir printer = new PrintAir(mContext,
                    Integer.valueOf(FlightData.SecSeq));

                if (isPreorder) {
                    // 列印Preorder
                    try {
                        if (printer.printPreOrderRefund(preorderPack.info[0].PreorderNO) == -1) {
                            printerHandler.sendMessage(Tools.createMsg(1));
                        } else {
                            printerHandler.sendMessage(Tools.createMsg(5));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                        _TSQL.WriteLog(FlightData.SecSeq,
                            "System", "RefundActivity", "printPreOrderRefund", e.getMessage());
                        printerHandler.sendMessage(Tools.createMsg(3));
                    }
                } else {
                    try {
                        if (printer.printVIPPaidRefund(preorderPack.info[0].PreorderNO) == -1) {
                            printerHandler.sendMessage(Tools.createMsg(2));
                        } else {
                            printerHandler.sendMessage(Tools.createMsg(5));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                        _TSQL.WriteLog(FlightData.SecSeq, "System", "RefundActivity", "printVIPPaidRefund", e.getMessage());
                        printerHandler.sendMessage(Tools.createMsg(4));
                    }
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

    private void loadOrderItems(final int position) {

        final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        new Thread() {
            public void run() {
                StringBuilder err = new StringBuilder();
                try {
                    final Receipt receiptNo = receiptNoList.rececipts[position];
                    final RefundReceiptDetial detial;

                    // DFS, 不顯示單品價格
                    switch (receiptNo.Type) {
                        case "DFS":
                            // 依據單據取得購買物品
                            detial = DBQuery.getReceiptDetailItem(err, _RefundTransaction.GetBasketInfo(receiptNo.ReceiptNo));
                            if (detial == null) {
                                RefundActivity.this.runOnUiThread(() -> {
                                    mloadingDialog.dismiss();
                                    MessageBox.show("", "Get sales info error", mContext, "Return");
                                });
                                return;
                            }

                            // 將細項加入adapter
                            adapter.setIsPreorder(false);
                            adapter.clear();
                            adapter.setIsRightTwoVisible(false);

                            for (TransactionItem item : detial.items) {
                                // String ItemCode, String monyType, Double price, String itemName, int stock, Obj qty
                                adapter.addItem(
                                    item.ItemCode, item.SerialNo, "US", item.OriginalPrice, item.ItemName,
                                    item.SalesQty, item.SalesQty
                                );
                            }
                            RefundActivity.this.runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();

                                txtOrderListKind.setVisibility(View.GONE);
                                txtOrderListKind.setText("");
                                txtTag2.setVisibility(View.GONE);
                                txtTag3.setText("Qty");
                                txtTotalMoney.setText(setTotalMoneyTextView("USD", true));
                                mloadingDialog.dismiss();
                            });
                            break;

                        // VIP Sale
                        case "VS": {
                            // 依據單據取得購買物品
                            detial = DBQuery.getReceiptDetailItem(err, _VipSaleRefundTransaction.GetBasketInfo(receiptNo.ReceiptNo));
                            if (detial == null) {
                                RefundActivity.this.runOnUiThread(() -> {
                                    mloadingDialog.dismiss();
                                    MessageBox.show("", "Get sales info error", mContext, "Return");
                                });
                                return;
                            }

                            adapter.setIsPreorder(true);
                            // load picture key
                            ArrayList<String> itemCodeList = new ArrayList<>();
                            for (int i = 0; i < detial.items.length; i++) {
                                itemCodeList.add(detial.items[i].ItemCode);
                            }
                            adapter.setImageKeyCodeList(itemCodeList);

                            // 將細項加入adapter
                            adapter.clear();
                            adapter.setIsRightTwoVisible(true);

                            for (TransactionItem item : detial.items) {
                                adapter.addItem(
                                    item.ItemCode,
                                    item.SerialNo, "US", item.OriginalPrice, item.ItemName,
                                    item.SalesQty, item.OriginalPrice * item.SalesQty
                                );
                            }

                            RefundActivity.this.runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();

                                txtOrderListKind.setVisibility(View.VISIBLE);
                                txtOrderListKind.setText("VIP No: " + detial.PreorderNo); // VIP No

                                txtTag2.setVisibility(View.VISIBLE);
                                txtTag3.setText("Price");
                                txtTotalMoney.setText(setTotalMoneyTextView("USD", false));
                                mloadingDialog.dismiss();
                            });
                            break;
                        }

                        // 預訂單
                        default: {
                            // 用Preorder頁面的function查詢
                            preorderPack = DBQuery.getPRVPCanSaleRefund(mContext, err, FlightData.SecSeq, receiptNo.PreorderNo, null, "S");
                            if (preorderPack == null || preorderPack.info == null) {
                                RefundActivity.this.runOnUiThread(() -> {
                                    mloadingDialog.dismiss();
                                    MessageBox.show("", "Query pre-order data error", mContext, "Return");
                                    ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                                    RefundActivity.this.finish();
                                });
                                return;
                            }

                            final PreorderInformation orderDetail = preorderPack.info[0];
                            // load picture key
                            adapter.setIsPreorder(true);
                            ArrayList<String> itemCodeList = new ArrayList<>();
                            for (int i = 0; i < orderDetail.items.length; i++) {
                                itemCodeList.add(orderDetail.items[i].ItemCode);
                            }
                            adapter.setImageKeyCodeList(itemCodeList);
                            adapter.clear();
                            adapter.setIsRightTwoVisible(true);

                            for (int i = 0; i < orderDetail.items.length; i++) {
                                // String ItemCode, String moneyType, Double price, String itemName, int stock, Obj qty
                                adapter.addItem(
                                    orderDetail.items[i].ItemCode,
                                    orderDetail.items[i].SerialCode,
                                    "US", orderDetail.items[i].OriginalPrice,
                                    orderDetail.items[i].ItemName,
                                    orderDetail.items[i].SalesQty,
                                    orderDetail.items[i].SalesPrice * orderDetail.items[i].SalesQty
                                );
                            }

                            RefundActivity.this.runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();

                                txtOrderListKind.setVisibility(View.VISIBLE);
                                if (receiptNo.Type.equals("PR")) {
                                    txtOrderListKind.setText("Pre-order No: " + orderDetail.PreorderNO); //Preorder No
                                } else {
                                    txtOrderListKind.setText("VIP No: " + orderDetail.PreorderNO); // VIP No
                                }

                                txtTag2.setVisibility(View.VISIBLE);
                                txtTag3.setText("Price");
                                txtTotalMoney.setText(orderDetail.CurDvr + " " + Tools.getModiMoneyString(preorderPack.info[0].Amount));

                                mloadingDialog.dismiss();
                            });
                            break;
                        }
                    }

                    RefundActivity.this.runOnUiThread(() -> {
                        if (receiptNo.Type.equals("DFS") || receiptNo.Type.equals("VS")) {
                            btnRefund.setText("Next");
                        } else {
                            btnRefund.setText("Refund");
                        }
                        btnRefund.setEnabled(true);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    RefundActivity.this.runOnUiThread(() -> {
                        mloadingDialog.dismiss();
                        MessageBox.show("", "Load order items error", mContext, "Return");
                    });
                }
            }
        }.start();
    }

    // 取得應付款總額
    private String setTotalMoneyTextView(String currency, boolean isDFS) {
        try {
            if (receiptNoList != null && receiptNoList.rececipts != null) {
                // 先呼叫一次，取得總應付金額，這樣 GetCurrencyMaxAmount 才取得到值

                DBQuery.ShouldPayMoney payItem;
                if (isDFS) {
                    _RefundTransaction.GetPaymentMode();
                    payItem = DBQuery.getPayMoneyNow(new StringBuilder(), _RefundTransaction.GetRefundMaxAmount(currency));
                } else {
                    _VipSaleRefundTransaction.GetPaymentMode();
                    payItem = DBQuery.getPayMoneyNow(new StringBuilder(), _VipSaleRefundTransaction.GetRefundMaxAmount(currency));
                }
                if (payItem == null || payItem.Currency == null) {
                    MessageBox.show("", "Get pay info error", mActivity, "Return");
                    return "";
                }
                return (currency + " " + String.valueOf(payItem.MaxPayAmount));

            } else {
                return "USD 0";
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Set currency error", mContext, "Return");
            return "";
        }
    }


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
                result = false;
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                result = false;
                break;
            default:
                result = super.onKeyDown(keyCode, event);
                break;
        }

        return result;
    }


}