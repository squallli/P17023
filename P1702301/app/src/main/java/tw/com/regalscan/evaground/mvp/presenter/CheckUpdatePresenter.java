package tw.com.regalscan.evaground.mvp.presenter;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;

import com.jess.arms.integration.AppManager;
import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.mvp.BasePresenter;
import com.jess.arms.http.imageloader.ImageLoader;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.DeviceUtils;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;

import javax.inject.Inject;

import org.simple.eventbus.EventBus;
import tw.com.regalscan.evaground.mvp.contract.CheckUpdateContract;
import tw.com.regalscan.evaground.mvp.ui.fragment.ItemListFragment;


@ActivityScope
public class CheckUpdatePresenter extends BasePresenter<CheckUpdateContract.Model, CheckUpdateContract.View> {
    private RxErrorHandler mErrorHandler;
    private Application mApplication;
    private ImageLoader mImageLoader;
    private AppManager mAppManager;

    @Inject
    public CheckUpdatePresenter(CheckUpdateContract.Model model, CheckUpdateContract.View rootView
        , RxErrorHandler handler, Application application
        , ImageLoader imageLoader, AppManager appManager) {
        super(model, rootView);
        this.mErrorHandler = handler;
        this.mApplication = application;
        this.mImageLoader = imageLoader;
        this.mAppManager = appManager;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mErrorHandler = null;
        this.mAppManager = null;
        this.mImageLoader = null;
        this.mApplication = null;
    }

    public void initSearchView(SearchView searchView) {
        searchView.bringToFront();
        searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setIconified(false);
        searchView.setQuery("", false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                EventBus.getDefault().post(s, "search");
                return true;
            }
        });
    }

    /**
     * 將更改數據存放回資料庫
     */
    public void saveData() {

    }
}
