package tw.com.regalscan.evaground;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import org.greenrobot.eventbus.EventBus;

import tw.com.regalscan.BuildConfig;
import tw.com.regalscan.R;
import tw.com.regalscan.db02.DBFunctions;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.evaground.Models.RtnObject;
import tw.com.regalscan.utils.PrintGround;

public class MenuActivity extends AppCompatActivity {

    private LinearLayout ll_in_sv;

    private HashMap<Integer, String> hashMapAnnounce;
    private HashMap<Integer, String> hashMapConent;

    private TextView mFlightNo, mDate, mCart, mVipOrder, mPreOrder, mFlightInfo;

    private ImageView mImageView;

    private PrintGround mPrintGround;

    private Activity mActivity;

    private Context mContext;

    private ProgressDialog mloadingDialog;
    SharedPreferences settings;
    private final StringBuilder errMsg = new StringBuilder();

    Button mDownload,discrepancy,scrIn,checkUpdate,evaUpdate,upload;

    private void InitializeComponent() {
        settings = getSharedPreferences("PrintTag",0);
        hashMapAnnounce = new HashMap<>();
        hashMapConent = new HashMap<>();
        ll_in_sv = findViewById(R.id.ll_in_sv);

        if (RtnObject.getInstance().getNewsInfo() != null) {
            addListView();
        }

        discrepancy = findViewById(R.id.btn_Discrepancy);
        discrepancy.setOnClickListener(functionBtnOnclick);

        scrIn = findViewById(R.id.btn_ScrIn);
        scrIn.setOnClickListener(functionBtnOnclick);

        if (RtnObject.getInstance().getCompany().equals("EVA")) {
            evaUpdate = findViewById(R.id.btnEvaUpdate);
            evaUpdate.setOnClickListener(functionBtnOnclick);
            checkUpdate = findViewById(R.id.btnCheckUpdate);
            checkUpdate.setOnClickListener(functionBtnOnclick);
        } else {
            checkUpdate = findViewById(R.id.btnCheckUpdate);
            checkUpdate.setOnClickListener(functionBtnOnclick);
            evaUpdate = findViewById(R.id.btnEvaUpdate);
        }

        mDownload = findViewById(R.id.btnDownload);
        mDownload.setOnClickListener(functionBtnOnclick);

        upload = findViewById(R.id.btnUpload);
        upload.setOnClickListener(functionBtnOnclick);

        mDate = findViewById(R.id.tv_date);
        mFlightNo = findViewById(R.id.tv_FlightNo);
        mCart = findViewById(R.id.tv_CartNo);
        mPreOrder = findViewById(R.id.tv_Preorder);
        mVipOrder = findViewById(R.id.tv_Vip);
        mFlightInfo = findViewById(R.id.tv_FlightInfo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // 設定側邊選單抽屜
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        enableExpandableList();

        mPrintGround = new PrintGround(this);

        mActivity = this;
        mContext = this;

        if (!RtnObject.getInstance().getCompany().equals("EVA")) {
            evaUpdate.setText("Draw Qty");
            evaUpdate.setOnClickListener(functionBtnOnclick);
            discrepancy.setEnabled(false);
            discrepancy.setOnClickListener(null);
            scrIn.setEnabled(false);
            scrIn.setOnClickListener(null);
            upload.setEnabled(false);
            upload.setOnClickListener(null);
            mDownload.setEnabled(false);
            mDownload.setOnClickListener(null);
        } else {
            checkUpdate.setEnabled(true);
            checkUpdate.setText("Reload Sheet");
            checkUpdate.setOnClickListener(functionBtnOnclick);
        }

        //列印SCR IN 後，只能留1.SCR OUT  2.DRAW QTY  3.Download  4.Upload 其餘功能反白。
        if(settings.getBoolean("printSCRIN",false)){
            discrepancy.setEnabled(false);
            evaUpdate.setEnabled(false);
            checkUpdate.setEnabled(false);
        }

        mImageView = findViewById(R.id.imgV_uploadIcon);
    }

    private void enableExpandableList() {

        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ExpandableListView expandableListView = findViewById(R.id.expendlist);

        new NavigationDrawer(this, this, drawerLayout, toolbar, expandableListView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        InitializeComponent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        refreshData();
    }

    //加入公告
    private void addListView() {
        ll_in_sv.removeAllViews();

        int size = RtnObject.getInstance().getNewsInfo().size();

        for (int i = 0; i < size; i++) {

            String title = RtnObject.getInstance().getNewsInfo().get(i).getTitle();
            String content = RtnObject.getInstance().getNewsInfo().get(i).getContent();

            @SuppressLint("InflateParams") View view = LayoutInflater.from(MenuActivity.this).inflate(R.layout.item_announcement_view, null); //物件來源
            RelativeLayout rl = view.findViewById(R.id.rl); //取得personal_object中LinearLayout

            TextView mtxtTiltle = rl.findViewById(R.id.txtTitle);
            mtxtTiltle.setText(title);

            Button open = rl.findViewById(R.id.btnOpen);

            //將上面新建的例元件新增到主頁面的ll_in_sv中
            ll_in_sv.addView(view);
            hashMapAnnounce.put(i, mtxtTiltle.getText().toString());
            hashMapConent.put(i, content);
            open.setOnClickListener(functionViewOpen);
            open.setId(i);
        }
    }

    //公告按鈕點擊事件
    private final OnClickListener functionViewOpen = new OnClickListener() {
        final Intent intent = new Intent();

        @Override
        public void onClick(View v) {
            Button clickBtn = (Button)v;
            int key = clickBtn.getId();
            String title = hashMapAnnounce.get(key);
            String content = hashMapConent.get(key);

            intent.putExtra("announcementTitle", title);
            intent.putExtra("content", content);
            intent.setClass(MenuActivity.this, AnnouncementActivity.class);
            startActivity(intent);
        }
    };

    private final OnClickListener functionBtnOnclick = new OnClickListener() {
        final Intent intent = new Intent();

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_Discrepancy:
                    if (MessageBox.show("", "Print Discrepancy ?", mActivity, "Yes", "No"))
                        PrintSelect(3);
                    break;
                case R.id.btn_ScrIn:
                    if (MessageBox.show("", "Print SCR IN and Lock?", mActivity, "Yes", "No")){
                        settings.edit().putBoolean("printSCRIN",true).apply();
                        //列印SCR IN 後，只能留1.SCR OUT  2.DRAW QTY  3.Download  4.Upload 其餘功能反白。
                        if(settings.getBoolean("printSCRIN",false)){
                            discrepancy.setEnabled(false);
                            evaUpdate.setEnabled(false);
                            checkUpdate.setEnabled(false);
                        }
                        EventBus.getDefault().post("SCRIN");
                        PrintSelect(6);
                    }
                    break;
                case R.id.btnEvaUpdate:
                    if (RtnObject.getInstance().getCompany().equals("EVA")) {
                        intent.setClass(MenuActivity.this, ECheckUpdateActivity.class);
                        intent.putExtra("User", "EVA");
                        startActivity(intent);
                    } else {
                        if (MessageBox.show("", "Print Draw Qty ?", mActivity, "Yes", "No"))
                            PrintSelect(5);
                    }
                    break;
                case R.id.btnCheckUpdate:
                    if(!RtnObject.getInstance().getCompany().equals("EVA")){
                        intent.setClass(MenuActivity.this, ECheckUpdateActivity.class);
                        intent.putExtra("User", "EGAS");
                        startActivity(intent);
                    }else {
                       //EVA Reload報表
                        if (MessageBox.show("", "Print Reload list ?", mActivity, "Yes", "No"))
                            PrintSelect(4);
                        break;
                    }

                    break;
                case R.id.btnDownload:
                    intent.setClass(MenuActivity.this, DownloadActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btnUpload:
                    intent.setClass(MenuActivity.this, UploadActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

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

    private void refreshData() {
        mFlightInfo.setText("");
        StringBuilder err = new StringBuilder();
        final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Loading...", true, false);
        new Thread() {
            public void run() {
                final DBQuery.FlightInfoPack flightInfo = DBQuery.getFlightInfo(mContext, err);
                final DBQuery.BasicSalesInfo saleInfo = DBQuery.checkBasicSalesInfoIsReady(mContext, err);
                DBQuery.CurrentOpenFlightPack openFlightPack = DBQuery.getCurrentOpenFlightList(mContext, errMsg);
                DBFunctions dbFunctions = new DBFunctions(mContext, tw.com.regalscan.db.FlightData.SecSeq);

                MenuActivity.this.runOnUiThread(() -> {
                    if (flightInfo != null && saleInfo != null) {
                        mDate.setText(flightInfo.flights[0].FlightDate);
                        mFlightNo.setText(flightInfo.flights[0].FlightNo);
                        mCart.setText(flightInfo.flights[0].CarNo);

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

                        for (int i = 0; i < flightInfo.flights.length; i++) {
                            //航段編號
                            mFlightInfo.append(
                                    flightInfo.flights[i].DepStn + " - " + flightInfo.flights[i].ArivStn + " CA：" + flightInfo.flights[i].CrewID
                            );

                            if (i != flightInfo.flights.length - 1) {
                                mFlightInfo.append("\n");
                            }
                        }


                        if (RtnObject.getInstance().getCompany().equals("EVA") && openFlightPack != null && openFlightPack.openFlights != null) {
                            for (int i = 0; i < openFlightPack.openFlights.length; i++) {
                                if (openFlightPack.openFlights[i].Status.equals("Closed") && dbFunctions.GetUploadStatus()[i].equals("N")) {
                                    mImageView.setVisibility(View.VISIBLE);
                                    mDownload.setEnabled(false);
                                    break;
                                } else if (openFlightPack.openFlights[i].Status.equals("") && dbFunctions.GetUploadStatus()[i].equals("Y")) {
                                    mImageView.setVisibility(View.VISIBLE);
                                    mDownload.setEnabled(true);
                                } else {
                                    mImageView.setVisibility(View.INVISIBLE);
                                    mDownload.setEnabled(true);
                                }

                                //尚未開櫃- 可開櫃狀態
                                if(openFlightPack.openFlights[i].Status.equals("")){
                                    mDownload.setEnabled(false);
                                    discrepancy.setEnabled(false);
                                    scrIn.setEnabled(false);
                                    checkUpdate.setEnabled(false);
                                    evaUpdate.setEnabled(false);
                                    upload.setEnabled(false);
                                    mDownload.setEnabled(true);
                                }
                            }
                        }
                    }
                    mloadingDialog.dismiss();
                });
            }}.start();
    }

    //↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓列印用
    private void PrintSelect(int num) {
        mloadingDialog = ProgressDialog.show(mActivity, "", "Processing...", true);
        final Message msg = new Message();
        msg.what = 0; //給予初始值

        switch (num) {
            case 3:
                new Thread() {
                    public void run() {
                        try {
                            //noinspection LoopStatementThatDoesntLoop
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
            case 6:
                new Thread() {
                    public void run() {
                        try {
                            //noinspection LoopStatementThatDoesntLoop
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
        private final WeakReference<MenuActivity> weakActivity;

        PrinterHandler(MenuActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            MenuActivity handlerActivity = weakActivity.get();

            handlerActivity.mloadingDialog.dismiss();
            switch (msg.what) {
                case 3:
                case 4:
                case 5:
                case 6:
                    if (MessageBox.show("", "No paper, reprint?", handlerActivity, "Yes", "No")) {
                        handlerActivity.PrintSelect(msg.what);
                    }
                    break;
                case 119:
                    MessageBox.show("", "Print error", handlerActivity, "Return");
                    break;
            }
        }
    }

    private final Handler printerHandler = new PrinterHandler(this);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            EventBus.getDefault().unregister(this);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //列印SCR IN 後，只能留1.SCR OUT  2.DRAW QTY  3.Download  4.Upload 其餘功能反白。
        if(settings.getBoolean("printSCRIN",false)){
            discrepancy.setEnabled(false);
            evaUpdate.setEnabled(false);
            checkUpdate.setEnabled(false);
        }
    }

    //Thread內呼叫handle處理UI操2017-09-21 Howard
//  Handler handle = new Handler() {
//    @Override
//    public void handleMessage(Message msg) {
//      super.handleMessage(msg);
//      mloadingDialog.dismiss();
//      switch (msg.what){
//        case 3:case 6:
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
    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑列印用
}
