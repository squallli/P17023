package tw.com.regalscan.evaair.upgrade;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import java.util.Arrays;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.adapters.ItemListUpgradeAdapter;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.R;
import tw.com.regalscan.component.SpinnerHideItemAdapter;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.ReceiptList;
import tw.com.regalscan.db02.DBQuery.Receipt;
import tw.com.regalscan.db02.DBQuery.UpgradeItemPack;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.UpgradeRefundTranscation;
import tw.com.regalscan.utils.Tools;

public class UpgradeRefundActivity01 extends AppCompatActivity {



    private Button btnReturn, btnRefund;
    public Context mContext;
    public Activity mActivity;
    private Spinner spinnerOrder;
    private TextView txtToolbarTitle, txtTotalMoney;
    public ItemListUpgradeAdapter adapter;
    private ListView itemListView;

    // 所有單據號碼
    private DBQuery.ReceiptList receiptNoList, tmp_receiptNoList;
    public static UpgradeRefundTranscation _UpgradeRefundTranscation;
    private UpgradeItemPack upgradePack;
    private SpinnerHideItemAdapter orderList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_refund_01);



        // 整個pack
        Bundle argument = getIntent().getExtras();
        if(argument!=null){
            String itemString = argument.getString("jsonPack");
            Gson gson = new Gson();
            tmp_receiptNoList=  gson.fromJson(itemString, ReceiptList.class);
            init();
        }else{
            if(MessageBox.show("", "No upgrade list", this, "Ok")){
                finish();
            }
        }
    }


    private void init(){
        mContext=this;
        mActivity=this;
        enableExpandableList();

        btnReturn= findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
        });

        txtToolbarTitle = findViewById(R.id.toolbar_title);
        txtToolbarTitle.setText("Upgrade Refund");

        //items
        itemListView= findViewById(R.id.lvItemList);
        adapter = new ItemListUpgradeAdapter(mContext);
        adapter.setIsModifiedItem(false);
        itemListView.setAdapter(adapter);

        try{
            //建立交易
            _UpgradeRefundTranscation = new UpgradeRefundTranscation(mContext, FlightData.SecSeq);
        }catch (Exception e){
            e.printStackTrace();
            MessageBox.show("", "Get sales info error", mContext, "Return");
            return;
        }

        ArrayList<String> tmpList= new ArrayList<>();
        for(int i=0; i<tmp_receiptNoList.rececipts.length; i++){
            tmpList.add(tmp_receiptNoList.rececipts[i].ReceiptNo);
        }
        // 重新排序所有單據
        tmpList= Tools.resortListNo(tmpList);
        tmpList.add(0, "Choose receipt");
        //回傳的清單有Upgrade商品
        orderList= new SpinnerHideItemAdapter(this, R.layout.spinner_item, tmpList, 0);

        // 複製一份重新排列
        receiptNoList= new ReceiptList();
        receiptNoList.rececipts= Arrays.copyOf(tmp_receiptNoList.rececipts, tmp_receiptNoList.rececipts.length);
        for(int i=0; i<tmpList.size(); i++){
            for(int j=0; j<tmp_receiptNoList.rececipts.length; j++){
                if(tmpList.get(i).equals(tmp_receiptNoList.rececipts[j].ReceiptNo)){
                    receiptNoList.rececipts[i-1]=tmp_receiptNoList.rececipts[j];
                    break;
                }
            }
        }

        spinnerOrder = findViewById(R.id.spinner01);
        spinnerOrder.setAdapter(orderList);
        spinnerOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0){
                    //載入商品清單, 第一個位置不是單據
                    loadOrderItems(position-1);
                }else{
                    txtTotalMoney.setText( "USD 0" );
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    btnRefund.setEnabled(false);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        txtTotalMoney= findViewById(R.id.txtTotalMoney);


        //btn
        btnRefund= findViewById(R.id.btnRefund);
        btnRefund.setEnabled(false);
        btnRefund.setOnClickListener(view -> {
            if(upgradePack==null){
                MessageBox.show("", "Get upgrade info error", mContext, "Return");
                return;
            }

            if(spinnerOrder.getSelectedItemPosition()==0){
                UpgradeRefundActivity01.this.runOnUiThread(() -> MessageBox.show("", "Please choose receipt", mContext, "Return"));
                return;
            }

            try{
                //傳遞整張單
                Gson gson= new Gson();
                String jsonItem= gson.toJson(upgradePack);
                Bundle bundle = new Bundle();
                bundle.putString("order",  jsonItem);
                Intent intent= new Intent(mActivity, UpgradeRefundActivity02.class);
                intent.putExtras(bundle);
                mActivity.startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
                MessageBox.show("", "Get upgrade info error", mContext, "Return");
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

    private void loadOrderItems(int position){
        Receipt receiptNo= receiptNoList.rececipts[position];
        StringBuilder err= new StringBuilder();
        try{
            // 依據單據取得購買物品
            upgradePack= DBQuery.modifyUpgradeBasket(err,
                _UpgradeRefundTranscation.GetBasketInfo(receiptNo.ReceiptNo));
            if(upgradePack==null){
                MessageBox.show("", "Get upgrade info error", mContext, "Return");
                return;
            }

            adapter.clear();
            for(int i=0; i<upgradePack.items.length; i++){
                adapter.addItem(upgradePack.items[i].Infant, upgradePack.items[i].OriginalClass,
                    upgradePack.items[i].NewClass, upgradePack.items[i].SalesQty,
                    upgradePack.items[i].USDPrice *  upgradePack.items[i].SalesQty);
            }
            adapter.notifyDataSetChanged();
            txtTotalMoney.setText( "USD " + Tools.getModiMoneyString(upgradePack.USDAmount) );
            btnRefund.setEnabled(true);

        }catch (Exception e){
            e.printStackTrace();
            MessageBox.show("", "Get upgrade info error", mContext, "Return");
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