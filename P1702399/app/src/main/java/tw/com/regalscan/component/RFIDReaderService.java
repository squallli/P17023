package tw.com.regalscan.component;

import android.content.Context;
import android.device.MagManager;
import android.device.PiccManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;

import java.util.concurrent.ExecutorService;

import timber.log.Timber;
import tw.com.regalscan.evaground.*;

/**
 * Created by gabehsu on 2017/8/30.
 */

public class RFIDReaderService {

    public static final int MSG_OPEN_FAILED = 0;
    public static final int MSG_BLOCK_NO_NONE = 1;
    public static final int MSG_BLOCK_NO_ILLEGAL = 2;
    public static final int MSG_AUTHEN_FAIL = 3;
    public static final int MSG_READ_FAIL = 4;
    public static final int MSG_SHOW_BLOCK_DATA = 5;
    public static final int MSG_ACTIVE_FAIL = 6;
    public static final int MSG_BLOCK_DATA_NONE = 7;
    public static final int MSG_AUTHEN_BEFORE = 8;
    public static final int MSG_FOUND_UID = 9 ;

    public static final int KEY_TYPE_A = 0;
    public static final int KEY_TYPE_B = 1;

    public static final String CARD_UID = "CARD_UID";
    public static final String CARD_BLOCK_DATA = "CARD_BLOCK_DATA";
    public static final String EMPLOYEE_TYPE = "EMPLOYEE_TYPE";

    private String AUTH_KEY = "493530383743";
    private int KEY_TYPE = 0;
    private int BLOCK_NUMBER = 4;

    private final String EVA_AUTH_KEY = "500929520821";
    private final int EVA_KEY_TYPE = 0;
    private final int EVA_BLOCK_NUMBER = 34;

    //原始UID
//    private String originalUID = "";
    private Context mContext;
    private Handler mHandler;
    private PiccManager piccReader;
    private RFIDReaderThread rfidReaderThread;

    //設定驗證Key
    public void SetKeyValue(String Key) {

        this.AUTH_KEY = Key;
    }

    //設定驗證Key type
    public void SetKeyType(int KeyType) {

        this.KEY_TYPE = KeyType;
    }

    //設定讀取區塊
    public void SetBlockNumber(int BlockNumber) {

        this.BLOCK_NUMBER = BlockNumber;
    }

    //起始函式
    public RFIDReaderService (Context context, Handler handler) {
        mHandler = handler;
        mContext = context;
        piccReader = new PiccManager();
    }

    //啟動
    public synchronized void start() {

        if(rfidReaderThread != null) {
            rfidReaderThread.stopRFIDReader();
            rfidReaderThread = null;
        }

        rfidReaderThread = new RFIDReaderThread("RFID_READER");
        rfidReaderThread.start();
    }

    //停止
    public synchronized void stop() {

        if(piccReader != null) {
            piccReader.close();
        }

        if(rfidReaderThread != null) {
            rfidReaderThread.stopRFIDReader();
            rfidReaderThread = null;
        }
    }

    //釋放資源
    public synchronized void Dispose() {

        if(piccReader != null) {
            piccReader.close();
            piccReader = null;
        }

        if(rfidReaderThread != null) {
            rfidReaderThread.stopRFIDReader();
            rfidReaderThread = null;
        }
    }

    private class RFIDReaderThread extends Thread {

        private boolean running = true;
        byte CardType[] = new byte[2];
        byte Atq[] = new byte[14];
        byte[] SN = new byte[10];
        byte SN2[] = new byte[10];
        byte sak[] = new byte[1];
        byte[] blockData = new byte[16];
        String tmpBlockData = null;
        String tmpUID = null;
        int scan_card = -1;
        int SNLen = -1;

        public RFIDReaderThread(String name) {
            super(name);
            running = true;
        }

        public void stopRFIDReader() {
            running = false;
        }

        public void run() {
            if (piccReader != null) {
                int ret = piccReader.open();
                if (ret != 0) {
                    mHandler.sendEmptyMessage(MSG_OPEN_FAILED);
                    return;
                }
            }

            while (running) {
                if (piccReader == null)
                    return;

                //判斷有沒有讀到卡
                scan_card = piccReader.request_norats(CardType, Atq);

                //有讀到卡
                if (scan_card > 0) {
                    SNLen = piccReader.antisel(SN, sak);

                    //驗證是否為長榮集團員工
                    //將Key轉Byte
                    byte[] keyBates = hexStringToByteArray(AUTH_KEY);

                    //取得UID
                    tmpUID = bytesToHexString(SN, SNLen);

                    //若重複讀取則直接離開
//                    if (!originalUID.equals(tmpUID)) {

                        //驗證Key
                        int keyAuth = piccReader.m1_keyAuth(KEY_TYPE, BLOCK_NUMBER, keyBates.length, keyBates, SNLen, SN2);
                        if (keyAuth == 0) {

                            //讀取特定Block
                            piccReader.m1_readBlock(BLOCK_NUMBER, blockData);
                            tmpBlockData = bytesToHexString(blockData, blockData.length);
                            tmpBlockData = convertHexToString(tmpBlockData).replace(" ", "");
                        }
//                        else {
//                            //響聲和震動
//                            SoundTool.getMySound(mContext).playMusic("error");
//                        }

                        //確定有讀取到資料
                        if (tmpBlockData != null && !tmpBlockData.toString().equals("")) {

                            //判斷是哪個集團的
                            //將Key轉Byte
                            keyBates = hexStringToByteArray(EVA_AUTH_KEY);

                            //取得UID
                            tmpUID = bytesToHexString(SN, SNLen);

                            //驗證Key
                            keyAuth = piccReader.m1_keyAuth(EVA_KEY_TYPE, EVA_BLOCK_NUMBER, keyBates.length, keyBates, SNLen, SN2);

                            //響聲和震動
                            SoundTool.getMySound(mContext).playMusic("success");

                            if (keyAuth == 0) {
                                Message msg = mHandler.obtainMessage(MSG_SHOW_BLOCK_DATA);
                                Bundle bundle = new Bundle();
                                bundle.putString(CARD_BLOCK_DATA, tmpBlockData);
                                bundle.putString(CARD_UID, tmpUID);
                                bundle.putString(EMPLOYEE_TYPE, "EVA");
                                msg.setData(bundle);
                                mHandler.sendMessage(msg);
                            }
                            else {
                                Message msg = mHandler.obtainMessage(MSG_SHOW_BLOCK_DATA);
                                Bundle bundle = new Bundle();
                                bundle.putString(CARD_BLOCK_DATA, tmpBlockData);
                                bundle.putString(CARD_UID, tmpUID);
                                bundle.putString(EMPLOYEE_TYPE, "EGAS");
                                msg.setData(bundle);
                                mHandler.sendMessage(msg);
                            }

                            //加入到讀取的UID中
//                            originalUID = tmpUID;x
                        }
//                    }
                    tmpBlockData = null;

                    //有讀到卡要停久一點
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Convert a string of hex data into a byte array.
     * Original author is:
     * @param s The hex string to convert
     * @return An array of bytes with the values of the string.
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i+1), 16));
            }
        } catch (Exception e) {

        }
        return data;
    }

    public static String bytesToHexString(byte[] src, int len) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        if (len <= 0) {
            return "";
        }
        for (int i = 0; i < len; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private static String convertHexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        for (int i = 0; i < hex.length() - 1; i += 2) {
            String output = hex.substring(i, i + 2);
            int decimal = Integer.parseInt(output, 16);
            sb.append((char) decimal);
            temp.append(decimal);
        }

        return sb.toString();
    }
}
