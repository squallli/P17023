package tw.com.regalscan.app.entity;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tp00175 on 2018/1/15.
 */

public class PreOrderInfo implements Parcelable {

    private String SecSeq;
    private String ReceiptNo;
    private String PreorderNO; //Preorder No, VIP No
    private double MileDisc; //里程折抵
    private String ECouponCurrency;
    private double ECoupon; //E Coupon折扣卷
    private String CardType;
    private String CardNo;
    private String TravelDocument;
    private String CurDvr;
    private double PayAmt;
    private double Amount;
    private double Discount;
    private String PNR; //訂位代號
    private String PassengerName;
    private String PreorderType; //PR, VS, VP
    private String SaleFlag;
    private String EGASSaleFlag;
    private String EVASaleFlag;
    private List<ItemInfo> items;

    private PreOrderInfo(Parcel parcel) {
        this.SecSeq = parcel.readString();
        this.ReceiptNo = parcel.readString();
        this.PreorderNO = parcel.readString();
        this.MileDisc = parcel.readDouble();
        this.ECouponCurrency = parcel.readString();
        this.ECoupon = parcel.readDouble();
        this.CardType = parcel.readString();
        this.CardNo = parcel.readString();
        this.TravelDocument = parcel.readString();
        this.CurDvr = parcel.readString();
        this.PayAmt = parcel.readDouble();
        this.Amount = parcel.readDouble();
        this.Discount = parcel.readDouble();
        this.PNR = parcel.readString();
        this.PassengerName = parcel.readString();
        this.SaleFlag = parcel.readString();
        this.EGASSaleFlag = parcel.readString();
        this.EVASaleFlag = parcel.readString();

        if (items == null) {
            items = new ArrayList<>();
        }
        parcel.readTypedList(this.items, ItemInfo.CREATOR);
    }

    public static final Creator<PreOrderInfo> CREATOR = new Creator<PreOrderInfo>() {
        @Override
        public PreOrderInfo createFromParcel(Parcel parcel) {
            return new PreOrderInfo(parcel);
        }

        @Override
        public PreOrderInfo[] newArray(int size) {
            return new PreOrderInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.SecSeq);
        parcel.writeString(this.ReceiptNo);
        parcel.writeString(this.PreorderNO);
        parcel.writeDouble(this.MileDisc);
        parcel.writeString(this.ECouponCurrency);
        parcel.writeDouble(this.ECoupon);
        parcel.writeString(this.CardType);
        parcel.writeString(this.CardNo);
        parcel.writeString(this.TravelDocument);
        parcel.writeString(this.CurDvr);
        parcel.writeDouble(this.PayAmt);
        parcel.writeDouble(this.Amount);
        parcel.writeDouble(this.Discount);
        parcel.writeString(this.PNR);
        parcel.writeString(this.PassengerName);
        parcel.writeString(this.PreorderType);
        parcel.writeString(this.SaleFlag);
        parcel.writeString(this.EGASSaleFlag);
        parcel.writeString(this.EVASaleFlag);
        parcel.writeTypedList(this.items);
    }

    public PreOrderInfo() {

    }

    public String getSecSeq() {
        return SecSeq;
    }

    public void setSecSeq(String secSeq) {
        SecSeq = secSeq;
    }

    public String getReceiptNo() {
        return ReceiptNo;
    }

    public void setReceiptNo(String receiptNo) {
        ReceiptNo = receiptNo;
    }

    public String getPreorderNO() {
        return PreorderNO;
    }

    public void setPreorderNO(String preorderNO) {
        PreorderNO = preorderNO;
    }

    public double getMileDisc() {
        return MileDisc;
    }

    public void setMileDisc(double mileDisc) {
        MileDisc = mileDisc;
    }

    public String getECouponCurrency() {
        return ECouponCurrency;
    }

    public void setECouponCurrency(String ECouponCurrency) {
        this.ECouponCurrency = ECouponCurrency;
    }

    public double getECoupon() {
        return ECoupon;
    }

    public void setECoupon(double ECoupon) {
        this.ECoupon = ECoupon;
    }

    public String getCardType() {
        return CardType;
    }

    public void setCardType(String cardType) {
        CardType = cardType;
    }

    public String getCardNo() {
        return CardNo;
    }

    public void setCardNo(String cardNo) {
        CardNo = cardNo;
    }

    public String getTravelDocument() {
        return TravelDocument;
    }

    public void setTravelDocument(String travelDocument) {
        TravelDocument = travelDocument;
    }

    public String getCurDvr() {
        return CurDvr;
    }

    public void setCurDvr(String curDvr) {
        CurDvr = curDvr;
    }

    public double getPayAmt() {
        return PayAmt;
    }

    public void setPayAmt(double payAmt) {
        PayAmt = payAmt;
    }

    public double getAmount() {
        return Amount;
    }

    public void setAmount(double amount) {
        Amount = amount;
    }

    public double getDiscount() {
        return Discount;
    }

    public void setDiscount(double discount) {
        Discount = discount;
    }

    public String getPNR() {
        return PNR;
    }

    public void setPNR(String PNR) {
        this.PNR = PNR;
    }

    public String getPassengerName() {
        return PassengerName;
    }

    public void setPassengerName(String passengerName) {
        PassengerName = passengerName;
    }

    public String getPreorderType() {
        return PreorderType;
    }

    public void setPreorderType(String preorderType) {
        PreorderType = preorderType;
    }

    public String getSaleFlag() {
        return SaleFlag;
    }

    public void setSaleFlag(String saleFlag) {
        SaleFlag = saleFlag;
    }

    public String getEGASSaleFlag() {
        return EGASSaleFlag;
    }

    public void setEGASSaleFlag(String EGASSaleFlag) {
        this.EGASSaleFlag = EGASSaleFlag;
    }

    public String getEVASaleFlag() {
        return EVASaleFlag;
    }

    public void setEVASaleFlag(String EVASaleFlag) {
        this.EVASaleFlag = EVASaleFlag;
    }

    public List<ItemInfo> getItemInfos() {
        return items;
    }

    public void setItemInfos(List<ItemInfo> items) {
        this.items = items;
    }
}
