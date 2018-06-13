package tw.com.regalscan.evaground.mvp.model;

import javax.inject.Inject;

import android.app.Application;

import com.google.gson.Gson;
import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.integration.IRepositoryManager;
import com.jess.arms.mvp.BaseModel;
import tw.com.regalscan.evaground.mvp.contract.OfflineDownloadContract;


@ActivityScope
public class OfflineDownloadModel extends BaseModel implements OfflineDownloadContract.Model {
    private Gson mGson;
    private Application mApplication;

    @Inject
    public OfflineDownloadModel(IRepositoryManager repositoryManager, Gson gson, Application application) {
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

}