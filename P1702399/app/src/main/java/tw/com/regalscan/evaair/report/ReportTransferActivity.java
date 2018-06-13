package tw.com.regalscan.evaair.report;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.regalscan.sqlitelibrary.TSQL;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.R;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.TransferItemPack;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;

public class ReportTransferActivity extends AppCompatActivity {
    private Button btnReturn, btnPrint;
    public Context mContext;
    public Activity mActivity;
    private Spinner spinnerTransfer, spinnerReceipt;
    private TextView txtFrom, txtTo;
    private ItemListPictureModifyAdapter adapter;
    private ListView itemListView;


    //所有的Transfer單據資料
    private TransferItemPack transferInPack, transferOutPack;
    private ArrayAdapter<String> inList, outList;
    private ProgressDialog mloadingDialog;
    private String transferNo="", outString= "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_transfer);





      // 整個pack
        Bundle argument = getIntent().getExtras();
        if(argument!=null){
          String itenString01 = argument.getString("jasonTransferOutPack");
          String itenString02 = argument.getString("jasonTransferInPack");
          Gson gson = new Gson();
          transferOutPack=  gson.fromJson(itenString01, TransferItemPack.class);
          transferInPack=  gson.fromJson(itenString02, TransferItemPack.class);
          init();
        }else{
          MessageBox.show("", "Get transfer info error", mContext, "Return");
          finish();
        }
    }

    private void init() {
        mContext = this;
        mActivity = this;
        enableExpandableList();

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
            }
        });

        //From, To
        txtFrom= findViewById(R.id.txtFrom);
        txtTo= findViewById(R.id.txtTo);

        //物品清單
        itemListView= findViewById(R.id.lvItemList);
        adapter = new ItemListPictureModifyAdapter(mContext, itemListView);
        adapter.setIsModifiedItem(false);
        adapter.setIsPictureZoomIn(false);
        adapter.setIsRightTwoVisible(false);
        adapter.setIsMoneyVisible(false);
        itemListView.setAdapter(adapter);

        // 選擇Transfer In or Out
        spinnerTransfer = findViewById(R.id.spinner01);
        // 單號
        spinnerReceipt = findViewById(R.id.spinner02);
        // Transfer In/ Out List
        ArrayAdapter<String> typeList = new ArrayAdapter<String>(this, R.layout.spinner_item);

       // 暫存list
        ArrayList<String> tmp_IN= new ArrayList<>();
        ArrayList<String> tmp_OUT= new ArrayList<>();

        //加入transfer in
        inList = new ArrayAdapter<String>(this, R.layout.spinner_item);
        // 將所有單據號碼加入String[]
        // item是散的, 要比對所有的ReceiptNo, 同一張單只顯示一次
        if(transferInPack.transfers!=null){
          for( int i=0 ;i <transferInPack.transfers.length; i++ ){
            if(i==0 || !transferInPack.transfers[i].ReceiptNo.equals(transferInPack.transfers[i-1].ReceiptNo) ){
              //              inList.add(transferInPack.transfers[i].ReceiptNo);
              tmp_IN.add(transferInPack.transfers[i].ReceiptNo);
            }
          }
          typeList.add("Transfer In");
        }

        outList= new ArrayAdapter<String>(this, R.layout.spinner_item);
        // 檢查transfer out
        if(transferOutPack.transfers!=null){
          for( int i=0 ;i <transferOutPack.transfers.length; i++ ){
            if(i==0 || !transferOutPack.transfers[i].ReceiptNo.equals(transferOutPack.transfers[i-1].ReceiptNo) ){
              //              outList.add(transferOutPack.transfers[i].ReceiptNo);
              tmp_OUT.add(transferOutPack.transfers[i].ReceiptNo);
            }
          }
          typeList.add("Transfer Out");
        }
        spinnerTransfer.setAdapter(typeList);

        // 重新排序
        tmp_IN= Tools.resortListNo(tmp_IN);
        tmp_OUT= Tools.resortListNo(tmp_OUT);

        for(String s: tmp_IN){
          inList.add(s);
        }
        for(String s: tmp_OUT){
          outList.add(s);
        }

        // 根據in or out顯示可選擇的item
        spinnerTransfer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSpinnerReceiptNo();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        // 根據單據號碼選擇顯示的item與from to
        spinnerReceipt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(spinnerTransfer.getSelectedItem().toString().equals("Transfer In")){
                    setSpinnerItem( "IN",  inList.getItem(position) );
                }
                else if(spinnerTransfer.getSelectedItem().toString().equals("Transfer Out")) {
                    setSpinnerItem( "OUT", outList.getItem(position) );
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //btn
        btnPrint = findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              mloadingDialog= ProgressDialog.show(mContext, "", "Processing...", true, false);
              //列印收據
              String ReceiptNo= spinnerReceipt.getSelectedItem().toString();
              try{
                if(spinnerTransfer.getSelectedItem().toString().equals("Transfer In")){
                  for(int i=0; i<transferInPack.transfers.length; i++){
                    if(transferInPack.transfers[i].ReceiptNo.equals(ReceiptNo)){
                      transferNo= transferInPack.transfers[i].TransferNo;
                      break;
                    }
                  }
                  printData(true);
                }
                else{
                  StringBuilder outBuilder= new StringBuilder();

                  //  車櫃號後三碼 + 時分秒(六碼) + , + 來源車櫃(六碼) + , + 目的車櫃(六碼) + , +  數量(二碼) + Code(六碼) ....接續
                  //  EX: 2A1 095735 , 882957 , 102809 , 01 XG4122 , 01 XG4221
                  for(int i=0; i<transferOutPack.transfers.length; i++){
                    if(transferOutPack.transfers[i].ReceiptNo.equals(ReceiptNo)){
                      // 取得TrnasferNo, from, to
                      transferNo= transferOutPack.transfers[i].TransferNo;
                      outBuilder.append(transferOutPack.transfers[i].TransferNo);
                      outBuilder.append(",");
                      outBuilder.append(transferOutPack.transfers[i].CarFrom);
                      outBuilder.append(",");
                      outBuilder.append(transferOutPack.transfers[i].CarTo);
                      break;
                    }
                  }
                  for(int i=0; i<transferOutPack.transfers.length; i++){
                    if(transferOutPack.transfers[i].ReceiptNo.equals(ReceiptNo)){
                      String qty;
                      if(transferOutPack.transfers[i].Qty.length()<2){
                        qty= "0"+transferOutPack.transfers[i].Qty;
                      }else{
                        qty= transferOutPack.transfers[i].Qty;
                      }
                      outBuilder.append(",");
                      outBuilder.append( qty + transferOutPack.transfers[i].ItemCode );
                    }
                  }
                  outString= outBuilder.toString();
                  printData(false);
                }

              }catch (Exception ex){
                ex.printStackTrace();
                TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                _TSQL.WriteLog(FlightData.SecSeq,
                    "System", "ReportTransferActivity.java", "printTransferOut", ex.getMessage());
                outString="";
                transferNo="";
                return;
              }
            }
        });
        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }



  private static class PrinterHandler extends Handler {
    private WeakReference<ReportTransferActivity> weakActivity;
    PrinterHandler(ReportTransferActivity a) {
      weakActivity = new WeakReference<>(a);
    }
    @Override
    public void handleMessage(Message msg) {
      ReportTransferActivity handlerActivity = weakActivity.get();
      Context handlerContext= handlerActivity.mContext;

      handlerActivity.mloadingDialog.dismiss();
      switch(msg.what){
        case 1: // 沒紙(Transfer Out)
          if(MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No") ) handlerActivity.printData(true);
          else handlerActivity.doPrintFinal();
          break;

        case 2: // 沒紙(Transfer In)
          if(MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No") ) handlerActivity.printData(false);
          else handlerActivity.doPrintFinal();
          break;

        case 3: //Print error (Transfer Out)
          if(MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) handlerActivity.printData(true);
          else handlerActivity.doPrintFinal();
          break;

        case 4: //Print error (Transfer In)
          if(MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) handlerActivity.printData(false);
          else handlerActivity.doPrintFinal();
          break;

        case 5:
          handlerActivity.doPrintFinal();
          break;
      }
    }
  }

//  private Handler mHandler = new Handler(){
//    @Override
//    public void handleMessage(Message msg) {
//      mloadingDialog.dismiss();
//      switch(msg.what){
//        case 1: // 沒紙(Transfer Out)
//          if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData(true);
//          else doPrintFinal();
//          break;
//
//        case 2: // 沒紙(Transfer In)
//          if(MessageBox.show("", "No paper, reprint?", mContext, "Yes", "No") ) printData(false);
//          else doPrintFinal();
//          break;
//
//        case 3: //Print error (Transfer Out)
//          if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData(true);
//          else doPrintFinal();
//          break;
//
//        case 4: //Print error (Transfer In)
//          if(MessageBox.show("", "Print error, retry?", mContext, "Yes", "No")) printData(false);
//          else doPrintFinal();
//          break;
//
//        case 5:
//          doPrintFinal();
//          break;
//      }
//    }
//  };

    private void doPrintFinal(){
      ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
    }

    private void printData(final boolean isTransferOut){
    mloadingDialog.dismiss();
    mloadingDialog= ProgressDialog.show(mContext, "", "Processing...", true, false);
      Handler printerHandler = new PrinterHandler(this);
    new Thread() {
      public void run() {
        PrintAir printer= new PrintAir(mContext, Integer.valueOf(FlightData.SecSeq));
        try{
          if(isTransferOut) {
            if( printer.printTransferIn(transferNo)==-1){
              printerHandler.sendMessage(Tools.createMsg(1));
            }else{
              printerHandler.sendMessage(Tools.createMsg(5));
            }
          }else {
            if (printer.printTransferOut(transferNo, outString) == -1) {
              printerHandler.sendMessage(Tools.createMsg(2));
            } else {
              printerHandler.sendMessage(Tools.createMsg(5));
            }
          }
        }catch (Exception e) {
          e.printStackTrace();
          TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
          _TSQL.WriteLog(FlightData.SecSeq,
              "System", "ReportTransferActivity", "printTransferIn", e.getMessage());
          if(isTransferOut){
            printerHandler.sendMessage(Tools.createMsg(3));
          }else{
            printerHandler.sendMessage(Tools.createMsg(4));
          }
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

    private void setSpinnerReceiptNo(){
        // Transfer in
        if(spinnerTransfer.getSelectedItem().toString().equals("Transfer In")){
            if(transferInPack.transfers==null){
                MessageBox.show("", "No transfer in list", mContext, "Return");
                spinnerReceipt.setEnabled(false);
            }else{
                // 將spinner內容設定為 in 的第一筆
                spinnerReceipt.setAdapter(inList);
                spinnerReceipt.setEnabled(true);
                spinnerReceipt.setSelection(0);
            }
        }

        // Transfer out
        else if(spinnerTransfer.getSelectedItem().toString().equals("Transfer Out")) {
            if(transferOutPack.transfers==null){
                MessageBox.show("", "No transfer out list", mContext, "Return");
                spinnerReceipt.setEnabled(false);
            }else{
                // 將spinner內容設定為 out 的第一筆
                spinnerReceipt.setAdapter(outList);
                spinnerReceipt.setEnabled(true);
                spinnerReceipt.setSelection(0);
            }
        }
    }

    private void setSpinnerItem( String intentString, String RecipeNo ){
        //載入adapter清單
        adapter.clear();

        // Transfer In
        if(intentString.equals("IN")){
            for(DBQuery.TransferItem trans : transferInPack.transfers){

                //當選擇的ReceiptNo與Pack內的item ReceiptNo相同，加入adapter
                if( trans.ReceiptNo .equals(RecipeNo) ){
                    //轉出轉入車號
                    txtFrom.setText("From: " + trans.CarFrom);
                    txtTo.setText("To: " + trans.CarTo);

                    //String SerialCode, String monyType, int price, String itemName, int stock, int qt
                    adapter.addItem( trans.ItemCode, trans.SerialCode, "US", 0.0,
                            trans.ItemName, 0, Integer.valueOf(trans.Qty) );
                }
            }
        }

        // Transfer Out
        else{
            for(DBQuery.TransferItem trans : transferOutPack.transfers){
                //當選擇的ReceiptNo與Pack內的item ReceiptNo相同，加入adapter
                if( trans.ReceiptNo .equals(RecipeNo) ){
                    //轉出轉入車號
                    txtFrom.setText("From: " + trans.CarFrom);
                    txtTo.setText("To: " + trans.CarTo);

                    //String SerialCode, String monyType, int price, String itemName, int stock, int qt
                    adapter.addItem( trans.ItemCode, trans.SerialCode, "US", 0.0,
                            trans.ItemName, 0, Integer.valueOf(trans.Qty) );
                }
            }
        }
        adapter.notifyDataSetChanged();
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