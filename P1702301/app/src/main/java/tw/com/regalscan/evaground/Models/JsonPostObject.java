package tw.com.regalscan.evaground.Models;

/**
 * Created by tp00175 on 2017/7/25.
 */

public class JsonPostObject {

  private static final JsonPostObject ourInstance = new JsonPostObject();

  public static JsonPostObject getInstance() {
    return ourInstance;
  }

  private JsonPostObject() {
  }

  String UDID;
  String DEPT_DATE;
  String DEPT_FLT_NO;
  String EMPLOYEE_ID;
  String DOC_NO;

  public String getUDID() {
    return UDID;
  }

  public void setUDID(String UDID) {
    this.UDID = UDID;
  }

  public String getDEPT_DATE() {
    return DEPT_DATE;
  }

  public void setDEPT_DATE(String DEPT_DATE) {
    this.DEPT_DATE = DEPT_DATE;
  }

  public String getDEPT_FLT_NO() {
    return DEPT_FLT_NO;
  }

  public void setDEPT_FLT_NO(String DEPT_FLT_NO) {
    this.DEPT_FLT_NO = DEPT_FLT_NO;
  }

  public String getEMPLOYEE_ID() {
    return EMPLOYEE_ID;
  }

  public void setEMPLOYEE_ID(String EMPLOYEE_ID) {
    this.EMPLOYEE_ID = EMPLOYEE_ID;
  }

  public String getDOC_NO() {
    return DOC_NO;
  }

  public void setDOC_NO(String DOC_NO) {
    this.DOC_NO = DOC_NO;
  }
}
