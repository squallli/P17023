package tw.com.regalscan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tw.com.regalscan.customClass.ItemUpgradeInfo;
import tw.com.regalscan.R;

public class ItemListUpgradeAdapter extends BaseAdapter {
    private Context context = null;
    List<ItemUpgradeInfo> itemList = new ArrayList<ItemUpgradeInfo>();
    ListView listView=null;

    //listview寬度
    private int mRightWidth = 0;


    boolean isModifiedItem =true;
    public void setIsModifiedItem(boolean b){
        isModifiedItem=b;
    }

    //是否加上滑動刪除
    boolean isSwipeDelete=false;
    public void setIsSwipeDelete(boolean b) { isSwipeDelete=b; }


    public ItemListUpgradeAdapter(Context c) {
        context = c;
        //根据context上下文加载布局，这里的是Demo17Activity本身，即this
    }



    public ItemListUpgradeAdapter(Context c, int rightWidth) {
        context = c;
        mRightWidth = rightWidth;

    }

    public ItemListUpgradeAdapter(Context c, List<ItemUpgradeInfo> list){
        context=c;
        itemList=list;
    }

    //Adapter的列數總數
    @Override
    public int getCount() {
        return itemList.size();
    }

    //某列的內容
    @Override
    public ItemUpgradeInfo getItem(int position) {
        return itemList.get(position);
    }

    //取得某一列的id
    @Override
    public long getItemId(int position) {
        return position;
    }


    public void removeItem(int position) {
        itemList.remove(position);
    }

    public void clear() {
        itemList.clear();
    }



    //修改某列的數量
    public void modifiedItemChange(int position, int qty){
        ItemUpgradeInfo modifiedItem=itemList.get(position);
        modifiedItem.setQty(qty);
        itemList.set(position, modifiedItem);
    }

    public int getTotalMoney(){
        int total=0;

        for (int i = 0; i < itemList.size(); i++) {
            total+=itemList.get(i).getTotal();
        }

        return total;
    }


    public void addItem( String identity, String from, String to, int qty, double total) {
        ItemUpgradeInfo item=new ItemUpgradeInfo();
        item.setIdentity(identity);
        item.setFrom(from);
        item.setTo(to);
        item.setQty(qty);
        item.setTotal(total);

        itemList.add(item);
    }

    //修改某一列View的內容
    @Override
    public View getView(int position, View v, ViewGroup parent) {

        Holder holder = null;
        if (v == null) {

            holder = new Holder();

            if(isSwipeDelete){
                v = LayoutInflater.from(context).inflate(R.layout.item_list_view_five_delete, null);
                holder.item_left = v.findViewById(R.id.item_left);
                holder.item_right = v.findViewById(R.id.item_right);
                holder.item_right_btn = v.findViewById(R.id.item_right_btn);

                LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                holder.item_left.setLayoutParams(lp1);

                LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                        mRightWidth, LinearLayout.LayoutParams.MATCH_PARENT);
                holder.item_right.setLayoutParams(lp2);
            }
            else{
                v = LayoutInflater.from(context).inflate(R.layout.item_list_view_five, null);
            }

            holder.layout_parent= v.findViewById(R.id.layout_parent);
            holder.identity = v.findViewById(R.id.txtIdentity);
            holder.from = v.findViewById(R.id.txtFrom);
            holder.to = v.findViewById(R.id.txtTo);
            holder.qty = v.findViewById(R.id.txtQty);
            holder.total = v.findViewById(R.id.txtTotal);

            v.setTag(holder);
        } else {
            holder = (Holder) v.getTag();
        }

        ItemUpgradeInfo item=itemList.get(position);
        holder.identity.setText(item.getIdentity());
        holder.from.setText(item.getFrom());
        holder.to.setText(item.getTo());
        holder.qty.setText(String.valueOf(item.getQty()));
        holder.total.setText( String.valueOf(item.getTotal()) );
        //整數
        if(item.getIntegerTotal()!=-1){
            holder.total.setText(String.valueOf(item.getIntegerTotal()));
        }else{
            holder.total.setText( String.valueOf(item.getTotal()) );
        }

        //修改數量
        holder.layout_parent.setTag(position);
        if(isModifiedItem){
            holder.layout_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (Integer) v.findViewById(R.id.layout_parent).getTag();
                    itemInfoClickListener.txtItemInfoClickListener(position);

                }
            });
        }

        //滑動刪除
        if(isSwipeDelete){
            holder.item_right_btn.setTag(position);
            holder.item_right_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = (Integer) v.findViewById(R.id.item_right_btn).getTag();
                    itemSwipeDelete.swipeDeleteListener(position);
                }
            });
        }


        return v;
    }

    class Holder {
        public RelativeLayout layout_parent;
        public TextView identity;
        public TextView from;
        public TextView to;
        public TextView qty;
        public TextView total;

        public View item_left;
        public View item_right;                 //滑動刪除的view
        public ImageButton item_right_btn;      //滑動刪除的Btn
    }



    /**
     * itemInfo Click CallBack
     */
    private ItemListPictureModifyAdapter.ItemInfoClickListener itemInfoClickListener;

    public void setItemInfoClickListener(ItemListPictureModifyAdapter.ItemInfoClickListener f) {
        itemInfoClickListener = f;
    }

    public interface ItemInfoClickListener {
        void txtItemInfoClickListener(int position);
    }


    /**
     * item Swipe Delete CallBack
     */
    private ItemSwipeDeleteListener itemSwipeDelete;
    public void setItemSwipeListener(ItemSwipeDeleteListener f) {
        itemSwipeDelete = f;
    }
    public interface ItemSwipeDeleteListener {
        void swipeDeleteListener(int position);
    }

}
