package tw.com.regalscan.db;

import android.content.Context;

import com.regalscan.sqlitelibrary.TSQL;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import tw.com.regalscan.component.AESEncrypDecryp;

/**
 * Created by gabehsu on 2017/4/19.
 */

public class Transaction {

    public Context context;
    public String SecSeq;

    private PublicFunctions _PublicFunctions;

    public PayMode NowPayMode = PayMode.PAY;  //付款模式 Pay or Change or Balance
    private boolean IsTotalUseTWDPay = false;  //是否全用台幣付款

    //單據資訊
    public String ReceiptNo;         //交易單據號碼
    public String SeatNo;            //乘客座位
    public String IFEOrderNo;        //IFE Order No
    public Boolean AuthuticationFlag;//線上授權Flag Y or N
    private String OrderType;         //Y = IFE取得單據 N = POS產生單據

    //交易金額資訊
    private double OriUSDTotalAmount; //美金折扣前總價
    private double OriNTDTotalAmount; //台幣折扣前總價
    public double USDTotalAmount;    //美金應付款總金額
    private double NTDTotalAmount;    //台幣應付款總金額
    private double USDTotalPayed;     //美金已付款總金額
    private double NTDTotalPayed;     //台幣已付款總金額
    public double USDTotalUnpay;     //美金未付款總金額
    private double NTDTotalUnpay;     //台幣未付款總金額

    //折扣資訊
    public String DiscountType;          //折扣會員類別
    public String DiscountNo;            //折扣會員編號
    public double DiscountRate;      //折扣會員%數
    public double DiscountAmount;    //折扣會員金額
    public String UpperLimitType;        //刷卡上限會員類別
    private String UpperLimitDiscountNo;  //刷卡上限會員編號
    private int UpperLimit;               //刷卡上限金額

    private boolean canUseIFEDiscount = false;

    public int getUpperLimit() {
        return UpperLimit;
    }

    public String getUpperLimitType() {
        return UpperLimitType;
    }

    public void setCanUseIFEDiscount(boolean i) {
        this.canUseIFEDiscount = i;
    }

    //系統交易暫存
    private double LastPayAmount;   //最後付款金額
    public String LastPayCurrency; //最後付款幣別

    //信用卡資訊
    public String CardDate;        //信用卡效期
    public String CardName;        //信用卡持卡人姓名
    public String CardNo;          //信用卡號
    public String CardType;        //信用卡類別

    //商品清單
    private ArrayList<DFSItem> DFSItemList;

    //折扣清單
    private ArrayList<DiscountItem> DiscountList;

    //付款歷程
    public ArrayList<PaymentItem> PaymentList;

    public int IFEStock = 0;

    //付款列舉
    public enum PaymentType {
        Cash,
        Card,
        SC,
        DC,
        Change,
        Refund
    }

    //信用卡類別列舉
    public enum CreditCardType {
        VISA,
        MASTER,
        JCB,
        AMX,
        CUP
    }

    //付款模式列舉
    public enum PayMode {
        PAY,
        CHANGE,
        BALANCE
    }

    /**
     * 初始化Transaction
     */
    public Transaction(Context context, String SecSeq) {

        this.context = context;
        this.SecSeq = SecSeq;
        this.ReceiptNo = String.valueOf(GetNewReceiptNo());

        //初始變數
        this.SeatNo = "";
        this.IFEOrderNo = "";
        this.AuthuticationFlag = false;
        this.OrderType = "";
        this.DiscountType = "";          //折扣會員類別
        this.DiscountNo = "";            //折扣會員編號
        this.DiscountRate = 1;           //折扣會員%數
        this.DiscountAmount = 0;         //折扣會員金額
        this.UpperLimitType = "";        //刷卡上限會員類別
        this.UpperLimitDiscountNo = "";  //刷卡上限會員編號
        this.UpperLimit = 0;             //刷卡上限金額
        this.LastPayAmount = 0;          //最後付款金額
        this.LastPayCurrency = "";       //最後付款幣別
        this.CardDate = "";              //信用卡效期
        this.CardName = "";              //信用卡持卡人姓名
        this.CardNo = "";                //信用卡號
        this.CardType = "";              //信用卡類別

        //實作PublicFunctions
        _PublicFunctions = new PublicFunctions(context, SecSeq);

        //實作DFSItemList
        DFSItemList = new ArrayList<>();

        //實作DiscountList
        DiscountList = new ArrayList<>();

        //實作PaymentList
        PaymentList = new ArrayList<>();
    }

    ////////////////////////////////////////////////////////////////////////
    //                            Basket
    ////////////////////////////////////////////////////////////////////////

    /**
     * 設定座位號碼
     *
     * @param SeatNo 座位號碼
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject SetSeatNo(String SeatNo) {

        String ClassName = "Transaction";
        String FunctionName = "SetSeatNo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SeatNo:" + SeatNo);

        try {

            //若已經有商品列表，則不允許商品增減
            if (DFSItemList.size() > 0) {
                ReturnCode = "8";
                ReturnMessage = "Please delete sale items first.";
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //僅能三碼或是兩碼，六碼和七碼為車號
            if (SeatNo.length() != 2 && SeatNo.length() != 3 && SeatNo.length() != 6 && SeatNo.length() != 7) {
                ReturnCode = "8";
                ReturnMessage = "Seat no format error!";
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            //前一碼須為數字，後一碼為英文
            else if (SeatNo.length() == 2 &&
                    (!Character.isDigit(SeatNo.charAt(0)) ||
                            !Character.isLetter(SeatNo.charAt(1)))) {
                ReturnCode = "8";
                ReturnMessage = "Seat no format error!";
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            //前兩碼須為數字，後一碼為英文
            else if (SeatNo.length() == 3 &&
                    (!Character.isDigit(SeatNo.charAt(0)) ||
                            !Character.isDigit(SeatNo.charAt(1)) ||
                            !Character.isLetter(SeatNo.charAt(2)))) {
                ReturnCode = "8";
                ReturnMessage = "Seat no format error!";
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            this.SeatNo = SeatNo;

            ReturnCode = "0";
            ReturnMessage = "";
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 設定Gift Scan數量
     *
     * @param ItemCode 贈品商品編號
     * @param ScanQty  Scan數量
     * @return (JSONObject) 標準格式回傳
     */
    public void SetGiftScanQty(String ItemCode, int ScanQty) {

        String ClassName = "Transaction";
        String FunctionName = "SetGiftScanQty";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "ItemCode:" + ItemCode + " - ScanQty:" + ScanQty);

        try {

            //逐筆找到該贈品
            for (int i = 0; i < DFSItemList.size(); i++) {

                if (DFSItemList.get(i).ItemCode.equals(ItemCode)) {

                    //判斷是不是贈品
                    if (DFSItemList.get(i).GiftFlag.equals("N")) {
                        ReturnCode = "8";
                        ReturnMessage = "This's not gift.";
                        _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                        return;
                    }

                    //判斷是不是大於銷售量
                    if (ScanQty > DFSItemList.get(i).SalesQty) {
                        ReturnCode = "8";
                        ReturnMessage = "Scan qty error.";
                        _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                        return;
                    }

                    //設定Scan的量
                    DFSItemList.get(i).ScanQty = ScanQty;

                    ReturnCode = "0";
                    ReturnMessage = "";
                    _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                    return;
                }
            }

            ReturnCode = "8";
            ReturnMessage = "Item not found.";
            _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 新增、調整購物車商品
     *
     * @param ItemCode      商品料號
     * @param AdjustSaleQty 要調整的商品數量 　加商品傳+   減商品傳-
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject ModifyItemList(String ItemCode, int AdjustSaleQty) {

        String ClassName = "Transaction";
        String FunctionName = "ModifyItemList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONArray jaItem;

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "ItemCode:" + ItemCode + "-AdjustSaleQty:" + AdjustSaleQty);

        try {

            int SalesQty = 0;      //商品銷售量
            int POSStock = 0;      //商品POS總庫存量
//            int IFEStock = 0;      //商品IFE總庫存量
            int ItemIndex = 0;      //商品列表Index

            if (this.SeatNo.length() == 0) {
                ReturnCode = "8";
                ReturnMessage = "Please set seat no first.";
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //商品數量錯誤
            if (AdjustSaleQty == 0) {
                ReturnCode = "9";
                ReturnMessage = "Sales qty error.";
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //取得商品資訊
            String SQL = "SELECT AllParts.ItemCode,SerialCode,DrawNo,ItemName, " +
                    "ItemPriceUS AS USDPrice, ItemPriceTW AS TWDPrice, " +
                    " (EndQty - DamageQty) AS POSStock," +
                    "Remark, 'N' AS GiftFlag, GroupID, " +
                    "(CASE (SELECT COUNT(*) FROM DiscountException WHERE ItemCode = '" + ItemCode + "') WHEN 0 THEN 'Y' ELSE 'N' END) AS ExceptionDiscountFlag " +
                    "FROM AllParts LEFT JOIN Inventory " +
                    "ON AllParts.ItemCode = Inventory.ItemCode " +
                    "LEFT JOIN ItemGroup " +
                    "ON AllParts.ItemCode = ItemGroup.ItemCode " +
                    "WHERE AllParts.ItemCode = '" + ItemCode +
                    "' AND Inventory.SecSeq = '" + this.SecSeq + "'";
            jaItem = _TSQL.SelectSQLJsonArray(SQL);

            //若商品不存在則跳錯
            if (jaItem == null || jaItem.length() == 0 || jaItem.getJSONObject(0).getString("ItemCode").length() == 0) {
                ReturnCode = "8";
                ReturnMessage = "Pno code error: " + ItemCode;

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //若商品為贈品則跳錯
            if (jaItem.getJSONObject(0).getInt("USDPrice") == 0 || jaItem.getJSONObject(0).getInt("TWDPrice") == 0) {
                ReturnCode = "8";
                ReturnMessage = "The GWP can not be sold for individuals!";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //取得既有購物車數量
            for (int i = 0; i < DFSItemList.size(); i++) {

                if (DFSItemList.get(i).ItemCode.equals(ItemCode)) {
                    SalesQty = DFSItemList.get(i).SalesQty;
                    ItemIndex = i;
                }
            }

            //若連線則判斷是否超出IFE總量
//            if (FlightData.IFEConnectionStatus && (SalesQty + AdjustSaleQty) > IFEStock) {
//                ReturnCode = "8";
//                ReturnMessage = "IFE Qty not enough";
//
//                //寫入Log
//                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
//                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
//            }

            //若離線則判斷是否有超出POS庫存量
            if (!FlightData.IFEConnectionStatus && (SalesQty + AdjustSaleQty) > jaItem.getJSONObject(0).getInt("POSStock")) {
                ReturnCode = "8";
                ReturnMessage = "Qty not enough";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //更改原先商品銷售列表內的銷售數量
            if (SalesQty + AdjustSaleQty < 0) {

                //數量錯誤
                ReturnCode = "8";
                ReturnMessage = "Sales qty error.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else if (SalesQty + AdjustSaleQty == 0) {

                //刪除購物車
                DFSItemList.remove(ItemIndex);
            } else if (SalesQty == 0) {

                //新增購物車
                DFSItem _DFSItem = new DFSItem();
                _DFSItem.ItemCode = jaItem.getJSONObject(0).getString("ItemCode");
                _DFSItem.SerialCode = jaItem.getJSONObject(0).getString("SerialCode");
                _DFSItem.DrawerNo = jaItem.getJSONObject(0).getString("DrawNo");
                _DFSItem.ItemName = jaItem.getJSONObject(0).getString("ItemName");
                _DFSItem.OriginalPrice = jaItem.getJSONObject(0).optDouble("USDPrice");
                _DFSItem.USDPrice = jaItem.getJSONObject(0).optDouble("USDPrice");
                _DFSItem.TWDPrice = jaItem.getJSONObject(0).optDouble("TWDPrice");
                _DFSItem.POSStock = jaItem.getJSONObject(0).getInt("POSStock");
                _DFSItem.IFEStock = IFEStock;
                _DFSItem.SalesQty = AdjustSaleQty;
                _DFSItem.Remark = jaItem.getJSONObject(0).getString("Remark");
                _DFSItem.GiftFlag = jaItem.getJSONObject(0).getString("GiftFlag");
                _DFSItem.DiscountFlag = "";   //這邊先不放
                _DFSItem.ExceptionDiscountFlag = jaItem.getJSONObject(0).getString("ExceptionDiscountFlag");

                if (!jaItem.getJSONObject(0).isNull("GroupID")) {
                    _DFSItem.GroupID = jaItem.getJSONObject(0).getString("GroupID");
                }

                _DFSItem.DiscountExcptionType = new ArrayList<>();

                //取得該商品不打折的Discount List
                SQL = "SELECT DiscountType " +
                        "FROM DiscountException " +
                        "WHERE ItemCode = '" + _DFSItem.ItemCode + "'";
                jaItem = _TSQL.SelectSQLJsonArray(SQL);

                if (jaItem != null && jaItem.length() > 0) {
                    for (int i = 0; i < jaItem.length(); i++) {
                        _DFSItem.DiscountExcptionType.add(jaItem.getJSONObject(i).getString("DiscountType"));
                    }
                }

                DFSItemList.add(_DFSItem);
            } else {

                //調整購物車
                DFSItemList.get(ItemIndex).SalesQty += AdjustSaleQty;

                //因應可能做Transfer動作，所以庫存一併調整
                DFSItemList.get(ItemIndex).POSStock = jaItem.getJSONObject(0).getInt("POSStock");
            }

            //重新計算商品列表和折扣
            return GetBasketInfo();

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 新增折扣列表
     *
     * @param Type       折扣代碼
     * @param DiscountNo 會員編號
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject AddDiscountList(String Type, String DiscountNo) {

        String ClassName = "Transaction";
        String FunctionName = "AddDiscountList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "Type:" + Type + "-DiscountNo:" + DiscountNo);

        try {
            //檢核折扣代碼是否有誤
            DiscountRow _DiscountRow = FlightData.GetDiscount(Type);

            if (_DiscountRow == null) {
                ReturnCode = "8";
                ReturnMessage = "Discount type not found.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //逐項檢核既有折扣清單是否已有此折扣
            for (int i = 0; i < DiscountList.size(); i++) {

                if (DiscountList.get(i).Type.equals(Type)) {
                    ReturnCode = "8";
                    ReturnMessage = "Discount type repeated.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }
            }
            //region檢測購物車內是否皆為排外商品
            Boolean Discounttag=false;
            for (int j = 0; j < DFSItemList.size(); j++) {
                if(!DFSItemList.get(j).DiscountFlag.equals("N")) {
                    Discounttag = true;
                    break;
                }
            }

            if(!Discounttag)
            {
                ReturnCode = "8";
                ReturnMessage = "These items are not eligible for discount.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            //endregion


            if (Type.contains("STAFF")) {

                int ItemTotalCount = 0;

                FuncTableRow _FuncTableRow = FlightData.GetFuncTable(_DiscountRow.Description);

                for (int j = 0; j < DFSItemList.size(); j++) {
                    //若可以打折則可計
                    if (!DFSItemList.get(j).DiscountExcptionType.contains(DiscountType) &&
                            DFSItemList.get(j).USDPrice > 0 &&
                            DFSItemList.get(j).TWDPrice > 0) {
                        ItemTotalCount += DFSItemList.get(j).SalesQty;
                    }
                }
                if (ItemTotalCount >= Integer.valueOf(_FuncTableRow.Syntax.split("-")[1])) {
                    //新增進折扣列表中
                    DiscountItem _DiscountItem = new DiscountItem();
                    _DiscountItem.DiscountNo = DiscountNo;
                    _DiscountItem.Type = _DiscountRow.Type;
                    _DiscountItem.DiscountRate = _DiscountRow.DiscountRate;
                    _DiscountItem.DiscountAmount = _DiscountRow.DiscountAmount;
                    _DiscountItem.Description = _FuncTableRow.Description;
                    _DiscountItem.UpperLimit = _DiscountRow.UpperLimit;
                    _DiscountItem.FuncID = _DiscountRow.FuncID;
                    DiscountList.add(_DiscountItem);
                } else {
                    ReturnCode = "8";
                    ReturnMessage = "Item is not enough for Staff discount.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }
            } else {

                //新增進折扣列表中
                DiscountItem _DiscountItem = new DiscountItem();
                _DiscountItem.DiscountNo = DiscountNo;
                _DiscountItem.Type = _DiscountRow.Type;
                _DiscountItem.DiscountRate = _DiscountRow.DiscountRate;
                _DiscountItem.DiscountAmount = _DiscountRow.DiscountAmount;
                _DiscountItem.Description = _DiscountRow.Description;
                _DiscountItem.UpperLimit = _DiscountRow.UpperLimit;
                _DiscountItem.FuncID = _DiscountRow.FuncID;
                DiscountList.add(_DiscountItem);
            }

            //重新計算商品列表和折扣
            return GetBasketInfo();

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 新增放大刷卡上限身分
     *
     * @param Type       折扣代碼
     * @param DiscountNo 會員編號
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject AddUplimitType(String Type, String DiscountNo) {

        String ClassName = "Transaction";
        String FunctionName = "AddUplimitType";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "Type:" + Type + "-DiscountNo:" + DiscountNo);

        try {
            //檢核折扣代碼是否有誤
            DiscountRow _DiscountRow = FlightData.GetDiscount(Type);

            if (_DiscountRow == null) {
                ReturnCode = "8";
                ReturnMessage = "Discount type not found.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //逐項檢核既有折扣清單是否已有此折扣
            for (int i = 0; i < DiscountList.size(); i++) {

                if (DiscountList.get(i).Type.equals(Type)) {
                    ReturnCode = "8";
                    ReturnMessage = "Discount type repeated.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }
            }

            //再比對放大金額
            if (_DiscountRow.UpperLimit != 0 &&
                    _DiscountRow.UpperLimit > UpperLimit) {

                UpperLimitType = _DiscountRow.Type;
                UpperLimitDiscountNo = DiscountNo;
                UpperLimit = _DiscountRow.UpperLimit;
            }

            ReturnCode = "0";
            ReturnMessage = "";
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 刪除折扣列表
     *
     * @param Type 折扣代碼
     * @return (JSONObject) 標準格式回傳
     */
    public void DeleteDiscountList(String Type) {

        String ClassName = "Transaction";
        String FunctionName = "DeleteDiscountList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "Type:" + Type);

        try {
            //檢核折扣代碼是否有誤
            DiscountRow _DiscountRow = FlightData.GetDiscount(Type);

            if (_DiscountRow == null) {
                ReturnCode = "8";
                ReturnMessage = "Discount type not found.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                return;
            }

            //逐項檢核既有折扣清單是否已有此折扣
            for (int i = 0; i < DiscountList.size(); i++) {

                if (DiscountList.get(i).Type.equals(Type)) {

                    //刪除折扣
                    DiscountList.remove(i);

                    //重新計算商品列表和折扣
                    GetBasketInfo();
                    return;
                }
            }

            ReturnCode = "8";
            ReturnMessage = "Discount type not found.";

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
            _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 取回購物車內容
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetBasketInfo() {

        String ClassName = "Transaction";
        String FunctionName = "GetBasketInfo";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String ReturnCode, ReturnMessage;
        JSONArray jaSalesItemList = new JSONArray(), jaDiscountList = new JSONArray(), jaItem;
        JSONObject joSalesItem, joDiscountItem, ResponseJsonObject;

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //交易金額資訊
            this.OriUSDTotalAmount = 0; //美金折扣前總價
            this.OriNTDTotalAmount = 0; //台幣折扣前總價
            this.USDTotalAmount = 0;    //美金應付款總金額
            this.NTDTotalAmount = 0;    //台幣應付款總金額

            //折扣資訊
            this.DiscountType = "";     //折扣會員類別
            this.DiscountNo = "";       //折扣會員編號
            this.DiscountRate = 1;      //折扣會員%數
            this.DiscountAmount = 0;    //折扣會員金額
            this.UpperLimitType = "";   //刷卡上限會員類別
            this.UpperLimitDiscountNo = "";  //刷卡上限會員編號
            this.UpperLimit = 0;        //刷卡上限金額

            //region 逐一檢核每個商品，若為贈品則先刪除
            for (int i = 0; i < DFSItemList.size(); i++) {

                //若商品為贈品，則刪除
                if (DFSItemList.get(i).USDPrice == 0 || DFSItemList.get(i).TWDPrice == 0) {
                    DFSItemList.remove(i);
                    i--;
                }
            }
            //endregion

            //region 逐一檢核每個折扣，若是主動式則先刪除，先以被動式擇優
            for (int i = 0; i < DiscountList.size(); i++) {

                //若折扣為主動式折扣則刪除
                if (DiscountList.get(i).FuncID != null && DiscountList.get(i).FuncID.length() > 0) {
                    DiscountList.remove(i);
                    i--;
                    continue;
                }

                //折扣金額永遠優於折扣率
                //先比對折扣金額
                if (DiscountList.get(i).DiscountAmount != 0 &&
                        DiscountList.get(i).DiscountAmount > this.DiscountAmount) {

                    this.DiscountType = DiscountList.get(i).Type;
                    this.DiscountNo = DiscountList.get(i).DiscountNo;
                    this.DiscountAmount = DiscountList.get(i).DiscountAmount;
                }

                //再比對折扣率(須沒有折扣金額)
                if (this.DiscountAmount == 0 &&
                        DiscountList.get(i).DiscountRate != 0 &&
                        DiscountList.get(i).DiscountRate < DiscountRate) {

                    this.DiscountType = DiscountList.get(i).Type;
                    this.DiscountNo = DiscountList.get(i).DiscountNo;
                    this.DiscountRate = DiscountList.get(i).DiscountRate;
                }

                //再比對放大金額
                if (DiscountList.get(i).UpperLimit != 0 &&
                        DiscountList.get(i).UpperLimit > UpperLimit) {

                    this.UpperLimitType = DiscountList.get(i).Type;
                    this.UpperLimitDiscountNo = DiscountList.get(i).DiscountNo;
                    this.UpperLimit = DiscountList.get(i).UpperLimit;
                }
            }
            //endregion

            //region 計算主動式折扣
            int DiscountCount; //滿足的折扣量
            if (FlightData.Discount.size() > 0) {
                //逐筆判斷是否符合該主動式折扣
                for (int i = 0; i < FlightData.Discount.size(); i++) {
                    if (!FlightData.Discount.get(i).FuncID.equals("") && (DiscountCount = CheckFuncID(FlightData.Discount.get(i).Type, FlightData.Discount.get(i).FuncID,
                            FlightData.Discount.get(i).Progression)) > 0) {

                        DiscountItem _DiscountItem = new DiscountItem();
                        _DiscountItem.DiscountNo = "";
                        _DiscountItem.Type = FlightData.Discount.get(i).Type;
                        _DiscountItem.DiscountRate = FlightData.Discount.get(i).DiscountRate;
                        _DiscountItem.DiscountAmount = FlightData.Discount.get(i).DiscountAmount;

                        //若符合的折扣是送贈品的，則標記特殊字元@@供判斷
                        if (FlightData.Discount.get(i).DiscountGift.length() == 0) {
                            _DiscountItem.Description = FlightData.Discount.get(i).Description;
                        } else {
                            _DiscountItem.Description = "@@" + FlightData.Discount.get(i).Description;
                        }

                        _DiscountItem.UpperLimit = FlightData.Discount.get(i).UpperLimit;
                        _DiscountItem.FuncID = FlightData.Discount.get(i).FuncID;
                        _DiscountItem.DiscountCount = DiscountCount;
                        DiscountList.add(_DiscountItem);

                        //折扣金額永遠優於折扣率
                        //先比對折扣金額，若是可以累計贈送的，則已累計後的金額做比對
                        if (FlightData.Discount.get(i).DiscountAmount != 0 &&
                                ((FlightData.Discount.get(i).Progression && (FlightData.Discount.get(i).DiscountAmount * DiscountCount) > DiscountAmount) ||
                                        (!FlightData.Discount.get(i).Progression && FlightData.Discount.get(i).DiscountAmount > DiscountAmount))) {

                            DiscountType = FlightData.Discount.get(i).Type;
                            DiscountNo = "";

                            //看看能不能累計
                            if (FlightData.Discount.get(i).Progression && (FlightData.Discount.get(i).DiscountAmount * DiscountCount) > DiscountAmount) {
                                DiscountAmount = FlightData.Discount.get(i).DiscountAmount * DiscountCount;
                            } else if (!FlightData.Discount.get(i).Progression && FlightData.Discount.get(i).DiscountAmount > DiscountAmount) {
                                DiscountAmount = FlightData.Discount.get(i).DiscountAmount;
                            }

                            //清空折扣率
                            DiscountRate = 1;
                        }

                        //再比對折扣率(須沒有折扣金額)
                        if (DiscountAmount == 0 &&
                                FlightData.Discount.get(i).DiscountRate != 0 &&
                                FlightData.Discount.get(i).DiscountRate < DiscountRate) {

                            DiscountType = FlightData.Discount.get(i).Type;
                            DiscountNo = "";
                            DiscountRate = FlightData.Discount.get(i).DiscountRate;
                        }

                        //再比對放大金額
                        if (FlightData.Discount.get(i).UpperLimit != 0 &&
                                FlightData.Discount.get(i).UpperLimit > UpperLimit) {

                            UpperLimitType = FlightData.Discount.get(i).Type;
                            UpperLimitDiscountNo = "";
                            UpperLimit = FlightData.Discount.get(i).UpperLimit;
                        }

                        //折扣是送贈品的
                        if (FlightData.Discount.get(i).DiscountGift.length() > 0) {

                            //先找看看原本的購物清單內有沒有此贈品。
                            Boolean IsDiscounted = false;
                            for (int j = 0; j < DFSItemList.size(); j++) {
                                if (DFSItemList.get(j).ItemCode.equals(FlightData.Discount.get(i).DiscountGift)) {
                                    //調整贈送量
                                    DFSItemList.get(j).SalesQty = DFSItemList.get(j).SalesQty + DiscountCount;
                                    IsDiscounted = true;
                                    break;
                                }
                            }

                            //沒有在原本的購物清單內，需要新增
                            if (!IsDiscounted) {

                                //取得贈品的存量
                                String SQL = "SELECT AllParts.ItemCode,SerialCode,DrawNo,ItemName, " +
                                        "ItemPriceUS AS USDPrice, ItemPriceTW AS TWDPrice, " +
                                        " (EndQty - DamageQty) AS POSStock," +
                                        DiscountCount + " AS SalesQty, Remark, 'Y' AS GiftFlag, GroupID, " +
                                        "(CASE (SELECT COUNT(*) FROM DiscountException WHERE ItemCode = '" + FlightData.Discount.get(i).DiscountGift
                                        + "') WHEN 0 THEN 'Y' ELSE 'N' END) AS ExceptionDiscountFlag " +
                                        "FROM AllParts LEFT JOIN Inventory " +
                                        "ON AllParts.ItemCode = Inventory.ItemCode " +
                                        "LEFT JOIN ItemGroup " +
                                        "ON AllParts.ItemCode = ItemGroup.ItemCode " +
                                        "WHERE AllParts.ItemCode = '" + FlightData.Discount.get(i).DiscountGift +
                                        "' AND Inventory.SecSeq = '" + this.SecSeq + "'";
                                jaItem = _TSQL.SelectSQLJsonArray(SQL);

                                //若商品不存在則記Log
                                if (jaItem == null || jaItem.length() == 0 || jaItem.getJSONObject(0).getString("ItemCode").length() == 0) {
                                    ReturnCode = "8";
                                    ReturnMessage = "Pno code error: " + FlightData.Discount.get(i).DiscountGift;

                                    //寫入Log
                                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
//                                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                                } else {
                                    //加入到Cart，庫存數量為0也加入，由UI去判斷是否顯示紅字或是粗體字
                                    DFSItem _DFSItem = new DFSItem();
                                    _DFSItem.ItemCode = jaItem.getJSONObject(0).getString("ItemCode");
                                    _DFSItem.SerialCode = jaItem.getJSONObject(0).getString("SerialCode");
                                    _DFSItem.DrawerNo = jaItem.getJSONObject(0).getString("DrawNo");
                                    _DFSItem.ItemName = jaItem.getJSONObject(0).getString("ItemName");
                                    _DFSItem.OriginalPrice = jaItem.getJSONObject(0).getInt("USDPrice");
                                    _DFSItem.USDPrice = jaItem.getJSONObject(0).getInt("USDPrice");
                                    _DFSItem.TWDPrice = jaItem.getJSONObject(0).getInt("TWDPrice");
                                    _DFSItem.POSStock = jaItem.getJSONObject(0).getInt("POSStock");
                                    _DFSItem.IFEStock = jaItem.getJSONObject(0).getInt("POSStock");
                                    _DFSItem.SalesQty = jaItem.getJSONObject(0).getInt("SalesQty");
                                    _DFSItem.ScanQty = 0;         //Scan qty預設為0
                                    _DFSItem.Remark = jaItem.getJSONObject(0).getString("Remark");
                                    _DFSItem.GiftFlag = jaItem.getJSONObject(0).getString("GiftFlag");
                                    _DFSItem.DiscountFlag = "";   //這邊先不放
                                    _DFSItem.ExceptionDiscountFlag = jaItem.getJSONObject(0).getString("ExceptionDiscountFlag");
                                    _DFSItem.GroupID = "";        //先暫放空
                                    _DFSItem.DiscountExcptionType = new ArrayList<>();
                                    DFSItemList.add(_DFSItem);
                                }
                            }
                        }
                    }
                }
            }
            //endregion

            //計算商品總價以及可否打折，一併將商品列表轉為JSON
            int CanUseDiscountUSDAmount = 0, CanUseDiscountNTDAmount = 0;   //可打折之商品總價
            int CanNotDiscountUSDAmount = 0, CanNotDiscountNTDAmount = 0;   //不可打折之商品總價
            for (int i = 0; i < DFSItemList.size(); i++) {

                //進行折扣標記，
                if (DiscountType.equals("")) {

                    //無折扣用Allparts內的折扣標記顯示
                    if (DFSItemList.get(i).ExceptionDiscountFlag.equals("N")) {
                        CanNotDiscountUSDAmount += DFSItemList.get(i).USDPrice * DFSItemList.get(i).SalesQty;
                        CanNotDiscountNTDAmount += DFSItemList.get(i).TWDPrice * DFSItemList.get(i).SalesQty;
                        DFSItemList.get(i).DiscountFlag = "N";
                    } else {
                        CanUseDiscountUSDAmount += DFSItemList.get(i).USDPrice * DFSItemList.get(i).SalesQty;
                        CanUseDiscountNTDAmount += DFSItemList.get(i).TWDPrice * DFSItemList.get(i).SalesQty;
                        DFSItemList.get(i).DiscountFlag = "Y";
                    }

                } else {

                    //有折扣用Discount exception內的折扣標記顯示
                    if (DFSItemList.get(i).DiscountExcptionType.contains(this.DiscountType)) {
                        CanNotDiscountUSDAmount += DFSItemList.get(i).USDPrice * DFSItemList.get(i).SalesQty;
                        CanNotDiscountNTDAmount += DFSItemList.get(i).TWDPrice * DFSItemList.get(i).SalesQty;
                        DFSItemList.get(i).DiscountFlag = "N";
                    } else {
                        CanUseDiscountUSDAmount += DFSItemList.get(i).USDPrice * DFSItemList.get(i).SalesQty;
                        CanUseDiscountNTDAmount += DFSItemList.get(i).TWDPrice * DFSItemList.get(i).SalesQty;
                        DFSItemList.get(i).DiscountFlag = "Y";
                    }
                }

                //統計銷售折扣前總價
                this.OriUSDTotalAmount += DFSItemList.get(i).USDPrice * DFSItemList.get(i).SalesQty;
                this.OriNTDTotalAmount += DFSItemList.get(i).TWDPrice * DFSItemList.get(i).SalesQty;

                joSalesItem = new JSONObject();
                joSalesItem.put("ItemCode", DFSItemList.get(i).ItemCode);
                joSalesItem.put("SerialCode", DFSItemList.get(i).SerialCode);
                joSalesItem.put("DrawerNo", DFSItemList.get(i).DrawerNo);
                joSalesItem.put("ItemName", DFSItemList.get(i).ItemName);
                joSalesItem.put("OriginalPrice", DFSItemList.get(i).OriginalPrice);
                joSalesItem.put("USDPrice", DFSItemList.get(i).USDPrice);
                joSalesItem.put("TWDPrice", DFSItemList.get(i).TWDPrice);
                joSalesItem.put("POSStock", DFSItemList.get(i).POSStock);
                joSalesItem.put("IFEStock", DFSItemList.get(i).IFEStock);
                joSalesItem.put("SalesQty", DFSItemList.get(i).SalesQty);
                joSalesItem.put("Remark", DFSItemList.get(i).Remark);
                joSalesItem.put("GiftFlag", DFSItemList.get(i).GiftFlag);
                joSalesItem.put("DiscountFlag", DFSItemList.get(i).DiscountFlag);
                jaSalesItemList.put(joSalesItem);
            }

            //將折扣列表轉JSON
            for (int i = 0; i < DiscountList.size(); i++) {
                joDiscountItem = new JSONObject();
                joDiscountItem.put("DiscountNo", DiscountList.get(i).DiscountNo);
                joDiscountItem.put("Type", DiscountList.get(i).Type);
                joDiscountItem.put("Description", DiscountList.get(i).Description);
                joDiscountItem.put("DiscountCount", DiscountList.get(i).DiscountCount);
                joDiscountItem.put("FuncID", DiscountList.get(i).FuncID);
                jaDiscountList.put(joDiscountItem);
            }

            //計算打折
            if (!DiscountType.equals("") && this.DiscountRate != 0) {

                this.USDTotalAmount = Arith.mul(CanUseDiscountUSDAmount, this.DiscountRate);
                //this.NTDTotalAmount = Arith.mul(CanUseDiscountNTDAmount, this.DiscountRate);

                //台幣四捨五入十進位
                JSONObject jo = _PublicFunctions.ChangeCurrencyAmount("TWD", "TWD", Arith.mul(CanUseDiscountNTDAmount, this.DiscountRate), 0, true, 2);
                if (!jo.getString("ReturnCode").equals("0")) {
                    return jo;
                }
                this.NTDTotalAmount = jo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");


                this.USDTotalAmount += CanNotDiscountUSDAmount;
                this.NTDTotalAmount += CanNotDiscountNTDAmount;
            } else {
                this.USDTotalAmount = this.OriUSDTotalAmount;
                this.NTDTotalAmount = this.OriNTDTotalAmount;
            }

            //將計算結果轉為Response格式
            ResponseJsonObject = new JSONObject();
            ResponseJsonObject.put("ReceiptNo", this.ReceiptNo);
            ResponseJsonObject.put("SeatNo", this.SeatNo);
            ResponseJsonObject.put("IFEOrderNo", this.IFEOrderNo);
            ResponseJsonObject.put("OriUSDAmount", this.OriUSDTotalAmount);
//            ResponseJsonObject.put("OriTWDAmount", this.OriNTDTotalAmount);
            ResponseJsonObject.put("USDAmount", this.USDTotalAmount);
//            ResponseJsonObject.put("TWDAmount", this.NTDTotalAmount);
            ResponseJsonObject.put("DiscountType", this.DiscountType);
            ResponseJsonObject.put("DiscountNo", this.DiscountNo);
            ResponseJsonObject.put("DiscountRate", this.DiscountRate);
            ResponseJsonObject.put("DiscountAmount", this.DiscountAmount);
            ResponseJsonObject.put("UpperLimitType", this.UpperLimitType);
            ResponseJsonObject.put("UpperLimitDiscountNo", this.UpperLimitDiscountNo);
            ResponseJsonObject.put("UpperLimit", this.UpperLimit);
            ResponseJsonObject.put("Items", jaSalesItemList);
            ResponseJsonObject.put("DiscountList", jaDiscountList);

            ReturnCode = "0";
            ReturnMessage = "";
            JSONArray ReturnData = new JSONArray();
            ReturnData.put(ResponseJsonObject);
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, ReturnData);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //                            Payment
    ////////////////////////////////////////////////////////////////////////

    /**
     * 新增付款列表
     *
     * @param Currency 付款幣別
     * @param PayBy    付款類別
     * @param Amount   付款金額
     * @param CouponNo 折扣卷代碼，不使用則傳Null
     * @param CardNo   信用卡號，不使用則傳Null
     * @param CardName 信用卡持卡人姓名，不使用則傳Null
     * @param CardDate 信用卡效期，不使用則傳Null
     * @param CardType 信用卡別，不使用則傳Null
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject AddPaymentList(String Currency, PaymentType PayBy, double Amount, String CouponNo, String CardNo, String CardName, String CardDate, CreditCardType CardType) {

        String ClassName = "Transaction";
        String FunctionName = "AddPaymentList";
        String ReturnCode;
        StringBuilder ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONObject TempJo;

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName,
                "Function start:" + "Currency:" + Currency + "-PayBy:" + PayBy + "-Amount:" + Amount + "-CouponNo:" + CouponNo);

        try {

            //region 付款檢核
            //模式檢核
            if (NowPayMode != PayMode.PAY) {
                ReturnCode = "8";
                ReturnMessage = new StringBuilder("This's not pay mode.");

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
            }

            //檢核付款金額是否大於0
            if (Amount <= 0) {
                ReturnCode = "8";
                ReturnMessage = new StringBuilder("Please Key in Pay.");

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
            }

            //檢核付款幣別
            if (Currency == null || Currency.length() == 0) {
                ReturnCode = "8";
                ReturnMessage = new StringBuilder("Please check pay currency.");

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
            }

            //判斷是不是符合最後最大付款金額
            double MaxPayAmount;          //該幣別最大付款額
            double USDAmount;             //該金額代表美金金額

            //取得該幣別最大付款金額
            TempJo = GetCurrencyMaxAmount(Currency);
            if (!TempJo.getString("ReturnCode").equals("0")) {
                return TempJo;
            }
            MaxPayAmount = TempJo.getJSONArray("ResponseData").getJSONObject(0).getDouble("MaxPayAmount");

            //取得該金額所代表美金金額
            if ((Currency.equals(LastPayCurrency) && Amount == LastPayAmount) ||
                    (Amount == MaxPayAmount)) {

                //直接帶美金最後剩餘付款額
                USDAmount = USDTotalUnpay;
            } else {
                //先換算付款金額所代表之美金
                TempJo = _PublicFunctions.ChangeCurrencyAmount(Currency, "USD", Amount, 0.01, false, 2);
                if (!TempJo.getString("ReturnCode").equals("0")) {
                    return TempJo;
                }

                USDAmount = TempJo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");
            }

            int SwipeCount = 0;     //已刷卡次數
            double SwipeUSDAmount;  //已刷卡金額
            int LastLimitation = 0; //剩餘可刷卡金額
            //現金檢核
            if (PayBy == PaymentType.Cash) {

                //檢核付款金額是否符合該幣別最低應付金額
                if (Amount % _PublicFunctions.GetMiniValue(Currency) != 0) {
                    ReturnCode = "8";
                    ReturnMessage = new StringBuilder("The amount of error.");

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
                }

                //檢核所有幣別未付款金額是否大於0
                String[] currency = new String[]{"USD","TWD","JPY","CNY","HKD","GBP","EUR"};
                double USDunpayAmount = Arith.sub(USDTotalUnpay,USDAmount );  //直接帶美金最後應找零額
//                if(USDunpayAmount<0){
//                    ReturnCode = "8";
//                    ReturnMessage = new StringBuilder( "Payment amount is not enough,please correct the amount or use credit card to pay.");
//                    //寫入Log
//                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
//                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
//                }
                if (USDunpayAmount>0){
                    for (int i = 0; i < currency.length; i++) {
                        double amount = new PublicFunctions(context, FlightData.SecSeq).ChangeCurrencyAmount("USD", currency[i], USDunpayAmount, 0, true, 2)
                                .getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");
                        if (amount <= 0){
                            ReturnCode = "8";
                            ReturnMessage = new StringBuilder( "Payment amount is not enough,please correct the amount or use credit card to pay.");
                            //寫入Log
                            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
                            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
                        }
                    }
                }

            }
            //信用卡檢核
            else if (PayBy == PaymentType.Card) {

                //已經使用過信用卡
                if (this.CardNo != null &&
                        !this.CardNo.equals("")) {
                    ReturnCode = "8";
                    ReturnMessage = new StringBuilder("Already use other card.");

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
                }

                //信用卡資料不齊全
                if ((CardNo == null || CardNo.length() == 0) ||
                        (CardName == null || CardName.length() == 0) ||
                        (CardDate == null || CardDate.length() == 0) ||
                        CardType == null) {
                    ReturnCode = "8";
                    ReturnMessage = new StringBuilder("Please check credit card info.");

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
                }

                //再檢核一次黑名單
                TempJo = _PublicFunctions.CheckBlackCard(String.valueOf(CardType), CardNo);
                if (!TempJo.getString("ReturnCode").equals("0")) {
                    return TempJo;
                }

                //檢核若是使用聯名卡折扣或是放大刷卡上限，則需要使用該聯名卡消費
                if ((!Objects.equals(DiscountType, "") && (DiscountType.equals("AE") || DiscountType.equals("AEG") || DiscountType.equals("CUB")) && !DiscountNo.equals(CardNo)) ||
                        (!Objects.equals(UpperLimitType, "") && (UpperLimitType.equals("AE") || UpperLimitType.equals("AEG") || UpperLimitType.equals("CUB"))
                                && !UpperLimitDiscountNo.equals(CardNo))) {
                    ReturnCode = "8";
                    ReturnMessage = new StringBuilder("Please use co-brand card.");

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
                }

                //檢核該卡別卡號可以刷的幣別是否符合
                TempJo = _PublicFunctions.GetCardCurrencyList(String.valueOf(CardType), CardNo);
                if (TempJo.getString("ReturnCode").equals("0")) {

                    //逐筆檢核
                    Boolean IsRight = false;
                    for (int i = 0; i < TempJo.getJSONArray("ResponseData").length(); i++) {
                        if (TempJo.getJSONArray("ResponseData").getJSONObject(i).getString("Currency").equals(Currency)) {
                            IsRight = true;
                        }
                    }

                    //付款幣別不在該卡別卡號清單內
                    if (!IsRight && TempJo.getJSONArray("ResponseData").length() > 1) {

                        ReturnCode = "8";
                        ReturnMessage = new StringBuilder("Must be TWD or USD.");

                        for (int i = 0; i < TempJo.getJSONArray("ResponseData").length(); i++) {
                            ReturnMessage.append(TempJo.getJSONArray("ResponseData").getJSONObject(i).getString("Currency")).append(" or ");
                        }
                        ReturnMessage = new StringBuilder(ReturnMessage.substring(0, ReturnMessage.length() - 4));
                        ReturnMessage.append(".");

                        //寫入Log
                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
                        return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);

                    } else if (!IsRight && TempJo.getJSONArray("ResponseData").length() == 1) {

                        ReturnCode = "8";
                        ReturnMessage = new StringBuilder("This card accepts " + TempJo.getJSONArray("ResponseData").getJSONObject(0).getString("Currency") + " only.");

                        //寫入Log
                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
                        return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
                    }
                } else {
                    //錯誤，直接將錯誤碼拋出
                    return TempJo;
                }

                //已付款金額 + 此次付款金額 > 需付總額,則提示錯誤。
                if ((PaymentList.size() == 0 && Currency.equals("TWD") && Amount > MaxPayAmount) ||
                        (IsTotalUseTWDPay && Currency.equals("TWD") && Amount > MaxPayAmount) ||
                        (PaymentList.size() == 0 && !Currency.equals("TWD") && (Arith.add(USDTotalPayed, USDAmount)) > USDTotalAmount) ||
                        (!IsTotalUseTWDPay && (Arith.add(USDTotalPayed, USDAmount)) > USDTotalAmount)) {
//                if (Arith.add(USDTotalPayed,USDAmount) > USDTotalAmount) {
                    ReturnCode = "8";
                    ReturnMessage = new StringBuilder("Over payment.");

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
                }

                //取得卡片目前刷卡次數和金額
                TempJo = _PublicFunctions.GetCardTotalPayInfo(CardNo);

                if (TempJo.getString("ReturnCode").equals("0")) {
                    SwipeCount = TempJo.getJSONArray("ResponseData").getJSONObject(0).getInt("SwipeCount");
                    SwipeUSDAmount = TempJo.getJSONArray("ResponseData").getJSONObject(0).optDouble("SwipeUSDAmount");
                } else {
                    //錯誤，直接將錯誤碼拋出
                    return TempJo;
                }

                //取得該信用卡別可刷卡上限
                int MemberCardMaxUSDAmount;       //此聯名卡最大可刷卡金額
                TempJo = _PublicFunctions.GetCardMaxAmountInfo(String.valueOf(CardType), Currency);

                if (TempJo.getString("ReturnCode").equals("0")) {
                    MemberCardMaxUSDAmount = TempJo.getJSONArray("ResponseData").getJSONObject(0).getInt("MaxAmount");
                } else {
                    //錯誤，直接將錯誤碼拋出
                    return TempJo;
                }

                //判斷是否有超過刷卡額度
                if (MemberCardMaxUSDAmount > UpperLimit) {
                    UpperLimit = MemberCardMaxUSDAmount;
                }

                //計算剩餘可刷卡金額，整數呈現，不可呈現負數
                LastLimitation = (int) _PublicFunctions.DecimalPointChange((UpperLimit - SwipeUSDAmount), 1, 3);
                if (LastLimitation < 0) {
                    LastLimitation = 0;
                }

//                if (USDTotalUnpay > 1500) {
//                    ReturnCode = "8";
//                    ReturnMessage = new StringBuilder("This card had been used " + SwipeCount + " times, the last card limitation is USD " + LastLimitation);
//
//                    //寫入Log
//                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
//                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
//                }

                if (Arith.add(SwipeUSDAmount, USDAmount) > UpperLimit) {
                    ReturnCode = "8";
                    ReturnMessage = new StringBuilder("This card had been used " + SwipeCount + " times, the last card limitation is USD " + LastLimitation);

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
                }
            }
            //Coupon檢核
            else if (PayBy == PaymentType.SC || PayBy == PaymentType.DC) {

                //檢核Coupon票卷資訊是否正確
                TempJo = _PublicFunctions.GetCouponInfo(String.valueOf(PayBy), CouponNo);
                if (!TempJo.getString("ReturnCode").equals("0")) {
                    return TempJo;
                }

                //判斷付款的金額和幣別是否與Coupon面額相符
                if (!TempJo.getJSONArray("ResponseData").getJSONObject(0).getString("CouponCurrency").equals(Currency) ||
                        TempJo.getJSONArray("ResponseData").getJSONObject(0).getInt("CouponAmount") != Amount) {
                    ReturnCode = "8";
                    ReturnMessage = new StringBuilder("Coupon info error.");

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
                }

                //檢核是否已經在此付款歷程使用過
                for (int i = 0; i < PaymentList.size(); i++) {
                    if (PaymentList.get(i).CouponNo != null && PaymentList.get(i).CouponNo.equals(CouponNo)) {
                        ReturnCode = "8";
                        ReturnMessage = new StringBuilder("Coupon repeated.");

                        //寫入Log
                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage.toString());
                        return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
                    }
                }

                //檢核是否已經在此航班使用過
                TempJo = _PublicFunctions.CheckCouponNoIsExist(CouponNo);
                if (!TempJo.getString("ReturnCode").equals("0")) {
                    return TempJo;
                }
            }
            //endregion

            //region 加入付款歷程
            if (PayBy == PaymentType.SC || PayBy == PaymentType.DC) {

                //Coupon一定是新增
                PaymentItem _PaymentItem = new PaymentItem();
                if (USDAmount > USDTotalUnpay) {
                    //Coupon超付不找零
                    _PaymentItem.Currency = Currency;
                    _PaymentItem.PayBy = String.valueOf(PayBy);
                    _PaymentItem.Amount = Amount;
                    _PaymentItem.USDAmount = USDTotalUnpay;
                    _PaymentItem.CouponUSDAmount = USDAmount;
                    _PaymentItem.CouponNo = CouponNo;
                    _PaymentItem.CardType = "";
                    _PaymentItem.CardNo = "";
                    _PaymentItem.CardName = "";
                    _PaymentItem.CardDate = "";
                } else {
                    _PaymentItem.Currency = Currency;
                    _PaymentItem.PayBy = String.valueOf(PayBy);
                    _PaymentItem.Amount = Amount;
                    _PaymentItem.USDAmount = USDAmount;
                    _PaymentItem.CouponUSDAmount = USDAmount;
                    _PaymentItem.CouponNo = CouponNo;
                    _PaymentItem.CardType = "";
                    _PaymentItem.CardNo = "";
                    _PaymentItem.CardName = "";
                    _PaymentItem.CardDate = "";
                }
                PaymentList.add(_PaymentItem);
            } else {

                //逐筆檢核是否為新增
                Boolean IsAddNewPayment = true;

                for (int i = 0; i < PaymentList.size(); i++) {
                    if (PaymentList.get(i).PayBy.equals(String.valueOf(PayBy)) &&
                            PaymentList.get(i).Currency.equals(Currency)) {

                        PaymentList.get(i).Amount = Arith.add(PaymentList.get(i).Amount, Amount);
                        PaymentList.get(i).USDAmount = Arith.add(PaymentList.get(i).USDAmount, USDAmount);
                        PaymentList.get(i).CouponUSDAmount = Arith.add(PaymentList.get(i).CouponUSDAmount, USDAmount);
                        IsAddNewPayment = false;
                    }
                }

                //新增付款紀錄
                if (IsAddNewPayment) {
                    PaymentItem _PaymentItem = new PaymentItem();
                    _PaymentItem.Currency = Currency;
                    _PaymentItem.PayBy = String.valueOf(PayBy);
                    _PaymentItem.Amount = Amount;
                    _PaymentItem.USDAmount = USDAmount;
                    _PaymentItem.CouponUSDAmount = USDAmount;
                    _PaymentItem.CouponNo = "";

                    if (PayBy == PaymentType.Card) {
                        _PaymentItem.CardType = String.valueOf(CardType);
                        _PaymentItem.CardNo = CardNo;
                        _PaymentItem.CardName = CardName;
                        _PaymentItem.CardDate = CardDate;
                        _PaymentItem.SwipeCount = SwipeCount;
                        _PaymentItem.LastLimitation = LastLimitation;
                    } else {
                        _PaymentItem.CardType = "";
                        _PaymentItem.CardNo = "";
                        _PaymentItem.CardName = "";
                        _PaymentItem.CardDate = "";
                        _PaymentItem.SwipeCount = 0;
                        _PaymentItem.LastLimitation = 0;
                    }
                    PaymentList.add(_PaymentItem);
                }

                //若為信用卡付款則新增到信用卡資訊內
                if (PayBy == PaymentType.Card) {
                    this.CardNo = CardNo;
                    this.CardDate = CardDate;
                    this.CardName = CardName;
                    this.CardType = String.valueOf(CardType);
                }
            }
            //endregion

            //將原幣別剩餘的金額以手動方式帶出,
            LastPayCurrency = Currency;                       //最後交易幣別
            LastPayAmount = Arith.sub(MaxPayAmount, Amount);   //該幣別剩餘金額

            //重新計算剩餘付款額和付款頁面
            return GetPaymentMode();

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = new StringBuilder(ex.getMessage());
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage.toString(), null);
        }
    }

    /**
     * 新增找零列表
     *
     * @param Currency 找零幣別
     * @param Amount   找零金額
     * @return (JSONObject) 標準格式回傳
     */


    public JSONObject AddChangeList(String Currency, double Amount) {

        String ClassName = "Transaction";
        String FunctionName = "AddChangeList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONObject TempJo;

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "Currency:" + Currency + "-Amount:" + Amount);

        try {

            //模式檢核
            if (NowPayMode != PayMode.CHANGE) {
                ReturnCode = "8";
                ReturnMessage = "This's not change mode.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //檢核找零金額是否大於0
            if (Amount <= 0) {
                ReturnCode = "8";
                ReturnMessage = "Please Key in Pay.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }



            //檢核付款幣別
            if (Currency == null || Currency.length() == 0) {
                ReturnCode = "8";
                ReturnMessage = "Please check pay currency.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //判斷是不是符合最後最大找零金額
            double MaxChangeAmount;          //該幣別最大找零額
            double USDAmount;                //該金額代表美金金額

            //取得該幣別最大付款金額
            TempJo = GetCurrencyMaxAmount(Currency);
            if (!TempJo.getString("ReturnCode").equals("0")) {
                return TempJo;
            }
            MaxChangeAmount = TempJo.getJSONArray("ResponseData").getJSONObject(0).getDouble("MaxPayAmount");

            //正負號轉換
            MaxChangeAmount = Arith.sub(0, MaxChangeAmount);

            //取得該金額所代表美金金額
            if ((Currency.equals(LastPayCurrency) && Amount == LastPayAmount) ||
                    (Amount == MaxChangeAmount)) {

                //直接帶美金最後應找零額
                USDAmount = Arith.sub(USDTotalPayed, USDTotalAmount);
            } else {
                //先換算付款金額所代表之美金
                TempJo = _PublicFunctions.ChangeCurrencyAmount(Currency, "USD", Amount, 0.01, false, 2);
                if (!TempJo.getString("ReturnCode").equals("0")) {
                    return TempJo;
                }

                USDAmount = TempJo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");
            }

            //已找零金額 + 此次找零金額 > 需找零總額,則提示錯誤。
            if ((IsTotalUseTWDPay && Currency.equals("TWD") && Amount > MaxChangeAmount) || (!IsTotalUseTWDPay && USDAmount > Arith.sub(USDTotalPayed, USDTotalAmount))) {
                ReturnCode = "8";
                ReturnMessage = "input amount is exceeded the correct amount to customer.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //檢核找零金額是否符合該幣別最低應付金額
            if (Amount % _PublicFunctions.GetMiniValue(Currency) != 0) {
                ReturnCode = "8";
                ReturnMessage = "The amount of error.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //region 加入付款歷程
            //逐筆檢核是否為新增
            Boolean IsAddNewPayment = true;

            for (int i = 0; i < PaymentList.size(); i++) {
                if (PaymentList.get(i).PayBy.equals("Change") &&
                        PaymentList.get(i).Currency.equals(Currency)) {

                    PaymentList.get(i).Amount = Arith.add(PaymentList.get(i).Amount, Amount);
                    PaymentList.get(i).USDAmount = Arith.add(PaymentList.get(i).USDAmount, USDAmount);
                    PaymentList.get(i).CouponUSDAmount = Arith.add(PaymentList.get(i).CouponUSDAmount, USDAmount);
                    IsAddNewPayment = false;
                }
            }

            //新增付款紀錄
            if (IsAddNewPayment) {
                PaymentItem _PaymentItem = new PaymentItem();
                _PaymentItem.Currency = Currency;
                _PaymentItem.PayBy = "Change";
                _PaymentItem.Amount = Amount;
                _PaymentItem.USDAmount = USDAmount;
                _PaymentItem.CouponUSDAmount = USDAmount;
                _PaymentItem.CouponNo = "";
                _PaymentItem.CardType = "";
                _PaymentItem.CardNo = "";
                _PaymentItem.CardName = "";
                _PaymentItem.CardDate = "";
                PaymentList.add(_PaymentItem);
            }

            //endregion

            //將原幣別剩餘的金額以手動方式帶出,
            LastPayCurrency = Currency;                        //最後交易幣別
            LastPayAmount = Arith.sub(MaxChangeAmount, Amount); //該幣別找零金額

            //重新計算剩餘付款額和付款頁面
            return GetPaymentMode();

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 刪除付款列表
     *
     * @param Currency 付款幣別
     * @param PayBy    付款類別
     * @param CouponNo 折扣卷代碼，不使用則傳Null
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject DeletePaymentList(String Currency, PaymentType PayBy, String CouponNo) {

        String ClassName = "Transaction";
        String FunctionName = "DeletePaymentList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "Currency:" + Currency + "-PayBy:" + PayBy + "-CouponNo:" + CouponNo);

        try {

            int RemoveIndex = -1;              //刪除的Index
            int CouponDifferenceIndex = -1;    //需先刪除的Coupon Index
            int ChangeIndex = -1;              //找零的代表Index

            //先找到指定刪除的付款歷程
            for (int i = 0; i < PaymentList.size(); i++) {

                //找要刪除的Index
                if (PaymentList.get(i).PayBy.equals(String.valueOf(PayBy)) &&
                        PaymentList.get(i).Currency.equals(Currency)) {

                    if (CouponNo == null) {
                        RemoveIndex = i;
                    } else if (PaymentList.get(i).CouponNo.equals(CouponNo)) {
                        RemoveIndex = i;
                    }
                }

                //找有差異的Coupon付款紀錄
                if (PaymentList.get(i).USDAmount != PaymentList.get(i).CouponUSDAmount) {
                    CouponDifferenceIndex = i;
                }

                //找零的紀錄
                if (PaymentList.get(i).PayBy.equals("Change")) {
                    ChangeIndex = i;
                }
            }

            //沒有找到
            if (RemoveIndex == -1) {
                ReturnCode = "8";
                ReturnMessage = "Payment info not found.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //刪除Change的部份,可以直接刪除
            if (PayBy == PaymentType.Change) {

                PaymentList.remove(RemoveIndex);

            } else {

                //有差異的Coupon
                if (CouponDifferenceIndex != -1 &&
                        CouponDifferenceIndex != RemoveIndex) {
                    ReturnCode = "8";
                    ReturnMessage = "Please delete CPN No:" + PaymentList.get(CouponDifferenceIndex).CouponNo + " first.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //還有找零紀錄,提示先刪除
                if (ChangeIndex != -1) {
                    ReturnCode = "8";
                    ReturnMessage = "Please delete the return-change record first.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                PaymentList.remove(RemoveIndex);

                //清空信用卡列表
                if (PayBy == PaymentType.Card) {
                    this.CardNo = "";
                    this.CardDate = "";
                    this.CardName = "";
                    this.CardType = "";
                }
            }

            //清空最後付款幣別
            LastPayCurrency = "";         //最後交易幣別
            LastPayAmount = 0;            //該幣別找零金額

            //重新計算剩餘付款額和付款頁面
            return GetPaymentMode();

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 清空付款列表
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject ClearPaymentList() {

        String ClassName = "Transaction";
        String FunctionName = "ClearPaymentList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //清空付款列表
            PaymentList.clear();

            //清空信用卡資訊
            this.CardNo = "";
            this.CardDate = "";
            this.CardName = "";
            this.CardType = "";

            LastPayCurrency = "";         //最後交易幣別
            LastPayAmount = 0;            //該幣別找零金額

            //重新計算剩餘付款額和付款頁面
            return GetPaymentMode();

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    public List<PaymentItem> getPaymentList() {
        return PaymentList;
    }

    /**
     * 取得付款模式
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetPaymentMode() {

        String ClassName = "Transaction";
        String FunctionName = "GetPaymentMode";
        String ReturnCode, ReturnMessage;
        JSONObject jo;
        JSONArray ja = new JSONArray();
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //付款金額歸零,重新計算
            USDTotalPayed = 0;           //美金已付款金額
            NTDTotalPayed = 0;           //台幣已付款金額
            USDTotalUnpay = USDTotalAmount;         //美金未付款金額
            NTDTotalUnpay = NTDTotalAmount;         //台幣未付款金額

//            if (PaymentList.size() == 0)
//                IsTotalUseTWDPay = false;
//            else
            IsTotalUseTWDPay = true;

            for (int i = 0; i < PaymentList.size(); i++) {

                //判斷是不是全部台幣付款
                if (!PaymentList.get(i).Currency.equals("TWD")) {
                    IsTotalUseTWDPay = false;
                }

                //計算美金已付款金額
                if (PaymentList.get(i).PayBy.equals("Change")) {

                    //找零
                    USDTotalPayed = Arith.sub(USDTotalPayed, PaymentList.get(i).USDAmount);
                    USDTotalUnpay = Arith.add(USDTotalUnpay, PaymentList.get(i).USDAmount);

                    //計算台幣已付款金額
                    if (PaymentList.get(i).Currency.equals("TWD")) {
                        NTDTotalPayed = Arith.sub(NTDTotalPayed, PaymentList.get(i).Amount);
                        NTDTotalUnpay = Arith.add(NTDTotalUnpay, PaymentList.get(i).Amount);
                    }
                } else {

                    //付款
                    USDTotalPayed = Arith.add(USDTotalPayed, PaymentList.get(i).USDAmount);
                    USDTotalUnpay = Arith.sub(USDTotalUnpay, PaymentList.get(i).USDAmount);

                    //計算台幣已付款金額
                    if (PaymentList.get(i).Currency.equals("TWD")) {
                        NTDTotalPayed = Arith.add(NTDTotalPayed, PaymentList.get(i).Amount);
                        NTDTotalUnpay = Arith.sub(NTDTotalUnpay, PaymentList.get(i).Amount);
                    }
                }
            }

            //判斷付款模式，不以台幣為準，以美金為準，全台幣應付款金額由GetCurrencyMaxAmount帶出
            if ((USDTotalAmount == 0) ||
                    (NTDTotalAmount == 0) ||
                    (USDTotalAmount > USDTotalPayed)) {
//                    (!IsTotalUseTWDPay && USDTotalAmount > USDTotalPayed) ||
//                    (IsTotalUseTWDPay && NTDTotalAmount > NTDTotalPayed)) {

                //Pay mode
                NowPayMode = PayMode.PAY;

            } else if (USDTotalAmount == USDTotalPayed) {
//            } else if ((!IsTotalUseTWDPay && USDTotalAmount == USDTotalPayed) ||
//                    (IsTotalUseTWDPay && NTDTotalAmount == NTDTotalPayed)) {

                //Balance mode
                NowPayMode = PayMode.BALANCE;

            } else {

                //顛倒尾款正負
                if (LastPayAmount < 0) {
                    //LastPayAmount = Arith.sub(0, LastPayAmount);      //該幣別剩餘金額
                }

                //Change mode
                NowPayMode = PayMode.CHANGE;
            }

            //串目前的付款里程
            for (int i = 0; i < PaymentList.size(); i++) {
                jo = new JSONObject();
                jo.put("Currency", PaymentList.get(i).Currency);
                jo.put("PayBy", PaymentList.get(i).PayBy);
                jo.put("Amount", PaymentList.get(i).Amount);
                jo.put("USDAmount", PaymentList.get(i).USDAmount);
                jo.put("CouponNo", PaymentList.get(i).CouponNo);
                jo.put("SwipeCount", PaymentList.get(i).SwipeCount);
                jo.put("LastLimitation", PaymentList.get(i).LastLimitation);
                ja.put(jo);
            }

            //串目前付款資訊
            jo = new JSONObject();
            jo.put("ReceiptNo", this.ReceiptNo);
            jo.put("NowPayMode", String.valueOf(NowPayMode));
            jo.put("USDTotalAmount", USDTotalAmount);
            jo.put("USDTotalPayed", USDTotalPayed);
            jo.put("USDTotalUnpay", USDTotalUnpay);
            jo.put("CardNo", CardNo);

            if (NowPayMode != PayMode.BALANCE &&
                    LastPayCurrency != null &&
                    LastPayCurrency.length() > 0) {
                jo.put("LastPayCurrency", LastPayCurrency);
                jo.put("LastPayAmount", LastPayAmount);
            } else {
                jo.put("LastPayCurrency", "");
                jo.put("LastPayAmount", 0);
            }

            jo.put("PaymentList", ja);

            ReturnCode = "0";
            ReturnMessage = "";
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, new JSONArray().put(jo));

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 取得特定幣別最大應付款金額 (符合最低應付款進位)
     *
     * @param Currency 要查詢的幣別
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetCurrencyMaxAmount(String Currency) {

        String ClassName = "Transaction";
        String FunctionName = "GetCurrencyMaxAmount";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");




        double MaxPayAmount;  //新幣別應付款金額
        JSONObject jo;
        JSONArray ja = new JSONArray();

        boolean hasDiscount = false;

        for (DiscountItem discountitem : DiscountList) {
            if (discountitem.DiscountRate < 1.0) {
                hasDiscount = true;
                break;
            }
        }

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "Currency:" + Currency);

        try {

            //付款模式下，若全部都以台幣付款，則以台幣計價
            if (NowPayMode == PayMode.PAY &&
                    (IsTotalUseTWDPay && Currency.equals("TWD")) ||
                    (PaymentList.size() == 0 && Currency.equals("TWD"))) {

                jo = _PublicFunctions.ChangeCurrencyAmount("TWD", Currency, NTDTotalUnpay, 0, true, 2);

                if (!jo.getString("ReturnCode").equals("0")) {
                    return jo;
                }

                MaxPayAmount = jo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");

            }
            //付款模式下，且沒有折扣時，使用無條件進位換算
            else if (NowPayMode == PayMode.PAY && !hasDiscount) {
                jo = _PublicFunctions.ChangeCurrencyAmount("USD", Currency, USDTotalUnpay, 0, true, 1);

                if (!jo.getString("ReturnCode").equals("0")) {
                    return jo;
                }

                MaxPayAmount = jo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");
            }
            //找零模式下，若全部都以台幣付款，則其他幣別也由TWD換過去
            else if (NowPayMode == PayMode.CHANGE && IsTotalUseTWDPay && Currency.equals("TWD")) {

                jo = _PublicFunctions.ChangeCurrencyAmount("TWD", Currency, NTDTotalUnpay, 0, true, 2);

                if (!jo.getString("ReturnCode").equals("0")) {
                    return jo;
                }

                MaxPayAmount = jo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");
            }
            //找零模式下，美金會多找，所以要另外做判斷 2018/05/18 Sam 註解
//            else if (NowPayMode == PayMode.CHANGE && Currency.equals("USD")) {
//                String amount = String.valueOf(USDTotalUnpay);
//                String decimalPoint = amount.substring(amount.indexOf(".") + 1, amount.indexOf(".") + 2);
//                if (-1 < USDTotalUnpay && USDTotalUnpay < 0) {
//                    jo = _PublicFunctions.ChangeCurrencyAmount("USD", Currency, USDTotalUnpay, 0, true, 2);
//                } else {
//                    if (5 < Integer.valueOf(decimalPoint) && Integer.valueOf(decimalPoint) <= 9) {
//                        jo = _PublicFunctions.ChangeCurrencyAmount("USD", Currency, Arith.mul(Math.ceil(0 - USDTotalUnpay), -1), 0, true, 2);
//                    } else {
//                        jo = _PublicFunctions.ChangeCurrencyAmount("USD", Currency, Arith.mul(Math.floor(0 - USDTotalUnpay), -1), 0, true, 2);
//                    }
//                }
//
//                if (!jo.getString("ReturnCode").equals("0")) {
//                    return jo;
//                }
//
//                MaxPayAmount = jo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");
//            }
            //同幣別找零，同幣別直接進行相減
            else if (NowPayMode == PayMode.CHANGE && Currency.equals(PaymentList.get(PaymentList.size() - 1).Currency)) {
                MaxPayAmount = LastPayAmount;
            }
            //其餘的幣別
            else {
                //將未付款餘額取最低付款額，採四捨五入
                jo = _PublicFunctions.ChangeCurrencyAmount("USD", Currency, USDTotalUnpay, 0, true, 2);

                if (!jo.getString("ReturnCode").equals("0")) {
                    return jo;
                }

                MaxPayAmount = jo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");
            }

            jo = new JSONObject();
            jo.put("Currency", Currency);
            jo.put("MaxPayAmount", MaxPayAmount);
            ja.put(jo);

            ReturnCode = "0";
            ReturnMessage = "";
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

        } catch (Exception ex) {
            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //                          Save sales
    ////////////////////////////////////////////////////////////////////////

    /**
     * 儲存免稅品銷售資訊
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject SaveSalesInfo() {

        String ClassName = "Transaction";
        String FunctionName = "SaveSalesInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        ArrayList SQLCommands = new ArrayList();
        String SQL;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //先判斷目前模式為何
            if (this.NowPayMode != PayMode.BALANCE) {
                ReturnCode = "8";
                ReturnMessage = "Not is balance mode.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            // 放大折扣身分卡號大於等於14碼則為CUB, 需加密
            if (UpperLimitDiscountNo.length() >= 14) {
                UpperLimitDiscountNo = AESEncrypDecryp.getEncryptData(UpperLimitDiscountNo, FlightData.AESKey);
            }

            //銷售單頭
            SQL = "INSERT INTO SalesHead(SecSeq,ReceiptNo,SalesTime,RefundTime,OriPrice,TotalPrice," +
                    "UpperLimitType,UpperLimitNo,Status,PreorderNo,IFEOrderNo,SeatNo,AuthuticationFlag,OrderType) " +
                    "VALUES ('" + this.SecSeq +
                    "','" + this.ReceiptNo +
                    "','" + formatter.format(new Date(System.currentTimeMillis())) +
                    "','" + "" +
                    "'," + OriUSDTotalAmount +
                    "," + USDTotalAmount +
                    ",'" + UpperLimitType +
                    "','" + UpperLimitDiscountNo +
                    "','" + "S" +
                    "','" + "" +
                    "','" + this.IFEOrderNo +
                    "','" + this.SeatNo;

            if (this.AuthuticationFlag) {
                SQL += "','" + "Y";
            } else {
                SQL += "','" + "N";
            }

            SQL += "','" + this.OrderType + "')";
            SQLCommands.add(SQL);

            //銷售單身
            int RealSaleQty;  //實際商品銷售量
            for (int i = 0; i < DFSItemList.size(); i++) {

                if (DFSItemList.get(i).GiftFlag.equals("Y")) {

                    if (DFSItemList.get(i).ScanQty != DFSItemList.get(i).SalesQty) {

                        //贈品有差異寫入Log
                        _TSQL.WriteLog(this.SecSeq, "Sales", ClassName, FunctionName, "Receipt no:" + this.ReceiptNo + "- Gift:" + DFSItemList.get(i).ItemCode +
                                "- Stock:" + DFSItemList.get(i).POSStock + "- Sales qty:" + DFSItemList.get(i).SalesQty + "- Scan qty:" + DFSItemList.get(i).ScanQty);

                        //若是Scan量 = 0以及Stock != 0，則不儲存該贈品，僅記錄Log
                        if (DFSItemList.get(i).ScanQty == 0 && DFSItemList.get(i).POSStock != 0) {
                            DFSItemList.remove(i);
                            i--;
                            continue;
                        } else if (DFSItemList.get(i).ScanQty > DFSItemList.get(i).POSStock) {
                            RealSaleQty = DFSItemList.get(i).POSStock;
                        } else {
                            RealSaleQty = DFSItemList.get(i).ScanQty;
                        }
                    } else {
                        RealSaleQty = DFSItemList.get(i).SalesQty;
                    }
                } else {
                    RealSaleQty = DFSItemList.get(i).SalesQty;
                }

                //扣庫存SQL
                SQL = "UPDATE Inventory " +
                        "SET SalesQty = SalesQty + " + RealSaleQty +
                        ", EndQty = EndQty - " + RealSaleQty +
                        " WHERE SecSeq = '" + this.SecSeq +
                        "' AND ItemCode = '" + DFSItemList.get(i).ItemCode + "'";
                SQLCommands.add(SQL);

                //銷售資料轉SQL
                SQL = "INSERT INTO SalesDetail(SecSeq,ReceiptNo,ItemCode,OriPrice,SalesPrice,SalesQty,Discount,VipType,VipNo,Status) " +
                        "VALUES ('" + this.SecSeq +
                        "','" + this.ReceiptNo +
                        "','" + DFSItemList.get(i).ItemCode +
                        "'," + DFSItemList.get(i).OriginalPrice;

                //若有折扣才標記
                if (this.DiscountType != null && this.DiscountType.length() > 0 &&
                        DFSItemList.get(i).DiscountFlag.equals("Y")) {

                    String _No;
                    if (DiscountNo.length() >= 14) {
                        _No = AESEncrypDecryp.getEncryptData(DiscountNo, FlightData.AESKey);
                    } else {
                        _No = DiscountNo;
                    }

                    SQL += "," + Arith.mul(DFSItemList.get(i).USDPrice, this.DiscountRate) +
                            "," + RealSaleQty +
                            "," + this.DiscountRate +
                            ",'" + this.DiscountType +
                            "','" + _No +
                            "','" + "S" + "')";
                } else {

                    SQL += "," + DFSItemList.get(i).USDPrice +
                            "," + RealSaleQty +
                            "," + "1" +
                            ",'" + "" +
                            "','" + "" +
                            "','" + "S" + "')";
                }
                SQLCommands.add(SQL);
            }

            //付款資料轉SQL
            for (int i = 0; i < PaymentList.size(); i++) {
                SQL = "INSERT INTO PaymentInfo(SecSeq,ReceiptNo,PayBy,Currency,Amount,CardType,CardNo,CardName,CardDate,USDAmount,CouponNo,Status,PreorderNo) " +
                        "VALUES ('" + this.SecSeq +
                        "','" + this.ReceiptNo +
                        "','" + PaymentList.get(i).PayBy +
                        "','" + PaymentList.get(i).Currency +
                        "'," + PaymentList.get(i).Amount;

                if (PaymentList.get(i).PayBy.equals("Card")) {
                    SQL += ",'" + PaymentList.get(i).CardType +
                            "','" + AESEncrypDecryp.getEncryptData(PaymentList.get(i).CardNo, FlightData.AESKey) +
                            "','" + PaymentList.get(i).CardName +
                            "','" + AESEncrypDecryp.getEncryptData(PaymentList.get(i).CardDate, FlightData.AESKey);
                } else {
                    SQL += ",'" + "" +
                            "','" + "" +
                            "','" + "" +
                            "','" + "";
                }

                SQL += "'," + PaymentList.get(i).USDAmount +
                        ",'" + PaymentList.get(i).CouponNo +
                        "','" + "S" +
                        "','" + "" + "')";
                SQLCommands.add(SQL);

//                _TSQL.WriteLog(FlightData.SecSeq, LogType.ACTION, "Sale", "",
//                    "Receipt No: " + this.ReceiptNo
//                        + " - Pay By: " + PaymentList.get(i).PayBy
//                        + " - Currency: " + PaymentList.get(i).Currency
//                        + " - Total Amount: " + PaymentList.get(i).Amount
//                        + " - USD Amount: " + PaymentList.get(i).USDAmount
//                );
            }

            //寫入資料
            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Save sales info failed.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //                        Private functions
    ////////////////////////////////////////////////////////////////////////

    /**
     * 取得新的交易單據
     *
     * @return (String) 新的交易單據
     */
    private int GetNewReceiptNo() {

        String ClassName = "Transaction";
        String FunctionName = "GetNewReceiptNo";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        Object obj;

        try {
            //取得新的交易單據號碼
            SQL = "SELECT MAX(CAST(ReceiptNo AS int)) AS ReceiptNo " +
                    "FROM SalesHead ";

            obj = _TSQL.SelectSQLObject(SQL);

            if (obj != null && (int) obj > 0) {
                return (int) obj + 1;
            } else {
                return 1;
            }
        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 檢核是否滿足主動式折扣
     *
     * @param DiscountType 要判斷的折扣代碼
     * @param FuncID       要判斷的主動式條件ID
     * @param Progression  是否可以累計贈送
     * @return 符合的次數，0 不符合， > 0 有符合
     */
    private int CheckFuncID(String DiscountType, String FuncID, Boolean Progression) throws Exception {

        String ClassName = "Transaction";
        String FunctionName = "CheckFuncID";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        try {
            FuncTableRow _FuncTableRow;
            _FuncTableRow = FlightData.GetFuncTable(FuncID);

            //找不到就回傳檢核失敗
            if (_FuncTableRow == null || _FuncTableRow.FuncID.length() == 0) {
                return 0;
            }

            if (DiscountType.equals("IFE") && !canUseIFEDiscount) {
                return 0;
            }

            //根據不同的條件是去判斷比較方式
            int ItemTotalAmt = 0;
            int ItemTotalCount = 0;
            switch (_FuncTableRow.Syntax.split("-")[0].toUpperCase()) {
                case "PIECE":
                    //region 購買滿Item數全面折扣

                    //逐項判斷商品是否可以使用此條件
                    for (int j = 0; j < DFSItemList.size(); j++) {
                        //若可以打折則可計
                        if (!DFSItemList.get(j).DiscountExcptionType.contains(DiscountType) &&
                                DFSItemList.get(j).USDPrice > 0 &&
                                DFSItemList.get(j).TWDPrice > 0) {
                            ItemTotalCount += DFSItemList.get(j).SalesQty;
                        }
                    }

                    //判斷此條件式是否有成立
                    if (ItemTotalCount >= Integer.valueOf(_FuncTableRow.Syntax.split("-")[1])) {
                        //判斷是否還有子條件式
                        if (_FuncTableRow.ChildID.length() > 0)
                        //遞迴呼叫自己
                        {
                            return CheckFuncID(DiscountType, _FuncTableRow.ChildID, Progression);
                        } else {
                            if (Progression)
                            //回傳符合次數
                            {
                                return ItemTotalCount / Integer.valueOf(_FuncTableRow.Syntax.split("-")[1]);
                            } else
                            //不可累送則回1
                            {
                                return 1;
                            }
                        }
                    } else {
                        return 0;
                    }
                    //endregion
                case "SUM":
                    //region 購買滿特定金額全面折扣
                    //逐項判斷商品是否可以使用此條件
                    for (int j = 0; j < DFSItemList.size(); j++) {
                        //若可以打折則可計
                        if (!DFSItemList.get(j).DiscountExcptionType.contains(DiscountType) &&
                                DFSItemList.get(j).USDPrice > 0 &&
                                DFSItemList.get(j).TWDPrice > 0) {
                            ItemTotalAmt += (DFSItemList.get(j).USDPrice * DFSItemList.get(j).SalesQty);
                        }
                    }

                    //判斷此條件式是否有成立
                    if (ItemTotalAmt >= Integer.valueOf(_FuncTableRow.Syntax.split("-")[1])) {
                        //判斷是否還有子條件式
                        if (_FuncTableRow.ChildID.length() > 0)
                        //遞迴呼叫自己
                        {
                            return CheckFuncID(DiscountType, _FuncTableRow.ChildID, Progression);
                        } else {
                            if (Progression)
                            //回傳符合次數
                            {
                                return ItemTotalAmt / Integer.valueOf(_FuncTableRow.Syntax.split("-")[1]);
                            } else
                            //不可累送則回1
                            {
                                return 1;
                            }
                        }
                    } else {
                        return 0;
                    }
                    //endregion
                case "GROUP":
                    switch (_FuncTableRow.Syntax.split("-")[1].toUpperCase()) {
                        case "PIECE":
                            //region 購買滿特定群組數量，該群組商品折扣
                            //Example: GROUP-PIECE-I001-2  (識別碼, 類別, 群組ID, 滿足數量)

                            //取得該群組販售商品數
                            for (int j = 0; j < DFSItemList.size(); j++) {
                                //若可以打折則可計
                                if (DFSItemList.get(j).GroupID != null &&
                                        !DFSItemList.get(j).DiscountExcptionType.contains(DiscountType) &&
                                        DFSItemList.get(j).USDPrice > 0 &&
                                        DFSItemList.get(j).TWDPrice > 0 &&
                                        _FuncTableRow.Syntax.split("-")[2].equals(DFSItemList.get(j).GroupID)) {
                                    ItemTotalCount += DFSItemList.get(j).SalesQty;
                                }
                            }

                            //判斷此條件式是否有成立
                            if (ItemTotalCount >= Integer.valueOf(_FuncTableRow.Syntax.split("-")[3])) {
                                //判斷是否還有子條件式
                                if (_FuncTableRow.ChildID.length() > 0) {
                                    //遞迴呼叫自己，取得最小滿足數
                                    int SubCount = CheckFuncID(DiscountType, _FuncTableRow.ChildID, Progression);

                                    if ((ItemTotalCount / Integer.valueOf(_FuncTableRow.Syntax.split("-")[3])) > SubCount) {
                                        return SubCount;
                                    } else {
                                        return (ItemTotalCount / Integer.valueOf(_FuncTableRow.Syntax.split("-")[3]));
                                    }

                                } else {

                                    if (Progression)
                                    //回傳符合次數
                                    {
                                        return ItemTotalCount / Integer.valueOf(_FuncTableRow.Syntax.split("-")[3]);
                                    } else
                                    //不可累送則回1
                                    {
                                        return 1;
                                    }
                                }
                            } else {
                                return 0;
                            }

                            //endregion
                        case "SUM":
                            //region 購買滿特定群組金額，該群組商品折扣
                            // Example: GROUP-PRICE-I001-200  (識別碼, 類別, 群組ID, 滿足金額)

                            //取得該群組販售商品金額
                            for (int j = 0; j < DFSItemList.size(); j++) {
                                //若可以打折則可計
                                if (DFSItemList.get(j).GroupID != null &&
                                        !DFSItemList.get(j).DiscountExcptionType.contains(DiscountType) &&
                                        DFSItemList.get(j).USDPrice > 0 &&
                                        DFSItemList.get(j).TWDPrice > 0 &&
                                        _FuncTableRow.Syntax.split("-")[2].equals(DFSItemList.get(j).GroupID)) {
                                    ItemTotalAmt += (DFSItemList.get(j).USDPrice * DFSItemList.get(j).SalesQty);
                                }
                            }

                            //判斷此條件式是否有成立
                            if (ItemTotalAmt >= Integer.valueOf(_FuncTableRow.Syntax.split("-")[3])) {
                                //判斷是否還有子條件式
                                if (_FuncTableRow.ChildID.length() > 0) {
                                    //遞迴呼叫自己，取得最小滿足數
                                    int SubCount = CheckFuncID(DiscountType, _FuncTableRow.ChildID, Progression);

                                    if ((ItemTotalAmt / Integer.valueOf(_FuncTableRow.Syntax.split("-")[3])) > SubCount) {
                                        return SubCount;
                                    } else {
                                        return (ItemTotalAmt / Integer.valueOf(_FuncTableRow.Syntax.split("-")[3]));
                                    }

                                } else {

                                    if (Progression)
                                    //回傳符合次數
                                    {
                                        return ItemTotalAmt / Integer.valueOf(_FuncTableRow.Syntax.split("-")[3]);
                                    } else
                                    //不可累送則回1
                                    {
                                        return 1;
                                    }
                                }
                            } else {
                                return 0;
                            }

                            //endregion
                        default:
                            return 0;
                    }
                default:
                    return 0;
            }
        } catch (Exception ex) {
            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());
            throw ex;
        }
    }

    public void setIFEStock(int ifeStock) {
        this.IFEStock = ifeStock;
    }

    public void setIFEOrderNo(String IFEOrderNo) {
        this.IFEOrderNo = IFEOrderNo;
    }
}
