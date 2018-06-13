package tw.com.regalscan.evaground.di.module;

import java.util.ArrayList;
import java.util.List;

import com.jess.arms.di.scope.ActivityScope;
import dagger.Module;
import dagger.Provides;
import tw.com.regalscan.app.entity.ItemInfo;
import tw.com.regalscan.app.entity.PreOrderInfo;
import tw.com.regalscan.evaground.mvp.contract.DiscrepancyContract;
import tw.com.regalscan.evaground.mvp.model.DiscrepancyModel;
import tw.com.regalscan.evaground.mvp.ui.adapter.DiscrepancyAdapter;


@Module
public class DiscrepancyModule {
    private DiscrepancyContract.View view;

    /**
     * 构建DiscrepancyModule时,将View的实现类传进来,这样就可以提供View的实现类给presenter
     *
     * @param view
     */
    public DiscrepancyModule(DiscrepancyContract.View view) {
        this.view = view;
    }

    @ActivityScope
    @Provides
    DiscrepancyContract.View provideDiscrepancyView() {
        return this.view;
    }

    @ActivityScope
    @Provides
    DiscrepancyContract.Model provideDiscrepancyModel(DiscrepancyModel model) {
        return model;
    }

    @ActivityScope
    @Provides
    List<ItemInfo> provideItemInfoList() {
        return new ArrayList<>();
    }

    @ActivityScope
    @Provides
    List<PreOrderInfo> providePreOrderInfoList() {
        return new ArrayList<>();
    }

    @ActivityScope
    @Provides
    DiscrepancyAdapter provideDiscrepancyAdapter(List<ItemInfo> list, List<PreOrderInfo> preOrderInfos) {
        return new DiscrepancyAdapter(list, preOrderInfos);
    }
}