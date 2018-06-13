package tw.com.regalscan.evaair.basket;

import java.util.ArrayList;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import tw.com.regalscan.R;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.SpinnerHideItemAdapter;
import tw.com.regalscan.db.Transaction.PaymentType;
import tw.com.regalscan.db02.DBQuery;

public class FragmentPayCoupon extends Fragment {

    private Spinner spinnerType;
    public EditText editCuponNo;
    public TextView txtCurrency, txtAmount;
    public Button btnPay;

    private static final String SC = "S/C";
    private static final String DC = "D/C";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Fragment剛被建立時執行
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Fragment即將在螢幕上顯示時執行
        View view = inflater.inflate(R.layout.fragment_pay_coupon, container, false);
        initComponent(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        RefreshView();
    }

    public void initComponent(View v) {
        txtCurrency = v.findViewById(R.id.txtCurrency);
        txtAmount = v.findViewById(R.id.txtAmount);

        spinnerType = v.findViewById(R.id.spinnerCurrency);
        ArrayList<String> list = new ArrayList<>();
        list.add("Choose coupon");
        list.add(SC);
        list.add(DC);
        SpinnerHideItemAdapter listCurrency = new SpinnerHideItemAdapter(getActivity(), R.layout.spinner_item, list, 0);
        spinnerType.setAdapter(listCurrency);
        spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0)
                    MessageBox.show("", "Scan coupon", getActivity(), "Ok");
                else
                    editCuponNo.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        editCuponNo = v.findViewById(R.id.editTextCouponNo);

        btnPay = v.findViewById(R.id.btnPay);
        btnPay.setOnClickListener(v1 -> {
            if (spinnerType.getSelectedItemPosition() == 0) {
                MessageBox.show("", "Please choose coupon type", getActivity(), "Return");
                return;
            }

            String couponNo = editCuponNo.getText().toString();
            PayActivity.imm.hideSoftInputFromWindow(editCuponNo.getWindowToken(), 0);

            //折扣券號碼不能為空
            if (couponNo.equals("")) {
                MessageBox.show("", "Please input coupon no.", getActivity(), "Return");
                return;
            }

            try {
                //先檢查Coupon資訊，成功再回傳
                if (searchCoupon(couponNo)) {
                    PaymentType typeP;
                    if (spinnerType.getSelectedItem().toString().equals(SC)) {
                        typeP = PaymentType.SC;
                    } else {
                        typeP = PaymentType.DC;
                    }
                    // ("TWD", Transaction.PaymentType.SC,500,"1700000001",null,null,null,null);
                    PayActivity.getDBData(
                        txtCurrency.getText().toString(),
                        typeP,
                        Double.parseDouble(txtAmount.getText().toString()),
                        couponNo,
                        null, null, null, null, 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Pay error", getActivity(), "Return");
            }

            editCuponNo.setText("");
            txtCurrency.setText("");
            txtAmount.setText("");
        });
    }

    // Scan過來的
    public boolean searchCoupon(String couponNum) {
        StringBuilder err = new StringBuilder();
        if (spinnerType.getSelectedItemPosition() == 0) {
            return false;
        }

        DBQuery.CouponInfo couponInfo = DBQuery.getCouponInfo(getActivity(), err, spinnerType.getSelectedItem().toString().replace("/", ""), couponNum);
        if (couponInfo == null) {
            MessageBox.show("", err.toString(), getActivity(), "Return");
            return false;
        }

        // S/C折扣卷條碼過期, 格式錯誤 （前兩碼＋2為過期
        // D/C折扣卷條碼格式錯誤
        // 折扣卷號碼重複輸入
        // 選擇的折扣券類別錯誤

        editCuponNo.setText(couponNum);
        txtCurrency.setText(couponInfo.CouponCurrency);
        txtAmount.setText(String.valueOf(couponInfo.CouponAmount));
        return true;
    }

    public void RefreshView() {

    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {

        } else {
            spinnerType.setSelection(0);
            editCuponNo.setText("");
            txtCurrency.setText("");
            txtAmount.setText("");
        }
        super.onHiddenChanged(hidden);
    }
}
