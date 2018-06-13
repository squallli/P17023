package tw.com.regalscan.evaair.ife;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import aero.panasonic.inflight.crew.services.ordermanagement.v1.CrewOrder;
import aero.panasonic.inflight.crew.services.ordermanagement.v1.OrderStatus;
import com.jess.arms.utils.ArmsUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import tw.com.regalscan.R;
import tw.com.regalscan.adapters.ItemListPayAdapter;
import tw.com.regalscan.component.ActivityManager;
import tw.com.regalscan.component.MessageBox;
import tw.com.regalscan.utils.Cursor;
import tw.com.regalscan.utils.Tools;


public class OrderListActivity extends AppCompatActivity {

    public Context mContext;
    public Activity mActivity;

    public ItemListPayAdapter adapter;
    private ListView itemListView;
    private Button btnReturn;
    private TextView txtOrderCount;
    private ImageView imgDiscountPlus;
    private List<CrewOrder> mCrewOrders;

    private IFEFunction mIFEFunction;

    //已選中的單號
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        init();

        getOrderList();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void init() {
        mContext = this;
        mActivity = this;

        mIFEFunction = new IFEFunction(mContext);

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            if (ActivityManager.isActivityInStock(mActivity.getClass().getSimpleName())) {
                ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            }
            finish();
        });

        //訂單一覽
        itemListView = findViewById(R.id.lvItemList);
        adapter = new ItemListPayAdapter(mContext);
        adapter.setIsOnClickChangeBack(true);
        itemListView.setAdapter(adapter);
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            View view2; //保存點選的View
            int select_item = -1; //一開始未選擇任何一個item所以為-1
            boolean isClicked = false;

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //點選某個item並呈現被選取的狀態
                if ((select_item == -1)) {
                    //改變顏色
                    view.setBackground(view.getResources().getDrawable(R.drawable.shape_item_background_02));
                    selectedPosition = position;
                    isClicked = true;
                }
                //如果已點選過，要取消
                else if ((select_item == position)) {
                    if (isClicked) {
                        view.setBackground(view.getResources().getDrawable(R.drawable.shape_item_background));
                        selectedPosition = -1;
                        isClicked = false;
                    } else {
                        view.setBackground(view.getResources().getDrawable(R.drawable.shape_item_background_02));
                        selectedPosition = position;
                        isClicked = true;
                    }
                } else {
                    //將上一次點選的View保存在view2
                    view2.setBackground(view.getResources().getDrawable(R.drawable.shape_item_background));
                    //為View加上選取效果
                    view.setBackground(view.getResources().getDrawable(R.drawable.shape_item_background_02));
                    selectedPosition = position;
                }
                view2 = view; //保存點選的View
                select_item = position;//保存目前的View位置
            }
        });

        //訂單總筆數
        txtOrderCount = findViewById(R.id.txtOrderCount);
        txtOrderCount.setText(String.valueOf(adapter.getCount()));

        // 重整鈕
        imgDiscountPlus = findViewById(R.id.imgDiscountPlus);
        imgDiscountPlus.setOnClickListener(view -> {
            //重新取得order清單
            getOrderList();
        });

        Button btnEnter = findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener((View view) -> {

            //確認有沒有選訂單
            if (selectedPosition == -1) {
                MessageBox.show("", "No order selected!", mContext, "Return");
                return;
            }

            //確認訂單狀態是否處理中
            chkOrderStatus();
        });

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
    }


//  private void enableExpandableList() {
//    final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//    toolbar.setTitle("");
//    setSupportActionBar(toolbar);
//    ExpandableListView expandableListView = (ExpandableListView) findViewById(R.id.expendlist);
//    NavigationDrawer navigationDrawer = new NavigationDrawer(mActivity, mContext, drawerLayout, toolbar, expandableListView);
//  }


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
            default:
                result = super.onKeyDown(keyCode, event);
                break;
        }

        return result;
    }

    private void getOrderList() {
        mIFEFunction.getOrders("", OrderStatus.ORDER_STATUS_OPEN)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ifeReturnData -> {
                if (ifeReturnData.isSuccess()) {
                    List<CrewOrder> crewOrders = (List<CrewOrder>)ifeReturnData.getData();
                    txtOrderCount.setText(String.valueOf(crewOrders.size()));

                    Collections.sort(crewOrders, (obj1, obj2) -> obj1.getOrderDate().compareTo(obj2.getOrderDate()));

                    mCrewOrders = crewOrders;

                    adapter.clear();

                    for (CrewOrder crewOrder : crewOrders) {

                        String seat = crewOrder.getSeat();

                        DateTime startTime = DateTimeFormat.forPattern("EEE MMM d HH:mm:ss zZ yyyy").parseDateTime(crewOrder.getOrderDate().toString().replace("+08", "+00"));
                        DateTime endTime = new DateTime();
                        Period period = new Period(startTime.withZone(DateTimeZone.forID("Asia/Taipei")), endTime, PeriodType.minutes());

                        String spendTime = String.valueOf(period.getMinutes());

                        double amount = 0.0;
                        for (int i = 0; i < crewOrder.getTotalPrice().size(); i++) {
                            if (crewOrder.getTotalPrice().get(i).getCurrency().equals("US")) {
                                amount = crewOrder.getTotalPrice().get(i).getAmount().doubleValue();
                                break;
                            }
                        }

                        // Payby==下單時間, 幣別==座位號, 總額==總額, 其他隨便填
                        adapter.addItem(seat, spendTime, amount, 0, "");
                    }

                    adapter.notifyDataSetChanged();

                    Cursor.Normal();
                } else {
                    Cursor.Normal();
                    if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                        if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                            ArmsUtils.startActivity(IFEActivity01.class);
                            finish();
                        }
                    }
                }
            });
    }

    private void chkOrderStatus() {
        mIFEFunction.getOrders(mCrewOrders.get(selectedPosition).getOrderId(), null)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
            .flatMap(ifeReturnData -> {
                if (ifeReturnData.isSuccess()) {
                    List<CrewOrder> crewOrders = (List<CrewOrder>)ifeReturnData.getData();
                    if (crewOrders.get(0).getOrderStatus().equals(OrderStatus.ORDER_STATUS_OPEN)) {
                        return mIFEFunction.initOrder(crewOrders.get(0));
                    } else {
                        ifeReturnData.setSuccess(false);
                        ifeReturnData.setErrMsg("The current order is processing.");
                        return Observable.just(ifeReturnData);
                    }
                } else {
                    return Observable.just(ifeReturnData);
                }
            })
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally(Cursor::Normal)
            .subscribe(ifeReturnData -> {
                if (ifeReturnData.isSuccess()) {
                    //點選訂單內容進入下一頁
                    Intent intent = new Intent(mActivity, OrderDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("CrewOrder", mCrewOrders.get(selectedPosition));
                    bundle.putString("fromWhere", "OrderListActivity");
                    intent.putExtras(bundle);
                    mActivity.startActivity(intent);
                    finish();
                } else {
                    if (ifeReturnData.getErrMsg().equals("The current order is processing.")) {
                        Cursor.Normal();
                        MessageBox.show("", "The current order is processing.", mContext, "Ok");
                        getOrderList();
                    } else {
                        if (mIFEFunction.errorProcessing(mActivity, ifeReturnData.getErrMsg())) {
                            if (MessageBox.show("", getString(R.string.IFE_Offline_ReportCP), mContext, "Ok")) {
                                ArmsUtils.startActivity(IFEActivity01.class);
                                finish();
                            }
                        }
                    }
                }
            });
    }
}