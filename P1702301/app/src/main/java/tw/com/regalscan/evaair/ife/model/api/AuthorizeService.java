package tw.com.regalscan.evaair.ife.model.api;

import io.reactivex.Observable;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import tw.com.regalscan.evaair.ife.model.entity.AuthorizeModel;

/**
 * Created by tp00175 on 2017/11/8.
 */

public interface AuthorizeService {

    /**
     * 取得授權
     * @return
     */
    @Headers({"Domain-Name: OnlineAuthorize"})
    @POST("/vPosWebApp/api/Autorize/")
    @FormUrlEncoded
    Observable<AuthorizeModel> authorize(@Field("") String data);

    /**
     * 還原授權
     * @return
     */
    @Headers({"Domain-Name: OnlineAuthorize"})
    @POST("/vPosWebApp/api/DeAutorize/")
    @FormUrlEncoded
    Observable<AuthorizeModel> deAuthorize(@Field("") String data);

}
