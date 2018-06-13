package tw.com.regalscan.evaair.report;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import tw.com.regalscan.adapters.ItemListUpgradeAdapter;
import tw.com.regalscan.R;
import tw.com.regalscan.db02.DBQuery;


public class FragmentReportUpgradeItem extends Fragment {
    public ItemListUpgradeAdapter adapter;
    private ListView itemListView;
    private Context activityContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Fragment剛被建立時執行
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        // Fragment即將在螢幕上顯示時執行
        View view=inflater.inflate(R.layout.fragment_report_item_02, container, false);
        initComponent(view);
        return view;
    }


    @Override
    public void onResume(){
        super.onResume();
    }

    public void initComponent(View v) {
        activityContext=getActivity();
        //items
        itemListView= v.findViewById(R.id.lvItemList);
        adapter = new ItemListUpgradeAdapter(activityContext);
        adapter.setIsModifiedItem(false);
        itemListView.setAdapter(adapter);
    }

    public void loadItem( DBQuery.UpgradeTransactionInfo itemDetail ){
        adapter.clear();
        for (DBQuery.UpgradeTransactionItem item : itemDetail.items){
            // String identity, String from, String to, int qty, Double total
            adapter.addItem( item.Infant, item.OriginalClass, item.NewClass, item.SalesQty, item.SalesPrice * item.SalesQty);
        }
        adapter.notifyDataSetInvalidated();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
