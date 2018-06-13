package tw.com.regalscan.evaground.di.module;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jess.arms.di.scope.ActivityScope;
import dagger.Module;
import dagger.Provides;
import tw.com.regalscan.app.entity.ItemInfo;
import tw.com.regalscan.evaground.mvp.contract.ItemListContract;
import tw.com.regalscan.evaground.mvp.model.ItemListModel;
import tw.com.regalscan.evaground.mvp.ui.adapter.ItemListAdapter;


@Module
public class ItemListModule {
    private ItemListContract.View view;

    /**
     * 构建ItemListModule时,将View的实现类传进来,这样就可以提供View的实现类给presenter
     *
     * @param view
     */
    public ItemListModule(ItemListContract.View view) {
        this.view = view;
    }

    @ActivityScope
    @Provides
    ItemListContract.View provideItemListView() {
        return this.view;
    }

    @ActivityScope
    @Provides
    ItemListContract.Model provideItemListModel(ItemListModel model) {
        return model;
    }

    @ActivityScope
    @Provides
    List<ItemInfo> provideItemInfoList() {
        return new ArrayList<>();
    }

    @ActivityScope
    @Provides
    ItemListAdapter provideItemListAdapter(List<ItemInfo> list) {
        return new ItemListAdapter(list);
    }
}