package tw.com.regalscan.evaground.mvp.ui.holder;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import com.jess.arms.base.BaseHolder;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.utils.ArmsUtils;
import tw.com.regalscan.R;
import tw.com.regalscan.app.entity.News;
import tw.com.regalscan.evaground.AnnouncementActivity;

/**
 * Created by tp00175 on 2017/12/13.
 */

public class NewsItemHolder extends BaseHolder<News> {

    @BindView(R.id.txtTitle) TextView mTxtTitle;
    @BindView(R.id.btnOpen) Button mBtnOpen;

    private AppComponent mAppComponent;

    public NewsItemHolder(View itemView) {
        super(itemView);
        mAppComponent = ArmsUtils.obtainAppComponentFromContext(itemView.getContext());
    }

    @Override
    public void setData(News data, int position) {
        mTxtTitle.setText("Title :" + data.getTITLE());

        mBtnOpen.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.putExtra("News", data);
            intent.setClass(mAppComponent.appManager().getCurrentActivity(), AnnouncementActivity.class);
            ArmsUtils.startActivity(intent);
        });
    }
}
