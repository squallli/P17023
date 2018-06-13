package tw.com.regalscan.evaair.ife.Education;

import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import aero.panasonic.inflight.crew.services.ordermanagement.v1.CrewOrder;
import aero.panasonic.inflight.crew.services.ordermanagement.v1.OrderStatus;

public class EducationCrewOrder {
    private List<CrewOrder> Orders;

    public EducationCrewOrder(OrderStatus status)
    {
        String json;
        if(status == OrderStatus.ORDER_STATUS_OPEN)
        {
            json = EducationUtility.loadJSONFromFile("OrderOpen.json");
        }
        else
        {
            json = EducationUtility.loadJSONFromFile("OrderPROC.json");
        }

        json = json.replace("\n","");
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();

        Orders = gson.fromJson(json,new TypeToken<ArrayList<CrewOrder>>(){}.getType());

    }

    public List<CrewOrder> getOrders() {
        return Orders;
    }

    public CrewOrder getOrder(String OrderId)
    {
        for(CrewOrder c:Orders)
        {
            if(c.getOrderId().equals(OrderId))
            {
                return c;
            }
        }

        return null;
    }

    public String loadJSONFromFile(OrderStatus status) {
        String jsonStr = null;
        try {
            File fs;
            if(status == OrderStatus.ORDER_STATUS_OPEN)
            {
                fs = new File(Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator +  "Education" + File.separator + "OrderOpen.json");
            }
            else
            {
                fs = new File(Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator +  "Education" + File.separator + "OrderPROC.json");
            }


            FileInputStream stream = new FileInputStream(fs);

            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                jsonStr = Charset.defaultCharset().decode(bb).toString();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally {
                stream.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return jsonStr;

    }
}