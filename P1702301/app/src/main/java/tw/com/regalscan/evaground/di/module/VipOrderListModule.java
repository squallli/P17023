package tw.com.regalscan.evaground.di.module;

import com.jess.arms.di.scope.ActivityScope;

import dagger.Module;
import dagger.Provides;

import tw.com.regalscan.evaground.mvp.contract.VipOrderListContract;
import tw.com.regalscan.evaground.mvp.model.VipOrderListModel;


@Module
public class VipOrderListModule {
    private VipOrderListContract.View view;

    /**
     * 构建VipOrderListModule时,将View的实现类传进来,这样就可以提供View的实现类给presenter
     *
     * @param view
     */
    public VipOrderListModule(VipOrderListContract.View view) {
        this.view = view;
    }

    @ActivityScope
    @Provides
    VipOrderListContract.View provideVipOrderListView() {
        return this.view;
    }

    @ActivityScope
    @Provides
    VipOrderListContract.Model provideVipOrderListModel(VipOrderListModel model) {
        return model;
    }
}