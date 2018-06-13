package tw.com.regalscan.evaground.Models;

import java.util.List;

public class PreOrderInfo {
    String preOrderNo="";
    String status;
    List<PreorderItems> mPreorderItems;

    public PreOrderInfo() {

    }

    public PreOrderInfo(String preOrderNo, String status, List<PreorderItems> items) {
        this.preOrderNo = preOrderNo;
        this.status = status;
        this.mPreorderItems = items;
    }

    public String getPreOrderNo() {
        return preOrderNo;
    }

    public void setPreOrderNo(String preOrderNo) {
        this.preOrderNo = preOrderNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PreorderItems> getPreorderItems() {
        return mPreorderItems;
    }

    public void setPreorderItems(List<PreorderItems> preorderItems) {
        mPreorderItems = preorderItems;
    }
}
