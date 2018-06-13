package tw.com.regalscan.evaair.preorder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.google.gson.Gson;
import com.regalscan.sqlitelibrary.TSQL;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import tw.com.regalscan.R;
import tw.com.regalscan.activities.DiscountCheckActivity;
import tw.com.regalscan.adapters.ItemListPayAdapter;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.NavigationDrawer;
import tw.com.regalscan.component.SwipeListView;
import tw.com.regalscan.customClass.ItemPayInfo;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.PaymentItem;
import tw.com.regalscan.db.PublicFunctions;
import tw.com.regalscan.db.VipSaleTranscation.CreditCardType;
import tw.com.regalscan.db.VipSaleTranscation.PaymentType;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.PaymentList;
import tw.com.regalscan.db02.DBQuery.PreorderInfoPack;
import tw.com.regalscan.db02.DBQuery.VIPSaleHeader;
import tw.com.regalscan.evaair.ife.NCCCAuthorize;
import tw.com.regalscan.evaair.ife.model.entity.AuthorizeModel;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;

public class VipPayActivity extends AppCompatActivity {
    //Scan
    private final String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private int soundid;
    private String barcodeStr;

    //付款歷程
    public static ItemListPayAdapter adapter;
    private SwipeListView discountListView;

    private TextView txtToolbarTitle;
    private RelativeLayout rowText01;
    private static Button btnReturn, btnCash, btnCard, btnCupon, btnChange, btnPrint;
    public static Context mContext;
    public static Activity mActivity;
    private boolean isDiscountVisible = false;
    private ImageView arrow;
    private FragmentManager fm = getFragmentManager();
    private static FragmentVIPPayCard f_card = new FragmentVIPPayCard();
    private static FragmentVIPPayCash f_cash = new FragmentVIPPayCash();
    private static FragmentVIPPayChange f_change = new FragmentVIPPayChange();
    private static FragmentVIPPayCoupon f_coupon = new FragmentVIPPayCoupon();

    //應付款總額
    public static TextView txtShouldPayMoney, txtNotPayMoney, txtChangeMoney;
    public static VIPSaleHeader vipheader;
    public static DBQuery.PaymentModePack payPack;
    public static InputMethodManager imm;
    public static final int DISCOUNT_INFO = 600;

    private ProgressDialog mloadingDialog;

    private NCCCAuthorize mNCCCAuthorize;

    // 現在顯示哪個頁面
    private static int whichBtnIsClicked = -1;
    // 0= cash, 1= card, 2= coupon, 3= change, 4= balance
    public static String currentCurrency = "";

    public static boolean isCUB = false;

    private boolean AuthorizeFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip_pay);
        EventBus.getDefault().register(this);

        Bundle argument = getIntent().getExtras();
        if (argument != null) {
            String itemString = argument.getString("VIPPack");
            Gson gson = new Gson();
            vipheader = gson.fromJson(itemString, VIPSaleHeader.class);
            currentCurrency = argument.getString("Currency");
            init();
        } else {
            if (MessageBox.show("", "No VIP pay list", this, "Ok")) {
                finish();
            }
        }
    }

    private void init() {
        mContext = this;
        mActivity = this;
        mVibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        imm = (InputMethodManager)getSystemService(Service.INPUT_METHOD_SERVICE);
        enableExpandableList();

        mNCCCAuthorize = new NCCCAuthorize(mContext);

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            try {
                VipSaleActivity._VipSaleTranscation.ClearPaymentList();
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                finish();
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Clear pay info error", mContext, "Return");
            }
        });

        try {
            StringBuilder sb = new StringBuilder();
            payPack = DBQuery.getPayMode(sb, VipSaleActivity._VipSaleTranscation.GetPaymentMode());
            if (payPack == null) {
                MessageBox.show("", sb.toString(), mContext, "Return");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get pay info error", mContext, "Return");
            return;
        }

        discountListView = findViewById(R.id.discountList);
        adapter = new ItemListPayAdapter(mContext, discountListView.getRightViewWidth());
        adapter.setIsSwipeDelete(true);
        adapter.setItemSwipeListener(itemSwipeListener);
        discountListView.setVisibility(View.GONE);
        discountListView.setAdapter(adapter);
        for (PaymentList payItem : payPack.payLisy) {
            adapter.addItem(payItem.Currency, payItem.PayBy, payItem.Amount, payItem.USDAmount, payItem.CouponNo);
        }
        adapter.notifyDataSetChanged();

        //應付款金額
        txtShouldPayMoney = findViewById(R.id.txtshouldPayMoney);
        txtShouldPayMoney.setText(Tools.getModiMoneyString(payPack.USDTotalAmount));
        //待付款金額
        txtNotPayMoney = findViewById(R.id.txtNotPayMoney);
        txtNotPayMoney.setText(Tools.getModiMoneyString(payPack.USDTotalUnpay));
        //應找零金額
        txtChangeMoney = findViewById(R.id.txtChangeMoney);

        arrow = findViewById(R.id.img);
        arrow.setImageResource(R.drawable.icon_arrow_down_white);
        //付款歷程的顯示
        rowText01 = findViewById(R.id.rowText01);
        rowText01.setOnClickListener(v -> {
            // set ListView的visiable，和換箭頭icon
            if (isDiscountVisible) {
                isDiscountVisible = false;
                discountListView.setVisibility(View.GONE);
                arrow.setImageResource(R.drawable.icon_arrow_down_white);
            } else {
                isDiscountVisible = true;
                discountListView.setVisibility(View.VISIBLE);
                arrow.setImageResource(R.drawable.icon_arrow_up_white);
            }
        });

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.layout_fragment, f_cash, "Cash");
        ft.add(R.id.layout_fragment, f_card, "Card");
        ft.add(R.id.layout_fragment, f_coupon, "Coupon");
        ft.add(R.id.layout_fragment, f_change, "Change");
        ft.commit();

        btnCash = findViewById(R.id.btnCash);
        btnCard = findViewById(R.id.btnCard);
        btnCard.setText("Card");
        btnCupon = findViewById(R.id.btnCupon);
        btnChange = findViewById(R.id.btnChange);
        btnChange.setEnabled(false);
        setFragmentBtnOnclickAction();
        CUBBtnSetting(vipheader.UpperLimitType.equals("CUB"));

        btnPrint = findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(view -> {

            //當待付款 = 0且沒有應找零金額時，可以點選右下角Print按鈕進行結帳，並列印收據
            if (payPack.NowPayMode.equals("PAY")) {
                MessageBox.show("", "Should pay", mContext, "Ok");
                return;
            }

            if (payPack.NowPayMode.equals("CHANGE")) {
                MessageBox.show("", "Should change", mContext, "Ok");
                return;
            }

            if (FlightData.OnlineAuthorize) {
                AuthorizeModel authorizeModel = null;
                for (PaymentItem paymentItem : VipSaleActivity._VipSaleTranscation.PaymentList) {
                    if (paymentItem.PayBy.equals("Card") && !paymentItem.CardType.equals("CUP")) {
                        authorizeModel = new AuthorizeModel();
                        authorizeModel.setCREDIT_CARD_NO(paymentItem.CardNo);
                        authorizeModel.setEXP_MONTH(paymentItem.CardDate.substring(0, 2));
                        authorizeModel.setEXP_YEAR(paymentItem.CardDate.substring(2, 4));
                        authorizeModel.setCREDIT_CARD_NAME(paymentItem.CardName);

                        switch (paymentItem.CardType) {
                            case "JCB":
                                authorizeModel.setCREDIT_CARD_TYPE("JC");
                                break;
                            case "VISA":
                                authorizeModel.setCREDIT_CARD_TYPE("VI");
                                break;
                            case "MASTER":
                                authorizeModel.setCREDIT_CARD_TYPE("MT");
                                break;
                            case "AMX":
                                authorizeModel.setCREDIT_CARD_TYPE("AX");
                                break;
                        }

                        authorizeModel.setRECIPT_NO(Integer.parseInt(VipSaleActivity._VipSaleTranscation.ReceiptNo));
                        authorizeModel.setCurrency(paymentItem.Currency);

                        authorizeModel.setTWD_AMT(paymentItem.Currency.equals("TWD") ? (int)paymentItem.Amount : 0);
                        authorizeModel.setUSD_AMT(paymentItem.Currency.equals("USD") ? (int)paymentItem.Amount : 0);

                        authorizeModel.setUG_MARK("N");
                        authorizeModel.setDEPT_FLT_NO(FlightData.FlightNo);
                        authorizeModel.setVIP_NO("");
                        authorizeModel.setVIP_TYPE("");
                        authorizeModel.setORDER_NO(vipheader.PreorderNo);
                        authorizeModel.setIFE_SEQ("");
                        authorizeModel.setREAUTH_MARK("N");

                        break;
                    }
                }

                if (authorizeModel == null) {
                    savePayInfo();
                } else {
                    mNCCCAuthorize.SendRequestToNCCCGetAuthorize(authorizeModel, new NCCCAuthorize.AuthorizeReturn() {
                        @Override
                        public void success(AuthorizeModel authorizeModel) {
                            AuthorizeFlag = true;
                            savePayInfo();
                        }

                        @Override
                        public void failed(String errMsg) {
                            if (!errMsg.equals("") && MessageBox.show("", errMsg, mContext, "Ok")) {

                            } else {
                                savePayInfo();
                            }
                        }
                    });
                }
            } else {
                savePayInfo();
            }
        });
        btnPrint.setEnabled(false);

        setListenerToRootView();

        txtToolbarTitle = findViewById(R.id.toolbar_title);
        txtToolbarTitle.setText("VIP Sale Pay");

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }

    private static void CUBBtnSetting(boolean b) {
        if (b) {
            isCUB = true;
            btnCard.performClick();
            btnCash.setEnabled(false);
            btnCupon.setEnabled(false);
            btnChange.setEnabled(false);
            whichBtnIsClicked = 1;
            updateButtonColor(5);
        } else {
            btnCash.performClick();
            whichBtnIsClicked = 0;
        }
    }

    private void enableExpandableList() {
        final DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ExpandableListView expandableListView = findViewById(R.id.expendlist);
        NavigationDrawer navigationDrawer = new NavigationDrawer(mActivity, mContext, drawerLayout, toolbar, expandableListView);
    }

    private void setFragmentBtnOnclickAction() {
        btnCash.setOnClickListener(view -> {
            FragmentTransaction ft = fm.beginTransaction();
            if (fm.findFragmentByTag("Cash") != null) {
                ft.show(fm.findFragmentByTag("Cash"));
            }
            ft.hide(f_change);
            ft.hide(f_card);
            ft.hide(f_coupon);
            ft.commit();
            whichBtnIsClicked = 0;
            updateButtonColor(whichBtnIsClicked);
        });

        btnCard.setOnClickListener(view -> {
            FragmentTransaction ft = fm.beginTransaction();
            if (fm.findFragmentByTag("Card") != null) {
                ft.show(fm.findFragmentByTag("Card"));
            }
            ft.hide(f_change);
            ft.hide(f_cash);
            ft.hide(f_coupon);
            ft.commit();
            whichBtnIsClicked = 1;
            updateButtonColor(whichBtnIsClicked);

            //彈跳確認視窗
            final Dialog dialog = new Dialog(mContext);
            dialog.setContentView(R.layout.dialog_credit_card);
            dialog.setTitle("Please choose card type");
            dialog.setCancelable(true);
            RadioGroup rgroup = dialog.findViewById(R.id.radioGroup);
            rgroup.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId) {
                    case R.id.radioBtn01:
                        // 可用USD或TWD付款
                        // 選擇卡別後先不填入幣別(disable), 刷卡後判斷卡種再填入
                        f_card.txtCardType.setText("Credit Card");
                        // TWDOnly, isEnable
                        f_card.setCUP(false);
                        break;

                    case R.id.radioBtn02: // CUP只能用台幣付款
                        f_card.txtCardType.setText("Union Pay");
                        f_card.setCUP(true);
                        break;
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                }, 500);
            });
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        });

        btnCupon.setOnClickListener(view -> {
            FragmentTransaction ft = fm.beginTransaction();
            if (fm.findFragmentByTag("Coupon") != null) {
                ft.show(fm.findFragmentByTag("Coupon"));
            }
            ft.hide(f_change);
            ft.hide(f_cash);
            ft.hide(f_card);
            ft.commit();
            whichBtnIsClicked = 2;
            updateButtonColor(whichBtnIsClicked);
        });

        btnChange.setOnClickListener(view -> {
            FragmentTransaction ft = fm.beginTransaction();
            if (fm.findFragmentByTag("Change") != null) {
                ft.show(fm.findFragmentByTag("Change"));
            }
            ft.hide(f_coupon);
            ft.hide(f_cash);
            ft.hide(f_card);
            ft.commit();
            whichBtnIsClicked = 3;
            updateButtonColor(whichBtnIsClicked);
        });
        btnChange.setEnabled(false);
    }

    // 0= cash, 1= card, 2= coupon, 3= change, 4= balance
    // 5= cub強制只能刷cub
    private static void updateButtonColor(int whitchIsClick) {

        switch (whitchIsClick) {
            case 0:
                btnCash.setBackgroundColor(Color.parseColor("#007042"));
                btnCash.setTextColor(Color.WHITE);
                if (!btnCard.isEnabled()) {
                    btnCard.setBackgroundColor(Color.WHITE);
                    btnCard.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnCard.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnCard.setTextColor(Color.parseColor("#4E4A4B"));
                }
                if (!btnCupon.isEnabled()) {
                    btnCupon.setBackgroundColor(Color.WHITE);
                    btnCupon.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnCupon.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnCupon.setTextColor(Color.parseColor("#4E4A4B"));
                }
                if (!btnChange.isEnabled()) {
                    btnChange.setBackgroundColor(Color.WHITE);
                    btnChange.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnChange.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnChange.setTextColor(Color.parseColor("#4E4A4B"));
                }
                break;

            case 1:
                btnCard.setBackgroundColor(Color.parseColor("#007042"));
                btnCard.setTextColor(Color.WHITE);
                if (!btnCash.isEnabled()) {
                    btnCash.setBackgroundColor(Color.WHITE);
                    btnCash.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnCash.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnCash.setTextColor(Color.parseColor("#4E4A4B"));
                }
                if (!btnCupon.isEnabled()) {
                    btnCupon.setBackgroundColor(Color.WHITE);
                    btnCupon.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnCupon.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnCupon.setTextColor(Color.parseColor("#4E4A4B"));
                }
                if (!btnChange.isEnabled()) {
                    btnChange.setBackgroundColor(Color.WHITE);
                    btnChange.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnChange.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnChange.setTextColor(Color.parseColor("#4E4A4B"));
                }
                break;
            case 2:
                btnCupon.setBackgroundColor(Color.parseColor("#007042"));
                btnCupon.setTextColor(Color.WHITE);
                if (!btnCash.isEnabled()) {
                    btnCash.setBackgroundColor(Color.WHITE);
                    btnCash.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnCash.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnCash.setTextColor(Color.parseColor("#4E4A4B"));
                }
                if (!btnCard.isEnabled()) {
                    btnCard.setBackgroundColor(Color.WHITE);
                    btnCard.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnCard.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnCard.setTextColor(Color.parseColor("#4E4A4B"));
                }
                if (!btnChange.isEnabled()) {
                    btnChange.setBackgroundColor(Color.WHITE);
                    btnChange.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnChange.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnChange.setTextColor(Color.parseColor("#4E4A4B"));
                }
                break;
            case 3:
                btnChange.setBackgroundColor(Color.parseColor("#007042"));
                btnChange.setTextColor(Color.WHITE);
                if (!btnCash.isEnabled()) {
                    btnCash.setBackgroundColor(Color.WHITE);
                    btnCash.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnCash.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnCash.setTextColor(Color.parseColor("#4E4A4B"));
                }
                if (!btnCard.isEnabled()) {
                    btnCard.setBackgroundColor(Color.WHITE);
                    btnCard.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnCard.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnCard.setTextColor(Color.parseColor("#4E4A4B"));
                }
                if (!btnCupon.isEnabled()) {
                    btnCupon.setBackgroundColor(Color.WHITE);
                    btnCupon.setTextColor(Color.parseColor("#C9CACA"));
                } else {
                    btnCupon.setBackgroundColor(Color.parseColor("#C9CACA"));
                    btnCupon.setTextColor(Color.parseColor("#4E4A4B"));
                }
                break;
            case 4:
                btnCash.setBackgroundColor(Color.WHITE);
                btnCash.setTextColor(Color.parseColor("#C9CACA"));
                btnCard.setBackgroundColor(Color.WHITE);
                btnCard.setTextColor(Color.parseColor("#C9CACA"));
                btnCupon.setBackgroundColor(Color.WHITE);
                btnCupon.setTextColor(Color.parseColor("#C9CACA"));
                btnChange.setBackgroundColor(Color.WHITE);
                btnChange.setTextColor(Color.parseColor("#C9CACA"));
                break;

            case 5:
                btnCash.setBackgroundColor(Color.WHITE);
                btnCash.setTextColor(Color.parseColor("#C9CACA"));

                btnCard.setBackgroundColor(Color.parseColor("#007042"));
                btnCard.setTextColor(Color.WHITE);

                btnCupon.setBackgroundColor(Color.WHITE);
                btnCupon.setTextColor(Color.parseColor("#C9CACA"));
                btnChange.setBackgroundColor(Color.WHITE);
                btnChange.setTextColor(Color.parseColor("#C9CACA"));
                break;
        }
    }


    // 付款
    // Mode: 0= AddPaymentList, 1= DeletePaymentList, 2= AddChangeList
    public static void getDBData(String Currency, PaymentType PayBy, double Amount, String CouponNo,
                                 String CardNo, String CardName, String CardDate, CreditCardType CardType,
                                 int Mode) {

        try {
            StringBuilder err = new StringBuilder();
            // 新增或刪除付款
            switch (Mode) {
                case 0: //Add
                    payPack = DBQuery.getPayMode(err,
                        VipSaleActivity._VipSaleTranscation.AddPaymentList(
                            Currency, PayBy, Amount, CouponNo, CardNo, CardName, CardDate, CardType));
                    break;
                case 1: //Delete
                    // String Currency, PaymentType PayBy, String CouponNo
                    payPack = DBQuery.getPayMode(err,
                        VipSaleActivity._VipSaleTranscation.DeletePaymentList(Currency, PayBy, CouponNo));
                    break;
                case 2: //Change
                    payPack = DBQuery.getPayMode(err,
                        VipSaleActivity._VipSaleTranscation.AddChangeList(Currency, Amount));
                    break;
            }

            String upperLimit = VipSaleActivity._VipSaleTranscation.getUpperLimitType();

            if (payPack == null) {
                // 如果為新增付款, 且errMsg內有 "This card had been used(ry)", 超過刷卡限額, 詢問有無放大身分
                if (Mode == 0
                    && err.toString().length() > 23
                    && err.toString().substring(0, 23).equals("This card had been used")
                    && upperLimit.equals("")
                    && MessageBox.show("", "Is the PAX CD/CG/EC/EP/AEG/CUB/UNIONP  card holder or STAFF?", mActivity, "Yes", "No")) {
                    // 彈跳小視窗
                    Bundle argument = new Bundle();
                    argument.putString("fromWhere", "FragmentPayCard");
                    Intent intent = new Intent();
                    intent.putExtras(argument);
                    intent.putStringArrayListExtra("discountArrayList", new ArrayList<String>());
                    intent.setClass(mActivity, DiscountCheckActivity.class);
                    mActivity.startActivityForResult(intent, DISCOUNT_INFO);
                } else {
                    MessageBox.show("", err.toString(), mContext, "Return");
                    f_card.btnPay.setEnabled(false);
                    f_card.cardData = new ArrayList<>();
                }
                return;
            }

            // 取得已刷次數, 剩餘可刷金額
            int SwipeCount = 0, LastLimitation = 0;
            for (PaymentList p : payPack.payLisy) {
                if (p.PayBy.equals("Card")) {
                    SwipeCount = p.SwipeCount;
                    LastLimitation = p.LastLimitation;
                    break;
                }
            }
            // 如果是新增付款, 用刷卡, 且刷過一次以上 顯示已刷額度訊息
            if (Mode == 0 && PaymentType.Card == PayBy && SwipeCount > 0) {
                MessageBox.show("", "This card had been used " + SwipeCount
                    + " times, the last card limitation is USD "
                    + Tools.getModiMoneyString(LastLimitation), mContext, "Ok");
            }

            // 如果是新增付款, 卡片付款超過 TWD 8000/10000要顯示視窗
            if (Mode == 0 && PaymentType.Card == PayBy && vipheader.UpperLimitType.equals("")) {
                double amount;
                // 美金付款要換算
                if (Currency.equals("USD")) {
                    amount = new PublicFunctions(mContext, FlightData.SecSeq).ChangeCurrencyAmount("USD", "TWD", Amount, 0, true, 2)
                        .getJSONArray("ResponseData").getJSONObject(0).getDouble("NewAmount");
                }
                // 台幣付款直接拿總額
                else {
                    amount = Amount;
                }

                // 用VISA, JCB, MASTER, AMX付款超過8000顯示訊息
                if (amount >= 8000 &&
                    (CardType == CreditCardType.VISA || CardType == CreditCardType.MASTER
                        || CardType == CreditCardType.JCB || CardType == CreditCardType.AMX)) {
                    MessageBox.show("", "For DFS promotion, this month has the maximum credit card limit of US$1,000. "
                        + "For more DFS information, please refer to the leaflet in the sales cart.", mContext, "Ok");
                }
                // 用其他付款超過10000顯示訊息
                else if (amount >= 10000) {
                    MessageBox.show("", "For DFS promotion, this month has the maximum credit card limit of US$1,000. "
                        + "For more DFS information, please refer to the leaflet in the sales cart.", mContext, "Ok");
                }
            }

            setUI(Mode, PayBy);

        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get pay info error", mContext, "Return");
        }
    }

    // 設定使用者顯示的各種資訊
    private static void setUI(int Mode, PaymentType PayBy) {
        try {
            StringBuilder err = new StringBuilder();
            f_card.cardData = new ArrayList<>();

            // 設定未付款金額進editText, 按鈕
            switch (payPack.NowPayMode) {
                case "PAY":
                    txtNotPayMoney.setText(Tools.getModiMoneyString(Math.abs(payPack.USDTotalUnpay)));
                    txtChangeMoney.setText("0");

                    if (isCUB) {
                        CUBBtnSetting(true);
                    } else {
                        // 刪除付款歷程後不會有LastPayCurrency, 預設美金
                        if (Mode == 1) {
                            f_cash.setEditText("USD");
                            f_card.setEditText("USD");
                        } else {
                            f_cash.setEditText(payPack.LastPayCurrency);
                            f_card.setEditText(payPack.LastPayCurrency);
                        }
                        f_coupon.txtCurrency.setText("");
                        f_coupon.txtAmount.setText("");
                        f_coupon.editCuponNo.setText("");

                        btnCash.setEnabled(true);
                        btnCard.setEnabled(true);
                        btnCupon.setEnabled(true);
                        btnChange.setEnabled(false);
                        btnPrint.setEnabled(false);
                        f_card.btnPay.setEnabled(true);
                        f_cash.btnPay.setEnabled(true);
                        f_coupon.btnPay.setEnabled(true);
                        f_change.btnChange.setEnabled(true);

                        if (Mode == 1) {
                            if (PayBy == PaymentType.Cash) {
                                btnCash.performClick();
                            } else if (PayBy == PaymentType.Card) {
                                btnCard.performClick();
                            } else if (PayBy == PaymentType.DC || PayBy == PaymentType.SC) {
                                btnCupon.performClick();
                            } else if (PayBy == PaymentType.Change) {
                                btnChange.performClick();
                            }
                        }
                        updateButtonColor(whichBtnIsClicked);
                    }
                    break;

                case "CHANGE":
                    txtNotPayMoney.setText("0");
//                    txtChangeMoney.setText( Tools.getModiMoneyString(Math.abs(payPack.USDTotalUnpay)) );
                    DBQuery.ShouldPayMoney payItem = DBQuery.getPayMoneyNow(new StringBuilder(),
                        VipSaleActivity._VipSaleTranscation.GetCurrencyMaxAmount("USD"));
                    if (payItem == null || payItem.Currency == null) {
                        MessageBox.show("", "Get pay info error", mActivity, "Return");
                        return;
                    }
                    txtChangeMoney.setText(Tools.getModiMoneyString(Math.abs((payItem.MaxPayAmount))));

                    f_cash.setEditText("USD");
                    f_card.setEditText("USD");

                    // 刪除付款歷程後不會有LastPayCurrency, 預設美金
                    if (Mode == 1) {
                        f_change.setEditText("USD");
                    } else {
                        // Change Mode的 LastPayAmount就是應找零金額
                        if (!payPack.LastPayCurrency.equals("")) {
                            f_change.setEditText(payPack.LastPayCurrency);
                        } else {
                            f_change.setEditText("USD");
                        }
                    }
                    f_coupon.txtCurrency.setText("");
                    f_coupon.txtAmount.setText("");
                    f_coupon.editCuponNo.setText("");

                    btnCash.setEnabled(false);
                    btnCard.setEnabled(false);
                    btnCupon.setEnabled(false);
                    btnChange.setEnabled(true);
                    btnPrint.setEnabled(false);
                    btnChange.performClick();
                    f_card.btnPay.setEnabled(true);
                    f_cash.btnPay.setEnabled(true);
                    f_coupon.btnPay.setEnabled(true);
                    f_change.btnChange.setEnabled(true);
                    updateButtonColor(whichBtnIsClicked);
                    break;

                case "BALANCE":
                    txtNotPayMoney.setText("0");
                    txtChangeMoney.setText("0");
                    f_change.setEditText("USD");
                    f_cash.setEditText("USD");
                    f_card.setEditText("USD");
                    f_coupon.txtCurrency.setText("");
                    f_coupon.txtAmount.setText("");
                    f_coupon.editCuponNo.setText("");

                    btnCash.setEnabled(false);
                    btnCard.setEnabled(false);
                    btnCupon.setEnabled(false);
                    btnChange.setEnabled(false);
                    btnPrint.setEnabled(true);
                    f_card.btnPay.setEnabled(false);
                    f_cash.btnPay.setEnabled(false);
                    f_coupon.btnPay.setEnabled(false);
                    f_change.btnChange.setEnabled(false);
                    updateButtonColor(4);
                    break;
            }

            // 塞入adapter
            // String CurDvr, String PayType, Double Amount, Double USDAmount
            adapter.clear();
            for (PaymentList pay : payPack.payLisy) {
                adapter.addItem(pay.Currency, pay.PayBy, pay.Amount, pay.USDAmount, pay.CouponNo);
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.show("", "Get pay info error", mContext, "Return");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //折扣資訊
        if ((requestCode == DISCOUNT_INFO) && resultCode == RESULT_OK) {
            try {
                HashMap<String, String> discountHashmap = (HashMap)data.getExtras().get("discountHashMap");
                if (discountHashmap != null && discountHashmap.size() > 0) {
                    for (String key : discountHashmap.keySet()) {
                        JSONObject json = VipSaleActivity._VipSaleTranscation.AddUplimitType(key, discountHashmap.get(key));
                        String retCode = json.getString("ReturnCode");
                        if (!retCode.equals("0")) {
                            MessageBox.show("", json.getString("ReturnMessage"), mContext, "Return");
                            return;
                        }
                    }
                }
                // 有CUB身分
                if (discountHashmap.get("CUB") != null) {
                    CUBBtnSetting(true);
                }

                if (f_card.cardData.size() == 5) {
                    getDBData(
                        // String Currency, PaymentType PayBy, double Amount, String CouponNo
                        f_card.spinnerCurrency.getSelectedItem().toString(),
                        PaymentType.Card,
                        Double.parseDouble(f_card.editAmount.getText().toString()),
                        // cardData: 卡種, 卡號, 持卡人姓名, 到期日, 安全碼
                        // String CardNo, String CardName, String CardDate, CreditCardType CardType
                        null, f_card.cardData.get(1), f_card.cardData.get(2), f_card.cardData.get(3), f_card.CardType, 0);
                }

            } catch (Exception e) {
                e.printStackTrace();
                TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                _TSQL.WriteLog(FlightData.SecSeq, "System", "VipPayActivity", "onActivityResult", e.getMessage());
                MessageBox.show("", "Get discount identity error", mContext, "Return");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    // 儲存Pay資訊
    private void savePayInfo() {
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        try {

            if (AuthorizeFlag) {
                VipSaleActivity._VipSaleTranscation.AuthuticationFlag = true;
            }

            if (!VipSaleActivity._VipSaleTranscation.SaveSalesInfo().getString("ReturnCode").equals("0")) {
                mloadingDialog.dismiss();
                MessageBox.show("", "Save pay info error", mActivity, "Return");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            mloadingDialog.dismiss();
            MessageBox.show("", "Save pay info error", mActivity, "Return");
            return;
        }

        boolean isCredit = false;
        // 判斷有沒有信用卡付款:
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).getPayType().equals("Card")) {
                isCredit = true;
                break;
            }
        }
        printData(isCredit);
    }


    private static class PrinterHandler extends Handler {
        private WeakReference<VipPayActivity> weakActivity;

        PrinterHandler(VipPayActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            VipPayActivity handlerActivity = weakActivity.get();

            handlerActivity.mloadingDialog.dismiss();
            switch (msg.what) {
                case 1: // 沒紙 (信用卡)
                    if (MessageBox.show("", "No paper, reprint?", handlerActivity, "Yes", "No"))
                        handlerActivity.printData(true);
                    else handlerActivity.doPrintFinal();
                    break;

                case 2:
                    if (MessageBox.show("", "No paper, reprint?", handlerActivity, "Yes", "No"))
                        handlerActivity.printData(false);
                    else handlerActivity.doPrintFinal();
                    break;

                case 3://Print error (信用卡)
                    if (MessageBox.show("", "Print error, retry?", handlerActivity, "Yes", "No"))
                        handlerActivity.printData(true);
                    else handlerActivity.doPrintFinal();
                    break;

                case 4:
                    if (MessageBox.show("", "Print error, retry?", handlerActivity, "Yes", "No"))
                        handlerActivity.printData(false);
                    else handlerActivity.doPrintFinal();
                    break;

                case 5: // 印收據
                    if (MessageBox.show("", "Print receipt", handlerActivity, "Ok")) handlerActivity.printData(false);
                    break;

                case 6: //成功
                    handlerActivity.doPrintFinal();
                    break;
            }
        }
    }

    private void doPrintFinal() {
        if (MessageBox.show("", "Success", mContext, "Ok")) {

            PreorderInfoPack preorderitempack = DBQuery.getPRVPCanSaleRefund(mContext,
                new StringBuilder(), FlightData.SecSeq, null, new String[]{"VS"}, "N");

            //回到VIP Sale
            if (ActivityManager.isActivityInStock("VipSaleActivity"))
                ActivityManager.removeActivity("VipSaleActivity");

            if (preorderitempack == null || preorderitempack.info == null) {
                if (MessageBox.show("", "No VIP sale list", mContext, "Ok")) {
                    ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                }
            } else {
                Intent intent = new Intent(mActivity, VipSaleActivity.class);
                mActivity.startActivity(intent);
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            }
        }
    }

    private void printData(final boolean isCredit) {
        mloadingDialog.dismiss();
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        new Thread() {
            public void run() {
                //列印收據
                PrintAir printer = new PrintAir(mActivity, Integer.valueOf(FlightData.SecSeq));
                try {

                    // 有信用卡: 先印簽單再印收據
                    if (isCredit) {
                        // 先印簽單
                        if (printer.printSale(vipheader.ReceiptNo, 1, vipheader.PreorderNo) == -1) {
                            printerHandler.sendMessage(Tools.createMsg(1));
                        } else {
                            printerHandler.sendMessage(Tools.createMsg(5));
                        }
                    } else {
                        if (printer.printSale(vipheader.ReceiptNo, 0, vipheader.PreorderNo) == -1) {
                            printerHandler.sendMessage(Tools.createMsg(2));
                        } else {
                            printerHandler.sendMessage(Tools.createMsg(6));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                    _TSQL.WriteLog(FlightData.SecSeq,
                        "System", "VipPayActivity", "printSale", e.getMessage());
                    if (isCredit) {
                        printerHandler.sendMessage(Tools.createMsg(3));
                    } else {
                        printerHandler.sendMessage(Tools.createMsg(4));
                    }
                }
            }
        }.start();
    }

    // 監聽鍵盤彈出時收起付款歷程
    public void setListenerToRootView() {
        final View activityRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
            if (heightDiff > 100) { // 99% of the time the height diff will be due to a keyboard.
//                    Toast.makeText(getApplicationContext(), "Gotcha!!! softKeyboardup", 0).show();
                isDiscountVisible = false;
                discountListView.setVisibility(View.GONE);
                arrow.setImageResource(R.drawable.icon_arrow_down_white);
            }
        });
    }


    //滑動刪除Button onClick
    private ItemListPayAdapter.ItemSwipeDeleteListener itemSwipeListener
        = new ItemListPayAdapter.ItemSwipeDeleteListener() {
        @Override
        public void swipeDeleteListener(int position) {
            try {
                if (MessageBox.show("", "Delete this pay?", mContext, "Yes", "No")) {
                    deletePaymentList(position);
                }

            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Delete error", mContext, "Return");
            }
        }
    };

    public void deletePaymentList(int position){
        ItemPayInfo item = adapter.getItem(position);
        PaymentType _type = null;

        for (PaymentType type : PaymentType.values()) {
            if (type.toString().equals(item.getPayType())) {
                _type = type;
            }
        }
        // String Currency, PaymentType PayBy, double Amount, String CouponNo,
        // String CardNo, String CardName, String CardDate, CreditCardType CardType
        getDBData(item.getCurDvr(), _type, item.getAmount(), item.getCouponNo(), null, null, null, null, 1);

        discountListView.hiddenRight(discountListView.mPreItemView);
    }


    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            soundpool.play(soundid, 1, 1, 0, 0, 1);
            mVibrator.vibrate(100);

            byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
            int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
            byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte)0);
            android.util.Log.i("debug", "----codetype--" + temp);
            barcodeStr = new String(barcode, 0, barcodelen);

            //如果是Coupon再作search
            if (f_coupon != null && f_coupon.isVisible()) {
                f_coupon.searchCoupon(barcodeStr);
            }
            barcodeStr = null;
        }

    };

    // Scan init
    private void initScan() {

        mScanManager = new ScanManager();
        mScanManager.openScanner();

        mScanManager.switchOutputMode(0);
        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
        soundid = soundpool.load("/etc/Scan_new.ogg", 1);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String message) {
        try {
            int position = adapter.getCount()-1;
            deletePaymentList(position);
        }catch (Exception e){
            e.printStackTrace();
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        initScan();

        IntentFilter filter = new IntentFilter();
        int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
        String[] value_buf = mScanManager.getParameterString(idbuf);
        if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
            filter.addAction(value_buf[0]);
        } else {
            filter.addAction(SCAN_ACTION);
        }

        registerReceiver(mScanReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScanManager != null) {
            mScanManager.stopDecode();
        }
        unregisterReceiver(mScanReceiver);
    }

    //點空白處自動隱藏鍵盤
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View view = getCurrentFocus();
                Tools.hideSoftKeyboard(ev, view, this);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    //鎖返回鍵和menu
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean result;

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                result = false;
                break;
            case KeyEvent.KEYCODE_MENU:
                result = false;
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                result = true;
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                result = true;
                break;
            default:
                result = super.onKeyDown(keyCode, event);
                break;
        }

        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
