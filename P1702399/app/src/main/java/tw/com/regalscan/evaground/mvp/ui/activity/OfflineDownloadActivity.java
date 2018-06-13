package tw.com.regalscan.evaground.mvp.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.jess.arms.base.BaseActivity;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.DeviceUtils;
import com.jess.arms.utils.Preconditions;
import tw.com.regalscan.R;
import tw.com.regalscan.evaground.MessageBox;
import tw.com.regalscan.evaground.Models.Flight;
import tw.com.regalscan.evaground.di.component.DaggerOfflineDownloadComponent;
import tw.com.regalscan.evaground.di.module.OfflineDownloadModule;
import tw.com.regalscan.evaground.mvp.contract.OfflineDownloadContract;
import tw.com.regalscan.evaground.mvp.presenter.OfflineDownloadPresenter;
import tw.com.regalscan.utils.Cursor;


public class OfflineDownloadActivity extends BaseActivity<OfflineDownloadPresenter> implements OfflineDownloadContract.View {

    @BindView(R.id.edittxtDatePicker) EditText mEdittxtDatePicker;
    @BindView(R.id.et_cartNo) EditText mEtCartNo;
    @BindView(R.id.rbtn_br) RadioButton mRbtnBr;
    @BindView(R.id.rbtn_b7) RadioButton mRbtnB7;
    @BindView(R.id.rg_group) RadioGroup mRgGroup;
    @BindView(R.id.ll_SecSeq1) LinearLayout mLlSecSeq1;
    @BindView(R.id.ll_SecSeq2) LinearLayout mLlSecSeq2;
    @BindView(R.id.ll_SecSeq3) LinearLayout mLlSecSeq3;
    @BindView(R.id.ll_SecSeq4) LinearLayout mLlSecSeq4;
    @BindView(R.id.et_flightNo1) EditText mEtFlightNo1;
    @BindView(R.id.et_from1) EditText mEtFrom1;
    @BindView(R.id.et_to1) EditText mEtTo1;
    @BindView(R.id.et_flightNo2) EditText mEtFlightNo2;
    @BindView(R.id.et_from2) EditText mEtFrom2;
    @BindView(R.id.et_to2) EditText mEtTo2;
    @BindView(R.id.et_flightNo3) EditText mEtFlightNo3;
    @BindView(R.id.et_from3) EditText mEtFrom3;
    @BindView(R.id.et_to3) EditText mEtTo3;
    @BindView(R.id.et_flightNo4) EditText mEtFlightNo4;
    @BindView(R.id.et_from4) EditText mEtFrom4;
    @BindView(R.id.et_to4) EditText mEtTo4;
    @BindView(R.id.tv_secSeq) TextView mTvSecSeq;

    private String flightNo;
    private Flight[] mFlights;

    @Override
    public void setupActivityComponent(AppComponent appComponent) {
        DaggerOfflineDownloadComponent //如找不到该类,请编译一下项目
            .builder()
            .appComponent(appComponent)
            .offlineDownloadModule(new OfflineDownloadModule(this))
            .build()
            .inject(this);
    }

    @Override
    public int initView(Bundle savedInstanceState) {
        return R.layout.activity_offline_download; //如果你不需要框架帮你设置 setContentView(id) 需要自行设置,请返回 0
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        mPresenter.initDatePicker(mEdittxtDatePicker);
        mRbtnBr.callOnClick();
    }

    @Override
    public void showLoading() {
        Cursor.Busy(getString(R.string.Processing_Msg), this);
    }

    @Override
    public void hideLoading() {
        Cursor.Normal();
    }

    @Override
    public void showMessage(@NonNull String message) {
        Preconditions.checkNotNull(message);
        ArmsUtils.snackbarText(message);
    }

    @Override
    public void launchActivity(@NonNull Intent intent) {
        Preconditions.checkNotNull(intent);
        ArmsUtils.startActivity(intent);
    }

    @Override
    public void killMyself() {
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            DeviceUtils.hideSoftKeyboard(this, this.getCurrentFocus());
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void chkEditInfo() {

        String flightNo1 = mEtFlightNo1.getText().toString().toUpperCase();
        String flightNo2 = mEtFlightNo2.getText().toString().toUpperCase();
        String flightNo3 = mEtFlightNo3.getText().toString().toUpperCase();
        String flightNo4 = mEtFlightNo4.getText().toString().toUpperCase();

        String from1 = mEtFrom1.getText().toString().toUpperCase();
        String from2 = mEtFrom2.getText().toString().toUpperCase();
        String from3 = mEtFrom3.getText().toString().toUpperCase();
        String from4 = mEtFrom4.getText().toString().toUpperCase();

        String to1 = mEtTo1.getText().toString().toUpperCase();
        String to2 = mEtTo2.getText().toString().toUpperCase();
        String to3 = mEtTo3.getText().toString().toUpperCase();
        String to4 = mEtTo4.getText().toString().toUpperCase();

        if (mEtCartNo.getText().toString().equals("")) {
            MessageBox.show("", "Please enter cart number!", this, "Return");
        }

        if (mTvSecSeq.getText().toString().equals("2SecSeq")) {
            if (flightNo1.equals("") || from1.equals("") || to1.equals("") ||
                flightNo2.equals("") || from2.equals("") || to2.equals("")) {
                MessageBox.show("", "Please enter flight info!", this, "Return");
                return;
            }
            mFlights = new Flight[2];
            mFlights[0] = new Flight();
            mFlights[0].setFlightNo(flightNo + flightNo1);
            mFlights[0].setDeparture(from1);
            mFlights[0].setDestination(to1);

            mFlights[1] = new Flight();
            mFlights[1].setFlightNo(flightNo + flightNo2);
            mFlights[1].setDeparture(from2);
            mFlights[1].setDestination(to2);
        } else {
            if (flightNo1.equals("") || from1.equals("") || to1.equals("") ||
                flightNo2.equals("") || from2.equals("") || to2.equals("") ||
                flightNo3.equals("") || from3.equals("") || to3.equals("") ||
                flightNo4.equals("") || from4.equals("") || to3.equals("")) {
                MessageBox.show("", "Please enter flight info!", this, "Return");
                return;
            }
            mFlights = new Flight[4];

            mFlights[0] = new Flight();
            mFlights[0].setFlightNo(flightNo + flightNo1);
            mFlights[0].setDeparture(from1);
            mFlights[0].setDestination(to1);

            mFlights[1] = new Flight();
            mFlights[1].setFlightNo(flightNo + flightNo2);
            mFlights[1].setDeparture(from2);
            mFlights[1].setDestination(to2);

            mFlights[2] = new Flight();
            mFlights[2].setFlightNo(flightNo + flightNo3);
            mFlights[2].setDeparture(from3);
            mFlights[2].setDestination(to3);

            mFlights[3] = new Flight();
            mFlights[3].setFlightNo(flightNo + flightNo4);
            mFlights[3].setDeparture(from4);
            mFlights[3].setDestination(to4);
        }
    }

    @OnClick({R.id.rbtn_b7, R.id.rbtn_br})
    void onRadioButtonClicked(RadioButton radioButton) {
        boolean checked = radioButton.isChecked();

        switch (radioButton.getId()) {
            case R.id.rbtn_b7:
                if (checked) {
                    flightNo = radioButton.getText().toString();
                }
                break;
            case R.id.rbtn_br:
                if (checked) {
                    flightNo = radioButton.getText().toString();
                }
                break;
        }
    }

    @OnClick(R.id.btnDownload)
    void onDownloadClick() {

        chkEditInfo();

        mPresenter.backUpDBFile();

        mPresenter.createNewDBFile(mEtCartNo.getText().toString().toUpperCase(), mEdittxtDatePicker.getText().toString(), mFlights);
    }

    @OnClick(R.id.btn_switchSecSeq)
    public void onViewClicked() {
        String secSeq = mTvSecSeq.getText().toString();
        if (secSeq.equals("2SecSeq")) {
            mTvSecSeq.setText("4SecSeq");
            mLlSecSeq3.setVisibility(View.VISIBLE);
            mLlSecSeq4.setVisibility(View.VISIBLE);
        } else {
            mTvSecSeq.setText("2SecSeq");
            mLlSecSeq3.setVisibility(View.INVISIBLE);
            mLlSecSeq4.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.btnCancel)
    public void onCancelClicked() {
        killMyself();
    }
}
