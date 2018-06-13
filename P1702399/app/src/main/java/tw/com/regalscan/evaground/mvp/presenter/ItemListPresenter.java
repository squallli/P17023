package tw.com.regalscan.evaground.mvp.presenter;

import java.util.List;
import javax.inject.Inject;

import android.app.Application;
import android.widget.ArrayAdapter;

import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.http.imageloader.ImageLoader;
import com.jess.arms.integration.AppManager;
import com.jess.arms.mvp.BasePresenter;
import com.jess.arms.utils.ArmsUtils;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import org.simple.eventbus.EventBus;
import tw.com.regalscan.app.CacheDataTags;
import tw.com.regalscan.app.EventBusTags;
import tw.com.regalscan.app.entity.ItemInfo;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.evaground.mvp.contract.ItemListContract;
import tw.com.regalscan.evaground.mvp.ui.adapter.ItemListAdapter;


@ActivityScope
public class ItemListPresenter extends BasePresenter<ItemListContract.Model, ItemListContract.View> {
    private RxErrorHandler mErrorHandler;
    private Application mApplication;
    private ImageLoader mImageLoader;
    private AppManager mAppManager;
    private List<ItemInfo> mList;
    private ItemListAdapter mAdapter;
    private boolean isFirstTime = true;
    private int total;

    @Inject
    public ItemListPresenter(ItemListContract.Model model, ItemListContract.View rootView
        , RxErrorHandler handler, Application application
        , ImageLoader imageLoader, AppManager appManager, List<ItemInfo> infos, ItemListAdapter adapter) {
        super(model, rootView);
        this.mErrorHandler = handler;
        this.mApplication = application;
        this.mImageLoader = imageLoader;
        this.mAppManager = appManager;
        this.mList = infos;
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
        this.mAdapter = null;
    }

    /**
     * 取得商品清單
     */
    public void getItem() {

        total = 0;

        String secSeq = (String)ArmsUtils.obtainAppComponentFromContext(mAppManager.getCurrentActivity()).extras().get(CacheDataTags.SecSeq);

        DBQuery.ItemDataPack itemDataPack = DBQuery.getModifyProductEGAS(mAppManager.getCurrentActivity(), new StringBuilder(), secSeq, null, null, 0);

        if (mList.size() == 0) {
            for (DBQuery.ItemData itemData : itemDataPack.items) {
                ItemInfo itemInfo = new ItemInfo();
                itemInfo.setDrawNo(itemData.DrawNo);
                itemInfo.setSerialCode(itemData.SerialCode);
                itemInfo.setItemCode(itemData.ItemCode);
                itemInfo.setItemName(itemData.ItemName);
                itemInfo.setDamageQty(itemData.DamageQty);
                itemInfo.setStandQty(itemData.StandQty);
                itemInfo.setEGASCheckQty(itemData.EGASCheckQty);
                itemInfo.setEGASDamageQty(itemData.EGASDamageQty);

                total += itemInfo.getEGASCheckQty();

                mList.add(itemInfo);

                if (mRootView.getWorkType().equals("EGAS")) {
                    if ((itemData.EGASCheckQty != itemData.StandQty) || (itemData.EGASDamageQty != itemData.DamageQty)) {
                        EventBus.getDefault().post(itemInfo, EventBusTags.MODIFIED_ITEM);
                    }
                } else {
                    if ((itemData.EVACheckQty != itemData.StandQty) || (itemData.EVADamageQty != itemData.DamageQty)) {
                        EventBus.getDefault().post(itemInfo, EventBusTags.MODIFIED_ITEM);
                    }
                }
            }

            mAdapter.notifyDataSetChanged();

            mRootView.setTotal(String.valueOf(total));
        }
    }

    /**
     * 取得所有抽屜
     */
    public void getDraws() {
        DBQuery.DrawNoPack drawNoPack = DBQuery.getAllDrawerNo(mAppManager.getCurrentActivity(), new StringBuilder(), null, null, false);
        String[] drawNo = new String[drawNoPack.drawers.length];

        for (int i = 0; i < drawNo.length; i++) {
            drawNo[i] = drawNoPack.drawers[i].DrawNo;
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter<>(mAppManager.getCurrentActivity(), android.R.layout.simple_spinner_dropdown_item, drawNo);
        mRootView.initSpinner(arrayAdapter);
    }

    /**
     * 處理數量調整回傳之後的畫面顯示
     */
    public void processActivityResult(ItemInfo itemInfo, int position, boolean isFiltered) {

        int modifyQty;

        if (mRootView.getWorkType().equals("EGAS")) {
            modifyQty = itemInfo.getEGASCheckQty() - mList.get(mAdapter.getPosition(itemInfo)).getEGASCheckQty();
        } else {
            modifyQty = itemInfo.getEVACheckQty() - mList.get(mAdapter.getPosition(itemInfo)).getEVACheckQty();
        }

        mList.set(mAdapter.getPosition(itemInfo), itemInfo);

        if (isFiltered) {
            search(mRootView.getSpinnerSelectedItem());
        } else {
            mAdapter.notifyItemChanged(position, 0);
            mRootView.setTotal(String.valueOf(total += modifyQty));
        }

        mAdapter.notifyDataSetChanged();

        EventBus.getDefault().post(itemInfo, EventBusTags.MODIFIED_ITEM);
    }

    /**
     * 處理搜尋結果
     *
     * @param searchString
     */
    public void search(String searchString) {
        if (!isFirstTime) {
            mAdapter.getFilter().filter(searchString);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            total = mAdapter.getTotal();
            mRootView.setTotal(String.valueOf(mAdapter.getTotal()));
        } else {
            isFirstTime = false;
        }
    }
}
