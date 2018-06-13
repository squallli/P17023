package tw.com.regalscan.app.entity;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Created by tp00175 on 2017/9/25.
 */

public class UserInfo implements Parcelable {

    private String IS_VALID;
    private String MSG;
    private String COMPANY;
    private String DEPARTMENT;
    private String EMPLOYEE_ID;
    private List<News> NEWS_LIST;
    private String FULLNAME;
    private String VERSION;
    private String DOWNLOAD_URL;
    private String FORCE_UPDATE;
    private String FTP_IP;
    private String SYSTEM_TIME;

    private UserInfo(Parcel parcel) {
        this.IS_VALID = parcel.readString();
        this.MSG = parcel.readString();
        this.COMPANY = parcel.readString();
        this.DEPARTMENT = parcel.readString();
        this.EMPLOYEE_ID = parcel.readString();
        this.NEWS_LIST = parcel.createTypedArrayList(News.CREATOR);
        this.FULLNAME = parcel.readString();
        this.VERSION = parcel.readString();
        this.DOWNLOAD_URL = parcel.readString();
        this.FORCE_UPDATE = parcel.readString();
        this.FTP_IP = parcel.readString();
        this.SYSTEM_TIME = parcel.readString();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel parcel) {
            return new UserInfo(parcel);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.IS_VALID);
        parcel.writeString(this.MSG);
        parcel.writeString(this.COMPANY);
        parcel.writeString(this.DEPARTMENT);
        parcel.writeString(this.EMPLOYEE_ID);
        parcel.writeTypedList(this.NEWS_LIST);
        parcel.writeString(this.FULLNAME);
        parcel.writeString(this.VERSION);
        parcel.writeString(this.DOWNLOAD_URL);
        parcel.writeString(this.FORCE_UPDATE);
        parcel.writeString(this.FTP_IP);
        parcel.writeString(this.SYSTEM_TIME);
    }

    public UserInfo() {
    }

    public UserInfo(JSONObject jsonObject) {
        this.IS_VALID = jsonObject.getString("IS_VALID");
        this.MSG = jsonObject.getString("MSG");
        this.COMPANY = jsonObject.getString("COMPANY");
        this.DEPARTMENT = jsonObject.getString("DEPARTMENT");
        this.EMPLOYEE_ID = jsonObject.getString("EMPLOYEE_ID");

        JSONArray jsonArray = jsonObject.getJSONArray("NEWS_LIST");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonArrayJSONObject = jsonArray.getJSONObject(i);
            News news = new News(jsonArrayJSONObject);
            this.NEWS_LIST.add(news);
        }

        this.FULLNAME = jsonObject.getString("FULLNAME");
        this.VERSION = jsonObject.getString("VERSION");
        this.DOWNLOAD_URL = jsonObject.getString("DOWNLOAD_URL");
        this.FORCE_UPDATE = jsonObject.getString("FORCE_UPDATE");
        this.FTP_IP = jsonObject.getString("FTP_IP");
        this.SYSTEM_TIME = jsonObject.getString("SYSTEM_TIME");
    }

    public boolean isValid() {
        return IS_VALID != null && IS_VALID.equals("Y");
    }

    public String getIS_VALID() {
        return IS_VALID;
    }

    public String getMSG() {
        return MSG;
    }

    public String getCOMPANY() {
        return COMPANY;
    }

    public String getDEPARTMENT() {
        return DEPARTMENT;
    }

    public String getEMPLOYEE_ID() {
        return EMPLOYEE_ID;
    }

    public List<News> getNEWS_LIST() {
        return NEWS_LIST;
    }

    public String getFULLNAME() {
        return FULLNAME;
    }

    public String getVERSION() {
        return VERSION;
    }

    public String getDOWNLOAD_URL() {
        return DOWNLOAD_URL;
    }

    public String getFORCE_UPDATE() {
        return FORCE_UPDATE;
    }

    public String getFTP_IP() {
        return FTP_IP;
    }

    public String getSYSTEM_TIME() {
        return SYSTEM_TIME;
    }

    public void setIS_VALID(String IS_VALID) {
        this.IS_VALID = IS_VALID;
    }

    public void setMSG(String MSG) {
        this.MSG = MSG;
    }

    public void setCOMPANY(String COMPANY) {
        this.COMPANY = COMPANY;
    }

    public void setDEPARTMENT(String DEPARTMENT) {
        this.DEPARTMENT = DEPARTMENT;
    }

    public void setEMPLOYEE_ID(String EMPLOYEE_ID) {
        this.EMPLOYEE_ID = EMPLOYEE_ID;
    }

    public void setNEWS_LIST(List<News> NEWS_LIST) {
        this.NEWS_LIST = NEWS_LIST;
    }

    public void setFULLNAME(String FULLNAME) {
        this.FULLNAME = FULLNAME;
    }

    public void setVERSION(String VERSION) {
        this.VERSION = VERSION;
    }

    public void setDOWNLOAD_URL(String DOWNLOAD_URL) {
        this.DOWNLOAD_URL = DOWNLOAD_URL;
    }

    public void setFORCE_UPDATE(String FORCE_UPDATE) {
        this.FORCE_UPDATE = FORCE_UPDATE;
    }

    public void setFTP_IP(String FTP_IP) {
        this.FTP_IP = FTP_IP;
    }

    public void setSYSTEM_TIME(String SYSTEM_TIME) {
        this.SYSTEM_TIME = SYSTEM_TIME;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
