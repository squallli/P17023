package tw.com.regalscan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import tw.com.regalscan.R;

/**
 * Created by Heidi on 2017/3/3.
 */

public class SpinnerAdapter extends BaseAdapter {

    private LayoutInflater mInflater = null;
    private String[] items = null;
    int count=0;

    public SpinnerAdapter(Context context) {

        this.mInflater = LayoutInflater.from(context);
    }

    public void add(String[] items) {

        this.items = items;
    }

    public void clear() {
//        if(ver!=null){
//            ver = new String[ver.length];
//        }
//        if(existVer!=null){
//            existVer = new String[existVer.length];
//        }
        items=null;
    }

    @Override
    public int getCount() {
        if (items == null) {
            return 0;
        } else
            return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getItemId(String Text) {
        for(int i=0;i<items.length;i++){
            if(items[i].equals(Text)){
                return i;
            }
        }
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.spinner_item, null);
            holder = new ViewHolder();
            holder.textView = convertView.findViewById(R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(items[position]);

        return convertView;
    }

    class ViewHolder {
        public TextView textView;
    }
}
