package tw.com.regalscan.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.*;
import tw.com.regalscan.evaair.PictureActivity;

import java.io.File;
import java.util.List;

public class ImageAdapter extends BaseAdapter {

    private ViewGroup layout;
    private Context context;
    private List coll;
    public ImageView imageView;

    public ImageAdapter(Context context, List coll) {
        super();
        this.context = context;
        this.coll = coll;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        //縮圖的顯示
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowview = inflater.inflate(R.layout.item_photo, parent, false);
        layout = rowview.findViewById(R.id.rl_item_photo);
        imageView = rowview.findViewById(R.id.imageView1);

        // 計算一張圖片在不同裝置下不同解析度時應該縮放顯示的像素
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float dd = dm.density;
        float px = 25 * dd;
        float screenWidth = dm.widthPixels;
        int newWidth = (int) (screenWidth - px) / 4; // 一行顯示四個縮圖

        // 為了顯示正方形的縮圖形狀，先算出適合的寬高
        layout.setLayoutParams(new GridView.LayoutParams(newWidth, newWidth));
        imageView.setId(position);
        // Bitmap bm = BitmapFactory.decodeFile((String)coll.get(position));
        // Bitmap newBit = Bitmap.createScaledBitmap(bm, newWidth, newWidth,
        // true);

        Bitmap bm = MediaStore.Images.Thumbnails.getThumbnail(context
                        .getApplicationContext().getContentResolver(), Long
                        .parseLong((String) coll.get(position)),
                MediaStore.Images.Thumbnails.MICRO_KIND, null);

        imageView.setImageBitmap(bm);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        //點擊照片縮圖
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context, "index:" + position, Toast.LENGTH_SHORT).show();
                ((PictureActivity)context).setImageView(position);
            }
        });

        //長按照片縮圖
        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if(MessageBox.show("", "Delete?", context, "Yes", "No")){
                    try{

                        File file = new File(PictureActivity.mImagePaths.get(position));
                        PictureActivity.deleteImage(position);

                        if(file.exists()){
                            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                        }else{
                            PictureActivity.refreshImageList();
                            Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

                return true;
            }
        });

        return rowview;
    }

    @Override
    public int getCount() {
        return coll.size();
    }

    @Override
    public Object getItem(int arg0) {
        return coll.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}