package tw.com.regalscan.evaground.mvp.ui.adapter;

import java.util.List;

import android.view.View;

import com.jess.arms.base.BaseHolder;
import com.jess.arms.base.DefaultAdapter;
import tw.com.regalscan.R;
import tw.com.regalscan.app.entity.ItemInfo;
import tw.com.regalscan.evaground.mvp.ui.holder.PreorderInfoItemHolder;

/**
 * Created by tp00175 on 2018/1/12.
 */

public class PreOrderListAdapter extends DefaultAdapter<ItemInfo> {

    private PreorderInfoItemHolder mItemHolder;
    private String workType;

    public PreOrderListAdapter(List<ItemInfo> infos) {
        super(infos);
    }

    @Override
    public BaseHolder<ItemInfo> getHolder(View v, int viewType) {
        mItemHolder = new PreorderInfoItemHolder(v, workType);
        return mItemHolder;
    }

    @Override
    public int getLayoutId(int viewType) {
        return R.layout.item_list_view_preorder;
    }

    /**
     * 設定身分別
     *
     * @param workType
     */
    public void setWorkType(String workType) {
        this.workType = workType;
    }
}
