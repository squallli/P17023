package tw.com.regalscan.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.eva.Printer;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.List;

import tw.com.regalscan.R;
import tw.com.regalscan.component.FlightInfoManager;
import tw.com.regalscan.customClass.PrtData;
import tw.com.regalscan.db02.DBFunctions;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.SalesTotalList;
import tw.com.regalscan.evaground.ItemInfo;
import tw.com.regalscan.evaground.Models.RtnObject;

/**
 * Created by tp00175 on 2017/6/14.
 */

public class PrintGround {

    private final Printer mPrinter = new Printer();
    private StringBuilder errMsg = new StringBuilder();
    private Context context;
    private String SecSeq;
    private String CartNo = "";


    public PrintGround(Context context) {
        this.context = context;
        SecSeq = FlightInfoManager.getInstance().getCurrentSecSeq();
    }

    private void printHeader() {

//    DateTimeZone dateTimeZone = DateTimeZone.forID("Asia/Taipei");
//    DateTimeZone dateTimeZone = DateTimeZone.forID("Europe/London");
//    DateTime dateTime = new DateTime(dateTimeZone);
//    String year = String.valueOf(dateTime.getYear());
//    String month = String.format("%02d", dateTime.getMonthOfYear());
//    String day = String.format("%02d", dateTime.getDayOfMonth());

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.eva_logo);
        int oldwidth = bitmap.getWidth();
        int oldheight = bitmap.getHeight();

        float scaleWidth = 384 / (float) oldwidth;
        float scaleHeight = 83 / (float) oldheight;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, oldwidth, oldheight, matrix, true);

        mPrinter.Open();

        mPrinter.printImg(resizedBitmap);

        DBQuery.FlightInfo flightInfo = DBQuery.getFlightInfo(context, errMsg, "0");

        mPrinter.printSpace(3);
        mPrinter.printLine();
        mPrinter.printBigText("CART_NO:" + flightInfo.CarNo);
        mPrinter.printBigText("FLT_NO: " + flightInfo.FlightNo);
        mPrinter.printBigText("ROUTE:  " + flightInfo.DepStn + "-" + flightInfo.ArivStn);
        mPrinter.printBigText("DATE:   " + flightInfo.FlightDate);

        CartNo = flightInfo.CarNo;
    }

    private int printEnd() {
        mPrinter.printSpace(5);
        mPrinter.printSmallText("         End Print");
        mPrinter.printLine();
        mPrinter.printSpace(7);
        int ret = mPrinter.printPage();
        mPrinter.Close();

        // 沒紙回-1
        return ret;
    }

    /**
     * Print SCR out
     *
     * @param Sort true - draw, false - itemCode
     */
    public int printSCROUT(boolean Sort) {
        printHeader();
        mPrinter.printSpace(2);
        mPrinter.printBigText("     SCR OUT");
        mPrinter.printLine();

        //列印 Drawer 0, 取得所有Preoder商品數量
        if (CartNo.toLowerCase().contains("a")) {
            String preorderQty = DBQuery.getBeginPreorderAllItemQty(context, errMsg, "");
            if (preorderQty == null) {
                mPrinter.printSmallText("DRAWD0 " + "0" + " PreOrder/VIP");
            } else {
                mPrinter.printSmallText("DRAWD0 " + preorderQty + " PreOrder/VIP");
            }
        }

        //列印 Drawer
        DBQuery.DrawNoPack drawNoPack = DBQuery.getAllDrawerNo(context, errMsg, "0", null, false);
        if (drawNoPack != null && drawNoPack.drawers != null) {
            for (int i = 1; i < drawNoPack.drawers.length; i++) {
                mPrinter.printSmallText("DRAW" + drawNoPack.drawers[i].DrawNo + " " + drawNoPack.drawers[i].Qty);
            }
            mPrinter.printLine();
        }

        //Print PreOrder
        mPrinter.printSpace(2);
        mPrinter.printBigText("    PreOrder");
        mPrinter.printSpace(1);
        mPrinter.printLine();
        String[] strings = new String[]{"PR"};
        DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, strings, null);

        int Total = 0;
        if (preorderInfoPack != null && preorderInfoPack.info != null) {
            for (int i = 0; i < preorderInfoPack.info.length; i++) {
                mPrinter.printSmallText("PreOrderNo:" + preorderInfoPack.info[i].PreorderNO);
                mPrinter.printSmallText("Code    Item            Qty");
                for (int j = 0; j < preorderInfoPack.info[i].items.length; j++) {
                    String code = preorderInfoPack.info[i].items[j].ItemCode;
                    String ItemName;
                    if (preorderInfoPack.info[i].items[j].ItemName.length() < 16) {
                        ItemName = String.format("%1$-15s", preorderInfoPack.info[i].items[j].ItemName);
                    } else {
                        ItemName = preorderInfoPack.info[i].items[j].ItemName.substring(0, 15);
                    }
                    int qty = preorderInfoPack.info[i].items[j].SalesQty;
                    mPrinter.printSmallText(code + " " + ItemName + "   " + qty);
                    Total += qty;
                }
                mPrinter.printSpace(1);
            }
            mPrinter.printLine();
        }
        mPrinter.printSpace(1);
        mPrinter.printSmallText("Total Item = " + Total);
        mPrinter.printSpace(1);
        mPrinter.printLine();

        //Print VIP
        mPrinter.printSpace(1);
        mPrinter.printBigText("       VIP");
        mPrinter.printSpace(1);
        mPrinter.printLine();

        String[] vip = new String[]{"VS", "VP"};
        DBQuery.PreorderInfoPack vipPack = DBQuery.getPreorderInfo(context, errMsg, null, vip, null);

        Total = 0;
        if (vipPack != null && vipPack.info != null) {
            for (int i = 0; i < vipPack.info.length; i++) {
                mPrinter.printSmallText("VIP No:" + vipPack.info[i].PreorderNO);
                mPrinter.printSmallText("Code    Item            Qty");
                for (int j = 0; j < vipPack.info[i].items.length; j++) {
                    String code = vipPack.info[i].items[j].ItemCode;
                    String ItemName;
                    if (vipPack.info[i].items[j].ItemName.length() < 16) {
                        ItemName = String.format("%1$-15s", vipPack.info[i].items[j].ItemName);
                    } else {
                        ItemName = vipPack.info[i].items[j].ItemName.substring(0, 15);
                    }
                    int qty = vipPack.info[i].items[j].SalesQty;
                    mPrinter.printSmallText(code + " " + ItemName + "   " + qty);
                    Total += qty;
                }
                mPrinter.printSpace(1);
            }
            mPrinter.printLine();
        }
        mPrinter.printSpace(1);
        mPrinter.printSmallText("Total Item = " + Total);
        mPrinter.printSpace(1);

        //Print All items
        DBQuery.ItemDataPack itemDataPack;
        if (Sort) {
            itemDataPack = DBQuery.getProductInfo(context, errMsg, "0", null, null, 0);
        } else {
            itemDataPack = DBQuery.getProductInfo(context, errMsg, "0", null, null, 1);
        }
        if (itemDataPack != null && itemDataPack.items != null) {
            for (int i = 0; i < itemDataPack.items.length; i++) {
                String itemName;
                if (itemDataPack.items[i].ItemName.length() < 15) {
                    itemName = String.format("%1$-14s", itemDataPack.items[i].ItemName);
                } else {
                    itemName = itemDataPack.items[i].ItemName.substring(0, 14);
                }
                mPrinter.printSmallText(itemDataPack.items[i].ItemCode + " " + itemName + " " + itemDataPack.items[i].DrawNo + " " + itemDataPack.items[i].StartQty);
            }
        }
        return printEnd();
    }

    public int printEGASCheck(DateTime timeStart, DateTime timeEnd) {
        printHeader();
        mPrinter.printSpace(2);
        mPrinter.printBigText("   UPDATE QTY");
        mPrinter.printLine();
        mPrinter.printSmallText("User ID:" + RtnObject.getInstance().getEmployeeID());
        mPrinter.printSmallText(
                "TIME : " + String.format("%02d", timeStart.getHourOfDay()) + ":" + String.format("%02d", timeStart.getMinuteOfHour()) + "~" + String.format("%02d",
                        timeEnd.getHourOfDay()) + ":" + String.format("%02d", timeEnd.getMinuteOfHour()));
        Period period = new Period(timeStart, timeEnd);
        PeriodFormatter periodFormatter = new PeriodFormatterBuilder().appendMinutes().appendSuffix(":").appendSeconds().printZeroAlways().toFormatter();
        String spendtime = periodFormatter.print(period);
        mPrinter.printSmallText("TOTAL TIME : " + spendtime);
        mPrinter.printSpace(1);


        ArrayList<ItemInfo>  itemInfos = DBQuery.getEGASDiscrepancyItemList(context, errMsg);
        //DBQuery.ItemDataPack itemDataPack = DBQuery.getProductInfo(context, errMsg, "9", null, null, 0);
        DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, new String[]{"PR"}, null);
        DBQuery.PreorderInfoPack vipInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, new String[]{"VP", "VS"}, null);

//        if (itemDataPack != null && itemDataPack.items != null) {
//            for (int i = 0; i < itemDataPack.items.length; i++) {
//                if (itemDataPack.items[i].EGASCheckQty != itemDataPack.items[i].StandQty) {
//                    mPrinter.printSmallText(itemDataPack.items[i].ItemCode + " Update to " + String.format("%1$2d", itemDataPack.items[i].EGASCheckQty));
//                }
//            }
//            mPrinter.printSpace(1);
//        }

        if (itemInfos != null ) {
            for (int i = 0; i < itemInfos.size(); i++) {
                if (itemInfos.get(i).getEgascheck() != itemInfos.get(i).getOriginNum()) {
                    mPrinter.printSmallText(itemInfos.get(i).getItemCode() + " Update to " + String.format("%1$2d", itemInfos.get(i).getEgascheck()));
                }
            }
            mPrinter.printSpace(1);
        }

        if (preorderInfoPack != null && preorderInfoPack.info != null) {
            for (int i = 0; i < preorderInfoPack.info.length; i++) {
                if (preorderInfoPack.info[i].EGASSaleFlag.equals("S")) {
                    mPrinter.printSmallText("PreOrderNo:" + preorderInfoPack.info[i].PreorderNO);

                    for (int j = 0; j < preorderInfoPack.info[i].items.length; j++) {
                        if (preorderInfoPack.info[i].EGASSaleFlag.equals("S")) {
                            mPrinter.printSmallText(preorderInfoPack.info[i].items[j].ItemCode + " Update to  0");
                        }
                    }
                    mPrinter.printSpace(1);
                }
            }
        }

        if (vipInfoPack != null && vipInfoPack.info != null) {
            for (int i = 0; i < vipInfoPack.info.length; i++) {
                if (vipInfoPack.info[i].EGASSaleFlag.equals("S")) {
                    mPrinter.printSmallText("PreOrderNo:" + vipInfoPack.info[i].PreorderNO);

                    for (int j = 0; j < vipInfoPack.info[i].items.length; j++) {
                        if (vipInfoPack.info[i].EGASSaleFlag.equals("S")) {
                            mPrinter.printSmallText(vipInfoPack.info[i].items[j].ItemCode + " Update to  0");
                        }
                    }
                    mPrinter.printSpace(1);
                }
            }
        }

        mPrinter.printSpace(2);
        printAllDamageData();
        printEgasDamageData();
        return printEnd();
    }

    public int printEVACheck() {
        printHeader();
        mPrinter.printSpace(2);
        mPrinter.printBigText(" EVA UPDATE QTY");
        mPrinter.printLine();
        mPrinter.printSmallText("User ID:" + RtnObject.getInstance().getEmployeeID());
        mPrinter.printSpace(1);

        DBQuery.ItemDataPack itemDataPack = DBQuery.getProductInfo(context, errMsg, "9", null, null, 0);
        DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, new String[]{"PR"}, null);
        DBQuery.PreorderInfoPack vipInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, new String[]{"VP", "VS"}, null);

        if (itemDataPack != null && itemDataPack.items != null) {
            for (int i = 0; i < itemDataPack.items.length; i++) {
                if (itemDataPack.items[i].EVACheckQty != itemDataPack.items[i].EndQty) {
                    mPrinter.printSmallText(itemDataPack.items[i].ItemCode + " Update to       " + String.format("%1$3d", itemDataPack.items[i].EVACheckQty));
                }
            }
        }

        mPrinter.printSpace(1);

        if (preorderInfoPack != null && preorderInfoPack.info != null) {
            for (int i = 0; i < preorderInfoPack.info.length; i++) {
                if (!preorderInfoPack.info[i].EVASaleFlag.equals(preorderInfoPack.info[i].SaleFlag)) {
                    mPrinter.printSmallText("PreOrderNo:" + preorderInfoPack.info[i].PreorderNO);
                    for (int j = 0; j < preorderInfoPack.info[i].items.length; j++) {
                        if (preorderInfoPack.info[i].EVASaleFlag.equals("S")) {
                            mPrinter.printSmallText(preorderInfoPack.info[i].items[j].ItemCode + " Update to         0");
                        } else {
                            mPrinter.printSmallText(
                                    preorderInfoPack.info[i].items[j].ItemCode + " Update to       " + String.format("%1$3d", preorderInfoPack.info[i].items[j].SalesQty));
                        }
                    }
                    mPrinter.printSpace(1);
                }
            }
        }

        if (vipInfoPack != null && vipInfoPack.info != null) {
            for (int i = 0; i < vipInfoPack.info.length; i++) {
                if (!vipInfoPack.info[i].EVASaleFlag.equals(vipInfoPack.info[i].SaleFlag)) {
                    mPrinter.printSmallText("PreOrderNo:" + vipInfoPack.info[i].PreorderNO);
                    for (int j = 0; j < vipInfoPack.info[i].items.length; j++) {
                        if (vipInfoPack.info[i].EVASaleFlag.equals("S")) {
                            mPrinter.printSmallText(vipInfoPack.info[i].items[j].ItemCode + " Update to         0");
                        } else {
                            mPrinter.printSmallText(vipInfoPack.info[i].items[j].ItemCode + " Update to       " + String.format("%1$3d", vipInfoPack.info[i].items[j].SalesQty));
                        }
                    }
                    mPrinter.printSpace(1);
                }
            }
        }
        return printEnd();
    }

    public int printReloadQty() {
        printHeader();
        mPrinter.printSpace(2);
        mPrinter.printBigText("  Reload Sheet");
        mPrinter.printLine();
        mPrinter.printSpace(1);
        mPrinter.printSmallText("User ID:" + RtnObject.getInstance().getEmployeeID());
        mPrinter.printSpace(1);
        mPrinter.printLine();

        DBQuery.PreorderInfoPack infoPack = DBQuery.getPreorderInfo(context, errMsg, null, null, null);

        if (infoPack != null && infoPack.info != null) {

            //Print PreOrder
            mPrinter.printBigText(" PreOrder UnSale");
            mPrinter.printLine();
            mPrinter.printSmallText("Code                    Qty");

            String[] preorder = new String[]{"PR"};
            DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, preorder, null);
            if (preorderInfoPack != null && preorderInfoPack.info != null) {
                int count = 0;
                for (int i = 0; i < preorderInfoPack.info.length; i++) {
                    if (!preorderInfoPack.info[i].EVASaleFlag.equals("S")) {
                        mPrinter.printSmallText("PreOrderNo:" + preorderInfoPack.info[i].PreorderNO);
                        for (int j = 0; j < preorderInfoPack.info[i].items.length; j++) {
                            mPrinter.printSmallText(
                                    preorderInfoPack.info[i].items[j].ItemCode + "                 " + String.format("%1$3d", preorderInfoPack.info[i].items[j].SalesQty));
                        }
                        count++;
                        mPrinter.printSpace(1);
                    }
                }
                if (count == 0) {
                    mPrinter.printSmallText("    NIL");
                }
            } else {
                mPrinter.printSmallText("    NIL");
            }

            mPrinter.printLine();
            mPrinter.printBigText("  PreOrder Sold");
            mPrinter.printLine();
            mPrinter.printSmallText("Code                    Qty");
            if (preorderInfoPack != null && preorderInfoPack.info != null) {
                int count = 0;
                for (int i = 0; i < preorderInfoPack.info.length; i++) {
                    if (preorderInfoPack.info[i].EVASaleFlag.equals("S")) {
                        mPrinter.printSmallText("PreOrderNo:" + preorderInfoPack.info[i].PreorderNO);
                        for (int j = 0; j < preorderInfoPack.info[i].items.length; j++) {
                            mPrinter.printSmallText(
                                    preorderInfoPack.info[i].items[j].ItemCode + " " + String.format("%1$19d", preorderInfoPack.info[i].items[j].SalesQty));
                        }
                        count++;
                        mPrinter.printSpace(1);
                    }
                }
                if (count == 0) {
                    mPrinter.printSmallText("    NIL");
                }
            } else {
                mPrinter.printSmallText("    NIL");
            }

            mPrinter.printLine();
            mPrinter.printSpace(1);

            //Print VIP
            String[] stringsVIP = new String[]{"VS", "VP"};
            DBQuery.PreorderInfoPack VIP = DBQuery.getPreorderInfo(context, errMsg, null, stringsVIP, null);

            mPrinter.printBigText("   VIP UnSale");
            mPrinter.printLine();
            mPrinter.printSmallText("Code                    Qty");
            if (VIP != null && VIP.info != null) {
                int count = 0;
                for (int i = 0; i < VIP.info.length; i++) {
                    if (!VIP.info[i].EVASaleFlag.equals("S")) {
                        mPrinter.printSmallText("VIP No:" + VIP.info[i].PreorderNO);
                        for (int j = 0; j < VIP.info[i].items.length; j++) {
                            mPrinter.printSmallText(
                                    VIP.info[i].items[j].ItemCode + " " + String.format("%1$19d", VIP.info[i].items[j].SalesQty));
                        }
                        count++;
                        mPrinter.printSpace(1);
                    }
                }
                if (count == 0) {
                    mPrinter.printSmallText("    NIL");
                }
            } else {
                mPrinter.printSmallText("    NIL");
            }

            mPrinter.printLine();
            mPrinter.printBigText("    VIP Sold");
            mPrinter.printLine();
            mPrinter.printSmallText("Code                    Qty");
            if (VIP != null && VIP.info != null) {
                int count = 0;
                for (int i = 0; i < VIP.info.length; i++) {
                    if (VIP.info[i].EVASaleFlag.equals("S")) {
                        mPrinter.printSmallText("VIP No:" + VIP.info[i].PreorderNO);
                        for (int j = 0; j < VIP.info[i].items.length; j++) {
                            mPrinter.printSmallText(VIP.info[i].items[j].ItemCode + "                   0");
                        }
                        count++;
                    }
                    mPrinter.printSpace(1);
                }
                if (count == 0) {
                    mPrinter.printSmallText("    NIL");
                }
            } else {
                mPrinter.printSmallText("    NIL");
            }
        }

        mPrinter.printLine();

        printAllDamageData();
        printEgasDamageData();

        mPrinter.printBigText("   Cart Sales");
        mPrinter.printLine();

        //Print
        DBQuery.ItemDataPack OriginalItemPack = DBQuery.getProductInfo(context, errMsg, "0", null, null, 0);
        DBQuery.ItemDataPack itemDataPack = DBQuery.getProductInfo(context, errMsg, "9", null, null, 0);
        int total = 0;
//    for (int i = 0; i < OriginalItemPack.items.length; i++) {
//      for (int j = 0; j < FinalItemPack.items.length; j++) {
//        if (OriginalItemPack.items[i].ItemCode.equals(FinalItemPack.items[j].ItemCode)) {
//          if (OriginalItemPack.items[i].StartQty != FinalItemPack.items[j].EVACheckQty) {
//            int DifQty = OriginalItemPack.items[i].SalesQty - (FinalItemPack.items[j].EndQty + FinalItemPack.items[j].DamageQty) - FinalItemPack.items[j].EVACheckQty;
//            mPrinter.printSmallText(
//                FinalItemPack.items[j].ItemCode + " " + String.format("%1$-20s", FinalItemPack.items[j].ItemName).substring(0, 10) + " " + String
//                    .format("%3d", DifQty) + " " + FinalItemPack.items[j].DrawNo);
//            total += 1;
//          }
//        }
//      }
//    }

        SalesTotalList totalList = DBQuery.getSalesQty(context, errMsg);
        if (totalList != null && totalList.list != null) {
            for (int i = 0; i < totalList.list.length; i++) {
                for (int j = 0; j < itemDataPack.items.length; j++) {
                    if (totalList.list[i].ItemCode.equals(itemDataPack.items[j].ItemCode)) {
                        int diffQty = totalList.list[i].SalesTotal + (itemDataPack.items[j].EndQty - itemDataPack.items[j].EVACheckQty);
                        if (diffQty > 0) {
                            mPrinter.printSmallText(itemDataPack.items[j].ItemCode + " " +
                                    String.format("%1$-20s", itemDataPack.items[j].ItemName).substring(0, 10) + " " +
                                    String.format("%1$3d", diffQty) + " " + String.format("%1$4s", itemDataPack.items[j].DrawNo));
                            total += 1;
                        }
                    }
                }
            }
        }

        mPrinter.printSpace(2);
        mPrinter.printSmallText("Total Item = " + total);
        mPrinter.printSpace(1);

        return printEnd();

    }

    public int printDrawQty() {

        DBQuery.FlightInfo flightInfo = DBQuery.getFlightInfo(context, errMsg, "0");

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.eva_logo);
        int oldwidth = bitmap.getWidth();
        int oldheight = bitmap.getHeight();

        float scaleWidth = 384 / (float) oldwidth;
        float scaleHeight = 83 / (float) oldheight;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, oldwidth, oldheight, matrix, true);

        mPrinter.Open();

        mPrinter.printImg(resizedBitmap);

        mPrinter.printSpace(3);
        mPrinter.printLine();
        mPrinter.printBigText("Date:  " + flightInfo.FlightDate);
        mPrinter.printBigText("DocNo: " + flightInfo.CarNo);
        mPrinter.printBigText("FltNo: " + flightInfo.FlightNo);
        mPrinter.printBigText("Route: " + flightInfo.DepStn + "-" + flightInfo.ArivStn);
        mPrinter.printBigText("StdNo: " + flightInfo.Mode);
        mPrinter.printSpace(2);
        mPrinter.printBigText(" APN UPDATE QTY");


        DBFunctions dbFunctions = new DBFunctions(context, tw.com.regalscan.db.FlightData.SecSeq);
        List<PrtData> prtData = dbFunctions.getPrtData();
        int prtS = 1;
        for (PrtData data : prtData) {

            if (data.getPrt1().equals("C")) {
                if (prtS == 1) {
                    prtS = 2;
                    mPrinter.printLine();
                    mPrinter.printSmallText("Code    Ref_Qty ActQty Draw");
                }
                mPrinter.printSmallText(data.getPrt2() + " " +
                        String.format("%1$7s", data.getPrt3()) + " " +
                        String.format("%1$6s", data.getPrt4()) + " " +
                        String.format("%1$4s", data.getPrt5()));
            } else if(!data.getPrt1().equals("C") && prtS==1 ){
                prtS = 2;
            }

            if (data.getPrt1().equals("D")) {
                if (prtS == 2) {
                    prtS = 3;
                    mPrinter.printSpace(2);
                    mPrinter.printLine();
                    mPrinter.printSmallText("Draw    Qty");
                }

                if (data.getPrt2().equals("D0")) {
                    mPrinter.printSmallText(
                            String.format("%1$4s", data.getPrt2()) + "    " + String.format("%1$3s", data.getPrt3()) + " " + data.getPrt4() + "   PreOrder/VIP");
                } else {
                    if (!data.getPrt2().equals("")) {
                        mPrinter.printSmallText(String.format("%1$4s", data.getPrt2()) + "    " + String.format("%1$3s", data.getPrt3()) + " " + data.getPrt4());
                    }
                }
            }
        }
        //PreOrder
        mPrinter.printLine();
        mPrinter.printSmallText("         PreOrder");
        mPrinter.printLine();
        mPrinter.printSmallText("Code                    Qty");
        DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, new String[]{"PR"}, null);

        ArrayList<String> ar = new ArrayList<>();

        if (preorderInfoPack != null && preorderInfoPack.info != null) {
            for (int i = 0; i < preorderInfoPack.info.length; i++) {
                if (!ar.contains(preorderInfoPack.info[i].PreorderNO)) {
                    ar.add(preorderInfoPack.info[i].PreorderNO);
                }
            }

            if (ar.size() == 0) {
                mPrinter.printSmallText("     NIL");
            } else {
                for (int i = 0; i < ar.size(); i++) {
                    mPrinter.printSmallText("PreOrderNo:" + ar.get(i));
                    DBQuery.PreorderInfoPack infoPack = DBQuery.getPreorderInfo(context, errMsg, ar.get(i), null, null);
                    for (int j = 0; j < infoPack.info[0].items.length; j++) {
                        mPrinter.printSmallText(infoPack.info[0].items[j].ItemCode + " " + String.format("%1$19s", infoPack.info[0].items[j].SalesQty));
                    }
                    mPrinter.printSpace(1);
                }
            }
        }

        //VIPOrder
        mPrinter.printLine();
        mPrinter.printSmallText("            VIP");
        mPrinter.printLine();
        mPrinter.printSmallText("Code                    Qty");

        DBQuery.PreorderInfoPack infoPack = DBQuery.getPreorderInfo(context, errMsg, null, new String[]{"VS", "VP"}, null);
        ar.clear();

        if (infoPack != null && infoPack.info != null) {
            for (int i = 0; i < infoPack.info.length; i++) {
                if (!ar.contains(infoPack.info[i].PreorderNO)) {
                    ar.add(infoPack.info[i].PreorderNO);
                }
            }

            if (ar.size() == 0) {
                mPrinter.printSmallText("     NIL");
            } else {
                for (int i = 0; i < ar.size(); i++) {
                    mPrinter.printSmallText("VIP No:" + ar.get(i));
                    DBQuery.PreorderInfoPack infoPack1 = DBQuery.getPreorderInfo(context, errMsg, ar.get(i), null, null);
                    for (int j = 0; j < infoPack1.info[0].items.length; j++) {
                        mPrinter.printSmallText(infoPack1.info[0].items[j].ItemCode + " " + String.format("%1$19s", infoPack1.info[0].items[j].SalesQty));
                    }
                    mPrinter.printSpace(1);
                }
            }
        }
        return printEnd();
    }

    public int printDiscrep() {
        printHeader();
        mPrinter.printSpace(2);
        mPrinter.printBigText("   Discrepancy");
        mPrinter.printLine();
        mPrinter.printSpace(1);
        mPrinter.printSmallText("User ID:" + RtnObject.getInstance().getEmployeeID());
        mPrinter.printSpace(1);
        mPrinter.printBigText("    PreOrder");
        mPrinter.printLine();
        mPrinter.printSmallText("Code    Item      Crew Egas");

        int count = 0;
        //Print PreOrder
        String[] strings = new String[]{"PR"};
        DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, strings, null);
        if (preorderInfoPack != null && preorderInfoPack.info != null) {

            for (int i = 0; i < preorderInfoPack.info.length; i++) {
                if (!preorderInfoPack.info[i].SaleFlag.equals(preorderInfoPack.info[i].EGASSaleFlag)) {
                    mPrinter.printSmallText("PreOrderNo:" + preorderInfoPack.info[i].PreorderNO);
                    for (int j = 0; j < preorderInfoPack.info[i].items.length; j++) {
                        String itemCode = preorderInfoPack.info[i].items[j].ItemCode;
                        String itemName = String.format("%1$-20s", preorderInfoPack.info[i].items[j].ItemName).substring(0, 9);
                        int saleQty = preorderInfoPack.info[i].items[j].SalesQty;
                        if (preorderInfoPack.info[i].EGASSaleFlag.equals("S")) {
                            if (preorderInfoPack.info[i].SaleFlag.equals("S")) {
                                continue;
                            } else {
                                mPrinter.printSmallText(itemCode + " " + itemName + " " + String.format("%1$4d", saleQty) + " " + String.format("%1$4S", "0"));
                            }
                        } else {
                            if (preorderInfoPack.info[i].SaleFlag.equals("S")) {
                                mPrinter.printSmallText(itemCode + " " + itemName + " " + String.format("%1$4d", 0) + " " + String.format("%1$4S", saleQty));
                            } else {
                                continue;
                            }
                        }
                        count += 1;
                    }
                }
            }
        }
        mPrinter.printSpace(2);
        mPrinter.printSmallText("Total Item = " + count);
        mPrinter.printSpace(1);
        mPrinter.printBigText("     VIP");
        mPrinter.printLine();
        mPrinter.printSmallText("Code    Item      Crew Egas");

        count = 0;

        //Print VIP PreOrder
        String[] VipStrings = new String[]{"VS", "VP"};
        DBQuery.PreorderInfoPack vipInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, VipStrings, null);
        if (vipInfoPack != null && vipInfoPack.info != null) {
            for (int i = 0; i < vipInfoPack.info.length; i++) {
                if (!vipInfoPack.info[i].SaleFlag.equals(vipInfoPack.info[i].EGASSaleFlag)) {
                    mPrinter.printSmallText("VIP No:" + vipInfoPack.info[i].PreorderNO);
                    for (int j = 0; j < vipInfoPack.info[i].items.length; j++) {
                        String itemCode = vipInfoPack.info[i].items[j].ItemCode;
                        String itemName = String.format("%1$-20s", vipInfoPack.info[i].items[j].ItemName).substring(0, 9);
                        int saleQty = vipInfoPack.info[i].items[j].SalesQty;
                        if (vipInfoPack.info[i].EGASSaleFlag.equals("S")) {
                            if (preorderInfoPack.info[i].SaleFlag.equals("S")) {
                                continue;
                            } else {
                                mPrinter.printSmallText(itemCode + " " + itemName + " " + String.format("%1$4d", saleQty) + " " + String.format("%1$4S", "0"));
                            }
                            count += 1;
                        } else {
                            if (preorderInfoPack.info[i].SaleFlag.equals("S")) {
                                mPrinter.printSmallText(itemCode + " " + itemName + " " + String.format("%1$4d", 0) + " " + String.format("%1$4S", saleQty));
                            } else {
                                continue;
                            }
                            count += 1;
                        }

                    }
                }
            }
        }
        mPrinter.printSpace(2);
        mPrinter.printSmallText("Total Item = " + count);
        mPrinter.printSpace(1);
        mPrinter.printBigText("      Cart");
        mPrinter.printLine();
        mPrinter.printSmallText("Code    Item       C. E. D.");
        //Print Normal Item
        count = 0;
        DBQuery.ItemDataPack itemDataPack = DBQuery.getProductInfo(context, errMsg, SecSeq, null, null, 0);
        if (itemDataPack != null && itemDataPack.items != null) {
            for (int i = 0; i < itemDataPack.items.length; i++) {
                if (itemDataPack.items[i].EndQty != itemDataPack.items[i].EGASCheckQty) {
                    String itemName = String.format("%1$-20s", itemDataPack.items[i].ItemName).substring(0, 10);
                    mPrinter.printSmallText(itemDataPack.items[i].ItemCode + " " + itemName + " " + String.format("%1$2d", itemDataPack.items[i].EndQty) + " "
                            + String.format("%1$2d", itemDataPack.items[i].EGASCheckQty) + " " + itemDataPack.items[i].DrawNo);
                    count += 1;
                }
            }
        }
        mPrinter.printSpace(2);
        mPrinter.printSmallText("Total Item = " + count);
        mPrinter.printLine();

        printAllDamageData();
        printEgasDamageData();
        printCrewUpdateQty();
        printAllTransferIn();
        printAllTranferOut();

        return printEnd();
    }

    public int printSCRIN() {
        printHeader();
        mPrinter.printSpace(2);
        mPrinter.printBigText("     SCR IN");
        mPrinter.printSpace(1);
        mPrinter.printSpace(1);
        mPrinter.printSmallText("Code    Item            Qty");
        mPrinter.printLine();

        //Print PreOrder
        mPrinter.printSpace(2);
        mPrinter.printBigText("     PreOrder");
        mPrinter.printSpace(1);
        mPrinter.printLine();
        String[] strings = new String[]{"PR"};
        DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, strings, null);

        if (preorderInfoPack != null && preorderInfoPack.info != null) {
            for (int i = 0; i < preorderInfoPack.info.length; i++) {
                mPrinter.printSmallText("PreOrderNo:" + preorderInfoPack.info[i].PreorderNO);
                for (int j = 0; j < preorderInfoPack.info[i].items.length; j++) {
                    String code = preorderInfoPack.info[i].items[j].ItemCode;
                    String ItemName = String.format("%1$-20s", preorderInfoPack.info[i].items[j].ItemName).substring(0, 15);
                    int qty = preorderInfoPack.info[i].items[j].SalesQty;

                    mPrinter.printSmallText(code + " " + ItemName + "   " + qty);
                }
                mPrinter.printSpace(1);
            }
        } else {
            mPrinter.printSmallText("    NIL");
        }

        mPrinter.printSpace(1);

        //Print VIP
        mPrinter.printBigText("       VIP");
        mPrinter.printSpace(1);
        mPrinter.printSmallText("Code    Item            Qty");
        mPrinter.printLine();

        String[] vip = new String[]{"VS", "VP"};
        DBQuery.PreorderInfoPack vipPack = DBQuery.getPreorderInfo(context, errMsg, null, vip, null);

        if (vipPack != null && vipPack.info != null) {
            for (int i = 0; i < vipPack.info.length; i++) {
                mPrinter.printSmallText("VIP No:" + vipPack.info[i].PreorderNO);
                for (int j = 0; j < vipPack.info[i].items.length; j++) {
                    String code = vipPack.info[i].items[j].ItemCode;
                    String ItemName = String.format("%1$-20s", vipPack.info[i].items[j].ItemName).substring(0, 15);
                    int qty = vipPack.info[i].items[j].SalesQty;

                    mPrinter.printSmallText(code + " " + ItemName + "   " + qty);
                }
                mPrinter.printSpace(1);
            }
        } else {
            mPrinter.printSmallText("    NIL");
        }

        mPrinter.printSpace(1);
        mPrinter.printLine();

        //Print All items
        DBQuery.ItemDataPack itemDataPack = DBQuery.getProductInfo(context, errMsg, "0", null, null, 1);
        int total = 0;
        if (itemDataPack != null && itemDataPack.items != null) {
            for (int i = 0; i < itemDataPack.items.length; i++) {
                String itemName = String.format("%1$-20s", itemDataPack.items[i].ItemName).substring(0, 14);

                mPrinter.printSmallText(
                        itemDataPack.items[i].ItemCode + " " + itemName + " " + itemDataPack.items[i].DrawNo + " " + itemDataPack.items[i].StartQty);
                total += 1;
            }
        }
        mPrinter.printSpace(2);
        mPrinter.printSmallText("Total Item = " + total);
        mPrinter.printSpace(1);

        printAllDamageData();
        return printEnd();
    }


    private void printAllDamageData() {
        mPrinter.printBigText("   Crew Damage");
        mPrinter.printSpace(1);
        mPrinter.printSmallText("Code    Item       Qty Draw");
        int count = 0;

        DBQuery.ItemDataPack itemDataPack = DBQuery.getProductInfo(context, errMsg, "9", null, null, 0);
        if (itemDataPack != null && itemDataPack.items != null) {
            for (int i = 0; i < itemDataPack.items.length; i++) {
                if (itemDataPack.items[i].DamageQty != 0) {
                    mPrinter.printSmallText(
                            itemDataPack.items[i].ItemCode + " " + String.format("%1$20s", itemDataPack.items[i].ItemName).substring(0, 10) + " " + String.format("%3d",
                                    itemDataPack.items[i].DamageQty) + " " + String.format("%1$4s", itemDataPack.items[i].DrawNo));
                    count++;
                }
            }
        }

        if (count == 0) {
            mPrinter.printSmallText("    NIL");
        }

        mPrinter.printSpace(2);
    }

    private void printEgasDamageData() {
        mPrinter.printBigText("   EGAS Damage");
        mPrinter.printSpace(1);
        mPrinter.printSmallText("Code    Item       Qty Draw");
        int count = 0;

        DBQuery.ItemDataPack itemDataPack = DBQuery.getProductInfo(context, errMsg, "9", null, null, 0);
        if (itemDataPack != null && itemDataPack.items != null) {
            for (int i = 0; i < itemDataPack.items.length; i++) {
                if (itemDataPack.items[i].EGASDamageQty != 0) {
                    mPrinter.printSmallText(
                            itemDataPack.items[i].ItemCode + " " + itemDataPack.items[i].ItemName.substring(0, 10) + " " + String.format("%3d", itemDataPack.items[i].EGASDamageQty)
                                    + " " + String.format("%1$4s", itemDataPack.items[i].DrawNo));
                    count++;
                }
            }
        }

        if (count == 0) {
            mPrinter.printSmallText("    NIL");
        }

        mPrinter.printSpace(2);
    }

    private void printAllTransferIn() {
        mPrinter.printBigText("   Transfer In");
        mPrinter.printSpace(1);
        mPrinter.printLine();

        DBQuery.TransferItemPack transferItemPack = DBQuery.queryTransferItemQty(context, errMsg, null, "In");
        if (transferItemPack != null && transferItemPack.transfers != null) {
            for (int i = 0; i < transferItemPack.transfers.length; i++) {
                mPrinter.printSmallText("No      :" + transferItemPack.transfers[i].SerialCode);
                mPrinter.printSmallText("ItemCode:" + transferItemPack.transfers[i].ItemCode);
                mPrinter.printSmallText("ItemName:" + transferItemPack.transfers[i].ItemName);
                mPrinter.printSmallText("From    :" + transferItemPack.transfers[i].CarFrom);
                mPrinter.printSmallText("To      :" + transferItemPack.transfers[i].CarTo);
                mPrinter.printSmallText("Qty     :" + transferItemPack.transfers[i].Qty);

                if (i != transferItemPack.transfers.length - 1) {
                    mPrinter.printLine();
                }
            }
        } else {
            mPrinter.printSmallText("    NIL");
        }
        mPrinter.printLine();
    }

    private void printAllTranferOut() {
        mPrinter.printBigText("  Transfer Out");
        mPrinter.printSpace(1);
        mPrinter.printLine();

        DBQuery.TransferItemPack transferItemPack = DBQuery.queryTransferItemQty(context, errMsg, null, "In");
        if (transferItemPack != null && transferItemPack.transfers != null) {
            for (int i = 0; i < transferItemPack.transfers.length; i++) {
                mPrinter.printSmallText("No      :" + transferItemPack.transfers[i].SerialCode);
                mPrinter.printSmallText("ItemCode:" + transferItemPack.transfers[i].ItemCode);
                mPrinter.printSmallText("ItemName:" + transferItemPack.transfers[i].ItemName);
                mPrinter.printSmallText("From    :" + transferItemPack.transfers[i].CarFrom);
                mPrinter.printSmallText("To      :" + transferItemPack.transfers[i].CarTo);
                mPrinter.printSmallText("Qty     :" + transferItemPack.transfers[i].Qty);

                if (i != transferItemPack.transfers.length - 1) {
                    mPrinter.printLine();
                }
            }
        } else {
            mPrinter.printSmallText("    NIL");
        }
        mPrinter.printLine();
    }

    private void printCrewUpdateQty() {
        mPrinter.printBigText(" Crew Update Qty");
        mPrinter.printLine();
//        mPrinter.printSmallText("Code    Item  Eva Crew Draw");

        int count = 0;
        DBQuery.ItemDataPack itemDataPack = DBQuery.getProductInfo(context, errMsg, "9", null, null, 0);
        if (itemDataPack != null && itemDataPack.items != null) {
            for (int i = 0; i < itemDataPack.items.length; i++) {
                String itemCode = itemDataPack.items[i].ItemCode;
                String itemName = String.format("%1$-20s", itemDataPack.items[i].ItemName).substring(0, 10);
                String drawNo = itemDataPack.items[i].DrawNo;
                int standQty = itemDataPack.items[i].StandQty;
                int adjustQty = itemDataPack.items[i].AdjustQty;
                if (adjustQty != 0) {

                    mPrinter.printSmallText(itemCode + " Update to         " + String.valueOf(standQty + adjustQty));

                    count += 0;
                }
            }
        }
        mPrinter.printSpace(2);
        mPrinter.printSmallText("Total Item = " + count);
        mPrinter.printSpace(1);
        mPrinter.printLine();
    }
}
