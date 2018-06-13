package tw.com.regalscan.evaair.upgrade;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import tw.com.regalscan.activities.CPCheckActivity;
import tw.com.regalscan.component.*;
import tw.com.regalscan.R;
import tw.com.regalscan.utils.Tools;


public class UpgradeActivity01 extends AppCompatActivity {



    private TextView txtTitle;
    private Button btnReturn, btnUpgrade, btnUpgradeRefund;
    public Context mContext;
    public Activity mActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade_01);




        init();
    }

    private void init() {
        mContext = this;
        mActivity = this;

        enableExpandableList();

        txtTitle= findViewById(R.id.toolbar_title);
        txtTitle.setText("Upgrade");

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> ActivityManager.removeActivity(this.getClass().getSimpleName()));

        btnUpgrade = findViewById(R.id.btnUpgrade);
        btnUpgrade.setText("Upgrade");
        btnUpgrade.setOnClickListener(view -> {

            Intent intent= new Intent(mActivity, UpgradeBasketActivity.class);
//                    intent.putExtras(argument);
            mActivity.startActivity(intent);
        });

        btnUpgradeRefund = findViewById(R.id.btnUpgradeRefund);
        btnUpgradeRefund.setText("Upgrade Refund");
        btnUpgradeRefund.setOnClickListener(view -> {

            //先做身分驗證, alertDialog or alertActivity

            Bundle bundle = new Bundle();
            bundle.putString("fromWhere","UpgradeActivity01");
            Intent intent= new Intent(mActivity, CPCheckActivity.class);
            intent.putExtras(bundle);
            mActivity.startActivity(intent);
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