package tw.com.regalscan.evaground;

import static tw.com.regalscan.evaground.ECheckUpdateActivity.adapter;
import static tw.com.regalscan.evaground.ECheckUpdateActivity.adapterTotal;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import tw.com.regalscan.R;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.DrawNoPack;

public class FragmentItem extends Fragment implements ECheckUpdateActivity.OnMainListener {

  private TextView mtag, mTag2;

  public static Spinner itemSpinner;

  public static String key, user;

  //  final private String key, user;
  private ECheckUpdateActivity mActivity;
  private ListView listView, listViewTotal;

  private StringBuilder mStringBuilder = new StringBuilder();

  int FRICTION_SCALE_FACTOR = 10;

  private ArrayAdapter<String> arrayAdapter;

  private boolean isScan = false;

  private String barcodeStr;

//  static FragmentItem getInstance(String s, String user) {
//
//    FragmentItem fg = new FragmentItem();
//
//    Bundle bundle = new Bundle();
//    bundle.putString("key", s);
//    bundle.putString("user", user);
//    fg.setArguments(bundle);
//
//    return fg;
//  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    // Fragment剛被建立時執行
    Bundle bundle = getArguments();
    key = bundle != null ? bundle.getString("key") : null;
    user = bundle != null ? bundle.getString("user") : null;
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Fragment即將在螢幕上顯示時執行
    View view = inflater.inflate(R.layout.listview_items, container, false);
    initComponent(view);

    return view;
  }


  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mActivity = (ECheckUpdateActivity) activity;
  }

  @Override
  public void onMainAction(String info) {
    Toast.makeText(mActivity, "Item " + key, Toast.LENGTH_LONG).show();
  }

  public void initComponent(View v) {
    try {
      if (key != null && key.equals(mActivity.ItemsName)) {
        mtag = v.findViewById(R.id.tag);
        mTag2 = v.findViewById(R.id.tag2);
        if (user.equals("EGAS")) {
          ViewGroup.LayoutParams params = mtag.getLayoutParams();
          params.width = 0;
          params.height = 0;
          mtag.setLayoutParams(params);
          mTag2.setText("STD");
        }

        listView = v.findViewById(R.id.lvItemList);
        listView.setAdapter(adapter);
//        listView.setFriction(ViewConfiguration.getScrollFriction() * FRICTION_SCALE_FACTOR);
        listView.setOnItemClickListener(mActivity.itemClickListener);
        listViewTotal = v.findViewById(R.id.listTotal);
        listViewTotal.setAdapter(adapterTotal);

        itemSpinner = v.findViewById(R.id.spinner01);

        DrawNoPack drawNoPack = DBQuery.getAllDrawerNo(getContext(), mStringBuilder, LoginActivity.SecSeq, null, false);
        String[] drawerTotal = new String[drawNoPack.drawers.length];

        for (int i = 0; i < drawerTotal.length; i++) {
          drawerTotal[i] = drawNoPack.drawers[i].DrawNo;
        }

        arrayAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_dropdown_item, drawerTotal);
        itemSpinner.setAdapter(arrayAdapter);
        itemSpinner.setOnItemSelectedListener(itemsSpinnerSelectListner);

      } else if (key != null && key.equals(mActivity.DiscrepancyName)) {

//        mtag = v.findViewById(R.id.tag);
//        mTag2 = v.findViewById(R.id.tag2);
//
//        if (user.equals("EGAS")) {
//          ViewGroup.LayoutParams params = mtag.getLayoutParams();
//          params.width = 0;
//          params.height = 0;
//          mtag.setLayoutParams(params);
//          mTag2.setText("STD");
//        }
//        listView = v.findViewById(R.id.lvItemList);
//        listView.setAdapter(ECheckUpdateActivity.adapter2);
//        listView.setFriction(ViewConfiguration.getScrollFriction() * FRICTION_SCALE_FACTOR);
//        listView.setOnItemClickListener(mActivity.itemClickListener2);
//        itemSpinner = v.findViewById(R.id.spinner01);
//        itemSpinner.setVisibility(View.INVISIBLE);
      }
    } catch (Exception obj) {
      obj.printStackTrace();
     // Toast.makeText(mActivity, "Error: " + obj.getMessage() + "請返回上一頁", Toast.LENGTH_LONG).show();
    }
  }

  //items spinner監聽事件
  private AdapterView.OnItemSelectedListener itemsSpinnerSelectListner = new AdapterView.OnItemSelectedListener() {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

      try {
        adapter.setDrawer(position);
        adapter.notifyDataSetChanged();

        adapterTotal.setTotal(adapter.search("", "Items"));
        adapterTotal.notifyDataSetChanged();


        if (isScan) {
          adapterTotal.setTotal(adapter.search(barcodeStr.trim().toLowerCase(), "Items"));
          adapterTotal.notifyDataSetChanged();
          adapter.notifyDataSetChanged();
          isScan = false;
        }

      } catch (Exception obj) {
        obj.printStackTrace();
        //Toast.makeText(mActivity, "Error: " + obj.getMessage() + "請返回上一頁", Toast.LENGTH_LONG).show();
      }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
  };

  public void setSpinner(int position, String barcodeStr) {
    itemSpinner.setSelection(position, true);
    this.barcodeStr = barcodeStr;
    isScan = true;
  }
}
