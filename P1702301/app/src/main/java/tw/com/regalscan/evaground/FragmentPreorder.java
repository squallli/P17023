package tw.com.regalscan.evaground;

import static tw.com.regalscan.evaground.ECheckUpdateActivity.adapter2;
import static tw.com.regalscan.evaground.ECheckUpdateActivity.adapter3;
import static tw.com.regalscan.evaground.ECheckUpdateActivity.adapter4;
import static tw.com.regalscan.evaground.ECheckUpdateActivity.adapterTotalPreorder;

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
import android.widget.Toast;

import java.util.LinkedHashMap;
import java.util.Map;

import tw.com.regalscan.R;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.evaground.Models.PreOrderInfo;

public class FragmentPreorder extends Fragment implements ECheckUpdateActivity.OnMainListener {

    private static Spinner mSpineer;
    public static RadioButton radioSale;
    public static RadioButton radioUnSale;

    private String key;
    private static String user;
    private ECheckUpdateActivity mActivity;
    private ListView listView, listViewTotal;
    private static int spinnerId = 0;

    private static Context context;
    private static DBQuery.PreorderInfoPack preorderInfoPack;
    private static StringBuilder errMsg = new StringBuilder();

    private int FRICTION_SCALE_FACTOR = 10;

    private boolean isFromSpinner = false;

    private boolean isFirstCreate = true;

    private String preOrderNo;

    static FragmentPreorder newInstance(String s, String user) {
        FragmentPreorder fg = new FragmentPreorder();

        Bundle bundle = new Bundle();
        bundle.putString("key", s);
        bundle.putString("user", user);
        fg.setArguments(bundle);

        return fg;
    }

    private static Map mapPreorder;

    private static String[] arrayPreorder;

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

        radioSale.setOnClickListener(v -> {
            if(user.equals("EGAS")) {
                if (!DBQuery.eGASSavePreorderState(mActivity, errMsg, preOrderNo, "S")) {
                    MessageBox.show("", errMsg.toString(), mActivity, "Return");
                }
            }else {
                if (!DBQuery.eVASavePreorderState(mActivity, errMsg, preOrderNo, "S")) {
                    MessageBox.show("", errMsg.toString(), mActivity, "Return");
                }
            }
            adapterTotalPreorder.setTotal(0);
            adapterTotalPreorder.notifyDataSetChanged();

            adapter3.refreshPR(user);
            adapter3.setReceipt(preOrderNo);
            adapter3.notifyDataSetChanged();
        });

        radioUnSale.setOnClickListener(v -> {
            PreOrderInfo preOrderInfo = adapter3.getPreOrderInfo(preOrderNo);
            if(user.equals("EGAS")){
                if (!DBQuery.eGASSavePreorderState(mActivity, errMsg, preOrderNo, "N")) {
                    MessageBox.show("", errMsg.toString(), mActivity, "Return");
                }
            }else {
                if (!DBQuery.eVASavePreorderState(mActivity, errMsg, preOrderNo, "N")) {
                    MessageBox.show("", errMsg.toString(), mActivity, "Return");
                }
            }

            //更改回庫量
            adapterTotalPreorder.setTotal(adapter3.getTotal(preOrderInfo.getPreOrderNo()));
            adapterTotalPreorder.notifyDataSetChanged();

            adapter3.refreshPR(user);
            adapter3.setReceipt(preOrderNo);
            adapter3.notifyDataSetChanged();
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (ECheckUpdateActivity) activity;
    }

    @Override
    public void onMainAction(String info) {
    }

    @Override
    public void onPause() {
        super.onPause();
        isFirstCreate = true;
    }

    public void initComponent(View v) {
        //Preorder頁簽
        try {
            if (key != null) {
                TextView mtag = v.findViewById(R.id.tag);
                TextView tag2 = v.findViewById(R.id.tag2);
                if (user.equals("EGAS")) {
                    ViewGroup.LayoutParams params = mtag.getLayoutParams();
                    params.width = 0;
                    params.height = 0;
                    mtag.setLayoutParams(params);
                    tag2.setText("STD");
                }

                mapPreorder = new LinkedHashMap() {
                    {
                        String[] preorderType = new String[]{"PR"};
                        preorderInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, preorderType, null);

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
                listView.setFriction(ViewConfiguration.getScrollFriction() * FRICTION_SCALE_FACTOR);
                listView.setAdapter(adapter3);
                listViewTotal = v.findViewById(R.id.listTotal);
                listViewTotal.setAdapter(adapterTotalPreorder);

                RadioGroup radioGroupSale = v.findViewById(R.id.radioGroup1);
                //radioGroupSale.setOnCheckedChangeListener(radioCheckedListenerPreorder);
                radioSale = v.findViewById(R.id.radioSale);
                radioUnSale = v.findViewById(R.id.radioUnsale);

                mSpineer = v.findViewById(R.id.spinner01);

                int i = 0;

                String[] preorderType = new String[]{"PR"};
                preorderInfoPack = DBQuery.getPreorderInfo(context, errMsg, null, preorderType, null);

                if (preorderInfoPack.info != null) {
                    arrayPreorder = new String[preorderInfoPack.info.length];
                } else {
                    arrayPreorder = new String[1];
                }

                for (Object key : mapPreorder.keySet()) {
                    arrayPreorder[i] = key.toString();
                    i++;
                }

                if (!arrayPreorder[0].equals("")) {
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_dropdown_item, arrayPreorder);
                    mSpineer.setAdapter(arrayAdapter);
                    mSpineer.setSelection(spinnerId);
                    mSpineer.setOnItemSelectedListener(preorderSpinnerSelectListener);
                } else {
                    String[] strings = new String[]{"No Preorder"};
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_spinner_dropdown_item, strings);
                    mSpineer.setAdapter(arrayAdapter);
                    radioSale.setVisibility(View.GONE);
                    radioUnSale.setVisibility(View.GONE);
                }
            }
        } catch (Exception obj) {
            Toast.makeText(mActivity, "發生錯誤: " + obj.getMessage() + "請返回上一頁", Toast.LENGTH_LONG).show();
        }
    }

    //Preorder Spinner監聽事件
    private AdapterView.OnItemSelectedListener preorderSpinnerSelectListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            spinnerId = position;
            isFromSpinner = true;
            preOrderNo = mSpineer.getSelectedItem().toString();
            //設定receipt
            adapter3.setReceipt(preOrderNo);
            //adapter3.refreshPR(user);
            adapter3.notifyDataSetChanged();

            switch (adapter3.getPreOrderInfo(preOrderNo).getStatus()) {
                case "S":
                    //更改回庫量
                    adapterTotalPreorder.setTotal(0);
                    adapterTotalPreorder.notifyDataSetChanged();
                    radioSale.setChecked(true);
                    break;
                default:
                    //更改回庫量
                    adapterTotalPreorder.setTotal(adapter3.getTotal(preOrderNo));
                    adapterTotalPreorder.notifyDataSetChanged();
                    radioUnSale.setChecked(true);
                    break;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    //radiobutton Preorder監聽事件
    private RadioGroup.OnCheckedChangeListener radioCheckedListenerPreorder = (group, checkedId) -> {
        try {
            //判斷是否是spinner觸發
//            if (isFromSpinner) {
//                isFromSpinner = false;
//                return;
//            }

            PreOrderInfo preOrderInfo = adapter3.getPreOrderInfo(preOrderNo);

            switch (checkedId) {
                case R.id.radioSale:
                    if (!DBQuery.eGASSavePreorderState(mActivity, errMsg, preOrderNo, "S")) {
                        MessageBox.show("", errMsg.toString(), mActivity, "Return");
                    }

//                    adapter3.modifiedReceiptChange(preOrderInfo.getPreOrderNo(), true);
//
//                    if (user.equals("EGAS")) {
//                        adapter2.addPreorderItem(preOrderInfo.getPreOrderNo(), preOrderInfo.getStatus(), "Preorder");
//                    }
//
//                    adapter2.addSavePreorderItem(preOrderInfo.getPreOrderNo(), preOrderInfo.getStatus());
//
//                    adapter2.notifyDataSetChanged();
//                    adapter3.notifyDataSetChanged();

                    //更改回庫量
                    adapterTotalPreorder.setTotal(0);
                    adapterTotalPreorder.notifyDataSetChanged();
                    break;
                case R.id.radioUnsale:
                    if (!DBQuery.eGASSavePreorderState(mActivity, errMsg, preOrderNo, "N")) {
                        MessageBox.show("", errMsg.toString(), mActivity, "Return");
                    }
//                    adapter3.modifiedReceiptChange(preOrderInfo.getPreOrderNo(), false);
//
//                    if (user.equals("EGAS")) {
//                        adapter2.removePreorderItem(adapter2.getPreorderItemId(preOrderInfo.getPreOrderNo()));
//                    }
//
//                    adapter2.addSavePreorderItem(preOrderInfo.getPreOrderNo(), preOrderInfo.getStatus());
//
//                    adapter2.notifyDataSetChanged();
//                    adapter3.notifyDataSetChanged();

                    //更改回庫量
                    adapterTotalPreorder.setTotal(adapter3.getTotal(preOrderInfo.getPreOrderNo()));
                    adapterTotalPreorder.notifyDataSetChanged();
                    break;
            }
            adapter3.refreshPR(user);
            adapter3.setReceipt(preOrderNo);
            adapter3.notifyDataSetChanged();
            isFirstCreate = false;
        } catch (Exception obj) {
            obj.printStackTrace();
        }
    };

    //將Discrepancy中取消變更的項目 在Preorder改回初始值
    static void setReceiptState(String info) {

        if (user.equals("EGAS")) {
            adapter3.modifiedReceiptChange(info, false);
        } else {

        }

        if (mSpineer.getSelectedItem().toString().equals(info)) {
            if (radioSale.isChecked()) {
                radioUnSale.setChecked(true);
            } else {
                radioSale.setChecked(true);
            }
        }

        adapter3.notifyDataSetChanged();
    }

    static int getPosition() {
        return spinnerId;
    }
}