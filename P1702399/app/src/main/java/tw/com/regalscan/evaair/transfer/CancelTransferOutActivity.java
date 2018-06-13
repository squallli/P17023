package tw.com.regalscan.evaair.transfer;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import tw.com.regalscan.R;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.RFIDReaderService;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.activities.ItemPictureActivity;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.CrewInfo;
import tw.com.regalscan.db02.DBQuery.TransferItemPack;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.utils.Tools;

public class CancelTransferOutActivity extends AppCompatActivity {

    private Button btnReturn, btnCancel;
    public Context mContext;
    public Activity mActivity;

    //Transfer Number
    private Spinner spinnerDrawer;
    private TextView txtFrom, txtTo;

    //商品清單
    private ListView itemListView;
    private ItemListPictureModifyAdapter adapter;

    //CP ID, PW
    private EditText editId, editpw;

    //所有的Transfer單據資料
    private TransferItemPack transferItemPack;
    private InputMethodManager imm;
    private RFIDReaderService mRFIDReaderService;



    private static class RFIDHandler extends Handler {
        private WeakReference<CancelTransferOutActivity> weakActivity;
        RFIDHandler(CancelTransferOutActivity a) {
            weakActivity = new WeakReference<>(a);
        }
        @Override
        public void handleMessage(Message msg) {
            CancelTransferOutActivity handlerActivity = weakActivity.get();
            Context handlerContext= handlerActivity.getApplicationContext();

            switch (msg.what) {
                case RFIDReaderService.MSG_SHOW_BLOCK_DATA:
                    String UID = msg.getData().getString(RFIDReaderService.CARD_UID);
                    //員工證號
                    String BlockData = msg.getData().getString(RFIDReaderService.CARD_BLOCK_DATA);
                    //"EVA" or "EGAS", UID和BlockData為null的話就是ErrorString
                    String EMPLOYEE_TYPE = msg.getData().getString(RFIDReaderService.EMPLOYEE_TYPE);

                    if (UID != null && BlockData != null) {

                        StringBuilder err= new StringBuilder();
                        CrewInfo CP= DBQuery.getGetCrewPassword(handlerContext, err, BlockData);
                        if(CP==null){
                            MessageBox.show("", "Please check ID", handlerContext, "Return");
                            return;
                        }
                        handlerActivity.editId.setText(BlockData);
                        handlerActivity.editpw.setText(CP.Password);
                    } else {
                        // EMPLOYEE_TYPE為 Error String
                        MessageBox.show("", EMPLOYEE_TYPE, handlerContext, "Return");
                    }
                    break;

                case RFIDReaderService.MSG_OPEN_FAILED:
                    MessageBox.show("", "Please try again", handlerContext, "Return");
                    break;
            }
        }
    }

//    private final Handler mHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case RFIDReaderService.MSG_SHOW_BLOCK_DATA:
//                    String UID = msg.getData().getString(RFIDReaderService.CARD_UID);
//                    //員工證號
//                    String BlockData = msg.getData().getString(RFIDReaderService.CARD_BLOCK_DATA);
//                    //"EVA" or "EGAS", UID和BlockData為null的話就是ErrorString
//                    String EMPLOYEE_TYPE = msg.getData().getString(RFIDReaderService.EMPLOYEE_TYPE);
//
//                    if (UID != null && BlockData != null) {
//
//                        StringBuilder err= new StringBuilder();
//                        CrewInfo CP= DBQuery.getGetCrewPassword(mContext, err, BlockData);
//                        if(CP==null){
//                            MessageBox.show("", "Please check ID", mActivity, "Return");
//                            return;
//                        }
//                        editId.setText(BlockData);
//                        editpw.setText(CP.Password);
//                    } else {
//                        // EMPLOYEE_TYPE為 Error String
//                        MessageBox.show("", EMPLOYEE_TYPE, mActivity, "Return");
//                    }
//                    break;
//
//                case RFIDReaderService.MSG_OPEN_FAILED:
//                    MessageBox.show("", "Please try again", mActivity, "Return");
//                    break;
//            }
//        }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_transfer_out);
        // 整個pack
        Bundle argument = getIntent().getExtras();
        if(argument!=null){
            String itemString = argument.getString("jsonPack");
            Gson gson = new Gson();
            transferItemPack=  gson.fromJson(itemString, TransferItemPack.class);
            init();
        }else{
            if(MessageBox.show("", "No transfer out list", this, "Ok")){
                finish();
            }
        }
    }

    private void init() {
        mContext = this;
        mActivity = this;
        enableExpandableList();
        imm= (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
        Handler rfidHandler = new RFIDHandler(this);
        mRFIDReaderService = new RFIDReaderService(mActivity, rfidHandler);

        txtFrom= findViewById(R.id.txtFrom);
        txtTo= findViewById(R.id.txtTo);

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            imm.hideSoftInputFromWindow(editId.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editpw.getWindowToken(), 0);
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();

            if (!ActivityManager.isActivityInStock("TransferActivity01")) {
                Intent mIntent= new Intent(mActivity, TransferActivity01.class);
                mActivity.startActivity(mIntent);
            }
        });

        //物品清單
        itemListView= findViewById(R.id.lvItemList);
        adapter = new ItemListPictureModifyAdapter(mContext, itemListView);
        adapter.setIsModifiedItem(false);
        adapter.setIsPictureZoomIn(true);
        adapter.setIsRightTwoVisible(false);
        adapter.setIsMoneyVisible(false);
        adapter.setFilpperFunctionClickListener(itemPictureClickListener);
        itemListView.setAdapter(adapter);

        // 下拉選單, 取得所有其他的TransferNo塞進去
        spinnerDrawer = findViewById(R.id.spinner01);

        // 將回傳的所有item放入spinner
        // item是散的, 要比對所有的ReceiptNo, 同一張單只顯示一次
        ArrayList<String> tmpList= new ArrayList<>();
        for( int i=0 ;i <transferItemPack.transfers.length; i++ ){
            if(i==0 || !transferItemPack.transfers[i].ReceiptNo.equals(transferItemPack.transfers[i-1].ReceiptNo) ){
                tmpList.add(transferItemPack.transfers[i].ReceiptNo);
            }
        }
        tmpList= Tools.resortListNo(tmpList);
        ArrayAdapter<String> listNo = new ArrayAdapter<String>(this, R.layout.spinner_item);
        for(String s: tmpList){
            listNo.add(s);
        }

        spinnerDrawer.setAdapter(listNo);
        spinnerDrawer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try{
                    //載入adapter清單
                    adapter.clear();
                    for(DBQuery.TransferItem trans : transferItemPack.transfers){
                        //當選擇的ReceiptNo與Pack內的item ReceiptNo相同，加入adapter
                        if( trans.ReceiptNo.equals(spinnerDrawer.getSelectedItem().toString()) ){

                            //轉出轉入車號
                            txtFrom.setText("From: " + trans.CarFrom);
                            txtTo.setText("To: " + trans.CarTo);

                            //String SerialCode, String monyType, int price, String itemName, int stock, int qt
                            adapter.addItem( trans.ItemCode, trans.SerialCode, "US", 0.0,
                                trans.ItemName, 0, Integer.valueOf(trans.Qty) );
                        }
                    }
                    adapter.notifyDataSetChanged();

                }catch (Exception e){
                    e.printStackTrace();
                    MessageBox.show("", "Set cart list error", mContext, "Return");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        editId= findViewById(R.id.editId);
        editpw= findViewById(R.id.editPassword);

        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(view -> certificateCP());

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }


    private void certificateCP(){

        imm.hideSoftInputFromWindow(editId.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(editpw.getWindowToken(), 0);

        String CPId= editId.getText().toString();
        String CPPw= editpw.getText().toString();

        //確認cp id & pw 不為空
        if( CPId.equals("") ){
            MessageBox.show("", "Please input CP ID", CancelTransferOutActivity.this, "Return");
            return;
        }
        if(CPPw.equals("") ){
            MessageBox.show("", "Please input CP password", CancelTransferOutActivity.this, "Return");
            return;
        }

        StringBuilder err = new StringBuilder();

        //呼叫DB取消此筆單據
        if( MessageBox.show("", "Cancel transfer out?", CancelTransferOutActivity.this, "Yes", "No") ){
            try{
                //驗證CP帳號密碼, 5
                if( DBQuery.getGetCrewInfo(mContext, err, CPId, CPPw, "CP")==null ){
                    CancelTransferOutActivity.this.runOnUiThread(() -> MessageBox.show("", "Please check ID and password", mContext, "Return"));
                    return;
                }

                if(DBQuery.checkEmployee(mContext, err, CPId)==null){
                    MessageBox.show("", "Not employee", mContext, "Return");
                    return;
                }
                if(!FlightData.PurserID.equals(CPId)){
                    MessageBox.show("", "Wrong CP", CancelTransferOutActivity.this, "Return");
                    return;
                }

                String cancelNum=null;
                for(DBQuery.TransferItem trans : transferItemPack.transfers){
                    //當選擇的ReceiptNo與Pack內的item ReceiptNo相同，取得其TransferNo
                    if( trans.ReceiptNo.equals(spinnerDrawer.getSelectedItem().toString()) ){
                        cancelNum= trans.TransferNo;
                    }
                }

                if( cancelNum!=null && !( DBQuery.cancelTransfer(mContext, err, cancelNum) ) ){
                    MessageBox.show("", "Cancel transfer out error", mContext, "Return");
                    return;
                }

                if( MessageBox.show("", "Success", mContext, "Ok") ){
                    ActivityManager.removeActivity(mActivity.getClass().getSimpleName());

                    if (!ActivityManager.isActivityInStock("TransferActivity01")) {
                        Intent mIntent= new Intent(mActivity, TransferActivity01.class);
                        mActivity.startActivity(mIntent);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                MessageBox.show("", "Cancel transfer error", mContext, "Return");
            }
        }
    }

    @Override
    public void onStart() {
        mRFIDReaderService.start();
        super.onStart();
    }

    private void enableExpandableList(){
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ExpandableListView expandableListView = findViewById(R.id.expendlist);
        NavigationDrawer navigationDrawer= new NavigationDrawer(mActivity, mContext, drawerLayout, toolbar, expandableListView);
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

    @Override
    protected void onDestroy() {
        mRFIDReaderService.Dispose();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        mRFIDReaderService.Dispose();
        super.onPause();
    }
}