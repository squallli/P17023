package tw.com.regalscan.evaair.report;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import tw.com.regalscan.adapters.ItemListPayAdapter;
import tw.com.regalscan.R;
import tw.com.regalscan.db02.DBQuery;

public class FragmentReportUpgradePayList extends Fragment {
    private Context acticityContext;
    //付款歷程
    public ItemListPayAdapter adapter;
    private ListView discountListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Fragment剛被建立時執行
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        // Fragment即將在螢幕上顯示時執行
        View view=inflater.inflate(R.layout.fragment_report_list, container, false);
        initComponent(view);
        return view;
    }


    @Override
    public void onResume(){
        super.onResume();
    }

    public void initComponent(View v) {
        acticityContext=getActivity();
        discountListView= v.findViewById(R.id.lvItemList);
        adapter = new ItemListPayAdapter(acticityContext);
        discountListView.setAdapter(adapter);
    }


    public boolean isCardPaymentExit(){
        for(int i=0; i<adapter.getCount(); i++){
            if (adapter.getItem(i).getPayType().equals("Card")){
                return true;
            }
        }
        return false;
    }

    public void loadItem(DBQuery.TransactionPaymentPack[] payDetail){
        adapter.clear();
        for( DBQuery.TransactionPaymentPack item: payDetail ){
            // String payType, String monyType, Double mony
            adapter.addItem(item.Currency, item.PayBy, Math.abs(item.Amount), Math.abs(item.USDAmount), item.CouponNo);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
