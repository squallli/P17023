package tw.com.regalscan.evaair.ife;

import android.content.Context;

import com.regalscan.sqlitelibrary.TSQL;

import org.joda.time.DateTime;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tw.com.regalscan.component.AESEncrypDecryp;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.evaair.ife.entity.Catalog;
import tw.com.regalscan.evaair.ife.model.entity.AuthorizeModel;

/**
 * Created by tp00175 on 2017/10/19.
 */

public class IFEDBFunction {

    private Context context;
    private String SecSeq;

    //起始函式
    public IFEDBFunction(Context context, String SecSeq) {
        this.context = context;
        this.SecSeq = SecSeq;
    }

    //取得 enable item
    public boolean UpdateEnableItem(List<Catalog> catalogs) {
        String ClassName = "IFEDBFunction";
        String FunctionName = "UpdateEnableItem";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        ArrayList SQLCommands = new ArrayList();

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        try {
            if (catalogs != null && catalogs.size() > 0) {
                for (Catalog catalog : catalogs) {
                    SQLCommands.add(catalog.toUpdateSQL());
                    _TSQL.ExecutesSQLCommand("UPDATE Flight SET IFECatalogID = '" + catalog.getCatalogId() + "' WHERE SecSeq = '" + FlightData.SecSeq + "'");
                }
                _TSQL.ExecutesSQLCommand(SQLCommands);
            }

            JSONArray jsonArray = _TSQL.SelectSQLJsonArray("SELECT ItemID FROM AllParts WHERE ItemID <> ''");

            return jsonArray != null && jsonArray.length() > 0;

        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
            return false;
        }
    }

    //取得每個 enable item 的數量
    public HashMap<String, Integer> getEnableItem() {
        String ClassName = "IFEDBFunction";
        String FunctionName = "getEnableItem";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        HashMap<String, Integer> skuQty = new HashMap<>();

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        try {
            String SQL = "SELECT AllParts.IFEID, (EndQty - DamageQty) as stock FROM AllParts LEFT JOIN Inventory ON AllParts" +
                    ".ItemCode = Inventory.ItemCode WHERE ItemID <> '' AND SecSeq = '0' AND stock <> 0";
            JSONArray jsonArray = _TSQL.SelectSQLJsonArray(SQL);
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String sku = jsonArray.getJSONObject(i).getString("IFEID");
                    int qty = jsonArray.getJSONObject(i).getInt("stock");
                    skuQty.put(sku, qty);
                }
                return skuQty;
            }
        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }
        return skuQty;
    }

    public String getIFEID(String Code) {
        String ClassName = "IFEDBFunction";
        String FunctionName = "getIFEID";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        String IFEID = "";

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        if (Code.length() < 3) {
            Code = String.format("%03d", Integer.parseInt(Code));
        }

        try {
            String SQL = "SELECT IFEID FROM AllParts where SerialCode = '" + Code + "' or ItemCode='" + Code + "' or " +
                    "Barcode1 = '" + Code + "' or Barcode2 = '" + Code + "' or IFEID = '" + Code + "'";
            JSONArray jsonArray = _TSQL.SelectSQLJsonArray(SQL);
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    IFEID = jsonArray.getJSONObject(i).getString("IFEID");
                }
                return IFEID;
            }
        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }
        return IFEID;
    }

    public String getItemCode(String Code) {
        String ClassName = "IFEDBFunction";
        String FunctionName = "getItemCode";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        String itemCode = "";

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        if (Code.length() < 3) {
            Code = String.format("%03d", Integer.parseInt(Code));
        }

        try {
            String SQL = "SELECT ItemCode FROM AllParts where SerialCode = '" + Code + "' or ItemCode='" + Code + "' or " +
                    "Barcode1 = '" + Code + "' or Barcode2 = '" + Code + "' or IFEID = '" + Code + "'";
            JSONArray jsonArray = _TSQL.SelectSQLJsonArray(SQL);
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    itemCode = jsonArray.getJSONObject(i).getString("ItemCode");
                }
                return itemCode;
            }
        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }
        return itemCode;
    }

    public String getSerialCode(String Code) {
        String ClassName = "IFEDBFunction";
        String FunctionName = "getSerialCode";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        String serialCode = "";

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        try {
            String SQL = "SELECT SerialCode FROM AllParts where ItemCode='" + Code + "' or " +
                    "Barcode1 = '" + Code + "' or Barcode2 = '" + Code + "' or IFEID = '" + Code + "' or ItemID = '" + Code + "'";
            JSONArray jsonArray = _TSQL.SelectSQLJsonArray(SQL);
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    serialCode = jsonArray.getJSONObject(i).getString("SerialCode");
                }
                return serialCode;
            }
        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }
        return serialCode;
    }

    /**
     * 取得該行段 CatalogID
     */
    public String getCatalogID() {
        String ClassName = "IFEDBFunction";
        String FunctionName = "getCatalogID";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        String catalogID = "";

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        try {
            String SQL = "SELECT IFECatalogID FROM Flight WHERE SecSeq = '" + this.SecSeq + "'";
            JSONArray jsonArray = _TSQL.SelectSQLJsonArray(SQL);
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    catalogID = jsonArray.getJSONObject(i).getString("IFECatalogID");
                }
                return catalogID;
            }
        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }
        return catalogID;
    }

    /**
     * 使用ItemID取回ItemCode
     */
    public String getItemCodeByItemID(String ItemID) {
        String ClassName = "IFEDBFunction";
        String FunctionName = "getItemCodeByItemID";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        String itemID = "";

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        try {
            String SQL = "SELECT ItemCode FROM AllParts WHERE ItemID = '" + ItemID + "'";
            JSONArray jsonArray = _TSQL.SelectSQLJsonArray(SQL);
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    itemID = jsonArray.getJSONObject(i).getString("ItemCode");
                }
                return itemID;
            }
        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }
        return itemID;
    }

    /**
     * 用 itemID 取得 IFEID
     */
    public String getIFEIDByItemID(String ItemID) {
        String ClassName = "IFEDBFunction";
        String FunctionName = "getIFEIDByItemID";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        String IFEID = "";

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        try {
            String SQL = "SELECT IFEID FROM AllParts WHERE ItemID = '" + ItemID + "'";
            JSONArray jsonArray = _TSQL.SelectSQLJsonArray(SQL);
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    IFEID = jsonArray.getJSONObject(i).getString("IFEID");
                }
                return IFEID;
            }
        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }
        return IFEID;
    }

    /**
     * 取得ItemID
     */
    public String getItemID(String Code) {
        String ClassName = "IFEDBFunction";
        String FunctionName = "getItemID";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        if (Code.length() < 3) {
            Code = String.format("%03d", Integer.parseInt(Code));
        }

        String SQL = "SELECT ItemID FROM ALLParts WHERE SerialCode = '" + Code + "' OR ItemCode = '" + Code + "' OR " +
                "Barcode1 = '" + Code + "' OR Barcode2 = '" + Code + "' or IFEID = '" + Code + "'";

        try {
            JSONArray jsonArray = _TSQL.SelectSQLJsonArray(SQL);
            return jsonArray.getJSONObject(0).getString("ItemID");
        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }

        return "";
    }

    /**
     * 更新同步庫存狀態
     */
    public void inventoryPushed() {
        String ClassName = "IFEDBFunction";
        String FunctionName = "inventoryPushed";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        String SQL = "UPDATE Flight SET IFETokenID = 'Y' WHERE SecSeq = '" + FlightData.SecSeq + "'";

        try {
            _TSQL.ExecutesSQLCommand(SQL);
        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }
    }

    /**
     * 檢查是否同步庫存
     */
    public boolean chkPushInventory(String secSeq) {
        String ClassName = "IFEDBFunction";
        String FunctionName = "chkPushInventory";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        boolean isPushed = false;
        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        String SQL = "SELECT IFETokenID FROM Flight WHERE SecSeq = '" + secSeq + "'";

        try {
            JSONArray jsonArray = _TSQL.SelectSQLJsonArray(SQL);
            isPushed = !jsonArray.getJSONObject(0).getString("IFETokenID").equals("");
        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }
        return isPushed;
    }

    /**
     * 儲存取授權資訊
     */
    public void saveAuthorizeData(AuthorizeModel authorizeModel) {
        String ClassName = "IFEDBFunction";
        String FunctionName = "saveAuthorizeData";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        try {
            String SQL =
                    "INSERT INTO Authutication (SecSeq, ReceiptNo, UG_MARK, CardNo, CardName, CardType, ExpireDate, CurrencyType, TransAmount, OrderNo, PreorderNo, VipType, "
                            + "VipNo, REAUTH_MARK, APPROVE_CODE, RSPONSE_CODE, RSPONSE_MSG, TRANS_DATE, TRANS_TIME, AUTH_DATE, TRANS_SEQNO, TRANS_CODE, AUTH_RETCODE, AuthTime) "
                            +
                            "VALUES ( '" + authorizeModel.getSECTOR_SEQ() + "','" +
                            authorizeModel.getRECIPT_NO() + "','" +
                            authorizeModel.getUG_MARK() + "','" +
                            AESEncrypDecryp.getEncryptData(authorizeModel.getCREDIT_CARD_NO(), FlightData.AESKey) + "','" +
                            authorizeModel.getCREDIT_CARD_NAME() + "','" +
                            authorizeModel.getCREDIT_CARD_TYPE() + "','" +
                            AESEncrypDecryp.getEncryptData(authorizeModel.getEXP_MONTH() + authorizeModel.getEXP_YEAR(), FlightData.AESKey) + "','" +
                            authorizeModel.getCurrency() + "','" +
                            (authorizeModel.getCurrency().equals("USD") ? authorizeModel.getUSD_AMT() : authorizeModel.getTWD_AMT()) + "','" +
                            authorizeModel.getIFE_SEQ() + "','" +
                            authorizeModel.getORDER_NO() + "','" +
                            authorizeModel.getVIP_TYPE() + "','" +
                            (authorizeModel.getVIP_NO().length() > 12 ? AESEncrypDecryp.getEncryptData(authorizeModel.getVIP_NO(), FlightData.AESKey) : authorizeModel.getVIP_NO())
                            + "','" +
                            authorizeModel.getREAUTH_MARK() + "','" +
                            authorizeModel.getAPPROVE_CODE() + "','" +
                            authorizeModel.getRSPONSE_CODE() + "','" +
                            authorizeModel.getRSPONSE_MSG() + "','" +
                            authorizeModel.getTRANS_DATE() + "','" +
                            authorizeModel.getTRANS_TIME() + "','" +
                            authorizeModel.getAUTH_DATE() + "','" +
                            authorizeModel.getTRANS_SEQNO() + "','" +
                            authorizeModel.getTRANS_CODE() + "','" +
                            authorizeModel.getAUTH_RETCODE() + "','" +
                            DateTime.now().toString("yyyy/MM/dd HH:mm:ss") + "')";

            _TSQL.ExecutesSQLCommand(SQL);
        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }
    }

    public AuthorizeModel getAuthorizeInfo(String receiptNo, String refundType) {
        String ClassName = "IFEDBFunction";
        String FunctionName = "getAuthorizeInfo";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        String SQL;
        AuthorizeModel authorizeModel = new AuthorizeModel();

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        try {
            if (refundType.equals("DutyFreeRefund")) {
                SQL = "SELECT * FROM Authutication WHERE ReceiptNo = '" + receiptNo + "' AND SecSeq = '" + FlightData.SecSeq + "' AND UG_MARK = 'N' AND APPROVE_CODE <> ''";
            } else {
                SQL = "SELECT * FROM Authutication WHERE ReceiptNo = '" + receiptNo + "' AND SecSeq = '" + FlightData.SecSeq + "' AND UG_MARK = 'Y' AND APPROVE_CODE <> ''";
            }

            JSONArray jsonArray = _TSQL.SelectSQLJsonArray(SQL);

            if (jsonArray.length() > 0) {
                authorizeModel = new AuthorizeModel(jsonArray.getJSONObject(0));
            }

        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }

        return authorizeModel;
    }

    public void deAuthorizeSave(String receiptNo, String refundType) {
        String ClassName = "IFEDBFunction";
        String FunctionName = "deAuthorizeSave";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        String SQL;

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        try {
            if (refundType.equals("DutyFreeRefund")) {
                SQL = "UPDATE SalesHead SET AuthuticationFlag = 'N' WHERE ReceiptNo = '" + receiptNo + "' AND SecSeq = '" + FlightData.SecSeq + "'";
            } else {
                SQL = "UPDATE ClassSalesHead SET AuthuticationFlag = 'N' WHERE ReceiptNo = '" + receiptNo + "' AND SecSeq = '" + FlightData.SecSeq + "'";
            }

            _TSQL.ExecutesSQLCommand(SQL);

        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }
    }

    /**
     * 儲存IFE訂單修改歷程
     *
     * @param receiptNo  訂單號碼
     * @param oldOrderNo 舊IFE單號
     * @param newOrderNo 新IFE單號
     */
    public void saveIFEOrderHistory(String receiptNo, String oldOrderNo, String newOrderNo) {
        String ClassName = "IFEDBFunction";
        String FunctionName = "saveIFEOrderHistory";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        String SQL;

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start: SecSeq->" + this.SecSeq);

        try {
            SQL = "INSERT INTO OrderChangeHistory (ReceiptNo, SecSeq, OldOrderNo, NewOrderNo)"
                    + "VALUES ('" + receiptNo
                    + "','" + FlightData.SecSeq
                    + "','" + oldOrderNo
                    + "','" + newOrderNo + "')";

            _TSQL.ExecutesSQLCommand(SQL);

        } catch (Exception e) {
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function Error: SecSeq->" + this.SecSeq + ", errMsg->" + e.getMessage());
        }
    }
}
