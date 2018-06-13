package tw.com.regalscan.evaground.di.component;

import com.jess.arms.di.scope.ActivityScope;
import dagger.Component;
import com.jess.arms.di.component.AppComponent;

import tw.com.regalscan.evaground.di.module.CheckUpdateModule;

import tw.com.regalscan.evaground.mvp.ui.activity.CheckUpdateActivity;

@ActivityScope
@Component(modules = CheckUpdateModule.class, dependencies = AppComponent.class)
public interface CheckUpdateComponent {
    void inject(CheckUpdateActivity activity);
}