package tw.com.regalscan.evaground.di.module;

import com.jess.arms.di.scope.ActivityScope;
import dagger.Module;
import dagger.Provides;
import tw.com.regalscan.evaground.mvp.contract.OfflineDownloadContract;
import tw.com.regalscan.evaground.mvp.model.OfflineDownloadModel;


@Module
public class OfflineDownloadModule {
    private OfflineDownloadContract.View view;

    /**
     * 构建OfflineDownloadModule时,将View的实现类传进来,这样就可以提供View的实现类给presenter
     *
     * @param view
     */
    public OfflineDownloadModule(OfflineDownloadContract.View view) {
        this.view = view;
    }

    @ActivityScope
    @Provides
    OfflineDownloadContract.View provideOfflineDownloadView() {
        return this.view;
    }

    @ActivityScope
    @Provides
    OfflineDownloadContract.Model provideOfflineDownloadModel(OfflineDownloadModel model) {
        return model;
    }
}