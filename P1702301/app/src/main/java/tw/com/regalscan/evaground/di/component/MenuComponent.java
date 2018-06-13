package tw.com.regalscan.evaground.di.component;

import com.jess.arms.di.scope.ActivityScope;
import dagger.Component;
import com.jess.arms.di.component.AppComponent;

import tw.com.regalscan.evaground.di.module.MenuModule;

import tw.com.regalscan.evaground.mvp.ui.activity.MenuActivity;

@ActivityScope
@Component(modules = MenuModule.class, dependencies = AppComponent.class)
public interface MenuComponent {
    void inject(MenuActivity activity);
}