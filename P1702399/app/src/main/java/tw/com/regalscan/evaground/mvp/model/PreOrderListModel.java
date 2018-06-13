package tw.com.regalscan.evaground.mvp.model;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.graphics.Typeface;

import com.google.gson.Gson;
import com.jess.arms.integration.IRepositoryManager;
import com.jess.arms.mvp.BaseModel;

import com.jess.arms.di.scope.ActivityScope;

import javax.inject.Inject;

import tw.com.regalscan.app.entity.PreOrderInfo;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.evaground.mvp.contract.PreOrderListContract;


@ActivityScope
public class PreOrderListModel extends BaseModel implements PreOrderListContract.Model {
    private Gson mGson;
    private Application mApplication;

    @Inject
    public PreOrderListModel(IRepositoryManager repositoryManager, Gson gson, Application application) {
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
    public List<PreOrderInfo> getPreOrderList() {
        List<PreOrderInfo> list = new ArrayList<>();
        DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(mApplication, new StringBuilder(), null, new String[]{"PR"}, null);
        for (DBQuery.PreorderInformation information : preorderInfoPack.info) {
            PreOrderInfo preOrderInfo = mGson.fromJson(mGson.toJson(information), PreOrderInfo.class);
            if (preOrderInfo.getPreorderType().equals("PR")) {
                list.add(preOrderInfo);
            }
        }
        return list;
    }
}