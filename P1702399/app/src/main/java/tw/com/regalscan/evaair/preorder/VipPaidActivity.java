package tw.com.regalscan.evaair.preorder;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.regalscan.sqlitelibrary.TSQL;
import org.json.JSONObject;

import tw.com.regalscan.R;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.SpinnerHideItemAdapter;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.activities.ItemPictureActivity;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.PreorderInfoPack;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;


public class VipPaidActivity extends AppCompatActivity {

    public ItemListPictureModifyAdapter adapter;
    private ListView itemListView;

    private Spinner spinnerMoney, spinnerOrderNum;
    private TextView txtReceipt, txtToolbarTitle, txtTotal;
    private Button btnReturn, btnPrint;
    public Context mContext;
    public Activity mActivity;
    // preorder list pack
    private PreorderInfoPack preorderPack;
    private SpinnerHideItemAdapter orderList;
    private ProgressDialog mloadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preorder_sale_and_vip_paid_sale);





        // 整個pack
        Bundle argument = getIntent().getExtras();
        if(argument!=null){
            String itemString = argument.getString("jsonPack");
            Gson gson = new Gson();
            preorderPack=  gson.fromJson(itemString, PreorderInfoPack.class);
            init();
        }else{
          if(MessageBox.show("", "No VIP paid list", this, "Ok")){
            finish();
          }
        }
    }


    private void init() {
        mContext = this;
        mActivity = this;
        enableExpandableList();

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
            }
        });

        txtToolbarTitle = findViewById(R.id.toolbar_title);
        txtToolbarTitle.setText("VIP Paid");
        txtReceipt = findViewById(R.id.txtReceipt);
        txtReceipt.setText("VIP No: ");
        spinnerOrderNum = findViewById(R.id.spinner01);

        //回傳的清單有Pre-oreder商品
        ArrayList<String> list= new ArrayList<>();
        list.add("Choose no.");
        for(int i=0; i<preorderPack.info.length; i++){
            list.add( preorderPack.info[i].PreorderNO );
        }
        orderList= new SpinnerHideItemAdapter(this, R.layout.spinner_item, list, 0);
        spinnerOrderNum.setAdapter(orderList);

        spinnerOrderNum.setVisibility(View.VISIBLE);
        spinnerOrderNum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0){
                    //載入商品清單, 第一個位置不是單據
                    searchPreorder(position-1);
                }else{
                    txtTotal.setText("USD 0");
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    btnPrint.setEnabled(false);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // 幣別
        spinnerMoney = findViewById(R.id.spinner02);
        spinnerMoney.setVisibility(View.INVISIBLE);

        //items
        itemListView= findViewById(R.id.lvItemList);
        adapter = new ItemListPictureModifyAdapter(mContext, itemListView, true);
        adapter.setIsModifiedItem(false);
        adapter.setIsPictureZoomIn(true);
        adapter.setIsRightTwoVisible(true);
        adapter.setQtyPutPrice(true);
        adapter.setFilpperFunctionClickListener(itemPictureClickListener);
        itemListView.setAdapter(adapter);

        // Total
        txtTotal = findViewById(R.id.txtMoney);

        //btn
        btnPrint = findViewById(R.id.btnNext);
        btnPrint.setText("Print");
        btnPrint.setEnabled(false);
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final StringBuilder err= new StringBuilder();
                mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
                try{
                    if(spinnerOrderNum.getSelectedItemPosition()==0){
                        VipPaidActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mloadingDialog.dismiss();
                                MessageBox.show("", "Please choose", mContext, "Return");
                            }
                        });
                        return;
                    }
                    // 更新Preorder資訊
                    JSONObject request= new JSONObject();
                    request.put("PreorderNo", preorderPack.info[spinnerOrderNum.getSelectedItemPosition()-1].PreorderNO);
                    request.put("PreorderType", "VP");
                    request.put("USDAmount", "");
                    request.put("UpperlimitType", "");
                    request.put("UpperlimitDiscountNo", "");
                    request.put("VerifyType", "");
                    request.put("PaymentList", null);
                    if( ! DBQuery.savePreorderInfo(mContext, err, request) ){
                        VipPaidActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mloadingDialog.dismiss();
                                MessageBox.show("", "Save VIP data error, please retry", mContext, "Return");
                            }
                        });
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    VipPaidActivity.this.runOnUiThread(() -> {
                        mloadingDialog.dismiss();
                        MessageBox.show("", "Save VIP paid data error", mContext, "Return");
                    });
                    return;
                }
                printData();
            }
        });
        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }




    private static class PrinterHandler extends Handler {
        private WeakReference<VipPaidActivity> weakActivity;
        PrinterHandler(VipPaidActivity a) {
            weakActivity = new WeakReference<>(a);
        }
        @Override
        public void handleMessage(Message msg) {
            VipPaidActivity handlerActivity = weakActivity.get();
            Context handlerContext= handlerActivity.mContext;

            handlerActivity.mloadingDialog.dismiss();
            switch(msg.what){
                case 1: // 沒紙
                    if(MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No") ) handlerActivity.printData();
                    else handlerActivity.doPrintFinal();
                    break;

                case 2: //Print error
                    if(MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) handlerActivity.printData();
                    else handlerActivity.doPrintFinal();
                    break;

                case 3: //成功
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
//                case 1: // 沒紙
//                    if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData();
//                    else doPrintFinal();
//                    break;
//
//                case 2: //Print error
//                    if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData();
//                    else doPrintFinal();
//                    break;
//
//                case 3: //成功
//                    doPrintFinal();
//                    break;
//            }
//        }
//    };

    private void doPrintFinal(){
        StringBuilder err= new StringBuilder();
        if(MessageBox.show("", "Success", mContext, "Ok")){
            // 重新取得所有單據號碼
            preorderPack= DBQuery.getPRVPCanSaleRefund(mContext, err,FlightData.SecSeq, null, new String[]{"VP"}, "N");
            if (preorderPack==null) {
                MessageBox.show("", "Query VIP paid data error", mContext, "Return");
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
                return;
            }
            //回傳的清單有Pre-oreder商品
            if (preorderPack.info!=null){
                orderList.clear();
                orderList.add("Choose no.");
                for(int i=0; i<preorderPack.info.length; i++){
                    orderList.add( preorderPack.info[i].PreorderNO );
                }
                orderList.notifyDataSetChanged();
                spinnerOrderNum.setSelection(0);
                adapter.clear();
                adapter.notifyDataSetChanged();
            }else{
                if(MessageBox.show("", "No VIP paid list", mContext, "Ok")){
                    ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                    finish();
                }
            }
        }
    }

    private void printData(){
        mloadingDialog.dismiss();
        mloadingDialog= ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        new Thread() {
            public void run() {
                // 列印
                PrintAir printer= new PrintAir(mContext,
                    Integer.valueOf(FlightData.SecSeq));
                try{
                    if(printer.printVIPPaid( preorderPack.info[spinnerOrderNum.getSelectedItemPosition()-1].PreorderNO)==-1){
                        printerHandler.sendMessage(Tools.createMsg(1));
                    }else{
                        printerHandler.sendMessage(Tools.createMsg(3));
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                    _TSQL.WriteLog(FlightData.SecSeq,
                        "System", "VipPaidActivity", "printVIPPaid", e.getMessage());
                    printerHandler.sendMessage(Tools.createMsg(2));
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


    private void searchPreorder(final int packPosition){
        final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        new Thread() {
            public void run() {
                try{
                    DBQuery.PreorderInformation orderDetail= preorderPack.info[packPosition];

                    // load key
                    ArrayList<String> itemCodeList=new ArrayList<>();
                    for(int i=0; i<orderDetail.items.length; i++){
                        itemCodeList.add(orderDetail.items[i].ItemCode);
                    }
                    adapter.setImageKeyCodeList(itemCodeList);

                    adapter.clear();
                    for(int i=0; i<orderDetail.items.length; i++){
                        // String itemCode, String monyType, Double price, String itemName, int stock, Double qty
                        adapter.addItem(
                            orderDetail.items[i].ItemCode,
                            orderDetail.items[i].SerialCode,
                            "US", orderDetail.items[i].OriginalPrice,
                            orderDetail.items[i].ItemName,
                            orderDetail.items[i].SalesQty,
                            orderDetail.items[i].SalesPrice * orderDetail.items[i].SalesQty
                        );
                    }
                    final String ss= orderDetail.CurDvr +" " + Tools.getModiMoneyString(preorderPack.info[packPosition].Amount);
                    VipPaidActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            txtTotal.setText( ss );
                            btnPrint.setEnabled(true);
                            mloadingDialog.dismiss();
                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                    VipPaidActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mloadingDialog.dismiss();
                            MessageBox.show("", "Search item info error", mContext, "Return");
                        }
                    });
                }
            }
        }.start();
    }


    //點選 Item 圖片放大圖片
    private ItemListPictureModifyAdapter.ItemListFunctionClickListener itemPictureClickListener
            = new ItemListPictureModifyAdapter.ItemListFunctionClickListener() {

        @Override
        public void toolItemFunctionClickListener(String itemInfo) {
            try{
                ItemInfo item=adapter.getItem(itemInfo);
                Intent intent = new Intent();
                intent.setClass(mActivity, ItemPictureActivity.class);
                intent.putExtra("itemCode", item.getItemCode());
                mActivity.startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
                MessageBox.show("", "Zoom in item image error", mContext, "Return");
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

}