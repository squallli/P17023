package tw.com.regalscan.evaair.upgrade;

import android.app.Fragment;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.R;
import tw.com.regalscan.db.Arith;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db.UpgradeTransaction.PaymentType;
import tw.com.regalscan.utils.Tools;

public class FragmentUpgradePayCash extends Fragment {

    private Spinner spinnerCurrency;
    private EditText editAmount;
    public Button btnPay;
    private ArrayAdapter<String> listCurrency;
    private int USDIndex;

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


    public void setEditText(String money, String LastCurrency){
        for(int i=0; i<listCurrency.getCount(); i++){
            if(listCurrency.getItem(i).equals(LastCurrency)){
                spinnerCurrency.setSelection(i);
            }
        }
        editAmount.setText(money);
    }


    public void initComponent(View v) {

        spinnerCurrency = v.findViewById(R.id.spinnerCurrency);
        listCurrency = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item);
        listCurrency.add("USD");
        listCurrency.add("TWD");
        USDIndex= 0;
        spinnerCurrency.setAdapter(listCurrency);
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try{
                    DBQuery.ShouldPayMoney payItem= DBQuery.getPayMoneyNow( new StringBuilder(),
                        UpgradeBasketActivity.s_UpgradeTransaction.GetCurrencyMaxAmount( spinnerCurrency.getSelectedItem().toString() ));
                    if(payItem==null || payItem.Currency==null){
                        MessageBox.show("", "Get pay info error", getActivity(), "Return");
                        return;
                    }
                    editAmount.setText( String.valueOf(payItem.MaxPayAmount));
                }catch (Exception e){
                    e.printStackTrace();
                    MessageBox.show("", "Get pay info error", getActivity(), "Return");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        for(int i=0; i<listCurrency.getCount(); i++){
            if(listCurrency.getItem(i).equals(UpgradePayActivity.currentCurrency)){
                spinnerCurrency.setSelection(i);
            }
        }

        editAmount= v.findViewById(R.id.editTextAmount);
        editAmount.setInputType(InputType.TYPE_NULL);
        editAmount.setText(Tools.getModiMoneyString(Arith.round(UpgradePayActivity.payPack.USDTotalUnpay, 0)));

        btnPay= v.findViewById(R.id.btnPay);
        btnPay.setOnClickListener(v1 -> {

            String money= editAmount.getText().toString().trim();
            //金額空白
            if(money.equals("")){
                MessageBox.show("", "Please input money", getActivity(), "Return");
                return;
            }

            // String Currency, PaymentType PayBy, double Amount, String CouponNo,
            // String CardNo, String CardName, String CardDate, CreditCardType CardType
            UpgradePayActivity.getDBData(
                spinnerCurrency.getSelectedItem().toString(),
                PaymentType.Cash,
                Double.parseDouble(money),
                null, null, null, null, 0 );
        });
    }

    public void RefreshView() {

    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
