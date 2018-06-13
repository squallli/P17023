package tw.com.regalscan.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import tw.com.regalscan.customClass.ItemPayInfo;
import tw.com.regalscan.R;
import tw.com.regalscan.utils.Tools;

public class ItemListPayAdapter extends BaseAdapter {
    private Context context = null;
    List<ItemPayInfo> itemList = new ArrayList<ItemPayInfo>();

    //listview寬度
    private int mRightWidth = 0;


    //是否加上滑動刪除
    boolean isSwipeDelete=false;
    public void setIsSwipeDelete(boolean b) { isSwipeDelete=b; }

    //是否點下去更改背景顏色
    boolean isOnClickChangeBack=false;
    public void setIsOnClickChangeBack(boolean b){
        isOnClickChangeBack=b;
    }


    // 是否為Refund (Cash, SC, DC不顯示刪除icon)
    boolean isRefundPage= false;
    public void setIsRefundPage(boolean b){
        isRefundPage= b;
    }




    public ItemListPayAdapter(Context c) {
        context = c;
        //根据context上下文加载布局，这里的是Demo17Activity本身，即this
    }



    public ItemListPayAdapter(Context c, int rightWidth) {
        context = c;
        mRightWidth = rightWidth;

    }

    public ItemListPayAdapter(Context c,  List<ItemPayInfo> list){
        context=c;
        itemList=list;
    }


    //判斷是否全部都信用卡或現金
    public boolean isAllCash(){

        if(itemList.size()==0)
            return false;

        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getPayType().equals("Credit")) {
                return false;
            }
        }
        return true;
    }

    public boolean isAllCreditCard(){

        if(itemList.size()==0)
            return false;

        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getPayType().equals("Cash")) {
                return false;
            }
        }
        return true;
    }


    //Adapter的列數總數
    @Override
    public int getCount() {
        return itemList.size();
    }

    //某列的內容
    @Override
    public ItemPayInfo getItem(int position) {
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

    public void addItem( String CurDvr, String PayType, double Amount, double USDAmount, String CouponNo) {
        itemList.add(new ItemPayInfo(CurDvr, PayType, Amount, USDAmount, CouponNo));
    }

    //修改某一列View的內容
    @Override
    public View getView(int position, View v, ViewGroup parent) {

        Holder holder = null;
//        if (v == null) {

            holder = new Holder();

            if(isSwipeDelete){
                v = LayoutInflater.from(context).inflate(R.layout.item_list_view_three_delete, null);
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
            else if(isOnClickChangeBack){
                v = LayoutInflater.from(context).inflate(R.layout.item_list_view_three_no_back, null);
                v.setBackground( v.getResources().getDrawable(R.drawable.shape_item_background) );
            }
            else{
                v = LayoutInflater.from(context).inflate(R.layout.item_list_view_three, null);
            }

            holder.payType = v.findViewById(R.id.txtTo);
            holder.moneyType = v.findViewById(R.id.txtQty);
            holder.money = v.findViewById(R.id.txtTotal);

            v.setTag(holder);
//        } else {
//            holder = (Holder) v.getTag();
//        }


        try{
            ItemPayInfo item=itemList.get(position);
            holder.payType.setText(item.getPayType());
            holder.moneyType.setText(item.getCurDvr());
            holder.money.setText(String.valueOf(Tools.getModiMoneyString(item.getAmount())));

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
                if(isRefundPage &&
                    ( item.getPayType().equals("Card") || item.getPayType().equals("SC") || item.getPayType().equals("DC") )){
                    holder.item_right_btn.setBackground(null);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

        return v;
    }

    class Holder {
        public TextView payType;
        public TextView moneyType;
        public TextView money;

        public View item_left;
        public View item_right;                 //滑動刪除的view
        public ImageButton item_right_btn;      //滑動刪除的Btn
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
