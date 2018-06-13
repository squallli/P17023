package tw.com.regalscan.evaair.basket;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.regalscan.sqlitelibrary.TSQL;
import tw.com.regalscan.R;
import tw.com.regalscan.component.MagReadService;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.db.Arith;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.LogType;
import tw.com.regalscan.db.MagReader;
import tw.com.regalscan.db.Transaction.CreditCardType;
import tw.com.regalscan.db.Transaction.PaymentType;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.ShouldPayMoney;
import tw.com.regalscan.evaair.ife.CrewCartActivity;
import tw.com.regalscan.evaair.ife.OnlineBasketActivity;
import tw.com.regalscan.evaair.ife.OrderDetailActivity;
import tw.com.regalscan.utils.Tools;

//import tw.com.regalscan.evaair.ife.ProcessingOrderDetailActivity;

public class FragmentPayCard extends Fragment {

    public Spinner spinnerCurrency;
    public EditText editAmount;
    public Button btnPay;
    private Activity mActivity;
    private Context mContext;
    public ArrayList<String> cardData;
    private boolean isCUP = false;
    private String track1 = "";
    private String track2 = "";
    private MagReadService mReadService;
    public CreditCardType CardType = null;

    private TSQL mTSQL;

    //卡片資訊
    public TextView txtCardNumber, txtCardDate, txtCardType;
    public static ArrayAdapter<String> listCurrency;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            String msgWhitchContryCard = "";

            switch (msg.what) {
                case MagReadService.MESSAGE_READ_MAG:
                    track1 = msg.getData().getString(MagReadService.CARD_TRACK1);
                    track2 = msg.getData().getString(MagReadService.CARD_TRACK2);

                    try {
                        // 1. 偵測VISA等卡 (Credit Card)
                        MagReader magReader = new MagReader(FlightData.FlightDate);

                        // CardType, CardNo, 持卡人名稱, 到期日, ServiceCode
                        cardData = magReader.GetCardData(isCUP, track1, track2);

                        // cardData.get(0): 0為失敗, 1為成功
                        // size>5: 有資料, size==2: cardData.get(1)為錯誤訊息
                        if (cardData.size() < 5) {
                            MessageBox.show("", cardData.get(1), mActivity, "Return");
                            return;
                        }

                        if (isCUP && !cardData.get(0).equals("CUP")) {
                            MessageBox.show("", getString(R.string.Error_GetCardTypeError), mActivity, "Return");
                            return;
                        }

                        // 確認黑名單卡
                        StringBuilder err = new StringBuilder();
                        if (DBQuery.checkBlackCard(mContext, err, cardData.get(0), cardData.get(1))) {
                            mTSQL.WriteLog(FlightData.SecSeq, LogType.ACTION, "Black card", "", "Pay by black card - " + cardData.get(1));
                            MessageBox.show("", err.toString(), mActivity, "Return");
                            return;
                        }
                        listCurrency.clear();
                        // (1) AE: TWD Only
                        // (2) JCB, Visa, Master:
                        // if(台灣發行)   TWD only
                        // else USD/ TWD
                        if (!isCUP) {
                            // 台灣卡: 顯示彈跳視窗
                            if (DBQuery.checkIsTaiwanCard(mContext, err, cardData.get(1))) {
                                listCurrency.add("TWD");
                                msgWhitchContryCard = "This card accepts TWD only";
                            } else {
                                // 不顯示彈跳視窗
                                listCurrency.add("USD");
                                listCurrency.add("TWD");
                            }
                        } else {
                            // 2. CUP: TWD only, 顯示彈跳視窗
                            listCurrency.add("TWD");
                        }
                        listCurrency.notifyDataSetChanged();

                        // CardType, CardNo, 持卡人名稱, 到期日, ServiceCode
                        txtCardNumber.setText(cardData.get(1));
                        txtCardDate.setText(cardData.get(3));
                        if (cardData.get(0).equals("CUP")) {
                            txtCardType.setText("Union Pay");
                        } else {
                            txtCardType.setText(cardData.get(0));
                        }

                        btnPay.setEnabled(true);
                        spinnerCurrency.setEnabled(true);

                        // 顯示台灣卡提示視窗
                        if (!msgWhitchContryCard.equals("")) {
                            MessageBox.show("", msgWhitchContryCard, mActivity, "Ok");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        MessageBox.show("", "Please retry", mActivity, "Return");
                        TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                        _TSQL.WriteLog(FlightData.SecSeq,
                            "System", "FragmentPayCard", "MagReadService", e.getMessage());
                        return;
                    }
                    break;
                case MagReadService.MESSAGE_OPEN_MAG:
                    break;
                case MagReadService.MESSAGE_CHECK_FAILE:
                    break;
                case MagReadService.MESSAGE_CHECK_OK:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Fragment剛被建立時執行
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Fragment即將在螢幕上顯示時執行
        View view = inflater.inflate(R.layout.fragment_pay_card, container, false);
        initComponent(view);
        mReadService = new MagReadService(mActivity, mHandler, mActivity);
//        if (PayActivity.isCUB) {
            mReadService.start();
//        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        RefreshView();
        if (mReadService != null) {
            mReadService.start();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mReadService != null) {
            mReadService.start();
        }
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

        if (flag) {
            currency = LastCurrency;
        } else {
            currency = "USD";
        }

        // 沒有的話預設填入美金金額
        try {
            ShouldPayMoney payItem = new ShouldPayMoney();
            switch (PayActivity.fromWhere) {
                case "BasketActivity":
                    payItem = DBQuery.getPayMoneyNow(new StringBuilder(), BasketActivity.getBasketTransaction().GetCurrencyMaxAmount(currency));
                    break;
                case "OrderEditActivity":
                case "OrderDetailActivity":
                    payItem = DBQuery.getPayMoneyNow(new StringBuilder(), OrderDetailActivity._Transaction.GetCurrencyMaxAmount(currency));
                    break;
                case "ProcessingOrderEditActivity":
                case "ProcessingOrderDetailActivity":
                    payItem = DBQuery.getPayMoneyNow(new StringBuilder(), OrderDetailActivity._Transaction.GetCurrencyMaxAmount(currency));
                    break;
                case "CrewCartActivity":
                    payItem = DBQuery.getPayMoneyNow(new StringBuilder(), CrewCartActivity.s_Transaction.GetCurrencyMaxAmount(currency));
                    break;
                case "OnlineBasketActivity":
                case "OrderDetail":
                case "ProcessingOrder":
                    payItem = DBQuery.getPayMoneyNow(new StringBuilder(), OnlineBasketActivity.getBasketTransaction().GetCurrencyMaxAmount(currency));
                    break;
            }

            if (payItem == null || payItem.Currency == null) {
                MessageBox.show("", "Get pay info error", mActivity, "Return");
                return;
            }
            editAmount.setText(Tools.getModiMoneyString(payItem.MaxPayAmount));
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get pay info error", mActivity, "Return");
        }
    }


    // CUP僅顯示TWD
    public void setCUP(boolean CUP) {
        isCUP = CUP;
        listCurrency.clear();
        btnPay.setEnabled(false);
        spinnerCurrency.setEnabled(false);
    }


    public void initComponent(View v) {
        mActivity = getActivity();
        mContext = mActivity.getApplicationContext();

        mTSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");

        txtCardType = v.findViewById(R.id.txtCardtype);
        txtCardNumber = v.findViewById(R.id.txtCardNum);
        txtCardDate = v.findViewById(R.id.txtCardDate);

        spinnerCurrency = v.findViewById(R.id.spinnerCurrency);
        listCurrency = new ArrayAdapter<>(getActivity(), R.layout.spinner_item);
        spinnerCurrency.setAdapter(listCurrency);
        spinnerCurrency.setEnabled(false);
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setEditText(spinnerCurrency.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        editAmount = v.findViewById(R.id.editTextAmount);
        editAmount.setText(Tools.getModiMoneyString(Arith.round(PayActivity.payPack.USDTotalUnpay, 0)));

        btnPay = v.findViewById(R.id.btnPay);
        btnPay.setOnClickListener(v1 -> {

            String money = editAmount.getText().toString().trim();
            PayActivity.imm.hideSoftInputFromWindow(editAmount.getWindowToken(), 0);

            //金額空白
            if (money.equals("")) {
                MessageBox.show("", "Please input money", mActivity, "Return");
                return;
            }

            // 確認是否取到卡片
            if (cardData.isEmpty()) {
                MessageBox.show("", "Please slash credit card", mActivity, "Return");
                return;
            }

            // 判斷卡種
            String cardType = cardData.get(0);
            switch (cardType) {
                case "VISA":
                    CardType = CreditCardType.VISA;
                    break;
                case "MASTER":
                    CardType = CreditCardType.MASTER;
                    break;
                case "JCB":
                    CardType = CreditCardType.JCB;
                    break;
                case "AE":
                    CardType = CreditCardType.AMX;
                    break;
                case "CUP":
                    CardType = CreditCardType.CUP;
                    break;
            }

            if (CardType == null) {
                MessageBox.show("", "Please check card type", mActivity, "Return");
                return;
            }

            PayActivity.getDBData(
                // String Currency, PaymentType PayBy, double Amount, String CouponNo,
                spinnerCurrency.getSelectedItem().toString(),
                PaymentType.Card,
                Double.parseDouble(money),
                // cardData: 卡種, 卡號, 持卡人姓名, 到期日, 安全碼
                // String CardNo, String CardName, String CardDate, CreditCardType CardType
                null, cardData.get(1), cardData.get(2), cardData.get(3), CardType, 0);

            txtCardNumber.setText("No");
            txtCardDate.setText("Date");
            txtCardType.setText("Type");
//            setEditText(spinnerCurrency.getSelectedItem().toString());
        });
    }


    public void RefreshView() {

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mReadService != null) {
            mReadService.stop();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mReadService != null) {
            mReadService.stop();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            mReadService.stop();

            txtCardNumber.setText("No");
            txtCardDate.setText("Date");
            cardData = new ArrayList<>();
        } else {
            if (mReadService != null)
                mReadService.start();
        }
        super.onHiddenChanged(hidden);
    }
}
