package tw.com.regalscan.evaair.ife;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
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
import com.jess.arms.utils.DeviceUtils;
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
import tw.com.regalscan.component.SpinnerHideItemAdapter;
import tw.com.regalscan.db.FlightData;
import tw.com.regalscan.db02.DBQuery;
import tw.com.regalscan.utils.Cursor;
import tw.com.regalscan.utils.Tools;


public class ProcessingOrderListActivity extends AppCompatActivity {

    public Context mContext;
    public Activity mActivity;

    private EditText editSeatNum;
    private CheckBox checkCrew;
    //當前車櫃編號
    private String cartNum = "";
    public ItemListPayAdapter adapter;
    private ListView itemListView;
    private Button btnReturn, btnEnter;
    private TextView txtOrderCount;
    private Spinner spinnerCar;

    //已選中的單號
    private int selectedPosition = -1;

    //IFE
    private List<CrewOrder> mCrewOrders = new ArrayList<>();
    private List<CrewOrder> filterOrders = new ArrayList<>();
    private IFEFunction mIFEFunction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_processing_order_list);

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

        adapter.clear();
        adapter.notifyDataSetChanged();
        txtOrderCount.setText("0");
        editSeatNum.setText("");
    }

    public void getProcessingOrders(String searchString) {
        mIFEFunction.getOrders("", OrderStatus.ORDER_STATUS_PROCESSING)
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(disposable -> Cursor.Busy(getString(R.string.Processing_Msg), mContext))
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ifeReturnData -> {
                if (ifeReturnData.isSuccess()) {

                    mCrewOrders = (List<CrewOrder>)ifeReturnData.getData();

                    int count = 0;
                    int j = 0;
                    filterOrders.clear();
                    adapter.clear();
                    for (CrewOrder crewOrder : mCrewOrders) {
                        if (crewOrder.getSeat().equals(searchString)) {
                            count++;
                            filterOrders.add(j++, crewOrder);
                        }
                    }

                    Collections.sort(filterOrders, (obj1, obj2) -> obj1.getOrderDate().compareTo(obj2.getOrderDate()));

                    for (CrewOrder crewOrder : filterOrders) {
                        String seat = crewOrder.getSeat();

                        DateTime startTime =  DateTimeFormat.forPattern("EEE MMM d HH:mm:ss zZ yyyy").parseDateTime(crewOrder.getOrderDate().toString().replace("+08", "+00"));
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

                        adapter.notifyDataSetChanged();
                    }

                    if (count == 0) {
                        MessageBox.show("", "There is no processing order", mContext, "Ok");
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                        editSeatNum.setText("");
                    }

                    txtOrderCount.setText(String.valueOf(count));

                    DeviceUtils.hideSoftKeyboard(mContext, editSeatNum);

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

    private void init() {
        // 座位號
        cartNum = FlightData.CartNo;
        mContext = this;
        mActivity = this;

        mIFEFunction = new IFEFunction(mContext);

        //btn
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(view -> {
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
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

        editSeatNum = findViewById(R.id.editSeatNum);
        editSeatNum.setOnKeyListener((view, keyCode, keyEvent) -> {
            if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                getProcessingOrders(editSeatNum.getText().toString());
            }

            return false;
        });

        checkCrew = findViewById(R.id.checkBox);
        // 若勾選crew，Seat No欄位帶入車號並反灰不可輸入
        checkCrew.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkCrew.isChecked()) {
                editSeatNum.setEnabled(false);
                editSeatNum.setText(cartNum);
                spinnerCar.setVisibility(View.VISIBLE);
            } else {
                editSeatNum.setEnabled(true);
                editSeatNum.setText("");
                spinnerCar.setVisibility(View.INVISIBLE);
            }
        });

        // 下拉選單, 取得所有其他的車號塞進去
        StringBuilder err = new StringBuilder();
        spinnerCar = findViewById(R.id.spinner01);
        DBQuery.CartNoPack numbers = DBQuery.getAllCartList(mContext, err);
        if (numbers == null) {
            MessageBox.show("", "Get cart number error", mContext, "Return");
            ActivityManager.removeActivity(mActivity.getClass().getSimpleName());
            finish();
            return;
        }

        ArrayList<String> arrayList = new ArrayList<>();

        arrayList.add("Select Cart");

        for (int i = 0; i < numbers.cartList.length; i++) {
            arrayList.add(numbers.cartList[i].CartNo);
        }

        SpinnerHideItemAdapter drawerList = new SpinnerHideItemAdapter(this, R.layout.spinner_item, arrayList, 0);

        spinnerCar.setAdapter(drawerList);

        spinnerCar.setVisibility(View.INVISIBLE);

        spinnerCar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    getProcessingOrders(spinnerCar.getSelectedItem().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnEnter = findViewById(R.id.btnEnter);
        btnEnter.setOnClickListener(view -> {

            //確認有沒有選訂單
            if (selectedPosition == -1) {
                MessageBox.show("", "No order selected!", ProcessingOrderListActivity.this, "Return");
                return;
            }

            if (filterOrders.get(0).getOrderStatus().equals(OrderStatus.ORDER_STATUS_PROCESSING)) {
                //點選訂單內容進入下一頁
                Bundle bundle = new Bundle();
                bundle.putString("fromWhere", "dealInProgress");
                bundle.putParcelable("CrewOrder", filterOrders.get(selectedPosition));
                Intent intent = new Intent(mActivity, OrderDetailActivity.class);
                intent.putExtras(bundle);
                mActivity.startActivity(intent);
                finish();
            } else {
                MessageBox.show("", "The current order is not processing.", mContext, "Return");
            }
        });

        ActivityManager.getInstance().addActivity(mActivity.getClass().getSimpleName(), mActivity);
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
            default:
                result = super.onKeyDown(keyCode, event);
                break;
        }

        return result;
    }
}