package tw.com.regalscan.evaground.di.component;

import com.jess.arms.di.component.AppComponent;
import com.jess.arms.di.scope.ActivityScope;
import dagger.Component;
import tw.com.regalscan.evaground.di.module.ItemListModule;
import tw.com.regalscan.evaground.mvp.ui.fragment.ItemListFragment;

@ActivityScope
@Component(modules = ItemListModule.class, dependencies = AppComponent.class)
public interface ItemListComponent {
    void inject(ItemListFragment fragment);
}