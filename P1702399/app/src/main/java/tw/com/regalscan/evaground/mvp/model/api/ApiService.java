package tw.com.regalscan.evaground.mvp.model.api;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.*;
import tw.com.regalscan.app.entity.UserInfo;
import tw.com.regalscan.utils.Constant;

/**
 * Created by tp00175 on 2017/12/6.
 */

public interface ApiService {

    @FormUrlEncoded
    @POST(Constant.LOGIN_URL)
    Observable<UserInfo> login(@Field("InputContent") String strBase64);

    @Streaming
    @GET
    Observable<Response<ResponseBody>> downloadApk(@Url String fileUrl);
}
