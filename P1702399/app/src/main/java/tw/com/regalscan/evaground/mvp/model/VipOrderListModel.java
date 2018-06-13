package tw.com.regalscan.evaground.mvp.model;

import android.app.Application;

import com.google.gson.Gson;
import com.jess.arms.integration.IRepositoryManager;
import com.jess.arms.mvp.BaseModel;

import com.jess.arms.di.scope.ActivityScope;

import javax.inject.Inject;

import tw.com.regalscan.evaground.mvp.contract.VipOrderListContract;


@ActivityScope
public class VipOrderListModel extends BaseModel implements VipOrderListContract.Model {
    private Gson mGson;
    private Application mApplication;

    @Inject
    public VipOrderListModel(IRepositoryManager repositoryManager, Gson gson, Application application) {
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