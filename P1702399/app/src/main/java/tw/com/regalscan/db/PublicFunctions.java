package tw.com.regalscan.db;

import android.content.Context;

import com.regalscan.sqlitelibrary.TSQL;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import tw.com.regalscan.component.AESEncrypDecryp;

public class PublicFunctions {

    private Context context;
    private String SecSeq;

    //起始函式
    public PublicFunctions(Context context, String SecSeq) {
        this.context = context;
        this.SecSeq = SecSeq;
    }

    /////////////////////////////////////
    //
    //            公用函式
    //
    /////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////
    //                               System
    //////////////////////////////////////////////////////////////////////////

    /**
     * 產生新的基本銷售空白資料庫 + VM當日檔資料表 + CUP差異資料表
     *
     * @return (JSONObject) 資料庫產生成功與否
     */
    public JSONObject CreateNewDatabase() {

        String ClassName = "PublicFunctions";
        String FunctionName = "CreateNewDatabase";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //對資料進行加密
        _TSQL.encryptDB("P17023");
//        _TSQL.encryptDB("Black");
//        _TSQL.encryptDB("CUPBlack");
        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //產生Commands
            String[] CreateCommand = new String[]{
                    "DROP TABLE IF EXISTS [AllParts] ",
                    "CREATE TABLE [AllParts] (" +
                            "  [ItemCode] VARCHAR(10), " +
                            "  [ItemPriceUS] Float, " +
                            "  [ItemPriceTW] Float, " +
                            "  [ItemName] VARCHAR(50), " +
                            "  [Barcode1] VARCHAR(13), " +
                            "  [Barcode2] VARCHAR(13), " +
                            "  [SerialCode] VARCHAR(3), " +
                            "  [ItemID] VARCHAR(10), " +
                            "  [IFEID] VARCHAR(8), " +
                            "  [Remark] VARCHAR(100), " +
                            "  CONSTRAINT [] PRIMARY KEY ([ItemCode]));",
                    "DROP TABLE IF EXISTS [Inventory] ",
                    "CREATE TABLE [Inventory] (" +
                            "  [SecSeq] VARCHAR(2), " +
                            "  [ItemCode] VARCHAR(10), " +
                            "  [DrawNo] VARCHAR(3), " +
                            "  [StandQty] Int, " +
                            "  [StartQty] Int, " +
                            "  [AdjustQty] Int," +
                            "  [SalesQty] Int, " +
                            "  [TransferQty] Int, " +
                            "  [DamageQty] Int, " +
                            "  [EndQty] Int, " +
                            "  [EGASCheckQty] Int, " +
                            "  [EGASDamageQty] Int, " +
                            "  [EVACheckQty] Int, " +
                            "  [EVADamageQty] Int," +
                            "  CONSTRAINT [] PRIMARY KEY ([SecSeq], [ItemCode]));",
                    "DROP TABLE IF EXISTS [Rate] ",
                    "CREATE TABLE [Rate] (" +
                            "  [ExchDate] VARCHAR(8), " +
                            "  [CurDdd] VARCHAR(5), " +
                            "  [CurDvr] VARCHAR(5), " +
                            "  [ExchRate] Float, " +
                            "  [MiniValue] Float, " +
                            "  [CashCurrency] VARCHAR(1), " +
                            "  [CardCurrency] VARCHAR(1), " +
                            "  [TaiwanCardCurrency] VARCHAR(1)," +
                            "  CONSTRAINT [] PRIMARY KEY ([ExchDate], [CurDdd], [CurDvr]));",
                    "DROP TABLE IF EXISTS [Adjust] ",
                    "CREATE TABLE [Adjust] (" +
                            "  [SecSeq] VARCHAR(2), " +
                            "  [ReceiptNo] Integer PRIMARY KEY Autoincrement," +
                            "  [ItemCode] VARCHAR(10), " +
                            "  [OldQty] Int, " +
                            "  [NewQty] Int, " +
                            "  [CrewID] VARCHAR(10), " +
                            "  [CrewType] VARCHAR(5), " +
                            "  [WorkingTime] Datetime);",
                    "DROP TABLE IF EXISTS [Flight] ",
                    "CREATE TABLE [Flight] (" +
                            "  [DepFlightNo] VARCHAR(6), " +
                            "  [FlightNo] VARCHAR(6), " +
                            "  [SecSeq] VARCHAR(2), " +
                            "  [FlightDate] VARCHAR(8), " +
                            "  [DepStn] VARCHAR(5), " +
                            "  [ArivStn] VARCHAR(5), " +
                            "  [CarNo] VARCHAR(7), " +
                            "  [ISOpen] Int, " +
                            "  [ISClose] Int, " +
                            "  [CrewID] VARCHAR(10), " +
                            "  [PurserID] VARCHAR(10), " +
                            "  [IFECatalogID] VARCHAR(10), " +
                            "  [IFETokenID] VARCHAR(100), " +
                            "  [Mode] VARCHAR(20), " +
                            "  [IsUpload] VARCHAR(1), " +
                            "  CONSTRAINT [] PRIMARY KEY ([FlightNo], [SecSeq]));",
                    "DROP TABLE IF EXISTS [CrewInfo] ",
                    "CREATE TABLE [CrewInfo] (" +
                            "  [CrewID] VARCHAR(10), " +
                            "  [Password1] VARCHAR(20), " +
                            "  [Password2] VARCHAR(20), " +
                            "  [CrewType] VARCHAR(5), " +
                            "  [Name] VARCHAR(50), " +
                            "  CONSTRAINT [] PRIMARY KEY ([CrewID], [CrewType]));",
                    "DROP TABLE IF EXISTS [Damage] ",
                    "CREATE TABLE [Damage] (" +
                            "  [SecSeq] VARCHAR(2), " +
                            "  [ReceiptNo] Integer PRIMARY KEY Autoincrement, " +
                            "  [ItemCode] VARCHAR(10), " +
                            "  [Qty] Int, " +
                            "  [Status] VARCHAR(1), " +
                            "  [IFEDamage] VARCHAR(1), " +
                            "  [WorkingTime] DateTime);",
                    "DROP TABLE IF EXISTS [Transfer] ",
                    "CREATE TABLE [Transfer] (" +
                            "  [SecSeq] VARCHAR(2), " +
                            "  [ReceiptNo] VARCHAR(10), " +
                            "  [TransferNo] VARCHAR(9), " +
                            "  [SerialNo] Integer PRIMARY KEY Autoincrement, " +
                            "  [ItemCode] VARCHAR(10), " +
                            "  [CarFrom] VARCHAR(7), " +
                            "  [CarTo] VARCHAR(7), " +
                            "  [Qty] Int, " +
                            "  [TransferType] VARCHAR(5), " +
                            "  [WorkingTime] DateTime);",
                    "DROP TABLE IF EXISTS [SalesHead] ",
                    "CREATE TABLE [SalesHead] (" +
                            "  [SecSeq] VARCHAR(2), " +
                            "  [ReceiptNo] VARCHAR(10), " +
                            "  [SalesTime] DateTime, " +
                            "  [RefundTime] DateTime, " +
                            "  [OriPrice] Float, " +
                            "  [TotalPrice] Float, " +
                            "  [UpperLimitType] VARCHAR(10), " +
                            "  [UpperLimitNo] VARCHAR(100), " +
                            "  [Status] VARCHAR (1), " +
                            "  [PreorderNo] VARCHAR (13), " +
                            "  [IFEOrderNo] VARCHAR(10), " +
                            "  [SeatNo] VARCHAR(10), " +
                            "  [AuthuticationFlag] VARCHAR(1), " +
                            "  [OrderType] VARCHAR(1), " +
                            "  CONSTRAINT [] PRIMARY KEY ([SecSeq], [ReceiptNo]));",
                    "DROP TABLE IF EXISTS [SalesDetail] ",
                    "CREATE TABLE [SalesDetail] (" +
                            "  [SecSeq] VARCHAR(2), " +
                            "  [ReceiptNo] VARCHAR(10), " +
                            "  [SerialNo] Integer PRIMARY KEY Autoincrement, " +
                            "  [ItemCode] VARCHAR(10), " +
                            "  [OriPrice] Float, " +
                            "  [SalesPrice] Float, " +
                            "  [SalesQty] Int, " +
                            "  [Discount] Float, " +
                            "  [VipType] VARCHAR (10), " +
                            "  [VipNo] VARCHAR (100), " +
                            "  [Status] VARCHAR (1));",
                    "DROP TABLE IF EXISTS [PaymentInfo] ",
                    "CREATE TABLE [PaymentInfo] (" +
                            "  [SecSeq] VARCHAR(2), " +
                            "  [ReceiptNo] VARCHAR(10), " +
                            "  [PayBy] VARCHAR(10), " +
                            "  [Currency] VARCHAR (5), " +
                            "  [Amount] Float, " +
                            "  [CardType] VARCHAR (8), " +
                            "  [CardNo] VARCHAR (100), " +
                            "  [CardName] VARCHAR (20), " +
                            "  [CardDate] VARCHAR (50), " +
                            "  [USDAmount] Float, " +
                            "  [CouponNo] VARCHAR (10), " +
                            "  [Status] VARCHAR (1), " +
                            "  [PreorderNo] VARCHAR (13), " +
                            "  CONSTRAINT [] PRIMARY KEY ([SecSeq], [ReceiptNo], [PayBy], [Currency], [CouponNo], [Status]));",
                    "DROP TABLE IF EXISTS [CreditCard] ",
                    "CREATE TABLE [CreditCard] (" +
                            "  [CardType] VARCHAR (8), " +
                            "  [Currency] VARCHAR (3), " +
                            "  [MID] VARCHAR (20), " +
                            "  [BankText] VARCHAR (100), " +
                            "  [MaxAmount] Float, " +
                            "  [MiniAmount] Float, " +
                            "  CONSTRAINT [] PRIMARY KEY ([CardType], [Currency]));",
                    "DROP TABLE IF EXISTS [SystemLog] ",
                    "CREATE TABLE [SystemLog] (" +
                            "  [SerialNo] Integer PRIMARY KEY Autoincrement, " +
                            "  [SystemDate] DateTime, " +
                            "  [SecSeq] VARCHAR (2), " +
                            "  [LogType] VARCHAR (30), " +
                            "  [OperationName] VARCHAR (50), " +
                            "  [FunctionName] VARCHAR (50), " +
                            "  [LogText] VARCHAR(500));",
                    "DROP TABLE IF EXISTS [BankDis] ",
                    "CREATE TABLE [BankDis] (" +
                            "  [BinCode] VARCHAR (10), " +
                            "  [DiscountType] VARCHAR (10), " +
                            "  CONSTRAINT [] PRIMARY KEY ([BinCode]));",
//                    "DROP TABLE IF EXISTS [Staff] ",
//                    "CREATE TABLE [Staff] (" +
//                            "  [EmployeeID] VARCHAR (10)\t, " +
//                            "  [DiscountType] VARCHAR (10), " +
//                            "  CONSTRAINT [] PRIMARY KEY ([EmployeeID]));",
                    "DROP TABLE IF EXISTS [PreorderSalesHead] ",
                    "CREATE TABLE [PreorderSalesHead] (" +
                            "  [SecSeq] VARCHAR (2), " +
                            "  [ReceiptNo] VARCHAR (10), " +
                            "  [SalesTime] DateTime, " +
                            "  [RefundTime] DateTime, " +
                            "  [PreorderNo] VARCHAR (13), " +
                            "  [SaleFlag] VARCHAR  (1), " +
                            "  [VerifyType] VARCHAR  (1), " +
                            "  CONSTRAINT [] PRIMARY KEY ([SecSeq],[ReceiptNo]));",
                    "DROP TABLE IF EXISTS [PreorderHead] ",
                    "CREATE TABLE [PreorderHead] (" +
                            "  [PreorderNO] VARCHAR (13), " +
                            "  [SecSeq] VARCHAR (2), " +
                            "  [MileDisc] Float, " +
                            "  [ECouponCurrency] VARCHAR  (5), " +
                            "  [ECoupon] Float, " +
                            "  [CardType] VARCHAR  (8), " +
                            "  [CardNo] VARCHAR  (100), " +
                            "  [TravelDocument] VARCHAR  (300), " +
                            "  [CurDvr] VARCHAR  (5), " +
                            "  [PayAmt] Float, " +
                            "  [Amount] Float, " +
                            "  [Discount] Float, " +
                            "  [PNR] VARCHAR  (13), " +
                            "  [PassengerName] VARCHAR  (30), " +
                            "  [SaleFlag] VARCHAR  (1), " +
                            "  [EGASSaleFlag] VARCHAR  (1), " +
                            "  [EVASaleFlag] VARCHAR  (1), " +
                            "  [PreorderType] VARCHAR  (2), " +
                            "  CONSTRAINT [] PRIMARY KEY ([PreorderNO]));",
                    "DROP TABLE IF EXISTS [PreorderDetail] ",
                    "CREATE TABLE [PreorderDetail] (" +
                            "  [PreorderNo] VARCHAR (13), " +
                            "  [DrawNo] VARCHAR (3), " +
                            "  [ItemCode] VARCHAR (10), " +
                            "  [SerialCode] VARCHAR (3), " +
                            "  [ItemName] VARCHAR (100), " +
                            "  [OriginalPrice] Float, " +
                            "  [SalesPrice] Float, " +
                            "  [SalesPriceTW] Float, " +
                            "  [SalesQty] Int, " +
                            "  CONSTRAINT [] PRIMARY KEY ([PreorderNO], [SerialCode]));",
                    "DROP TABLE IF EXISTS [Authutication] ",
                    "CREATE TABLE [Authutication] (" +
                            "  [SecSeq] VARCHAR(2), " +
                            "  [ReceiptNo] VARCHAR(10), " +
                            "  [UG_MARK] VARCHAR (1), " +
                            "  [CardNo] VARCHAR (100), " +
                            "  [CardName] VARCHAR (60), " +
                            "  [CardType] VARCHAR (8), " +
                            "  [ExpireDate] VARCHAR (50), " +
                            "  [CurrencyType] VARCHAR (5), " +
                            "  [TransAmount] INT, " +
                            "  [OrderNo] VARCHAR (10), " +
                            "  [PreorderNo] VARCHAR (13), " +
                            "  [VipType] VARCHAR (10), " +
                            "  [VipNo] VARCHAR (100), " +
                            "  [REAUTH_MARK] VARCHAR (1), " +
                            "  [APPROVE_CODE] VARCHAR (8), " +
                            "  [RSPONSE_CODE] VARCHAR (3), " +
                            "  [RSPONSE_MSG] VARCHAR (60), " +
                            "  [TRANS_DATE] VARCHAR (8), " +
                            "  [TRANS_TIME] VARCHAR (6), " +
                            "  [AUTH_DATE] VARCHAR (8), " +
                            "  [TRANS_SEQNO] VARCHAR (12), " +
                            "  [TRANS_CODE] VARCHAR (2), " +
                            "  [AUTH_RETCODE] VARCHAR (4), " +
                            "  [AuthTime] DateTime, " +
                            "  CONSTRAINT [] PRIMARY KEY ([SecSeq], [ReceiptNo], [UG_MARK], [AuthTime]));",
                    "DROP TABLE IF EXISTS [CartList] ",
                    "CREATE TABLE [CartList] (" +
                            "  [CartNo] VARCHAR (7), " +
                            "  CONSTRAINT [] PRIMARY KEY ([CartNo]));",
                    "DROP TABLE IF EXISTS [ClassAllParts] ",
                    "CREATE TABLE [ClassAllParts] (" +
                            "  [Infant] VARCHAR(10), " +
                            "  [OriginalClass] VARCHAR(10), " +
                            "  [NewClass] VARCHAR(10), " +
                            "  [USDPrice] Float, " +
                            "  [TWDPrice] Float, " +
                            "  CONSTRAINT [] PRIMARY KEY ([Infant], [OriginalClass], [NewClass]));",
                    "DROP TABLE IF EXISTS [ClassSalesHead] ",
                    "CREATE TABLE [ClassSalesHead] (" +
                            "  [SecSeq] VARCHAR(2), " +
                            "  [ReceiptNo] VARCHAR(10), " +
                            "  [SalesTime] DateTime, " +
                            "  [RefundTime] DateTime, " +
                            "  [TotalPrice] Float, " +
                            "  [Status] VARCHAR (1), " +
                            "  [AuthuticationFlag] VARCHAR (1), " +
                            "  CONSTRAINT [] PRIMARY KEY ([SecSeq], [ReceiptNo]));",
                    "DROP TABLE IF EXISTS [ClassSalesDetail] ",
                    "CREATE TABLE [ClassSalesDetail] (" +
                            "  [SecSeq] VARCHAR(2), " +
                            "  [ReceiptNo] VARCHAR(10), " +
                            "  [SerialNo] Integer PRIMARY KEY Autoincrement," +
                            "  [Infant] VARCHAR(10), " +
                            "  [OriginalClass] VARCHAR(10), " +
                            "  [NewClass] VARCHAR(10), " +
                            "  [SalesPrice] Float, " +
                            "  [SalesQty] Int, " +
                            "  [Status] VARCHAR (1));",
                    "DROP TABLE IF EXISTS [ClassPaymentInfo] ",
                    "CREATE TABLE [ClassPaymentInfo] (" +
                            "  [SecSeq] VARCHAR(2), " +
                            "  [ReceiptNo] VARCHAR(10), " +
                            "  [PayBy] VARCHAR (10), " +
                            "  [Currency] VARCHAR (5), " +
                            "  [Amount] Float, " +
                            "  [CardType] VARCHAR (8), " +
                            "  [CardNo] VARCHAR (100), " +
                            "  [CardName] VARCHAR (20), " +
                            "  [CardDate] VARCHAR (50), " +
                            "  [USDAmount] Float, " +
                            "  [Status] VARCHAR (1), " +
                            "  CONSTRAINT [] PRIMARY KEY ([SecSeq], [ReceiptNo], [PayBy], [Currency], [Status]));",
                    "DROP TABLE IF EXISTS [SSIDList] ",
                    "CREATE TABLE [SSIDList] (" +
                            "  [SSID] VARCHAR(50), " +
                            "  [MachineID] VARCHAR(14), " +
                            "  CONSTRAINT [] PRIMARY KEY ([SSID], [MachineID]));",
                    "DROP TABLE IF EXISTS [PromotionsInfo] ",
                    "CREATE TABLE [PromotionsInfo] (" +
                            "  [promotionsTitle] VARCHAR (100), " +
                            "  [PromotionsDetail] VARCHAR (500), " +
                            "  [StartDate] VARCHAR (8), " +
                            "  [EndDate] VARCHAR (8), " +
                            "  [PromotionsCode] VARCHAR (20), " +
                            "  [Note] VARCHAR (500), " +
                            "  [PrintType] VARCHAR (20), " +
                            "  CONSTRAINT [] PRIMARY KEY ([promotionsTitle], [PromotionsDetail]));",
                    "DROP TABLE IF EXISTS [OrderChangeHistory] ",
                    "CREATE TABLE [OrderChangeHistory] (" +
                            "  [ReceiptNo] Int, " +
                            "  [SecSeq] VARCHAR (2), " +
                            "  [OldOrderNo] VARCHAR (20), " +
                            "  [NewOrderNo] VARCHAR (20));",
                    "DROP TABLE IF EXISTS [Discount] ",
                    "CREATE TABLE [Discount] (" +
                            "  [Type] VARCHAR (6), " +
                            "  [DiscountRate] Float, " +
                            "  [DiscountAmount] Int, " +
                            "  [DiscountGift] VARCHAR (10), " +
                            "  [Description] VARCHAR (100), " +
                            "  [Upperlimit] Int, " +
                            "  [FuncID] VARCHAR (3), " +
                            "  [Progression] VARCHAR (1), " +
                            "  CONSTRAINT [] PRIMARY KEY ([Type]));",
                    "DROP TABLE IF EXISTS [FuncTable] ",
                    "CREATE TABLE [FuncTable] (" +
                            "  [FuncID] VARCHAR  (3), " +
                            "  [Syntax] VARCHAR  (200), " +
                            "  [Description] VARCHAR  (200), " +
                            "  [ChildID] VARCHAR  (3), " +
                            "  CONSTRAINT [] PRIMARY KEY ([FuncID]));",
                    "DROP TABLE IF EXISTS [ItemGroup] ",
                    "CREATE TABLE [ItemGroup] (" +
                            "  [GroupID] VARCHAR  (6), " +
                            "  [ItemCode] VARCHAR  (20), " +
                            "  CONSTRAINT [] PRIMARY KEY ([GroupID],[ItemCode]));",
                    "DROP TABLE IF EXISTS [TaiwanCreditCardInfo] ",
                    "CREATE TABLE [TaiwanCreditCardInfo] (" +
                            "  [BinCode] VARCHAR (15), " +
                            "  CONSTRAINT [] PRIMARY KEY ([BinCode]));",
                    "DROP TABLE IF EXISTS [DiscountException] ",
                    "CREATE TABLE [DiscountException] (" +
                            "  [DiscountType] VARCHAR (6), " +
                            "  [ItemCode] VARCHAR (20), " +
                            "  CONSTRAINT [] PRIMARY KEY ([DiscountType], [ItemCode]));",
//                    "DROP TABLE IF EXISTS [Settings] ",
//                    "CREATE TABLE [Settings] (" +
//                            "  [Key] VARCHAR (50), " +
//                            "  [Value] VARCHAR (50), " +
//                            "  CONSTRAINT [] PRIMARY KEY ([Key]));",
                    "DROP TABLE IF EXISTS [SCoupon] ",
                    "CREATE TABLE [SCoupon] (" +
                            " [StartDate] VARCHAR(10), " +
                            " [ExpireDate] VARCHAR(10));",
                    "DROP TABLE IF EXISTS [PrtData]",
                    "CREATE TABLE [PrtData] (" +
                            "[prt1] VARCHAR(20), " +
                            "[prt2] VARCHAR(20), " +
                            "[prt3] VARCHAR(20), " +
                            "[prt4] VARCHAR(20), " +
                            "[prt5] VARCHAR(20))"
            };

            //執行SQL Commands
            ArrayList<String> CreateCommands = new ArrayList(Arrays.asList(CreateCommand));
            if (_TSQL.ExecutesSQLCommand(CreateCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Create database failed.";

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
     * 依據文字檔，寫入航班資料
     *
     * @return (JSONObject) 資料庫寫入成功與否
     */
    public JSONObject InsertFlightData() {

        String ClassName = "PublicFunctions";
        String FunctionName = "InsertFlightData";
        String ReturnCode, ReturnMessage;
        String InsertTextName = "";
        String SQL, TextLine;
        ArrayList<String> SQLCommands = new ArrayList<>();
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        String FilePath = android.os.Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator + "EVAPOSDownloadText";

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        //文字檔案名稱
        String[] FilesList = new String[]{
                "FLIGHT",
                "DOC",
                "SSID",
                "CREW",
                "DISCOUNT",
                "EXCEPTION",
                "ITEMGROUP",
                "ALLPART",
                "INVENTORY",
                "PREORDERM",
                "PREORDERD",
                "EMPLOYEE",
                "RATE",
                "UPGRADE",
                "COUPON_DISC",
                "DCARD",
                "TCARDBIN",
                "CREDIT_CARD_INFO",
                "SCOUPON_LIST",
                "PRTDATA",
                "SETTING"
        };

        try {

//            _TSQL.clearAllTable();

            //逐筆文字檔寫入
            int FlightSecSeqCount = 0;          //航段總數
            int DataCount = FilesList.length;   //資料數量
            String[] TempInsertData;     //暫存資料

            for (String aFilesList : FilesList) {
                //判斷文件是否存在，不存在報錯
                File files = new File(FilePath + File.separator + aFilesList + ".txt");
                if (!files.exists()) {
                    ReturnCode = "8";
                    ReturnMessage = "File " + aFilesList + " not found.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //讀取文件檔路徑
                FileReader mFileReader = new FileReader(FilePath + File.separator + aFilesList + ".txt");
                BufferedReader mBufferedReader = new BufferedReader(mFileReader);

                //標記工作中文字檔
                InsertTextName = aFilesList;

                switch (aFilesList.toUpperCase()) {
                    case "TCARDBIN":
                        //刪除原本的資料
                        SQL = "DELETE FROM TaiwanCreditCardInfo";
                        SQLCommands.add(SQL);
                        //一行一行取出文字字串裝入String裡，直到沒有下一行文字停止跳出
                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            SQL = "INSERT INTO TaiwanCreditCardInfo (BinCode) " +
                                    "VALUES ('" + TextLine + "')";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "DOC":
                        //刪除原本的資料
                        SQL = "DELETE FROM CartList";
                        SQLCommands.add(SQL);

                        //一行一行取出文字字串裝入String裡，直到沒有下一行文字停止跳出
                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            SQL = "INSERT INTO CartList (CartNo) " +
                                    "VALUES ('" + TextLine + "')";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "SSID":
                        //刪除原本的資料
                        SQL = "DELETE FROM SSIDList";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");
                            SQL = "INSERT INTO SSIDList (SSID, MachineID) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] + "')";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "CREW":
                        //刪除原本的資料
                        SQL = "DELETE FROM CrewInfo";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");
                            SQL = "INSERT INTO CrewInfo (CrewID, Password1, Password2, CrewType, Name) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] +
                                    "','" + TempInsertData[2] +
                                    "','" + TempInsertData[3];

                            if (TempInsertData.length == 5) {
                                SQL += "','" + TempInsertData[4] + "')";
                            } else {
                                SQL += "','')";
                            }

                            SQLCommands.add(SQL);
                        }
                        break;
                    case "DISCOUNT":
                        //刪除原本的資料
                        SQL = "DELETE FROM Discount";
                        SQLCommands.add(SQL);
                        SQL = "DELETE FROM FuncTable ";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");

                            if (!TempInsertData[0].contains("STAFF")) {
                                SQL = "INSERT INTO Discount (Type, DiscountRate, DiscountAmount, DiscountGift, Description, Upperlimit, FuncID, Progression) " +
                                        "VALUES ('" + TempInsertData[0] +
                                        "'," + TempInsertData[1] +
                                        "," + TempInsertData[2] +
                                        ",'" + TempInsertData[3] +
                                        "','" + TempInsertData[4] +
                                        "'," + TempInsertData[5] +
                                        ",'" + TempInsertData[6].split("#")[0].split("@")[0] +
                                        "','" + TempInsertData[7] + "')";
                                SQLCommands.add(SQL);
                            } else {
                                SQL = "INSERT INTO Discount (Type, DiscountRate, DiscountAmount, DiscountGift, Description, Upperlimit, FuncID, Progression) " +
                                        "VALUES ('" + TempInsertData[0] +
                                        "'," + TempInsertData[1] +
                                        "," + TempInsertData[2] +
                                        ",'" + TempInsertData[3] +
                                        "','" + TempInsertData[6].split("#")[0].split("@")[0] +
                                        "'," + TempInsertData[5] +
                                        ",'" + "" +
                                        "','" + TempInsertData[7] + "')";
                                SQLCommands.add(SQL);
                            }

                            for (int j = 0; j < TempInsertData[6].split("#").length; j++) {

                                if (TempInsertData[6].split("#")[0].length() == 0) {
                                    continue;
                                }

                                if (!TempInsertData[0].contains("STAFF")) {
                                    SQL = "INSERT INTO FuncTable (FuncID,Syntax,Description,ChildID) " +
                                            "VALUES ('" + TempInsertData[6].split("#")[j].split("@")[0] +
                                            "','" + TempInsertData[6].split("#")[j].split("@")[1] +
                                            "','" + TempInsertData[6].split("#")[j].split("@")[2];
                                } else {
                                    SQL = "INSERT INTO FuncTable (FuncID,Syntax,Description,ChildID) " +
                                            "VALUES ('" + TempInsertData[6].split("#")[j].split("@")[0] +
                                            "','" + TempInsertData[6].split("#")[j].split("@")[1] +
                                            "','" + TempInsertData[6].split("#")[j].split("@")[2];
                                }
                                //判斷是否為條件結尾
                                if (j == (TempInsertData[6].split("#").length - 1)) {
                                    SQL += "','')";
                                } else {
                                    SQL += "','" + TempInsertData[6].split("#")[j + 1].split("@")[0] + "')";
                                }

                                SQLCommands.add(SQL);
                            }
                        }
                        break;
                    case "EXCEPTION":
                        //刪除原本的資料
                        SQL = "DELETE FROM DiscountException";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");
                            SQL = "INSERT INTO DiscountException (DiscountType, ItemCode) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] + "')";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "FLIGHT":
                        //刪除原本的資料
                        SQL = "DELETE FROM Flight";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            FlightSecSeqCount += 1;
                            TempInsertData = TextLine.split(",");

                            SQL =
                                    "INSERT INTO Flight (DepFlightNo, FlightNo, SecSeq, FlightDate, DepStn, ArivStn, CarNo, ISOpen, ISClose, CrewID, PurserID, IFECatalogID, "
                                            + "IFETokenID, Mode, IsUpload) "
                                            +
                                            "VALUES ('" + TempInsertData[0] +
                                            "','" + TempInsertData[1] +
                                            "','" + TempInsertData[2] +
                                            "','" + TempInsertData[3] +
                                            "','" + TempInsertData[4] +
                                            "','" + TempInsertData[5] +
                                            "','" + TempInsertData[6] +
                                            "'," + "0" +
                                            "," + "0" +
                                            ",'" + "" +
                                            "','" + "" +
                                            "','" + "" +
                                            "','" + "" +
                                            "','" + TempInsertData[7] +
                                            "','N')";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "ITEMGROUP":
                        //刪除原本的資料
                        SQL = "DELETE FROM ItemGroup";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");
                            SQL = "INSERT INTO ItemGroup (GroupID, ItemCode) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] + "')";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "ALLPART":
                        //刪除原本的資料
                        SQL = "DELETE FROM AllParts";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");
                            SQL = "INSERT INTO AllParts (ItemCode, ItemPriceUS, ItemPriceTW, ItemName, Barcode1, Barcode2, SerialCode, ItemID, IFEID, Remark) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] +
                                    "','" + TempInsertData[2] +
                                    "','" + TempInsertData[3] +
                                    "','" + TempInsertData[4] +
                                    "','" + TempInsertData[5] +
                                    "','" + TempInsertData[6] +
                                    "','" + "" +
                                    "','" + TempInsertData[7];

                            if (TempInsertData.length == 9) {
                                SQL += "','" + TempInsertData[8] + "')";
                            } else {
                                SQL += "','" + "" + "')";
                            }

                            SQLCommands.add(SQL);
                        }
                        break;
                    case "INVENTORY":
                        //刪除原本的資料
                        SQL = "DELETE FROM Inventory";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {

                            TempInsertData = TextLine.split(",");
                            int SecSeq = 0;
                            for (int j = 0; j < FlightSecSeqCount; j++) {

                                if (SecSeq == (FlightSecSeqCount - 1)) {
                                    SecSeq = 9;
                                }

                                SQL = "INSERT INTO Inventory (SecSeq, ItemCode, DrawNo, StandQty, StartQty, AdjustQty, SalesQty, TransferQty, DamageQty," +
                                        "EndQty, EGASCheckQty, EGASDamageQty, EVACheckQty, EVADamageQty) " +
                                        "VALUES ('" + SecSeq +
                                        "','" + TempInsertData[0] +
                                        "','" + TempInsertData[1] +
                                        "'," + TempInsertData[2] +
                                        "," + TempInsertData[3] +
                                        "," + "0" +
                                        "," + "0" +
                                        "," + "0" +
                                        "," + "0" +
                                        "," + TempInsertData[3] +
                                        "," + TempInsertData[3] +
                                        "," + "0" +
                                        "," + TempInsertData[3] +
                                        "," + "0" + ")";
                                SQLCommands.add(SQL);

                                SecSeq += 1;
                            }
                        }
                        break;
                    case "PREORDERM":
                        //刪除原本的資料
                        SQL = "DELETE FROM PreorderHead";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");
                            SQL = "INSERT INTO PreorderHead (PreorderNO, SecSeq, MileDisc, ECouponCurrency, ECoupon, CardType, CardNo, TravelDocument, CurDvr," +
                                    "PayAmt, Amount, Discount, PNR, PassengerName, SaleFlag, EGASSaleFlag, EVASaleFlag, PreorderType) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] +
                                    "'," + TempInsertData[2] +
                                    ",'" + TempInsertData[3] +
                                    "'," + TempInsertData[4] +
                                    ",'" + TempInsertData[5] +
                                    "','" + TempInsertData[6] +
                                    "','" + TempInsertData[7] +
                                    "','" + TempInsertData[8] +
                                    "'," + TempInsertData[9] +
                                    "," + TempInsertData[10] +
                                    "," + TempInsertData[11] +
                                    ",'" + TempInsertData[12] +
                                    "','" + TempInsertData[13] +
                                    "','" + "N" +
                                    "','" + "N" +
                                    "','" + "N" +
                                    "','" + TempInsertData[14] + "')";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "PREORDERD":
                        //刪除原本的資料
                        SQL = "DELETE FROM PreorderDetail";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");
                            SQL = "INSERT INTO PreorderDetail (PreorderNO, DrawNo, ItemCode, SerialCode, ItemName, OriginalPrice, SalesPrice, SalesPriceTW, SalesQty) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] +
                                    "','" + TempInsertData[2] +
                                    "','" + TempInsertData[3] +
                                    "','" + TempInsertData[4] +
                                    "'," + TempInsertData[5] +
                                    "," + TempInsertData[6] +
                                    "," + TempInsertData[7] +
                                    "," + TempInsertData[8] + ")";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "EMPLOYEE":
                        //刪除原本的資料
                        SQL = "DELETE FROM Staff";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");
                            SQL = "INSERT INTO Staff (EmployeeID, DiscountType) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] + "')";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "RATE":
                        //刪除原本的資料
                        SQL = "DELETE FROM Rate";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");
                            SQL = "INSERT INTO Rate (ExchDate, CurDdd, CurDvr, ExchRate, MiniValue, CashCurrency, CardCurrency, TaiwanCardCurrency) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] +
                                    "','" + TempInsertData[2] +
                                    "','" + TempInsertData[3] +
                                    "','" + TempInsertData[4] +
                                    "','" + TempInsertData[5] +
                                    "','" + TempInsertData[6] +
                                    "','" + TempInsertData[7] + "')";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "UPGRADE":
                        //刪除原本的資料
                        SQL = "DELETE FROM ClassAllParts";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");
                            SQL = "INSERT INTO ClassAllParts (Infant, OriginalClass, NewClass, USDPrice, TWDPrice) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] +
                                    "','" + TempInsertData[2] +
                                    "'," + TempInsertData[3] +
                                    "," + TempInsertData[4] + ")";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "COUPON_DISC":
                        //刪除原本的資料
                        SQL = "DELETE FROM PromotionsInfo";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");
                            SQL = "INSERT INTO PromotionsInfo (promotionsTitle, PromotionsDetail, StartDate, EndDate, PromotionsCode, Note, PrintType) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] +
                                    "','" + TempInsertData[2] +
                                    "','" + TempInsertData[3] +
                                    "','" + TempInsertData[4] +
                                    "','" + TempInsertData[5] +
                                    "','" + TempInsertData[6] + "')";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "DCARD":
                        //刪除原本的資料
                        SQL = "DELETE FROM BankDis";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");
                            SQL = "INSERT INTO BankDis (BinCode, DiscountType) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] + "')";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "CREDIT_CARD_INFO":
                        //刪除原本的資料
                        SQL = "DELETE FROM CreditCard";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");
                            SQL = "INSERT INTO CreditCard (CardType, Currency, MID, BankText, MaxAmount, MiniAmount) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] +
                                    "','" + TempInsertData[2] +
                                    "','" + TempInsertData[3] +
                                    "'," + TempInsertData[4] +
                                    "," + TempInsertData[5] + ")";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "SETTING":
                        SQL = "DELETE FROM Settings";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");
                            SQL = "INSERT INTO Settings (Key, Value) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] + "')";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "SCOUPON_LIST":
                        SQL = "DELETE FROM SCoupon";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");

                            SQL = "INSERT INTO SCoupon (StartDate, ExpireDate) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] + "')";
                            SQLCommands.add(SQL);
                        }
                        break;
                    case "PRTDATA":
                        SQL = "DELETE FROM PrtData";
                        SQLCommands.add(SQL);

                        while ((TextLine = mBufferedReader.readLine()) != null && TextLine.length() > 0) {
                            TempInsertData = TextLine.split(",");

                            SQL = "INSERT INTO PrtData (prt1, prt2, prt3, prt4, prt5) " +
                                    "VALUES ('" + TempInsertData[0] +
                                    "','" + TempInsertData[1] +
                                    "','" + TempInsertData[2] +
                                    "','" + TempInsertData[3];

                            if (TempInsertData.length == 5) {
                                SQL += "','" + TempInsertData[4] + "')";
                            } else {
                                SQL += "','" + "" + "')";
                            }

                            SQLCommands.add(SQL);
                        }
                        break;
                }

                mBufferedReader.close();
                mFileReader.close();
            }

            //執行SQL Commands
            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Insert data failed.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, InsertTextName + "-" + ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 取得POS機號對應的SSID
     *
     * @param MachineNo POS機號
     * @return (JSONObject) 標準格式回傳 航班資訊
     */
    public JSONObject GetWifiSSID(String MachineNo) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetWifiSSID";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {
            String SQL = "SELECT SSID " +
                    "FROM SSIDList " +
                    "WHERE MachineID = '" + MachineNo + "'";

            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, _TSQL.SelectSQLJsonArray(SQL));

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 取得POS內的Log
     *
     * @return (JSONObject) 標準格式回傳 航班資訊
     */
    public JSONObject GetSystemLog() {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetSystemLog";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {
            String SQL = "SELECT * " +
                    "FROM SystemLog";

            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, _TSQL.SelectSQLJsonArray(SQL));

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(SecSeq, "System", ClassName, FunctionName, ex.getMessage());

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
     * 字串右邊補0
     *
     * @param OriginalString 原始字串
     * @param TotalWight     要補足字串長度
     * @param PaddingChar    補足字元
     * @return (String) 處理完字串
     */
    public String PadRight(String OriginalString, int TotalWight, char PaddingChar) {

        try {
            if (OriginalString.length() < TotalWight) {
                while (OriginalString.length() < TotalWight) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(OriginalString).append(PaddingChar);// 右補
                    OriginalString = sb.toString();
                }
            }
            return OriginalString;

        } catch (Exception ex) {

            throw ex;
        }
    }

    //////////////////////////////////////////////////////////////////////////
    //                         Flight Information
    //////////////////////////////////////////////////////////////////////////

    /**
     * 確認基本銷售資訊是否齊全
     *
     * @return (JSONObject) 標準格式回傳 裝載資訊
     */
    public JSONObject CheckBasicSalesInfoIsReady() {

        String ClassName = "PublicFunctions";
        String FunctionName = "CheckBasicSalesInfoIsReady";
        String ReturnCode, ReturnMessage, SQL;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        TSQL _BlackTSQL = TSQL.getINSTANCE(context, this.SecSeq, "Black");
        TSQL _CUPBlackTSQL = TSQL.getINSTANCE(context, this.SecSeq, "CUPBlack");
        String[] CheckTableList = new String[]{"Allparts", "Inventory", "Rate", "Flight", "CrewInfo", "CreditCard",
                "CartList", "ClassAllParts", "SSIDList", "TaiwanCreditCardInfo"};
        String[] CheckBlackList = new String[]{"BinBlack", "VMBlack", "CUPWhiteNo"};
        String[] CheckCUPBlackList = new String[]{"CUPBlack0", "CUPBlack1", "CUPBlack2", "CUPBlack3", "CUPBlack4", "CUPBlack5", "CUPBlack6", "CUPBlack7", "CUPBlack8", "CUPBlack9"};

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            Object TempObj;

            //-----檢核基本資訊--------------------------------------

            //確認商品庫存檔皆對應到商品主檔
            SQL = "SELECT SUM(EndQty) AS TotalSumQTty " +
                    "FROM Inventory LEFT JOIN AllParts " +
                    "ON Inventory.ItemCode = AllParts.ItemCode " +
                    "WHERE AllParts.ItemCode IS NULL ";
            TempObj = _TSQL.SelectSQLObject(SQL);

            if (TempObj != null && (int) TempObj > 0) {
                ReturnCode = "8";
                ReturnMessage = "Failed check flight information!\r\nPlease download again.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            for (String aCheckTableList : CheckTableList) {
                //逐項確認
                SQL = "SELECT Count(*) AS TotalCount " +
                        "FROM " + aCheckTableList;
                TempObj = _TSQL.SelectSQLObject(SQL);

                if (TempObj == null || (int) TempObj == 0) {
                    ReturnCode = "8";
                    ReturnMessage = "Failed check flight information!(" + aCheckTableList + ")\r\nPlease download again.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }
            }

            //確認VM黑名單主檔案在有效期限內
            SQL = "SELECT VMBlackInfo " +
                    "FROM VMBlackInfo";
            TempObj = _BlackTSQL.SelectSQLObject(SQL);

            if (TempObj == null) {
                ReturnCode = "8";
                ReturnMessage = "Failed check black information!\r\nPlease download again.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {

                //比對VM黑名單主檔日期是否過舊，日期需等於起飛日期
                SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date BlackDate = DateFormat.parse(String.valueOf(TempObj));

                SQL = "SELECT FlightDate " +
                        "FROM Flight " +
                        "LIMIT 1";
                TempObj = _TSQL.SelectSQLObject(SQL);
                Date FlightDate = DateFormat.parse(
                        String.valueOf(TempObj).substring(0, 4) + "-" + String.valueOf(TempObj).substring(4, 6) + "-" + String.valueOf(TempObj).substring(6, 8));

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(BlackDate);
                BlackDate = calendar.getTime();//取得加減過後的Date

                //若日期不等於起飛日期
                if (!BlackDate.after(FlightDate) && !BlackDate.equals(FlightDate)) {
                    ReturnCode = "8";
                    ReturnMessage = "Please update black card info.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }
            }

            for (String aCheckBlackList : CheckBlackList) {
                //逐項確認
                SQL = "SELECT Count(*) AS TotalCount " + "FROM " + aCheckBlackList;
                TempObj = _BlackTSQL.SelectSQLObject(SQL);

                if (TempObj == null || (int) TempObj == 0) {
                    ReturnCode = "8";
                    ReturnMessage = "Failed check black information!(" + aCheckBlackList + ")\r\nPlease download again.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }
            }

            //確認CUP黑名單主檔案在有效期限內
            SQL = "SELECT CUPBlackInfo " +
                    "FROM CUPBlackInfo";
            TempObj = _CUPBlackTSQL.SelectSQLObject(SQL);

            if (TempObj == null) {
                ReturnCode = "8";
                ReturnMessage = "Failed check black information!\r\nPlease download again.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {

                //比對黑名單主檔日期是否過舊，日期須為起飛日期一個月內(含一個月)
                SimpleDateFormat DateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date CUPBlackDate = DateFormat.parse(String.valueOf(TempObj));

                SQL = "SELECT FlightDate " +
                        "FROM Flight " +
                        "LIMIT 1";
                TempObj = _TSQL.SelectSQLObject(SQL);
                Date FlightDate = DateFormat.parse(
                        String.valueOf(TempObj).substring(0, 4) + "-" + String.valueOf(TempObj).substring(4, 6) + "-" + String.valueOf(TempObj).substring(6, 8));

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(CUPBlackDate);
                calendar.add(Calendar.MONTH, 1);//月份+1
                CUPBlackDate = calendar.getTime();//取得加減過後的Date

                //若飛行日期大於主檔效期 + 一個月
                if (FlightDate.after(CUPBlackDate)) {
                    ReturnCode = "8";
                    ReturnMessage = "Please update CUP black card info.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }
            }

            for (int i = 0; i < CheckCUPBlackList.length; i++) {
                //逐項確認
                SQL = "SELECT Count(*) AS TotalCount " + "FROM " + CheckCUPBlackList[i];
                TempObj = _CUPBlackTSQL.SelectSQLObject(SQL);

                if (TempObj == null || (int) TempObj == 0) {
                    ReturnCode = "8";
                    ReturnMessage = "Failed check black information!(" + CheckBlackList[i] + ")\r\nPlease download again.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }
            }

            SQL = "SELECT COUNT(*) AS PreorderCount, " +
                    "(SELECT COUNT(*) AS VIPCount " +
                    "FROM PreorderHead " +
                    "WHERE PreorderType IN ('VS','VP')) AS VIPCount " +
                    "FROM PreorderHead " +
                    "WHERE PreorderType = 'PR' ";
            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, _TSQL.SelectSQLJsonArray(SQL));

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 取得航班資訊
     *
     * @param SecSeq 要取得的航段序號
     * @return (JSONObject) 標準格式回傳 航班資訊
     */
    public JSONObject GetFlightInfo(String SecSeq) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetFlightInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq);

        try {
            String SQL = "SELECT FlightNo,SecSeq,FlightDate,DepStn,ArivStn,CarNo,CrewID,PurserID,IFECatalogID,IFETokenID,Mode " +
                    "FROM Flight " +
                    "WHERE SecSeq = '" + SecSeq + "'";

            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, _TSQL.SelectSQLJsonArray(SQL));

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 取得所有航班資訊
     *
     * @return (JSONObject) 標準格式回傳 航班資訊
     */
    public JSONObject GetFlightInfo() {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetFlightInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {
            String SQL = "SELECT FlightNo,SecSeq,FlightDate,DepStn,ArivStn,CarNo,CrewID,PurserID,IFECatalogID,IFETokenID,Mode " +
                    "FROM Flight ";

            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, _TSQL.SelectSQLJsonArray(SQL));

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 取得當前準備開櫃之航段序號
     *
     * @return (JSONObject) 標準格式回傳 準備開櫃航班資訊
     */
    public JSONObject GetCurrentOpenFlightList() {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetCurrentOpenFlightList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            String NowSecSeq = GetNowSecSeq();
            String ReopenSecSeq = GetReopenSecSeq();

            //正在開櫃航段
            String SQL = "SELECT FlightNo,SecSeq,DepStn,ArivStn, 'Open' AS Status " +
                    "FROM FLIGHT " +
                    "WHERE IsOpen = 1 " +
                    "AND IsClose = 0 ";

            //可重新開櫃航段
            if (ReopenSecSeq.length() > 0) {
                SQL += " UNION " +
                        "SELECT FlightNo,SecSeq,DepStn,ArivStn, 'Closed' AS Status " +
                        "FROM FLIGHT " +
                        "WHERE IsOpen = 1 " +
                        "AND IsClose = 1 " +
                        "AND SecSeq = '" + ReopenSecSeq + "' ";
            }

            //可開櫃航段
            SQL += " UNION " +
                    "SELECT FlightNo,SecSeq,DepStn,ArivStn, '' AS Status " +
                    "FROM FLIGHT " +
                    "WHERE IsOpen = 0 " +
                    "AND IsClose = 0 " +
                    "AND (CAST(SecSeq AS INTEGER) >= " + NowSecSeq + ") ";

            if (ReopenSecSeq.length() > 0) {
                SQL += " AND (CAST(SecSeq AS INTEGER) > " + ReopenSecSeq + ") ";
            }

            SQL += "ORDER BY SecSeq";

            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, _TSQL.SelectSQLJsonArray(SQL));

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 進行開櫃(Open)
     *
     * @param SecSeq     要Open的航段序號
     * @param CPID       CP ID
     * @param CPPassword CP 密碼
     * @param CAID       CA ID
     * @return (JSONObject) 標準格式回傳 成功與否
     */
    public JSONObject OpenFlightSecSeq(String SecSeq, String CPID, String CPPassword, String CAID) {

        String ClassName = "PublicFunctions";
        String FunctionName = "OpenFlightSecSeq";
        String ReturnCode, ReturnMessage, SQL;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq + "-CPID:" + CPID + "-CAID:" + CAID);

        try {
            //檢核SecSeq是可以進行開櫃的
            SQL = "SELECT * " +
                    "FROM FLIGHT " +
                    "WHERE IsOpen = 0 " +
                    "AND IsClose = 0 " +
                    "AND SecSeq = '" + SecSeq + "'";
            if (!_TSQL.CheckDataIsExist(SQL)) {
                ReturnCode = "8";
                ReturnMessage = "Open sector is wrong.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //檢核CA ID是否正確
            SQL = "SELECT CrewID " +
                    "FROM CrewInfo " +
                    "WHERE CrewID = '" + CAID + "'";
            if (!_TSQL.CheckDataIsExist(SQL)) {
                ReturnCode = "8";
                ReturnMessage = "CA ID Wrong...";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //檢核CP ID是否正確
            SQL = "SELECT CrewID " +
                    "FROM CrewInfo " +
                    "WHERE CrewID = '" + CPID +
                    "' AND CrewType = 'CP'";
            if (!_TSQL.CheckDataIsExist(SQL)) {
                ReturnCode = "8";
                ReturnMessage = "CP ID Wrong...";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //檢核CP 密碼是否正確
            SQL = "SELECT CrewID " +
                    "FROM CrewInfo " +
                    "WHERE CrewID = '" + CPID +
                    "' AND (Password1 = '" + CPPassword +
                    "' OR Password2 = '" + CPPassword +
                    "') AND CrewType = 'CP'";
            if (!_TSQL.CheckDataIsExist(SQL)) {
                ReturnCode = "8";
                ReturnMessage = "CP password Wrong...";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //進行開櫃動作
            SQL = "UPDATE FLIGHT " +
                    "SET IsOpen = 1, CrewID = '" + CAID +
                    "', PurserID = '" + CPID +
                    "' WHERE SecSeq = '" + SecSeq + "'";
            if (_TSQL.ExecutesSQLCommand(SQL)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Failed Open.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 進行開櫃設定(Open)
     *
     * @return (JSONObject) 標準格式回傳 成功與否
     */
    public JSONObject OpenFlight() {

        String ClassName = "PublicFunctions";
        String FunctionName = "OpenFlight";
        String ReturnCode, ReturnMessage, SQL;
        JSONArray ja;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {
            //先清空FlightData
            FlightData.ClearFlightData();

            //檢核有SecSeq是開櫃狀態的
            SQL = "SELECT * " +
                    "FROM FLIGHT " +
                    "WHERE IsOpen = 1 " +
                    "AND IsClose = 0 ";
            ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja == null || ja.length() == 0) {
                ReturnCode = "8";
                ReturnMessage = "Get sector info failed.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //逐項塞值
            FlightData.FlightNo = ja.getJSONObject(0).getString("FlightNo");
            FlightData.SecSeq = ja.getJSONObject(0).getString("SecSeq");
            FlightData.FlightDate = ja.getJSONObject(0).getString("FlightDate");
            FlightData.Sector = ja.getJSONObject(0).getString("DepStn") + "-" + ja.getJSONObject(0).getString("ArivStn");
            FlightData.CartNo = ja.getJSONObject(0).getString("CarNo");
            FlightData.CrewID = ja.getJSONObject(0).getString("CrewID");
            FlightData.PurserID = ja.getJSONObject(0).getString("PurserID");
            FlightData.AESKey = FlightData.FlightDate + FlightData.CartNo;
            FlightData.MachineID = "";

            //折扣：
            SQL = "SELECT Type,DiscountRate,DiscountAmount,DiscountGift,Description,Upperlimit,FuncID,Progression " +
                    "FROM Discount ";
            ja = _TSQL.SelectSQLJsonArray(SQL);
            for (int i = 0; i < ja.length(); i++) {
                FlightData.SetDiscount(ja.getJSONObject(i).getString("Type"),
                        ja.getJSONObject(i).getDouble("DiscountRate"),
                        ja.getJSONObject(i).getInt("DiscountAmount"),
                        ja.getJSONObject(i).getString("DiscountGift"),
                        ja.getJSONObject(i).getString("Description"),
                        ja.getJSONObject(i).getInt("Upperlimit"),
                        ja.getJSONObject(i).getString("FuncID"),
                        ja.getJSONObject(i).getString("Progression"));
            }

            //折扣條件：
            SQL = "SELECT FuncID,Syntax,Description,ChildID " +
                    "FROM FuncTable ";
            ja = _TSQL.SelectSQLJsonArray(SQL);
            for (int i = 0; i < ja.length(); i++) {
                FlightData.SetFuncTable(
                        ja.getJSONObject(i).getString("FuncID"),
                        ja.getJSONObject(i).getString("Syntax"),
                        ja.getJSONObject(i).getString("Description"),
                        ja.getJSONObject(i).getString("ChildID"));
            }

            //匯率：
            SQL = "SELECT CurDdd,CurDvr,ExchRate,MiniValue " +
                    "FROM Rate ";
            ja = _TSQL.SelectSQLJsonArray(SQL);
            for (int i = 0; i < ja.length(); i++) {
                FlightData.SetRate(
                        ja.getJSONObject(i).getString("CurDdd"),
                        ja.getJSONObject(i).getString("CurDvr"),
                        Double.valueOf(ja.getJSONObject(i).getString("ExchRate")),
                        Double.valueOf(ja.getJSONObject(i).getString("MiniValue")));
            }

            //更改開櫃狀態
            FlightData.OpenFlightFlag = true;

            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 進行關櫃(Close)
     *
     * @return (JSONObject) 標準格式回傳 成功與否
     */
    public JSONObject CloseFlightSecSeq() {

        String ClassName = "PublicFunctions";
        String FunctionName = "CloseFlightSecSeq";
        String ReturnCode, ReturnMessage, SQL;
        ArrayList SQLCommands = new ArrayList();
        JSONArray ja;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {
            //檢核有SecSeq是可以進行關櫃的
            SQL = "SELECT * " +
                    "FROM FLIGHT " +
                    "WHERE IsOpen = 1 " +
                    "AND IsClose = 0 " +
                    "AND SecSeq = '" + this.SecSeq + "'";
            if (!_TSQL.CheckDataIsExist(SQL)) {
                ReturnCode = "8";
                ReturnMessage = "Close sector is wrong.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //進行關櫃動作
            SQL = "UPDATE FLIGHT " +
                    "SET IsClose = 1 " +
                    "WHERE SecSeq = '" + this.SecSeq + "'";
            SQLCommands.add(SQL);

            SQL = "SELECT ItemCode, StandQty, DamageQty, EndQty " +
                    "FROM Inventory " +
                    "WHERE SecSeq = '" + this.SecSeq + "'";
            ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("ItemCode")) {

                int MaxCount = ja.length();
                for (int i = 0; i < MaxCount; i++) {
                    SQL = "UPDATE Inventory " +
                            "SET EGASCheckQty = " + ja.getJSONObject(i).getString("StandQty") +
                            ", EGASDamageQty = 0" +
                            ", EVACheckQty = " + ja.getJSONObject(i).getString("EndQty") +
                            ", EVADamageQty = " + ja.getJSONObject(i).getString("DamageQty") +
                            " WHERE SecSeq = '" + this.SecSeq + "'" +
                            " AND ItemCode = '" + ja.getJSONObject(i).getString("ItemCode") + "'";
                    SQLCommands.add(SQL);
                }
            } else {
                ReturnCode = "8";
                ReturnMessage = "Inventory error.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //若非為最後航段，則將現有航段資訊傳到下一航段
            if (!SecSeq.equals("9")) {
                if (ja.length() > 0 && !ja.getJSONObject(0).isNull("ItemCode")) {

                    int MaxCount = ja.length();
                    for (int i = 0; i < MaxCount; i++) {

                        SQL = "UPDATE Inventory " +
                                "SET StartQty = " + ja.getJSONObject(i).getString("EndQty") +
                                ", EndQty = " + ja.getJSONObject(i).getString("EndQty") +
//                                ", EGASCheckQty = " + ja.getJSONObject(i).getString("StandQty") +
//                                ", EGASDamageQty = 0"+
                                ", EVACheckQty = " + ja.getJSONObject(i).getString("EndQty") +
//                                ", EVADamageQty = " + ja.getJSONObject(i).getString("DamageQty") +
                                ", DamageQty = " + ja.getJSONObject(i).getString("DamageQty") +
                                " WHERE CAST(SecSeq AS INTEGER) > " + this.SecSeq +
                                " AND ItemCode = '" + ja.getJSONObject(i).getString("ItemCode") + "'";
                        SQLCommands.add(SQL);

                        SQL = "UPDATE Inventory " +
                                "SET EGASCheckQty = " + ja.getJSONObject(i).getString("StandQty") +
//                                ", EndQty = " + ja.getJSONObject(i).getString("EndQty") +
//                                ", EGASCheckQty = " + ja.getJSONObject(i).getString("StandQty") +
                                ", EGASDamageQty = 0" +
                                ", EVACheckQty = " + ja.getJSONObject(i).getString("EndQty") +
                                ", EVADamageQty = " + ja.getJSONObject(i).getString("DamageQty") +
//                                ", DamageQty = " + ja.getJSONObject(i).getString("DamageQty") +
                                " WHERE SecSeq = '" + this.SecSeq + "'" +
                                " AND ItemCode = '" + ja.getJSONObject(i).getString("ItemCode") + "'";
                        SQLCommands.add(SQL);
                    }

                } else {
                    ReturnCode = "8";
                    ReturnMessage = "Inventory error.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }
            }

            //關閉Flight Data
            FlightData.ClearFlightData();

            //寫入資料
            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Failed Close.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 進行重新開櫃(Reopen)
     *
     * @param SecSeq     要Reopen的航段序號
     * @param CPID       CP ID
     * @param CPPassword CP 密碼
     * @param CAID       CA ID
     * @return (JSONObject) 標準格式回傳 成功與否
     */
    public JSONObject ReopenFlightSecSeq(String SecSeq, String CPID, String CPPassword, String CAID) {

        String ClassName = "PublicFunctions";
        String FunctionName = "ReopenFlightSecSeq";
        String ReturnCode, ReturnMessage, SQL;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq + "-CPID:" + CPID + "-CPPassword:" + CPPassword + "-CAID:" + CAID);

        try {

            //檢核航段序號是否可Reopen
            if (!SecSeq.equals(GetReopenSecSeq())) {
                ReturnCode = "8";
                ReturnMessage = "Please check reopen sector.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //檢核CA資訊是否等於最後關櫃航段CA
            SQL = "SELECT CrewID " +
                    "FROM Flight " +
                    "WHERE SecSeq = '" + SecSeq +
                    "' AND CrewID = '" + CAID + "'";
            if (!_TSQL.CheckDataIsExist(SQL)) {
                ReturnCode = "8";
                ReturnMessage = "CA ID Wrong...";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //檢核CP資訊是否等於最後關櫃航段CP
            SQL = "SELECT CrewID " +
                    "FROM Flight " +
                    "WHERE SecSeq = '" + SecSeq +
                    "' AND PurserID = '" + CPID + "'";
            if (!_TSQL.CheckDataIsExist(SQL)) {
                ReturnCode = "8";
                ReturnMessage = "CP ID Wrong...";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //檢核CP帳密是否正確
            SQL = "SELECT CrewID " +
                    "FROM CrewInfo " +
                    "WHERE CrewID = '" + CPID +
                    "' AND (Password1 = '" + CPPassword +
                    "' OR Password2 = '" + CPPassword + "')";
            if (!_TSQL.CheckDataIsExist(SQL)) {
                ReturnCode = "8";
                ReturnMessage = "CP password Wrong...";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //進行重新開櫃
            SQL = "UPDATE FLIGHT " +
                    "SET IsClose = 0 " +
                    "WHERE SecSeq = '" + SecSeq + "'";
            if (_TSQL.ExecutesSQLCommand(SQL)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Failed Reopen.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }
        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 取得特定組員資訊
     *
     * @param CrewID       Crew ID
     * @param CrewPassword Crew 密碼
     * @param CrewType     確認類別
     * @return (JSONObject) 標準格式回傳 Crew資訊
     */
    public JSONObject GetCrewInfo(String CrewID, String CrewPassword, String CrewType) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetCrewInfo_1";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "CrewID:" + CrewID + "-CrewPassword:" + CrewPassword + "-CrewType:" + CrewType);

        try {

            String SQL = "SELECT CrewID,CrewType,Name " +
                    "FROM CrewInfo " +
                    "WHERE CrewID = '" + CrewID +
                    "' AND (Password1 = '" + CrewPassword +
                    "' OR Password2 = '" + CrewPassword +
                    "') AND CrewType = '" + CrewType + "'";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("CrewID")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";

                if (CrewType.equals("CP")) {
                    ReturnMessage = "CP info is not exist.";
                } else {
                    ReturnMessage = "CA info is not exist.";
                }

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
     * 取得特定組員資訊
     *
     * @param CrewID       Crew ID
     * @param CrewPassword Crew 密碼
     * @return (JSONObject) 標準格式回傳 Crew資訊
     */
    public JSONObject GetCrewInfo(String CrewID, String CrewPassword) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetCrewInfo_2";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "CrewID:" + CrewID + "-CrewPassword:" + CrewPassword);

        try {

            String SQL = "SELECT CrewID,CrewType,Name " +
                    "FROM CrewInfo " +
                    "WHERE CrewID = '" + CrewID +
                    "' AND (Password1 = '" + CrewPassword +
                    "' OR Password2 = '" + CrewPassword +
                    "')";

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

    /**
     * 取得特定組員資訊
     *
     * @param CrewID Crew ID
     * @return (JSONObject) 標準格式回傳 Crew資訊
     */
    public JSONObject GetCrewInfo(String CrewID) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetCrewInfo_3";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "CrewID:" + CrewID);

        try {

            String SQL = "SELECT CrewID,CrewType,Name " +
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

    /**
     * 取得可以使用的幣別
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetAllCurrencyList() {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetAllCurrencyList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {
            //取得購物車顯示幣別資訊。
            String SQL = "SELECT CurDvr,MiniValue,'N' AS BasketCurrency,CashCurrency,CardCurrency,TaiwanCardCurrency " +
                    "FROM Rate ";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("CurDvr")) {

                JSONObject jo;
                //增加註記哪個是購物車頁面可顯示的幣別
                for (int i = 0; i < ja.length(); i++) {
                    if (ja.getJSONObject(i).getString("CurDvr").equals("USD") ||
                            ja.getJSONObject(i).getString("CurDvr").equals("TWD") ||
                            ja.getJSONObject(i).getString("CurDvr").equals("JPY") ||
                            ja.getJSONObject(i).getString("CurDvr").equals("CNY")) {
                        jo = ja.getJSONObject(i);
                        jo.put("BasketCurrency", "Y");
                    }
                }

                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Get basket currency list error.";

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
     * 取得該航班所有的車櫃號
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetAllCartList() {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetAllCartList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {
            //取得購物車顯示幣別資訊。
            String SQL = "SELECT CartNo " +
                    "FROM CartList ";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("CartNo")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Get basket currency list error.";

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

    //////////////////////////////////////////////////////////////////////////
    //                           IFE System
    //////////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////////
    //                         DFS Transaction
    //////////////////////////////////////////////////////////////////////////

    /**
     * 取得免稅品交易單據資訊(DFS or Vip Sale)
     *
     * @param ReceiptNo 交易單號
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetDFSTransactionInfo(String ReceiptNo) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetDFSTransactionInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "ReceiptNo:" + ReceiptNo);

        JSONArray jaSalesItemList = new JSONArray(), jaHead, jaPaymentList, jaItemList;
        JSONObject joSalesItem, ResponseJsonObject;
        String SQL;

        String DiscountType = "", DiscountNo = "";

        try {
            //先取得銷售資訊以及所屬單據類別
            SQL = "SELECT ReceiptNo, OriPrice, TotalPrice, UpperLimitType, UpperLimitNo, Status, PreorderNo, SeatNo " +
                    "FROM SalesHead " +
                    "WHERE SecSeq = '" + this.SecSeq +
                    "' AND ReceiptNo = '" + ReceiptNo + "'";
            jaHead = _TSQL.SelectSQLJsonArray(SQL);

            if (jaHead == null || jaHead.length() == 0) {
                ReturnCode = "8";
                ReturnMessage = "Receipt no not found.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //取得銷售資訊和商品列表
            if (jaHead.getJSONObject(0).getString("PreorderNo").length() == 0) {

                //DFS商品
                SQL = "SELECT Inventory.DrawNo, SerialCode, VipType, VipNo, " +
                        "SalesDetail.ItemCode, AllParts.ItemName, OriPrice, SalesDetail.SalesPrice, SalesDetail.SalesQty " +
                        "FROM SalesDetail LEFT JOIN Inventory " +
                        "ON SalesDetail.[ItemCode] = Inventory.[ItemCode] " +
                        "LEFT JOIN AllParts " +
                        "ON SalesDetail.[ItemCode] = AllParts.[ItemCode] " +
                        "WHERE Inventory.DrawNo <> '' AND Inventory.Secseq = '" + this.SecSeq +
                        "' AND SalesDetail.ReceiptNo = '" + ReceiptNo + "'";
                jaItemList = _TSQL.SelectSQLJsonArray(SQL);

                //帶出商品
                for (int i = 0; i < jaItemList.length(); i++) {

                    joSalesItem = new JSONObject();
                    joSalesItem.put("SerialCode", jaItemList.getJSONObject(i).getString("SerialCode"));
                    joSalesItem.put("ItemCode", jaItemList.getJSONObject(i).getString("ItemCode"));
                    joSalesItem.put("ItemName", jaItemList.getJSONObject(i).getString("ItemName"));
                    joSalesItem.put("OriginalPrice", jaItemList.getJSONObject(i).getDouble("OriPrice"));
                    joSalesItem.put("USDPrice", jaItemList.getJSONObject(i).getDouble("SalesPrice"));
                    joSalesItem.put("SalesQty", jaItemList.getJSONObject(i).getInt("SalesQty"));
                    jaSalesItemList.put(joSalesItem);

                    if (jaItemList.getJSONObject(i).getString("VipType").length() > 0) {
                        DiscountType = jaItemList.getJSONObject(i).getString("VipType");
                        DiscountNo = jaItemList.getJSONObject(i).getString("VipNo");
                    }
                }
            } else {

                //VS商品
                SQL = "SELECT PreorderNo,ItemCode,SerialCode,ItemName,OriginalPrice,SalesPrice,SalesQty " +
                        "FROM PreorderDetail " +
                        "WHERE PreorderNo = '" + jaHead.getJSONObject(0).getString("PreorderNo") + "'";
                jaItemList = _TSQL.SelectSQLJsonArray(SQL);

                //帶出商品
                for (int i = 0; i < jaItemList.length(); i++) {

                    joSalesItem = new JSONObject();
                    joSalesItem.put("SerialCode", jaItemList.getJSONObject(i).getString("SerialCode"));
                    joSalesItem.put("ItemCode", jaItemList.getJSONObject(i).getString("ItemCode"));
                    joSalesItem.put("ItemName", jaItemList.getJSONObject(i).getString("ItemName"));
                    joSalesItem.put("OriginalPrice", jaItemList.getJSONObject(i).getDouble("OriginalPrice"));
                    joSalesItem.put("USDPrice", jaItemList.getJSONObject(i).getDouble("SalesPrice"));
                    joSalesItem.put("SalesQty", jaItemList.getJSONObject(i).getInt("SalesQty"));
                    jaSalesItemList.put(joSalesItem);
                }
            }

            //帶出付款歷程
            SQL = "SELECT Currency, PayBy, Amount, USDAmount, CouponNo, CardNo, CardName, CardType, CardDate " +
                    "FROM SalesHead LEFT JOIN PaymentInfo " +
                    "ON SalesHead.ReceiptNo = PaymentInfo.ReceiptNo " +
                    "WHERE SalesHead.Secseq = '" + this.SecSeq +
                    "' AND SalesHead.ReceiptNo = '" + ReceiptNo +
                    "' AND PaymentInfo.Status = '" + jaHead.getJSONObject(0).getString("Status") + "'";
            jaPaymentList = _TSQL.SelectSQLJsonArray(SQL);

            if (jaPaymentList == null || jaPaymentList.length() == 0) {
                ReturnCode = "8";
                ReturnMessage = "Receipt no not found.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //將信用卡密碼解密
            for (int i = 0; i < jaPaymentList.length(); i++) {

                if (jaPaymentList.getJSONObject(i).getString("PayBy").equals("Card")) {
                    jaPaymentList.getJSONObject(i).put("CardNo", AESEncrypDecryp.getDectyptData(jaPaymentList.getJSONObject(i).getString("CardNo"), FlightData.AESKey));
                    jaPaymentList.getJSONObject(i).put("CardDate", AESEncrypDecryp.getDectyptData(jaPaymentList.getJSONObject(i).getString("CardDate"), FlightData.AESKey));
                }
            }

            ResponseJsonObject = new JSONObject();
            ResponseJsonObject.put("ReceiptNo", jaHead.getJSONObject(0).getString("ReceiptNo"));
            ResponseJsonObject.put("Status", jaHead.getJSONObject(0).getString("Status"));
            ResponseJsonObject.put("SeatNo", jaHead.getJSONObject(0).getString("SeatNo"));
            ResponseJsonObject.put("PreorderNo", jaHead.getJSONObject(0).getString("PreorderNo"));
            ResponseJsonObject.put("OriUSDAmount", jaHead.getJSONObject(0).getDouble("OriPrice"));
            ResponseJsonObject.put("USDAmount", jaHead.getJSONObject(0).getDouble("TotalPrice"));
            ResponseJsonObject.put("DiscountType", DiscountType);
            ResponseJsonObject.put("DiscountNo", DiscountNo);
            ResponseJsonObject.put("UpperLimitType", jaHead.getJSONObject(0).getString("UpperLimitType"));
            ResponseJsonObject.put("UpperLimitNo", jaHead.getJSONObject(0).getString("UpperLimitNo"));
            ResponseJsonObject.put("Items", jaSalesItemList);
            ResponseJsonObject.put("PaymentList", jaPaymentList);

            ReturnCode = "0";
            ReturnMessage = "";
            JSONArray ReturnData = new JSONArray();
            ReturnData.put(ResponseJsonObject);
            return GetReturnJsonObject(ReturnCode, ReturnMessage, ReturnData);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 判別Coupon幣別和金額
     *
     * @param CouponType 欲判斷的Coupon類別 SC or DC
     * @param CouponCode 欲判斷的Coupon條碼
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetCouponInfo(String CouponType, String CouponCode) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetCouponInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONArray ja = new JSONArray();
        JSONObject jo = new JSONObject();

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "CouponType:" + CouponType + "-CouponCode:" + CouponCode);

        try {
            //判斷此折扣券是否已經使用過。
            String SQL = "SELECT CouponNo " +
                    "FROM SalesHead LEFT JOIN PaymentInfo " +
                    "ON SalesHead.ReceiptNo = PaymentInfo.ReceiptNo " +
                    "WHERE SalesHead.Status = 'S' " +
                    "AND CouponNo = '" + CouponCode + "'";

            //若已經使用過
            if (_TSQL.CheckDataIsExist(SQL)) {
                ReturnCode = "8";
                ReturnMessage = "Coupon repeated!";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            SQL = "SELECT * FROM SCoupon WHERE '" + CouponCode + "' BETWEEN StartDate AND ExpireDate";

            JSONArray jsonArray = _TSQL.SelectSQLJsonArray(SQL);

            switch (CouponType) {
                case "SC":
                    //拆解SC

                    //S/C 樂購卷 長度10碼，每張面額TWD 500
                    if (CouponCode.length() != 10) {
                        ReturnCode = "8";
                        ReturnMessage = "Wrong S/C Format!";

                        //寫入Log
                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                        return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                    }

                    //前兩碼須為數字
                    if (!Character.isDigit(CouponCode.charAt(0)) ||
                            !Character.isDigit(CouponCode.charAt(1))) {
                        ReturnCode = "8";
                        ReturnMessage = "Wrong S/C Format!";

                        //寫入Log
                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                        return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                    }

                    //檢核票據有效日期
//                    if ((Integer.valueOf(CouponCode.substring(0, 2)) + 2) < Integer.valueOf(FlightData.FlightDate.substring(2, 4))) {
//                        ReturnCode = "8";
//                        ReturnMessage = "This coupon is expired!";
//
//                        //寫入Log
//                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
//                        return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
//                    }
                    if (jsonArray == null) {
                        ReturnCode = "8";
                        ReturnMessage = "This coupon is expired!";

                        //寫入Log
                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                        return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                    }

                    jo.put("CouponCurrency", "TWD");
                    jo.put("CouponAmount", 500);

                    ja.put(jo);

                    ReturnCode = "0";
                    ReturnMessage = "";
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
                case "DC":
                    //拆解DC

                    //D/C 樂購卷 長度6碼
                    if (CouponCode.length() != 6) {
                        ReturnCode = "8";
                        ReturnMessage = "Wrong D/C Format!";

                        //寫入Log
                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                        return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                    }

                    //檢核 前四碼數字，後兩碼英文
                    if (!(Character.isDigit(CouponCode.charAt(0)) &&
                            Character.isDigit(CouponCode.charAt(1)) &&
                            Character.isDigit(CouponCode.charAt(2)) &&
                            Character.isDigit(CouponCode.charAt(3)) &&
                            Character.isLetter(CouponCode.charAt(4)) &&
                            Character.isLetter(CouponCode.charAt(5)))) {
                        ReturnCode = "8";
                        ReturnMessage = "Wrong D/C Format!";

                        //寫入Log
                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                        return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                    }

                    //檢核票據金額
                    if ((int) CouponCode.charAt(5) >= 65 &&
                            (int) CouponCode.charAt(5) <= 77) {

                        //A ~ M 面額10元
                        jo.put("CouponCurrency", "USD");
                        jo.put("CouponAmount", 10);
                        ja.put(jo);

                        ReturnCode = "0";
                        ReturnMessage = "";
                        return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
                    } else {
                        //N ~ Z 面額50元
                        jo.put("CouponCurrency", "USD");
                        jo.put("CouponAmount", 50);
                        ja.put(jo);

                        ReturnCode = "0";
                        ReturnMessage = "";
                        return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
                    }
                default:
                    ReturnCode = "8";
                    ReturnMessage = "Coupon type error!";

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
     * 判別信用卡黑名單
     *
     * @param CardType 欲判斷的信用卡類別
     * @param CardNo   欲判斷的信用卡號碼
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject CheckBlackCard(String CardType, String CardNo) {

        String ClassName = "PublicFunctions";
        String FunctionName = "CheckBlackCard";
        String ReturnCode, ReturnMessage, SQL;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        TSQL _BlackTSQL = TSQL.getINSTANCE(context, this.SecSeq, "Black");
        TSQL _CUPBlackTSQL = TSQL.getINSTANCE(context, this.SecSeq, "CUPBlack");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "CardType:" + CardType + "-CardNo:" + CardNo);

        try {

            if (CardType.equals("VISA") ||
                    CardType.equals("MASTER") ||
                    CardType.equals("JCB") ||
                    CardType.equals("AMX")) {
                //V.M

                //先檢查前六碼
                SQL = "SELECT BinCode " +
                        "FROM BinBlack " +
                        "WHERE BinCode = '" + CardNo.substring(0, 6) + "'";
                if (_BlackTSQL.CheckDataIsExist(SQL)) {
                    ReturnCode = "8";
                    ReturnMessage = "Bin code blocked.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //再找全碼
                SQL = "SELECT BlackNo " +
                        "FROM VMBlack " +
                        "WHERE BlackNo = '" + CardNo + "'";
                if (_BlackTSQL.CheckDataIsExist(SQL)) {
                    ReturnCode = "8";
                    ReturnMessage = "Black Card.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //都過了
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);

            } else {
                //CUP

                //先檢查白名單
                SQL = "SELECT WhiteNo " +
                        "FROM CUPWhiteNo " +
                        "WHERE WhiteNo = '" + CardNo.substring(0, 6) + "'";
                if (!_BlackTSQL.CheckDataIsExist(SQL)) {
                    ReturnCode = "8";
                    ReturnMessage = "Not accepted card!";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //再找差異檔和漂白卡
                SQL = "SELECT BlackNo,Type " +
                        "FROM CUPDifferenceBlack " +
                        "WHERE BlackNo = '" + CardNo + "'";
                Object Type = _BlackTSQL.SelectSQLObject(SQL);

                if (Type != null && String.valueOf(Type).equals("W")) {
                    //漂白卡
                    ReturnCode = "0";
                    ReturnMessage = "";
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                } else if (Type != null && String.valueOf(Type).equals("B")) {
                    ReturnCode = "8";
                    ReturnMessage = "Black Card.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //最後找黑名單月主檔
                SQL = "SELECT BlackNo " +
                        "FROM CUPBlack" + CardNo.substring(11, 12) +
                        " WHERE BlackNo = '" + CardNo + "'";
                if (_CUPBlackTSQL.CheckDataIsExist(SQL)) {
                    ReturnCode = "8";
                    ReturnMessage = "Black Card.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //都過了
                ReturnCode = "0";
                ReturnMessage = "";
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
     * 幣別轉換
     *
     * @param FromCurrency     被轉換的原始幣別
     * @param ToCurrency       要轉換的目的幣別
     * @param OriAmount        原始幣別金額
     * @param DecimalPoint     要取得的位數 ex:100 = 百位數 0.1 = 小數點第一位
     * @param MappingMiniValue 是否要取得目的幣別最小進位，若是與上一個參數衝突，則以此參數為主
     * @param Type             1. 無條件進位    2. 四捨五入    3. 無條件捨去
     * @return 目的幣別金額
     */
    public JSONObject ChangeCurrencyAmount(String FromCurrency, String ToCurrency, double OriAmount, double DecimalPoint, boolean MappingMiniValue, int Type) {

        String ClassName = "PublicFunctions";
        String FunctionName = "ChangeCurrencyAmount";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        RateRow _RateRow;
        String SQL;
        double Rate;  //折扣率
        double NewAmount;  //換算後金額

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName,
                "Function start:" + "FromCurrency:" + FromCurrency + "-ToCurrency:" + ToCurrency + "-OriAmount:" + OriAmount + "-DecimalPoint:" + DecimalPoint
                        + "-MappingMiniValue:" + MappingMiniValue + "-Type:" + Type);

        try {
            if (!FlightData.OpenFlightFlag) {
                ReturnCode = "8";
                ReturnMessage = "Please open flight.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //同幣別間切換
            if (FromCurrency.equals(ToCurrency)) {

                NewAmount = OriAmount;
                if (MappingMiniValue) {
                    //回覆符合最小進位的金額
                    NewAmount = DecimalPointChange(NewAmount, GetMiniValue(ToCurrency), Type);
                } else {
                    //直接擷取位數回去
                    NewAmount = DecimalPointChange(NewAmount, DecimalPoint, Type);
                }
            }
            //美金換台幣
            else if (FromCurrency.equals("USD") && ToCurrency.equals("TWD")) {

                //先換算
//                SQL = "SELECT ExchRate,MiniValue " +
//                        "FROM Rate " +
//                        "WHERE CurDdd = '" + "TWD" +
//                        "' AND CurDvr = '" + "USD" + "'";
//                Rate = Double.valueOf(String.valueOf(_TSQL.SelectSQLObject(SQL)));
                _RateRow = FlightData.GetRate(ToCurrency, FromCurrency);
                NewAmount = Arith.mul(OriAmount, _RateRow.ExchRate);

                if (MappingMiniValue) {
                    //回覆符合最小進位的金額
                    NewAmount = DecimalPointChange(NewAmount, GetMiniValue(ToCurrency), Type);
                } else {
                    //直接擷取位數回去
                    NewAmount = DecimalPointChange(NewAmount, DecimalPoint, Type);
                }
            }
            //美金換台幣外其他幣別
            else if (FromCurrency.equals("USD") && !ToCurrency.equals("TWD")) {

                //先換算
//                SQL = "SELECT ExchRate,MiniValue " +
//                        "FROM Rate " +
//                        "WHERE CurDdd = '" + "USD" +
//                        "' AND CurDvr = '" + ToCurrency + "'";
//                Rate = Double.valueOf(String.valueOf(_TSQL.SelectSQLObject(SQL)));
                _RateRow = FlightData.GetRate(FromCurrency, ToCurrency);
                NewAmount = Arith.div(OriAmount, _RateRow.ExchRate);

                if (MappingMiniValue) {
                    //回覆符合最小進位的金額
                    NewAmount = DecimalPointChange(NewAmount, GetMiniValue(ToCurrency), Type);
                } else {
                    //直接擷取位數回去
                    NewAmount = DecimalPointChange(NewAmount, DecimalPoint, Type);
                }
            }
            //台幣換美金外其他幣別
            else if (FromCurrency.equals("TWD") && !ToCurrency.equals("USD")) {

                //先台幣換算成美金
                _RateRow = FlightData.GetRate("USD", FromCurrency);
                NewAmount = Arith.mul(OriAmount, _RateRow.ExchRate);

                //再美金換算成外幣
                _RateRow = FlightData.GetRate("USD", ToCurrency);
                NewAmount = Arith.div(NewAmount, _RateRow.ExchRate);

                if (MappingMiniValue) {
                    //回覆符合最小進位的金額
                    NewAmount = DecimalPointChange(NewAmount, GetMiniValue(ToCurrency), Type);
                } else {
                    //直接擷取位數回去
                    NewAmount = DecimalPointChange(NewAmount, DecimalPoint, Type);
                }
            }
            //其他幣別換美金
            else if (ToCurrency.equals("USD")) {

                //先換算
//                SQL = "SELECT ExchRate,MiniValue " +
//                        "FROM Rate " +
//                        "WHERE CurDdd = '" + "USD" +
//                        "' AND CurDvr = '" + FromCurrency + "'";
//                Rate = Double.valueOf(String.valueOf(_TSQL.SelectSQLObject(SQL)));
                _RateRow = FlightData.GetRate(ToCurrency, FromCurrency);
                NewAmount = Arith.mul(OriAmount, _RateRow.ExchRate);

                if (MappingMiniValue) {
                    //回覆符合最小進位的金額
                    NewAmount = DecimalPointChange(NewAmount, GetMiniValue(ToCurrency), Type);
                } else {
                    //直接擷取位數回去
                    NewAmount = DecimalPointChange(NewAmount, DecimalPoint, Type);
                }
            }
            //出現錯誤
            else {
                ReturnCode = "8";
                ReturnMessage = "Currency error.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            ReturnCode = "0";
            ReturnMessage = "";
            jo.put("NewAmount", NewAmount);
            ja.put(jo);
            return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 取得特定信用卡可以使用的交易幣別
     *
     * @param CardType 要查詢的信用卡別
     * @param CardNo   要查詢的信用卡號，不使用傳Null
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetCardCurrencyList(String CardType, String CardNo) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetCardCurrencyList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "CardType:" + CardType + "-CardNo:" + CardNo);

        try {
            //取得購物車顯示幣別資訊。
            String SQL = "SELECT Currency " +
                    "FROM CreditCard " +
                    "WHERE CardType = '" + CardType + "'";
            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("Currency")) {

                //判斷是不是台灣信用卡
                if (CardNo != null && CheckIsTaiwanCard(CardNo)) {
                    //刪除台幣外的幣別
                    for (int i = 0; i < ja.length(); i++) {
                        if (!ja.getJSONObject(i).getString("Currency").equals("TWD")) {
                            ja.remove(i);
                            i--;
                            break;
                        }
                    }
                }

                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Card Type error.";

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
     * 取得特定信用卡商店代碼資訊
     *
     * @param CardType 信用卡類別
     * @param Currency 消費幣別
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetCardMIDInfo(String CardType, String Currency) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetCardMIDInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "CardType:" + CardType + "-Currency:" + Currency);

        try {
            //取得購物車顯示幣別資訊。
            String SQL = "SELECT MID,BankText " +
                    "FROM CreditCard " +
                    "WHERE CardType = '" + CardType +
                    "' AND Currency = '" + Currency + "'";
            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("MID")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Card info error.";

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
     * 取得特定信用卡別最大額度資訊
     *
     * @param CardType 信用卡類別
     * @param Currency 消費幣別
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetCardMaxAmountInfo(String CardType, String Currency) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetCardMaxAmountInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "CardType:" + CardType + "-Currency:" + Currency);

        try {
            //取得購物車顯示幣別資訊。
            String SQL = "SELECT MaxAmount, MaxAmount AS TWDMaxAmount " +
                    "FROM CreditCard " +
                    "WHERE CardType = '" + CardType +
                    "' AND Currency = '" + Currency + "'";
            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("MaxAmount")) {

                //將台幣金額依據匯率更換
                ja.getJSONObject(0).put("TWDMaxAmount", ChangeCurrencyAmount("USD", "TWD", ja.getJSONObject(0).getDouble("MaxAmount"), 1, false, 2).getJSONArray(
                        "ResponseData").getJSONObject(0).getDouble("NewAmount"));

                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Card info error.";

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
     * 取得特定信用卡的特定航段刷卡次數和金額(需加密查詢)
     *
     * @param CardNo 信用卡號
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetCardTotalPayInfo(String CardNo) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetCardTotalPayInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONArray ResponseData = new JSONArray();

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "CardNo:" + CardNo);

        try {
            //取得該卡別消費紀錄
            String SQL = "SELECT IFNULL(SUM(PaymentInfo.USDAmount),0) AS SwipeUSDAmount, COUNT(PaymentInfo.ReceiptNo) AS SwipeCount " +
                    "FROM SalesHead LEFT JOIN PaymentInfo " +
                    "ON SalesHead.ReceiptNo = PaymentInfo.ReceiptNo " +
                    "WHERE SalesHead.SecSeq = '" + this.SecSeq +
                    "' AND SalesHead.Status = 'S' " +
                    "AND PaymentInfo.PayBy = 'Card' " +
                    "AND PaymentInfo.CardNo = '" + AESEncrypDecryp.getEncryptData(CardNo, FlightData.AESKey) + "'";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("SwipeUSDAmount")) {

                JSONObject jo = new JSONObject();
                jo.put("SwipeUSDAmount", ja.getJSONObject(0).getDouble("SwipeUSDAmount"));
                jo.put("SwipeCount", ja.getJSONObject(0).getDouble("SwipeCount"));
                ResponseData.put(jo);

                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ResponseData);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Card info error.";

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
     * 判斷Coupon是否已經用過
     *
     * @param CouponNo 折扣卷號
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject CheckCouponNoIsExist(String CouponNo) {

        String ClassName = "PublicFunctions";
        String FunctionName = "CreateNewDatabase";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "CouponNo:" + CouponNo);

        try {
            //取得購物車顯示幣別資訊。
            String SQL = "SELECT CouponNo " +
                    "FROM SalesHead LEFT JOIN PaymentInfo " +
                    "ON SalesHead.ReceiptNo = PaymentInfo.ReceiptNo " +
                    "WHERE SalesHead.Status = 'S' " +
                    "AND CouponNo = '" + CouponNo + "'";

            if (_TSQL.CheckDataIsExist(SQL)) {
                ReturnCode = "8";
                ReturnMessage = "Coupon repeated!";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "0";
                ReturnMessage = "";
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

    //////////////////////////////////////////////////////////////////////////
    //                         DFS Transaction
    //////////////////////////////////////////////////////////////////////////

    /**
     * 取得升艙等交易單據資訊(Upgrade)
     *
     * @param ReceiptNo   交易單號
     * @param SalesStatus 查詢的銷售狀態(S or R)
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetUpgradeTransactionInfo(String ReceiptNo, String SalesStatus) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetUpgradeTransactionInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONArray jaSalesItemList, jaHead, jaPaymentList, jaItemList, ResponseData = new JSONArray();
        JSONObject joSalesItem, ReceiptDataObject;
        String SQL;

        String DiscountType = "", DiscountNo = "";

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "ReceiptNo:" + ReceiptNo + "-SalesStatus:" + SalesStatus);

        try {
            //先取得銷售資訊以及所屬單據類別
            SQL = "SELECT ReceiptNo, TotalPrice, Status " +
                    "FROM ClassSalesHead " +
                    "WHERE SecSeq = '" + this.SecSeq + "'";

            if (SalesStatus != null && SalesStatus.length() > 0) {
                SQL += " AND Status = '" + SalesStatus + "'";
            }

            if (ReceiptNo != null && ReceiptNo.length() > 0) {
                SQL += " AND ReceiptNo = '" + ReceiptNo + "'";
            }

            jaHead = _TSQL.SelectSQLJsonArray(SQL);

            if (jaHead == null || jaHead.length() == 0) {
                ReturnCode = "8";
                ReturnMessage = "Receipt no not found.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //逐筆取出資料
            for (int i = 0; i < jaHead.length(); i++) {
                //取得銷售資訊和商品列表
                SQL = "SELECT ReceiptNo,Infant,OriginalClass,NewClass,SalesPrice,SalesQty " +
                        "FROM ClassSalesDetail " +
                        "WHERE SecSeq = '" + this.SecSeq +
                        "' AND ReceiptNo = '" + jaHead.getJSONObject(i).getString("ReceiptNo") +
                        "' ORDER BY SerialNo";
                jaItemList = _TSQL.SelectSQLJsonArray(SQL);

                //帶出商品
                jaSalesItemList = new JSONArray();
                for (int j = 0; j < jaItemList.length(); j++) {

                    joSalesItem = new JSONObject();
                    joSalesItem.put("Infant", jaItemList.getJSONObject(j).getString("Infant"));
                    joSalesItem.put("OriginalClass", jaItemList.getJSONObject(j).getString("OriginalClass"));
                    joSalesItem.put("NewClass", jaItemList.getJSONObject(j).getString("NewClass"));
                    joSalesItem.put("SalesPrice", jaItemList.getJSONObject(j).getDouble("SalesPrice"));
                    joSalesItem.put("SalesQty", jaItemList.getJSONObject(j).getInt("SalesQty"));
                    jaSalesItemList.put(joSalesItem);
                }

                //帶出付款歷程
                SQL = "SELECT Currency, PayBy, Amount, USDAmount, CardNo, CardName, CardType, CardDate " +
                        "FROM ClassPaymentInfo " +
                        "WHERE Secseq = '" + this.SecSeq +
                        "' AND ReceiptNo = '" + jaHead.getJSONObject(i).getString("ReceiptNo") +
                        "' AND Status = '" + jaHead.getJSONObject(i).getString("Status") + "'";
                jaPaymentList = _TSQL.SelectSQLJsonArray(SQL);

                if (jaPaymentList == null || jaPaymentList.length() == 0) {
                    ReturnCode = "8";
                    ReturnMessage = "Receipt no not found.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //將信用卡密碼解密
                for (int j = 0; j < jaPaymentList.length(); j++) {

                    if (jaPaymentList.getJSONObject(j).getString("PayBy").equals("Card")) {
                        jaPaymentList.getJSONObject(j).put("CardNo", AESEncrypDecryp.getDectyptData(jaPaymentList.getJSONObject(j).getString("CardNo"), FlightData.AESKey));
                        jaPaymentList.getJSONObject(j).put("CardDate", AESEncrypDecryp.getDectyptData(jaPaymentList.getJSONObject(j).getString("CardDate"), FlightData.AESKey));
                    }
                }

                ReceiptDataObject = new JSONObject();
                ReceiptDataObject.put("ReceiptNo", jaHead.getJSONObject(i).getString("ReceiptNo"));
                ReceiptDataObject.put("Status", jaHead.getJSONObject(i).getString("Status"));
                ReceiptDataObject.put("TotalPrice", jaHead.getJSONObject(i).getDouble("TotalPrice"));
                ReceiptDataObject.put("Items", jaSalesItemList);
                ReceiptDataObject.put("PaymentList", jaPaymentList);
                ResponseData.put(ReceiptDataObject);
            }

            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, ResponseData);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    //                             Inventory
    //////////////////////////////////////////////////////////////////////////

    /**
     * 確認是否已經經過商品銷售和移儲
     *
     * @return (JSONObject) 標準格式回傳 是否有經過商品銷售和移儲
     */
    public JSONObject CheckCurrentFlightCanUpdate() {

        String ClassName = "PublicFunctions";
        String FunctionName = "CheckCurrentFlightCanUpdate";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {

            //首先判斷是否有銷售物品,或Transfer時，不能修改。
            String SQL = "SELECT ReceiptNo " +
                    "FROM SalesHead " +
                    "WHERE SalesHead.SecSeq = '" + this.SecSeq +
                    "' and PreOrderNo='' UNION " +
                    "SELECT ReceiptNo " +
                    "FROM Transfer " +
                    "WHERE Transfer.SecSeq = '" + this.SecSeq + "' ";

            if (_TSQL.CheckDataIsExist(SQL)) {
                ReturnCode = "8";
                ReturnMessage = "Already selling, You can't Update";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "0";
                ReturnMessage = "";
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
     * 取得所有的抽屜號碼，和各抽屜裝載商品數量
     *
     * @param SecSeq   航段序號，不使用時傳Null
     * @param DrawerNo 抽屜編號，不使用時傳Null
     * @param QtyType  要判斷的數量欄位，True =  Start qty, False = End qty
     * @return (JSONObject) 標準格式回傳 抽屜資訊
     */
    public JSONObject GetAllDrawerInfo(String SecSeq, String DrawerNo, boolean QtyType) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetAllDrawerInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq + "-DrawerNo:" + DrawerNo);

        String Qty = "";
        if (QtyType) {
            Qty = "StartQty";
        } else {
            Qty = "EndQTY";
        }

        try {

            //取得所有的抽屜。
            String SQL = "";
            if (DrawerNo == null) {
                SQL += "SELECT 'All Drawer' AS DrawNo,SUM(" + Qty + ") AS Qty " +
                        "FROM Inventory " +
                        "WHERE 1=1 AND DrawNo <> '' ";

                if (SecSeq != null) {
                    SQL += "AND SecSeq = '" + SecSeq + "' ";
                }

                SQL += "UNION ";
            }

            SQL += "SELECT DrawNo,SUM(" + Qty + ") AS Qty " +
                    "FROM Inventory " +
                    "WHERE 1=1 AND DrawNo <> '' ";

            if (SecSeq != null) {
                SQL += "AND SecSeq = '" + SecSeq + "' ";
            }

            if (DrawerNo != null) {
                SQL += "AND DrawNo = '" + DrawerNo + "' ";
            }

            SQL += "GROUP BY DrawNo";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    public JSONObject getEGASDiscrepancyItemList() {
        String ClassName = "PublicFunctions";
        String FunctionName = "getEGASDiscrepancyItemList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq );

        String Qty = "";

        try {
//            String SQL = "SELECT  Inventory.DrawNo, Inventory. ItemCode,Inventory.StandQty,Inventory.EGASCheckQty,AllParts.ItemName,Inventory.EGASDamageQty " +
//                    "FROM Inventory inner join AllParts on Inventory.ItemCode = allParts.ItemCode " +
//                    "WHERE 1=1 AND ( EGASCheckQty <> StandQty or Inventory.EGASDamageQty >0 ) and SecSeq='9'";
            String SQL ="select Inventory.*,AllParts.ItemName  from (select SecSeq,Inventory.DrawNo, Inventory. ItemCode,Inventory.StandQty,Inventory.EGASCheckQty,Inventory.EGASDamageQty  from Inventory where secseq='9' and ItemCode not in (select prt2 from PrtData)union select SecSeq,Inventory.DrawNo, Inventory. ItemCode,prt4,Inventory.EGASCheckQty,Inventory.EGASDamageQty  from Inventory inner join PrtData on ItemCode = prt2 where secseq='9') Inventory   inner join AllParts on Inventory.ItemCode = allParts.ItemCode WHERE 1=1 AND ( EGASCheckQty <> StandQty or Inventory.EGASDamageQty >0 ) and SecSeq='9'";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }


    public JSONObject getEGASDiscrepancyPreorderList() {
        String ClassName = "PublicFunctions";
        String FunctionName = "getEGASDiscrepancyItemList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq );

        String Qty = "";

        try {
            String SQL = "SELECT  PreorderNO,PreorderType" +
                    " FROM PreorderHead" +
                    " WHERE EGASSaleFlag = 'S'";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }


    public JSONObject getEVADiscrepancyItemList() {
        String ClassName = "PublicFunctions";
        String FunctionName = "getEVADiscrepancyItemList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq );

        String Qty = "";

        try {
            String SQL = "SELECT  Inventory.DrawNo, Inventory. ItemCode,Inventory.EndQty,Inventory.EGASCheckQty,Inventory.EVACheckQty,AllParts.ItemName,Inventory.EVADamageQty " +
                    "FROM Inventory inner join AllParts on Inventory.ItemCode = allParts.ItemCode " +
                    "WHERE 1=1 AND ( EVACheckQty <> EndQty or Inventory.EVADamageQty >0 ) and SecSeq='9'";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }


    public JSONObject getEVADiscrepancyPreorderList() {
        String ClassName = "PublicFunctions";
        String FunctionName = "getEVADiscrepancyItemList";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq );

        String Qty = "";

        try {
            String SQL = "SELECT  PreorderNO,PreorderType" +
                    " FROM PreorderHead" +
                    " WHERE EVASaleFlag = 'S'";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }
    /**
     * 依據條件取得商品資訊
     *
     * @param SecSeq   航段編號，不使用時傳Null
     * @param Code     商品編號、雜誌編號或商品條碼，不使用時傳Null
     * @param DrawerNo 抽屜編號，不使用時傳Null
     * @param Sort     排序方式，0 - Draw ; 1 - ItemCode ; 2 - SerialCode
     * @return (JSONObject) 標準格式回傳 商品資訊
     */
    public JSONObject GetProductInfo(String SecSeq, String Code, String DrawerNo, int Sort) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetProductInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq + "-Code:" + Code + "-DrawerNo:" + DrawerNo + "-Sort:" + Sort);

        try {
            //取得商品資訊。
            String SQL = "SELECT AllParts.ItemCode,DrawNo,SerialCode,ItemName,Remark,ItemPriceUS,ItemPriceTW, " +
                    "StandQty,StartQty,AdjustQty,SalesQty,TransferQty,DamageQty,EndQty,EGASCheckQty,EGASDamageQty,EVACheckQty,EVADamageQty " +
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

            if (Sort == 0) {
                SQL += " ORDER BY DrawNo, AllParts.ItemCode";
            } else if (Sort == 1) {
                SQL += " ORDER BY AllParts.ItemCode, DrawNo";
            } else if (Sort == 2) {
                SQL += " ORDER BY DrawNo, SerialCode";
            } else if (Sort == 3) {
                SQL += " ORDER BY SerialCode";
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

    /**
     * 依據條件取得開櫃調整資訊
     *
     * @param SecSeq   航段編號，不使用時傳Null
     * @param Code     商品編號、雜誌編號或商品條碼，不使用時傳Null
     * @param DrawerNo 抽屜編號，不使用時傳Null
     * @param Sort     排序方式，0 - Draw ; 1 - ItemCode ; 2 - SerialCode
     * @return (JSONObject) 標準格式回傳 商品資訊
     */
    public JSONObject GetAdjustInfo(String SecSeq, String Code, String DrawerNo, int Sort) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetAdjustInfo";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "SecSeq:" + SecSeq + "-Code:" + Code + "-DrawerNo:" + DrawerNo + "-Sort:" + Sort);

        try {
            //取得商品資訊。
            String SQL = "SELECT AllParts.ItemCode,DrawNo,SerialCode,ItemName,Remark,ItemPriceUS,ItemPriceTW, " +
                    "StandQty,StartQty,AdjustQty,SalesQty,TransferQty,DamageQty,EndQty,EGASCheckQty,EGASDamageQty,EVACheckQty,EVADamageQty " +
                    "FROM Inventory LEFT JOIN AllParts " +
                    "ON Inventory.ItemCode = AllParts.ItemCode " +
                    "WHERE AdjustQty <> 0 AND DrawNo <> '' ";

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

            if (Sort == 0) {
                SQL += " ORDER BY DrawNo, AllParts.ItemCode";
            } else if (Sort == 1) {
                SQL += " ORDER BY AllParts.ItemCode, DrawNo";
            } else if (Sort == 2) {
                SQL += " ORDER BY DrawNo, SerialCode";
            } else if (Sort == 3) {
                SQL += " ORDER BY SerialCode";
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

    /**
     * 進行開櫃調整
     *
     * @param AdjustArray 要調整的商品Array
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject AdjustItemQty(JSONArray AdjustArray) {

        String ClassName = "PublicFunctions";
        String FunctionName = "AdjustItemQty";
        String ReturnCode, ReturnMessage, SQL;
        ArrayList SQLCommands;
        JSONArray ja;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        int OriEndQty = 0;
        int OriStartQty = 0;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "AdjustArray:" + AdjustArray);

        try {
            if (!FlightData.OpenFlightFlag) {
                ReturnCode = "8";
                ReturnMessage = "Please open flight.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            SQLCommands = new ArrayList();
            for (int i = 0; i < AdjustArray.length(); i++) {
                //先取得原始數量
                SQL = "SELECT StartQty, EndQty " +
                        "FROM Inventory " +
                        " WHERE ItemCode = '" + AdjustArray.getJSONObject(i).getString("ItemCode") +
                        "' AND SecSeq = '" + this.SecSeq + "'";
                ja = _TSQL.SelectSQLJsonArray(SQL);

                //若商品不存在則跳錯
                if (ja == null || ja.length() == 0 || ja.getJSONObject(0).getString("EndQty").length() == 0) {
                    ReturnCode = "8";
                    ReturnMessage = "Pno code error: " + AdjustArray.getJSONObject(i).getString("ItemCode");

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                OriEndQty = ja.getJSONObject(0).getInt("EndQty");
                OriStartQty = ja.getJSONObject(0).getInt("StartQty");

                //檢查不可以將商品調整到負數
                if (AdjustArray.getJSONObject(i).getInt("AdjustQty") < 0) {
                    ReturnCode = "8";
                    ReturnMessage = "Qty not enough.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }

                //逐筆換成SQL Command
                SQL = "UPDATE Inventory " +
                        "SET AdjustQty  = " + (AdjustArray.getJSONObject(i).getInt("AdjustQty") - OriStartQty) +
                        ", EndQty = " + AdjustArray.getJSONObject(i).getInt("AdjustQty") +
                        " WHERE ItemCode = '" + AdjustArray.getJSONObject(i).getString("ItemCode") +
                        "' AND SecSeq = '" + this.SecSeq + "'";
                SQLCommands.add(SQL);

                SQL = "INSERT INTO Adjust (SecSeq,ItemCode,OldQty,NewQty,CrewID,CrewType,WorkingTime) " +
                        "VALUES ('" + this.SecSeq +
                        "','" + AdjustArray.getJSONObject(i).getString("ItemCode") +
                        "'," + OriEndQty +
                        "," + AdjustArray.getJSONObject(i).getInt("AdjustQty") +
                        ",'" + FlightData.CrewID +
                        "','" + "AIR" +
                        "','" + formatter.format(new Date(System.currentTimeMillis())) + "')";
                SQLCommands.add(SQL);
            }

            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Adjust failed.";

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
     * 進行Damage調整
     *
     * @param DamageArray 要轉瑕疵品的商品Array，無調整可傳Null
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject DamageItemQty(JSONArray DamageArray) {

        String ClassName = "PublicFunctions";
        String FunctionName = "DamageItemQty";
        String ReturnCode, ReturnMessage, SQL;
        ArrayList SQLCommands;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONArray ja;

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "DamageArray:" + DamageArray);

        try {

            //有需要調整的商品，DamageQty數量都會是正數， 因為不扣庫存量
            if (DamageArray != null) {
                SQLCommands = new ArrayList();
                for (int i = 0; i < DamageArray.length(); i++) {
                    //先取得原始數量，
                    SQL = "SELECT ItemCode, EndQty, DamageQty " +
                            "FROM Inventory " +
                            " WHERE ItemCode = '" + DamageArray.getJSONObject(i).getString("ItemCode") +
                            "' AND SecSeq = '" + this.SecSeq + "'";
                    ja = _TSQL.SelectSQLJsonArray(SQL);

                    //若商品不存在則跳錯
                    if (ja == null || ja.length() == 0 || ja.getJSONObject(0).getString("ItemCode").length() == 0) {
                        ReturnCode = "8";
                        ReturnMessage = "Pno code error: " + DamageArray.getJSONObject(i).getString("ItemCode");

                        //寫入Log
                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                        return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                    }

                    //若Damage商品數量大於庫存量，或更改後數量小於0則不允許
                    if ((ja.getJSONObject(0).getInt("DamageQty") + DamageArray.getJSONObject(i).getInt("UpdateDamageQty") > ja.getJSONObject(0).getInt("EndQty")) ||
                            (ja.getJSONObject(0).getInt("DamageQty") + DamageArray.getJSONObject(i).getInt("UpdateDamageQty") < 0)) {
                        ReturnCode = "8";
                        ReturnMessage = "Damage qty error.";

                        //寫入Log
                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                        return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                    }

                    //每個數量都要獨立Rec
                    for (int j = 0; j < Math.abs(DamageArray.getJSONObject(i).getInt("UpdateDamageQty")); j++) {

                        SQLCommands.clear();

                        if (DamageArray.getJSONObject(i).getInt("UpdateDamageQty") > 0) {

                            //逐筆換成SQL Command
                            SQL = "UPDATE Inventory " +
                                    "SET DamageQty  = DamageQty + 1 " +
                                    " WHERE ItemCode = '" + DamageArray.getJSONObject(i).getString("ItemCode") +
                                    "' AND SecSeq = '" + this.SecSeq + "'";
                            SQLCommands.add(SQL);

                            //增加Damage
                            SQL = "INSERT INTO Damage (SecSeq,ItemCode,Qty,Status,IFEDamage,WorkingTime) " +
                                    "VALUES ('" + this.SecSeq +
                                    "','" + DamageArray.getJSONObject(i).getString("ItemCode") +
                                    "'," + "1" +
                                    ",'" + "S" +
                                    "','" + "" +
                                    "','" + formatter.format(new Date(System.currentTimeMillis())) + "')";
                            SQLCommands.add(SQL);
                        } else {

                            //逐筆換成SQL Command
                            SQL = "UPDATE Inventory " +
                                    "SET DamageQty  = DamageQty - 1 " +
                                    " WHERE ItemCode = '" + DamageArray.getJSONObject(i).getString("ItemCode") +
                                    "' AND SecSeq = '" + this.SecSeq + "'";
                            SQLCommands.add(SQL);

                            //先取出ID，還有IFE
                            SQL = "SELECT ReceiptNo,IFEDamage " +
                                    "FROM Damage " +
                                    "WHERE Status = 'S' " +
                                    "AND ItemCode = '" + DamageArray.getJSONObject(i).getString("ItemCode") +
                                    "' AND SecSeq = '" + this.SecSeq + "'";
                            ja = _TSQL.SelectSQLJsonArray(SQL);

                            //減少Damage
                            SQL = "UPDATE Damage " +
                                    "SET Status = 'R',WorkingTime = '" + formatter.format(new Date(System.currentTimeMillis())) +
                                    "' WHERE ReceiptNo = '" + ja.getJSONObject(0).getInt("ReceiptNo") +
                                    "' AND SecSeq = '" + this.SecSeq + "'";
                            SQLCommands.add(SQL);
                        }

                        //考量到與IFE溝通，所以每個Damage都要獨立執行，這樣取的ReceiptNo才是正確的
                        if (!_TSQL.ExecutesSQLCommand(SQLCommands)) {
                            ReturnCode = "8";
                            ReturnMessage = "Damage failed.";

                            //寫入Log
                            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                        }
                    }
                }
            }

            //回傳新的Damage List
            SQL = "SELECT AllParts.ItemCode,DrawNo,SerialCode,ItemName,ItemPriceUS,COUNT(Damage.ItemCode) AS DamageQty, EndQty " +
                    "FROM Damage LEFT JOIN Inventory " +
                    "ON Damage.ItemCode = Inventory.ItemCode " +
                    "LEFT JOIN AllParts " +
                    "ON Damage.ItemCode = AllParts.ItemCode " +
                    "WHERE DrawNo <> '' AND Inventory.SecSeq = '" + this.SecSeq +
                    "' AND Damage.SecSeq = '" + this.SecSeq +
                    "' AND Damage.Status = 'S' " +
                    "GROUP BY Damage.ItemCode " +
                    "ORDER BY ReceiptNo";
            ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && !ja.getJSONObject(0).isNull("ItemCode")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "0";
                ReturnMessage = "";
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
     * 查詢Transfer單據內容
     *
     * @param TransferNo   要查詢的Transfer No，全部查詢請使用Null
     * @param TransferType 要查詢的Transfer Type，參數請使用'IN' or 'OUT'，全部查詢請使用Null
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject QueryTransferItemQty(String TransferNo, String TransferType) {

        String ClassName = "PublicFunctions";
        String FunctionName = "QueryTransferItemQty";
        String ReturnCode, ReturnMessage, SQL;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "TransferNo:" + TransferNo + "-TransferType:" + TransferType);

        try {

            //依據Transfer取得單據內容
            SQL = "SELECT AllParts.ItemCode,DrawNo,SerialCode,ItemName,ReceiptNo,TransferNo,CarFrom,CarTo,Qty,TransferType " +
                    "FROM Transfer LEFT JOIN AllParts " +
                    "ON Transfer.ItemCode = AllParts.ItemCode " +
                    "LEFT JOIN Inventory " +
                    "ON Transfer.ItemCode = Inventory.ItemCode " +
                    "WHERE DrawNo <> '' AND Transfer.SecSeq = '" + this.SecSeq +
                    "' AND Inventory.SecSeq = '" + this.SecSeq + "' ";

            if (TransferNo != null) {
                SQL += " AND TransferNo = '" + TransferNo + "'";
            }

            if (TransferType != null) {
                SQL += " AND TransferType = '" + TransferType.toUpperCase() + "'";
            }

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);
            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /**
     * 進行Transfer調整
     *
     * @param TransferArray 要Transfer的商品Array
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject TransferItemQty(JSONObject TransferArray) throws Exception {

        String ClassName = "PublicFunctions";
        String FunctionName = "TransferItemQty";
        String ReturnCode, ReturnMessage, SQL;
        ArrayList SQLCommands;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        int OriQty;
        String ReceiptNo = GetNewTransferInfo();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "TransferArray:" + TransferArray);

        try {

            SQLCommands = new ArrayList();

            //檢核Transfer No是否重複
            SQL = "SELECT TransferNo " +
                    "FROM Transfer " +
                    "WHERE TransferNo = '" + TransferArray.getString("TransferNo") +
                    "' LIMIT 1";
            if (_TSQL.CheckDataIsExist(SQL)) {
                ReturnCode = "8";
                ReturnMessage = "Transfer repeated!";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //Transfer IN
            if (TransferArray.getString("TransferType").toUpperCase().equals("IN")) {
                for (int i = 0; i < TransferArray.getJSONArray("TransferItem").length(); i++) {

                    //逐筆換成SQL Command
                    SQL = "UPDATE Inventory " +
                            "SET TransferQty = TransferQty + " + TransferArray.getJSONArray("TransferItem").getJSONObject(i).getInt("TransferQty") +
                            " , EndQty = EndQty + " + TransferArray.getJSONArray("TransferItem").getJSONObject(i).getInt("TransferQty") +
                            " WHERE ItemCode = '" + TransferArray.getJSONArray("TransferItem").getJSONObject(i).getString("ItemCode") +
                            "' AND SecSeq = '" + this.SecSeq + "'";
                    SQLCommands.add(SQL);

                    SQL = "INSERT INTO Transfer (SecSeq,ReceiptNo,TransferNo,ItemCode,CarFrom,CarTo,Qty,TransferType,WorkingTime) " +
                            "VALUES ('" + this.SecSeq +
                            "','" + ReceiptNo +
                            "','" + TransferArray.getString("TransferNo") +
                            "','" + TransferArray.getJSONArray("TransferItem").getJSONObject(i).getString("ItemCode") +
                            "','" + TransferArray.getString("CarFrom") +
                            "','" + TransferArray.getString("CarTo") +
                            "'," + TransferArray.getJSONArray("TransferItem").getJSONObject(i).getInt("TransferQty") +
                            ",'" + "IN" +
                            "','" + formatter.format(new Date(System.currentTimeMillis())) + "')";
                    SQLCommands.add(SQL);
                }
            }
            //Transfer OUT
            else {
                for (int i = 0; i < TransferArray.getJSONArray("TransferItem").length(); i++) {

                    //先取得原始數量
                    SQL = "SELECT (EndQty - DamageQty) AS EndQty " +
                            "FROM Inventory " +
                            " WHERE ItemCode = '" + TransferArray.getJSONArray("TransferItem").getJSONObject(i).getString("ItemCode") +
                            "' AND SecSeq = '" + this.SecSeq + "'";
                    OriQty = (int) _TSQL.SelectSQLObject(SQL);

                    //若Transfer Out商品數量大於庫存量則不允許
                    if (Math.abs(TransferArray.getJSONArray("TransferItem").getJSONObject(i).getInt("TransferQty")) > OriQty) {
                        ReturnCode = "8";
                        ReturnMessage = "Transfer qty error.";

                        //寫入Log
                        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                        return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                    }

                    //逐筆換成SQL Command
                    SQL = "UPDATE Inventory " +
                            "SET TransferQty = TransferQty - " + TransferArray.getJSONArray("TransferItem").getJSONObject(i).getInt("TransferQty") +
                            " , EndQty = EndQty - " + TransferArray.getJSONArray("TransferItem").getJSONObject(i).getInt("TransferQty") +
                            " WHERE ItemCode = '" + TransferArray.getJSONArray("TransferItem").getJSONObject(i).getString("ItemCode") +
                            "' AND SecSeq = '" + this.SecSeq + "'";
                    SQLCommands.add(SQL);

                    SQL = "INSERT INTO Transfer (SecSeq,ReceiptNo,TransferNo,ItemCode,CarFrom,CarTo,Qty,TransferType,WorkingTime) " +
                            "VALUES ('" + this.SecSeq +
                            "','" + ReceiptNo +
                            "','" + TransferArray.getString("TransferNo") +
                            "','" + TransferArray.getJSONArray("TransferItem").getJSONObject(i).getString("ItemCode") +
                            "','" + TransferArray.getString("CarFrom") +
                            "','" + TransferArray.getString("CarTo") +
                            "'," + TransferArray.getJSONArray("TransferItem").getJSONObject(i).getInt("TransferQty") +
                            ",'" + "OUT" +
                            "','" + formatter.format(new Date(System.currentTimeMillis())) + "')";
                    SQLCommands.add(SQL);
                }
            }

            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Transfer failed.";

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
     * 取消特定單據號碼之Transfer交易內容
     *
     * @param TransferNo 要取消的Transfer No
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject CancelTransfer(String TransferNo) {

        String ClassName = "PublicFunctions";
        String FunctionName = "CancelTransfer";
        String ReturnCode, ReturnMessage, SQL;
        ArrayList SQLCommands;
        JSONArray ja;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "TransferNo:" + TransferNo);

        try {

            SQLCommands = new ArrayList();

            //取得Transfer資料
            SQL = "SELECT * " +
                    "FROM Transfer " +
                    "WHERE TransferNo = '" + TransferNo +
                    "' AND SecSeq = '" + this.SecSeq + "'";
            ja = _TSQL.SelectSQLJsonArray(SQL);

            //檢核transfer no是否存在
            if (ja == null || ja.length() == 0) {
                ReturnCode = "8";
                ReturnMessage = "Transfer no error.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //檢核Receipt No是否已經Cancel
            if (ja.getJSONObject(0).getString("TransferType").equals("CANC")) {
                ReturnCode = "8";
                ReturnMessage = "Cancel transfer repeated!";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //檢核Receipt No是否為Out
            if (ja.getJSONObject(0).getString("TransferType").equals("IN")) {
                ReturnCode = "8";
                ReturnMessage = "This transfer can't cancel.";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //執行Cancel transfer out
            SQLCommands = new ArrayList();
            SQL = "UPDATE Transfer " +
                    "SET TransferType = 'CANC', WorkingTime = '" + formatter.format(new Date(System.currentTimeMillis())) +
                    "' WHERE TransferNo = '" + TransferNo + "'";
            SQLCommands.add(SQL);

            for (int i = 0; i < ja.length(); i++) {
                //逐筆換成SQL Command
                SQL = "UPDATE Inventory " +
                        "SET TransferQty = TransferQty + " + ja.getJSONObject(i).getInt("Qty") +
                        " , EndQty = EndQty + " + ja.getJSONObject(i).getInt("Qty") +
                        " WHERE ItemCode = '" + ja.getJSONObject(i).getString("ItemCode") +
                        "' AND SecSeq = '" + this.SecSeq + "'";
                SQLCommands.add(SQL);
            }

            if (_TSQL.ExecutesSQLCommand(SQLCommands)) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            } else {
                ReturnCode = "8";
                ReturnMessage = "Cancel transfer failed.";

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

    //////////////////////////////////////////////////////////////////////////
    //                       Discount Information
    //////////////////////////////////////////////////////////////////////////

    /**
     * 取得所有的被動式折扣類別
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject GetAllDiscountType() {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetAllDiscountType";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        try {
            //取得被動式折扣資訊。
            String SQL = "SELECT Type,Description " +
                    "FROM Discount " +
                    "WHERE FuncID = ''";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("Type")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                //有可能沒有折扣
                ReturnCode = "0";
                ReturnMessage = "";
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
     * 確認是否為員工(工號檢核)、PT(資料庫比對)、組員(工號檢核)
     *
     * @param EmployeeID 要比對的員工工號
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject CheckEmployee(String EmployeeID) {

        String ClassName = "PublicFunctions";
        String FunctionName = "CheckEmployee";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "EmployeeID:" + EmployeeID);

        try {
            String SQL = "SELECT DiscountType " +
                    "FROM Staff " +
                    "WHERE EmployeeID = '" + EmployeeID + "'";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("DiscountType")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {

                //不是PT，進行工號檢核
                if (CheckEmployeeID(EmployeeID) && ja != null) {
                    ReturnCode = "0";
                    ReturnMessage = "";
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
                } else {
                    ReturnCode = "8";
                    ReturnMessage = "Staff info is not exist.";

                    //寫入Log
                    _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                    return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
                }
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
     * 確認是否屬於聯名卡
     *
     * @param CardNo 要比對的信用卡卡號，比對前六碼
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject CheckCoBrandedCard(String CardNo) {

        String ClassName = "PublicFunctions";
        String FunctionName = "CheckCoBrandedCard";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "CardNo:" + CardNo);

        try {
            String SQL = "SELECT DiscountType " +
                    "FROM BankDis " +
                    "WHERE BinCode = '" + CardNo.substring(0, 6) +
                    "' OR BinCode = '" + CardNo.substring(0, 8) + "'";

            JSONArray ja = _TSQL.SelectSQLJsonArray(SQL);

            if (ja != null && ja.length() > 0 && !ja.getJSONObject(0).isNull("DiscountType")) {
                ReturnCode = "0";
                ReturnMessage = "";
                return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);
            } else {
                ReturnCode = "8";
                ReturnMessage = "CoBranded card info is not exist.";

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
     * 確認是否屬於會員卡
     *
     * @param CardType 要比對的會員卡類別
     * @param CardNo   要比對的會員卡卡號
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject CheckMemberCard(String CardType, String CardNo) {

        String ClassName = "PublicFunctions";
        String FunctionName = "CheckMemberCard";
        String ReturnCode, ReturnMessage;
        JSONArray ja = new JSONArray();
        JSONObject jo = new JSONObject();
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start:" + "CardType:" + CardType + "-CardNo:" + CardNo);

        try {

            if (!CardType.equals("CD") &&
                    !CardType.equals("CG") &&
                    !CardType.equals("EC") &&
                    !CardType.equals("EP")) {
                ReturnCode = "8";
                ReturnMessage = "Please check ID type!";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //需全為數字
            if (!isNumeric(CardNo)) {
                ReturnCode = "8";
                ReturnMessage = "Please check " + CardType + " ID!";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //長度須為10碼
            if (CardNo.length() != 10) {
                ReturnCode = "8";
                ReturnMessage = "Please check " + CardType + " ID!";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            //驗證前9位取餘數=第10位
            if (((Long.valueOf(CardNo) / 10) % 7) != (Long.valueOf(CardNo) % 10)) {
                ReturnCode = "8";
                ReturnMessage = "Please check " + CardType + " ID!";

                //寫入Log
                _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ReturnMessage);
                return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
            }

            jo.put("DiscountType", CardType);
            ja.put(jo);

            ReturnCode = "0";
            ReturnMessage = "";
            return GetReturnJsonObject(ReturnCode, ReturnMessage, ja);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /////////////////////////////////////
    //
    //            私有函式
    //
    /////////////////////////////////////

    /**
     * 判別是否為台灣發行的信用卡
     *
     * @param CardNo 信用卡號碼
     * @return (JSONObject) 標準格式回傳
     */
    public Boolean CheckIsTaiwanCard(String CardNo) {

        String ClassName = "PublicFunctions";
        String FunctionName = "CheckIsTaiwanCard";
        String ReturnCode, ReturnMessage;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();

        try {
            //判別是否為台灣發行的信用卡。
            String SQL = "SELECT * " +
                    "FROM TaiwanCreditCardInfo " +
                    "WHERE BinCode = '" + CardNo.substring(0, 6) +
                    "' OR BinCode = '" + CardNo.substring(0, 7) +
                    "' OR BinCode = '" + CardNo.substring(0, 8) +
                    "' OR BinCode = '" + CardNo.substring(0, 9) +
                    "' OR BinCode = '" + CardNo.substring(0, 10) +
                    "' OR BinCode = '" + CardNo.substring(0, 11) + "'";

            return _TSQL.CheckDataIsExist(SQL);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得該幣別最小進位
     *
     * @param Currency 要取得的幣別
     * @return 該幣別最小進位
     */
    public double GetMiniValue(String Currency) {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetMiniValue";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        try {

            String SQL = "SELECT MiniValue " +
                    "FROM Rate " +
                    "WHERE CurDvr = '" + Currency + "'";

            return Double.valueOf(String.valueOf(_TSQL.SelectSQLObject(SQL)));
        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 位數轉換、進位
     *
     * @param OriAmount  被轉換的原始金額
     * @param PointCount 要取得的位數 ex:100 = 百位數 0.1 = 小數點第一位
     * @param Type       1. 無條件進位    2. 四捨五入    3. 無條件捨去
     * @return 向下取整的值
     */
    public double DecimalPointChange(double OriAmount, double PointCount, int Type) {

        String ClassName = "PublicFunctions";
        String FunctionName = "DecimalPointChange";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        try {

            double NewAmount = Arith.div(OriAmount, PointCount);
            switch (Type) {
                case 1:   //無條件進位
                    NewAmount = Math.ceil(NewAmount);
                    break;

                case 2:   //四捨五入
                    if(NewAmount<0){
                        NewAmount=0-NewAmount;
                        NewAmount = Math.round(NewAmount);
                        NewAmount=0-NewAmount;
                    }else {
                        NewAmount = Math.round(NewAmount);
                    }

                    break;

                case 3:   //無條件捨去
                    NewAmount = Math.floor(NewAmount);
                    break;
            }

            NewAmount = Arith.mul(NewAmount, PointCount);

            return NewAmount;
        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 判斷是否為數字
     *
     * @param str 要判斷的數字
     * @return true = 數字、false = 非數字
     */
    private boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]+");
        return pattern.matcher(str).matches();
    }

    /**
     * 檢核工號是否正確
     *
     * @param EmployeeID 要判斷的工號
     * @return true = 正確、false = 不正確
     */
    private boolean CheckEmployeeID(String EmployeeID) {

        String ClassName = "PublicFunctions";
        String FunctionName = "CheckEmployeeID";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        try {

            //員工編號一定是6碼，如果不是6碼，則為錯誤。
            return EmployeeID.length() == 6;

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得新的Transfer單據號碼
     *
     * @return Receipt No
     */
    private String GetNewTransferInfo() throws Exception {

        String ClassName = "PublicFunctions";
        String FunctionName = "DecimalPointChange";
        String SQL;
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

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
            _TSQL.WriteLog(SecSeq, "System", ClassName, FunctionName, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 確認商品是否為贈品
     *
     * @return (String) True = Gift, False = Not gift
     */
    private Boolean CheckItemIsGift(String ItemCode) {

        String ClassName = "PublicFunctions";
        String FunctionName = "CheckItemIsGift";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        try {
            String SQL = "SELECT ItemCode " +
                    "FROM AllParts " +
                    "WHERE ItemPriceUS = 0 AND ItemPriceTW = 0";
            Object obj = _TSQL.SelectSQLObject(SQL);

            return obj != null && String.valueOf(obj).length() > 0;

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(SecSeq, "System", ClassName, FunctionName, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得此次要開櫃的航段序號
     *
     * @return (String) 航段序號
     */
    private String GetNowSecSeq() {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetNowSecSeq";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        try {
            String SQL = "SELECT MAX(CAST(SecSeq AS INTEGER)) AS SecSeq " +
                    "FROM FLIGHT " +
                    "WHERE IsOpen = 1 " +
                    "AND IsClose = 1";
            Object obj = _TSQL.SelectSQLObject(SQL);

            if (obj != null && String.valueOf(obj).length() > 0) {
                return String.valueOf((int) obj + 1);
            } else {
                return "0";  //還沒開櫃過,回傳0
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(SecSeq, "System", ClassName, FunctionName, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 取得可以Reopen的航段序號
     *
     * @return (String) 航段序號
     */
    private String GetReopenSecSeq() {

        String ClassName = "PublicFunctions";
        String FunctionName = "GetReopenSecSeq";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        try {
            String SQL = "SELECT MAX(CAST(SecSeq AS INTEGER)) AS SecSeq " +
                    "FROM FLIGHT " +
                    "WHERE IsOpen = 1 " +
                    "AND IsClose = 1";
            Object obj = _TSQL.SelectSQLObject(SQL);

            if (obj != null && String.valueOf(obj).length() > 0) {
                return String.valueOf(obj);
            } else {
                return "";
            }

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(SecSeq, "System", ClassName, FunctionName, ex.getMessage());
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
    public JSONObject GetReturnJsonObject(String ReturnCode, String ReturnMessage, JSONArray ResponseData) {
        String ClassName = "PublicFunctions";
        String FunctionName = "GetReturnJsonObject";
        TSQL _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("ReturnCode", ReturnCode);
            jsonObject.put("ReturnMessage", ReturnMessage);

            if (ResponseData != null && ResponseData.length() > 0) {
                jsonObject.put("ResponseData", ResponseData);
            }

            return jsonObject;

        } catch (Exception ex) {
            _TSQL.WriteLog(SecSeq, "System", ClassName, FunctionName, ex.getMessage());
            return null;
        }
    }
}
