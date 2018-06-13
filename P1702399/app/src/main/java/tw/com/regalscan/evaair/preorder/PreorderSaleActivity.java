package tw.com.regalscan.evaair.preorder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

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
import com.jess.arms.utils.ArmsUtils;
import com.regalscan.sqlitelibrary.TSQL;
import org.json.JSONObject;
import tw.com.regalscan.R;
import tw.com.regalscan.activities.ItemPictureActivity;
import tw.com.regalscan.activities.PreorderCheckActivity;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.component.SpinnerHideItemAdapter;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.PreorderInfoPack;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;


public class PreorderSaleActivity extends AppCompatActivity {


    public ItemListPictureModifyAdapter adapter;
    private ListView itemListView;
    private Spinner spinnerMoney, spinnerOrderNum;
    private TextView txtReceipt, txtToolbarTitle, txtTotal;
    private Button btnReturn, btnNext;
    public Context mContext;
    public Activity mActivity;
    private int CERIFIED_INFO = 500;

    //單據
    private SpinnerHideItemAdapter orderList;

    // preorder list pack
    private PreorderInfoPack preorderPack;

    private ProgressDialog mloadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preorder_sale_and_vip_paid_sale);

        // 整個 pack
        Bundle argument = getIntent().getExtras();
        if (argument != null) {
            String itemString = argument.getString("jsonPack");
            Gson gson = new Gson();
            preorderPack = gson.fromJson(itemString, PreorderInfoPack.class);
            init();
        } else {
            if (MessageBox.show("", "No pre-order list", this, "Ok")) {
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

        txtToolbarTitle = findViewById(R.id.toolbar_title);
        txtToolbarTitle.setText("Pre-order");
        txtReceipt = findViewById(R.id.txtReceipt);
        txtReceipt.setText("Pre-order No: ");

        spinnerOrderNum = findViewById(R.id.spinner01);

        // 將可使用的被動式折扣放入spinner
        ArrayList<String> list = new ArrayList<>();
        list.add("Choose no.");
        for (int i = 0; i < preorderPack.info.length; i++) {
            list.add(preorderPack.info[i].PreorderNO);
        }
        orderList = new SpinnerHideItemAdapter(this, R.layout.spinner_item, list, 0);
        spinnerOrderNum.setAdapter(orderList);

        spinnerOrderNum.setVisibility(View.VISIBLE);
        spinnerOrderNum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    //載入商品清單, 第一個位置不是單據
                    searchPreorder(position - 1);
                } else {
                    txtTotal.setText("USD 0");
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    btnNext.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 幣別 (不顯示)
        spinnerMoney = findViewById(R.id.spinner02);
        spinnerMoney.setVisibility(View.INVISIBLE);

        //items
        itemListView = findViewById(R.id.lvItemList);
        adapter = new ItemListPictureModifyAdapter(mContext, itemListView, true);
        adapter.setIsModifiedItem(false);
        adapter.setIsPictureZoomIn(true);
        adapter.setIsRightTwoVisible(true);
        adapter.setIsMoneyVisible(true);
        adapter.setQtyPutPrice(true);
        adapter.setFilpperFunctionClickListener(itemPictureClickListener);
        itemListView.setAdapter(adapter);

        // Total
        txtTotal = findViewById(R.id.txtMoney);

        btnNext = findViewById(R.id.btnNext);
        btnNext.setEnabled(false);
        btnNext.setOnClickListener(view -> {

            if (preorderPack == null) {
                PreorderSaleActivity.this.runOnUiThread(() -> MessageBox.show("", "Pre-order data error", mContext, "Return"));
                return;
            }

            if (spinnerOrderNum.getSelectedItemPosition() == 0) {
                PreorderSaleActivity.this.runOnUiThread(() -> MessageBox.show("", "Please choose", mContext, "Return"));
                return;
            }

            //傳遞整包preorderPack
            Bundle bundle = new Bundle();
            Gson gson = new Gson();
            String jsonPrerorderPack = gson.toJson(preorderPack.info[spinnerOrderNum.getSelectedItemPosition() - 1]);
            bundle.putString("ListDetail", jsonPrerorderPack);

            Intent intent = new Intent(mActivity, PreorderCheckActivity.class);
            intent.putExtras(bundle);
            mActivity.startActivityForResult(intent, CERIFIED_INFO);
        });
        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }

    private void enableExpandableList() {
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ExpandableListView expandableListView = findViewById(R.id.expendlist);
        NavigationDrawer navigationDrawer = new NavigationDrawer(mActivity, mContext, drawerLayout, toolbar, expandableListView);
    }

    private void searchPreorder(final int packPosition) {

        final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        new Thread() {
            public void run() {
                try {
                    DBQuery.PreorderInformation orderDetail = preorderPack.info[packPosition];

                    // load key
                    ArrayList<String> itemCodeList = new ArrayList<>();
                    for (int i = 0; i < orderDetail.items.length; i++) {
                        itemCodeList.add(orderDetail.items[i].ItemCode);
                    }
                    adapter.setImageKeyCodeList(itemCodeList);
                    adapter.clear();
                    for (int i = 0; i < orderDetail.items.length; i++) {
                        // String itemCode, String monyType, Double price, String itemName, int stock, Double qty
                        adapter.addItem(
                            orderDetail.items[i].ItemCode,
                            orderDetail.items[i].SerialCode,
                            "US", orderDetail.items[i].OriginalPrice,
                            orderDetail.items[i].ItemName,
                            orderDetail.items[i].SalesQty,
                            orderDetail.items[i].SalesPrice * orderDetail.items[i].SalesQty
                        );
                    }
                    final String ss = orderDetail.CurDvr + " " + Tools.getModiMoneyString(preorderPack.info[packPosition].Amount);
                    PreorderSaleActivity.this.runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        txtTotal.setText(ss);
                        btnNext.setEnabled(true);
                        mloadingDialog.dismiss();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    PreorderSaleActivity.this.runOnUiThread(() -> {
                        mloadingDialog.dismiss();
                        MessageBox.show("", "Search item info error", mContext, "Return");
                    });
                }
            }
        }.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        //通過驗證
        if ((requestCode == CERIFIED_INFO) && resultCode == RESULT_OK) {
            mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
            try {
                // 更新Preorder資訊
                final StringBuilder err = new StringBuilder();
                JSONObject request = new JSONObject();
                request.put("PreorderNo", preorderPack.info[spinnerOrderNum.getSelectedItemPosition() - 1].PreorderNO);
                request.put("PreorderType", "PR");
                request.put("USDAmount", "");
                request.put("UpperlimitType", "");
                request.put("UpperlimitDiscountNo", "");
                request.put("VerifyType", data.getStringExtra("VerifyType"));
                request.put("PaymentList", null);

                if (!DBQuery.savePreorderInfo(mContext, err, request)) {
                    PreorderSaleActivity.this.runOnUiThread(() -> {
                        mloadingDialog.dismiss();
                        MessageBox.show("", "Save pre-order error, please retry", mContext, "Return");
                    });
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                mloadingDialog.dismiss();
                MessageBox.show("", "Save pre-order error", mContext, "Return");
                return;
            }
            printData();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private static class PrinterHandler extends Handler {
        private WeakReference<PreorderSaleActivity> weakActivity;

        PrinterHandler(PreorderSaleActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            PreorderSaleActivity handlerActivity = weakActivity.get();
            Context handlerContext = handlerActivity.mContext;

            handlerActivity.mloadingDialog.dismiss();
            switch (msg.what) {
                case 1: // 沒紙
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No"))
                        handlerActivity.printData();
                    else handlerActivity.doPrintFinal();
                    break;

                case 2: //Print error
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No"))
                        handlerActivity.printData();
                    else handlerActivity.doPrintFinal();
                    break;

                case 3: //成功
                    handlerActivity.doPrintFinal();
                    break;
            }
        }
    }

    private void doPrintFinal() {
        StringBuilder err = new StringBuilder();
        if (MessageBox.show("", "Success", mContext, "Ok")) {
            // 重新取得所有單據號碼
            preorderPack = DBQuery.getPRVPCanSaleRefund(mContext, err, FlightData.SecSeq, null, new String[]{"PR"}, "N");
            if (preorderPack == null) {
                MessageBox.show("", "Query pre-order data error", mContext, "Return");
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
                return;
            }
            //回傳的清單有Pre-oreder商品
            if (preorderPack.info != null) {
                orderList.clear();
                orderList.add("Choose no.");
                for (int i = 0; i < preorderPack.info.length; i++) {
                    orderList.add(preorderPack.info[i].PreorderNO);
                }
                orderList.notifyDataSetChanged();
                spinnerOrderNum.setSelection(0);
                adapter.clear();
                adapter.notifyDataSetChanged();
            } else {
                if (MessageBox.show("", "No pre-order sale list", mContext, "Ok")) {
                    ArmsUtils.startActivity(PreorderSaleActivity.class);
                    ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                    finish();
                }
            }
        }
    }

    private void printData() {
        mloadingDialog.dismiss();
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        new Thread() {
            public void run() {
                // 列印
                PrintAir printer = new PrintAir(mContext,
                    Integer.valueOf(FlightData.SecSeq));
                try {
                    if (printer.printPreOrder(preorderPack.info[spinnerOrderNum.getSelectedItemPosition() - 1].PreorderNO) == -1) {
                        printerHandler.sendMessage(Tools.createMsg(1));
                    } else {
                        printerHandler.sendMessage(Tools.createMsg(3));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                    _TSQL.WriteLog(FlightData.SecSeq,
                        "System", "PreorderSaleActivity", "printPreOrder", e.getMessage());
                    printerHandler.sendMessage(Tools.createMsg(2));
                }
            }
        }.start();
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