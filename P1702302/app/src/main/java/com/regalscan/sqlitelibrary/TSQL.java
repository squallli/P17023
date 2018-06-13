package com.regalscan.sqlitelibrary;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import net.sqlcipher.database.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/*
 * Created by gabehsu on 2017/3/6.
 */

public class TSQL extends SQLiteOpenHelper {

    private static TSQL INSTANCE;

    //DB版本號
    private final static int DBVersion = 1;

    //資料庫路徑
    private File DBPath;

    private static String DBName;
    private static String SecSeq;
    private SQLiteDatabase CurrentDB;
    private Context mContext;

    private static final String SDcardPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator;
    private static final String DB_PSW = "12345";

    //region 產生資料庫指令
    private final static String CREATE_ALLPARTS = "CREATE TABLE IF NOT EXISTS [AllParts] (" +
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
            "  CONSTRAINT [] PRIMARY KEY ([ItemCode]));";

    private final static String CREATE_INVENTORY = "CREATE TABLE IF NOT EXISTS [Inventory] (" +
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
            "  CONSTRAINT [] PRIMARY KEY ([SecSeq], [ItemCode]));";

    private final static String CREATE_RATE = "CREATE TABLE IF NOT EXISTS [Rate] (" +
            "  [ExchDate] VARCHAR(8), " +
            "  [CurDdd] VARCHAR(5), " +
            "  [CurDvr] VARCHAR(5), " +
            "  [ExchRate] Float, " +
            "  [MiniValue] Float, " +
            "  [CashCurrency] VARCHAR(1), " +
            "  [CardCurrency] VARCHAR(1), " +
            "  [TaiwanCardCurrency] VARCHAR(1)," +
            "  CONSTRAINT [] PRIMARY KEY ([ExchDate], [CurDdd], [CurDvr]));";

    private final static String CREATE_ADJUST = "CREATE TABLE IF NOT EXISTS [Adjust] (" +
            "  [SecSeq] VARCHAR(2), " +
            "  [ReceiptNo] Integer PRIMARY KEY Autoincrement," +
            "  [ItemCode] VARCHAR(10), " +
            "  [OldQty] Int, " +
            "  [NewQty] Int, " +
            "  [CrewID] VARCHAR(10), " +
            "  [CrewType] VARCHAR(5), " +
            "  [WorkingTime] Datetime);";

    private final static String CREATE_FLIGHT = "CREATE TABLE IF NOT EXISTS [Flight] (" +
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
            "  CONSTRAINT [] PRIMARY KEY ([FlightNo], [SecSeq]));";

    private final static String CREATE_CREWINFO = "CREATE TABLE IF NOT EXISTS [CrewInfo] (" +
            "  [CrewID] VARCHAR(10), " +
            "  [Password1] VARCHAR(20), " +
            "  [Password2] VARCHAR(20), " +
            "  [CrewType] VARCHAR(5), " +
            "  [Name] VARCHAR(50), " +
            "  CONSTRAINT [] PRIMARY KEY ([CrewID], [CrewType]));";

    private final static String CREATE_DAMAGE = "CREATE TABLE IF NOT EXISTS [Damage] (" +
            "  [SecSeq] VARCHAR(2), " +
            "  [ReceiptNo] Integer PRIMARY KEY Autoincrement, " +
            "  [ItemCode] VARCHAR(10), " +
            "  [Qty] Int, " +
            "  [Status] VARCHAR(1), " +
            "  [IFEDamage] VARCHAR(1), " +
            "  [WorkingTime] DateTime);";

    private final static String CREATE_TRANSFER = "CREATE TABLE IF NOT EXISTS [Transfer] (" +
            "  [SecSeq] VARCHAR(2), " +
            "  [ReceiptNo] VARCHAR(10), " +
            "  [TransferNo] VARCHAR(9), " +
            "  [SerialNo] Integer PRIMARY KEY Autoincrement, " +
            "  [ItemCode] VARCHAR(10), " +
            "  [CarFrom] VARCHAR(7), " +
            "  [CarTo] VARCHAR(7), " +
            "  [Qty] Int, " +
            "  [TransferType] VARCHAR(5), " +
            "  [WorkingTime] DateTime);";

    private final static String CREATE_SALESHEAD = "CREATE TABLE IF NOT EXISTS [SalesHead] (" +
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
            "  CONSTRAINT [] PRIMARY KEY ([SecSeq], [ReceiptNo]));";

    private final static String CREATE_SALESDETAIL = "CREATE TABLE IF NOT EXISTS [SalesDetail] (" +
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
            "  [Status] VARCHAR (1));";

    private final static String CREATE_PAYMENTINFO = "CREATE TABLE IF NOT EXISTS[PaymentInfo] (" +
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
            "  CONSTRAINT [] PRIMARY KEY ([SecSeq], [ReceiptNo], [PayBy], [Currency], [CouponNo], [Status]));";

    private final static String CREATE_CREDITCARD = "CREATE TABLE IF NOT EXISTS [CreditCard] (" +
            "  [CardType] VARCHAR (8), " +
            "  [Currency] VARCHAR (3), " +
            "  [MID] VARCHAR (20), " +
            "  [BankText] VARCHAR (100), " +
            "  [MaxAmount] Float, " +
            "  [MiniAmount] Float, " +
            "  CONSTRAINT [] PRIMARY KEY ([CardType], [Currency]));";

    private final static String CREATE_SYSTOMLOG = "CREATE TABLE IF NOT EXISTS [SystemLog] (" +
            "  [SearilNo] Integer PRIMARY KEY Autoincrement, " +
            "  [SystemDate] DateTime, " +
            "  [SecSeq] VARCHAR (2), " +
            "  [LogType] VARCHAR (30), " +
            "  [OperationName] VARCHAR (50), " +
            "  [FunctionName] VARCHAR (50), " +
            "  [LogText] NTEXT);";

    private final static String CREATE_BANKDIS = "CREATE TABLE IF NOT EXISTS [BankDis] (" +
            "  [BinCode] VARCHAR (10), " +
            "  [DiscountType] VARCHAR (10), " +
            "  CONSTRAINT [] PRIMARY KEY ([BinCode]));";

    private final static String CREATE_STAFF = "CREATE TABLE IF NOT EXISTS [Staff] (" +
            "  [EmployeeID] VARCHAR (10), " +
            "  [DiscountType] VARCHAR (10), " +
            "  CONSTRAINT [] PRIMARY KEY ([EmployeeID]));";

    private final static String CREATE_PREORDERSALESHEAD = "CREATE TABLE IF NOT EXISTS [PreorderSalesHead] (" +
            "  [SecSeq] VARCHAR (2), " +
            "  [ReceiptNo] VARCHAR (10), " +
            "  [SalesTime] DateTime, " +
            "  [RefundTime] DateTime, " +
            "  [PreorderNo] VARCHAR (13), " +
            "  [SaleFlag] VARCHAR  (1), " +
            "  [VerifyType] VARCHAR  (1), " +
            "  CONSTRAINT [] PRIMARY KEY ([SecSeq],[ReceiptNo]));";

    private final static String CREATE_PREORDERHEAD = "CREATE TABLE IF NOT EXISTS [PreorderHead] (" +
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
            "  CONSTRAINT [] PRIMARY KEY ([PreorderNO]));";

    private final static String CREATE_PREORDERDETAIL = "CREATE TABLE IF NOT EXISTS [PreorderDetail] (" +
            "  [PreorderNo] VARCHAR (13), " +
            "  [DrawNo] VARCHAR (3), " +
            "  [ItemCode] VARCHAR (10), " +
            "  [SerialCode] VARCHAR (3), " +
            "  [ItemName] VARCHAR (100), " +
            "  [OriginalPrice] Float, " +
            "  [SalesPrice] Float, " +
            "  [SalesPriceTW] Float, " +
            "  [SalesQty] Int, " +
            "  CONSTRAINT [] PRIMARY KEY ([PreorderNO], [SerialCode]));";

    private final static String CREATE_AUTHUTICATION = "CREATE TABLE IF NOT EXISTS [Authutication] (" +
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
            "  CONSTRAINT [] PRIMARY KEY ([SecSeq], [ReceiptNo], [UG_MARK]));";

    private final static String CREATE_CARTLIST = "CREATE TABLE IF NOT EXISTS [CartList] (" +
            "  [CartNo] VARCHAR (7), " +
            "  CONSTRAINT [] PRIMARY KEY ([CartNo]));";

    private final static String CREATE_CLASSALLPARTS = "CREATE TABLE IF NOT EXISTS [ClassAllParts] (" +
            "  [Infant] VARCHAR(10), " +
            "  [OriginalClass] VARCHAR(10), " +
            "  [NewClass] VARCHAR(10), " +
            "  [USDPrice] Float, " +
            "  [TWDPrice] Float, " +
            "  CONSTRAINT [] PRIMARY KEY ([Infant], [OriginalClass], [NewClass]));";

    private final static String CREATE_CLASSSALESHEAD = "CREATE TABLE IF NOT EXISTS [ClassSalesHead] (" +
            "  [SecSeq] VARCHAR(2), " +
            "  [ReceiptNo] VARCHAR(10), " +
            "  [SalesTime] DateTime, " +
            "  [RefundTime] DateTime, " +
            "  [TotalPrice] Float, " +
            "  [Status] VARCHAR (1), " +
            "  [AuthuticationFlag] VARCHAR (1), " +
            "  CONSTRAINT [] PRIMARY KEY ([SecSeq], [ReceiptNo]));";

    private final static String CREATE_CLASSSALESDETAIL = "CREATE TABLE IF NOT EXISTS [ClassSalesDetail] (" +
            "  [SecSeq] VARCHAR(2), " +
            "  [ReceiptNo] VARCHAR(10), " +
            "  [SerialNo] Integer PRIMARY KEY Autoincrement," +
            "  [Infant] VARCHAR(10), " +
            "  [OriginalClass] VARCHAR(10), " +
            "  [NewClass] VARCHAR(10), " +
            "  [SalesPrice] Float, " +
            "  [SalesQty] Int, " +
            "  [Status] VARCHAR (1));";

    private final static String CREATE_CLASSPAYMENTINFO = "CREATE TABLE IF NOT EXISTS [ClassPaymentInfo] (" +
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
            "  CONSTRAINT [] PRIMARY KEY ([SecSeq], [ReceiptNo], [PayBy], [Currency], [Status]));";

    private final static String CREATE_SSIDLIST = "CREATE TABLE IF NOT EXISTS [SSIDList] (" +
            "  [SSID] VARCHAR(50), " +
            "  [MachineID] VARCHAR(14), " +
            "  CONSTRAINT [] PRIMARY KEY ([SSID], [MachineID]));";

    private final static String CREATE_PROMOTIONSINFO = "CREATE TABLE IF NOT EXISTS [PromotionsInfo] (" +
            "  [promotionsTitle] VARCHAR (100), " +
            "  [PromotionsDetail] VARCHAR (500), " +
            "  [StartDate] VARCHAR (8), " +
            "  [EndDate] VARCHAR (8), " +
            "  [PromotionsCode] VARCHAR (20), " +
            "  [Note] VARCHAR (500), " +
            "  [PrintType] VARCHAR (20), " +
            "  CONSTRAINT [] PRIMARY KEY ([promotionsTitle], [PromotionsDetail]));";

    private final static String CREATE_ORDERCHANGEHISTORY = "CREATE TABLE IF NOT EXISTS [OrderChangeHistory] (" +
            "  [ReceiptNo] Integer PRIMARY KEY Autoincrement, " +
            "  [SecSeq] VARCHAR (2), " +
            "  [OldOrderNo] VARCHAR (20), " +
            "  [NewOrderNo] VARCHAR (20));";

    private final static String CREATE_DISCOUNT = "CREATE TABLE IF NOT EXISTS [Discount] (" +
            "  [Type] VARCHAR (6), " +
            "  [DiscountRate] Float, " +
            "  [DiscountAmount] Int, " +
            "  [DiscountGift] VARCHAR (10), " +
            "  [Description] VARCHAR (100), " +
            "  [Upperlimit] Int, " +
            "  [FuncID] VARCHAR (3), " +
            "  [Progression] VARCHAR (1), " +
            "  CONSTRAINT [] PRIMARY KEY ([Type]));";

    private final static String CREATE_FUNCTABLE = "CREATE TABLE IF NOT EXISTS [FuncTable] (" +
            "  [FuncID] VARCHAR  (3), " +
            "  [Syntax] VARCHAR  (200), " +
            "  [Description] VARCHAR  (200), " +
            "  [ChildID] VARCHAR  (3), " +
            "  CONSTRAINT [] PRIMARY KEY ([FuncID]));";

    private final static String CREATE_ITEMGROUP = "CREATE TABLE IF NOT EXISTS [ItemGroup] (" +
            "  [GroupID] VARCHAR  (6), " +
            "  [ItemCode] VARCHAR  (20), " +
            "  CONSTRAINT [] PRIMARY KEY ([GroupID],[ItemCode]));";

    private final static String CREATE_TAIWANCREDITCARDINFO = "CREATE TABLE IF NOT EXISTS [TaiwanCreditCardInfo] (" +
            "  [BinCode] VARCHAR (15), " +
            "  CONSTRAINT [] PRIMARY KEY ([BinCode]));";

    private final static String CREATE_DISCOUNTEXCEPTION = "CREATE TABLE IF NOT EXISTS [DiscountException] (" +
            "  [DiscountType] VARCHAR (6), " +
            "  [ItemCode] VARCHAR (20), " +
            "  CONSTRAINT [] PRIMARY KEY ([DiscountType], [ItemCode]));";

    private final static String CREATE_SETTINGS = "CREATE TABLE IF NOT EXISTS [Settings] (" +
            "  [Key] VARCHAR (50), " +
            "  [Value] VARCHAR (50), " +
            "  CONSTRAINT [] PRIMARY KEY ([Key]));";
    //endregion

    //建構函式
    private TSQL(Context context, String SecSeq, String DBName) {
        super(context, DBName, null, DBVersion);
        SQLiteDatabase.loadLibs(context);
        this.DBPath = new File(SDcardPath + DBName + ".db3");
        TSQL.SecSeq = SecSeq;
        TSQL.DBName = DBName;
        this.mContext = context;

        if (DBName.equals("P17023")) {
            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, DB_PSW, null);
        } else {
            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, "", null);
        }
    }

    public static synchronized TSQL getINSTANCE(Context context, String secSeq, String dbName) {
        if (INSTANCE == null || !dbName.equals(DBName) || !secSeq.equals(SecSeq)) {
            INSTANCE = new TSQL(context, secSeq, dbName);
        }
        return INSTANCE;
    }

//    public TSQL(Context context, String SecSeq, String DBName) {
//        super(context, DBName, null, DBVersion);
//        this.DBPath = new File(SDcardPath + DBName + ".db3");
//        this.SecSeq = SecSeq;
//        this.DBName = DBName;
//        this.mContext = context;
//    }

    public void encryptDB(String dbName) {
        try {
            File originalFile = new File(SDcardPath + dbName + ".db3");

            if (originalFile.exists()) {
                File newFile = new File(SDcardPath + "temp.db3");

                SQLiteDatabase existing_db = SQLiteDatabase.openOrCreateDatabase(SDcardPath + dbName + ".db3", "", null);

//            existing_db.rawExecSQL("ATTACH DATABASE '" + newFile.getPath() + "' AS encrypted KEY '" + DB_PSW + "';");
                existing_db.rawExecSQL(String.format("ATTACH DATABASE '%s' AS encrypted KEY '%s';", newFile.getAbsolutePath(), DB_PSW));
                existing_db.rawExecSQL("SELECT sqlcipher_export('encrypted')");
                existing_db.rawExecSQL("DETACH DATABASE encrypted;");

                existing_db = SQLiteDatabase.openDatabase(newFile.getAbsolutePath(), DB_PSW, null, SQLiteDatabase.OPEN_READWRITE);

                existing_db.close();

                originalFile.delete();

                newFile.renameTo(originalFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //建立函式
    @Override
    public void onCreate(android.database.sqlite.SQLiteDatabase db) {
        db.execSQL(CREATE_ADJUST);
        db.execSQL(CREATE_ALLPARTS);
        db.execSQL(CREATE_AUTHUTICATION);
        db.execSQL(CREATE_BANKDIS);
        db.execSQL(CREATE_CARTLIST);
        db.execSQL(CREATE_CLASSALLPARTS);
        db.execSQL(CREATE_CLASSPAYMENTINFO);
        db.execSQL(CREATE_CLASSSALESDETAIL);
        db.execSQL(CREATE_CLASSSALESHEAD);
        db.execSQL(CREATE_CREDITCARD);
        db.execSQL(CREATE_CREWINFO);
        db.execSQL(CREATE_DAMAGE);
        db.execSQL(CREATE_DISCOUNT);
        db.execSQL(CREATE_DISCOUNTEXCEPTION);
        db.execSQL(CREATE_FLIGHT);
        db.execSQL(CREATE_FUNCTABLE);
        db.execSQL(CREATE_INVENTORY);
        db.execSQL(CREATE_ITEMGROUP);
        db.execSQL(CREATE_ORDERCHANGEHISTORY);
        db.execSQL(CREATE_PAYMENTINFO);
        db.execSQL(CREATE_PREORDERDETAIL);
        db.execSQL(CREATE_PREORDERHEAD);
        db.execSQL(CREATE_PREORDERSALESHEAD);
        db.execSQL(CREATE_PROMOTIONSINFO);
        db.execSQL(CREATE_RATE);
        db.execSQL(CREATE_SALESDETAIL);
        db.execSQL(CREATE_SALESHEAD);
        db.execSQL(CREATE_SETTINGS);
        db.execSQL(CREATE_SSIDLIST);
        db.execSQL(CREATE_STAFF);
        db.execSQL(CREATE_SYSTOMLOG);
        db.execSQL(CREATE_TAIWANCREDITCARDINFO);
        db.execSQL(CREATE_TRANSFER);
    }

    //升級函式
    @Override
    public void onUpgrade(android.database.sqlite.SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 清空所有table
     */
    public void clearAllTable() {
//        CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, null);
        CurrentDB.delete("Adjust", null, null);
        CurrentDB.delete("AllParts", null, null);
        CurrentDB.delete("Authutication", null, null);
        CurrentDB.delete("BankDis", null, null);
        CurrentDB.delete("CartList", null, null);
        CurrentDB.delete("ClassAllParts", null, null);
        CurrentDB.delete("ClassPaymentInfo", null, null);
        CurrentDB.delete("ClassSalesDetail", null, null);
        CurrentDB.delete("ClassSalesHead", null, null);
        CurrentDB.delete("CreditCard", null, null);
        CurrentDB.delete("CrewInfo", null, null);
        CurrentDB.delete("Damage", null, null);
        CurrentDB.delete("Discount", null, null);
        CurrentDB.delete("DiscountException", null, null);
        CurrentDB.delete("Flight", null, null);
        CurrentDB.delete("FuncTable", null, null);
        CurrentDB.delete("Inventory", null, null);
        CurrentDB.delete("ItemGroup", null, null);
        CurrentDB.delete("OrderChangeHistory", null, null);
        CurrentDB.delete("PaymentInfo", null, null);
        CurrentDB.delete("PreorderDetail", null, null);
        CurrentDB.delete("PreorderHead", null, null);
        CurrentDB.delete("PreorderSalesHead", null, null);
        CurrentDB.delete("PromotionsInfo", null, null);
        CurrentDB.delete("Rate", null, null);
        CurrentDB.delete("SSIDList", null, null);
        CurrentDB.delete("SalesDetail", null, null);
        CurrentDB.delete("SalesHead", null, null);
        CurrentDB.delete("Settings", null, null);
        CurrentDB.delete("Staff", null, null);
        CurrentDB.delete("SystemLog", null, null);
        CurrentDB.delete("TaiwanCreditCardInfo", null, null);
        CurrentDB.delete("Transfer", null, null);
        CurrentDB.close();
    }

    /////////////////////////////////////
    //
    //            公用函式
    //
    /////////////////////////////////////

    /**
     * 執行SQL指令 by Sales DB
     *
     * @param SQLCommand 要執行的SQL指令
     * @return True = 成功，False = 失敗
     */
    public Boolean ExecutesSQLCommand(String SQLCommand) {

        String SQL = "";

        try {
            //打開資料庫連線
//            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, null);
//            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, DB_PSW, null);
            //this.openOrCreateDatabase //package內部的資料庫取得, 預設於/data/data/com.test/databases之下的DB

            //開啟Transcation
            CurrentDB.beginTransaction();

            SQL = SQLCommand;
            CurrentDB.execSQL(SQLCommand);

            //完成Transcation
            CurrentDB.setTransactionSuccessful();

            return true;
        } catch (Exception ex) {

            //結束Transcation
            if (CurrentDB.inTransaction()) {
                CurrentDB.endTransaction();
            }

            //寫入Log
            WriteLog(SecSeq, "System DB Error", "TSQL", "ExecutesSQLCommand", ex.getMessage() + " -- SQL Command:" + SQL);

            throw ex;
            //return false;

        } finally {

            //結束Transcation
            if (CurrentDB.inTransaction()) {
                CurrentDB.endTransaction();
            }

            //關閉連線
//            if (CurrentDB != null) {
//                CurrentDB.close();
//            }
        }
    }

    /**
     * 執行SQL指令 by Sales DB
     *
     * @param SQLCommands 要執行的SQL指令陣列
     * @return True = 成功，False = 失敗
     */
    public Boolean ExecutesSQLCommand(ArrayList SQLCommands) {

        int i = 0;

        try {
            //打開資料庫連線
//            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, null);
//            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, DB_PSW, null);

            //開啟Transaction
            CurrentDB.beginTransaction();

            int CommandsSize = SQLCommands.size();
            for (i = 0; i < CommandsSize; i++) {
                CurrentDB.execSQL((String) SQLCommands.get(i));
            }

            //完成Transcation
            CurrentDB.setTransactionSuccessful();

            return true;
        } catch (Exception ex) {

            //結束Transcation
            if (CurrentDB.inTransaction()) {
                CurrentDB.endTransaction();
            }

            //寫入Log
            WriteLog(SecSeq, "System DB Error", "TSQL", "ExecutesSQLCommand", ex.getMessage() + " -- SQL Command:" + (String) SQLCommands.get(i));

            throw ex;
            //return false;

        } finally {

            //結束Transcation
            if (CurrentDB.inTransaction()) {
                CurrentDB.endTransaction();
            }

            //關閉連線
//            if (CurrentDB != null) {
//                CurrentDB.close();
//            }
        }
    }

    /**
     * 查詢SQL指令 by Sales DB
     *
     * @param SQLCommand 要查詢的SQL指令
     * @return JSONArray
     */
    public JSONArray SelectSQLJsonArray(String SQLCommand) throws Exception {

        Cursor cursor = null;
        JSONArray resultSet = new JSONArray();
        JSONObject rowObject;

        try {
            //打開資料庫連線
//            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, null);
//            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, DB_PSW, null);

            cursor = CurrentDB.rawQuery(SQLCommand, null);

            if (cursor != null && cursor.getCount() > 0) {

                //逐筆取得Cursor資料
                cursor.moveToFirst(); // 移到第 1 筆資料
                int CursorCount = cursor.getColumnCount();
                do {
                    rowObject = new JSONObject();

                    // 逐筆讀出資料
                    for (int i = 0; i < CursorCount; i++) {

                        switch (cursor.getType(i)) {
                            case Cursor.FIELD_TYPE_BLOB://大的二进制数据
                                rowObject.put(cursor.getColumnName(i), cursor.getBlob(i));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT://浮点类型
                                rowObject.put(cursor.getColumnName(i), cursor.getDouble(i));
                                break;
                            case Cursor.FIELD_TYPE_INTEGER://整数
                                rowObject.put(cursor.getColumnName(i), cursor.getInt(i));
                                break;
                            case Cursor.FIELD_TYPE_STRING://字符串
                                rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                                break;
                            case Cursor.FIELD_TYPE_NULL://空
                                break;
                        }
                    }

                    //rowObject
                    if (String.valueOf(rowObject).length() > 2) {
                        resultSet.put(rowObject);
                    }

                } while (cursor.moveToNext()); // 有一下筆就繼續迴圈
            }

            if (String.valueOf(resultSet).length() > 2) {
                return resultSet;
            } else {
                return null;
            }

        } catch (Exception ex) {

            //寫入Log
            WriteLog(SecSeq, "System DB Error", "TSQL", "SelectSQLJsonArray", ex.getMessage() + " -- SQL Command:" + SQLCommand);

            throw ex;
            //return null;

        } finally {

            //關閉Cursor
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            //關閉連線
//            if (CurrentDB != null) {
//                CurrentDB.close();
//            }
        }
    }

    /**
     * 查詢SQL指令
     *
     * @param SQLCommand 要查詢的SQL指令
     * @return JSONArray（全字串型態)
     */
    public JSONArray SelectSQLJsonArrayByString(String SQLCommand) throws Exception {

        Cursor cursor = null;
        JSONArray resultSet = new JSONArray();
        JSONObject rowObject;

        try {
            //打開資料庫連線
//            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, null);
//            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, DB_PSW, null);

            cursor = CurrentDB.rawQuery(SQLCommand, null);

            if (cursor != null && cursor.getCount() > 0) {

                //逐筆取得Cursor資料
                cursor.moveToFirst(); // 移到第 1 筆資料
                int CursorCount = cursor.getColumnCount();
                do {
                    rowObject = new JSONObject();

                    // 逐筆讀出資料
                    for (int i = 0; i < CursorCount; i++) {

                        switch (cursor.getType(i)) {
                            case Cursor.FIELD_TYPE_BLOB://大的二进制数据
                                rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                                break;
                            case Cursor.FIELD_TYPE_FLOAT://浮点类型
                                rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                                break;
                            case Cursor.FIELD_TYPE_INTEGER://整数
                                rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                                break;
                            case Cursor.FIELD_TYPE_STRING://字符串
                                rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                                break;
                            case Cursor.FIELD_TYPE_NULL://空
                                break;
                        }
                    }

                    //rowObject
                    if (String.valueOf(rowObject).length() > 2) {
                        resultSet.put(rowObject);
                    }

                } while (cursor.moveToNext()); // 有一下筆就繼續迴圈
            }

            if (String.valueOf(resultSet).length() > 2) {
                return resultSet;
            } else {
                return null;
            }

        } catch (Exception ex) {

            //寫入Log
            WriteLog(SecSeq, "System DB Error", "TSQL", "SelectSQLJsonArrayByString", ex.getMessage() + " -- SQL Command:" + SQLCommand);

            throw ex;
            //return null;

        } finally {

            //關閉Cursor
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            //關閉連線
//            if (CurrentDB != null) {
//                CurrentDB.close();
//            }
        }
    }

    /**
     * 查詢SQL指令 by Sales DB
     *
     * @param SQLCommand 要查詢的SQL指令
     * @return (Object) 查詢物件
     */
    public Object SelectSQLObject(String SQLCommand) {

        Cursor cursor = null;

        try {
            //打開資料庫連線
//            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, null);
//            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, DB_PSW, null);

            cursor = CurrentDB.rawQuery(SQLCommand, null);

            if (cursor != null && cursor.getCount() > 0) {

                //逐筆取得Cursor資料
                cursor.moveToFirst(); // 移到第 1 筆資料
                switch (cursor.getType(0)) {
                    case Cursor.FIELD_TYPE_BLOB://大的二进制数据
                        return cursor.getBlob(0);
                    case Cursor.FIELD_TYPE_FLOAT://浮点类型
                        return cursor.getFloat(0);
                    case Cursor.FIELD_TYPE_INTEGER://整数
                        return cursor.getInt(0);
                    case Cursor.FIELD_TYPE_STRING://字符串
                        return cursor.getString(0);
                    case Cursor.FIELD_TYPE_NULL://空
                        return null;
                }
            }

            return null;

        } catch (Exception ex) {

            //寫入Log
            WriteLog(SecSeq, "System DB Error", "TSQL", "SelectSQLObject", ex.getMessage() + " -- SQL Command:" + SQLCommand);

            throw ex;
            //return null;

        } finally {

            //關閉Cursor
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            //關閉連線
//            if (CurrentDB != null) {
//                CurrentDB.close();
//            }
        }
    }

    /**
     * 確認查詢的資料是否存在 by Sales DB
     *
     * @param SQLCommand 要查詢的SQL指令
     * @return (Boolean) True = 存在, False = 不存在
     */
    public Boolean CheckDataIsExist(String SQLCommand) {

        Cursor cursor = null;

        try {
            //打開資料庫連線
//            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, null);
//            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, DB_PSW, null);

            cursor = CurrentDB.rawQuery(SQLCommand, null);

            return cursor != null && cursor.getCount() > 0;

        } catch (Exception ex) {

            //寫入Log
            WriteLog(SecSeq, "System DB Error", "TSQL", "CheckDataIsExist", ex.getMessage() + " -- SQL Command:" + SQLCommand);

            throw ex;
            //return null;

        } finally {

            //關閉Cursor
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }

            //關閉連線
//            if (CurrentDB != null) {
//                CurrentDB.close();
//            }
        }
    }

    /**
     * 進行Log紀錄
     *
     * @param SecSeq        航段序號
     * @param LogType       LOG分類
     * @param OperationName 作業名稱
     * @param FunctionName  函式名稱
     * @param LogText       LOG內容
     * @return True = 成功，False = 失敗
     */
    public Boolean WriteLog(String SecSeq, String LogType, String OperationName, String FunctionName, String LogText) {

        String SQL;

        if (SecSeq.length() > 2) {
            SecSeq = SecSeq.substring(0, 2);
        }
        if (LogType.length() > 30) {
            LogType = LogType.substring(0, 30);
        }
        if (OperationName.length() > 50) {
            OperationName = OperationName.substring(0, 50);
        }
        if (FunctionName.length() > 50) {
            FunctionName = FunctionName.substring(0, 50);
        }

        try {
            //打開資料庫連線
//            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, null);
//            CurrentDB = SQLiteDatabase.openOrCreateDatabase(DBPath, DB_PSW, null);

            //開啟Transcation
            CurrentDB.beginTransaction();

            SQL = "INSERT INTO SystemLog(SystemDate,SecSeq,LogType,OperationName,FunctionName,LogText) " +
                    "VALUES ('" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(System.currentTimeMillis())) +
                    "','" + SecSeq +
                    "','" + LogType +
                    "','" + OperationName +
                    "','" + FunctionName +
                    "','" + LogText.replace("'", "''") + "')";

            CurrentDB.execSQL(SQL);

            //完成Transcation
            CurrentDB.setTransactionSuccessful();

            return true;
        } catch (Exception ex) {

            //結束Transcation
            if (CurrentDB.inTransaction()) {
                CurrentDB.endTransaction();
            }

            //throw ex;
            return false;

        } finally {

            //結束Transcation
            if (CurrentDB.inTransaction()) {
                CurrentDB.endTransaction();
            }

            //關閉連線
//            if (CurrentDB != null) {
//                CurrentDB.close();
//            }
        }
    }
}
