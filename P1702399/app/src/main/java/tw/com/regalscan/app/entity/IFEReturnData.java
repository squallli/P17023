package tw.com.regalscan.app.entity;

/**
 * Created by tp00175 on 2017/11/27.
 */

public class IFEReturnData {
    private boolean isSuccess; //IFE 回傳成功或失敗
    private String errMsg;     //錯誤訊息
    private Object data;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
