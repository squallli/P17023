package tw.com.regalscan.db;

import java.util.ArrayList;

/**
 * Created by gabehsu on 2017/5/16.
 */

public class TransferItem {

    //商品料號
    public String ItemCode;

    //雜誌編號
    public String SerialCode;

    //抽屜編號
    public String DrawerNo;

    //商品名稱
    public String ItemName;

    //商品折扣後售價(USD)
    public double USDPrice;

    //商品折扣後售價(TWD)
    public double TWDPrice;

    //POS庫存量
    public int POSStock;

    //銷售數量
    public int TransferOutQty;

    //Scan數量
    public int ScanQty;

    //贈品旗標 Y 贈品，N 非贈品
    public String GiftFlag;

    //商品群組ID
    public String GroupID;

    //此商品不打折的折扣類別
    public ArrayList<String> DiscountExcptionType;
}
