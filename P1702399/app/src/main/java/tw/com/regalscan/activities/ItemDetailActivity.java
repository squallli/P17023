package tw.com.regalscan.activities;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import tw.com.regalscan.R;
import tw.com.regalscan.customClass.ItemInfo;

public class ItemDetailActivity extends Activity {

    private TextView mtxtMagazineNum, mtxtPrice, mtxtTopic, mStock, mSoldQty;
    private Button btnReturn, btnAccept, btnMinus, btnPlus;
    private String index, canModifiedToZero, fromWhere;
    private ImageView image;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_item_detail);

        this.setFinishOnTouchOutside(false);

        //取得傳來的資訊
        ItemInfo item = (ItemInfo)getIntent().getExtras().get("item");
        index = (String)getIntent().getExtras().get("index");
        canModifiedToZero = (String)getIntent().getExtras().get("canModifiedToZero");
        fromWhere = (String)getIntent().getExtras().get("fromWhere");

        //設定更改數量的Tag
        TextView txtStock = findViewById(R.id.text01);
        TextView txtQty = findViewById(R.id.text02);
        switch (fromWhere) {
            case "BasketActivity":
            case "TransferOutActivity01":
                txtQty.setText("Qty");
                break;
            case "DamageActivity":
                txtQty.setText("Damage Qty");
                break;
            case "UpdateActivity":
            case "UpdateCpCheckActivity":
                txtStock.setText("Ori Qty");
                txtQty.setText("New Qty");
                break;
        }

        image = findViewById(R.id.imgItem);
        mtxtMagazineNum = findViewById(R.id.txtMagazineNum);
        mtxtTopic = findViewById(R.id.txttopic);
        mStock = findViewById(R.id.txtStock);
        mSoldQty = findViewById(R.id.txtQty);
        mtxtPrice = findViewById(R.id.txtPrice);

        mtxtMagazineNum.setText(item.getSerialNo());
        //整數價格
        if (item.getIntegerPrice() != -1) {
            mtxtPrice.setText(item.getMonyType() + " " + String.valueOf(item.getIntegerPrice()));
        } else {
            mtxtPrice.setText(item.getMonyType() + " " + String.valueOf(item.getPrice()));
        }
        mtxtTopic.setText(item.getItemName());
        mStock.setText(String.valueOf(item.getStock()));
        mSoldQty.setText(String.valueOf(item.getQty()));

        BitmapFactory.Options opts = null;
        opts = new BitmapFactory.Options();
        // 設置為True時，表示系統內存不足時可以被回 收，設置為False時，表示不能被回收。
        opts.inPurgeable = true;
        try {
            //設定大圖
            File imgFile = new File(Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator + "pic"
                , item.getItemCode() + ".jpg");
            if (imgFile.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath(),opts);
                image.setImageBitmap(bmp);
            } else {
                Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_loading,opts);
                image.setImageBitmap(bmp);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //加減數量
        btnMinus = findViewById(R.id.minus02);
        btnMinus.setOnClickListener(minus_onClickListener);
        btnPlus = findViewById(R.id.plus02);
        btnPlus.setOnClickListener(plus_onClickListener);

        //確定與取消
        btnAccept = findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent backIntent = getIntent();
                backIntent.putExtra("index", index);
                backIntent.putExtra("newQty", mSoldQty.getText().toString().trim());

                setResult(RESULT_OK, backIntent);
                ItemDetailActivity.this.finish();
            }
        });

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemDetailActivity.this.finish();
            }
        });

    }

    //減少
    private View.OnClickListener minus_onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int minusQty = Integer.valueOf(mSoldQty.getText().toString().trim());

            if (canModifiedToZero.equals("false")) {
                //不能減為0
                if (minusQty != 1) {
                    minusQty--;
                    mSoldQty.setText(String.valueOf(minusQty));
                }
            } else {
                if (minusQty > 0) {
                    minusQty--;
                    mSoldQty.setText(String.valueOf(minusQty));
                }
            }

        }
    };

    //增加
    private View.OnClickListener plus_onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            int plusQty = Integer.valueOf(mSoldQty.getText().toString().trim());
            int stock = Integer.valueOf(mStock.getText().toString().trim());

            // qty不能大於stock (只有Update可以大於stock)
            if (plusQty < stock || fromWhere.equals("UpdateActivity") || fromWhere.equals("UpdateCpCheckActivity")) {
                if (fromWhere.equals("TransferOutActivity01")) {
                    //最多99個物品
                    if (plusQty < 99) {
                        plusQty++;
                        mSoldQty.setText(String.valueOf(plusQty));
                    }
                } else {
                    plusQty++;
                    mSoldQty.setText(String.valueOf(plusQty));
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
    }

}
