package tw.com.regalscan.utils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.device.DeviceManager;
import android.util.Base64;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by tp00175 on 2017/9/20.
 */

public class EvaUtils {

    private EvaUtils() {
    }

    /**
     * 刪除指定文件
     */
    public static void deleteFile(String filePath, String fileName) {
//    String path = Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator + fileName;
        String path = filePath + fileName;
        File file = new File(path);

        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 刪除指定資料夾內所有檔案
     */
    public static void deleteAllFileInFolder(String filePath, String folderName) {
        String path = filePath + folderName;
        File file = new File(path);
        if (file.isDirectory()) {
            String[] children = file.list();
            for (String aChildren : children) {
                new File(file, aChildren).delete();
            }
        }
    }

    /**
     * json進行base64加密
     */
    public static String jsonToBase64(JSONObject jsonObject) {

        jsonObject.put("UDID", new DeviceManager().getDeviceId());

        return Base64.encodeToString(Tools.stringToByte(jsonObject.toString(), "UTF-8"), Base64.NO_WRAP);
    }

    /**
     * 檢查座位號碼
     *
     * @param seatNo
     * @return
     */
    public static boolean checkSeatNo(String seatNo) {
        String regExp = "^([0-9][0-9][A-Z])|([0-9][A-Z])$";
        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(seatNo);
        return matcher.matches();
    }
}
