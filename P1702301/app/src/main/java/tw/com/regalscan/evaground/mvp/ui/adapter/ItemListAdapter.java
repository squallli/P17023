package tw.com.regalscan.evaground.mvp.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;

import com.jess.arms.base.BaseHolder;
import com.jess.arms.base.DefaultAdapter;
import tw.com.regalscan.R;
import tw.com.regalscan.app.entity.ItemInfo;
import tw.com.regalscan.app.entity.PreOrderInfo;
import tw.com.regalscan.evaground.mvp.ui.holder.ItemInfoItemHolder;

/**
 * Created by tp00175 on 2017/12/15.
 */

public class ItemListAdapter extends DefaultAdapter<ItemInfo> implements Filterable {

    private ItemInfoItemHolder mItemHolder;
    private List<ItemInfo> mFilteredList;
    private int total;
    private String workType;

    public ItemListAdapter(List<ItemInfo> infos) {
        super(infos);
        mFilteredList = infos;
    }

    @Override
    public BaseHolder<ItemInfo> getHolder(View v, int viewType) {
        v.setTag(mInfos);
        mItemHolder = new ItemInfoItemHolder(v, workType);
        return mItemHolder;
    }

    @Override
    public int getLayoutId(int viewType) {
        return R.layout.item_list_view;
    }

    @Override
    public void onBindViewHolder(BaseHolder<ItemInfo> holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (payloads.isEmpty()) {
            mItemHolder.setIsRecyclable(false);
            mItemHolder.setData(mFilteredList.get(position), position);
        } else {
            mItemHolder.updateView(mFilteredList.get(position));
        }
    }

    @Override
    public ItemInfo getItem(int position) {
        return mFilteredList.get(position);
    }

    @Override
    public int getItemCount() {
        return mFilteredList == null ? 0 : mFilteredList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();
                List<ItemInfo> filteredList = new ArrayList<>();
                total = 0;

                if (charString.isEmpty() || charString.equals("all drawer")) {
                    filteredList = mInfos;
                    for (ItemInfo itemInfo : mInfos) {
                        total += itemInfo.getEGASCheckQty();
                    }
                } else {
                    for (ItemInfo itemInfo : mInfos) {
                        if (itemInfo.getDrawNo().toLowerCase().contains(charString)
                            || itemInfo.getItemCode().toLowerCase().contains(charString)) {
                            filteredList.add(itemInfo);
                            total += itemInfo.getEGASCheckQty();
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                filterResults.count = total;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList = (List<ItemInfo>)filterResults.values;
                total = filterResults.count;

                notifyDataSetChanged();
            }
        };
    }

    /**
     * 取得篩選後總量
     *
     * @return
     */
    public int getTotal() {
        return total;
    }

    /**
     * 取得物件所在列表位置
     *
     * @param data
     * @return
     */
    public int getPosition(ItemInfo data) {

        int position = -1;

        for (int i = 0; i < mInfos.size(); i++) {
            if (mInfos.get(i).getItemCode().equals(data.getItemCode())) {
                position = i;
            }
        }

        return position;
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
