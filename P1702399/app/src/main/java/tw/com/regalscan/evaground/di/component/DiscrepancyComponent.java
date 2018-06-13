package tw.com.regalscan.evaground.di.component;

import com.jess.arms.di.scope.ActivityScope;
import dagger.Component;
import com.jess.arms.di.component.AppComponent;

import tw.com.regalscan.evaground.di.module.DiscrepancyModule;

import tw.com.regalscan.evaground.mvp.ui.fragment.DiscrepancyFragment;

@ActivityScope
@Component(modules = DiscrepancyModule.class, dependencies = AppComponent.class)
public interface DiscrepancyComponent {
    void inject(DiscrepancyFragment fragment);
}