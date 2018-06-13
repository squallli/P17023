package tw.com.regalscan.evaground.di.module;

import com.jess.arms.di.scope.ActivityScope;

import dagger.Module;
import dagger.Provides;

import tw.com.regalscan.evaground.mvp.contract.CheckUpdateContract;
import tw.com.regalscan.evaground.mvp.model.CheckUpdateModel;


@Module
public class CheckUpdateModule {
    private CheckUpdateContract.View view;

    /**
     * 构建CheckUpdateActivityModule时,将View的实现类传进来,这样就可以提供View的实现类给presenter
     *
     * @param view
     */
    public CheckUpdateModule(CheckUpdateContract.View view) {
        this.view = view;
    }

    @ActivityScope
    @Provides
    CheckUpdateContract.View provideCheckUpdateActivityView() {
        return this.view;
    }

    @ActivityScope
    @Provides
    CheckUpdateContract.Model provideCheckUpdateActivityModel(CheckUpdateModel model) {
        return model;
    }
}