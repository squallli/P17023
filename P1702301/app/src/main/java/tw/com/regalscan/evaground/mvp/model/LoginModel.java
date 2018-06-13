package tw.com.regalscan.evaground.mvp.model;

import java.io.File;
import javax.inject.Inject;

import android.app.Application;
import android.os.Build;
import android.os.Environment;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.integration.IRepositoryManager;
import com.jess.arms.mvp.BaseModel;
import io.reactivex.Observable;
import okio.BufferedSink;
import okio.Okio;
import tw.com.regalscan.BuildConfig;
import tw.com.regalscan.app.entity.UserInfo;
import tw.com.regalscan.evaground.mvp.contract.LoginContract;
import tw.com.regalscan.evaground.mvp.model.api.ApiService;
import tw.com.regalscan.utils.EvaUtils;


@ActivityScope
public class LoginModel extends BaseModel implements LoginContract.Model {
    private Gson mGson;
    private Application mApplication;

    @Inject
    public LoginModel(IRepositoryManager repositoryManager, Gson gson, Application application) {
        super(repositoryManager);
        this.mGson = gson;
        this.mApplication = application;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mGson = null;
        this.mApplication = null;
    }

    @Override
    public Observable<UserInfo> login(String userId, String password, String rfidMark, String company) {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("OS_VERSION", Build.VERSION.RELEASE);
        jsonObject.put("APP_VERSION", BuildConfig.VERSION_NAME);
        jsonObject.put("EMPLOYEE_ID", userId);
        jsonObject.put("PASSWORD", password);
        jsonObject.put("RFID_MARK", rfidMark);
        jsonObject.put("COMPANY", company);

        return mRepositoryManager.obtainRetrofitService(ApiService.class).login(EvaUtils.jsonToBase64(jsonObject));
    }

    @Override
    public Observable<File> downloadApk(String fileUrl) {
        return mRepositoryManager.obtainRetrofitService(ApiService.class).downloadApk(fileUrl)
            .flatMap(responseBodyResponse -> Observable.create(e -> {
                try {
//                    String header = responseBodyResponse.headers().get("Content-Disposition");
//                    String fileName = header.replace("attachment; filename=", "");

                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), "EvaPOS.apk");

                    BufferedSink sink = Okio.buffer(Okio.sink(file));

                    sink.writeAll(responseBodyResponse.body().source());
                    sink.close();
                    e.onNext(file);
                    e.onComplete();
                } catch (Exception ex) {
                    e.onError(ex);
                }
            }));
    }
}