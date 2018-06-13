package tw.com.regalscan.evaair.transfer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.regalscan.sqlitelibrary.TSQL;

import java.lang.ref.WeakReference;
import java.util.EnumMap;
import java.util.Map;

import tw.com.regalscan.R;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;


public class TransferOutActivity02 extends AppCompatActivity {


    private Button btnExit, btnPrint;
    public Context mContext;
    public Activity mActivity;
    private ImageView imageQRCode;
    private Bitmap bitmapQRCode;
    private String teansferString;
    private ProgressDialog mloadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_out_02);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            teansferString = (String) bundle.get("TransferList");
            init();
        } else {
            MessageBox.show("", "Get transfer data error", mContext, "Return");
            finish();
        }
    }

    private void init() {
        mContext = this;
        mActivity = this;

        //String轉成QR Code
        bitmapQRCode = createQRCode(teansferString);

        // 設定為 QR code 影像
        imageQRCode = findViewById(R.id.imageViewQR);
        imageQRCode.setImageBitmap(bitmapQRCode);

        //btn
        btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(view -> {
            //回到transfer menu
            if (ActivityManager.isActivityInStock("TransferOutActivity01")) {
                ActivityManager.removeActivity("TransferOutActivity01");
            }

            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();

            if (!ActivityManager.isActivityInStock("TransferActivity01")) {
                Intent mIntent = new Intent(mActivity, TransferActivity01.class);
                mActivity.startActivity(mIntent);
            }
        });

        btnPrint = findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(view -> printData());
        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }


    private static class PrinterHandler extends Handler {
        private WeakReference<TransferOutActivity02> weakActivity;

        PrinterHandler(TransferOutActivity02 a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            TransferOutActivity02 handlerActivity = weakActivity.get();
            Context handlerContext = handlerActivity.mContext;

            handlerActivity.mloadingDialog.dismiss();
            switch (msg.what) {
                case 1: // 沒紙
                    if (MessageBox.show("", "No paper, reprint?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData();
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 2: //Print error
                    if (MessageBox.show("", "Print error, retry?", handlerContext, "Yes", "No")) {
                        handlerActivity.printData();
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 3: //成功
                    handlerActivity.doPrintFinal();
                    break;
            }
        }
    }

    private void doPrintFinal() {
        if (MessageBox.show("", "Success", mContext, "Ok")) {
            // 回到transfer menu
            if (ActivityManager.isActivityInStock("TransferOutActivity01")) {
                ActivityManager.removeActivity("TransferOutActivity01");
            }

            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());

            if (!ActivityManager.isActivityInStock("TransferActivity01")) {
                Intent mIntent = new Intent(mActivity, TransferActivity01.class);
                mActivity.startActivity(mIntent);
            }
        }
    }

    private void printData() {
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        new Thread() {
            public void run() {
                //列印QR Code
                PrintAir printer = new PrintAir(mContext, Integer.valueOf(FlightData.SecSeq));
                try {
                    // TransferNo (Transfer String前九碼 (車櫃後三碼+ 時分秒六碼)) , QRString
                    if (printer.printTransferOut(teansferString.substring(0, 9), teansferString) == -1) {
                        printerHandler.sendMessage(Tools.createMsg(1));
                    } else {
                        printerHandler.sendMessage(Tools.createMsg(3));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                    _TSQL.WriteLog(FlightData.SecSeq,
                            "System", "TransferOutActivity02", "printTransferOut", e.getMessage());
                    printerHandler.sendMessage(Tools.createMsg(2));
                }
            }
        }.start();
    }


    //產生QR Code
    private Bitmap createQRCode(String teansferString) {

        int QRCodeWidth = 200;
        int QRCodeHeight = 200;

        //內容編碼
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            // 容錯率姑且可以將它想像成解析度，分為 4 級：L(7%)，M(15%)，Q(25%)，H(30%)
            // 設定 QR code 容錯率為 H
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

            // 建立 QR code 的資料矩陣
            BitMatrix result = writer.encode(teansferString, BarcodeFormat.QR_CODE, QRCodeWidth, QRCodeHeight, hints);
            // ZXing 還可以生成其他形式條碼，如：BarcodeFormat.CODE_39、BarcodeFormat.CODE_93、BarcodeFormat.CODE_128、BarcodeFormat.EAN_8、BarcodeFormat.EAN_13...

            //建立點陣圖
            Bitmap bitmap = Bitmap.createBitmap(QRCodeWidth, QRCodeHeight, Bitmap.Config.ARGB_8888);
            // 將 QR code 資料矩陣繪製到點陣圖上
            for (int y = 0; y < QRCodeHeight; y++) {

                for (int x = 0; x < QRCodeWidth; x++) {
                    bitmap.setPixel(x, y, result.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            MessageBox.show("", "Building QR code error", mContext, "Return");
        }
        return null;
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