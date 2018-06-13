package tw.com.regalscan.evaground.mvp.ui.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;

import butterknife.BindView;
import butterknife.OnClick;
import com.jess.arms.base.BaseActivity;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.DeviceUtils;
import tw.com.regalscan.R;
import tw.com.regalscan.app.entity.UserInfo;
import tw.com.regalscan.evaground.di.component.DaggerCheckUpdateComponent;
import tw.com.regalscan.evaground.di.module.CheckUpdateModule;
import tw.com.regalscan.evaground.mvp.contract.CheckUpdateContract;
import tw.com.regalscan.evaground.mvp.presenter.CheckUpdatePresenter;
import tw.com.regalscan.evaground.mvp.ui.fragment.DiscrepancyFragment;
import tw.com.regalscan.evaground.mvp.ui.fragment.ItemListFragment;
import tw.com.regalscan.evaground.mvp.ui.fragment.PreOrderListFragment;
import tw.com.regalscan.evaground.mvp.ui.fragment.VipOrderListFragment;
import tw.com.regalscan.utils.Cursor;

import static com.jess.arms.utils.Preconditions.checkNotNull;


public class CheckUpdateActivity extends BaseActivity<CheckUpdatePresenter> implements CheckUpdateContract.View {

    @BindView(R.id.iv_searchicon) ImageView mIvSearchicon;
    @BindView(R.id.sv_searchbox) SearchView mSvSearchbox;
    @BindView(R.id.tab_layout) TabLayout mTabLayout;
    @BindView(R.id.myviewpager) ViewPager mViewPager;
    @BindView(R.id.btnReturn) Button mBtnReturn;
    @BindView(R.id.btnSave) Button mBtnSave;
    @BindView(R.id.rowText01) LinearLayout mLinearLayout;
    @BindView(R.id.tv_Title) TextView mTvTitle;

    ArrayList<Fragment> mFragments;

    @Override
    public void setupActivityComponent(AppComponent appComponent) {
        DaggerCheckUpdateComponent //如找不到该类,请编译一下项目
            .builder()
            .appComponent(appComponent)
            .checkUpdateModule(new CheckUpdateModule(this))
            .build()
            .inject(this);
    }

    @Override
    public int initView(Bundle savedInstanceState) {
        return R.layout.activity_evaupdate_egascheck; //如果你不需要框架帮你设置 setContentView(id) 需要自行设置,请返回 0
    }

    @Override
    public void initData(Bundle savedInstanceState) {

        UserInfo userInfo = getIntent().getParcelableExtra("UserInfo");

        String workType = getIntent().getStringExtra("type");

        if (workType.equals("EVA")) {
            mTvTitle.setText("EVA Update");
        } else {
            mTvTitle.setText("EGAS Check");
        }

        mLinearLayout.setVisibility(View.INVISIBLE);

        ItemListFragment itemListFragment;
        DiscrepancyFragment discrepancyFragment;
        PreOrderListFragment preOrderListFragment;
        VipOrderListFragment vipOrderListFragment;

        itemListFragment = ItemListFragment.newInstance(workType);
        discrepancyFragment = DiscrepancyFragment.newInstance(workType);
        preOrderListFragment = PreOrderListFragment.newInstance(workType);
        vipOrderListFragment = VipOrderListFragment.newInstance(workType);

        if (mFragments == null) {
            mFragments = new ArrayList<>();
            mFragments.add(itemListFragment);
            mFragments.add(discrepancyFragment);
            mFragments.add(preOrderListFragment);
            mFragments.add(vipOrderListFragment);
        }

        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return POSITION_NONE;
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mIvSearchicon.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        mIvSearchicon.setVisibility(View.INVISIBLE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mViewPager.setOffscreenPageLimit(1);
        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.setSelectedTabIndicatorHeight(0);

        TextView tabOne = (TextView)LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabOne.setText(getString(R.string.Items));
        mTabLayout.getTabAt(0).setCustomView(tabOne);
        setTabWeight(0, 0.7f);

        TextView tabTwo = (TextView)LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabTwo.setText(getString(R.string.Discrepancy));
        mTabLayout.getTabAt(1).setCustomView(tabTwo);
        setTabWeight(1, 1.3f);

        TextView tabThree = (TextView)LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabThree.setText(getString(R.string.PreOrder));
        mTabLayout.getTabAt(2).setCustomView(tabThree);

        TextView tabFour = (TextView)LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        tabFour.setText(getString(R.string.Vip));
        mTabLayout.getTabAt(3).setCustomView(tabFour);
        setTabWeight(3, 0.5f);
    }

    /**
     * 設定tab比重
     *
     * @param tagPosition tab位置
     * @param weight      tab比重
     */
    private void setTabWeight(int tagPosition, float weight) {
        LinearLayout layout = ((LinearLayout)((LinearLayout)mTabLayout.getChildAt(0)).getChildAt(tagPosition));
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)layout.getLayoutParams();
        layoutParams.weight = weight;
        layout.setLayoutParams(layoutParams);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof ItemListFragment) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
            
            if (fragment instanceof DiscrepancyFragment) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
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
        ArmsUtils.snackbarText(message);
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

    @OnClick(R.id.btnReturn)
    void clickReturn() {
        killMyself();
    }

    @OnClick(R.id.btnSave)
    void clickSave() {

    }

    @OnClick(R.id.iv_searchicon)
    void clickIcon() {
        if (mSvSearchbox.getVisibility() == View.INVISIBLE) {
            mSvSearchbox.setVisibility(View.VISIBLE);
            mPresenter.initSearchView(mSvSearchbox);
        } else {
            mSvSearchbox.setVisibility(View.INVISIBLE);
        }
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mFragments = null;
    }
}
