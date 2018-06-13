package tw.com.regalscan.db;

/**
 * Created by gabehsu on 2017/6/23.
 */

public class DiscountItem {

    //會員編號
    public String DiscountNo;

    //折扣代碼
    public String Type;

    //折扣%數
    public double DiscountRate;

    //折扣金額
    public int DiscountAmount;

    //折扣說明
    public String Description;

    //放大刷卡額度
    public int UpperLimit;

    //折扣條件式
    public String FuncID;

    //滿足次數
    public int DiscountCount;
}
