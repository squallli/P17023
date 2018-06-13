package tw.com.regalscan.evaair.preorder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import tw.com.regalscan.R;
import tw.com.regalscan.activities.ItemPictureActivity;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.component.SpinnerHideItemAdapter;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.PreorderInfoPack;
import tw.com.regalscan.db02.DBQuery.VIPSaleHeader;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.VipSaleTranscation;

public class VipSaleActivity extends AppCompatActivity {



    public ItemListPictureModifyAdapter adapter;
    private ListView itemListView;
    private Spinner spinnerCurrency, spinnerOrderNum;
    private TextView txtReceipt, txtToolbarTitle, txtTotal;
    private Button btnReturn, btnPay;
    public Context mContext;
    public Activity mActivity;
    private TextView txtRightOne, txtRightTwo;

    //幣別選單
    private ArrayAdapter<String> listCurrency;
    private SpinnerHideItemAdapter orderList;
    //所有可使用的幣別
    private DBQuery.AllCurrencyListPack allCurrencyPack;
    // preorder list pack
    public static VipSaleTranscation _VipSaleTranscation;
    private VIPSaleHeader vipheader;
    private PreorderInfoPack preorderitempack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preorder_sale_and_vip_paid_sale);




        // 整個pack
        Bundle argument = getIntent().getExtras();
        if(argument!=null){
            String itemString = argument.getString("jsonPack");
            Gson gson = new Gson();
            preorderitempack=  gson.fromJson(itemString, PreorderInfoPack.class);
            init();
        }else{
            if(MessageBox.show("", "No VIP sale list", this, "Ok")){
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
        txtToolbarTitle.setText("VIP Sale");
        txtReceipt = findViewById(R.id.txtReceipt);
        txtReceipt.setText("VIP No: ");

        //品項文字Tag
        txtRightOne= findViewById(R.id.tag3);
        txtRightOne.setText("Qty");
        txtRightTwo= findViewById(R.id.tag2);
        txtRightTwo.setVisibility(View.INVISIBLE);
        spinnerOrderNum = findViewById(R.id.spinner01);

        try{
            //建立交易
            _VipSaleTranscation = new VipSaleTranscation(mActivity, FlightData.SecSeq);
        }catch (Exception e){
            e.printStackTrace();
            MessageBox.show("", "Get VIP data error", mContext, "Return");
            return;
        }

        // 將可使用的被動式折扣放入spinner
        ArrayList<String> list= new ArrayList<>();
        list.add("Choose no.");
        for(int i=0; i<preorderitempack.info.length; i++){
            list.add( preorderitempack.info[i].PreorderNO );
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
                    txtTotal.setText( "USD 0" );
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    btnPay.setEnabled(false);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        //取得可以使用的幣別
        StringBuilder err= new StringBuilder();
        allCurrencyPack= DBQuery.getAllCurrencyList(mContext, err);
        if( allCurrencyPack==null ){
            VipSaleActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MessageBox.show("", "Get currency error", mContext, "Return");
                }
            });
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
            return;
        }
        spinnerCurrency = findViewById(R.id.spinner02);
        listCurrency = new ArrayAdapter<String>(this, R.layout.spinner_item);
        for(int i=0; i<allCurrencyPack.currencyList.length; i++){
            //判斷是否要顯示在購物車上
            if(allCurrencyPack.currencyList[i].BasketCurrency.equals("Y")){
                listCurrency.add(allCurrencyPack.currencyList[i].CurDvr);
            }
        }
        spinnerCurrency.setAdapter(listCurrency);
        spinnerCurrency.setVisibility(View.VISIBLE);
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                txtTotal.setText(setTotalMoneyTextView());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        //items
        itemListView= findViewById(R.id.lvItemList);
        adapter = new ItemListPictureModifyAdapter(mContext, itemListView, true);
        adapter.setIsModifiedItem(false);
        adapter.setIsPictureZoomIn(true);
        adapter.setIsRightTwoVisible(false);
        adapter.setFilpperFunctionClickListener(itemPictureClickListener);
        itemListView.setAdapter(adapter);

        // Total
        txtTotal = findViewById(R.id.txtMoney);
        txtTotal.setText(setTotalMoneyTextView());

        //btn
        btnPay = findViewById(R.id.btnNext);
        btnPay.setText("Pay");
        btnPay.setEnabled(false);
        btnPay.setOnClickListener(view -> {

            if(spinnerOrderNum.getSelectedItemPosition()==0){
                VipSaleActivity.this.runOnUiThread(() -> MessageBox.show("", "Please choose", mContext, "Return"));
                return;
            }
            try{
                //傳遞整包BasketItemPack
                Bundle bundle = new Bundle();
                Gson gson = new Gson();
                String jsonBasketItemPack = gson.toJson(vipheader);
                bundle.putString("Currency", spinnerCurrency.getSelectedItem().toString());
                bundle.putString("VIPPack", jsonBasketItemPack);
                bundle.putString("Currency", spinnerCurrency.getSelectedItem().toString());

                Intent intent= new Intent(mActivity, VipPayActivity.class);
                intent.putExtras(bundle);
                mActivity.startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
                MessageBox.show("", "Set pay data error", mContext, "Return");
            }
        });
        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }


    private void enableExpandableList(){
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ExpandableListView expandableListView = findViewById(R.id.expendlist);
        NavigationDrawer navigationDrawer= new NavigationDrawer(mActivity, mContext, drawerLayout, toolbar, expandableListView);
    }

    // 取得應付款總額
    private String setTotalMoneyTextView(){
        try{
            if(listCurrency!=null){
                // 先呼叫一次，取得總應付金額，這樣 GetCurrencyMaxAmount 才取得到值
                _VipSaleTranscation.GetPaymentMode();

                String currency= spinnerCurrency.getSelectedItem().toString();
                DBQuery.ShouldPayMoney payItem= DBQuery.getPayMoneyNow( new StringBuilder(),
                    _VipSaleTranscation.GetCurrencyMaxAmount( currency ));
                if(payItem==null || payItem.Currency==null){
                    MessageBox.show("", "Get pay info error", mActivity, "Return");
                    return "";
                }
                if(adapter.getCount()!=0)
                    return (currency + " " + String.valueOf(payItem.MaxPayAmount));
                else
                    return (currency + " 0");
            }else{
                return "USD 0";
            }
        }catch (Exception e){
            e.printStackTrace();
            MessageBox.show("", "Set currency error", mContext, "Return");
            return "";
        }
    }


    private void searchPreorder(final int packPosition){

        final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        new Thread() {
            public void run() {
                try{
                    StringBuilder err= new StringBuilder();
                    vipheader= DBQuery.getVIPSaleHeader(err,_VipSaleTranscation.GetBasketInfo(preorderitempack.info[packPosition].PreorderNO));
                    if(vipheader==null){
                        VipSaleActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mloadingDialog.dismiss();
                                MessageBox.show("", "Get VIP data error", mContext, "Return");
                            }
                        });
                        return;
                    }

                    vipheader.preorderItem= preorderitempack.info[packPosition].items;
                    // load key
                    ArrayList<String> itemCodeList=new ArrayList<>();
                    for(int i=0; i<preorderitempack.info[packPosition].items.length; i++){
                        itemCodeList.add(preorderitempack.info[packPosition].items[i].ItemCode);
                    }
                    adapter.setImageKeyCodeList(itemCodeList);
                    adapter.clear();

                    for(int i=0; i<vipheader.preorderItem.length; i++){
                        // String itemCode, String serialNo, String monyType, Double price, String itemName, int stock, int qty
                        adapter.addItem(
                            vipheader.preorderItem[i].ItemCode,
                            vipheader.preorderItem[i].SerialCode ,
                            "US",
                            vipheader.preorderItem[i].OriginalPrice,
                            vipheader.preorderItem[i].ItemName, 0,
                            vipheader.preorderItem[i].SalesQty );
                    }

                    // 設定為美金
                    for(int i=0; i<listCurrency.getCount(); i++){
                        if(listCurrency.getItem(i).equals("USD")){
                            final int index= i;
                            VipSaleActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    spinnerCurrency.setSelection(index);
                                }
                            });
                            break;
                        }
                    }

                    VipSaleActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            txtTotal.setText( setTotalMoneyTextView() );
                            btnPay.setEnabled(true);
                            mloadingDialog.dismiss();
                        }
                    });

                }catch (Exception e){
                    e.printStackTrace();
                    VipSaleActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mloadingDialog.dismiss();
                            MessageBox.show("", "Get item error", mContext, "Return");
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



    @Override
    protected void onResume() {
        super.onResume();
        try{
            _VipSaleTranscation.GetBasketInfo(preorderitempack.info[spinnerOrderNum.getSelectedItemPosition()-1].PreorderNO);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

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