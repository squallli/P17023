package tw.com.regalscan.evaground;

import static tw.com.regalscan.evaground.ECheckUpdateActivity.adapter2;
import static tw.com.regalscan.evaground.ECheckUpdateActivity.adapter3;
import static tw.com.regalscan.evaground.ECheckUpdateActivity.adapter4;
import static tw.com.regalscan.evaground.ECheckUpdateActivity.adapterTotalVip;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Map;

import tw.com.regalscan.R;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.evaground.Models.PreOrderInfo;

/**
 * Created by tp00169 on 2017/3/20.
 */

public class FragmentVip extends Fragment implements ECheckUpdateActivity.OnMainListener {

    private TextView mtag, mTag2;

    private static Spinner mSpinnerVip;
    private RadioGroup radioGroupSale;
    public static RadioButton radioSaleVip;
    public static RadioButton radioUnsaleVip;

    private String key;
    private static String user;
    private ECheckUpdateActivity mActivity;
    private ListView listView, listViewTotal;
    private int spinnerId = 0;
    private boolean allow = false;
    private static StringBuilder errMsg = new StringBuilder();
    private static Context context;
    private static DBQuery.PreorderInfoPack preorderInfoPack;

    private int FRICTION_SCALE_FACTOR = 10;

    private boolean isFirstCreate = true;

    private String preOrderNo;

    static FragmentVip newInstance(String s, String user) {
        FragmentVip fg = new FragmentVip();

        Bundle bundle = new Bundle();
        bundle.putString("key", s);
        bundle.putString("user", user);
        FragmentVip.user = user;
        fg.setArguments(bundle);

        return fg;
    }

    private static Map mapVip;

    private static String[] arrayVip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Fragment剛被建立時執行
        Bundle bundle = getArguments();
        key = bundle != null ? bundle.getString("key") : null;
        user = bundle != null ? bundle.getString("user") : null;
        super.onCreate(savedInstanceState);

        context = getContext();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Fragment即將在螢幕上顯示時執行

        View view = inflater.inflate(R.layout.listview_items_preorder, container, false);
        initComponent(view);

        allow = false;//fragment重新載入時設定為false

        radioSaleVip.setOnClickListener(v -> {
            if(user.equals("EGAS")){
                if (!DBQuery.eGASSavePreorderState(mActivity, errMsg, preOrderNo, "S")) {
                    MessageBox.show("", errMsg.toString(), mActivity, "Return");
                }
            }else {
                if (!DBQuery.eVASavePreorderState(mActivity, errMsg, preOrderNo, "S")) {
                    MessageBox.show("", errMsg.toString(), mActivity, "Return");
                }
            }
            adapterTotalVip.setTotal(0);
            adapterTotalVip.notifyDataSetChanged();

            adapter4.refreshVIP(user);
            adapter4.setReceipt(preOrderNo);
            adapter4.notifyDataSetChanged();
        });

        radioUnsaleVip.setOnClickListener(v -> {
            PreOrderInfo preOrderInfo = adapter4.getPreOrderInfo(preOrderNo);
            if(user.equals("EGAS")){
                if (!DBQuery.eGASSavePreorderState(mActivity, errMsg, preOrderNo, "N")) {
                    MessageBox.show("", errMsg.toString(), mActivity, "Return");
                }
            }else {
                if (!DBQuery.eVASavePreorderState(mActivity, errMsg, preOrderNo, "N")) {
                    MessageBox.show("", errMsg.toString(), mActivity, "Return");
                }
            }

            adapterTotalVip.setTotal(adapter4.getTotal(preOrderInfo.getPreOrderNo()));
            adapterTotalVip.notifyDataSetChanged();

            adapter4.refreshVIP(user);
            adapter4.setReceipt(preOrderNo);
            adapter4.notifyDataSetChanged();
        });


        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (ECheckUpdateActivity) activity;
    }

    @Override
    public void onPause() {
        super.onPause();
        isFirstCreate = true;
    }

    //將Discrepancy中取消變更的項目 在Vip改回初始值
    @Override
    public void onMainAction(String info) {
    }

    public void initComponent(View v) {
        try {
            if (key != null) {
                mtag = v.findViewById(R.id.tag);
                mTag2 = v.findViewById(R.id.tag2);
                if (user.equals("EGAS")) {
                    ViewGroup.LayoutParams params = mtag.getLayoutParams();
                    params.width = 0;
                    params.height = 0;
                    mtag.setLayoutParams(params);
                    mTag2.setText("STD");
                }

                mapVip = new LinkedHashMap() {
                    {
                        String[] preorderType = new String[]{"VP", "VS"};
                        DBQuery.PreorderInfoPack preorderInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, preorderType, null);

                        if (preorderInfoPack.info != null) {
                            for (int i = 0; i < preorderInfoPack.info.length; i++) {
                                if (user.equals("EGAS")) {
                                    put(preorderInfoPack.info[i].PreorderNO, preorderInfoPack.info[i].EGASSaleFlag);
                                } else {
                                    put(preorderInfoPack.info[i].PreorderNO, preorderInfoPack.info[i].EVASaleFlag);
                                }
                            }
                        } else {
                            put("", "N");
                        }
                    }
                };

                listView = v.findViewById(R.id.lvItemList);
                listView.setAdapter(adapter4);
                listView.setFriction(ViewConfiguration.getScrollFriction() * FRICTION_SCALE_FACTOR);
                listViewTotal = v.findViewById(R.id.listTotal);
                listViewTotal.setAdapter(adapterTotalVip);

                radioGroupSale = v.findViewById(R.id.radioGroup1);
                //radioGroupSale.setOnCheckedChangeListener(radioCheckedListener);
                radioSaleVip = v.findViewById(R.id.radioSale);
                radioUnsaleVip = v.findViewById(R.id.radioUnsale);

                mSpinnerVip = v.findViewById(R.id.spinner01);

                String[] preorderType = new String[]{"VP", "VS"};
                preorderInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, preorderType, null);

                if (preorderInfoPack.info != null) {
                    arrayVip = new String[preorderInfoPack.info.length];
                } else {
                    arrayVip = new String[1];
                }

                int i = 0;
                for (Object key : mapVip.keySet()) {
                    arrayVip[i] = key.toString();
                    i++;
                }

                if (!arrayVip[0].equals("")) {
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_dropdown_item, arrayVip);
                    mSpinnerVip.setAdapter(arrayAdapter);
                    mSpinnerVip.setSelection(spinnerId);

                    mSpinnerVip.setOnItemSelectedListener(spinnerSelectListener);
                } else {
                    String[] strings = new String[]{"No Preorder"};
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_dropdown_item, strings);
                    mSpinnerVip.setAdapter(arrayAdapter);
                    radioUnsaleVip.setVisibility(View.GONE);
                    radioSaleVip.setVisibility(View.GONE);
                }
            }

        } catch (Exception obj) {
            obj.printStackTrace();
//            Toast.makeText(mActivity, "發生錯誤: " + obj.getMessage() + "請返回上一頁", Toast.LENGTH_LONG).show();
        }
    }

    //Vip Spinner監聽事件
    private AdapterView.OnItemSelectedListener spinnerSelectListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            allow = true;
            preOrderNo = mSpinnerVip.getSelectedItem().toString();

            //設定receipt
            adapter4.setReceipt(preOrderNo);
            adapter4.notifyDataSetChanged();

            spinnerId = position;

            switch (adapter4.getPreOrderInfo(preOrderNo).getStatus()) {
                case "S":
                    //更改回庫量
                    adapterTotalVip.setTotal(0);
                    adapterTotalVip.notifyDataSetChanged();
                    radioSaleVip.setChecked(true);
                    break;
                default:
                    //更改回庫量
                    adapterTotalVip.setTotal(adapter4.getTotal(preOrderNo));
                    adapterTotalVip.notifyDataSetChanged();
                    radioUnsaleVip.setChecked(true);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    //radiobutton Vip監聽事件
    private RadioGroup.OnCheckedChangeListener radioCheckedListener = (group, checkedId) -> {
        try {
            //判斷是否是spinner觸發
//            if (allow) {
//                allow = false;
//                return;
//            }

            PreOrderInfo preOrderInfo = adapter4.getPreOrderInfo(preOrderNo);

            switch (checkedId) {
                case R.id.radioSale:
                    if (!DBQuery.eGASSavePreorderState(mActivity, errMsg, preOrderNo, "S")) {
                        MessageBox.show("", errMsg.toString(), mActivity, "Return");
                    }

//                    adapter4.modifiedReceiptChange(preOrderInfo.getPreOrderNo(), true);
//
//                    if (user.equals("EGAS")) {
//                        adapter2.addPreorderItem(preOrderInfo.getPreOrderNo(), preOrderInfo.getStatus(), "Vip");
//                    }
//
//                    adapter2.addSavePreorderItem(preOrderInfo.getPreOrderNo(), preOrderInfo.getStatus());
//
//                    adapter2.notifyDataSetChanged();
//                    adapter4.notifyDataSetChanged();
//
                    //更改回庫量
                    adapterTotalVip.setTotal(0);
                    adapterTotalVip.notifyDataSetChanged();
                    break;
                case R.id.radioUnsale:
                    if (!DBQuery.eGASSavePreorderState(mActivity, errMsg, preOrderNo, "N")) {
                        MessageBox.show("", errMsg.toString(), mActivity, "Return");
                    }
//                    adapter4.modifiedReceiptChange(preOrderInfo.getPreOrderNo(), false);
//
//                if (user.equals("EGAS")) {
//                    adapter2.removePreorderItem(adapter2.getPreorderItemId(preOrderInfo.getPreOrderNo()));
//                }
//
//                adapter2.addSavePreorderItem(preOrderInfo.getPreOrderNo(), preOrderInfo.getStatus());
//
//                adapter2.notifyDataSetChanged();
//                adapter4.notifyDataSetChanged();
//
                    //更改回庫量
                    adapterTotalVip.setTotal(adapter4.getTotal(preOrderInfo.getPreOrderNo()));
                    adapterTotalVip.notifyDataSetChanged();
                    break;
            }
            adapter4.refreshVIP(user);
            adapter4.setReceipt(preOrderNo);
            adapter4.notifyDataSetChanged();
            isFirstCreate = false;

        } catch (Exception obj) {
            obj.printStackTrace();
//                Toast.makeText(mActivity, "發生錯誤: " + obj.getMessage() + "請返回上一頁", Toast.LENGTH_LONG).show();
        }
    };


    //將Discrepancy中取消變更的項目 在Preorder改回初始值
    static void setReceiptState(String info) {

        if (user.equals("EGAS")) {
            adapter4.modifiedReceiptChange(info, false);
        }
        if (mSpinnerVip != null) {
            if (mSpinnerVip.getSelectedItem().toString().equals(info)) {
                if (radioSaleVip.isChecked()) {
                    radioUnsaleVip.setChecked(true);
                } else {
                    radioSaleVip.setChecked(true);
                }
            }
        }

        adapter4.notifyDataSetChanged();
    }

}
