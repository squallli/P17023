package tw.com.regalscan.evaground.di.module;

import java.util.ArrayList;
import java.util.List;

import com.jess.arms.di.scope.ActivityScope;
import dagger.Module;
import dagger.Provides;
import tw.com.regalscan.app.entity.ItemInfo;
import tw.com.regalscan.evaground.mvp.contract.PreOrderListContract;
import tw.com.regalscan.evaground.mvp.model.PreOrderListModel;
import tw.com.regalscan.evaground.mvp.ui.adapter.PreOrderListAdapter;


@Module
public class PreOrderListModule {
    private PreOrderListContract.View view;

    /**
     * 构建PreOrderListModule时,将View的实现类传进来,这样就可以提供View的实现类给presenter
     *
     * @param view
     */
    public PreOrderListModule(PreOrderListContract.View view) {
        this.view = view;
    }

    @ActivityScope
    @Provides
    PreOrderListContract.View providePreOrderListView() {
        return this.view;
    }

    @ActivityScope
    @Provides
    PreOrderListContract.Model providePreOrderListModel(PreOrderListModel model) {
        return model;
    }

    @ActivityScope
    @Provides
    List<ItemInfo> provideItemInfoList() {
        return new ArrayList<>();
    }

    @ActivityScope
    @Provides
    PreOrderListAdapter providePreOrderListAdapter(List<ItemInfo> list) {
        return new PreOrderListAdapter(list);
    }

}