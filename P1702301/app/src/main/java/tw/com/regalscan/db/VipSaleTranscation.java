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
 * Created by gabehsu on 2017/4/19.
 */

public class VipSaleTranscation {

    public Context context = null;
    public String SecSeq;

    public PublicFunctions _PublicFunctions;

    public PayMode NowPayMode = PayMode.PAY;  //付款模式 Pay or Change or Balance
    public boolean IsTotalUseTWDPay = false;  //是否全用台幣付款

    //單據資訊
    public String PreorderNo;        //交易單據號碼
    public String ReceiptNo;         //交易單據號碼
    public Boolean AuthuticationFlag;//線上授權Flag Y or N

    //交易金額資訊
    public double OriUSDTotalAmount; //美金折扣前總價
    public double OriNTDTotalAmount; //台幣折扣前總價
    public double USDTotalAmount;    //美金應付款總金額
    public double NTDTotalAmount;    //台幣應付款總金額
    public double USDTotalPayed;     //美金已付款總金額
    public double NTDTotalPayed;     //台幣已付款總金額
    public double USDTotalUnpay;     //美金未付款總金額
    public double NTDTotalUnpay;     //台幣未付款總金額

    //折扣資訊
//    public String DiscountType;          //折扣會員類別
//    public String DiscountNo;            //折扣會員編號
    public double DiscountRate = 1;      //折扣會員%數
    //    public double DiscountAmount = 0;    //折扣會員金額
    public String UpperLimitType;        //刷卡上限會員類別
    public String UpperLimitDiscountNo;  //刷卡上限會員編號
    public int UpperLimit;               //刷卡上限金額

    public int getUpperLimit() {
        return UpperLimit;
    }

    public String getUpperLimitType() {
        return UpperLimitType;
    }

    //系統交易暫存
    public double LastPayAmount;   //最後付款金額
    public String LastPayCurrency; //最後付款幣別

    //信用卡資訊
    public String CardDate;        //信用卡效期
    public String CardName;        //信用卡持卡人姓名
    public String CardNo;          //信用卡號
    public String CardType;        //信用卡類別

    //商品清單
    public ArrayList<DFSItem> DFSItemList;

    //折扣清單
    public ArrayList<DiscountItem> DiscountList;

    //付款歷程
    public ArrayList<PaymentItem> PaymentList;

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
    public VipSaleTranscation(Context context, String SecSeq) {

        this.context = context;
        this.SecSeq = SecSeq;
        this.ReceiptNo = String.valueOf(GetNewReceiptNo());

        //初始變數
        this.AuthuticationFlag = false;
        this.DiscountRate = 1;           //折扣會員%數
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
        DFSItemList = new ArrayList<DFSItem>();

        //實作DiscountList
        DiscountList = new ArrayList<DiscountItem>();

        //實作PaymentList
        PaymentList = new ArrayList<PaymentItem>();
    }

    ////////////////////////////////////////////////////////////////////////
    //                            Basket
    ////////////////////////////////////////////////////////////////////////

    /**
     * 新增折扣列表
     *
     * @param Type       折扣代碼
     * @param DiscountNo 會員編號
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject AddDiscountList(String Type, String DiscountNo) {

        String ClassName = "VipSaleTranscation";
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

            //重新計算商品列表和折扣
            return GetBasketInfo(this.PreorderNo);

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

        String ClassName = "VipSaleTranscation";
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
    public JSONObject DeleteDiscountList(String Type) {

        String ClassName = "VipSaleTranscation";
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
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //逐項檢核既有折扣清單是否已有此折扣
            for (int i = 0; i < DiscountList.size(); i++) {

                if (DiscountList.get(i).Type.equals(Type)) {

                    //刪除折扣
                    DiscountList.remove(i);

                    //重新計算商品列表和折扣
                    return GetBasketInfo(this.PreorderNo);
                }
            }

            ReturnCode = "8";
            ReturnMessage = "Discount type not found.";

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
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
     * 取回購物車內容
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetBasketInfo(String PreorderNo) {

        String ClassName = "VipSaleTranscation";
        String FunctionName = "GetBasketInfo";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String ReturnCode, ReturnMessage;
        JSONArray jaItem;
        JSONObject ResponseJsonObject;
        String SQL;

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "PreorderNo:" + PreorderNo);

        try {

            //交易金額資訊
            this.OriUSDTotalAmount = 0; //美金折扣前總價
            this.OriNTDTotalAmount = 0; //台幣折扣前總價
            this.USDTotalAmount = 0;    //美金應付款總金額
            this.NTDTotalAmount = 0;    //台幣應付款總金額

            //折扣資訊
            this.UpperLimitType = "";   //刷卡上限會員類別
            this.UpperLimitDiscountNo = "";  //刷卡上限會員編號
            this.UpperLimit = 0;        //刷卡上限金額

            //region 逐一檢核每個折扣，若是主動式則先刪除，以被動式擇優
            for (int i = 0; i < DiscountList.size(); i++) {

                //若折扣為主動式折扣則刪除
                if (DiscountList.get(i).FuncID != null && DiscountList.get(i).FuncID.length() > 0) {
                    DiscountList.remove(i);
                    i--;
                    continue;
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

            //取得該Preorder所有的商品
            SQL = "SELECT PreorderHead.PreorderNO,MileDisc,ECouponCurrency,ECoupon,CardType,CardNo,CurDvr,PayAmt, " +
                    "Amount,Discount,PNR,PassengerName,ItemCode,SerialCode,ItemName,OriginalPrice,SalesPrice,SalesPriceTW,SalesQty " +
                    "FROM PreorderHead LEFT JOIN PreorderDetail " +
                    "ON PreorderHead.PreorderNO = PreorderDetail.PreorderNO " +
                    "WHERE PreorderHead.PreorderNO = '" + PreorderNo +
                    "' AND PreorderHead.SecSeq = '" + this.SecSeq +
                    "' AND PreorderHead.SaleFlag IN ('N','R')" +
                    " AND PreorderHead.PreorderType = '" + "VS" + "'";
            jaItem = _TSQL.SelectSQLJsonArray(SQL);

            //若商品不存在則跳錯
            if (jaItem == null || jaItem.length() == 0 || jaItem.getJSONObject(0).getString("ItemCode").length() == 0) {
                ReturnCode = "8";
                ReturnMessage = "The VIP No not exist: " + PreorderNo;

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //紀錄Preorder NO
            this.PreorderNo = PreorderNo;

            //帶出折扣率
            this.DiscountRate = jaItem.getJSONObject(0).getDouble("Discount");

            //帶出應付款總額
            DFSItemList.clear();
            for (int i = 0; i < jaItem.length(); i++) {
                DFSItem _DFSItem = new DFSItem();
                _DFSItem.ItemCode = jaItem.getJSONObject(i).getString("ItemCode");
                _DFSItem.SerialCode = jaItem.getJSONObject(i).getString("SerialCode");
                _DFSItem.ItemName = jaItem.getJSONObject(i).getString("ItemName");
                _DFSItem.OriginalPrice = jaItem.getJSONObject(i).getDouble("OriginalPrice");
                _DFSItem.USDPrice = jaItem.getJSONObject(i).getDouble("SalesPrice");
                _DFSItem.SalesQty = jaItem.getJSONObject(i).getInt("SalesQty");
                DFSItemList.add(_DFSItem);

                this.OriUSDTotalAmount = Arith.add(OriUSDTotalAmount, jaItem.getJSONObject(i).getDouble("OriginalPrice"));
                this.USDTotalAmount = Arith.add(USDTotalAmount, jaItem.getJSONObject(i).getDouble("SalesPrice"));
                this.NTDTotalAmount = Arith.add(NTDTotalAmount, jaItem.getJSONObject(i).getDouble("SalesPriceTW"));
            }

            //將計算結果轉為Response格式
            ResponseJsonObject = new JSONObject();
            ResponseJsonObject.put("PreorderNo", this.PreorderNo);
            ResponseJsonObject.put("ReceiptNo", this.ReceiptNo);
            ResponseJsonObject.put("OriUSDAmount", this.OriUSDTotalAmount);
            ResponseJsonObject.put("USDAmount", this.USDTotalAmount);
            ResponseJsonObject.put("TWDAmount", this.NTDTotalAmount);
            ResponseJsonObject.put("PNR", jaItem.getJSONObject(0).getString("PNR"));
            ResponseJsonObject.put("PassengerName", jaItem.getJSONObject(0).getString("PassengerName"));
            ResponseJsonObject.put("DiscountRate", jaItem.getJSONObject(0).getDouble("Discount"));
            ResponseJsonObject.put("UpperLimitType", this.UpperLimitType);
            ResponseJsonObject.put("UpperLimitDiscountNo", this.UpperLimitDiscountNo);
            ResponseJsonObject.put("UpperLimit", this.UpperLimit);

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

        String ClassName = "VipSaleTranscation";
        String FunctionName = "AddPaymentList";
        String ReturnCode, ReturnMessage;
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
                ReturnMessage = "This's not pay mode.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //檢核付款金額是否大於0
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

            int SwipeCount = 0;                      //已刷卡次數
            double SwipeUSDAmount = 0;               //已刷卡金額
            int LastLimitation = 0;                  //剩餘可刷卡金額
            //現金檢核
            if (PayBy == PaymentType.Cash) {

                //檢核付款金額是否符合該幣別最低應付金額
                if (Amount % _PublicFunctions.GetMiniValue(Currency) != 0) {
                    ReturnCode = "8";
                    ReturnMessage = "The amount of error.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //檢核所有幣別未付款金額是否大於0
                String[] currency = new String[]{"USD","TWD","JPY","CNY","HKD","GBP","EUR"};
                double USDunpayAmount = Arith.sub(USDTotalUnpay, USDAmount);  //直接帶美金最後應找零額
//                if(USDunpayAmount<0){
//                    ReturnCode = "8";
//                    ReturnMessage =  "Payment amount is not enough,please correct the amount or use credit card to pay.";
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
                            ReturnMessage = "Payment amount is not enough,please correct the amount or use credit card to pay.";
                            //寫入Log
                            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                        }
                    }
                }
            }
            //信用卡檢核
            if (PayBy == PaymentType.Card) {

                //已經使用過信用卡
                if (this.CardNo != null &&
                        !this.CardNo.equals("")) {
                    ReturnCode = "8";
                    ReturnMessage = "Already use other card.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //信用卡資料不齊全
                if ((CardNo == null || CardNo.length() == 0) ||
                        (CardName == null || CardName.length() == 0) ||
                        (CardDate == null || CardDate.length() == 0) ||
                        CardType == null) {
                    ReturnCode = "8";
                    ReturnMessage = "Please check credit card info.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //再檢核一次黑名單
                TempJo = _PublicFunctions.CheckBlackCard(String.valueOf(CardType), CardNo);
                if (!TempJo.getString("ReturnCode").equals("0")) {
                    return TempJo;
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
                        ReturnMessage = "Must be TWD or USD.";

                        for (int i = 0; i < TempJo.getJSONArray("ResponseData").length(); i++) {
                            ReturnMessage += TempJo.getJSONArray("ResponseData").getJSONObject(i).getString("Currency") + " or ";
                        }
                        ReturnMessage = ReturnMessage.substring(0, ReturnMessage.length() - 4);
                        ReturnMessage += ".";

                        //寫入Log
                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                        return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);

                    } else if (!IsRight && TempJo.getJSONArray("ResponseData").length() == 1) {

                        ReturnCode = "8";
                        ReturnMessage = "This card accepts " + TempJo.getJSONArray("ResponseData").getJSONObject(0).getString("Currency") + " only.";

                        //寫入Log
                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                        return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
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
                    ReturnMessage = "Over payment.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
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
                int MemberCardMaxUSDAmount = 0;       //此聯名卡最大可刷卡金額
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

                if (Arith.add(SwipeUSDAmount, USDAmount) > UpperLimit) {
                    ReturnCode = "8";
                    ReturnMessage = "This card had been used " + SwipeCount + " times, the last card limitation is USD " + LastLimitation;

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
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
                    ReturnMessage = "Coupon info error.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //檢核是否已經在此付款歷程使用過
                for (int i = 0; i < PaymentList.size(); i++) {
                    if (PaymentList.get(i).CouponNo != null && PaymentList.get(i).CouponNo.equals(CouponNo)) {
                        ReturnCode = "8";
                        ReturnMessage = "Coupon repeated.";

                        //寫入Log
                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                        return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
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
            ReturnMessage = ex.getMessage();
            return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
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

        String ClassName = "VipSaleTranscation";
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
            if ((IsTotalUseTWDPay && Currency.equals("TWD") && Amount > MaxChangeAmount) ||
                    (!IsTotalUseTWDPay && USDAmount > Arith.sub(USDTotalPayed, USDTotalAmount))) {
//            if (USDAmount > Arith.sub(USDTotalPayed,USDTotalAmount)) {
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

        String ClassName = "VipSaleTranscation";
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

        String ClassName = "VipSaleTranscation";
        String FunctionName = "ClearPaymentList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

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

    /**
     * 取得付款模式
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetPaymentMode() {

        String ClassName = "VipSaleTranscation";
        String FunctionName = "GetPaymentMode";
        String ReturnCode, ReturnMessage;
        JSONObject jo;
        JSONArray ja = new JSONArray();
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

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

        String ClassName = "VipSaleTranscation";
        String FunctionName = "GetCurrencyMaxAmount";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        double MaxPayAmount = 0;  //新幣別應付款金額
        JSONObject jo;
        JSONArray ja = new JSONArray();

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "Currency:" + Currency);

        try {

            //若全部都以台幣付款，則以台幣計價
            if (NowPayMode == PayMode.PAY &&
                    (IsTotalUseTWDPay && Currency.equals("TWD")) ||
                    (PaymentList.size() == 0 && Currency.equals("TWD"))) {

                jo = _PublicFunctions.ChangeCurrencyAmount("TWD", Currency, NTDTotalUnpay, 0, true, 2);

                if (!jo.getString("ReturnCode").equals("0")) {
                    return jo;
                }

                MaxPayAmount = jo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");
            }
            //找零模式下，若全部都以台幣付款，則其他幣別也由TWD換過去
            else if (NowPayMode == PayMode.CHANGE &&
                    IsTotalUseTWDPay) {

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
//                if (-1 < USDTotalUnpay && USDTotalUnpay < 0 ) {
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

        String ClassName = "VipSaleTranscation";
        String FunctionName = "SaveSalesInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        ArrayList SQLCommands = new ArrayList();
        String SQL;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //先判斷目前模式為何
            if (this.NowPayMode != PayMode.BALANCE) {
                ReturnCode = "8";
                ReturnMessage = "Not is balance mode.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //Preorder單頭
            SQL = "UPDATE PreorderHead " +
                    "SET SaleFlag = 'S' " +
                    ", EVASaleFlag = 'S' " +
                    "WHERE PreorderNO = '" + this.PreorderNo + "'";
            SQLCommands.add(SQL);


            // 放大折扣身分卡號大於等於14碼則為CUB, 需加密
            if (this.UpperLimitDiscountNo.length() >= 14) {
                this.UpperLimitDiscountNo = AESEncrypDecryp.getEncryptData(UpperLimitDiscountNo, FlightData.AESKey);
            }

            //銷售單頭
            SQL = "INSERT INTO SalesHead(SecSeq,ReceiptNo,SalesTime,RefundTime,OriPrice,TotalPrice," +
                    "UpperLimitType,UpperLimitNo,Status,PreorderNo,IFEOrderNo,SeatNo,AuthuticationFlag,OrderType) " +
                    "VALUES ('" + this.SecSeq +
                    "','" + this.ReceiptNo +
                    "','" + formatter.format(new Date(System.currentTimeMillis())) +
                    "','" + "" +
                    "'," + this.OriUSDTotalAmount +
                    "," + this.USDTotalAmount +
                    ",'" + this.UpperLimitType +
                    "','" + this.UpperLimitDiscountNo +
                    "','" + "S" +
                    "','" + this.PreorderNo +
                    "','" + "" +
                    "','" + "";

            if (this.AuthuticationFlag) {
                SQL += "','" + "Y";
            } else {
                SQL += "','" + "N";
            }

            SQL += "','" + "" + "')";
            SQLCommands.add(SQL);

            //銷售單身
            for (int i = 0; i < DFSItemList.size(); i++) {

                //銷售資料轉SQL
                SQL = "INSERT INTO SalesDetail(SecSeq,ReceiptNo,ItemCode,OriPrice,SalesPrice,SalesQty,Discount,VipType,VipNo,Status) " +
                        "VALUES ('" + this.SecSeq +
                        "','" + this.ReceiptNo +
                        "','" + DFSItemList.get(i).ItemCode +
                        "'," + DFSItemList.get(i).OriginalPrice +
                        "," + DFSItemList.get(i).USDPrice +
                        "," + DFSItemList.get(i).SalesQty +
                        "," + this.DiscountRate +
                        ",'" + "" +
                        "','" + "" +
                        "','" + "S" + "')";
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
                        "','" + this.PreorderNo + "')";
                SQLCommands.add(SQL);
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
    //                        public functions
    ////////////////////////////////////////////////////////////////////////

    /**
     * 取得新的交易單據
     *
     * @return (String) 新的交易單據
     */
    public int GetNewReceiptNo() {

        String ClassName = "VipSaleTransaction";
        String FunctionName = "GetNewReceiptNo";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        Object obj;

        JSONObject jo;
        JSONArray ja;

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

}
