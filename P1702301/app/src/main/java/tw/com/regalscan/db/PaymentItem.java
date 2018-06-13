package tw.com.regalscan.db;

/**
 * Created by gabehsu on 2017/6/1.
 */

public class PaymentItem {

    //付款幣別
    public String Currency;

    //付款類別 Cash(現金)、Card(信用卡)、SC(折扣卷)、DC(折扣卷)、Change(找零)、Refund(退款)
    public String PayBy;

    //付款金額
    public double Amount;

    //付款金額實際代表USD
    public double USDAmount;

    //付款金額代表USD for Coupon
    public double CouponUSDAmount;

    //折扣卷號
    public String CouponNo;

    //信用卡別
    public String CardType;

    //信用卡號
    public String CardNo;

    //持卡人名稱
    public String CardName;

    //信用卡效期
    public String CardDate;

    //信用卡已刷卡次數
    public int SwipeCount;

    //信用卡剩餘可刷卡上限(USD)
    public double LastLimitation;
}
