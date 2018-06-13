package tw.com.regalscan.evaground.di.module;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jess.arms.di.scope.ActivityScope;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;
import tw.com.regalscan.app.entity.News;
import tw.com.regalscan.evaground.mvp.contract.MenuContract;
import tw.com.regalscan.evaground.mvp.model.MenuModel;
import tw.com.regalscan.evaground.mvp.ui.adapter.BulletinAdapter;


@Module
public class MenuModule {
    private MenuContract.View view;

    /**
     * 构建MenuModule时,将View的实现类传进来,这样就可以提供View的实现类给presenter
     */
    public MenuModule(MenuContract.View view) {
        this.view = view;
    }

    @ActivityScope
    @Provides
    MenuContract.View provideMenuView() {
        return this.view;
    }

    @ActivityScope
    @Provides
    MenuContract.Model provideMenuModel(MenuModel model) {
        return model;
    }

    @ActivityScope
    @Provides
    RecyclerView.LayoutManager provideLayoutManager() {
        return new LinearLayoutManager(view.getActivity());
    }

    @ActivityScope
    @Provides
    List<News> provideNewsList() {
        return new ArrayList<>();
    }

    @ActivityScope
    @Provides
    RecyclerView.Adapter provideBulletinAdapter(List<News> list) {
        return new BulletinAdapter(list);
    }
}