package tw.com.regalscan.evaair.report;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import com.regalscan.sqlitelibrary.TSQL;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.R;
import tw.com.regalscan.db02.DBQuery.UpgradeTransactionInfoPack;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;

public class ReportUpgradeActivity extends AppCompatActivity {



    private TextView txtToolbarTitle, txtTotalMoney;
    private FragmentManager fm = getFragmentManager();
    private FragmentReportUpgradeItem f_upgrade_item=new FragmentReportUpgradeItem();
    private FragmentReportUpgradePayList f_payList=new FragmentReportUpgradePayList();

    private Button btnReturn, btnPrint, btnItem, btnPayList;
    public Context mContext;
    public Activity mActivity;
    private Spinner spinnerReceipt;
    private String fromWhere;

    //升等倉交易資訊
    private UpgradeTransactionInfoPack transactionPack, tmp_transactionPack;
    private ArrayAdapter<String> orderList;
    private StringBuilder err= new StringBuilder();
    private ProgressDialog mloadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_sale_refund);




        Bundle argument = getIntent().getExtras();
        if(argument!=null){
            fromWhere = argument.getString("intent");
            String itemString = argument.getString("jsonPack");
            Gson gson = new Gson();
            tmp_transactionPack=  gson.fromJson(itemString, UpgradeTransactionInfoPack.class);
            init();
        }
        else{
            MessageBox.show("", "Get upgrade info error", mContext, "Return");
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

        if(fromWhere.equals("Upgrade")){
            txtToolbarTitle.setText("Upgrade Report");
        }else if(fromWhere.equals("UpgradeRefund")){
            txtToolbarTitle.setText("Upgrade Refund Report");
        }

        txtTotalMoney= findViewById(R.id.txtMoney);

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.layout_fragment, f_upgrade_item, "Item");
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
                if(fm.findFragmentByTag("Item") != null){
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
                if(fm.findFragmentByTag("PayList") != null){
                    ft.show(fm.findFragmentByTag("PayList"));
                }
                ft.hide(f_upgrade_item);
                ft.commit();
                updateButtonColor(1);
            }
        });

        // 將編號重新排序
        ArrayList<String> tmpList= new ArrayList<>();
        for(int i=0; i<tmp_transactionPack.info.length; i++){
            tmpList.add( tmp_transactionPack.info[i].ReceiptNo );
        }
        tmpList= Tools.resortListNo(tmpList);
        transactionPack= new UpgradeTransactionInfoPack();
        transactionPack.info= Arrays.copyOf(tmp_transactionPack.info, tmp_transactionPack.info.length);
        for(int i=0; i<tmpList.size(); i++){
            for(int j=0; j<tmp_transactionPack.info.length; j++){
                if(tmpList.get(i).equals(tmp_transactionPack.info[j].ReceiptNo)){
                    transactionPack.info[i]=tmp_transactionPack.info[j];
                    break;
                }
            }
        }

        //Receipt No
        spinnerReceipt = findViewById(R.id.spinner01);
        orderList = new ArrayAdapter<String>(this, R.layout.spinner_item);
        for(String s: tmpList){
            orderList.add(s);
        }
        spinnerReceipt.setAdapter(orderList);
        spinnerReceipt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //物品清單
                f_upgrade_item.loadItem(transactionPack.info[position]);
                //付款清單
                f_payList.loadItem(transactionPack.info[position].payments);
                txtTotalMoney.setText("USD " + Tools.getModiMoneyString(transactionPack.info[position].TotalPrice));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        //btn
        btnPrint = findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                printData(f_payList.isCardPaymentExit());
                printData(true);
            }
        });
        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }


    private static class PrinterHandler extends Handler {
        private WeakReference<ReportUpgradeActivity> weakActivity;
        PrinterHandler(ReportUpgradeActivity a) {
            weakActivity = new WeakReference<>(a);
        }
        @Override
        public void handleMessage(Message msg) {
            ReportUpgradeActivity handlerActivity = weakActivity.get();
            Context handlerContext= handlerActivity.mContext;

            handlerActivity.mloadingDialog.dismiss();
            switch(msg.what){
                // upgrade沒紙 (信用卡)
                case 1:     if(MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No") ) handlerActivity.printData(true);
                else handlerActivity.doPrintFinal();    break;
                // upgrade沒紙 (現金)
                case 2:     if(MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No") ) handlerActivity.printData(true);
                else handlerActivity.doPrintFinal();    break;
                // upgrade Print error (信用卡)
                case 3:     if(MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) handlerActivity.printData(true);
                else handlerActivity.doPrintFinal();    break;
                // upgrade Print error (現金)
                case 4:     if(MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) handlerActivity.printData(true);
                else handlerActivity.doPrintFinal();    break;
                // upgrade 印收據
                case 5:     if(MessageBox.show("", "Print receipt", handlerContext, "Ok")) handlerActivity.printData(true);  break;


                // refund沒紙(信用卡)
                case 6:     if(MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No") ) handlerActivity.printData(true);
                else handlerActivity.doPrintFinal();    break;
                // refund沒紙(現金)
                case 7:     if(MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No") ) handlerActivity.printData(true);
                else handlerActivity.doPrintFinal();    break;
                // refund Print error (信用卡)
                case 8:     if(MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) handlerActivity.printData(true);
                else handlerActivity.doPrintFinal();    break;
                // refund Print error (現金)
                case 9:     if(MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) handlerActivity.printData(true);
                else handlerActivity.doPrintFinal();    break;
                // refund 印簽單
                case 10:
                    if(MessageBox.show("", "Print receipt", handlerContext, "Ok"))
                    handlerActivity.printData(true);
                break;
                // refund 印收據
                case 101:
                    if(MessageBox.show("", "Print receipt", handlerContext, "Ok"))
                        handlerActivity.printData(false);
                    break;

                case 11: //成功
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
//                // upgrade沒紙 (信用卡)
//                case 1:     if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData(true);
//                            else doPrintFinal();    break;
//                // upgrade沒紙 (現金)
//                case 2:     if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData(false);
//                            else doPrintFinal();    break;
//                // upgrade Print error (信用卡)
//                case 3:     if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData(true);
//                            else doPrintFinal();    break;
//                // upgrade Print error (現金)
//                case 4:     if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData(false);
//                            else doPrintFinal();    break;
//                // upgrade 印收據
//                case 5:     if(MessageBox.show("", "Print receipt", mContext, "Ok")) printData(false);  break;
//
//
//                // refund沒紙(信用卡)
//                case 6:     if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData(true);
//                            else doPrintFinal();    break;
//                // refund沒紙(現金)
//                case 7:     if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData(false);
//                            else doPrintFinal();    break;
//                // refund Print error (信用卡)
//                case 8:     if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData(true);
//                            else doPrintFinal();    break;
//                // refund Print error (現金)
//                case 9:     if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData(false);
//                            else doPrintFinal();    break;
//                // refund 印收據
//                case 10:  if(MessageBox.show("", "Print receipt", mContext, "Ok")) printData(false);    break;
//
//
//                case 11: //成功
//                    doPrintFinal();
//                    break;
//            }
//        }
//    };

    private void doPrintFinal(){
        ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
    }

    private void printData(final boolean isCredit){
        mloadingDialog= ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        new Thread() {
            public void run() {
                final PrintAir printer= new PrintAir(mContext,
                    Integer.valueOf(FlightData.SecSeq));
                try{
                    if(fromWhere.equals("Upgrade")){

                        if(isCredit){
                            if(printer.printUpgrade(spinnerReceipt.getSelectedItem().toString(), true)==-1){
                                printerHandler.sendMessage(Tools.createMsg(1));
                            }else{
                                printerHandler.sendMessage(Tools.createMsg(5));
                            }
                        }else{
                            if(printer.printUpgrade(spinnerReceipt.getSelectedItem().toString(), false)==-1){
                                printerHandler.sendMessage(Tools.createMsg(2));
                            }else{
                                printerHandler.sendMessage(Tools.createMsg(11));
                            }
                        }

                    }else if(fromWhere.equals("UpgradeRefund")){
                        if(isCredit){
                            if(printer.printUpgradeRefund(spinnerReceipt.getSelectedItem().toString(), true)==-1){
                                printerHandler.sendMessage(Tools.createMsg(6));
                            }else{
                                printerHandler.sendMessage(Tools.createMsg(101));
                            }
                        }else{
                            if(printer.printUpgradeRefund(spinnerReceipt.getSelectedItem().toString(), false)==-1){
                                printerHandler.sendMessage(Tools.createMsg(7));
                            }else{
                                printerHandler.sendMessage(Tools.createMsg(11));
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                    _TSQL.WriteLog(FlightData.SecSeq,
                        "System", "ReportUpgradeActivity", "printUpgrade", e.getMessage());
                    if(fromWhere.equals("Upgrade")){
                        if(isCredit){
                            printerHandler.sendMessage(Tools.createMsg(3));
                        }else{
                            printerHandler.sendMessage(Tools.createMsg(4));
                        }
                    }else if(fromWhere.equals("UpgradeRefund")){
                        if(isCredit){
                            printerHandler.sendMessage(Tools.createMsg(8));
                        }else{
                            printerHandler.sendMessage(Tools.createMsg(9));
                        }
                    }
                }
            }
        }.start();
    }

    private void enableExpandableList(){
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ExpandableListView expandableListView = findViewById(R.id.expendlist);
        NavigationDrawer navigationDrawer= new NavigationDrawer(mActivity, mContext, drawerLayout, toolbar, expandableListView);
    }

    // =0 item, 1= list
    private void updateButtonColor(int whitchIsClick){
        switch (whitchIsClick){
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