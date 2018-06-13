package tw.com.regalscan.evaair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;
import tw.com.regalscan.R;
import tw.com.regalscan.component.MenuPageView;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.PreorderInfoPack;
import tw.com.regalscan.db02.DBQuery.ReceiptList;
import tw.com.regalscan.evaair.ife.IFEActivity01;
import tw.com.regalscan.evaair.ife.IFEDBFunction;
import tw.com.regalscan.evaair.ife.OnlineBasketActivity;
import tw.com.regalscan.evaair.preorder.PreorderSaleActivity;
import tw.com.regalscan.evaair.report.ReportActivity;
import tw.com.regalscan.evaair.transfer.TransferActivity01;

public class MenuPageOneView extends MenuPageView {

    //畫面Menu按鈕
    private Button btnUpdate, btnSale, btnRefund, btnPreorder, btnTransfer, btnReport, btnWifi;
    private Context activityContext;
    private Activity mActivity;
    private IFEDBFunction mIFEDBFunction;

    public MenuPageOneView(Context context, Activity activity) {
        super(context);
        View mview = LayoutInflater.from(context).inflate(R.layout.pageview_menu01, null);
        activityContext = context;
        mActivity = activity;
        mIFEDBFunction = new IFEDBFunction(context, FlightData.SecSeq);
        Drawable img = getContext().getResources().getDrawable(R.drawable.icon_refresh);
        img.setBounds(0, 0, 60, 60); //指定一個矩形區域, 在這個矩形區域內畫圖

        //btn
        btnUpdate = mview.findViewById(R.id.btnUpdate);
        btnSale = mview.findViewById(R.id.btnSale);
        btnRefund = mview.findViewById(R.id.btnRefund);
        btnPreorder = mview.findViewById(R.id.btnPreorder);
        btnTransfer = mview.findViewById(R.id.btnTransfer);
        btnReport = mview.findViewById(R.id.btnReport);
        btnWifi = mview.findViewById(R.id.btnWifi);
//
        OnClickListener menuBtnOnClick = v -> {

            StringBuilder err = new StringBuilder();
            Intent intent;
            Bundle argument = new Bundle();
            Gson gson = new Gson();
            String jsonPack;

            switch (v.getId()) {
                case R.id.btnUpdate:
                    //確認是否已經經過商品銷售和移儲, 10
                    //沒經過銷售可以進去
                    if (!DBQuery.checkCurrentFlightCanUpdate(activityContext, err) ) {
                        MessageBox.show("", "You can't update after selling, transferring or initializing", activityContext, "Return");
                        return;
                    }

                    if (mIFEDBFunction.chkPushInventory(FlightData.SecSeq)) {
                        MessageBox.show("", "You can't update after selling, transferring or initializing", activityContext, "Return");
                        return;
                    }

                    intent = new Intent(activityContext, UpdateActivity.class);
                    activityContext.startActivity(intent);
                    break;

                case R.id.btnSale:

                    if (DBQuery.checkCurrentFlightCanUpdate(activityContext, err) && !mIFEDBFunction.chkPushInventory(FlightData.SecSeq)) {
                        MessageBox.show("", "Don't forget to check inventory!", activityContext, "Ok");
                    }

                    if (!FlightData.IFEConnectionStatus) {
                        argument.putString("FromWhere", "Menu");
                        intent = new Intent(activityContext, tw.com.regalscan.evaair.basket.BasketActivity.class);
//                        intent = new Intent(activityContext, tw.com.regalscan.evaair.mvp.ui.activity.BasketActivity.class);
                        intent.putExtras(argument);
                        activityContext.startActivity(intent);
                    } else {
                        argument.putString("fromWhere", "Menu");
                        intent = new Intent(activityContext, OnlineBasketActivity.class);
                        intent.putExtras(argument);
                        activityContext.startActivity(intent);
                    }
                    break;

                case R.id.btnRefund:
                    ReceiptList receiptNoList = DBQuery.getAllRceciptNoList(activityContext, err, "Sale", false);
                    if (receiptNoList == null) {
                        MessageBox.show("", "Query order data error", activityContext, "Return");
                        return;
                    }
                    if (receiptNoList.rececipts == null) {
                        MessageBox.show("", "No receipt can refund", activityContext, "Ok");
                        return;
                    }
                    // 轉json傳入
                    jsonPack = gson.toJson(receiptNoList);
                    argument.putString("jsonPack", jsonPack);
                    intent = new Intent(activityContext, RefundActivity.class);
                    intent.putExtras(argument);
                    activityContext.startActivity(intent);
                    break;

                case R.id.btnPreorder:
                    PreorderInfoPack preorderPack = DBQuery.getPRVPCanSaleRefund(activityContext,
                        err, FlightData.SecSeq, null, new String[]{"PR"}, "N");
                    if (preorderPack == null) {
                        MessageBox.show("", "Query pre-order data error", activityContext, "Return");
                        return;
                    }
                    if (preorderPack.info == null) {
                        MessageBox.show("", "No pre-order sale list", activityContext, "Ok");
                        return;
                    }
                    // 轉json傳入
                    jsonPack = gson.toJson(preorderPack);
                    argument.putString("jsonPack", jsonPack);
                    intent = new Intent(activityContext, PreorderSaleActivity.class);
                    intent.putExtras(argument);
                    activityContext.startActivity(intent);
                    break;

                case R.id.btnTransfer:
                    intent = new Intent(activityContext, TransferActivity01.class);
//                    intent.putExtras(argument);
                    activityContext.startActivity(intent);
                    break;

                case R.id.btnReport:
                    intent = new Intent(activityContext, ReportActivity.class);
//                    intent.putExtras(argument);
                    activityContext.startActivity(intent);
                    break;

                case R.id.btnWifi:
                    intent = new Intent(activityContext, IFEActivity01.class);
//                    intent.putExtras(argument);
                    activityContext.startActivity(intent);
                    break;
            }
        };
        btnUpdate.setOnClickListener(menuBtnOnClick);
        btnSale.setOnClickListener(menuBtnOnClick);
        btnRefund.setOnClickListener(menuBtnOnClick);
        btnPreorder.setOnClickListener(menuBtnOnClick);
        btnTransfer.setOnClickListener(menuBtnOnClick);
        btnReport.setOnClickListener(menuBtnOnClick);
        btnWifi.setOnClickListener(menuBtnOnClick);

        addView(mview);
    }


    @Override
    public void refreshView() {

    }
}
