package tw.com.regalscan.evaground.Models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

/**
 * Created by tp00175 on 2017/5/23.
 */

public class RtnObject {

  private static final RtnObject TAG = new RtnObject();

  public static RtnObject getInstance() {
    return TAG;
  }

  private RtnObject() {
    super();
  }

  @SerializedName("IS_VALID")
  private String isValid;
  @SerializedName("MSG")
  private String msg;
  @SerializedName("COMPANY")
  private String company;
  @SerializedName("DEPARTMENT")
  private String department;
  @SerializedName("EMPLOYEE_ID")
  private String employeeID;
  @SerializedName("NEWS_LIST")
  private ArrayList<NewsInfo> mNewsInfo;
  @SerializedName("FULLNAME")
  private String fullName;
  @SerializedName("VERSION")
  private String version;
  @SerializedName("DOWNLOAD_URL")
  private String downloadURL;
  @SerializedName("FORCE_UPDATE")
  private String forceUpdate;
  @SerializedName("FTP_IP")
  private String ftpIP;
  @SerializedName("SYSTEM_TIME")
  private String systemTime;
  @SerializedName("IS_TRANS")
  private String isTrans;

  public String getIsValid() {
    return isValid;
  }

  public void setIsValid(String isValid) {
    this.isValid = isValid;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(String company) {
    this.company = company;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getEmployeeID() {
    return employeeID;
  }

  public void setEmployeeID(String employeeID) {
    this.employeeID = employeeID;
  }

  public ArrayList<NewsInfo> getNewsInfo() {
    return mNewsInfo;
  }

  public void setNewsInfo(ArrayList<NewsInfo> newsInfo) {
    mNewsInfo = newsInfo;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDownloadURL() {
    return downloadURL;
  }

  public void setDownloadURL(String downloadURL) {
    this.downloadURL = downloadURL;
  }

  public String getForceUpdate() {
    return forceUpdate;
  }

  public void setForceUpdate(String forceUpdate) {
    this.forceUpdate = forceUpdate;
  }

  public String getFtpIP() {
    return ftpIP;
  }

  public void setFtpIP(String ftpIP) {
    this.ftpIP = ftpIP;
  }

  public String getSystemTime() {
    return systemTime;
  }

  public void setSystemTime(String systemTime) {
    this.systemTime = systemTime;
  }

  public String getIsTrans() {
    return isTrans;
  }

  public void setIsTrans(String isTrans) {
    this.isTrans = isTrans;
  }
}
