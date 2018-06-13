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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnItemSelected;
import com.jess.arms.base.BaseFragment;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.utils.ArmsUtils;
import org.simple.eventbus.Subscriber;
import tw.com.regalscan.R;
import tw.com.regalscan.evaground.di.component.DaggerItemListComponent;
import tw.com.regalscan.evaground.di.module.ItemListModule;
import tw.com.regalscan.evaground.mvp.contract.ItemListContract;
import tw.com.regalscan.evaground.mvp.presenter.ItemListPresenter;
import tw.com.regalscan.evaground.mvp.ui.adapter.ItemListAdapter;

import static com.jess.arms.utils.Preconditions.checkNotNull;


public class ItemListFragment extends BaseFragment<ItemListPresenter> implements ItemListContract.View {

    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    @BindView(R.id.tag) TextView mTag;
    @BindView(R.id.tag2) TextView mTag2;
    @BindView(R.id.spinner01) Spinner mSpinner;
    @BindView(R.id.tv_total) TextView mTotal;

    @Inject ItemListAdapter mAdapter;

    private String workType;

    public static ItemListFragment newInstance(String workType) {
        ItemListFragment fragment = new ItemListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("workType", workType);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void setupFragmentComponent(AppComponent appComponent) {
        DaggerItemListComponent //如找不到该类,请编译一下项目
            .builder()
            .appComponent(appComponent)
            .itemListModule(new ItemListModule(this))
            .build()
            .inject(this);
    }

    @Override
    public View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.listview_items, container, false);
    }

    @Override
    public void initData(Bundle savedInstanceState) {

        workType = getArguments().getString("workType");

        if (workType.equals("EGAS")) {
            ViewGroup.LayoutParams params = mTag.getLayoutParams();
            params.width = 0;
            params.height = 0;
            mTag.setLayoutParams(params);
            mTag2.setText("STD");
        }

        initRecyclerView();

        mAdapter.setWorkType(workType);

        mPresenter.getItem();

        mPresenter.getDraws();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 500) {
                mPresenter.processActivityResult(data.getParcelableExtra("ItemInfo"), (int)data.getExtras().get("position"), !mSpinner.getSelectedItem().toString().toLowerCase().equals("all drawer"));
            }
        }
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
    void itemSelected() {
        mPresenter.search(mSpinner.getSelectedItem().toString().toLowerCase());
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void setTotal(String total) {
        mTotal.setText("Total: " + total);
    }

    @Override
    public String getWorkType() {
        return workType;
    }

    @Override
    public String getSpinnerSelectedItem() {
        return mSpinner.getSelectedItem().toString().toLowerCase();
    }

    @Subscriber(tag = "search")
    public void search(String searchString) {
        mPresenter.search(searchString);
    }
}
