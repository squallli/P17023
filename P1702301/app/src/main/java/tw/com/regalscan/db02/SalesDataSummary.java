package tw.com.regalscan.db02;


import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.regalscan.sqlitelibrary.TSQL;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by gabehsu on 2017/8/3.
 */

public class SalesDataSummary {

    private Handler handler;

    //銷售資料Json字串
    private Context context;
    private String SecSeq;
    private TSQL _TSQL;

    //起始函式
    public SalesDataSummary(Context context, String SecSeq, Handler handler) {
        this.context = context;
        this.SecSeq = SecSeq;
        this.handler = handler;
    }

    private void sendMsg(int flag) {
        Message message = new Message();
        message.what = flag;
        handler.sendMessage(message);
    }

    /////////////////////////////////////
    //
    //            公用函式
    //
    /////////////////////////////////////

    /**
     * 將銷售資料轉為Json格式
     *
     * @return (JSONObject) 標準格式回傳
     */
    public JSONObject SalesDataConvertToJson() throws Exception {

        String ClassName = "SalesDataSummary";
        String FunctionName = "SalesDataConvertToJson";
        String ReturnCode, ReturnMessage;

        _TSQL = TSQL.getINSTANCE(context, this.SecSeq, "P17023");

        //寫入Log
//        _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Function start");

        //彙總的JSON 字串
        JSONObject SalesDataJSONObject = new JSONObject();
        String WorkingTextName = "";    //目前正在處理工作名稱

        try {

            // 1. IP 文字檔 (取得POS產生的IFE order log)
            WorkingTextName = "toJSON_IP";
            toJSON_IP(SalesDataJSONObject);
            sendMsg(1);
            Thread.sleep(1000);

            // 2. IM 文字檔 (取得自IFE取得的order log)
            WorkingTextName = "toJSON_IM";
            toJSON_IM(SalesDataJSONObject);
            sendMsg(2);
            Thread.sleep(1000);

            // 3. LC 文字檔 (取得所有的取還授權紀錄(不管是否成功))
            WorkingTextName = "toJSON_LC";
            toJSON_LC(SalesDataJSONObject);
            sendMsg(3);
            Thread.sleep(1000);

            // 4. RR 文字檔 (機敏資料加密版本) (取得所有的還授權和退款)
            WorkingTextName = "toJSON_RR";
            toJSON_RR(SalesDataJSONObject);
            sendMsg(4);
            Thread.sleep(1000);

            // 5. RS 文字檔 (各航段商品庫存數量列表)
            WorkingTextName = "toJSON_RS";
            toJSON_RS(SalesDataJSONObject);
            sendMsg(5);
            Thread.sleep(1000);

            // 6. RD 文字檔 (機敏資料加密版本) (取得該航班有折扣的銷售商品價格)
            WorkingTextName = "toJSON_RD";
            toJSON_RD(SalesDataJSONObject);
            sendMsg(6);
            Thread.sleep(1000);

            // 7. RC 文字檔 (機敏資料加密版本) (該航班的信用卡刷卡交易明細)
            WorkingTextName = "toJSON_RC";
            toJSON_RC(SalesDataJSONObject);
            sendMsg(7);
            Thread.sleep(1000);

            // 8. RZ 文字檔 (地勤清點人員清單) (抓最後航段的最後清點人員)
            WorkingTextName = "toJSON_RZ";
            toJSON_RZ(SalesDataJSONObject);
            sendMsg(8);
            Thread.sleep(1000);

            // 9. RA 文字檔 (各航段的收受金額彙總)
            WorkingTextName = "toJSON_RA";
            toJSON_RA(SalesDataJSONObject);
            sendMsg(9);
            Thread.sleep(1000);

            // 10. RV1 文字檔 (航班內各交易金流，含IFE資訊)
            WorkingTextName = "toJSON_RV1";
            toJSON_RV1(SalesDataJSONObject);
            sendMsg(10);
            Thread.sleep(1000);

            // 11. RV2 文字檔 (取得銷售顧客資訊檔，已無用處，不使用)
//            WorkingTextName = "toJSON_RV2";
//            toJSON_RV2(SalesDataJSONObject);

            // 12. RV3 文字檔 (取得每筆單據內，每個item的銷售總價/數量/單價)
            WorkingTextName = "toJSON_RV3";
            toJSON_RV3(SalesDataJSONObject);
            sendMsg(11);
            Thread.sleep(1000);

            // 13. RC1 文字檔 (升艙等信用卡付款紀錄)
            WorkingTextName = "toJSON_RC1";
            toJSON_RC1(SalesDataJSONObject);
            sendMsg(12);
            Thread.sleep(1000);

            // 14. RA1 文字檔 (每個航段的升艙等 現金/信用卡，美金/台幣收受總額)
            WorkingTextName = "toJSON_RA1";
            toJSON_RA1(SalesDataJSONObject);
            sendMsg(13);
            Thread.sleep(1000);

            // 15. LOG 文字檔 (系統Log匯出)
            WorkingTextName = "toJSON_LOG";
            toJSON_LOG(SalesDataJSONObject);
            sendMsg(14);
            Thread.sleep(1000);

            // 16. PODS 文字檔 (取出地面預訂機上取貨的訂單狀態 VIP Paid and Preorder，不包含VIP Sale)
            WorkingTextName = "toJSON_PODS";
            toJSON_PODS(SalesDataJSONObject);
            sendMsg(15);
            Thread.sleep(1000);

            // 17. PODR 文字檔 (取出地面預訂機上退貨的訂單狀態 VIP Paid and Preorder，不包含VIP Sale)
            WorkingTextName = "toJSON_PODR";
            toJSON_PODR(SalesDataJSONObject);
            sendMsg(16);
            Thread.sleep(1000);

            // 18. COU 文字檔 (取得DC Coupon消費紀錄)
            WorkingTextName = "toJSON_COU";
            toJSON_COU(SalesDataJSONObject);
            sendMsg(17);
            Thread.sleep(1000);

            // 19. RU1 文字檔 (航班內各升倉等交易金流)
            WorkingTextName = "toJSON_RU1";
            toJSON_RU1(SalesDataJSONObject);
            sendMsg(18);
            Thread.sleep(1000);

            // 20. RU3 文字檔 (取得每筆升倉等單據內，每個item的銷售總價/數量/單價)
            WorkingTextName = "toJSON_RU3";
            toJSON_RU3(SalesDataJSONObject);
            sendMsg(19);
            Thread.sleep(1000);


            // 21. Carousel 文字檔
            WorkingTextName = "toCarousel";
            toTxt_Carousel();
            sendMsg(22);
            Thread.sleep(1000);


            _TSQL.close();
            ReturnCode = "0";
            ReturnMessage = "";
            JSONArray ReturnData = new JSONArray();
            ReturnData.put(SalesDataJSONObject);
            return GetReturnJsonObject(ReturnCode, ReturnMessage, ReturnData);

        } catch (Exception ex) {

            //寫入Log
            _TSQL.WriteLog(this.SecSeq, "System", ClassName, FunctionName, "Working Name:" + WorkingTextName + " - Error message:" + ex.getMessage());

            ReturnCode = "9";
            ReturnMessage = "Working Name:" + WorkingTextName + " - Error message:" + ex.getMessage();
            return GetReturnJsonObject(ReturnCode, ReturnMessage, null);
        }
    }

    /////////////////////////////////////
    //
    //   　     銷售資料轉檔函式
    //
    /////////////////////////////////////

    // 1. IP 文字檔 (取得POS產生的IFE order log)
    private void toJSON_IP(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "IP": [{
//                        "FlightDate": "",
//                        "DepFlightNo": "",
//                        "CarNo": "",
//                        "SecSeq": "",
//                        "CrewID": "",
//                        "NewOrderNo": ""
//            }, {
//                        "FlightDate": "",
//                        "DepFlightNo": "",
//                        "CarNo": "",
//                        "SecSeq": "",
//                        "CrewID": "",
//                        "NewOrderNo": ""
//            }]
        //endregion

        String SQL = "SELECT FlightDate, DepFlightNo, CarNo, " +
                "OrderChangeHistory.SecSeq, CrewID, NewOrderNo " +
                "FROM OrderChangeHistory LEFT JOIN Flight " +
                "ON OrderChangeHistory.SecSeq = Flight.SecSeq " +
                "WHERE OldOrderNo = ''";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja == null) {
            ja = new JSONArray();
        }

        SalesDataJSONObject.put("IP", ja);

    }

    // 2. IM 文字檔 (取得自IFE取得的order log)
    private void toJSON_IM(JSONObject SalesDataJSONObject) throws Exception {

        try {
            //region JSON Example
//            "IM":[
//            {
//                    "FlightDate":"",
//                    "DepFlightNo":"",
//                    "CarNo":"",
//                    "SecSeq":"",
//                    "CrewID":"",
//                    "OldOrderNo":"",
//                    "NewOrderNo":""
//            },
//            {
//                    "FlightDate":"",
//                    "DepFlightNo":"",
//                    "CarNo":"",
//                    "SecSeq":"",
//                    "CrewID":"",
//                    "OldOrderNo":"",
//                    "NewOrderNo":""
//            }]
            //endregion

            String SQL = "SELECT FlightDate, DepFlightNo, CarNo, " +
                    "OrderChangeHistory.SecSeq, CrewID, OldOrderNo, NewOrderNo " +
                    "FROM OrderChangeHistory LEFT JOIN Flight " +
                    "ON OrderChangeHistory.SecSeq = Flight.SecSeq " +
                    "WHERE OldOrderNo <> ''";
            JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

            if (ja == null) {
                ja = new JSONArray();
            }

            SalesDataJSONObject.put("IM", ja);

        } catch (Exception ex) {
            throw ex;
        }
    }

    // 3. LC 文字檔 (取得所有的取還授權紀錄(不管是否成功))
    private void toJSON_LC(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "LC":[{
//                        "AuthDate":"",
//                        "AuthTime":"",
//                        "OnlineTransactionRemark":"",
//                        "FlightDate":"",
//                        "DepFlightNo":"",
//                        "RecNo":"",
//                        "OrderNo":"",
//                        "CaNo":"",
//                        "SecSeq":"",
//                        "CardType":"",
//                        "CardNo":"",
//                        "CardName":"",
//                        "ExpireDate":"",
//                        "USDAMT":"",
//                        "TWDAMT":"",
//                        "CarNo":"",
//                        "PreorderNo":"",
//                        "VIPType":"",
//                        "VIPNo":"",
//                        "UG_MARK":"",
//                        "TRANS_CODE":"",
//                        "TRANS_MODE":"",
//                        "TRANS_DATE":"",
//                        "TRANS_TIME":"",
//                        "APPROVE_CODE":"",
//                        "RSPONSE_CODE":"",
//                        "RSPONSE_MSG":"",
//                        "AUTH_RETCODE":"",
//                        "TRANS_SEQNO":"",
//                        "REAUTH_MARK":"",
//                        "AUTH_DATE":"",
//                        "Remark":""
//            }]
        //endregion

        String SQL = "SELECT AUTH_DATE, AuthTime, 'Y' AS OnlineTransactionRemark," +
                "FlightDate, DepFlightNo, " +
                "ReceiptNo, OrderNo, CrewID, Authutication.SecSeq AS SecSeq," +
                "(CASE CardType WHEN 'JCB' THEN 'JC' WHEN 'VISA' THEN 'VI' WHEN 'MASTER' THEN 'MT' WHEN 'AMX' THEN 'AX' WHEN 'DINERS' THEN 'DC' ELSE CardType END) AS CardType, " +
                "CardNo, CardName, ExpireDate,  " +
                "(CASE WHEN CurrencyType = 'USD' THEN TransAmount ELSE 0 END) AS USDAMT, " +
                "(CASE WHEN CurrencyType = 'TWD' THEN TransAmount ELSE 0 END) AS TWDAMT, " +
                "CarNo, PreorderNo, VipType, VipNo, UG_MARK, (CASE WHEN TRANS_CODE = 'null' THEN '' ELSE TRANS_CODE END) as TRANS_CODE, " +
                "'0' AS TRANS_MODE, TRANS_DATE, (CASE WHEN TRANS_TIME = 'null' THEN '' ELSE TRANS_TIME END) as TRANS_TIME, APPROVE_CODE, RSPONSE_CODE, " +
                "RSPONSE_MSG, AUTH_RETCODE, TRANS_SEQNO, AUTH_DATE, '' AS Remark, " +
                "(CASE WHEN REAUTH_MARK = 'N' THEN 'A' ELSE 'R' END) AS REAUTH_MARK " +
                "FROM Authutication LEFT JOIN Flight " +
                "ON Authutication.SecSeq = Flight.SecSeq";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja != null) {
            for (int i = 0; i < ja.length(); i++) {

                // DateTime預設格式為 yyyy-MM-dd HH:mm:ss
                ja.getJSONObject(i).put("AuthDate", ja.getJSONObject(i).optString("AUTH_DATE"));

                ja.getJSONObject(i).put("AuthTime", ja.getJSONObject(i).getString("AuthTime").split(" ")[1].replace(":", ""));
            }
        } else {
            ja = new JSONArray();
        }

        SalesDataJSONObject.put("LC", ja);

    }

    // 4. RR 文字檔 (取得所有的還授權和退款)
    private void toJSON_RR(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "RR":[{
//                        "FlightDate":"",
//                        "DepFlightNo":"",
//                        "ReceiptNo":"",
//                        "IFEOrderNo":"",
//                        "CrewID":"",
//                        "SecSeq":"",
//                        "CardType":"",
//                        "CardNo":"",
//                        "CardName":"",
//                        "CardDate":"",
//                        "USDAMT":"",
//                        "TWDAMT":"",
//                        "CarNo":"",
//                        "PreorderNo":"",
//                        "UpperLimitType":"",
//                        "UpperLimitNo":"",
//                        "UG_MARK":"",
//                        "TRANS_CODE":"",
//                        "TRANS_MODE":"",
//                        "TRANS_DATE":"",
//                        "TRANS_TIME":"",
//                        "APPROVE_CODE":"",
//                        "RSPONSE_CODE":"",
//                        "RSPONSE_MSG":"",
//                        "AUTH_RETCODE":"",
//                        "TRANS_SEQNO":"",
//                        "AUTH_DATE":""
//            }]
        //endregion

        //取得DFS還授權 + Class還授權
        String SQL = "SELECT FlightDate, DepFlightNo, " +
                "SalesHead.ReceiptNo AS ReceiptNo, SalesHead.IFEOrderNo AS IFEOrderNo, " +
                "CrewID, SalesHead.SecSeq AS SecSeq, " +
                "(CASE PaymentInfo.CardType WHEN 'JCB' THEN 'JC' WHEN 'VISA' THEN 'VI' WHEN 'MASTER' THEN 'MT' WHEN 'AMX' THEN 'AX' WHEN 'DINERS' THEN 'DC' ELSE PaymentInfo"
                + ".CardType END) AS CardType, "
                +
                "PaymentInfo.CardNo AS CardNo, PaymentInfo.CardName AS CardName, " +
                "PaymentInfo.CardDate AS CardDate, " +
                "(CASE WHEN CurrencyType = 'USD' THEN TransAmount ELSE 0 END) AS USDAMT, " +
                "(CASE WHEN CurrencyType = 'TWD' THEN TransAmount ELSE 0 END) AS TWDAMT, " +
                "CarNo, SalesHead.PreorderNo AS PreorderNo, " +
                "UpperLimitType, UpperLimitNo, " +
                "'N' AS UG_MARK,TRANS_CODE,'0' AS TRANS_MODE, " +
                "TRANS_DATE,TRANS_TIME,APPROVE_CODE,RSPONSE_CODE, " +
                "RSPONSE_MSG,AUTH_RETCODE,TRANS_SEQNO,AUTH_DATE " +
                "FROM SalesHead LEFT JOIN PaymentInfo " +
                "ON SalesHead.ReceiptNo = PaymentInfo.ReceiptNo " +
                "AND SalesHead.SecSeq = PaymentInfo.SecSeq " +
                "AND SalesHead.Status = 'R' " +
                "LEFT JOIN Authutication " +
                "ON Authutication.ReceiptNo = SalesHead.ReceiptNo " +
                "AND Authutication.SecSeq = SalesHead.SecSeq " +
                "AND Authutication.UG_MARK = 'N' " +
                "AND REAUTH_MARK = 'Y' " +
                "LEFT JOIN Flight " +
                "ON SalesHead.SecSeq = Flight.SecSeq " +
                "WHERE PayBy = 'Card'" +
                "UNION " +

                "SELECT FlightDate, DepFlightNo, " +
                "ClassSalesHead.ReceiptNo AS ReceiptNo, '' AS IFEOrderNo, " +
                "CrewID, ClassSalesHead.SecSeq AS SecSeq, " +
                "(CASE ClassPaymentInfo.CardType WHEN 'JCB' THEN 'JC' WHEN 'VISA' THEN 'VI' WHEN 'MASTER' THEN 'MT' WHEN 'AMX' THEN 'AX' WHEN 'DINERS' THEN 'DC' ELSE "
                + "ClassPaymentInfo.CardType END) AS CardType, "
                +
                "ClassPaymentInfo.CardNo AS CardNo, ClassPaymentInfo.CardName AS CardName, " +
                "ClassPaymentInfo.CardDate AS CardDate, " +
                "(CASE WHEN CurrencyType = 'USD' THEN TransAmount ELSE 0 END) AS USDAMT, " +
                "(CASE WHEN CurrencyType = 'TWD' THEN TransAmount ELSE 0 END) AS TWDAMT, " +
                "CarNo, '' AS PreorderNo, " +
                "'' AS UpperLimitType,'' AS UpperLimitNo,'Y' AS UG_MARK, TRANS_CODE,'0' AS TRANS_MODE, " +
                "TRANS_DATE,TRANS_TIME,APPROVE_CODE,RSPONSE_CODE, " +
                "RSPONSE_MSG,AUTH_RETCODE,TRANS_SEQNO,AUTH_DATE " +
                "FROM ClassSalesHead LEFT JOIN ClassPaymentInfo " +
                "ON ClassSalesHead.ReceiptNo = ClassPaymentInfo.ReceiptNo " +
                "AND ClassSalesHead.SecSeq = ClassPaymentInfo.SecSeq " +
                "AND ClassSalesHead.Status = 'R' " +
                "LEFT JOIN Authutication " +
                "ON Authutication.ReceiptNo = ClassSalesHead.ReceiptNo " +
                "AND Authutication.SecSeq = ClassSalesHead.SecSeq " +
                "AND Authutication.UG_MARK = 'Y' " +
                "AND REAUTH_MARK = 'Y' " +
                "LEFT JOIN Flight " +
                "ON ClassSalesHead.SecSeq = Flight.SecSeq " +
                "WHERE PayBy = 'Card'";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja == null) {
            ja = new JSONArray();
        }

        SalesDataJSONObject.put("RR", ja);

    }

    //產生卡囉嗦文字檔
    private void toTxt_Carousel()
    {
        //region 轉成文字檔TXT .RS
        JSONArray jaInventory = null;
        JSONArray jaStartQty = null;
        JSONArray jaTransfer = null;
        JSONArray jaDamage = null;

        FileWriter fw = null;
        BufferedWriter bw = null;
        String DepFlightNo = "";
        String FlightDate = "";
        String CarNo = "";

        String SQL = "SELECT DepFlightNo, FlightDate, CarNo, Inventory.ItemCode AS ItemCode, Flight.SecSeq AS SecSeq, " +
                "                SalesQty, AllParts.ItemPriceUS AS ItemPriceUS, StartQty, AdjustQty, " +
                "                EndQty, EGASCheckQty, EVACheckQty, " +
                "               '0' AS TransInQty, " +
            "                   '0' AS TransOutQty, " +
                "                 DamageQty, EGASDamageQty, EvaDamageQty " +
                "                FROM Inventory LEFT JOIN AllParts " +
                "                ON Inventory.ItemCode = AllParts.ItemCode " +
            "                   inner join Flight on Inventory.SecSeq = Flight.SecSeq" +
                "               where Inventory.SecSeq = '9'";


        try {
            jaInventory = _TSQL.SelectSQLJsonArrayByString(SQL);


            File sdFile = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String path = sdFile.getPath() + File.separator + "Upload";
            File dirFile = new File(path);

            ForceDeleteDir(dirFile); //刪除資料夾
            if (!dirFile.exists()) {//如果資料夾不存在
                dirFile.mkdir();//建立資料夾
            }



            for (int i = 0; i < jaInventory.length(); i++) {
                String SQL1 = "select DepFlightNo, FlightDate, CarNo,ItemCode,StartQty from Inventory inner join Flight on Inventory.SecSeq = Flight.SecSeq where Inventory.SecSeq = '0' and ItemCode='" + jaInventory.getJSONObject(i).getString("ItemCode") + "'";

                String SQL2 = "SELECT ItemCode, SUM(Qty) AS TransQty, TransferType " +
                        "FROM Transfer " +
                        "WHERE (TransferType = 'IN' OR TransferType = 'OUT') " +
                        "AND ItemCode = '" + jaInventory.getJSONObject(i).getString("ItemCode") + "' " +
                        "GROUP BY ItemCode, TransferType";

                String SQL3 = "select itemCode,sum(EVADamageQty) as EVADamageQty from Inventory where ItemCode='"+ jaInventory.getJSONObject(i).getString("ItemCode") +"'";

                jaStartQty = _TSQL.SelectSQLJsonArrayByString(SQL1);
                jaTransfer = _TSQL.SelectSQLJsonArrayByString(SQL2);
                jaDamage = _TSQL.SelectSQLJsonArrayByString(SQL3);

                if (jaTransfer != null) {
                    for (int j = 0; j < jaTransfer.length(); j++) {
                        if (jaTransfer.getJSONObject(j).getString("TransferType").equals("IN")) {
                            jaInventory.getJSONObject(i).put("TransInQty", jaTransfer.getJSONObject(j).getString("TransQty"));
                        } else {
                            jaInventory.getJSONObject(i).put("TransOutQty", jaTransfer.getJSONObject(j).getString("TransQty"));
                        }
                    }
                }

                if (jaDamage != null) {
                    jaInventory.getJSONObject(i).put("EVADamageQty", jaDamage.getJSONObject(0).getString("EVADamageQty"));
                }

                if(i == 0)
                {
                    DepFlightNo = PadRight(jaStartQty.getJSONObject(0).getString("DepFlightNo"), 7);
                    FlightDate = jaStartQty.getJSONObject(0).getString("FlightDate");
                    CarNo = PadRight(jaStartQty.getJSONObject(0).getString("CarNo"),7);

                    fw = new FileWriter(dirFile+"/"+FlightDate+"_"+FlightDate+"_"+CarNo+".RS", false);
                    bw = new BufferedWriter(fw); //將BufferedWeiter與FileWrite物件做連結
                }

                String ItemCode = PadRight(jaInventory.getJSONObject(i).getString("ItemCode"),8);
                int needFullqty=jaStartQty.getJSONObject(0).getInt("StartQty")
                        -jaInventory.getJSONObject(i).getInt("EVACheckQty")
                        +jaInventory.getJSONObject(i).getInt("TransInQty")
                        -jaInventory.getJSONObject(i).getInt("TransOutQty");
                int EvaDamageQty= jaInventory.getJSONObject(i).getInt("EVADamageQty");
                if(needFullqty >0 || EvaDamageQty >0){
                    bw.write("XXXX" + DepFlightNo + FlightDate + CarNo + "  " + ItemCode + "999"
                            + String.format("%04d",needFullqty)
                            + String.format("%03d",EvaDamageQty)+"\r\n");
                   // bw.newLine();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //endregion
    }



    // 5. RS 文字檔 (各航段商品庫存數量列表)
    private void toJSON_RS(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "RS":[{
//                        "DepFlightNo":"",
//                        "FlightDate":"",
//                        "CarNo":"",
//                        "ItemCode":"",
//                        "SecSeq":"",
//                        "SalesQty":"",
//                        "ItemPriceUS":"",
//                        "StartQty":"",
//                        "AdjustQty":"",
//                        "EndQty":"",
//                        "EGASCheckQty":"",
//                        "EVACheckQty":"",
//                        "TransInQty":"",
//                        "TransOutQty":"",
//                        "PreorderNo":"",
//                        "DamageQty":"",
//                        "EgasDamageQty":"",
//                        "EvaDamageQty":""
//            }]
        //endregion

        // 取得所有Item 含VIP & Preorder
        String SQL = "SELECT DepFlightNo, FlightDate, CarNo, Inventory.ItemCode AS ItemCode, Flight.SecSeq AS SecSeq, " +
                "SalesQty, AllParts.ItemPriceUS AS ItemPriceUS, StartQty, abs(AdjustQty) as AdjustQty, " +
                "EndQty, EGASCheckQty, EVACheckQty, 0 AS TransInQty, 0 AS TransOutQty, " +
                "'' AS PreorderNo, DamageQty, EGASDamageQty, EvaDamageQty " +
                "FROM Inventory LEFT JOIN AllParts " +
                "ON Inventory.ItemCode = AllParts.ItemCode " +
                "LEFT JOIN Flight " +
                "ON Inventory.SecSeq = Flight.SecSeq " +
                "UNION ALL " +
                "SELECT DepFlightNo, FlightDate, CarNo, ItemCode, Flight.SecSeq AS SecSeq, " +
                "SalesQty, SalesPrice AS ItemPriceUS, SalesQty AS StartQty,  '0' AS AdjustQty, " +
                "(CASE WHEN SaleFlag = 'S' THEN 0 ELSE SalesQty END) AS EndQty, " +
                "(CASE WHEN EGASSaleFlag = 'S' THEN 0 ELSE SalesQty END) AS EGASCheckQty, " +
                "(CASE WHEN EVASaleFlag = 'S' THEN 0 ELSE SalesQty END) AS EVACheckQty, " +
                "0 AS TransInQty, 0 AS TransOutQty,  " +
                "PreorderHead.PreorderNo, 0 AS DamageQty, 0 AS EGASDamageQty, 0 AS EvaDamageQty " +
                "FROM PreorderDetail LEFT JOIN PreorderHead " +
                "ON PreorderDetail.PreorderNo= PreorderHead.PreorderNO " +
                "LEFT JOIN Flight " +
                "ON PreorderHead.SecSeq = Flight.SecSeq " +
                "ORDER BY Flight.SecSeq, PreorderNO";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja != null) {
            for (int i = 0; i < ja.length(); i++) {

                //若是免稅品，則計算轉出轉入數量
                if (ja.getJSONObject(i).getString("PreorderNo").length() == 0) {

                    SQL = "SELECT ItemCode, SUM(Qty) AS TransQty, TransferType " +
                            "FROM Transfer " +
                            "WHERE (TransferType = 'IN' OR TransferType = 'OUT') " +
                            "AND ItemCode = '" + ja.getJSONObject(i).getString("ItemCode") +
                            "' AND SecSeq = '" + ja.getJSONObject(i).getString("SecSeq") +
                            "' GROUP BY ItemCode, TransferType";
                    JSONArray jaTransfer = _TSQL.SelectSQLJsonArrayByString(SQL);

                    if (jaTransfer != null) {
                        for (int j = 0; j < jaTransfer.length(); j++) {
                            if (jaTransfer.getJSONObject(j).getString("TransferType").equals("IN")) {
                                ja.getJSONObject(i).put("TransInQty", jaTransfer.getJSONObject(j).getString("TransQty"));
                            } else {
                                ja.getJSONObject(i).put("TransOutQty", jaTransfer.getJSONObject(j).getString("TransQty"));
                            }
                        }
                    }
                }
            }

        } else {
            ja = new JSONArray();
        }



        SalesDataJSONObject.put("RS", ja);

    }

    public  boolean ForceDeleteDir(File dir_target) {
        if (dir_target.isDirectory() && dir_target.exists()) {
            String[] fileList = dir_target.list();

            for (int i = 0; i < fileList.length; i++) {
                String sFile = dir_target.getPath() + File.separator + fileList[i];
                File tmp = new File(sFile);
                if (tmp.isFile()){
                    tmp.delete();
                }
                if (tmp.isDirectory()){
                    ForceDeleteDir(new File(sFile));
                }
            }
            dir_target.delete();
        } else {
            return false;
        }
        return true;
    }

    public static String PadRight(String str, int strLength) {
        int strLen =str.length();
        if (strLen <strLength) {
            while (strLen< strLength) {
                StringBuffer sb = new StringBuffer();
//                sb.append("0").append(str);//左補0
                sb.append(str).append(" ");//右補空 
                str= sb.toString();
                strLen= str.length();
            }
        }

        return str;
    }

    // 6. RD 文字檔 (機敏資料加密版本) (取得該航班有折扣的銷售商品價格)
    private void toJSON_RD(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "RD":[{
//                        "FlightDate":"",
//                        "DepFlightNo":"",
//                        "SecSeq":"",
//                        "CarNo":"",
//                        "CrewID":"",
//                        "VipNo":"",
//                        "VipType":"",
//                        "ItemCode":"",
//                        "SalesQty":"",
//                        "SalesTotalPrice":"",
//                        "SalesPrice":"",
//                        "PreorderNo":""
//            }]
        //endregion

        String SQL = "SELECT SalesHead.ReceiptNo, FlightDate, DepFlightNo, SalesHead.SecSeq AS SecSeq, CarNo, CrewID, " +
                "VipNo, VipType, ItemCode, SalesQty, (SalesQty * SalesPrice) AS SalesTotalPrice, " +
                "SalesPrice, PreorderNo " +
                "FROM SalesHead LEFT JOIN SalesDetail " +
                "ON SalesHead.SecSeq= SalesDetail.SecSeq " +
                "AND SalesHead.ReceiptNo = SalesDetail.ReceiptNo " +
                "LEFT JOIN Flight " +
                "ON SalesHead.SecSeq = Flight.SecSeq " +
                "WHERE SalesHead.ReceiptNo IN " +
                "(SELECT DISTINCT(ReceiptNo) FROM SalesDetail WHERE VipType <> ' ' and SalesHead.Status <>'R'  ) " +
                "ORDER BY SalesHead.SecSeq, SalesHead.ReceiptNo";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja == null) {
            ja = new JSONArray();
        }else {
            for (int i = 0; i < ja.length(); i++) {

                //取得各航段款項細節
                SQL = "SELECT ReceiptNo, Currency " +
                        "FROM PaymentInfo " +
                        "WHERE ReceiptNo = '" + ja.getJSONObject(i).getString("ReceiptNo") + "'";
                JSONArray jaPaymentSummary = _TSQL.SelectSQLJsonArray(SQL);
                boolean USDtag = false;
                if (jaPaymentSummary != null) {
                    for (int j = 0; j < jaPaymentSummary.length(); j++) {
                        if (jaPaymentSummary.getJSONObject(j).getString("Currency").toUpperCase().equals("USD")) {
                            USDtag = true;
                        } else {
                            USDtag = false;
                            break;
                        }
                    }
                }
                //皆使用美金付款
                if (USDtag) {
                    String SalesQty = ja.getJSONObject(i).getString("SalesQty");
                    String SalesPrice = ja.getJSONObject(i).getString("SalesPrice");

                    //四捨五入
                    double RoundPrice = Math.round(Double.parseDouble(SalesPrice));
                    String RoundTotalPrice = String.valueOf( new BigDecimal(Integer.parseInt(SalesQty) * RoundPrice).stripTrailingZeros());
                    ja.getJSONObject(i).put("SalesTotalPrice", RoundTotalPrice);
                    ja.getJSONObject(i).put("SalesPrice", RoundPrice);
                }

            }
        }

        SalesDataJSONObject.put("RD", ja);

    }

    // 7. RC 文字檔 (該航班的信用卡刷卡交易明細，已退貨傳0)
    private void toJSON_RC(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "RC":
//            [{
//                    "FlightDate":"",
//                    "DepFlightNo":"",
//                    "SecSeq":"",
//                    "CrewID":"",
//                    "CardType":"",
//                    "CardNo":"",
//                    "ReceiptNo":"",
//                    "CardName":"",
//                    "CardDate":"",
//                    "USDAmount":"",
//                    "TWDAmount":"",
//                    "CarNo":"",
//                    "PreorderNo":"",
//                    "UpperLimitType":"",
//                    "UpperLimitNo":""
//            }]
        //endregion

        String SQL = "SELECT FlightDate, DepFlightNo, PaymentInfo.SecSeq AS SecSeq, CrewID, " +
                "CardType, CardNo, PaymentInfo.ReceiptNo AS ReceiptNo, CardName, CardDate, " +
                "(CASE WHEN Currency = 'USD' THEN SUM(Amount) ELSE 0 END) AS USDAmount,  " +
                "(CASE WHEN Currency = 'TWD' THEN SUM(Amount) ELSE 0 END) AS TWDAmount, " +
                "CarNo, SalesHead.PreorderNo AS PreorderNo, UpperLimitType, UpperLimitNo " +
                "FROM PaymentInfo LEFT JOIN SalesHead " +
                "ON PaymentInfo.SecSeq = SalesHead.SecSeq " +
                "AND PaymentInfo.ReceiptNo = SalesHead.ReceiptNo " +
                "LEFT JOIN Flight " +
                "ON PaymentInfo.SecSeq = Flight.SecSeq " +
                "WHERE PayBy = 'Card' " +
                "GROUP BY PaymentInfo.SecSeq, PaymentInfo.ReceiptNo, CardNo";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja == null) {
            ja = new JSONArray();
        }

        SalesDataJSONObject.put("RC", ja);

    }

    // 8. RZ 文字檔 (地勤清點人員清單) (抓最後航段的最後清點人員)
    private void toJSON_RZ(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "RZ":
//            [{
//                        "DepFlightDate":"",
//                        "FlightNo":"",
//                        "SecSeq":"",
//                        "CarNo":"",
//                        "EGASID":"",
//                        "EVAID":""
//            }]
        //endregion

        String SQL = "SELECT FlightDate, DepFlightNo, Adjust.SecSeq AS SecSeq, CarNo, " +
                "(CASE CrewType WHEN 'EGAS' THEN Adjust.CrewID ELSE '' END) AS EGASID, " +
                "(CASE CrewType WHEN 'EVA' THEN Adjust.CrewID ELSE '' END) AS EVAID " +
                "FROM Adjust LEFT JOIN Flight " +
                "ON Adjust.SecSeq = Flight.SecSeq " +
                "WHERE CrewType = 'EGAS' OR CrewType = 'EVA' ";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja != null) {
            for (int i = 0; i < ja.length(); i++) {

                if (i >= 1) {

                    //SecSeq複寫回第一列
                    ja.getJSONObject(0).put("SecSeq", ja.getJSONObject(i).getString("SecSeq"));

                    //EGAS ID複寫回第一列
                    if (ja.getJSONObject(i).getString("EGASID").length() > 0) {
                        ja.getJSONObject(0).put("EGASID", ja.getJSONObject(i).getString("EGASID"));
                    }

                    //EVA ID複寫回第一列
                    if (ja.getJSONObject(i).getString("EVAID").length() > 0) {
                        ja.getJSONObject(0).put("EVAID", ja.getJSONObject(i).getString("EVAID"));
                    }

                    //刪除此列
                    ja.remove(i);

                    //減少i
                    i--;
                }
            }

        } else {
            ja = new JSONArray();
        }

        SalesDataJSONObject.put("RZ", ja);

    }

    // 9. RA 文字檔 (各航段的收受金額彙總)
    private void toJSON_RA(JSONObject SalesDataJSONObject) throws Exception {

        try {
            //region JSON Example
//            "RA":
//            [{
//                        "FlightDate":"",
//                        "DepFlightNo":"",
//                        "SecSeq":"",
//                        "CarNo":"",
//                        "CrewID":"",
//                        "CARD_USD":"",
//                        "CARD_TWD":"",
//                        "CASH_USD":"",
//                        "CASH_TWD":"",
//                        "CASH_HKD":"",
//                        "CASH_JPY":"",
//                        "CASH_SGD":"",
//                        "CASH_GBP":"",
//                        "CASH_EUR":"",
//                        "CASH_CNY":"",
//                        "COUPON_SC":"",
//                        "COUPON_DC":""
//            }]
            //endregion

            long Card_USD, Card_TWD, Cash_USD, Cash_TWD, Cash_HKD, Cash_JPY, Cash_SGD, Cash_GBP, Cash_EUR, Cash_CNY, COUPON_SC, COUPON_DC;

            String SQL = "SELECT FlightDate, DepFlightNo, SecSeq, CarNo, CrewID " +
                    "FROM Flight";
            JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

            if (ja != null) {
                for (int i = 0; i < ja.length(); i++) {

                    //清空變數
                    Card_USD = 0;
                    Card_TWD = 0;
                    Cash_USD = 0;
                    Cash_TWD = 0;
                    Cash_HKD = 0;
                    Cash_JPY = 0;
                    Cash_SGD = 0;
                    Cash_GBP = 0;
                    Cash_EUR = 0;
                    Cash_CNY = 0;
                    COUPON_SC = 0;
                    COUPON_DC = 0;

                    //取得各航段款項細節
                    SQL = "SELECT SecSeq, ReceiptNo, PayBy, Currency, SUM(Amount) AS Amount,Status " +
                            "FROM PaymentInfo " +
                            "WHERE SecSeq = '" + ja.getJSONObject(i).getString("SecSeq") +
                            "' GROUP BY SecSeq,ReceiptNo,PayBy,Currency,Status";
                    JSONArray jaPaymentSummary = _TSQL.SelectSQLJsonArray(SQL);

                    if (jaPaymentSummary != null) {
                        for (int j = 0; j < jaPaymentSummary.length(); j++) {
                            if( jaPaymentSummary.getJSONObject(j).getString("Status").equals("R")){
                                int refoundValue=0-jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                jaPaymentSummary.getJSONObject(j).put("Amount",refoundValue);
                            }


                            switch (jaPaymentSummary.getJSONObject(j).getString("PayBy").toUpperCase()) {
                                case "CASH":
                                    switch (jaPaymentSummary.getJSONObject(j).getString("Currency").toUpperCase()) {
                                        case "USD":
                                            Cash_USD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "TWD":
                                            Cash_TWD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "HKD":
                                            Cash_HKD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "JPY":
                                            Cash_JPY += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "SGD":
                                            Cash_SGD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "GBP":
                                            Cash_GBP += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "EUR":
                                            Cash_EUR += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "CNY":
                                            Cash_CNY += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                    }
                                    break;
                                case "CARD":
                                    switch (jaPaymentSummary.getJSONObject(j).getString("Currency").toUpperCase()) {
                                        case "USD":
                                            Card_USD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "TWD":
                                            Card_TWD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                    }
                                    break;
                                case "SC":
                                    COUPON_SC += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                    break;
                                case "DC":
                                    COUPON_DC += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                    break;
                                case "CHANGE":
                                    switch (jaPaymentSummary.getJSONObject(j).getString("Currency").toUpperCase()) {
                                        case "USD":
                                            Cash_USD -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "TWD":
                                            Cash_TWD -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "HKD":
                                            Cash_HKD -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "JPY":
                                            Cash_JPY -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "SGD":
                                            Cash_SGD -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "GBP":
                                            Cash_GBP -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "EUR":
                                            Cash_EUR -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                        case "CNY":
                                            Cash_CNY -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                            break;
                                    }
                                    break;
                            }
                        }
                    }

                    //未開櫃航段 組員補六個X
                    if (ja.getJSONObject(i).getString("CrewID").equals("")) {
                        ja.getJSONObject(i).put("CrewID", "XXXXXX");
                    }

                    //補齊JSON
                    ja.getJSONObject(i).put("CARD_USD", String.valueOf(Card_USD));
                    ja.getJSONObject(i).put("CARD_TWD", String.valueOf(Card_TWD));
                    ja.getJSONObject(i).put("CASH_USD", String.valueOf(Cash_USD));
                    ja.getJSONObject(i).put("CASH_TWD", String.valueOf(Cash_TWD));
                    ja.getJSONObject(i).put("CASH_HKD", String.valueOf(Cash_HKD));
                    ja.getJSONObject(i).put("CASH_JPY", String.valueOf(Cash_JPY));
                    ja.getJSONObject(i).put("CASH_SGD", String.valueOf(Cash_SGD));
                    ja.getJSONObject(i).put("CASH_GBP", String.valueOf(Cash_GBP));
                    ja.getJSONObject(i).put("CASH_EUR", String.valueOf(Cash_EUR));
                    ja.getJSONObject(i).put("CASH_CNY", String.valueOf(Cash_CNY));
                    ja.getJSONObject(i).put("COUPON_SC", String.valueOf(COUPON_SC));
                    ja.getJSONObject(i).put("COUPON_DC", String.valueOf(COUPON_DC));
                }

            } else {
                ja = new JSONArray();
            }

            SalesDataJSONObject.put("RA", ja);

        } catch (Exception ex) {
            throw ex;
        }
    }

    // 10. RV1 文字檔 (航班內各交易金流，含IFE資訊)
    private void toJSON_RV1(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "RV1":
//            [{
//                        "FlightDate":"",
//                        "DepFlightNo":"",
//                        "SecSeq":"",
//                        "CarNo":"",
//                        "ReceiptNo":"",
//                        "IFEOrderNo":"",
//                        "OrderType":"",
//                        "SeatNo":"",
//                        "PreorderNo":"",
//                        "CARD_USD":"",
//                        "CARD_TWD":"",
//                        "CASH_USD":"",
//                        "CASH_TWD":"",
//                        "CASH_HKD":"",
//                        "CASH_JPY":"",
//                        "CASH_SGD":"",
//                        "CASH_GBP":"",
//                        "CASH_EUR":"",
//                        "CASH_CNY":"",
//                        "COUPON_SC":"",
//                        "COUPON_DC":""
//            }]
        //endregion

        long Card_USD, Card_TWD, Cash_USD, Cash_TWD, Cash_HKD, Cash_JPY, Cash_SGD, Cash_GBP, Cash_EUR, Cash_CNY, COUPON_SC, COUPON_DC;

        String SQL = "SELECT FlightDate, DepFlightNo, SalesHead.SecSeq, CarNo, ReceiptNo, IFEOrderNo, OrderType, SeatNo, PreorderNo " +
                "FROM SalesHead LEFT JOIN Flight " +
                "ON SalesHead.SecSeq = Flight.SecSeq";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja != null) {
            for (int i = 0; i < ja.length(); i++) {

                //清空變數
                Card_USD = 0;
                Card_TWD = 0;
                Cash_USD = 0;
                Cash_TWD = 0;
                Cash_HKD = 0;
                Cash_JPY = 0;
                Cash_SGD = 0;
                Cash_GBP = 0;
                Cash_EUR = 0;
                Cash_CNY = 0;
                COUPON_SC = 0;
                COUPON_DC = 0;

                //取得各航段款項細節
                SQL = "SELECT ReceiptNo, PayBy, Currency, SUM(Amount) AS Amount " +
                        "FROM PaymentInfo " +
                        "WHERE ReceiptNo = '" + ja.getJSONObject(i).getString("ReceiptNo") +
                        "' GROUP BY ReceiptNo,PayBy,Currency";
                JSONArray jaPaymentSummary = _TSQL.SelectSQLJsonArray(SQL);

                if (jaPaymentSummary != null) {
                    for (int j = 0; j < jaPaymentSummary.length(); j++) {

                        switch (jaPaymentSummary.getJSONObject(j).getString("PayBy").toUpperCase()) {
                            case "CASH":
                                switch (jaPaymentSummary.getJSONObject(j).getString("Currency").toUpperCase()) {
                                    case "USD":
                                        Cash_USD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "TWD":
                                        Cash_TWD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "HKD":
                                        Cash_HKD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "JPY":
                                        Cash_JPY += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "SGD":
                                        Cash_SGD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "GBP":
                                        Cash_GBP += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "EUR":
                                        Cash_EUR += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "CNY":
                                        Cash_CNY += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                }
                                break;
                            case "CARD":
                                switch (jaPaymentSummary.getJSONObject(j).getString("Currency").toUpperCase()) {
                                    case "USD":
                                        Card_USD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "TWD":
                                        Card_TWD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                }
                                break;
                            case "SC":
                                COUPON_SC += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                break;
                            case "DC":
                                COUPON_DC += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                break;
                            case "CHANGE":
                                switch (jaPaymentSummary.getJSONObject(j).getString("Currency").toUpperCase()) {
                                    case "USD":
                                        Cash_USD -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "TWD":
                                        Cash_TWD -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "HKD":
                                        Cash_HKD -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "JPY":
                                        Cash_JPY -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "SGD":
                                        Cash_SGD -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "GBP":
                                        Cash_GBP -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "EUR":
                                        Cash_EUR -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "CNY":
                                        Cash_CNY -= jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                }
                                break;
                        }
                    }
                }

                //補齊JSON
                ja.getJSONObject(i).put("CARD_USD", String.valueOf(Card_USD));
                ja.getJSONObject(i).put("CARD_TWD", String.valueOf(Card_TWD));
                ja.getJSONObject(i).put("CASH_USD", String.valueOf(Cash_USD));
                ja.getJSONObject(i).put("CASH_TWD", String.valueOf(Cash_TWD));
                ja.getJSONObject(i).put("CASH_HKD", String.valueOf(Cash_HKD));
                ja.getJSONObject(i).put("CASH_JPY", String.valueOf(Cash_JPY));
                ja.getJSONObject(i).put("CASH_SGD", String.valueOf(Cash_SGD));
                ja.getJSONObject(i).put("CASH_GBP", String.valueOf(Cash_GBP));
                ja.getJSONObject(i).put("CASH_EUR", String.valueOf(Cash_EUR));
                ja.getJSONObject(i).put("CASH_CNY", String.valueOf(Cash_CNY));
                ja.getJSONObject(i).put("COUPON_SC", String.valueOf(COUPON_SC));
                ja.getJSONObject(i).put("COUPON_DC", String.valueOf(COUPON_DC));
            }

        } else {
            ja = new JSONArray();
        }

        SalesDataJSONObject.put("RV1", ja);

    }

    // 12. RV3 文字檔 (取得每筆單據內，每個item的銷售總價/數量/單價)
    private void toJSON_RV3(JSONObject SalesDataJSONObject) throws Exception {

        try {
            //region JSON Example
//            "RV3":[{
//                        "FlightDate":"",
//                        "DepFlightNo":"",
//                        "CarNo":"",
//                        "SecSeq":"",
//                        "ReceiptNo":"",
//                        "ItemCode":"",
//                        "SalesTotalPrice ":"",
//                        "SalesQty":"",
//                        "SalesPrice":""
//            }]
            //endregion

            String SQL = "SELECT FlightDate, DepFlightNo, CarNo, SalesDetail.SecSeq AS SecSeq, ReceiptNo, " +
                    "ItemCode, (SalesQty * SalesPrice) AS SalesTotalPrice, SalesQty, SalesPrice " +
                    "FROM SalesDetail LEFT JOIN Flight " +
                    "ON SalesDetail.SecSeq = Flight.SecSeq " +
                    "WHERE Status = 'S' " +
                    "ORDER BY ReceiptNo";
            JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

            if (ja == null) {
                ja = new JSONArray();
            } else {
                for (int i = 0; i < ja.length(); i++) {

                    //取得各航段款項細節
                    SQL = "SELECT ReceiptNo, Currency " +
                            "FROM PaymentInfo " +
                            "WHERE ReceiptNo = '" + ja.getJSONObject(i).getString("ReceiptNo") + "'";
                    JSONArray jaPaymentSummary = _TSQL.SelectSQLJsonArray(SQL);
                    boolean USDtag = false;
                    if (jaPaymentSummary != null) {
                        for (int j = 0; j < jaPaymentSummary.length(); j++) {
                            if (jaPaymentSummary.getJSONObject(j).getString("Currency").toUpperCase().equals("USD")) {
                                USDtag = true;
                            } else {
                                USDtag = false;
                                break;
                            }
                        }
                    }
                    //皆使用美金付款
                    if (USDtag) {
                        String SalesQty = ja.getJSONObject(i).getString("SalesQty");
                        String SalesPrice = ja.getJSONObject(i).getString("SalesPrice");

                        //四捨五入
                        double RoundPrice = Math.round(Double.parseDouble(SalesPrice));

                        String RoundTotalPrice = String.valueOf( new BigDecimal(Integer.parseInt(SalesQty) * RoundPrice).stripTrailingZeros());
                        ja.getJSONObject(i).put("SalesTotalPrice", RoundTotalPrice);
                    }

                }
            }

            SalesDataJSONObject.put("RV3", ja);

        } catch (Exception ex) {
            throw ex;
        }
    }

    // 13. RC1 文字檔 (升艙等信用卡付款紀錄，已退貨傳0)
    private void toJSON_RC1(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "RC1":
//            [{
//                        "FlightDate":"",
//                        "DepFlightNo":"",
//                        "SecSeq":"",
//                        "CrewID":"",
//                        "CardType":"",
//                        "CardNo":"",
//                        "ReceiptNo":"",
//                        "CardName":"",
//                        "CardDate":"",
//                        "USDAmount":"",
//                        "TWDAmount":"",
//                        "CarNo":"",
//                        "UpperLimitType":"",
//                        "UpperLimitNo":""
//            }]
        //endregion

        String SQL = "SELECT FlightDate, DepFlightNo, ClassPaymentInfo.SecSeq AS SecSeq, CrewID, " +
                "CardType, CardNo, ReceiptNo, CardName, CardDate, " +
                "(CASE WHEN Currency = 'USD' THEN SUM(Amount) ELSE 0 END) AS USDAmount,  " +
                "(CASE WHEN Currency = 'TWD' THEN SUM(Amount) ELSE 0 END) AS TWDAmount, " +
                "CarNo, '' AS UpperLimitType, '' AS UpperLimitNo " +
                "FROM ClassPaymentInfo LEFT JOIN Flight " +
                "ON ClassPaymentInfo.SecSeq = Flight.SecSeq " +
                "WHERE PayBy = 'Card' " +
                "GROUP BY ClassPaymentInfo.SecSeq, ReceiptNo, CardNo";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja == null) {
            ja = new JSONArray();
        }

        SalesDataJSONObject.put("RC1", ja);

    }

    // 14. RA1 文字檔 (每個航段的升艙等 現金/信用卡，美金/台幣收受總額)
    private void toJSON_RA1(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "RA1":
//            [{
//                        "FlightDate":"",
//                        "DepFlightNo":"",
//                        "SecSeq":"",
//                        "CarNo":"",
//                        "CrewID":"",
//                        "CARD_USD":"",
//                        "CARD_TWD":"",
//                        "CASH_USD":"",
//                        "CASH_TWD":""
//            }]
        //endregion

        long Card_USD, Card_TWD, Cash_USD, Cash_TWD;

        String SQL = "SELECT FlightDate, DepFlightNo, SecSeq, CarNo, CrewID " +
                "FROM Flight";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja != null) {
            for (int i = 0; i < ja.length(); i++) {

                //清空變數
                Card_USD = 0;
                Card_TWD = 0;
                Cash_USD = 0;
                Cash_TWD = 0;

                //取得各航段款項細節
                SQL = "SELECT SecSeq, ReceiptNo, PayBy, Currency, SUM(Amount) AS Amount " +
                        "FROM ClassPaymentInfo " +
                        "WHERE SecSeq = '" + ja.getJSONObject(i).getString("SecSeq") +
                        "' GROUP BY SecSeq,ReceiptNo,PayBy,Currency";
                JSONArray jaPaymentSummary = _TSQL.SelectSQLJsonArrayByString(SQL);

                if (jaPaymentSummary != null) {
                    for (int j = 0; j < jaPaymentSummary.length(); j++) {

                        switch (jaPaymentSummary.getJSONObject(j).getString("PayBy").toUpperCase()) {
                            case "CASH":
                                switch (jaPaymentSummary.getJSONObject(j).getString("Currency").toUpperCase()) {
                                    case "USD":
                                        Cash_USD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "TWD":
                                        Cash_TWD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                }
                                break;
                            case "CARD":
                                switch (jaPaymentSummary.getJSONObject(j).getString("Currency").toUpperCase()) {
                                    case "USD":
                                        Card_USD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "TWD":
                                        Card_TWD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                }
                                break;
                        }
                    }
                }

                //未開櫃航段 組員補六個X
                if (ja.getJSONObject(i).getString("CrewID").equals("")) {
                    ja.getJSONObject(i).put("CrewID", "XXXXXX");
                }

                //補齊JSON
                ja.getJSONObject(i).put("CARD_USD", String.valueOf(Card_USD));
                ja.getJSONObject(i).put("CARD_TWD", String.valueOf(Card_TWD));
                ja.getJSONObject(i).put("CASH_USD", String.valueOf(Cash_USD));
                ja.getJSONObject(i).put("CASH_TWD", String.valueOf(Cash_TWD));
            }

        } else {
            ja = new JSONArray();
        }

        SalesDataJSONObject.put("RA1", ja);

    }

    // 15. LOG 文字檔
    private void toJSON_LOG(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "LOG":
//            [{
//                    "SystemDate":"",
//                    "SecSeq":"",
//                    "LogType":"",
//                    "LogText":""
//            }]
        //endregion

        String SQL = "SELECT SystemDate, SecSeq, LogType, SUBSTR((OperationName || ' - ' || FunctionName || ' - ' || LogText), 0, 1000) AS LogText " +
                "FROM SystemLog ";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja == null) {
            ja = new JSONArray();
        }

        SalesDataJSONObject.put("LOG", ja);

    }

    // 16. PODS 文字檔 (取出地面預訂機上取貨的訂單狀態 VIP Paid and Preorder，不包含VIP Sale)
    private void toJSON_PODS(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "PODS":
//            [{
//                        "PreorderNo":"",
//                        "FlightDate":"",
//                        "DepFlightNo":"",
//                        "CarNo":"",
//                        "SecSeq":"",
//                        "CrewID":"",
//                        "ReceiptNo":"",
//                        "VerifyType":"",
//                        "VerifyNo":"",
//                        "SaleFlag":""
//            }]
        //endregion

        String SQL = "SELECT PreorderHead.PreorderNo, FlightDate, DepFlightNo, CarNo, PreorderHead.SecSeq AS SecSeq, CrewID, " +
                "ReceiptNo, VerifyType, " +
                "(CASE VerifyType WHEN 'C' THEN CardNo WHEN 'P' THEN TravelDocument ELSE '' END) AS VerifyNo, " +
                "'1' AS SaleFlag " +
                "FROM PreorderHead LEFT JOIN PreorderSalesHead " +
                "ON PreorderHead.PreorderNO = PreorderSalesHead.PreorderNO " +
                "LEFT JOIN Flight " +
                "ON PreorderHead.SecSeq = Flight.SecSeq " +
                "WHERE PreorderHead.EVASaleFlag = 'S' " +
                "AND PreorderSalesHead.SaleFlag = 'S' ";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja == null) {
            ja = new JSONArray();
        }

        SalesDataJSONObject.put("PODS", ja);

    }

    // 17. PODR 文字檔 (取出地面預訂機上退貨的訂單狀態 VIP Paid and Preorder，不包含VIP Sale)
    private void toJSON_PODR(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "PODS":
//            [{
//                        "PreorderNo":"",
//                        "FlightDate":"",
//                        "DepFlightNo":"",
//                        "CarNo":"",
//                        "SecSeq":"",
//                        "CrewID":"",
//                        "ReceiptNo":"",
//                        "VerifyType":"",
//            }]
        //endregion

        String SQL = "SELECT PreorderHead.PreorderNo, FlightDate, DepFlightNo, CarNo, PreorderHead.SecSeq AS SecSeq, CrewID, " +
                "ReceiptNo, VerifyType " +
                "FROM PreorderHead LEFT JOIN PreorderSalesHead " +
                "ON PreorderHead.PreorderNO = PreorderSalesHead.PreorderNO " +
                "LEFT JOIN Flight " +
                "ON PreorderHead.SecSeq = Flight.SecSeq " +
                "WHERE PreorderHead.EVASaleFlag = 'R' " +
                "AND PreorderSalesHead.SaleFlag = 'R' ";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja == null) {
            ja = new JSONArray();
        }

        SalesDataJSONObject.put("PODR", ja);

    }

    // 18. COU 文字檔 (取得DC Coupon消費紀錄)
    private void toJSON_COU(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "COU":
//            [{
//                        "FlightDate":"",
//                        "DepFlightNo":"",
//                        "CarNo":"",
//                        "SecSeq":"",
//                        "ReceiptNo ":"",
//                        "CouponNo":"",
//                        "Amount":""
//            }]
        //endregion

        //////////////////////////////////////////////////////////////////////////
        long COUPON_DC;
        String ReceiptNo,CouponNo;
        String SQL = "SELECT FlightDate, DepFlightNo, SecSeq, CarNo " +
                "FROM Flight";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja != null) {
            for (int i = 0; i < ja.length(); i++) {

                //清空變數
                COUPON_DC = 0;
                ReceiptNo="";
                CouponNo="";

                //取得各航段款項細節DC
                SQL = "SELECT SecSeq, ReceiptNo, PayBy, Currency, CouponNo , SUM(Amount) AS Amount,Status " +
                        "FROM PaymentInfo " +
                        "WHERE SecSeq = '" + ja.getJSONObject(i).getString("SecSeq") +
                        "' GROUP BY SecSeq,ReceiptNo,PayBy,Currency,Status";
                JSONArray jaPaymentSummary = _TSQL.SelectSQLJsonArray(SQL);

                if (jaPaymentSummary != null) {
                    for (int j = 0; j < jaPaymentSummary.length(); j++) {
                        if( jaPaymentSummary.getJSONObject(j).getString("Status").equals("R")){
                            int refoundValue=0-jaPaymentSummary.getJSONObject(j).getInt("Amount");
                            jaPaymentSummary.getJSONObject(j).put("Amount",refoundValue);
                        }

                        switch (jaPaymentSummary.getJSONObject(j).getString("PayBy").toUpperCase()) {
                            case "DC":
                                COUPON_DC += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                ReceiptNo=jaPaymentSummary.getJSONObject(j).getString("ReceiptNo");
                                CouponNo=jaPaymentSummary.getJSONObject(j).getString("CouponNo");
                                break;
                        }
                    }
                }

                //補齊JSON
                ja.getJSONObject(i).put("ReceiptNo", ReceiptNo );
                ja.getJSONObject(i).put("CouponNo", CouponNo );
                ja.getJSONObject(i).put("Amount", String.valueOf(COUPON_DC));

            }

        } else {
            ja = new JSONArray();
        }

        SalesDataJSONObject.put("COU", ja);
        /////////////////////////////////////////////////////////////////////////

//        String SQL = "SELECT FlightDate, DepFlightNo, CarNo, PaymentInfo.SecSeq AS SecSeq, ReceiptNo, CouponNo, Amount,Status  " +
//                "FROM PaymentInfo LEFT JOIN Flight " +
//                "ON PaymentInfo.SecSeq = Flight.SecSeq " +
//                "WHERE PayBy = 'DC'"+
//                " GROUP BY  Flight.SecSeq,PaymentInfo.ReceiptNo,PayBy,Currency,Status ";
//        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);
//        int Amount = 0;
//        for (int i = 0; i < ja.length() ; i++) {
//            if(ja.getJSONObject(i).getString("Status").equals("R")){
//                int refoundValue=0-ja.getJSONObject(i).getInt("Amount");
//                ja.getJSONObject(i).put("Amount",refoundValue);
//            }
//            Amount += ja.getJSONObject(i).getInt("Amount");
//        }
//
//
//        if (ja == null) {
//            ja = new JSONArray();
//        }
//
//        SalesDataJSONObject.put("COU", ja);

    }

    // 19. RU1 文字檔 (航班內各升倉等交易金流)
    private void toJSON_RU1(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "RU1":
//            [{
//                    "FlightDate":"",
//                    "DepFlightNo":"",x
//                    "SecSeq":"",
//                    "CarNo":"",
//                    "CANo":"",
//                    "ReceiptNo":"",
//                    "CARD_USD":"",
//                    "CARD_TWD":"",
//                    "CASH_USD":"",
//                    "CASH_TWD":""
//            }]
        //endregion

        long Card_USD, Card_TWD, Cash_USD, Cash_TWD;

        String SQL = "SELECT FlightDate, DepFlightNo, CrewID AS CANo,ClassSalesHead.SecSeq AS SecSeq, CarNo, ReceiptNo " +
                "FROM ClassSalesHead LEFT JOIN Flight " +
                "ON ClassSalesHead.SecSeq = Flight.SecSeq";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja != null) {
            for (int i = 0; i < ja.length(); i++) {

                //清空變數
                Card_USD = 0;
                Card_TWD = 0;
                Cash_USD = 0;
                Cash_TWD = 0;

                //取得各航段款項細節
                SQL = "SELECT ReceiptNo, PayBy, Currency, SUM(Amount) AS Amount " +
                        "FROM ClassPaymentInfo " +
                        "WHERE ReceiptNo = '" + ja.getJSONObject(i).getString("ReceiptNo") +
                        "' GROUP BY ReceiptNo,PayBy,Currency";
                JSONArray jaPaymentSummary = _TSQL.SelectSQLJsonArray(SQL);

                if (jaPaymentSummary != null) {
                    for (int j = 0; j < jaPaymentSummary.length(); j++) {

                        switch (jaPaymentSummary.getJSONObject(j).getString("PayBy").toUpperCase()) {
                            case "CASH":
                                switch (jaPaymentSummary.getJSONObject(j).getString("Currency").toUpperCase()) {
                                    case "USD":
                                        Cash_USD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "TWD":
                                        Cash_TWD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                }
                                break;
                            case "CARD":
                                switch (jaPaymentSummary.getJSONObject(j).getString("Currency").toUpperCase()) {
                                    case "USD":
                                        Card_USD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                    case "TWD":
                                        Card_TWD += jaPaymentSummary.getJSONObject(j).getInt("Amount");
                                        break;
                                }
                                break;
                        }
                    }
                }

                //補齊JSON
                ja.getJSONObject(i).put("CARD_USD", String.valueOf(Card_USD));
                ja.getJSONObject(i).put("CARD_TWD", String.valueOf(Card_TWD));
                ja.getJSONObject(i).put("CASH_USD", String.valueOf(Cash_USD));
                ja.getJSONObject(i).put("CASH_TWD", String.valueOf(Cash_TWD));
            }

        } else {
            ja = new JSONArray();
        }

        SalesDataJSONObject.put("RU1", ja);

    }

    // 20. RU3 文字檔 (取得每筆升倉等單據內，每個item的銷售總價/數量/單價)
    private void toJSON_RU3(JSONObject SalesDataJSONObject) throws Exception {

        //region JSON Example
//            "RU3":[{
//                    "FlightDate":"",
//                    "DepFlightNo":"",
//                    "CarNo":"",
//                    "SecSeq":"",
//                    "ReceiptNo":"",
//                    "Infant":"",
//                    "OriginalClass":"",
//                    "NewClass":"",
//                    "SalesTotalPrice ":"",
//                    "SalesQty":"",
//                    "SalesPrice":""
//            }]
        //endregion

        String SQL = "SELECT FlightDate, DepFlightNo, CarNo, ClassSalesDetail.SecSeq AS SecSeq, ReceiptNo, " +
                "Infant, OriginalClass, NewClass, (SalesQty * SalesPrice) AS SalesTotalPrice, SalesQty, SalesPrice " +
                "FROM ClassSalesDetail LEFT JOIN Flight " +
                "ON ClassSalesDetail.SecSeq = Flight.SecSeq " +
                "WHERE Status = 'S' " +
                "ORDER BY ReceiptNo";
        JSONArray ja = _TSQL.SelectSQLJsonArrayByString(SQL);

        if (ja == null) {
            ja = new JSONArray();
        }

        SalesDataJSONObject.put("RU3", ja);

    }

    /////////////////////////////////////
    //
    //            Log 函式
    //
    /////////////////////////////////////

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


}
