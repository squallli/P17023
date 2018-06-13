package tw.com.regalscan.evaground;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import tw.com.regalscan.R;
import tw.com.regalscan.component.AsyncImageFileLoader;

public class ItemPictureActivity extends Activity {

  private TextView mtxtTopic;
  private ImageView mitemImage;
  private AsyncImageFileLoader asyncImageFileLoader;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.item_picture_activity);

    String itemInfo = (String) getIntent().getExtras().get("itemInfo");

    //商品標題
    mtxtTopic = findViewById(R.id.txttopic);
    mtxtTopic.setText(itemInfo);

    //商品圖片
    mitemImage = findViewById(R.id.imgItem);

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
