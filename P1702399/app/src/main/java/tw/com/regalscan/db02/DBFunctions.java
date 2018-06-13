package tw.com.regalscan.db02;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.regalscan.sqlitelibrary.TSQL;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tw.com.regalscan.app.entity.Setting;
import tw.com.regalscan.customClass.PrtData;
import tw.com.regalscan.db.FlightData;

public class DBFunctions {

    private Context context;
    private String SecSeq;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //起始函式
    public DBFunctions(Context context, String SecSeq) {
        this.context = context;
        this.SecSeq = SecSeq;
    }

    public JSONObject GetInventoryPreorderDetail(String SecSeq, String[] PreorderType, boolean isBeginInventory) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        JSONArray details;
        try {
            String SQL = "SELECT PreorderNO, SecSeq, MileDisc, ECouponCurrency, ECoupon, "
                    + "CardType,CardNo, TravelDocument,  CurDvr, PayAmt, Amount, Discount, PNR, PassengerName, "
                    + "PreorderType, SaleFlag, EGASSaleFlag, EVASaleFlag "
                    + "FROM PreorderHead ";

            if (isBeginInventory) {
                SQL += "WHERE ((SecSeq = '" + SecSeq + "') OR (SecSeq <> '" + SecSeq + "' AND SaleFlag <> 'S')) ";
            } else {
                SQL += " WHERE (SaleFlag = 'R' OR SaleFlag = 'N')";
            }

            if (PreorderType != null) {
                switch (PreorderType.length) {
                    case 1:
                        SQL += " AND PreorderType = '" + PreorderType[0] + "'";
                        break;
                    case 2:
                        SQL += " AND (PreorderType = '" + PreorderType[0] + "'"
                                + " OR PreorderType = '" + PreorderType[1] + "')";
                        break;
                    case 3:
                        SQL += " AND (PreorderType = '" + PreorderType[0] + "'"
                                + " OR PreorderType = '" + PreorderType[1] + "'"
                                + " OR PreorderType = '" + PreorderType[2] + "')";
                        break;
                }
            }

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }

            for (int i = 0; i < ja.length(); i++) {
                SQL = "SELECT ItemCode, ItemName, DrawNo, SerialCode, OriginalPrice, SalesPrice, SalesPriceTW, SalesQty FROM PreorderDetail" +
                        " WHERE DrawNo <> '' AND PreorderNo='" + ja
                        .getJSONObject(i).getString("PreorderNO") + "'";

                details = _TSQL.SelectSQLJsonArray(SQL);
                ja.getJSONObject(i).put("Detail", details);
            }

            ReturnCode = "0";
            ReturnMessage = "";

            return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

        } catch (Exception ex) {
            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetProductInfo", ex.getMessage());
            throw ex;
        }
    }

    public JSONObject GetPRVPCanSale(String SecSeq, String PreorderNo, String[] PreorderType, String SalesType) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        JSONArray details;
        try {
            String SQL = "SELECT PreorderNO, SecSeq, MileDisc, ECouponCurrency, ECoupon, "
                    + "CardType,CardNo, TravelDocument,  CurDvr, PayAmt, Amount, Discount, PNR, PassengerName, "
                    + "PreorderType, SaleFlag, EGASSaleFlag, EVASaleFlag "
                    + "FROM PreorderHead ";

            if (SalesType != null) {
                switch (SalesType) {
                    case "S":
                        SQL += " WHERE SaleFlag = 'S'";
                        break;
                    case "N":
                        SQL += " WHERE (SaleFlag = 'R' OR SaleFlag = 'N')";
                        break;
                    case "R":
                        SQL += " WHERE (SaleFlag = 'R') ";
                        break;
                }
            } else {
                SQL += " WHERE (SaleFlag = 'R' OR SaleFlag = 'N' OR SaleFlag = 'S')";
            }

            if (SecSeq != null) {
                SQL += "AND SecSeq ='" + SecSeq + "'";
            }

            if (PreorderNo != null) {
                SQL += " AND PreorderNO = '" + PreorderNo + "'";
            }

            if (PreorderType != null) {
                switch (PreorderType.length) {
                    case 1:
                        SQL += " AND PreorderType = '" + PreorderType[0] + "'";
                        break;
                    case 2:
                        SQL += " AND (PreorderType = '" + PreorderType[0] + "'"
                                + " OR PreorderType = '" + PreorderType[1] + "')";
                        break;
                    case 3:
                        SQL += " AND (PreorderType = '" + PreorderType[0] + "'"
                                + " OR PreorderType = '" + PreorderType[1] + "'"
                                + " OR PreorderType = '" + PreorderType[2] + "')";
                        break;
                }
            }

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }

            for (int i = 0; i < ja.length(); i++) {
                SQL = "SELECT ItemCode, ItemName, DrawNo, SerialCode, OriginalPrice, SalesPrice, SalesPriceTW, SalesQty FROM PreorderDetail" +
                        " WHERE DrawNo <> '' AND PreorderNo='" + ja
                        .getJSONObject(i).getString("PreorderNO") + "'";

                details = _TSQL.SelectSQLJsonArray(SQL);
                ja.getJSONObject(i).put("Detail", details);
            }

            ReturnCode = "0";
            ReturnMessage = "";

            return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

        } catch (Exception ex) {
            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetProductInfo", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 依據條件取得Preorder商品資訊
     *
     * @param PreorderNo   Preorder 單據號碼，不使用時請傳Null
     * @param PreorderType Preorder 單據類別(“PR” or “VS” or “VP”)，不使用請傳Null
     * @param SalesType    S:已取貨，R:是有經過取貨又退貨，N: 未有動作，不使用請傳Null
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetPreorderInfo(String PreorderNo, String[] PreorderType, String SalesType) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        JSONArray details;
        try {

            // PR, VP
            String SQL = "SELECT DISTINCT PreorderHead.PreorderNO AS PreorderNO, "
                    + "PreorderHead.SecSeq AS SecSeq, ReceiptNo, MileDisc, ECouponCurrency, ECoupon, "
                    + "CardType,CardNo, TravelDocument,  CurDvr, PayAmt, Amount, Discount, PNR, PassengerName, "
                    + "PreorderType, PreorderHead.SaleFlag AS SaleFlag, EGASSaleFlag, EVASaleFlag "
                    + "FROM PreorderHead "
                    + "LEFT JOIN PreorderSalesHead "
                    + "ON PreorderHead.SecSeq= PreorderSalesHead.SecSeq "
                    + "AND PreorderHead.PreorderNO= PreorderSalesHead.PreorderNo "
                    + "WHERE 1 = 1 ";

            if (PreorderNo != null) {
                SQL += " AND PreorderHead.PreorderNO = '" + PreorderNo + "' ";
            }

            if (PreorderType != null) {
                switch (PreorderType.length) {
                    case 1:
                        SQL += " AND PreorderType = '" + PreorderType[0] + "' ";
                        break;
                    case 2:
                        SQL += " AND (PreorderType = '" + PreorderType[0] + "' "
                                + " OR PreorderType = '" + PreorderType[1] + "') ";
                        break;
                    case 3:
                        SQL += " AND (PreorderType = '" + PreorderType[0] + "' "
                                + " OR PreorderType = '" + PreorderType[1] + "' "
                                + " OR PreorderType = '" + PreorderType[2] + "' ) ";
                        break;
                }
            }

            if (SalesType != null) {
                if (PreorderType != null) {
                    for (String s : PreorderType) {
                        if (s.equals("VS")) {
                            switch (SalesType) {
                                case "S":
                                    SQL += " AND PreorderHead.SaleFlag = 'S' ";
                                    break;
                                case "N":
                                    SQL += " AND (PreorderHead.SaleFlag = 'R' OR PreorderHead.SaleFlag = 'N') ";
                                    break;
                                case "R":
                                    SQL += " AND (PreorderHead.SaleFlag = 'R') ";
                                    break;
                            }
                        } else if (s.equals("VP") || s.equals("PR")) {
                            switch (SalesType) {
                                case "S":
                                    SQL += " AND PreorderSalesHead.SaleFlag = 'S' ";
                                    break;
                                case "N":
                                    SQL += " AND (PreorderSalesHead.SaleFlag = 'R' OR PreorderHead.SaleFlag = 'N') ";
                                    break;
                                case "R":
                                    SQL += " AND (PreorderSalesHead.SaleFlag = 'R') ";
                                    break;
                            }
                        }
                    }
                } else {
                    switch (SalesType) {
                        case "S":
                            SQL += " AND PreorderSalesHead.SaleFlag = 'S' ";
                            break;
                        case "N":
                            SQL += " AND (PreorderSalesHead.SaleFlag = 'R' OR PreorderHead.SaleFlag = 'N') ";
                            break;
                        case "R":
                            SQL += " AND (PreorderSalesHead.SaleFlag = 'R') ";
                            break;
                    }
                }
            }

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }

            for (int i = 0; i < ja.length(); i++) {
                SQL = "SELECT ItemCode, ItemName, SerialCode, OriginalPrice, SalesPrice, SalesPriceTW, SUM(SalesQty) as SalesQty FROM PreorderDetail" +
                        " WHERE PreorderNo='" + ja.getJSONObject(i).getString("PreorderNO") + "'" +
                        " GROUP BY ItemCode";

                details = _TSQL.SelectSQLJsonArray(SQL);
                ja.getJSONObject(i).put("Detail", details);
            }

            ReturnCode = "0";
            ReturnMessage = "";

            return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

        } catch (Exception ex) {
            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetProductInfo", ex.getMessage());
            throw ex;
        }
    }


    /**
     * 進行EGAS回庫調整
     *
     * @param AdjustArray 要調整的商品Array
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject EGASAdjustItemQty(JSONArray AdjustArray) throws Exception {

        String ReturnCode, ReturnMessage, SQL;
        ArrayList SQLCommands;
        JSONArray ja;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        int OriQty;

        try {
            SQLCommands = new ArrayList();
            for (int i = 0; i < AdjustArray.length(); i++) {
                //先取得原始數量
                SQL = "SELECT EndQty, EGASCheckQty " +
                        "FROM Inventory " +
                        " WHERE ItemCode = '" + AdjustArray.getJSONObject(i).getString("ItemCode") +
                        "' AND SecSeq = '9'";
                ja = _TSQL.SelectSQLJsonArray(SQL);

                //若商品不存在則跳錯
                if (ja.length() == 0 || ja.getJSONObject(0).getString("EndQty").length() == 0) {
                    ReturnCode = "9";
                    ReturnMessage = "Pno code error: " + AdjustArray.getJSONObject(i).getString("ItemCode");
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                OriQty = ja.getJSONObject(0).getInt("EGASCheckQty");

                //檢查不可以將商品調整到負數
                if (OriQty + AdjustArray.getJSONObject(i).getInt("AdjustQty") < 0) {
                    ReturnCode = "9";
                    ReturnMessage = "Qty not enough.";
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //逐筆換成SQL Command
                SQL = "UPDATE Inventory " +
                        "SET EGASCheckQty = EGASCheckQty + " + AdjustArray.getJSONObject(i).getInt("AdjustQty") +
                        " WHERE ItemCode = '" + AdjustArray.getJSONObject(i).getString("ItemCode") +
                        "' AND SecSeq = '9'";
                SQLCommands.add(SQL);

//                SQL = "INSERT INTO Adjust (SecSeq,ItemCode,OldQty,NewQty,CrewID,CrewType,WorkingTime) " +
//                        "VALUES (''" +
//                        "','" + AdjustArray.getJSONObject(i).getString("ItemCode") +
//                        "'," + 0 +
//                        "," + AdjustArray.getJSONObject(i).getInt("AdjustQty") +
//                        ",''" +
//                        "','" + "EGAS" +
//                        "','" + formatter.format(new Date(System.currentTimeMillis())) + "')";
//                SQLCommands.add(SQL);
            }

            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "9";
                ReturnMessage = "Adjust failed.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "EGASAdjustItemQty", ex.getMessage());
            throw ex;
        }
    }

    public JSONObject EGASAdjustItemQty(String itemCode,int stockQty) throws Exception {

        String ReturnCode, ReturnMessage, SQL;
        ArrayList SQLCommands;
        JSONArray ja;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        int OriQty;

        try {
            SQLCommands = new ArrayList();

                //先取得原始數量
                SQL = "SELECT EndQty, EGASCheckQty " +
                        "FROM Inventory " +
                        " WHERE ItemCode = '" + itemCode +
                        "' AND SecSeq = '9'";
                ja = _TSQL.SelectSQLJsonArray(SQL);

                //若商品不存在則跳錯
                if (ja.length() == 0 || ja.getJSONObject(0).getString("EndQty").length() == 0) {
                    ReturnCode = "9";
                    ReturnMessage = "Pno code error: " + itemCode;
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                OriQty = ja.getJSONObject(0).getInt("EGASCheckQty");

                //檢查不可以將商品調整到負數
                if (OriQty + stockQty < 0) {
                    ReturnCode = "9";
                    ReturnMessage = "Qty not enough.";
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //逐筆換成SQL Command
                SQL = "UPDATE Inventory " +
                        "SET EGASCheckQty = " +stockQty +
                        " WHERE ItemCode = '" +itemCode +"'";

                SQLCommands.add(SQL);

//                SQL = "INSERT INTO Adjust (SecSeq,ItemCode,OldQty,NewQty,CrewID,CrewType,WorkingTime) " +
//                        "VALUES (''" +
//                        "','" + AdjustArray.getJSONObject(i).getString("ItemCode") +
//                        "'," + 0 +
//                        "," + AdjustArray.getJSONObject(i).getInt("AdjustQty") +
//                        ",''" +
//                        "','" + "EGAS" +
//                        "','" + formatter.format(new Date(System.currentTimeMillis())) + "')";
//                SQLCommands.add(SQL);


            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "9";
                ReturnMessage = "Adjust failed.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "EGASAdjustItemQty", ex.getMessage());
            throw ex;
        }
    }

    public JSONObject EVAAdjustItemQty(String itemCode,int stockQty) throws Exception {

        String ReturnCode, ReturnMessage, SQL;
        ArrayList SQLCommands;
        JSONArray ja;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        int OriQty;

        try {
            SQLCommands = new ArrayList();

            //先取得原始數量
            SQL = "SELECT EndQty, EVACheckQty " +
                    "FROM Inventory " +
                    " WHERE ItemCode = '" + itemCode +
                    "' AND SecSeq = '9'";
            ja = _TSQL.SelectSQLJsonArray(SQL);

            //若商品不存在則跳錯
            if (ja.length() == 0 || ja.getJSONObject(0).getString("EndQty").length() == 0) {
                ReturnCode = "9";
                ReturnMessage = "Pno code error: " + itemCode;
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            OriQty = ja.getJSONObject(0).getInt("EVACheckQty");

            //檢查不可以將商品調整到負數
            if (OriQty + stockQty < 0) {
                ReturnCode = "9";
                ReturnMessage = "Qty not enough.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //逐筆換成SQL Command
            SQL = "UPDATE Inventory " +
                    "SET EVACheckQty = " +stockQty +
                    " WHERE ItemCode = '" +itemCode +"'";

            SQLCommands.add(SQL);

//                SQL = "INSERT INTO Adjust (SecSeq,ItemCode,OldQty,NewQty,CrewID,CrewType,WorkingTime) " +
//                        "VALUES (''" +
//                        "','" + AdjustArray.getJSONObject(i).getString("ItemCode") +
//                        "'," + 0 +
//                        "," + AdjustArray.getJSONObject(i).getInt("AdjustQty") +
//                        ",''" +
//                        "','" + "EGAS" +
//                        "','" + formatter.format(new Date(System.currentTimeMillis())) + "')";
//                SQLCommands.add(SQL);


            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "9";
                ReturnMessage = "Adjust failed.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "EVAAdjustItemQty", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 進行EVA回庫調整
     *
     * @param AdjustArray 要調整的商品Array
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject EVAAdjustItemQty(JSONArray AdjustArray) throws Exception {

        String ReturnCode, ReturnMessage, SQL;
        ArrayList SQLCommands;
        JSONArray ja;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        int OriQty;

        try {
            SQLCommands = new ArrayList();
            for (int i = 0; i < AdjustArray.length(); i++) {
                //先取得原始數量
                SQL = "SELECT EndQty, EVACheckQty " +
                        "FROM Inventory " +
                        " WHERE ItemCode = '" + AdjustArray.getJSONObject(i).getString("ItemCode") +
                        "' AND SecSeq = '9'";
                ja = _TSQL.SelectSQLJsonArray(SQL);

                //若商品不存在則跳錯
                if (ja == null || ja.length() == 0 || ja.getJSONObject(0).getString("EndQty").length() == 0) {
                    ReturnCode = "9";
                    ReturnMessage = "Pno code error: " + AdjustArray.getJSONObject(i).getString("ItemCode");
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                OriQty = ja.getJSONObject(0).getInt("EVACheckQty");

                //檢查不可以將商品調整到負數
                if (OriQty + AdjustArray.getJSONObject(i).getInt("AdjustQty") < 0) {
                    ReturnCode = "9";
                    ReturnMessage = "Qty not enough.";
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //逐筆換成SQL Command
                SQL = "UPDATE Inventory " +
                        "SET EVACheckQty = EVACheckQty + " + AdjustArray.getJSONObject(i).getInt("AdjustQty") +
                        " WHERE ItemCode = '" + AdjustArray.getJSONObject(i).getString("ItemCode") +
                        "' AND SecSeq = '9'";
                SQLCommands.add(SQL);

//                SQL = "INSERT INTO Adjust (SecSeq,ItemCode,OldQty,NewQty,CrewID,CrewType,WorkingTime) " +
//                        "VALUES (''" +
//                        "','" + AdjustArray.getJSONObject(i).getString("ItemCode") +
//                        "'," + 0 +
//                        "," + AdjustArray.getJSONObject(i).getInt("AdjustQty") +
//                        ",''" +
//                        "','" + "EVA" +
//                        "','" + formatter.format(new Date(System.currentTimeMillis())) + "')";
//                SQLCommands.add(SQL);
            }

            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "9";
                ReturnMessage = "Adjust failed.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "EVAAdjustItemQty", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 進行 EGAS Damage調整
     *
     * @param DamageArray 要調整商品Array
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject EGASDamageItemQty(JSONArray DamageArray) throws Exception {

        String ReturnCode, ReturnMessage, SQL;
        ArrayList SQLCommands;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONArray ja;

        try {
            //有需要調整的商品，DamageQty數量都會是正數， 因為不扣庫存量
            SQLCommands = new ArrayList();
            for (int i = 0; i < DamageArray.length(); i++) {
                //先取得原始數量，
                SQL = "SELECT ItemCode, EGASCheckQty, EGASDamageQty " +
                        "FROM Inventory " +
                        " WHERE ItemCode = '" + DamageArray.getJSONObject(i).getString("ItemCode") +
                        "' AND SecSeq = '9'";
                ja = _TSQL.SelectSQLJsonArray(SQL);

                //若商品不存在則跳錯
                if (ja == null || ja.length() == 0 || ja.getJSONObject(0).getString("ItemCode").length() == 0) {
                    ReturnCode = "9";
                    ReturnMessage = "Pno code error: " + DamageArray.getJSONObject(i).getString("ItemCode");
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //若Damage商品數量大於庫存量，或更改後數量小於0則不允許
                if ((ja.getJSONObject(0).getInt("EGASDamageQty") + DamageArray.getJSONObject(i).getInt("DamageQty") > ja.getJSONObject(0)
                        .getInt("EGASCheckQty")) || (ja.getJSONObject(0).getInt("EGASDamageQty") + DamageArray.getJSONObject(i).getInt("DamageQty") < 0)) {

                    ReturnCode = "9";
                    ReturnMessage = "Damage qty error.";
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //逐筆換成SQL Command
                SQL = "UPDATE Inventory " +
                        "SET EGASDamageQty  = EGASDamageQty + " + DamageArray.getJSONObject(i).getInt("DamageQty") +
                        " WHERE ItemCode = '" + DamageArray.getJSONObject(i).getString("ItemCode") +
                        "' AND SecSeq = '9'";
                SQLCommands.add(SQL);
            }

            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "9";
                ReturnMessage = "Adjust failed.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "EGASDamageItemQty", ex.getMessage());
            throw ex;
        }
    }

    public JSONObject EGASDamageItemQty(String itemCode,int damageQty) throws Exception {

        String ReturnCode, ReturnMessage, SQL;
        ArrayList SQLCommands;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONArray ja;

        try {
            //有需要調整的商品，DamageQty數量都會是正數， 因為不扣庫存量
            SQLCommands = new ArrayList();

                //先取得原始數量，
                SQL = "SELECT ItemCode, EGASCheckQty, EGASDamageQty " +
                        "FROM Inventory " +
                        " WHERE ItemCode = '" + itemCode +"'";
//                        "' AND SecSeq = '9'";
                ja = _TSQL.SelectSQLJsonArray(SQL);

                //若商品不存在則跳錯
                if (ja == null || ja.length() == 0 || itemCode.length() == 0) {
                    ReturnCode = "9";
                    ReturnMessage = "Pno code error: " + itemCode;
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //若Damage商品數量大於庫存量，或更改後數量小於0則不允許
                if ((ja.getJSONObject(0).getInt("EGASDamageQty") + damageQty > ja.getJSONObject(0)
                        .getInt("EGASCheckQty")) || (ja.getJSONObject(0).getInt("EGASDamageQty") + damageQty < 0)) {

                    ReturnCode = "9";
                    ReturnMessage = "Damage qty error.";
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //逐筆換成SQL Command
                SQL = "UPDATE Inventory " +
                        "SET EGASDamageQty  =  " + damageQty +
                        " WHERE ItemCode = '" + itemCode +
                        "' AND SecSeq = '9'";
                SQLCommands.add(SQL);
            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "9";
                ReturnMessage = "Adjust failed.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {
            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "EGASDamageItemQty", ex.getMessage());
            throw ex;
        }

    }

    public JSONObject EVADamageItemQty(String itemCode,int damageQty) throws Exception {

        String ReturnCode, ReturnMessage, SQL;
        ArrayList SQLCommands;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONArray ja;

        try {
            //有需要調整的商品，DamageQty數量都會是正數， 因為不扣庫存量
            SQLCommands = new ArrayList();

            //先取得原始數量，
            SQL = "SELECT ItemCode, EVACheckQty, EVADamageQty " +
                    "FROM Inventory " +
                    " WHERE ItemCode = '" + itemCode +"'";;
//                    "' AND SecSeq = '9'";
            ja = _TSQL.SelectSQLJsonArray(SQL);

            //若商品不存在則跳錯
            if (ja == null || ja.length() == 0 || itemCode.length() == 0) {
                ReturnCode = "9";
                ReturnMessage = "Pno code error: " + itemCode;
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //若Damage商品數量大於庫存量，或更改後數量小於0則不允許
            if ((ja.getJSONObject(0).getInt("EVADamageQty") + damageQty > ja.getJSONObject(0)
                    .getInt("EVACheckQty")) || (ja.getJSONObject(0).getInt("EVADamageQty") + damageQty < 0)) {

                ReturnCode = "9";
                ReturnMessage = "Damage qty error.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //逐筆換成SQL Command
            SQL = "UPDATE Inventory " +
                    "SET EVADamageQty  =  " + damageQty +
                    " WHERE ItemCode = '" + itemCode +
                    "' AND SecSeq = '9'";
            SQLCommands.add(SQL);
            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "9";
                ReturnMessage = "Adjust failed.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {
            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "EVADamageItemQty", ex.getMessage());
            throw ex;
        }

    }

    /**
     * 進行 EVA Damage調整
     *
     * @param DamageArray 要調整商品Array
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject EVADamageItemQty(JSONArray DamageArray) throws Exception {

        String ReturnCode, ReturnMessage, SQL;
        ArrayList SQLCommands;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONArray ja;

        try {
            //有需要調整的商品，DamageQty數量都會是正數， 因為不扣庫存量
            SQLCommands = new ArrayList();
            for (int i = 0; i < DamageArray.length(); i++) {
                //先取得原始數量，
                SQL = "SELECT ItemCode, EVACheckQty, EVADamageQty " +
                        "FROM Inventory " +
                        " WHERE ItemCode = '" + DamageArray.getJSONObject(i).getString("ItemCode") +
                        "' AND SecSeq = '9'";
                ja = _TSQL.SelectSQLJsonArray(SQL);

                //若商品不存在則跳錯
                if (ja == null || ja.length() == 0 || ja.getJSONObject(0).getString("ItemCode").length() == 0) {
                    ReturnCode = "9";
                    ReturnMessage = "Pno code error: " + DamageArray.getJSONObject(i).getString("ItemCode");
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //若Damage商品數量大於庫存量，或更改後數量小於0則不允許
                if ((ja.getJSONObject(0).getInt("EVADamageQty") + DamageArray.getJSONObject(i).getInt("DamageQty") > ja.getJSONObject(0)
                        .getInt("EVACheckQty")) || (ja.getJSONObject(0).getInt("EVADamageQty") + DamageArray.getJSONObject(i).getInt("DamageQty") < 0)) {

                    ReturnCode = "9";
                    ReturnMessage = "Damage qty error.";
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //逐筆換成SQL Command
                SQL = "UPDATE Inventory " +
                        "SET EVADamageQty  = EVADamageQty + " + DamageArray.getJSONObject(i).getInt("DamageQty") +
                        " WHERE ItemCode = '" + DamageArray.getJSONObject(i).getString("ItemCode") +
                        "' AND SecSeq = '9'";
                SQLCommands.add(SQL);
            }

            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "9";
                ReturnMessage = "Adjust failed.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "EVADamageItemQty", ex.getMessage());
            throw ex;
        }
    }


    /**
     * 儲存Preorder資訊
     *
     * @param PreorderInfo Preorder銷售資訊
     * @return boolean
     */
    public JSONObject SavePreorderInfo(JSONObject PreorderInfo) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String PreorderNO, PreorderType, SaleFlag, VerifyType;
        String SQL, SQL02;
        JSONArray jaItem;

        try {
            PreorderNO = PreorderInfo.getString("PreorderNo");
            PreorderType = PreorderInfo.getString("PreorderType");
            VerifyType = PreorderInfo.getString("VerifyType");

            // 以PreorderNo取得PreorderHead內的單子
            SQL = "SELECT PreorderNO, SaleFlag FROM PreorderHead WHERE PreorderNO='" + PreorderNO +
                    "' AND PreorderType='" + PreorderType + "'";
            jaItem = _TSQL.SelectSQLJsonArray(SQL);

            //若單據號碼不存在則跳錯
            if (jaItem == null || jaItem.length() == 0 || jaItem.getJSONObject(0).getString("PreorderNO").length() == 0) {
                ReturnCode = "9";
                ReturnMessage = "No such PreorderNo: " + PreorderNO + ", Type: " + PreorderType;
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            // SalesType S:已取貨，R:說R是有經過取貨又退貨，N: 未有動作
            SaleFlag = jaItem.getJSONObject(0).getString("SaleFlag");

            // 若狀態不為可以取貨
            if (SaleFlag.equals("S")) {
                ReturnCode = "9";
                ReturnMessage = "Can't sale" + PreorderNO;
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            // 產生新的流水號ReceiptNo
            String ss = "";
            if (PreorderType.equals("PR")) {
                ss = "P";
            } else if (PreorderType.equals("VP")) {
                ss = "V";
            }
            SQL = "SELECT IFNULL(MAX(CAST(SUBSTR(ReceiptNo,2,3) AS INT)), 0 ) + 1 AS NewMaxNo "
                    + "FROM PreorderSalesHead "
                    + "WHERE SUBSTR(ReceiptNo,1,1) == '" + ss + "'";
            jaItem = _TSQL.SelectSQLJsonArray(SQL);

            if (jaItem.length() == 0 || jaItem.getJSONObject(0).getString("NewMaxNo").length() == 0) {
                ReturnCode = "9";
                ReturnMessage = "Create New ReceiptNo Failed";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            ss = jaItem.getJSONObject(0).getString("NewMaxNo");
            // 判斷長度，將收據編號補到2位數
            if (ss.length() == 1) {
                if (PreorderType.equals("PR")) {
                    ss = "P0" + ss;
                } else if (PreorderType.equals("VP")) {
                    ss = "V0" + ss;
                }
            } else {
                if (PreorderType.equals("PR")) {
                    ss = "P" + ss;
                } else if (PreorderType.equals("VP")) {
                    ss = "V" + ss;
                }
            }
            // 產生銷貨時間
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 1. update PreorderHead 的SalesFlag
            // 2. update PreorderSalesHead 的SalesFlag 與流水號ReceiptNo
            if (PreorderType.equals("PR")) {
                // PR: 將SalesType改成S, VerifyType改成(P or C)
                SQL = "UPDATE PreorderHead SET SaleFlag='S', EVASaleFlag = 'S' "
                        + "WHERE PreorderNO='" + PreorderNO + "' AND PreorderType='PR'";
            } else if (PreorderType.equals("VP")) {
                // VP: 將SalesType改成S, VerifyType改成("")
                SQL = "UPDATE PreorderHead SET SaleFlag='S', EVASaleFlag = 'S' "
                        + "WHERE PreorderNO='" + PreorderNO + "' AND PreorderType='VP'";
            }

            SQL02 = "INSERT INTO PreorderSalesHead( SecSeq, ReceiptNo, SalesTime, PreorderNo, SaleFlag, VerifyType ) "
                    + "VALUES ( "
                    + "'" + this.SecSeq + "', "
                    + "'" + ss + "', "
                    + "'" + formatter.format(new Date(System.currentTimeMillis())) + "', "
                    + "'" + PreorderNO + "', "
                    + "'S', "
                    + "'" + VerifyType + "' ) ";

            if (_TSQL.ExecutesSQLCommand(SQL) && _TSQL.ExecutesSQLCommand(SQL02)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "9";
                ReturnMessage = "Adjust failed.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "SavePreorderInfo", ex.getMessage());
            throw ex;
        }

    }


    /**
     * 退貨Preorder資訊
     *
     * @param PreorderInfo Preorder銷售資訊
     * @return boolean
     */
    public JSONObject RefundPreorderInfo(JSONObject PreorderInfo) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String PreorderNO, PreorderType;
        String SQL, SQL02;
        JSONArray jaItem;

        try {
            PreorderNO = PreorderInfo.getString("PreorderNo");
            PreorderType = PreorderInfo.getString("PreorderType");

            // 以PreorderNo取得PreorderHead內的單子
            SQL = "SELECT PreorderNO, SaleFlag FROM PreorderHead WHERE PreorderNO='" + PreorderNO +
                    "' AND PreorderType='" + PreorderType + "'";
            jaItem = _TSQL.SelectSQLJsonArray(SQL);

            //若單據號碼不存在則跳錯
            if (jaItem.length() == 0 || jaItem.getJSONObject(0).getString("PreorderNO").length() == 0) {
                ReturnCode = "9";
                ReturnMessage = "No such PreorderNo: " + PreorderNO + ", Type: " + PreorderType;
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            // 產生退貨時間
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // PR, VP: HistoryRefund 和 PaymentList傳空
            // 1. 將 PreorderHead 的狀態改為退貨
            SQL = "UPDATE PreorderHead SET SaleFlag='R', EVASaleFlag = 'R' WHERE PreorderNO='" + PreorderNO +
                    "' AND PreorderType='" + PreorderType + "'";

            // 2. 將 PreorderSalesHead 的狀態改為退貨，並記錄退貨時間
            SQL02 = "UPDATE PreorderSalesHead "
                    + "SET RefundTime ='" + formatter.format(new Date(System.currentTimeMillis()))
                    + "', SaleFlag= 'R' "
                    + "WHERE SecSeq='" + this.SecSeq + "' AND PreorderNo= '" + PreorderNO + "' ";

            if (_TSQL.ExecutesSQLCommand(SQL) && _TSQL.ExecutesSQLCommand(SQL02)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "9";
                ReturnMessage = "Save failed.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "RefundPreorderInfo", ex.getMessage());
            throw ex;
        }

    }


    /**
     * EGAS儲存Preorder狀態
     *
     * @param PreorderNO Preorder銷售資訊
     * @param SalesType  S = 已被取貨， N 未被取貨
     * @return boolean
     */
    public JSONObject EGASSavePreorderState(String PreorderNO, String SalesType) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        ArrayList SQLCommands = new ArrayList();
        String SQL;
        JSONArray jaItem;

        try {
            // 以PreorderNo取得PreorderHead內的單子
            SQL = "SELECT PreorderNO, EGASSaleFlag FROM PreorderHead WHERE PreorderNO='" + PreorderNO + "'";
            jaItem = _TSQL.SelectSQLJsonArray(SQL);

            //若單據號碼不存在則跳錯l
            if (jaItem == null || jaItem.length() == 0 || jaItem.getJSONObject(0).getString("PreorderNO").length() == 0) {
                ReturnCode = "9";
                ReturnMessage = "No such PreorderNo: " + PreorderNO;
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            SQL = "UPDATE PreorderHead SET EGASSaleFlag='" + SalesType + "' WHERE PreorderNO='" + PreorderNO + "'";
            SQLCommands.add(SQL);

            if (_TSQL.ExecutesSQLCommand(SQL)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "9";
                ReturnMessage = "Save failed.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {
            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "EGASSavePreorderState", ex.getMessage());
            throw ex;
        }
    }


    /**
     * EVA儲存Preorder狀態
     *
     * @param PreorderNO Preorder銷售資訊
     * @param SalesType  S = 已被取貨， N 未被取貨
     * @return boolean
     */
    public JSONObject EVASavePreorderState(String PreorderNO, String SalesType) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray jaItem;

        try {
            // 以PreorderNo取得PreorderHead內的單子
            SQL = "SELECT PreorderNO, EVASaleFlag FROM PreorderHead WHERE PreorderNO='" + PreorderNO + "'";
            jaItem = _TSQL.SelectSQLJsonArray(SQL);

            //若單據號碼不存在則跳錯
            if (jaItem == null || jaItem.length() == 0 || jaItem.getJSONObject(0).getString("PreorderNO").length() == 0) {
                ReturnCode = "9";
                ReturnMessage = "No such PreorderNo: " + PreorderNO;
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            SQL = "UPDATE PreorderHead SET EVASaleFlag='" + SalesType + "' WHERE PreorderNO='" + PreorderNO + "'";

            if (_TSQL.ExecutesSQLCommand(SQL)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "9";
                ReturnMessage = "Save failed.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {
            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "EVASavePreorderState", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得升艙等商品資訊
     */
    public JSONObject GetUpgradeProductInfo() throws Exception {

        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        try {
            //取得升艙等商品資訊
            String SQL = "SELECT * FROM ClassAllParts";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("OriginalClass")) {

                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

            } else {
                ReturnCode = "9";
                ReturnMessage = "Get Upgrade Class list error.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetUpgradeProductInfo", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得VIP Type "CUSS"
     */
    public JSONObject GetCUSSInfo(String secSeq) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray ja;
        try {
            SQL = "SELECT SalesHead.ReceiptNo FROM SalesHead LEFT JOIN SalesDetail ON SalesHead.ReceiptNo = SalesDetail.ReceiptNo "
                    + "WHERE VIPType LIKE '%CUSS%' AND TotalPrice > 0 AND SalesHead.SecSeq = '" + secSeq + "' ";

            ja = _TSQL.SelectSQLJsonArray(SQL);
            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("ReceiptNo")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "9";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
        } catch (Exception ex) {
            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetCUSSInfo", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得各商品銷售數量、銷售總金額
     */
    public JSONObject GetSalesTotalSummary(String secSeq) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray ja;
        try {
            SQL =
                    "SELECT AllParts.SerialCode, SalesDetail.ItemCode, AllParts.ItemName, sum(SalesDetail.SalesQty) as TotalQty, sum(SalesDetail.SalesPrice * "
                            + "SalesDetail.SalesQty) as TotalPrice FROM SalesDetail inner join SalesHead on SalesDetail.ReceiptNo = SalesHead.ReceiptNo  left join "
                            + "AllParts on  SalesDetail.ItemCode = AllParts.ItemCode  where  SalesHead.PreOrderNo='' and SalesDetail.Status='S' and SalesHead.SecSeq = '"
                            + secSeq + "' group by SalesDetail.Itemcode, Allparts.serialcode, AllParts.ItemName ORDER BY AllParts.SerialCode";

            ja = _TSQL.SelectSQLJsonArray(SQL);
            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("ItemCode")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "9";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
        } catch (Exception ex) {
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetSalesTotalSummary", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得所有付款資訊
     */
    public JSONObject GetPaymentInfo(String secSeq, String PreOrderNo) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray ja;

        try {
            SQL = "SELECT Currency, Amount, PayBy, CardNo, CardType, CardName, CardDate, USDAmount, ReceiptNo FROM PaymentInfo WHERE SecSeq = '" + secSeq
                    + "' AND PreOrderNo = '" + PreOrderNo + "' ORDER BY PayBy";
            ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("Amount")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "9";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }
        } catch (Exception ex) {
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetPaymentInfo", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得該航段Coupon收受匯總(列印用)
     *
     * @param Coupontype Coupon類型, 取得全部傳null
     */
    public JSONObject GetCouponInfoSummary(String Coupontype) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray ja;

        try {
            if (Coupontype.equals("SC")) {
                SQL = "SELECT SalesHead.ReceiptNo, SUM (Amount) as Amount, CouponNo "
                        + "FROM PaymentInfo "
                        + "LEFT JOIN SalesHead "
                        + "ON SalesHead.ReceiptNo= PaymentInfo.ReceiptNo "
                        + "WHERE SalesHead.SecSeq = '" + SecSeq + "' "
                        + "AND PayBy = 'SC' AND Amount > 0 AND Currency = 'TWD' "
                        + "AND SalesHead.Status='S' "
                        + "AND PaymentInfo.Status='S' "
                        + "GROUP BY SalesHead.ReceiptNo ORDER BY CAST(SalesHead.ReceiptNo AS INT) ";
            } else {
                SQL = "SELECT SalesHead.ReceiptNo, Amount, CouponNo "
                        + "FROM PaymentInfo "
                        + "LEFT JOIN SalesHead "
                        + "ON SalesHead.ReceiptNo= PaymentInfo.ReceiptNo "
                        + "WHERE SalesHead.SecSeq = '" + SecSeq + "' "
                        + "AND PayBy = 'DC' AND Amount > 0 AND Currency = 'USD' "
                        + "AND SalesHead.Status='S' "
                        + "AND PaymentInfo.Status='S' "
                        + "ORDER BY CAST(SalesHead.ReceiptNo AS INT), CAST(Amount AS INT) ";
            }

            ja = _TSQL.SelectSQLJsonArray(SQL);
            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("ReceiptNo")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "9";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }
        } catch (Exception ex) {
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetCouponInfoSummary", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 統計各種貨幣的銷售現金金額。(找錢時，有的貨幣可能出現負數。)
     */
    public JSONObject GetCashTotalAmt(String PaymentCurrency) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray ja;

        try {
//            SQL = "SELECT SUM(case PayBy WHEN 'Change' Then (0 - Amount) "
//                + "WHEN 'Refund' Then (0 - Amount) ELSE Amount END) AS Amount "
//                + "FROM PaymentInfo "
//                + "LEFT JOIN SalesHead "
//                + "ON SalesHead.ReceiptNo= PaymentInfo.ReceiptNo "
//                + "WHERE Currency = '" + PaymentCurrency + "' "
//                + "AND SalesHead.SecSeq = '" + this.SecSeq + "' "
//                + "AND PayBy IN ('Change','Cash','Refund') ";
            SQL = "SELECT SUM(case WHEN (PaymentInfo.Status = 'S' and payby = 'Change') Then (0 - Amount) "
                    + "WHEN (PaymentInfo.Status = 'R' and payby <> 'Change') Then (0 - Amount)"
                    + "WHEN (PaymentInfo.Status = 'R' and payby = 'Change') Then Amount ELSE Amount END) AS Amount "
                    + "FROM PaymentInfo "
                    + "LEFT JOIN SalesHead "
                    + "ON SalesHead.ReceiptNo= PaymentInfo.ReceiptNo "
                    + "WHERE Currency = '" + PaymentCurrency + "' "
                    + "AND SalesHead.SecSeq = '" + this.SecSeq + "' "
                    + "AND PayBy IN ('Change','Cash','Refund') ";
            ja = _TSQL.SelectSQLJsonArray(SQL);
            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("Amount")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "9";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }
        } catch (Exception ex) {
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetCashTotalAmt", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得PreOrder各卡別及幣別金額
     */
    public JSONObject GetPreOrderSummary() throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray ja;

        try {
            SQL = "SELECT CardType, SUM(Amount) as Amount, CurDvr FROM PreorderHead WHERE SaleFlag = 'S' AND SecSeq = '" + this.SecSeq + "' GROUP BY CardType, CurDvr";
            ja = _TSQL.SelectSQLJsonArray(SQL);
            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("Amount")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "9";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }
        } catch (Exception ex) {
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetPreOrderSummary", ex.getMessage());
            throw ex;
        }
    }

    public JSONObject GetUpgradeSummary() throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray ja;

        try {
            SQL = "SELECT OriginalClass, NewClass, SUM(SalesQty) AS TotalQty FROM ClassSalesDetail WHERE Status = 'S' AND SecSeq = '"
                    + this.SecSeq + "' GROUP BY OriginalClass, NewClass";
            ja = _TSQL.SelectSQLJsonArray(SQL);
            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("TotalQty")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "9";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }
        } catch (Exception ex) {
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetPreOrderSummary", ex.getMessage());
            throw ex;
        }
    }


    /**
     * 取得所有單據編號與種類
     *
     * @param SalesStatus 銷售狀態(’Sale’, ’Refund’)，不使用則傳Null
     * @return boolean
     */
    public JSONObject GetAllReceiptNo(String SalesStatus, String SecSeq, boolean GetRefundReceiptCanPrint) throws Exception {

        String SQL;
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        StringBuilder sb = new StringBuilder();

        try {
            // 篩選銷售狀態
            String SaleFlag = " ";
            if (SalesStatus == null) {
                SalesStatus = " ";
                SaleFlag = " ";
            } else if (SalesStatus.equals("Sale")) {
                SalesStatus = " AND Status = 'S' ";
                SaleFlag = "S";
            } else if (SalesStatus.equals("Refund")) {
                SalesStatus = " AND Status = 'R' ";
                SaleFlag = "R";
            }

            // 分辨是否要取Report內Sale Refund的補印單據
            String PreorderString;
            if (GetRefundReceiptCanPrint) {
                PreorderString =
                        "UNION "
                                + "SELECT PreorderSalesHead.SecSeq AS SecSeq, ReceiptNo, PreorderHead.PreorderNO AS PreorderNo, "
                                + "PreorderSalesHead.SaleFlag AS SaleFlag, 'PR' AS Type, 1 AS SerialNo "
                                + "FROM PreorderSalesHead "
                                + "LEFT JOIN PreorderHead ON PreorderHead.PreorderNO= PreorderSalesHead.PreorderNO "
                                + "WHERE PreorderSalesHead.SecSeq = '" + SecSeq + "' "
                                + "AND PreorderHead.SaleFlag = '" + SaleFlag + "' "
                                + "AND PreorderSalesHead.SaleFlag = '" + SaleFlag + "' "
                                + "AND SUBSTR(ReceiptNo,1,1)= 'P' "

                                + "UNION "
                                + "SELECT PreorderSalesHead.SecSeq AS SecSeq, ReceiptNo, PreorderHead.PreorderNO AS PreorderNo,"
                                + " PreorderSalesHead.SaleFlag AS SaleFlag, 'VP' AS Type, 2 AS SerialNo "
                                + "FROM PreorderSalesHead "
                                + "LEFT JOIN PreorderHead ON PreorderHead.PreorderNO= PreorderSalesHead.PreorderNO "
                                + "WHERE PreorderSalesHead.SecSeq = '" + SecSeq + "' "
                                + "AND PreorderHead.SaleFlag = '" + SaleFlag + "' "
                                + "AND PreorderSalesHead.SaleFlag = '" + SaleFlag + "' "
                                + "AND SUBSTR(ReceiptNo,1,1)= 'V' ";

            } else {
                PreorderString =
                        "UNION "
                                + "SELECT SecSeq, ReceiptNo, PreorderNO AS PreorderNo, SaleFlag, 'PR' AS Type, 1 AS SerialNo "
                                + "FROM PreorderSalesHead "
                                + "WHERE SecSeq = '" + SecSeq + "' "
                                + "AND SaleFlag = '" + SaleFlag + "' "
                                + "AND SUBSTR(ReceiptNo,1,1)= 'P' "

                                + "UNION "
                                + "SELECT SecSeq, ReceiptNo, PreorderNO AS PreorderNo, SaleFlag, 'VP' AS Type, 2 AS SerialNo "
                                + "FROM PreorderSalesHead "
                                + "WHERE SecSeq = '" + SecSeq + "' "
                                + "AND SaleFlag = '" + SaleFlag + "' "
                                + "AND SUBSTR(ReceiptNo,1,1)= 'V' ";
            }

            // 取全部
            sb.append("SELECT SecSeq, ReceiptNo, '' AS PreorderNo, Status AS SaleFlag, 'DFS' AS Type, 0 AS SerialNo "
                    + "FROM SalesHead "
                    + "WHERE SecSeq = '" + SecSeq + "' " + SalesStatus + " AND PreorderNo = '' "

                    + "UNION "
                    + "SELECT SecSeq, ReceiptNo, PreorderNo, Status AS SaleFlag, 'VS' AS Type, 0 AS SerialNo "
                    + "FROM SalesHead "
                    + "WHERE SecSeq = '" + SecSeq + "' "
                    + SalesStatus + " AND PreorderNo <> '' "

                    + PreorderString

                    + "ORDER BY SerialNo, PreorderNo, ReceiptNo ");
            SQL = sb.toString();

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("ReceiptNo")) {

                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

            } else {
                ReturnCode = "9";
                ReturnMessage = "Get receipt list error";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetUpgradeProductInfo", ex.getMessage());
            throw ex;
        }

    }


    /**
     * 取得所有Upgrade單據編號
     *
     * @param SalesStatus 銷售狀態(’Sale’, ’Refund’)，不使用則傳Null
     * @return boolean
     */
    public JSONObject GetAllUpgradeRceciptNoList(String ReceiptNo, String SalesStatus, String SecSeq) throws Exception {

        String SQL;
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        try {
            // 篩選收據序號
            if (ReceiptNo == null) {
                ReceiptNo = " ";
            } else if (ReceiptNo.length() > 9) {
                // Preorder
                ReceiptNo = " AND PreorderNO= '" + ReceiptNo + "' ";
            } else {
                // DFS
                ReceiptNo = " AND ReceiptNo= '" + ReceiptNo + "' ";
            }

            if (SalesStatus == null) {
                SalesStatus = " ";
            } else if (SalesStatus.equals("Sale")) {
                SalesStatus = " AND Status = 'S' ";
            } else if (SalesStatus.equals("Refund")) {
                SalesStatus = " AND Status = 'R' ";
            }

            SQL = "SELECT SecSeq, ReceiptNo "
                    + "FROM ClassSalesHead "
                    + "WHERE SecSeq = '" + SecSeq + "' " + SalesStatus + ReceiptNo
                    + "ORDER BY ReceiptNo";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }

            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("ReceiptNo")) {

                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

            } else {
                ReturnCode = "9";
                ReturnMessage = "Get Upgrade Class list error.";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetUpgradeProductInfo", ex.getMessage());
            throw ex;
        }
    }


    /**
     * 取得取得現金及信用卡付款資訊 (列印銷售收)
     *
     * @param PayBy 付款方式
     */
    public JSONObject GetPaymentSalesInfo(String ReceiptNo, String PayBy, String Amount, String Status) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray ja;

        try {
            switch (PayBy) {
                case "Cash&Card":
                    SQL = "SELECT Currency, Amount, PayBy, CardNo, CardType, CardName, CardDate, CouponNo FROM PaymentInfo WHERE ReceiptNo = '" + ReceiptNo + "'"
                            + " AND SecSeq='" + this.SecSeq + "' AND Status='" + Status + "'"
                            + " AND (CouponNo = '' OR CouponNo IS NULL) AND PayBy <> 'Change' AND PayBy <> 'SC' AND PayBy <> 'DC' ORDER BY PayBy";
                    break;
                case "SC":
                    SQL = "SELECT Currency, Amount, PayBy, CardNo, CardType, CardName, CardDate, CouponNo FROM PaymentInfo WHERE ReceiptNo = '" + ReceiptNo + "'"
                            + " AND SecSeq='" + this.SecSeq + "' AND Status='" + Status + "'"
                            + " AND PayBy = 'SC' AND Amount = '" + Amount + "' ORDER BY PayBy";
                    break;
                case "DC":
                    SQL = "SELECT Currency, Amount, PayBy, CardNo, CardType, CardName, CardDate, CouponNo FROM PaymentInfo WHERE ReceiptNo = '" + ReceiptNo + "'"
                            + " AND SecSeq='" + this.SecSeq + "' AND Status='" + Status + "'"
                            + " AND PayBy = 'DC' AND Amount = '" + Amount + "' ORDER BY PayBy";
                    break;
                case "Change":
                    SQL = "SELECT Currency, Amount, PayBy, CardNo, CardType, CardName, CardDate, CouponNo FROM PaymentInfo WHERE ReceiptNo = '" + ReceiptNo + "'"
                            + " AND SecSeq='" + this.SecSeq + "' AND Status='" + Status + "'"
                            + " AND (CouponNo = '' OR CouponNo IS NULL) AND PayBy = 'Change' ORDER BY PayBy";
                    break;
                default:
                    SQL = "SELECT Currency, Amount, PayBy, CardNo, CardType, CardName, CardDate, CouponNo FROM PaymentInfo WHERE ReceiptNo = '" + ReceiptNo + "'"
                            + " AND SecSeq='" + this.SecSeq + "' AND Status='" + Status + "'"
                            + " AND PayBy = 'Refund'";
                    break;
            }

            ja = _TSQL.SelectSQLJsonArray(SQL);
            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("Amount")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "9";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }
        } catch (Exception ex) {
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetPaymentSalesInfo", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得所有付款資訊
     */
    public JSONObject GetPaymentInfo(String secSeq, String PreOrderNo, String ReceiptNo) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray ja;

        try {
            if (PreOrderNo != null && ReceiptNo == null) {
                SQL =
                        "SELECT Currency, Amount, PayBy, CardNo, CardType, CardName, CardDate, USDAmount, SalesHead.ReceiptNo, SalesHead.PreorderNo "
                                + "FROM PaymentInfo "
                                + "LEFT JOIN SalesHead "
                                + "ON SalesHead.ReceiptNo= PaymentInfo.ReceiptNo "
                                + "WHERE SalesHead.SecSeq = '" + secSeq + "' "
                                + "AND SalesHead.Status='S' "
                                + "AND PaymentInfo.Status='S' "
                                + "AND SalesHead.PreorderNo = '" + PreOrderNo + "' "
                                + "ORDER BY PayBy ";
            } else if (PreOrderNo == null && ReceiptNo != null) {
                SQL =
                        "SELECT Currency, Amount, PayBy, CardNo, CardType, CardName, CardDate, USDAmount, SalesHead.ReceiptNo, SalesHead.PreorderNo "
                                + "FROM PaymentInfo "
                                + "LEFT JOIN SalesHead "
                                + "ON SalesHead.ReceiptNo= PaymentInfo.ReceiptNo "
                                + "WHERE SalesHead.SecSeq = '" + secSeq + "' "
                                + "AND SalesHead.Status='S' "
                                + "AND PaymentInfo.Status='S' "
                                + "AND SalesHead.ReceiptNo = '" + ReceiptNo + "'";
            } else {
                SQL =
                        "SELECT Currency, Amount, PayBy, CardNo, CardType, CardName, CardDate, USDAmount, SalesHead.ReceiptNo, SalesHead.PreorderNo "
                                + "FROM PaymentInfo "
                                + "LEFT JOIN SalesHead "
                                + "ON SalesHead.ReceiptNo= PaymentInfo.ReceiptNo "
                                + "WHERE SalesHead.SecSeq = '" + secSeq + "' "
                                + "AND SalesHead.Status='S' "
                                + "AND PaymentInfo.Status='S' ";
            }
            ja = _TSQL.SelectSQLJsonArray(SQL);
            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("Amount")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "9";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }
        } catch (Exception ex) {
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetPaymentInfo", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得促銷活動資訊
     */
    public JSONObject GetPromotionsInfo() throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray ja;

        try {

            SQL = "SELECT * FROM PromotionsInfo";

            ja = _TSQL.SelectSQLJsonArray(SQL);
            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("promotionsTitle")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "9";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }
        } catch (Exception ex) {
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetPromotionInfo", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得移儲(Out)匯總
     */
    public JSONObject GetTransferOutSummary() throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray ja;

        try {
            SQL =
                    "SELECT Transfer.CarFrom, Transfer.CarTo, Transfer.ItemCode, SUM(Transfer.Qty) as Qty, AllParts.ItemName, AllParts.Serialcode FROM Transfer INNER JOIN "
                            + "AllParts ON "
                            + "Transfer.ItemCode = AllParts.ItemCode WHERE Transfer.SecSeq = '" + this.SecSeq
                            + "' AND TransferType = 'OUT' GROUP BY Transfer.CarFrom, Transfer.CarTo, AllParts.Serialcode ORDER BY Transfer.CarFrom, Transfer.CarTo, AllParts"
                            + ".Serialcode ";

            ja = _TSQL.SelectSQLJsonArray(SQL);
            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("ItemCode")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "9";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }
        } catch (Exception ex) {
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetTransferOutSummary", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得移儲(In)匯總
     */
    public JSONObject GetTransferInSummary() throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray ja;

        try {
            SQL =
                    "SELECT Transfer.CarFrom, Transfer.CarTo, Transfer.ItemCode, SUM(Transfer.Qty) as Qty, AllParts.ItemName, AllParts.Serialcode FROM Transfer INNER JOIN "
                            + "AllParts ON "
                            + "Transfer.ItemCode = AllParts.ItemCode WHERE Transfer.SecSeq = '" + this.SecSeq
                            + "' AND TransferType = 'IN' GROUP BY Transfer.ItemCode, ItemName, SerialCode ORDER BY Transfer.CarFrom, Transfer.CarTo, AllParts.Serialcode ";

            ja = _TSQL.SelectSQLJsonArray(SQL);
            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("ItemCode")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "9";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }
        } catch (Exception ex) {
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetTransferInSummary", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得升艙等 刷卡資訊
     */
    public JSONObject GetCreditCardAmt(String Currency) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray ja;

        try {
            SQL =
                    "SELECT ClassPaymentInfo.ReceiptNo, CardType, CardNo, Amount FROM ClassPaymentInfo INNER JOIN ClassSalesHead ON "
                            + "ClassPaymentInfo.ReceiptNo = ClassSalesHead.ReceiptNo WHERE ClassPaymentInfo.SecSeq = '" + this.SecSeq + "' AND "
                            + "ClassSalesHead.Status = 'S' AND ClassPaymentInfo.Status = 'S' AND PayBy = 'Card'";

            if (Currency.equals("USD")) {
                SQL += " AND Currency = 'USD'";
            } else {
                SQL += " AND Currency = 'TWD'";
            }

            SQL += " ORDER BY CardType, ClassPaymentInfo.ReceiptNo";

            ja = _TSQL.SelectSQLJsonArray(SQL);
            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("ReceiptNo")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "9";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }
        } catch (Exception ex) {
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetCreditCardAmt", ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得升艙等現金付款匯總
     */
    public JSONObject GetUpgradeAmount(String MoneyType) throws Exception {
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        JSONArray ja;

        try {
            SQL = "SELECT SUM(case WHEN 'Change' Then (0 - Amount) ELSE Amount END) AS Amount "
                    + "FROM ClassPaymentInfo "
                    + "LEFT JOIN ClassSalesHead "
                    + "ON ClassPaymentInfo.ReceiptNo= ClassSalesHead.ReceiptNo "
                    + "WHERE Currency = '" + MoneyType + "'  "
                    + "AND ClassSalesHead.SecSeq = '" + this.SecSeq + "' "
                    + "AND PayBy IN ('Change','Cash') "
                    + "AND ClassSalesHead.Status = 'S' "
                    + "AND ClassPaymentInfo.Status = 'S'";

            ja = _TSQL.SelectSQLJsonArray(SQL);
            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("Amount")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "9";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            }
        } catch (Exception ex) {
            _TSQL.WriteLog(this.SecSeq, "System", "DBFunctions", "GetUpgradeAmount", ex.getMessage());
            throw ex;
        }
    }


    /**
     * 取得回庫修改數量
     */
    public JSONObject GetModifyProductEGAS(String SecSeq, String Code, String DrawerNo, int Sort, String condition) throws Exception {

        String ClassName = "DBFunctions";
        String FunctionName = "GetModifyProductEGAS";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq + "-Code:" + Code + "-DrawerNo:" + DrawerNo + "-Sort:" + Sort);

        try {
            //取得商品資訊。
//            String SQL = "SELECT DISTINCT AllParts.ItemCode,DrawNo,SerialCode,ItemName,Remark,ItemPriceUS,ItemPriceTW, " +
//                    "StandQty,AdjustQty,SalesQty,TransferQty,DamageQty,EndQty,EGASCheckQty,EGASDamageQty,EVACheckQty,EVADamageQty, " +
//                    "(SELECT StartQty FROM Inventory as A WHERE A.ItemCode = ItemCode AND A.SecSeq = 0 ) as StartQty " +
//                    "FROM Inventory LEFT JOIN AllParts " +
//                    "ON Inventory.ItemCode = AllParts.ItemCode " +
//                    "WHERE 1=1 AND DrawNo <> '' ";
            String SQL = "select * from (select SecSeq,ItemCode,DrawNo,StandQty,(SELECT StartQty FROM Inventory as A WHERE A.ItemCode = ItemCode AND A.SecSeq = 0 ) as StartQty,AdjustQty,SalesQty,TransferQty,DamageQty,EndQty,EGASCheckQty,EGASDamageQty,EVACheckQty,EVADamageQty from Inventory where secseq='9' and ItemCode not in (select prt2 from PrtData)" +
                    "union " +
                    "select SecSeq,ItemCode,DrawNo,Prt4,(SELECT StartQty FROM Inventory as A WHERE A.ItemCode = ItemCode AND A.SecSeq = 0 ) as StartQty,AdjustQty,SalesQty,TransferQty,DamageQty,EndQty,EGASCheckQty,EGASDamageQty,EVACheckQty,EVADamageQty from Inventory inner join PrtData on ItemCode = prt2 where secseq='9') Inventory LEFT JOIN AllParts " +
                    "ON Inventory.ItemCode = AllParts.ItemCode " +
                    "WHERE 1=1 AND DrawNo <> '' ";
            if (SecSeq != null) {
                SQL += " AND SecSeq = '" + SecSeq + "' ";
            }

            if (Code != null) {

                if (Code.length() < 3) {
                    Code = PadLeft(Code, 3, '0');
                }

                SQL += " AND (AllParts.ItemCode = '" + Code +
                        "' OR AllParts.SerialCode = '" + Code +
                        "' OR AllParts.Barcode1 = '" + Code +
                        "' OR AllParts.Barcode2 = '" + Code +
                        "') ";
            }

            if (DrawerNo != null) {
                SQL += " AND DrawNo = '" + DrawerNo + "' ";
            }

            if (condition != null) {
                switch (condition) {
                    case "0":
                        SQL += " AND (EGASCheckQty <> StartQty OR EGASDamageQty <> DamageQty)";
                        break;
                    case "1":
                        SQL += " AND (EGASCheckQty <> StartQty OR EGASDamageQty <> DamageQty OR EVADamageQty <> DamageQty OR EVACheckQty <> EndQty)";
                        break;
                    case "2":
                        SQL += " AND (EGASCheckQty = StartQty AND EGASDamageQty = DamageQty)";
                        break;
                    case "3":
                        SQL += " AND (EGASCheckQty = StartQty AND EGASDamageQty = DamageQty AND EVADamageQty = DamageQty AND EVACheckQty = EndQty)";
                        break;
                }
            }

            if (Sort == 0) {
                SQL += " ORDER BY DrawNo, AllParts.ItemCode";
            } else if (Sort == 1) {
                SQL += " ORDER BY AllParts.ItemCode, DrawNo";
            } else {
                SQL += " ORDER BY DrawNo, SerialCode";
            }

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("ItemCode")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Pno code error";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    //EVA將有差異之項目排序至頂
    public JSONObject GetModifyProductEVA(String SecSeq, String Code, String DrawerNo, int Sort, String condition) throws Exception {

        String ClassName = "DBFunctions";
        String FunctionName = "GetModifyProductEVA";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq + "-Code:" + Code + "-DrawerNo:" + DrawerNo + "-Sort:" + Sort);

        try {
            //取得商品資訊。
            String SQL = "SELECT DISTINCT AllParts.ItemCode,DrawNo,SerialCode,ItemName,Remark,ItemPriceUS,ItemPriceTW, " +
                    "StandQty,AdjustQty,SalesQty,TransferQty,DamageQty,EndQty,EGASCheckQty,EGASDamageQty,EVACheckQty,EVADamageQty, " +
                    "(SELECT StartQty FROM Inventory as A WHERE A.ItemCode = ItemCode AND A.SecSeq = 0 ) as StartQty " +
                    "FROM Inventory LEFT JOIN AllParts " +
                    "ON Inventory.ItemCode = AllParts.ItemCode " +
                    "WHERE 1=1 AND DrawNo <> '' ";

            if (SecSeq != null) {
                SQL += " AND SecSeq = '" + SecSeq + "' ";
            }

            if (Code != null) {

                if (Code.length() < 3) {
                    Code = PadLeft(Code, 3, '0');
                }

                SQL += " AND (AllParts.ItemCode = '" + Code +
                        "' OR AllParts.SerialCode = '" + Code +
                        "' OR AllParts.Barcode1 = '" + Code +
                        "' OR AllParts.Barcode2 = '" + Code +
                        "') ";
            }

            if (DrawerNo != null) {
                SQL += " AND DrawNo = '" + DrawerNo + "' ";
            }

            if (condition != null) {
                switch (condition) {
                    case "0":
                        SQL += " AND (EGASCheckQty <> StartQty OR EGASDamageQty <> DamageQty)";
                        break;
                    case "1":
                        SQL += " AND (EGASCheckQty <> StartQty OR EGASDamageQty <> DamageQty OR EVADamageQty <> DamageQty OR EVACheckQty <> EndQty)";
                        break;
                    case "2":
                        SQL += " AND (EGASCheckQty = StartQty AND EGASDamageQty = DamageQty)";
                        break;
                    case "3":
                        SQL += " AND (EGASCheckQty = StartQty AND EGASDamageQty = DamageQty AND EVADamageQty = DamageQty AND EVACheckQty = EndQty)";
                        break;
                }
            }

            if (Sort == 0) {
                SQL += " ORDER BY DrawNo, AllParts.ItemCode";
            } else if (Sort == 1) {
                SQL += " ORDER BY AllParts.ItemCode, DrawNo";
            } else {
                SQL += " ORDER BY DrawNo, SerialCode";
            }

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("ItemCode")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Pno code error";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    // 取得銷售總量
    public JSONObject GetSalesQty() throws Exception {
        String ClassName = "DBFunctions";
        String FunctionName = "GetSalesQty";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq);

        try {
            //取得商品資訊。
            String SQL = "SELECT AllParts.ItemCode as ItemCode,SUM(SalesQty) as SalesQty FROM Inventory LEFT JOIN AllParts ON Inventory.ItemCode = AllParts.ItemCode "
                    + "GROUP BY Inventory.ItemCode HAVING SUM(SalesQty) > 0 "
                    + "UNION SELECT AllParts.ItemCode as ItemCode,SUM(SalesQty) as SalesQty FROM Inventory LEFT JOIN AllParts ON Inventory.ItemCode = AllParts.ItemCode "
                    + "WHERE EVACheckQty <> EndQty  GROUP BY Inventory.ItemCode";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("ItemCode")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Pno code error";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    // 取得Preorder所有可銷售的商品總數
    public JSONObject GetBeginPreorderAllItemQty(String SecSeq) throws Exception {
        String ClassName = "DBFunctions";
        String FunctionName = "GetPreorderAllItemQty";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq);

        try {
            // 自己航段: 取所有狀態
            // 非自己航段: 取N或R
            String SQL = "SELECT PreorderHead.PreorderNO, DrawNo, SUM(SalesQty) as SalesQty "
                    + "FROM PreorderDetail left join PreorderHead "
                    + "ON PreorderHead.PreorderNO = PreorderDetail.PreorderNo "
                    + "WHERE DrawNo <> '' AND (SecSeq = '" + SecSeq + "') OR (SecSeq <> '" + SecSeq + "' AND SaleFlag <> 'S') "
                    + "group by DrawNo ";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("DrawNo")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "PreorderNo error";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    // 取得關櫃時的Preorder商品裝載總數
    public JSONObject GetPreorderEndInventoryItemQty(String SecSeq) throws Exception {
        String ClassName = "DBFunctions";
        String FunctionName = "GetPreorderEndInventoryItemQty";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq);

        try {
            String SQL = "SELECT SUM(PreorderDetail.SalesQty) AS SalesQty "
                    + "FROM PreorderHead "
                    + "LEFT JOIN PreorderDetail "
                    + "WHERE PreorderHead.PreorderNO= PreorderDetail.PreorderNo "
//                + "AND SecSeq='"+ SecSeq +"' "
                    + "AND (SaleFlag = 'R' OR SaleFlag = 'N') ";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("SalesQty")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "PreorderNo error";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }


    public JSONObject GetCrewPassword(String CrewID) throws Exception {

        String ClassName = "DBFunctions";
        String FunctionName = "GetCrewPassword";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "CrewID:" + CrewID);

        try {

            String SQL = "SELECT CrewID,CrewType,Name, Password1 AS Password " +
                    "FROM CrewInfo " +
                    "WHERE CrewID = '" + CrewID + "'";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("CrewID")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Crew info is not exist.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }


    public JSONObject GetSalesItemDiscountReport(String SecSeq) throws Exception {

        String ClassName = "DBFunctions";
        String FunctionName = "GetSalesItemDiscountReport";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        try {
            String SQL = "select SalesDetail.ItemCode, SalesDetail.SalesPrice, SalesDetail.SalesQty, "
                    + "Discount, (CASE VipNo WHEN '' THEN VipType ELSE VipNo END) AS VipNo, SalesHead.Status, AllParts.SerialCode AS SerialCode "
                    + "from SalesDetail "
                    + "LEFT JOIN SalesHead "
                    + "ON SalesDetail.ReceiptNo = SalesHead.ReceiptNo "
                    + "left join AllParts "
                    + "on SalesDetail.ItemCode= AllParts.ItemCode "
                    + "where SalesHead.Status='S' and SalesDetail.SecSeq='" + SecSeq + "' "
                    + "AND SalesHead.PreorderNo = '' AND VipType <>''";
            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("ItemCode")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Sales Item Discount Report is not exist.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }


    //列印此航段之前的SecSeq,所以此航段的SecSeq設為排外條件
    public JSONObject GetBeginInventoryDamageQty(String SecSeq) throws Exception {

        String ClassName = "DBFunctions";
        String FunctionName = "GetBeginInventoryDamageQty";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        try {
            String SQL = "SELECT COUNT(Inventory.ItemCode) AS Qty, Inventory.ItemCode, "
                    + "AllParts.ItemName, AllParts.SerialCode, Inventory.DrawNo "
                    + "FROM Damage LEFT JOIN Inventory "
                    + "ON Damage.ItemCode = Inventory.ItemCode "
                    + "LEFT JOIN AllParts "
                    + "ON Inventory.ItemCode = AllParts.ItemCode "
                    + "WHERE Inventory.DrawNo <> '' AND Inventory.SecSeq = '" + SecSeq + "' "
                    + "AND Damage.SecSeq <> '" + SecSeq + "' "
                    + "AND Status = 'S' "
                    + "GROUP BY Inventory.ItemCode, AllParts.ItemName, AllParts.ItemName, Inventory.DrawNo ";
            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("ItemCode")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Sales Item Discount Report is not exist.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }


    public JSONObject GetEndInventoryDamageQty(String SecSeq) throws Exception {

        String ClassName = "DBFunctions";
        String FunctionName = "GetBeginInventoryDamageQty";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        try {
            String SQL = "SELECT COUNT(Inventory.ItemCode) AS Qty, Inventory.ItemCode, "
                    + "AllParts.ItemName, AllParts.SerialCode, Inventory.DrawNo "
                    + "FROM Damage LEFT JOIN Inventory "
                    + "ON Damage.ItemCode = Inventory.ItemCode "
                    + "LEFT JOIN AllParts "
                    + "ON Inventory.ItemCode = AllParts.ItemCode "
                    + "WHERE Inventory.DrawNo <> '' AND Inventory.SecSeq = '" + SecSeq + "' "
//                + "AND Damage.SecSeq <> '" + SecSeq + "' "
                    + "AND Status = 'S' "
                    + "GROUP BY Inventory.ItemCode, AllParts.ItemName, AllParts.ItemName, Inventory.DrawNo ";
            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("ItemCode")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Sales Item Discount Report is not exist.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 取得設定檔內容
     */
    public Setting getSettings() {

        String ClassName = "DBFunctions";
        String FunctionName = "getSettings";
//        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        TSQL _TSQL = TSQL.getINSTANCE(context, FlightData.SecSeq, "P17023");
        Setting setting = new Setting();

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:  SecSeq->" + this.SecSeq);

        try {
            String SQL = "SELECT * FROM Settings";
            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            for (int i = 0; i < ja.length(); i++) {
                switch (ja.getJSONObject(i).getString("Key")) {
                    case "AuthenticationMode":
                        setting.setAuthenticationMode(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "AirposConnecTryCount":
                        setting.setAirPosConnecTryCount(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "AuthenticationStatus":
                        setting.setAuthenticationStatus(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "Authorize":
                        setting.setAuthorize(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "BlackFTPServerIP":
                        setting.setBlackFTPServerIP(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "BlackFTPServerPort":
                        setting.setBlackFTPServerPort(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "BlackFTPUserName":
                        setting.setBlackFTPUserName(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "BlackFTPUserPassword":
                        setting.setBlackFTPUserPassword(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "Certificates":
                        setting.setCertificate(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "FTPServerIP":
                        setting.setFTPServerIP(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "FTPServerPort":
                        setting.setFTPServerPort(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "FTPUserName":
                        setting.setFTPUserName(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "FTPUserPassword":
                        setting.setFTPUserPassword(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "GroundConnecTryCount":
                        setting.setGroundConnectTryCount(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "GROUNDSSID":
                        setting.setGroundSSID(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "IFEIP":
                        setting.setIFEIP(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "IFEKEY":
                        setting.setIFEKEY(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "IFESSID":
                        setting.setIFESSID(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "IPAddress":
                        setting.setIPAddress(ja.getJSONObject(i).getString("Value"));
                        break;
                    case "WebServiceURL":
                        setting.setWebServiceURL(ja.getJSONObject(i).getString("Value"));
                        break;
                }
            }

            return setting;

        } catch (Exception e) {
            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, e.getMessage());
            return null;
        }
    }

    public JSONObject GetBinCode() throws Exception {

        String ClassName = "DBFunctions";
        String FunctionName = "GetBinCode";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:  SecSeq->" + this.SecSeq);

        try {
            String SQL = "select BinCode from BankDis";
            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja == null) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("BinCode")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Query Error";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }


    /**
     * 字串左邊補0
     *
     * @param OriginalString 原始字串
     * @param TotalWight     要補足字串長度
     * @param PaddingChar    補足字元
     * @return (String) 處理完字串
     */
    public String PadLeft(String OriginalString, int TotalWight, char PaddingChar) {

        try {
            if (OriginalString.length() < TotalWight) {
                while (OriginalString.length() < TotalWight) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(PaddingChar).append(OriginalString);// 左補
                    OriginalString = sb.toString();
                }
            }
            return OriginalString;

        } catch (Exception ex) {

            throw ex;
        }
    }


    /**
     * 轉為標準回傳JSONObject
     *
     * @param ReturnCode    要回傳的號碼，0為成功
     * @param ReturnMessage 要回傳的訊息內容
     * @param ResponseData  要回傳的資料
     * @return (JSONObject) 轉出來的JSONObject
     */
    public JSONObject GetReturnJsonObject(String ReturnCode, String ReturnMessage, JSONArray ResponseData) throws Exception {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ReturnCode", ReturnCode);
        jsonObject.put("ReturnMessage", ReturnMessage);

        if (ResponseData != null && ResponseData.length() > 0) {
            jsonObject.put("ResponseData", ResponseData);
        }

        return jsonObject;

    }

    public void updateUploadStatus() {
        String ClassName = "DBFunctions";
        String FunctionName = "updateUploadStatus";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        ArrayList SQLCommand = new ArrayList();
        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:  SecSeq->" + this.SecSeq);

        try {

            String SQL = "SELECT * FROM Flight";
            JSONArray jsonArray = _TSQL.SelectSQLJsonArray(SQL);
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    SQL = "UPDATE Flight SET IsUpload = 'Y' WHERE SecSeq = '" + jsonArray.getJSONObject(i).getString("SecSeq") + "'";
                    SQLCommand.add(SQL);
                }
            }

            _TSQL.ExecutesSQLCommand(SQLCommand);

        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }
    }

    public String[] GetUploadStatus() {
        String ClassName = "DBFunctions";
        String FunctionName = "GetUploadStatus";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String[] strings = null;

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:  SecSeq->" + this.SecSeq);

        try {
            String SQL = "SELECT IsUpload FROM Flight";
            JSONArray jsonArray = _TSQL.SelectSQLJsonArray(SQL);
            if (jsonArray != null && jsonArray.length() > 0) {
                strings = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    strings[i] = jsonArray.getJSONObject(i).getString("IsUpload");
                }
            }
        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }

        return strings;
    }

    public String[] getFlightDate() {
        String ClassName = "DBFunctions";
        String FunctionName = "GetUploadStatus";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String[] strings = null;

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:  SecSeq->" + this.SecSeq);

        try {
            String SQL = "SELECT IsUpload FROM Flight";
            JSONArray jsonArray = _TSQL.SelectSQLJsonArray(SQL);
            if (jsonArray != null && jsonArray.length() > 0) {
                strings = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    strings[i] = jsonArray.getJSONObject(i).getString("IsUpload");
                }
            }
        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }

        return strings;
    }

    public List<PrtData> getPrtData() {
        String ClassName = "DBFunctions";
        String FunctionName = "getPrtData";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:  SecSeq->" + this.SecSeq);

        try {
            String SQL = "SELECT * FROM PrtData";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja == null) {
                return null;
            }

            if (ja.length() > 0 && !ja.getJSONObject(0).isNull("prt1")) {
                return JSON.parseObject(ja.toString(), new TypeReference<List<PrtData>>() {
                });
            } else {
                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "PrtData is empty");
                return null;
            }
        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
            return null;
        }
    }
}
