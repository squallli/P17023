package tw.com.regalscan.HttpRequest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Base64;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import timber.log.Timber;
import tw.com.regalscan.utils.Tools;

/**
 * Created by tp00175 on 2017/7/5.
 */

public class Http_Post extends Service {

    private static final String TAG = Http_Post.class.getSimpleName();

    private static String strTxt = null;
    private static String postUrl = null;
    private static String strResult = null;

    public static void Post(final CallBack callBack, JSONObject StrTxt, final String PostUrl, final Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm != null ? cm.getActiveNetworkInfo() : null;
        if (networkInfo == null) {
            callBack.onError("No available connection.");
            return;
        }

        strTxt = Base64.encodeToString(Tools.stringToByte(StrTxt.toString(), "UTF-8"), Base64.NO_WRAP);

        Timber.tag(TAG).d(strTxt);

        postUrl = PostUrl;
        new Thread(() -> {
            try {
                //設定Http參數
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 60000);
                HttpConnectionParams.setSoTimeout(httpParams, 60000);
                //建立HttpClient物件
                HttpClient httpClient = new DefaultHttpClient(httpParams);
                //建立一個Post物件，並給予要連線的Url
                HttpPost httpPost = new HttpPost(postUrl);
                //建立一個ArrayList且需是NameValuePair，此ArrayList是用來傳送給Http server端的訊息
                List params = new ArrayList();
                params.add(new BasicNameValuePair("InputContent", strTxt));


                //發送Http Request，內容為params，且為UTF8格式
                httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                //接收Http Server的回應
                HttpResponse httpResponse = httpClient.execute(httpPost);
                //判斷Http Server是否回傳OK(200)
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    //將Post回傳的值轉為String，將轉回來的值轉為UTF8，否則若是中文會亂碼

                    XPathFactory xPathFactory = XPathFactory.newInstance();
                    XPath xPath = xPathFactory.newXPath();
//                    NodeList nodeList = xPath.evaluate("string", new InputSource())

                    HttpEntity httpEntity = httpResponse.getEntity();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpEntity.getContent(), "UTF-8"), 8 * 1024);
                    StringBuilder entityStringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        entityStringBuilder.append(line);
                    }

                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setValidating(false);
                    DocumentBuilder documentBuilder = factory.newDocumentBuilder();

                    InputStream inputStream = new ByteArrayInputStream(entityStringBuilder.toString().getBytes("UTF-8"));
                    Document document = documentBuilder.parse(inputStream);

//                    strResult = EntityUtils.toString(httpEntity, HTTP.UTF_8);

                    Node node = (Node) xPath.evaluate("string", document, XPathConstants.NODE);

//                    int startposition = strResult.indexOf("\">");
//                    int endposition = strResult.indexOf("</");
//                    strResult = strResult.substring(startposition + 2, endposition);
//                    strResult = strResult.replace("=", "");
                    strResult = new String(Base64.decode(node.getTextContent(), Base64.DEFAULT), StandardCharsets.UTF_8);

                    try {
                        JSONObject jsonObject = new JSONObject(strResult);
                        callBack.onSuccess(jsonObject);
                    } catch (JSONException e) {
                        callBack.onError(e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                callBack.onError(e.getMessage());
                // Log exception
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
