package tw.com.regalscan.evaair;


import android.os.PowerManager;
import android.widget.TextView;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import tw.com.regalscan.R;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.AsyncImageFileLoader;
import tw.com.regalscan.adapters.ImageAdapter;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.utils.Tools;

public class PictureActivity extends AppCompatActivity implements SurfaceHolder.Callback{



    private static GridView mGridView;
    private ImageView mImageView;
    static public List<String> mThumbs;  //存放縮圖的id
    static public List<String> mImagePaths;  //存放圖片的路徑
    private static ImageAdapter mImageAdapter;  //用來顯示縮圖, thumbs的圖片ID去取縮圖
    private static Context mContext;
    private Button mBtn01, mBtn02, mbtnSave, mbtnCancel;
    private TextView txtPhotoHint;

    public Activity mActivity;
    private Button btnReturn;

    //相機
    private Camera mCamera=null; //紀錄尋找到的相機物件
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Bitmap mBmpPhoto=null;
    private List<Camera.Size> mSupportedSizes;   //紀錄相機支援的解析度
    private Camera.Size optimalSize;
    private final int optimalPixels=5000000; //預設最佳拍照解析度為500萬畫素(或是該相機的最大值)
    private LinearLayout camreaBtnRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);




        mContext=PictureActivity.this;
        mActivity = this;
        enableExpandableList();

        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
            }
        });

        mGridView = findViewById(R.id.gridView1);
        mImageView = findViewById(R.id.imageView2);

        //相機, 相簿, 截圖
        mBtn01= findViewById(R.id.btnCamera);
        mBtn02= findViewById(R.id.btnAlbum);
        camreaBtnRow= findViewById(R.id.rowBtn02);
        txtPhotoHint= findViewById(R.id.textPhotoHint);

        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);

        //拍照, 儲存, 取消
        mSurfaceView= findViewById(R.id.surfaceView);

        ViewTreeObserver vto = mGridView.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                mGridView.getViewTreeObserver().removeOnPreDrawListener(this);
//                int finalHeight = mGridView.getMeasuredHeight();
//                int finalWidth = mGridView.getMeasuredWidth();
                return true;
            }
        });

        mSurfaceHolder=mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mbtnSave= findViewById(R.id.btnSave);
        mbtnCancel= findViewById(R.id.btnCancel);

        //拍照
        mBtn01.setOnClickListener((new OnClickListener(){
            @Override
            public void onClick(View v) {
                //初始化相機
                try{
                    setCameraComponent();
                }catch (Exception e){
                    e.printStackTrace();
                    MessageBox.show("", "Init error", mContext, "Return");
                    return;
                }
                mImageView.setVisibility(View.GONE);
                mGridView.setVisibility(View.GONE);
                mBtn01.setVisibility(View.VISIBLE);
                mBtn02.setVisibility(View.VISIBLE);

                mbtnSave.setEnabled(false);
                mbtnCancel.setEnabled(false);

                txtPhotoHint.setVisibility(View.VISIBLE);
                camreaBtnRow.setVisibility(View.GONE);
                mSurfaceView.setVisibility(View.VISIBLE);

                mCamera.startPreview();
            }
        }));

        //相簿
        mBtn02.setOnClickListener((new OnClickListener(){
            @Override
            public void onClick(View v) {
                // 取出DCIM圖片, 放入Adapter, 連接GridView
                refreshImageList();
                txtPhotoHint.setVisibility(View.GONE);
                camreaBtnRow.setVisibility(View.GONE);
                mSurfaceView.setVisibility(View.GONE);
                mGridView.setVisibility(View.VISIBLE);
                mBtn01.setVisibility(View.VISIBLE);
                mBtn02.setVisibility(View.VISIBLE);

                // 停止預覽, 清空照片
                if(mCamera!=null){
                    mCamera.release();
                    mCamera=null;
                }
                if(mBmpPhoto!=null){
                    mBmpPhoto.recycle();
                    mBmpPhoto= null;
                }
            }
        }));
        mBtn02.performClick();

        // 點大圖片顯示adapter
        mImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mImageView.setVisibility(View.GONE);
                mGridView.setVisibility(View.VISIBLE);
                mBtn01.setVisibility(View.VISIBLE);
                mBtn02.setVisibility(View.VISIBLE);
                btnReturn.setVisibility(View.VISIBLE);
            }

        });
        mImageView.setVisibility(View.GONE);

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


    //取得相機支援最少滿足optimalPixels設定值的解析度，若低於optimalPixels則傳回最高解析度
    private Camera.Size getOptimalSize(List<Camera.Size> sizes) {
        Camera.Size o_size=null;
        int pixels=0;

        for(Camera.Size size:sizes) {
            int tmp=size.width*size.height;

            if(pixels<optimalPixels) {
                if(tmp>pixels) {
                    pixels=tmp;
                    o_size=size;
                }
            } else {
                if(tmp>=optimalPixels && tmp<pixels) {
                    pixels=tmp;
                    o_size=size;
                }
            }
        }
        return o_size;
    }

    //取得相機最大解析度
    private Camera.Size getMaxSize(List<Camera.Size> sizes){
        Camera.Size max=sizes.get(0);

        for(int i=1; i<sizes.size(); i++){
            Camera.Size tmp=sizes.get(i);
            if(tmp.height>=max.height && tmp.width>=max.width)
                max=tmp;
        }
        return max;
    }

    //設定相機
    public void setCameraComponent(){
        Camera.CameraInfo cameraInfo=new Camera.CameraInfo();

        //尋找後置相機
        if(mCamera==null){
            for(int i=0;i<Camera.getNumberOfCameras();i++){
                Camera.getCameraInfo(i,cameraInfo);
                //判斷是否為後置相機
                if(cameraInfo.facing== Camera.CameraInfo.CAMERA_FACING_BACK){
                    mCamera=Camera.open(i);
                    break;
                }
            }
        }
        //如果找不到後置相機就結束返回
        if(mCamera==null){
            if(MessageBox.show("", "Camera not found.", mContext, "Return")){
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            }
        }

        //儲存
        mbtnSave.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                if(mBmpPhoto!=null){
//                    //將拍照後的照片存放在App目錄下
//                    File imgFile=new File(getFilesDir(),imgFilenameOfCamera);

                    //用日期時間當檔名
                    Date time=new Date(System.currentTimeMillis());
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");

                    //儲存路徑與檔名
//                    String SDCARD_PATH = Environment.getExternalStorageDirectory() +
//                            File.separator + "DCIM" + File.separator + "100ANDRO";
                    String SDCARD_PATH = Environment.getExternalStorageDirectory() +
                        File.separator + Environment.DIRECTORY_PICTURES + File.separator + "Screenshots";

                    File sdPath = new File(SDCARD_PATH);
                    if (!sdPath.exists()) {
                        sdPath.mkdirs();
                    }

                    String imgFilePathCamera=  SDCARD_PATH + File.separator + "EvaPos_" + sdf.format(time) + ".jpg";
                    File imgFile = new File(imgFilePathCamera);

                    try{
                        BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(imgFile));
                        mBmpPhoto.compress(Bitmap.CompressFormat.JPEG,100,bos); //100%表不壓縮
                        bos.flush();
                        bos.close();

                        //回收以節省記憶體
                        mBmpPhoto.recycle();
                        mBmpPhoto= null;

                        // 將照片加入 MediaStore
                        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(imgFile);
                        scanIntent.setData(contentUri);
                        sendBroadcast(scanIntent);

                        mbtnSave.setEnabled(false);
                        mbtnCancel.setEnabled(false);
                        camreaBtnRow.setVisibility(View.GONE);
                        txtPhotoHint.setVisibility(View.VISIBLE);

                        MessageBox.show("", "Save success", mContext, "Return");

                    }catch(Exception ex){
                        ex.printStackTrace();
                        MessageBox.show("", "Save failed", mContext, "Return");
                    }
                    mCamera.startPreview();
                }
            }
        });

        //取消
        mbtnCancel.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                try{
                    if(mBmpPhoto!=null){
                        mBmpPhoto.recycle();
                        mBmpPhoto= null;
                        mCamera.startPreview();
                    }
                    mbtnSave.setEnabled(false);
                    mbtnCancel.setEnabled(false);
                    camreaBtnRow.setVisibility(View.GONE);
                    txtPhotoHint.setVisibility(View.VISIBLE);
                }catch (Exception e){
                    e.printStackTrace();
                    MessageBox.show("", "Cancel error", mContext, "Return");
                }
            }
        });

        //取得相機的設定參數
        Camera.Parameters parameters= mCamera.getParameters();

        //取得相機預覽所支援的解析度
        mSupportedSizes=mCamera.getParameters().getSupportedPreviewSizes();
        optimalSize=getOptimalSize(mSupportedSizes);
        //改變相機的預覽解析度
        parameters.setPreviewSize(optimalSize.width,optimalSize.height);

        //取得相機拍照所支援的解析度
        mCamera.getParameters();
        mSupportedSizes=mCamera.getParameters().getSupportedPictureSizes();
        optimalSize=getMaxSize(mSupportedSizes);
        //改變相機的拍照解析度
        parameters.setPictureSize(optimalSize.width,optimalSize.height);

        //改變拍照格式
        parameters.setPictureFormat(PixelFormat.JPEG);

        //設定儲存的圖片旋轉90度
        parameters.setRotation(90);

        //以改變後的參數設定相機
        mCamera.setParameters(parameters);

        try{
            //設定相機預覽
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch(Exception e){
            e.printStackTrace();
        }

//        //執行Preview，預設的相機會向左轉90度，所以程式設定向右轉90度
        mCamera.setDisplayOrientation(90);

        //使用者按下預覽畫面後執行
        mSurfaceView.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v){
                try{
                    mCamera.autoFocus(mAutoFocusCallback);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }


    // 長按Adapter的物件, 刪除照片
    public static void deleteImage(int position){
        try{
            //依據圖片 id 將 MediaStore 內的圖片資料刪除
            int deleteId=Integer.parseInt(mThumbs.get(position));
            Uri uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, deleteId
            );

            mContext.getContentResolver().delete(uri, null, null);
            //掃描媒體更新
            String[] paths = new String[]{Environment.getExternalStorageDirectory().toString()};
            MediaScannerConnection.scanFile(mContext, paths, null, null);
        }catch (Exception e){
            e.printStackTrace();
            MessageBox.show("", "Delete image error", mContext, "Return");
        }
    }

    // 取得SD卡內的圖片路徑與Id放入list
    public static void refreshImageList(){
        try{
            //掃描媒體更新
            String[] paths = new String[]{Environment.getExternalStorageDirectory().toString()};
            MediaScannerConnection.scanFile(mContext, paths, null, null);

            // 查詢ContentProvider的欄位
            ContentResolver cr = mContext.getContentResolver();

            //要查詢的URI資源
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            //要查詢的字段
            String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };

//        // 條件
//        String selection = MediaStore.Images.Media.MIME_TYPE + "=?";
            // 條件值(參數標準, 圖片)
//        String[] selectionArgs = { "image/jpeg" };
//        // 排序
//        String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";

            //查詢SD卡所有圖片
//        Cursor cursor = cr.query(uri, projection, selection, selectionArgs, sortOrder);
            Cursor cursor = cr.query(uri, projection, null, null, null);

            mThumbs = new ArrayList<String>();
            mImagePaths = new ArrayList<String>();

            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                //獲得圖片路徑 (可用路徑建構 Uri)
                String filepath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

                if( (filepath.length()>=34 && filepath.substring(0, 34).equals("/storage/emulated/0/DCIM/100ANDRO/"))
                    || (filepath.length()>=41 && filepath.substring(0, 41).equals("/storage/emulated/0/Pictures/Screenshots/")) ){
                    mImagePaths.add(filepath);
                    //獲得圖片ID
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                    mThumbs.add(id + "");
                }
            }

            cursor.close();

            // 設定Adapter與更新
            mImageAdapter = new ImageAdapter(mContext, mThumbs);
            mGridView.setAdapter(mImageAdapter);
            mImageAdapter.notifyDataSetChanged();

        }catch (Exception e){
            e.printStackTrace();
            MessageBox.show("", "Refresh image list error", mContext, "Return");
        }
    }

    // 點圖片放大檢視
    public void setImageView(int position){

        try{
            BitmapFactory.Options opt = new BitmapFactory.Options();

            opt.inJustDecodeBounds = true; //設定BitmapFactory.decodeStream不decode，只抓取原始圖片的長度和寬度
            BitmapFactory.decodeFile(mImagePaths.get(position), opt);

            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
            opt.inPurgeable  = true;
            opt.inInputShareable = true;
            //計算適合的縮放大小，避免OutOfMenery
            opt.inSampleSize = AsyncImageFileLoader.computeSampleSize(opt, -1, 720*1280);
            opt.inJustDecodeBounds = false;//設定BitmapFactory.decodeStream需decodeFile

            Bitmap bmp = BitmapFactory.decodeFile(mImagePaths.get(position), opt);
            System.gc(); // system garbage recycle


            if(bmp != null) {
                mImageView.setImageBitmap(bmp);
            }

            mImageView.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.GONE);
            mBtn01.setVisibility(View.GONE);
            mBtn02.setVisibility(View.GONE);
            btnReturn.setVisibility(View.GONE);

        }catch (Exception e){
            e.printStackTrace();
            MessageBox.show("", "Zoom in image error", mContext, "Return");
        }
    }


    //設定先自動對焦再拍照
    private Camera.AutoFocusCallback mAutoFocusCallback=new Camera.AutoFocusCallback(){
        @Override
        public void onAutoFocus(boolean success, Camera camera){
            if(success){ //true:已對焦
                mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
            }
        }
    };

    //按下快門
    private Camera.ShutterCallback shutterCallback=new Camera.ShutterCallback(){
        @Override
        public void onShutter(){ }
    };

    private Camera.PictureCallback rawCallback=new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] data, Camera camera){ }
    };

    private Camera.PictureCallback jpegCallback=new Camera.PictureCallback(){
        @Override
        public void onPictureTaken(byte[] data, Camera camera){
            mCamera.stopPreview();
            mBmpPhoto= BitmapFactory.decodeByteArray(data, 0, data.length);
            mbtnSave.setEnabled(true);
            mbtnCancel.setEnabled(true);
            camreaBtnRow.setVisibility(View.VISIBLE);

            txtPhotoHint.setVisibility(View.INVISIBLE);
        }
    };

    //清空相機變數
    @Override
    protected void onDestroy(){
        super.onDestroy();

        if(mBmpPhoto!=null){
            mBmpPhoto.recycle();
            mBmpPhoto= null;
        }

        if(mCamera!=null){
            mCamera.release();
            mCamera=null;
        }

        System.gc();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        try{
            if(mCamera!=null){
                mCamera.setPreviewDisplay(holder);
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        try{
            if(mCamera!=null){
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
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