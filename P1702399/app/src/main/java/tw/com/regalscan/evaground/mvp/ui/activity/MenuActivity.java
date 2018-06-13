package tw.com.regalscan.evaground.mvp.ui.activity;

import javax.inject.Inject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import com.jess.arms.base.BaseActivity;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.DeviceUtils;
import tw.com.regalscan.R;
import tw.com.regalscan.app.entity.UserInfo;
import tw.com.regalscan.evaground.MessageBox;
import tw.com.regalscan.evaground.di.component.DaggerMenuComponent;
import tw.com.regalscan.evaground.di.module.MenuModule;
import tw.com.regalscan.evaground.mvp.contract.MenuContract;
import tw.com.regalscan.evaground.mvp.presenter.MenuPresenter;
import tw.com.regalscan.utils.Cursor;

import static com.jess.arms.utils.Preconditions.checkNotNull;


public class MenuActivity extends BaseActivity<MenuPresenter> implements MenuContract.View {

    @BindView(R.id.imgV_uploadIcon) ImageView mImgVUploadIcon;
    @BindView(R.id.tv_date) TextView mTvDate;
    @BindView(R.id.tv_FlightNo) TextView mTvFlightNo;
    @BindView(R.id.tv_CartNo) TextView mTvCartNo;
    @BindView(R.id.tv_Preorder) TextView mTvPreorder;
    @BindView(R.id.tv_Vip) TextView mTvVip;
    @BindView(R.id.tv_FlightInfo) TextView mTvFlightInfo;
    @BindView(R.id.btn_Discrepancy) Button mBtnDiscrepancy;
    @BindView(R.id.btn_ScrIn) Button mBtnScrIn;
    @BindView(R.id.btnCheckUpdate) Button mBtnCheckUpdate;
    @BindView(R.id.btnEvaUpdate) Button mBtnEvaUpdate;
    @BindView(R.id.btnDownload) Button mBtnDownload;
    @BindView(R.id.btnUpload) Button mBtnUpload;
    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;

    @Inject
    RecyclerView.LayoutManager mLayoutManager;

    @Inject
    RecyclerView.Adapter mAdapter;

    private UserInfo mUserInfo;

    @Override
    public void setupActivityComponent(AppComponent appComponent) {
        DaggerMenuComponent //如找不到该类,请编译一下项目
            .builder()
            .appComponent(appComponent)
            .menuModule(new MenuModule(this))
            .build()
            .inject(this);
    }

    @Override
    public int initView(Bundle savedInstanceState) {
        return R.layout.activity_menu; //如果你不需要框架帮你设置 setContentView(id) 需要自行设置,请返回 0
    }

    @Override
    public void initData(Bundle savedInstanceState) {

        mUserInfo = getIntent().getParcelableExtra("UserInfo");

        if (mUserInfo.getCOMPANY().equals("EGAS")) {
            mBtnEvaUpdate.setEnabled(false);
            mBtnDiscrepancy.setEnabled(false);
            mBtnScrIn.setEnabled(false);
            mBtnUpload.setEnabled(false);
            mBtnDownload.setEnabled(false);
        }

        if ((boolean)ArmsUtils.obtainAppComponentFromContext(this).extras().get("isOnline")) {
            mBtnDownload.setEnabled(false);
            mBtnUpload.setEnabled(false);
        }

        initRecyclerView();
        mRecyclerView.setAdapter(mAdapter);

        if (mUserInfo.getNEWS_LIST() != null) {
            mPresenter.addNews(mUserInfo.getNEWS_LIST());
        }
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        ArmsUtils.configRecycleView(mRecyclerView, mLayoutManager);
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
        checkNotNull(message);
        MessageBox.show("", message, this, "Ok");
    }

    @Override
    public void launchActivity(@NonNull Intent intent) {
        checkNotNull(intent);
        ArmsUtils.startActivity(intent);
    }

    @Override
    public void killMyself() {
        finish();
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void setFlightNo(String flightNo) {
        mTvFlightNo.setText(flightNo);
    }

    @Override
    public void setFlightDate(String flightDate) {
        mTvDate.setText(flightDate);
    }

    @Override
    public void setCartNo(String cartNo) {
        mTvCartNo.setText(cartNo);
    }

    @Override
    public void setVipOrder(String vipOrder) {
        mTvVip.setText(vipOrder);
    }

    @Override
    public void setPreOrder(String preOrder) {
        mTvPreorder.setText(preOrder);
    }

    @Override
    public void setFlightInfo(String flightInfo) {
        mTvFlightInfo.append(flightInfo);
    }

    @Override
    public void setNeedUpload(Boolean isUpload) {
        if (isUpload) {
            mImgVUploadIcon.setVisibility(View.VISIBLE);
            mBtnDownload.setEnabled(false);
        } else {
            mImgVUploadIcon.setVisibility(View.INVISIBLE);
            mBtnDownload.setEnabled(true);
        }
    }

    @OnClick(R.id.btn_Discrepancy)
    void clickDiscrepancy() {
//        mPresenter.printDiscrepancy();
    }

    @OnClick(R.id.btn_ScrIn)
    void clickScrIn() {
//        mPresenter.printSCRIn();
    }

    @OnClick(R.id.btnCheckUpdate)
    void clickEGASCheck() {
        Intent intent = new Intent(this, CheckUpdateActivity.class);
        intent.putExtra("UserInfo", mUserInfo);
        intent.putExtra("type", "EGAS");
        launchActivity(intent);
    }

    @OnClick(R.id.btnEvaUpdate)
    void clickEvaUpdate() {
        Intent intent = new Intent(this, CheckUpdateActivity.class);
        intent.putExtra("UserInfo", mUserInfo);
        intent.putExtra("type", "EVA");
        launchActivity(intent);
    }

    @OnClick(R.id.btnDownload)
    void clickDownload() {

    }

    @OnClick(R.id.btnUpload)
    void clickUpload() {

    }

    /**
     * 點擊空白處隱藏鍵盤
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.getCurrentFocus() != null) {
            DeviceUtils.hideSoftKeyboard(this, this.getWindow().getCurrentFocus());
            return true;
        }
        return super.onTouchEvent(event);
    }
}
