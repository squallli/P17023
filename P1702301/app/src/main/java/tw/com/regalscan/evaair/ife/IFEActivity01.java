package tw.com.regalscan.evaair.ife;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gesture.GestureUtils;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ExpandableListView;

import tw.com.regalscan.R;
import tw.com.regalscan.activities.CPCheckActivity;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.evaground.Models.Flight;
import tw.com.regalscan.utils.Tools;

public class IFEActivity01 extends AppCompatActivity {

    private Context mContext;
    private Activity mActivity;
    private IFEDBFunction mIFEDBFunction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ife_01);

        init();
    }

    private void init() {
        mContext = this;
        mActivity = this;
        mIFEDBFunction = new IFEDBFunction(mContext, FlightData.SecSeq);

        enableExpandableList();

        //btn
        Button btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            ActivityManager.closeAllActivity();
            finish();
        });

        Button btn77M = findViewById(R.id.btnEX2);
        btn77M.setOnClickListener(view -> {
            Intent intent = new Intent(mActivity, IFEEX2Activity.class);
            mActivity.startActivity(intent);
        });

        Button btn77A = findViewById(R.id.btnEX3);
        btn77A.setOnClickListener(view -> {
            Intent intent = new Intent(mActivity, IFEEX3Activity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("type", 0);
            intent.putExtras(bundle);
            mActivity.startActivity(intent);
        });

        Button btn787 = findViewById(R.id.btn787);
        btn787.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, IFEEX3Activity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("type", 1);
            intent.putExtras(bundle);
            mActivity.startActivity(intent);
        });

        Button btnOrderList = findViewById(R.id.btnOrderList);
        btnOrderList.setOnClickListener(view -> {

            if (!FlightData.IFEConnectionStatus) {
                MessageBox.show("", getString(R.string.Please_Sync_IFE), mContext, "Return");
                return;
            } else {
                if (!mIFEDBFunction.chkPushInventory(FlightData.SecSeq)) {
                   MessageBox.show("", getString(R.string.Please_Push_Inventory), mContext, "Return");
                   return;
                }
            }

//            if (FlightData.IFEConnectionStatus) {
                Intent intent = new Intent(mActivity, OrderListActivity.class);
                mActivity.startActivity(intent);
//            } else {
//                MessageBox.show("", getString(R.string.Please_Sync_IFE), mContext, "Return");
//            }
        });

        Button btnDealInProgress = findViewById(R.id.btnDealInProgress);
        btnDealInProgress.setOnClickListener(view -> {
            //確認IFE的連線與庫存同步狀態

            if (!FlightData.IFEConnectionStatus) {
                MessageBox.show("", getString(R.string.Please_Sync_IFE), mContext, "Return");
                return;
            } else {
                if (!mIFEDBFunction.chkPushInventory(FlightData.SecSeq)) {
                    MessageBox.show("", getString(R.string.Please_Push_Inventory), mContext, "Return");
                    return;
                }
            }

//            if (FlightData.IFEConnectionStatus) {
                Bundle bundle = new Bundle();
                bundle.putString("fromWhere", "IFEActivity01");
                Intent intent = new Intent(mActivity, CPCheckActivity.class);
                intent.putExtras(bundle);
                mActivity.startActivity(intent);
//            } else {
//                MessageBox.show("", getString(R.string.Please_Sync_IFE), mContext, "Return");
//            }
        });

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }

    private void enableExpandableList() {
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ExpandableListView expandableListView = findViewById(R.id.expendlist);
        NavigationDrawer navigationDrawer = new NavigationDrawer(mActivity, mContext, drawerLayout, toolbar, expandableListView);
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