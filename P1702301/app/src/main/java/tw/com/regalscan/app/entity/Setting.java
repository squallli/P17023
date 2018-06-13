package tw.com.regalscan.app.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tp00175 on 2017/12/4.
 */

public class Setting implements Parcelable{

    private String WebServiceURL;
    private String IPAddress;
    private String IFESSID;
    private String IFEKEY;
    private String IFEIP;
    private String GroundConnectTryCount;
    private String GroundSSID;
    private String FTPUserName;
    private String FTPUserPassword;
    private String FTPServerPort;
    private String FTPServerIP;
    private String Certificate;
    private String BlackFTPUserName;
    private String BlackFTPUserPassword;
    private String BlackFTPServerPort;
    private String BlackFTPServerIP;
    private String Authorize;
    private String AuthenticationStatus;
    private String AuthenticationMode;
    private String AirPosConnecTryCount;

    private Setting(Parcel parcel) {
        this.WebServiceURL = parcel.readString();
        this.IPAddress = parcel.readString();
        this.IFESSID = parcel.readString();
        this.IFEKEY = parcel.readString();
        this.IFEIP = parcel.readString();
        this.GroundConnectTryCount = parcel.readString();
        this.GroundSSID = parcel.readString();
        this.FTPUserName = parcel.readString();
        this.FTPUserPassword = parcel.readString();
        this.FTPServerPort = parcel.readString();
        this.FTPServerIP = parcel.readString();
        this.Certificate = parcel.readString();
        this.BlackFTPUserName = parcel.readString();
        this.BlackFTPUserPassword = parcel.readString();
        this.BlackFTPServerPort = parcel.readString();
        this.BlackFTPServerIP = parcel.readString();
        this.Authorize = parcel.readString();
        this.AuthenticationStatus = parcel.readString();
        this.AuthenticationMode = parcel.readString();
        this.AirPosConnecTryCount = parcel.readString();
    }

    public static final Creator<Setting> CREATOR = new Creator<Setting>() {
        @Override
        public Setting createFromParcel(Parcel parcel) {
            return new Setting(parcel);
        }

        @Override
        public Setting[] newArray(int size) {
            return new Setting[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.WebServiceURL);
        parcel.writeString(this.IPAddress);
        parcel.writeString(this.IFESSID);
        parcel.writeString(this.IFEKEY);
        parcel.writeString(this.IFEIP);
        parcel.writeString(this.GroundConnectTryCount);
        parcel.writeString(this.GroundSSID);
        parcel.writeString(this.FTPUserName);
        parcel.writeString(this.FTPUserPassword);
        parcel.writeString(this.FTPServerPort);
        parcel.writeString(this.FTPServerIP);
        parcel.writeString(this.Certificate);
        parcel.writeString(this.BlackFTPUserName);
        parcel.writeString(this.BlackFTPUserPassword);
        parcel.writeString(this.BlackFTPServerPort);
        parcel.writeString(this.BlackFTPServerIP);
        parcel.writeString(this.Authorize);
        parcel.writeString(this.AuthenticationStatus);
        parcel.writeString(this.AuthenticationMode);
        parcel.writeString(this.AirPosConnecTryCount);
    }

    public Setting(){

    }

    public String getWebServiceURL() {
        return WebServiceURL;
    }

    public void setWebServiceURL(String webServiceURL) {
        WebServiceURL = webServiceURL;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    public String getIFESSID() {
        return IFESSID;
    }

    public void setIFESSID(String IFESSID) {
        this.IFESSID = IFESSID;
    }

    public String getIFEKEY() {
        return IFEKEY;
    }

    public void setIFEKEY(String IFEKEY) {
        this.IFEKEY = IFEKEY;
    }

    public String getIFEIP() {
        return IFEIP;
    }

    public void setIFEIP(String IFEIP) {
        this.IFEIP = IFEIP;
    }

    public String getGroundConnectTryCount() {
        return GroundConnectTryCount;
    }

    public void setGroundConnectTryCount(String groundConnectTryCount) {
        GroundConnectTryCount = groundConnectTryCount;
    }

    public String getGroundSSID() {
        return GroundSSID;
    }

    public void setGroundSSID(String groundSSID) {
        GroundSSID = groundSSID;
    }

    public String getFTPUserName() {
        return FTPUserName;
    }

    public void setFTPUserName(String FTPUserName) {
        this.FTPUserName = FTPUserName;
    }

    public String getFTPUserPassword() {
        return FTPUserPassword;
    }

    public void setFTPUserPassword(String FTPUserPassword) {
        this.FTPUserPassword = FTPUserPassword;
    }

    public String getFTPServerPort() {
        return FTPServerPort;
    }

    public void setFTPServerPort(String FTPServerPort) {
        this.FTPServerPort = FTPServerPort;
    }

    public String getFTPServerIP() {
        return FTPServerIP;
    }

    public void setFTPServerIP(String FTPServerIP) {
        this.FTPServerIP = FTPServerIP;
    }

    public String getCertificate() {
        return Certificate;
    }

    public void setCertificate(String certificate) {
        Certificate = certificate;
    }

    public String getBlackFTPUserName() {
        return BlackFTPUserName;
    }

    public void setBlackFTPUserName(String blackFTPUserName) {
        BlackFTPUserName = blackFTPUserName;
    }

    public String getBlackFTPUserPassword() {
        return BlackFTPUserPassword;
    }

    public void setBlackFTPUserPassword(String blackFTPUserPassword) {
        BlackFTPUserPassword = blackFTPUserPassword;
    }

    public String getBlackFTPServerPort() {
        return BlackFTPServerPort;
    }

    public void setBlackFTPServerPort(String blackFTPServerPort) {
        BlackFTPServerPort = blackFTPServerPort;
    }

    public String getBlackFTPServerIP() {
        return BlackFTPServerIP;
    }

    public void setBlackFTPServerIP(String blackFTPServerIP) {
        BlackFTPServerIP = blackFTPServerIP;
    }

    public String getAuthorize() {
        return Authorize;
    }

    public void setAuthorize(String authorize) {
        Authorize = authorize;
    }

    public String getAuthenticationStatus() {
        return AuthenticationStatus;
    }

    public void setAuthenticationStatus(String authenticationStatus) {
        AuthenticationStatus = authenticationStatus;
    }

    public String getAuthenticationMode() {
        return AuthenticationMode;
    }

    public void setAuthenticationMode(String authenticationMode) {
        AuthenticationMode = authenticationMode;
    }

    public String getAirPosConnecTryCount() {
        return AirPosConnecTryCount;
    }

    public void setAirPosConnecTryCount(String airPosConnecTryCount) {
        AirPosConnecTryCount = airPosConnecTryCount;
    }
}
