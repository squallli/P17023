package tw.com.regalscan.evaair.ife.Education;


import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import tw.com.regalscan.evaair.ife.entity.Catalog;

public class EducationUtility {

    public static String loadJSONFromFile(String fileName) {
        String jsonStr = null;
        try {
            File fs;
            fs = new File(Environment.getExternalStorageDirectory() + File.separator + "Download" + File.separator + "Education" + File.separator + fileName);


            FileInputStream stream = new FileInputStream(fs);

            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                jsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return jsonStr;

    }

    public static List<Catalog> populateCatalogs(JSONArray json) {
        Catalog catalog;
        List<Catalog> mCatalogs = new ArrayList<>();
        for (int i = 0; i < json.length(); i++) {
            try {
                catalog = new Catalog(json.getJSONObject(i));

                mCatalogs.add(catalog);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return mCatalogs;
    }
}
