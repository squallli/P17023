package tw.com.regalscan.evaground.di.component;

import com.jess.arms.di.component.AppComponent;
import com.jess.arms.di.scope.ActivityScope;
import dagger.Component;
import tw.com.regalscan.evaground.di.module.OfflineDownloadModule;
import tw.com.regalscan.evaground.mvp.ui.activity.OfflineDownloadActivity;

@ActivityScope
@Component(modules = OfflineDownloadModule.class, dependencies = AppComponent.class)
public interface OfflineDownloadComponent {
    void inject(OfflineDownloadActivity activity);
}