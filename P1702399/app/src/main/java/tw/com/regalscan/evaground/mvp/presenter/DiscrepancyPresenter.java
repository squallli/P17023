package tw.com.regalscan.evaground.mvp.presenter;

import java.util.List;
import javax.inject.Inject;

import android.app.Application;

import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.http.imageloader.ImageLoader;
import com.jess.arms.integration.AppManager;
import com.jess.arms.mvp.BasePresenter;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import tw.com.regalscan.app.entity.ItemInfo;
import tw.com.regalscan.app.entity.PreOrderInfo;
import tw.com.regalscan.evaground.mvp.contract.DiscrepancyContract;
import tw.com.regalscan.evaground.mvp.ui.adapter.DiscrepancyAdapter;


@ActivityScope
public class DiscrepancyPresenter extends BasePresenter<DiscrepancyContract.Model, DiscrepancyContract.View> {
    private RxErrorHandler mErrorHandler;
    private Application mApplication;
    private ImageLoader mImageLoader;
    private AppManager mAppManager;
    private List<ItemInfo> mList;
    private List<PreOrderInfo> mPreOrderInfos;
    private DiscrepancyAdapter mAdapter;


    @Inject
    public DiscrepancyPresenter(DiscrepancyContract.Model model, DiscrepancyContract.View rootView
        , RxErrorHandler handler, Application application
        , ImageLoader imageLoader, AppManager appManager, List<ItemInfo> infos, List<PreOrderInfo> preOrderInfos, DiscrepancyAdapter adapter) {
        super(model, rootView);
        this.mErrorHandler = handler;
        this.mApplication = application;
        this.mImageLoader = imageLoader;
        this.mAppManager = appManager;
        this.mList = infos;
        this.mPreOrderInfos = preOrderInfos;
        this.mAdapter = adapter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mErrorHandler = null;
        this.mAppManager = null;
        this.mImageLoader = null;
        this.mApplication = null;
        this.mList = null;
        this.mPreOrderInfos = null;
        this.mAdapter = null;
    }

    /**
     * 將修改過的商品加入列表
     */
    public void addModifiedItem(ItemInfo itemInfo) {
        if (mList.size() == 0) {
            mList.add(itemInfo);
        } else {
            for (int i = 0; i < mList.size(); i++) {
                if (mList.get(i).getItemCode().equals(itemInfo.getItemCode())) {
                    if (mRootView.getWorkType().equals("EGAS")) {
                        if (mList.get(i).getStandQty() == itemInfo.getEGASCheckQty() && mList.get(i).getDamageQty() == itemInfo.getEGASDamageQty()) {
                            mList.remove(i);
                        } else {
                            mList.set(i, itemInfo);
                        }
                    } else {
                        if (mList.get(i).getStandQty() == itemInfo.getEVACheckQty() && mList.get(i).getDamageQty() == itemInfo.getEVADamageQty()) {
                            mList.remove(i);
                        } else {
                            mList.set(i, itemInfo);
                        }
                    }
                } else {
                    if (i == mList.size() - 1) {
                        mList.add(itemInfo);
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 將修改過的Pre Order 加入列表
     *
     * @param preOrderInfo
     */
    public void addModifiedPreOrder(PreOrderInfo preOrderInfo) {
        if (mPreOrderInfos.size() == 0) {
            mPreOrderInfos.add(preOrderInfo);
        } else {
            for (int i = 0; i < mPreOrderInfos.size(); i++) {
                if (mPreOrderInfos.get(i).getPreorderNO().equals(preOrderInfo.getPreorderNO())) {
                    if (mRootView.getWorkType().equals("EGAS")) {
                        if (mPreOrderInfos.get(i).getSaleFlag().equals(preOrderInfo.getEGASSaleFlag())) {
                            mPreOrderInfos.remove(i);
                        } else {
                            mPreOrderInfos.set(i, preOrderInfo);
                        }
                    } else {
                        if (mPreOrderInfos.get(i).getSaleFlag().equals(preOrderInfo.getEVASaleFlag())) {
                            mPreOrderInfos.remove(i);
                        } else {
                            mPreOrderInfos.set(i, preOrderInfo);
                        }
                    }
                } else {
                    if (i == mPreOrderInfos.size() - 1) {
                        mPreOrderInfos.add(preOrderInfo);
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    public void processActivityResult(ItemInfo itemInfo, PreOrderInfo preOrderInfo, int position, int resultCode) {
        switch (resultCode) {
            case 500:

                break;
            case 400:

                break;
        }
    }
}
