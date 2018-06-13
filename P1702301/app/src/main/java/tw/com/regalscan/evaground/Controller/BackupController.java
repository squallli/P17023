package tw.com.regalscan.evaground.Controller;


import android.content.Context;
import com.google.gson.Gson;
import com.regalscan.sqlitelibrary.TSQL;
import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tw.com.regalscan.evaground.Models.BackupInventory;
import tw.com.regalscan.evaground.Models.FlightData;

public class BackupController {

    static private Boolean UpdateBlackDate(Context ctx,String updateDate)
    {
        TSQL tsql = TSQL.getINSTANCE(ctx, "0", "Black");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");


        try {
            Date date = sdf.parse(updateDate);
            sdf = new SimpleDateFormat("yyyy-MM-dd");
            tsql.ExecutesSQLCommand("update VMBlackInfo set VMBlackInfo='" +  sdf.format(date) + "'");
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    static private Boolean UpdateCUPDate(Context ctx,String updateDate)
    {
        TSQL tsql = TSQL.getINSTANCE(ctx, "0", "CUPBlack");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");


        try {
            Date date = sdf.parse(updateDate);
            sdf = new SimpleDateFormat("yyyy-MM-dd");
            tsql.ExecutesSQLCommand("update CUPBlackInfo set CUPBlackInfo='" +  sdf.format(date) + "'");
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    static public Boolean backup(Context ctx, FlightData filightData)
    {
        try {
            TSQL tsql = TSQL.getINSTANCE(ctx, "0", "P17023");

            //先清除所有必需清除的Table
            ArrayList<String> sqlArray = new ArrayList<>();

            BackupInventory[] inventory = null;
            String Mode = "";
            try {
                JSONArray JInventory = tsql.SelectSQLJsonArray("SELECT * FROM Inventory where SecSeq='9'");

                Gson gson = new Gson();
                inventory = gson.fromJson(JInventory.toString(),BackupInventory[].class);

                Mode = (String)tsql.SelectSQLObject("select distinct Mode from Flight");
            } catch (Exception e) {
                e.printStackTrace();
            }
            sqlArray.add("delete from ClassPaymentInfo");
            sqlArray.add("delete from ClassSalesDetail");
            sqlArray.add("delete from ClassSalesHead");
            sqlArray.add("delete from Damage");
            sqlArray.add("delete from OrderChangeHistory");
            sqlArray.add("delete from PaymentInfo");
            sqlArray.add("delete from PreorderDetail");
            sqlArray.add("delete from PreorderHead");
            sqlArray.add("delete from PreorderSalesHead");
            sqlArray.add("delete from SalesDetail");
            sqlArray.add("delete from PreorderSalesHead");
            sqlArray.add("delete from SalesHead");
            sqlArray.add("delete from SystemLog");
            sqlArray.add("delete from Transfer");
            sqlArray.add("delete from Inventory");
            sqlArray.add("delete from Flight");
            sqlArray.add("update allparts set ItemId=''");

            int secSeq = 0;
            for(int i=0;i<filightData.getFlights().length;i++)
            {
                //最後一段為9
                if(secSeq == filightData.getFlights().length - 1) secSeq = 9;

                for(BackupInventory binventory : inventory)
                {
                    sqlArray.add("insert into Inventory(SecSeq,ItemCode,DrawNo,StandQty,StartQty,AdjustQty,SalesQty,TransferQty,DamageQty,EndQty,EGASCheckQty,EGASDamageQty,EVACheckQty,EVADamageQty) values(" +
                            "'" + String.valueOf(secSeq) + "','" + binventory.getItemCode() + "','" +binventory.getDrawNo() + "'," +
                            binventory.getEndQty() + "," + binventory.getEndQty() + ",0,0,0,0," + binventory.getEndQty() + "," +
                            binventory.getEndQty() + ",0," + binventory.getEndQty() + ",0)");
                }

                String DepFlightNo;
                String FlightNo;
                DepFlightNo = filightData.getFlights()[0].getFlightNo();
                FlightNo = filightData.getFlights()[i].getFlightNo();

                sqlArray.add("insert into Flight(DepFlightNo,FlightNo,SecSeq,FlightDate,DepStn,ArivStn,CarNo,ISOpen,ISClose,CrewID,PurserID,IFECatalogID,IFETokenID,Mode,IsUpload) values(" +
                        "'" + DepFlightNo + "','" + FlightNo + "','" + secSeq + "','" + filightData.getFlightDate() + "'," +
                        "'" + filightData.getFlights()[i].getDeparture() + "','" + filightData.getFlights()[i].getDestination() + "',"+
                        "'" + filightData.getCartNo() + "',0,0,'','','','','"+Mode+"','N')");

                secSeq++;
            }

            tsql.ExecutesSQLCommand(sqlArray);

            UpdateBlackDate(ctx,filightData.getFlightDate());

            UpdateCUPDate(ctx,filightData.getFlightDate());

            return true;

        }
        catch (Exception ex)
        {
            return false;
        }

    }
}
