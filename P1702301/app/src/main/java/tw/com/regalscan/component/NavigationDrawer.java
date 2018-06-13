package tw.com.regalscan.component;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;

import aero.panasonic.inflight.crew.services.ordermanagement.v1.CrewOrder;
import aero.panasonic.inflight.crew.services.ordermanagement.v1.OrderStatus;
import com.google.gson.Gson;
import com.regalscan.sqlitelibrary.TSQL;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tw.com.regalscan.MainActivity;
import tw.com.regalscan.R;
import tw.com.regalscan.activities.CPCheckActivity;
import tw.com.regalscan.adapters.ExpandableListViewAdapter;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.PreorderInfoPack;
import tw.com.regalscan.db02.DBQuery.TransferItemPack;
import tw.com.regalscan.evaair.*;
import tw.com.regalscan.evaair.basket.BasketActivity;
import tw.com.regalscan.evaair.ife.*;
import tw.com.regalscan.evaair.preorder.PreorderSaleActivity;
import tw.com.regalscan.evaair.preorder.VipPaidActivity;
import tw.com.regalscan.evaair.preorder.VipSaleActivity;
import tw.com.regalscan.evaair.report.ReportActivity;
import tw.com.regalscan.evaair.transfer.CancelTransferOutActivity;
import tw.com.regalscan.evaair.transfer.TransferInActivity;
import tw.com.regalscan.evaair.transfer.TransferOutActivity01;
import tw.com.regalscan.evaair.upgrade.UpgradeBasketActivity;
import tw.com.regalscan.utils.Cursor;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;


public class NavigationDrawer implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ExpandableListView expandableListView;
    private Context activityContext;
    private Activity mActivity;
    private String loginMemberNum, loginMemberName;
    private ProgressDialog mloadingDialog;
    private String mFlightNo, mSector;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private List<String> group_list = new ArrayList<String>();
    private List<List<String>> item_list = new ArrayList<List<String>>();
    private Intent intent = new Intent();
    private Bundle argument = new Bundle();
    private IFEDBFunction mIFEDBFunction;

    //非從menu跳過來
    public NavigationDrawer(Activity a, Context c, DrawerLayout d, Toolbar t, ExpandableListView e) {
        mActivity = a;
        activityContext = c;
        drawerLayout = d;
        toolbar = t;
        expandableListView = e;

//        loginMemberNum= FlightInfoManager.getInstance().getLoginType() + FlightInfoManager.getInstance().getLoginNumber();
        loginMemberNum = FlightData.CrewID; // CA ID
        loginMemberName = FlightInfoManager.getInstance().getCAName(); // CA Name
        mFlightNo = FlightData.FlightNo;
        mSector = FlightData.Sector;
        mIFEDBFunction = new IFEDBFunction(c, FlightData.SecSeq);

//        setDrawer();
        initDrawer();
    }

    private void initDrawer() {

        prepareListData();

        expandableListView.setAdapter(new ExpandableListViewAdapter(activityContext, group_list, item_list));
        expandableListView.setOnGroupClickListener(this);
        expandableListView.setOnChildClickListener(this);
        expandableListView.setGroupIndicator(null);

        actionBarDrawerToggle = new ActionBarDrawerToggle(mActivity, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we don't want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we don't want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    private void prepareListData() {

        String[] menuString = {
            "Update", "Sale", "Refund", "Pre-order", "Transfer", "Report",
            "Wi-Fi", "Damage", "Upgrade", "VIP Paid", "VIP Sale", "Catalog", "Picture",
            "Close", mFlightNo + " - " + mSector, loginMemberNum + " " + loginMemberName};

        // group list的內容
        for (String s : menuString) {
            group_list.add(s);
        }

        // 父item的子item list
        List<String> transferList = new ArrayList<String>();
        transferList.add("Transfer Out");
        transferList.add("Transfer In");
        transferList.add("Cancel Transfer Out");

        List<String> WifiList = new ArrayList<String>();
        WifiList.add("77M/A333");
        WifiList.add("77A/77B");
        WifiList.add("787");
        WifiList.add("Order List");
        WifiList.add("Deal in Progress");

        List<String> upgradeList = new ArrayList<String>();
        upgradeList.add("Upgrade");
        upgradeList.add("Upgrade Refund");

        //每個group item點開後的item list
        for (int i = 0; i < menuString.length; i++) {
            if (i == 4) // Transfer
            {
                item_list.add(transferList);
            } else if (i == 6) //Wifi
            {
                item_list.add(WifiList);
            } else if (i == 8) //Upgrade
            {
                item_list.add(upgradeList);
            } else {
                item_list.add(new ArrayList<>());
            }
        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        StringBuilder err = new StringBuilder();

        // 有子選單者要在選擇子選單時再跳出小視窗
        if (groupPosition != 4 && groupPosition != 6 && groupPosition != 8) {
            // 如果在Damage頁面，且有修改過Damage物品沒儲存
            if (ActivityManager.isActivityInStock("DamageActivity") && DamageActivity.isDamageListModified()) {
                MessageBox.drawerShow("", "Press save!", mActivity, "Return", new IMsgBoxOnClick() {
                    @Override
                    public void onYesClick() {
                        drawerLayout.closeDrawers();
                    }

                    @Override
                    public void onNoClick() {

                    }
                });

                return true;
            }

            // 確認是否已經經過商品銷售和移儲
            if (groupPosition == 0 && (!DBQuery.checkCurrentFlightCanUpdate(activityContext, err) || mIFEDBFunction.chkPushInventory(FlightData.SecSeq))) {
                MessageBox.drawerShow("", "You can't update after selling, transferring or initializing", mActivity, "Return", new IMsgBoxOnClick() {
                    @Override
                    public void onYesClick() {
                        drawerLayout.closeDrawers();
                    }

                    @Override
                    public void onNoClick() {

                    }
                });
                return true;
            }
        }

        // 沒有子選單，且不是關櫃項目和登入者工號項目
        if (item_list.get(groupPosition).isEmpty() && groupPosition != 13 && groupPosition != 14 && groupPosition != 15) {

            Gson gson = new Gson();

            switch (groupPosition) {
                case 0: //update
                    intent = new Intent(mActivity, UpdateActivity.class);
                    mActivity.startActivity(intent);
                    break;

                case 1: //sale
                    if (DBQuery.checkCurrentFlightCanUpdate(activityContext, err) && !mIFEDBFunction.chkPushInventory(FlightData.SecSeq)) {
                        MessageBox.drawerShow("", "Don't forget to check inventory!", mActivity, "Ok", new IMsgBoxOnClick() {
                            @Override
                            public void onYesClick() {
                                argument.putString("FromWhere", "Menu");
                                if (FlightData.IFEConnectionStatus) {
                                    intent = new Intent(mActivity, OnlineBasketActivity.class);
                                } else {
                                    intent = new Intent(mActivity, BasketActivity.class);
                                }
                                intent.putExtras(argument);
                                mActivity.startActivity(intent);
                                ActivityManager.closeAllActivity();
                                drawerLayout.closeDrawers();
                            }

                            @Override
                            public void onNoClick() {

                            }
                        });
                    } else {
                        argument.putString("FromWhere", "Menu");
                        if (FlightData.IFEConnectionStatus) {
                            intent = new Intent(mActivity, OnlineBasketActivity.class);
                        } else {
                            intent = new Intent(mActivity, BasketActivity.class);
                        }
                        intent.putExtras(argument);
                        mActivity.startActivity(intent);
                        ActivityManager.closeAllActivity();
                        drawerLayout.closeDrawers();
                    }
                    break;

                case 2: //refund
                    DBQuery.ReceiptList receiptNoList = DBQuery.getAllRceciptNoList(activityContext, err, "Sale", false);
                    if (receiptNoList == null) {
                        MessageBox.drawerShow("", "Query order data error", mActivity, "Return", new IMsgBoxOnClick() {
                            @Override
                            public void onYesClick() {
                                drawerLayout.closeDrawers();
                            }

                            @Override
                            public void onNoClick() {

                            }
                        });
                        return true;
                    } else {
                        if (receiptNoList.rececipts == null) {
                            MessageBox.drawerShow("", "No receipt can refund", mActivity, "Ok", new IMsgBoxOnClick() {
                                @Override
                                public void onYesClick() {
                                    drawerLayout.closeDrawers();
                                }

                                @Override
                                public void onNoClick() {

                                }
                            });
                            return true;
                        } else {
                            String jsonPack = gson.toJson(receiptNoList);
                            argument.putString("jsonPack", jsonPack);
                            intent = new Intent(mActivity, RefundActivity.class);
                            intent.putExtras(argument);
                            mActivity.startActivity(intent);
                        }
                    }


                    // 轉json傳入
//          String jsonPack = gson.toJson(receiptNoList);
//          argument.putString("jsonPack", jsonPack);
//          intent = new Intent(mActivity, RefundActivity.class);
//          intent.putExtras(argument);
                    break;

                case 3: //pre-order
                    PreorderInfoPack preorderPack = DBQuery.getPRVPCanSaleRefund(activityContext, err, FlightData.SecSeq, null, new String[]{"PR"}, "N");
                    if (preorderPack == null) {
                        MessageBox.drawerShow("", "Query pre-order data error", mActivity, "Return", new IMsgBoxOnClick() {
                            @Override
                            public void onYesClick() {

                                drawerLayout.closeDrawers();
                            }

                            @Override
                            public void onNoClick() {

                            }
                        });
                        return true;
                    } else {
                        if (preorderPack.info == null) {
                            MessageBox.drawerShow("", "No pre-order sale list", mActivity, "Ok", new IMsgBoxOnClick() {
                                @Override
                                public void onYesClick() {

                                    drawerLayout.closeDrawers();
                                }

                                @Override
                                public void onNoClick() {

                                }
                            });
                            return true;
                        } else {
                            String jsonPack;
                            jsonPack = gson.toJson(preorderPack);
                            argument.putString("jsonPack", jsonPack);
                            intent = new Intent(mActivity, PreorderSaleActivity.class);
                            intent.putExtras(argument);
                            mActivity.startActivity(intent);
                        }
                    }


                    // 轉json傳入
//          String jsonPack = "";
//          jsonPack = gson.toJson(preorderPack);
//          argument.putString("jsonPack", jsonPack);
//          intent = new Intent(mActivity, PreorderSaleActivity.class);
//          intent.putExtras(argument);
                    break;

                case 5: //Report
                    intent = new Intent(mActivity, ReportActivity.class);
                    mActivity.startActivity(intent);
                    break;

                case 7: //Damage
                    intent = new Intent(mActivity, DamageActivity.class);
                    mActivity.startActivity(intent);
                    break;

                case 9: //VIP Paid
                    PreorderInfoPack vipPaidPack = DBQuery.getPRVPCanSaleRefund(activityContext, err,
                        FlightData.SecSeq, null, new String[]{"VP"}, "N");
                    if (vipPaidPack == null) {
                        MessageBox.drawerShow("", "Query VIP paid data error", mActivity, "Return", new IMsgBoxOnClick() {
                            @Override
                            public void onYesClick() {
                                drawerLayout.closeDrawers();
                            }

                            @Override
                            public void onNoClick() {

                            }
                        });
                        return true;
                    } else {
                        if (vipPaidPack.info == null) {
                            MessageBox.drawerShow("", "No VIP paid list", mActivity, "Ok", new IMsgBoxOnClick() {
                                @Override
                                public void onYesClick() {
                                    drawerLayout.closeDrawers();
                                }

                                @Override
                                public void onNoClick() {

                                }
                            });
                            return true;
                        } else {
                            String jsonPack = gson.toJson(vipPaidPack);
                            argument.putString("jsonPack", jsonPack);
                            intent = new Intent(mActivity, VipPaidActivity.class);
                            intent.putExtras(argument);
                            mActivity.startActivity(intent);
                        }
                    }

                    // 轉json傳入
//          jsonPack = gson.toJson(vipPaidPack);
//          argument.putString("jsonPack", jsonPack);
//          intent = new Intent(mActivity, VipPaidActivity.class);
//          intent.putExtras(argument);
                    break;

                case 10: //VIP Sale
                    PreorderInfoPack vipSalePack = DBQuery.getPRVPCanSaleRefund(activityContext, err, FlightData.SecSeq, null, new String[]{"VS"}, "N");
                    if (vipSalePack == null) {
                        MessageBox.drawerShow("", "Query Vip sale data error", mActivity, "Return", new IMsgBoxOnClick() {
                            @Override
                            public void onYesClick() {

                                drawerLayout.closeDrawers();
                            }

                            @Override
                            public void onNoClick() {

                            }
                        });
                        return true;
                    } else {
                        if (vipSalePack.info == null) {
                            MessageBox.drawerShow("", "No VIP sale list", mActivity, "Ok", new IMsgBoxOnClick() {
                                @Override
                                public void onYesClick() {
                                    drawerLayout.closeDrawers();
                                }

                                @Override
                                public void onNoClick() {

                                }
                            });
                            return true;
                        } else {
                            String jsonPack = gson.toJson(vipSalePack);
                            argument.putString("jsonPack", jsonPack);
                            intent = new Intent(mActivity, VipSaleActivity.class);
                            intent.putExtras(argument);
                            mActivity.startActivity(intent);
                        }
                    }


                    // 轉json傳入
//          String jsonPack = gson.toJson(vipSalePack);
//          argument.putString("jsonPack", jsonPack);
//          intent = new Intent(mActivity, VipSaleActivity.class);
//          intent.putExtras(argument);
                    break;

                case 11: //Catalog
                    intent = new Intent(mActivity, CatalogActivity.class);
                    mActivity.startActivity(intent);
                    break;

                case 12: //Picture
                    intent = new Intent(mActivity, PictureActivity.class);
                    mActivity.startActivity(intent);
                    break;
            }

            if (groupPosition != 1) {
                // 關掉所有開過的子選單畫面
                ActivityManager.closeAllActivity();
                drawerLayout.closeDrawers();
            }
            return true;
        }
        // 關櫃
        else {
            switch (groupPosition) {
                case 13:
                    if (FlightData.IFEConnectionStatus) {
                        IFEFunction ifeFunction = new IFEFunction(mActivity);
                        ifeFunction.getOrders("", OrderStatus.ORDER_STATUS_OPEN)
                            .subscribeOn(Schedulers.io())
                            .doOnSubscribe(disposable -> Cursor.Busy(mActivity.getString(R.string.Processing_Msg), mActivity))
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally(Cursor::Normal)
                            .subscribe(ifeReturnData -> {
                                if (ifeReturnData.isSuccess()) {
                                    List<CrewOrder> crewOrders = (List<CrewOrder>)ifeReturnData.getData();
                                    if (crewOrders.size() > 0) {
                                        MessageBox.drawerShow("", "You have " + String.valueOf(crewOrders.size()) + " order(s)\r\nDo you want to close?", mActivity, "Yes", "No", new IMsgBoxOnClick() {
                                            @Override
                                            public void onYesClick() {
                                                printData(true);
                                            }

                                            @Override
                                            public void onNoClick() {

                                            }
                                        });
                                    } else {
                                        MessageBox.drawerShow("", "Do you want to close?", mActivity, "Yes", "No", new IMsgBoxOnClick() {
                                            @Override
                                            public void onYesClick() {
                                                printData(true);
                                            }

                                            @Override
                                            public void onNoClick() {
                                                drawerLayout.closeDrawers();
                                            }
                                        });
                                    }
                                }
                            });
                    } else {
                        MessageBox.drawerShow("", "Do you want to close?", mActivity, "Yes", "No", new IMsgBoxOnClick() {
                            @Override
                            public void onYesClick() {
                                printData(true);
                            }

                            @Override
                            public void onNoClick() {
                                drawerLayout.closeDrawers();
                            }
                        });
                    }
                    break;
            }
        }
        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

        if (ActivityManager.isActivityInStock("DamageActivity") && DamageActivity.isDamageListModified()) {
            drawerLayout.closeDrawers();
            MessageBox.drawerShow("", "Press save!", mActivity, "Return", new IMsgBoxOnClick() {
                @Override
                public void onYesClick() {

                }

                @Override
                public void onNoClick() {

                }
            });

            return false;
        }

        StringBuilder err = new StringBuilder();
        TransferItemPack transferItemPack = null;

        if (groupPosition == 4 && childPosition == 2) {
            transferItemPack = DBQuery.queryTransferItemQty(activityContext, err, null, "OUT");
            if (transferItemPack == null) {
                MessageBox.drawerShow("", "Query transfer list error.", mActivity, "Return", new IMsgBoxOnClick() {
                    @Override
                    public void onYesClick() {
                        drawerLayout.closeDrawers();
                    }

                    @Override
                    public void onNoClick() {

                    }
                });

                return true;
            }
            if (transferItemPack.transfers == null) {
                MessageBox.drawerShow("", "No transfer out list", mActivity, "Ok", new IMsgBoxOnClick() {
                    @Override
                    public void onYesClick() {
                        drawerLayout.closeDrawers();
                    }

                    @Override
                    public void onNoClick() {

                    }
                });
                return true;
            }
        }

        if (groupPosition == 6 && (childPosition == 3 || childPosition == 4)) {
            if (!Tools.isInternetLinked(mActivity)) {
                MessageBox.drawerShow("", "No internet", mActivity, "Return", new IMsgBoxOnClick() {
                    @Override
                    public void onYesClick() {
                        drawerLayout.closeDrawers();
                    }

                    @Override
                    public void onNoClick() {

                    }
                });
                return true;
            }
        }

        Intent intent = new Intent();
        //有子項目:
        switch (groupPosition) {

            case 4: //Transfer
                if (childPosition == 0) {
                    intent = new Intent(mActivity, TransferOutActivity01.class);
                } else if (childPosition == 1) {
                    intent = new Intent(mActivity, TransferInActivity.class);
                } else if (childPosition == 2) {
                    Bundle argument = new Bundle();
                    Gson gson = new Gson();
                    String jsonPack = gson.toJson(transferItemPack);
                    argument.putString("jsonPack", jsonPack);
                    intent = new Intent(mActivity, CancelTransferOutActivity.class);
                    intent.putExtras(argument);
                }
                break;

            case 6: //Wi-fi
                if (childPosition == 0) {
                    intent = new Intent(mActivity, IFEEX2Activity.class);
                } else if (childPosition == 1) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("type", 0);
                    intent = new Intent(mActivity, IFEEX3Activity.class);
                    intent.putExtras(bundle);
                } else if (childPosition == 2) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("type", 1);
                    intent = new Intent(mActivity, IFEEX3Activity.class);
                    intent.putExtras(bundle);
                } else if (childPosition == 3) {
                    intent = new Intent(mActivity, OrderListActivity.class);
                } else if (childPosition == 4) {
                    Bundle bundle = new Bundle();
                    bundle.putString("fromWhere", "MenuDealInProgress");
                    intent = new Intent(mActivity, CPCheckActivity.class);
                    intent.putExtras(bundle);
                }
                break;

            case 8: //Upgrade
                if (childPosition == 0) {
                    intent = new Intent(mActivity, UpgradeBasketActivity.class);
                } else if (childPosition == 1) {
                    Bundle bundle = new Bundle();
                    bundle.putString("fromWhere", "MenuUpgradeRefund");
                    intent = new Intent(mActivity, CPCheckActivity.class);
                    intent.putExtras(bundle);
                }
                break;
        }

        mActivity.startActivity(intent);
        //關掉所有開過的子選單畫面
        ActivityManager.closeAllActivity();
//          parent.collapseGroup(groupPosition);
        drawerLayout.closeDrawers();
        return false;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mloadingDialog.dismiss();
            switch (msg.what) {
                case 1: // 沒紙(收受)
                    if (MessageBox.show("", "No paper, reprint?", mActivity, "Yes", "No")) {

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        printData(true);
                    } else {
                        doPrintFinal();
                    }
                    break;

                case 2: // 沒紙(報表)
                    if (MessageBox.show("", "No paper, reprint?", mActivity, "Yes", "No")) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        printData(false);
                    } else {
                        doPrintFinal();
                    }
                    break;

                case 3: //Print error (收受)
                    if (MessageBox.show("", "Print error, retry?", mActivity, "Yes", "No")) {

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        printData(true);
                    } else {
                        doPrintFinal();
                    }
                    break;

                case 4: //Print error (報表)
                    if (MessageBox.show("", "Print error, retry?", mActivity, "Yes", "No")) {

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        printData(false);
                    } else {
                        doPrintFinal();
                    }
                    break;

                case 5: // 印收據
                    if (MessageBox.show("", "Print next sheet", mActivity, "Ok")) {

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        printData(false);
                    }
                    break;

                case 6: //成功
                    doPrintFinal();
                    break;

                case 7: //問要不要再印一張End Inventory
                    if (MessageBox.show("", "Reprint end inventory?", mActivity, "Yes", "No")) {

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        printData(false);
                    } else {
                        doPrintFinal();
                    }
                    break;
            }
        }
    };

    private void doPrintFinal() {
        StringBuilder err = new StringBuilder();
        if (DBQuery.closeFlightSecSeq(activityContext, err)) {
            if (MessageBox.show("", "Sector closed", mActivity, "Ok")) {

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent intent02 = new Intent(mActivity, MainActivity.class);
                mActivity.startActivity(intent02);
                mActivity.finish();
            }
        } else {
            MessageBox.show("", "Close error", mActivity, "Return");

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void printData(final boolean isPrintSummery) {
        mloadingDialog = ProgressDialog.show(mActivity, "", "Processing...", true, false);
        new Thread() {
            public void run() {
                PrintAir printer = new PrintAir(mActivity, Integer.valueOf(FlightInfoManager.getInstance().getCurrentSecSeq()));
                try {

                    if (isPrintSummery) {
                        if (printer.printSaleSummary(Integer.valueOf(FlightInfoManager.getInstance().getCurrentSecSeq())) == -1) {
                            mHandler.sendMessage(Tools.createMsg(1));
                        } else {
                            mHandler.sendMessage(Tools.createMsg(5));
                        }
                    } else {
                        if (printer.printEndInventory() == -1) {
                            mHandler.sendMessage(Tools.createMsg(2));
                        } else {
                            mHandler.sendMessage(Tools.createMsg(7));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mActivity, FlightInfoManager.getInstance().getCurrentSecSeq(), "P17023");
                    _TSQL.WriteLog(FlightInfoManager.getInstance().getCurrentSecSeq(),
                        "System", "NavigationDrawer", "printSaleSummary, printEndInventory", e.getMessage());
                    if (isPrintSummery) {
                        mHandler.sendMessage(Tools.createMsg(3));
                    } else {
                        mHandler.sendMessage(Tools.createMsg(4));
                    }
                }
            }
        }.start();
    }

}
