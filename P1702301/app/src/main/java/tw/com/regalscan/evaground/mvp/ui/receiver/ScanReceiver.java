package tw.com.regalscan.evaground.mvp.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by tp00175 on 2017/12/15.
 */

public class ScanReceiver {

    private Context mContext;

    private IntentFilter mIntentFilter;
    private Vibrator mVibrator;
    private SoundPool mSoundPool;
    private int soundID;

    public ScanReceiver(Context context) {
        this.mContext = context;

        ScanManager scanManager = new ScanManager();
        scanManager.openScanner();

        scanManager.switchOutputMode(0);
        mVibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mSoundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
        soundID = mSoundPool.load("/etc/Scan_new.ogg", 1);

        mIntentFilter = new IntentFilter();
        int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
        String[] value_buf = scanManager.getParameterString(idbuf);
        if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
            mIntentFilter.addAction(value_buf[0]);
        } else {
            mIntentFilter.addAction(ScanManager.ACTION_DECODE);
        }
    }

    public Observable<String> receive() {
        return Observable.create(e -> {
            final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    mSoundPool.play(soundID, 1, 1, 0, 0, 1);
                    mVibrator.vibrate(100);

                    byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
                    int barcodeLen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);

                    e.onNext(new String(barcode, 0, barcodeLen));
                }
            };

            mContext.registerReceiver(broadcastReceiver, mIntentFilter);
            e.setCancellable(() -> mContext.unregisterReceiver(broadcastReceiver));
        });
    }
}
