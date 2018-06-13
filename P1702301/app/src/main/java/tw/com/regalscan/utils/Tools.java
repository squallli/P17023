package tw.com.regalscan.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import tw.com.regalscan.db.FlightData;

/**
 * Created by Heidi on 2017/5/25.
 */

public class Tools {

    // 重新排序所有單據
    public static ArrayList<String> resortListNo(ArrayList<String> tmpList) {

        ArrayList<String> noChar = new ArrayList<>();
        ArrayList<String> pChar = new ArrayList<>();
        ArrayList<String> vChar = new ArrayList<>();
        for (String s : tmpList) {
            if (s.substring(0, 1).equals("P")) {
                pChar.add(s);
            } else if (s.substring(0, 1).equals("V")) {
                vChar.add(s);
            } else {
                noChar.add(s);
            }
        }
        noChar = sortNo(noChar);
        pChar = sortNo(pChar);
        vChar = sortNo(vChar);
        tmpList = new ArrayList<>();

        for (String s : noChar) {
            tmpList.add(s);
        }
        for (String s : pChar) {
            tmpList.add(s);
        }
        for (String s : vChar) {
            tmpList.add(s);
        }

        return tmpList;
    }


    private static ArrayList<String> sortNo(ArrayList<String> originList) {
        Collections.sort(originList, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return extractInt(o1) - extractInt(o2);
            }
            // \d : 數字0-9
            // \D : 非數字，數字的除外集合

            int extractInt(String s) {
                String num = s.replaceAll("\\D", "");
                // return 0 if no digits found
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
        });
        return originList;
    }

    // 確認有沒有網路
    public static boolean isInternetLinked(Activity activity) {
        boolean result;
        ConnectivityManager connManager = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        result = !(info == null || !info.isConnected()) && info.isAvailable();
        return result;
    }

    // Msg產生
    public static Message createMsg(int msgWhat) {
        Message msg = new Message();
        msg.what = msgWhat;
        return msg;
    }

    public static byte[] stringToByte(String plainText, String charset) {
        try {
            //String to Hex
            String hexString = String.format("%040x", new BigInteger(1, plainText.getBytes(charset)));

            //Hex to byte
            byte[] bytes = new byte[hexString.length() / 2];
            for (int i = 0; i < bytes.length; i++)
                bytes[i] = (byte)Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
            return bytes;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String byteArrayToString(byte[] b) {
        //byte to Hex
        StringBuilder hexString = new StringBuilder();
        for (byte aB : b) hexString.append(Integer.toHexString((aB & 0x000000FF) | 0xFFFFFF00).substring(6));

        //Hex to String
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < hexString.length(); i += 2)
            str.append((char)Integer.parseInt(hexString.substring(i, i + 2), 16));

        return str.toString();
    }

    public static String byteArrayToHexString(byte[] b) {
        StringBuilder result = new StringBuilder();
        for (byte aB : b) {
            result.append(Integer.toHexString((aB & 0x000000FF) | 0xFFFFFF00).substring(6));
        }
        return result.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4)
                + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
            (b[2] & 0xFF) << 8 |
            (b[1] & 0xFF) << 16 |
            (b[0] & 0xFF) << 24;
    }

    public static byte[] intToByteArray(int a) {
        return new byte[]{
            (byte)((a >> 24) & 0xFF),
            (byte)((a >> 16) & 0xFF),
            (byte)((a >> 8) & 0xFF),
            (byte)(a & 0xFF)
        };
    }

    public static long byteArrayToLong(byte[] b) {
        long i = 0;
        int j = 0;
        for (byte c : b) {
            String s = Integer.toHexString((c & 0x000000FF) | 0xFFFFFF00).substring(6);
            i += (long)(Integer.parseInt(s, 16) * Math.pow(256, j));
            j++;
        }
        return i;
    }

    //判斷總金額是否有小數位金額，回傳處理過的可顯示String
    public static String getModiMoneyString(double originMoney) {
        if (Math.abs(originMoney) % 2 == 0 || Math.abs(originMoney) % 2 == 1) { //整數
            return String.valueOf((int)originMoney);
        }
        return String.valueOf(originMoney);
    }

    //清空資料夾下的檔案,並以遞回方式以避免該資料夾下還有其他子資料夾
    public static void deleteSubFile(File file) {
        String[] files = file.list();
        for (String file1 : files) {
            File subfile = new File(file, file1);
            if (subfile.isDirectory()) {
                deleteSubFile(subfile);
            }
//            System.out.println("File : " + subfile.getName() + " delete...");
            subfile.delete();
        }
//        System.out.println("Directory : " +file.getName() + " delete...");
        file.delete();
    }


    /**
     * 信用卡有效期限編碼
     */
    public static String CreditCardDateDeCode(String CardDate) {

        if (CardDate.length() != 4) {
            return "";
        }

        //信用卡的日期以月年運算,所以要換位置
//        CardDate = CardDate.substring(2, 4) + CardDate.substring(0, 2);

        //欲回傳的編碼
        String NewDataCode;

        //開始編碼
        String FliDate = FlightData.FlightDate;
        int DeCode1 = Integer.valueOf(FliDate.substring(0, 1) + Integer.valueOf(FliDate.substring(1, 2)));
        DeCode1 = ((DeCode1 % 10) + Integer.valueOf(CardDate.substring(0, 1))) % 10;
        int DeCode2 = Integer.valueOf(FliDate.substring(2, 3) + Integer.valueOf(FliDate.substring(3, 4)));
        DeCode2 = ((DeCode2 % 10) + Integer.valueOf(CardDate.substring(1, 2))) % 10;
        int DeCode3 = Integer.valueOf(FliDate.substring(4, 5) + Integer.valueOf(FliDate.substring(5, 6)));
        DeCode3 = ((DeCode3 % 10) + Integer.valueOf(CardDate.substring(2, 3))) % 10;
        int DeCode4 = Integer.valueOf(FliDate.substring(6, 7) + Integer.valueOf(FliDate.substring(7, 8)));
        DeCode4 = ((DeCode4 % 10) + Integer.valueOf(CardDate.substring(3, 4))) % 10;
        int CheckSum = (DeCode1 + DeCode2 + DeCode3 + DeCode4) % 10;

        //串起來
        NewDataCode = String.valueOf(DeCode1) + String.valueOf(DeCode2) + String.valueOf(DeCode3) + String.valueOf(DeCode4) + String.valueOf(CheckSum);
        return NewDataCode;
    }

    /**
     * 取得信用卡ACQ
     */
    public static String GetCreditCardACQ(String CardType) {
        switch (CardType) {
            case "VISA":
            case "MASTER":
            case "JCB":
            case "CUP":
                return "National Credit Card Center";
            case "AMX":
                return "American Express Int'l(Taiwan),Inc";
            default:
                return "";
        }
    }

    /**
     * 取得信用卡MID
     */
    public static String GetCreditCardMID(String CardType, String CurDvr) {
        if (CurDvr.equals("TWD")) {
            switch (CardType) {
                case "VISA":
                    return "1412000261";
                case "MASTER":
                    return "1412000279";
                case "JCB":
                    return "1412000287";
                case "AMX":
                    return "0112000772";
                case "CUP":
                    return "1412000371";
                default:
                    return "20102349";
            }
        } else {
            switch (CardType) {
                case "VISA":
                    return "1412000232";
                case "MASTER":
                    return "1412000240";
                case "JCB":
                    return "1412000253";
                case "AMX":
                    return "0112001279";
                case "CUP":
                    return "1412000371";
                default:
                    return "40001018";
            }
        }
    }

    /**
     * 根據觸控位置，判斷是否隱藏鍵盤
     *
     * @param event
     * @param view
     */
    public static void hideSoftKeyboard(MotionEvent event, View view, Activity activity) {
        try {
            if (view != null && view instanceof EditText) {
                int[] location = {0, 0};
                view.getLocationInWindow(location);
                int left = location[0], top = location[1], right = left + view.getWidth(), bottom = view.getHeight();
                if (event.getRawX() < left || event.getRawX() > right || event.getRawY() < top || event.getRawY() > bottom) {
                    IBinder token = view.getWindowToken();
                    InputMethodManager inputMethodManager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
