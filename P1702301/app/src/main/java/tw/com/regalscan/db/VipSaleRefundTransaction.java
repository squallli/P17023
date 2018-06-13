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

public class VipSaleRefundTransaction {

    public Context context;
    public String SecSeq;

    private PublicFunctions _PublicFunctions;

    private PayMode NowPayMode = PayMode.PAY;  //付款模式 Pay or Change or Balance
    private boolean IsTotalUseTWDPay = false;  //是否全用台幣付款
    private boolean IsTotalUseTWDRefund = false;  //是否全用台幣找零

    //單據資訊
    private String PreorderNo;        //地面預定單號
    private String ReceiptNo;         //交易單據號碼
    private Boolean AuthuticationFlag;//線上授權Flag Y or N

    //交易金額資訊
    private double USDTotalAmount;    //美金應找零總金額
    private double NTDTotalAmount;    //台幣應找零總金額
    private double USDTotalChanged;   //美金已找零總金額
    private double NTDTotalChanged;   //台幣應找零總金額
    private double USDTotalUnchange;  //美金未找零總金額
    private double NTDTotalUnchange;  //台幣應找零總金額
    //系統交易暫存
    private double LastChangeAmount;   //最後付款金額
    private String LastChangeCurrency; //最後付款幣別

    //商品列表
    private ArrayList<DFSItem> DFSItemList;

    //付款歷程
    private ArrayList<PaymentItem> PaymentList;

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
    public VipSaleRefundTransaction(Context context, String SecSeq) {

        this.context = context;
        this.SecSeq = SecSeq;

        //初始變數
        AuthuticationFlag = false;

        //實作PublicFunctions
        _PublicFunctions = new PublicFunctions(context, SecSeq);

        //實作PaymentList
        DFSItemList = new ArrayList<>();

        //實作PaymentList
        PaymentList = new ArrayList<>();

        //實作原始PaymentList
        OriginalPaymentList = new ArrayList<>();
    }

    ////////////////////////////////////////////////////////////////////////
    //                            Basket
    ////////////////////////////////////////////////////////////////////////

    /**
     * 取得退貨商品列表
     *
     * @param ReceiptNo 要退款的Vip sales單據號碼
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetBasketInfo(String ReceiptNo) {

        String ClassName = "VipSaleRefundTransaction";
        String FunctionName = "GetBasketInfo";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String ReturnCode, ReturnMessage;
        JSONArray jaSalesItemList = new JSONArray(), jaItem;
        JSONObject joSalesItem, ResponseJsonObject;
        String SQL;

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "ReceiptNo:" + ReceiptNo);

        try {

            //檢核單據號碼是否允許進行VIP退貨
            SQL = "SELECT SalesHead.TotalPrice, SalesHead.ReceiptNo, SalesHead.AuthuticationFlag, 'D1' AS DrawNo, SerialCode, " +
                    "ItemCode, ItemName, OriginalPrice, SalesQty, SalesHead.PreorderNo " +
                    "FROM SalesHead LEFT JOIN PreorderDetail " +
                    "ON SalesHead.PreorderNo = PreorderDetail.PreorderNo " +
                    "WHERE SalesHead.Status = 'S' " +
                    "AND SalesHead.ReceiptNo = '" + ReceiptNo +
                    "' AND SalesHead.SecSeq = '" + this.SecSeq +
                    "' AND SalesHead.PreorderNo <> ''";
            jaItem = _TSQL.SelectSQLJsonArray(SQL);

            if (jaItem == null || jaItem.length() == 0) {
                ReturnCode = "8";
                ReturnMessage = "Receipt no not found.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //帶出交易資訊
            this.ReceiptNo = ReceiptNo;
            this.PreorderNo = jaItem.getJSONObject(0).getString("PreorderNo");
            this.USDTotalAmount = jaItem.getJSONObject(0).getDouble("TotalPrice");
            this.NTDTotalAmount = 0;
            this.USDTotalChanged = 0;
            this.NTDTotalChanged = 0;
            this.USDTotalUnchange = 0;
            this.NTDTotalUnchange = 0;

            this.AuthuticationFlag = jaItem.getJSONObject(0).getString("AuthuticationFlag").equals("Y");

            //帶出商品列表
            DFSItemList.clear();
            for (int i = 0; i < jaItem.length(); i++) {
                DFSItem _DFSItem = new DFSItem();
                _DFSItem.ItemCode = jaItem.getJSONObject(i).getString("ItemCode");
                _DFSItem.SerialCode = jaItem.getJSONObject(i).getString("SerialCode");
                _DFSItem.DrawerNo = jaItem.getJSONObject(i).getString("DrawNo");
                _DFSItem.ItemName = jaItem.getJSONObject(i).getString("ItemName");
                _DFSItem.OriginalPrice = jaItem.getJSONObject(i).getDouble("OriginalPrice");
                _DFSItem.SalesQty = jaItem.getJSONObject(i).getInt("SalesQty");
                DFSItemList.add(_DFSItem);

                //將銷售品轉JSON
                joSalesItem = new JSONObject();
                joSalesItem.put("ItemCode", DFSItemList.get(i).ItemCode);
                joSalesItem.put("SerialCode", DFSItemList.get(i).SerialCode);
                joSalesItem.put("DrawerNo", DFSItemList.get(i).DrawerNo);
                joSalesItem.put("ItemName", DFSItemList.get(i).ItemName);
                joSalesItem.put("OriginalPrice", DFSItemList.get(i).OriginalPrice);
                joSalesItem.put("SalesQty", DFSItemList.get(i).SalesQty);
                jaSalesItemList.put(joSalesItem);
            }

            //取得付款紀錄
            SQL = "SELECT PayBy, Currency, Amount, CardType, CardNo, CardName, CardDate, USDAmount, CouponNo " +
                    "FROM PaymentInfo " +
                    "WHERE SecSeq = '" + this.SecSeq +
                    "' AND ReceiptNo = '" + this.ReceiptNo + "'";
            jaItem = _TSQL.SelectSQLJsonArray(SQL);

            if (jaItem == null || jaItem.length() == 0) {
                ReturnCode = "8";
                ReturnMessage = "Receipt no not found.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            Double totalAmount = 0.0;
            boolean isTotalUseUSDPay = true;

            //帶出原先付款紀錄，並判斷當初是否為全台幣付款
            IsTotalUseTWDPay = true;
            OriginalPaymentList.clear();
            PaymentList.clear();
            for (int i = 0; i < jaItem.length(); i++) {
                if (!jaItem.getJSONObject(i).getString("Currency").equals("TWD")) {
                    IsTotalUseTWDPay = false;
                }

                if (!jaItem.getJSONObject(i).getString("Currency").equals("USD")) {
                    isTotalUseUSDPay = false;
                }

                //依照付款歷程， 重新計算退款金額
                JSONObject TempJo = _PublicFunctions.ChangeCurrencyAmount(jaItem.getJSONObject(i).getString("Currency"), "USD", jaItem.getJSONObject(i).getDouble("Amount"), 0.01,
                        false, 2);

                if (!TempJo.getString("ReturnCode").equals("0")) {
                    return TempJo;
                }

                if (jaItem.getJSONObject(i).getString("PayBy").equals("Change")) {
                    totalAmount -= TempJo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");
                } else {
                    totalAmount += TempJo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");
                }

                //計算台幣總付款金額，，若為全台幣付款以及全台幣找零才用到的到
                if (!jaItem.getJSONObject(i).getString("PayBy").equals("Change") &&
                        jaItem.getJSONObject(i).getString("Currency").equals("TWD")) {
                    NTDTotalAmount = Arith.add(NTDTotalAmount, jaItem.getJSONObject(i).getDouble("Amount"));
                } else {
                    NTDTotalAmount = Arith.sub(NTDTotalAmount, jaItem.getJSONObject(i).getDouble("Amount"));
                }

                //原始付款歷程
                PaymentItem _PaymentItem = new PaymentItem();
                _PaymentItem.Currency = jaItem.getJSONObject(i).getString("Currency");
                _PaymentItem.PayBy = jaItem.getJSONObject(i).getString("PayBy");
                _PaymentItem.Amount = jaItem.getJSONObject(i).getDouble("Amount");
                _PaymentItem.USDAmount = jaItem.getJSONObject(i).getDouble("USDAmount");
                _PaymentItem.CouponUSDAmount = jaItem.getJSONObject(i).getDouble("USDAmount");
                _PaymentItem.CouponNo = jaItem.getJSONObject(i).getString("CouponNo");

                if (_PaymentItem.PayBy.equals("Card")) {
                    _PaymentItem.CardType = jaItem.getJSONObject(i).getString("CardType");
                    _PaymentItem.CardNo = AESEncrypDecryp.getDectyptData(jaItem.getJSONObject(i).getString("CardNo"), FlightData.AESKey);
                    _PaymentItem.CardName = jaItem.getJSONObject(i).getString("CardName");
                    _PaymentItem.CardDate = AESEncrypDecryp.getDectyptData(jaItem.getJSONObject(i).getString("CardDate"), FlightData.AESKey);
                } else {
                    _PaymentItem.CardType = "";
                    _PaymentItem.CardNo = "";
                    _PaymentItem.CardName = "";
                    _PaymentItem.CardDate = "";
                }
                OriginalPaymentList.add(_PaymentItem);

                //若信用卡或Coupon no則帶到新付款歷程當作預設找零
                if (jaItem.getJSONObject(i).getString("PayBy").equals("Card") ||
                        jaItem.getJSONObject(i).getString("PayBy").equals("SC") ||
                        jaItem.getJSONObject(i).getString("PayBy").equals("DC")) {
                    PaymentList.add(_PaymentItem);
                }
            }

            if (!isTotalUseUSDPay) {
                this.USDTotalAmount = totalAmount;
            }

            //將計算結果轉為Response格式
            ResponseJsonObject = new JSONObject();
            ResponseJsonObject.put("ReceiptNo", this.ReceiptNo);
            ResponseJsonObject.put("PreorderNo", this.PreorderNo);
            ResponseJsonObject.put("USDAmount", this.USDTotalAmount);
            ResponseJsonObject.put("Items", jaSalesItemList);

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
     * 新增退款列表
     *
     * @param Currency 退款幣別
     * @param Amount   退款金額
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject AddRefundList(String Currency, double Amount) {

        String ClassName = "VipSaleRefundTransaction";
        String FunctionName = "GetBasketInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONObject TempJo;

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "Currency:" + Currency + "-Amount:" + Amount);

        try {

            //模式檢核
            if (NowPayMode != PayMode.PAY) {
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

            //取得該金額所代表美金金額
            if ((Currency.equals(LastChangeCurrency) && Amount == LastChangeAmount) ||
                    (Amount == MaxChangeAmount)) {

                //直接帶美金最後應找零額
                USDAmount = USDTotalUnchange;
            } else {
                //先換算付款金額所代表之美金
                TempJo = _PublicFunctions.ChangeCurrencyAmount(Currency, "USD", Amount, 0.01, false, 2);
                if (!TempJo.getString("ReturnCode").equals("0")) {
                    return TempJo;
                }

                USDAmount = TempJo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");
            }

            //已找零金額 + 此次找零金額 > 需找零總額,則提示錯誤，其他幣別因為已於上面換算過USD，所以都已USD為準。
            if ((IsTotalUseTWDPay && IsTotalUseTWDRefund && Currency.equals("TWD") && Amount > MaxChangeAmount) ||
                    ((!IsTotalUseTWDPay || !IsTotalUseTWDRefund) && Arith.add(USDTotalChanged, USDAmount) > USDTotalAmount)) {
//            if (Arith.add(USDTotalChanged,USDAmount) > USDTotalAmount) {
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
                if (PaymentList.get(i).PayBy.equals("Cash") &&
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
                _PaymentItem.PayBy = "Cash";
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
            LastChangeCurrency = Currency;                        //最後交易幣別
            LastChangeAmount = Arith.sub(MaxChangeAmount, Amount); //該幣別找零金額

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
     * 刪除退款列表
     *
     * @param Currency 退款幣別
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject DeleteRefundList(String Currency) {

        String ClassName = "VipSaleRefundTransaction";
        String FunctionName = "DeleteRefundList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "Currency:" + Currency);

        try {

            int RemoveIndex = -1;              //刪除的Index

            //先找到指定刪除的付款歷程
            for (int i = 0; i < PaymentList.size(); i++) {

                //找要刪除的Index
                if (PaymentList.get(i).PayBy.equals("Cash") &&
                        PaymentList.get(i).Currency.equals(Currency)) {
                    RemoveIndex = i;
                }
            }

            //沒有找到
            if (RemoveIndex == -1) {
                ReturnCode = "8";
                ReturnMessage = "Refund info not found.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            PaymentList.remove(RemoveIndex);

            //清空最後付款幣別
            LastChangeCurrency = "";         //最後交易幣別
            LastChangeAmount = 0;            //該幣別找零金額

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

        String ClassName = "VipSaleRefundTransaction";
        String FunctionName = "ClearPaymentList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //清空付款列表
            PaymentList.clear();

            LastChangeCurrency = "";         //最後交易幣別
            LastChangeAmount = 0;            //該幣別找零金額

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

        String ClassName = "VipSaleRefundTransaction";
        String FunctionName = "GetBasketInfo";
        String ReturnCode, ReturnMessage;
        JSONObject jo;
        JSONArray ja = new JSONArray(), jaItem = new JSONArray();
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {
            //付款金額歸零,重新計算
            USDTotalChanged = 0;           //美金已找零金額
            NTDTotalChanged = 0;           //台幣已找零金額
            USDTotalUnchange = USDTotalAmount;         //美金未找零金額
            NTDTotalUnchange = NTDTotalAmount;         //台幣未找零金額

//            if (PaymentList.size() == 0)
//                IsTotalUseTWDRefund = false;
//            else
            IsTotalUseTWDRefund = true;

            for (int i = 0; i < PaymentList.size(); i++) {

                //判斷是不是全部台幣付款
                if (!PaymentList.get(i).Currency.equals("TWD")) {
                    IsTotalUseTWDRefund = false;
                }

                //計算美金已退款金額
                USDTotalChanged = Arith.add(USDTotalChanged, PaymentList.get(i).USDAmount);
                USDTotalUnchange = Arith.sub(USDTotalUnchange, PaymentList.get(i).USDAmount);

                //計算台幣已付款金額
                if (PaymentList.get(i).Currency.equals("TWD")) {
                    NTDTotalChanged = Arith.add(NTDTotalChanged, PaymentList.get(i).Amount);
                    NTDTotalUnchange = Arith.sub(NTDTotalUnchange, PaymentList.get(i).Amount);
                }
            }

            //判斷付款模式，不以台幣為準，以美金為準，全台幣應付款金額由GetCurrencyMaxAmount帶出
            if ((USDTotalAmount == 0) ||
                    (NTDTotalAmount == 0) ||
                    (USDTotalAmount > USDTotalChanged)) {
//                    (!IsTotalUseTWDPay && USDTotalAmount > USDTotalPayed) ||
//                    (IsTotalUseTWDPay && NTDTotalAmount > NTDTotalPayed)) {

                //Pay mode
                NowPayMode = PayMode.PAY;

            } else if (USDTotalAmount == USDTotalChanged) {
//            } else if ((!IsTotalUseTWDPay && USDTotalAmount == USDTotalPayed) ||
//                    (IsTotalUseTWDPay && NTDTotalAmount == NTDTotalPayed)) {

                //Balance mode
                NowPayMode = PayMode.BALANCE;

            } else {

                //顛倒尾款正負
                if (LastChangeAmount < 0) {
                    LastChangeAmount = Arith.sub(0, LastChangeAmount);      //該幣別剩餘金額
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
                ja.put(jo);
            }

            //串目前付款資訊
            jo = new JSONObject();
            jo.put("ReceiptNo", this.ReceiptNo);
            jo.put("NowPayMode", String.valueOf(NowPayMode));
            jo.put("USDTotalAmount", USDTotalAmount);
            jo.put("USDTotalPayed", USDTotalChanged);
            jo.put("USDTotalUnpay", USDTotalUnchange);

            if (NowPayMode != PayMode.BALANCE &&
                    LastChangeCurrency != null &&
                    LastChangeCurrency.length() > 0) {
                jo.put("LastPayCurrency", LastChangeCurrency);
                jo.put("LastPayAmount", LastChangeAmount);
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
     * 取得原始付款模式
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetOriginalPaymentMold() {

        String ClassName = "VipSaleRefundTransaction";
        String FunctionName = "GetOriginalPaymentMold";
        String ReturnCode, ReturnMessage;
        JSONObject jo;
        JSONArray ja = new JSONArray();
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

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
            jo.put("LastPayCurrency", "");
            jo.put("LastPayAmount", 0);
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

        String ClassName = "VipSaleRefundTransaction";
        String FunctionName = "GetCurrencyMaxAmount";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        double MaxPayAmount;  //新幣別應付款金額
        JSONObject jo;
        JSONArray ja = new JSONArray();

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "Currency:" + Currency);

        try {

            //若全部都以台幣付款，則以台幣計價
            if ((IsTotalUseTWDPay && IsTotalUseTWDRefund && Currency.equals("TWD") ||
                    (IsTotalUseTWDPay && PaymentList.size() == 0 && Currency.equals("TWD")))) {

                jo = _PublicFunctions.ChangeCurrencyAmount("TWD", Currency, NTDTotalUnchange, 0, true, 2);

                if (!jo.getString("ReturnCode").equals("0")) {
                    return jo;
                }

                MaxPayAmount = jo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");
            }
            //其餘的幣別
            else {

                //先試算剩餘需refund金額是否會因為四捨五入關係多給錢
                jo = _PublicFunctions.ChangeCurrencyAmount("USD", Currency, USDTotalUnchange, 0, true, 2);

                if (!jo.getString("ReturnCode").equals("0")) {
                    return jo;
                }

//                jo = _PublicFunctions.ChangeCurrencyAmount(Currency, "USD", jo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount"), 0.01, true, 2);
//
//                if (!jo.getString("ReturnCode").equals("0")) {
//                    return jo;
//                }
//
//                //如果四捨五入後，會多給錢改用無條件捨去計算
//                if (jo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount") > USDTotalUnchange) {
//                    jo = _PublicFunctions.ChangeCurrencyAmount("USD", Currency, USDTotalUnchange, 0, true, 3);
//                } else {
//                    jo = _PublicFunctions.ChangeCurrencyAmount("USD", Currency, USDTotalUnchange, 0, true, 2);
//                }
//
//                if (!jo.getString("ReturnCode").equals("0")) {
//                    return jo;
//                }

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

    /**
     * 取得退款總金額 (符合最低應付款進位)
     *
     * @param Currency 要查詢的幣別
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetRefundMaxAmount(String Currency) {

        String ClassName = "VipSaleRefundTransaction";
        String FunctionName = "GetRefundMaxAmount";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        double MaxPayAmount = 0;  //新幣別應付款金額
        JSONObject jo;
        JSONArray ja = new JSONArray();

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "Currency:" + Currency);

        try {

            //將未付款餘額取最低付款額，採四捨五入
            jo = _PublicFunctions.ChangeCurrencyAmount("USD", Currency, USDTotalAmount, 0, true, 2);

            if (!jo.getString("ReturnCode").equals("0")) {
                return jo;
            }

            MaxPayAmount = jo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");

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
     * 進行免稅品退貨(原始付款方式)
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject SaveRefundInfoByOriginal() {

        String ClassName = "VipSaleRefundTransaction";
        String FunctionName = "SaveRefundInfoByOriginal";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        ArrayList SQLCommands = new ArrayList();
        String SQL;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //退貨單頭
            SQL = "UPDATE SalesHead " +
                    "SET RefundTime = '" + formatter.format(new Date(System.currentTimeMillis())) +
                    "', Status = 'R' " +
                    "WHERE SecSeq = '" + this.SecSeq +
                    "' AND ReceiptNo = '" + this.ReceiptNo + "'";
            SQLCommands.add(SQL);

            //退貨單身
            SQL = "UPDATE SalesDetail " +
                    "SET Status = 'R' " +
                    "WHERE SecSeq = '" + this.SecSeq +
                    "' AND ReceiptNo = '" + this.ReceiptNo + "'";
            SQLCommands.add(SQL);

            //Preorder單頭
            SQL = "UPDATE PreorderHead " +
                    "SET SaleFlag = 'R', EVASaleFlag = 'S' " +
                    "WHERE PreorderNO = '" + this.PreorderNo + "'";
            SQLCommands.add(SQL);

            //退貨資料轉SQL
            for (int i = 0; i < OriginalPaymentList.size(); i++) {
                SQL = "INSERT INTO PaymentInfo(SecSeq,ReceiptNo,PayBy,Currency,Amount,CardType,CardNo,CardName,CardDate,USDAmount,CouponNo,Status,PreorderNo) " +
                        "VALUES ('" + this.SecSeq +
                        "','" + this.ReceiptNo +
                        "','" + OriginalPaymentList.get(i).PayBy +
                        "','" + OriginalPaymentList.get(i).Currency;

                SQL += "'," + OriginalPaymentList.get(i).Amount;

                if (OriginalPaymentList.get(i).PayBy.equals("Card")) {
                    SQL += ",'" + OriginalPaymentList.get(i).CardType +
                            "','" + AESEncrypDecryp.getEncryptData(OriginalPaymentList.get(i).CardNo, FlightData.AESKey) +
                            "','" + OriginalPaymentList.get(i).CardName +
                            "','" + AESEncrypDecryp.getEncryptData(OriginalPaymentList.get(i).CardDate, FlightData.AESKey);
                } else {
                    SQL += ",'" + "" +
                            "','" + "" +
                            "','" + "" +
                            "','" + "";
                }

                SQL += "','" + OriginalPaymentList.get(i).USDAmount;

                SQL += "','" + OriginalPaymentList.get(i).CouponNo +
                        "','" + "R" +
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

    /**
     * 進行免稅品退貨(新付款方式)
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject SaveRefundInfoByNew() {

        String ClassName = "VipSaleRefundTransaction";
        String FunctionName = "SaveRefundInfoByNew";
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

            //退貨單頭
            SQL = "UPDATE SalesHead " +
                    "SET RefundTime = '" + formatter.format(new Date(System.currentTimeMillis())) +
                    "', Status = 'R' " +
                    "WHERE SecSeq = '" + this.SecSeq +
                    "' AND ReceiptNo = '" + this.ReceiptNo + "'";
            SQLCommands.add(SQL);

            //退貨單身
            SQL = "UPDATE SalesDetail " +
                    "SET Status = 'R' " +
                    "WHERE SecSeq = '" + this.SecSeq +
                    "' AND ReceiptNo = '" + this.ReceiptNo + "'";
            SQLCommands.add(SQL);

            //Preorder單頭
            SQL = "UPDATE PreorderHead " +
                    "SET SaleFlag = 'R' " +
                    "WHERE PreorderNO = '" + this.PreorderNo + "'";
            SQLCommands.add(SQL);

            //退貨資料轉SQL
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
                        "','" + "R" +
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
