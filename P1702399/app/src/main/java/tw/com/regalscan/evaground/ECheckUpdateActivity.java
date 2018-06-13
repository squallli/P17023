package tw.com.regalscan.evaground;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
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
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;
import tw.com.regalscan.R;
import tw.com.regalscan.component.FlightInfoManager;
import tw.com.regalscan.db02.DBFunctions;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.evaground.Models.PreOrderInfo;
import tw.com.regalscan.evaground.Models.PreorderItems;
import tw.com.regalscan.utils.PrintGround;

public class ECheckUpdateActivity extends AppCompatActivity {

    private FragmentAdapter fragmentAdapter;

    private OnMainListener mainListener;

    private ViewPager mViewPager;
    private ArrayList<Fragment> fmlist = null;

    private String user, SecSeq;

    private Button mReturn, mSave;

    private DateTime EGASTimeStart = DateTime.now();
    private DateTime EGASTimeEnd = DateTime.now();

    PrintGround printGround = new PrintGround(ECheckUpdateActivity.this);

    //Scan
    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private int soundid;
    private String barcodeStr;

    //總回庫量
    public static ItemTotalAdapter adapterTotal;
    public static PreorderTotalAdapter adapterTotalPreorder;
    public static PreorderTotalAdapter adapterTotalVip;

    public static ItemListAdapter adapter;
    public static DiscrepancyListAdapter adapter2;
    public static PreorderListAdapter adapter3;
    public static PreorderListAdapter adapter4;

    public final String ItemsName = "Items";
    public final String DiscrepancyName = "Discrepancy";
    public final String PreorderName = "Preorder";
    public final String VipName = "Vip";

    public final int ITEMS_DETAIL = 500;
    public final int ITEMS_PICTURE = 501;
    public final int DISCREPANCY_ITEMS_DETAIL = 600;
    public final int DISCREPANCY_PREORDER_DETAIL = 601;

    public static Toolbar toolbar;

    private TextView mTitle, mTag1, mTag2, mTag3, mTag4;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private SearchView searchView;

    private ImageView mSearchIcon;

    private StringBuilder errMsg = new StringBuilder();

    private FragmentItem fg1;
    private FragmentDiscrepancy fg2;
    private FragmentPreorder fg3;
    private FragmentVip fg4;

    private TabLayout mTabLayout;

    private Activity mActivity;

    private Context mContext;

    private ProgressDialog mloadingDialog;



    private void InitializeComponent() {

        mViewPager = findViewById(R.id.myviewpager);

        mTag1 = findViewById(R.id.tag1);
        mTag1.setOnClickListener(new MyClickListener(0));
        mTag2 = findViewById(R.id.tag2);
        mTag2.setOnClickListener(new MyClickListener(1));
        mTag3 = findViewById(R.id.tag3);
        mTag3.setOnClickListener(new MyClickListener(2));
        mTag4 = findViewById(R.id.tag4);
        mTag4.setOnClickListener(new MyClickListener(3));

        mReturn = findViewById(R.id.btnReturn);
        mReturn.setOnClickListener(btnClick);
        mSave = findViewById(R.id.btnSave);
        mSave.setOnClickListener(btnClick);

        mSearchIcon = findViewById(R.id.iv_searchicon);
        mSearchIcon.setOnClickListener(onIconClick);

        mTitle = findViewById(R.id.tv_Title);

        FrameLayout frameLayout = findViewById(R.id.fl_topbar);
        frameLayout.bringToFront();

        //ItemsTotal
        adapterTotal = new ItemTotalAdapter(this);

        //PreorderTotal
        adapterTotalPreorder = new PreorderTotalAdapter(this);

        //VipTotal
        adapterTotalVip = new PreorderTotalAdapter(this);

        //items
        adapter = new ItemListAdapter(this,this, user);
        adapter.setFilpperFunctionClickListener(itemPictureClickListener);

        //Discrepancy
        adapter2 = new DiscrepancyListAdapter(this, user);
        adapter2.setFilpperFunctionClickListener(itemPictureClickListener2);

        //preorder
        adapter3 = new PreorderListAdapter(this, user);
        adapter3.setFilpperFunctionClickListener(itemPictureClickListener3);

        //vip
        adapter4 = new PreorderListAdapter(this, user);
        adapter4.setFilpperFunctionClickListener(itemPictureClickListener4);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbarCreate(toolbar);

        searchView = findViewById(R.id.sv_searchbox);

        mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout.setVisibility(View.INVISIBLE);
    }

    private void enableExpandableList() {

        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ExpandableListView expandableListView = findViewById(R.id.expendlist);

        new NavigationDrawer(this, this, drawerLayout, toolbar,
                expandableListView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaupdate_egascheck);

        Bundle bundle = this.getIntent().getExtras();
        user = bundle.getString("User");

        SecSeq = FlightInfoManager.getInstance().getCurrentSecSeq();

        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        InitializeComponent();
        initViewPager();

        //查詢已盤與未盤資料,載入Adapter
        refreshAdapter();

        if (user.equals("EGAS")) {
            EGASTimeStart = DateTime.now();
        }

        //設定標題
        if (user.equals("EVA")) {
            mTitle.setText("EVA Update");
        } else if (user.equals("EGAS")) {
            mTitle.setText("EGAS Check");
        }

        mActivity = this;
        mContext = this;

        //隱藏鍵盤
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onBackPressed() {
    }

    //側邊選單建造
    public void toolbarCreate(Toolbar toolbar) {
        // 設定側邊選單抽屜
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        enableExpandableList();
    }

    private void setSearchView() {
        searchView.bringToFront();
        searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setIconified(false);
        searchView.setQuery("", false);

        searchView.requestFocus();

//    searchView.set(new OnKeyListener() {
//      @Override
//      public boolean onKey(View v, int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
//          InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//          mInputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//          searchView.setVisibility(View.INVISIBLE);
//        }
//        return false;
//      }
//    });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (mViewPager.getCurrentItem() == 0) {
                    adapterTotal.setTotal(adapter.search(query.trim().toLowerCase(), "Items"));
                    adapterTotal.notifyDataSetChanged();
                    adapter.notifyDataSetChanged();

                } else if (mViewPager.getCurrentItem() == 1) {
                    adapter2.search(query.trim(), "Discrepancy");
                    adapter2.notifyDataSetChanged();
                }

                InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                mInputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                searchView.clearFocus();
                searchView.setVisibility(View.INVISIBLE);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mViewPager.getCurrentItem() == 0) {
                    adapterTotal.setTotal(adapter.search(newText.trim().toLowerCase(), "Items"));
                    adapterTotal.notifyDataSetChanged();
                    adapter.notifyDataSetChanged();
                } else if (mViewPager.getCurrentItem() == 1) {
                    adapter2.search(newText.trim().toLowerCase(), "Discrepancy");
                    adapter2.notifyDataSetChanged();
                }

                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                return true;
            }
        });
    }

    //Show search bar
    OnClickListener onIconClick = new OnClickListener() {
        boolean flag = true;

        @Override
        public void onClick(View v) {

            if (flag) {
                searchView.setVisibility(View.VISIBLE);
                setSearchView();

                flag = false;
            } else {
                searchView.setVisibility(View.INVISIBLE);
                flag = true;
            }
        }
    };

    // 設定 ViewPager 內放的Fragment
    private void initViewPager() {
        try {

            fg1 = new FragmentItem();
            Bundle bundle = new Bundle();
            bundle.putString("key", ItemsName);
            bundle.putString("user", user);
            fg1.setArguments(bundle);

            fg2 = new FragmentDiscrepancy();
            Bundle bundle1 = new Bundle();
            bundle1.putString("key", DiscrepancyName);
            bundle1.putString("user", user);
            fg2.setArguments(bundle1);

            fg3 = FragmentPreorder.newInstance(PreorderName, user);
            fg4 = FragmentVip.newInstance(VipName, user);

            fmlist = new ArrayList<>();
            fmlist.add(fg1);
            fmlist.add(fg2);
            fmlist.add(fg3);
            fmlist.add(fg4);

            fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), fmlist);

//      fragmentAdapter.getPageTitle()

            // 預設 Fragment
            mViewPager.setAdapter(fragmentAdapter);

            if (!user.equals("EVA")) {
                mViewPager.setCurrentItem(0);
            } else {
                mViewPager.setCurrentItem(1);
                mTag1.setBackgroundColor(getResources().getColor(R.color.colorEVA));
                mTag2.setBackgroundColor(getResources().getColor(R.color.colorEVAGreen));
                mTag3.setBackgroundColor(getResources().getColor(R.color.colorEVA));
                mTag4.setBackgroundColor(getResources().getColor(R.color.colorEVA));
            }

            mViewPager.setOnPageChangeListener(new MyViewPagerChangedListener());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //點選 Item 項目修改內容
    public ListView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            ItemInfo item = adapter.getItem(i);

            Intent intent = new Intent();
            intent.setClass(ECheckUpdateActivity.this, ItemDetailActivity.class);

            intent.putExtra("itemInfo", item.getItemInfo());
            intent.putExtra("drawerNo", item.getDrawer());
            intent.putExtra("itemCode", item.getItemCode());
            if (user.equals("EGAS")) {
                intent.putExtra("stock", item.getNewNum());
            } else {
                intent.putExtra("stock", item.getEvacheck());
            }

            intent.putExtra("damage", item.getDamage());

            startActivityForResult(intent, ITEMS_DETAIL, ActivityOptions.makeSceneTransitionAnimation(ECheckUpdateActivity.this).toBundle());
        }
    };

    //點選 Discrepancy 項目將其修改內容或移除
    public ListView.OnItemClickListener itemClickListener2 = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            try {
                int type = adapter2.getItemViewType(i);

                switch (type) {
                    case 0:
                        ItemInfo item = adapter2.getItem(i);

                        Intent intentItem = new Intent();
                        intentItem.setClass(ECheckUpdateActivity.this, ItemDetailActivity.class);

                        intentItem.putExtra("itemInfo", item.getItemInfo());
                        intentItem.putExtra("drawerNo", item.getDrawer());
                        intentItem.putExtra("itemCode", item.getItemCode());
                        if (user.equals("EGAS")) {
                            intentItem.putExtra("stock", item.getEgascheck());
                        } else {
                            intentItem.putExtra("stock", item.getEvacheck());
                        }
                        intentItem.putExtra("damage", item.getDamage());

                        startActivityForResult(intentItem, DISCREPANCY_ITEMS_DETAIL, ActivityOptions.makeSceneTransitionAnimation(ECheckUpdateActivity.this).toBundle());
                        break;

                    case 1:
                        PreorderReceiptInfo reciept = adapter2.getPreorder(i);

                        Intent intentReceipt = new Intent();
                        intentReceipt.setClass(ECheckUpdateActivity.this, RecieptDetailActivity.class);

                        intentReceipt.putExtra("receiptInfo", reciept.getPreorderReceiptInfo());
                        intentReceipt.putExtra("saleState", reciept.getSaleState());

                        startActivityForResult(intentReceipt, DISCREPANCY_PREORDER_DETAIL, ActivityOptions.makeSceneTransitionAnimation(ECheckUpdateActivity.this).toBundle());
                        break;
                }
            } catch (Exception obj) {
                Toast.makeText(ECheckUpdateActivity.this, "發生錯誤: " + obj.getMessage() + "請返回上一頁",
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    //點選 Item 圖片放大圖片
    private ItemListAdapter.ItemListFunctionClickListener itemPictureClickListener = new ItemListAdapter.ItemListFunctionClickListener() {

        @Override
        public void toolItemFunctionClickListener(String position) {

            ItemInfo item = adapter.getImgItem(position);

            Intent intent = new Intent();
            intent.setClass(ECheckUpdateActivity.this, ItemPictureActivity.class);
            intent.putExtra("itemInfo", item.getItemInfo());
            intent.putExtra("itemCode", item.getItemCode());

            startActivity(intent);
        }
    };

    //點選 Discrepancy 圖片放大圖片
    private DiscrepancyListAdapter.ItemListFunctionClickListener itemPictureClickListener2 = new DiscrepancyListAdapter.ItemListFunctionClickListener() {

        @Override
        public void toolItemFunctionClickListener(String position) {

            ItemInfo item = adapter2.getImgItem(position);

            Intent intent = new Intent();
            intent.setClass(ECheckUpdateActivity.this, ItemPictureActivity.class);
            intent.putExtra("itemInfo", item.getItemInfo());
            intent.putExtra("itemCode", item.getItemCode());

            startActivity(intent);
        }
    };

    //點選 PreOrder 圖片放大圖片
    private PreorderListAdapter.ItemListFunctionClickListener itemPictureClickListener3 = new PreorderListAdapter.ItemListFunctionClickListener() {

        @Override
        public void toolItemFunctionClickListener(String position) {
            try{
                PreorderItems item = adapter3.getImgItem(position);

                Intent intent = new Intent();
                intent.setClass(ECheckUpdateActivity.this, ItemPictureActivity.class);
                intent.putExtra("itemInfo",item.getPreorderInfo());
                intent.putExtra("itemCode", item.getItemCode());

                startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    };

    //點選 VIP 圖片放大圖片
    private PreorderListAdapter.ItemListFunctionClickListener itemPictureClickListener4 = new PreorderListAdapter.ItemListFunctionClickListener() {

        @Override
        public void toolItemFunctionClickListener(String position) {

            PreorderItems item = adapter4.getImgItem(position);

            Intent intent = new Intent();
            intent.setClass(ECheckUpdateActivity.this, ItemPictureActivity.class);
            intent.putExtra("itemInfo",item.getPreorderInfo());
            intent.putExtra("itemCode", item.getItemCode());

            startActivity(intent);
        }
    };

    @Override // 覆寫 onActivityResult，傳值回來時會執行此方法。
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //隱藏鍵盤
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        try {
            //由Items頁簽回傳
            if ((requestCode == ITEMS_DETAIL) && resultCode == RESULT_OK) {
                String itemInfo = (String) data.getExtras().get("itemInfo");
                String stock = (String) data.getExtras().get("stock");
                String damage = (String) data.getExtras().get("damage");

                ItemInfo item = adapter.getItem(itemInfo);
                String itemCode = item.getItemCode();


                if (user.equals("EGAS")) {
                    //Stock寫入資料庫
                    if (!DBQuery.eGASAdjustQty(ECheckUpdateActivity.this, errMsg, itemCode, Integer.parseInt(stock))) {
                        MessageBox.show("", errMsg.toString(), ECheckUpdateActivity.this, "YES");
                    }else {
                        item.setNewNum(Integer.parseInt(stock));
                    }
                    //Damage寫入資料庫
                    if (!DBQuery.eGASDamageQty(ECheckUpdateActivity.this, errMsg, itemCode, Integer.parseInt(damage))) {
                        MessageBox.show("", errMsg.toString(), ECheckUpdateActivity.this, "YES");
                    }else {
                        item.setDamage(Integer.parseInt(damage));
                    }

                }else {
                    //Stock寫入資料庫
                    if (!DBQuery.eVAAdjustQty(ECheckUpdateActivity.this, errMsg, itemCode, Integer.parseInt(stock))) {
                        MessageBox.show("", errMsg.toString(), ECheckUpdateActivity.this, "YES");
                    }else {
                        item.setEvacheck(Integer.parseInt(stock));
                    }
                    //Damage寫入資料庫
                    if (!DBQuery.eVADamageQty(ECheckUpdateActivity.this, errMsg, itemCode, Integer.parseInt(damage))) {
                        MessageBox.show("", errMsg.toString(), ECheckUpdateActivity.this, "YES");
                    }else {
                        item.setDamage(Integer.parseInt(damage));
                    }
                }
                adapter.notifyDataSetChanged();
//                final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Saving...", true, false);
//                new Thread() {
//                    public void run() {
//                        adapter.refreshItemList(user);
//                        ECheckUpdateActivity.this.runOnUiThread(() -> {
//                            adapter.notifyDataSetChanged();
//                            mloadingDialog.dismiss();
//                        });
//                    }}.start();

            } else if ((requestCode == DISCREPANCY_ITEMS_DETAIL) && resultCode == RESULT_OK) {

                //由Discrepancy頁簽中來自Items項目回傳
                String itemInfo = (String) data.getExtras().get("itemInfo");
                String stock = (String) data.getExtras().get("stock");
                String damage = (String) data.getExtras().get("damage");

                ItemInfo item = adapter2.getItem(itemInfo);
                String itemNewNumber = Integer.toString(item.getNewNum());
                String itemDamage = Integer.toString(item.getDamage());
                String itemCode = item.getItemCode();

                if (user.equals("EGAS")) {
                    //Stock寫入資料庫
                    if (!DBQuery.eGASAdjustQty(ECheckUpdateActivity.this, errMsg, itemCode, Integer.parseInt(stock))) {
                        MessageBox.show("", errMsg.toString(), ECheckUpdateActivity.this, "YES");
                    }

                    //Damage寫入資料庫
                    if (!DBQuery.eGASDamageQty(ECheckUpdateActivity.this, errMsg, itemCode, Integer.parseInt(damage))) {
                        MessageBox.show("", errMsg.toString(), ECheckUpdateActivity.this, "YES");
                    }
                } else {
                    if (!DBQuery.eVAAdjustQty(ECheckUpdateActivity.this, errMsg, itemCode, Integer.parseInt(stock))) {
                        MessageBox.show("", errMsg.toString(), ECheckUpdateActivity.this, "YES");
                    }

                    //Damage寫入資料庫
                    if (!DBQuery.eVADamageQty(ECheckUpdateActivity.this, errMsg, itemCode, Integer.parseInt(damage))) {
                        MessageBox.show("", errMsg.toString(), ECheckUpdateActivity.this, "YES");
                    }
                }


                final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Saving...", true, false);
                new Thread() {
                    public void run() {
                        adapter2.refreshDiscrepancyList(user);
                        ECheckUpdateActivity.this.runOnUiThread(() -> {
                            adapter2.notifyDataSetChanged();
                            mloadingDialog.dismiss();
                        });
                    }}.start();

            } else if ((requestCode == DISCREPANCY_PREORDER_DETAIL) && resultCode == RESULT_OK) {

                //由Discrepancy頁簽中來自Preorder項目回傳
                String preorderRecieptInfo = (String) data.getExtras().get("receiptInfo");
                String saleState = (String) data.getExtras().get("saleState");
                String position = adapter2.getPreorderItemId(preorderRecieptInfo);

                if(user.equals("EGAS")){
                    if (!DBQuery.eGASSavePreorderState(mActivity, errMsg, preorderRecieptInfo, "N")) {
                        MessageBox.show("", errMsg.toString(), mActivity, "Return");
                    }
                }else {
                    if (!DBQuery.eVASavePreorderState(mActivity, errMsg, preorderRecieptInfo, "N")) {
                        MessageBox.show("", errMsg.toString(), mActivity, "Return");
                    }
                }

                final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Saving...", true, false);
                new Thread() {
                    public void run() {
                        adapter2.refreshDiscrepancyList(user);
                        ECheckUpdateActivity.this.runOnUiThread(() -> {
                            adapter2.notifyDataSetChanged();
                            mloadingDialog.dismiss();
                        });
                    }}.start();

            }
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception obj) {
            Toast.makeText(ECheckUpdateActivity.this, "發生錯誤: " + obj.getMessage() + "請返回上一頁", Toast.LENGTH_LONG).show();
        }
    }

    private void refreshAdapter() {
        try {

            adapter.refreshItemList(user);
            adapter.notifyDataSetChanged();


            adapter2.refreshDiscrepancyList(user);
            adapter2.notifyDataSetChanged();

            adapter3.refreshPR(user);
            adapter3.notifyDataSetChanged();

            adapter4.refreshVIP(user);
            adapter4.notifyDataSetChanged();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //切換頁面滑動
    class MyViewPagerChangedListener implements ViewPager.OnPageChangeListener {

        // 在狀態改變的時候調用
        // arg0 ==1表示正在滑動, arg0==2表示滑動完畢了, arg0==0表示什麼都沒做
        // 當頁面開始滑動的時候, 三種狀態的變化順序為（1，2，0）
        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        // 當頁面在滑動的時候會調用此方法，在滑動被停止之前，此方法會一直得到調用
        // arg0 :當前頁面，及你點擊滑動的頁面, arg1:當前頁面偏移的百分比, arg2:當前頁面偏移的像素位置
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        // 頁面跳轉完後得到調用, arg0是你當前選中的頁面的position
        @Override
        public void onPageSelected(int arg0) {
            switch (arg0) {
                //Item
                case 0:
                    toolbar.setVisibility(View.VISIBLE);
                    searchView.setVisibility(View.INVISIBLE);
                    mSearchIcon.setVisibility(View.VISIBLE);
                    mTag1.setBackgroundColor(getResources().getColor(R.color.colorEVAGreen));
                    mTag2.setBackgroundColor(getResources().getColor(R.color.colorEVA));
                    mTag3.setBackgroundColor(getResources().getColor(R.color.colorEVA));
                    mTag4.setBackgroundColor(getResources().getColor(R.color.colorEVA));

                    toolbarCreate(toolbar);

                    final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Loading...", true, false);
                    new Thread() {
                        public void run() {
                            adapter.clear();
                            adapter.refreshItemList(user);
                            ECheckUpdateActivity.this.runOnUiThread(() -> {
                                adapter.notifyDataSetChanged();
                                mloadingDialog.dismiss();
                                if(FragmentItem.itemSpinner.getSelectedItemPosition()!=0){
                                    FragmentItem.itemSpinner.setSelection(0);
                                }

                            });
                        }}.start();

                    break;

                //Discrepancy
                case 1:
                    toolbar.setVisibility(View.VISIBLE);
                    searchView.setVisibility(View.INVISIBLE);
                    mSearchIcon.setVisibility(View.VISIBLE);
                    mTag1.setBackgroundColor(getResources().getColor(R.color.colorEVA));
                    mTag2.setBackgroundColor(getResources().getColor(R.color.colorEVAGreen));
                    mTag3.setBackgroundColor(getResources().getColor(R.color.colorEVA));
                    mTag4.setBackgroundColor(getResources().getColor(R.color.colorEVA));

                    toolbarCreate(toolbar);

                    adapter2.refreshDiscrepancyList(user);
                    adapter2.notifyDataSetChanged();

                    break;

                //Preorder
                case 2:
                    toolbar.setVisibility(View.VISIBLE);
                    searchView.setVisibility(View.INVISIBLE);
                    mSearchIcon.setVisibility(View.INVISIBLE);
                    mTag1.setBackgroundColor(getResources().getColor(R.color.colorEVA));
                    mTag2.setBackgroundColor(getResources().getColor(R.color.colorEVA));
                    mTag3.setBackgroundColor(getResources().getColor(R.color.colorEVAGreen));
                    mTag4.setBackgroundColor(getResources().getColor(R.color.colorEVA));

                    toolbarCreate(toolbar);

                    adapter3.refreshPR(user);
                    if(!adapter3.getCurrentReceipt().equals("-1")){
                        adapter3.setReceipt(adapter3.getCurrentReceipt());
                        if(adapter3.getPreOrderInfo(adapter3.getCurrentReceipt()).getStatus().equals("S")){
                            adapterTotalPreorder.setTotal(0);
                            FragmentPreorder.radioSale.setChecked(true);
                        }else{
                            adapterTotalPreorder.setTotal(adapter3.getTotal(adapter3.getCurrentReceipt()));
                            FragmentPreorder.radioUnSale.setChecked(true);
                        }
                        adapterTotalPreorder.notifyDataSetChanged();
                    }else {
//                        FragmentPreorder.radioUnSale.setVisibility(View.GONE);
//                        FragmentPreorder.radioSale.setVisibility(View.GONE);
                    }
                    adapter3.notifyDataSetChanged();
                    break;

                //Vip
                case 3:
                    toolbar.setVisibility(View.VISIBLE);
                    searchView.setVisibility(View.INVISIBLE);
                    mSearchIcon.setVisibility(View.INVISIBLE);
                    mTag1.setBackgroundColor(getResources().getColor(R.color.colorEVA));
                    mTag2.setBackgroundColor(getResources().getColor(R.color.colorEVA));
                    mTag3.setBackgroundColor(getResources().getColor(R.color.colorEVA));
                    mTag4.setBackgroundColor(getResources().getColor(R.color.colorEVAGreen));

                    toolbarCreate(toolbar);


                    adapter4.refreshVIP(user);
                    if(!adapter4.getCurrentReceipt().equals("-1")){
                        adapter4.setReceipt(adapter4.getCurrentReceipt());
                        if(adapter4.getPreOrderInfo(adapter4.getCurrentReceipt()).getStatus().equals("S")){
                            adapterTotalPreorder.setTotal(0);
                            FragmentVip.radioSaleVip.setChecked(true);
                        }else{
                            adapterTotalPreorder.setTotal(adapter4.getTotal(adapter4.getCurrentReceipt()));
                            FragmentVip.radioUnsaleVip.setChecked(true);
                        }
                        adapterTotalPreorder.setTotal(adapter4.getTotal(adapter4.getCurrentReceipt()));
                        adapterTotalPreorder.notifyDataSetChanged();
                    }else {
//                        FragmentVip.radioSaleVip.setVisibility(View.GONE);
//                        FragmentVip.radioUnsaleVip.setVisibility(View.GONE);
                    }
                    adapter4.notifyDataSetChanged();

                    break;
            }
        }
    }

    //按下 TextView 切換 ViewPager
    class MyClickListener implements OnClickListener {

        private int index;

        public MyClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            try {
                mViewPager.setCurrentItem(index);

                if ((index == 2) || (index == 3)) {
                    toolbar.setVisibility(View.VISIBLE);
                    toolbarCreate(toolbar);
                } else {
                    toolbar.setVisibility(View.VISIBLE);
                    toolbarCreate(toolbar);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        try {
            mainListener = (OnMainListener) fragment;
            //Toast.makeText(ECheckUpdateActivity.this, fragment.getArguments().toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(ECheckUpdateActivity.this, "發生錯誤: " + e.getMessage() + "請返回上一頁", Toast.LENGTH_LONG).show();
            throw new ClassCastException(this.toString() + " must implement OnMainListener");
        }
        super.onAttachFragment(fragment);
    }

    // 接口
    public interface OnMainListener {

        void onMainAction(String info);
    }

    @Override
    public void onResume() {

        super.onResume();

        new Thread() {
            public void run() {
                initScan();
                IntentFilter filter = new IntentFilter();
                int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
                String[] value_buf = mScanManager.getParameterString(idbuf);
                if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
                    filter.addAction(value_buf[0]);
                } else {
                    filter.addAction(SCAN_ACTION);
                }

                registerReceiver(mScanReceiver, filter);

            }}.start();


        //隱藏鍵盤
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void searchItem(String itemNum) {

        DBQuery.ItemDataPack itemPack = DBQuery.getProductInfo(ECheckUpdateActivity.this, errMsg, SecSeq, itemNum, null, 0);

        if (itemPack == null) {
            tw.com.regalscan.component.MessageBox.show("", "Pno Code error.", ECheckUpdateActivity.this, "Return");
            return;
        }

        DBQuery.DrawNoPack drawNoPack = DBQuery.getAllDrawerNo(ECheckUpdateActivity.this, errMsg, null, null, false);

        for (int i = 0; i < drawNoPack.drawers.length; i++) {
            if (drawNoPack.drawers[i].DrawNo.equals(itemPack.items[0].DrawNo)) {
                //切換到該商品的抽屜
                fg1.setSpinner(i, itemPack.items[0].ItemCode);
                break;
            }
        }

        searchView.setQuery(itemPack.items[0].ItemCode, false);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            soundpool.play(soundid, 1, 1, 0, 0, 1);
            mVibrator.vibrate(100);

            byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
            int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
            byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte) 0);
            Timber.tag("debug").i("----codetype--" + temp);
            barcodeStr = new String(barcode, 0, barcodelen);

            //用掃到的條碼搜尋物品
            searchItem(barcodeStr);

            barcodeStr = null;
        }
    };

    // Scan init
    private void initScan() {

        mScanManager = new ScanManager();
        mScanManager.openScanner();

        mScanManager.switchOutputMode(0);
        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
        soundid = soundpool.load("/etc/Scan_new.ogg", 1);
    }

    OnClickListener btnClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnReturn:
                    Intent intent = new Intent(ECheckUpdateActivity.this, MenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ECheckUpdateActivity.this).toBundle());
                    ECheckUpdateActivity.this.finish();
                    break;
                case R.id.btnSave:
                    try {
                        if (user.equals("EGAS")) {
                            EGASTimeEnd = DateTime.now();
                            if (MessageBox.show("", "Print EGAS Check ?", mActivity, "Yes", "No"))
                                PrintSelect(1);
                        } else {
                            if (MessageBox.show("", "Print EVA Check ?", mActivity, "Yes", "No")) {
                                PrintSelect(2);
                            } else if (MessageBox.show("", "Print Reload list ?", mActivity, "Yes", "No")) {
                                PrintSelect(3);
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
            }
        }
    };

    //↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓列印用
    Message msg;

    public void PrintSelect(int num) {
        mloadingDialog = ProgressDialog.show(mActivity, "", "Processing...", true);
        msg = new Message();
        msg.what = 0; //給予初始值
        switch (num) {
            case 1:
                new Thread() {
                    public void run() {
                        try {
                            while (printGround.printEGASCheck(EGASTimeStart, EGASTimeEnd) == -1) {
                                //無紙调用Handler
                                msg.what = 1;
                                handle.sendMessage(msg);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //列印錯誤调用Handler
                            msg.what = 119;
                            handle.sendMessage(msg);
                            return;
                        }
                        //結束调用Handler取消ProgressDialog
                        handle.sendMessage(msg);
                    }
                }.start();
                break;
            case 2:
                new Thread() {
                    public void run() {
                        try {
                            while (printGround.printEVACheck() == -1) {
                                //無紙调用Handler
                                msg.what = 2;
                                handle.sendMessage(msg);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //列印錯誤调用Handler
                            msg.what = 119;
                            handle.sendMessage(msg);
                            return;
                        }
                        //結束调用Handler取消ProgressDialog
                        //列印完畢繼續詢問是否列印 Reload list
                        msg.what = 3_1;
                        handle.sendMessage(msg);
                    }
                }.start();


                break;
            case 3:
                new Thread() {
                    public void run() {
                        try {
                            while (printGround.printReloadQty() == -1) {
                                //無紙调用Handler
                                msg.what = 3;
                                handle.sendMessage(msg);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            //列印錯誤调用Handler
                            msg.what = 119;
                            handle.sendMessage(msg);
                            return;
                        }
                        //結束调用Handler取消ProgressDialog
                        handle.sendMessage(msg);
                    }
                }.start();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScanReceiver != null) {
            unregisterReceiver(mScanReceiver);
        }
    }

    //Thread內呼叫handle處理UI操2017-09-21 Howard
    @SuppressLint("HandlerLeak")
    Handler handle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mloadingDialog.dismiss();
            switch (msg.what) {
                case 1:
                case 2:
                case 3:
                    if (MessageBox.show("", "No paper, reprint?", mActivity, "Yes", "No")) {
                        PrintSelect(msg.what);
                    }
                    break;
                case 3_1: // Reload 詢問列印
                    if (MessageBox.show("", "Print Reload list ?", mActivity, "Yes", "No")) {
                        PrintSelect(3);
                    }
                    break;
                case 119:
                    MessageBox.show("", "Print error", mActivity, "Return");
                    break;

            }
        }
    };
    //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑列印用
}
