package tw.com.regalscan.db;

/**
 * Created by gabehsu on 2017/6/9.
 */

public class DiscountRow {

    //折扣代碼
    public String Type;

    //折扣%數
    public double DiscountRate;

    //折扣金額
    public int DiscountAmount;

    //贈品編號
    public String DiscountGift;

    //折扣說明
    public String Description;

    //放大刷卡額度
    public int UpperLimit;

    //折扣條件式
    public String FuncID;

    //True = 可累進(針對金額、贈品折扣)
    //False = 不可累進(針對金額、贈品折扣)
    public boolean Progression;
}
