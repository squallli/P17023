package tw.com.regalscan.db;

import java.util.ArrayList;

/**
 * Created by gabehsu on 2017/6/8.
 */

/// <summary>
/// 列舉航班必要資訊
/// </summary>
public class FlightData {

    //開櫃狀態
    public static Boolean OpenFlightFlag = false;

    public static String FlightNo = "";      //航班代碼
    public static String SecSeq = "";        //航段代碼
    public static String Sector = "";        //起訖點簡稱
    public static String CrewID = "";        //CA代碼
    public static String PurserID = "";      //CP代碼
    public static String FlightDate = "";    //飛行日期
    public static String CartNo = "";        //本車櫃號
    public static String MachineID = "";     //機器14碼序號
    public static String AESKey = "";        //AES 加解密Key

    //IFE 相關
    public static String IFE_SSID = null;                  //IFE AP使用SSID
    //    public static String IFEIPAddress = "";                //IFE IP Address
    public static String IFEWebKey = "";                   //IFE Web Key
    public static String IFEAuthenticationMode = "0";      //IFE 驗證 0 = Open, 1 = WPA, 2 = WPA2, 3 = WPA2PSK
    public static String IFEWEPStatus = "2";               //IFE Web Key 0 = AESEnabled, 1 = TKIPEnabled, 2 = WEPDisabled, 3 = WEPEnabled
    public static String IFEEAPParameters = "0";           //IFE EAP參數 Key 0 = null, 1 = 啟用

    public static Boolean IFECertificates = false;         //IFE Certificates參數 True = 啟用, False = 不啟用
    public static Boolean IFEPingOpen = false;             //IFE 是否要啟用Ping來判斷有正確連線 True = 啟用, False = 不啟用
    public static Boolean IFEConnectionStatus = false;     //IFE 開關,預設為關
    public static Boolean IFEGetEnabledItemCode = false;   //標記是否取得 IFE特有的Item Code
    public static Boolean IFEPushInitialize = false;       //標記此航段是否已同步過Inventory
    public static String CatalogID = null;                 //IFE目前使用目錄編號

    //Wifi連線相關
    public static int IFERetryCount = 0;                   //Airpos連線次數
    public static int IFETimerout = 0;                      //Airpos連線逾時設定(單位:秒)

    //線上取授權IP
    public static String AuthorizeUrl = "";                //EVA取授權Service Url
    public static Boolean OnlineAuthorize = false;         //EVA線上取授權開啟設定 True = 啟用, False = 不啟用

    //以下是必要的交易資訊,Menu From Load時先讀進來
    public static ArrayList<DiscountRow> Discount = new ArrayList<>();            //折扣列表
    public static ArrayList<FuncTableRow> FuncTable = new ArrayList<>();          //折扣條件列表
    public static ArrayList<RateRow> Rate = new ArrayList<>();                    //各幣別匯率

    //教育訓練Flag
    public static boolean isEducationMode = false;
    public static boolean isDisconnected = false;

    /**
     * 設定折扣資訊
     */
    public static void SetDiscount(String Type, double DiscountRate, int DiscountAmount, String DiscountGift, String Description,
                                   int UpperLimit, String FuncID, String Progression) {

        DiscountRow _DiscountRow = new DiscountRow();
        _DiscountRow.Type = Type;
        _DiscountRow.DiscountRate = DiscountRate;
        _DiscountRow.DiscountAmount = DiscountAmount;
        _DiscountRow.DiscountGift = DiscountGift;
        _DiscountRow.Description = Description;
        _DiscountRow.UpperLimit = UpperLimit;
        _DiscountRow.FuncID = FuncID;

        _DiscountRow.Progression = Progression.equals("Y");

        Discount.add(_DiscountRow);
    }

    /**
     * 取得特定Type折扣資訊
     */
    public static DiscountRow GetDiscount(String Type) {

        for (int i = 0; i < Discount.size(); i++) {
            if (Discount.get(i).Type.equals(Type)) {
                return Discount.get(i);
            }
        }
        return null;
    }

    /**
     * 取得特定匯率
     */
    public static RateRow GetRate(String CurDdd, String CurDvr) {

        for (int i = 0; i < Rate.size(); i++) {
            if (Rate.get(i).CurDdd.equals(CurDdd) &&
                Rate.get(i).CurDvr.equals(CurDvr)) {
                return Rate.get(i);
            }
        }
        return null;
    }

    /**
     * 取得特定Type折扣條件資訊
     */
    public static FuncTableRow GetFuncTable(String FuncID) {

        for (int i = 0; i < FuncTable.size(); i++) {
            if (FuncTable.get(i).FuncID.equals(FuncID)) {
                return FuncTable.get(i);
            }
        }
        return null;
    }

    /**
     * 設定折扣資訊
     */
    public static void SetFuncTable(String FuncID, String Syntax, String Description,
                                    String ChildID) {

        FuncTableRow _FuncTableRow = new FuncTableRow();
        _FuncTableRow.FuncID = FuncID;
        _FuncTableRow.Syntax = Syntax;
        _FuncTableRow.Description = Description;
        _FuncTableRow.ChildID = ChildID;

        FuncTable.add(_FuncTableRow);
    }

    /**
     * 設定匯率資訊
     */
    public static void SetRate(String CurDdd, String CurDvr, double ExchRate,
                               double MiniValue) {

        RateRow _RateRow = new RateRow();
        _RateRow.CurDdd = CurDdd;
        _RateRow.CurDvr = CurDvr;
        _RateRow.ExchRate = ExchRate;
        _RateRow.MiniValue = MiniValue;

        Rate.add(_RateRow);
    }

    /**
     * 清空航班資訊
     */
    public static void ClearFlightData() {

        OpenFlightFlag = false;

        FlightNo = "";     //航班代碼
        SecSeq = "";       //航段代碼
        Sector = "";       //起訖點簡稱
        CrewID = "";       //CA代碼
        PurserID = "";     //CP代碼
        FlightDate = "";   //飛行日期
        CartNo = "";       //本車櫃號
        MachineID = "";    //機器14碼序號
        AESKey = "";       //AES 加解密Key

        //IFE 相關
        IFEConnectionStatus = false;     //IFE 開關,預設為關
        IFEGetEnabledItemCode = false;   //標記是否取得 IFE特有的Item Code
        IFEPushInitialize = false;      //標記此航段是否已同步過Inventory
        CatalogID = "";                 //IFE目前使用目錄編號
        isEducationMode = false;        //教育版本

        //線上取授權IP
        AuthorizeUrl = "";                //EVA取授權Service Url
        OnlineAuthorize = false;          //EVA線上取授權開啟設定 True = 啟用, False = 不啟用

        //以下是必要的交易資訊,Menu From Load時先讀進來
        Rate.clear();                 //各幣別匯率
        Discount.clear();             //折扣列表
        FuncTable.clear();            //折扣條件列表
    }
}
