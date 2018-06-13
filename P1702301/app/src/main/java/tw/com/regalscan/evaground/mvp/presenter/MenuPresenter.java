package tw.com.regalscan.evaground.mvp.presenter;

import android.app.Application;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportActivity;
import android.support.v7.widget.RecyclerView;

import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.http.imageloader.ImageLoader;
import com.jess.arms.integration.AppManager;
import com.jess.arms.mvp.BasePresenter;

import java.util.List;

import javax.inject.Inject;

import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import tw.com.regalscan.app.entity.News;
import tw.com.regalscan.db02.DBFunctions;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.evaground.mvp.contract.MenuContract;


@ActivityScope
public class MenuPresenter extends BasePresenter<MenuContract.Model, MenuContract.View> {
    private RxErrorHandler mErrorHandler;
    private Application mApplication;
    private ImageLoader mImageLoader;
    private AppManager mAppManager;
    private List<News> mNews;
    private RecyclerView.Adapter mAdapter;
    private StringBuilder err = new StringBuilder();

    @Inject
    public MenuPresenter(MenuContract.Model model, MenuContract.View rootView
            , RxErrorHandler handler, Application application
            , ImageLoader imageLoader, AppManager appManager, List<News> news, RecyclerView.Adapter adapter) {
        super(model, rootView);
        this.mErrorHandler = handler;
        this.mApplication = application;
        this.mImageLoader = imageLoader;
        this.mAppManager = appManager;
        this.mNews = news;
        this.mAdapter = adapter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mErrorHandler = null;
        this.mAppManager = null;
        this.mImageLoader = null;
        this.mApplication = null;
    }

    /**
     * 使用 2017 Google IO 发布的 Architecture Components 中的 Lifecycles 的新特性 (此特性已被加入 Support library)
     * 使 {@code Presenter} 可以与 {@link SupportActivity} 和 {@link Fragment} 的部分生命周期绑定
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    void onCreate() {
        getInfo();
        checkNeedUpload();
    }

    /**
     * 把公告欄資訊塞進list
     *
     * @param news 公告欄資訊
     */
    public void addNews(List<News> news) {
        mNews.addAll(news);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 取得當前車的資訊
     */
    private void getInfo() {
        final DBQuery.FlightInfoPack flightInfo = DBQuery.getFlightInfo(mAppManager.getCurrentActivity(), err);
        final DBQuery.BasicSalesInfo saleInfo = DBQuery.checkBasicSalesInfoIsReady(mAppManager.getCurrentActivity(), err);

        if (flightInfo != null && saleInfo != null) {

            mRootView.setFlightDate(flightInfo.flights[0].FlightDate);
            mRootView.setFlightNo(flightInfo.flights[0].FlightNo);
            mRootView.setCartNo(flightInfo.flights[0].CarNo);

            if (String.valueOf(saleInfo.PreorderCount).equals("0")) {
                mRootView.setPreOrder("");
            } else {
                mRootView.setPreOrder("PreOrder：" + String.valueOf(saleInfo.PreorderCount));
            }
            if (String.valueOf(saleInfo.VIPCount).equals("0")) {
                mRootView.setVipOrder("");
            } else {
                mRootView.setVipOrder("VIP：" + String.valueOf(saleInfo.VIPCount));
            }

            for (int i = 0; i < flightInfo.flights.length; i++) {
                //航段編號
                mRootView.setFlightInfo(
                        flightInfo.flights[i].DepStn + " - " + flightInfo.flights[i].ArivStn + " CA：" + flightInfo.flights[i].CrewID
                );

                if (i != flightInfo.flights.length - 1) {
                    mRootView.setFlightInfo("\n");
                }
            }
        }
    }

    /**
     * 檢查是否需要上傳
     */
    private void checkNeedUpload() {
        DBQuery.CurrentOpenFlightPack openFlightPack = DBQuery.getCurrentOpenFlightList(mAppManager.getCurrentActivity(), err);

        DBFunctions dbFunctions = new DBFunctions(mAppManager.getCurrentActivity(), "");

        if (openFlightPack != null && openFlightPack.openFlights != null) {
            for (int i = 0; i < openFlightPack.openFlights.length; i++) {
                if (openFlightPack.openFlights[i].Status.equals("Closed") && dbFunctions.GetUploadStatus()[i].equals("N")) {
                    mRootView.setNeedUpload(true);
                    break;
                } else if (openFlightPack.openFlights[i].Status.equals("") && dbFunctions.GetUploadStatus()[i].equals("Y")) {
                    mRootView.setNeedUpload(false);
                } else {
                    mRootView.setNeedUpload(false);
                }
            }
        }
    }

//    public void printDiscrepancy() {
//        mPrintGround.printDiscrep()
//            .subscribeOn(Schedulers.io())
//            .doOnSubscribe(disposable -> mRootView.showLoading())
//            .observeOn(AndroidSchedulers.mainThread())
//            .doFinally(() -> mRootView.hideLoading())
//            .compose(RxLifecycleUtils.bindToLifecycle(mRootView))
//            .subscribe(new ErrorHandleSubscriber<String>(mErrorHandler) {
//                @Override
//                public void onNext(String s) {
//                    mRootView.showMessage(s);
//                }
//            });
//    }

//    public void printSCRIn() {
//        mPrintGround.printSCRIN()
//            .subscribeOn(Schedulers.io())
//            .doOnSubscribe(disposable -> mRootView.showLoading())
//            .observeOn(AndroidSchedulers.mainThread())
//            .doFinally(() -> mRootView.hideLoading())
//            .compose(RxLifecycleUtils.bindToLifecycle(mRootView))
//            .subscribe(new ErrorHandleSubscriber<String>(mErrorHandler) {
//                @Override
//                public void onNext(String s) {
//                    mRootView.showMessage(s);
//                }
//            });
//    }
}
