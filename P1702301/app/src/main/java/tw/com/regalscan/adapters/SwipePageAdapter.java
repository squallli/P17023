package tw.com.regalscan.adapters;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import tw.com.regalscan.component.MenuPageView;


/**
 * Created by Heidi on 2017/2/23.
 */

//滑動頁面adapter
public class SwipePageAdapter extends PagerAdapter {
    private List<MenuPageView> mPageList;


    public SwipePageAdapter(List<MenuPageView> pageList) {
        mPageList=pageList;
    }

    //總頁數
    @Override
    public int getCount() {
        return mPageList.size();
    }

    //顯示的是否是同一張頁面
    @Override
    public boolean isViewFromObject(View view, Object o) {
        return o == view;
    }

    //當要顯示的頁面可以進行暫存, 則調用此方法初始化頁面
    //將要顯示的頁面設定並return
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mPageList.get(position));
        return mPageList.get(position);
    }

    //如果滑動的頁面超出暫存的範圍, 就會調用此方法, 將頁面銷毀
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}