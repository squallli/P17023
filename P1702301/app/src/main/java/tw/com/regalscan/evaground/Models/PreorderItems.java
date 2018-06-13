package tw.com.regalscan.evaground.Models;

public class PreorderItems {
    String preorderInfo, ItemCode;
    String itemInfo;
    int originNum;
    int SaleQty;
    int newNum;
    boolean isModified;

    public int getSaleQty() {
        return SaleQty;
    }

    public void setSaleQty(int saleQty) {
        SaleQty = saleQty;
    }

    public String getPreorderInfo() {
        return preorderInfo;
    }

    public void setPreorderInfo(String preorderInfo) {
        this.preorderInfo = preorderInfo;
    }

    public int getOriginNum() {
        return originNum;
    }

    public String getItemInfo() {
        return itemInfo;
    }

    public void setItemInfo(String itemInfo) {
        this.itemInfo = itemInfo;
    }


    public void setOriginNum(int originNum) {
        this.originNum = originNum;
    }

    public int getNewNum() {
        return newNum;
    }

    public void setNewNum(int newNum) {
        this.newNum = newNum;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }

    public String getItemCode() {
        return ItemCode;
    }

    public void setItemCode(String itemCode) {
        ItemCode = itemCode;
    }
}
