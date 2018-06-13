package tw.com.regalscan.evaground.di.component;

import com.jess.arms.di.scope.ActivityScope;
import dagger.Component;
import com.jess.arms.di.component.AppComponent;

import tw.com.regalscan.evaground.di.module.VipOrderListModule;

import tw.com.regalscan.evaground.mvp.ui.fragment.VipOrderListFragment;

@ActivityScope
@Component(modules = VipOrderListModule.class, dependencies = AppComponent.class)
public interface VipOrderListComponent {
    void inject(VipOrderListFragment fragment);
}