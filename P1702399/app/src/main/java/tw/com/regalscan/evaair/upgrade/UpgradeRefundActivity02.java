package tw.com.regalscan.evaair.upgrade;

import java.lang.ref.WeakReference;

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
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.regalscan.sqlitelibrary.TSQL;
import tw.com.regalscan.R;
import tw.com.regalscan.adapters.ItemListPayAdapter;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.PaymentList;
import tw.com.regalscan.db02.DBQuery.UpgradeItemPack;
import tw.com.regalscan.evaair.ife.IFEDBFunction;
import tw.com.regalscan.evaair.ife.NCCCAuthorize;
import tw.com.regalscan.evaair.ife.model.entity.AuthorizeModel;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;

public class UpgradeRefundActivity02 extends AppCompatActivity {

    //付款歷程
    public ItemListPayAdapter adapter;
    private ListView discountListView;

    private Button btnReturn, btnRefund;
    public Context mContext;
    public Activity mActivity;
    private UpgradeItemPack upgradePack;
    //當前訂單明細
    private DBQuery.PaymentModePack payPackOld;

    private TextView txtTotalMoney;
    private ProgressDialog mloadingDialog;

    private IFEDBFunction mIFEDBFunction;
    private NCCCAuthorize mNCCCAuthorize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_refund_02);

        Bundle argument = getIntent().getExtras();
        if (argument != null) {
            String jsonOrder = argument.getString("order");
            Gson gson = new Gson();
            upgradePack = gson.fromJson(jsonOrder, UpgradeItemPack.class);
            if (upgradePack == null) {
                UpgradeRefundActivity02.this.runOnUiThread(() -> MessageBox.show("", "Get pay info error", mContext, "Return"));
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
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

        mIFEDBFunction = new IFEDBFunction(mContext, FlightData.SecSeq);
        mNCCCAuthorize = new NCCCAuthorize(mContext);

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            if (ActivityManager.isActivityInStock("UpgradeRefundActivity01")) {
                ActivityManager.removeActivity("UpgradeRefundActivity01");
            }
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
            if (!ActivityManager.isActivityInStock("UpgradeActivity01")) {
                Intent mIntent = new Intent(mActivity, UpgradeActivity01.class);
                mActivity.startActivity(mIntent);
            }
        });

        try {
            // 取得原始退款交易物件
            payPackOld = DBQuery.getPayMode(new StringBuilder(), UpgradeRefundActivity01._UpgradeRefundTranscation.GetOriginalPaymentMold());
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get payment info error.", mContext, "Return");
            return;
        }

        //付款歷程
        discountListView = findViewById(R.id.discountList);
        adapter = new ItemListPayAdapter(mContext);
        for (PaymentList pay : payPackOld.payLisy) {
            adapter.addItem(pay.Currency, pay.PayBy, pay.Amount, pay.USDAmount, pay.CouponNo);
        }
        discountListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        txtTotalMoney = findViewById(R.id.txtMoneyTotal);
        txtTotalMoney.setText("USD " + Tools.getModiMoneyString(payPackOld.USDTotalAmount));

        btnRefund = findViewById(R.id.btnRefund);
        btnRefund.setOnClickListener(view -> {
            if (MessageBox.show("", "Refund?", UpgradeRefundActivity02.this, "Yes", "No")) {
                mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
                try {
                    if (!UpgradeRefundActivity01._UpgradeRefundTranscation.SaveRefundInfoByOriginal().getString("ReturnCode")
                        .equals("0")) {
                        UpgradeRefundActivity02.this.runOnUiThread(() -> {
                            mloadingDialog.dismiss();
                            MessageBox.show("", "Save refund info error", mContext, "Return");
                        });
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    UpgradeRefundActivity02.this.runOnUiThread(() -> {
                        mloadingDialog.dismiss();
                        MessageBox.show("", "Save refund info error", mContext, "Return");
                    });
                    return;
                }
                boolean isCredit = false;
                for (PaymentList item : payPackOld.payLisy) {
                    // String payType, String monyType, Double mony
                    adapter.addItem(item.Currency, item.PayBy, item.Amount, item.USDAmount, item.CouponNo);
                    if (item.PayBy.equals("Card")) {
                        isCredit = true;
                    }
                }
                //printData(isCredit);
                printData(true);
            }
        });
        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }


    private static class PrinterHandler extends Handler {
        private WeakReference<UpgradeRefundActivity02> weakActivity;

        PrinterHandler(UpgradeRefundActivity02 a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            UpgradeRefundActivity02 handlerActivity = weakActivity.get();
            Context handlerContext = handlerActivity.mContext;

            handlerActivity.mloadingDialog.dismiss();
            switch (msg.what) {
                case 1: // 沒紙(信用卡)
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No"))
                        handlerActivity.printData(true);
                    else handlerActivity.doPrintFinal();
                    break;

                case 2: // 沒紙(現金)
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No"))
                        handlerActivity.printData(true);
                    else handlerActivity.doPrintFinal();
                    break;

                case 3: //Print error (信用卡)
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No"))
                        handlerActivity.printData(true);
                    else handlerActivity.doPrintFinal();
                    break;

                case 4: //Print error (現金)
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No"))
                        handlerActivity.printData(true);
                    else handlerActivity.doPrintFinal();
                    break;

                case 5: // 印簽單
                    if (MessageBox.show("", "Print receipt", handlerContext, "Ok")) handlerActivity.printData(true);
                    break;
                case 51: // 印收據
                    if (MessageBox.show("", "Print receipt", handlerContext, "Ok")) handlerActivity.printData(false);
                    break;

                case 6: //成功
                    handlerActivity.doPrintFinal();
                    break;
            }
        }
    }

    private void doPrintFinal() {

        AuthorizeModel authorizeModel = mIFEDBFunction.getAuthorizeInfo(upgradePack.ReceiptNo, "UpgradeRefund");

        if (FlightData.OnlineAuthorize && authorizeModel != null) {
            authorizeModel.setREAUTH_MARK("Y");
            authorizeModel.setUG_MARK("Y");

            mNCCCAuthorize.SendRequestToNCCCGetAuthorize(authorizeModel, new NCCCAuthorize.AuthorizeReturn() {
                @Override
                public void success(AuthorizeModel authorizeModel) {
                    if (MessageBox.show("", "Success", mContext, "Ok")) {
                        ActivityManager.closeAllActivity();
                    }
                }

                @Override
                public void failed(String errMsg) {
                    if (MessageBox.show("", "Success", mContext, "Ok")) {
                        ActivityManager.closeAllActivity();
                    }
                }
            });
        } else {
            if (MessageBox.show("", "Success", mContext, "Ok")) {
                ActivityManager.closeAllActivity();
            }
        }
    }

    private void printData(final boolean isCredit) {
        mloadingDialog.dismiss();
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        new Thread() {
            public void run() {
                try {
                    //列印收據
                    PrintAir printer = new PrintAir(mContext,
                        Integer.valueOf(FlightData.SecSeq));
                    if (isCredit) {
                        // String ReceiptNo, int flag, String PreOrderNo, String VipOrderNo
                        if (printer.printUpgradeRefund(upgradePack.ReceiptNo, isCredit) == -1) {
                            printerHandler.sendMessage(Tools.createMsg(1));
                        } else {
                            printerHandler.sendMessage(Tools.createMsg(51));
                        }
                    } else {
                        if (printer.printUpgradeRefund(upgradePack.ReceiptNo, isCredit) == -1) {
                            printerHandler.sendMessage(Tools.createMsg(2));
                        } else {
                            printerHandler.sendMessage(Tools.createMsg(6));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                    _TSQL.WriteLog(FlightData.SecSeq,
                        "System", "UpgradeRefundActivity02.java", "printUpgradeRefund", e.getMessage());
                    if (isCredit) {
                        printerHandler.sendMessage(Tools.createMsg(3));
                    } else {
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