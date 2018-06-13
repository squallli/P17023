package tw.com.regalscan.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by tp00175 on 2017/7/18.
 */

public class PrefsUtils {

  public static final String sSTATUS = "STATUS"; //0 = test, 1 = trial, 2 = official
  public static final String sAPI_test = "API_TEST";
  public static final String sAPI_trial = "API_TRIAL";
  public static final String sAPI_official = "API_OFFICIAL";

  public static final String sFTP_id = "FTP_ID";
  public static final String sFTP_psw = "FTP_PSW";
  public static final String sFTP_ip= "FTP_IP";

  public static final String sTIMEOUT_ife = "TIMEOUT_IFE";
  public static final String sTIMEOUT_ground = "TIMEOUT_GROUND";
  public static final String sPROPERTYNO = "PROPERTYNO";

  public static final String sWPA3_ssid = "WPA2_SSID";
  public static final String sWPA3_psw = "WPA2_PSW";

  private static SharedPreferences getPreferences(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  private static SharedPreferences.Editor getPreferencesEditor(Context context) {
    return getPreferences(context).edit();
  }


  public static String getPreferenceValue(Context context, String key, String defValue) {
    return getPreferences(context).getString(key, defValue);
  }

  public static int getPreferenceValue(Context context, String key, int defValue) {
    return getPreferences(context).getInt(key, defValue);
  }

  public static boolean getPreferenceValue(Context context, String key, boolean defValue) {
    return getPreferences(context).getBoolean(key, defValue);
  }


  public static void setPreferenceValue(Context context, String key, String prefsValue) {
    getPreferencesEditor(context).putString(key, prefsValue).apply();
  }

  public static void setPreferenceValue(Context context, String key, int prefsValue) {
    getPreferencesEditor(context).putInt(key, prefsValue).apply();
  }

  public static void setPreferenceValue(Context context, String key, boolean prefsValue) {
    getPreferencesEditor(context).putBoolean(key, prefsValue).apply();
  }


  public static boolean containsPreferenceKey(Context context, String key) {
    return getPreferences(context).contains(key);
  }

  public static void removePreferenceValue(Context context, String key) {
    getPreferencesEditor(context).remove(key).apply();
  }
}
