package tw.com.regalscan.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.eva.Printer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.HashSet;
import java.util.Set;

import tw.com.regalscan.R;
import tw.com.regalscan.component.AESEncrypDecryp;
import tw.com.regalscan.db.Arith;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.FlightInfoPack;
import tw.com.regalscan.db02.DBQuery.Receipt;
import tw.com.regalscan.db02.DBQuery.ReceiptList;
import tw.com.regalscan.db02.DBQuery.SalesReceiptPack;
import tw.com.regalscan.db02.DBQuery.TransactionPaymentPack;

/**
 * Created by tp00175 on 2017/5/17.
 */

public class PrintAir {

    private Printer print = new Printer();

    private StringBuilder errMsg = new StringBuilder();

    private Context mContext;

    int SecSeq;

    String key;
    String _FlightNo;
    String _Data;
    String _CartNo;
    String _CAID;
    String _CPID;
    String _FLIGHT_DATE;
    String _Sector;

    public PrintAir(Context context, int SecSeq) {
        mContext = context;
        this.SecSeq = SecSeq;

        _FlightNo = FlightData.FlightNo;
        _Sector = FlightData.Sector; //起訖點簡稱
        _Data = FlightData.FlightDate;
        _CartNo = FlightData.CartNo;
        _CAID = FlightData.CrewID;
        _CPID = FlightData.PurserID;
        _FLIGHT_DATE = FlightData.FlightDate;
        key = _Data + _CartNo;

        if (_FlightNo.equals("") || _Sector.equals("") || _Data.equals("") || _CartNo.equals("") ||
                _CAID.equals("") || _CPID.equals("") || _FLIGHT_DATE.equals("") || key.equals("")) {

            FlightInfoPack flightPack = DBQuery.getFlightInfo(mContext, new StringBuilder());

            for (int i = 0; i < flightPack.flights.length; i++) {
                if (flightPack.flights[i].SecSeq.equals(String.valueOf(SecSeq))) {
                    _FlightNo = flightPack.flights[i].FlightNo;
                    _Sector = flightPack.flights[i].DepStn + "-"
                            + flightPack.flights[i].ArivStn;
                    _Data = flightPack.flights[i].FlightDate;
                    _CartNo = flightPack.flights[i].CarNo;
                    _CAID = flightPack.flights[i].CrewID;
                    _CPID = flightPack.flights[i].PurserID;
                    _FLIGHT_DATE = flightPack.flights[i].FlightDate;
                    key = _Data + _CartNo;
                    break;
                }
            }

        }
    }

    /**
     * 列印收據開頭
     */
    private void printHeader() {

        DateTimeZone dateTimeZone = DateTimeZone.forID("Asia/Taipei");
//    DateTimeZone dateTimeZone = DateTimeZone.forID("Europe/London");
        DateTime dateTime = new DateTime(dateTimeZone);
//    String year = String.valueOf(dateTime.getYear());
//    String month = String.format("%02d", dateTime.getMonthOfYear());
//    String day = String.format("%02d", dateTime.getDayOfMonth());
        String time = String.format("%02d", dateTime.getHourOfDay()) + ":" + String.format("%02d", dateTime.getMinuteOfHour());

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.eva_logo);
        int oldwidth = bitmap.getWidth();
        int oldheight = bitmap.getHeight();

        float scaleWidth = 384 / (float) oldwidth;
        float scaleHeight = 83 / (float) oldheight;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, oldwidth, oldheight, matrix, true);

        print.Open();

        print.printImg(resizedBitmap);

        print.printSpace(3);
        print.printLine();
        print.printSmallText("CA ID:    " + _CAID);
        print.printSmallText("CP ID:    " + _CPID);
        print.printSmallText("CART NO:  " + _CartNo);
        print.printSmallText("FLT NO:   " + _FlightNo);
        print.printSmallText("ROUTE:    " + _Sector);
//    print.printSmallText("TPE DATE: " + year + month + day);
        print.printSmallText("TPE DATE: " + _FLIGHT_DATE);
        print.printSmallText("TPE TIME: " + time);
    }

    /**
     * 列印結尾
     */
    private int printEnd() {
        print.printSpace(2);
        print.printSmallText("         End Print");
        print.printLine();
        print.printSpace(7);
        int ret = print.printPage();
        print.Close();

        // 沒紙回-1
        return ret;
    }


    /**
     * 列印開櫃報表
     */
    public int printBeginInventory() {
        int ret = 0;
        try {
            printHeader();
            print.printSpace(2);
            print.printBigText(" Begin Inventory");
            print.printLine();

            //列印 Drawer 00, 取得所有Preoder商品數量
            if (_CartNo.toLowerCase().contains("a")) {
                String preorderQty = DBQuery.getBeginPreorderAllItemQty(mContext, errMsg, Integer.toString(SecSeq));
                if (preorderQty == null) {
                    print.printSmallText("DRAWD0 " + "0" + " PreOrder/VIP");
                } else {
                    print.printSmallText("DRAWD0 " + preorderQty + " PreOrder/VIP");
                }
            }

            //列印 Drawer 與 Damage 庫存
            DBQuery.DrawNoPack drawNoPack = DBQuery.getAllDrawerNo(mContext, errMsg, Integer.toString(SecSeq), null, true);
            DBQuery.DamageItemPack damageItemPack = DBQuery.getBeginInventoryDamageQty(mContext, errMsg, Integer.toString(SecSeq));
            if (drawNoPack != null && drawNoPack.drawers[0] != null) {
                for (int i = 1; i < drawNoPack.drawers.length; i++) {

                    if (damageItemPack != null && damageItemPack.damages != null) {
                        int damageTotal = 0;
                        for (int j = 0; j < damageItemPack.damages.length; j++) {
                            if (drawNoPack.drawers[i].DrawNo.equals(damageItemPack.damages[j].DrawNo)) {
                                damageTotal += damageItemPack.damages[j].DamageQty;
                            }
                        }
                        if (damageTotal != 0) {
                            print.printSmallText("DRAW" + drawNoPack.drawers[i].DrawNo + " " + drawNoPack.drawers[i].Qty + " Damage:" + damageTotal);
                        } else {
                            print.printSmallText("DRAW" + drawNoPack.drawers[i].DrawNo + " " + drawNoPack.drawers[i].Qty);
                        }

                    } else {
                        print.printSmallText("DRAW" + drawNoPack.drawers[i].DrawNo + " " + drawNoPack.drawers[i].Qty);
                    }
                }
                print.printLine();
            }

            if (damageItemPack != null && damageItemPack.damages != null) {
                print.printSpace(2);
                print.printBigText("      Damage");
                print.printLine();
                print.printSmallText("No  Code    Item   Draw Qty");
                for (int i = 0; i < damageItemPack.damages.length; i++) {
                    String serialcode = damageItemPack.damages[i].SerialCode;
                    String itemcode = damageItemPack.damages[i].ItemCode;
                    String itemname = damageItemPack.damages[i].ItemName;
                    String draw = damageItemPack.damages[i].DrawNo;
                    int qty = damageItemPack.damages[i].DamageQty;
                    print.printSmallText(serialcode + " " + itemcode + " "
                            + String.format("%1$10s", itemname).substring(0, 6) + " "
                            + String.format("%1$-4s", draw) + " " + String.format("%1$3s", qty));
                }
                print.printLine();
            }

            if (_CartNo.toLowerCase().contains("a")) {
                //PreOrder
                FlightInfoPack flightInfo = DBQuery.getFlightInfo(mContext, errMsg);

                print.printSpace(2);
                print.printBigText("    PreOrder");
                print.printLine();

                if (flightInfo != null && flightInfo.flights != null) {
                    DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getInventoryPreorderDetail(
                            mContext, errMsg, Integer.toString(SecSeq), new String[]{"PR"}, true);

                    if (preorderInfoPack != null && preorderInfoPack.info != null) {
                        for (int i = 0; i < flightInfo.flights.length; i++) {
                            for (int j = 0; j < preorderInfoPack.info.length; j++) {
                                if (flightInfo.flights[i].SecSeq.equals(preorderInfoPack.info[j].SecSeq)) {
                                    print.printSpace(1);
                                    print.printSmallText(flightInfo.flights[i].FlightNo + "-" + flightInfo.flights[i].DepStn + " to " + flightInfo.flights[i].ArivStn);
                                    break;
                                }
                            }

                            for (int j = 0; j < preorderInfoPack.info.length; j++) {
                                if (flightInfo.flights[i].SecSeq.equals(preorderInfoPack.info[j].SecSeq)) {
                                    print.printSpace(1);
                                    print.printSmallText("  PreOrderNo:" + preorderInfoPack.info[j].PreorderNO);
                                    print.printSmallText("No");
                                    for (int k = 0; k < preorderInfoPack.info[j].items.length; k++) {
                                        String ItemName = "";
                                        if (preorderInfoPack.info[j].items[k].ItemName.length() < 18) {
                                            ItemName = String.format("%1$-17s", preorderInfoPack.info[j].items[k].ItemName);
                                        } else {
                                            ItemName = preorderInfoPack.info[j].items[k].ItemName.substring(0, 17);
                                        }
                                        print.printSmallText(
                                                preorderInfoPack.info[j].items[k].SerialCode + " " + ItemName + " D0 " + preorderInfoPack.info[j].items[k].SalesQty);
                                    }
                                }
                            }
                        }
                    }
                }
                print.printSpace(1);
                print.printLine();
                print.printSpace(2);

                //VIP
                print.printBigText("       VIP");
                print.printLine();

                if (flightInfo != null && flightInfo.flights != null) {
                    DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getInventoryPreorderDetail(
                            mContext, errMsg, Integer.toString(SecSeq), new String[]{"VP", "VS"}, true);

                    if (preorderInfoPack != null && preorderInfoPack.info != null) {
                        for (int i = 0; i < flightInfo.flights.length; i++) {

                            for (int j = 0; j < preorderInfoPack.info.length; j++) {
                                if (flightInfo.flights[i].SecSeq.equals(preorderInfoPack.info[j].SecSeq)) {
                                    print.printSpace(1);
                                    print.printSmallText(flightInfo.flights[i].FlightNo + "-" + flightInfo.flights[i].DepStn + " to " + flightInfo.flights[i].ArivStn);
                                    break;
                                }
                            }

                            for (int j = 0; j < preorderInfoPack.info.length; j++) {
                                if (flightInfo.flights[i].SecSeq.equals(preorderInfoPack.info[j].SecSeq)) {
                                    print.printSpace(1);
                                    if (preorderInfoPack.info[j].PreorderType.equals("VP")) {
                                        print.printSmallText("  VIP Paid:" + preorderInfoPack.info[j].PreorderNO);
                                    } else if (preorderInfoPack.info[j].PreorderType.equals("VS")) {
                                        print.printSmallText("  VIP Sale:" + preorderInfoPack.info[j].PreorderNO);
                                    }
                                    print.printSmallText("No");
                                    for (int k = 0; k < preorderInfoPack.info[j].items.length; k++) {
                                        String ItemName = "";
                                        if (preorderInfoPack.info[j].items[k].ItemName.length() < 18) {
                                            ItemName = String.format("%1$-17s", preorderInfoPack.info[j].items[k].ItemName);
                                        } else {
                                            ItemName = preorderInfoPack.info[j].items[k].ItemName.substring(0, 17);
                                        }
                                        print.printSmallText(
                                                preorderInfoPack.info[j].items[k].SerialCode + " " + ItemName + " D0 " + preorderInfoPack.info[j].items[k].SalesQty);
                                    }
                                }
                            }
                        }
                    }
                }
                print.printSpace(1);
                print.printLine();
            }

            print.printSpace(2);
            print.printSmallText("No");

            DBQuery.ItemDataPack itemDataPack = DBQuery.getProductInfo(mContext, errMsg, Integer.toString(SecSeq), null, null, 2);
            for (int i = 0; i < itemDataPack.items.length; i++) {
                String itemName = "";
                if (itemDataPack.items[i].ItemName.length() < 16) {
                    itemName = String.format("%1$-16s", itemDataPack.items[i].ItemName);
                } else {
                    itemName = itemDataPack.items[i].ItemName.substring(0, 16);
                }
                print.printSmallText(itemDataPack.items[i].SerialCode + " " + itemName + " "
                        + itemDataPack.items[i].DrawNo + " " + itemDataPack.items[i].StartQty);
            }

            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    /**
     * 列印更新盤點報表
     */
    public int printUpdateList() {
        int ret = 0;
        try {
//      printHeader();
//      print.printSpace(2);
//      print.printBigText("   Update List");
//      print.printLine();
//      print.printSmallText("No  Code    Ori Qty New Qty");
//
//      DBQuery.ItemDataPack itemDataPack = DBQuery.getProductInfo(mContext, errMsg, Integer.toString(SecSeq), null, null, 0);
//      for (int i = 0; i < itemDataPack.items.length; i++) {
//        String ItemCade = String.format("%1$20s", itemDataPack.items[i].ItemCode).substring(13, 20);
//        String StartQty = String.format("%1$20s", itemDataPack.items[i].StartQty).substring(12, 20);
////        String AdjustQty = String.format("%1$20s", itemDataPack.items[i].StartQty + itemDataPack.items[i].AdjustQty).substring(12, 20);
//        String AdjustQty = String.format("%1$20s", itemDataPack.items[i].EndQty).substring(12, 20);
//
//        if ( itemDataPack.items[i].StartQty != itemDataPack.items[i].EndQty) {
//          print.printSmallText(itemDataPack.items[i].SerialCode + " " + ItemCade + StartQty + AdjustQty);
//        }
//      }
//
//      ret = printEnd();
            DBQuery.ItemDataPack itemDataPack = DBQuery.getAdjustInfo(mContext, errMsg, Integer.toString(SecSeq), null, null, 0);
            if (itemDataPack != null && itemDataPack.items != null) {
                boolean flagPrint = false;
                for (int i = 0; i < itemDataPack.items.length; i++) {
                    if (itemDataPack.items[i].StartQty != itemDataPack.items[i].EndQty) {
                        flagPrint = true;
                        break;
                    }
                }

                if (flagPrint) {
                    printHeader();
                    print.printSpace(2);
                    print.printBigText("   Update List");
                    print.printLine();
                    print.printSmallText("No  Code    Ori Qty New Qty");

                    for (int i = 0; i < itemDataPack.items.length; i++) {
                        String ItemCade = String.format("%1$20s", itemDataPack.items[i].ItemCode).substring(13, 20);
                        String StartQty = String.format("%1$20s", itemDataPack.items[i].StartQty).substring(12, 20);
                        String AdjustQty = String.format("%1$20s", itemDataPack.items[i].EndQty).substring(12, 20);
                        if (itemDataPack.items[i].StartQty != itemDataPack.items[i].EndQty) {
                            print.printSmallText(itemDataPack.items[i].SerialCode + " " + ItemCade + StartQty + AdjustQty);
                        }
                    }

                    ret = printEnd();
                }

            }
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    /**
     * 列印售貨票據
     *
     * @param ReceiptNo  收據代碼
     * @param flag       是否列印簽單
     * @param VipOrderNo 如果不用放 ""
     */
    public int printSale(String ReceiptNo, int flag, String VipOrderNo) throws Exception {
        int ret;

        try {
            //flag=1:第1張
            //flag=0:第2張
            //flag=2:再印收據

            String sZ = "";
            boolean UseCard = false;
            String sDiscount = "0";
            String VipKind = "";
            String VipNo = "";
            String DiscountPrintType = "";

            printHeader();
            print.printSpace(2);
            print.printBigText("  Sale Receipt");
            print.printLine();
            print.printSmallText("Receipt No:" + ReceiptNo);

            if (!VipOrderNo.equals("")) {
                print.printSmallText("VIP No:" + VipOrderNo);
            }

            DBQuery.TransactionInfoPack infoPack = DBQuery.getDFSTransactionInfo(mContext, errMsg, ReceiptNo);
            if (infoPack != null) {
                if (!infoPack.info[0].SeatNo.equals("")) {
                    print.printSmallText("Seat No:" + infoPack.info[0].SeatNo);
                }
            }

            if (!infoPack.info[0].DiscountType.equals("")) {
                if (!infoPack.info[0].DiscountNo.equals("")) {
                    String ss;
                    switch (infoPack.info[0].DiscountType) {
                        case "CUB":
                            ss = AESEncrypDecryp.getDectyptData(infoPack.info[0].DiscountNo, key).substring(0, 8);
                            break;
                        case "AE":
                        case "AEG":
                            ss = infoPack.info[0].DiscountNo.substring(0, 6);
                            break;
                        default:
                            ss = infoPack.info[0].DiscountNo;
                            break;
                    }
                    print.printSmallText("Discount:" + infoPack.info[0].DiscountType + "-" + ss);
                } else {
                    print.printSmallText("Discount:" + infoPack.info[0].DiscountType);
                }
            }

            print.printLine();
            //列印物品的流水號、物品名稱、數量、價錢
            print.printSmallText("No  Item        Qty   Total");

            for (int i = 0; i < infoPack.info[0].items.length; i++) {
                String itemName = String.format("%1$-20s", infoPack.info[0].items[i].ItemName).substring(0, 10);
                String SalesQty = String.format("%1$4s", infoPack.info[0].items[i].SalesQty);
                String total = Tools.getModiMoneyString(Arith.mul(infoPack.info[0].items[i].SalesQty, infoPack.info[0].items[i].OriginalPrice));
                total = String.format("%1$10s", total).substring(3);
//        if (!PreOrderNo.equals("") && sDiscount.equals("0")) {
//
//        } else {
//          if (total.indexOf('.') > 0) {
//            String[] temp;
//            temp = total.split("\\.");
//            total = temp[0];
//          }
//        }

                if (itemName.substring(itemName.length() - 1).equals("-")) {
                    itemName = itemName.substring(0, itemName.length() - 1) + " ";
                }

                print.printSmallText(infoPack.info[0].items[i].SerialNo + " " + itemName + " " + SalesQty + " " + total);
            }
            print.printLine();

            // sTotal: 原價
            print.printSmallText("Discount :   USD " + ((int) Math.round(infoPack.info[0].OriUSDAmount) - (int) Math.round(infoPack.info[0].USDAmount)));
            print.printSmallText("Total :      USD " + (int) Math.round(infoPack.info[0].USDAmount));

            if (VipNo.contains("CUSS")) {
                print.printSpace(1);
                print.printSmallText("  CUSS__1__");
            }

            if (!sDiscount.equals("0")) {
                print.printSpace(1);
                print.printSmallText(" After Discount : " + sDiscount + " USD");
            }

            print.printSpace(1);

            sZ = "F";  //判斷是否為第一筆找錢
            //先列印現金和信用卡
            SalesReceiptPack receiptPack = DBQuery.getPaymentSalesInfo(mContext, errMsg, ReceiptNo, "Cash&Card", null, "S");
            if (receiptPack != null && receiptPack.list != null) {
                for (int i = 0; i < receiptPack.list.length; i++) {
                    String Currency, Amount, PayBy, CardNo;
                    Currency = receiptPack.list[i].Currency;
                    Amount = String.valueOf(receiptPack.list[i].Amount);

                    if (Amount.indexOf('.') > 0) {
                        String[] aTemp;
                        aTemp = Amount.split("\\.");
                        Amount = aTemp[0];
                    }

                    if (Integer.valueOf(Amount) > 0) {
                        PayBy = receiptPack.list[i].PayBy;
                        switch (PayBy) {
                            case "Cash":
                                print.printSmallText("Cash " + Currency + " : " + Tools.getModiMoneyString(Double.parseDouble(Amount)));
                                break;
                            case "Card":
                                print.printSmallText("Card " + Currency + " : " + Tools.getModiMoneyString(Double.parseDouble(Amount)));
                                CardNo = AESEncrypDecryp.getDectyptData(receiptPack.list[i].CardNo, key);
                                if (flag == 0) {
                                    CardNo = CardNo.substring(0, 6) + "******" + CardNo.substring(CardNo.length() - 4);
                                }
                                print.printSmallText("       No: " + CardNo);
                                print.printSmallText("     Type: " + receiptPack.list[i].CardType);
                                print.printSmallText("     Name: " + receiptPack.list[i].CardName);
                                print.printSmallText("Check No.: " + Tools.CreditCardDateDeCode(AESEncrypDecryp.getDectyptData(receiptPack.list[i].CardDate, key)));
                                print.printSpace(1);
                                String acq = Tools.GetCreditCardACQ(receiptPack.list[i].CardType);
                                if (acq.length() > 21) {
                                    print.printSmallText("ACQ : " + acq.substring(0, 21));
                                    print.printSmallText("      " + acq.substring(21));
                                } else {
                                    print.printSmallText("ACQ : " + acq);
                                }
                                print.printSmallText("MID : " + Tools.GetCreditCardMID(receiptPack.list[i].CardType, Currency));
                                print.printLine();
                                print.printSmallText("Card Total : " + Currency + " " + Tools.getModiMoneyString(Double.parseDouble(Amount)));
                                UseCard = true;
                                break;
                        }
                    }
                }
            }

            //列印S/C
            receiptPack = DBQuery.getPaymentSalesInfo(mContext, errMsg, ReceiptNo, "SC", "500", "S");
            if (receiptPack != null && receiptPack.list != null) {
                print.printSmallText("S/C TWD : " + (receiptPack.list.length * 500));
            }

            //列印 50 D/C
            receiptPack = DBQuery.getPaymentSalesInfo(mContext, errMsg, ReceiptNo, "DC", "50", "S");
            if (receiptPack != null && receiptPack.list != null) {
                for (int i = 0; i < receiptPack.list.length; i++) {
                    if (i == 0) {
                        print.printSmallText("D/C USD : 50*" + receiptPack.list.length + "    " + receiptPack.list[i].CouponNo);
                    } else {
                        print.printSmallText("                  " + receiptPack.list[i].CouponNo);
                    }
                }
            }

            //列印 10 D/C
            receiptPack = DBQuery.getPaymentSalesInfo(mContext, errMsg, ReceiptNo, "DC", "10", "S");
            if (receiptPack != null && receiptPack.list != null) {
                for (int i = 0; i < receiptPack.list.length; i++) {
                    if (i == 0) {
                        print.printSmallText("D/C USD : 10*" + receiptPack.list.length + "    " + receiptPack.list[i].CouponNo);
                    } else {
                        print.printSmallText("                  " + receiptPack.list[i].CouponNo);
                    }
                }
            }

            receiptPack = DBQuery.getPaymentSalesInfo(mContext, errMsg, ReceiptNo, "Change", null, "S");
            if (receiptPack != null && receiptPack.list != null) {
                for (int i = 0; i < receiptPack.list.length; i++) {
                    if (i == 0) {
                        print.printSpace(1);
                        print.printLine();
                    }
                    print.printSmallText("Change : " + receiptPack.list[i].Currency + " " + Tools.getModiMoneyString(receiptPack.list[i].Amount));
                }
            }

            receiptPack = DBQuery.getPaymentSalesInfo(mContext, errMsg, ReceiptNo, "Refund", null, "S");
            if (receiptPack != null && receiptPack.list != null) {
                print.printSpace(1);
                for (int i = 0; i < receiptPack.list.length; i++) {
                    if (receiptPack.list[i].Amount > 0) {
                        print.printSmallText("Refund : " + receiptPack.list[i].Currency + " " + Tools.getModiMoneyString(receiptPack.list[i].Amount));
                    }
                }
            }

            boolean prtSign = UseCard && !(flag == 0);
            boolean prtRec = !(flag == 1) || (!UseCard);
            if (prtSign) {
                print.printSpace(1);
                print.printSmallText("Signature:");
                print.printSpace(1);
                print.printSmallText("X");
                print.printLine();
                print.printSpace(1);
                print.printSmallText("I agree to pay total ");
                print.printSmallText("amount according to the");
                print.printSmallText("issuer agreement.");
                print.printSpace(1);
            }

            //顧客收執聯
            if (prtRec) {
                print.printSpace(2);
                print.printSmallText("Please keep this receipt ");
                print.printSmallText("for refund within 14 days");

                DBQuery.PromotionsInfoPack promotionsInfoPack = DBQuery.getPromotionsInfo(mContext, errMsg);

                if (promotionsInfoPack != null && promotionsInfoPack.list != null) {
                    for (int i = 0; i < promotionsInfoPack.list.length; i++) {
                        if (promotionsInfoPack.list[i].PrintType.equals("1")) {
                            print.printLine();
                            print.printSpace(1);

                            String strTmp;
                            if (!promotionsInfoPack.list[i].promotionTitles.equals("")) {
                                for (int irows = 0; irows < promotionsInfoPack.list[i].promotionTitles.split("@").length; irows++) {
                                    strTmp = promotionsInfoPack.list[i].promotionTitles.split("@")[irows];
                                    if (strTmp.length() < 27) {
                                        int length = strTmp.length();
                                        strTmp = String.format("%1$" + ((28 + length) / 2) + "s", strTmp);
                                    }
                                    print.printSmallText(strTmp);
                                }
                                print.printSpace(1);
                            }
                            if (!promotionsInfoPack.list[i].PromotionsDetail.equals("")) {
                                for (int irows = 0; irows < promotionsInfoPack.list[i].PromotionsDetail.split("@").length; irows++) {
                                    strTmp = promotionsInfoPack.list[i].PromotionsDetail.split("@")[irows];
                                    print.printSmallText(strTmp);
                                }
                            }
                            if (!promotionsInfoPack.list[i].StartDate.equals("")) {
                                print.printSmallText("*Valid " + promotionsInfoPack.list[i].StartDate + "Through " + promotionsInfoPack.list[i].EndDate + " only.");
                                print.printSpace(1);
                            }
                            if (!promotionsInfoPack.list[i].PromotionsCode.equals("")) {
                                strTmp = promotionsInfoPack.list[i].PromotionsCode;
                                print.printSmallText("*Promotion Code:" + strTmp);
                                print.printSpace(1);
                            }
                            if (!promotionsInfoPack.list[i].Note.equals("")) {
                                for (int irows = 0; irows < promotionsInfoPack.list[i].Note.split("@").length; irows++) {
                                    strTmp = promotionsInfoPack.list[i].Note.split("@")[irows];
                                    print.printSmallText(strTmp);
                                }
                                print.printSpace(1);
                            }
                        }
                    }
                }

                //QR Code
//        print.printSpace(1);
//        print.printLine();
//        print.printSpace(1);
//        print.printSmallText("For more products and ");
//        print.printSmallText("discount information, ");
//        print.printSmallText("please visit at ");
//        print.printSmallText("https://www.shopeva.com/");
//        print.printSmallText("or download EVA SKY SHOP ");
//        print.printSmallText("App now.");
//        print.printSpace(1);
//        print.printQRCode("https://itunes.apple.com/us/app/eva-sky-shop/id787668768",
//            "https://play.google.com/store/apps/details?id=com.evaair.preorder");
//        print.printSmallText("    iOS          Android");
            }
            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    /**
     * 列印退貨訊息
     *
     * @param ReceiptNo 單據號碼
     * @param flag      是否為簽單 true-簽單, false-收據
     */
    public int printRefund(String ReceiptNo, boolean flag) throws Exception {
        int ret;
        try {
            DBQuery.TransactionInfoPack infoPack = DBQuery.getDFSTransactionInfo(mContext, errMsg, ReceiptNo);
            int index = infoPack.info.length - 1;

            printHeader();
            print.printSpace(1);
            print.printBigText("     Refund");
            print.printLine();
            print.printSmallText("Receipt No:" + infoPack.info[index].ReceiptNo);
            print.printSmallText("Seat No:" + infoPack.info[index].SeatNo);
            if (!infoPack.info[0].DiscountType.equals("")) {
                if (!infoPack.info[0].DiscountNo.equals("")) {
                    String ss;
                    switch (infoPack.info[0].DiscountType) {
                        case "CUB":
                            ss = AESEncrypDecryp.getDectyptData(infoPack.info[0].DiscountNo, key).substring(0, 8);
                            break;
                        case "AE":
                        case "AEG":
                            ss = infoPack.info[0].DiscountNo.substring(0, 6);
                            break;
                        default:
                            ss = infoPack.info[0].DiscountNo;
                            break;
                    }
                    print.printSmallText("Discount:" + infoPack.info[0].DiscountType + "-" + ss);
                } else {
                    print.printSmallText("Discount:" + infoPack.info[0].DiscountType);
                }
            }
            print.printLine();
            //退貨資訊
            print.printSmallText("No  Item                Qty");

            for (int i = 0; i < infoPack.info[index].items.length; i++) {
                String serialcode = infoPack.info[index].items[i].SerialNo;
                String itemname = String.format("%1$-20s", infoPack.info[index].items[i].ItemName).substring(0, 18);
                int qty = infoPack.info[index].items[i].SalesQty;

                print.printSmallText(serialcode + " " + itemname + String.format("%1$5s", Integer.toString(qty)));
            }

            print.printLine();
            //退貨總額
            print.printSpace(1);
            print.printSmallText("Refund AMT : USD " + (int) Math.round(infoPack.info[index].USDAmount));
            print.printSpace(1);

            //判斷有沒有收CUSS卷，若有要印出退卷
            if (infoPack.info[index].PreorderNo.indexOf("CUSS") > 0) {
                print.printSmallText("Refund CUSS__1__");
            }

            // 現金退款
            int USDRefund = 0, TWDRefund = 0, JPYRefund = 0, HKDRefund = 0,
                    GBPRefund = 0, EURRefund = 0, CNYRefund = 0;
            // 卡片退款
            int CardRefund = 0;
            String CardCurrency = "";
            for (TransactionPaymentPack pack : infoPack.info[index].payments) {
                switch (pack.PayBy) {
                    case "Cash":
                        switch (pack.Currency) {
                            case "USD":
                                USDRefund += pack.Amount;
                                break;
                            case "TWD":
                                TWDRefund += pack.Amount;
                                break;
                            case "JPY":
                                JPYRefund += pack.Amount;
                                break;
                            case "HKD":
                                HKDRefund += pack.Amount;
                                break;
                            case "GBP":
                                GBPRefund += pack.Amount;
                                break;
                            case "EUR":
                                EURRefund += pack.Amount;
                                break;
                            case "CNY":
                                CNYRefund += pack.Amount;
                                break;
                        }
                        break;
                    case "Card":
                        CardRefund += pack.Amount;
                        CardCurrency = pack.Currency;
                        break;
                    case "Change":
                        switch (pack.Currency) {
                            case "USD":
                                USDRefund -= pack.Amount;
                                break;
                            case "TWD":
                                TWDRefund -= pack.Amount;
                                break;
                            case "JPY":
                                JPYRefund -= pack.Amount;
                                break;
                            case "HKD":
                                HKDRefund -= pack.Amount;
                                break;
                            case "GBP":
                                GBPRefund -= pack.Amount;
                                break;
                            case "EUR":
                                EURRefund -= pack.Amount;
                                break;
                            case "CNY":
                                CNYRefund -= pack.Amount;
                                break;
                        }
                        break;
                }
            }

            if (USDRefund > 0) {
                print.printSmallText("Refund Cash USD: " + Tools.getModiMoneyString(Math.abs(USDRefund)));
            }
            if (TWDRefund > 0) {
                print.printSmallText("Refund Cash TWD: " + Tools.getModiMoneyString(Math.abs(TWDRefund)));
            }
            if (JPYRefund > 0) {
                print.printSmallText("Refund Cash JPY: " + Tools.getModiMoneyString(Math.abs(JPYRefund)));
            }
            if (HKDRefund > 0) {
                print.printSmallText("Refund Cash HKD: " + Tools.getModiMoneyString(Math.abs(HKDRefund)));
            }
            if (GBPRefund > 0) {
                print.printSmallText("Refund Cash GBP: " + Tools.getModiMoneyString(Math.abs(GBPRefund)));
            }
            if (EURRefund > 0) {
                print.printSmallText("Refund Cash EUR: " + Tools.getModiMoneyString(Math.abs(EURRefund)));
            }
            if (CNYRefund > 0) {
                print.printSmallText("Refund Cash CNY: " + Tools.getModiMoneyString(Math.abs(CNYRefund)));
            }

            //Card
            if (!CardCurrency.equals("") && CardRefund != 0) {
                print.printSmallText("Refund Card " + CardCurrency + ": " + CardRefund + " to 0");
            }

            //Coupon不能退
            SalesReceiptPack receiptPack;
            receiptPack = DBQuery.getPaymentSalesInfo(mContext, errMsg, ReceiptNo, "SC", "500", "R");
            if (receiptPack != null && receiptPack.list != null) {
                print.printSmallText("Refund S/C TWD : " + (receiptPack.list.length * 500));
            }

            receiptPack = DBQuery.getPaymentSalesInfo(mContext, errMsg, ReceiptNo, "DC", "50", "R");
            if (receiptPack != null && receiptPack.list != null) {
                for (int i = 0; i < receiptPack.list.length; i++) {
                    if (i == 0) {
                        print.printSmallText("Refund D/C USD: 50*" + receiptPack.list.length + " " + receiptPack.list[i].CouponNo);
                    } else {
                        print.printSmallText("                   " + "  " + receiptPack.list[i].CouponNo);
                    }
                }
            }

            receiptPack = DBQuery.getPaymentSalesInfo(mContext, errMsg, ReceiptNo, "DC", "10", "R");
            if (receiptPack != null && receiptPack.list != null) {
                for (int i = 0; i < receiptPack.list.length; i++) {
                    if (i == 0) {
                        print.printSmallText("Refund D/C USD: 10*" + receiptPack.list.length + " " + receiptPack.list[i].CouponNo);
                    } else {
                        print.printSmallText("                   " + "  " + receiptPack.list[i].CouponNo);
                    }
                }
            }


            if (USDRefund < 0 || TWDRefund < 0 || JPYRefund < 0 || HKDRefund < 0 ||
                    GBPRefund < 0 || EURRefund < 0 || CNYRefund < 0) {
                print.printSpace(1);
                print.printLine();
                if (USDRefund < 0) {
                    print.printSmallText("Change: USD " + Tools.getModiMoneyString(Math.abs(USDRefund)));
                }
                if (TWDRefund < 0) {
                    print.printSmallText("Change: TWD " + Tools.getModiMoneyString(Math.abs(TWDRefund)));
                }
                if (JPYRefund < 0) {
                    print.printSmallText("Change: JPY " + Tools.getModiMoneyString(Math.abs(JPYRefund)));
                }
                if (HKDRefund < 0) {
                    print.printSmallText("Change: HKD " + Tools.getModiMoneyString(Math.abs(HKDRefund)));
                }
                if (GBPRefund < 0) {
                    print.printSmallText("Change: GBP " + Tools.getModiMoneyString(Math.abs(GBPRefund)));
                }
                if (EURRefund < 0) {
                    print.printSmallText("Change: EUR " + Tools.getModiMoneyString(Math.abs(EURRefund)));
                }
                if (CNYRefund < 0) {
                    print.printSmallText("Change: CNY " + Tools.getModiMoneyString(Math.abs(CNYRefund)));
                }
            }

            if (flag) {
                print.printLine();
                print.printSpace(2);
                print.printSmallText("Signature:");
                print.printSpace(1);
                print.printSmallText("X");
            } else {
                print.printSpace(2);
                print.printSmallText("Please check refund item");
                print.printSmallText("and cash !!");
            }

            print.printLine();
//      print.printSpace(3);
            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    /**
     * 打印Vip退貨信息
     *
     * @param ReceiptNo 收據單號
     * @param flag      是否列印簽單
     */
    public int printVIPRefund(String ReceiptNo, boolean flag) throws Exception {
        int ret;
        try {
            DBQuery.TransactionInfoPack transactionInfo = DBQuery.getDFSTransactionInfo(mContext, errMsg, ReceiptNo);
            int index = transactionInfo.info.length - 1;

            printHeader();
            print.printSpace(2);
            print.printBigText("      Refund");
            print.printLine();
            print.printSmallText("Receipt No:" + transactionInfo.info[index].ReceiptNo);
            print.printSmallText("VIP No:" + transactionInfo.info[index].PreorderNo);
            if (!transactionInfo.info[index].DiscountType.equals("")) {
                if (!transactionInfo.info[index].DiscountNo.equals("")) {
                    print.printSmallText("Discount:" + transactionInfo.info[index].DiscountType + "-" + transactionInfo.info[index].DiscountNo);
                } else {
                    print.printSmallText("Discount:" + transactionInfo.info[index].DiscountType);
                }
            }
            if (!transactionInfo.info[index].DiscountType.equals("")) {
                if (!transactionInfo.info[index].DiscountNo.equals("")) {
                    String ss;
                    switch (transactionInfo.info[index].DiscountType) {
                        case "CUB":
                            ss = AESEncrypDecryp.getDectyptData(transactionInfo.info[index].DiscountNo, key).substring(0, 8);
                            break;
                        case "AE":
                        case "AEG":
                            ss = transactionInfo.info[index].DiscountNo.substring(0, 6);
                            break;
                        default:
                            ss = transactionInfo.info[index].DiscountNo;
                            break;
                    }
                    print.printSmallText("Discount:" + transactionInfo.info[index].DiscountType + "-" + ss);
                } else {
                    print.printSmallText("Discount:" + transactionInfo.info[index].DiscountType);
                }
            }
            print.printLine();
            //退貨訊息
            print.printSmallText("No  Item                Qty");

            for (int i = 0; i < transactionInfo.info[index].items.length; i++) {
                String serialcode = transactionInfo.info[index].items[i].SerialNo;
                String itemname = String.format("%1$-20s", transactionInfo.info[index].items[i].ItemName).substring(0, 18);
                int qty = transactionInfo.info[index].items[i].SalesQty;

                print.printSmallText(serialcode + " " + itemname + String.format("%1$5s", Integer.toString(qty)));
            }
            print.printLine();
            //退貨總額
            print.printSpace(1);
            print.printSmallText("Refund AMT : USD " + (int) Math.round(transactionInfo.info[index].USDAmount));
            print.printSpace(1);

            //判斷有沒有收KIOS卷，若有要印出退卷
            if (transactionInfo.info[index].PreorderNo.indexOf("CUSS") > 0) {
                print.printSmallText("Refund CUSS__1__");
            }

            // 現金退款
            int USDRefund = 0, TWDRefund = 0, JPYRefund = 0, HKDRefund = 0,
                    GBPRefund = 0, EURRefund = 0, CNYRefund = 0;

            // 卡片退款
            int CardRefund = 0;
            String CardCurrency = "";

            for (TransactionPaymentPack pack : transactionInfo.info[index].payments) {
                switch (pack.PayBy) {
                    case "Cash":
                        switch (pack.Currency) {
                            case "USD":
                                USDRefund += pack.Amount;
                                break;
                            case "TWD":
                                TWDRefund += pack.Amount;
                                break;
                            case "JPY":
                                JPYRefund += pack.Amount;
                                break;
                            case "HKD":
                                HKDRefund += pack.Amount;
                                break;
                            case "GBP":
                                GBPRefund += pack.Amount;
                                break;
                            case "EUR":
                                EURRefund += pack.Amount;
                                break;
                            case "CNY":
                                CNYRefund += pack.Amount;
                                break;
                        }
                        break;
                    case "Card":
                        CardRefund += pack.Amount;
                        CardCurrency = pack.Currency;
                        break;
                    case "Change":
                        switch (pack.Currency) {
                            case "USD":
                                USDRefund -= pack.Amount;
                                break;
                            case "TWD":
                                TWDRefund -= pack.Amount;
                                break;
                            case "JPY":
                                JPYRefund -= pack.Amount;
                                break;
                            case "HKD":
                                HKDRefund -= pack.Amount;
                                break;
                            case "GBP":
                                GBPRefund -= pack.Amount;
                                break;
                            case "EUR":
                                EURRefund -= pack.Amount;
                                break;
                            case "CNY":
                                CNYRefund -= pack.Amount;
                                break;
                        }
                        break;
                }
            }

            if (USDRefund > 0) {
                print.printSmallText("Refund Cash USD: " + Tools.getModiMoneyString(Math.abs(USDRefund)));
            }
            if (TWDRefund > 0) {
                print.printSmallText("Refund Cash TWD: " + Tools.getModiMoneyString(Math.abs(TWDRefund)));
            }
            if (JPYRefund > 0) {
                print.printSmallText("Refund Cash JPY: " + Tools.getModiMoneyString(Math.abs(JPYRefund)));
            }
            if (HKDRefund > 0) {
                print.printSmallText("Refund Cash HKD: " + Tools.getModiMoneyString(Math.abs(HKDRefund)));
            }
            if (GBPRefund > 0) {
                print.printSmallText("Refund Cash GBP: " + Tools.getModiMoneyString(Math.abs(GBPRefund)));
            }
            if (EURRefund > 0) {
                print.printSmallText("Refund Cash EUR: " + Tools.getModiMoneyString(Math.abs(EURRefund)));
            }
            if (CNYRefund > 0) {
                print.printSmallText("Refund Cash CNY: " + Tools.getModiMoneyString(Math.abs(CNYRefund)));
            }

            //Card
            if (!CardCurrency.equals("") && CardRefund != 0) {
                print.printSmallText("Refund Card " + CardCurrency + ": " + CardRefund + " to 0");
            }

            //Coupon不能退
            SalesReceiptPack receiptPack;
            receiptPack = DBQuery.getPaymentSalesInfo(mContext, errMsg, ReceiptNo, "SC", "500", "R");
            if (receiptPack != null && receiptPack.list != null) {
                print.printSmallText("Refund S/C TWD : " + (receiptPack.list.length * 500));
            }

            receiptPack = DBQuery.getPaymentSalesInfo(mContext, errMsg, ReceiptNo, "DC", "50", "R");
            if (receiptPack != null && receiptPack.list != null) {
                for (int i = 0; i < receiptPack.list.length; i++) {
                    if (i == 0) {
                        print.printSmallText("Refund D/C USD: 50*" + receiptPack.list.length + "    " + receiptPack.list[i].CouponNo);
                    } else {
                        print.printSmallText("              " + receiptPack.list[i].CouponNo);
                    }
                }
            }

            receiptPack = DBQuery.getPaymentSalesInfo(mContext, errMsg, ReceiptNo, "DC", "10", "R");
            if (receiptPack != null && receiptPack.list != null) {
                for (int i = 0; i < receiptPack.list.length; i++) {
                    if (i == 0) {
                        print.printSmallText("Refund D/C USD: 10*" + receiptPack.list.length + " " + receiptPack.list[i].CouponNo);
                    } else {
                        print.printSmallText("                      " + receiptPack.list[i].CouponNo);
                    }
                }
            }

            if (USDRefund < 0 || TWDRefund < 0 || JPYRefund < 0 || HKDRefund < 0 ||
                    GBPRefund < 0 || EURRefund < 0 || CNYRefund < 0) {
                print.printSpace(1);
                print.printLine();
                if (USDRefund < 0) {
                    print.printSmallText("Change: USD " + Tools.getModiMoneyString(Math.abs(USDRefund)));
                }
                if (TWDRefund < 0) {
                    print.printSmallText("Change: TWD " + Tools.getModiMoneyString(Math.abs(TWDRefund)));
                }
                if (JPYRefund < 0) {
                    print.printSmallText("Change: JPY " + Tools.getModiMoneyString(Math.abs(JPYRefund)));
                }
                if (HKDRefund < 0) {
                    print.printSmallText("Change: HKD " + Tools.getModiMoneyString(Math.abs(HKDRefund)));
                }
                if (GBPRefund < 0) {
                    print.printSmallText("Change: GBP " + Tools.getModiMoneyString(Math.abs(GBPRefund)));
                }
                if (EURRefund < 0) {
                    print.printSmallText("Change: EUR " + Tools.getModiMoneyString(Math.abs(EURRefund)));
                }
                if (CNYRefund < 0) {
                    print.printSmallText("Change: CNY " + Tools.getModiMoneyString(Math.abs(CNYRefund)));
                }
            }

//      receiptPack = DBQuery.getPaymentSalesInfo(mContext, errMsg, ReceiptNo, "Change", null, "S");
//      if (receiptPack != null && receiptPack.list != null) {
//        for (int i = 0; i < receiptPack.list.length; i++) {
//          if (i == 0) {
//            print.printSpace(1);
//            print.printLine();
//          }
//          print.printSmallText("Change : " + receiptPack.list[i].Currency + " " + Tools.getModiMoneyString(receiptPack.list[i].Amount));
//        }
//      }

            if (flag) {
                print.printLine();
                print.printSpace(2);
                print.printSmallText("Signature:");
                print.printSpace(1);
                print.printSmallText("X");
            } else {
                print.printSpace(2);
                print.printSmallText("Please check refund item");
                print.printSmallText("and cash !!");
            }

            print.printLine();
//      print.printSpace(3);
            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    /**
     * 列印Preorder票據
     */
    public int printPreOrder(String PreorderNo) {
        int ret;
        try {
            DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(mContext, errMsg, PreorderNo, new String[]{"PR"}, "S");
            int index = preorderInfoPack.info.length - 1;

            printHeader();
            print.printSpace(2);
            print.printBigText("  Sales Receipt");
            print.printLine();
            print.printSmallText("Receipt No: " + preorderInfoPack.info[index].ReceiptNo);
            print.printSmallText("PreOrder No:" + preorderInfoPack.info[index].PreorderNO);
            print.printLine();
            print.printSmallText("No  Item        Qty   Total");

            for (int i = 0; i < preorderInfoPack.info[index].items.length; i++) {
                String serialcode = preorderInfoPack.info[index].items[i].SerialCode;
                String itemname = preorderInfoPack.info[index].items[i].ItemName;
                int qty = preorderInfoPack.info[index].items[i].SalesQty;
                double total = preorderInfoPack.info[index].items[i].SalesPrice * qty;

                print.printSmallText(serialcode + " " + itemname.substring(0, 10) + " " +
                        String.format("%1$4s", Integer.toString(qty)) +
                        "   " + String.format("%1$10s", Tools.getModiMoneyString(total)).substring(5));
            }

            print.printSpace(1);
            if (preorderInfoPack.info[index].ECoupon != 0) {
                print.printSmallText("E-Cpn: USD " + Tools.getModiMoneyString(preorderInfoPack.info[index].ECoupon));
            } else if (preorderInfoPack.info[index].MileDisc != 0) {
                print.printSmallText("Total Miles: " + Tools.getModiMoneyString(preorderInfoPack.info[index].MileDisc));
            }

            print.printSpace(1);
            print.printSmallText("Total: " + preorderInfoPack.info[index].CurDvr + " " + Tools.getModiMoneyString(preorderInfoPack.info[index].Amount));
            print.printLine();
            print.printSmallText("Type:    P");
            print.printSmallText("PAX PNR: " + preorderInfoPack.info[index].PNR);
            print.printSmallText("PAX Name:" + preorderInfoPack.info[index].PassengerName);
            print.printLine();

            print.printSmallText("Please keep this receipt ");
            print.printSmallText("for refund within 14 days");

            DBQuery.PromotionsInfoPack promotionsInfoPack = DBQuery.getPromotionsInfo(mContext, errMsg);

            if (promotionsInfoPack != null && promotionsInfoPack.list != null) {
                for (int i = 0; i < promotionsInfoPack.list.length; i++) {
                    if (promotionsInfoPack.list[i].PrintType.equals("1") || promotionsInfoPack.list[i].equals("4")) {
                        print.printLine();
                        print.printSpace(1);

                        String strTmp = "";
                        if (!promotionsInfoPack.list[i].promotionTitles.equals("")) {
                            for (int irows = 0; irows < promotionsInfoPack.list[i].promotionTitles.split("@").length; irows++) {
                                strTmp = promotionsInfoPack.list[i].promotionTitles.split("@")[irows];
                                if (strTmp.length() < 27) {
                                    int length = strTmp.length();
                                    strTmp = String.format("%1$" + ((28 + length) / 2) + "s", strTmp);
                                }
                                print.printSmallText(strTmp);
                            }
                            print.printSpace(1);
                        }
                        if (!promotionsInfoPack.list[i].PromotionsDetail.equals("")) {
                            for (int irows = 0; irows < promotionsInfoPack.list[i].PromotionsDetail.split("@").length; irows++) {
                                strTmp = promotionsInfoPack.list[i].PromotionsDetail.split("@")[irows];
                                print.printSmallText(strTmp);
                            }
                        }
                        if (!promotionsInfoPack.list[i].StartDate.equals("")) {
                            print.printSmallText("*Valid " + promotionsInfoPack.list[i].StartDate + "Through " + promotionsInfoPack.list[i].EndDate + " only.");
                            print.printSpace(1);
                        }
                        if (!promotionsInfoPack.list[i].PromotionsCode.equals("")) {
                            strTmp = promotionsInfoPack.list[i].PromotionsCode;
                            print.printSmallText("*Promotion Code:" + strTmp);
                            print.printSpace(1);
                        }
                        if (!promotionsInfoPack.list[i].Note.equals("")) {
                            for (int irows = 0; irows < promotionsInfoPack.list[i].Note.split("@").length; irows++) {
                                strTmp = promotionsInfoPack.list[i].Note.split("@")[irows];
                                print.printSmallText(strTmp);
                            }
                            print.printSpace(1);
                        }
                    }
                }
            }

            //QR Code
//      print.printSpace(1);
//      print.printLine();
//      print.printSpace(1);
//      print.printSmallText("For more products and ");
//      print.printSmallText("discount information, ");
//      print.printSmallText("please visit at ");
//      print.printSmallText("https://www.shopeva.com/");
//      print.printSmallText("or download EVA SKY SHOP ");
//      print.printSmallText("App now.");
//      print.printSpace(1);
//      print.printQRCode("https://itunes.apple.com/us/app/eva-sky-shop/id787668768",
//          "https://play.google.com/store/apps/details?id=com.evaair.preorder");
//      print.printSmallText("    iOS          Android");

            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    /**
     * Refund Preorder 票據
     */
    public int printPreOrderRefund(String PreorderNo) {
        int ret = 0;
        try {
            printHeader();
            DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(mContext, errMsg, PreorderNo, new String[]{"PR"}, null);
            int index = preorderInfoPack.info.length - 1;

            print.printSpace(2);
            print.printBigText("     Refund");
            print.printLine();
            print.printSmallText("Receipt No: " + preorderInfoPack.info[index].ReceiptNo);
            print.printSmallText("PreOrder No:" + preorderInfoPack.info[index].PreorderNO);
            print.printLine();
            print.printSmallText("No  Item        Qty   Total");

            for (int i = 0; i < preorderInfoPack.info[index].items.length; i++) {
                String serialcode = preorderInfoPack.info[index].items[i].SerialCode;
                String itemname = preorderInfoPack.info[index].items[i].ItemName;
                int qty = preorderInfoPack.info[index].items[i].SalesQty;
                double total = preorderInfoPack.info[index].items[i].SalesPrice * qty;

                print.printSmallText(serialcode + " " + itemname.substring(0, 10) + " " + String.format("%1$4s", Integer.toString(qty))
                        + "   " + String.format("%1$10s", Tools.getModiMoneyString(total)).substring(5));
            }

            print.printLine();
            if (preorderInfoPack.info[index].ECoupon != 0) {
                print.printSmallText("Refund E-Cpn: USD " + Tools.getModiMoneyString(preorderInfoPack.info[index].ECoupon));
                print.printSpace(1);
            } else if (preorderInfoPack.info[index].MileDisc != 0) {
                print.printSmallText("Refund Total Miles: " + Tools.getModiMoneyString(preorderInfoPack.info[index].MileDisc));
                print.printSpace(1);
            }
            print.printSmallText("Refund AMT:  " + preorderInfoPack.info[index].CurDvr + " " + (int) Math.round(preorderInfoPack.info[index].Amount));
            print.printSmallText(
                    "Refund Card: " + preorderInfoPack.info[index].CurDvr + " " + Tools.getModiMoneyString(preorderInfoPack.info[index].Amount) + " to 0");
            print.printLine();
            print.printSmallText("Type:    P");
            print.printSmallText("PAX PNR: " + preorderInfoPack.info[index].PNR);
            print.printSmallText("PAX Name:" + preorderInfoPack.info[index].PassengerName);
            print.printLine();
            print.printSpace(1);

            if (preorderInfoPack.info[index].ECoupon != 0) {
                print.printSmallText("*E-coupon refund may take");
                print.printSmallText(" up 10 days to process.");
                print.printSpace(1);
            } else if (preorderInfoPack.info[index].MileDisc != 0) {
                print.printSmallText("*Miles refund may take");
                print.printSmallText(" up 10 days to process.");
                print.printSpace(1);
            }

            print.printSmallText("Please check refund item ");
            print.printSmallText("and amount and we will ");
            print.printSmallText("not charge you any cost");
            print.printLine();

            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }


    /**
     * 列印Vip Order票據
     *
     * @param PreorderNo 收據單號
     */
    public int printVIPPaid(String PreorderNo) {
        int ret = 0;
        try {
            DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(mContext, errMsg, PreorderNo, new String[]{"VP"}, "S");
            int index = preorderInfoPack.info.length - 1;

            printHeader();
            print.printSpace(2);
            print.printBigText("  Sales Receipt");
            print.printLine();
            print.printSmallText("Receipt No: " + preorderInfoPack.info[index].ReceiptNo);
            print.printSmallText("VIP No:" + preorderInfoPack.info[index].PreorderNO);
            print.printLine();
            print.printSmallText("No  Item        Qty   Total");

            for (int i = 0; i < preorderInfoPack.info[index].items.length; i++) {
                String serialcode = preorderInfoPack.info[index].items[i].SerialCode;
                String itemname = preorderInfoPack.info[index].items[i].ItemName;
                int qty = preorderInfoPack.info[index].items[i].SalesQty;
                double total = preorderInfoPack.info[index].items[i].SalesPrice * qty;

                print.printSmallText(serialcode + " " + itemname.substring(0, 10) + " " + String.format("%1$4s", Integer.toString(qty))
                        + "   " + String.format("%1$10s", Tools.getModiMoneyString(total)).substring(5));
            }
            print.printSpace(1);
            print.printSmallText("Total: " + preorderInfoPack.info[index].CurDvr + " " + Tools.getModiMoneyString(preorderInfoPack.info[index].Amount));
            print.printLine();
            print.printSmallText("Type:    VIP PAID");
            print.printSmallText("PAX PNR: " + preorderInfoPack.info[index].PNR);
            print.printSmallText("PAX Name:" + preorderInfoPack.info[index].PassengerName);
            print.printLine();

            print.printSmallText("Please keep this receipt ");
            print.printSmallText("for refund within 14 days");

            DBQuery.PromotionsInfoPack promotionsInfoPack = DBQuery.getPromotionsInfo(mContext, errMsg);

            if (promotionsInfoPack != null && promotionsInfoPack.list != null) {
                for (int i = 0; i < promotionsInfoPack.list.length; i++) {
                    if (promotionsInfoPack.list[i].PrintType.equals("1") || promotionsInfoPack.list[i].equals("4")) {
                        print.printLine();
                        print.printSpace(1);

                        String strTmp;
                        if (!promotionsInfoPack.list[i].promotionTitles.equals("")) {
                            for (int irows = 0; irows < promotionsInfoPack.list[i].promotionTitles.split("@").length; irows++) {
                                strTmp = promotionsInfoPack.list[i].promotionTitles.split("@")[irows];
                                if (strTmp.length() < 27) {
                                    int length = strTmp.length();
                                    strTmp = String.format("%1$" + ((28 + length) / 2) + "s", strTmp);
                                }
                                print.printSmallText(strTmp);
                            }
                            print.printSpace(1);
                        }
                        if (!promotionsInfoPack.list[i].PromotionsDetail.equals("")) {
                            for (int irows = 0; irows < promotionsInfoPack.list[i].PromotionsDetail.split("@").length; irows++) {
                                strTmp = promotionsInfoPack.list[i].PromotionsDetail.split("@")[irows];
                                print.printSmallText(strTmp);
                            }
                        }
                        if (!promotionsInfoPack.list[i].StartDate.equals("")) {
                            print.printSmallText("*Valid " + promotionsInfoPack.list[i].StartDate + "Through " + promotionsInfoPack.list[i].EndDate + " only.");
                            print.printSpace(1);
                        }
                        if (!promotionsInfoPack.list[i].PromotionsCode.equals("")) {
                            strTmp = promotionsInfoPack.list[i].PromotionsCode;
                            print.printSmallText("*Promotion Code:" + strTmp);
                            print.printSpace(1);
                        }
                        if (!promotionsInfoPack.list[i].Note.equals("")) {
                            for (int irows = 0; irows < promotionsInfoPack.list[i].Note.split("@").length; irows++) {
                                strTmp = promotionsInfoPack.list[i].Note.split("@")[irows];
                                print.printSmallText(strTmp);
                            }
                            print.printSpace(1);
                        }
                    }
                }
            }

            //QR Code
//      print.printSpace(1);
//      print.printLine();
//      print.printSpace(1);
//      print.printSmallText("For more products and ");
//      print.printSmallText("discount information, ");
//      print.printSmallText("please visit at ");
//      print.printSmallText("https://www.shopeva.com/");
//      print.printSmallText("or download EVA SKY SHOP ");
//      print.printSmallText("App now.");
//      print.printSpace(1);
//      print.printQRCode("https://itunes.apple.com/us/app/eva-sky-shop/id787668768",
//          "https://play.google.com/store/apps/details?id=com.evaair.preorder");
//      print.printSmallText("    iOS          Android");

            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    /**
     * Refund VipOrder 票據
     *
     * @param PreorderNo 收據單號
     */
    public int printVIPPaidRefund(String PreorderNo) {
        int ret = 0;
        try {
            DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(mContext, errMsg, PreorderNo, new String[]{"VP"}, "R");
            int index = preorderInfoPack.info.length - 1;

            printHeader();
            print.printSpace(2);
            print.printBigText("     Refund");
            print.printLine();
            print.printSmallText("Receipt No: " + preorderInfoPack.info[index].ReceiptNo);
            print.printSmallText("VIP No:" + preorderInfoPack.info[index].PreorderNO);
            print.printLine();
            print.printSmallText("No  Item        Qty   Total");

            for (int i = 0; i < preorderInfoPack.info[index].items.length; i++) {
                String serialcode = preorderInfoPack.info[index].items[i].SerialCode;
                String itemname = preorderInfoPack.info[index].items[i].ItemName;
                int qty = preorderInfoPack.info[index].items[i].SalesQty;
                double total = preorderInfoPack.info[index].items[i].SalesPrice * qty;

                print.printSmallText(serialcode + " " + itemname.substring(0, 10) + " " + String.format("%1$4s", Integer.toString(qty))
                        + "   " + String.format("%1$10s", Tools.getModiMoneyString(total)).substring(5));
            }
            print.printSpace(1);
            print.printSmallText("Refund AMT : " + preorderInfoPack.info[index].CurDvr + " " + (int) Math.round(preorderInfoPack.info[index].Amount));
            print.printSmallText("Refund Card: " + preorderInfoPack.info[index].CurDvr + " " + Tools.getModiMoneyString(preorderInfoPack.info[index].Amount) + " to 0");
            print.printLine();
            print.printSpace(1);
            print.printSmallText("Please check refund item ");
            print.printSmallText("and amount and we will ");
            print.printSmallText("not charge you any cost");
            print.printLine();

            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    public int printTransferOut(String TransferNo, String QRCode) {
        int ret = 0;
        try {
            DBQuery.TransferItemPack transferItemPack = DBQuery.queryTransferItemQty(mContext, errMsg, TransferNo, "OUT");

            printHeader();
            print.printSpace(2);
            print.printBigText("  Transfer Out");
            print.printLine();

            String cartFrom = String.format("%1$-10s", transferItemPack.transfers[0].CarFrom).substring(0, 8);
            String cartTo = String.format("%1$-10s", transferItemPack.transfers[0].CarTo).substring(0, 8);
            print.printSmallText("From      To");
            print.printSmallText(cartFrom + "  " + cartTo);
            print.printSpace(1);

            print.printSmallText("No  Item                Qty");
            for (int i = 0; i < transferItemPack.transfers.length; i++) {
                String itemName = String.format("%1$-20s", transferItemPack.transfers[i].ItemName).substring(0, 19);
                print.printSmallText(
                        transferItemPack.transfers[i].SerialCode + " " + itemName + " "
                                + String.format("%1$3s", Integer.valueOf(transferItemPack.transfers[i].Qty)));
            }

//      print.printSmallText("No  Item    From To   Qty");
//      for (int i = 0; i < transferItemPack.transfers.length; i++) {
//        String itemName = String.format("%1$-20s", transferItemPack.transfers[i].ItemName).substring(0, 7);
//        print.printSmallText(
//            transferItemPack.transfers[i].SerialCode + " " + itemName + " " +
//                transferItemPack.transfers[i].CarFrom.substring(3) + "  "
//                + transferItemPack.transfers[i].CarTo.substring(3) + "  "
//                + String.format("%1$3s", Integer.valueOf(transferItemPack.transfers[i].Qty)));
//      }

            print.printLine();
            print.printSpace(1);
            print.printSmallText("Transfer QR Code:");
            print.printSpace(1);
            print.printCenterQRCode(QRCode);

            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    public int printTransferIn(String TransferNo) {
        int ret = 0;
        try {
            DBQuery.TransferItemPack transferItemPack = DBQuery.queryTransferItemQty(mContext, errMsg, TransferNo, "IN");
            printHeader();
            print.printSpace(2);
            print.printBigText("  Transfer In");
            print.printLine();
            print.printLine();

            String cartFrom = String.format("%1$-10s", transferItemPack.transfers[0].CarFrom).substring(0, 8);
            String cartTo = String.format("%1$-10s", transferItemPack.transfers[0].CarTo).substring(0, 8);
            print.printSmallText("From      To");
            print.printSmallText(cartFrom + "  " + cartTo);
            print.printSpace(1);

            print.printSmallText("No  Item                Qty");
            for (int i = 0; i < transferItemPack.transfers.length; i++) {
                String itemName = String.format("%1$-20s", transferItemPack.transfers[i].ItemName).substring(0, 19);
                print.printSmallText(
                        transferItemPack.transfers[i].SerialCode + " " + itemName + " "
                                + String.format("%1$3s", Integer.valueOf(transferItemPack.transfers[i].Qty)));
            }

            print.printLine();
            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    public int printDamageList() {
        int ret = 0;
        try {
            DBQuery.DamageItemPack damageItemPack = DBQuery.damageItemQty(mContext, errMsg, null);

            printHeader();
            print.printSpace(2);
//      print.printBigText("Transfer To Damage");
            print.printBigText("     Damage");
            print.printLine();
            print.printSmallText("No  Code    Item   Draw Qty");

            if (damageItemPack != null && damageItemPack.damages != null) {
                for (int i = 0; i < damageItemPack.damages.length; i++) {
                    String serialcode = damageItemPack.damages[i].SerialCode;
                    String itemcode = damageItemPack.damages[i].ItemCode;
                    String itemname = damageItemPack.damages[i].ItemName;
                    String draw = damageItemPack.damages[i].DrawNo;
                    int qty = damageItemPack.damages[i].DamageQty;
                    print.printSmallText(serialcode + " " + itemcode + " "
                            + String.format("%1$10s", itemname).substring(0, 6) + " "
                            + String.format("%1$-4s", draw) + " " + String.format("%1$3s", qty));
                }
            }

            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    /**
     * 列印Upgrade收據
     *
     * @param receiptNo 收據號碼
     * @param flag      是否為簽單 true-簽單, false-收據
     */
    public int printUpgrade(String receiptNo, boolean flag) {
        int ret;
        try {
            String CardNo;

            DBQuery.UpgradeTransactionInfoPack upgradeTransactionInfoPack = DBQuery.getUpgradeTransactionInfo(mContext, errMsg, receiptNo, "S");
            printHeader();
            print.printSpace(2);
            print.printBigText(" Upgrade Receipt");
            print.printLine();
            print.printSmallText("Receipt No:" + receiptNo);
            print.printLine();
            print.printSmallText("Item   Ori  New  Qty  Total");

            for (int i = 0; i < upgradeTransactionInfoPack.info[0].items.length; i++) {
                String item = upgradeTransactionInfoPack.info[0].items[i].Infant;
                if (item.equals("A")) {
                    item = "Adult";
                } else {
                    item = "Infant";
                }
                item = String.format("%1$-10s", upgradeTransactionInfoPack.info[0].items[i].Infant).substring(0, 6);

                String original = String.format("%1$-10s", upgradeTransactionInfoPack.info[0].items[i].OriginalClass).substring(0, 4);
                String newClass = String.format("%1$-10s", upgradeTransactionInfoPack.info[0].items[i].NewClass).substring(0, 4);
                String qtyString = String.format("%1$10s", upgradeTransactionInfoPack.info[0].items[i].SalesQty).substring(7);
                int qty = upgradeTransactionInfoPack.info[0].items[i].SalesQty;
                double Total = upgradeTransactionInfoPack.info[0].items[i].SalesPrice * qty;
                String total = String.format("%1$10s", String.valueOf(Tools.getModiMoneyString(Total))).substring(5);

                print.printSmallText(item + " " + original + " " + newClass + " " + qtyString + "  " + total);
            }

            print.printLine();
            print.printSmallText("Total: USD " + Tools.getModiMoneyString(upgradeTransactionInfoPack.info[0].TotalPrice));
            print.printSpace(1);

            if (upgradeTransactionInfoPack.info[0].payments[0].PayBy.equals("Cash")) {
                print.printSmallText(
                        "Cash    " + upgradeTransactionInfoPack.info[0].payments[0].Currency + " : " + Tools.getModiMoneyString(
                                upgradeTransactionInfoPack.info[0].payments[0].Amount));
                print.printLine();
                print.printSmallText("Cash Total : " + upgradeTransactionInfoPack.info[0].payments[0].Currency + " " + Tools.getModiMoneyString(
                        upgradeTransactionInfoPack.info[0].payments[0].Amount));
            } else {
                print.printSmallText(
                        "Card " + upgradeTransactionInfoPack.info[0].payments[0].Currency + " : " + Tools.getModiMoneyString(
                                upgradeTransactionInfoPack.info[0].payments[0].Amount));

                CardNo = upgradeTransactionInfoPack.info[0].payments[0].CardNo;
                if (!flag) {
                    CardNo = CardNo.substring(0, 6) + "******" + CardNo.substring(CardNo.length() - 4);
                }
                print.printSmallText("       No: " + CardNo);
                print.printSmallText("     Type: " + upgradeTransactionInfoPack.info[0].payments[0].CardType);
                print.printSmallText("     Name: " + upgradeTransactionInfoPack.info[0].payments[0].CardName);
                print.printSmallText(
                        "Check No.: " + Tools.CreditCardDateDeCode(upgradeTransactionInfoPack.info[0].payments[0].CardDate));
                print.printSpace(1);
                String acq = Tools.GetCreditCardACQ(upgradeTransactionInfoPack.info[0].payments[0].CardType);
                if (acq.length() > 21) {
                    print.printSmallText("ACQ : " + acq.substring(0, 21));
                    print.printSmallText("      " + acq.substring(21));
                } else {
                    print.printSmallText("ACQ : " + acq);
                }
                print.printSmallText("MID : " + Tools
                        .GetCreditCardMID(upgradeTransactionInfoPack.info[0].payments[0].CardType, upgradeTransactionInfoPack.info[0].payments[0].Currency));
                print.printLine();
                print.printSmallText(
                        "Card Total : " + upgradeTransactionInfoPack.info[0].payments[0].Currency + " " + Tools.getModiMoneyString(
                                upgradeTransactionInfoPack.info[0].payments[0].Amount));
            }

            if (flag == true) {
                print.printSpace(1);
                print.printSmallText("Signature:");
                print.printSpace(1);
                print.printSmallText(" X");
                print.printLine();
                print.printSpace(1);
            } else {
                print.printSpace(2);
                print.printSmallText("Please keep this receipt ");
                print.printSmallText("for refund");
                print.printLine();
                print.printSpace(1);
            }

            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    /**
     * 列印升倉等退票
     *
     * @param receiptNo 收據編號
     * @param flag      是否為簽單 true-簽單, false-收據
     */
    public int printUpgradeRefund(String receiptNo, boolean flag) {
        int ret;
        try {
            DBQuery.UpgradeTransactionInfoPack upgradeTransactionInfoPack = DBQuery.getUpgradeTransactionInfo(mContext, errMsg, receiptNo, "R");
            printHeader();
            print.printSpace(2);
            print.printBigText(" Upgrade Refund");
            print.printLine();
            print.printSmallText("Receipt No:" + receiptNo);
            print.printLine();
            print.printSmallText("Item   Ori  New  Qty  Total");

            for (int i = 0; i < upgradeTransactionInfoPack.info[0].items.length; i++) {
                String item = upgradeTransactionInfoPack.info[0].items[i].Infant;
                if (item.equals("A")) {
                    item = "Adult";
                } else {
                    item = "Infant";
                }
                item = String.format("%1$-10s", upgradeTransactionInfoPack.info[0].items[i].Infant).substring(0, 6);

                String original = String.format("%1$-10s", upgradeTransactionInfoPack.info[0].items[i].OriginalClass).substring(0, 4);
                String newClass = String.format("%1$-10s", upgradeTransactionInfoPack.info[0].items[i].NewClass).substring(0, 4);
                String qtyString = String.format("%1$10s", upgradeTransactionInfoPack.info[0].items[i].SalesQty).substring(7);
                int qty = upgradeTransactionInfoPack.info[0].items[i].SalesQty;
                double Total = upgradeTransactionInfoPack.info[0].items[i].SalesPrice * qty;
                String total = String.format("%1$10s", String.valueOf(Tools.getModiMoneyString(Total))).substring(5);

                print.printSmallText(item + " " + original + " " + newClass + " " + qtyString + "  " + total);
            }

            print.printLine();
            print.printSpace(1);
            print.printSmallText("Refund AMT : USD " + (int) Math.round(upgradeTransactionInfoPack.info[0].TotalPrice));
            print.printSpace(1);

            if (upgradeTransactionInfoPack.info[0].payments[0].PayBy.equals("Cash")) {
                print.printSmallText(
                        "Refund Cash " + upgradeTransactionInfoPack.info[0].payments[0].Currency + ": " + Tools.getModiMoneyString(
                                upgradeTransactionInfoPack.info[0].payments[0].Amount));
            } else {
                print.printSmallText(
                        "Refund Card " + upgradeTransactionInfoPack.info[0].payments[0].Currency + ": " + Tools.getModiMoneyString(
                                upgradeTransactionInfoPack.info[0].payments[0].Amount)
                                + " to 0");
            }

            if (flag) {
//        if (upgradeTransactionInfoPack.info[0].payments[0].PayBy.equals("Cash")) {
//          print.printSmallText("Cash Total:" + upgradeTransactionInfoPack.info[0].payments[0].Currency + " " + Tools.getModiMoneyString(upgradeTransactionInfoPack.info[0]
// .payments[0].Amount));
//        } else {
//          print.printSmallText("Card Total:" + upgradeTransactionInfoPack.info[0].payments[0].Currency + " " + Tools.getModiMoneyString(upgradeTransactionInfoPack.info[0]
// .payments[0].Amount));
//        }
                print.printLine();
                print.printSpace(2);
                print.printSmallText("Signature:");
                print.printSpace(1);
                print.printSmallText(" X");
                print.printLine();
                print.printSpace(1);
            } else {
//        if (upgradeTransactionInfoPack.info[0].payments[0].PayBy.equals("Cash")) {
//          print.printSmallText("Cash Total:" + upgradeTransactionInfoPack.info[0].payments[0].Currency + " " + Tools.getModiMoneyString(upgradeTransactionInfoPack.info[0]
// .payments[0].Amount));
//        } else {
//          print.printSmallText("Card Total:" + upgradeTransactionInfoPack.info[0].payments[0].Currency + " " + Tools.getModiMoneyString(upgradeTransactionInfoPack.info[0]
// .payments[0].Amount));
//        }
                print.printSpace(2);
                print.printSmallText("Please check refund item");
                print.printSmallText("and cash !!");
                print.printLine();
            }
//      print.printSpace(3);

            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    /**
     * 列印銷售統計
     */
    public int printSaleSummary(int SecSeq) throws Exception {
        int ret;
        try {
            String CreditCardNo;
            int[] nTotal = new int[12];

            printHeader();
            print.printSpace(2);
            // 27字
            print.printSmallText("Seal No: __________________");
            print.printSpace(1);
            print.printSmallText("Cash bag No: ______________");
            print.printSpace(1);
            print.printSmallText("Note: _____________________");
            print.printSpace(2);
            print.printBigText("  Sales Summary ");
            print.printBigText("      Sheet     ");
            print.printLine();

            //首先打印各產品的銷售數量，銷售總金額
            DBQuery.CUSSInfoPack cussInfoPack = DBQuery.getCUSSInfo(mContext, errMsg, String.valueOf(SecSeq));
            if (cussInfoPack != null && cussInfoPack.list != null) {
                print.printSmallText("RecNo  Item         Qty");
                for (int i = 0; i < cussInfoPack.list.length; i++) {
                    print.printSmallText(cussInfoPack.list[i].ReceiptNo + "     CUSS        1");
                }
                print.printLine();
                print.printSmallText("Total CUSS:" + cussInfoPack.list.length);
                print.printLine();
            }

            DBQuery.TotalSalesSummaryPack summaryPack = DBQuery.getTotalSalesSummary(mContext, errMsg, String.valueOf(SecSeq));
            if (summaryPack != null && summaryPack.list != null) {
                print.printSmallText("No  Item        Qty     Amt");
                for (int i = 0; i < summaryPack.list.length; i++) {
                    String serialCode = summaryPack.list[i].SerialCode;
                    String itemName = String.format("%1$-20s", summaryPack.list[i].ItemName).substring(0, 11);
                    String totalQty = String.format("%1$3s", String.valueOf(summaryPack.list[i].TotalQty));

                    String totalPrice = String.format("%7.2f", summaryPack.list[i].TotalPrice);

                    print.printSmallText(serialCode + " " + itemName + " " + totalQty + " " + totalPrice);
                }
            } else {
                print.printSpace(1);
                print.printSmallText("NIL");
            }

            print.printSpace(1);
            print.printLine();
            print.printSpace(1);

            DBQuery.PreorderInfoPack infoPack = DBQuery.getPreorderInfo(mContext, errMsg, null, null, "S");
            if (infoPack != null && infoPack.info != null) {

                Set<String> numberList = new HashSet<>();
                for (int i = 0; i < infoPack.info.length; i++) {
                    if (infoPack.info[i].SecSeq.equals(FlightData.SecSeq)) {
                        numberList.add(infoPack.info[i].PreorderNO);
                    }
                }

                if (numberList.size() > 0) {
                    print.printSmallText("PreOrder/VIP Receipt: " + numberList.size());
                    print.printLine();
                    print.printSpace(1);
                }
            }

            //列印PreOrder已銷售PreOrderNo
            DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(mContext, errMsg, null, new String[]{"PR"}, "S");
            print.printSmallText("         PreOrder          ");
            print.printLine();

            if (preorderInfoPack != null && preorderInfoPack.info != null) {

                Set<String> preorderNoList = new HashSet<>();
                for (int i = 0; i < preorderInfoPack.info.length; i++) {
                    if (preorderInfoPack.info[i].SecSeq.equals(FlightData.SecSeq)) {
                        preorderNoList.add(preorderInfoPack.info[i].PreorderNO);
                    }
                }

                for (String SetPreorderNo : preorderNoList) {
                    for (int i = 0; i < preorderInfoPack.info.length; i++) {
                        if (preorderInfoPack.info[i].PreorderNO.equals(SetPreorderNo)) {
                            print.printSpace(1);
                            print.printSmallText("PreOrderNo:" + preorderInfoPack.info[i].PreorderNO);
                            print.printSmallText("No              Qty     Amt");
                            for (int j = 0; j < preorderInfoPack.info[i].items.length; j++) {
                                String serialNo = preorderInfoPack.info[i].items[j].SerialCode;
                                String itemName = String.format("%1$-20s", preorderInfoPack.info[i].items[j].ItemName).substring(0, 11);
                                String qty = String.format("%1$3s", String.valueOf(preorderInfoPack.info[i].items[j].SalesQty));

                                String Amt = String.format("%1$7s",
                                        Tools.getModiMoneyString(preorderInfoPack.info[i].items[j].SalesQty * preorderInfoPack.info[i].items[j].SalesPrice));

                                print.printSmallText(serialNo + " " + itemName + " " + qty + " " + Amt);
                            }

                            print.printLine();

                            if (preorderInfoPack.info[i].ECoupon != 0) {
                                print.printSpace(1);
                                print.printSmallText("E-Cpn: USD " + Tools.getModiMoneyString(preorderInfoPack.info[i].ECoupon));
                            }

                            if (!preorderInfoPack.info[i].CardType.equals("CU")) {
                                CreditCardNo = AESEncrypDecryp.getDectyptData(preorderInfoPack.info[i].CardNo, key);
                                CreditCardNo = CreditCardNo.substring(0, 6) + "******" + CreditCardNo.substring(CreditCardNo.length() - 4);
                            } else {
                                CreditCardNo = "";
                            }

                            if (CreditCardNo.equals("111111******1111")) {
                                print.printSmallText("Card USD : 0");
                                print.printSmallText("      No : ---");
                                print.printSmallText("    Type : ---");
                            } else {
                                print.printSmallText("Card " + preorderInfoPack.info[i].CurDvr + " : "
                                        + Tools.getModiMoneyString(preorderInfoPack.info[i].PayAmt));
                                print.printSmallText("      No : " + CreditCardNo);
                                print.printSmallText("    Type : " + (preorderInfoPack.info[i].CardType.equals("CUP") ? "CU" : preorderInfoPack.info[i].CardType));
                            }
                            break;
                        }
                    }
                }
            } else {
                print.printSpace(1);
                print.printSmallText("NIL");
                print.printSpace(1);
            }
            print.printLine();
            print.printSpace(1);

            //VIP 機上付款
            StringBuilder err = new StringBuilder();
            DBQuery.PreorderInfoPack VipSaleinfoPack = DBQuery.getPreorderInfo(mContext, errMsg, null, new String[]{"VS"}, "S");
            ReceiptList receiptNoList = DBQuery.getAllRceciptNoList(mContext, err, "Sale", false);

            boolean isVIPExit = false;
            print.printSmallText("            VIP            ");
            print.printLine();
            if (VipSaleinfoPack != null && VipSaleinfoPack.info != null) {

                Set<String> vipNoList = new HashSet<>();
                for (int i = 0; i < VipSaleinfoPack.info.length; i++) {
                    if (VipSaleinfoPack.info[i].SecSeq.equals(FlightData.SecSeq)) {
                        isVIPExit = true;
                        vipNoList.add(VipSaleinfoPack.info[i].PreorderNO);
                    }
                }

                for (String setVipSaleNo : vipNoList) {
                    for (int i = 0; i < VipSaleinfoPack.info.length; i++) {

                        if (setVipSaleNo.equals(VipSaleinfoPack.info[i].PreorderNO)) {
                            print.printSpace(1);
                            String preOrderNo = VipSaleinfoPack.info[i].PreorderNO;
                            print.printSmallText("Vip Sale:" + preOrderNo);
                            print.printSmallText("No              Qty     Amt");
                            for (int j = 0; j < VipSaleinfoPack.info[i].items.length; j++) {
                                String serialNo = VipSaleinfoPack.info[i].items[j].SerialCode;
                                String itemName = String.format("%1$-20s", VipSaleinfoPack.info[i].items[j].ItemName).substring(0, 11);
                                String qty = String.format("%1$3s", String.valueOf(VipSaleinfoPack.info[i].items[j].SalesQty));

                                String Amt = String.format("%1$7s", Tools.getModiMoneyString(
                                        VipSaleinfoPack.info[i].items[j].SalesQty * VipSaleinfoPack.info[i].items[j].SalesPrice)); //-----------//

                                print.printSmallText(serialNo + " " + itemName + " " + qty + " " + Amt);
                            }
                            print.printLine();

                            String sZ = "F";
                            // 先取得所有單據號碼, VipSaleinfoPack.info[i].ReceiptNo是""
                            for (Receipt r : receiptNoList.rececipts) {
                                if (r.PreorderNo.equals(VipSaleinfoPack.info[i].PreorderNO) && r.Type.equals("VS")) {
                                    VipSaleinfoPack.info[i].ReceiptNo = r.ReceiptNo;
                                }
                            }
                            DBQuery.TransactionInfoPack transactionInfo = DBQuery.getDFSTransactionInfo(mContext, errMsg, VipSaleinfoPack.info[i].ReceiptNo);
                            if (transactionInfo != null && transactionInfo.info != null) {
                                int CouponUSDAmount = 0, CouponTWDAmount = 0;
                                for (int k = 0; k < transactionInfo.info[0].payments.length; k++) {
                                    if (transactionInfo.info[0].payments[k].Amount > 0) {
                                        if (transactionInfo.info[0].payments[k].PayBy.equals("SC") || transactionInfo.info[0].payments[k].PayBy.equals("DC")) {
                                            if (transactionInfo.info[0].payments[k].Currency.equals("USD")) {
                                                CouponUSDAmount += transactionInfo.info[0].payments[k].Amount;
                                            } else if (transactionInfo.info[0].payments[k].Currency.equals("TWD")) {
                                                CouponTWDAmount += transactionInfo.info[0].payments[k].Amount;
                                            }
                                        }
                                    }
                                }
                                boolean printCoupon = true;
                                if (CouponUSDAmount > 0 || CouponTWDAmount > 0) {
                                    printCoupon = false;
                                }

                                for (int k = 0; k < transactionInfo.info[0].payments.length; k++) {
                                    String amount = Tools.getModiMoneyString(transactionInfo.info[0].payments[k].Amount); //-------//
                                    if (transactionInfo.info[0].payments[k].Amount > 0) {
                                        switch (transactionInfo.info[0].payments[k].PayBy) {
                                            case "Cash":
                                                print.printSmallText("Cash " + transactionInfo.info[0].payments[k].Currency + " : " + amount);
                                                break;
                                            case "Card":
                                                CreditCardNo = transactionInfo.info[0].payments[k].CardNo.substring(0, 6) + "******"
                                                        + transactionInfo.info[0].payments[k].CardNo.substring(transactionInfo.info[0].payments[k].CardNo.length() - 4);
                                                print.printSmallText("Card " + transactionInfo.info[0].payments[k].Currency + " : " + amount);
                                                print.printSmallText("       No: " + CreditCardNo);
                                                print.printSmallText("     Type: " + transactionInfo.info[0].payments[k].CardType);
                                                break;
                                            case "SC":
                                            case "DC":
                                                if (!printCoupon && (CouponUSDAmount > 0 || CouponTWDAmount > 0)) {
                                                    String USD = Tools.getModiMoneyString(CouponUSDAmount);
                                                    String TWD = Tools.getModiMoneyString(CouponTWDAmount);
                                                    if (CouponUSDAmount > 0) {
                                                        print.printSmallText("Coupon USD : " + USD);
                                                    }
                                                    if (CouponTWDAmount > 0) {
                                                        print.printSmallText("Coupon TWD : " + TWD);
                                                    }
                                                    printCoupon = true;
                                                }
                                                break;
                                            case "Change":
                                                if (sZ.equals("F")) {
                                                    print.printSpace(2);
                                                    print.printLine();
                                                    sZ = "T";
                                                }
                                                print.printSmallText("Change : " + transactionInfo.info[0].payments[k].Currency + " " + amount);
                                                break;
                                        }
                                    }
                                }
                            }
                            print.printSpace(1);
                            break;
                        }
                    }
                }
            }

            //列印地面預售VIP
            DBQuery.PreorderInfoPack VipPaidinfoPack = DBQuery.getPreorderInfo(mContext, errMsg, null, new String[]{"VP"}, "S");
            if (VipPaidinfoPack != null && VipPaidinfoPack.info != null) {

                Set<String> vipNoList = new HashSet<>();
                for (int i = 0; i < VipPaidinfoPack.info.length; i++) {
                    if (VipPaidinfoPack.info[i].SecSeq.equals(FlightData.SecSeq)) {
                        isVIPExit = true;
                        vipNoList.add(VipPaidinfoPack.info[i].PreorderNO);
                    }
                }

                for (String setVipPaidNo : vipNoList) {
                    for (int i = 0; i < VipPaidinfoPack.info.length; i++) {
                        if (setVipPaidNo.equals(VipPaidinfoPack.info[i].PreorderNO)) {
                            String preOrderNo = VipPaidinfoPack.info[i].PreorderNO;
                            print.printSmallText("Vip Paid:" + preOrderNo);
                            print.printSmallText("No              Qty     Amt");
                            for (int j = 0; j < VipPaidinfoPack.info[i].items.length; j++) {
                                String serialNo = VipPaidinfoPack.info[i].items[j].SerialCode;
                                String itemName = String.format("%1$-20s", VipPaidinfoPack.info[i].items[j].ItemName).substring(0, 11);
                                String qty = String.format("%1$3s", String.valueOf(VipPaidinfoPack.info[i].items[j].SalesQty));

                                String Amt = String.format("%1$7s", Tools.getModiMoneyString(
                                        VipPaidinfoPack.info[i].items[j].SalesQty * VipPaidinfoPack.info[i].items[j].SalesPrice)); //-----------//
                                print.printSmallText(serialNo + " " + itemName + " " + qty + " " + Amt);
                            }
                            print.printLine();

                            if (!VipPaidinfoPack.info[i].CardType.equals("CU")) {
                                CreditCardNo = AESEncrypDecryp.getDectyptData(VipPaidinfoPack.info[i].CardNo, key);
                                CreditCardNo = CreditCardNo.substring(0, 6) + "******" + CreditCardNo.substring(CreditCardNo.length() - 4);
                            } else {
                                CreditCardNo = "";
                            }
                            print.printSmallText("Card " + VipPaidinfoPack.info[i].CurDvr + " : "
                                    + Tools.getModiMoneyString(VipPaidinfoPack.info[i].Amount));
                            print.printSmallText("      No : " + CreditCardNo);
                            print.printSmallText("    Type : " + (VipPaidinfoPack.info[i].CardType.equals("CUP") ? "CU" : VipPaidinfoPack.info[i].CardType));
                            break;
                        }
                    }
                }
            }
            if (!isVIPExit) {
                print.printSpace(1);
                print.printSmallText("NIL");
                print.printSpace(1);
            }
            print.printLine();
            print.printSpace(1);


            // 列印此航段內單品銷售有包含折扣的數量和價格
            print.printSmallText("          Discount         ");
            print.printLine();
            print.printSpace(1);

            DBQuery.SalesItemDiscountPack salesItemDiscountPack = DBQuery.getSalesItemDiscountReport(mContext, errMsg, String.valueOf(SecSeq));
            String[] BinCodeList = DBQuery.getBinCode(mContext, errMsg);
            if (salesItemDiscountPack != null && salesItemDiscountPack.items != null) {
                String VipNo;
                print.printSmallText("No  Code    Qty Dis    Amt");
                for (int i = 0; i < salesItemDiscountPack.items.length; i++) {

                    // 6碼 discount No, serialCode, itemCode, 銷售數量, 折扣後金額
                    // VipNo.length()>=14 : AES解碼
                    // VipNo.substring(0,6)
//          boolean isBinCode= false;
//          for(String s: BinCodeList){
//            if(salesItemDiscountPack.items[i].VipNo.contains(s)){
//              isBinCode= true;
//            }
//          }
//
                    // 信用卡
//          if(!isBinCode && salesItemDiscountPack.items[i].VipNo.length()>12){
//            VipNo = aesEncrypDecryp.getDectyptData(salesItemDiscountPack.items[i].VipNo, key);
//            VipNo= String.format("%1$-12s", VipNo.substring(0, 10));
//          }
//          // 會員卡
//          else if(salesItemDiscountPack.items[i].VipNo.length()>12){
//            VipNo= String.format("%1$-12s", salesItemDiscountPack.items[i].VipNo.substring(0,10));
//          }
//          // 其他VIP No
//          else{
//            VipNo= String.format("%1$-12s", salesItemDiscountPack.items[i].VipNo);
//          }

                    // CUB
//                    if (salesItemDiscountPack.items[i].VipNo.length() >= 14) {
//                        VipNo = AESEncrypDecryp.getDectyptData(salesItemDiscountPack.items[i].VipNo, key);
//                        VipNo = String.format("%1$-12s", VipNo.substring(0, 8));
//                    }
//                    // 其他VIP No
//                    else {
//                        VipNo = String.format("%1$-12s", salesItemDiscountPack.items[i].VipNo);
//                    }

                    print.printSmallText(salesItemDiscountPack.items[i].SerialCode + " " +
                            salesItemDiscountPack.items[i].ItemCode + " " +
                            String.format("%1$3s", String.valueOf(salesItemDiscountPack.items[i].SalesQty)) + " " +
                            String.format("%1$3s", String.valueOf(Arith.mul(100, Arith.sub(1, salesItemDiscountPack.items[i].Discount)))).substring(0, 2) + "% " +
                            String.format("%6.2f", salesItemDiscountPack.items[i].SalesPrice));
                }
            } else {
                print.printSmallText("NIL");
                print.printSpace(1);
            }
            print.printLine();
            print.printSpace(1);

            //列印 刷卡金額 USD  (payBy為B 時，即使用信用卡;PayInfo為信用卡卡號)
            print.printSmallText("     Credit Card in USD     ");
            print.printLine();
            print.printSpace(1);
            boolean flag = false;

            DBQuery.PaymentInfoPack paymentInfoPack = DBQuery.getPaymentInfo(mContext, errMsg, String.valueOf(SecSeq), null, null);
            if (paymentInfoPack != null && paymentInfoPack.list != null) {
                for (int i = 0; i < paymentInfoPack.list.length; i++) {
                    if (paymentInfoPack.list[i].PayBy.equals("Card") && paymentInfoPack.list[i].Currency.equals("USD")) {
                        flag = true;
                        switch (paymentInfoPack.list[i].CardType.substring(0, 1).toUpperCase()) {
                            case "M":
                                nTotal[7] += paymentInfoPack.list[i].Amount;
                                break;
                            case "A":
                                nTotal[1] += paymentInfoPack.list[i].Amount;
                                break;
                            case "D":
                                nTotal[3] += paymentInfoPack.list[i].Amount;
                                break;
                            case "J":
                                nTotal[5] += paymentInfoPack.list[i].Amount;
                                break;
                            case "C":
                                nTotal[11] += paymentInfoPack.list[i].Amount;
                                break;
                            default:
                                nTotal[9] += paymentInfoPack.list[i].Amount;
                                break;
                        }
                        CreditCardNo = AESEncrypDecryp.getDectyptData(paymentInfoPack.list[i].CardNo, key);
                        CreditCardNo = CreditCardNo.substring(0, 6) + "******" + CreditCardNo.substring(CreditCardNo.length() - 4);

                        print.printSmallText(String.format("%1$-22s", "Receipt No: " + paymentInfoPack.list[i].ReceiptNo) + "Check");
                        print.printSmallText(String.format("%1$-22s", "Amt: " + Tools.getModiMoneyString(paymentInfoPack.list[i].Amount)) + "_____");
                        print.printSmallText("CardNo:" + CreditCardNo);
                        print.printSmallText("CardType:" + paymentInfoPack.list[i].CardType);

                        print.printSpace(1);
                    }
                }
            }
            if (!flag) {
                print.printSmallText("NIL");
            }
            print.printSpace(1);
            print.printLine();
            print.printSpace(1);

            //列印 刷卡金額 TWD  (payBy為B 時，即使用信用卡;PayInfo為信用卡卡號)
            print.printSmallText("     Credit Card in TWD     ");
            print.printLine();
            print.printSpace(1);
            flag = false;

            if (paymentInfoPack != null && paymentInfoPack.list != null) {
                for (int i = 0; i < paymentInfoPack.list.length; i++) {
                    if (paymentInfoPack.list[i].PayBy.equals("Card") && paymentInfoPack.list[i].Currency.equals("TWD")) {
                        flag = true;
                        switch (paymentInfoPack.list[i].CardType.substring(0, 1).toUpperCase()) {
                            case "M":
                                nTotal[6] += paymentInfoPack.list[i].Amount;
                                break;
                            case "A":
                                nTotal[0] += paymentInfoPack.list[i].Amount;
                                break;
                            case "D":
                                nTotal[2] += paymentInfoPack.list[i].Amount;
                                break;
                            case "J":
                                nTotal[4] += paymentInfoPack.list[i].Amount;
                                break;
                            case "C":
                                nTotal[10] += paymentInfoPack.list[i].Amount;
                                break;
                            default:
                                nTotal[8] += paymentInfoPack.list[i].Amount;
                                break;
                        }
                        CreditCardNo = AESEncrypDecryp.getDectyptData(paymentInfoPack.list[i].CardNo, key);
                        CreditCardNo = CreditCardNo.substring(0, 6) + "******" + CreditCardNo.substring(CreditCardNo.length() - 4);

                        print.printSmallText(String.format("%1$-22s", "Receipt No: " + paymentInfoPack.list[i].ReceiptNo) + "Check");
                        print.printSmallText(String.format("%1$-22s", "Amt: " + Tools.getModiMoneyString(paymentInfoPack.list[i].Amount)) + "_____");
                        print.printSmallText("CardNo:" + CreditCardNo);
                        print.printSmallText("CardType:" + paymentInfoPack.list[i].CardType);

                        print.printSpace(1);
                    }
                }
            }
            if (!flag) {
                print.printSmallText("NIL");
                print.printSpace(1);
            }
            print.printLine();
            print.printSpace(1);

            //列印 S/C Coupon金額
            int CouponTotal = 0;
            print.printSmallText("           Coupon           ");
            print.printLine();
            print.printSpace(1);
            print.printSmallText("[S/C]");

            DBQuery.CouponInfoSummaryPack SCPack = DBQuery.getCouponInfoSummary(mContext, errMsg, "SC");

            if (SCPack != null && SCPack.list != null) {
                print.printSmallText("RecNo     Amt     Check CPN");
                for (int i = 0; i < SCPack.list.length; i++) {
                    print.printSmallText(String.format("%1$5s", SCPack.list[i].ReceiptNo) + " "
                            + String.format("%1$7s", Tools.getModiMoneyString(SCPack.list[i].Amount)) + "     _________");
                    CouponTotal += SCPack.list[i].Amount;
                }
                print.printSpace(1);
                print.printSmallText("Total TWD :" + CouponTotal + " Count:" + (CouponTotal / 500));
                print.printSpace(1);
            } else {
                print.printSmallText("NIL");
            }

            //列印 D/C Coupon金額
            CouponTotal = 0;
            print.printSpace(1);
            print.printSmallText("[D/C]");
            // Rec Value CPN No Check CPN
            // 001  9999 123456  ________

            DBQuery.CouponInfoSummaryPack DCPack = DBQuery.getCouponInfoSummary(mContext, errMsg, "DC");

            if (DCPack != null && DCPack.list != null) {
                print.printSmallText("Rec Value CPN No  Check CPN");
                for (int i = 0; i < DCPack.list.length; i++) {
                    print.printSmallText(
                            String.format("%1$3s", DCPack.list[i].ReceiptNo) + " "
                                    + String.format("%1$5s", Tools.getModiMoneyString(DCPack.list[i].Amount)) + " "
                                    + String.format("%1$6s", DCPack.list[i].CouponNo) + "  _________");
                    CouponTotal += DCPack.list[i].Amount;
                }
                print.printSpace(1);
                print.printSmallText("Total USD :" + CouponTotal);
                print.printSpace(1);
            } else {
                print.printSmallText("NIL");
            }
            print.printSpace(1);
            print.printLine();
            print.printSpace(1);


            print.printSmallText("       Sales Amount");
            print.printLine();
            print.printSpace(1);
            print.printSmallText("Cash                Check");
            DBQuery.CashTotalAmtPack f;
            boolean isSalesCashExit = false;

            f = DBQuery.getCashTotalAmt(mContext, errMsg, "USD");
            if (f != null && f.list != null && f.list[0].Amount != 0) {
                isSalesCashExit = true;
                print.printSmallText("USD: " + String.format("%1$-20s", Tools.getModiMoneyString(f.list[0].Amount)).substring(0, 11) + " __________");
                print.printSpace(1);
            }

            f = DBQuery.getCashTotalAmt(mContext, errMsg, "TWD");
            if (f != null && f.list != null && f.list[0].Amount != 0) {
                isSalesCashExit = true;
                print.printSmallText("TWD: " + String.format("%1$-20s", Tools.getModiMoneyString(f.list[0].Amount)).substring(0, 11) + " __________");
                print.printSpace(1);
            }

            f = DBQuery.getCashTotalAmt(mContext, errMsg, "JPY");
            if (f != null && f.list != null && f.list[0].Amount != 0) {
                isSalesCashExit = true;
                print.printSmallText("JPY: " + String.format("%1$-20s", Tools.getModiMoneyString(f.list[0].Amount)).substring(0, 11) + " __________");
                print.printSpace(1);
            }

            f = DBQuery.getCashTotalAmt(mContext, errMsg, "HKD");
            if (f != null && f.list != null && f.list[0].Amount != 0) {
                isSalesCashExit = true;
                print.printSmallText("HKD: " + String.format("%1$-20s", Tools.getModiMoneyString(f.list[0].Amount)).substring(0, 11) + " __________");
                print.printSpace(1);
            }

            f = DBQuery.getCashTotalAmt(mContext, errMsg, "GBP");
            if (f != null && f.list != null && f.list[0].Amount != 0) {
                isSalesCashExit = true;
                print.printSmallText("GBP: " + String.format("%1$-20s", Tools.getModiMoneyString(f.list[0].Amount)).substring(0, 11) + " __________");
                print.printSpace(1);
            }

            f = DBQuery.getCashTotalAmt(mContext, errMsg, "EUR");
            if (f != null && f.list != null && f.list[0].Amount != 0) {
                isSalesCashExit = true;
                print.printSmallText("EUR: " + String.format("%1$-20s", Tools.getModiMoneyString(f.list[0].Amount)).substring(0, 11) + " __________");
                print.printSpace(1);
            }

            f = DBQuery.getCashTotalAmt(mContext, errMsg, "CNY");
            if (f != null && f.list != null && f.list[0].Amount != 0) {
                isSalesCashExit = true;
                print.printSmallText("CNY: " + String.format("%1$-20s", Tools.getModiMoneyString(f.list[0].Amount)).substring(0, 11) + " __________");
                print.printSpace(1);
            }

            if (!isSalesCashExit) {
                print.printSmallText("NIL");
                print.printSpace(1);
            }

            print.printLine();
            print.printSpace(1);
            print.printSmallText("Credit Card         Check");
            boolean isSalesCardExit = false;
            for (int i = 0; i < 12; i++) {
                if (nTotal[i] > 0) {
                    switch (i) {
                        case 0:
                            isSalesCardExit = true;
                            print.printSmallText("AX-TWD :" + String.format("%1$-20s", Tools.getModiMoneyString(nTotal[i])).substring(0, 8) + " __________");
                            print.printSpace(1);
                            break;
                        case 1:
                            isSalesCardExit = true;
                            print.printSmallText("AX-USD :" + String.format("%1$-20s", Tools.getModiMoneyString(nTotal[i])).substring(0, 8) + " __________");
                            print.printSpace(1);
                            break;
                        case 2:
                            isSalesCardExit = true;
                            print.printSmallText("DC-TWD :" + String.format("%1$-20s", Tools.getModiMoneyString(nTotal[i])).substring(0, 8) + " __________");
                            print.printSpace(1);
                            break;
                        case 3:
                            isSalesCardExit = true;
                            print.printSmallText("DC-USD :" + String.format("%1$-20s", Tools.getModiMoneyString(nTotal[i])).substring(0, 8) + " __________");
                            print.printSpace(1);
                            break;
                        case 4:
                            isSalesCardExit = true;
                            print.printSmallText("JC-TWD :" + String.format("%1$-20s", Tools.getModiMoneyString(nTotal[i])).substring(0, 8) + " __________");
                            print.printSpace(1);
                            break;
                        case 5:
                            isSalesCardExit = true;
                            print.printSmallText("JC-USD :" + String.format("%1$-20s", Tools.getModiMoneyString(nTotal[i])).substring(0, 8) + " __________");
                            print.printSpace(1);
                            break;
                        case 6:
                            isSalesCardExit = true;
                            print.printSmallText("MT-TWD :" + String.format("%1$-20s", Tools.getModiMoneyString(nTotal[i])).substring(0, 8) + " __________");
                            print.printSpace(1);
                            break;
                        case 7:
                            isSalesCardExit = true;
                            print.printSmallText("MT-USD :" + String.format("%1$-20s", Tools.getModiMoneyString(nTotal[i])).substring(0, 8) + " __________");
                            print.printSpace(1);
                            break;
                        case 8:
                            isSalesCardExit = true;
                            print.printSmallText("VI-TWD :" + String.format("%1$-20s", Tools.getModiMoneyString(nTotal[i])).substring(0, 8) + " __________");
                            print.printSpace(1);
                            break;
                        case 9:
                            isSalesCardExit = true;
                            print.printSmallText("VI-USD :" + String.format("%1$-20s", Tools.getModiMoneyString(nTotal[i])).substring(0, 8) + " __________");
                            print.printSpace(1);
                            break;
                        case 10:
                            isSalesCardExit = true;
                            print.printSmallText("CU-TWD :" + String.format("%1$-20s", Tools.getModiMoneyString(nTotal[i])).substring(0, 8) + " __________");
                            print.printSpace(1);
                            break;
                        case 11:
                            isSalesCardExit = true;
                            print.printSmallText("CU-USD :" + String.format("%1$-20s", Tools.getModiMoneyString(nTotal[i])).substring(0, 8) + " __________");
                            print.printSpace(1);
                            break;
                    }
                }
            }

            if (!isSalesCardExit) {
                print.printSmallText("NIL");
                print.printSpace(1);
            }

            //列印online_PreOrder各卡別及幣別金額
            print.printLine();
            print.printSpace(1);
            print.printSmallText("        PreOrder/VIP       ");
            print.printLine();
            print.printSpace(1);
            DBQuery.PreOrderSummaryPack preOrderPack = DBQuery.getPreOrderSummary(mContext, errMsg);
            if (preOrderPack != null && preOrderPack.list != null) {
                print.printSmallText("                    Check");
                for (int i = 0; i < preOrderPack.list.length; i++) {
                    if (!preOrderPack.list[i].CardType.equals("")) {
                        String text = String.format("%1$-20s",
                                preOrderPack.list[i].CardType + "-" + preOrderPack.list[i].CurDvr + " :" + preOrderPack.list[i].TotalAmount).substring(0, 16);
                        print.printSmallText(text + " _____________");
                        print.printSpace(1);
                    }
                }
                print.printLine();
            } else {
                print.printSmallText("NIL");
                print.printSpace(1);
                print.printLine();
            }

            PrintUpgradeSummarySheet();
            print.printSpace(1);
            PrintTransferInSummarySheet();
            print.printSpace(1);
            PrintTransferOutSummarySheet();

            print.printSpace(2);
            print.printSmallText("CA ID: ___" + "_________________");
            print.printSpace(1);
            print.printSmallText("Signature:" + " ________________");
            print.printSpace(1);
            print.printSmallText("Leader ID:" + " ________________");
            print.printSpace(1);
            print.printSmallText("Signature:" + " ________________");
            print.printSpace(2);

            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    /**
     * 列印關櫃報表
     */
    public int printEndInventory() {
        int ret;
        try {
            printHeader();
            print.printSpace(2);
            print.printBigText("  End Inventory");
            print.printLine();

            //列印 Drawer 00, 取得所有Preoder商品數量
            String preorderQty = DBQuery.getPreorderEndInventoryItemQty(mContext, errMsg, Integer.toString(SecSeq));
            if (_CartNo.toLowerCase().contains("a")) {
                if (preorderQty == null) {
                    print.printSmallText("DRAWD0 0" + " PreOrder/VIP");
                } else {
                    print.printSmallText("DRAWD0 " + preorderQty + " PreOrder/VIP");
                }
            }


            //列印 Damage 庫存
            DBQuery.DrawNoPack drawNoPack = DBQuery.getAllDrawerNo(mContext, errMsg, Integer.toString(SecSeq), null, false);
            DBQuery.DamageItemPack damageItemPack = DBQuery.getEndInventoryDamageQty(mContext, errMsg, Integer.toString(SecSeq));
            if (drawNoPack != null && drawNoPack.drawers[0] != null) {
                for (int i = 1; i < drawNoPack.drawers.length; i++) {

                    if (damageItemPack != null && damageItemPack.damages != null) {
                        int damageTotal = 0;
                        for (int j = 0; j < damageItemPack.damages.length; j++) {
                            if (drawNoPack.drawers[i].DrawNo.equals(damageItemPack.damages[j].DrawNo)) {
                                damageTotal += damageItemPack.damages[j].DamageQty;
                            }
                        }
                        if (damageTotal != 0) {
                            print.printSmallText("DRAW" + drawNoPack.drawers[i].DrawNo + " " + drawNoPack.drawers[i].Qty + " Damage:" + damageTotal);
                        } else {
                            print.printSmallText("DRAW" + drawNoPack.drawers[i].DrawNo + " " + drawNoPack.drawers[i].Qty);
                        }
                    } else {
                        print.printSmallText("DRAW" + drawNoPack.drawers[i].DrawNo + " " + drawNoPack.drawers[i].Qty);
                    }

                }
                print.printLine();
            }

            if (damageItemPack != null && damageItemPack.damages != null) {
                print.printSpace(2);
                print.printBigText("      Damage");
                print.printLine();
                print.printSmallText("No  Code    Item   Draw Qty");

                for (int i = 0; i < damageItemPack.damages.length; i++) {
                    String serialcode = damageItemPack.damages[i].SerialCode;
                    String itemcode = damageItemPack.damages[i].ItemCode;
                    String itemname = damageItemPack.damages[i].ItemName;
                    String draw = damageItemPack.damages[i].DrawNo;
                    int qty = damageItemPack.damages[i].DamageQty;
                    print.printSmallText(serialcode + " " + itemcode + " "
                            + String.format("%1$10s", itemname).substring(0, 6) + " "
                            + String.format("%1$-4s", draw) + " " + String.format("%1$3s", qty));
                }
                print.printSpace(1);
            }

            //PreOrder
            if (_CartNo.toLowerCase().contains("a")) {
                FlightInfoPack flightInfo = DBQuery.getFlightInfo(mContext, errMsg);

                print.printLine();
                print.printSpace(2);
                print.printBigText("     PreOrder");
                print.printLine();
                if (flightInfo != null && flightInfo.flights != null) {
                    DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getInventoryPreorderDetail(
                            mContext, errMsg, Integer.toString(SecSeq), new String[]{"PR"}, false);

                    if (preorderInfoPack != null && preorderInfoPack.info != null) {
                        for (int i = 0; i < flightInfo.flights.length; i++) {
                            for (int j = 0; j < preorderInfoPack.info.length; j++) {
                                if (flightInfo.flights[i].SecSeq.equals(preorderInfoPack.info[j].SecSeq)) {
                                    print.printSpace(1);
                                    print.printSmallText(flightInfo.flights[i].FlightNo + "-" + flightInfo.flights[i].DepStn + " to " + flightInfo.flights[i].ArivStn);
                                    break;
                                }
                            }

                            for (int j = 0; j < preorderInfoPack.info.length; j++) {
                                if (flightInfo.flights[i].SecSeq.equals(preorderInfoPack.info[j].SecSeq)) {
                                    print.printSpace(1);
                                    print.printSmallText("  PreOrderNo:" + preorderInfoPack.info[j].PreorderNO);
                                    print.printSmallText("No");
                                    for (int k = 0; k < preorderInfoPack.info[j].items.length; k++) {
                                        String ItemName;
                                        if (preorderInfoPack.info[j].items[k].ItemName.length() < 18) {
                                            ItemName = String.format("%1$-17s", preorderInfoPack.info[j].items[k].ItemName);
                                        } else {
                                            ItemName = preorderInfoPack.info[j].items[k].ItemName.substring(0, 17);
                                        }
                                        print.printSmallText(
                                                preorderInfoPack.info[j].items[k].SerialCode + " " + ItemName + " D0 " + preorderInfoPack.info[j].items[k].SalesQty);
                                    }
                                }
                            }
                        }
                    }
                }
                print.printLine();
                print.printSpace(2);

                //VIP
                print.printBigText("       VIP");
                print.printLine();

                if (flightInfo != null && flightInfo.flights != null) {
                    DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getInventoryPreorderDetail(
                            mContext, errMsg, Integer.toString(SecSeq), new String[]{"VP", "VS"}, false);

                    if (preorderInfoPack != null && preorderInfoPack.info != null) {
                        for (int i = 0; i < flightInfo.flights.length; i++) {

                            for (int j = 0; j < preorderInfoPack.info.length; j++) {
                                if (flightInfo.flights[i].SecSeq.equals(preorderInfoPack.info[j].SecSeq)) {
                                    print.printSpace(1);
                                    print.printSmallText(flightInfo.flights[i].FlightNo + "-" + flightInfo.flights[i].DepStn + " to " + flightInfo.flights[i].ArivStn);
                                    break;
                                }
                            }

                            for (int j = 0; j < preorderInfoPack.info.length; j++) {
                                if (flightInfo.flights[i].SecSeq.equals(preorderInfoPack.info[j].SecSeq)) {
                                    print.printSpace(1);
                                    if (preorderInfoPack.info[j].PreorderType.equals("VP")) {
                                        print.printSmallText("  VIP Paid:" + preorderInfoPack.info[j].PreorderNO);
                                    } else if (preorderInfoPack.info[j].PreorderType.equals("VS")) {
                                        print.printSmallText("  VIP Sale:" + preorderInfoPack.info[j].PreorderNO);
                                    }
                                    print.printSmallText("No");
                                    for (int k = 0; k < preorderInfoPack.info[j].items.length; k++) {
                                        String ItemName = "";
                                        if (preorderInfoPack.info[j].items[k].ItemName.length() < 18) {
                                            ItemName = String.format("%1$-17s", preorderInfoPack.info[j].items[k].ItemName);
                                        } else {
                                            ItemName = preorderInfoPack.info[j].items[k].ItemName.substring(0, 17);
                                        }
                                        print.printSmallText(
                                                preorderInfoPack.info[j].items[k].SerialCode + " " + ItemName + " D0 " + preorderInfoPack.info[j].items[k].SalesQty);
                                    }
                                }
                            }
                        }
                    }
                }
                print.printSpace(1);
                print.printLine();
            }

            print.printSpace(1);
            print.printLine();
            print.printSpace(2);
            print.printSmallText("No");

            DBQuery.ItemDataPack itemDataPack = DBQuery.getProductInfo(mContext, errMsg, String.valueOf(SecSeq), null, null, 2);
//      for (int i = 0; i < itemDataPack.items.length; i++) {
//        String itemName = String.format("%1$-20s", itemDataPack.items[i].ItemName).substring(0, 16);
            for (int i = 0; i < itemDataPack.items.length; i++) {
                String itemName = "";
                if (itemDataPack.items[i].ItemName.length() < 16) {
                    itemName = String.format("%1$-16s", itemDataPack.items[i].ItemName);
                } else {
                    itemName = itemDataPack.items[i].ItemName.substring(0, 16);
                }

                print.printSmallText(itemDataPack.items[i].SerialCode + " " + itemName + " "
                        + itemDataPack.items[i].DrawNo + " " + itemDataPack.items[i].EndQty);
            }

            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    /**
     * 列印最新庫存
     *
     * @param sort true - serialNo, false - draw
     */
    public int printAllItem(boolean sort) {
        int ret;
        try {
            DBQuery.ItemDataPack itemDataPack;

            printHeader();
            print.printSpace(2);
            if (sort) {
                print.printBigText("Inventory by No.");
                print.printLine();
            } else {
                print.printBigText("    Inventory   ");
                print.printBigText("    by Drawer   ");
                print.printLine();
            }

            DBQuery.DrawNoPack drawNoPack = DBQuery.getAllDrawerNo(mContext, errMsg, String.valueOf(SecSeq), null, false);

            if (drawNoPack != null && drawNoPack.drawers != null) {
                for (int i = 1; i < drawNoPack.drawers.length; i++) {
                    print.printSmallText("DRAW" + drawNoPack.drawers[i].DrawNo + " " + drawNoPack.drawers[i].Qty);
                }
            }

            print.printLine();
            print.printSpace(2);
            print.printSmallText("No");

            if (sort) {
                itemDataPack = DBQuery.getProductInfo(mContext, errMsg, String.valueOf(SecSeq), null, null, 3);
            } else {
                itemDataPack = DBQuery.getProductInfo(mContext, errMsg, String.valueOf(SecSeq), null, null, 2);
            }
            if (itemDataPack != null && itemDataPack.items != null) {
                for (int i = 0; i < itemDataPack.items.length; i++) {
                    String itemName = String.format("%1$-20s", itemDataPack.items[i].ItemName).substring(0, 9);
                    print.printSmallText(
                            itemDataPack.items[i].SerialCode + " " + itemDataPack.items[i].ItemCode + " " +
                                    itemName + " " + itemDataPack.items[i].DrawNo + " " + itemDataPack.items[i].EndQty);
                }
            }

            print.printSpace(2);
            ret = printEnd();
        } catch (Exception e) {
            throw e;
        }
        return ret;
    }

    private void PrintUpgradeSummarySheet() throws Exception {
        int[] Total = new int[10];
        String CreditCardNo;

        DBQuery.UpgradeSummaryPack upgradeSummaryPack = DBQuery.getUpgradeSummary(mContext, errMsg);
        print.printSpace(1);
        print.printSmallText("    Upgrade Summary Sheet   ");
        print.printLine();

        if (upgradeSummaryPack != null && upgradeSummaryPack.list != null) {
            print.printSpace(1);
            print.printSmallText("Original   New          Qty");
            for (int i = 0; i < upgradeSummaryPack.list.length; i++) {
                String from = String.format("%1$-9s", upgradeSummaryPack.list[i].OriginalClass);
                String to = String.format("%1$-9s", upgradeSummaryPack.list[i].NewClass);
                print.printSmallText(from + "  " + to + String.format("%1$7s", String.valueOf(upgradeSummaryPack.list[i].TotalQty)));
            }
            print.printSpace(1);
        } else {
            print.printSpace(1);
            print.printSmallText("NIL");
            print.printSpace(1);
        }
        print.printLine();


        print.printSmallText("           Upgrade          ");
        print.printSpace(1);
        print.printSmallText("     Credit Card in USD     ");
        print.printLine();
        DBQuery.CreditCardAmtPack creditCardAmt = DBQuery.getCreditCardAmtPack(mContext, errMsg, "USD");
        if (creditCardAmt != null && creditCardAmt.list != null) {
            print.printSpace(1);
            for (int i = 0; i < creditCardAmt.list.length; i++) {
                switch (creditCardAmt.list[i].CardType.substring(0, 1)) {
                    case "M":
                        Total[7] = Total[7] + creditCardAmt.list[i].TotalPrice;
                        break;
                    case "A":
                        Total[1] = Total[1] + creditCardAmt.list[i].TotalPrice;
                        break;
                    case "D":
                        Total[3] = Total[3] + creditCardAmt.list[i].TotalPrice;
                        break;
                    case "J":
                        Total[5] = Total[5] + creditCardAmt.list[i].TotalPrice;
                        break;
                    default:
                        Total[9] = Total[9] + creditCardAmt.list[i].TotalPrice;
                        break;
                }
                CreditCardNo = AESEncrypDecryp.getDectyptData(creditCardAmt.list[i].CardNo, key);
                CreditCardNo = CreditCardNo.substring(0, 6) + "******" + CreditCardNo.substring(CreditCardNo.length() - 4);

                print.printSmallText(String.format("%1$-22s", "Receipt No: " + creditCardAmt.list[i].ReceiptNo) + "Check");
                print.printSmallText(String.format("%1$-22s", "Amt: " + Tools.getModiMoneyString(creditCardAmt.list[i].TotalPrice)) + "_____");
                print.printSmallText("CardNo:" + CreditCardNo);
                print.printSmallText("CardType:" + creditCardAmt.list[i].CardType);

                print.printSpace(1);
            }
        } else {
            print.printSpace(1);
            print.printSmallText("NIL");
            print.printSpace(1);
        }
        print.printLine();


        print.printSpace(1);
        print.printSmallText("     Credit Card in TWD     ");
        print.printLine();
        DBQuery.CreditCardAmtPack TWDamtPack = DBQuery.getCreditCardAmtPack(mContext, errMsg, "TWD");
        if (TWDamtPack != null && TWDamtPack.list != null) {
            print.printSpace(1);
            for (int i = 0; i < TWDamtPack.list.length; i++) {
                switch (TWDamtPack.list[i].CardType.substring(0, 1)) {
                    case "M":
                        Total[6] = Total[6] + TWDamtPack.list[i].TotalPrice;
                        break;
                    case "A":
                        Total[0] = Total[0] + TWDamtPack.list[i].TotalPrice;
                        break;
                    case "D":
                        Total[2] = Total[2] + TWDamtPack.list[i].TotalPrice;
                        break;
                    case "J":
                        Total[4] = Total[4] + TWDamtPack.list[i].TotalPrice;
                        break;
                    default:
                        Total[8] = Total[8] + TWDamtPack.list[i].TotalPrice;
                        break;
                }
                CreditCardNo = AESEncrypDecryp.getDectyptData(TWDamtPack.list[i].CardNo, key);
                CreditCardNo = CreditCardNo.substring(0, 6) + "******" + CreditCardNo.substring(CreditCardNo.length() - 4);

                print.printSmallText(String.format("%1$-22s", "Receipt No: " + TWDamtPack.list[i].ReceiptNo) + "Check");
                print.printSmallText(String.format("%1$-22s", "Amt: " + Tools.getModiMoneyString(TWDamtPack.list[i].TotalPrice)) + "_____");
                print.printSmallText("CardNo:" + CreditCardNo);
                print.printSmallText("CardType:" + TWDamtPack.list[i].CardType);

                print.printSpace(1);
            }
        } else {
            print.printSpace(1);
            print.printSmallText("NIL");
            print.printSpace(1);
        }
        print.printLine();

        print.printSpace(1);
        print.printSmallText("        Upgrade Amount      ");
        print.printLine();
        print.printSpace(1);
        print.printSmallText("Cash                Check");
        DBQuery.CashTotalAmtPack f;
        boolean isCashExit = false;

        f = DBQuery.getUpgradeAmount(mContext, errMsg, "TWD");
        if (f != null && f.list != null && f.list[0].Amount != 0) {
            isCashExit = true;
            print.printSmallText("TWD: " + String.format("%1$-20s", Tools.getModiMoneyString(f.list[0].Amount)).substring(0, 11) + " __________");
            print.printSpace(1);
        }

        f = DBQuery.getUpgradeAmount(mContext, errMsg, "USD");
        if (f != null && f.list != null && f.list[0].Amount != 0) {
            isCashExit = true;
            print.printSmallText("USD: " + String.format("%1$-20s", Tools.getModiMoneyString(f.list[0].Amount)).substring(0, 11) + " __________");
            print.printSpace(1);
        }
        if (!isCashExit) {
            print.printSmallText("NIL");
            print.printSpace(1);
        }

        print.printLine();
        print.printSpace(1);
        print.printSmallText("Credit Card         Check");
        boolean isCardExit = false;
        for (int i = 0; i < 10; i++) {
            if (Total[i] > 0) {
                switch (i) {
                    case 0:
                        isCardExit = true;
                        print.printSmallText("AX-TWD :" + String.format("%1$-20s", Tools.getModiMoneyString(Total[i])).substring(0, 8) + " __________");
                        print.printSpace(1);
                        break;
                    case 1:
                        isCardExit = true;
                        print.printSmallText("AX-USD :" + String.format("%1$-20s", Tools.getModiMoneyString(Total[i])).substring(0, 8) + " __________");
                        print.printSpace(1);
                        break;
                    case 2:
                        isCardExit = true;
                        print.printSmallText("DC-TWD :" + String.format("%1$-20s", Tools.getModiMoneyString(Total[i])).substring(0, 8) + " __________");
                        print.printSpace(1);
                        break;
                    case 3:
                        isCardExit = true;
                        print.printSmallText("DC-USD :" + String.format("%1$-20s", Tools.getModiMoneyString(Total[i])).substring(0, 8) + " __________");
                        print.printSpace(1);
                        break;
                    case 4:
                        isCardExit = true;
                        print.printSmallText("JC-TWD :" + String.format("%1$-20s", Tools.getModiMoneyString(Total[i])).substring(0, 8) + " __________");
                        print.printSpace(1);
                        break;
                    case 5:
                        isCardExit = true;
                        print.printSmallText("JC-USD :" + String.format("%1$-20s", Tools.getModiMoneyString(Total[i])).substring(0, 8) + " __________");
                        print.printSpace(1);
                        break;
                    case 6:
                        isCardExit = true;
                        print.printSmallText("MT-TWD :" + String.format("%1$-20s", Tools.getModiMoneyString(Total[i])).substring(0, 8) + " __________");
                        print.printSpace(1);
                        break;
                    case 7:
                        isCardExit = true;
                        print.printSmallText("MT-USD :" + String.format("%1$-20s", Tools.getModiMoneyString(Total[i])).substring(0, 8) + " __________");
                        print.printSpace(1);
                        break;
                    case 8:
                        isCardExit = true;
                        print.printSmallText("VI-TWD :" + String.format("%1$-20s", Tools.getModiMoneyString(Total[i])).substring(0, 8) + " __________");
                        print.printSpace(1);
                        break;
                    case 9:
                        isCardExit = true;
                        print.printSmallText("VI-USD :" + String.format("%1$-20s", Tools.getModiMoneyString(Total[i])).substring(0, 8) + " __________");
                        print.printSpace(1);
                        break;
                }
            }
        }
        if (!isCardExit) {
            print.printSmallText("NIL");
            print.printSpace(1);
        }
        print.printLine();

    }

    private void PrintTransferOutSummarySheet() {
        String itemName = "";
        print.printSmallText("        Transfer Out        ");
        print.printLine();
        DBQuery.TransferInOutSummaryPack summaryPack = DBQuery.getTransferOutSummary(mContext, errMsg);
        boolean isTransferExit = false;

        if (summaryPack != null && summaryPack.list != null) {
            DBQuery.CartNoPack numbers = DBQuery.getAllCartList(mContext, new StringBuilder());
            for (int k = 0; k < numbers.cartList.length; k++) {
                // 取非自己車號比對Transfer List
                if (!numbers.cartList[k].CartNo.equals(_CartNo)) {
                    boolean flag = false;
                    for (int i = 0; i < summaryPack.list.length; i++) {
                        if (summaryPack.list[i].CarTo.equals(numbers.cartList[k].CartNo)) {
                            print.printSmallText("Cart " + numbers.cartList[k].CartNo);
                            print.printSmallText("No  Item          Qty Check");
                            isTransferExit = true;
                            flag = true;
                            break;
                        }
                    }
                    for (int i = 0; i < summaryPack.list.length; i++) {
                        // 如果為index= k車號則印細項
                        if (summaryPack.list[i].CarTo.equals(numbers.cartList[k].CartNo)) {
                            itemName = String.format("%1$-20s", summaryPack.list[i].ItemName).substring(0, 13);
                            String qty = String.format("%1$3s", String.valueOf(summaryPack.list[i].Qty));
                            print.printSmallText(summaryPack.list[i].SerialCode + " " + itemName + " " + qty + " _____");
                        }
                    }
                    if (flag) {
                        print.printSpace(1);
                        print.printSmallText("CA ID: ___" + "_________________");
                        print.printSpace(1);
                        print.printSmallText("Signature:" + " ________________");
//            print.printSpace(1);
                        print.printLine();
                    }
                }
            }
        }
        if (!isTransferExit) {
            print.printSpace(1);
            print.printSmallText("NIL");
            print.printSpace(1);
            print.printLine();
        }
    }

    private void PrintTransferInSummarySheet() {
        String itemName = "";
        print.printSmallText("         Transfer In        ");
        print.printLine();
        DBQuery.TransferInOutSummaryPack summaryPack = DBQuery.getTransferInSummary(mContext, errMsg);
        boolean isTransferExit = false;

        if (summaryPack != null && summaryPack.list != null) {
            DBQuery.CartNoPack numbers = DBQuery.getAllCartList(mContext, new StringBuilder());
            for (int k = 0; k < numbers.cartList.length; k++) {
                // 取非自己車號比對Transfer List
                if (!numbers.cartList[k].CartNo.equals(_CartNo)) {
                    boolean flag = false;
                    for (int i = 0; i < summaryPack.list.length; i++) {
                        if (summaryPack.list[i].CarFrom.equals(numbers.cartList[k].CartNo)) {
                            print.printSmallText("Cart " + numbers.cartList[k].CartNo);
                            print.printSmallText("No  Item          Qty Check");
                            isTransferExit = true;
                            flag = true;
                            break;
                        }
                    }
                    for (int i = 0; i < summaryPack.list.length; i++) {
                        // 如果為index= k車號則印細項
                        if (summaryPack.list[i].CarFrom.equals(numbers.cartList[k].CartNo)) {
                            itemName = String.format("%1$-20s", summaryPack.list[i].ItemName).substring(0, 13);
                            String qty = String.format("%1$3s", String.valueOf(summaryPack.list[i].Qty));
                            print.printSmallText(summaryPack.list[i].SerialCode + " " + itemName + " " + qty + " _____");
                        }
                    }
                    if (flag) {
                        print.printSpace(1);
                        print.printSmallText("CA ID: ___" + "_________________");
                        print.printSpace(1);
                        print.printSmallText("Signature:" + " ________________");
//            print.printSpace(1);
                        print.printLine();
                    }
                }
            }
        }
        if (!isTransferExit) {
            print.printSpace(1);
            print.printSmallText("NIL");
            print.printSpace(1);
            print.printLine();
        }
    }

}
