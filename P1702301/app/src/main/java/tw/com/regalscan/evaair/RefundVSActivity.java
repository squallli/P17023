package tw.com.regalscan.evaair;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.graphics.Color;
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
import tw.com.regalscan.R;
import tw.com.regalscan.adapters.ItemListPayAdapter;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.component.SwipeListView;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.PaymentList;
import tw.com.regalscan.evaair.ife.IFEDBFunction;
import tw.com.regalscan.evaair.ife.NCCCAuthorize;
import tw.com.regalscan.evaair.ife.model.entity.AuthorizeModel;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;


public class RefundVSActivity extends AppCompatActivity {


    //change歷程
    //History
    private ListView historyListView;
    private ItemListPayAdapter adapterHistory;
    //Change
    private SwipeListView changeListView;
    public ItemListPayAdapter adapter;

    private Spinner spinnerMoney;
    private EditText editMoney;
    private Button btnAdd;
    private RelativeLayout rowAdd;

    private Button btnReturn, btnRefund;
    private static Button btnHistory, btnChange;
    public Context mContext;
    public Activity mActivity;
    private TextView txtToolbarTitle, txtTotal;

    //所有可使用的幣別
    private DBQuery.AllCurrencyListPack allCurrencyPack;
    //當前訂單明細
    private DBQuery.PaymentModePack payPackOld;
    // 新的訂單明細
    private DBQuery.PaymentModePack payPackNew;
    private int USDIndex = 0;

    // 畫面顯示的是哪個listview
    private boolean isHistoryShow = true;
    private String ReceiptNo;
    //鍵盤
    private InputMethodManager imm;
    private ProgressDialog mloadingDialog;

    private IFEDBFunction mIFEDBFunction;
    private NCCCAuthorize mNCCCAuthorize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refund_02);

        Bundle argument = getIntent().getExtras();
        if (argument != null) {
            ReceiptNo = argument.getString("ReceiptNo");

            String jsonOrder = argument.getString("order");
            Gson gson = new Gson();
            payPackOld = gson.fromJson(jsonOrder, DBQuery.PaymentModePack.class);
            if (payPackOld == null) {
                MessageBox.show("", "Get pay info error", mContext, "Return");
                return;
            }
            init();
        } else {
            MessageBox.show("", "Get pay info error", mContext, "Return");
            finish();
        }
    }

    private void init() {
        mContext = this;
        mActivity = this;
        enableExpandableList();
        imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);

        mIFEDBFunction = new IFEDBFunction(mContext, FlightData.SecSeq);
        mNCCCAuthorize = new NCCCAuthorize(mContext);

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            try {
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Clear pay info error", mContext, "Return");
            }
            finish();
        });

        txtToolbarTitle = findViewById(R.id.toolbar_title);
        txtToolbarTitle.setText("VIP Sale Refund");

        //change找零幣別金額的layout
        rowAdd = findViewById(R.id.row02);

        btnHistory = findViewById(R.id.btnHistory);
        btnChange = findViewById(R.id.btnChange);
        btnChange.setEnabled(false);
        btnRefund = findViewById(R.id.btnRefund);

        try {
            // 取得新的退款交易物件
            payPackNew = DBQuery.getPayMode(new StringBuilder(), RefundActivity._VipSaleRefundTransaction.GetPaymentMode());
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get payment info error", mContext, "Return");
            return;
        }

        //取得可以使用的幣別, 20
        StringBuilder err = new StringBuilder();
        allCurrencyPack = DBQuery.getAllCurrencyList(mContext, err);
        if (allCurrencyPack == null) {
            RefundVSActivity.this.runOnUiThread(() -> MessageBox.show("", "Get currency error", mContext, "Return"));
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
            return;
        }

        //下拉選單幣別
        spinnerMoney = findViewById(R.id.spinner01);
        ArrayAdapter<String> listCurrency = new ArrayAdapter<String>(this, R.layout.spinner_item);
        for (int i = 0; i < allCurrencyPack.currencyList.length; i++) {
            listCurrency.add(allCurrencyPack.currencyList[i].CurDvr);
            if (allCurrencyPack.currencyList[i].CurDvr.equals("USD")) {
                USDIndex = i;
            }
        }
        spinnerMoney.setAdapter(listCurrency);
        spinnerMoney.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    DBQuery.ShouldPayMoney payItem = DBQuery.getPayMoneyNow(new StringBuilder(),
                        RefundActivity._VipSaleRefundTransaction.GetCurrencyMaxAmount(spinnerMoney.getSelectedItem().toString()));
                    if (payItem == null || payItem.Currency == null) {
                        MessageBox.show("", "Get pay info error.", mActivity, "Return");
                        return;
                    }
                    editMoney.setText(String.valueOf(payItem.MaxPayAmount));
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageBox.show("", "Get pay info error.", mActivity, "Return");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerMoney.setSelection(USDIndex);

        //找零金額
        editMoney = findViewById(R.id.editMoney);
        editMoney.setText(String.valueOf(payPackNew.USDTotalUnpay));

        // History
        historyListView = findViewById(R.id.lvItemList02);
        adapterHistory = new ItemListPayAdapter(mContext);

        //載入付款歷程
        for (PaymentList pay : payPackOld.payLisy) {
            adapterHistory.addItem(pay.Currency, pay.PayBy, pay.Amount, pay.USDAmount, pay.CouponNo);
        }
        for (PaymentList pay : payPackOld.payLisy) {
            // 如果有非信用卡付款，才可進入Change頁面
            if (!pay.PayBy.equals("Card")) {
                btnChange.setEnabled(true);
                break;
            }
        }
        historyListView.setVisibility(View.VISIBLE);
        historyListView.setAdapter(adapterHistory);

        //Change
        changeListView = findViewById(R.id.lvItemList);
        adapter = new ItemListPayAdapter(mContext, changeListView.getRightViewWidth());
        adapter.setIsSwipeDelete(true);
        adapter.setIsRefundPage(false);

        try {
            //如果有信用卡和苦碰付款資料，預先加入退款歷程，且無法刪除
            for (PaymentList pay : payPackNew.payLisy) {
                adapter.addItem(pay.Currency, pay.PayBy, pay.Amount, pay.USDAmount, pay.CouponNo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get pay info error", mContext, "Return");
            return;
        }

        //滑動刪除Button onClick
        adapter.setItemSwipeListener(position -> {
            try {
                if (!adapter.getItem(position).getPayType().equals("Card")
                    && !adapter.getItem(position).getPayType().equals("SC")
                    && !adapter.getItem(position).getPayType().equals("DC")) {
                    if (MessageBox.show("", "Delete this change?", RefundVSActivity.this, "Yes", "No")) {
                        modifyRefundList(1, payPackNew.payLisy[position].Currency, payPackNew.payLisy[position].Amount);
                    }
                } else {
                    MessageBox.show("", "Can't delete this pay", RefundVSActivity.this, "Return");
                }
                changeListView.hiddenRight(changeListView.mPreItemView);

            } catch (Exception e) {
                e.printStackTrace();
//                    MessageBox.show("", "Delete pay error", mContext, "Return");
            }
        });
        changeListView.setAdapter(adapter);
        changeListView.setVisibility(View.GONE);

        //總額
        txtTotal = findViewById(R.id.txtTotal);
        txtTotal.setText("USD " + (int)Math.round(payPackOld.USDTotalAmount));

        //加入找零資訊
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(view -> {
            imm.hideSoftInputFromWindow(editMoney.getWindowToken(), 0);
            if (editMoney.getText().toString().trim().equals("")) {
                MessageBox.show("", "Please input money", RefundVSActivity.this, "Return");
                return;
            }
            modifyRefundList(0, spinnerMoney.getSelectedItem().toString(), Double.parseDouble(editMoney.getText().toString()));                //如果畫面上還有顯示的刪除按鈕, 把它復原
            changeListView.hiddenRight(changeListView.mPreItemView);
        });

        //btn
        btnHistory.setOnClickListener(view -> {
            btnRefund.setEnabled(true);

            //隱藏加入找零的layout row
            rowAdd.setVisibility(View.GONE);

            historyListView.setVisibility(View.VISIBLE);
            changeListView.setVisibility(View.GONE);
            isHistoryShow = true;
            updateButtonColor(0);
        });
        btnHistory.performClick();

        btnChange.setOnClickListener(view -> {
            //顯示加入找零的layout row
            rowAdd.setVisibility(View.VISIBLE);

            historyListView.setVisibility(View.GONE);
            changeListView.setVisibility(View.VISIBLE);
            isHistoryShow = false;
            if (payPackNew.NowPayMode.equals("PAY")) {
                btnRefund.setEnabled(false);
            } else {
                btnRefund.setEnabled(true);
            }
            updateButtonColor(1);
        });

        btnRefund.setOnClickListener(view -> {
            if (MessageBox.show("", "Refund?", RefundVSActivity.this, "Yes", "No")) {
                mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
                try {
                    // 以畫面停留的頁面辨識要用哪個退款歷程退錢
                    if (isHistoryShow) { // 用原始退款
                        if (!RefundActivity._VipSaleRefundTransaction.SaveRefundInfoByOriginal().getString("ReturnCode").equals("0")) {
                            RefundVSActivity.this.runOnUiThread(() -> {
                                mloadingDialog.dismiss();
                                MessageBox.show("", "Save refund info error.", mContext, "Return");
                            });
                            return;
                        }
                    } else { // 用新的退款
                        if (!RefundActivity._VipSaleRefundTransaction.SaveRefundInfoByNew().getString("ReturnCode").equals("0")) {
                            RefundVSActivity.this.runOnUiThread(() -> {
                                mloadingDialog.dismiss();
                                MessageBox.show("", "Save refund info error.", mContext, "Return");
                            });
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    RefundVSActivity.this.runOnUiThread(() -> {
                        mloadingDialog.dismiss();
                        MessageBox.show("", "Save refund info error", mContext, "Return");
                    });
                    return;
                }

                boolean isCredit = false;
                for (PaymentList pay : payPackOld.payLisy) {
                    if (pay.PayBy.equals("Card")) {
                        isCredit = true;
                        break;
                    }
                }
                printData(true);
            }
        });
        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }


    private static class PrinterHandler extends Handler {

        private WeakReference<RefundVSActivity> weakActivity;

        PrinterHandler(RefundVSActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            RefundVSActivity handlerActivity = weakActivity.get();
            Context handlerContext = handlerActivity.mContext;

            handlerActivity.mloadingDialog.dismiss();
            switch (msg.what) {
                case 1: // 沒紙(信用卡)
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(true);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 2: // 沒紙(現金)
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(false);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 3: //Print error (信用卡)
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(true);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 4: //Print error (現金)
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(true);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 5: // 印簽單
                    if (MessageBox.show("", "Print receipt", handlerContext, "Ok")) {
                        handlerActivity.printData(true);
                    }
                case 51: // 印收據
                    if (MessageBox.show("", "Print receipt", handlerContext, "Ok")) {
                        handlerActivity.printData(false);
                    }
                    break;

                case 6: //成功
                    handlerActivity.doPrintFinal();
                    break;
            }
        }
    }

//    private Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            mloadingDialog.dismiss();
//            switch(msg.what){
//                case 1: // 沒紙(信用卡)
//                    if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData(true);
//                    else doPrintFinal();
//                    break;
//
//                case 2: // 沒紙(現金)
//                    if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData(false);
//                    else doPrintFinal();
//                    break;
//
//                case 3: //Print error (信用卡)
//                    if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData(true);
//                    else doPrintFinal();
//                    break;
//
//                case 4: //Print error (現金)
//                    if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData(false);
//                    else doPrintFinal();
//                    break;
//
//                case 5: // 印收據
//                    if(MessageBox.show("", "Print receipt", mContext, "Ok")) printData(false);
//                    break;
//
//                case 6: //成功
//                    doPrintFinal();
//                    break;
//            }
//        }
//    };

    private void doPrintFinal() {

        AuthorizeModel authorizeModel = mIFEDBFunction.getAuthorizeInfo(ReceiptNo, "DutyFreeRefund");

        if (FlightData.OnlineAuthorize && authorizeModel != null) {
            authorizeModel.setREAUTH_MARK("Y");
            authorizeModel.setUG_MARK("N");

            mNCCCAuthorize.SendRequestToNCCCGetAuthorize(authorizeModel, new NCCCAuthorize.AuthorizeReturn() {
                @Override
                public void success(AuthorizeModel authorizeModel) {
                    mIFEDBFunction.deAuthorizeSave(ReceiptNo, "DutyFreeRefund");
                    if (MessageBox.show("", "Success", mContext, "Ok")) {
                        if (ActivityManager.isActivityInStock("RefundActivity")) {
                            ActivityManager.removeActivity("RefundActivity");
                        }
                        ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                    }
                }

                @Override
                public void failed(String errMsg) {
                    if (MessageBox.show("", "Success", mContext, "Ok")) {
                        if (ActivityManager.isActivityInStock("RefundActivity")) {
                            ActivityManager.removeActivity("RefundActivity");
                        }
                        ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                    }
                }
            });
        } else {
            if (MessageBox.show("", "Success", mContext, "Ok")) {
                if (ActivityManager.isActivityInStock("RefundActivity")) {
                    ActivityManager.removeActivity("RefundActivity");
                }
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            }
        }
    }

    private void printData(final boolean isCredit) {
        mloadingDialog.dismiss();
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        new Thread() {
            public void run() {
                PrintAir printer = new PrintAir(mContext, Integer.valueOf(FlightData.SecSeq));
                try {
                    if (isCredit) {
                        // String ReceiptNo, int flag, String PreOrderNo, String VipOrderNo
                        if (printer.printVIPRefund(ReceiptNo, isCredit) == -1) {
                            printerHandler.sendMessage(Tools.createMsg(1));
                        } else {
                            printerHandler.sendMessage(Tools.createMsg(51));
                        }
                    } else {
                        if (printer.printVIPRefund(ReceiptNo, isCredit) == -1) {
                            printerHandler.sendMessage(Tools.createMsg(2));
                        } else {
                            printerHandler.sendMessage(Tools.createMsg(6));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                    _TSQL.WriteLog(FlightData.SecSeq,
                        "System", "RefundVSActivity", "printVIPRefund", e.getMessage());
                    if (isCredit) {
                        printerHandler.sendMessage(Tools.createMsg(3));
                    } else {
                        printerHandler.sendMessage(Tools.createMsg(4));
                    }
                }

            }
        }.start();
    }


    // =0 History, 1= Change
    private static void updateButtonColor(int whitchIsClick) {
        switch (whitchIsClick) {
            case 0:
                btnHistory.setBackgroundColor(Color.parseColor("#1CB074"));
                btnHistory.setTextColor(Color.WHITE);
                if (!btnChange.isEnabled()) {
                    btnChange.setBackgroundColor(Color.WHITE);
                    btnChange.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnChange.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnChange.setTextColor(Color.parseColor("#4E4A4B"));
                }
                break;

            case 1:
                btnHistory.setBackgroundColor(Color.parseColor("#C9CACA"));
                btnHistory.setTextColor(Color.parseColor("#4E4A4B"));
                if (!btnChange.isEnabled()) {
                    btnChange.setBackgroundColor(Color.WHITE);
                    btnChange.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnChange.setBackgroundColor(Color.parseColor("#1CB074"));
                    btnChange.setTextColor(Color.WHITE);
                }
                break;
        }
    }

    // Action : 0= 新增, 1=刪除
    private void modifyRefundList(int Action, String Currency, Double Money) {

        StringBuilder err = new StringBuilder();
        try {
            switch (Action) {
                // 新增
                case 0:
                    payPackNew = DBQuery.getPayMode(err, RefundActivity._VipSaleRefundTransaction.AddRefundList(Currency, Money));
                    break;

                // 刪除
                case 1:
                    payPackNew = DBQuery.getPayMode(err, RefundActivity._VipSaleRefundTransaction.DeleteRefundList(Currency));
                    break;
            }
            if (payPackNew == null) {
                MessageBox.show("", err.toString(), mContext, "Return");
                return;
            }

            // 設定未付款金額, 按鈕
            switch (payPackNew.NowPayMode) {
                case "PAY":
                    RefundActivity._VipSaleRefundTransaction.GetCurrencyMaxAmount("TWD");

                    if (Action == 1) {
                        // 不會有LastPayCurrency, 預設上次刪除的幣別
                        int CurrencyIndex = -1;
                        int USDindex = 0;
                        for (int i = 0; i < allCurrencyPack.currencyList.length; i++) {
                            if (allCurrencyPack.currencyList[i].CurDvr.equals(Currency)) {
                                CurrencyIndex = i;
                            }
                            if (allCurrencyPack.currencyList[i].CurDvr.equals("USD")) {
                                USDindex = i;
                            }
                        }
                        // 找不到幣別則預設美金
                        if (CurrencyIndex == -1) {
                            spinnerMoney.setSelection(USDindex);
                        } else {
                            spinnerMoney.setSelection(CurrencyIndex);
                        }
                    } else {
                        // 依據LastPayCurrency切換幣別
                        for (int i = 0; i < spinnerMoney.getCount(); i++) {
                            if (spinnerMoney.getItemAtPosition(i).toString().equals(payPackNew.LastPayCurrency)) {
                                spinnerMoney.setSelection(i);
                            }
                        }
                    }
                    DBQuery.ShouldPayMoney payItem = DBQuery.getPayMoneyNow(new StringBuilder(),
                        RefundActivity._VipSaleRefundTransaction.GetCurrencyMaxAmount(spinnerMoney.getSelectedItem().toString()));
                    if (payItem == null || payItem.Currency == null) {
                        MessageBox.show("", "Get pay info error.", mActivity, "Return");
                        return;
                    }
                    editMoney.setText(String.valueOf(payItem.MaxPayAmount));

                    // 畫面在新退款頁面就檢查要不要enable btn
                    if (!isHistoryShow) {
                        btnRefund.setEnabled(false);
                    } else {
                        btnRefund.setEnabled(true);
                    }
                    break;

                case "CHANGE":
                    break;

                case "BALANCE":
                    editMoney.setText("");
                    btnRefund.setEnabled(true);
                    break;
            }
            // 塞入adapter
            adapter.clear();
            for (PaymentList pay : payPackNew.payLisy) {
                adapter.addItem(pay.Currency, pay.PayBy, pay.Amount, pay.USDAmount, pay.CouponNo);
            }
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get pay info error", mContext, "Return");
        }
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