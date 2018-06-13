package tw.com.regalscan.customClass;

/**
 * Created by Heidi on 2017/3/3.
 */

public class ItemPayInfo {
    String CurDvr="";
    String PayType="";
    double Amount=0.0;
    double USDAmount=0.0;
    String CouponNo= null;

    public ItemPayInfo(String a, String b, double c, double d, String e){
        CurDvr= a;
        PayType= b;
        Amount= c;
        USDAmount= d;
        CouponNo= e;
    }


    public String getCurDvr() {
        return CurDvr;
    }

    public void setCurDvr(String curDvr) {
        CurDvr = curDvr;
    }

    public String getPayType() {
        return PayType;
    }

    public void setPayType(String payType) {
        PayType = payType;
    }

    public double getAmount() {
        return Amount;
    }

    public void setAmount(double amount) {
        Amount = amount;
    }

    public double getUSDAmount() {
        return USDAmount;
    }

    public void setUSDAmount(double USDAmount) {
        this.USDAmount = USDAmount;
    }

    public String getCouponNo() {
        return CouponNo;
    }

    public void setCouponNo(String couponNo) {
        CouponNo = couponNo;
    }
}
