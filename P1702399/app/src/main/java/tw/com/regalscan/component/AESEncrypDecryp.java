package tw.com.regalscan.component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

import tw.com.regalscan.utils.Tools;

/**
 * Created by Heidi on 2017/6/2.
 */

public class AESEncrypDecryp {

    private AESEncrypDecryp() {

    }

    // aes 256 cbc加密需使用相同一組 32位英數組合Key 與 16位英數組合iv
//    final private String IV_STRING= "GabeHeidiSamGabe";
    static final private byte[] IV_BYTE = new byte[]{
        (byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xEE,
        (byte)0xFF, (byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD,
        (byte)0xEE, (byte)0xFF, (byte)0xAA, (byte)0xBB, (byte)0xCC,
        (byte)0xDD};

    // NO_WRAP: 略去所有的換行符
    // URL_SAFE: 加密時不使用對URL和文件名有特殊意義的字符來作為加密字符 (用"-"和"_"取代"+"和"/")
    final private int FLAGS = Base64.NO_WRAP | Base64.URL_SAFE;

//    public void sample(){
//        // 航班日期+航班號碼+車號, 剩下補0
//        String key= "20170101BR00120012A1000000000000";
//        String originData= "Test Encrypt Data";
//
//        // 餵Key
//        if(key.length()==32){
//            AESEncrypDecryp aes= new AESEncrypDecryp();
//            String eee= aes.getEncryptData(originData, key);
//            String ddd= aes.getDectyptData(eee, key);
//        }
//    }

    // 取得加密資料, 回傳Hex String
    public static String getEncryptData(String originData, String keyString) throws Exception {
        if (keyString != null && keyString.length() <= 32) {

            StringBuilder sb = new StringBuilder(keyString);
            // 航班日期+航班號碼+車號, 剩下補0
            while (sb.length() < 32) {
                sb.append("0");
            }
            keyString = sb.toString();

            // 產生 Secret Keys
//                byte[] raw = keyString.getBytes();
            byte[] raw = keyString.getBytes("UTF-8");

            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // 算法/模式/補碼方式

            // 使用CBC模式需要一個向量iv, 增加加密算法的強度
            // iv在解密時, 也要用當初加密使用的相同iv
//                IvParameterSpec iv = new IvParameterSpec(IV_STRING.getBytes());
            IvParameterSpec iv = new IvParameterSpec(IV_BYTE);

            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            // 編碼使用UTF-8, 確保中文字不會出錯
//                byte[] encrypted = cipher.doFinal(originData.getBytes("UTF-8"));
            byte[] encrypted = cipher.doFinal(originData.getBytes("UTF-8"));

            // 使用BASE64轉碼, 同時能起到2次加密的作用
//                return Base64.encodeToString(encrypted, FLAGS);
            return Tools.byteArrayToHexString(encrypted);
        }

        return null;
    }

    // 取得解密資料
    public static String getDectyptData(String encryptedString, String keyString) throws Exception {
        if (keyString != null && keyString.length() <= 32) {
            StringBuilder sb = new StringBuilder(keyString);
            // FlightData.FlightDate + FlightData.CartNo,  剩下補0
            while (sb.length() < 32) {
                sb.append("0");
            }
            keyString = sb.toString();

            // 產生 Secret Key
//                byte[] raw = keyString.getBytes("UTF-8");
            byte[] raw = keyString.getBytes("UTF-8");

            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

//                IvParameterSpec iv = new IvParameterSpec(IV_STRING.getBytes());
            IvParameterSpec iv = new IvParameterSpec(IV_BYTE);

            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            //先用base64解密
//                byte[] encryptedByte = Base64.decode(encryptedString, FLAGS);

            //解aes
//                byte[] result = cipher.doFinal(encryptedByte);
            byte[] result = cipher.doFinal(Tools.hexStringToByteArray(encryptedString));

//                return new String(result);
            return new String(result, "UTF-8");
        }
        return null;
    }

}
