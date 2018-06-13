package tw.com.regalscan.evaground;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.ExpandableListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import tw.com.regalscan.MainActivity;
import tw.com.regalscan.R;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.IMsgBoxOnClick;
import tw.com.regalscan.db02.DBFunctions;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.evaground.Models.RtnObject;
import tw.com.regalscan.evaground.Models.clsMenu;
import tw.com.regalscan.evaground.mvp.ui.activity.OfflineDownloadActivity;
import tw.com.regalscan.utils.PrintGround;

/**
 * Created by tp00175 on 2017/5/11.
 */

public class NavigationDrawer {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ExpandableListView expandableListView;
    private Activity mActivity;

    private String loginMemberNum, loginMemberName;

    private PrintGround mPrintGround;

    private ProgressDialog mloadingDialog;

    //非從menu跳過來
    public NavigationDrawer(Activity a, Context c, DrawerLayout d
            , Toolbar t, ExpandableListView e) {

        EventBus.getDefault().register(this);

        mActivity = a;
        drawerLayout = d;
        toolbar = t;
        expandableListView = e;

        loginMemberNum = RtnObject.getInstance().getEmployeeID();
        if (RtnObject.getInstance().getFullName() != null) {
            loginMemberName = RtnObject.getInstance().getFullName();
        } else {
            loginMemberName = "";
        }

        mPrintGround = new PrintGround(c);

        setDrawer();
    }
    boolean NotOpenTag=false;
    Boolean DownloadTag=false;
    SharedPreferences settings;
    ExpandableListViewAdapter expandableAdapter;
    final List<List<clsMenu>> item_list = new ArrayList<>();
    List<clsMenu> group_list = new ArrayList<>();
    private void setDrawer() {
        StringBuilder errMsg = new StringBuilder();
        DBQuery.CurrentOpenFlightPack openFlightPack = DBQuery.getCurrentOpenFlightList(mActivity, errMsg);
        DBFunctions dbFunctions = new DBFunctions(mActivity, tw.com.regalscan.db.FlightData.SecSeq);



        // 設定toolbar按下去跳出側選單
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                mActivity, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
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

        //String[] menuString;
        clsMenu menu = null;
        if (RtnObject.getInstance().getCompany().equals("EVA")) {
            menu = new clsMenu("Report",true);
            group_list.add(menu);

            menu = new clsMenu("End Inventory",true);
            group_list.add(menu);

            menu = new clsMenu("Functions",true);
            group_list.add(menu);

            menu = new clsMenu( loginMemberNum + " " + loginMemberName,true);
            group_list.add(menu);

            menu = new clsMenu("                    Exit",true);
            group_list.add(menu);

        } else {
            menu = new clsMenu(loginMemberNum,true);
            group_list.add(menu);

            menu = new clsMenu("                    Exit",true);
            group_list.add(menu);

        }

        List<clsMenu> functions = new ArrayList<>();
        if (RtnObject.getInstance().getCompany().equals("EVA")) {
            // functions.add("EGAS Check");
            menu = new clsMenu("EVA Update",true);
            functions.add(menu);
            menu = new clsMenu("Download",true);
            functions.add(menu);
            menu = new clsMenu("Upload",true);
            functions.add(menu);
            menu = new clsMenu("Offline Download",true);
            functions.add(menu);

            //每個group item點開後的item list
            for (int i = 0; i < group_list.size(); i++) {
                if (i == 2) // Functions
                {
                    item_list.add(functions);
                } else {
                    item_list.add(new ArrayList<>());
                }
            }
        } else {
            for (clsMenu aMenuString :  group_list) {
                item_list.add(new ArrayList<>());
            }
        }

        expandableListView.setGroupIndicator(null);
        expandableAdapter = new ExpandableListViewAdapter(mActivity, group_list, item_list);
        expandableListView.setAdapter(expandableAdapter);
        settings = mActivity.getSharedPreferences("PrintTag",0);
        // 父item onClick
        expandableListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {

            //沒有子項目:
            if (item_list.get(groupPosition).isEmpty() && groupPosition != 3 && groupPosition != 4) {
                Intent intent;
                Bundle argument = new Bundle();

                //關掉所有開過的子選單畫面
                ActivityManager.closeAllActivity();

                switch (groupPosition) {
                    case 0:
                        if (RtnObject.getInstance().getCompany().equals("EVA")) {

                            drawerLayout.closeDrawer(Gravity.START);
                            intent = new Intent(mActivity, ReportActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mActivity.startActivity(intent);
                        }
                        break;
                    case 1:
                        if (RtnObject.getInstance().getCompany().equals("EVA")) {
                            if (NotOpenTag){
                                break;
                            }
                            MessageBox.drawerShow("", "Print SCR IN and Lock?", mActivity, "Yes", "No", new IMsgBoxOnClick() {
                                @Override
                                public void onYesClick()
                                {
                                    settings.edit().putBoolean("printSCRIN",true).apply();
                                    EventBus.getDefault().post("SCRIN");
                                    PrintSelect(6);
                                }

                                @Override
                                public void onNoClick() {

                                }
                            });
                        } else {
                            MessageBox.drawerShow("", "Do you want to exit?", mActivity, "Yes", "No", new IMsgBoxOnClick() {
                                @Override
                                public void onYesClick() {
                                    drawerLayout.closeDrawer(Gravity.LEFT);
                                    Intent intent = new Intent(mActivity, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    mActivity.startActivity(intent);
                                    mActivity.finish();
                                }

                                @Override
                                public void onNoClick() {

                                }
                            });
                        }
                        break;
                }
                drawerLayout.closeDrawers();
                return true;

            } else {
                switch (groupPosition) {
                    case 4: //Close
                        MessageBox.drawerShow("", "Do you want to exit?", mActivity, "Yes", "No", new IMsgBoxOnClick() {
                            @Override
                            public void onYesClick() {
                                drawerLayout.closeDrawer(Gravity.LEFT);
                                Intent intent = new Intent(mActivity, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                mActivity.startActivity(intent);
                                mActivity.finish();
                            }

                            @Override
                            public void onNoClick() {

                            }
                        });
                        break;
                }
            }
            return false;
        });

        // 子item onClick
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {

            //關掉所有開過的子選單畫面
            ActivityManager.closeAllActivity();

            //有子項目:
            switch (groupPosition) {
                case 2:
                    if (childPosition == 0) {
                        if (NotOpenTag || settings.getBoolean("printSCRIN", false)){
                            break;
                        }
                        if (RtnObject.getInstance().getCompany().equals("EVA")) {
                            drawerLayout.closeDrawer(Gravity.LEFT);
                            Intent intent = new Intent(mActivity, ECheckUpdateActivity.class);
                            intent.putExtra("User", "EVA");
                            mActivity.startActivity(intent);
                        }
                    } else if (childPosition == 1) {
                        if (RtnObject.getInstance().getCompany().equals("EVA")) {
                            if (!DownloadTag) {
                              break;
                            }
                            drawerLayout.closeDrawer(Gravity.LEFT);
                            Intent intent = new Intent(mActivity, DownloadActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mActivity.startActivity(intent);
                        }
                    } else if (childPosition == 2) {
                        if (RtnObject.getInstance().getCompany().equals("EVA")) {
                            if (NotOpenTag){
                                break;
                            }
                            drawerLayout.closeDrawer(Gravity.LEFT);
                            Intent intent = new Intent(mActivity, UploadActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mActivity.startActivity(intent);
                        }
                    } else if (childPosition == 3) {
                        if (RtnObject.getInstance().getCompany().equals("EVA")) {
                            drawerLayout.closeDrawer(Gravity.LEFT);
                            Intent intent = new Intent(mActivity, OfflineDownloadActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            mActivity.startActivity(intent);
                        }
                    }
                    break;
            }
            parent.collapseGroup(groupPosition);
            drawerLayout.closeDrawers();
            return false;
        });


        if (openFlightPack != null && openFlightPack.openFlights != null) {
            for (int i = 0; i < openFlightPack.openFlights.length; i++) {
                if (openFlightPack.openFlights[i].Status.equals("Closed") && dbFunctions.GetUploadStatus()[i].equals("N")) {
                    DownloadTag = false;
                    break;
                } else if (openFlightPack.openFlights[i].Status.equals("") && dbFunctions.GetUploadStatus()[i].equals("Y")) {
                    DownloadTag = true;
                } else {
                    DownloadTag = true;
                }
            }
        }
        if (!DownloadTag) {
            item_list.get(2).get(1).setEnable(false);
        }

        if (openFlightPack != null && openFlightPack.openFlights != null) {
            for (int i = 0; i < openFlightPack.openFlights.length; i++) {
                if (openFlightPack.openFlights[i].Status.equals("Closed")) {
                    NotOpenTag = false;
                    break;
                } else if (openFlightPack.openFlights[i].Status.equals("")) {
                    NotOpenTag = true;
                } else {
                    NotOpenTag = false;
                    break;
                }
            }
        }

        if(NotOpenTag){
            item_list.get(2).get(0).setEnable(false);
            item_list.get(2).get(2).setEnable(false);
            group_list.get(1).setEnable(false);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String message) {
        if(message.equals("SCRIN")){
            item_list.get(2).get(0).setEnable(false);

            if (DownloadTag) {
                item_list.get(2).get(1).setEnable(false);
            }

            expandableAdapter.notifyDataSetChanged();
        }
        /* Do something */
    }

    //↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓列印用
    public void PrintSelect(int num) {
        mloadingDialog = ProgressDialog.show(mActivity, "", "Processing...", true);
        final Message msg = new Message();
        msg.what = 0; //給予初始值

        switch (num) {
            case 6:
                new Thread() {
                    public void run() {
                        try {
                            while (mPrintGround.printSCRIN() == -1) {
                                //無紙调用Handler
                                msg.what = 6;
                                handle.sendMessage(msg);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //列印錯誤调用Handler
                            msg.what = 119;
                            handle.sendMessage(msg);
                            return;
                        }
                        //結束调用Handler
                        handle.sendMessage(msg);
                    }
                }.start();
                break;
        }

    }


    //Thread內呼叫handle處理UI操2017-09-21 Howard
    Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mloadingDialog.dismiss();
            switch (msg.what) {
                case 6:
                    if (MessageBox.show("", "No paper, reprint?", mActivity, "Yes", "No")) {
                        PrintSelect(msg.what);
                    }
                    break;
                case 119:
                    MessageBox.show("", "Print error", mActivity, "Return");
                    break;
            }
        }
    };
    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑列印用


}
