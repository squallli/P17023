package tw.com.regalscan.HttpRequest;

import org.json.JSONObject;

/**
 * Created by tp00175 on 2017/5/18.
 */

public interface CallBack {

  void onSuccess(JSONObject result);

  void onError(String error);
}
