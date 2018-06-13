package tw.com.regalscan.customClass;

import java.io.Serializable;

/**
 * Created by Heidi on 2017/3/13.
 */

public class ItemUpgradeInfo implements Serializable {
    private String identity;
    private String from;
    private String to;
    private int qty;
    private double total;



    //價格是否沒有小數
    public int getIntegerTotal(){
        if(this.total%2==0 || this.total%2==1){
            return (int)this.total;
        }
        return -1;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
