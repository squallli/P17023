package tw.com.regalscan.evaground;


/**
 * Created by Heidi on 2017/1/25.
 */

public class ItemInfo{
    String drawer;
    String itemCode;
    String serialCode;
    String itemInfo;
    int evacheck;
    int egascheck;
    int originNum;
    int newNum;
    int  damage;
    boolean isModified;

    public String getDrawer() {
        return drawer;
    }

    public void setDrawer(String drawer) {
        this.drawer = drawer;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getSerialCode() {
        return serialCode;
    }

    public void setSerialCode(String serialCode) {
        this.serialCode = serialCode;
    }

    public String getItemInfo() {
        return itemInfo;
    }

    public void setItemInfo(String itemInfo) {
        this.itemInfo = itemInfo;
    }

    public int getEvacheck() {
        return evacheck;
    }

    public void setEvacheck(int evacheck) {
        this.evacheck = evacheck;
    }

    public int getEgascheck() {
        return egascheck;
    }

    public void setEgascheck(int egascheck) {
        this.egascheck = egascheck;
    }

    public int getOriginNum() {
        return originNum;
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

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }

}
