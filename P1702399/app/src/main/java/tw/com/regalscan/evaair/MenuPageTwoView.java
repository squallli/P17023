package tw.com.regalscan.evaair;


import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import aero.panasonic.inflight.crew.services.ordermanagement.v1.CrewOrder;
import aero.panasonic.inflight.crew.services.ordermanagement.v1.OrderStatus;
import com.google.gson.Gson;
import com.regalscan.sqlitelibrary.TSQL;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import tw.com.regalscan.MainActivity;
import tw.com.regalscan.R;
import tw.com.regalscan.component.FlightInfoManager;
import tw.com.regalscan.component.MenuPageView;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.PreorderInfoPack;
import tw.com.regalscan.evaair.ife.IFEFunction;
import tw.com.regalscan.evaair.preorder.VipPaidActivity;
import tw.com.regalscan.evaair.preorder.VipSaleActivity;
import tw.com.regalscan.evaair.upgrade.UpgradeActivity01;
import tw.com.regalscan.utils.Cursor;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;

public class MenuPageTwoView extends MenuPageView {

    //畫面Menu按鈕
    private Button btnDamage, btnUpgrade, btnVipPaid,
        btnVipSale, btnClose, btnCatalog, btnPicture;
    private Context activityContext;
    private Activity mActivity;
    private ProgressDialog mloadingDialog;

    public MenuPageTwoView(Context context, Activity activity) {
        super(context);
        View view = LayoutInflater.from(context).inflate(R.layout.pageview_menu02, null);
        activityContext = context;
        mActivity = activity;
        Drawable img = getContext().getResources().getDrawable(R.drawable.icon_refresh);
        img.setBounds(0, 0, 60, 60); //指定一個矩形區域, 在這個矩形區域內畫圖

        //btn
        btnDamage = view.findViewById(R.id.btnDamage);
        btnUpgrade = view.findViewById(R.id.btnUpgrade);
        btnVipPaid = view.findViewById(R.id.btnVipPaid);
        btnVipSale = view.findViewById(R.id.btnVipSale);
        btnClose = view.findViewById(R.id.btnClose);
        btnCatalog = view.findViewById(R.id.btnCatalog);
        btnPicture = view.findViewById(R.id.btnPicture);

        btnDamage.setOnClickListener(menuBtnOnClick);
        btnUpgrade.setOnClickListener(menuBtnOnClick);
        btnVipPaid.setOnClickListener(menuBtnOnClick);
        btnVipSale.setOnClickListener(menuBtnOnClick);
        btnClose.setOnClickListener(menuBtnOnClick);
        btnCatalog.setOnClickListener(menuBtnOnClick);
        btnPicture.setOnClickListener(menuBtnOnClick);

        addView(view);
    }

    private OnClickListener menuBtnOnClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            StringBuilder err = new StringBuilder();
            Intent intent;
            Bundle argument = new Bundle();
            Gson gson = new Gson();
            String jsonPack;

            switch (v.getId()) {
                case R.id.btnDamage:
                    intent = new Intent(activityContext, DamageActivity.class);
                    activityContext.startActivity(intent);
                    break;

                case R.id.btnUpgrade:
                    intent = new Intent(activityContext, UpgradeActivity01.class);
                    activityContext.startActivity(intent);
                    break;

                case R.id.btnVipPaid:
                    PreorderInfoPack preorderPack = DBQuery.getPRVPCanSaleRefund(activityContext, err,
                        FlightData.SecSeq, null, new String[]{"VP"}, "N");
                    if (preorderPack == null) {
                        MessageBox.show("", "Query VIP paid data error", activityContext, "Return");
                        return;
                    }
                    if (preorderPack.info == null) {
                        MessageBox.show("", "No VIP paid list", activityContext, "Ok");
                        return;
                    }
                    // 轉json傳入
                    jsonPack = gson.toJson(preorderPack);
                    argument.putString("jsonPack", jsonPack);
                    intent = new Intent(activityContext, VipPaidActivity.class);
                    intent.putExtras(argument);
                    activityContext.startActivity(intent);
                    break;

                case R.id.btnVipSale:
                    PreorderInfoPack preorderitempack = DBQuery.getPRVPCanSaleRefund(activityContext,
                        err, FlightData.SecSeq, null, new String[]{"VS"}, "N");
                    if (preorderitempack == null) {
                        MessageBox.show("", "Query Vip sale data error", activityContext, "Return");
                        return;
                    }
                    if (preorderitempack.info == null) {
                        MessageBox.show("", "No VIP sale list", activityContext, "Ok");
                        return;
                    }
                    // 轉json傳入
                    jsonPack = gson.toJson(preorderitempack);
                    argument.putString("jsonPack", jsonPack);
                    intent = new Intent(activityContext, VipSaleActivity.class);
                    intent.putExtras(argument);
                    activityContext.startActivity(intent);
                    break;

                case R.id.btnClose:
                    if (FlightData.IFEConnectionStatus) {
                        IFEFunction ifeFunction = new IFEFunction(mActivity);
                        ifeFunction.getOrders("", OrderStatus.ORDER_STATUS_OPEN)
                            .subscribeOn(Schedulers.io())
                            .doOnSubscribe(disposable -> Cursor.Busy(mActivity.getString(R.string.Processing_Msg), mActivity))
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally(Cursor::Normal)
                            .subscribe(ifeReturnData -> {
                                List<CrewOrder> crewOrders = (List<CrewOrder>)ifeReturnData.getData();
                                if (crewOrders.size() > 0) {
                                    if (MessageBox.show("", "You have " + String.valueOf(crewOrders.size()) + " order(s)\r\nDo you want to close?", activityContext, "Yes", "No")) {
                                        mloadingDialog = ProgressDialog.show(activityContext, "", "Processing...", true, false);
                                        printData(true);
                                    }
                                } else {
                                    if (MessageBox.show("", "Do you want to close?", activityContext, "Yes", "No")) {
                                        mloadingDialog = ProgressDialog.show(activityContext, "", "Processing...", true, false);
                                        printData(true);
                                    }
                                }
                            });
                    } else {
                        if (MessageBox.show("", "Do you want to close?", activityContext, "Yes", "No")) {
                            mloadingDialog = ProgressDialog.show(activityContext, "", "Processing...", true, false);
                            printData(true);
                        }
                    }
                    break;

                case R.id.btnCatalog:
                    intent = new Intent(activityContext, CatalogActivity.class);
                    activityContext.startActivity(intent);
                    break;
                case R.id.btnPicture:
                    intent = new Intent(activityContext, PictureActivity.class);
                    activityContext.startActivity(intent);
                    break;
            }
        }
    };


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mloadingDialog.dismiss();
            switch (msg.what) {
                case 1: // 沒紙(收受)
                    if (MessageBox.show("", "No paper, reprint?", activityContext, "Yes", "No")) printData(true);
                    else doPrintFinal();
                    break;

                case 2: // 沒紙(報表)
                    if (MessageBox.show("", "No paper, reprint?", activityContext, "Yes", "No")) printData(false);
                    else doPrintFinal();
                    break;

                case 3: //Print error (收受)
                    if (MessageBox.show("", "Print error, retry?", activityContext, "Yes", "No")) printData(true);
                    else doPrintFinal();
                    break;

                case 4: //Print error (報表)
                    if (MessageBox.show("", "Print error, retry?", activityContext, "Yes", "No")) printData(false);
                    else doPrintFinal();
                    break;

                case 5: // 印收據
                    if (MessageBox.show("", "Print next sheet", activityContext, "Ok")) printData(false);
                    break;

                case 6: //成功
                    doPrintFinal();
                    break;

                case 7: //問要不要再印一張End Inventory
                    if (MessageBox.show("", "Reprint end inventory?", activityContext, "Yes", "No")) printData(false);
                    else doPrintFinal();
                    break;
            }
        }
    };

    private void doPrintFinal() {
        StringBuilder err = new StringBuilder();
        if (DBQuery.closeFlightSecSeq(activityContext, err)) {
            if (MessageBox.show("", "Sector closed", activityContext, "Ok")) {
                Intent intent02 = new Intent(mActivity, MainActivity.class);
                mActivity.startActivity(intent02);
                mActivity.finish();
            }
        } else {
            MessageBox.show("", "Close error", mActivity, "Return");
        }
    }

    private void printData(final boolean isPrintSummery) {
        mloadingDialog.dismiss();
        mloadingDialog = ProgressDialog.show(activityContext, "", "Processing...", true, false);
        new Thread() {
            public void run() {
                PrintAir printer = new PrintAir(activityContext,
                    Integer.valueOf(FlightInfoManager.getInstance().getCurrentSecSeq()));
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
                    TSQL _TSQL = TSQL.getINSTANCE(activityContext, FlightInfoManager.getInstance().getCurrentSecSeq(), "P17023");
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


    @Override
    public void refreshView() {

    }
}
