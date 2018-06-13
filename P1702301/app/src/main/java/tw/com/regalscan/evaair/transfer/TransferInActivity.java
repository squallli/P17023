package tw.com.regalscan.evaair.transfer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;

import com.regalscan.sqlitelibrary.TSQL;
import org.json.JSONObject;

import tw.com.regalscan.R;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.activities.ItemPictureActivity;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.TransferItemPack;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;


public class TransferInActivity extends AppCompatActivity {

    //Scan
    private final String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private int soundid;
    private String barcodeStr;

    private Button btnReturn,btnTransfer;
    public Context mContext;
    public Activity mActivity;

    private ItemListPictureModifyAdapter adapter;
    private ListView itemListView;
    private TextView txtFrom, txtTo;

    //紀錄此筆Transfer單據的號碼與來源車櫃
    private String transferNo="";
    private String transferFrom="";
    private ProgressDialog mloadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_in);
        init();
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//        addItem("2C2110506,0012C2,0012A2,018100995,015100940");
    }

    private void init() {
        mContext = this;
        mActivity = this;
        enableExpandableList();

        txtFrom= findViewById(R.id.txtFrom);
        txtTo= findViewById(R.id.txtTo);

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
                if (!ActivityManager.isActivityInStock("TransferActivity01")) {
                    Intent mIntent= new Intent(mActivity, TransferActivity01.class);
                    mActivity.startActivity(mIntent);
                }
            }
        });

        btnTransfer = findViewById(R.id.btnTransfer);
        btnTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(transferNo.equals("")){
                    MessageBox.show("", "Please scan transfer QR code", mContext, "Return");
                    return;
                }

                //詢問是否執行Transfer In, 不能in out同車
                if ( MessageBox.show("", "To transfer in?", mContext, "Yes", "No") ){
                    mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);

                    try{
                        // 建立tranfer單據
                        DBQuery.TransferAdjItem[] returnItems= new DBQuery.TransferAdjItem[adapter.getCount()];
                        for(int i = 0; i < adapter.getCount(); i++) {
                            returnItems[i]=new DBQuery.TransferAdjItem();
                            returnItems[i].ItemCode = adapter.getItem(i).getItemCode();
                            returnItems[i].TransferQty = (Integer)adapter.getItem(i).getQty();
                        }
                        // transferNo, carFrom ,carTo, in or out, items
                        DBQuery.TransferAdjItemPack returnPack= new DBQuery.TransferAdjItemPack(
                            transferNo, transferFrom,
                            FlightData.CartNo, "IN",
                            returnItems
                        );
                        // class轉 json Obj
                        Gson gson = new Gson();
                        String jsonString = gson.toJson(returnPack);
                        JSONObject jsonObj=new JSONObject(jsonString);

                        // 回傳tranfer單據
                        StringBuilder err = new StringBuilder();
                        if( !( DBQuery.transferItemQty(mContext, err, jsonObj) ) ){
                            TransferInActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mloadingDialog.dismiss();
                                    MessageBox.show("", "Transferred in repeatly", mContext, "Return");
                                }
                            });
                            return;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        mloadingDialog.dismiss();
                        MessageBox.show("", "Transfer error", mContext, "Return");
                        return;
                    }
//                    printData();
                    doPrintFinal();
                }
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

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }



    private static class PrinterHandler extends Handler {
        private WeakReference<TransferInActivity> weakActivity;
        PrinterHandler(TransferInActivity a) {
            weakActivity = new WeakReference<>(a);
        }
        @Override
        public void handleMessage(Message msg) {
            TransferInActivity handlerActivity = weakActivity.get();
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
        if(MessageBox.show("", "Success", mContext, "Ok")){
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            if (!ActivityManager.isActivityInStock("TransferActivity01")) {
                Intent mIntent= new Intent(mActivity, TransferActivity01.class);
                mActivity.startActivity(mIntent);
            }
        }
    }

    private void printData(){
        mloadingDialog.dismiss();
        mloadingDialog= ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        new Thread() {
            public void run() {
                //列印轉入收據
                PrintAir printer= new PrintAir(mContext, Integer.valueOf(FlightData.SecSeq));
                try{
                    if(printer.printTransferIn(transferNo)==-1){
                        printerHandler.sendMessage(Tools.createMsg(1));
                    }else{
                        printerHandler.sendMessage(Tools.createMsg(3));
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                    _TSQL.WriteLog(FlightData.SecSeq,
                        "System", "TransferInActivity", "printTransferIn", e.getMessage());
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

    //掃描Transfer Out收據上的QR Code, 讀取後顯示轉入的品項及車號
    private void addItem(String qrCode){
        try{
            //  車櫃號後三碼 + 時分秒(六碼) + , + 來源車櫃(六碼) + , + 目的車櫃(六碼) + , +  數量(二碼) + Code(六碼) ....接續
            //  EX: 2A1 095735 , 882957 , 102809 , 01 XG4122,  01 XG4221

            String splitString[]= qrCode.split(",");

            //掃讀Transfer In QR Code 目的車號錯誤
            if(!splitString[2].equals(FlightData.CartNo)) {
                TransferInActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MessageBox.show("", "Wrong cart", mContext, "Return");
                    }
                });
                return;
            }

            StringBuilder err = new StringBuilder();
            // Transfer不可In Out同車
            TransferItemPack transItemPack_01= DBQuery.queryTransferItemQty(mContext, err, splitString[0] , "OUT");
            if( transItemPack_01==null ){
                MessageBox.show("", "Query transfer list error", mContext, "Return");
                return;
            }
            if( transItemPack_01.transfers !=null ){
                MessageBox.show("", "Can't transfer to origin cart!", mContext, "Return");
                return;
            }

            // 16 查詢 Treanfer單據, Transfer No.: 2A1095735
            TransferItemPack transItemPack= DBQuery.queryTransferItemQty(mContext, err, splitString[0] , "OUT");
            if( transItemPack==null ){
                TransferInActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MessageBox.show("", "Query transfer list error", mContext, "Return");
                    }
                });
                return;
            }
            if(transItemPack.transfers!=null){
                TransferInActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MessageBox.show("", "Transfer No. was used!", mContext, "Return");
                    }
                });
                return;
            }

            adapter.clear();

            for(int j=3; j<splitString.length; j++){

                for (int i=2; i<splitString[j].length(); i+=8){
                    //用Transfer Out內的編號查詢DB
                    DBQuery.ItemDataPack itemPack = DBQuery.getProductInfo(
                        mContext, err, FlightData.SecSeq,
                        splitString[j].substring(i, i+7), null, 2);

                    //查無此商品
                    if ( itemPack==null ) {
                        TransferInActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MessageBox.show("", "Pno code error.", mContext, "Return");
                            }
                        });
                    }
                    //加入購物車
                    else{
                        //String SerialCode, String monyType, int price, String itemName,
                        // int stock, int qt
                        adapter.addItem(
                            itemPack.items[0].ItemCode,
                            itemPack.items[0].SerialCode, "US", 0.0,
                            itemPack.items[0].ItemName, 0,
                            Integer.valueOf(splitString[j].substring(i-2, i))
                        );
                    }
                }
            }

            adapter.notifyDataSetChanged();

            //紀錄單據號碼與來源車櫃
            transferNo= splitString[0];
            transferFrom= splitString[1];

            //顯示轉入的品項與車號
            txtFrom.setText("From: "+ splitString[1]);
            txtTo.setText("To: "+splitString[2]);

        }catch (Exception e){
            e.printStackTrace();
            MessageBox.show("", "Add item error", mContext, "Return");
        }
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
                MessageBox.show("", "Zoom in image error", mContext, "Return");
            }
        }
    };


    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            soundpool.play(soundid, 1, 1, 0, 0, 1);
            mVibrator.vibrate(100);

            byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
            int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
            byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte) 0);
            android.util.Log.i("debug", "----codetype--" + temp);
            barcodeStr = new String(barcode, 0, barcodelen);

            addItem(barcodeStr);

            barcodeStr=null;
        }

    };


    // Scan init
    private void initScan() {

        mScanManager = new ScanManager();
        mScanManager.openScanner();

        mScanManager.switchOutputMode( 0);
        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
        soundid = soundpool.load("/etc/Scan_new.ogg", 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initScan();

        IntentFilter filter = new IntentFilter();
        int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
        String[] value_buf = mScanManager.getParameterString(idbuf);
        if(value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
            filter.addAction(value_buf[0]);
        } else {
            filter.addAction(SCAN_ACTION);
        }

        registerReceiver(mScanReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mScanManager != null) {
            mScanManager.stopDecode();
        }
        unregisterReceiver(mScanReceiver);
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

}