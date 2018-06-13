package tw.com.regalscan.evaground.di.component;

import com.jess.arms.di.scope.ActivityScope;
import dagger.Component;
import com.jess.arms.di.component.AppComponent;

import tw.com.regalscan.evaground.di.module.PreOrderListModule;

import tw.com.regalscan.evaground.mvp.ui.fragment.PreOrderListFragment;

@ActivityScope
@Component(modules = PreOrderListModule.class, dependencies = AppComponent.class)
public interface PreOrderListComponent {
    void inject(PreOrderListFragment fragment);
}