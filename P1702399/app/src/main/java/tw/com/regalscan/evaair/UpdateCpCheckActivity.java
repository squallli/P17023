package tw.com.regalscan.evaair;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.regalscan.sqlitelibrary.TSQL;
import org.json.JSONArray;
import org.json.JSONObject;
import tw.com.regalscan.R;
import tw.com.regalscan.activities.ItemDetailActivity;
import tw.com.regalscan.activities.ItemPictureActivity;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.RFIDReaderService;
import tw.com.regalscan.component.SwipeListView;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.CrewInfo;
import tw.com.regalscan.utils.Constant;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;


public class UpdateCpCheckActivity extends AppCompatActivity {


    private EditText editId, editPw;
    public Context mContext;
    public Activity mActivity;
    private SwipeListView itemListView;
    private HashMap<String, ItemInfo> modifiedList;
    private TextView txtSector, txtCAId;

    private boolean isitetmClickModified = false;
    public ItemListPictureModifyAdapter adapter;
    //調整數量視窗
    public final int ITEMS_DETAIL = 500;
    private InputMethodManager imm;
    private ProgressDialog mloadingDialog;
    private PrintAir printer;
    private RFIDReaderService mRFIDReaderService;


    private static class RFIDHandler extends Handler {
        private WeakReference<UpdateCpCheckActivity> weakActivity;

        RFIDHandler(UpdateCpCheckActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            UpdateCpCheckActivity handlerActivity = weakActivity.get();
            Context handlerContext = handlerActivity.getApplicationContext();

            switch (msg.what) {
                case RFIDReaderService.MSG_SHOW_BLOCK_DATA:
                    String UID = msg.getData().getString(RFIDReaderService.CARD_UID);
                    //員工證號
                    String BlockData = msg.getData().getString(RFIDReaderService.CARD_BLOCK_DATA);
                    //"EVA" or "EGAS", UID和BlockData為null的話就是ErrorString
                    String EMPLOYEE_TYPE = msg.getData().getString(RFIDReaderService.EMPLOYEE_TYPE);

                    if (UID != null && BlockData != null) {

                        StringBuilder err = new StringBuilder();
                        CrewInfo CP = DBQuery.getGetCrewPassword(handlerContext, err, BlockData);
                        if (CP == null) {
                            MessageBox.show("", "Please check ID", handlerContext, "Return");
                            return;
                        }

                        handlerActivity.editId.setText(BlockData);
                        handlerActivity.editPw.setText(CP.Password);
                        handlerActivity.certificateCP();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_cp_check);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            //取得所有須調整物品
            modifiedList = (HashMap<String, ItemInfo>)bundle.getSerializable("UpdateList");
            init();
        }
    }

    @Override
    public void onStart() {
        mRFIDReaderService.start();
        super.onStart();
    }

    private void init() {
        mContext = this;
        mActivity = this;
        imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
        Handler rfidHandler = new RFIDHandler(this);
        mRFIDReaderService = new RFIDReaderService(mActivity, rfidHandler);

        //btn
        Button btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
        });

        //飛機起降地點資訊
        txtSector = findViewById(R.id.txtSector);
        String sectorInfo = FlightData.Sector;

        //文字加底線
        SpannableString txtUnderlineSector = new SpannableString(sectorInfo);
        txtUnderlineSector.setSpan(new UnderlineSpan(), 0, sectorInfo.length(), 0);
        txtSector.setText(txtUnderlineSector);

        //CA ID
        txtCAId = findViewById(R.id.txtCAId);
        String caId = FlightData.CrewID;
        SpannableString txtUnderlineCAId = new SpannableString(caId);
        txtUnderlineCAId.setSpan(new UnderlineSpan(), 0, caId.length(), 0);
        txtCAId.setText(txtUnderlineCAId);

        //調整物品清單
        itemListView = findViewById(R.id.lvItemList);
        adapter = new ItemListPictureModifyAdapter(mContext, itemListView, itemListView.getRightViewWidth());
        adapter.setIsMoneyVisible(false);
        adapter.setItemInfoClickListener(itemInformationClickListener);
        adapter.setFilpperFunctionClickListener(itemPictureClickListener);
        //滑動刪除
        adapter.setItemSwipeListener(position -> {
            try {
                // 從adapter內刪除，同時從HashMap內刪除
                modifiedList.remove(adapter.getItem(position).getItemCode());
                adapter.removeItem(position);
                adapter.notifyDataSetChanged();
                itemListView.hiddenRight(itemListView.mPreItemView);
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Delete item error", mContext, "Return");
            }
        });
        itemListView.setAdapter(adapter);

        //將item放入listView
        if (modifiedList != null) {
            for (Object key : modifiedList.keySet()) {
                ItemInfo item = modifiedList.get(key);

                //String SerialCode, String moneyType, int price, String itemName, int stock, int qt
                adapter.addItem(item.getItemCode(), item.getSerialNo(), item.getMonyType(), item.getPrice(), item.getItemName(), item.getStock(), (Integer)item.getQty()
                );
            }
        }

        // CPId, CPPw輸入框
        editId = findViewById(R.id.editId);
        editPw = findViewById(R.id.editPassword);
//        editId.setText(FlightInfoManager.getInstance().getLoginNumber());
//        editPw.setText("0222");

        Button btnSave = findViewById(R.id.btnSvae);
        btnSave.setOnClickListener(view -> certificateCP());
        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }

    private void certificateCP() {
        imm.hideSoftInputFromWindow(editPw.getWindowToken(), 0);
        StringBuilder err = new StringBuilder();

        final String CPId = editId.getText().toString();
        final String CPPw = editPw.getText().toString();

        //確認調整清單不為空
        if (adapter.getCount() == 0) {
            MessageBox.show("", "Update list is empty", UpdateCpCheckActivity.this, "Return");
            return;
        }
        //確認cp id & pw 不為空
        if (CPId.equals("")) {
            MessageBox.show("", "Please input CP ID", UpdateCpCheckActivity.this, "Return");
            return;
        }
        if (CPPw.equals("")) {
            MessageBox.show("", "Please input CP password", UpdateCpCheckActivity.this, "Return");
            return;
        }

        if (MessageBox.show("", "Confirm update qty is correct", UpdateCpCheckActivity.this, "Yes", "No")) {
            mloadingDialog = ProgressDialog.show(mContext, "", "Updating...", true, false);
            try {

                //驗證CP帳號密碼, 5
                if (DBQuery.getGetCrewInfo(mContext, err, CPId, CPPw, "CP") == null) {
                    mloadingDialog.dismiss();
                    MessageBox.show("", "Please check ID and password", mContext, "Return");
                    return;
                }

                if (DBQuery.checkEmployee(mContext, err, CPId) == null) {
                    mloadingDialog.dismiss();
                    MessageBox.show("", "Not employee.", mContext, "Return");
                    return;
                }

                if (!FlightData.PurserID.equals(CPId)) {
                    mloadingDialog.dismiss();
                    MessageBox.show("", "Wrong CP", UpdateCpCheckActivity.this, "Return");
                    return;
                }

                //調整完後呼叫13
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject;
                for (int i = 0; i < adapter.getCount(); i++) {
                    jsonObject = new JSONObject();
                    jsonObject.put("ItemCode", adapter.getItem(i).getItemCode());
                    jsonObject.put("AdjustQty", adapter.getItem(i).getQty()); //傳入調整後總量
                    jsonArray.put(jsonObject);
                }

                if (!DBQuery.adjustItemQty(mContext, err, jsonArray)) {
                    UpdateCpCheckActivity.this.runOnUiThread(() -> {
                        mloadingDialog.dismiss();
                        MessageBox.show("", "Error adjust item, please retry", mContext, "Return");
                    });
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                UpdateCpCheckActivity.this.runOnUiThread(() -> {
                    mloadingDialog.dismiss();
                    MessageBox.show("", "Update error", mContext, "Return");
                });
                return;
            }

            boolean flagPrint = false;
            DBQuery.ItemDataPack itemDataPack = DBQuery.getAdjustInfo(mContext, err, FlightData.SecSeq, null, null, 0);
            if (itemDataPack != null && itemDataPack.items != null) {
                for (int i = 0; i < itemDataPack.items.length; i++) {
                    if (itemDataPack.items[i].StartQty != itemDataPack.items[i].EndQty) {
                        flagPrint = true;
                        break;
                    }
                }
            }
            if (flagPrint) {
                printData();
            } else {
                doPrintFinal();
            }
        }
    }


    private static class PrinterHandler extends Handler {
        private WeakReference<UpdateCpCheckActivity> weakActivity;

        PrinterHandler(UpdateCpCheckActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            UpdateCpCheckActivity handlerActivity = weakActivity.get();
            Context handlerContext = handlerActivity.mContext;

            handlerActivity.mloadingDialog.dismiss();
            switch (msg.what) {
                case 1: // 沒紙
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No"))
                        handlerActivity.printData();
                    else handlerActivity.doPrintFinal();
                    break;

                case 2: //Print error
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No"))
                        handlerActivity.printData();
                    else handlerActivity.doPrintFinal();
                    break;

                case 3: //成功
                    handlerActivity.doPrintFinal();
                    break;
            }
        }
    }

    private void doPrintFinal() {
        if (ActivityManager.isActivityInStock("UpdateActivity")) {
            ActivityManager.removeActivity("UpdateActivity");
        }
        ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
    }

    private void printData() {
        mloadingDialog.dismiss();
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        new Thread() {
            public void run() {
                printer = new PrintAir(mContext, Integer.valueOf(FlightData.SecSeq));
                try {
                    if (printer.printUpdateList() == -1) {
                        printerHandler.sendMessage(Tools.createMsg(1));
                    } else {
                        printerHandler.sendMessage(Tools.createMsg(3));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                    _TSQL.WriteLog(FlightData.SecSeq,
                        "System", "UpdateCpCheckActivity", "printUpdateList", e.getMessage());
                    printerHandler.sendMessage(Tools.createMsg(2));
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //調整數量
        if ((requestCode == ITEMS_DETAIL) && resultCode == RESULT_OK) {
            try {
                int index = Integer.valueOf((String)data.getExtras().get("index"));
                String newQty = (String)data.getExtras().get("newQty");
                ItemInfo modifiedItem = adapter.getItem(index);

                adapter.modifiedItemChange(
                    index,
                    Integer.valueOf(newQty),
                    modifiedItem.getStock());
                // 調整後的該商品以紅字顯示
                adapter.modifiedItemColorChange(index, true);

                adapter.notifyDataSetChanged();

            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Modify item error", mContext, "Return");
                return;
            }
        }
        isitetmClickModified = false;

        super.onActivityResult(requestCode, resultCode, data);
    }


    //點選 Item 項目修改內容
    private ItemListPictureModifyAdapter.ItemInfoClickListener itemInformationClickListener
        = new ItemListPictureModifyAdapter.ItemInfoClickListener() {
        @Override
        public void txtItemInfoClickListener(int position) {
            try {
                //如果正在編輯就不能觸發第二次的activity彈跳視窗
                if (!isitetmClickModified) {
                    isitetmClickModified = true;

                    ItemInfo item = adapter.getItem(position);
                    Intent intent = new Intent();
                    intent.setClass(mActivity, ItemDetailActivity.class);

                    //String itemCode, String moneyType, int price, String itemName, int stock, int qt
                    intent.putExtra("item", item);
                    intent.putExtra("index", String.valueOf(position));
                    intent.putExtra("canModifiedToZero", "true");
                    intent.putExtra("fromWhere", "UpdateCpCheckActivity");
                    startActivityForResult(intent, ITEMS_DETAIL);
                }
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Modify item error", mContext, "Return");
            }
        }
    };


    //點選 Item 圖片放大圖片
    private ItemListPictureModifyAdapter.ItemListFunctionClickListener itemPictureClickListener
        = new ItemListPictureModifyAdapter.ItemListFunctionClickListener() {

        @Override
        public void toolItemFunctionClickListener(String itemInfo) {
            try {
                ItemInfo item = adapter.getItem(itemInfo);
                Intent intent = new Intent();
                intent.setClass(mActivity, ItemPictureActivity.class);
                intent.putExtra("itemCode", item.getItemCode());
                mActivity.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Zoom in item image error", mContext, "Return");
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();

        mRFIDReaderService.start();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.PRINT_ACTION);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mRFIDReaderService.Dispose();
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

    @Override
    protected void onDestroy() {
        mRFIDReaderService.Dispose();
        super.onDestroy();
    }
}
