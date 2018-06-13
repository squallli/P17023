package tw.com.regalscan.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import tw.com.regalscan.R;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.customClass.ItemUpgradeInfo;

public class ItemUpgradeActivity extends Activity {
    private TextView txtIdentity, txtFromTo, txtQty, txtMoney;
    private Button btnReturn, btnAccept, btnMinus, btnPlus;
    private String index;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_item_upgrade);




        this.setFinishOnTouchOutside(false);

        index= (String) getIntent().getExtras().get("index");
        ItemUpgradeInfo item= (ItemUpgradeInfo) getIntent().getExtras().get("item");

        txtIdentity= findViewById(R.id.txtIdentity);
        txtIdentity.setText(item.getIdentity());

        txtFromTo= findViewById(R.id.txtFromTo);
        txtFromTo.setText(item.getFrom() + " to " + item.getTo());

        txtMoney= findViewById(R.id.txtMoney);
        //整數價格
        if(item.getIntegerTotal()!=-1)
            txtMoney.setText("US " + String.valueOf(item.getIntegerTotal()));
        else
            txtMoney.setText("US " + String.valueOf(item.getTotal()));

        txtQty= findViewById(R.id.txtQty);
        txtQty.setText(String.valueOf(item.getQty()));


        //加減數量
        btnMinus= findViewById(R.id.minus02);
        btnMinus.setOnClickListener(minus_onClickListener);
        btnPlus= findViewById(R.id.plus02);
        btnPlus.setOnClickListener(plus_onClickListener);

        //確定與取消
        btnAccept= findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backIntent= getIntent();
                backIntent.putExtra("index", index);
                backIntent.putExtra("newQty", txtQty.getText().toString().trim());

                setResult(RESULT_OK, backIntent);
                ItemUpgradeActivity.this.finish();
            }
        });

        btnReturn= findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ItemUpgradeActivity.this.finish();
            }
        });

    }

    //減少
    private View.OnClickListener minus_onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int minusQty= Integer.valueOf(txtQty.getText().toString().trim());

            if( minusQty !=1 ){
                minusQty--;
                txtQty.setText(String.valueOf(minusQty));
            }

        }
    };

    //增加
    private View.OnClickListener plus_onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int minusQty= Integer.valueOf(txtQty.getText().toString().trim());
            minusQty++;
            txtQty.setText(String.valueOf(minusQty));
        }
    };

    @Override
    public void onBackPressed() {
    }



}
