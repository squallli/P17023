package tw.com.regalscan.component;

/**
 * Created by Heidi on 2017/3/8.
 */

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AsyncImageFileLoader {
    private static String TAG = "AsyncImageFileLoader";
    private HashMap<String, SoftReference<Bitmap>> imageCache;

    public AsyncImageFileLoader() {
        imageCache = new HashMap<String, SoftReference<Bitmap>>();
    }

    public Bitmap loadBitmap(final String imageFile, final int show_width, final int show_height
            , final ImageCallback imageCallback) {

        //如果此圖片已讀取過的話，將會暫存在cache中，所以可以直接從cache中讀取
        if (imageCache.containsKey(imageFile)) {
            SoftReference<Bitmap> softReference = imageCache.get(imageFile);
            Bitmap bmp = softReference.get();
            if (bmp != null) {
                return bmp;
            }
        }

        final Handler handler = new Handler() {
            public void handleMessage(Message message) {
                imageCallback.imageCallback((Bitmap) message.obj, imageFile);
            }
        };

        new Thread() {
            @Override
            public void run() {
                //sleep 500ms 讓GridView可以先顯示所有item，否則會因為開始讀取圖片而使系統忙碌
                //造成圖片讀取完畢後才顯示所有item
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Bitmap bmp = loadImageFromFile(imageFile, show_width, show_height);
                imageCache.put(imageFile, new SoftReference<Bitmap>(bmp));
                Message message = handler.obtainMessage(0, bmp);
                handler.sendMessage(message);
            }
        }.start();
        return null;
    }

    //從資料夾讀取圖片
    public Bitmap loadImageFromFile(String imgFile, int display_width, int display_height) {
//        Log.d(TAG, "readBitmap");

        File imagePath = new File(Environment.getExternalStorageDirectory() + File.separator+ "Download" + File.separator + "pic"
                , imgFile + ".jpg");

        BitmapFactory.Options opt = new BitmapFactory.Options();

        opt.inJustDecodeBounds = true; //設定BitmapFactory.decodeStream不decode，只抓取原始圖片的長度和寬度
        BitmapFactory.decodeFile(imagePath.getAbsolutePath(), opt);

        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opt.inPurgeable  = true;
        opt.inInputShareable = true;
        //計算適合的縮放大小，避免OutOfMenery
        opt.inSampleSize = computeSampleSize(opt, -1, display_width*display_height);
        opt.inJustDecodeBounds = false;//設定BitmapFactory.decodeStream需decodeFile

        Bitmap bmp = BitmapFactory.decodeFile(imagePath.getAbsolutePath(), opt);
        System.gc(); // system garbage recycle

        if(bmp != null) {
//            Log.d(TAG, "decodeFile success");
            return bmp;
        }
        else {
//            Log.d(TAG, "bmp == null");
            return null;
        }

    }


    public interface ImageCallback {
        void imageCallback(Bitmap imageBitmap, String imageFile);
    }

    public static int computeSampleSize(BitmapFactory.Options opts, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(opts, minSideLength, maxNumOfPixels);

        int roundedSize;

        if(initialSize <= 8) {
            roundedSize = 1;
            while(roundedSize < initialSize) {
                roundedSize<<=1;
            }
        }
        else {
            roundedSize = (initialSize+7)/8*8;
        }

//        Log.d(TAG, "roundedSize = "+roundedSize);

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options opts, int minSideLength, int maxNumOfPixels) {

        double w = opts.outWidth;
        double h = opts.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int)Math.ceil(Math.sqrt(w*h/maxNumOfPixels));
        int upperBound = (maxNumOfPixels == -1) ? 128 : (int)Math.min(Math.floor(w/minSideLength),Math.floor(h/minSideLength));

        if(upperBound < lowerBound) {
            return lowerBound;
        }

        if((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        }
        else if(minSideLength == -1) {
            return lowerBound;
        }
        else {
            return upperBound;
        }
    }

}