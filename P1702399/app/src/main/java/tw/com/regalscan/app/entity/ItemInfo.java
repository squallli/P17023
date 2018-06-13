package tw.com.regalscan.app.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tp00175 on 2017/12/15.
 */

public class ItemInfo implements Parcelable{

    private String DrawNo;
    private String SerialCode;
    private String ItemCode;
    private String ItemName;
    private int StandQty;
    private int DamageQty;
    private int EndQty;
    private int EVACheckQty;
    private int EVADamageQty;
    private int EGASCheckQty;
    private int EGASDamageQty;

    private double OriginalPrice;
    private double SalesPrice;
    private int SalesPriceTW;
    private int SalesQty;
    private boolean isSale;
    private boolean isChanged;

    public ItemInfo () {

    }

    public String getDrawNo() {
        return DrawNo;
    }

    public void setDrawNo(String drawNo) {
        DrawNo = drawNo;
    }

    public String getSerialCode() {
        return SerialCode;
    }

    public void setSerialCode(String serialCode) {
        SerialCode = serialCode;
    }

    public String getItemCode() {
        return ItemCode;
    }

    public void setItemCode(String itemCode) {
        ItemCode = itemCode;
    }

    public String getItemName() {
        return ItemName;
    }

    public void setItemName(String itemName) {
        ItemName = itemName;
    }

    public int getStandQty() {
        return StandQty;
    }

    public void setStandQty(int standQty) {
        StandQty = standQty;
    }

    public int getDamageQty() {
        return DamageQty;
    }

    public void setDamageQty(int damageQty) {
        DamageQty = damageQty;
    }

    public int getEndQty() {
        return EndQty;
    }

    public void setEndQty(int endQty) {
        EndQty = endQty;
    }

    public int getEVACheckQty() {
        return EVACheckQty;
    }

    public void setEVACheckQty(int EVACheckQty) {
        this.EVACheckQty = EVACheckQty;
    }

    public int getEVADamageQty() {
        return EVADamageQty;
    }

    public void setEVADamageQty(int EVADamageQty) {
        this.EVADamageQty = EVADamageQty;
    }

    public int getEGASCheckQty() {
        return EGASCheckQty;
    }

    public void setEGASCheckQty(int EGASCheckQty) {
        this.EGASCheckQty = EGASCheckQty;
    }

    public int getEGASDamageQty() {
        return EGASDamageQty;
    }

    public void setEGASDamageQty(int EGASDamageQty) {
        this.EGASDamageQty = EGASDamageQty;
    }

    public double getOriginalPrice() {
        return OriginalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        OriginalPrice = originalPrice;
    }

    public double getSalesPrice() {
        return SalesPrice;
    }

    public void setSalesPrice(double salesPrice) {
        SalesPrice = salesPrice;
    }

    public int getSalesPriceTW() {
        return SalesPriceTW;
    }

    public void setSalesPriceTW(int salesPriceTW) {
        SalesPriceTW = salesPriceTW;
    }

    public int getSalesQty() {
        return SalesQty;
    }

    public void setSalesQty(int salesQty) {
        SalesQty = salesQty;
    }

    public boolean isSale() {
        return isSale;
    }

    public void setSale(boolean sale) {
        isSale = sale;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.DrawNo);
        dest.writeString(this.SerialCode);
        dest.writeString(this.ItemCode);
        dest.writeString(this.ItemName);
        dest.writeInt(this.StandQty);
        dest.writeInt(this.DamageQty);
        dest.writeInt(this.EndQty);
        dest.writeInt(this.EVACheckQty);
        dest.writeInt(this.EVADamageQty);
        dest.writeInt(this.EGASCheckQty);
        dest.writeInt(this.EGASDamageQty);
        dest.writeDouble(this.OriginalPrice);
        dest.writeDouble(this.SalesPrice);
        dest.writeInt(this.SalesPriceTW);
        dest.writeInt(this.SalesQty);
        dest.writeByte(this.isSale ? (byte)1 : (byte)0);
        dest.writeByte(this.isChanged ? (byte)1 : (byte)0);
    }

    protected ItemInfo(Parcel in) {
        this.DrawNo = in.readString();
        this.SerialCode = in.readString();
        this.ItemCode = in.readString();
        this.ItemName = in.readString();
        this.StandQty = in.readInt();
        this.DamageQty = in.readInt();
        this.EndQty = in.readInt();
        this.EVACheckQty = in.readInt();
        this.EVADamageQty = in.readInt();
        this.EGASCheckQty = in.readInt();
        this.EGASDamageQty = in.readInt();
        this.OriginalPrice = in.readDouble();
        this.SalesPrice = in.readDouble();
        this.SalesPriceTW = in.readInt();
        this.SalesQty = in.readInt();
        this.isSale = in.readByte() != 0;
        this.isChanged = in.readByte() != 0;
    }

    public static final Creator<ItemInfo> CREATOR = new Creator<ItemInfo>() {
        @Override
        public ItemInfo createFromParcel(Parcel source) {
            return new ItemInfo(source);
        }

        @Override
        public ItemInfo[] newArray(int size) {
            return new ItemInfo[size];
        }
    };
}
