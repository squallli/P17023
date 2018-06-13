package tw.com.regalscan.evaair.report;

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

import com.google.gson.Gson;
import com.regalscan.sqlitelibrary.TSQL;
import tw.com.regalscan.R;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.ReceiptList;
import tw.com.regalscan.db02.DBQuery.TransferItemPack;
import tw.com.regalscan.db02.DBQuery.UpgradeTransactionInfoPack;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;


public class ReportActivity extends AppCompatActivity {


    private Button btnSale, btnRefund, btnTransfer, btnUpgrade,
        btnUpgradeRefund, btnDamage, btnUpdate, btnBeginInventory,
        btnInventoryNumber, btnInventoryDraw, btnReturn;
    public Context mContext;
    public Activity mActivity;
    private PrintAir printer;
    private ProgressDialog mloadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);


        init();
    }


    private void init() {
        mContext = this;
        mActivity = this;
        enableExpandableList();

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
        });

        printer = new PrintAir(mContext, Integer.valueOf(FlightData.SecSeq));

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnSale = findViewById(R.id.btnSale);
        btnRefund = findViewById(R.id.btnRefund);
        btnTransfer = findViewById(R.id.btnTransfer);
        btnUpgrade = findViewById(R.id.btnUpgrade);
        btnUpgradeRefund = findViewById(R.id.btnUpgradeRefund);
        btnDamage = findViewById(R.id.btnDamage);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBeginInventory = findViewById(R.id.btnBeginInventory);
        btnInventoryNumber = findViewById(R.id.btnInventoryNumber);
        btnInventoryDraw = findViewById(R.id.btnInventoryDraw);

        btnReturn.setOnClickListener(menuBtnOnClick);
        btnSale.setOnClickListener(menuBtnOnClick);
        btnRefund.setOnClickListener(menuBtnOnClick);
        btnTransfer.setOnClickListener(menuBtnOnClick);
        btnUpgrade.setOnClickListener(menuBtnOnClick);
        btnUpgradeRefund.setOnClickListener(menuBtnOnClick);
        btnDamage.setOnClickListener(menuBtnOnClick);
        btnUpdate.setOnClickListener(menuBtnOnClick);
        btnBeginInventory.setOnClickListener(menuBtnOnClick);
        btnInventoryNumber.setOnClickListener(menuBtnOnClick);
        btnInventoryDraw.setOnClickListener(menuBtnOnClick);

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


    private Button.OnClickListener menuBtnOnClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent;
            Bundle argument = new Bundle();
            final StringBuilder err = new StringBuilder();
            Gson gson = new Gson();
            String jsonPack = "";
            String jaonTransferOutPack = "";
            String jaonTransferInPack = "";
            ReceiptList receiptNoList;
            UpgradeTransactionInfoPack transactionPack;

            switch (v.getId()) {
                case R.id.btnReturn:
                    ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                    break;

                case R.id.btnSale:
                    receiptNoList = DBQuery.getAllRceciptNoList(mContext, err, "Sale", false);
                    if (receiptNoList == null) {
                        MessageBox.show("", "Query order data error", mContext, "Return");
                        return;
                    }
                    if (receiptNoList.rececipts == null) {
                        MessageBox.show("", "No sales report", mContext, "Return");
                        return;
                    }
                    // 轉json傳入
                    jsonPack = gson.toJson(receiptNoList);
                    argument.putString("jsonPack", jsonPack);
                    argument.putString("intent", "Sale");
                    intent = new Intent(mActivity, ReportSaleActivity.class);
                    intent.putExtras(argument);
                    mActivity.startActivity(intent);
                    break;

                case R.id.btnRefund:
                    receiptNoList = DBQuery.getAllRceciptNoList(mContext, err, "Refund", true);
                    if (receiptNoList == null) {
                        MessageBox.show("", "Query order data error", mContext, "Return");
                        return;
                    }
                    if (receiptNoList.rececipts == null) {
                        MessageBox.show("", "No sales report", mContext, "Return");
                        return;
                    }
                    // 轉json傳入
                    jsonPack = gson.toJson(receiptNoList);
                    argument.putString("jsonPack", jsonPack);
                    argument.putString("intent", "SaleRefund");
                    intent = new Intent(mActivity, ReportSaleActivity.class);
                    intent.putExtras(argument);
                    mActivity.startActivity(intent);
                    break;

                case R.id.btnTransfer:
                    TransferItemPack transferOutPack = DBQuery.queryTransferItemQty(mContext, err, null, "OUT");
                    TransferItemPack transferInPack = DBQuery.queryTransferItemQty(mContext, err, null, "IN");
                    if (transferInPack == null) {
                        MessageBox.show("", "Query transfer list error", mContext, "Return");
                        return;
                    }
                    if (transferOutPack == null) {
                        MessageBox.show("", "Query transfer list error", mContext, "Return");
                        return;
                    }
                    if (transferInPack.transfers == null && transferOutPack.transfers == null) {
                        MessageBox.show("", "No transfer report", mContext, "Return");
                        return;
                    }
                    // 轉json傳入
                    jaonTransferOutPack = gson.toJson(transferOutPack);
                    jaonTransferInPack = gson.toJson(transferInPack);
                    argument.putString("jasonTransferOutPack", jaonTransferOutPack);
                    argument.putString("jasonTransferInPack", jaonTransferInPack);
                    intent = new Intent(mActivity, ReportTransferActivity.class);
                    intent.putExtras(argument);
                    mActivity.startActivity(intent);
                    break;

                case R.id.btnUpgrade:
                    transactionPack = DBQuery.getUpgradeTransactionInfo(mContext, err, null, "S");
                    if (transactionPack == null) {
                        MessageBox.show("", "No upgrade report", mContext, "Return");
                        return;
                    }
                    jsonPack = gson.toJson(transactionPack);
                    argument.putString("jsonPack", jsonPack);
                    argument.putString("intent", "Upgrade");
                    intent = new Intent(mActivity, ReportUpgradeActivity.class);
                    intent.putExtras(argument);
                    mActivity.startActivity(intent);
                    break;

                case R.id.btnUpgradeRefund:
                    transactionPack = DBQuery.getUpgradeTransactionInfo(mContext, err, null, "R");
                    if (transactionPack == null) {
                        MessageBox.show("", "No upgrade report", mContext, "Return");
                        return;
                    }
                    jsonPack = gson.toJson(transactionPack);
                    argument.putString("jsonPack", jsonPack);
                    argument.putString("intent", "UpgradeRefund");
                    intent = new Intent(mActivity, ReportUpgradeActivity.class);
                    intent.putExtras(argument);
                    mActivity.startActivity(intent);
                    break;

                /* 直接印出單據 */
                case R.id.btnDamage:
                    mloadingDialog = ProgressDialog.show(mActivity, "", "Processing...", true, false);
                    try {
                        DBQuery.DamageItemPack originDamagePack = DBQuery.damageItemQty(mContext, err, null);
                        if (originDamagePack == null) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mloadingDialog.dismiss();
                                    MessageBox.show("", "Get damage data error", mActivity, "Return");
                                }
                            });
                            return;
                        }
                        if (originDamagePack.damages == null) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mloadingDialog.dismiss();
                                    MessageBox.show("", "No damage list", mActivity, "Ok");
                                }
                            });
                            return;
                        }
                    } catch (Exception e) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mloadingDialog.dismiss();
                                MessageBox.show("", "Get damage data error", mActivity, "Return");
                            }
                        });
                        return;
                    }
                    printData(1);
                    break;

                case R.id.btnUpdate:
                    mloadingDialog = ProgressDialog.show(mActivity, "", "Processing...", true, false);
                    try {
                        boolean flagPrint = false;
                        DBQuery.ItemDataPack itemDataPack = DBQuery.getAdjustInfo(mContext, err, FlightData.SecSeq, null, null, 0);
                        if (itemDataPack != null && itemDataPack.items != null) {
                            for (int i = 0; i < itemDataPack.items.length; i++) {
                                if (itemDataPack.items[i].StartQty != itemDataPack.items[i].EndQty) {
                                    flagPrint = true;
                                    break;
                                }
                            }
                        }
                        if (!flagPrint) {
                            mActivity.runOnUiThread(() -> {
                                mloadingDialog.dismiss();
                                MessageBox.show("", "No update list", mActivity, "Ok");
                            });
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mloadingDialog.dismiss();
                                MessageBox.show("", "Get update data error", mActivity, "Return");
                            }
                        });
                        return;
                    }
                    printData(2);
                    break;

                case R.id.btnBeginInventory:
                    mloadingDialog = ProgressDialog.show(mActivity, "", "Processing...", true, false);
                    printData(3);
                    break;

                case R.id.btnInventoryNumber:
                    mloadingDialog = ProgressDialog.show(mActivity, "", "Processing...", true, false);
                    printData(4);
                    break;

                case R.id.btnInventoryDraw:
                    mloadingDialog = ProgressDialog.show(mActivity, "", "Processing...", true, false);
                    printData(5);
                    break;
            }
        }
    };


    private static class PrinterHandler extends Handler {
        private WeakReference<ReportActivity> weakActivity;

        PrinterHandler(ReportActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            ReportActivity handlerActivity = weakActivity.get();
            Context handlerContext = handlerActivity.mContext;

            handlerActivity.mloadingDialog.dismiss();
            switch (msg.what) {
                // 沒紙
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No"))
                        handlerActivity.printData(msg.what);
                    break;

                //Print error
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No"))
                        handlerActivity.printData(msg.what - 5);
                    break;

                case 11: //成功
                    break;
            }
        }
    }

//  private Handler mHandler = new Handler(){
//    @Override
//    public void handleMessage(Message msg) {
//      mloadingDialog.dismiss();
//      switch(msg.what){
//        // 沒紙
//        case 1: case 2: case 3: case 4: case 5:
//          if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData(msg.what);
//          break;
//
//        //Print error
//        case 6: case 7: case 8: case 9: case 10:
//          if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData(msg.what-5);
//          break;
//
//        case 11: //成功
//          break;
//      }
//    }
//  };

    private void printData(int functionNumber) {
        mloadingDialog.dismiss();
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        switch (functionNumber) {
            case 1: // Damage
                new Thread() {
                    public void run() {
                        try {
                            if (printer.printDamageList() == -1) {
                                printerHandler.sendMessage(Tools.createMsg(1));
                            } else {
                                printerHandler.sendMessage(Tools.createMsg(11));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                            _TSQL.WriteLog(FlightData.SecSeq,
                                "System", "ReportActivity", "printDamageList", e.getMessage());
                            printerHandler.sendMessage(Tools.createMsg(6));
                        }
                    }
                }.start();

                break;

            case 2: //Update
                new Thread() {
                    public void run() {
                        try {
                            if (printer.printUpdateList() == -1) {
                                printerHandler.sendMessage(Tools.createMsg(2));
                            } else {
                                printerHandler.sendMessage(Tools.createMsg(11));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                            _TSQL.WriteLog(FlightData.SecSeq,
                                "System", "ReportActivity.java", "printUpdateList", e.getMessage());
                            printerHandler.sendMessage(Tools.createMsg(7));
                        }
                    }
                }.start();
                break;

            case 3: // Begin Inventory
                new Thread() {
                    public void run() {
                        try {
                            if (printer.printBeginInventory() == -1) {
                                printerHandler.sendMessage(Tools.createMsg(3));
                            } else {
                                printerHandler.sendMessage(Tools.createMsg(11));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                            _TSQL.WriteLog(FlightData.SecSeq,
                                "System", "ReportActivity", "printBeginInventory", e.getMessage());
                            printerHandler.sendMessage(Tools.createMsg(8));
                        }
                    }
                }.start();
                break;

            case 4: //Inventory by Number
                new Thread() {
                    public void run() {
                        try {
                            if (printer.printAllItem(true) == -1) {
                                printerHandler.sendMessage(Tools.createMsg(4));
                            } else {
                                printerHandler.sendMessage(Tools.createMsg(11));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                            _TSQL.WriteLog(FlightData.SecSeq,
                                "System", "ReportActivity", "printAllItem", e.getMessage());
                            printerHandler.sendMessage(Tools.createMsg(9));
                        }
                    }
                }.start();
                break;

            case 5://Inventory by Drawer
                new Thread() {
                    public void run() {
                        try {
                            if (printer.printAllItem(false) == -1) {
                                printerHandler.sendMessage(Tools.createMsg(5));
                            } else {
                                printerHandler.sendMessage(Tools.createMsg(11));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                            _TSQL.WriteLog(FlightData.SecSeq,
                                "System", "ReportActivity", "printAllItem", e.getMessage());
                            printerHandler.sendMessage(Tools.createMsg(10));
                        }
                    }
                }.start();
                break;
        }
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