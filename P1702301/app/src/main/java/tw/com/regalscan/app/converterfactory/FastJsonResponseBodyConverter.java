package tw.com.regalscan.app.converterfactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import android.util.Base64;

import com.alibaba.fastjson.JSON;
import com.jess.arms.utils.ZipHelper;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Converter;
import timber.log.Timber;
import tw.com.regalscan.component.AESEncrypDecryp;

import static com.jess.arms.http.log.RequestInterceptor.convertCharset;

/**
 * Created by tp00175 on 2017/12/7.
 */

public class FastJsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private static final String TAG = FastJsonResponseBodyConverter.class.getSimpleName();

    private final Type mType;

    FastJsonResponseBodyConverter(Type type) {
        mType = type;
    }

    @Override
    public T convert(ResponseBody responseBody) throws IOException {

        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();

        Buffer clone = buffer.clone();

        //解析response content
        String bodyString = parseContent(responseBody, null, clone);

        if (bodyString.substring(0, 1).equals("\"")) {
            bodyString = bodyString.replace("\"", "");
            try {
                bodyString = AESEncrypDecryp.getDectyptData(bodyString, "P35SMASC");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            int startPosition = bodyString.indexOf("\">");
            int endPosition = bodyString.indexOf("</");
            bodyString = bodyString.substring(startPosition + 2, endPosition);
            bodyString = bodyString.replace("=", "");

            bodyString = new String(Base64.decode(bodyString, Base64.DEFAULT), StandardCharsets.UTF_8);
        }

        Timber.tag(TAG).w(bodyString);

        return JSON.parseObject(bodyString, mType);
    }

    /**
     * 解析服务器响应的内容
     *
     * @param responseBody
     * @param encoding
     * @param clone
     * @return
     */
    private String parseContent(ResponseBody responseBody, String encoding, Buffer clone) {
        Charset charset = Charset.forName("UTF-8");
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(charset);
        }
        if (encoding != null && encoding.equalsIgnoreCase("gzip")) {//content使用gzip压缩
            return ZipHelper.decompressForGzip(clone.readByteArray(), convertCharset(charset));//解压
        } else if (encoding != null && encoding.equalsIgnoreCase("zlib")) {//content使用zlib压缩
            return ZipHelper.decompressToStringForZlib(clone.readByteArray(), convertCharset(charset));//解压
        } else {//content没有被压缩
            return clone.readString(charset);
        }
    }
}
