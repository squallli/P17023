package tw.com.regalscan.evaair.upgrade;

import java.util.*;

import android.app.Activity;
import android.app.Service;
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
import android.widget.*;

import com.google.gson.Gson;
import tw.com.regalscan.R;
import tw.com.regalscan.activities.CPCheckActivity;
import tw.com.regalscan.activities.ItemUpgradeActivity;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.adapters.ItemListUpgradeAdapter;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.component.SwipeListView;
import tw.com.regalscan.customClass.ItemUpgradeInfo;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.UpgradeTransaction;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.UpgradeItemPack;
import tw.com.regalscan.utils.Tools;

public class UpgradeBasketActivity extends AppCompatActivity {


    private EditText editQty;
    public ItemListUpgradeAdapter adapter;
    private SwipeListView itemListView;
    private Button btnReturn, btnPay, btnSold;
    public Context mContext;
    public Activity mActivity;
    private Spinner spinnerIdentity, spinnerFrom, spinnerTo, spinnerMoney;
    private boolean isitetmClickModified = false;
    public final int ITEMS_DETAIL = 500;
    private TextView txtTotalMoney;

    //升艙等資訊
    private DBQuery.UpgradeProductInfoPack productPack;
    private ArrayAdapter<String> identityList, fromList, toList, listCurrency;

    //鍵盤
    private InputMethodManager imm;
    // 交易function
    public static UpgradeTransaction s_UpgradeTransaction;
    private UpgradeItemPack upgradePack;
    private Set<String> className;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_basket);
        init();
    }

    private void init() {
        mContext = this;
        mActivity = this;
        enableExpandableList();
        imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
            if (!ActivityManager.isActivityInStock("UpgradeActivity01")) {
                Intent mIntent03 = new Intent(mActivity, UpgradeActivity01.class);
                mActivity.startActivity(mIntent03);
            }
        });


        final StringBuilder err = new StringBuilder();
        try {
            //建立交易
            s_UpgradeTransaction = new UpgradeTransaction(mContext, FlightData.SecSeq);
            upgradePack = DBQuery.modifyUpgradeBasket(err, s_UpgradeTransaction.GetBasketInfo());
            if (upgradePack == null) {
                MessageBox.show("", "Get upgrade info error", mContext, "Return");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get upgrade info error", mContext, "Return");
            return;
        }

        //艙等身分
        spinnerIdentity = findViewById(R.id.spinner03);
        //From, To
        spinnerFrom = findViewById(R.id.spinner01);
        spinnerTo = findViewById(R.id.spinner02);
        fromList = new ArrayAdapter<String>(this, R.layout.spinner_itemcenter_text_18sp);
        toList = new ArrayAdapter<String>(this, R.layout.spinner_itemcenter_text_18sp);
        spinnerFrom.setAdapter(fromList);
        spinnerTo.setAdapter(toList);

        //取得座艙種類, 27
        productPack = DBQuery.getUpgradeProductInfo(mContext, err);
        if (productPack == null) {
            UpgradeBasketActivity.this.runOnUiThread(() -> MessageBox.show("", "Query upgrade data error", mContext, "Return"));
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
            return;
        }
        // 將升等艙身分放入spinner
        if (productPack.items != null) {
            identityList = new ArrayAdapter<String>(this, R.layout.spinner_item);
            // 過濾重複物件
            className = new HashSet<>();
            for (int i = 0; i < productPack.items.length; i++) {
                className.add(productPack.items[i].Infant);
            }
            // 排序
            List<String> sortedList = new ArrayList<String>(className);
            Collections.sort(sortedList);
            // 放入Adapter
            for (String s : sortedList) {
                identityList.add(s);
            }
            spinnerIdentity.setAdapter(identityList);
        }

        //依照選擇的身分將from, to的spinner填滿
        spinnerIdentity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setFromSpinner(productPack.items[position].Infant);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //依照選擇的 from艙等切換to艙等
        spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setToSpinner(spinnerFrom.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        //取得所有可使用的幣別

        //設定下拉幣別選單
        spinnerMoney = findViewById(R.id.spinner04);
        listCurrency = new ArrayAdapter<String>(this, R.layout.spinner_item);
        listCurrency.add("USD");
        listCurrency.add("TWD");
        spinnerMoney.setAdapter(listCurrency);
        spinnerMoney.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                txtTotalMoney.setText(setTotalMoneyTextView());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        //items
        itemListView = findViewById(R.id.lvItemList);
        adapter = new ItemListUpgradeAdapter(mContext, itemListView.getRightViewWidth());
        adapter.setIsSwipeDelete(true);
        adapter.setItemSwipeListener(new ItemListUpgradeAdapter.ItemSwipeDeleteListener() {
            @Override
            public void swipeDeleteListener(int position) {
                ItemUpgradeInfo item = adapter.getItem(position);
                // String identity, String from, String to, String qtyString
                getModifiedItem(item.getIdentity(), item.getFrom(), item.getTo(), 0 - item.getQty());

                itemListView.hiddenRight(itemListView.mPreItemView);
            }
        });
        adapter.setIsModifiedItem(true);
        adapter.setItemInfoClickListener(itemInformationClickListener);
        itemListView.setAdapter(adapter);

        //總額
        txtTotalMoney = findViewById(R.id.txtTotalMoney);
        editQty = findViewById(R.id.editQty);

        //btn
        btnSold = findViewById(R.id.btnSold);
        btnSold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //數量為空
                if (editQty.getText().toString().trim().equals("") || editQty.getText().toString().trim().equals("0")) {
                    MessageBox.show("", "Qty error", UpgradeBasketActivity.this, "Return");
                    return;
                }
                //物品已在購物車內就不動作
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (adapter.getItem(i).getIdentity().equals(spinnerIdentity.getSelectedItem().toString()) &&
                        adapter.getItem(i).getFrom().equals(spinnerFrom.getSelectedItem().toString()) &&
                        adapter.getItem(i).getTo().equals(spinnerTo.getSelectedItem().toString())) {
                        return;
                    }
                }
                try {
                    getModifiedItem(spinnerIdentity.getSelectedItem().toString(),
                        spinnerFrom.getSelectedItem().toString(), spinnerTo.getSelectedItem().toString(),
                        Integer.valueOf(editQty.getText().toString().trim()));
                } catch (Exception e) {
                    e.printStackTrace();
                    MessageBox.show("", "Get item error", mContext, "Return");
                    return;
                }
                editQty.setText("");
            }
        });

        btnPay = findViewById(R.id.btnPay);
        btnPay.setOnClickListener(view -> {
            //adapter不可為空
            if (adapter.getCount() == 0) {
                MessageBox.show("", "Basket can't null", UpgradeBasketActivity.this, "Return");
                return;
            }
            try {
                //傳遞整包BasketItemPack
                Bundle bundle = new Bundle();
                Gson gson = new Gson();
                String jsonBasketItemPack = gson.toJson(upgradePack);
                bundle.putString("Currency", spinnerMoney.getSelectedItem().toString());
                bundle.putString("UpgradeItemPack", jsonBasketItemPack);
                bundle.putString("fromWhere", "UpgradeBasketActivity");

                Intent intent = new Intent(mActivity, CPCheckActivity.class);
                intent.putExtras(bundle);
                mActivity.startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Get item data error", mContext, "Return");
            }
        });

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }

    // 取得應付款總額
    private String setTotalMoneyTextView() {
        try {
            String currency = spinnerMoney.getSelectedItem().toString();
            if (upgradePack != null && adapter.getCount() > 0) {
                // 先呼叫一次，取得總應付金額，這樣 GetCurrencyMaxAmount 才取得到值
                s_UpgradeTransaction.GetPaymentMode();

                DBQuery.ShouldPayMoney payItem = DBQuery.getPayMoneyNow(new StringBuilder(),
                    s_UpgradeTransaction.GetCurrencyMaxAmount(currency));
                if (payItem == null || payItem.Currency == null) {
                    MessageBox.show("", "Get pay info error", mActivity, "Return");
                    return "";
                }
                if (adapter.getCount() != 0)
                    return (currency + " " + String.valueOf(payItem.MaxPayAmount));
                else
                    return (currency + " 0");
            } else {
                if (currency.equals("USD")) {
                    return "USD 0";
                } else {
                    return "TWD 0";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Set currency error", mContext, "Return");
            return "";
        }
    }


    public void getModifiedItem(String identity, String from, String to, int qty) {
        imm.hideSoftInputFromWindow(editQty.getWindowToken(), 0);
        try {
            StringBuilder err = new StringBuilder();
            upgradePack = DBQuery.modifyUpgradeBasket(err,
                s_UpgradeTransaction.ModifyItemList(identity, from, to, qty));
            if (upgradePack == null) {
                MessageBox.show("", "Get upgrade info error", mContext, "return");
                return;
            }

            /*-------- 將所有item放入adapter --------*/
            adapter.clear();
            for (int i = 0; i < upgradePack.items.length; i++) {
                adapter.addItem(upgradePack.items[i].Infant, upgradePack.items[i].OriginalClass,
                    upgradePack.items[i].NewClass, upgradePack.items[i].SalesQty,
                    upgradePack.items[i].USDPrice * upgradePack.items[i].SalesQty);
            }
            adapter.notifyDataSetChanged();
            txtTotalMoney.setText(setTotalMoneyTextView());

        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get upgrade info error", mContext, "Return");
        }
    }


    private void enableExpandableList() {
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ExpandableListView expandableListView = findViewById(R.id.expendlist);
        NavigationDrawer navigationDrawer = new NavigationDrawer(mActivity, mContext, drawerLayout, toolbar, expandableListView);
    }


    //設定該身分別有哪些升艙等的選項
    private void setFromSpinner(String chooseInfant) {
        fromList.clear();
        className = new HashSet<>();
        for (int i = 0; i < productPack.items.length; i++) {
            if (productPack.items[i].Infant.equals(chooseInfant)) {
                className.add(productPack.items[i].OriginalClass);
            }
        }
        for (String s : className) {
            fromList.add(s);
        }
        fromList.notifyDataSetChanged();
    }

    //設定目標艙等選項
    private void setToSpinner(String fromString) {
        toList.clear();
        className = new HashSet<>();
        for (int i = 0; i < productPack.items.length; i++) {
            if (productPack.items[i].OriginalClass.equals(fromString) &&
                productPack.items[i].Infant.equals(spinnerIdentity.getSelectedItem().toString())) {
                className.add(productPack.items[i].NewClass);
            }
        }
        for (String s : className) {
            toList.add(s);
        }
        toList.notifyDataSetChanged();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //調整數量
        if ((requestCode == ITEMS_DETAIL) && resultCode == RESULT_OK) {
            try {
                int index = Integer.valueOf((String)data.getExtras().get("index"));
                int modiQty = Integer.valueOf((String)data.getExtras().get("newQty")) -
                    adapter.getItem(index).getQty();

                if (modiQty != 0) {
                    // 包成物件回傳
                    // String identity, String from, String to, String qtyString
                    getModifiedItem(adapter.getItem(index).getIdentity(), adapter.getItem(index).getFrom(),
                        adapter.getItem(index).getTo(), modiQty);
                }
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

                    ItemUpgradeInfo item = adapter.getItem(position);
                    Intent intent = new Intent();
                    intent.setClass(mActivity, ItemUpgradeActivity.class);

                    intent.putExtra("index", String.valueOf(position));
                    intent.putExtra("item", item);
                    startActivityForResult(intent, ITEMS_DETAIL);
                }
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Modify item error", mContext, "Return");
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