package tw.com.regalscan.wifi;

import android.content.Context;

import com.regalscan.sqlitelibrary.TSQL;
import org.json.JSONException;
import tw.com.regalscan.db.FlightData;

public class WifiUtil {
    private static final String TAG = WifiUtil.class.getSimpleName();

    private WifiConnector mWifiConnector;

    private TSQL mTSQL;

    public WifiUtil(Context context) {
        mWifiConnector = new WifiConnector(context);
        mTSQL = TSQL.getINSTANCE(context, FlightData.SecSeq, "P17023");
    }

    public void connect(String SSID, String PSW, int wifiType) throws WifiConnector.WifiException {
        try {
            mWifiConnector.connectToNetwork(SSID, PSW, wifiType);
        } catch (WifiConnector.WifiException e) {
            mTSQL.WriteLog(FlightData.SecSeq, "WIFI", TAG, "connect", e.toString());
            throw e;
        }
    }

    public void disconnect() {
        try {
            mWifiConnector.disconnectFromNetwork();
        } catch (WifiConnector.WifiException e) {
            mTSQL.WriteLog(FlightData.SecSeq, "WIFI", TAG, "disconnect", e.toString());
        }
    }

    public String getSSID() {
        try {
            return mWifiConnector.getWifiInfo().get("ssid").toString();
        } catch (JSONException | WifiConnector.WifiException e) {
            e.printStackTrace();
            return "";
        }
    }
}
