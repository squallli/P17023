package tw.com.regalscan.app.converterfactory;

import java.io.IOException;

import android.util.Base64;

import com.alibaba.fastjson.JSON;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;
import tw.com.regalscan.utils.Tools;

/**
 * Created by tp00175 on 2017/12/7.
 */

public class FastJsonRequestBodyConverter<T> implements Converter<T, RequestBody> {

//    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
    private static final MediaType MEDIA_TYPE = MediaType.parse("text/xml; charset=UTF-8");

    @Override
    public RequestBody convert(T value) {

        String strBase64 = Base64.encodeToString(Tools.stringToByte(value.toString(), "UTF-8"), Base64.NO_WRAP);

        return RequestBody.create(MEDIA_TYPE, JSON.toJSONBytes(strBase64));
    }
}
