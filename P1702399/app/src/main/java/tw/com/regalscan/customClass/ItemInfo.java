package tw.com.regalscan.customClass;

import java.io.Serializable;

/**
 * Created by Heidi on 2017/1/25.
 */

public class ItemInfo implements Serializable {
    private String itemCode;    //物品編號
    private String serialNo;    //雜誌編號
    private String moneyType;    //幣別
    private double price;          //價格
    private String itemName;    //物品名稱
    private Object qty;            //販售數量
    private int stock;          //在庫數量
    private int giftScan;       //已scan的gift數量
    private int ifeStock = -1;    //IFE數量
    private boolean canDiscount = true;   //是否可打折
    private boolean isModified = false;   //是否經過修改, 或 qty> stock的flag
    private boolean isCheck = false;      //是否有check



    /*----------------------functions------------------------*/

    //價格是否沒有小數, 沒有的話回傳去掉小數的價格
    //有的話回傳-1
    public int getIntegerPrice() {
        if (this.price % 2 == 0 || this.price % 2 == 1) {
            return (int)this.price;
        }
        return -1;
    }

    // Qty的值在Preorder Sale和Vip Paid被放置Price
    // 判斷價格是否為小數
    public int getIntegerQty() {
        double d = Double.parseDouble(this.qty.toString());
        if (d % 2 == 0 || d % 2 == 1) {
            return (int)d;
        }
        return -1;
    }

    /*----------------------constructor------------------------*/

    public ItemInfo(String itemCode, String serialNo, int stock, int qty, boolean isModified) {
        this.itemCode = itemCode;
        this.serialNo = serialNo;
        this.qty = qty;
        this.stock = stock;
        this.isModified = isModified;
    }

    public ItemInfo(String itemCode, String serialNo, String monyType, double price,
                    String itemName, int stock, Object qty) {
        this.itemCode = itemCode;
        this.serialNo = serialNo;
        this.moneyType = monyType;
        this.price = price;
        this.itemName = itemName;
        this.stock = stock;
        this.qty = qty;
    }

    public ItemInfo(String itemCode, String serialNo, String monyType, double price,
                    String itemName, int stock, int qty, boolean canDiscount) {
        this.itemCode = itemCode;
        this.serialNo = serialNo;
        this.moneyType = monyType;
        this.price = price;
        this.itemName = itemName;
        this.stock = stock;
        this.qty = qty;
        this.canDiscount = canDiscount;
    }

    public ItemInfo(String itemCode, String serialNo, String monyType, double price,
                    String itemName, int stock, int qty, boolean canDiscount, boolean isModified) {
        this.itemCode = itemCode;
        this.serialNo = serialNo;
        this.moneyType = monyType;
        this.price = price;
        this.itemName = itemName;
        this.stock = stock;
        this.qty = qty;
        this.canDiscount = canDiscount;
        this.isModified = isModified;
    }


    public ItemInfo(String itemCode, String serialNo, String monyType, double price,
                    String itemName, int stock, int qty, int ifeStock, boolean canDiscount, boolean isModified) {
        this.itemCode = itemCode;
        this.serialNo = serialNo;
        this.moneyType = monyType;
        this.price = price;
        this.itemName = itemName;
        this.stock = stock;
        this.qty = qty;
        this.canDiscount = canDiscount;
        this.isModified = isModified;
        this.ifeStock = ifeStock;
    }


    public ItemInfo(String itemCode, String serialNo, String moneyType, double price,
                    String itemName, int stock, int qty, int ifeStock, int giftScan, boolean canDiscount, boolean isModified) {
        this.itemCode = itemCode;
        this.serialNo = serialNo;
        this.moneyType = moneyType;
        this.price = price;
        this.itemName = itemName;
        this.stock = stock;
        this.qty = qty;
        this.canDiscount = canDiscount;
        this.isModified = isModified;
        this.ifeStock = ifeStock;
        this.giftScan = giftScan;
    }

    /*----------------------getter, setter------------------------*/

    public String getItemCode() {
        return itemCode;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public String getMonyType() {
        return moneyType;
    }

    public double getPrice() {
        return price;
    }

    public String getItemName() {
        return itemName;
    }

    public Object getQty() {
        return qty;
    }

    public void setQty(Object qty) {
        this.qty = qty;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getIfeStock() {
        return ifeStock;
    }

    public void setIfeStock(int ifeStock) {
        this.ifeStock = ifeStock;
    }

    public int getGiftScan() {
        return giftScan;
    }

    public void setGiftScan(int giftScan) {
        this.giftScan = giftScan;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }

    public boolean isCanDiscount() {
        return canDiscount;
    }

    public void setCanDiscount(boolean canDiscount) {
        this.canDiscount = canDiscount;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

}
