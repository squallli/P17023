package tw.com.regalscan.evaair.report;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.regalscan.sqlitelibrary.TSQL;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import tw.com.regalscan.R;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.Receipt;
import tw.com.regalscan.db02.DBQuery.TransactionInfoPack;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;


public class ReportSaleActivity extends AppCompatActivity {

    private TextView txtToolbarTitle, txtTotalMoney;
    private FragmentManager fm = getFragmentManager();
    private FragmentReportItem f_item = new FragmentReportItem();
    private FragmentReportPayList f_payList = new FragmentReportPayList();

    private Button btnReturn, btnPrint, btnItem, btnPayList;
    public Context mContext;
    public Activity mActivity;
    private Spinner spinnerReceipt;
    private String fromWhere;
    private ArrayAdapter<String> orderList;
    // 所有單據號碼
    private DBQuery.ReceiptList receiptNoList, tmp_receiptNoList;
    private TransactionInfoPack transPack;
    private ProgressDialog mloadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_sale_refund);


        Bundle argument = getIntent().getExtras();
        if (argument != null) {
            fromWhere = argument.getString("intent");
            String itemString = argument.getString("jsonPack");
            Gson gson = new Gson();
            tmp_receiptNoList = gson.fromJson(itemString, DBQuery.ReceiptList.class);
            init();
        } else {
            MessageBox.show("", "Get sales info error", mContext, "Return");
            finish();
        }
    }

    private void init() {
        mContext = this;
        mActivity = this;
        enableExpandableList();

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
            }
        });

        txtToolbarTitle = findViewById(R.id.toolbar_title);
        if (fromWhere.equals("Sale")) {
            txtToolbarTitle.setText("Sale Report");
        } else if (fromWhere.equals("SaleRefund")) {
            txtToolbarTitle.setText("Refund Report");
        }
        txtTotalMoney = findViewById(R.id.txtMoney);

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.layout_fragment, f_item, "Item");
        ft.add(R.id.layout_fragment, f_payList, "PayList");
        ft.commit();

        //根據spinner選擇的單子載入內容
        btnItem = findViewById(R.id.btnItem);
        btnPayList = findViewById(R.id.btnList);
        btnPayList.setText("Pay List");

        btnItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = fm.beginTransaction();
                if (fm.findFragmentByTag("Item") != null) {
                    ft.show(fm.findFragmentByTag("Item"));
                }
                ft.hide(f_payList);
                ft.commit();
                updateButtonColor(0);
            }
        });
        btnItem.performClick();

        btnPayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = fm.beginTransaction();
                if (fm.findFragmentByTag("PayList") != null) {
                    ft.show(fm.findFragmentByTag("PayList"));
                }
                ft.hide(f_item);
                ft.commit();
                updateButtonColor(1);
            }
        });

        String SaleFlag;
        if (fromWhere.equals("SaleRefund")) {
            SaleFlag = "R";
        } else {
            SaleFlag = "S";
        }
        ArrayList<String> tmpList = new ArrayList<>();
        for (int i = 0; i < tmp_receiptNoList.rececipts.length; i++) {
            if (tmp_receiptNoList.rececipts[i].SaleFlag.equals(SaleFlag)) {
                tmpList.add(tmp_receiptNoList.rececipts[i].ReceiptNo);
            }
        }
        if (tmpList.isEmpty()) {
            if (MessageBox.show("", "No sales report", mContext, "Return")) {
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
                return;
            }
        }
        // 重新排序所有單據
        tmpList = Tools.resortListNo(tmpList);
        receiptNoList = new DBQuery.ReceiptList();
        receiptNoList.rececipts = Arrays.copyOf(tmp_receiptNoList.rececipts, tmp_receiptNoList.rececipts.length);
        for (int i = 0; i < tmpList.size(); i++) {
            for (int j = 0; j < tmp_receiptNoList.rececipts.length; j++) {
                if (tmpList.get(i).equals(tmp_receiptNoList.rececipts[j].ReceiptNo)) {
                    receiptNoList.rececipts[i] = tmp_receiptNoList.rececipts[j];
                    break;
                }
            }
        }

        // 加入下拉選單
        orderList = new ArrayAdapter<String>(this, R.layout.spinner_item);
        for (String s : tmpList) {
            orderList.add(s);
        }
        orderList.notifyDataSetChanged();

        spinnerReceipt = findViewById(R.id.spinner01);
        spinnerReceipt.setAdapter(orderList);
        spinnerReceipt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadReceiptItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        //btn
        btnPrint = findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String spinnerString = spinnerReceipt.getSelectedItem().toString().toUpperCase();
                mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
                if (fromWhere.equals("Sale")) {
                    // PreOrder
                    if (spinnerString.contains("P")) {
                        printData(1, false);
                    } else if (spinnerString.contains("V")) {
                        printData(2, false);
                    } else {
                        printData(3, f_payList.isCardPaymentExit());
                    }
                } else if (fromWhere.equals("SaleRefund")) {
                    if (spinnerString.contains("P")) {
                        printData(4, false);
                    } else if (spinnerString.contains("V")) {
                        printData(5, false);
                    } else {
                        if (!transPack.info[0].PreorderNo.equals("")) {
                            printData(6, true);
                        } else {
                            printData(7, true);
                        }
                    }
                }
            }
        });

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }


    private static class PrinterHandler extends Handler {
        private WeakReference<ReportSaleActivity> weakActivity;

        PrinterHandler(ReportSaleActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            ReportSaleActivity handlerActivity = weakActivity.get();
            Context handlerContext = handlerActivity.mContext;

            handlerActivity.mloadingDialog.dismiss();
            switch (msg.what) {
                // 沒紙
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(msg.what, false);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                // Print Error
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(msg.what - 7, false);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                // 完成
                case 15:
                    handlerActivity.doPrintFinal();
                    break;


                // 沒紙 Sale 信用卡
                case 16:
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(3, true);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;
                // 沒紙 VS Refund 信用卡
                case 17:
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(6, true);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;
                // 沒紙 VS Refund 信用卡
                case 18:
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(7, true);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 19: // 提示 Sale 印收據
                    if (MessageBox.show("", "Print receipt", handlerContext, "Ok"))
                        handlerActivity.printData(3, false);
                    break;

                case 20: // 提示 VS Refund 印簽單
                    if (MessageBox.show("", "Print receipt", handlerContext, "Ok"))
                        handlerActivity.printData(6, true);
                    break;
                case 201: // 提示 VS Refund 印收據
                    if (MessageBox.show("", "Print receipt", handlerContext, "Ok"))
                        handlerActivity.printData(6, false);
                    break;
                case 21: // 提示 Sale Refund 印簽單
                    if (MessageBox.show("", "Print receipt", handlerContext, "Ok"))
                        handlerActivity.printData(7, true);
                    break;
                case 211: // 提示 Sale Refund 印收據
                    if (MessageBox.show("", "Print receipt", handlerContext, "Ok"))
                        handlerActivity.printData(7, false);
                    break;


                case 22: // Print Error (Sale) 信用卡
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(3, true);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;
                case 23: // Print Error (VS Refund) 信用卡
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(6, true);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;
                case 24: // Print Error (Sale Refund) 信用卡
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData(7, true);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;
            }
        }
    }


//    private Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            mloadingDialog.dismiss();
//            switch(msg.what){
//                // 沒紙
//                case 1: case 2: case 3: case 4: case 5: case 6: case 7:
//                    if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData(msg.what, false);
//                    else doPrintFinal();
//                    break;
//
//                // Print Error
//                case 8: case 9: case 10: case 11: case 12: case 13: case 14:
//                    if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData(msg.what-7, false);
//                    else doPrintFinal();
//                    break;
//
//                // 完成
//                case 15:
//                    doPrintFinal();
//                    break;
//
//
//                // 沒紙 Sale 信用卡
//                case 16:    if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData(3, true);
//                            else doPrintFinal();    break;
//                // 沒紙 VS Refund 信用卡
//                case 17:    if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData(6, true);
//                            else doPrintFinal();    break;
//                // 沒紙 VS Refund 信用卡
//                case 18:    if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData(7, true);
//                            else doPrintFinal();    break;
//
//                case 19: // 提示 Sale 印收據
//                    if(MessageBox.show("", "Print receipt", mContext, "Ok")) printData(3, false);   break;
//                case 20: // 提示 VS Refund 印收據
//                    if(MessageBox.show("", "Print receipt", mContext, "Ok")) printData(6, false);   break;
//                case 21: // 提示 Sale Refund 印收據
//                    if(MessageBox.show("", "Print receipt", mContext, "Ok")) printData(7, false);   break;
//
//
//                case 22: // Print Error (Sale) 信用卡
//                    if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData(3, true);
//                    else doPrintFinal();    break;
//                case 23: // Print Error (VS Refund) 信用卡
//                    if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData(6, true);
//                    else doPrintFinal();    break;
//                case 24: // Print Error (Sale Refund) 信用卡
//                    if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData(7, true);
//                    else doPrintFinal();    break;
//            }
//        }
//    };

    private void doPrintFinal() {
        ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
    }

    private void printData(final int action, final boolean isCredit) {
        mloadingDialog.dismiss();
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        final int spinnerIndex = spinnerReceipt.getSelectedItemPosition();
        Handler printerHandler = new PrinterHandler(this);

        new Thread() {
            public void run() {
                //列印收據
                PrintAir printer = new PrintAir(mContext,
                        Integer.valueOf(FlightData.SecSeq));
                try {
                    switch (action) {
                        case 1:
                            if (printer.printPreOrder(receiptNoList.rececipts[spinnerIndex].PreorderNo) == -1) {
                                printerHandler.sendMessage(Tools.createMsg(action));
                            } else {
                                printerHandler.sendMessage(Tools.createMsg(15));
                            }
                            break;

                        case 2:
                            // VipPaid
                            if (printer.printVIPPaid(receiptNoList.rececipts[spinnerIndex].PreorderNo) == -1) {
                                printerHandler.sendMessage(Tools.createMsg(action));
                            } else {
                                printerHandler.sendMessage(Tools.createMsg(15));
                            }
                            break;

                        case 3: // DFS, VipSale
                            // 有信用卡
                            if (isCredit) {
                                // 簽單
                                // DFS
                                if (receiptNoList.rececipts[spinnerIndex].PreorderNo.equals("")) {
                                    if (printer.printSale(spinnerReceipt.getSelectedItem().toString(), 1, "") == -1) {
                                        // 沒紙
                                        printerHandler.sendMessage(Tools.createMsg(16));
                                    } else {
                                        // 提示印收據
                                        printerHandler.sendMessage(Tools.createMsg(19));
                                    }
                                } else {
                                    // VS
                                    if (printer.printSale(spinnerReceipt.getSelectedItem().toString(), 1,
                                            receiptNoList.rececipts[spinnerIndex].PreorderNo) == -1) {
                                        // 沒紙
                                        printerHandler.sendMessage(Tools.createMsg(16));
                                    } else {
                                        // 提示印收據
                                        printerHandler.sendMessage(Tools.createMsg(19));
                                    }
                                }
                            } else {
                                // 收據
                                // DFS
                                if (receiptNoList.rececipts[spinnerIndex].PreorderNo.equals("")) {
                                    if (printer.printSale(spinnerReceipt.getSelectedItem().toString(), 0, "") == -1) {
                                        // 沒紙
                                        printerHandler.sendMessage(Tools.createMsg(action));
                                    } else {
                                        // 完成
                                        printerHandler.sendMessage(Tools.createMsg(15));
                                    }
                                } else {
                                    //VS
                                    if (printer.printSale(spinnerReceipt.getSelectedItem().toString(), 0,
                                            receiptNoList.rececipts[spinnerIndex].PreorderNo) == -1) {
                                        // 沒紙
                                        printerHandler.sendMessage(Tools.createMsg(action));
                                    } else {
                                        // 完成
                                        printerHandler.sendMessage(Tools.createMsg(15));
                                    }
                                }

                            }
                            break;

                        case 4:
                            if (printer.printPreOrderRefund(receiptNoList.rececipts[spinnerIndex].PreorderNo) == -1) {
                                printerHandler.sendMessage(Tools.createMsg(action));
                            } else {
                                printerHandler.sendMessage(Tools.createMsg(15));
                            }
                            break;

                        case 5:
                            // VipPaid
                            if (printer.printVIPPaidRefund(receiptNoList.rececipts[spinnerIndex].PreorderNo) == -1) {
                                printerHandler.sendMessage(Tools.createMsg(action));
                            } else {
                                printerHandler.sendMessage(Tools.createMsg(15));
                            }
                            break;

                        case 6:
                            if (isCredit) {
                                //簽單
                                if (printer.printVIPRefund(spinnerReceipt.getSelectedItem().toString(), true) == -1) {
                                    printerHandler.sendMessage(Tools.createMsg(17));
                                } else {
                                    // 提示印收據
                                    printerHandler.sendMessage(Tools.createMsg(201));
                                }
                            }else {
                                //收據
                                if (printer.printVIPRefund(spinnerReceipt.getSelectedItem().toString(), false) == -1) {
                                    printerHandler.sendMessage(Tools.createMsg(action));
                                } else {
                                    // 完成
                                    printerHandler.sendMessage(Tools.createMsg(15));
                                }
                            }
                            break;

                        case 7: //退款Refund
                            if (isCredit) {
                                // 簽單
                                if (printer.printRefund(spinnerReceipt.getSelectedItem().toString(), true) == -1) {
                                    printerHandler.sendMessage(Tools.createMsg(18));
                                } else {
                                    // 提示印收據
                                    printerHandler.sendMessage(Tools.createMsg(211));
                                }
                            }else {
                                //收據
                                if (printer.printRefund(spinnerReceipt.getSelectedItem().toString(), false) == -1) {
                                    printerHandler.sendMessage(Tools.createMsg(action));
                                } else {
                                    // 完成
                                    printerHandler.sendMessage(Tools.createMsg(15));
                                }
                            }

                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                    _TSQL.WriteLog(FlightData.SecSeq,
                            "System", "ReportSaleActivity", "printSale" + "function " + action, e.getMessage());
                    if (isCredit) {
                        switch (action) {
                            case 3:
                                printerHandler.sendMessage(Tools.createMsg(22));
                                break;
                            case 6:
                                printerHandler.sendMessage(Tools.createMsg(23));
                                break;
                            case 7:
                                printerHandler.sendMessage(Tools.createMsg(24));
                                break;
                        }
                    } else {
                        printerHandler.sendMessage(Tools.createMsg(action + 7));
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

    // =0 item, 1= list
    private void updateButtonColor(int whitchIsClick) {
        switch (whitchIsClick) {
            case 0:
                btnItem.setBackgroundColor(Color.parseColor("#1CB074"));
                btnItem.setTextColor(Color.WHITE);
                btnPayList.setBackgroundColor(Color.parseColor("#C9CACA"));
                btnPayList.setTextColor(Color.parseColor("#4E4A4B"));
                break;

            case 1:
                btnItem.setBackgroundColor(Color.parseColor("#C9CACA"));
                btnItem.setTextColor(Color.parseColor("#4E4A4B"));
                btnPayList.setBackgroundColor(Color.parseColor("#1CB074"));
                btnPayList.setTextColor(Color.WHITE);
                break;
        }
    }


    private void loadReceiptItem(final int position) {

        final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        new Thread() {
            public void run() {
                StringBuilder err = new StringBuilder();
                Receipt receiptNo = receiptNoList.rececipts[position];
                try {
                    // PR, VP單據
                    if (receiptNo.ReceiptNo.toLowerCase().contains("p") || receiptNo.ReceiptNo.toLowerCase().contains("v")) {
                        final DBQuery.PreorderInfoPack preorderPack;
                        // 用Preorder頁面的function查詢
                        if (fromWhere.equals("Sale")) {
                            preorderPack = DBQuery.getPRVPCanSaleRefund(mContext, err, FlightData.SecSeq, receiptNo.PreorderNo, new String[]{"PR", "VP"}, "S");
                        } else {
                            preorderPack = DBQuery.getPRVPCanSaleRefund(mContext, err, FlightData.SecSeq, receiptNo.PreorderNo, new String[]{"PR", "VP"}, "R");
                        }
                        ReportSaleActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (preorderPack == null || preorderPack.info == null) {
                                    mloadingDialog.dismiss();
                                    MessageBox.show("", "Query pre-order data error", mContext, "Return");
                                    ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                                    ReportSaleActivity.this.finish();
                                    return;
                                }
                                //購買物品清單
                                f_item.loadItem(preorderPack.info[0]);
                                //付款歷程清單
                                f_payList.loadItem();
                                txtTotalMoney.setText(preorderPack.info[0].CurDvr + " " + (int) Math.round(preorderPack.info[0].Amount));
                                mloadingDialog.dismiss();
                            }
                        });

                    } else {
                        transPack = DBQuery.getDFSTransactionInfo(mContext, err, receiptNo.ReceiptNo);
                        if (transPack == null) {
                            ReportSaleActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mloadingDialog.dismiss();
                                    MessageBox.show("", "Get sales info error", mContext, "Return");
                                }
                            });
                            return;
                        }
                        ReportSaleActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //購買物品清單
                                f_item.loadItem(transPack.info[0]);
                                //付款歷程清單
                                f_payList.loadItem(transPack.info[0].payments);
                                txtTotalMoney.setText("USD " + (int) Math.round(transPack.info[0].USDAmount));
                                mloadingDialog.dismiss();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ReportSaleActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mloadingDialog.dismiss();
                            MessageBox.show("", "Get sales info error", mContext, "Return");
                        }
                    });
                }
            }
        }.start();
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