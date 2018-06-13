package tw.com.regalscan.evaground;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import tw.com.regalscan.R;
import tw.com.regalscan.evaground.Models.clsMenu;

public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    private Context context = null;
    private List<clsMenu> group_list = null;
    private List<List<clsMenu>> item_list = null;

    public ExpandableListViewAdapter(Context context) {
        this.context = context;
    }

    public ExpandableListViewAdapter(Context context, List<clsMenu> group_list, List<List<clsMenu>> item_list) {
        this.context = context;
        this.group_list = group_list;
        this.item_list = item_list;
    }

    @Override
    public int getGroupCount() {
        return group_list.size();
    }

    //特定group內的item個數
    @Override
    public int getChildrenCount(int groupPosition) {
        return item_list.get(groupPosition).size();
    }

    //特定group內的資料
    @Override
    public String getGroup(int groupPosition) {
        return group_list.get(groupPosition).getMenuString();
    }


    // 特定group內的特定item資料
    @Override
    public String getChild(int groupPosition, int childPosition) {
        return item_list.get(groupPosition).get(childPosition).getMenuString();
    }


    //特定gruop的id
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    //特定group中的item id
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /**
     * 組和子元素是否持有穩定的ID,也就是底層數據的改變不會影響到它們
     *
     * @return
     * @see android.widget.ExpandableListAdapter#hasStableIds()
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * 獲取顯示指定組的視圖對像
     *
     * @param groupPosition 組位置
     * @param isExpanded    該組是展開狀態還是伸縮狀態
     * @param convertView   重用已有的視圖對像
     * @param parent        返回的視圖對像始終依附於的視圖組
     * @return
     * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean, View,
     * ViewGroup)
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.menu_expendlist_group, null);
            groupHolder = new GroupHolder();
            groupHolder.txt = convertView.findViewById(R.id.txt);
            groupHolder.img = convertView.findViewById(R.id.img);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder)convertView.getTag();
        }

        if (!isExpanded) {
            groupHolder.img.setBackgroundResource(R.drawable.icon_arrow_down);
        } else {
            groupHolder.img.setBackgroundResource(R.drawable.icon_arrow_up);
        }

        if(group_list.get(groupPosition).getEnable())
        {
            groupHolder.txt.setTextColor(context.getResources().getColor(R.color.colorText));
        }
        else
        {
            groupHolder.txt.setTextColor(Color.parseColor("#A3A3A3"));
        }

        groupHolder.txt.setText(group_list.get(groupPosition).getMenuString());

        if (groupPosition == 2) {
            groupHolder.img.setVisibility(View.VISIBLE);

        } else {
            groupHolder.img.setVisibility(View.GONE);
        }
        return convertView;
    }

    /**
     * 獲取一個視圖對像，顯示指定組中的指定子元素數據。
     *
     * @param groupPosition 組位置
     * @param childPosition 子元素位置
     * @param isLastChild   子元素是否處於組中的最後一個
     * @param convertView   重用已有的視圖(View)對像
     * @param parent        返回的視圖(View)對像始終依附於的視圖組
     * @return
     * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, View,
     * ViewGroup)
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ItemHolder itemHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.menu_expendlist_item, null);
            TextView textView = convertView.findViewById(R.id.txt);
            textView.setTextSize(20);
            itemHolder = new ItemHolder();
            itemHolder.txt = convertView.findViewById(R.id.txt);
//            itemHolder.img = (ImageView)convertView.findViewById(R.id.img);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ItemHolder)convertView.getTag();
        }
        if(item_list.get(groupPosition).get(childPosition).getEnable())
        {
            itemHolder.txt.setTextColor(context.getResources().getColor(R.color.colorText));
        }
        else
        {
            itemHolder.txt.setTextColor(Color.parseColor("#C0C0C0"));
        }
        itemHolder.txt.setText(item_list.get(groupPosition).get(childPosition).getMenuString());
        return convertView;
    }

    //是否選中指定位置上的child item
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    class GroupHolder {
        public ImageView img;
        public TextView txt;
    }

    class ItemHolder {
        public TextView txt;
        public ImageView img;
    }

}
