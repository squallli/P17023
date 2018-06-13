package tw.com.regalscan.evaground.mvp.ui.adapter;

import java.util.List;

import android.view.View;

import com.jess.arms.base.BaseHolder;
import com.jess.arms.base.DefaultAdapter;
import tw.com.regalscan.R;
import tw.com.regalscan.app.entity.News;
import tw.com.regalscan.evaground.mvp.ui.holder.NewsItemHolder;

/**
 * Created by tp00175 on 2017/12/13.
 */

public class BulletinAdapter extends DefaultAdapter<News> {

    public BulletinAdapter(List<News> infos) {
        super(infos);
    }

    @Override
    public BaseHolder<News> getHolder(View v, int viewType) {
        return new NewsItemHolder(v);
    }

    @Override
    public int getLayoutId(int viewType) {
        return R.layout.item_announcement_view;
    }
}
