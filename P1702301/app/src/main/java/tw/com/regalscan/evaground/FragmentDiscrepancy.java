package tw.com.regalscan.evaground;

import static tw.com.regalscan.evaground.ECheckUpdateActivity.adapter;
import static tw.com.regalscan.evaground.ECheckUpdateActivity.adapter2;
import static tw.com.regalscan.evaground.ECheckUpdateActivity.adapterTotal;

import android.app.Activity;
import android.nfc.Tag;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.w3c.dom.Text;
import tw.com.regalscan.R;

/**
 * Created by tp00175 on 2017/6/9.
 */

public class FragmentDiscrepancy extends Fragment implements ECheckUpdateActivity.OnMainListener {

  private TextView mtag, mTag2;

  public Spinner spinner;

  public static String key, user;

  //  final private String key, user;
  private ECheckUpdateActivity mActivity;
  private ListView listView, listViewTotal;

  int FRICTION_SCALE_FACTOR = 10;

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

      } else if (key != null && key.equals(mActivity.DiscrepancyName)) {

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
        listView.setAdapter(ECheckUpdateActivity.adapter2);
        listView.setFriction(ViewConfiguration.getScrollFriction() * FRICTION_SCALE_FACTOR);
        listView.setOnItemClickListener(mActivity.itemClickListener2);
        spinner = v.findViewById(R.id.spinner01);
        spinner.setVisibility(View.INVISIBLE);
      }
    } catch (Exception obj) {
      Toast.makeText(mActivity, "Error: " + obj.getMessage() + "請返回上一頁",
          Toast.LENGTH_LONG).show();
    }
  }
}
