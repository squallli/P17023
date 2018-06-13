package tw.com.regalscan.evaair.ife.Education;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import aero.panasonic.inflight.crew.services.cartmanagement.v1.model.CrewCart;

public class EducationCrewCart {

    private List<CrewCart> carts;

    public List<CrewCart> getCarts() {
        return carts;
    }

    public EducationCrewCart(Context ctx)
    {

        carts = new ArrayList<>();
        String json = EducationUtility.loadJSONFromFile("CrewCart.json");

        try {
            JSONArray jsonArray = new JSONArray(json);
            for(int i=0;i<jsonArray.length();i++)
            {
                carts.add(new CrewCart(ctx,jsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public CrewCart getCrewCart(String seatNumber)
    {
        for(CrewCart cart : carts)
        {
            if(cart.getSeatNumber().equals(seatNumber))
            {
                return cart;
            }
        }

        return null;
    }
}
