package tw.com.regalscan.evaground;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import tw.com.regalscan.R;

/**
 * Created by tp00169 on 2017/3/10.
 */

public class ItemTotalAdapter extends BaseAdapter {
    private Context context = null;
    private ArrayList<String> totalList = new ArrayList<String>();
    private String string = "";

    public ItemTotalAdapter(Context c) {
        context = c;
        totalList.add(string);
        //根据context上下文加载布局，这里的是Demo17Activity本身，即this
    }

    @Override
    public int getCount() {
        return totalList.size();
    }

    @Override
    public Object getItem(int position) {
        return totalList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setTotal(int number){
        if(totalList.size() > 0)
            totalList.set(0, Integer.toString(number));
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        Holder holder = null;
        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_total, null);
            holder = new Holder();
            holder.txtTotal = v.findViewById(R.id.txtTotal);

            v.setTag(holder);
        } else {
            holder = (Holder) v.getTag();
        }

        String total = totalList.get(position);

        holder.txtTotal.setText(total);

        return v;
    }

    class Holder {
        public TextView txtTotal;
    }
}
