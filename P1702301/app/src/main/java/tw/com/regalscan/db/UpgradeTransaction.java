package tw.com.regalscan.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;

import com.regalscan.sqlitelibrary.TSQL;
import org.json.JSONArray;
import org.json.JSONObject;
import tw.com.regalscan.component.AESEncrypDecryp;

/**
 * Created by gabehsu on 2017/4/19.
 */

public class UpgradeTransaction {

    public Context context = null;
    public String SecSeq;

    public PublicFunctions _PublicFunctions;

    public PayMode NowPayMode = PayMode.PAY;  //付款模式 Pay or Change or Balance
    public boolean IsTotalUseTWDPay = false;  //是否全用台幣付款

    //單據資訊
    public String ReceiptNo;         //交易單據號碼
    public Boolean AuthuticationFlag;//線上授權Flag Y or N

    //交易金額資訊
    public double USDTotalAmount;    //美金應付款總金額
    public double NTDTotalAmount;    //台幣應付款總金額
    public double USDTotalPayed;     //美金已付款總金額
    public double NTDTotalPayed;     //台幣已付款總金額
    public double USDTotalUnpay;     //美金未付款總金額
    public double NTDTotalUnpay;     //台幣未付款總金額

    //信用卡資訊
    public String CardDate;        //信用卡效期
    public String CardName;        //信用卡持卡人姓名
    public String CardNo;          //信用卡號
    public String CardType;        //信用卡類別

    //商品清單
    public ArrayList<UpgradeItem> UpgradeItemList;

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
    public UpgradeTransaction(Context context, String SecSeq) throws Exception {

        this.context = context;
        this.SecSeq = SecSeq;
        this.ReceiptNo = String.valueOf(GetNewReceiptNo());

        //初始變數
        AuthuticationFlag = false;

        //實作PublicFunctions
        _PublicFunctions = new PublicFunctions(context, SecSeq);

        //實作UpgradeItemList
        UpgradeItemList = new ArrayList<UpgradeItem>();

        //實作PaymentList
        PaymentList = new ArrayList<PaymentItem>();
    }

    ////////////////////////////////////////////////////////////////////////
    //                            Basket
    ////////////////////////////////////////////////////////////////////////

    /**
     * 新增、調整購物車商品
     *
     * @param Infant        商品料號
     * @param OriginalClass 原倉代號
     * @param NewClass      新倉代號
     * @param AdjustSaleQty 要調整的銷售數量 　加商品傳+   減商品傳-
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject ModifyItemList(String Infant, String OriginalClass, String NewClass, int AdjustSaleQty) {

        String ClassName = "VipSaleTranscation";
        String FunctionName = "ModifyItemList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONArray jaItem;

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "Infant:" + Infant + "-OriginalClass:" + OriginalClass + "-NewClass:" + NewClass + "-AdjustSaleQty:" + AdjustSaleQty);

        try {

            int SalesQty = 0;      //商品銷售量
            int ItemIndex = 0;      //商品列表Index

            //商品數量錯誤
            if (AdjustSaleQty == 0) {
                ReturnCode = "8";
                ReturnMessage = "Sales qty error.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //取得商品資訊
            String SQL = "SELECT Infant, OriginalClass, NewClass, USDPrice, TWDPrice " +
                "FROM ClassAllParts " +
                "WHERE Infant = '" + Infant +
                "' AND OriginalClass = '" + OriginalClass +
                "' AND NewClass = '" + NewClass + "'";
            jaItem = _TSQL.SelectSQLJsonArray(SQL);

            //若商品不存在則跳錯
            if (jaItem == null || jaItem.length() == 0 || jaItem.getJSONObject(0).getString("Infant").length() == 0) {
                ReturnCode = "8";
                ReturnMessage = "Upgrade info error ";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //取得既有購物車數量
            for (int i = 0; i < UpgradeItemList.size(); i++) {

                if (UpgradeItemList.get(i).Infant.equals(Infant) &&
                    UpgradeItemList.get(i).OriginalClass.equals(OriginalClass) &&
                    UpgradeItemList.get(i).NewClass.equals(NewClass)) {

                    SalesQty = UpgradeItemList.get(i).SalesQty;
                    ItemIndex = i;
                }
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
                UpgradeItemList.remove(ItemIndex);
            } else if (SalesQty == 0) {

                //新增購物車
                UpgradeItem _UpgradeItem = new UpgradeItem();
                _UpgradeItem.Infant = Infant;
                _UpgradeItem.OriginalClass = OriginalClass;
                _UpgradeItem.NewClass = NewClass;
                _UpgradeItem.USDPrice = jaItem.getJSONObject(0).optDouble("USDPrice");
                _UpgradeItem.TWDPrice = jaItem.getJSONObject(0).optDouble("TWDPrice");
                _UpgradeItem.SalesQty = AdjustSaleQty;

                UpgradeItemList.add(_UpgradeItem);
            } else {

                //調整購物車
                UpgradeItemList.get(ItemIndex).SalesQty += AdjustSaleQty;
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
     * 取回購物車內容
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetBasketInfo() {

        String ClassName = "VipSaleTranscation";
        String FunctionName = "GetBasketInfo";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String ReturnCode, ReturnMessage;
        JSONArray jaSalesItemList = new JSONArray();
        JSONObject joSalesItem, ResponseJsonObject;

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //交易金額資訊
            this.USDTotalAmount = 0;    //美金應付款總金額
            this.NTDTotalAmount = 0;    //台幣應付款總金額

            //計算商品總價，一併將商品列表轉為JSON
            for (int i = 0; i < UpgradeItemList.size(); i++) {

                //統計銷售折扣前總價
                this.USDTotalAmount += UpgradeItemList.get(i).USDPrice * UpgradeItemList.get(i).SalesQty;
                this.NTDTotalAmount += UpgradeItemList.get(i).TWDPrice * UpgradeItemList.get(i).SalesQty;

                joSalesItem = new JSONObject();
                joSalesItem.put("Infant", UpgradeItemList.get(i).Infant);
                joSalesItem.put("OriginalClass", UpgradeItemList.get(i).OriginalClass);
                joSalesItem.put("NewClass", UpgradeItemList.get(i).NewClass);
                joSalesItem.put("USDPrice", UpgradeItemList.get(i).USDPrice);
                joSalesItem.put("TWDPrice", UpgradeItemList.get(i).TWDPrice);
                joSalesItem.put("SalesQty", UpgradeItemList.get(i).SalesQty);
                jaSalesItemList.put(joSalesItem);
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
     * @param CardNo   信用卡號，不使用則傳Null
     * @param CardName 信用卡持卡人姓名，不使用則傳Null
     * @param CardDate 信用卡效期，不使用則傳Null
     * @param CardType 信用卡別，不使用則傳Null
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject AddPaymentList(String Currency, PaymentType PayBy, double Amount, String CardNo, String CardName, String CardDate, CreditCardType CardType) {

        String ClassName = "VipSaleTranscation";
        String FunctionName = "AddPaymentList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONObject TempJo;

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "Currency:" + Currency + "-PayBy:" + PayBy + "-Amount:" + Amount);

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

            //只允許一次全付
            if (Amount == MaxPayAmount) {

                //直接帶美金最後剩餘付款額
                USDAmount = USDTotalUnpay;
            } else {
                ReturnCode = "8";
                ReturnMessage = "Please check pay amount.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
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
            }
            //endregion

            //region 加入付款歷程
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
            } else {
                _PaymentItem.CardType = "";
                _PaymentItem.CardNo = "";
                _PaymentItem.CardName = "";
                _PaymentItem.CardDate = "";
            }
            PaymentList.add(_PaymentItem);

            //若為信用卡付款則新增到信用卡資訊內
            if (PayBy == PaymentType.Card) {
                this.CardNo = CardNo;
                this.CardDate = CardDate;
                this.CardName = CardName;
                this.CardType = String.valueOf(CardType);
            }
            //endregion

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
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject DeletePaymentList(String Currency, PaymentType PayBy) {

        String ClassName = "VipSaleTranscation";
        String FunctionName = "DeletePaymentList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "Currency:" + Currency + "-PayBy:" + PayBy);

        try {

            int RemoveIndex = -1;              //刪除的Index
            int CouponDifferenceIndex = -1;    //需先刪除的Coupon Index
            int ChangeIndex = -1;              //找零的代表Index

            //先找到指定刪除的付款歷程
            for (int i = 0; i < PaymentList.size(); i++) {

                //找要刪除的Index
                if (PaymentList.get(i).PayBy.equals(String.valueOf(PayBy)) &&
                    PaymentList.get(i).Currency.equals(Currency)) {

                    RemoveIndex = i;
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
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //清空付款列表
            PaymentList.clear();

            //清空信用卡資訊
            this.CardNo = "";
            this.CardDate = "";
            this.CardName = "";
            this.CardType = "";

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
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //付款金額歸零,重新計算
            USDTotalPayed = 0;           //美金已付款金額
            NTDTotalPayed = 0;           //台幣已付款金額
            USDTotalUnpay = USDTotalAmount;         //美金未付款金額
            NTDTotalUnpay = NTDTotalAmount;         //台幣未付款金額

            IsTotalUseTWDPay = PaymentList.size() != 0;

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

                //Pay mode
                NowPayMode = PayMode.PAY;

            } else if (USDTotalAmount == USDTotalPayed) {

                //Balance mode
                NowPayMode = PayMode.BALANCE;

            } else {

                ReturnCode = "8";
                ReturnMessage = "Pay mode error.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return _PublicFunctions.GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //串目前的付款里程
            if (PaymentList.size() > 0) {
                jo = new JSONObject();
                jo.put("Currency", PaymentList.get(0).Currency);
                jo.put("PayBy", PaymentList.get(0).PayBy);
                jo.put("Amount", PaymentList.get(0).Amount);
                jo.put("USDAmount", PaymentList.get(0).USDAmount);
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
            if ((IsTotalUseTWDPay && Currency.equals("TWD")) ||
                (PaymentList.size() == 0 && Currency.equals("TWD"))) {

                jo = _PublicFunctions.ChangeCurrencyAmount("TWD", Currency, NTDTotalUnpay, 0, true, 2);

                if (!jo.getString("ReturnCode").equals("0")) {
                    return jo;
                }

                MaxPayAmount = jo.getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");
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

            //銷售單頭
            SQL = "INSERT INTO ClassSalesHead (SecSeq,ReceiptNo,SalesTime,RefundTime,TotalPrice," +
                "Status,AuthuticationFlag) " +
                "VALUES ('" + this.SecSeq +
                "','" + this.ReceiptNo +
                "','" + formatter.format(new Date(System.currentTimeMillis())) +
                "','" + "" +
                "'," + USDTotalAmount +
                ",'" + "S";

            if (this.AuthuticationFlag) {
                SQL += "','" + "Y" + "')";
            } else {
                SQL += "','" + "N" + "')";
            }
            SQLCommands.add(SQL);

            //銷售單身
            for (int i = 0; i < UpgradeItemList.size(); i++) {

                //銷售資料轉SQL
                SQL = "INSERT INTO ClassSalesDetail (SecSeq,ReceiptNo,Infant,OriginalClass,NewClass,SalesPrice,SalesQty,Status) " +
                    "VALUES ('" + this.SecSeq +
                    "','" + this.ReceiptNo +
                    "','" + UpgradeItemList.get(i).Infant +
                    "','" + UpgradeItemList.get(i).OriginalClass +
                    "','" + UpgradeItemList.get(i).NewClass +
                    "'," + UpgradeItemList.get(i).USDPrice +
                    "," + UpgradeItemList.get(i).SalesQty +
                    ",'" + "S" + "')";
                SQLCommands.add(SQL);
            }

            //付款資料轉SQL
            SQL = "INSERT INTO ClassPaymentInfo (SecSeq,ReceiptNo,PayBy,Currency,Amount,CardType,CardNo,CardName,CardDate,USDAmount,Status) " +
                "VALUES ('" + this.SecSeq +
                "','" + this.ReceiptNo +
                "','" + PaymentList.get(0).PayBy +
                "','" + PaymentList.get(0).Currency +
                "'," + PaymentList.get(0).Amount;

            if (PaymentList.get(0).PayBy.equals("Card")) {
                SQL += ",'" + PaymentList.get(0).CardType +
                    "','" + AESEncrypDecryp.getEncryptData(PaymentList.get(0).CardNo, FlightData.AESKey) +
                    "','" + PaymentList.get(0).CardName +
                    "','" + AESEncrypDecryp.getEncryptData(PaymentList.get(0).CardDate, FlightData.AESKey);
            } else {
                SQL += ",'" + "" +
                    "','" + "" +
                    "','" + "" +
                    "','" + "";
            }

            SQL += "'," + PaymentList.get(0).USDAmount +
                ",'" + "S" + "')";
            SQLCommands.add(SQL);

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
    public int GetNewReceiptNo() {

        String ClassName = "VipSaleTranscation";
        String FunctionName = "GetNewReceiptNo";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        String SQL;
        Object obj;

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {
            //取得新的交易單據號碼
            SQL = "SELECT MAX(CAST(ReceiptNo AS int)) AS ReceiptNo " +
                "FROM ClassPaymentInfo ";

            obj = _TSQL.SelectSQLObject(SQL);

            if (obj != null && (int)obj > 0) {
                return (int)obj + 1;
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
