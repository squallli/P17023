package tw.com.regalscan.evaground.mvp.ui.adapter;

import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import tw.com.regalscan.R;
import tw.com.regalscan.app.entity.ItemInfo;
import tw.com.regalscan.app.entity.PreOrderInfo;
import tw.com.regalscan.evaground.mvp.ui.holder.ItemInfoItemHolder;
import tw.com.regalscan.evaground.mvp.ui.holder.PreOrderInfoHolder;

/**
 * Created by tp00175 on 2018/2/8.
 */

public class DiscrepancyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ItemInfo> itemInfos;
    private List<PreOrderInfo> preOrderInfos;

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_PREORDER = 1;

    private String workType;

    public DiscrepancyAdapter(List<ItemInfo> itemInfos, List<PreOrderInfo> preOrderInfos) {
        this.itemInfos = itemInfos;
        this.preOrderInfos = preOrderInfos;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < itemInfos.size()) {
            return TYPE_ITEM;
        } else {
            return TYPE_PREORDER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        View view;

        switch (viewType) {
            case TYPE_ITEM:
                view = View.inflate(parent.getContext(), R.layout.item_list_view, null);
                viewHolder = new ItemInfoItemHolder(view, workType);
                break;
            case TYPE_PREORDER:
                view = View.inflate(parent.getContext(), R.layout.item_list_view_preorder_receipt, null);
                viewHolder = new PreOrderInfoHolder(view, workType);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemInfoItemHolder) {
            ((ItemInfoItemHolder)holder).setData(itemInfos.get(position), position);
        }

        if (holder instanceof PreOrderInfoHolder) {
            ((PreOrderInfoHolder)holder).setData(preOrderInfos.get(position - itemInfos.size()), position);
        }
    }

    @Override
    public int getItemCount() {
        return itemInfos.size() + preOrderInfos.size();
    }

    /**
     * 設定身分
     *
     * @param workType
     */
    public void setWorkType(String workType) {
        this.workType = workType;
    }
}
