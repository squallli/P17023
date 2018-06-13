package tw.com.regalscan.evaair.transfer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;

import com.google.gson.Gson;
import tw.com.regalscan.R;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.TransferItemPack;

public class TransferActivity01  extends AppCompatActivity {


    private Button btnReturn,btnTransIn, btnTransOut,btnCancelTransOut;
    public Context mContext;
    public Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_01);




        init();
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
                ActivityManager.closeAllActivity();
                finish();
            }
        });


        btnTransOut = findViewById(R.id.btnTransOut);
        btnTransOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(mActivity, TransferOutActivity01.class);
//                    intent.putExtras(argument);
                mActivity.startActivity(intent);
            }
        });

        btnTransIn = findViewById(R.id.btnTransIn);
        btnTransIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(mActivity, TransferInActivity.class);
//                    intent.putExtras(argument);
                mActivity.startActivity(intent);
            }
        });

        btnCancelTransOut = findViewById(R.id.btnCancelTransOut);
        btnCancelTransOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder err= new StringBuilder();
                TransferItemPack transferItemPack= DBQuery.queryTransferItemQty(mContext, err, null, "OUT");
                if( transferItemPack==null ){
                    MessageBox.show("", "Query transfer list error.", mContext, "Return");
                    return;
                }
                if(transferItemPack.transfers==null){
                    MessageBox.show("", "No transfer out list", mContext, "Ok");
                    return;
                }
                Bundle argument = new Bundle();
                Gson gson= new Gson();
                String jsonPack= gson.toJson(transferItemPack);
                argument.putString("jsonPack", jsonPack);
                Intent intent= new Intent(mActivity, CancelTransferOutActivity.class);
                intent.putExtras(argument);
                mActivity.startActivity(intent);
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