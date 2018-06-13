package tw.com.regalscan.evaair.preorder;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import tw.com.regalscan.R;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.db.Arith;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db.VipSaleTranscation.PaymentType;
import tw.com.regalscan.utils.Tools;

/**
 * Created by Heidi on 2017/3/1.
 */

public class FragmentVIPPayCash extends Fragment {

    private Spinner spinnerCurrency;
    private EditText editAmount;
    public Button btnPay;
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
        View view=inflater.inflate(R.layout.fragment_pay_cash, container, false);
        initComponent(view);
        return view;
    }


    @Override
    public void onResume(){
        super.onResume();
        RefreshView();
    }

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
            DBQuery.ShouldPayMoney payItem= DBQuery.getPayMoneyNow( new StringBuilder(),
                VipSaleActivity._VipSaleTranscation.GetCurrencyMaxAmount( currency ));
            if(payItem==null || payItem.Currency==null){
                MessageBox.show("", "Get pay info error", mActivity, "Return");
                return;
            }
            editAmount.setText(Tools.getModiMoneyString(payItem.MaxPayAmount));
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

        editAmount= v.findViewById(R.id.editTextAmount);
        editAmount.setText(Tools.getModiMoneyString(Arith.round(VipPayActivity.payPack.USDTotalUnpay, 0)));

        spinnerCurrency.setAdapter(listCurrency);
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setEditText(spinnerCurrency.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        for(int i=0; i<listCurrency.getCount(); i++){
            if(listCurrency.getItem(i).equals(VipPayActivity.currentCurrency)){
                spinnerCurrency.setSelection(i);
            }
        }

        btnPay= v.findViewById(R.id.btnPay);
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String money= editAmount.getText().toString().trim();
                VipPayActivity.imm.hideSoftInputFromWindow(editAmount.getWindowToken(), 0);
                //金額空白
                if(money.equals("")){
                    MessageBox.show("", "Please input money", getActivity(), "Return");
                    return;
                }

                // String Currency, PaymentType PayBy, double Amount, String CouponNo,
                // String CardNo, String CardName, String CardDate, CreditCardType CardType
                VipPayActivity.getDBData(
                    spinnerCurrency.getSelectedItem().toString(),
                    PaymentType.Cash,
                    Double.parseDouble(money),
                    null, null, null, null, null, 0 );
            }
        });
    }

    public void RefreshView() {

    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        if(hidden){

        }else{
            setEditText(spinnerCurrency.getSelectedItem().toString());
        }
        super.onHiddenChanged(hidden);
    }
}
