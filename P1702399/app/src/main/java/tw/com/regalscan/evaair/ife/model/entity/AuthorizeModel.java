package tw.com.regalscan.evaair.ife.model.entity;

import java.util.Objects;

import org.json.JSONObject;
import tw.com.regalscan.component.AESEncrypDecryp;
import tw.com.regalscan.db.FlightData;

/**
 * Created by tp00175 on 2017/11/8.
 */


public class AuthorizeModel {

    //傳送參數
    private String CREDIT_CARD_NO;   //信用卡號
    private String EXP_YEAR;         //卡片到期日(年)
    private String EXP_MONTH;        //卡片到期日(月)
    private String CREDIT_CARD_NAME; //持卡人姓名
    private String CREDIT_CARD_TYPE; //信用卡別
    private int RECIPT_NO;           //POS收據號碼
    private int TWD_AMT;             //交易金額(台幣)
    private int USD_AMT;             //交易金額(美金)
    private String DEPT_FLT_NO;      //航班
    private String Currency;         //付款幣別
    private String CA_NO;            //CA_NO
    private String DEPT_DATE;        //航班日期
    private String DOC_NO;           //車號
    private String IFE_SEQ;          //IFE訂單號碼
    private String REAUTH_MARK;      //是否取消授權
    private int SECTOR_SEQ;          //航段
    private String UG_MARK;          //是否為Upgrade卡
    private String VIP_NO;           //折扣號碼
    private String VIP_TYPE;         //折扣種類
    private String ORDER_NO;         //VIP機上付款，訂單號碼

    //回傳參數
    private String APPROVE_CODE;     //授權碼
    private String RSPONSE_CODE;     //授權回應碼
    private String RSPONSE_MSG;      //授權回應訊息
    private String TRANS_DATE;       //交易日
    private String TRANS_TIME;       //交易時間
    private String AUTH_DATE;        //授權日
    private String TRANS_SEQNO;      //交易流水編號
    private String TRANS_CODE;       //執行結果
    private String AUTH_RETCODE;     //授權回應碼
    private double ResponseSeconds;
    private String StartTime;
    private String EndTime;

    public AuthorizeModel() {

    }

    public AuthorizeModel(JSONObject jsonObject) {
        try {
            SECTOR_SEQ = Objects.equals(jsonObject.optString("SecSeq"), "") ? 0 : Integer.valueOf(jsonObject.optString("SecSeq"));
            RECIPT_NO = jsonObject.optInt("ReceiptNo");
            UG_MARK = jsonObject.optString("UG_MARK");
            CREDIT_CARD_NO = AESEncrypDecryp.getDectyptData(jsonObject.optString("CardNo"), FlightData.AESKey);
            CREDIT_CARD_NAME = jsonObject.optString("CardName");
            CREDIT_CARD_TYPE = jsonObject.optString("CardType");
            EXP_MONTH = AESEncrypDecryp.getDectyptData(jsonObject.optString("ExpireDate"), FlightData.AESKey).substring(0, 2);
            EXP_YEAR = AESEncrypDecryp.getDectyptData(jsonObject.optString("ExpireDate"), FlightData.AESKey).substring(2, 3);
            Currency = jsonObject.optString("CurrencyType");
            TWD_AMT = Currency.equals("TWD") ? jsonObject.optInt("TransAmount") : 0;
            USD_AMT = Currency.equals("USD") ? jsonObject.optInt("TransAmount") : 0;
            IFE_SEQ = jsonObject.optString("OrderNo");
            ORDER_NO = jsonObject.optString("PreorderNo");
            VIP_NO = jsonObject.optString("VipNo").length() > 12 ? AESEncrypDecryp.getDectyptData(jsonObject.optString("VipNo"), FlightData.AESKey) : jsonObject.optString("VipNo");
            VIP_TYPE = jsonObject.optString("VipType");
            REAUTH_MARK = jsonObject.optString("REAUTH_MARK");
            APPROVE_CODE = jsonObject.optString("APPROVE_CODE");
            RSPONSE_CODE = jsonObject.optString("RSPONSE_CODE");
            RSPONSE_MSG = jsonObject.optString("RSPONSE_MSG");
            TRANS_DATE = jsonObject.optString("TRANS_DATE");
            TRANS_TIME = jsonObject.optString("TRANS_TIME");
            AUTH_DATE = jsonObject.optString("AUTH_DATE");
            TRANS_SEQNO = jsonObject.optString("TRANS_SEQNO");
            TRANS_CODE = jsonObject.optString("TRANS_CODE");
            AUTH_RETCODE = jsonObject.optString("AUTH_RETCODE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCREDIT_CARD_NO() {
        return CREDIT_CARD_NO;
    }

    public void setCREDIT_CARD_NO(String CREDIT_CARD_NO) {
        this.CREDIT_CARD_NO = CREDIT_CARD_NO;
    }

    public String getEXP_YEAR() {
        return EXP_YEAR;
    }

    public void setEXP_YEAR(String EXP_YEAR) {
        this.EXP_YEAR = EXP_YEAR;
    }

    public String getEXP_MONTH() {
        return EXP_MONTH;
    }

    public void setEXP_MONTH(String EXP_MONTH) {
        this.EXP_MONTH = EXP_MONTH;
    }

    public String getCREDIT_CARD_NAME() {
        return CREDIT_CARD_NAME;
    }

    public void setCREDIT_CARD_NAME(String CREDIT_CARD_NAME) {
        this.CREDIT_CARD_NAME = CREDIT_CARD_NAME;
    }

    public String getCREDIT_CARD_TYPE() {
        return CREDIT_CARD_TYPE;
    }

    public void setCREDIT_CARD_TYPE(String CREDIT_CARD_TYPE) {
        this.CREDIT_CARD_TYPE = CREDIT_CARD_TYPE;
    }

    public int getRECIPT_NO() {
        return RECIPT_NO;
    }

    public void setRECIPT_NO(int RECIPT_NO) {
        this.RECIPT_NO = RECIPT_NO;
    }

    public int getTWD_AMT() {
        return TWD_AMT;
    }

    public void setTWD_AMT(int TWD_AMT) {
        this.TWD_AMT = TWD_AMT;
    }

    public int getUSD_AMT() {
        return USD_AMT;
    }

    public void setUSD_AMT(int USD_AMT) {
        this.USD_AMT = USD_AMT;
    }

    public String getDEPT_FLT_NO() {
        return DEPT_FLT_NO;
    }

    public void setDEPT_FLT_NO(String DEPT_FLT_NO) {
        this.DEPT_FLT_NO = DEPT_FLT_NO;
    }

    public String getCurrency() {
        return Currency;
    }

    public void setCurrency(String currency) {
        Currency = currency;
    }

    public String getCA_NO() {
        return CA_NO;
    }

    public void setCA_NO(String CA_NO) {
        this.CA_NO = CA_NO;
    }

    public String getDEPT_DATE() {
        return DEPT_DATE;
    }

    public void setDEPT_DATE(String DEPT_DATE) {
        this.DEPT_DATE = DEPT_DATE;
    }

    public String getDOC_NO() {
        return DOC_NO;
    }

    public void setDOC_NO(String DOC_NO) {
        this.DOC_NO = DOC_NO;
    }

    public String getIFE_SEQ() {
        return IFE_SEQ;
    }

    public void setIFE_SEQ(String IFE_SEQ) {
        this.IFE_SEQ = IFE_SEQ;
    }

    public String getREAUTH_MARK() {
        return REAUTH_MARK;
    }

    public void setREAUTH_MARK(String REAUTH_MARK) {
        this.REAUTH_MARK = REAUTH_MARK;
    }

    public int getSECTOR_SEQ() {
        return SECTOR_SEQ;
    }

    public void setSECTOR_SEQ(int SECTOR_SEQ) {
        this.SECTOR_SEQ = SECTOR_SEQ;
    }

    public String getUG_MARK() {
        return UG_MARK;
    }

    public void setUG_MARK(String UG_MARK) {
        this.UG_MARK = UG_MARK;
    }

    public String getVIP_NO() {
        return VIP_NO;
    }

    public void setVIP_NO(String VIP_NO) {
        this.VIP_NO = VIP_NO;
    }

    public String getVIP_TYPE() {
        return VIP_TYPE;
    }

    public void setVIP_TYPE(String VIP_TYPE) {
        this.VIP_TYPE = VIP_TYPE;
    }

    public String getORDER_NO() {
        return ORDER_NO;
    }

    public void setORDER_NO(String ORDER_NO) {
        this.ORDER_NO = ORDER_NO;
    }

    public String getAPPROVE_CODE() {
        return APPROVE_CODE;
    }

    public void setAPPROVE_CODE(String APPROVE_CODE) {
        this.APPROVE_CODE = APPROVE_CODE;
    }

    public String getRSPONSE_CODE() {
        return RSPONSE_CODE;
    }

    public void setRSPONSE_CODE(String RSPONSE_CODE) {
        this.RSPONSE_CODE = RSPONSE_CODE;
    }

    public String getRSPONSE_MSG() {
        return RSPONSE_MSG;
    }

    public void setRSPONSE_MSG(String RSPONSE_MSG) {
        this.RSPONSE_MSG = RSPONSE_MSG;
    }

    public String getTRANS_DATE() {
        return TRANS_DATE;
    }

    public void setTRANS_DATE(String TRANS_DATE) {
        this.TRANS_DATE = TRANS_DATE;
    }

    public String getTRANS_TIME() {
        return TRANS_TIME;
    }

    public void setTRANS_TIME(String TRANS_TIME) {
        this.TRANS_TIME = TRANS_TIME;
    }

    public String getAUTH_DATE() {
        return AUTH_DATE;
    }

    public void setAUTH_DATE(String AUTH_DATE) {
        this.AUTH_DATE = AUTH_DATE;
    }

    public String getTRANS_SEQNO() {
        return TRANS_SEQNO;
    }

    public void setTRANS_SEQNO(String TRANS_SEQNO) {
        this.TRANS_SEQNO = TRANS_SEQNO;
    }

    public String getTRANS_CODE() {
        return TRANS_CODE;
    }

    public void setTRANS_CODE(String TRANS_CODE) {
        this.TRANS_CODE = TRANS_CODE;
    }

    public String getAUTH_RETCODE() {
        return AUTH_RETCODE;
    }

    public void setAUTH_RETCODE(String AUTH_RETCODE) {
        this.AUTH_RETCODE = AUTH_RETCODE;
    }

    public double getResponseSeconds() {
        return ResponseSeconds;
    }

    public void setResponseSeconds(double responseSeconds) {
        ResponseSeconds = responseSeconds;
    }

    public String getStartTime() {
        return StartTime;
    }

    public void setStartTime(String startTime) {
        StartTime = startTime;
    }

    public String getEndTime() {
        return EndTime;
    }

    public void setEndTime(String endTime) {
        EndTime = endTime;
    }
}
