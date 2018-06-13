package tw.com.regalscan.evaground.mvp.ui.holder;

import java.io.File;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import com.jess.arms.base.BaseHolder;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.http.imageloader.ImageLoader;
import com.jess.arms.http.imageloader.glide.ImageConfigImpl;
import com.jess.arms.utils.ArmsUtils;
import tw.com.regalscan.R;
import tw.com.regalscan.app.entity.ItemInfo;
import tw.com.regalscan.evaground.ItemPictureActivity;

/**
 * Created by tp00175 on 2018/1/12.
 */

public class PreorderInfoItemHolder extends BaseHolder<ItemInfo> {

    @BindView(R.id.txtOrigin) TextView mTxtOrigin;
    @BindView(R.id.txtOrigin2) TextView mTxtOrigin2;
    @BindView(R.id.txtNew) TextView mTxtNew;
    @BindView(R.id.row02) RelativeLayout mRow02;
    @BindView(R.id.imgItem) ImageView mImgItem;
    @BindView(R.id.txtItemInfo) TextView mTxtItemInfo;

    private AppComponent mAppComponent;
    private ImageLoader mImageLoader;

    public PreorderInfoItemHolder(View itemView, String workType) {
        super(itemView);

        mAppComponent = ArmsUtils.obtainAppComponentFromContext(itemView.getContext());
        mImageLoader = mAppComponent.imageLoader();

        if (workType.equals("EGAS")) {
            ViewGroup.LayoutParams params = mTxtOrigin2.getLayoutParams();
            params.width = 0;
            params.height = 0;
            mTxtOrigin2.setLayoutParams(params);
        }
    }

    @Override
    public void setData(ItemInfo data, int position) {
        String itemInfo = data.getItemName();
        String saleQty = String.valueOf(data.getSalesQty());

        mTxtItemInfo.setText(itemInfo);
        mTxtOrigin.setText(saleQty);
        mTxtNew.setText(saleQty);

        if (data.isSale()) {
            mTxtNew.setText("0");
        }

        if (data.isChanged()) {
            mTxtItemInfo.setTextColor(Color.RED);
            mTxtOrigin.setTextColor(Color.RED);
            mTxtOrigin2.setTextColor(Color.RED);
            mTxtNew.setTextColor(Color.RED);
        } else {
            mTxtItemInfo.setTextColor(Color.BLACK);
            mTxtOrigin.setTextColor(Color.BLACK);
            mTxtOrigin2.setTextColor(Color.BLACK);
            mTxtNew.setTextColor(Color.BLACK);
        }

        //region 圖片加載
        String imgPath = Environment.getExternalStorageDirectory() + File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator + "pic" + File.separator + data.getItemCode() + ".jpg";
        File file = new File(imgPath);
        Uri imgUri = Uri.fromFile(file);

        mImageLoader.loadImage(itemView.getContext(),
            ImageConfigImpl
                .builder()
                .placeholder(R.drawable.icon_loading)
                .cacheStrategy(1)
                .isClearMemory(true)
                .url(imgUri.toString())
                .imageView(mImgItem)
                .build());

        //點選列表圖片，顯示放大圖片
        mImgItem.setOnClickListener(view -> {
            Intent intent = new Intent(mAppComponent.appManager().getTopActivity(), ItemPictureActivity.class);
            intent.putExtra("itemInfo", data.getItemName());
            intent.putExtra("itemCode", data.getItemCode());
            ArmsUtils.startActivity(intent);
        });
        //endregion
    }
}
