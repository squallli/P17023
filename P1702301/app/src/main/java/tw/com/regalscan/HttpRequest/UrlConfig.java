package tw.com.regalscan.HttpRequest;

import android.content.Context;

import com.jess.arms.utils.ArmsUtils;
import tw.com.regalscan.app.entity.Setting;

/**
 * Created by tp00175 on 2017/7/18.
 */

public class UrlConfig {

  private static final UrlConfig ourInstance = new UrlConfig();

  public static UrlConfig getInstance() {
    return ourInstance;
  }

  private UrlConfig() {
  }

  final public String url_LOGIN = "SMAS_POS_WS/LOGIN_WS.asmx/FUN_LOGIN";
  final public String url_GETDOCNO = "SMAS_POS_WS/POS_GET_DOCNO_WS.asmx/getDOC_NO";
  final public String url_DOWNLOADABLE = "SMAS_POS_WS/POS_DOWNLOADABLE_WS.asmx/IS_DOWNLOADABLE";
  final public String url_DOWNLOAD = "SMAS_POS_WS/POS_DOWNLOAD_WS.asmx/DOWNLOAD_TO_POS";
  final public String url_UPDATESTATUS = "SMAS_POS_WS/POS_UPDATE_STATUS_WS.asmx/UPDATE_POS_STATUS";
  final public String url_TRANS = "SMAS_POS_WS/POS_TRANS_WS.asmx/IS_TRANS";
  final public String url_UPLOAD= "SMAS_POS_WS/POS_UPLOAD_WS.asmx/UPLOAD_TO_SMAS";

  public String getUrl(Context context) {
    return ((Setting)ArmsUtils.obtainAppComponentFromContext(context).extras().get("Setting")).getWebServiceURL();
  }
}
