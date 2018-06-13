package tw.com.regalscan.evaground;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import tw.com.regalscan.R;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.evaground.Models.RtnObject;
import tw.com.regalscan.utils.PrintGround;

import static tw.com.regalscan.evaground.ECheckUpdateActivity.toolbar;

/**
 * Created by rguser on 2017/3/5.
 */

public class ReportActivity extends AppCompatActivity {

    private Button mSCROutCode, mSCROutDraw, mDescrepancy, mReloadSheet, mDrawQty,
        mSCRIn, mReturn;

    private TextView mDate, mFlightNo, mVipOrder, mPreOrder, mCart;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private PrintGround mPrintGround = new PrintGround(ReportActivity.this);

    private Context mContext;

    private ProgressDialog mloadingDialog;
    public Activity mActivity;

    private void enableExpandableList() {

        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ExpandableListView expandableListView = findViewById(R.id.expendlist);

        new NavigationDrawer(this, this, drawerLayout, toolbar, expandableListView);
    }

    private void InitializeComponent() {

        mSCROutCode = findViewById(R.id.btnSCROutCode);
        mSCROutCode.setOnClickListener(reportBtnOnClick);
        mSCROutDraw = findViewById(R.id.btnSCROutDraw);
        mSCROutDraw.setOnClickListener(reportBtnOnClick);
        mDescrepancy = findViewById(R.id.btnDiscrepancy);
        mDescrepancy.setOnClickListener(reportBtnOnClick);
        mReloadSheet = findViewById(R.id.btnReloadSheet);
        mReloadSheet.setOnClickListener(reportBtnOnClick);
        mDrawQty = findViewById(R.id.btnDrawQty);
        mDrawQty.setOnClickListener(reportBtnOnClick);
        mSCRIn = findViewById(R.id.btnSCRIn);
        mSCRIn.setOnClickListener(reportBtnOnClick);
        mReturn = findViewById(R.id.btnReturn);
        mReturn.setOnClickListener(reportBtnOnClick);

        mDate = findViewById(R.id.txtDateNow);
        mFlightNo = findViewById(R.id.txtFlightNum);
        mCart = findViewById(R.id.tv_CartNo);
        mPreOrder = findViewById(R.id.txtPreorder);
        mVipOrder = findViewById(R.id.txtVip);

        mContext = ReportActivity.this;
        mActivity = this;

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 設定側邊選單抽屜
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        enableExpandableList();

        // 設定toolbar按下去跳出側選單
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if (!RtnObject.getInstance().getCompany().equals("EVA")) {
            mDescrepancy.setEnabled(false);
            mSCRIn.setEnabled(false);
            mSCROutCode.setEnabled(false);
            mSCROutDraw.setEnabled(false);
            mReloadSheet.setEnabled(false);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//    getWindow().setEnterTransition(new Fade());
//    getWindow().setExitTransition(new Fade());
        setContentView(R.layout.activity_report_ground);

        InitializeComponent();

        StringBuilder err = new StringBuilder();
        final DBQuery.FlightInfoPack flightInfo = DBQuery.getFlightInfo(this, err);
        final DBQuery.BasicSalesInfo saleInfo = DBQuery.checkBasicSalesInfoIsReady(this, err);

        if (flightInfo != null) {
            mDate.setText(flightInfo.flights[0].FlightDate);
            mFlightNo.setText(flightInfo.flights[0].FlightNo);
            mCart.setText(flightInfo.flights[0].CarNo);
        }

        if (saleInfo == null) {
            MessageBox.show("", "Get basic sales info error", mContext, "YES");
        } else {
            if (String.valueOf(saleInfo.PreorderCount).equals("0")) {
                mPreOrder.setText("");
            } else {
                mPreOrder.setText("PreOrder：" + String.valueOf(saleInfo.PreorderCount));
            }
            if (String.valueOf(saleInfo.VIPCount).equals("0")) {
                mVipOrder.setText("");
            } else {
                mVipOrder.setText("VIP：" + String.valueOf(saleInfo.VIPCount));
            }
        }
    }

    private View.OnClickListener reportBtnOnClick = new View.OnClickListener() {
        Intent intent = new Intent();

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnSCROutCode:
                    if (MessageBox.show("", "Print SCR Out ?", mActivity, "Yes", "No"))
                        PrintSelect(1);
                    break;
                case R.id.btnSCROutDraw:
                    if (MessageBox.show("", "Print SCR Out ?", mActivity, "Yes", "No"))
                        PrintSelect(2);
                    break;
                case R.id.btnDiscrepancy:
                    if (MessageBox.show("", "Print Discrepancy ?", mActivity, "Yes", "No"))
                        PrintSelect(3);
                    break;
                case R.id.btnReloadSheet:
                    if (MessageBox.show("", "Print Reload list ?", mActivity, "Yes", "No"))
                        PrintSelect(4);
                    break;
                case R.id.btnDrawQty:
                    if (MessageBox.show("", "Print Draw Qty ?", mActivity, "Yes", "No"))
                        PrintSelect(5);
                    break;
                case R.id.btnSCRIn:
                    if (MessageBox.show("", "Print SCR IN and Lock?", mActivity, "Yes", "No"))
                        PrintSelect(6);
                    break;
                case R.id.btnReturn:
                    Intent intent = new Intent(mContext, MenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ReportActivity.this).toBundle());
                    break;
            }
        }
    };


    public void PrintSelect(int num) {
        mloadingDialog = ProgressDialog.show(mActivity, "", "Processing...", true);
        final Message msg = new Message();
        msg.what = 0; //給予初始值

        switch (num) {
            case 1:
                new Thread() {
                    public void run() {
                        try {
                            while (mPrintGround.printSCROUT(false) == -1) {
                                //無紙调用Handler
                                msg.what = 1;
                                printerHandler.sendMessage(msg);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //列印錯誤调用Handler
                            msg.what = 119;
                            printerHandler.sendMessage(msg);
                            return;
                        }
                        //結束调用Handler取消ProgressDialog
                        printerHandler.sendMessage(msg);
                    }
                }.start();
                break;
            case 2:
                new Thread() {
                    public void run() {
                        try {
                            while (mPrintGround.printSCROUT(true) == -1) {
                                //無紙调用Handler
                                msg.what = 2;
                                printerHandler.sendMessage(msg);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //列印錯誤调用Handler
                            msg.what = 119;
                            printerHandler.sendMessage(msg);
                            return;
                        }
                        //結束调用Handler取消ProgressDialog
                        printerHandler.sendMessage(msg);

                    }
                }.start();
                break;
            case 3:
                new Thread() {
                    public void run() {
                        try {
                            while (mPrintGround.printDiscrep() == -1) {
                                //無紙调用Handler
                                msg.what = 3;
                                printerHandler.sendMessage(msg);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //列印錯誤调用Handler
                            msg.what = 119;
                            printerHandler.sendMessage(msg);
                            return;
                        }
                        //結束调用Handler取消ProgressDialog
                        printerHandler.sendMessage(msg);
                    }
                }.start();
                break;
            case 4:
                new Thread() {
                    public void run() {
                        try {
                            while (mPrintGround.printReloadQty() == -1) {
                                //無紙调用Handler
                                msg.what = 4;
                                printerHandler.sendMessage(msg);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //列印錯誤调用Handler
                            msg.what = 119;
                            printerHandler.sendMessage(msg);
                            return;
                        }
                        //結束调用Handler取消ProgressDialog
                        printerHandler.sendMessage(msg);
                    }
                }.start();
                break;
            case 5:
                new Thread() {
                    public void run() {
                        try {
                            while (mPrintGround.printDrawQty() == -1) {
                                //無紙调用Handler
                                msg.what = 5;
                                printerHandler.sendMessage(msg);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //列印錯誤调用Handler
                            msg.what = 119;
                            printerHandler.sendMessage(msg);
                            return;
                        }
                        //結束调用Handler取消ProgressDialog
                        printerHandler.sendMessage(msg);
                    }
                }.start();
                break;
            case 6:
                new Thread() {
                    public void run() {
                        try {
                            while (mPrintGround.printSCRIN() == -1) {
                                //無紙调用Handler
                                msg.what = 6;
                                printerHandler.sendMessage(msg);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //列印錯誤调用Handler
                            msg.what = 119;
                            printerHandler.sendMessage(msg);
                            return;
                        }
                        //結束调用Handler取消ProgressDialog
                        printerHandler.sendMessage(msg);
                    }
                }.start();
                break;
        }

    }


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
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No")) {
                        handlerActivity.PrintSelect(msg.what);
                    }
                    break;
                case 119:
                    MessageBox.show("", "Print error", handlerContext, "Return");
                    break;
            }

        }
    }

    private Handler printerHandler = new PrinterHandler(this);

    //Thread內呼叫handle處理UI操2017-09-21 Howard
//  Handler handle = new Handler() {
//    @Override
//    public void handleMessage(Message msg) {
//      super.handleMessage(msg);
//      mloadingDialog.dismiss();
//      switch (msg.what){
//        case 1:case 2:case 3:case 4:case 5:case 6:
//          if (MessageBox.show("", "No paper, reprint?", mActivity, "Yes", "No")) {
//            PrintSelect(msg.what);
//          }
//          break;
//        case 119:
//          MessageBox.show("", "Print error", mActivity, "Return");
//          break;
//      }
//    }
//  };


    @Override
    public void onBackPressed() {
    }
}
