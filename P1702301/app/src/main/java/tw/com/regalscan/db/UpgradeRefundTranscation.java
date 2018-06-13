package tw.com.regalscan.db;

import android.content.Context;

import com.regalscan.sqlitelibrary.TSQL;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import tw.com.regalscan.component.AESEncrypDecryp;

/**
 * Created by gabehsu on 2017/6/24.
 */

public class UpgradeRefundTranscation {

    public Context context;
    public String SecSeq;

    private PublicFunctions _PublicFunctions;

    private PayMode NowPayMode = PayMode.PAY;  //付款模式 Pay or Change or Balance
    private boolean IsTotalUseTWDPay = false;  //是否全用台幣付款
    private boolean IsTotalUseTWDRefund = false;  //是否全用台幣找零

    //單據資訊
    private String ReceiptNo;         //交易單據號碼
    private Boolean AuthuticationFlag;//線上授權Flag Y or N

    //交易金額資訊
    private double USDTotalAmount;    //美金應找零總金額
    private double NTDTotalAmount;    //台幣應找零總金額
    private double USDTotalChanged;   //美金已找零總金額
    private double NTDTotalChanged;   //台幣應找零總金額
    private double USDTotalUnchange;  //美金未找零總金額
    private double NTDTotalUnchange;  //台幣應找零總金額

    //商品列表
    private ArrayList<UpgradeItem> UpgradeItemList;

    //原始付款歷程
    private ArrayList<PaymentItem> OriginalPaymentList;

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
     * 初始化Transcation
     */
    public UpgradeRefundTranscation(Context context, String SecSeq) {

        this.context = context;
        this.SecSeq = SecSeq;

        //初始變數
        AuthuticationFlag = false;

        //實作PublicFunctions
        _PublicFunctions = new PublicFunctions(context, SecSeq);

        //實作PaymentList
        UpgradeItemList= new ArrayList<UpgradeItem>();

        //實作原始PaymentList
        OriginalPaymentList = new ArrayList<PaymentItem>();
    }

    ////////////////////////////////////////////////////////////////////////
    //                            Basket
    ////////////////////////////////////////////////////////////////////////

    /**
     * 取得退貨商品列表
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetBasketInfo(String ReceiptNo) {

        String ClassName = "UpgradeRefundTranscation";
        String FunctionName = "GetBasketInfo";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String ReturnCode, ReturnMessage;
        JSONArray jaSalesItemList = new JSONArray(), jaItem;
        JSONObject joSalesItem, ResponseJsonObject;
        String SQL;

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "ReceiptNo:" + ReceiptNo);

        try {

            //檢核單據號碼是否允許進行Upgrade退貨
            SQL = "SELECT TotalPrice, ClassSalesHead.ReceiptNo, AuthuticationFlag, " +
                    "ClassSalesDetail.Infant, ClassSalesDetail.OriginalClass, ClassSalesDetail.NewClass, USDPrice, TWDPrice, SalesQty " +
                    "FROM ClassSalesHead LEFT JOIN ClassSalesDetail  " +
                    "ON ClassSalesHead.ReceiptNo = ClassSalesDetail.ReceiptNo " +
                    "LEFT JOIN ClassAllParts " +
                    "ON ClassSalesDetail.Infant = ClassAllParts.Infant " +
                    "AND ClassSalesDetail.OriginalClass = ClassAllParts.OriginalClass " +
                    "AND ClassSalesDetail.NewClass = ClassAllParts.NewClass " +
                    "WHERE ClassSalesHead.ReceiptNo = '" + ReceiptNo +
                    "' AND ClassSalesHead.SecSeq = '" + this.SecSeq +
                    "' AND ClassSalesHead.Status = 'S' ";
            jaItem = _TSQL.SelectSQLJsonArray(SQL);

            if (jaItem == null || jaItem.length() == 0)
            {
                ReturnCode = "8";
                ReturnMessage = "Receipt no not found.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //帶出交易資訊
            this.ReceiptNo = ReceiptNo;
            this.USDTotalAmount =jaItem.getJSONObject(0).getDouble("TotalPrice");
            this.NTDTotalAmount = 0;
            this.USDTotalChanged = 0;
            this.NTDTotalChanged = 0;
            this.USDTotalUnchange = 0;
            this.NTDTotalUnchange = 0;

            this.AuthuticationFlag = jaItem.getJSONObject(0).getString("AuthuticationFlag").equals("Y");

            //帶出商品列表
            UpgradeItemList.clear();
            for (int i = 0;i < jaItem.length(); i ++)
            {
                UpgradeItem _UpgradeItem = new UpgradeItem();
                _UpgradeItem.Infant = jaItem.getJSONObject(i).getString("Infant");
                _UpgradeItem.OriginalClass = jaItem.getJSONObject(i).getString("OriginalClass");
                _UpgradeItem.NewClass = jaItem.getJSONObject(i).getString("NewClass");
                _UpgradeItem.USDPrice = jaItem.getJSONObject(i).getDouble("USDPrice");
                _UpgradeItem.TWDPrice = jaItem.getJSONObject(i).getDouble("TWDPrice");
                _UpgradeItem.SalesQty = jaItem.getJSONObject(i).getInt("SalesQty");
                UpgradeItemList.add(_UpgradeItem);

                //將銷售品轉JSON
                joSalesItem = new JSONObject();
                joSalesItem.put("Infant",_UpgradeItem.Infant);
                joSalesItem.put("OriginalClass",_UpgradeItem.OriginalClass);
                joSalesItem.put("NewClass",_UpgradeItem.NewClass);
                joSalesItem.put("USDPrice",_UpgradeItem.USDPrice);
                joSalesItem.put("TWDPrice",_UpgradeItem.TWDPrice);
                joSalesItem.put("SalesQty",_UpgradeItem.SalesQty);
                jaSalesItemList.put(joSalesItem);
            }

            //取得付款紀錄
            SQL = "SELECT PayBy, Currency, Amount, CardType, CardNo, CardName, CardDate, USDAmount " +
                    "FROM ClassPaymentInfo " +
                    "WHERE SecSeq = '" + this.SecSeq +
                    "' AND ReceiptNo = '" +this.ReceiptNo + "'";
            jaItem = _TSQL.SelectSQLJsonArray(SQL);

            if (jaItem == null || jaItem.length() == 0)
            {
                ReturnCode = "8";
                ReturnMessage = "Receipt no not found.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //帶出原先付款紀錄，並判斷當初是否為全台幣付款
            IsTotalUseTWDPay = true;
            OriginalPaymentList.clear();
            for (int i = 0;i < jaItem.length(); i ++)
            {
                if (!jaItem.getJSONObject(i).getString("Currency").equals("TWD")) {
                    IsTotalUseTWDPay = false;
                }

                //計算台幣總付款金額，，若為全台幣付款以及全台幣找零才用到的到
                if (!jaItem.getJSONObject(i).getString("PayBy").equals("Change") &&
                        jaItem.getJSONObject(i).getString("Currency").equals("TWD")) {
                    NTDTotalAmount = Arith.add(NTDTotalAmount,jaItem.getJSONObject(i).getDouble("Amount"));
                } else {
                    NTDTotalAmount = Arith.sub(NTDTotalAmount,jaItem.getJSONObject(i).getDouble("Amount"));
                }

                //原始付款歷程
                PaymentItem _PaymentItem = new PaymentItem();
                _PaymentItem.Currency = jaItem.getJSONObject(i).getString("Currency");
                _PaymentItem.PayBy = jaItem.getJSONObject(i).getString("PayBy");
                _PaymentItem.Amount = jaItem.getJSONObject(i).getDouble("Amount");
                _PaymentItem.USDAmount = jaItem.getJSONObject(i).getDouble("USDAmount");
                _PaymentItem.CouponUSDAmount = jaItem.getJSONObject(i).getDouble("USDAmount");

                if (_PaymentItem.PayBy.equals("Card"))
                {
                    _PaymentItem.CardType = jaItem.getJSONObject(i).getString("CardType");
                    _PaymentItem.CardNo = AESEncrypDecryp.getDectyptData(jaItem.getJSONObject(i).getString("CardNo"), FlightData.AESKey);
                    _PaymentItem.CardName = jaItem.getJSONObject(i).getString("CardName");
                    _PaymentItem.CardDate = AESEncrypDecryp.getDectyptData(jaItem.getJSONObject(i).getString("CardDate"), FlightData.AESKey);
                }else {
                    _PaymentItem.CardType = "";
                    _PaymentItem.CardNo = "";
                    _PaymentItem.CardName = "";
                    _PaymentItem.CardDate = "";
                }
                OriginalPaymentList.add(_PaymentItem);
            }

            //將計算結果轉為Response格式
            ResponseJsonObject = new JSONObject();
            ResponseJsonObject.put("ReceiptNo", this.ReceiptNo);
            ResponseJsonObject.put("USDAmount", this.USDTotalAmount);
            ResponseJsonObject.put("Items", jaSalesItemList);

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
    //                            Payment
    ////////////////////////////////////////////////////////////////////////

    /**
     * 取得原始付款模式
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetOriginalPaymentMold() {

        String ClassName = "UpgradeRefundTranscation";
        String FunctionName = "GetOriginalPaymentMold";
        String ReturnCode, ReturnMessage;
        JSONObject jo;
        JSONArray ja = new JSONArray();
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {
            //依據原始付款歷程，直接回Balance
            //Balance mode
            NowPayMode = PayMode.BALANCE;

            //串目前的付款里程
            for (int i = 0; i < OriginalPaymentList.size(); i++) {
                jo = new JSONObject();
                jo.put("Currency", OriginalPaymentList.get(i).Currency);
                jo.put("PayBy", OriginalPaymentList.get(i).PayBy);
                jo.put("Amount", OriginalPaymentList.get(i).Amount);
                jo.put("USDAmount", OriginalPaymentList.get(i).USDAmount);
                jo.put("CouponNo", OriginalPaymentList.get(i).CouponNo);
                ja.put(jo);
            }

            //串目前付款資訊
            jo = new JSONObject();
            jo.put("ReceiptNo", this.ReceiptNo);
            jo.put("NowPayMode", String.valueOf(NowPayMode));
            jo.put("USDTotalAmount", USDTotalAmount);
            jo.put("USDTotalPayed", USDTotalAmount);
            jo.put("USDTotalUnpay", 0);
            jo.put("PaymentList", ja);

            ReturnCode = "0";
            ReturnMessage = "";
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, new JSONArray().put(jo));

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
     * 進行Upgrade退貨(原始付款方式)
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject SaveRefundInfoByOriginal() {

        String ClassName = "UpgradeRefundTransaction";
        String FunctionName = "SaveRefundInfoByOriginal";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        ArrayList SQLCommands = new ArrayList();
        String SQL;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //退貨單頭
            SQL = "UPDATE ClassSalesHead " +
                    "SET RefundTime = '" + formatter.format(new Date(System.currentTimeMillis())) +
                    "', Status = 'R' "+
                    "WHERE SecSeq = '" + this.SecSeq +
                    "' AND ReceiptNo = '" + this.ReceiptNo + "'";
            SQLCommands.add(SQL);

            //退貨單身
            SQL = "UPDATE ClassSalesDetail " +
                    "SET Status = 'R' "+
                    "WHERE SecSeq = '" + this.SecSeq +
                    "' AND ReceiptNo = '" + this.ReceiptNo + "'";
            SQLCommands.add(SQL);

            //退貨資料轉SQL
                SQL = "INSERT INTO ClassPaymentInfo (SecSeq,ReceiptNo,PayBy,Currency,Amount,CardType,CardNo,CardName,CardDate,USDAmount,Status) " +
                        "VALUES ('" + this.SecSeq +
                        "','" + this.ReceiptNo +
                        "','" + OriginalPaymentList.get(0).PayBy +
                        "','" + OriginalPaymentList.get(0).Currency +
                        "'," + OriginalPaymentList.get(0).Amount;

            if (OriginalPaymentList.get(0).PayBy.equals("Card")) {
                SQL +=  ",'" + OriginalPaymentList.get(0).CardType +
                        "','" + AESEncrypDecryp.getEncryptData(OriginalPaymentList.get(0).CardNo, FlightData.AESKey) +
                        "','" + OriginalPaymentList.get(0).CardName +
                        "','" +  AESEncrypDecryp.getEncryptData(OriginalPaymentList.get(0).CardDate, FlightData.AESKey);
            } else {
                SQL +=  ",'" + "" +
                        "','" + "" +
                        "','" + "" +
                        "','" + "";
            }

            SQL +=  "'," + OriginalPaymentList.get(0).USDAmount +
                        ",'" + "R" +  "')";
                SQLCommands.add(SQL);

            //寫入資料
            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Save refund info failed.";

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

}
