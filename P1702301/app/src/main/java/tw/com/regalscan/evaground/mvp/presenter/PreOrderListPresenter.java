package tw.com.regalscan.evaground.mvp.presenter;

import java.util.List;
import javax.inject.Inject;

import android.app.Application;
import android.widget.ArrayAdapter;

import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.http.imageloader.ImageLoader;
import com.jess.arms.integration.AppManager;
import com.jess.arms.mvp.BasePresenter;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import org.simple.eventbus.EventBus;
import tw.com.regalscan.app.EventBusTags;
import tw.com.regalscan.app.entity.ItemInfo;
import tw.com.regalscan.app.entity.PreOrderInfo;
import tw.com.regalscan.evaground.mvp.contract.PreOrderListContract;
import tw.com.regalscan.evaground.mvp.ui.adapter.PreOrderListAdapter;


@ActivityScope
public class PreOrderListPresenter extends BasePresenter<PreOrderListContract.Model, PreOrderListContract.View> {
    private RxErrorHandler mErrorHandler;
    private Application mApplication;
    private ImageLoader mImageLoader;
    private AppManager mAppManager;
    private PreOrderListAdapter mAdapter;
    private List<PreOrderInfo> mPreOrderInfoList;
    private PreOrderInfo mPreOrderInfo = new PreOrderInfo();
    private List<ItemInfo> mList;
    private boolean isFirstTime;
    private boolean isSale;
    private boolean isChanged;

    @Inject
    public PreOrderListPresenter(PreOrderListContract.Model model, PreOrderListContract.View rootView
        , RxErrorHandler handler, Application application
        , ImageLoader imageLoader, AppManager appManager, List<ItemInfo> list, PreOrderListAdapter preOrderListAdapter) {
        super(model, rootView);
        this.mErrorHandler = handler;
        this.mApplication = application;
        this.mImageLoader = imageLoader;
        this.mAppManager = appManager;
        this.mAdapter = preOrderListAdapter;
        this.mList = list;
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
     * 取得所有 Pre Order 單號
     */
    public void getPreOrderList() {
        mPreOrderInfoList = mModel.getPreOrderList();
        ArrayAdapter arrayAdapter;
        if (mPreOrderInfoList.size() == 0) {
            arrayAdapter = new ArrayAdapter(mAppManager.getCurrentActivity(), android.R.layout.simple_spinner_dropdown_item, new String[]{"No PreOrder"});
            mRootView.initSpinner(arrayAdapter);
        } else {
            String[] strings = new String[mPreOrderInfoList.size()];
            for (int i = 0; i < mPreOrderInfoList.size(); i++) {
                strings[i] = mPreOrderInfoList.get(i).getPreorderNO();
            }
            arrayAdapter = new ArrayAdapter(mAppManager.getCurrentActivity(), android.R.layout.simple_spinner_dropdown_item, strings);
            mRootView.initSpinner(arrayAdapter);
        }

        for (PreOrderInfo preOrderInfo : mPreOrderInfoList) {
            if (mRootView.getWorkType().equals("EGAS")) {
                if (!preOrderInfo.getSaleFlag().equals(preOrderInfo.getEGASSaleFlag())) {
                    EventBus.getDefault().post(preOrderInfo, EventBusTags.MODIFIED_PRE_ORDER);
                }
            } else {
                if (!preOrderInfo.getSaleFlag().equals(preOrderInfo.getEVASaleFlag())) {
                    EventBus.getDefault().post(preOrderInfo, EventBusTags.MODIFIED_PRE_ORDER);
                }
            }
        }
    }

    /**
     * 產生Pre Order list
     *
     * @param position
     */
    public void generateList(int position) {
        if (mList.size() > 0) {
            mList.clear();
        }

        isFirstTime = true;

        mPreOrderInfo = mPreOrderInfoList.get(position);

        mList.addAll(mPreOrderInfo.getItemInfos());

        mRootView.setTotal(String.valueOf(mPreOrderInfo.getItemInfos().size()));
        mAdapter.notifyDataSetChanged();

        if (mRootView.getWorkType().equals("EVA")) {
            isSale = mPreOrderInfo.getEVASaleFlag().equals("S");

            isChanged = !mPreOrderInfo.getSaleFlag().equals(mPreOrderInfo.getEVASaleFlag());

            mRootView.setSelectButton(mPreOrderInfo.getEVASaleFlag().equals("S"));
        } else {
            isSale = mPreOrderInfo.getEGASSaleFlag().equals("S");

            isChanged = !mPreOrderInfo.getSaleFlag().equals(mPreOrderInfo.getEGASSaleFlag());

            mRootView.setSelectButton(mPreOrderInfo.getEGASSaleFlag().equals("S"));
        }
    }


    /**
     * 刷新訂單狀態及畫面
     *
     * @param isSale
     */
    public void changeStatus(boolean isSale) {

        if (isFirstTime) {
            isFirstTime = false;
            for (int i = 0; i < mList.size(); i++) {
                mList.get(i).setChanged(isChanged);
                mList.get(i).setSale(this.isSale);
            }
            mAdapter.notifyDataSetChanged();
        } else {
            if (isSale) {
                for (int i = 0; i < mList.size(); i++) {
                    mList.get(i).setChanged(!mPreOrderInfo.getSaleFlag().equals("S"));
                    mList.get(i).setSale(true);
                }
                mAdapter.notifyDataSetChanged();

                if (mRootView.getWorkType().equals("EVA")) {
                    mPreOrderInfo.setEVASaleFlag("S");
                } else {
                    mPreOrderInfo.setEGASSaleFlag("S");
                }

                EventBus.getDefault().post(mPreOrderInfo, EventBusTags.MODIFIED_PRE_ORDER);
            } else {
                for (int i = 0; i < mList.size(); i++) {
                    mList.get(i).setChanged(mPreOrderInfo.getSaleFlag().equals("S"));
                    mList.get(i).setSale(false);
                }
                mAdapter.notifyDataSetChanged();

                if (mRootView.getWorkType().equals("EVA")) {
                    mPreOrderInfo.setEVASaleFlag("N");
                } else {
                    mPreOrderInfo.setEGASSaleFlag("N");
                }

                EventBus.getDefault().post(mPreOrderInfo, EventBusTags.MODIFIED_PRE_ORDER);
            }
        }
    }
}
