package tw.com.regalscan.evaground;

import java.io.File;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.transition.Fade;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import tw.com.regalscan.R;

public class ItemDetailActivity extends Activity {

    private TextView mtxtTopic, drawer;
    private TextView mStock, mDamage;
    private Button mbtnDecrease01, mbtnDecrease02, mbtnIncrease01, mbtnIncrease02, btnConfirm, btnCancel;
    private ImageView mImageview;

    private String itemName, drawerNo, itemCode;
    private int stock, damage, position;

    private tw.com.regalscan.app.entity.ItemInfo itemInfo;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_item_detail_ground);
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());
        this.setFinishOnTouchOutside(false);

        itemInfo = getIntent().getParcelableExtra("ItemInfo");

        if (itemInfo == null) {
            itemName = (String)getIntent().getExtras().get("itemInfo");
            drawerNo = (String)getIntent().getExtras().get("drawerNo");
            itemCode = (String)getIntent().getExtras().get("itemCode");
            stock = (int)getIntent().getExtras().get("stock");
            damage = (int)getIntent().getExtras().get("damage");
        } else {
            itemName = itemInfo.getItemName();
            drawerNo = itemInfo.getDrawNo();
            itemCode = itemInfo.getItemCode();
            stock = itemInfo.getEGASCheckQty();
            damage = itemInfo.getEGASDamageQty();
            position = (int)getIntent().getExtras().get("position");
        }

        //商品標題
        mtxtTopic = findViewById(R.id.txttopic);
        mtxtTopic.setText(itemName);
        drawer = findViewById(R.id.tv_Drawer);
        drawer.setText(drawerNo + " - " + itemCode);

        //數量
        mStock = findViewById(R.id.txtStock);
        mStock.setText(String.valueOf(stock));
        mDamage = findViewById(R.id.txtQty);
        mDamage.setText(String.valueOf(damage));

        //按鈕
        mbtnDecrease01 = findViewById(R.id.btnDecrease01);
        mbtnDecrease01.setOnClickListener(mbtnDecrease01_OnClickListner);
        mbtnDecrease02 = findViewById(R.id.minus02);
        mbtnDecrease02.setOnClickListener(mbtnDecrease02_OnClickListner);
        mbtnIncrease01 = findViewById(R.id.btnIncrease01);
        mbtnIncrease01.setOnClickListener(mbtnIncrease01_OnClickListner);
        mbtnIncrease02 = findViewById(R.id.plus02);
        mbtnIncrease02.setOnClickListener(mbtnIncrease02_OnClickListner);
        btnConfirm = findViewById(R.id.btnAccept);
        btnConfirm.setOnClickListener(mbtnconfirm_OnClickListener);
        btnCancel = findViewById(R.id.btnReturn);
        btnCancel.setOnClickListener(mbtncancel_OnClickListener);

        //圖片
        mImageview = findViewById(R.id.imgItem);

        File imgFile = new File(Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator + "pic"
            , itemCode + ".jpg");
        if (imgFile.exists()) {
            Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            mImageview.setImageBitmap(bmp);
        } else {
            Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_loading);
            mImageview.setImageBitmap(bmp);
        }
    }

    private View.OnClickListener mbtnDecrease01_OnClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mStock.getText().toString().equals("0")) {
                mStock.setText("0");
            } else {
                mStock.setText(Integer.toString(Integer.valueOf(mStock.getText().toString()) - 1));
            }
        }
    };

    private View.OnClickListener mbtnDecrease02_OnClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mDamage.getText().toString().equals("0")) {
                mDamage.setText("0");
            } else {
                mDamage.setText(Integer.toString(Integer.valueOf(mDamage.getText().toString()) - 1));
            }
        }
    };

    private View.OnClickListener mbtnIncrease01_OnClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mStock.setText(Integer.toString(Integer.valueOf(mStock.getText().toString()) + 1));
        }
    };

    private View.OnClickListener mbtnIncrease02_OnClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mDamage.setText(Integer.toString(Integer.valueOf(mDamage.getText().toString()) + 1));
        }
    };

    private View.OnClickListener mbtnconfirm_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String modiStock = mStock.getText().toString().trim();
            String modiDamage = mDamage.getText().toString().trim();

            if (modiStock.equals("")) {
                MessageBox.show("錯誤", "請輸入Stock數量", ItemDetailActivity.this, false);
                mStock.requestFocus();
                return;
            }
            if (modiDamage.equals("")) {
                MessageBox.show("錯誤", "請輸入Damage數量", ItemDetailActivity.this, false);
                mDamage.requestFocus();
                return;
            }

            Pattern pattern = Pattern.compile("^[-+]?\\d+$");
            if (!pattern.matcher(modiStock).matches()) {
                MessageBox.show("錯誤", "Stock數量必須為數字", ItemDetailActivity.this, false);
                mStock.requestFocus();
                return;
            }
            if (!pattern.matcher(modiDamage).matches()) {
                MessageBox.show("錯誤", "Damage數量必須為數字", ItemDetailActivity.this, false);
                mDamage.requestFocus();
                return;
            }

            if (Integer.valueOf(modiDamage) > Integer.valueOf(modiStock)) {
                MessageBox.show("", "Damage qty can not more than stock qty.", ItemDetailActivity.this, false);
                mDamage.requestFocus();
                return;
            }

            if (itemInfo != null) {
                itemInfo.setEGASCheckQty(Integer.valueOf(modiStock));
                itemInfo.setEGASDamageQty(Integer.valueOf(modiDamage));
            }

            Intent backIntent = getIntent();
            backIntent.putExtra("itemInfo", mtxtTopic.getText().toString());
            backIntent.putExtra("stock", modiStock);
            backIntent.putExtra("damage", modiDamage);
            backIntent.putExtra("position", position);
            backIntent.putExtra("ItemInfo", itemInfo);


            setResult(RESULT_OK, backIntent);
            ItemDetailActivity.this.finish();
        }
    };


    private View.OnClickListener mbtncancel_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ItemDetailActivity.this.finish();
        }
    };


}
