package tw.com.regalscan.evaground.mvp.ui.fragment;

import javax.inject.Inject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnItemSelected;
import com.jess.arms.base.BaseFragment;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.utils.ArmsUtils;
import tw.com.regalscan.R;
import tw.com.regalscan.evaground.di.component.DaggerPreOrderListComponent;
import tw.com.regalscan.evaground.di.module.PreOrderListModule;
import tw.com.regalscan.evaground.mvp.contract.PreOrderListContract;
import tw.com.regalscan.evaground.mvp.presenter.PreOrderListPresenter;
import tw.com.regalscan.evaground.mvp.ui.adapter.PreOrderListAdapter;

import static com.jess.arms.utils.Preconditions.checkNotNull;


public class PreOrderListFragment extends BaseFragment<PreOrderListPresenter> implements PreOrderListContract.View {

    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    @BindView(R.id.tag) TextView mTag;
    @BindView(R.id.spinner01) Spinner mSpinner;
    @BindView(R.id.tv_total) TextView mTotal;
    @BindView(R.id.radioSale) RadioButton mRadioSale;
    @BindView(R.id.radioUnsale) RadioButton mRadioUnSale;
    @BindView(R.id.radioGroup1) RadioGroup mRadioGroup1;

    @Inject PreOrderListAdapter mAdapter;

    private String workType;

    public static PreOrderListFragment newInstance(String workType) {
        PreOrderListFragment fragment = new PreOrderListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("workType", workType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void setupFragmentComponent(AppComponent appComponent) {
        DaggerPreOrderListComponent //如找不到该类,请编译一下项目
            .builder()
            .appComponent(appComponent)
            .preOrderListModule(new PreOrderListModule(this))
            .build()
            .inject(this);
    }

    @Override
    public View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.listview_items_preorder, container, false);
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        workType = getArguments().getString("workType");

        if (workType.equals("EGAS")) {
            ViewGroup.LayoutParams params = mTag.getLayoutParams();
            params.width = 0;
            params.height = 0;
            mTag.setLayoutParams(params);
        }

        initRecyclerView();

        mAdapter.setWorkType(workType);

        mPresenter.getPreOrderList();
    }

    /**
     * 此方法是让外部调用使fragment做一些操作的,比如说外部的activity想让fragment对象执行一些方法,
     * 建议在有多个需要让外界调用的方法时,统一传Message,通过what字段,来区分不同的方法,在setData
     * 方法中就可以switch做不同的操作,这样就可以用统一的入口方法做不同的事
     * <p>
     * 使用此方法时请注意调用时fragment的生命周期,如果调用此setData方法时onCreate还没执行
     * setData里却调用了presenter的方法时,是会报空的,因为dagger注入是在onCreated方法中执行的,然后才创建的presenter
     * 如果要做一些初始化操作,可以不必让外部调setData,在initData中初始化就可以了
     *
     * @param data
     */

    @Override
    public void setData(Object data) {

    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        ArmsUtils.configRecyclerView(mRecyclerView, new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

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

    }

    @Override
    public void initSpinner(ArrayAdapter arrayAdapter) {
        mSpinner.setAdapter(arrayAdapter);
    }

    @OnItemSelected(R.id.spinner01)
    void onSpinnerSelected(int position) {
        mPresenter.generateList(position);
    }

    @Override
    public void setTotal(String total) {
        mTotal.setText("Total: " + total);
    }

    @OnCheckedChanged({R.id.radioSale, R.id.radioUnsale})
    void onSaleSelected(CompoundButton button, boolean checked) {
        if (checked) {
            switch (button.getId()) {
                case R.id.radioSale:
                    mPresenter.changeStatus(true);
                    break;
                case R.id.radioUnsale:
                    mPresenter.changeStatus(false);
                    break;
            }
        }
    }

    @Override
    public String getWorkType() {
        return workType;
    }

    @Override
    public void setSelectButton(boolean isSale) {
        if (isSale) {
            mRadioSale.setChecked(true);
        } else {
            mRadioUnSale.setChecked(true);
        }
    }
}
