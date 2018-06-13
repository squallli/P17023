package tw.com.regalscan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tw.com.regalscan.*;

public class DiscountAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> discountList=new ArrayList<String>();

    public DiscountAdapter(Context c) {
        context = c;
    }

    public DiscountAdapter(Context c, ArrayList<String> list) {
        context = c;
        discountList=list;
    }


    @Override
    public int getCount() {
        return discountList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }





    public ArrayList<String> getCurrentDiscountList(){
        return discountList;
    }

    public void insertItemToFirst(String discount){
        discountList.add(0, discount);
    }

    public void addItem(String discount){
        discountList.add(discount);
    }


    public void clear() {
        discountList.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        Holder holder;
        if(v == null){
            v = LayoutInflater.from(context).inflate(R.layout.discount_listview, null);
            holder = new Holder();
            holder.text = v.findViewById(R.id.disCount);

            v.setTag(holder);
        } else{
            holder = (Holder) v.getTag();
        }

        holder.text.setText(discountList.get(position));

        return v;
    }
    class Holder{
        TextView text;
    }
}