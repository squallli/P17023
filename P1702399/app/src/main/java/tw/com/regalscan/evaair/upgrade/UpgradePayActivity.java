package tw.com.regalscan.evaair.upgrade;

import java.lang.ref.WeakReference;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
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
import tw.com.regalscan.component.SwipeListView;
import tw.com.regalscan.customClass.ItemPayInfo;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.PaymentItem;
import tw.com.regalscan.db.UpgradeTransaction.CreditCardType;
import tw.com.regalscan.db.UpgradeTransaction.PaymentType;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.PaymentList;
import tw.com.regalscan.evaair.ife.NCCCAuthorize;
import tw.com.regalscan.evaair.ife.model.entity.AuthorizeModel;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;

import static android.view.View.GONE;


public class UpgradePayActivity extends AppCompatActivity {

    //付款歷程
    public static ItemListPayAdapter adapter;
    private SwipeListView discountListView;

    private RelativeLayout rowText01;
    private TextView txtToolbarTitle;
    private static Button btnReturn, btnPrint, btnCash, btnCard, btnCupon, btnChange;
    public static Context mContext;
    public Activity mActivity;
    private boolean isDiscountVisible = false;
    private ImageView arrow;
    private FragmentManager fm = getFragmentManager();
    private static FragmentUpgradePayCard f_card = new FragmentUpgradePayCard();
    private static FragmentUpgradePayCash f_cash = new FragmentUpgradePayCash();
    public static TextView txtShouldPayMoney, txtNotPayMoney, txtChangeMoney;
    public RelativeLayout rowtxt03;


    private DBQuery.UpgradeItemPack upgradePack;
    public static DBQuery.PaymentModePack payPack;
    public static String currentCurrency = "";
    private ProgressDialog mloadingDialog;

    private NCCCAuthorize mNCCCAuthorize;

    private boolean AuthorizeFlag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_01);

        Bundle argument = getIntent().getExtras();
        if (argument != null) {
            String itemString = argument.getString("UpgradeItemPack");
            Gson gson = new Gson();
            currentCurrency = argument.getString("Currency");
            upgradePack = gson.fromJson(itemString, DBQuery.UpgradeItemPack.class);
            init();
        } else {
            MessageBox.show("", "Get pay info error", mContext, "Return");
            finish();
        }
    }

    private void init() {
        mContext = this;
        mActivity = this;

        mNCCCAuthorize = new NCCCAuthorize(mContext);

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> new Thread(() -> {
            try {
                UpgradeBasketActivity.s_UpgradeTransaction.ClearPaymentList();
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Clear pay info error", mContext, "Return");
            }
        }).start());

        try {
            payPack = DBQuery.getPayMode(new StringBuilder(), UpgradeBasketActivity.s_UpgradeTransaction.GetPaymentMode());
            if (payPack == null) {
                MessageBox.show("", "Get pay info error", mContext, "Return");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get pay info error", mContext, "Return");
            return;
        }

        discountListView = findViewById(R.id.discountList);
        adapter = new ItemListPayAdapter(mContext, discountListView.getRightViewWidth());
        adapter.setIsSwipeDelete(true);
        adapter.setItemSwipeListener(itemSwipeListener);
        discountListView.setVisibility(View.GONE);
        discountListView.setAdapter(adapter);

        //應付款金額
        txtShouldPayMoney = findViewById(R.id.txtshouldPayMoney);
        txtShouldPayMoney.setText(Tools.getModiMoneyString(payPack.USDTotalAmount));
        //待付款金額
        txtNotPayMoney = findViewById(R.id.txtNotPayMoney);
        txtNotPayMoney.setText(Tools.getModiMoneyString(payPack.USDTotalUnpay));
        //應找零金額
        txtChangeMoney = findViewById(R.id.txtChangeMoney);
        txtChangeMoney.setText("0");

        // 找零的RelativeLayout
        rowtxt03 = findViewById(R.id.rowtxt03);
        rowtxt03.setVisibility(GONE);

        arrow = findViewById(R.id.img);
        arrow.setImageResource(R.drawable.icon_arrow_down_white);

        //付款歷程的顯示
        rowText01 = findViewById(R.id.rowText01);
        rowText01.setOnClickListener(v -> {
            // set ListView的visiable，和換箭頭icon
            if (isDiscountVisible) {
                isDiscountVisible = false;
                discountListView.setVisibility(View.GONE);
                arrow.setImageResource(R.drawable.icon_arrow_down_white);
            } else {
                isDiscountVisible = true;
                discountListView.setVisibility(View.VISIBLE);
                arrow.setImageResource(R.drawable.icon_arrow_up_white);
            }
        });

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.layout_fragment, f_cash, "Cash");
        ft.add(R.id.layout_fragment, f_card, "Card");
        ft.commit();

        btnCash = findViewById(R.id.btnCash);
        btnCard = findViewById(R.id.btnCard);
        btnCard.setText("Card");
        setFragmentBtnOnclickAction();
        btnCash.performClick();

        //設定title來源
        txtToolbarTitle = findViewById(R.id.toolbar_title);
        txtToolbarTitle.setText("Upgrade Pay");
        btnCupon = findViewById(R.id.btnCupon);
        btnCupon.setVisibility(GONE);
        btnChange = findViewById(R.id.btnChange);
        btnChange.setVisibility(GONE);
        ImageView line_02 = findViewById(R.id.line_02);
        line_02.setVisibility(GONE);
        ImageView line_03 = findViewById(R.id.line_03);
        line_03.setVisibility(GONE);

        btnPrint = findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(view -> {
            if (payPack.NowPayMode.equals("PAY")) {
                MessageBox.show("", "Should pay", mContext, "Return");
                return;
            }
            if (payPack.NowPayMode.equals("CHANGE")) {
                MessageBox.show("", "Should change", mContext, "Return");
                return;
            }

            if (FlightData.OnlineAuthorize) {
                AuthorizeModel authorizeModel = null;
                for (PaymentItem paymentItem : UpgradeBasketActivity.s_UpgradeTransaction.PaymentList) {
                    if (paymentItem.PayBy.equals("Card") && !paymentItem.CardType.equals("CUP")) {
                        authorizeModel = new AuthorizeModel();
                        authorizeModel.setCREDIT_CARD_NO(paymentItem.CardNo);
                        authorizeModel.setEXP_MONTH(paymentItem.CardDate.substring(0, 2));
                        authorizeModel.setEXP_YEAR(paymentItem.CardDate.substring(2, 4));
                        authorizeModel.setCREDIT_CARD_NAME(paymentItem.CardName);

                        switch (paymentItem.CardType) {
                            case "JCB":
                                authorizeModel.setCREDIT_CARD_TYPE("JC");
                                break;
                            case "VISA":
                                authorizeModel.setCREDIT_CARD_TYPE("VI");
                                break;
                            case "MASTER":
                                authorizeModel.setCREDIT_CARD_TYPE("MT");
                                break;
                            case "AMX":
                                authorizeModel.setCREDIT_CARD_TYPE("AX");
                                break;
                        }

                        authorizeModel.setRECIPT_NO(Integer.parseInt(UpgradeBasketActivity.s_UpgradeTransaction.ReceiptNo));
                        authorizeModel.setCurrency(paymentItem.Currency);

                        authorizeModel.setTWD_AMT(paymentItem.Currency.equals("TWD") ? (int)paymentItem.Amount : 0);
                        authorizeModel.setUSD_AMT(paymentItem.Currency.equals("USD") ? (int)paymentItem.Amount : 0);

                        authorizeModel.setUG_MARK("Y");
                        authorizeModel.setDEPT_FLT_NO(FlightData.FlightNo);
                        authorizeModel.setVIP_NO("");
                        authorizeModel.setVIP_TYPE("");
                        authorizeModel.setORDER_NO("");
                        authorizeModel.setIFE_SEQ("");
                        authorizeModel.setREAUTH_MARK("N");

                        break;
                    }
                }

                if (authorizeModel == null) {
                    savePayInfo();
                } else {
                    mNCCCAuthorize.SendRequestToNCCCGetAuthorize(authorizeModel, new NCCCAuthorize.AuthorizeReturn() {
                        @Override
                        public void success(AuthorizeModel authorizeModel) {
                            AuthorizeFlag = true;
                            savePayInfo();
                        }

                        @Override
                        public void failed(String errMsg) {
                            if (!errMsg.equals("") && MessageBox.show("", errMsg, mContext, "Ok")) {

                            } else {
                                savePayInfo();
                            }
                        }
                    });
                }
            } else {
                savePayInfo();
            }
        });
        btnPrint.setEnabled(false);
        setListenerToRootView();
        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }

    private void setFragmentBtnOnclickAction() {
        btnCash.setOnClickListener(view -> {
            FragmentTransaction ft = fm.beginTransaction();
            if (fm.findFragmentByTag("Cash") != null) {
                ft.show(fm.findFragmentByTag("Cash"));
            }
            ft.hide(f_card);
            ft.commit();
            updateButtonColor(0);
        });
        btnCard.setOnClickListener(view -> {

            FragmentTransaction ft = fm.beginTransaction();
            if (fm.findFragmentByTag("Card") != null) {
                ft.show(fm.findFragmentByTag("Card"));
            }
            ft.hide(f_cash);
            ft.commit();
            f_card.txtCardNumber.setText("No");
            f_card.txtCardDate.setText("Date");

            //彈跳確認視窗
            final Dialog dialog = new Dialog(mContext);
            dialog.setContentView(R.layout.dialog_credit_card);
            dialog.setTitle("Please choose card type");
            dialog.setCancelable(true);
            RadioGroup rgroup = dialog.findViewById(R.id.radioGroup);
            rgroup.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId) {
                    case R.id.radioBtn01:
                        // 可用USD或TWD付款
                        // 選擇卡別後先不填入幣別(disable), 刷卡後判斷卡種再填入
                        f_card.txtCardType.setText("Credit Card");
                        // TWDOnly, isEnable
                        f_card.setCUP(false);
                        break;

                    case R.id.radioBtn02: // CUP只能用台幣付款
                        f_card.txtCardType.setText("Union Pay");
                        f_card.setCUP(true);
                        break;
                }
                new Handler().postDelayed(() -> dialog.dismiss(), 500);
            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            updateButtonColor(1);
        });
    }

    // 0= cash, 1= card, 2= balance
    private static void updateButtonColor(int whitchIsClick) {
        switch (whitchIsClick) {
            case 0:
                if (!btnCash.isEnabled()) {
                    btnCard.setBackgroundColor(Color.WHITE);
                    btnCard.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnCash.setBackgroundColor(Color.parseColor("#007042"));
                    btnCash.setTextColor(Color.WHITE);
                }
                if (!btnCard.isEnabled()) {
                    btnCard.setBackgroundColor(Color.WHITE);
                    btnCard.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnCard.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnCard.setTextColor(Color.parseColor("#4E4A4B"));
                }
                break;

            case 1:
                if (!btnCash.isEnabled()) {
                    btnCard.setBackgroundColor(Color.WHITE);
                    btnCard.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnCash.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnCash.setTextColor(Color.parseColor("#4E4A4B"));
                }
                if (!btnCard.isEnabled()) {
                    btnCard.setBackgroundColor(Color.WHITE);
                    btnCard.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnCard.setBackgroundColor(Color.parseColor("#007042"));
                    btnCard.setTextColor(Color.WHITE);
                }
                break;

            case 2:
                btnCash.setBackgroundColor(Color.WHITE);
                btnCash.setTextColor(Color.parseColor("#C9CACA"));
                btnCard.setBackgroundColor(Color.WHITE);
                btnCard.setTextColor(Color.parseColor("#C9CACA"));
                break;
        }
    }


    // Mode: 0= AddPaymentList, 1= DeletePaymentList
    public static void getDBData(
        String Currency, PaymentType PayBy, double Amount,
        String CardNo, String CardName, String CardDate, CreditCardType CardType,
        int Mode) {

        try {
            StringBuilder err = new StringBuilder();
            // 新增或刪除付款
            switch (Mode) {
                case 0: //Add
                    payPack = DBQuery.getPayMode(err, UpgradeBasketActivity.s_UpgradeTransaction.AddPaymentList(Currency, PayBy, Amount, CardNo, CardName, CardDate, CardType));
                    break;
                case 1: //Delete
                    // String Currency, PaymentType PayBy
                    payPack = DBQuery.getPayMode(err, UpgradeBasketActivity.s_UpgradeTransaction.DeletePaymentList(Currency, PayBy));
                    break;
            }
            if (payPack == null) {
                MessageBox.show("", err.toString(), mContext, "Return");
                return;
            }

            // 設定未付款金額進editText, 按鈕
            switch (payPack.NowPayMode) {
                case "PAY":
                    txtNotPayMoney.setText(Tools.getModiMoneyString(payPack.USDTotalUnpay));
                    txtChangeMoney.setText("0");
                    // 不會有LastPayCurrency, 預設美金
                    f_cash.setEditText(Tools.getModiMoneyString(payPack.USDTotalAmount), "USD");
                    f_card.setEditText(Tools.getModiMoneyString(payPack.USDTotalUnpay), "USD");

                    btnCash.setEnabled(true);
                    btnCard.setEnabled(true);
                    btnPrint.setEnabled(false);
                    btnCash.performClick();
                    f_card.btnPay.setEnabled(true);
                    f_cash.btnPay.setEnabled(true);
                    updateButtonColor(0);
                    break;

                case "BALANCE":
                    txtNotPayMoney.setText("0");
                    txtChangeMoney.setText("0");

                    btnCash.setEnabled(false);
                    btnCard.setEnabled(false);
                    btnPrint.setEnabled(true);
                    f_card.btnPay.setEnabled(false);
                    f_cash.btnPay.setEnabled(false);
                    updateButtonColor(2);
                    break;
            }

            // 更改已付、未付金額
            txtShouldPayMoney.setText(Tools.getModiMoneyString(payPack.USDTotalAmount));

            // 塞入adapter
            // String CurDvr, String PayType, Double Amount, Double USDAmount
            adapter.clear();
            for (PaymentList pay : payPack.payLisy) {
                adapter.addItem(pay.Currency, pay.PayBy, pay.Amount, pay.USDAmount, pay.CouponNo);
            }
            adapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get pay info error", mContext, "Return");
        }
    }

    // 儲存Pay資訊
    private void savePayInfo() {
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        try {

            if (AuthorizeFlag) {
                UpgradeBasketActivity.s_UpgradeTransaction.AuthuticationFlag = true;
            }

            if (!UpgradeBasketActivity.s_UpgradeTransaction.SaveSalesInfo().getString("ReturnCode").equals("0")) {
                UpgradePayActivity.this.runOnUiThread(() -> {
                    mloadingDialog.dismiss();
                    MessageBox.show("", "Save pay info error", mActivity, "Return");
                });
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            UpgradePayActivity.this.runOnUiThread(() -> {
                mloadingDialog.dismiss();
                MessageBox.show("", "Save pay info error", mActivity, "Return");
            });
            return;
        }

        boolean isCredit = false;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).getPayType().equals("Card")) {
                isCredit = true;
                break;
            }
        }
        printData(isCredit);
    }


    private static class PrinterHandler extends Handler {
        private WeakReference<UpgradePayActivity> weakActivity;

        PrinterHandler(UpgradePayActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            UpgradePayActivity handlerActivity = weakActivity.get();

            handlerActivity.mloadingDialog.dismiss();
            switch (msg.what) {
                case 1: // 沒紙(信用卡)
                    if (MessageBox.show("", "No paper, reprint?", handlerActivity, "Yes", "No"))
                        handlerActivity.printData(true);
                    else handlerActivity.doPrintFinal();
                    break;

                case 2: // 沒紙(現金)
                    if (MessageBox.show("", "No paper, reprint?", handlerActivity, "Yes", "No"))
                        handlerActivity.printData(false);
                    else handlerActivity.doPrintFinal();
                    break;

                case 3: //Print error (信用卡)
                    if (MessageBox.show("", "Print error, retry?", handlerActivity, "Yes", "No"))
                        handlerActivity.printData(true);
                    else handlerActivity.doPrintFinal();
                    break;

                case 4: //Print error (現金)
                    if (MessageBox.show("", "Print error, retry?", handlerActivity, "Yes", "No"))
                        handlerActivity.printData(false);
                    else handlerActivity.doPrintFinal();
                    break;

                case 5: // 印收據
                    if (MessageBox.show("", "Print receipt", handlerActivity, "Ok")) handlerActivity.printData(false);
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
        if (MessageBox.show("", "Success", mContext, "Ok")) {
            //回到Basket, 清空
            if (ActivityManager.isActivityInStock("UpgradeBasketActivity"))
                ActivityManager.removeActivity("UpgradeBasketActivity");
            Intent intent = new Intent(mActivity, UpgradeBasketActivity.class);
            mActivity.startActivity(intent);

            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
        }
    }

    private void printData(final boolean isCredit) {
        mloadingDialog.dismiss();
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        new Thread() {
            public void run() {
                //列印收據, 簽單
                PrintAir printer = new PrintAir(mContext, Integer.valueOf(FlightData.SecSeq));
                try {
                    if (isCredit) {
                        // String ReceiptNo, int flag, String PreOrderNo, String VipOrderNo
                        if (printer.printUpgrade(upgradePack.ReceiptNo, isCredit) == -1) {
                            printerHandler.sendMessage(Tools.createMsg(1));
                        } else {
                            printerHandler.sendMessage(Tools.createMsg(5));
                        }
                    } else {
                        if (printer.printUpgrade(upgradePack.ReceiptNo, isCredit) == -1) {
                            printerHandler.sendMessage(Tools.createMsg(2));
                        } else {
                            printerHandler.sendMessage(Tools.createMsg(6));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                    _TSQL.WriteLog(FlightData.SecSeq,
                        "System", "UpgradePayActivity", "printUpgrade", e.getMessage());
                    if (isCredit) {
                        printerHandler.sendMessage(Tools.createMsg(3));
                    } else {
                        printerHandler.sendMessage(Tools.createMsg(4));
                    }
                }
            }
        }.start();
    }

    // 監聽鍵盤彈出時收起付款歷程
    public void setListenerToRootView() {
        final View activityRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
            if (heightDiff > 100) { // 99% of the time the height diff will be due to a keyboard.
//                    Toast.makeText(getApplicationContext(), "Gotcha!!! softKeyboardup", 0).show();
                isDiscountVisible = false;
                discountListView.setVisibility(View.GONE);
                arrow.setImageResource(R.drawable.icon_arrow_down_white);
            }
        });
    }

    //滑動刪除Button onClick
    private ItemListPayAdapter.ItemSwipeDeleteListener itemSwipeListener
        = new ItemListPayAdapter.ItemSwipeDeleteListener() {
        @Override
        public void swipeDeleteListener(int position) {
            try {
                if (MessageBox.show("", "Delete this pay?", mContext, "Yes", "No")) {
                    ItemPayInfo item = adapter.getItem(position);
                    PaymentType _type = null;

                    for (PaymentType type : PaymentType.values()) {
                        if (type.toString().equals(item.getPayType())) {
                            _type = type;
                        }
                    }
                    // String Currency, PaymentType PayBy, double Amount,
                    // String CardNo, String CardName, String CardDate, CreditCardType CardType
                    getDBData(item.getCurDvr(), _type, item.getAmount(), null, null, null, null, 1);

                    discountListView.hiddenRight(discountListView.mPreItemView);
                }
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Delete item error", mContext, "Return");
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
