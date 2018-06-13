package tw.com.regalscan.evaair.report;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import tw.com.regalscan.adapters.ItemListPictureModifyAdapter;
import tw.com.regalscan.R;
import tw.com.regalscan.db02.DBQuery;

public class FragmentReportItem extends Fragment {
    public ItemListPictureModifyAdapter adapter;
    private ListView itemListView;
    private Context acticityContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Fragment剛被建立時執行
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        // Fragment即將在螢幕上顯示時執行
        View view=inflater.inflate(R.layout.fragment_report_item, container, false);
        initComponent(view);
        return view;
    }


    @Override
    public void onResume(){
        super.onResume();
        RefreshView();
    }

    public void initComponent(View v) {
        acticityContext=getActivity();

        //items
        itemListView= v.findViewById(R.id.lvItemList);
        adapter = new ItemListPictureModifyAdapter(acticityContext, itemListView);
        adapter.setIsModifiedItem(false);
        adapter.setIsPictureZoomIn(false);
        adapter.setIsRightTwoVisible(false);
        adapter.setIsMoneyVisible(false);
        adapter.setIsRightTwoVisible(false);
        itemListView.setAdapter(adapter);
    }

    // DFS
    public void loadItem(DBQuery.TransactionInfo itemDetail){
        adapter.setIsPreorder(false);
        //DFS, 不顯示單品價格
        adapter.clear();
        for(DBQuery.TransactionItem item : itemDetail.items){
            // String ItemCode, String monyType, Double price, String itemName, int stock, Obj qty
            adapter.addItem(
                item.ItemCode, item.SerialNo, "US", item.OriginalPrice, item.ItemName,
                item.SalesQty, item.SalesQty
            );
        }
        adapter.notifyDataSetChanged();
    }


    // Preorder
    public void loadItem(DBQuery.PreorderInformation itemDetail){
        adapter.setIsPreorder(true);

        // load key
        ArrayList<String> itemCodeList=new ArrayList<>();
        for(int i=0; i<itemDetail.items.length; i++){
            itemCodeList.add(itemDetail.items[i].ItemCode);
        }
        adapter.setImageKeyCodeList(itemCodeList);

        adapter.clear();
        for(DBQuery.PreorderItem item : itemDetail.items){
            // String ItemCode, String monyType, Double price, String itemName, int stock, Obj qty
            adapter.addItem(
                item.ItemCode, item.SerialCode, "US", item.OriginalPrice, item.ItemName,
                item.SalesQty, item.SalesQty
            );
        }
        adapter.notifyDataSetChanged();
    }


    public void RefreshView() {

    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
