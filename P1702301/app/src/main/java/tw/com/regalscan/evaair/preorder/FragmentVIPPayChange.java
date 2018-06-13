package tw.com.regalscan.evaair.preorder;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.greenrobot.eventbus.EventBus;

import tw.com.regalscan.R;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db.VipSaleTranscation.PaymentType;
import tw.com.regalscan.utils.Tools;

/**
 * Created by Heidi on 2017/3/1.
 */

public class FragmentVIPPayChange extends Fragment {

    private Spinner spinnerCurrency;
    private EditText editAmount;
    public Button btnChange;
    private DBQuery.AllCurrencyListPack allCurrencyPack;
    private ArrayAdapter<String> listCurrency;
    private Activity mActivity;


  @Override
    public void onCreate(Bundle savedInstanceState) {
        // Fragment剛被建立時執行
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        // Fragment即將在螢幕上顯示時執行
        View view=inflater.inflate(R.layout.fragment_pay_change, container, false);
        initComponent(view);
        return view;
    }


    @Override
    public void onResume(){
        super.onResume();
        RefreshView();
    }
    DBQuery.ShouldPayMoney payItem;
  public void setEditText(String LastCurrency) {

    // 有上次付款幣別就設定成上次付款幣別
    boolean flag = false;
    String currency;
    try {
      for (int i = 0; i < listCurrency.getCount(); i++) {
        if (listCurrency.getItem(i).equals(LastCurrency)) {
          spinnerCurrency.setSelection(i);
          flag = true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      MessageBox.show("", "Set currency error", mActivity, "Return");
      return;
    }

    if(flag){
      currency= LastCurrency;
    }else{
      currency= "USD";
    }

    // 沒有的話預設填入美金金額
    try {
      payItem= DBQuery.getPayMoneyNow( new StringBuilder(),
          VipSaleActivity._VipSaleTranscation.GetCurrencyMaxAmount( currency ));
      if(payItem==null || payItem.Currency==null){
        MessageBox.show("", "Get pay info error", mActivity, "Return");
        return;
      }
      editAmount.setText(Tools.getModiMoneyString(Math.abs((payItem.MaxPayAmount))));
    }catch (Exception e){
      e.printStackTrace();
      MessageBox.show("", "Get pay info error", getActivity(), "Return");
    }
  }

    public void initComponent(View v) {
      mActivity = getActivity();

        spinnerCurrency = v.findViewById(R.id.spinnerCurrency);
        //取得可以使用的幣別, 20
        //Cash可用購物車的七種幣別付款
        StringBuilder err=new StringBuilder();
        allCurrencyPack= DBQuery.getAllCurrencyList(getActivity(), err);
        if( allCurrencyPack==null ){
            MessageBox.show("", "Get currency error", getActivity(), "Return");
            ActivityManager.removeActivity(getActivity().getClass().getSimpleName());
            getActivity().finish();
        }
      listCurrency = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item);
      for(int i=0; i<allCurrencyPack.currencyList.length; i++){
        listCurrency.add( allCurrencyPack.currencyList[i].CurDvr );
      }

        editAmount= v.findViewById(R.id.editTextChange);

        spinnerCurrency.setAdapter(listCurrency);
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
              setEditText(spinnerCurrency.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        btnChange= v.findViewById(R.id.btnChange02);
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String money= editAmount.getText().toString().trim();
                VipPayActivity.imm.hideSoftInputFromWindow(editAmount.getWindowToken(), 0);
                //金額空白
                if(money.equals("")){
                    MessageBox.show("", "Please input money", getActivity(), "Ok");
                    return;
                }

                // String Currency, PaymentType PayBy, double Amount, String CouponNo,
                // String CardNo, String CardName, String CardDate, CreditCardType CardType
              VipPayActivity.getDBData(
                    spinnerCurrency.getSelectedItem().toString(),
                    PaymentType.Change,
                    Double.parseDouble(money),
                    null, null, null, null, null, 2 );
            }
        });
    }

    public void RefreshView() {

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    Boolean dialogShow = false;

    //檢核
    public void convertToABS(int MaxPayAmount) {
        try {
            if (!dialogShow) {
                if (MaxPayAmount > 0) {
                    dialogShow = true;
                    //正數無法找零
                    new AlertDialog.Builder(mActivity)
                            .setTitle("")
                            .setCancelable(false)
                            .setMessage("Payment amount is not enough,please correct the amount or use credit card to pay.")
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    EventBus.getDefault().post("刪除最後一筆");
                                    dialogShow = false;
                                }
                            }).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    @Override
  public void onHiddenChanged(boolean hidden) {
//    if(hidden){
//
//    }else{
//      setEditText(spinnerCurrency.getSelectedItem().toString());
//    }
        if(!hidden){
            try {
                String currency = spinnerCurrency.getSelectedItem().toString();
                setEditText(currency);
                convertToABS(payItem.MaxPayAmount);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    super.onHiddenChanged(hidden);
  }
}
