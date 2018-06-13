package tw.com.regalscan.db;

import java.util.ArrayList;

/**
 * Created by gabehsu on 2017/5/16.
 */

public class DFSItem {

    //商品料號
    public String ItemCode;

    //雜誌編號
    public String SerialCode;

    //抽屜編號
    public String DrawerNo;

    //商品名稱
    public String ItemName;

    //商品原售價
    public double OriginalPrice;

    //商品折扣後售價(USD)
    public double USDPrice;

    //商品折扣後售價(TWD)
    public double TWDPrice;

    //POS庫存量
    public int POSStock;

    //IFE庫存量
    public int IFEStock;

    //銷售數量
    public int SalesQty;

    //Scan數量
    public int ScanQty;

    //商品備註
    public String Remark;

    //贈品旗標 Y 贈品，N 非贈品
    public String GiftFlag;

    //折扣旗標 Y 可以打折，N 不可以打折
    public String DiscountFlag;

    //Discount Exception內折扣旗標 Y 沒出現過，可以打折，N 有出現過，不可以打折
    public String ExceptionDiscountFlag;

    //商品群組ID
    public String GroupID;

    //會員類別
    public String DiscountType;

    //會員編號
    public String DiscountNo;

    //此商品不打折的折扣類別
    public ArrayList<String> DiscountExcptionType;
}
