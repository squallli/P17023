package tw.com.regalscan.wifi;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.text.TextUtils;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;

import timber.log.Timber;

public class WifiConnector {
    private static final String TAG = WifiConnector.class.getSimpleName();
    private static final long DEFAULT_TIMEOUT = 15 * 1000;
    private static final long POLL_TIME = 1000;
    private Context mContext;
    private WifiManager mWifiManager;
    private PrivateKey mPrivateKey;
    private String identity;

    /**
     * Thrown when an error occurs while manipulating Wi-Fi services.
     */
    public static class WifiException extends Exception {
        public WifiException(String msg) {
            super(msg);
        }

        public WifiException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    public WifiConnector(final Context context) {
        mContext = context;
        mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private static String quote(String str) {
        return String.format("\"%s\"", str);
    }

    /**
     * Waits until an expected condition is satisfied for DEFAULT_TIMEOUT.
     *
     * @param checker a <code>Callable</code> to check the expected condition
     * @throws WifiException if DEFAULT_TIMEOUT expires
     */
    private void waitForCallable(final Callable<Boolean> checker, final String timeoutMsg) throws WifiException {
        long endTime = System.currentTimeMillis() + DEFAULT_TIMEOUT;
        try {
            while (System.currentTimeMillis() < endTime) {
                if (checker.call()) {
                    return;
                }
                Thread.sleep(POLL_TIME);
            }
        } catch (final Exception e) {
            throw new WifiException("failed to wait for callable", e);
        }
        throw new WifiException(timeoutMsg);
    }

    /**
     * Adds a Wi-Fi network configuration.
     *
     * @param ssid     SSID of a Wi-Fi network
     * @param psk      PSK(Pre-Shared Key) of a Wi-Fi network. This can be null if the given SSID is for
     *                 an open network.
     * @param wifiType 0 - ; 1 - enterprise Wi-Fi
     * @return the network ID of a new network configuration
     * @throws WifiException if the operation fails
     */
    public int addNetwork(final String ssid, final String psk, final int wifiType) throws WifiException {
        final WifiConfiguration config = new WifiConfiguration();
        final WifiEnterpriseConfig enterpriseConfig = new WifiEnterpriseConfig();
        int networkId = -1;
        try {
            switch (wifiType) {
                case 0:
                    config.SSID = ssid;
                    config.preSharedKey = "\"" + psk + "\"";
                    config.hiddenSSID = true;
                    config.status = WifiConfiguration.Status.ENABLED;
                    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                    config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

                    networkId = mWifiManager.addNetwork(config);
                    break;
                case 1:
                    X509Certificate CA = getCaCrt();
                    X509Certificate userCrt = getUserCrt();
                    config.SSID = ssid;
                    config.hiddenSSID = true;
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.IEEE8021X);
                    enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
                    enterpriseConfig.setCaCertificate(CA);
                    enterpriseConfig.setClientKeyEntry(mPrivateKey, userCrt);
                    if (!TextUtils.isEmpty(this.identity)) {
                        enterpriseConfig.setIdentity(this.identity);
                    }
                    config.enterpriseConfig = enterpriseConfig;

                    networkId = mWifiManager.addNetwork(config);

                    break;
                case 2:
                    // A string SSID _must_ be enclosed in double-quotation marks
                    config.SSID = quote(ssid);
                    if (psk == null) {
                        // KeyMgmt should be NONE only
                        final BitSet keymgmt = new BitSet();
                        keymgmt.set(WifiConfiguration.KeyMgmt.NONE);
                        config.allowedKeyManagement = keymgmt;
                    } else {
                        config.preSharedKey = quote(psk);
                    }

                    networkId = mWifiManager.addNetwork(config);

                    break;
            }

            if (networkId == -1) {
                throw new WifiException("failed to add network");
            }
        } catch (Exception e) {
            throw new WifiException(e.getMessage());
        }
        return networkId;
    }

    /**
     * Removes all Wi-Fi network configurations.
     *
     * @param throwIfFail <code>true</code> if a caller wants an exception to be thrown when the
     *                    operation fails. Otherwise <code>false</code>.
     * @throws WifiException if the operation fails
     */
    public void removeAllNetworks(boolean throwIfFail) throws WifiException {
        List<WifiConfiguration> netlist = mWifiManager.getConfiguredNetworks();
        if (netlist != null) {
            int failCount = 0;
            for (WifiConfiguration config : netlist) {
                if (!mWifiManager.removeNetwork(config.networkId)) {
                    Timber.tag(TAG).w(String.format("failed to remove network id %d (SSID = %s)", config.networkId, config.SSID));
                    failCount++;
                }
            }
            if (0 < failCount && throwIfFail) {
                throw new WifiException("failed to remove all networks.");
            }
            mWifiManager.saveConfiguration();
        }
    }

    /**
     * Check network connectivity by sending a HTTP request to a given URL.
     *
     * @param urlToCheck URL to send a test request to
     * @return <code>true</code> if the test request succeeds. Otherwise <code>false</code>.
     */
    public boolean checkConnectivity(final String urlToCheck) {
        final HttpClient httpclient = new DefaultHttpClient();
        try {
            httpclient.execute(new HttpGet(urlToCheck));
        } catch (final IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Connects a device to a given Wi-Fi network and check connectivity.
     *
     * @param ssid     SSID of a Wi-Fi network
     * @param psk      PSK of a Wi-Fi network
     * @param wifiType of a Wi=Fi network
     * @throws WifiException if the operation fails
     */
    public void connectToNetwork(final String ssid, final String psk, final int wifiType)
            throws WifiException {
        if (!mWifiManager.setWifiEnabled(true)) {
            throw new WifiException("failed to enable wifi");
        }
        updateLastNetwork(ssid, psk);
        waitForCallable(() -> mWifiManager.isWifiEnabled(), "failed to enable wifi");
        removeAllNetworks(false);
        final int networkId = addNetwork(ssid, psk, wifiType);
        if (!mWifiManager.enableNetwork(networkId, true)) {
            throw new WifiException(String.format("failed to enable network %s", ssid));
        }
        if (!mWifiManager.saveConfiguration()) {
            throw new WifiException(String.format("failed to save configuration", ssid));
        }
        waitForCallable(() -> {
            final SupplicantState state = mWifiManager.getConnectionInfo()
                    .getSupplicantState();
            return SupplicantState.COMPLETED == state;
        }, String.format("failed to associate to network %s", ssid));
        waitForCallable(() -> {
            final WifiInfo info = mWifiManager.getConnectionInfo();
            return 0 != info.getIpAddress();
        }, String.format("dhcp timeout when connecting to wifi network %s", ssid));
//        waitForCallable(() -> checkConnectivity(urlToCheck), String.format("request to %s failed after connecting to wifi network %s",
//            urlToCheck, ssid));
    }

    /**
     * Disconnects a device from Wi-Fi network and disable Wi-Fi.
     *
     * @throws WifiException if the operation fails
     */
    public void disconnectFromNetwork() throws WifiException {
        if (mWifiManager.isWifiEnabled()) {
//            removeAllNetworks(false);
            if (!mWifiManager.setWifiEnabled(false)) {
                throw new WifiException("failed to disable wifi");
            }
            waitForCallable(() -> !mWifiManager.isWifiEnabled(), "failed to disable wifi");
        }
    }

    /**
     * Returns Wi-Fi information of a device.
     *
     * @return a {@link JSONObject} containing the current Wi-Fi status
     * @throws WifiException if the operation fails
     */
    public JSONObject getWifiInfo() throws WifiException {
        final JSONObject json = new JSONObject();
        try {
            final WifiInfo info = mWifiManager.getConnectionInfo();
            json.put("ssid", info.getSSID());
            json.put("bssid", info.getBSSID());
            final int addr = info.getIpAddress();
            // IP address is stored with the first octet in the lowest byte
            final int a = (addr) & 0xff;
            final int b = (addr >> 8) & 0xff;
            final int c = (addr >> 16) & 0xff;
            final int d = (addr >> 24) & 0xff;
            json.put("ipAddress", String.format("%s.%s.%s.%s", a, b, c, d));
            json.put("linkSpeed", info.getLinkSpeed());
            json.put("rssi", info.getRssi());
            json.put("macAddress", info.getMacAddress());
        } catch (final JSONException e) {
            throw new WifiException(e.toString());
        }
        return json;
    }

    /**
     * Reconnects a device to a last connected Wi-Fi network and check connectivity.
     *
     * @throws WifiException if the operation fails
     */
//    public void reconnectToLastNetwork() throws WifiException {
//        final SharedPreferences prefs = mContext.getSharedPreferences(TAG, 0);
//        final String ssid = prefs.getString("ssid", null);
//        final String psk = prefs.getString("psk", null);
//        if (ssid == null) {
//            throw new WifiException("No last connected network.");
//        }
//        connectToNetwork(ssid, psk);
//    }
    private void updateLastNetwork(final String ssid, final String psk) {
        final SharedPreferences prefs = mContext.getSharedPreferences(TAG, 0);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ssid", ssid);
        editor.putString("psk", psk);
        editor.commit();
    }

    /**
     * 取得 CA 憑證
     *
     * @return CA certificate
     * @throws Exception if the operation fails
     */
    private X509Certificate getCaCrt() throws Exception {
        String serverCertFile = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "AirlineCA.cer";
        CertificateFactory certFactory;
        FileInputStream inStream;
        X509Certificate certificate;
        certFactory = CertificateFactory.getInstance("X.509");
        inStream = new FileInputStream(serverCertFile);
        certificate = (X509Certificate) certFactory.generateCertificate(inStream);
        inStream.close();
        return certificate;
    }

    /**
     * 取得使用者憑證
     *
     * @return user's certificate
     * @throws Exception if the operation fails
     */
    private X509Certificate getUserCrt() throws Exception {
        String path = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS;
        X509Certificate certificate = null;
        File directory = new File(path);
        File[] files = directory.listFiles();

        for (File file : files) {
            if (file.getName().matches("^EVAPOS-EVA.*$")) {
                path = path + File.separator + file.getName();
                this.identity = file.getName().substring(0, file.getName().length() - 9);
                break;
            }
        }

        KeyStore keyStore = KeyStore.getInstance("pkcs12");
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
        keyStore.load(inputStream, "23225229".toCharArray());
        Enumeration<String> aliases = keyStore.aliases();

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            certificate = (X509Certificate) keyStore.getCertificate(alias);

            mPrivateKey = (PrivateKey) keyStore.getKey(alias, "23225229".toCharArray());
        }
        return certificate;
    }
}
