package tw.com.regalscan.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import tw.com.regalscan.R;

public class ItemPictureActivity extends Activity {
    private TextView mtxtTopic;
    private ImageView mitemImage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_item_picture);






//        String itemInfo= (String) getIntent().getExtras().get("itemInfo");
//
//        //商品標題
//        mtxtTopic=(TextView)findViewById(R.id.txttopic);
//        mtxtTopic.setText(itemInfo);

        //商品圖片
        mitemImage= findViewById(R.id.imgItem);

        String itemCode= (String) getIntent().getExtras().get("itemCode");

        //設定大圖
        File imgFile = new File(Environment.getExternalStorageDirectory() + File.separator+ "Download" + File.separator + "pic"
                , itemCode + ".jpg");
        if(imgFile.exists()){
            Bitmap bmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            mitemImage.setImageBitmap(bmp);
        }else{
            Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_loading);
            mitemImage.setImageBitmap(bmp);
        }

    }

}
