package tw.com.regalscan.evaair;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.widget.ExpandableListView;

import me.relex.circleindicator.CircleIndicator;
import tw.com.regalscan.R;
import tw.com.regalscan.adapters.SwipePageAdapter;
import tw.com.regalscan.component.FlightInfoManager;
import tw.com.regalscan.component.MenuPageView;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;


public class MenuActivity extends AppCompatActivity {


    private ViewPager mViewPager;
    private List<MenuPageView> pageList;
    private CircleIndicator indicator;
    private SwipePageAdapter mPageAdapter;
    private Activity mActivity;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_air);
        mActivity = this;
        mContext = this;

//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        init();
    }

    private void init() {
        final ProgressDialog mloadingDialog = ProgressDialog.show(mContext, "", "Loading...", true, false);
        new Thread() {
            public void run() {
                try {
                    StringBuilder err = new StringBuilder();
                    // call OpenFlight 產生必要的變數
                    if (!DBQuery.openFlight(mContext, err)) {
                        MenuActivity.this.runOnUiThread(() -> MessageBox.show("", "Get flight data error", mContext, "Return"));
                        return;
                    }

                    DBQuery.CrewInfo CA = DBQuery.getGetCrewInfo(mContext, err, FlightData.CrewID);
                    FlightInfoManager.getInstance().setCurrentSecSeq(FlightData.SecSeq);
                    FlightInfoManager.getInstance().setCAName(CA.Name); // CA Name

                } catch (Exception e) {
                    e.printStackTrace();
                    MenuActivity.this.runOnUiThread(() -> {
                        MessageBox.show("", "Get flight data error", mContext, "Return");
                    });

                    return;
                }

                MenuActivity.this.runOnUiThread(() -> {
                    mloadingDialog.dismiss();
                });
            }}.start();



        //滑動頁面設定
        pageList = new ArrayList<>();
        pageList.add(new MenuPageOneView(MenuActivity.this, mActivity));
        pageList.add(new MenuPageTwoView(MenuActivity.this, mActivity));

        mPageAdapter = new SwipePageAdapter(pageList);
        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //設定頁面滑動時下方跟著變動的小圈圈
        indicator = findViewById(R.id.indicator);
        indicator.setViewPager(mViewPager);
        mPageAdapter.registerDataSetObserver(indicator.getDataSetObserver());

        // 側邊選單設定
        enableExpandableList();
    }

    private void enableExpandableList() {
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ExpandableListView expandableListView = findViewById(R.id.expendlist);
        NavigationDrawer navigationDrawer = new NavigationDrawer(mActivity, mContext, drawerLayout, toolbar, expandableListView);
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
