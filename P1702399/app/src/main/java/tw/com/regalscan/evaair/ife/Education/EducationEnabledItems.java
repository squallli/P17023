package tw.com.regalscan.evaair.ife.Education;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import tw.com.regalscan.evaair.ife.entity.Catalog;

public class EducationEnabledItems {

    public static List<Catalog> getEnabledItems()
    {
        List<Catalog> catalogs = null;
        try {
            JSONArray jsnobject = new JSONArray(EducationUtility.loadJSONFromFile("enabledItems.json"));
            catalogs = EducationUtility.populateCatalogs(jsnobject);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return catalogs;
    }



}
