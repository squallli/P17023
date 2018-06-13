package tw.com.regalscan.db02;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.PublicFunctions;
import tw.com.regalscan.evaground.ItemInfo;
import tw.com.regalscan.evaground.PreorderReceiptInfo;

/**
 * Upgrade to 公用函式規格書(V1.7) 20170601
 */

public class DBQuery {

    private static PublicFunctions pFunction;
    private static DBFunctions pFunctionHeidi;

    //JSON Object starts with a { and ends with a }
    // JSON Array starts with a [ and ends with a ]


    private static String getSecSeq() {
        String SecSeq = FlightData.SecSeq;

        if (SecSeq != null) {
            return SecSeq;
        } else {
            return "";
        }
    }


    /*----------------A. Flight Information--------*/


    /**
     * 1. 確認基本銷售資訊是否齊全
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @return BasicSalesInfo
     */
    public static BasicSalesInfo checkBasicSalesInfoIsReady(Context mContext, StringBuilder errMsg) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        BasicSalesInfo ret = null;
        try {
            json = pFunction.CheckBasicSalesInfoIsReady();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new BasicSalesInfo();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.PreorderCount = jsonArray.getJSONObject(0).getInt("PreorderCount");
                ret.VIPCount = jsonArray.getJSONObject(0).getInt("VIPCount");
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class BasicSalesInfo {

        public int PreorderCount = 0;
        public int VIPCount = 0;
    }


    /**
     * 2. 取得特定航段資訊
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param SecSeq   航段編號
     * @return FlightInfo
     */
    public static FlightInfo getFlightInfo(Context mContext, StringBuilder errMsg, String SecSeq) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        FlightInfo ret = null;

        try {
            json = pFunction.GetFlightInfo(SecSeq);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new FlightInfo();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.FlightNo = jsonArray.getJSONObject(0).getString("FlightNo");
                ret.SecSeq = jsonArray.getJSONObject(0).getString("SecSeq");
                ret.FlightDate = jsonArray.getJSONObject(0).getString("FlightDate");
                ret.DepStn = jsonArray.getJSONObject(0).getString("DepStn");
                ret.ArivStn = jsonArray.getJSONObject(0).getString("ArivStn");
                ret.CarNo = jsonArray.getJSONObject(0).getString("CarNo");
                ret.CrewID = jsonArray.getJSONObject(0).getString("CrewID"); //CA
                ret.PurserID = jsonArray.getJSONObject(0).getString("PurserID"); //CP
                ret.IFECatalogID = jsonArray.getJSONObject(0).getString("IFECatalogID");
                ret.IFETokenID = jsonArray.getJSONObject(0).getString("IFETokenID");
                ret.Mode = jsonArray.getJSONObject(0).getString("Mode");

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class FlightInfo {

        public String FlightNo = "";
        public String SecSeq = "";
        public String FlightDate = "";
        public String DepStn = "";
        public String ArivStn = "";
        public String CarNo = "";
        public String CrewID = ""; //CA ID
        public String PurserID = ""; //上次登入的CP ID
        public String IFECatalogID = "";
        public String IFETokenID = "";
        public String Mode = "";
    }


    /**
     * 3. 取得全部航段資訊
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @return FilghtInfoPack
     */
    public static FlightInfoPack getFlightInfo(Context mContext, StringBuilder errMsg) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        FlightInfoPack ret = null;

        try {
            json = pFunction.GetFlightInfo();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret = new FlightInfoPack();
                ret.flights = new FlightInfo[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.flights[i] = new FlightInfo();
                    ret.flights[i].FlightNo = jsonArray.getJSONObject(i).getString("FlightNo");
                    ret.flights[i].SecSeq = jsonArray.getJSONObject(i).getString("SecSeq");
                    ret.flights[i].FlightDate = jsonArray.getJSONObject(i).getString("FlightDate");
                    ret.flights[i].DepStn = jsonArray.getJSONObject(i).getString("DepStn");
                    ret.flights[i].ArivStn = jsonArray.getJSONObject(i).getString("ArivStn");
                    ret.flights[i].CarNo = jsonArray.getJSONObject(i).getString("CarNo");
                    ret.flights[i].CrewID = jsonArray.getJSONObject(i).getString("CrewID");
                    ret.flights[i].PurserID = jsonArray.getJSONObject(i).getString("PurserID");
                    ret.flights[i].IFECatalogID = jsonArray.getJSONObject(i).getString("IFECatalogID");
                    ret.flights[i].IFETokenID = jsonArray.getJSONObject(i).getString("IFETokenID");
                    ret.flights[i].Mode = jsonArray.getJSONObject(i).getString("Mode");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class FlightInfoPack {

        public FlightInfo[] flights;
    }


    /**
     * 4. 取得當前準備開櫃之航段資訊
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @return CurrentOpenFlightPack
     */
    public static CurrentOpenFlightPack getCurrentOpenFlightList(Context mContext, StringBuilder errMsg) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CurrentOpenFlightPack ret = null;

        try {
            json = pFunction.GetCurrentOpenFlightList();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CurrentOpenFlightPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.openFlights = new CurrentOpenFlight[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.openFlights[i] = new CurrentOpenFlight();
                    ret.openFlights[i].FlightNo = jsonArray.getJSONObject(i).getString("FlightNo");
                    ret.openFlights[i].SecSeq = jsonArray.getJSONObject(i).getString("SecSeq");
                    ret.openFlights[i].DepStn = jsonArray.getJSONObject(i).getString("DepStn");
                    ret.openFlights[i].ArivStn = jsonArray.getJSONObject(i).getString("ArivStn");
                    ret.openFlights[i].Status = jsonArray.getJSONObject(i).getString("Status");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class CurrentOpenFlight {
        // 當前準備開櫃之航段序號
        // 可依據航段狀態判斷是否為預設Open航段，或是可Reopen的航段
        // 也可以判斷是否目前是否為已開櫃狀態

        // Status : ”Open”   = 該航段目前為已開櫃狀態
        // Status : ”Closed” = 該航段目前為已關櫃狀態，若是最後一筆關櫃航段，則允許重新開櫃，如下例，則0航段允許重新開櫃
        // Status : ””       = 該航段目前為未開櫃狀態，若是最後一筆未開櫃航段，則為預設開櫃航段，如下例，則1為預設開櫃航段

        public String FlightNo = "";
        public String SecSeq = "";
        public String DepStn = "";
        public String ArivStn = "";
        public String Status = "";
    }

    public static class CurrentOpenFlightPack {

        public CurrentOpenFlight[] openFlights;
    }


    /**
     * 5. 取得特定組員資訊
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param CrewID   組員ID
     * @param CrewPw   組員密碼
     * @param CrewType 組員類別, C或P
     * @return CrewInfo
     */
    public static CrewInfo getGetCrewInfo(Context mContext, StringBuilder errMsg,
                                          String CrewID, String CrewPw, String CrewType) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CrewInfo ret = null;

        try {
            json = pFunction.GetCrewInfo(CrewID, CrewPw, CrewType);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CrewInfo();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.CrewID = jsonArray.getJSONObject(0).getString("CrewID");
                ret.CrewType = jsonArray.getJSONObject(0).getString("CrewType");
                ret.Name = jsonArray.getJSONObject(0).getString("Name");
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class CrewInfo {

        public String Password = "";
        public String CrewID = "";
        public String CrewType = "";
        public String Name = "";

        public CrewInfo() {

        }

        public CrewInfo(String cid, String ctype, String cname) {
            CrewID = cid;
            CrewType = ctype;
            Name = cname;
        }
    }


    /**
     * 6. 取得特定組員資訊
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param CrewID   組員ID
     * @param CrewPw   組員密碼
     * @return CrewInfo
     */
    public static CrewInfo getGetCrewInfo(Context mContext, StringBuilder errMsg,
                                          String CrewID, String CrewPw) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CrewInfo ret = null;

        try {
            json = pFunction.GetCrewInfo(CrewID, CrewPw);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CrewInfo();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.CrewID = jsonArray.getJSONObject(0).getString("CrewID");
                ret.CrewType = jsonArray.getJSONObject(0).getString("CrewType");
                ret.Name = jsonArray.getJSONObject(0).getString("Name");
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }


    /**
     * 7. 取得特定組員資訊
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param CrewID   組員ID
     * @return CrewInfo
     */
    public static CrewInfo getGetCrewInfo(Context mContext, StringBuilder errMsg, String CrewID) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CrewInfo ret = null;

        try {
            json = pFunction.GetCrewInfo(CrewID);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CrewInfo();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.CrewID = jsonArray.getJSONObject(0).getString("CrewID");
                ret.CrewType = jsonArray.getJSONObject(0).getString("CrewType");
                ret.Name = jsonArray.getJSONObject(0).getString("Name");
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }


    /**
     * 8. 進行重新開櫃(Reopen)
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param SecSeq   要重新開櫃的航段序號
     * @param CPId     CPId
     * @param CPPw     CPPw
     * @param CAId     CAId
     * @return boolean
     */
    public static boolean reopenFlightSecSeq(Context mContext, StringBuilder errMsg,
                                             String SecSeq, String CPId, String CPPw, String CAId) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunction.ReopenFlightSecSeq(SecSeq, CPId, CPPw, CAId);
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }


    /**
     * 9. 進行開櫃(Reopen)
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param SecSeq   要重新開櫃的航段序號
     * @param CPId     CPId
     * @param CPPw     CPPw
     * @param CAId     CAId
     * @return boolean
     */
    public static boolean openFlightSecSeq(Context mContext, StringBuilder errMsg,
                                           String SecSeq, String CPId, String CPPw, String CAId) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunction.OpenFlightSecSeq(SecSeq, CPId, CPPw, CAId);
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }

    /**
     * 9. 將開櫃後的必要變數儲存起來
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @return boolean
     */
    public static boolean openFlight(Context mContext, StringBuilder errMsg) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunction.OpenFlight();
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }


    /**
     * 10.  進行關櫃(Close)
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @return boolean
     */
    public static boolean closeFlightSecSeq(Context mContext, StringBuilder errMsg) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunction.CloseFlightSecSeq();
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }


    /**
     * 11. 取得可以使用的幣別
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @return boolean
     */
    public static AllCurrencyListPack getAllCurrencyList(Context mContext, StringBuilder errMsg) {

        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        AllCurrencyListPack ret = null;

        try {
            json = pFunction.GetAllCurrencyList();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new AllCurrencyListPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.currencyList = new AllCurrencyList[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.currencyList[i] = new AllCurrencyList();
                    ret.currencyList[i].CurDvr = jsonArray.getJSONObject(i).getString("CurDvr");
                    ret.currencyList[i].MiniValue = jsonArray.getJSONObject(i).getInt("MiniValue");
                    ret.currencyList[i].BasketCurrency = jsonArray.getJSONObject(i).getString("BasketCurrency");
                    ret.currencyList[i].CashCurrency = jsonArray.getJSONObject(i).getString("CashCurrency");
                    ret.currencyList[i].CardCurrency = jsonArray.getJSONObject(i).getString("CardCurrency");
                    ret.currencyList[i].TaiwanCardCurrency = jsonArray.getJSONObject(i).getString("TaiwanCardCurrency");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class AllCurrencyList implements Serializable {

        public String CurDvr = ""; //幣別簡稱
        public int MiniValue = 0; //幣別最小接受付款金額
        public String BasketCurrency = ""; //要在購物車畫面顯示與否 Y or N
        public String CashCurrency = ""; //現金使用幣別 Y or N
        public String CardCurrency = ""; //信用卡使用幣別 Y or N
        public String TaiwanCardCurrency = ""; //台灣發行信用卡使用幣別 Y or N
    }

    public static class AllCurrencyListPack implements Serializable {

        public AllCurrencyList[] currencyList;
    }


    /**
     * 12. 取得該航班所有的車櫃號
     *
     * @param mContext Activity Context
     * @return boolean
     */
    public static CartNoPack getAllCartList(Context mContext, StringBuilder errMsg) {

        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CartNoPack ret = null;

        try {
            json = pFunction.GetAllCartList();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CartNoPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.cartList = new CartNoList[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.cartList[i] = new CartNoList();
                    ret.cartList[i].CartNo = jsonArray.getJSONObject(i).getString("CartNo");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class CartNoList {

        public String CartNo = "";
    }

    public static class CartNoPack {

        public CartNoList[] cartList;
    }




    /*----------------B. DFS Transaction--------*/

    /**
     * 13 產生或修改購物車內容
     *
     * @param errMsg 錯誤訊息
     * @param json   銷售商品資訊，以及符合之折扣列表
     * @return BasketItemPack
     */
    public static BasketItemPack modifyBasket(StringBuilder errMsg, JSONObject json) {
        String retCode;
        BasketItemPack ret = null;

        try {
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new BasketItemPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.ReceiptNo = jsonArray.getJSONObject(0).getString("ReceiptNo");
                ret.SeatNo = jsonArray.getJSONObject(0).getString("SeatNo");
                ret.IFEOrderNo = jsonArray.getJSONObject(0).getString("IFEOrderNo");
                ret.OriUSDAmount = jsonArray.getJSONObject(0).getDouble("OriUSDAmount");
                ret.USDAmount = jsonArray.getJSONObject(0).getDouble("USDAmount");
                ret.DiscountType = jsonArray.getJSONObject(0).getString("DiscountType");
                ret.DiscountRate = jsonArray.getJSONObject(0).getDouble("DiscountRate");
                ret.DiscountNo = jsonArray.getJSONObject(0).getString("DiscountNo");

                ret.DiscountAmount = jsonArray.getJSONObject(0).getInt("DiscountAmount");
                ret.UpperLimitType = jsonArray.getJSONObject(0).getString("UpperLimitType");
                ret.UpperLimitDiscountNo = jsonArray.getJSONObject(0).getString("UpperLimitDiscountNo");
                ret.UpperLimit = jsonArray.getJSONObject(0).getInt("UpperLimit");

                JSONArray jsonItems = jsonArray.getJSONObject(0).getJSONArray("Items");
                ret.items = new BasketItem[jsonItems.length()];

                for (int i = 0; i < jsonItems.length(); i++) {
                    ret.items[i] = new BasketItem();
                    ret.items[i].ItemCode = jsonItems.getJSONObject(i).getString("ItemCode");
                    ret.items[i].SerialCode = jsonItems.getJSONObject(i).getString("SerialCode");
                    ret.items[i].DrawerNo = jsonItems.getJSONObject(i).getString("DrawerNo");
                    ret.items[i].ItemName = jsonItems.getJSONObject(i).getString("ItemName");
                    ret.items[i].OriginalPrice = jsonItems.getJSONObject(i).getDouble("OriginalPrice");
                    ret.items[i].USDPrice = jsonItems.getJSONObject(i).getDouble("USDPrice");
                    ret.items[i].TWDPrice = jsonItems.getJSONObject(i).getInt("TWDPrice");
                    ret.items[i].POSStock = jsonItems.getJSONObject(i).getInt("POSStock");
                    ret.items[i].IFEStock = jsonItems.getJSONObject(i).getInt("IFEStock");
                    ret.items[i].SalesQty = jsonItems.getJSONObject(i).getInt("SalesQty");
                    ret.items[i].Remark = jsonItems.getJSONObject(i).getString("Remark");
                    ret.items[i].GiftFlag = jsonItems.getJSONObject(i).getString("GiftFlag");
                    ret.items[i].DiscountFlag = jsonItems.getJSONObject(i).getString("DiscountFlag");
                }

                JSONArray jsonTypes = jsonArray.getJSONObject(0).getJSONArray("DiscountList");
                ret.types = new DiscountList[jsonTypes.length()];

                for (int i = 0; i < jsonTypes.length(); i++) {
                    ret.types[i] = new DiscountList();
                    ret.types[i].DiscountNo = jsonTypes.getJSONObject(i).getString("DiscountNo");
                    ret.types[i].Type = jsonTypes.getJSONObject(i).getString("Type");
                    ret.types[i].Description = jsonTypes.getJSONObject(i).getString("Description");
                    ret.types[i].DiscountCount = jsonTypes.getJSONObject(i).getInt("DiscountCount");
                    ret.types[i].FuncID = jsonTypes.getJSONObject(i).getString("FuncID");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class BasketItem {

        public String ItemCode = "";
        public String SerialCode = "";
        public String DrawerNo = "";
        public String ItemName = "";
        public double OriginalPrice = 0;
        public double USDPrice = 0;
        public int TWDPrice = 0;
        public int POSStock = 0;
        public int IFEStock = 0;
        public int SalesQty = 0;
        public String Remark = "";
        public String GiftFlag = "";      // Y贈品，N 非贈品
        public String DiscountFlag = "";  // Y 可以打折，N 不可以打折，
    }

    // 所有滿足條件的折扣身分(包含主被動)
    public static class DiscountList {

        public String DiscountNo = "";
        public String Type = "";
        public String Description = "";
        public int DiscountCount = 0;
        public String FuncID = "";
    }

    public static class BasketItemPack {

        public String ReceiptNo = "";

        public String SeatNo = "";
        public String IFEOrderNo = "";

        public double OriUSDAmount = 0; //原始金額
        public double USDAmount = 0; //打折後的金額

        public String DiscountType = ""; //當下真正使用的折扣
        public String DiscountNo = ""; // 折扣卡號
        public double DiscountRate = 0; //趴數折扣
        public int DiscountAmount = 0; //現金折扣

        public String UpperLimitType = "";
        public String UpperLimitDiscountNo = "";
        public int UpperLimit = 0; //刷卡金額上限

        public BasketItem[] items; //購買商品列表
        public DiscountList[] types; //折扣列表
    }


    /**
     * 14. 取得付款模式
     *
     * @param errMsg 錯誤訊息
     * @param json   銷售商品資訊以及付款資訊
     * @return PaymentModePack
     */
    public static PaymentModePack getPayMode(StringBuilder errMsg, JSONObject json) {
        String retCode;
        PaymentModePack ret = null;

        try {
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new PaymentModePack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.NowPayMode = jsonArray.getJSONObject(0).getString("NowPayMode");
                ret.USDTotalAmount = jsonArray.getJSONObject(0).getDouble("USDTotalAmount");
                ret.USDTotalPayed = jsonArray.getJSONObject(0).getDouble("USDTotalPayed");
                ret.USDTotalUnpay = jsonArray.getJSONObject(0).getDouble("USDTotalUnpay");
                if (!jsonArray.getJSONObject(0).isNull("LastPayCurrency")) {
                    ret.LastPayCurrency = jsonArray.getJSONObject(0).getString("LastPayCurrency");
                }
                if (!jsonArray.getJSONObject(0).isNull("LastPayAmount")) {
                    ret.LastPayAmount = jsonArray.getJSONObject(0).getInt("LastPayAmount");
                }

                JSONArray jsonPay = jsonArray.getJSONObject(0).getJSONArray("PaymentList");
                ret.payLisy = new PaymentList[jsonPay.length()];

                for (int i = 0; i < jsonPay.length(); i++) {
                    ret.payLisy[i] = new PaymentList();
                    ret.payLisy[i].Currency = jsonPay.getJSONObject(i).getString("Currency");
                    ret.payLisy[i].PayBy = jsonPay.getJSONObject(i).getString("PayBy");
                    ret.payLisy[i].Amount = jsonPay.getJSONObject(i).getDouble("Amount");
                    ret.payLisy[i].USDAmount = jsonPay.getJSONObject(i).getDouble("USDAmount");
                    if (!jsonPay.getJSONObject(i).isNull("CouponNo")) {
                        ret.payLisy[i].CouponNo = jsonPay.getJSONObject(i).getString("CouponNo");
                    } else {
                        ret.payLisy[i].CouponNo = null;
                    }

                    if (!jsonPay.getJSONObject(i).isNull("SwipeCount")) {
                        ret.payLisy[i].SwipeCount = jsonPay.getJSONObject(i).getInt("SwipeCount");
                    }
                    if (!jsonPay.getJSONObject(i).isNull("LastLimitation")) {
                        ret.payLisy[i].LastLimitation = jsonPay.getJSONObject(i).getInt("LastLimitation");
                    }

                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class PaymentModePack {

        public String NowPayMode = ""; //Pay or Change or Balance
        public double USDTotalAmount = 0;
        public double USDTotalPayed = 0;
        public double USDTotalUnpay = 0;
        public String LastPayCurrency = "";
        public int LastPayAmount = 0;
        public PaymentList[] payLisy;
    }

    public static class PaymentList {

        public String Currency = "";
        public String PayBy = "";
        public double Amount = 0;
        public double USDAmount = 0;
        public String CouponNo = "";

        public int SwipeCount = 0; //已刷卡次數
        public int LastLimitation = 0; //剩餘可刷金額
    }


    // 取得當前幣別應付金額
    public static ShouldPayMoney getPayMoneyNow(StringBuilder errMsg, JSONObject json) {
        String retCode;
        ShouldPayMoney ret = null;

        try {
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new ShouldPayMoney();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.Currency = jsonArray.getJSONObject(0).getString("Currency");
                ret.MaxPayAmount = jsonArray.getJSONObject(0).getInt("MaxPayAmount");

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class ShouldPayMoney {

        public String Currency = "";
        public int MaxPayAmount = 0;
    }


    /**
     * 15. 取得Coupon資訊
     *
     * @param mContext   Activity Context
     * @param errMsg     錯誤訊息
     * @param CouponType 折扣卷類別 DC or SC
     * @param CouponCode 6碼 or 10碼
     * @return CouponInfo
     */
    public static CouponInfo getCouponInfo(Context mContext, StringBuilder errMsg,
                                           String CouponType, String CouponCode) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CouponInfo ret = null;

        try {
            json = pFunction.GetCouponInfo(CouponType, CouponCode);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CouponInfo();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.CouponCurrency = jsonArray.getJSONObject(0).getString("CouponCurrency");
                ret.CouponAmount = jsonArray.getJSONObject(0).getInt("CouponAmount");

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;

    }

    public static class CouponInfo {

        public String CouponCurrency = "";
        public int CouponAmount = 0;
    }


    /**
     * 18. 取得免稅品交易單據資訊
     *
     * @param mContext  Activity Context
     * @param errMsg    錯誤訊息
     * @param ReceiptNo 交易單號，不使用則傳Null
     * @return boolean
     */
    public static TransactionInfoPack getDFSTransactionInfo(Context mContext, StringBuilder errMsg, String ReceiptNo) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        TransactionInfoPack ret = null;

        try {
            json = pFunction.GetDFSTransactionInfo(ReceiptNo);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {

                ret = new TransactionInfoPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.info = new TransactionInfo[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.info[i] = new TransactionInfo();
                    ret.info[i].ReceiptNo = jsonArray.getJSONObject(i).getString("ReceiptNo");
                    ret.info[i].Status = jsonArray.getJSONObject(i).getString("Status");
                    ret.info[i].SeatNo = jsonArray.getJSONObject(i).getString("SeatNo");
                    ret.info[i].PreorderNo = jsonArray.getJSONObject(i).getString("PreorderNo");
                    ret.info[i].OriUSDAmount = jsonArray.getJSONObject(i).getDouble("OriUSDAmount");
                    ret.info[i].USDAmount = jsonArray.getJSONObject(i).getDouble("USDAmount");
                    ret.info[i].DiscountType = jsonArray.getJSONObject(i).getString("DiscountType");
                    ret.info[i].DiscountNo = jsonArray.getJSONObject(i).getString("DiscountNo");
                    ret.info[i].UpperLimitType = jsonArray.getJSONObject(i).getString("UpperLimitType");
                    ret.info[i].UpperLimitNo = jsonArray.getJSONObject(i).getString("UpperLimitNo");

                    JSONArray jsonItems = jsonArray.getJSONObject(i).getJSONArray("Items");
                    ret.info[i].items = new TransactionItem[jsonItems.length()];

                    for (int j = 0; j < jsonItems.length(); j++) {
                        ret.info[i].items[j] = new TransactionItem();
                        ret.info[i].items[j].SerialNo = jsonItems.getJSONObject(j).getString("SerialCode");
                        ret.info[i].items[j].ItemCode = jsonItems.getJSONObject(j).getString("ItemCode");
                        ret.info[i].items[j].ItemName = jsonItems.getJSONObject(j).getString("ItemName");
                        ret.info[i].items[j].OriginalPrice = jsonItems.getJSONObject(j).getDouble("OriginalPrice");
                        ret.info[i].items[j].USDPrice = jsonItems.getJSONObject(j).getDouble("USDPrice");
                        ret.info[i].items[j].SalesQty = jsonItems.getJSONObject(j).getInt("SalesQty");
                    }

                    JSONArray jsonPay = jsonArray.getJSONObject(0).getJSONArray("PaymentList");
                    ret.info[i].payments = new TransactionPaymentPack[jsonPay.length()];

                    for (int k = 0; k < jsonPay.length(); k++) {
                        ret.info[i].payments[k] = new TransactionPaymentPack();
                        ret.info[i].payments[k].Currency = jsonPay.getJSONObject(k).getString("Currency");
                        ret.info[i].payments[k].PayBy = jsonPay.getJSONObject(k).getString("PayBy");
                        ret.info[i].payments[k].Amount = jsonPay.getJSONObject(k).getDouble("Amount");
                        ret.info[i].payments[k].USDAmount = jsonPay.getJSONObject(k).getDouble("USDAmount");
                        ret.info[i].payments[k].CouponNo = jsonPay.getJSONObject(k).getString("CouponNo");
                        ret.info[i].payments[k].CardNo = jsonPay.getJSONObject(k).getString("CardNo");
                        ret.info[i].payments[k].CardName = jsonPay.getJSONObject(k).getString("CardName");
                        ret.info[i].payments[k].CardType = jsonPay.getJSONObject(k).getString("CardType");
                        ret.info[i].payments[k].CardDate = jsonPay.getJSONObject(k).getString("CardDate");
                    }
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }


    public static class TransactionInfoPack {

        public TransactionInfo[] info;
    }

    public static class TransactionInfo {

        public String SecSeq = "";
        public String ReceiptNo = "";
        public String Status = "";
        public String SeatNo = "";
        public String PreorderNo = "";
        public double OriUSDAmount = 0;
        public double USDAmount = 0;
        public String DiscountType = "";
        public String DiscountNo = "";
        public String UpperLimitType = "";
        public String UpperLimitNo = "";

        public TransactionItem[] items;
        public TransactionPaymentPack[] payments;
    }

    public static class TransactionItem { //購買商品列表

        public String ItemCode = "";
        public String SerialNo = "";
        public String DrawerNo = "";
        public String ItemName = "";
        public double OriginalPrice = 0;
        public double USDPrice = 0;
        public int SalesQty = 0;
    }

    public static class TransactionPaymentPack { //現有的付款歷程

        public String Currency = "";
        public String PayBy = "";
        public double Amount = 0;
        public double USDAmount = 0;
        public String CouponNo = "";
        public String CardNo = "";
        public String CardName = "";
        public String CardType = "";
        public String CardDate = "";
    }


    /**
     * 取得所有可退貨的單據號碼
     *
     * @param mContext                 Activity Context
     * @param errMsg                   錯誤訊息
     * @param SaleStatus               銷售狀態(’Sale’, ’Refund’)，不使用則傳Null
     * @param GetRefundReceiptCanPrint 在Report補印頁面可以補印的Sales Refund Report
     * @return boolean
     */
    public static ReceiptList getAllRceciptNoList(Context mContext, StringBuilder errMsg,
                                                  String SaleStatus, boolean GetRefundReceiptCanPrint) {

        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        ReceiptList ret = null;

        try {
            json = pFunctionHeidi.GetAllReceiptNo(SaleStatus, getSecSeq(), GetRefundReceiptCanPrint);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {

                ret = new ReceiptList();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                // 過濾掉非Refund的收據號碼
                if (GetRefundReceiptCanPrint) {
                    ArrayList<Integer> tmpList = new ArrayList<>();

                    if (jsonArray.length() == 1) {
                        tmpList.add(0);
                    } else {
                        for (int i = 0; i < jsonArray.length() - 1; i++) {
                            String preSec = jsonArray.getJSONObject(i).getString("SecSeq");
                            String nextSec = jsonArray.getJSONObject(i + 1).getString("SecSeq");
                            String prePreorderNo = jsonArray.getJSONObject(i).getString("PreorderNo");
                            String nextPreorderNo = jsonArray.getJSONObject(i + 1).getString("PreorderNo");

                            // Preorder: 若Preorder兩個的編號相同, 且為同一個航段, 就不要顯示舊的被Refund掉的Preorder單據, 顯示最後一筆即可
                            // DFS: 若沒有Preorder單據號碼就顯示出來
                            if (prePreorderNo.equals("") || !(preSec.equals(nextSec) && prePreorderNo.equals(nextPreorderNo))) {
                                tmpList.add(i);
                            }
                            if (i == (jsonArray.length() - 2)) {
                                tmpList.add(jsonArray.length() - 1);
                            }
                        }
                    }

                    ret.rececipts = new Receipt[tmpList.size()];
                    for (int j = 0; j < tmpList.size(); j++) {
                        int _index = tmpList.get(j);
                        ret.rececipts[j] = new Receipt();
                        ret.rececipts[j].SecSeq = jsonArray.getJSONObject(_index).getString("SecSeq");
                        ret.rececipts[j].ReceiptNo = jsonArray.getJSONObject(_index).getString("ReceiptNo");
                        ret.rececipts[j].SaleFlag = jsonArray.getJSONObject(_index).getString("SaleFlag");
                        ret.rececipts[j].PreorderNo = jsonArray.getJSONObject(_index).getString("PreorderNo");
                        ret.rececipts[j].Type = jsonArray.getJSONObject(_index).getString("Type");
                    }
                }

                // 正常單子號碼

                else {
                    ret.rececipts = new Receipt[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        ret.rececipts[i] = new Receipt();
                        ret.rececipts[i].SecSeq = jsonArray.getJSONObject(i).getString("SecSeq");
                        ret.rececipts[i].ReceiptNo = jsonArray.getJSONObject(i).getString("ReceiptNo");
                        ret.rececipts[i].SaleFlag = jsonArray.getJSONObject(i).getString("SaleFlag");
                        ret.rececipts[i].PreorderNo = jsonArray.getJSONObject(i).getString("PreorderNo");
                        ret.rececipts[i].Type = jsonArray.getJSONObject(i).getString("Type");
                    }
                }


            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class ReceiptList {

        public Receipt[] rececipts;
    }

    public static class Receipt {

        public String SaleFlag = "";
        public String SecSeq = "";
        public String ReceiptNo = "";
        public String PreorderNo = "";
        public String Type = "";
    }


    // 取得退貨列表的商品顯示
    public static RefundReceiptDetial getReceiptDetailItem(StringBuilder errMsg, JSONObject json) {
        String retCode;
        RefundReceiptDetial ret = null;

        try {
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {

                ret = new RefundReceiptDetial();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.ReceiptNo = jsonArray.getJSONObject(0).getString("ReceiptNo");
                ret.USDAmount = jsonArray.getJSONObject(0).getDouble("USDAmount");
                if (!jsonArray.getJSONObject(0).isNull("PreorderNo")) {
                    ret.PreorderNo = jsonArray.getJSONObject(0).getString("PreorderNo");
                }

                JSONArray jsonItems = jsonArray.getJSONObject(0).getJSONArray("Items");
                ret.items = new TransactionItem[jsonItems.length()];

                for (int i = 0; i < jsonItems.length(); i++) {
                    ret.items[i] = new TransactionItem();
                    ret.items[i].ItemCode = jsonItems.getJSONObject(i).getString("ItemCode");
                    ret.items[i].SerialNo = jsonItems.getJSONObject(i).getString("SerialCode");
                    ret.items[i].DrawerNo = jsonItems.getJSONObject(i).getString("DrawerNo");
                    ret.items[i].ItemName = jsonItems.getJSONObject(i).getString("ItemName");
                    ret.items[i].OriginalPrice = jsonItems.getJSONObject(i).getDouble("OriginalPrice");
                    ret.items[i].SalesQty = jsonItems.getJSONObject(i).getInt("SalesQty");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }

        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class RefundReceiptDetial {

        public String ReceiptNo = "";
        public String PreorderNo;
        public double USDAmount = 0;
        public TransactionItem items[];
    }


    /**
     * 取得所有可退貨的Upgrade單據號碼
     *
     * @param mContext  Activity Context
     * @param errMsg    錯誤訊息
     * @param ReceiptNo 交易單號，不使用則傳Null
     * @return boolean
     */
    public static ReceiptList getAllUpgradeRceciptNoList(Context mContext, StringBuilder errMsg,
                                                         String ReceiptNo, String SaleStatus) {

        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        ReceiptList ret = null;

        try {
            json = pFunctionHeidi.GetAllUpgradeRceciptNoList(ReceiptNo, SaleStatus, getSecSeq());
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {

                ret = new ReceiptList();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.rececipts = new Receipt[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.rececipts[i] = new Receipt();
                    ret.rececipts[i].SecSeq = jsonArray.getJSONObject(i).getString("SecSeq");
                    ret.rececipts[i].ReceiptNo = jsonArray.getJSONObject(i).getString("ReceiptNo");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    /**
     * 19. 付款金額轉換
     *
     * @param mContext Activity Context
     * @param errMsg 錯誤訊息
     * @param FromCurrency 被轉換的原始幣別
     * @param ToCurrency 要轉換的目的幣別
     * @param OriAmount 原始幣別金額 (ex. JP轉HK的話, 此處傳美金總額)
     * @param DecimalPoint 要取得的位數 ex:100 = 百位數,  0.1 = 小數點第一位 (Air傳0)
     * @param MappingMiniValue 是否要取得目的幣別最小進位，若是與上一個參數衝突，則以此參數為主 (Air傳true)
     * @param Type 1. 無條件進位    2. 四捨五入    3. 無條件捨去 (Basket應付總額=1, 付款=2, 找零=3)
     * @return boolean
     */
//  public static Double changeCurrencyAmount(Context mContext, StringBuilder errMsg, String FromCurrency,
//      String ToCurrency, Double OriAmount, double DecimalPoint,
//      boolean MappingMiniValue, int Type) {
//    pFunction = new PublicFunctions(mContext, getSecSeq());
//    JSONObject json;
//    String retCode;
//    Double ret = 0.0;
//    try {
//
//      json = pFunction.ChangeCurrencyAmount(FromCurrency, ToCurrency,
//          OriAmount, DecimalPoint, MappingMiniValue, Type);
//      retCode = json.getString("ReturnCode");
//
//      if (retCode.equals("0")) {
//
//        JSONArray jsonArray = json.getJSONArray("ResponseData");
//        if (jsonArray == null) {
//          return null;
//        }
//        ret = jsonArray.getJSONObject(0).getDouble("NewAmount");
//
//      } else {
//        errMsg.append(json.getString("ReturnMessage"));
//      }
//    } catch (Exception ex) {
//      errMsg.append(ex.getMessage());
//      return null;
//    }
//    return ret;
//  }


    /**
     * 20. 判斷信用卡黑名單
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param CardType 欲判斷的信用卡類別 VISA or JCB …
     * @param CardNo   欲判斷的信用卡號碼
     * @return boolean
     */
    public static boolean checkBlackCard(Context mContext, StringBuilder errMsg,
                                         String CardType, String CardNo) {

        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        // 判斷是否為黑名單卡，若不是的話回0, 若是的話回9, 還會再回是那種黑名單卡
        try {
            json = pFunction.CheckBlackCard(CardType, CardNo);
            retCode = json.getString("ReturnCode");
            errMsg.append(json.getString("ReturnMessage"));

            return !retCode.equals("0");
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return true;
        }
    }


    /**
     * 21. 確認是否為台灣信用卡
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param CardNo   欲判斷的信用卡號碼
     * @return boolean
     */
    public static boolean checkIsTaiwanCard(Context mContext, StringBuilder errMsg, String CardNo) {

        pFunction = new PublicFunctions(mContext, getSecSeq());
        try {
            return pFunction.CheckIsTaiwanCard(CardNo);
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
    }



    /*----------------C. Upgrade Transaction--------*/


    /**
     * 22. 產生或修改升艙等購物車內容
     *
     * @param errMsg 錯誤訊息
     * @param json   銷售商品資訊，以及符合之折扣列表
     * @return boolean
     */
    public static UpgradeItemPack modifyUpgradeBasket(StringBuilder errMsg, JSONObject json) {

        String retCode;
        UpgradeItemPack ret = null;

        try {
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new UpgradeItemPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.ReceiptNo = jsonArray.getJSONObject(0).getString("ReceiptNo");
                ret.USDAmount = jsonArray.getJSONObject(0).getDouble("USDAmount");

                JSONArray jsonItems = jsonArray.getJSONObject(0).getJSONArray("Items");
                ret.items = new UpgradeItem[jsonItems.length()];

                for (int i = 0; i < jsonItems.length(); i++) {
                    ret.items[i] = new UpgradeItem();
                    ret.items[i].Infant = jsonItems.getJSONObject(i).getString("Infant");
                    ret.items[i].OriginalClass = jsonItems.getJSONObject(i).getString("OriginalClass");
                    ret.items[i].NewClass = jsonItems.getJSONObject(i).getString("NewClass");
                    ret.items[i].USDPrice = jsonItems.getJSONObject(i).getDouble("USDPrice");
                    ret.items[i].TWDPrice = jsonItems.getJSONObject(i).getDouble("TWDPrice");
                    ret.items[i].SalesQty = jsonItems.getJSONObject(i).getInt("SalesQty");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class UpgradeItem {

        public String Infant = "";
        public String OriginalClass = "";
        public String NewClass = "";
        public double USDPrice = 0;
        public double TWDPrice = 0;
        public int SalesQty = 0;
    }

    public static class UpgradeItemPack {

        public String ReceiptNo = "";
        public double USDAmount = 0;

        public UpgradeItem[] items; //購買商品列表
    }


    /**
     * 24. 取得升艙等交易單據資訊
     *
     * @param mContext    Activity Context
     * @param errMsg      錯誤訊息
     * @param ReceiptNo   交易單號，不使用則傳Null
     * @param SalesStatus 銷售狀態(’S’, ’R’)，不使用則傳Null
     * @return boolean
     */
    public static UpgradeTransactionInfoPack getUpgradeTransactionInfo(Context mContext, StringBuilder errMsg,
                                                                       String ReceiptNo, String SalesStatus) {

        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        UpgradeTransactionInfoPack ret = null;

        try {
            json = pFunction.GetUpgradeTransactionInfo(ReceiptNo, SalesStatus);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new UpgradeTransactionInfoPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.info = new UpgradeTransactionInfo[jsonArray.length()];

                for (int k = 0; k < jsonArray.length(); k++) {
                    ret.info[k] = new UpgradeTransactionInfo();
                    ret.info[k].ReceiptNo = jsonArray.getJSONObject(k).getString("ReceiptNo");
                    ret.info[k].Status = jsonArray.getJSONObject(k).getString("Status");
                    ret.info[k].TotalPrice = jsonArray.getJSONObject(k).getDouble("TotalPrice");

                    JSONArray jsonItems = jsonArray.getJSONObject(k).getJSONArray("Items");
                    ret.info[k].items = new UpgradeTransactionItem[jsonItems.length()];

                    for (int i = 0; i < jsonItems.length(); i++) {
                        ret.info[k].items[i] = new UpgradeTransactionItem();
                        ret.info[k].items[i].Infant = jsonItems.getJSONObject(i).getString("Infant");
                        ret.info[k].items[i].OriginalClass = jsonItems.getJSONObject(i).getString("OriginalClass");
                        ret.info[k].items[i].NewClass = jsonItems.getJSONObject(i).getString("NewClass");
                        ret.info[k].items[i].SalesPrice = jsonItems.getJSONObject(i).getDouble("SalesPrice");
                        ret.info[k].items[i].SalesQty = jsonItems.getJSONObject(i).getInt("SalesQty");
                    }

                    JSONArray jsonPay = jsonArray.getJSONObject(k).getJSONArray("PaymentList");
                    ret.info[k].payments = new TransactionPaymentPack[jsonPay.length()];

                    for (int i = 0; i < jsonPay.length(); i++) {
                        ret.info[k].payments[i] = new TransactionPaymentPack();
                        ret.info[k].payments[i].Currency = jsonPay.getJSONObject(i).getString("Currency");
                        ret.info[k].payments[i].PayBy = jsonPay.getJSONObject(i).getString("PayBy");
                        ret.info[k].payments[i].Amount = jsonPay.getJSONObject(i).getDouble("Amount");
                        ret.info[k].payments[i].USDAmount = jsonPay.getJSONObject(i).getDouble("USDAmount");
                        ret.info[k].payments[i].CardNo = jsonPay.getJSONObject(i).getString("CardNo");
                        ret.info[k].payments[i].CardName = jsonPay.getJSONObject(i).getString("CardName");
                        ret.info[k].payments[i].CardType = jsonPay.getJSONObject(i).getString("CardType");
                        ret.info[k].payments[i].CardDate = jsonPay.getJSONObject(i).getString("CardDate");
                    }
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class UpgradeTransactionInfoPack {

        public UpgradeTransactionInfo[] info;
    }

    public static class UpgradeTransactionInfo {

        public String ReceiptNo = "";
        public String Status = "";
        public double TotalPrice = 0;

        public UpgradeTransactionItem[] items; //購買商品列表
        public TransactionPaymentPack[] payments;  //現有的付款歷程
    }

    public static class UpgradeTransactionItem {

        public String Infant = "";
        public String OriginalClass = "";
        public String NewClass = "";
        public double SalesPrice = 0;
        public int SalesQty = 0;
    }




    /*----------------D. Inventory--------*/


    /**
     * 26. 確認是否已經經過商品銷售和移儲
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @return boolean
     */
    public static boolean checkCurrentFlightCanUpdate(Context mContext, StringBuilder errMsg) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunction.CheckCurrentFlightCanUpdate();
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }


    /**
     * 27.取得抽屜資訊
     *
     * @param mContext  Activity Context
     * @param errMsg    錯誤訊息
     * @param seqNum    航段編號
     * @param drawerNum 抽屜編號
     * @return DrawNoPack
     */
    public static DrawNoPack getAllDrawerNo(Context mContext, StringBuilder errMsg, String seqNum, String drawerNum, boolean isBeginInventory) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        DrawNoPack ret = null;

        try {
            json = pFunction.GetAllDrawerInfo(seqNum, drawerNum, isBeginInventory);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new DrawNoPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.drawers = new DrawNo[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.drawers[i] = new DrawNo();
                    ret.drawers[i].DrawNo = jsonArray.getJSONObject(i).getString("DrawNo");
                    ret.drawers[i].Qty = jsonArray.getJSONObject(i).getString("Qty");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    //EGAS==差異畫面 Items
    public static ArrayList<ItemInfo> getEGASDiscrepancyItemList(Context mContext, StringBuilder errMsg) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        ArrayList<ItemInfo> ret = new ArrayList<>();

        try {
            json = pFunction.getEGASDiscrepancyItemList();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {


                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ItemInfo info = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    info = new ItemInfo();


                    info.setDrawer(jsonArray.getJSONObject(i).getString("DrawNo"));
                    info.setItemCode(jsonArray.getJSONObject(i).getString("ItemCode"));
                    info.setOriginNum(jsonArray.getJSONObject(i).getInt("StandQty"));
                    info.setEgascheck(jsonArray.getJSONObject(i).getInt("EGASCheckQty"));
                    info.setItemInfo(jsonArray.getJSONObject(i).getString("ItemName"));
                    info.setDamage(jsonArray.getJSONObject(i).getInt("EGASDamageQty"));

                    ret.add(info);
                }


            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    //EGAS==差異畫面-PerOrder & PerOrder  VIP
    public static ArrayList<PreorderReceiptInfo> getEGASDiscrepancyPreOrderList(Context mContext, StringBuilder errMsg) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        ArrayList<PreorderReceiptInfo> ret = new ArrayList<>();

        try {
            json = pFunction.getEGASDiscrepancyPreorderList();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {


                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                PreorderReceiptInfo info = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    info = new PreorderReceiptInfo();
                    info.setPreorderReceiptInfo(jsonArray.getJSONObject(i).getString("PreorderNO"));
                    info.setSaleState("Sale");
                    ret.add(info);
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    //EVA==差異畫面 Items
    public static ArrayList<ItemInfo> getEVADiscrepancyItemList(Context mContext, StringBuilder errMsg) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        ArrayList<ItemInfo> ret = new ArrayList<>();

        try {
            json = pFunction.getEVADiscrepancyItemList();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {


                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ItemInfo info = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    info = new ItemInfo();


                    info.setDrawer(jsonArray.getJSONObject(i).getString("DrawNo"));
                    info.setItemCode(jsonArray.getJSONObject(i).getString("ItemCode"));
                    info.setItemInfo(jsonArray.getJSONObject(i).getString("ItemName"));
                    info.setOriginNum(jsonArray.getJSONObject(i).getInt("EndQty"));
                    info.setEgascheck(jsonArray.getJSONObject(i).getInt("EGASCheckQty"));
                    info.setEvacheck(jsonArray.getJSONObject(i).getInt("EVACheckQty"));
                    info.setDamage(jsonArray.getJSONObject(i).getInt("EVADamageQty"));

                    ret.add(info);
                }


            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    //EVA==差異畫面-PerOrder & PerOrder  VIP
    public static ArrayList<PreorderReceiptInfo> getEVADiscrepancyPreOrderList(Context mContext, StringBuilder errMsg) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        ArrayList<PreorderReceiptInfo> ret = new ArrayList<>();

        try {
            json = pFunction.getEVADiscrepancyPreorderList();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {


                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                PreorderReceiptInfo info = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    info = new PreorderReceiptInfo();
                    info.setPreorderReceiptInfo(jsonArray.getJSONObject(i).getString("PreorderNO"));
                    info.setSaleState("Sale");
                    ret.add(info);
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class DrawNo {

        public String DrawNo = "";
        public String Qty = "";
    }

    public static class DrawNoPack {

        public DrawNo[] drawers;
    }


    /**
     * 28. 依據條件取得商品資訊, 三個參數皆在不使用時傳null
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param SecSeq   航段編號，不使用時傳Null
     * @param Code     商品編號、雜誌編號或商品條碼，不使用時傳Null
     * @param DrawerNo 抽屜編號，不使用時傳Null
     * @param Sort     排序方式，0 - Draw+ItemCode; 1 - ItemCode+DrawNo ; 2 - DrawNo+SerialCode; 3 - SerialCode
     * @return ItemDataPack
     */
    public static ItemDataPack getProductInfo(Context mContext, StringBuilder errMsg, String SecSeq, String Code, String DrawerNo, int Sort) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        ItemDataPack ret = null;

        try {
            json = pFunction.GetProductInfo(SecSeq, Code, DrawerNo, Sort);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new ItemDataPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.items = new ItemData[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.items[i] = new ItemData();
                    ret.items[i].ItemCode = jsonArray.getJSONObject(i).getString("ItemCode");
                    ret.items[i].DrawNo = jsonArray.getJSONObject(i).getString("DrawNo");
                    ret.items[i].SerialCode = jsonArray.getJSONObject(i).getString("SerialCode");
                    ret.items[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.items[i].Remark = jsonArray.getJSONObject(i).getString("Remark");
                    ret.items[i].ItemPriceUS = jsonArray.getJSONObject(i).getDouble("ItemPriceUS");
                    ret.items[i].ItemPriceTW = jsonArray.getJSONObject(i).getDouble("ItemPriceTW");
                    ret.items[i].StandQty = jsonArray.getJSONObject(i).getInt("StandQty");
                    ret.items[i].StartQty = jsonArray.getJSONObject(i).getInt("StartQty");
                    ret.items[i].AdjustQty = jsonArray.getJSONObject(i).getInt("AdjustQty");
                    ret.items[i].SalesQty = jsonArray.getJSONObject(i).getInt("SalesQty");
                    ret.items[i].TransferQty = jsonArray.getJSONObject(i).getInt("TransferQty");
                    ret.items[i].DamageQty = jsonArray.getJSONObject(i).getInt("DamageQty");
                    ret.items[i].EndQty = jsonArray.getJSONObject(i).getInt("EndQty");
                    ret.items[i].EGASCheckQty = jsonArray.getJSONObject(i).getInt("EGASCheckQty");
                    ret.items[i].EGASDamageQty = jsonArray.getJSONObject(i).getInt("EGASDamageQty");
                    ret.items[i].EVACheckQty = jsonArray.getJSONObject(i).getInt("EVACheckQty");
                    ret.items[i].EVADamageQty = jsonArray.getJSONObject(i).getInt("EVADamageQty");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class ItemData {

        public String ItemCode = ""; //商品編號
        public String DrawNo = ""; //抽屜號碼
        public String SerialCode = "";  //商品雜誌編號
        public String ItemName = "";
        public String Remark = "";
        public double ItemPriceUS = 0;
        public double ItemPriceTW = 0;
        public int StandQty = 0; //標準裝載量

        public int StartQty = 0; //開櫃數量
        public int AdjustQty = 0; //開櫃更新數量
        public int SalesQty = 0; //銷售數量
        public int TransferQty = 0; //移儲數量
        public int DamageQty = 0; //瑕疵品數量
        public int EndQty = 0; //關櫃數量

        public int EGASCheckQty = 0;
        public int EGASDamageQty = 0;
        public int EVACheckQty = 0;
        public int EVADamageQty = 0;
    }

    public static class ItemDataPack {

        public ItemData[] items;
    }


    public static ItemDataPack getAdjustInfo(Context mContext, StringBuilder errMsg, String SecSeq, String Code, String DrawerNo, int Sort) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        ItemDataPack ret = null;

        try {
            json = pFunction.GetAdjustInfo(SecSeq, Code, DrawerNo, Sort);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new ItemDataPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.items = new ItemData[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.items[i] = new ItemData();
                    ret.items[i].ItemCode = jsonArray.getJSONObject(i).getString("ItemCode");
                    ret.items[i].DrawNo = jsonArray.getJSONObject(i).getString("DrawNo");
                    ret.items[i].SerialCode = jsonArray.getJSONObject(i).getString("SerialCode");
                    ret.items[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.items[i].Remark = jsonArray.getJSONObject(i).getString("Remark");
                    ret.items[i].ItemPriceUS = jsonArray.getJSONObject(i).getDouble("ItemPriceUS");
                    ret.items[i].ItemPriceTW = jsonArray.getJSONObject(i).getDouble("ItemPriceTW");
                    ret.items[i].StandQty = jsonArray.getJSONObject(i).getInt("StandQty");
                    ret.items[i].StartQty = jsonArray.getJSONObject(i).getInt("StartQty");
                    ret.items[i].AdjustQty = jsonArray.getJSONObject(i).getInt("AdjustQty");
                    ret.items[i].SalesQty = jsonArray.getJSONObject(i).getInt("SalesQty");
                    ret.items[i].TransferQty = jsonArray.getJSONObject(i).getInt("TransferQty");
                    ret.items[i].DamageQty = jsonArray.getJSONObject(i).getInt("DamageQty");
                    ret.items[i].EndQty = jsonArray.getJSONObject(i).getInt("EndQty");
                    ret.items[i].EGASCheckQty = jsonArray.getJSONObject(i).getInt("EGASCheckQty");
                    ret.items[i].EGASDamageQty = jsonArray.getJSONObject(i).getInt("EGASDamageQty");
                    ret.items[i].EVACheckQty = jsonArray.getJSONObject(i).getInt("EVACheckQty");
                    ret.items[i].EVADamageQty = jsonArray.getJSONObject(i).getInt("EVADamageQty");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }


    /**
     * 29. 取得升艙等商品資訊
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @return UpgradeProductInfoPack
     */
    public static UpgradeProductInfoPack getUpgradeProductInfo(Context mContext, StringBuilder errMsg) {

        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        UpgradeProductInfoPack ret = null;

        try {
            json = pFunctionHeidi.GetUpgradeProductInfo();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new UpgradeProductInfoPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.items = new UpgradeProductInfo[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.items[i] = new UpgradeProductInfo();
                    ret.items[i].Infant = jsonArray.getJSONObject(i).getString("Infant");
                    ret.items[i].OriginalClass = jsonArray.getJSONObject(i).getString("OriginalClass");
                    ret.items[i].NewClass = jsonArray.getJSONObject(i).getString("NewClass");
                    ret.items[i].USDPrice = jsonArray.getJSONObject(i).getDouble("USDPrice");
                    ret.items[i].TWDPrice = jsonArray.getJSONObject(i).getInt("TWDPrice");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class UpgradeProductInfoPack {

        public UpgradeProductInfo[] items;
    }

    public static class UpgradeProductInfo {

        public String Infant = "";
        public String OriginalClass = "";
        public String NewClass = "";
        public double USDPrice = 0;
        public int TWDPrice = 0;
    }


    /**
     * 30. 進行開櫃調整
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param inputObj 要調整的商品Array
     * @return boolean
     */
    public static boolean adjustItemQty(Context mContext, StringBuilder errMsg, JSONArray inputObj) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunction.AdjustItemQty(inputObj);
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }


    /**
     * 31. 進行EGAS回庫調整
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param inputObj 要調整的商品Array
     * @return boolean
     */
    public static boolean eGASAdjustItemQty(Context mContext, StringBuilder errMsg, JSONArray inputObj) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunctionHeidi.EGASAdjustItemQty(inputObj);
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }

    public static boolean eGASAdjustQty(Context mContext, StringBuilder errMsg, String itemCode, int stockQty) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunctionHeidi.EGASAdjustItemQty(itemCode, stockQty);
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }


    public static boolean eGASDamageQty(Context mContext, StringBuilder errMsg, String itemCode, int damageQty) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunctionHeidi.EGASDamageItemQty(itemCode, damageQty);
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }

    public static boolean eVAAdjustQty(Context mContext, StringBuilder errMsg, String itemCode, int stockQty) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunctionHeidi.EVAAdjustItemQty(itemCode, stockQty);
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }


    public static boolean eVADamageQty(Context mContext, StringBuilder errMsg, String itemCode, int damageQty) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunctionHeidi.EVADamageItemQty(itemCode, damageQty);
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }

    /**
     * 32. 進行EVA回庫調整
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param inputObj 要調整的商品Array
     * @return boolean
     */
    public static boolean eVAAdjustItemQty(Context mContext, StringBuilder errMsg, JSONArray inputObj) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunctionHeidi.EVAAdjustItemQty(inputObj);
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }





    /*----------------D. Damage--------*/

    /**
     * 33. 查詢、調整Damage商品
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param inputObj 要轉瑕疵品的商品Array，無調整可傳Null
     * @return DamageItemPack
     */
    public static DamageItemPack damageItemQty(Context mContext, StringBuilder errMsg, JSONArray inputObj) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        DamageItemPack ret = null;

        try {
            json = pFunction.DamageItemQty(inputObj);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {

                ret = new DamageItemPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.damages = new DamageItem[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.damages[i] = new DamageItem();
                    ret.damages[i].ItemCode = jsonArray.getJSONObject(i).getString("ItemCode");
                    ret.damages[i].DrawNo = jsonArray.getJSONObject(i).getString("DrawNo");
                    ret.damages[i].SerialCode = jsonArray.getJSONObject(i).getString("SerialCode");
                    ret.damages[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.damages[i].ItemPriceUS = jsonArray.getJSONObject(i).getDouble("ItemPriceUS");
                    ret.damages[i].DamageQty = jsonArray.getJSONObject(i).getInt("DamageQty");
                    ret.damages[i].EndQty = jsonArray.getJSONObject(i).getInt("EndQty");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class DamageItem {

        public String ItemCode = "";
        public String DrawNo = "";
        public String SerialCode = "";
        public String ItemName = "";
        public double ItemPriceUS = 0;
        public int DamageQty = 0;
        public int EndQty = 0;
    }

    public static class DamageItemPack {

        public DamageItem[] damages;
    }


    /**
     * 34. 進行EGAS回庫Damage調整
     *
     * @param mContext    Activity Context
     * @param errMsg      錯誤訊息
     * @param AdjustArray 要調整的商品Array
     * @return DamageItemPack
     */
    public static boolean eGASDamageItemQty(Context mContext, StringBuilder errMsg, JSONArray AdjustArray) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunctionHeidi.EGASDamageItemQty(AdjustArray);
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }

    /**
     * 35. 進行EVA回庫Damage調整
     *
     * @param mContext    Activity Context
     * @param errMsg      錯誤訊息
     * @param AdjustArray 要調整的商品Array
     * @return DamageItemPack
     */
    public static boolean eVADamageItemQty(Context mContext, StringBuilder errMsg,
                                           JSONArray AdjustArray) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunctionHeidi.EVADamageItemQty(AdjustArray);
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }


    /*----------------F. Transfer--------*/


    /**
     * 36. 進行Transfer調整
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param inputObj 要移儲的商品Array
     * @return boolean
     */
    public static boolean transferItemQty(Context mContext, StringBuilder errMsg, JSONObject inputObj) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunction.TransferItemQty(inputObj);
            retCode = json.getString("ReturnCode");

            if (!retCode.equals("0")) {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }

    public static class TransferAdjItemPack {

        public String TransferNo = "";
        public String CarFrom = "";
        public String CarTo = "";
        public String TransferType = "";
        public TransferAdjItem TransferItem[];

        public TransferAdjItemPack(String no, String from, String to,
                                   String type, TransferAdjItem[] i) {
            TransferNo = no;
            CarFrom = from;
            CarTo = to;
            TransferType = type;
            TransferItem = i;
        }
    }

    public static class TransferAdjItem {

        public String ItemCode = "";
        public int TransferQty = 0;
    }


    /**
     * 37. 查詢Transfer單據內容
     *
     * @param mContext     Activity Context
     * @param errMsg       錯誤訊息
     * @param TransferNo   要查詢的Transfer No，全部查詢就傳入Null
     * @param TransferType 要查詢的Transfer Type，參數使用'IN' or 'OUT'，全部查詢就傳入Null
     * @return TransferItemPack
     */
    public static TransferItemPack queryTransferItemQty(Context mContext, StringBuilder errMsg,
                                                        String TransferNo, String TransferType) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        TransferItemPack ret = null;

        try {
            json = pFunction.QueryTransferItemQty(TransferNo, TransferType);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new TransferItemPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.transfers = new TransferItem[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.transfers[i] = new TransferItem();
                    ret.transfers[i].ItemCode = jsonArray.getJSONObject(i).getString("ItemCode");
                    ret.transfers[i].DrawNo = jsonArray.getJSONObject(i).getString("DrawNo");
                    ret.transfers[i].SerialCode = jsonArray.getJSONObject(i).getString("SerialCode");
                    ret.transfers[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.transfers[i].ReceiptNo = jsonArray.getJSONObject(i).getString("ReceiptNo");
                    ret.transfers[i].TransferNo = jsonArray.getJSONObject(i).getString("TransferNo");
                    ret.transfers[i].CarFrom = jsonArray.getJSONObject(i).getString("CarFrom");
                    ret.transfers[i].CarTo = jsonArray.getJSONObject(i).getString("CarTo");
                    ret.transfers[i].Qty = jsonArray.getJSONObject(i).getString("Qty");
                    ret.transfers[i].TransferType = jsonArray.getJSONObject(i).getString("TransferType");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class TransferItem {

        public String ItemCode = "";
        public String DrawNo = "";
        public String SerialCode = "";
        public String ItemName = "";
        public String ReceiptNo = "";
        public String TransferNo = "";
        public String CarFrom = "";
        public String CarTo = "";
        public String Qty = "";
        public String TransferType = "";
    }

    public static class TransferItemPack {

        public TransferItem[] transfers;
    }

    public static class ItemCodeAndNumber {

        String ItemCode = "";
        int Number = 0;

        public ItemCodeAndNumber(String s, int i) {
            ItemCode = s;
            Number = i;
        }

        public String getItemCode() {
            return ItemCode;
        }

        public void setItemCode(String itemCode) {
            ItemCode = itemCode;
        }

        public int getNumber() {
            return Number;
        }

        public void setNumber(int number) {
            Number = number;
        }
    }


    /**
     * 38. 取消Transfer交易內容
     *
     * @param mContext  Activity Context
     * @param errMsg    錯誤訊息
     * @param ReceiptNo 要取消的Transfer Receipt No
     * @return boolean
     */
    public static boolean cancelTransfer(Context mContext, StringBuilder errMsg,
                                         String ReceiptNo) {

        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunction.CancelTransfer(ReceiptNo);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                return true;
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }




    /*----------------G. Preorder & VIP--------*/

    public static PreorderInfoPack getPRVPCanSaleRefund(Context mContext, StringBuilder errMsg,
                                                        String SecSeq, String PreorderNo, String[] PreorderType, String SalesType) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        PreorderInfoPack ret = null;

        try {
            json = pFunctionHeidi.GetPRVPCanSale(SecSeq, PreorderNo, PreorderType, SalesType);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new PreorderInfoPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.info = new PreorderInformation[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.info[i] = new PreorderInformation();
                    ret.info[i].SecSeq = jsonArray.getJSONObject(i).getString("SecSeq");
                    if (!jsonArray.getJSONObject(i).isNull("ReceiptNo")) {
                        ret.info[i].ReceiptNo = jsonArray.getJSONObject(i).getString("ReceiptNo");
                    }
                    ret.info[i].PreorderNO = jsonArray.getJSONObject(i).getString("PreorderNO");
                    ret.info[i].MileDisc = jsonArray.getJSONObject(i).getDouble("MileDisc");
                    ret.info[i].ECouponCurrency = jsonArray.getJSONObject(i).getString("ECouponCurrency");
                    ret.info[i].ECoupon = jsonArray.getJSONObject(i).getDouble("ECoupon");
                    ret.info[i].CardType = jsonArray.getJSONObject(i).getString("CardType");
                    ret.info[i].CardNo = jsonArray.getJSONObject(i).getString("CardNo");
                    ret.info[i].TravelDocument = jsonArray.getJSONObject(i).getString("TravelDocument");
                    ret.info[i].CurDvr = jsonArray.getJSONObject(i).getString("CurDvr");
                    ret.info[i].PayAmt = jsonArray.getJSONObject(i).getDouble("PayAmt");
                    ret.info[i].Amount = jsonArray.getJSONObject(i).getDouble("Amount");
                    ret.info[i].Discount = jsonArray.getJSONObject(i).getDouble("Discount");
                    ret.info[i].PNR = jsonArray.getJSONObject(i).getString("PNR");
                    ret.info[i].PassengerName = jsonArray.getJSONObject(i).getString("PassengerName");
                    ret.info[i].PreorderType = jsonArray.getJSONObject(i).getString("PreorderType");
                    ret.info[i].SaleFlag = jsonArray.getJSONObject(i).getString("SaleFlag");
                    ret.info[i].EGASSaleFlag = jsonArray.getJSONObject(i).getString("EGASSaleFlag");
                    ret.info[i].EVASaleFlag = jsonArray.getJSONObject(i).getString("EVASaleFlag");

                    if (!jsonArray.getJSONObject(i).isNull("Detail")) {
                        JSONArray jsonItems = jsonArray.getJSONObject(i).getJSONArray("Detail");
                        ret.info[i].items = new PreorderItem[jsonItems.length()];
                        for (int j = 0; j < jsonItems.length(); j++) {
                            ret.info[i].items[j] = new PreorderItem();
                            ret.info[i].items[j].DrawNo = jsonItems.getJSONObject(j).getString("DrawNo");
                            ret.info[i].items[j].SerialCode = jsonItems.getJSONObject(j).getString("SerialCode");
                            ret.info[i].items[j].OriginalPrice = jsonItems.getJSONObject(j).getDouble("OriginalPrice");
                            ret.info[i].items[j].ItemName = jsonItems.getJSONObject(j).getString("ItemName");
                            ret.info[i].items[j].ItemCode = jsonItems.getJSONObject(j).getString("ItemCode");
                            ret.info[i].items[j].SalesPrice = jsonItems.getJSONObject(j).getDouble("SalesPrice");
                            ret.info[i].items[j].SalesPriceTW = jsonItems.getJSONObject(j).getInt("SalesPriceTW");
                            ret.info[i].items[j].SalesQty = jsonItems.getJSONObject(j).getInt("SalesQty");
                        }
                    } else {
                        ret.info[i].items = new PreorderItem[0];
                    }
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;

    }


    /**
     * 39. 取得Preorder資訊
     *
     * @param mContext     Activity Context
     * @param errMsg       錯誤訊息
     * @param PreorderNo   Preorder單據號碼，不使用請傳Null
     * @param PreorderType Preorder 單據類別(“PR” or “VS” or “VP”) ，不使用請傳Null
     * @param SalesType    S:已取貨，N:未有動作(資料庫會回出R與N)，不使用請傳Null
     * @return boolean
     */
    public static PreorderInfoPack getPreorderInfo(Context mContext, StringBuilder errMsg, String PreorderNo, String[] PreorderType, String SalesType) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        PreorderInfoPack ret = null;

        try {
            json = pFunctionHeidi.GetPreorderInfo(PreorderNo, PreorderType, SalesType);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {

                ret = new PreorderInfoPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.info = new PreorderInformation[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.info[i] = new PreorderInformation();
                    ret.info[i].SecSeq = jsonArray.getJSONObject(i).getString("SecSeq");
                    if (!jsonArray.getJSONObject(i).isNull("ReceiptNo")) {
                        ret.info[i].ReceiptNo = jsonArray.getJSONObject(i).getString("ReceiptNo");
                    }
                    ret.info[i].PreorderNO = jsonArray.getJSONObject(i).getString("PreorderNO");
                    ret.info[i].MileDisc = jsonArray.getJSONObject(i).getDouble("MileDisc");
                    ret.info[i].ECouponCurrency = jsonArray.getJSONObject(i).getString("ECouponCurrency");
                    ret.info[i].ECoupon = jsonArray.getJSONObject(i).getDouble("ECoupon");
                    ret.info[i].CardType = jsonArray.getJSONObject(i).getString("CardType");
                    ret.info[i].CardNo = jsonArray.getJSONObject(i).getString("CardNo");
                    ret.info[i].TravelDocument = jsonArray.getJSONObject(i).getString("TravelDocument");
                    ret.info[i].CurDvr = jsonArray.getJSONObject(i).getString("CurDvr");
                    ret.info[i].PayAmt = jsonArray.getJSONObject(i).getDouble("PayAmt");
                    ret.info[i].Amount = jsonArray.getJSONObject(i).getDouble("Amount");
                    ret.info[i].Discount = jsonArray.getJSONObject(i).getDouble("Discount");
                    ret.info[i].PNR = jsonArray.getJSONObject(i).getString("PNR");
                    ret.info[i].PassengerName = jsonArray.getJSONObject(i).getString("PassengerName");
                    ret.info[i].PreorderType = jsonArray.getJSONObject(i).getString("PreorderType");
                    ret.info[i].SaleFlag = jsonArray.getJSONObject(i).getString("SaleFlag");
                    ret.info[i].EGASSaleFlag = jsonArray.getJSONObject(i).getString("EGASSaleFlag");
                    ret.info[i].EVASaleFlag = jsonArray.getJSONObject(i).getString("EVASaleFlag");

                    if (!jsonArray.getJSONObject(i).isNull("Detail")) {
                        JSONArray jsonItems = jsonArray.getJSONObject(i).getJSONArray("Detail");
                        ret.info[i].items = new PreorderItem[jsonItems.length()];
                        for (int j = 0; j < jsonItems.length(); j++) {
                            ret.info[i].items[j] = new PreorderItem();
                            ret.info[i].items[j].SerialCode = jsonItems.getJSONObject(j).getString("SerialCode");
                            ret.info[i].items[j].OriginalPrice = jsonItems.getJSONObject(j).getDouble("OriginalPrice");
                            ret.info[i].items[j].ItemName = jsonItems.getJSONObject(j).getString("ItemName");
                            ret.info[i].items[j].ItemCode = jsonItems.getJSONObject(j).getString("ItemCode");
                            ret.info[i].items[j].SalesPrice = jsonItems.getJSONObject(j).getDouble("SalesPrice");
                            ret.info[i].items[j].SalesPriceTW = jsonItems.getJSONObject(j).getInt("SalesPriceTW");
                            ret.info[i].items[j].SalesQty = jsonItems.getJSONObject(j).getInt("SalesQty");
                            //todo add EndQTY
                        }
                    } else {
                        ret.info[i].items = new PreorderItem[0];
                    }
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class PreorderInformation implements Serializable {

        public String SecSeq = "";
        public String ReceiptNo = "";
        public String PreorderNO = ""; //Preorder No, VIP No
        public double MileDisc = 0; //里程折抵
        public String ECouponCurrency = "";
        public double ECoupon = 0; //E Coupon折扣卷
        public String CardType = "";
        public String CardNo = "";
        public String TravelDocument = "";
        public String CurDvr = "";
        public double PayAmt = 0;
        public double Amount = 0;
        public double Discount = 0;
        public String PNR = ""; //訂位代號
        public String PassengerName = "";
        public String PreorderType = ""; //PR, VS, VP
        public String SaleFlag = "";
        public String EGASSaleFlag = "";
        public String EVASaleFlag = "";

        public PreorderItem[] items;
    }

    public static class PreorderItem {

        public String DrawNo = "";
        public String SerialCode = "";
        public double OriginalPrice = 0;
        public String ItemCode = "";
        public String ItemName = "";
        public double SalesPrice = 0;
        public int SalesPriceTW = 0;
        public int SalesQty = 0;
    }

    public static class PreorderInfoPack {

        public PreorderInformation[] info;
    }


    public static VIPSaleHeader getVIPSaleHeader(StringBuilder errMsg, JSONObject json) {
        VIPSaleHeader ret = null;

        try {
            String retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new VIPSaleHeader();
                if (json.isNull("ResponseData")) {
                    return ret;
                }
                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret = new VIPSaleHeader();
                ret.PreorderNo = jsonArray.getJSONObject(0).getString("PreorderNo");
                ret.ReceiptNo = jsonArray.getJSONObject(0).getString("ReceiptNo");
                ret.OriUSDAmount = jsonArray.getJSONObject(0).getDouble("OriUSDAmount");
                ret.OriUSDAmount = jsonArray.getJSONObject(0).getDouble("OriUSDAmount");
                ret.USDAmount = jsonArray.getJSONObject(0).getDouble("USDAmount");
                ret.TWDAmount = jsonArray.getJSONObject(0).getDouble("TWDAmount");
                ret.PNR = jsonArray.getJSONObject(0).getString("PNR");
                ret.PassengerName = jsonArray.getJSONObject(0).getString("PassengerName");
                ret.DiscountRate = jsonArray.getJSONObject(0).getDouble("DiscountRate");
                ret.UpperLimitType = jsonArray.getJSONObject(0).getString("UpperLimitType");
                ret.UpperLimitDiscountNo = jsonArray.getJSONObject(0).getString("UpperLimitDiscountNo");
                ret.UpperLimit = jsonArray.getJSONObject(0).getInt("UpperLimit");

//          ret.items = new PreorderItem[jsonItems.length()];

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class VIPSaleHeaderPack {

        public VIPSaleHeader vips[];
    }

    public static class VIPSaleHeader {

        public String PreorderNo = "";
        public String ReceiptNo = "";
        public double OriUSDAmount = 0;
        public double USDAmount = 0;
        public double TWDAmount = 0;
        public String PNR = "";
        public String PassengerName = "";
        public double DiscountRate = 0;
        public String UpperLimitType = "";
        public String UpperLimitDiscountNo = "";
        public int UpperLimit = 0;
        public PreorderItem preorderItem[];
    }


    /**
     * 40. 儲存Preorder資訊 ( PR和VP使用 )
     *
     * @param mContext     Activity Context
     * @param errMsg       錯誤訊息
     * @param PreorderInfo Preorder銷售資訊
     * @return boolean
     */
    public static boolean savePreorderInfo(Context mContext, StringBuilder errMsg,
                                           JSONObject PreorderInfo) {

        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunctionHeidi.SavePreorderInfo(PreorderInfo);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                return true;
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }

    /**
     * 41. 退貨Preorder (PR和VP使用)
     *
     * @param mContext     Activity Context
     * @param errMsg       錯誤訊息
     * @param PreorderInfo 退貨PreorderInfo資訊
     * @return boolean
     */
    public static boolean refundPreorderInfo(Context mContext, StringBuilder errMsg,
                                             JSONObject PreorderInfo) {

        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunctionHeidi.RefundPreorderInfo(PreorderInfo);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                return true;
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }

    /**
     * 42. EGAS儲存Preorder狀態
     *
     * @param mContext   Activity Context
     * @param errMsg     錯誤訊息
     * @param PreorderNo Preorder單據號碼
     * @param SalesType  S = 已被取貨， N 未被取貨
     * @return boolean
     */
    public static boolean eGASSavePreorderState(Context mContext, StringBuilder errMsg, String PreorderNo, String SalesType) {

        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunctionHeidi.EGASSavePreorderState(PreorderNo, SalesType);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                return true;
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }


    /**
     * 43. EVA儲存Preorder狀態
     *
     * @param mContext   Activity Context
     * @param errMsg     錯誤訊息
     * @param PreorderNo Preorder單據號碼
     * @param SalesType  S = 已被取貨， N 未被取貨
     * @return boolean
     */
    public static boolean eVASavePreorderState(Context mContext, StringBuilder errMsg,
                                               String PreorderNo, String SalesType) {

        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunctionHeidi.EVASavePreorderState(PreorderNo, SalesType);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                return true;
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return false;
        }
        return retCode.equals("0");
    }



    /*----------------H. Sales Summery--------*/


    /**
     * 42. 取得該航段現金收受匯總
     *
     * @param mContext        Activity Context
     * @param errMsg          錯誤訊息
     * @param SummaryType     要計算的消費類別(‘DFS’ or ‘Upgrade’)
     * @param PaymentCurrency 要列入計算的幣別，不使用則傳Null
     * @return boolean
     */
    public static CashSummaryPack getCashSummary(Context mContext, StringBuilder errMsg,
                                                 String SummaryType, String PaymentCurrency) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CashSummaryPack ret = null;

        try {
//            json = pFunction.GetCashSummary(SummaryType, PaymentCurrency);
            json = new JSONObject();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CashSummaryPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.AmountSummary = jsonArray.getJSONObject(0).getDouble("AmountSummary");
                ret.list = new SummaryList[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new SummaryList();
                    ret.list[i].Currency = jsonArray.getJSONObject(i).getString("PreorderNO");
                    ret.list[i].Amount = jsonArray.getJSONObject(i).getDouble("MileDisc");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class CashSummaryPack {

        public double AmountSummary = 0;
        public SummaryList[] list;
    }

    public static class SummaryList {

        public String Currency = "";
        public double Amount = 0;
    }


    /**
     * 43.. 取得該航段信用卡收受匯總
     *
     * @param mContext        Activity Context
     * @param errMsg          錯誤訊息
     * @param SummaryType     要計算的消費類別(‘DFS’ or ‘Upgrade’)
     * @param PaymentCurrency 要列入計算的幣別，不使用則傳Null
     * @return boolean
     */
    public static CashSummaryPack getCardSummary(Context mContext, StringBuilder errMsg,
                                                 String SummaryType, String PaymentCurrency) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CashSummaryPack ret = null;

        try {
//            json = pFunction.GetCardSummary(SummaryType, PaymentCurrency);
            json = new JSONObject();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CashSummaryPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }
                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.AmountSummary = jsonArray.getJSONObject(0).getDouble("AmountSummary");
                ret.list = new SummaryList[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new SummaryList();
                    ret.list[i].Currency = jsonArray.getJSONObject(i).getString("PreorderNO");
                    ret.list[i].Amount = jsonArray.getJSONObject(i).getDouble("MileDisc");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }


    /**
     * 44.. 取得該航段信用卡收受資訊列表
     *
     * @param mContext        Activity Context
     * @param errMsg          錯誤訊息
     * @param SummaryType     要計算的消費類別(‘DFS’ or ‘Upgrade’)
     * @param PaymentCurrency 要列入計算的幣別，不使用則傳Null
     * @return boolean
     */
    public static CardPaymentPack getCardPaymentList(Context mContext, StringBuilder errMsg,
                                                     String SummaryType, String[] PaymentCurrency) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CardPaymentPack ret = null;

        try {
//            json = pFunction.GetCardPaymentList(SummaryType, PaymentCurrency);
            json = new JSONObject();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CardPaymentPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.payments = new CardPayment[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.payments[i] = new CardPayment();
                    ret.payments[i].CardType = jsonArray.getJSONObject(i).getString("CardType");
                    ret.payments[i].CardNo = jsonArray.getJSONObject(i).getString("CardNo");
                    ret.payments[i].ReceiptNo = jsonArray.getJSONObject(i).getString("ReceiptNo");
                    ret.payments[i].Currency = jsonArray.getJSONObject(i).getString("Currency");
                    ret.payments[i].Amount = jsonArray.getJSONObject(i).getDouble("Amount");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class CardPayment {

        public String CardType = "";
        public String CardNo = "";
        public String ReceiptNo = "";
        public String Currency = "";
        public double Amount = 0;
    }

    public static class CardPaymentPack {

        public CardPayment[] payments;
    }


    /**
     * 45. 取得該航段折扣資訊匯總
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @return boolean
     */
    public static DiscountSummaryPack getDiscountSummary(Context mContext, StringBuilder errMsg) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        DiscountSummaryPack ret = null;

        try {
//            json = pFunction.GetDiscountSummary();
            json = new JSONObject();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new DiscountSummaryPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.list = new DiscountSummary[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new DiscountSummary();
                    ret.list[i].VIPType = jsonArray.getJSONObject(i).getString("VIPType");
                    ret.list[i].VIPNo = jsonArray.getJSONObject(i).getString("VIPNo");
                    ret.list[i].Qty = jsonArray.getJSONObject(i).getInt("Qty");
                    ret.list[i].DiscountAmount = jsonArray.getJSONObject(i).getInt("DiscountAmount");
                    ret.list[i].Discount = jsonArray.getJSONObject(i).getDouble("Discount");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class DiscountSummary {

        public String VIPType = "";
        public String VIPNo = "";
        public int Qty = 0;
        public int DiscountAmount = 0;
        public double Discount = 0;
    }

    public static class DiscountSummaryPack {

        public DiscountSummary[] list;
    }


    /**
     * 46. 取得該航段Coupon收受匯總
     *
     * @param mContext    Activity Context
     * @param errMsg      錯誤訊息
     * @param CoouponType 折扣卷類別(”SC” or ”DC”)
     * @return boolean
     */
    public static CouponSummaryPack getCouponSummary(Context mContext, StringBuilder errMsg,
                                                     String CoouponType) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CouponSummaryPack ret = null;

        try {
//            json = pFunction.GetCouponSummary(CoouponType);
            json = new JSONObject();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CouponSummaryPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new CouponSummary[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new CouponSummary();
                    ret.list[i].ReceiptNo = jsonArray.getJSONObject(i).getString("VIPType");
                    ret.list[i].Amount = jsonArray.getJSONObject(i).getDouble("Amount");
                    ret.list[i].CouponNo = jsonArray.getJSONObject(i).getString("CouponNo");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class CouponSummary {

        public String ReceiptNo = "";
        public double Amount = 0;
        public String CouponNo = "";
    }

    public static class CouponSummaryPack {

        public CouponSummary[] list;
    }


    /**
     * 47.  取得該航段商品銷售匯總
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @return boolean
     */
    public static SalesSummaryPack getSalesSummary(Context mContext, StringBuilder errMsg, String SecSeq) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        SalesSummaryPack ret = null;

        try {
//            json = pFunction.GetSalesSummary (SecSeq);
            json = new JSONObject();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new SalesSummaryPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.list = new SalesSummary[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new SalesSummary();
                    ret.list[i].SerialNo = jsonArray.getJSONObject(i).getString("SerialNo");
                    ret.list[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.list[i].SaleQty = jsonArray.getJSONObject(i).getInt("SaleQty");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class SalesSummary {

        public String SerialNo = "";
        public String ItemName = "";
        public int SaleQty = 0;
    }

    public static class SalesSummaryPack {

        public SalesSummary[] list;
    }


    /**
     * 48.  取得該航段瑕疵品匯總
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @return boolean
     */
    public static DamageSummaryPack getDamageSummary(Context mContext, StringBuilder errMsg, String SecSeq) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        DamageSummaryPack ret = null;

        try {
//            json = pFunction.GetDamageSummary();
            json = new JSONObject();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new DamageSummaryPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.list = new DamageSummary[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new DamageSummary();
                    ret.list[i].SerialNo = jsonArray.getJSONObject(i).getString("SerialNo");
                    ret.list[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.list[i].DamageQty = jsonArray.getJSONObject(i).getInt("DamageQty");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class DamageSummary {

        public String SerialNo = "";
        public String ItemName = "";
        public int DamageQty = 0;
    }

    public static class DamageSummaryPack {

        public DamageSummary[] list;
    }


    /**
     * 49.  取得該航段調整匯總
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @return boolean
     */
    public static AdjuctSummaryPack getAdjuctSummary(Context mContext, StringBuilder errMsg, String SecSeq) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        AdjuctSummaryPack ret = null;

        try {
//            json = pFunction.GetAdjuctSummary();
            json = new JSONObject();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new AdjuctSummaryPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new AdjuctSummary[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new AdjuctSummary();
                    ret.list[i].SerialNo = jsonArray.getJSONObject(i).getString("SerialNo");
                    ret.list[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.list[i].AdjuctQty = jsonArray.getJSONObject(i).getInt("AdjuctQty");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class AdjuctSummary {

        public String SerialNo = "";
        public String ItemName = "";
        public int AdjuctQty = 0;
    }

    public static class AdjuctSummaryPack {

        public AdjuctSummary[] list;
    }


    /**
     * 50.  取得該航段移儲品銷售匯總
     *
     * @param mContext     Activity Context
     * @param errMsg       錯誤訊息
     * @param TransferType 移儲類別(”In” or ”Out”)，不使用則傳Null
     * @return boolean
     */
    public static TransferSummaryPack getTransferSummary(Context mContext, StringBuilder errMsg, String TransferType) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        TransferSummaryPack ret = null;

        try {
//            json = pFunction.GetTransferSummary();
            json = new JSONObject();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new TransferSummaryPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.list = new TransferSummary[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new TransferSummary();
                    ret.list[i].SerialNo = jsonArray.getJSONObject(i).getString("SerialNo");
                    ret.list[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.list[i].TransferType = jsonArray.getJSONObject(i).getString("TransferType");
                    ret.list[i].TransferQty = jsonArray.getJSONObject(i).getInt("TransferQty");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class TransferSummary {

        public String SerialNo = "";
        public String ItemName = "";
        public String TransferType = "";
        public int TransferQty = 0;
    }

    public static class TransferSummaryPack {

        public TransferSummary[] list;
    }



    /*----------------I. Discount Information--------*/

    /**
     * 51.  取得所有的被動式折扣類別
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @return AllDiscountTypePack
     */
    public static AllDiscountTypePack getAllDiscountType(Context mContext, StringBuilder errMsg) {

        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        AllDiscountTypePack ret = null;

        try {
            json = pFunction.GetAllDiscountType();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new AllDiscountTypePack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.discounts = new AllDiscountType[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.discounts[i] = new AllDiscountType();
                    ret.discounts[i].Type = jsonArray.getJSONObject(i).getString("Type");
                    ret.discounts[i].Description = jsonArray.getJSONObject(i).getString("Description");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class AllDiscountType implements Serializable {

        public String Type = "";
        public String Description = "";

        public AllDiscountType(String t, String d) {
            Type = t;
            Description = d;
        }

        public AllDiscountType() {
        }
    }

    public static class AllDiscountTypePack implements Serializable {

        public AllDiscountType[] discounts;
    }


    /**
     * 52.  確認是否屬於員工、PT、組員
     *
     * @param mContext   Activity Context
     * @param errMsg     錯誤訊息
     * @param EmployeeNo 要比對的員工工號
     * @return DiscountCheck
     */
    public static DiscountCheck checkEmployee(Context mContext, StringBuilder errMsg,
                                              String EmployeeNo) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        DiscountCheck ret = null;

        try {
            json = pFunction.CheckEmployee(EmployeeNo);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new DiscountCheck();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.DiscountType = jsonArray.getJSONObject(0).getString("DiscountType");

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class DiscountCheck {

        public String DiscountType = "";
    }


    /**
     * 53. 確認是否屬於聯名卡
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param CardNo   要比對的信用卡號
     * @return DiscountCheck
     */
    public static DiscountCheck checkCoBrandedCard(Context mContext, StringBuilder errMsg,
                                                   String CardNo) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        DiscountCheck ret = null;

        try {
            json = pFunction.CheckCoBrandedCard(CardNo);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new DiscountCheck();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.DiscountType = jsonArray.getJSONObject(0).getString("DiscountType");

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }


    /**
     * 54. 確認是否屬於會員卡
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param CardType 要比對的會員卡類別 (CD、CG、EC、EP)
     * @param CardNo   要比對的會員卡號
     * @return DiscountCheck
     */
    public static DiscountCheck checkMemnerCard(Context mContext, StringBuilder errMsg,
                                                String CardType, String CardNo) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        DiscountCheck ret = null;

        try {
            json = pFunction.CheckMemberCard(CardType, CardNo);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new DiscountCheck();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.DiscountType = jsonArray.getJSONObject(0).getString("DiscountType");

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }



    /*----------------J. System Functions--------*/

    /**
     * 55. 取得POS機號對應的SSID
     *
     * @param mContext  Activity Context
     * @param errMsg    錯誤訊息
     * @param MachineNo POS機號
     * @return boolean
     */
    public static WifiSSID getWifiSSID(Context mContext, StringBuilder errMsg,
                                       String MachineNo) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        WifiSSID ret = null;

        try {
            json = pFunction.GetWifiSSID(MachineNo);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new WifiSSID();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.MachineNo = jsonArray.getJSONObject(0).getString("MachineNo");
                ret.SSID = jsonArray.getJSONObject(0).getString("SSID");

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class WifiSSID {

        public String MachineNo = "";
        public String SSID = "";
    }


    /**
     * 56. 取得POS內的Log
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @return boolean
     */
    public static SystemLogPack getSystemLog(Context mContext, StringBuilder errMsg) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        SystemLogPack ret = null;

        try {
            json = pFunction.GetSystemLog();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new SystemLogPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.list = new SystemLogInfo[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new SystemLogInfo();
                    ret.list[i].SearilNo = jsonArray.getJSONObject(i).getInt("SerialNo");
                    ret.list[i].SystemDate = jsonArray.getJSONObject(i).getString("SystemDate");
                    ret.list[i].SecSeq = jsonArray.getJSONObject(i).getString("SecSeq");
                    ret.list[i].LogType = jsonArray.getJSONObject(i).getString("LogType");
                    ret.list[i].OperationName = jsonArray.getJSONObject(i).getString("OperationName");
                    ret.list[i].FunctionName = jsonArray.getJSONObject(i).getString("FunctionName");
                    ret.list[i].LogText = jsonArray.getJSONObject(i).getString("LogText");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class SystemLogInfo {

        public int SearilNo = 0;
        public String SystemDate = "";
        public String SecSeq = "";
        public String LogType = "";
        public String OperationName = "";
        public String FunctionName = "";
        public String LogText = "";
    }

    public static class SystemLogPack {

        public SystemLogInfo[] list;
    }

    /**
     *57. 產生新的銷售資料庫
     *
     * @param mContext Activity Context
     * @param errMsg  錯誤訊息
     * @return boolean
     */

    /**
     * 58. 產生新的黑名單資料庫
     *
     * @param mContext Activity Context
     * @param errMsg  錯誤訊息
     * @return boolean
     */

    /**
     * 59. 寫入銷售資料庫
     *
     * @param mContext Activity Context
     * @param errMsg  錯誤訊息
     * @return boolean
     */

    /**
     * 60. 寫入黑名單資料庫
     *
     * @param mContext Activity Context
     * @param errMsg  錯誤訊息
     * @return boolean
     */


    /**
     * 取得CUSS資訊
     */
    public static CUSSInfoPack getCUSSInfo(Context mContext, StringBuilder errMsg, String SecSeq) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CUSSInfoPack ret = null;

        try {
            json = pFunctionHeidi.GetCUSSInfo(SecSeq);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CUSSInfoPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new CUSSInfo[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new CUSSInfo();
                    ret.list[i].ReceiptNo = jsonArray.getJSONObject(i).getString("ReceiptNo");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return ret;
    }

    public static class CUSSInfo {

        public String ReceiptNo = "";
    }

    public static class CUSSInfoPack {

        public CUSSInfo[] list;
    }

    /**
     * 取得各商品銷售數量、銷售總金額
     */
    public static TotalSalesSummaryPack getTotalSalesSummary(Context mContext, StringBuilder errMsg, String SecSeq) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        TotalSalesSummaryPack ret = null;

        try {
            json = pFunctionHeidi.GetSalesTotalSummary(SecSeq);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new TotalSalesSummaryPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new TotalSalesSummary[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new TotalSalesSummary();
                    ret.list[i].SerialCode = jsonArray.getJSONObject(i).getString("SerialCode");
                    ret.list[i].ItemCode = jsonArray.getJSONObject(i).getString("ItemCode");
                    ret.list[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.list[i].TotalQty = jsonArray.getJSONObject(i).getInt("TotalQty");
                    ret.list[i].TotalPrice = jsonArray.getJSONObject(i).getDouble("TotalPrice");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return ret;
    }

    public static class TotalSalesSummary {

        public String SerialCode = "";
        public String ItemCode = "";
        public String ItemName = "";
        public int TotalQty = 0;
        public double TotalPrice = 0;
    }

    public static class TotalSalesSummaryPack {

        public TotalSalesSummary[] list;
    }

    /**
     * 取得所有付款資訊
     */
    public static PaymentInfoPack getPaymentInfo(Context mContext, StringBuilder errMsg, String SecSeq, String PreOrderNo, String ReceiptNo) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        PaymentInfoPack ret = null;

        try {
            json = pFunctionHeidi.GetPaymentInfo(SecSeq, PreOrderNo, ReceiptNo);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new PaymentInfoPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new PaymentInfo[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new PaymentInfo();
                    ret.list[i].Currency = jsonArray.getJSONObject(i).getString("Currency");
                    ret.list[i].Amount = jsonArray.getJSONObject(i).getInt("Amount");
                    ret.list[i].USDAmount = jsonArray.getJSONObject(i).getDouble("USDAmount");
                    ret.list[i].PayBy = jsonArray.getJSONObject(i).getString("PayBy");
                    ret.list[i].CardNo = jsonArray.getJSONObject(i).getString("CardNo");
                    ret.list[i].CardType = jsonArray.getJSONObject(i).getString("CardType");
                    ret.list[i].CardName = jsonArray.getJSONObject(i).getString("CardName");
                    ret.list[i].CardDate = jsonArray.getJSONObject(i).getString("CardDate");
                    ret.list[i].ReceiptNo = jsonArray.getJSONObject(i).getString("ReceiptNo");
                    ret.list[i].PreorderNo = jsonArray.getJSONObject(i).getString("PreorderNo");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return ret;
    }

    public static class PaymentInfo {

        public String Currency = "";
        public int Amount = 0;
        public double USDAmount = 0;
        public String PayBy = "";
        public String CardNo = "";
        public String CardType = "";
        public String CardName = "";
        public String CardDate = "";
        public String ReceiptNo = "";
        public String PreorderNo = "";
    }

    public static class PaymentInfoPack {

        public PaymentInfo[] list;
    }

    /**
     * 取得該航段Coupon收受匯總(列印用)
     */
    public static CouponInfoSummaryPack getCouponInfoSummary(Context mContext, StringBuilder errMsg, String CouponType) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CouponInfoSummaryPack ret = null;

        try {
            json = pFunctionHeidi.GetCouponInfoSummary(CouponType);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CouponInfoSummaryPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new CouponInfoSummary[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new CouponInfoSummary();
                    ret.list[i].Amount = jsonArray.getJSONObject(i).getInt("Amount");
                    ret.list[i].ReceiptNo = jsonArray.getJSONObject(i).getString("ReceiptNo");
                    ret.list[i].CouponNo = jsonArray.getJSONObject(i).getString("CouponNo");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return ret;
    }

    public static class CouponInfoSummary {

        public String ReceiptNo = "";
        public int Amount = 0;
        public String CouponNo = "";
    }

    public static class CouponInfoSummaryPack {

        public CouponInfoSummary[] list;
    }


    /**
     * 統計各種貨幣的銷售現金金額。(找錢時，有的貨幣可能出現負數。)
     *
     * @param PaymentCurrency 要列入計算的幣別
     */
    public static CashTotalAmtPack getCashTotalAmt(Context mContext, StringBuilder errMsg, String PaymentCurrency) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CashTotalAmtPack ret = null;

        try {
            json = pFunctionHeidi.GetCashTotalAmt(PaymentCurrency);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CashTotalAmtPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new CashTotalAmt[jsonArray.length()];
                ret.list[0] = new CashTotalAmt();
                ret.list[0].Amount = jsonArray.getJSONObject(0).getInt("Amount");

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return ret;
    }

    public static class CashTotalAmt {

        public int Amount = 0;
    }

    public static class CashTotalAmtPack {

        public CashTotalAmt[] list;
    }


    public static CashTotalAmtPack getUpgradeAmount(Context mContext, StringBuilder errMsg, String PaymentCurrency) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CashTotalAmtPack ret = null;

        try {
            json = pFunctionHeidi.GetUpgradeAmount(PaymentCurrency);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CashTotalAmtPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new CashTotalAmt[jsonArray.length()];
                ret.list[0] = new CashTotalAmt();
                ret.list[0].Amount = jsonArray.getJSONObject(0).getInt("Amount");

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return ret;
    }


    /**
     * 取得PreOrder各卡別及幣別金額
     */
    public static PreOrderSummaryPack getPreOrderSummary(Context mContext, StringBuilder errMsg) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        PreOrderSummaryPack ret = null;

        try {
            json = pFunctionHeidi.GetPreOrderSummary();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new PreOrderSummaryPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new PreOrderSummary[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new PreOrderSummary();
                    ret.list[i].TotalAmount = jsonArray.getJSONObject(i).getInt("Amount");
                    ret.list[i].CardType = jsonArray.getJSONObject(i).getString("CardType");
                    ret.list[i].CurDvr = jsonArray.getJSONObject(i).getString("CurDvr");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return ret;
    }

    public static class PreOrderSummary {

        public String CardType = "";
        public int TotalAmount = 0;
        public String CurDvr = "";
    }

    public static class PreOrderSummaryPack {

        public PreOrderSummary[] list;
    }

    /**
     * 取得升艙等明細
     */
    public static UpgradeSummaryPack getUpgradeSummary(Context mContext, StringBuilder errMsg) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        UpgradeSummaryPack ret = null;

        try {
            json = pFunctionHeidi.GetUpgradeSummary();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new UpgradeSummaryPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new UpgradeSummary[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new UpgradeSummary();
                    ret.list[i].OriginalClass = jsonArray.getJSONObject(i).getString("OriginalClass");
                    ret.list[i].NewClass = jsonArray.getJSONObject(i).getString("NewClass");
                    ret.list[i].TotalQty = jsonArray.getJSONObject(i).getInt("TotalQty");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }

        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return ret;
    }

    public static class UpgradeSummary {

        public String OriginalClass = "";
        public String NewClass = "";
        public int TotalQty = 0;
    }

    public static class UpgradeSummaryPack {

        public UpgradeSummary[] list;
    }


    public static CreditCardAmtPack getCreditCardAmtPack(Context mContext, StringBuilder errMsg, String Currency) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CreditCardAmtPack ret = null;

        try {
            json = pFunctionHeidi.GetCreditCardAmt(Currency);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CreditCardAmtPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new CreditCardAmt[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new CreditCardAmt();
                    ret.list[i].ReceiptNo = jsonArray.getJSONObject(i).getString("ReceiptNo");
                    ret.list[i].CardType = jsonArray.getJSONObject(i).getString("CardType");
                    ret.list[i].CardNo = jsonArray.getJSONObject(i).getString("CardNo");
                    ret.list[i].TotalPrice = jsonArray.getJSONObject(i).getInt("Amount");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return ret;
    }

    public static class CreditCardAmt {

        public String ReceiptNo = "";
        public String CardType = "";
        public String CardNo = "";
        public int TotalPrice = 0;
    }

    public static class CreditCardAmtPack {

        public CreditCardAmt[] list;
    }


    /**
     * 取得移儲(Out)匯總
     */
    public static TransferInOutSummaryPack getTransferOutSummary(Context mContext, StringBuilder errMsg) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        TransferInOutSummaryPack ret = null;

        try {
            json = pFunctionHeidi.GetTransferOutSummary();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new TransferInOutSummaryPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new TransferInOutSummary[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new TransferInOutSummary();
                    ret.list[i].CarFrom = jsonArray.getJSONObject(i).getString("CarFrom");
                    ret.list[i].CarTo = jsonArray.getJSONObject(i).getString("CarTo");
                    ret.list[i].ItemCode = jsonArray.getJSONObject(i).getString("ItemCode");
                    ret.list[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.list[i].SerialCode = jsonArray.getJSONObject(i).getString("SerialCode");
                    ret.list[i].Qty = jsonArray.getJSONObject(i).getInt("Qty");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return ret;
    }

    /**
     * 取得移儲(In)匯總
     */
    public static TransferInOutSummaryPack getTransferInSummary(Context mContext, StringBuilder errMsg) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        TransferInOutSummaryPack ret = null;

        try {
            json = pFunctionHeidi.GetTransferInSummary();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new TransferInOutSummaryPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new TransferInOutSummary[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new TransferInOutSummary();
                    ret.list[i].CarFrom = jsonArray.getJSONObject(i).getString("CarFrom");
                    ret.list[i].CarTo = jsonArray.getJSONObject(i).getString("CarTo");
                    ret.list[i].ItemCode = jsonArray.getJSONObject(i).getString("ItemCode");
                    ret.list[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.list[i].SerialCode = jsonArray.getJSONObject(i).getString("SerialCode");
                    ret.list[i].Qty = jsonArray.getJSONObject(i).getInt("Qty");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return ret;
    }

    public static class TransferInOutSummary {

        public String CarFrom = "";
        public String CarTo = "";
        public String ItemCode = "";
        public String ItemName = "";
        public String SerialCode = "";
        public int Qty = 0;
    }

    public static class TransferInOutSummaryPack {

        public TransferInOutSummary[] list;
    }


    /**
     * 取得現金及信用卡付款資訊 (列印銷售收據)
     */
    public static SalesReceiptPack getPaymentSalesInfo(Context mContext, StringBuilder errMsg,
                                                       String ReceiptNo, String PayBy, String Amount, String Status) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        SalesReceiptPack ret = null;

        try {
            json = pFunctionHeidi.GetPaymentSalesInfo(ReceiptNo, PayBy, Amount, Status);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new SalesReceiptPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new SalesReceipt[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new SalesReceipt();
                    ret.list[i].Currency = jsonArray.getJSONObject(i).getString("Currency");
                    ret.list[i].Amount = jsonArray.getJSONObject(i).getDouble("Amount");
                    ret.list[i].PayBy = jsonArray.getJSONObject(i).getString("PayBy");
                    ret.list[i].CardNo = jsonArray.getJSONObject(i).getString("CardNo");
                    ret.list[i].CardType = jsonArray.getJSONObject(i).getString("CardType");
                    ret.list[i].CardName = jsonArray.getJSONObject(i).getString("CardName");
                    ret.list[i].CardDate = jsonArray.getJSONObject(i).getString("CardDate");
                    ret.list[i].CouponNo = jsonArray.getJSONObject(i).getString("CouponNo");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return ret;
    }

    public static class SalesReceipt {

        public String Currency = "";
        public double Amount = 0;
        public String PayBy = "";
        public String CardNo = "";
        public String CardType = "";
        public String CardName = "";
        public String CardDate = "";
        public String CouponNo = "";
    }

    public static PromotionsInfoPack getPromotionsInfo(Context mContext, StringBuilder errMsg) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        PromotionsInfoPack ret = null;

        try {
            json = pFunctionHeidi.GetPromotionsInfo();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new PromotionsInfoPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new PromotionsInfo[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new PromotionsInfo();
                    ret.list[i].promotionTitles = jsonArray.getJSONObject(i).getString("promotionTitles");
                    ret.list[i].PromotionsDetail = jsonArray.getJSONObject(i).getString("PromotionsDetail");
                    ret.list[i].StartDate = jsonArray.getJSONObject(i).getString("StartDate");
                    ret.list[i].EndDate = jsonArray.getJSONObject(i).getString("EndDate");
                    ret.list[i].PromotionsCode = jsonArray.getJSONObject(i).getString("PromotionsCode");
                    ret.list[i].Note = jsonArray.getJSONObject(i).getString("Note");
                    ret.list[i].PrintType = jsonArray.getJSONObject(i).getString("PrintType");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return ret;
    }

    public static class SalesReceiptPack {

        public SalesReceipt[] list;
    }

    public static class PromotionsInfo {

        public String promotionTitles = "";
        public String PromotionsDetail = "";
        public String StartDate = "";
        public String EndDate = "";
        public String PromotionsCode = "";
        public String Note = "";
        public String PrintType = "";
    }

    public static class PromotionsInfoPack {

        public PromotionsInfo[] list;
    }


    // 取得信用卡該幣別最大付款額
    public static CardMaxAmountPack getCardMaxAmountInfo(Context mContext, StringBuilder errMsg, String CardType, String Currency) {
        pFunction = new PublicFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CardMaxAmountPack ret = null;

        try {
            json = pFunction.GetCardMaxAmountInfo(CardType, Currency);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CardMaxAmountPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret = new CardMaxAmountPack();
                ret.TWDMaxAmount = jsonArray.getJSONObject(0).getInt("TWDMaxAmount");
                ret.USDMaxAmount = jsonArray.getJSONObject(0).getInt("MaxAmount");
                return ret;

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return ret;
    }

    public static class CardMaxAmountPack {

        public int TWDMaxAmount = 0;
        public int USDMaxAmount = 0;
    }


    //取出 Item 頁面資料=EGAS
    public static ItemDataPack getModifyProductEGAS(Context mContext, StringBuilder errMsg, String SecSeq,
                                                    String Code, String DrawerNo, int Sort) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        ItemDataPack ret = null;

        try {
            json = pFunctionHeidi.GetModifyProductEGAS(SecSeq, Code, DrawerNo, Sort, null);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new ItemDataPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.items = new ItemData[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.items[i] = new ItemData();
                    ret.items[i].ItemCode = jsonArray.getJSONObject(i).getString("ItemCode");
                    ret.items[i].DrawNo = jsonArray.getJSONObject(i).getString("DrawNo");
                    ret.items[i].SerialCode = jsonArray.getJSONObject(i).getString("SerialCode");
                    ret.items[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.items[i].Remark = jsonArray.getJSONObject(i).getString("Remark");
                    ret.items[i].ItemPriceUS = jsonArray.getJSONObject(i).getDouble("ItemPriceUS");
                    ret.items[i].ItemPriceTW = jsonArray.getJSONObject(i).getDouble("ItemPriceTW");
                    ret.items[i].StandQty = jsonArray.getJSONObject(i).getInt("StandQty");
                    ret.items[i].StartQty = jsonArray.getJSONObject(i).getInt("StartQty");
                    ret.items[i].AdjustQty = jsonArray.getJSONObject(i).getInt("AdjustQty");
                    ret.items[i].SalesQty = jsonArray.getJSONObject(i).getInt("SalesQty");
                    ret.items[i].TransferQty = jsonArray.getJSONObject(i).getInt("TransferQty");
                    ret.items[i].DamageQty = jsonArray.getJSONObject(i).getInt("DamageQty");
                    ret.items[i].EndQty = jsonArray.getJSONObject(i).getInt("EndQty");
                    ret.items[i].EGASCheckQty = jsonArray.getJSONObject(i).getInt("EGASCheckQty");
                    ret.items[i].EGASDamageQty = jsonArray.getJSONObject(i).getInt("EGASDamageQty");
                    ret.items[i].EVACheckQty = jsonArray.getJSONObject(i).getInt("EVACheckQty");
                    ret.items[i].EVADamageQty = jsonArray.getJSONObject(i).getInt("EVADamageQty");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }


    //取出 Item 頁面資料=EVA
    public static ItemDataPack getModifyProductEVA(Context mContext, StringBuilder errMsg, String SecSeq,
                                                   String Code, String DrawerNo, int Sort) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        ItemDataPack ret = null;

        try {
            json = pFunctionHeidi.GetModifyProductEVA(SecSeq, Code, DrawerNo, Sort, null);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new ItemDataPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.items = new ItemData[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.items[i] = new ItemData();
                    ret.items[i].ItemCode = jsonArray.getJSONObject(i).getString("ItemCode");
                    ret.items[i].DrawNo = jsonArray.getJSONObject(i).getString("DrawNo");
                    ret.items[i].SerialCode = jsonArray.getJSONObject(i).getString("SerialCode");
                    ret.items[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.items[i].Remark = jsonArray.getJSONObject(i).getString("Remark");
                    ret.items[i].ItemPriceUS = jsonArray.getJSONObject(i).getDouble("ItemPriceUS");
                    ret.items[i].ItemPriceTW = jsonArray.getJSONObject(i).getDouble("ItemPriceTW");
                    ret.items[i].StandQty = jsonArray.getJSONObject(i).getInt("StandQty");
                    ret.items[i].StartQty = jsonArray.getJSONObject(i).getInt("StartQty");
                    ret.items[i].AdjustQty = jsonArray.getJSONObject(i).getInt("AdjustQty");
                    ret.items[i].SalesQty = jsonArray.getJSONObject(i).getInt("SalesQty");
                    ret.items[i].TransferQty = jsonArray.getJSONObject(i).getInt("TransferQty");
                    ret.items[i].DamageQty = jsonArray.getJSONObject(i).getInt("DamageQty");
                    ret.items[i].EndQty = jsonArray.getJSONObject(i).getInt("EndQty");
                    ret.items[i].EGASCheckQty = jsonArray.getJSONObject(i).getInt("EGASCheckQty");
                    ret.items[i].EGASDamageQty = jsonArray.getJSONObject(i).getInt("EGASDamageQty");
                    ret.items[i].EVACheckQty = jsonArray.getJSONObject(i).getInt("EVACheckQty");
                    ret.items[i].EVADamageQty = jsonArray.getJSONObject(i).getInt("EVADamageQty");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }


    //合併兩個JSONArray
    private static JSONArray joinJSONArray(JSONArray mData, JSONArray array) {
        StringBuffer buffer = new StringBuffer();
        try {
            int len = mData.length();
            for (int i = 0; i < len; i++) {
                JSONObject obj1 = (JSONObject) mData.get(i);
                if (i == len - 1)
                    buffer.append(obj1.toString());
                else
                    buffer.append(obj1.toString()).append(",");
            }
            len = array.length();
            if (len > 0)
                buffer.append(",");
            for (int i = 0; i < len; i++) {
                JSONObject obj1 = (JSONObject) array.get(i);
                if (i == len - 1)
                    buffer.append(obj1.toString());
                else
                    buffer.append(obj1.toString()).append(",");
            }
            buffer.insert(0, "[").append("]");
            return new JSONArray(buffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static SalesTotalList getSalesQty(Context mContext, StringBuilder errMsg) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        SalesTotalList ret = null;

        try {
            json = pFunctionHeidi.GetSalesQty();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new SalesTotalList();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.list = new SalesTotal[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.list[i] = new SalesTotal();
                    ret.list[i].ItemCode = jsonArray.getJSONObject(i).getString("ItemCode");
                    ret.list[i].SalesTotal = jsonArray.getJSONObject(i).getInt("SalesQty");
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class SalesTotal {

        public String ItemCode = "";
        public int SalesTotal = 0;
    }

    public static class SalesTotalList {

        public SalesTotal[] list;
    }


    public static String getBeginPreorderAllItemQty(Context mContext, StringBuilder errMsg, String SecSeq) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunctionHeidi.GetBeginPreorderAllItemQty(SecSeq);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                return jsonArray.getJSONObject(0).getString("SalesQty");

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return null;
    }

    public static String getPreorderEndInventoryItemQty(Context mContext, StringBuilder errMsg, String SecSeq) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;

        try {
            json = pFunctionHeidi.GetPreorderEndInventoryItemQty(SecSeq);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                return jsonArray.getJSONObject(0).getString("SalesQty");

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
        }
        return null;
    }


    /**
     * 產生或修改Transfer內容
     *
     * @param errMsg 錯誤訊息
     * @param json   商品資訊
     * @return BasketItemPack
     */
    public static TransferOutItemPack modifyTransfer(StringBuilder errMsg, JSONObject json) {
        String retCode;
        TransferOutItemPack ret = null;

        try {
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new TransferOutItemPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.ReceiptNo = jsonArray.getJSONObject(0).getString("ReceiptNo");

                JSONArray jsonItems = jsonArray.getJSONObject(0).getJSONArray("Items");
                ret.items = new TransferOutItem[jsonItems.length()];

                for (int i = 0; i < jsonItems.length(); i++) {
                    ret.items[i] = new TransferOutItem();
                    ret.items[i].ItemCode = jsonItems.getJSONObject(i).getString("ItemCode");
                    ret.items[i].SerialCode = jsonItems.getJSONObject(i).getString("SerialCode");
                    ret.items[i].DrawerNo = jsonItems.getJSONObject(i).getString("DrawerNo");
                    ret.items[i].ItemName = jsonItems.getJSONObject(i).getString("ItemName");
                    ret.items[i].POSStock = jsonItems.getJSONObject(i).getInt("POSStock");
                    ret.items[i].TransferQty = jsonItems.getJSONObject(i).getInt("TransferQty");
                    ret.items[i].GiftFlag = jsonItems.getJSONObject(i).getString("GiftFlag");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }

    public static class TransferOutItemPack {

        public String ReceiptNo = "";
        public TransferOutItem items[];
    }

    public static class TransferOutItem {

        public String ItemCode = "";
        public String SerialCode = "";
        public String DrawerNo = "";
        public String ItemName = "";
        public int POSStock = 0;
        public int TransferQty = 0;
        public String GiftFlag = "";
    }

    // VIP Sale Pay的Basket item
    public static VIPSalePayInfo getVIPSalePayInfo(StringBuilder errMsg, JSONObject json) {
        String retCode;
        VIPSalePayInfo ret = null;

        try {
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new VIPSalePayInfo();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.ReceiptNo = jsonArray.getJSONObject(0).getString("ReceiptNo");
                ret.ReceiptNo = jsonArray.getJSONObject(0).getString("ReceiptNo");
                ret.OriUSDAmount = jsonArray.getJSONObject(0).getDouble("OriUSDAmount");
                ret.USDAmount = jsonArray.getJSONObject(0).getInt("USDAmount");
                ret.TWDAmount = jsonArray.getJSONObject(0).getInt("TWDAmount");
                ret.PNR = jsonArray.getJSONObject(0).getString("PNR");
                ret.PassengerName = jsonArray.getJSONObject(0).getString("PassengerName");
                ret.DiscountRate = jsonArray.getJSONObject(0).getDouble("DiscountRate");
                ret.UpperLimitType = jsonArray.getJSONObject(0).getString("UpperLimitType");
                ret.UpperLimitDiscountNo = jsonArray.getJSONObject(0).getString("UpperLimitDiscountNo");
                ret.UpperLimit = jsonArray.getJSONObject(0).getInt("UpperLimit");

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }


    public static class VIPSalePayInfo {

        public String PreorderNo = "";
        public String ReceiptNo = "";
        public double OriUSDAmount = 0;
        public int USDAmount = 0;
        public int TWDAmount = 0;
        public String PNR = "";
        public String PassengerName = "";
        public double DiscountRate = 0;
        public String UpperLimitType = "";
        public String UpperLimitDiscountNo = "";
        public int UpperLimit = 0;
    }


    /**
     * 7. 取得特定組員密碼
     *
     * @param mContext Activity Context
     * @param errMsg   錯誤訊息
     * @param CrewID   組員ID
     * @return CrewInfo
     */
    public static CrewInfo getGetCrewPassword(Context mContext, StringBuilder errMsg, String CrewID) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        CrewInfo ret = null;

        try {
            json = pFunctionHeidi.GetCrewPassword(CrewID);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new CrewInfo();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.CrewID = jsonArray.getJSONObject(0).getString("CrewID");
                ret.CrewType = jsonArray.getJSONObject(0).getString("CrewType");
                ret.Password = jsonArray.getJSONObject(0).getString("Password");
                ret.Name = jsonArray.getJSONObject(0).getString("Name");
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }


    public static SalesItemDiscountPack getSalesItemDiscountReport(Context mContext, StringBuilder errMsg, String SecSeq) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        SalesItemDiscountPack ret = null;

        try {
            json = pFunctionHeidi.GetSalesItemDiscountReport(SecSeq);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {
                ret = new SalesItemDiscountPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }

                ret.items = new SalesItemDiscount[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.items[i] = new SalesItemDiscount();
                    ret.items[i].ItemCode = jsonArray.getJSONObject(i).getString("ItemCode");
                    ret.items[i].SalesPrice = jsonArray.getJSONObject(i).getDouble("SalesPrice");
                    ret.items[i].SalesQty = jsonArray.getJSONObject(i).getInt("SalesQty");
                    ret.items[i].Discount = jsonArray.getJSONObject(i).getDouble("Discount");
                    ret.items[i].VipNo = jsonArray.getJSONObject(i).getString("VipNo");
                    ret.items[i].SerialCode = jsonArray.getJSONObject(i).getString("SerialCode");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }


    public static class SalesItemDiscountPack {

        public SalesItemDiscount[] items;
    }


    public static class SalesItemDiscount {

        public String ItemCode = "";
        public double SalesPrice = 0;
        public int SalesQty = 0;
        public double Discount = 0;
        public String VipNo = "";
        public String SerialCode = "";
    }


    public static DamageItemPack getBeginInventoryDamageQty(Context mContext, StringBuilder errMsg, String SecSeq) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        DamageItemPack ret = null;

        try {
            json = pFunctionHeidi.GetBeginInventoryDamageQty(SecSeq);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {

                ret = new DamageItemPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.damages = new DamageItem[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.damages[i] = new DamageItem();
                    ret.damages[i].DamageQty = jsonArray.getJSONObject(i).getInt("Qty");
                    ret.damages[i].ItemCode = jsonArray.getJSONObject(i).getString("ItemCode");
                    ret.damages[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.damages[i].SerialCode = jsonArray.getJSONObject(i).getString("SerialCode");
                    ret.damages[i].DrawNo = jsonArray.getJSONObject(i).getString("DrawNo");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }


    public static DamageItemPack getEndInventoryDamageQty(Context mContext, StringBuilder errMsg, String SecSeq) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        DamageItemPack ret = null;

        try {
            json = pFunctionHeidi.GetEndInventoryDamageQty(SecSeq);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {

                ret = new DamageItemPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.damages = new DamageItem[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.damages[i] = new DamageItem();
                    ret.damages[i].DamageQty = jsonArray.getJSONObject(i).getInt("Qty");
                    ret.damages[i].ItemCode = jsonArray.getJSONObject(i).getString("ItemCode");
                    ret.damages[i].ItemName = jsonArray.getJSONObject(i).getString("ItemName");
                    ret.damages[i].SerialCode = jsonArray.getJSONObject(i).getString("SerialCode");
                    ret.damages[i].DrawNo = jsonArray.getJSONObject(i).getString("DrawNo");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }


    public static PreorderInfoPack getInventoryPreorderDetail(Context mContext, StringBuilder errMsg,
                                                              String SecSeq, String[] PreorderType, boolean isBeginInventory) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        PreorderInfoPack ret = null;

        try {
            json = pFunctionHeidi.GetInventoryPreorderDetail(SecSeq, PreorderType, isBeginInventory);
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {

                ret = new PreorderInfoPack();
                if (json.isNull("ResponseData")) {
                    return ret;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret.info = new PreorderInformation[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret.info[i] = new PreorderInformation();
                    ret.info[i].SecSeq = jsonArray.getJSONObject(i).getString("SecSeq");
                    if (!jsonArray.getJSONObject(i).isNull("ReceiptNo")) {
                        ret.info[i].ReceiptNo = jsonArray.getJSONObject(i).getString("ReceiptNo");
                    }
                    ret.info[i].PreorderNO = jsonArray.getJSONObject(i).getString("PreorderNO");
                    ret.info[i].MileDisc = jsonArray.getJSONObject(i).getDouble("MileDisc");
                    ret.info[i].ECouponCurrency = jsonArray.getJSONObject(i).getString("ECouponCurrency");
                    ret.info[i].ECoupon = jsonArray.getJSONObject(i).getDouble("ECoupon");
                    ret.info[i].CardType = jsonArray.getJSONObject(i).getString("CardType");
                    ret.info[i].CardNo = jsonArray.getJSONObject(i).getString("CardNo");
                    ret.info[i].TravelDocument = jsonArray.getJSONObject(i).getString("TravelDocument");
                    ret.info[i].CurDvr = jsonArray.getJSONObject(i).getString("CurDvr");
                    ret.info[i].PayAmt = jsonArray.getJSONObject(i).getDouble("PayAmt");
                    ret.info[i].Amount = jsonArray.getJSONObject(i).getDouble("Amount");
                    ret.info[i].Discount = jsonArray.getJSONObject(i).getDouble("Discount");
                    ret.info[i].PNR = jsonArray.getJSONObject(i).getString("PNR");
                    ret.info[i].PassengerName = jsonArray.getJSONObject(i).getString("PassengerName");
                    ret.info[i].PreorderType = jsonArray.getJSONObject(i).getString("PreorderType");
                    ret.info[i].SaleFlag = jsonArray.getJSONObject(i).getString("SaleFlag");
                    ret.info[i].EGASSaleFlag = jsonArray.getJSONObject(i).getString("EGASSaleFlag");
                    ret.info[i].EVASaleFlag = jsonArray.getJSONObject(i).getString("EVASaleFlag");

                    if (!jsonArray.getJSONObject(i).isNull("Detail")) {
                        JSONArray jsonItems = jsonArray.getJSONObject(i).getJSONArray("Detail");
                        ret.info[i].items = new PreorderItem[jsonItems.length()];
                        for (int j = 0; j < jsonItems.length(); j++) {
                            ret.info[i].items[j] = new PreorderItem();
                            ret.info[i].items[j].DrawNo = jsonItems.getJSONObject(j).getString("DrawNo");
                            ret.info[i].items[j].SerialCode = jsonItems.getJSONObject(j).getString("SerialCode");
                            ret.info[i].items[j].OriginalPrice = jsonItems.getJSONObject(j).getDouble("OriginalPrice");
                            ret.info[i].items[j].ItemName = jsonItems.getJSONObject(j).getString("ItemName");
                            ret.info[i].items[j].ItemCode = jsonItems.getJSONObject(j).getString("ItemCode");
                            ret.info[i].items[j].SalesPrice = jsonItems.getJSONObject(j).getDouble("SalesPrice");
                            ret.info[i].items[j].SalesPriceTW = jsonItems.getJSONObject(j).getInt("SalesPriceTW");
                            ret.info[i].items[j].SalesQty = jsonItems.getJSONObject(j).getInt("SalesQty");
                        }
                    } else {
                        ret.info[i].items = new PreorderItem[0];
                    }
                }

            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;

    }


    public static String[] getBinCode(Context mContext, StringBuilder errMsg) {
        pFunctionHeidi = new DBFunctions(mContext, getSecSeq());
        JSONObject json;
        String retCode;
        String[] ret = null;

        try {
            json = pFunctionHeidi.GetBinCode();
            retCode = json.getString("ReturnCode");

            if (retCode.equals("0")) {

                if (json.isNull("ResponseData")) {
                    return null;
                }

                JSONArray jsonArray = json.getJSONArray("ResponseData");
                if (jsonArray == null) {
                    return null;
                }
                ret = new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    ret[i] = jsonArray.getJSONObject(i).getString("BinCode");
                }
            } else {
                errMsg.append(json.getString("ReturnMessage"));
            }
        } catch (Exception ex) {
            errMsg.append(ex.getMessage());
            return null;
        }
        return ret;
    }
}
