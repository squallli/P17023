package tw.com.regalscan.db;

import android.content.Context;

import com.regalscan.sqlitelibrary.TSQL;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by gabehsu on 2017/4/19.
 */

public class Transfer {

    public Context context = null;
    public String SecSeq;

    private PublicFunctions _PublicFunctions;

    //單據資訊
    private String ReceiptNo;         //交易單據號碼
    private String TransferNo;        //Transfer流水號
    private String CarFrom;           //來源車櫃
    private String CarTo;             //目的車櫃

    //商品清單
    private ArrayList<TransferItem> TransferList;

    /**
     * 初始化Transcation
     */
    public Transfer(Context context, String SecSeq, String CarFrom, String CarTo) throws Exception {

        this.context = context;
        this.SecSeq = SecSeq;
        this.CarFrom = CarFrom;
        this.CarTo = CarTo;
        this.TransferNo = "";
        this.ReceiptNo = String.valueOf(GetNewReceiptNo());

        //實作PublicFunctions
        _PublicFunctions = new PublicFunctions(context, SecSeq);

        //實作DFSItemList
        TransferList = new ArrayList<TransferItem>();
    }

    ////////////////////////////////////////////////////////////////////////
    //                            Basket
    ////////////////////////////////////////////////////////////////////////

    /**
     * 設定目的車櫃
     *
     * @param CarTo         目的車櫃號
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject SetCarTo(String CarTo) {

        String ClassName = "Transfer";
        String FunctionName = "CarTo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "CarTo:" + CarTo);

        try {

            //僅能六碼或是七碼
            if (CarTo.length() != 6 && CarTo.length() != 7) {
                ReturnCode = "8";
                ReturnMessage = "The CartNo format is worng!";
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            this.CarTo = CarTo;

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
     * @param ItemCode       贈品商品編號
     * @param ScanQty        Scan數量
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject SetGiftScanQty(String ItemCode, int ScanQty) {

        String ClassName = "Transfer";
        String FunctionName = "SetGiftScanQty";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "ItemCode:" + ItemCode + " - ScanQty:" + ScanQty);

        try {

            //逐筆找到該贈品
            for (int i =0;i < TransferList.size();i++) {

                if (TransferList.get(i).ItemCode.equals(ItemCode)) {

                    //判斷是不是贈品
                    if (TransferList.get(i).GiftFlag.equals("N")) {
                        ReturnCode = "8";
                        ReturnMessage = "This's not gift.";
                        return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                    }

                    //判斷是不是大於銷售量
                    if (ScanQty > TransferList.get(i).TransferOutQty) {
                        ReturnCode = "8";
                        ReturnMessage = "Scan qty error.";
                        return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                    }

                    //設定Scan的量
                    TransferList.get(i).ScanQty = ScanQty;

                    ReturnCode = "0";
                    ReturnMessage = "";
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }
            }

            ReturnCode = "8";
            ReturnMessage = "Item not found.";
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
     * 新增、調整移儲商品
     *
     * @param ItemCode             商品料號
     * @param AdjustTransferQty    要調整的商品數量 　加商品傳+   減商品傳-
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject ModifyTransferList(String ItemCode, int AdjustTransferQty) {

        String ClassName = "Transfer";
        String FunctionName = "ModifyTransferList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONArray jaItem;

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "ItemCode:" + ItemCode + "-AdjustTransferQty:" + AdjustTransferQty);

        try {

            int OriTransferQty = 0;     //商品移儲量
            int ItemIndex = 0;     //商品列表Index

            //商品數量錯誤
            if (AdjustTransferQty == 0) {
                ReturnCode = "9";
                ReturnMessage = "Transfer qty error.";
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //取得商品資訊
            String SQL = "SELECT AllParts.ItemCode,SerialCode,DrawNo,ItemName, " +
                    "ItemPriceUS AS USDPrice, ItemPriceTW AS TWDPrice, " +
                    " (EndQty - DamageQty) AS POSStock," +
                    "'N' AS GiftFlag, GroupID " +
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
                ReturnMessage = "Gift can't be transfer!";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //取得既有購物車數量
            for (int i = 0; i < TransferList.size(); i++) {

                if (TransferList.get(i).ItemCode.equals(ItemCode)) {
                    OriTransferQty = TransferList.get(i).TransferOutQty;
                    ItemIndex = i;
                }
            }

            //判斷是否有超出POS庫存量
            if ((OriTransferQty + AdjustTransferQty) > jaItem.getJSONObject(0).getInt("POSStock")) {
                ReturnCode = "8";
                ReturnMessage = "Qty not enough: " + jaItem.getJSONObject(0).getInt("SerialCode");

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //更改原先商品銷售列表內的銷售數量
            if (OriTransferQty + AdjustTransferQty < 0) {

                //數量錯誤
                ReturnCode = "8";
                ReturnMessage = "Transfer out qty error.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else if (OriTransferQty + AdjustTransferQty == 0) {

                //刪除購物車
                TransferList.remove(ItemIndex);
            } else if (OriTransferQty == 0) {

                //最多僅可以六個商品
                if (TransferList.size() >= 6) {
                    //數量錯誤
                    ReturnCode = "8";
                    ReturnMessage = "Record must be less then 6.";
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //新增購物車
                TransferItem _TransferItem = new TransferItem();
                _TransferItem.ItemCode = jaItem.getJSONObject(0).getString("ItemCode");
                _TransferItem.SerialCode = jaItem.getJSONObject(0).getString("SerialCode");
                _TransferItem.DrawerNo = jaItem.getJSONObject(0).getString("DrawNo");
                _TransferItem.ItemName = jaItem.getJSONObject(0).getString("ItemName");
                _TransferItem.USDPrice = jaItem.getJSONObject(0).optDouble("USDPrice");
                _TransferItem.TWDPrice = jaItem.getJSONObject(0).optDouble("TWDPrice");
                _TransferItem.POSStock = jaItem.getJSONObject(0).getInt("POSStock");
                _TransferItem.TransferOutQty = AdjustTransferQty;
                _TransferItem.GiftFlag = jaItem.getJSONObject(0).getString("GiftFlag");

                if (!jaItem.getJSONObject(0).isNull("GroupID")) {
                    _TransferItem.GroupID = jaItem.getJSONObject(0).getString("GroupID");
                }

                _TransferItem.DiscountExcptionType = new ArrayList<>();

                //取得該商品不打折的Discount List
                SQL = "SELECT DiscountType " +
                        "FROM DiscountException " +
                        "WHERE ItemCode = '" + _TransferItem.ItemCode + "'";
                jaItem = _TSQL.SelectSQLJsonArray(SQL);

                if (jaItem != null && jaItem.length() > 0) {
                    for (int i = 0;i<jaItem.length();i++) {
                        _TransferItem.DiscountExcptionType.add(jaItem.getJSONObject(i).getString("DiscountType"));
                    }
                }

                TransferList.add(_TransferItem);
            } else {

                //調整購物車
                TransferList.get(ItemIndex).TransferOutQty += AdjustTransferQty;
            }

            //重新計算移儲列表
            return GetTransferList();

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 取回移儲列表內容
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetTransferList() {

        String ClassName = "Transfer";
        String FunctionName = "GetTransferList";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String ReturnCode, ReturnMessage;
        JSONArray jaTransferItemList = new JSONArray(),jaItem;
        JSONObject joTransferItem, ResponseJsonObject;

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //region 逐一檢核每個商品，若為贈品則先刪除
            for (int i = 0; i < TransferList.size(); i++) {

                //若商品為贈品，則刪除
                if (TransferList.get(i).USDPrice == 0) {
                    TransferList.remove(i);
                    i--;
                }
            }
            //endregion

            //region 計算主動式折扣
            int DiscountCount = 0; //滿足的折扣量
            if (FlightData.Discount.size() > 0) {
                //逐筆判斷是否符合該主動式折扣
                for (int i = 0; i < FlightData.Discount.size(); i++) {
                    if (!FlightData.Discount.get(i).FuncID.equals("") &&
                            (DiscountCount = CheckFuncID(FlightData.Discount.get(i).Type,
                                    FlightData.Discount.get(i).FuncID,
                                    FlightData.Discount.get(i).Progression)) > 0) {

                        //折扣是送贈品的
                        if (FlightData.Discount.get(i).DiscountGift.length() > 0) {

                            //先找看看原本的購物清單內有沒有此贈品。
                            Boolean IsDiscounted = false;
                            for (int j = 0; j < TransferList.size(); j++) {
                                if (TransferList.get(j).ItemCode.equals(FlightData.Discount.get(i).DiscountGift)) {
                                    if (TransferList.get(j).TransferOutQty + DiscountCount > TransferList.get(j).POSStock)
                                        //以庫存量為最大贈送量
                                        TransferList.get(j).TransferOutQty = TransferList.get(j).POSStock;
                                    else
                                        //調整贈送量
                                        TransferList.get(j).TransferOutQty = TransferList.get(j).TransferOutQty + DiscountCount;
                                    IsDiscounted = true;
                                    break;
                                }
                            }

                            //沒有在原本的購物清單內，需要新增
                            if (!IsDiscounted) {

                                //取得贈品的存量
                                String SQL = "SELECT AllParts.ItemCode,SerialCode,DrawNo,ItemName, " +
                                        "ItemPriceUS AS USDPrice, ItemPriceTW AS TWDPrice, " +
                                        " (EndQty - DamageQty) AS POSStock, 'Y' AS GiftFlag, GroupID " +
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
                                    //return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                                }
                                else {
                                    //加入到Cart，庫存數量為0也加入，由UI去判斷是否顯示紅字或是粗體字
                                    TransferItem _TransferItem = new TransferItem();
                                    _TransferItem.ItemCode = jaItem.getJSONObject(0).getString("ItemCode");
                                    _TransferItem.SerialCode = jaItem.getJSONObject(0).getString("SerialCode");
                                    _TransferItem.DrawerNo = jaItem.getJSONObject(0).getString("DrawNo");
                                    _TransferItem.ItemName = jaItem.getJSONObject(0).getString("ItemName");
                                    _TransferItem.USDPrice = jaItem.getJSONObject(0).getInt("USDPrice");
                                    _TransferItem.TWDPrice = jaItem.getJSONObject(0).getInt("TWDPrice");
                                    _TransferItem.POSStock = jaItem.getJSONObject(0).getInt("POSStock");

                                    if (DiscountCount > jaItem.getJSONObject(0).getInt("POSStock"))
                                        //以庫存量為最大贈送量
                                        _TransferItem.TransferOutQty = jaItem.getJSONObject(0).getInt("POSStock");
                                    else
                                        //調整贈送量
                                        _TransferItem.TransferOutQty = DiscountCount;

                                    _TransferItem.ScanQty = 0;         //Scan qty預設為0
                                    _TransferItem.GiftFlag = jaItem.getJSONObject(0).getString("GiftFlag");
                                    _TransferItem.GroupID = "";        //先暫放空
                                    _TransferItem.DiscountExcptionType = new ArrayList<>();
                                    TransferList.add(_TransferItem);
                                }
                            }
                        }
                    }
                }
            }
            //endregion

            //將商品列表轉為JSON
            for (int i = 0; i < TransferList.size(); i++) {

                joTransferItem = new JSONObject();
                joTransferItem.put("ItemCode",TransferList.get(i).ItemCode);
                joTransferItem.put("SerialCode",TransferList.get(i).SerialCode);
                joTransferItem.put("DrawerNo",TransferList.get(i).DrawerNo);
                joTransferItem.put("ItemName",TransferList.get(i).ItemName);
                joTransferItem.put("POSStock",TransferList.get(i).POSStock);
                joTransferItem.put("TransferQty",TransferList.get(i).TransferOutQty);
                joTransferItem.put("GiftFlag",TransferList.get(i).GiftFlag);
                jaTransferItemList.put(joTransferItem);
            }

            //將計算結果轉為Response格式
            ResponseJsonObject = new JSONObject();
            ResponseJsonObject.put("ReceiptNo", this.ReceiptNo);
//            ResponseJsonObject.put("TransferNo", this.TransferNo);
            ResponseJsonObject.put("Items", jaTransferItemList);

            ReturnCode = "0";
            ReturnMessage = "";
            JSONArray ReturnData = new JSONArray();
            ReturnData.put(ResponseJsonObject);
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, ReturnData);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName , FunctionName , ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    //                          Save sales
    ////////////////////////////////////////////////////////////////////////

    /**
     * 儲存免稅品移儲資訊
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject SaveTransferInfo() {

        String ClassName = "Transfer";
        String FunctionName = "SaveTransferInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        ArrayList SQLCommands = new ArrayList();
        String SQL,QRCodeData = "";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //若無商品則跳錯
            if (TransferList.size() == 0){
                ReturnCode = "8";
                ReturnMessage = "Item is empty!";
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //取得Transfer No
            this.TransferNo = GetNewTransferNo();

            //車櫃號後三碼 + 時分秒(六碼) + , + 來源酒單(六碼,) + , + 目的酒單(六碼,) + , +  數量(二碼) + Code(七碼) ....接續
            //EX:2A1095735,882957,102809,01XG4122,01XG422
            QRCodeData = this.TransferNo + "," + this.CarFrom + "," + this.CarTo;

            int RealTransferQty = 0;  //實際商品移轉量
            for (int i = 0; i < TransferList.size(); i++) {

                if (TransferList.get(i).GiftFlag.equals("Y")) {

                    if (TransferList.get(i).ScanQty != TransferList.get(i).TransferOutQty) {

                        //贈品有差異寫入Log
                        _TSQL.WriteLog(this.SecSeq, "Transfer", ClassName, FunctionName, "Receipt no:" + this.ReceiptNo + "- Gift:" + TransferList.get(i).ItemCode +
                                "- Stock:" + TransferList.get(i).POSStock + "- Transfer qty:" + TransferList.get(i).TransferOutQty + "- Scan qty:" + TransferList.get(i).ScanQty);

                        //若是Scan量 = 0則不儲存該贈品，僅記錄Log
                        if (TransferList.get(i).ScanQty == 0) {
                            TransferList.remove(i);
                            i--;
                            continue;
                        }
                        else if (TransferList.get(i).ScanQty > TransferList.get(i).POSStock) {
                            RealTransferQty = TransferList.get(i).POSStock;
                        } else {
                            RealTransferQty = TransferList.get(i).ScanQty;
                        }
                    }
                    else
                        RealTransferQty = TransferList.get(i).TransferOutQty;
                }
                else {
                    RealTransferQty = TransferList.get(i).TransferOutQty;
                }

                //加入到QR Code中
                QRCodeData += "," + (_PublicFunctions.PadLeft(String.valueOf(RealTransferQty),2,'0') + TransferList.get(i).ItemCode);

                //逐筆換成SQL Command
                SQL = "UPDATE Inventory " +
                        "SET TransferQty = TransferQty - " + RealTransferQty +
                        " , EndQty = EndQty - " + RealTransferQty +
                        " WHERE ItemCode = '" + TransferList.get(i).ItemCode +
                        "' AND SecSeq = '" + this.SecSeq + "'";
                SQLCommands.add(SQL);

                SQL = "INSERT INTO Transfer (SecSeq,ReceiptNo,TransferNo,ItemCode,CarFrom,CarTo,Qty,TransferType,WorkingTime) " +
                        "VALUES ('" + this.SecSeq +
                        "','" + this.ReceiptNo +
                        "','" + this.TransferNo +
                        "','" + TransferList.get(i).ItemCode +
                        "','" + this.CarFrom +
                        "','" + this.CarTo +
                        "'," + RealTransferQty +
                        ",'" + "OUT" +
                        "','" + formatter.format(new Date(System.currentTimeMillis())) + "')";
                SQLCommands.add(SQL);
            }

            //寫入資料
            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";

                //將計算結果轉為Response格式
                JSONObject ResponseJsonObject = new JSONObject();
                ResponseJsonObject.put("ReceiptNo", this.ReceiptNo);
                ResponseJsonObject.put("TransferNo", this.TransferNo);
                ResponseJsonObject.put("QRData", QRCodeData);
                JSONArray ReturnData = new JSONArray();
                ReturnData.put(ResponseJsonObject);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, ReturnData);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Transfer out error.";

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
    private String GetNewReceiptNo() throws Exception {

        String ClassName = "Transfer";
        String FunctionName = "GetNewReceiptNo";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        try {
            //依據Transger取得單據內容
            SQL = "SELECT MAX(CAST(ReceiptNo AS Integer)) AS ReceiptNo " +
                    "FROM Transfer ";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja == null || ja.getJSONObject(0).isNull("ReceiptNo")) {
                return "1";
            } else {
                return String.valueOf(ja.getJSONObject(0).getInt("ReceiptNo") + 1);
            }
        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得新的Transfer號碼
     *
     * @return (String) 新的交易單據
     */
    private String GetNewTransferNo() {

        String ClassName = "Transfer";
        String FunctionName = "GetNewTransferNo";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        try {
            Date time=new Date(System.currentTimeMillis());
            SimpleDateFormat sdf=new SimpleDateFormat("HHmmss");
            String TransferNo= this.CarFrom.substring(3) + sdf.format(time);

                return TransferNo;
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

        String ClassName = "Transfer";
        String FunctionName = "CheckFuncID";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        try {
            FuncTableRow _FuncTableRow;
            _FuncTableRow = FlightData.GetFuncTable(FuncID);

            //找不到就回傳檢核失敗
            if (_FuncTableRow == null || _FuncTableRow.FuncID.length() == 0) {
                return 0;
            }

            //根據不同的條件是去判斷比較方式
            int ItemTotalAmt = 0;
            int ItemTotalCount = 0;
            switch (_FuncTableRow.Syntax.split("-")[0].toUpperCase()) {
                case "PIECE":
                    //region 購買滿Item數全面折扣

                    //逐項判斷商品是否可以使用此條件
                    for (int j = 0; j < TransferList.size(); j++) {
                        //若可以打折則可計
                        if (!TransferList.get(j).DiscountExcptionType.contains(DiscountType) &&
                                TransferList.get(j).USDPrice > 0 &&
                                TransferList.get(j).TWDPrice > 0) {
                            ItemTotalCount += TransferList.get(j).TransferOutQty;
                        }
                    }

                    //判斷此條件式是否有成立
                    if (ItemTotalCount >= Integer.valueOf(_FuncTableRow.Syntax.split("-")[1])) {
                        //判斷是否還有子條件式
                        if (_FuncTableRow.ChildID.length() > 0)
                            //遞迴呼叫自己
                            return CheckFuncID(DiscountType, _FuncTableRow.ChildID, Progression);
                        else{
                            if (Progression)
                                //回傳符合次數
                                return ItemTotalCount / Integer.valueOf(_FuncTableRow.Syntax.split("-")[1]);
                            else
                                //不可累送則回1
                                return 1;
                        }
                    } else
                        return 0;
                    //endregion
                case "SUM":
                    //region 購買滿特定金額全面折扣

                    //逐項判斷商品是否可以使用此條件
                    for (int j = 0; j < TransferList.size(); j++) {
                        //若可以打折則可計
                        if (!TransferList.get(j).DiscountExcptionType.contains(DiscountType) &&
                                TransferList.get(j).USDPrice > 0 &&
                                TransferList.get(j).TWDPrice > 0) {
                            ItemTotalAmt += (TransferList.get(j).USDPrice * TransferList.get(j).TransferOutQty);
                        }
                    }

                    //判斷此條件式是否有成立
                    if (ItemTotalAmt >= Integer.valueOf(_FuncTableRow.Syntax.split("-")[1])) {
                        //判斷是否還有子條件式
                        if (_FuncTableRow.ChildID.length() > 0)
                            //遞迴呼叫自己
                            return CheckFuncID(DiscountType, _FuncTableRow.ChildID, Progression);
                        else {
                            if (Progression)
                                //回傳符合次數
                                return ItemTotalAmt / Integer.valueOf(_FuncTableRow.Syntax.split("-")[1]);
                            else
                                //不可累送則回1
                                return 1;
                        }
                    } else
                        return 0;
                    //endregion
                case "GROUP":
                    if (_FuncTableRow.Syntax.split("-")[1].toUpperCase().equals("PIECE")) {
                        //region 購買滿特定群組數量，該群組商品折扣
                        //Example: GROUP-PIECE-I001-2  (識別碼, 類別, 群組ID, 滿足數量)

                        //取得該群組販售商品數
                        for (int j = 0; j < TransferList.size(); j++) {
                            //若可以打折則可計
                            if (TransferList.get(j).GroupID != null &&
                                    !TransferList.get(j).DiscountExcptionType.contains(DiscountType) &&
                                    TransferList.get(j).USDPrice > 0 &&
                                    TransferList.get(j).TWDPrice > 0 &&
                                    _FuncTableRow.Syntax.split("-")[2].equals(TransferList.get(j).GroupID)) {
                                ItemTotalCount += TransferList.get(j).TransferOutQty;
                            }
                        }

                        //判斷此條件式是否有成立
                        if (ItemTotalCount >= Integer.valueOf(_FuncTableRow.Syntax.split("-")[3])) {
                            //判斷是否還有子條件式
                            if (_FuncTableRow.ChildID.length() > 0) {
                                //遞迴呼叫自己，取得最小滿足數
                                int SubCount = CheckFuncID(DiscountType, _FuncTableRow.ChildID, Progression);

                                if ((ItemTotalCount / Integer.valueOf(_FuncTableRow.Syntax.split("-")[3])) > SubCount)
                                    return SubCount;
                                else
                                    return (ItemTotalCount / Integer.valueOf(_FuncTableRow.Syntax.split("-")[3]));

                            } else {

                                if (Progression)
                                    //回傳符合次數
                                    return ItemTotalCount / Integer.valueOf(_FuncTableRow.Syntax.split("-")[3]);
                                else
                                    //不可累送則回1
                                    return 1;
                            }
                        } else {
                            return 0;
                        }

                        //endregion
                    } else if (_FuncTableRow.Syntax.split("-")[1].toUpperCase().equals("SUM")) {
                        //region 購買滿特定群組金額，該群組商品折扣
                        // Example: GROUP-PRICE-I001-200  (識別碼, 類別, 群組ID, 滿足金額)

                        //取得該群組販售商品金額
                        for (int j = 0; j < TransferList.size(); j++) {
                            //若可以打折則可計
                            if (TransferList.get(j).GroupID != null &&
                                    !TransferList.get(j).DiscountExcptionType.contains(DiscountType) &&
                                    TransferList.get(j).USDPrice > 0 &&
                                    TransferList.get(j).TWDPrice > 0 &&
                                    _FuncTableRow.Syntax.split("-")[2].equals(TransferList.get(j).GroupID)) {
                                ItemTotalAmt += (TransferList.get(j).USDPrice * TransferList.get(j).TransferOutQty);
                            }
                        }

                        //判斷此條件式是否有成立
                        if (ItemTotalAmt >= Integer.valueOf(_FuncTableRow.Syntax.split("-")[3])) {
                            //判斷是否還有子條件式
                            if (_FuncTableRow.ChildID.length() > 0) {
                                //遞迴呼叫自己，取得最小滿足數
                                int SubCount = CheckFuncID(DiscountType, _FuncTableRow.ChildID, Progression);

                                if ((ItemTotalAmt / Integer.valueOf(_FuncTableRow.Syntax.split("-")[3])) > SubCount)
                                    return SubCount;
                                else
                                    return (ItemTotalAmt / Integer.valueOf(_FuncTableRow.Syntax.split("-")[3]));

                            } else {

                                if (Progression)
                                    //回傳符合次數
                                    return ItemTotalAmt / Integer.valueOf(_FuncTableRow.Syntax.split("-")[3]);
                                else
                                    //不可累送則回1
                                    return 1;
                            }
                        } else {
                            return 0;
                        }

                        //endregion
                    } else
                        return 0;
                default:
                    return 0;
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName , ex.getMessage());
            throw ex;
        }
    }
}
