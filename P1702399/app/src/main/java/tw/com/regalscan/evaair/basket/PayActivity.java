package tw.com.regalscan.evaair.basket;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.Service;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jess.arms.utils.ArmsUtils;
import com.regalscan.sqlitelibrary.TSQL;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import aero.panasonic.inflight.crew.services.ordermanagement.v1.CrewOrder;
import aero.panasonic.inflight.services.data.ifemessage.MessageEvent;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;
import tw.com.regalscan.R;
import tw.com.regalscan.activities.DiscountCheckActivity;
import tw.com.regalscan.adapters.ItemListPayAdapter;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.component.SwipeListView;
import tw.com.regalscan.customClass.ItemInfo;
import tw.com.regalscan.customClass.ItemPayInfo;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db.PaymentItem;
import tw.com.regalscan.db.PublicFunctions;
import tw.com.regalscan.db.Transaction.CreditCardType;
import tw.com.regalscan.db.Transaction.PaymentType;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.db02.DBQuery.PaymentList;
import tw.com.regalscan.db02.DBQuery.ShouldPayMoney;
import tw.com.regalscan.evaair.ife.CrewCartActivity;
import tw.com.regalscan.evaair.ife.IFEActivity01;
import tw.com.regalscan.evaair.ife.IFEFunction;
import tw.com.regalscan.evaair.ife.NCCCAuthorize;
import tw.com.regalscan.evaair.ife.OnlineBasketActivity;
import tw.com.regalscan.evaair.ife.OrderDetailActivity;
import tw.com.regalscan.evaair.ife.OrderListActivity;
import tw.com.regalscan.evaair.ife.ProcessingOrderListActivity;
import tw.com.regalscan.evaair.ife.model.entity.AuthorizeModel;
import tw.com.regalscan.utils.Constant;
import tw.com.regalscan.utils.Cursor;
import tw.com.regalscan.utils.PrintAir;
import tw.com.regalscan.utils.Tools;

public class PayActivity extends AppCompatActivity {

    // 五個頁面共用PayActivity:
    // Basket, OrderDetail, OrderEdit, ProcessingOrderDetail, ProcessingOrderEdit
    public static String fromWhere;

    //Scan
    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;//default action
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private SoundPool soundpool = null;
    private int soundid;

    //付款歷程
    public static ItemListPayAdapter adapter;
    private SwipeListView discountListView;

    private RelativeLayout rowText01;
    private Toolbar toolbar;
    private static Button btnReturn, btnCash, btnCard, btnCupon;
    private static Button btnChange, btnPrint;
    public static Context mContext;
    public static Activity mActivity;
    private boolean isDiscountVisible = false;
    private ImageView arrow;
    private FragmentManager fm = getFragmentManager();
    private static FragmentPayCard f_card = new FragmentPayCard();
    private static FragmentPayCash f_cash = new FragmentPayCash();
    private static FragmentPayChange f_change = new FragmentPayChange();
    private static FragmentPayCoupon f_coupon = new FragmentPayCoupon();

    //應付款總額
    public static TextView txtShouldPayMoney, txtNotPayMoney, txtChangeMoney;
    public static DBQuery.BasketItemPack basketItemPack;
    public static DBQuery.PaymentModePack payPack;
    public static InputMethodManager imm;
    private PrintAir printer;

    // 現在顯示哪個頁面
    private static int whichBtnIsClicked = -1;
    // 0= cash, 1= card, 2= coupon, 3= change, 4= balance

    public static String currentCurrency = "";

    private ProgressDialog mloadingDialog;

    public static final int DISCOUNT_INFO = 600;

    public String LAGRemarkMsg = "";

    public static boolean isCUB = false;

    private CrewOrder mCrewOrder;

    private IFEFunction mIFEFunction;

    private NCCCAuthorize mNCCCAuthorize;

    private boolean AuthorizeFlag = false;

    private boolean isCompleteOrder = true;

    private TSQL mTSQL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_01);
        EventBus.getDefault().register(this);

        //整個購物車item
        Bundle argument = getIntent().getExtras();
        if (argument != null) {
            String itemString = argument.getString("BasketItemPack");
            fromWhere = argument.getString("fromWhere");
            Gson gson = new Gson();
            basketItemPack = gson.fromJson(itemString, DBQuery.BasketItemPack.class);
            currentCurrency = argument.getString("Currency");
            LAGRemarkMsg = argument.getString("RemarkMsg");

            if (argument.getParcelable("CrewOrder") != null) {
                mCrewOrder = argument.getParcelable("CrewOrder");
            }

            init();

        } else {
            MessageBox.show("", "Get pay info error", mContext, "Return");
            finish();
        }
    }

    private void init() {
        mContext = this;
        mActivity = this;
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        mIFEFunction = new IFEFunction(mContext);
        mNCCCAuthorize = new NCCCAuthorize(mContext);
        mTSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            try {

                final Intent[] intent = new Intent[1];
                final Bundle[] bundle = new Bundle[1];

                switch (fromWhere) {
                    case "BasketActivity":
                        BasketActivity.getBasketTransaction().ClearPaymentList();
                        ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                        finish();
                        break;
                    case "OrderDetailActivity":
                    case "ProcessingOrderDetailActivity":
                        OrderDetailActivity._Transaction.ClearPaymentList();

                        intent[0] = new Intent(this, OrderDetailActivity.class);
                        bundle[0] = new Bundle();
                        bundle[0].putParcelable("CrewOrder", mCrewOrder);
                        if (fromWhere.equals("ProcessingOrderDetailActivity")) {
                            bundle[0].putString("fromWhere", "dealInProgress");
                        } else {
                            bundle[0].putString("fromWhere", "PayActivity");
                        }
                        intent[0].putExtras(bundle[0]);
                        this.startActivity(intent[0]);

                        ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                        finish();
                        break;
                    case "CrewCartActivity":
                        CrewCartActivity.s_Transaction.ClearPaymentList();
                        ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                        finish();
                        break;
                    case "OnlineBasketActivity":
                    case "OrderDetail":
                    case "ProcessingOrder":
                        if (mCrewOrder != null) {
                            mIFEFunction.convertToCart(mCrewOrder)
                                    .subscribeOn(Schedulers.io())
                                    .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(ifeReturnData -> {
                                        if (ifeReturnData.isSuccess()) {
                                            Cursor.Normal();
                                            OnlineBasketActivity.getBasketTransaction().ClearPaymentList();
                                            intent[0] = new Intent(PayActivity.this, OnlineBasketActivity.class);
                                            bundle[0] = new Bundle();

                                            switch (fromWhere) {
                                                case "OrderDetail":
                                                    bundle[0].putString("fromWhere", "OrderDetail");
                                                    break;
                                                case "ProcessingOrder":
                                                    bundle[0].putString("fromWhere", "ProcessingOrder");
                                                    break;
                                                default:
                                                    bundle[0].putString("fromWhere", "PayActivity");
                                                    break;
                                            }

                                            bundle[0].putString("basketInfo", (OnlineBasketActivity.getBasketTransaction().GetBasketInfo()).toString());

                                            bundle[0].putString("SeatNo", basketItemPack.SeatNo);

//                                            bundle[0].putString("orderNo", mCrewOrder != null ? mCrewOrder.getOrderId() : "");

                                            if (mCrewOrder != null) {
                                                bundle[0].putParcelable("CrewOrder", mCrewOrder);
                                            }
                                            intent[0].putExtras(bundle[0]);

                                            startActivity(intent[0]);

                                            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                                            finish();
                                        } else {
                                            Cursor.Normal();
                                            if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                                                if (MessageBox.show("", getString(R.string.IFE_Offline_Processing_Order_List), mContext, "Ok")) {
                                                    ArmsUtils.startActivity(IFEActivity01.class);
                                                    finish();
                                                }
                                            }
                                        }
                                    });
                        } else {
                            OnlineBasketActivity.getBasketTransaction().ClearPaymentList();
                            intent[0] = new Intent(PayActivity.this, OnlineBasketActivity.class);
                            bundle[0] = new Bundle();

                            switch (fromWhere) {
                                case "OrderDetail":
                                    bundle[0].putString("fromWhere", "OrderDetail");
                                    break;
                                case "ProcessingOrder":
                                    bundle[0].putString("fromWhere", "ProcessingOrder");
                                    break;
                                default:
                                    bundle[0].putString("fromWhere", "PayActivity");
                                    break;
                            }
                            bundle[0].putString("SeatNo", basketItemPack.SeatNo);
                            intent[0].putExtras(bundle[0]);
                            startActivity(intent[0]);

                            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                            finish();
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Clear pay info error", mContext, "Return");
            }
        });

        try {
            StringBuilder sb = new StringBuilder();

            switch (fromWhere) {
                case "BasketActivity":
                    payPack = DBQuery.getPayMode(sb, BasketActivity.getBasketTransaction().GetPaymentMode());
                    break;
                case "OrderEditActivity":
                case "OrderDetailActivity":
                    payPack = DBQuery.getPayMode(sb, OrderDetailActivity._Transaction.GetPaymentMode());
                    break;
                case "ProcessingOrderEditActivity":
                case "ProcessingOrderDetailActivity":
                    payPack = DBQuery.getPayMode(sb, OrderDetailActivity._Transaction.GetPaymentMode());
                    break;
                case "CrewCartActivity":
                    payPack = DBQuery.getPayMode(sb, CrewCartActivity.s_Transaction.GetPaymentMode());
                    break;
                case "OnlineBasketActivity":
                case "OrderDetail":
                case "ProcessingOrder":
                    payPack = DBQuery.getPayMode(sb, OnlineBasketActivity.getBasketTransaction().GetPaymentMode());
                    break;
            }

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
            // set ListView的visible，和換箭頭icon
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
        CUBBtnSetting(basketItemPack.UpperLimitType.equals("CUB") || basketItemPack.DiscountType.equals("CUB"));

        btnPrint = findViewById(R.id.btnPrint);
        btnPrint.setOnClickListener(view -> {
            //當待付款 = 0且沒有應找零金額時，可以點選右下角Print按鈕進行結帳，並列印收據
            if (payPack.NowPayMode.equals("PAY")) {
                MessageBox.show("", "Should pay", mContext, "Return");
                return;
            }

            if (payPack.NowPayMode.equals("CHANGE")) {
                MessageBox.show("", "Should change", mContext, "Return");
                return;
            }

            if (FlightData.OnlineAuthorize) {
                onlineAuthorize();
            } else if (FlightData.IFEConnectionStatus && !FlightData.OnlineAuthorize) {
                completeOrder();
            } else {
                savePayInfo();
            }

        });
        btnPrint.setEnabled(false);

        setListenerToRootView();

        // 設定ToolBar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }

    private void onlineAuthorize() {

        AuthorizeModel authorizeModel = null;

        switch (fromWhere) {
            case "BasketActivity":
                for (PaymentItem paymentItem : BasketActivity.getBasketTransaction().getPaymentList()) {
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

                        authorizeModel.setRECIPT_NO(Integer.parseInt(BasketActivity.getBasketTransaction().ReceiptNo));
                        authorizeModel.setCurrency(paymentItem.Currency);

                        authorizeModel.setTWD_AMT(paymentItem.Currency.equals("TWD") ? (int) paymentItem.Amount : 0);
                        authorizeModel.setUSD_AMT(paymentItem.Currency.equals("USD") ? (int) paymentItem.Amount : 0);

                        authorizeModel.setUG_MARK("N");
                        authorizeModel.setDEPT_FLT_NO(FlightData.FlightNo);
                        authorizeModel.setVIP_NO(BasketActivity.getBasketTransaction().DiscountNo);
                        authorizeModel.setVIP_TYPE(BasketActivity.getBasketTransaction().DiscountType);
                        authorizeModel.setORDER_NO("");
                        authorizeModel.setIFE_SEQ(mCrewOrder != null ? mCrewOrder.getOrderId() : "");
                        authorizeModel.setREAUTH_MARK("N");

                        break;
                    }
                }
                break;
            case "OrderEditActivity":
            case "OrderDetailActivity":
            case "ProcessingOrderDetailActivity":
            case "ProcessingOrderEditActivity":
                for (PaymentItem paymentItem : OrderDetailActivity._Transaction.getPaymentList()) {
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

                        authorizeModel.setRECIPT_NO(Integer.parseInt(OrderDetailActivity._Transaction.ReceiptNo));
                        authorizeModel.setCurrency(paymentItem.Currency);

                        authorizeModel.setTWD_AMT(paymentItem.Currency.equals("TWD") ? (int) paymentItem.Amount : 0);
                        authorizeModel.setUSD_AMT(paymentItem.Currency.equals("USD") ? (int) paymentItem.Amount : 0);

                        authorizeModel.setUG_MARK("N");
                        authorizeModel.setDEPT_FLT_NO(FlightData.FlightNo);
                        authorizeModel.setVIP_NO(OrderDetailActivity._Transaction.DiscountNo);
                        authorizeModel.setVIP_TYPE(OrderDetailActivity._Transaction.DiscountType);
                        authorizeModel.setORDER_NO("");
                        authorizeModel.setIFE_SEQ(mCrewOrder != null ? mCrewOrder.getOrderId() : "");
                        authorizeModel.setREAUTH_MARK("N");

                        break;
                    }
                }
                break;
            case "CrewCartActivity":
                for (PaymentItem paymentItem : CrewCartActivity.getBasketTransaction().getPaymentList()) {
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

                        authorizeModel.setRECIPT_NO(Integer.parseInt(CrewCartActivity.getBasketTransaction().ReceiptNo));
                        authorizeModel.setCurrency(paymentItem.Currency);

                        authorizeModel.setTWD_AMT(paymentItem.Currency.equals("TWD") ? (int) paymentItem.Amount : 0);
                        authorizeModel.setUSD_AMT(paymentItem.Currency.equals("USD") ? (int) paymentItem.Amount : 0);

                        authorizeModel.setUG_MARK("N");
                        authorizeModel.setDEPT_FLT_NO(FlightData.FlightNo);
                        authorizeModel.setVIP_NO(CrewCartActivity.getBasketTransaction().DiscountNo);
                        authorizeModel.setVIP_TYPE(CrewCartActivity.getBasketTransaction().DiscountType);
                        authorizeModel.setORDER_NO("");
                        authorizeModel.setIFE_SEQ(mCrewOrder != null ? mCrewOrder.getOrderId() : "");
                        authorizeModel.setREAUTH_MARK("N");

                        break;
                    }
                }
                break;
            case "OrderDetail":
            case "ProcessingOrder":
            case "OnlineBasketActivity":
                for (PaymentItem paymentItem : OnlineBasketActivity.getBasketTransaction().getPaymentList()) {
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

                        authorizeModel.setRECIPT_NO(Integer.parseInt(OnlineBasketActivity.getBasketTransaction().ReceiptNo));
                        authorizeModel.setCurrency(paymentItem.Currency);

                        authorizeModel.setTWD_AMT(paymentItem.Currency.equals("TWD") ? (int) paymentItem.Amount : 0);
                        authorizeModel.setUSD_AMT(paymentItem.Currency.equals("USD") ? (int) paymentItem.Amount : 0);

                        authorizeModel.setUG_MARK("N");
                        authorizeModel.setDEPT_FLT_NO(FlightData.FlightNo);
                        authorizeModel.setVIP_NO(OnlineBasketActivity.getBasketTransaction().DiscountNo);
                        authorizeModel.setVIP_TYPE(OnlineBasketActivity.getBasketTransaction().DiscountType);
                        authorizeModel.setORDER_NO("");
                        authorizeModel.setIFE_SEQ(mCrewOrder != null ? mCrewOrder.getOrderId() : "");
                        authorizeModel.setREAUTH_MARK("N");

                        break;
                    }
                }
                break;
        }

        if (authorizeModel == null) {
            if (FlightData.IFEConnectionStatus) {
                completeOrder();
            } else {
                savePayInfo();
            }
        } else {
            mNCCCAuthorize.SendRequestToNCCCGetAuthorize(authorizeModel, new NCCCAuthorize.AuthorizeReturn() {
                @Override
                public void success(AuthorizeModel authorizeModel) {
                    AuthorizeFlag = true;
                    // Remark有訊息的話, Show
                    if (FlightData.IFEConnectionStatus) {
                        completeOrder();
                    } else {
                        savePayInfo();
                    }
                }

                @Override
                public void failed(String errMsg) {
                    if (!errMsg.equals("") && MessageBox.show("", errMsg, mContext, "Ok")) {

                    } else {
                        if (FlightData.IFEConnectionStatus) {
                            completeOrder();
                        } else {
                            savePayInfo();
                        }
                    }
                }
            });
        }
    }

    private void completeOrder() {
        if (mCrewOrder != null) {
            mIFEFunction.completeOrder(mCrewOrder)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(ifeReturnData -> {
                        if (ifeReturnData.isSuccess()) {
                            savePayInfo();
                        } else {
                            isCompleteOrder = false;
                            savePayInfo();
                        }
                    });
        } else {
            savePayInfo();
        }
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
            isCUB = false;
            btnCash.performClick();
            whichBtnIsClicked = 0;
        }
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
                new Handler().postDelayed(dialog::dismiss, 500);
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
    }

    // 0= cash, 1= card, 2= coupon, 3= change, 4= balance
    // 5= cub強制只能刷cub
    private static void updateButtonColor(int whichIsClick) {

        switch (whichIsClick) {
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
                    switch (fromWhere) {
                        case "BasketActivity":
                            payPack = DBQuery.getPayMode(err,
                                    BasketActivity.getBasketTransaction().AddPaymentList(Currency, PayBy, Amount, CouponNo, CardNo, CardName, CardDate, CardType));
                            break;
                        case "OrderEditActivity":
                        case "OrderDetailActivity":
                            payPack = DBQuery.getPayMode(err,
                                    OrderDetailActivity._Transaction.AddPaymentList(Currency, PayBy, Amount, CouponNo, CardNo, CardName, CardDate, CardType));
                            break;
                        case "ProcessingOrderEditActivity":
                        case "ProcessingOrderDetailActivity":
                            payPack = DBQuery.getPayMode(err,
                                    OrderDetailActivity._Transaction.AddPaymentList(Currency, PayBy, Amount, CouponNo, CardNo, CardName, CardDate, CardType));
                            break;
                        case "CrewCartActivity":
                            payPack = DBQuery.getPayMode(err,
                                    CrewCartActivity.s_Transaction.AddPaymentList(Currency, PayBy, Amount, CouponNo, CardNo, CardName, CardDate, CardType));
                            break;
                        case "OnlineBasketActivity":
                        case "OrderDetail":
                        case "ProcessingOrder":
                            payPack = DBQuery.getPayMode(err,
                                    OnlineBasketActivity.getBasketTransaction().AddPaymentList(Currency, PayBy, Amount, CouponNo, CardNo, CardName, CardDate, CardType));
                            break;
                    }
                    break;
                case 1: //Delete
                    switch (fromWhere) {
                        case "BasketActivity":
                            // String Currency, PaymentType PayBy, String CouponNo
                            payPack = DBQuery.getPayMode(err, BasketActivity.getBasketTransaction().DeletePaymentList(Currency, PayBy, CouponNo));
                            break;
                        case "OrderEditActivity":
                        case "OrderDetailActivity":
                            payPack = DBQuery.getPayMode(err, OrderDetailActivity._Transaction.DeletePaymentList(Currency, PayBy, CouponNo));
                            break;
                        case "ProcessingOrderEditActivity":
                        case "ProcessingOrderDetailActivity":
                            payPack = DBQuery.getPayMode(err, OrderDetailActivity._Transaction.DeletePaymentList(Currency, PayBy, CouponNo));
                            break;
                        case "CrewCartActivity":
                            payPack = DBQuery.getPayMode(err, CrewCartActivity.s_Transaction.DeletePaymentList(Currency, PayBy, CouponNo));
                            break;
                        case "OnlineBasketActivity":
                        case "OrderDetail":
                        case "ProcessingOrder":
                            payPack = DBQuery.getPayMode(err, OnlineBasketActivity.getBasketTransaction().DeletePaymentList(Currency, PayBy, CouponNo));
                            break;
                    }
                    break;
                case 2: //Change
                    switch (fromWhere) {
                        case "BasketActivity":
                            payPack = DBQuery.getPayMode(err, BasketActivity.getBasketTransaction().AddChangeList(Currency, Amount));
                            break;
                        case "OrderEditActivity":
                        case "OrderDetailActivity":
                            payPack = DBQuery.getPayMode(err, OrderDetailActivity._Transaction.AddChangeList(Currency, Amount));
                            break;
                        case "ProcessingOrderEditActivity":
                        case "ProcessingOrderDetailActivity":
                            payPack = DBQuery.getPayMode(err, OrderDetailActivity._Transaction.AddChangeList(Currency, Amount));
                            break;
                        case "CrewCartActivity":
                            payPack = DBQuery.getPayMode(err, CrewCartActivity.s_Transaction.AddChangeList(Currency, Amount));
                            break;
                        case "OnlineBasketActivity":
                        case "OrderDetail":
                        case "ProcessingOrder":
                            payPack = DBQuery.getPayMode(err, OnlineBasketActivity.getBasketTransaction().AddChangeList(Currency, Amount));
                            break;
                    }
                    break;
            }

            // 1. 若已放大身分, 又超過刷卡限額, 則只顯示"此張卡使用幾次, 餘額多少"
            // 2. 顯示over payment:

            String upperLimitType = "";
            if (fromWhere.equals("BasketActivity")) {
                upperLimitType = BasketActivity.getBasketTransaction().getUpperLimitType();
            }

//            switch (fromWhere) {
//                case "BasketActivity":
//                    upperLimitType = BasketActivity.getBasketTransaction().getUpperLimitType();
//                    break;
//                case "OrderEditActivity":
//                case "OrderDetailActivity":
//                    upperLimitType = OrderDetailActivity._Transaction.getUpperLimitType();
//                    break;
//                case "ProcessingOrderEditActivity":
//                case "ProcessingOrderDetailActivity":
//                    upperLimitType = OrderDetailActivity._Transaction.getUpperLimitType();
//                    break;
//                case "CrewCartActivity":
//                    upperLimitType = CrewCartActivity.s_Transaction.getUpperLimitType();
//                    break;
//                case "OnlineBasketActivity":
//                case "OrderDetail":
//                case "ProcessingOrder":
//                    upperLimitType = OnlineBasketActivity.getBasketTransaction().getUpperLimitType();
//                    break;
//            }

            if (payPack == null) {
                // 如果為新增付款, 且errMsg內有 "This card had been used(ry)", 超過刷卡限額, 詢問有無放大身分
                if (Mode == 0
                        && err.toString().length() > 23
                        && err.toString().substring(0, 23).equals("This card had been used")
                        && upperLimitType.equals("")
                        && MessageBox.show("", "Is the PAX CD/CG/EC/EP/AEG/CUB card holder or STAFF?", mActivity, "Yes", "No")) {
                    // 彈跳小視窗
                    Bundle argument = new Bundle();
                    argument.putString("fromWhere", "FragmentPayCard");
                    Intent intent = new Intent();
                    intent.putExtras(argument);
                    intent.putStringArrayListExtra("discountArrayList", new ArrayList<>());
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
            if (Mode == 0 && PaymentType.Card == PayBy && basketItemPack.UpperLimitType.equals("")) {
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
                                || CardType == CreditCardType.JCB || CardType == CreditCardType.AMX || CardType == CreditCardType.CUP)) {
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
    // Mode 1= 刪除付款歷程
    private static void setUI(int Mode, PaymentType PayBy) {
        try {
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
                            f_cash.setEditText("USD");;
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

                    ShouldPayMoney payItem = new ShouldPayMoney();
                    switch (PayActivity.fromWhere) {
                        case "BasketActivity":
                            payItem = DBQuery.getPayMoneyNow(new StringBuilder(), BasketActivity.getBasketTransaction().GetCurrencyMaxAmount("USD"));
                            break;
                        case "OrderEditActivity":
                        case "OrderDetailActivity":
                            payItem = DBQuery.getPayMoneyNow(new StringBuilder(), OrderDetailActivity._Transaction.GetCurrencyMaxAmount("USD"));
                            break;
                        case "ProcessingOrderEditActivity":
                        case "ProcessingOrderDetailActivity":
                            payItem = DBQuery.getPayMoneyNow(new StringBuilder(), OrderDetailActivity._Transaction.GetCurrencyMaxAmount("USD"));
                            break;
                        case "CrewCartActivity":
                            payItem = DBQuery.getPayMoneyNow(new StringBuilder(), CrewCartActivity.s_Transaction.GetCurrencyMaxAmount("USD"));
                            break;
                        case "OnlineBasketActivity":
                        case "OrderDetail":
                        case "ProcessingOrder":
                            payItem = DBQuery.getPayMoneyNow(new StringBuilder(), OnlineBasketActivity.getBasketTransaction().GetCurrencyMaxAmount("USD"));
                            break;
                    }

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

                HashMap<String, String> discountHashmap = (HashMap) data.getExtras().get("discountHashMap");
                if (discountHashmap != null && discountHashmap.size() > 0) {
                    for (String key : discountHashmap.keySet()) {

                        JSONObject json = null;

                        switch (fromWhere) {
                            case "BasketActivity":
                                json = BasketActivity.getBasketTransaction().AddUplimitType(key, discountHashmap.get(key));
                                break;
                            case "OrderEditActivity":
                            case "OrderDetailActivity":
                                json = OrderDetailActivity._Transaction.AddUplimitType(key, discountHashmap.get(key));
                                break;
                            case "ProcessingOrderEditActivity":
                            case "ProcessingOrderDetailActivity":
                                json = OrderDetailActivity._Transaction.AddUplimitType(key, discountHashmap.get(key));
                                break;
                            case "CrewCartActivity":
                                json = CrewCartActivity.s_Transaction.AddUplimitType(key, discountHashmap.get(key));
                                break;
                            case "OnlineBasketActivity":
                            case "OrderDetail":
                            case "ProcessingOrder":
                                json = OnlineBasketActivity.getBasketTransaction().AddUplimitType(key, discountHashmap.get(key));
                                break;
                        }

                        String retCode = json != null ? json.getString("ReturnCode") : null;
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
                _TSQL.WriteLog(FlightData.SecSeq, "System", "PayActivity", "onActivityResult", e.getMessage());
                MessageBox.show("", "Get discount identity error", mContext, "Return");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    // 儲存Pay資訊
    public void savePayInfo() {

        if (!LAGRemarkMsg.equals("")) {
            MessageBox.show("", LAGRemarkMsg, mContext, "Ok");
        }

        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        try {
            boolean flag = true;
            switch (fromWhere) {
                case "BasketActivity":
                    for (String key : BasketActivity.getBasketGiftList().keySet()) {
                        ItemInfo item = BasketActivity.getBasketGiftList().get(key);
                        if (item.getGiftScan() > 0) {
                            BasketActivity.getBasketTransaction().SetGiftScanQty(item.getItemCode(), item.getGiftScan());
                        }
                    }

                    if (AuthorizeFlag) {
                        BasketActivity.getBasketTransaction().AuthuticationFlag = true;
                    }

                    if (!BasketActivity.getBasketTransaction().SaveSalesInfo().getString("ReturnCode").equals("0")) {
                        flag = false;
                    }
                    break;
                case "ProcessingOrderDetailActivity":
                case "OrderDetailActivity":
                    for (String key : OrderDetailActivity.giftList.keySet()) {
                        ItemInfo item = OrderDetailActivity.giftList.get(key);
                        if (item.getGiftScan() > 0) {
                            OrderDetailActivity._Transaction.SetGiftScanQty(item.getItemCode(), item.getGiftScan());
                        }
                    }

                    OrderDetailActivity._Transaction.setIFEOrderNo(mCrewOrder.getOrderId());

                    if (AuthorizeFlag) {
                        OrderDetailActivity._Transaction.AuthuticationFlag = true;
                    }

                    if (!OrderDetailActivity._Transaction.SaveSalesInfo().getString("ReturnCode").equals("0")) {
                        flag = false;
                    }
                    break;
                case "CrewCartActivity":
                    for (String key : CrewCartActivity.getBasketGiftList().keySet()) {
                        ItemInfo item = CrewCartActivity.getBasketGiftList().get(key);
                        if (item.getGiftScan() > 0) {
                            CrewCartActivity.getBasketTransaction().SetGiftScanQty(item.getItemCode(), item.getGiftScan());
                        }
                    }

                    CrewCartActivity.getBasketTransaction().setIFEOrderNo(mCrewOrder.getOrderId());

                    if (AuthorizeFlag) {
                        CrewCartActivity.getBasketTransaction().AuthuticationFlag = true;
                    }

                    if (!CrewCartActivity.getBasketTransaction().SaveSalesInfo().getString("ReturnCode").equals("0")) {
                        flag = false;
                    }
                    break;
                case "OrderDetail":
                case "ProcessingOrder":
                case "OnlineBasketActivity":
                    for (String key : OnlineBasketActivity.getBasketGiftList().keySet()) {
                        ItemInfo item = OnlineBasketActivity.getBasketGiftList().get(key);
                        if (item.getGiftScan() > 0) {
                            OnlineBasketActivity.getBasketTransaction().SetGiftScanQty(item.getItemCode(), item.getGiftScan());
                        }
                    }

                    OnlineBasketActivity.getBasketTransaction().setIFEOrderNo(mCrewOrder.getOrderId());

                    if (AuthorizeFlag) {
                        OnlineBasketActivity.getBasketTransaction().AuthuticationFlag = true;
                    }

                    if (!OnlineBasketActivity.getBasketTransaction().SaveSalesInfo().getString("ReturnCode").equals("0")) {
                        flag = false;
                    }
                    break;
            }

            if (!flag) {
                PayActivity.this.runOnUiThread(() -> {
                    mloadingDialog.dismiss();
                    MessageBox.show("", "Save pay info error", mActivity, "Return");
                });
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            TSQL tsql = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
            tsql.WriteLog(FlightData.SecSeq, "Exception", "PayActivity", "", e.getMessage());
            PayActivity.this.runOnUiThread(() -> {
                mloadingDialog.dismiss();
                MessageBox.show("", "Save pay info error", mActivity, "Return");
            });
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

        private WeakReference<PayActivity> weakActivity;

        PrinterHandler(PayActivity a) {
            weakActivity = new WeakReference<>(a);
        }

        @Override
        public void handleMessage(Message msg) {
            PayActivity handlerActivity = weakActivity.get();

            handlerActivity.mloadingDialog.dismiss();
            switch (msg.what) {
                case 1: // 沒紙(信用卡)
                    if (MessageBox.show("", "No paper, reprint?", handlerActivity, "Yes", "No")) {
                        handlerActivity.printData(true);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 2: // 沒紙(現金)
                    if (MessageBox.show("", "No paper, reprint?", handlerActivity, "Yes", "No")) {
                        handlerActivity.printData(false);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 3: //Print error (信用卡)
                    if (MessageBox.show("", "Print error, retry?", handlerActivity, "Yes", "No")) {
                        handlerActivity.printData(true);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 4: //Print error (現金)
                    if (MessageBox.show("", "Print error, retry?", handlerActivity, "Yes", "No")) {
                        handlerActivity.printData(false);
                    } else {
                        handlerActivity.doPrintFinal();
                    }
                    break;

                case 5: // 印收據
                    if (MessageBox.show("", "Print receipt", handlerActivity, "Ok")) {
                        handlerActivity.printData(false);
                    }
                    break;

                case 6: //成功
                    handlerActivity.doPrintFinal();
                    break;
            }
        }
    }

    private void doPrintFinal() {
        if (MessageBox.show("", "Success", mContext, "Ok")) {

            if (mCrewOrder != null && !isCompleteOrder) {
                MessageBox.show("", getString(R.string.IFE_Offline_CompleteOrder_Fail) + mCrewOrder.getSeat(), mContext, "Ok");
            }

            switch (fromWhere) {
                case "BasketActivity":
                    //回到Basket
                    if (ActivityManager.isActivityInStock("BasketActivity")) {
                        ActivityManager.removeActivity("BasketActivity");
                    }
                    Intent intent = new Intent(mActivity, BasketActivity.class);
                    mActivity.startActivity(intent);
                    break;
                case "OrderDetailActivity":
                    ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                    if (ActivityManager.isActivityInStock("OnlineBasketActivity")) {
                        ActivityManager.removeActivity("OnlineBasketActivity");
                    }
                    ArmsUtils.startActivity(OrderListActivity.class);
                    finish();
                    break;
                case "ProcessingOrderDetailActivity":
                    ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
                    if (ActivityManager.isActivityInStock("ProcessingOrderDetailActivity")) {
                        ActivityManager.removeActivity("ProcessingOrderDetailActivity");
                    }
                    ArmsUtils.startActivity(ProcessingOrderListActivity.class);
                    finish();
                    break;
                case "CrewCartActivity":
                    if (ActivityManager.isActivityInStock("CrewCartActivity")) {
                        ActivityManager.removeActivity("CrewCartActivity");
                    }
                    intent = new Intent(mActivity, CrewCartActivity.class);
                    mActivity.startActivity(intent);
                    finish();
                    break;

                case "ProcessingOrder":
                    if (ActivityManager.isActivityInStock("OnlineBasketActivity")) {
                        ActivityManager.removeActivity("OnlineBasketActivity");
                    }
                    ArmsUtils.startActivity(ProcessingOrderListActivity.class);
                    finish();
                    break;
                case "OrderDetail":
                    if (ActivityManager.isActivityInStock("OnlineBasketActivity")) {
                        ActivityManager.removeActivity("OnlineBasketActivity");
                    }
                    ArmsUtils.startActivity(OrderListActivity.class);
                    finish();
                    break;
                case "OnlineBasketActivity":
                    if (ActivityManager.isActivityInStock("OnlineBasketActivity")) {
                        ActivityManager.removeActivity("OnlineBasketActivity");
                    }
                    intent = new Intent(mActivity, OnlineBasketActivity.class);
                    mActivity.startActivity(intent);
                    finish();
                    break;
            }
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
        }
    }

    private void printData(final boolean isCredit) {
        Cursor.Normal();
        mloadingDialog = ProgressDialog.show(mContext, "", "Processing...", true, false);
        Handler printerHandler = new PrinterHandler(this);
        new Thread() {
            public void run() {//列印收據
                printer = new PrintAir(mActivity, Integer.valueOf(FlightData.SecSeq));
                try {
                    if (isCredit) {
                        // String ReceiptNo, int flag, String PreOrderNo, String VipOrderNo
                        if (printer.printSale(basketItemPack.ReceiptNo, 1, "") == -1) {
                            printerHandler.sendMessage(Tools.createMsg(1));
                        } else {
                            printerHandler.sendMessage(Tools.createMsg(5));
                        }
                    } else {
                        if (printer.printSale(basketItemPack.ReceiptNo, 0, "") == -1) {
                            printerHandler.sendMessage(Tools.createMsg(2));
                        } else {
                            printerHandler.sendMessage(Tools.createMsg(6));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    TSQL _TSQL = TSQL.getINSTANCE(mContext, FlightData.SecSeq, "P17023");
                    _TSQL.WriteLog(FlightData.SecSeq, "System", "PayActivity", "printSale", e.getMessage());
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String message) {
        try {
            int position = adapter.getCount()-1;
            deletePaymentList(position);
        }catch (Exception e){
            e.printStackTrace();
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

    //滑動刪除Button onClick
    private ItemListPayAdapter.ItemSwipeDeleteListener itemSwipeListener
            = new ItemListPayAdapter.ItemSwipeDeleteListener() {
        @Override
        public void swipeDeleteListener(int position) {
            try {
                if (MessageBox.show("", "Delete this pay?", mContext, "Yes", "No")) {
                    deletePaymentList(position);
//                    EventBus.getDefault().post("刪除最後一筆");
                }
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.show("", "Delete error", mContext, "Return");
            }
        }
    };

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            soundpool.play(soundid, 1, 1, 0, 0, 1);
            mVibrator.vibrate(100);

            byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
            int barcodelen = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
            byte temp = intent.getByteExtra(ScanManager.BARCODE_TYPE_TAG, (byte) 0);
            Timber.tag("debug").i("----codetype--" + temp);
            String barcodeStr = new String(barcode, 0, barcodelen);

            //如果是Coupon再作search
            if (f_coupon != null && f_coupon.isVisible()) {
                f_coupon.searchCoupon(barcodeStr);
            }
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

        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Constant.PRINT_ACTION);
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

    //鎖返回
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
            case KeyEvent.KEYCODE_ESCAPE:
                FlightData.isEducationMode = !FlightData.isEducationMode;
                if (FlightData.isEducationMode) {
                    ArmsUtils.snackbarText("POS is now offline!");
                } else {
                    ArmsUtils.snackbarText("POS is now online!");
                }
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
