package tw.com.regalscan.evaground.mvp.ui.holder;

import java.io.File;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import com.bumptech.glide.Glide;
import com.jess.arms.base.BaseHolder;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.http.imageloader.ImageLoader;
import com.jess.arms.http.imageloader.glide.ImageConfigImpl;
import com.jess.arms.utils.ArmsUtils;
import tw.com.regalscan.R;
import tw.com.regalscan.app.entity.ItemInfo;
import tw.com.regalscan.evaground.ItemDetailActivity;
import tw.com.regalscan.evaground.ItemPictureActivity;

/**
 * Created by tp00175 on 2017/12/15.
 */

public class ItemInfoItemHolder extends BaseHolder<ItemInfo> {

    @BindView(R.id.imgItem) ImageView mImgItem;
    @BindView(R.id.txtItemInfo) TextView mTxtItemInfo;
    @BindView(R.id.txtOrigin) TextView mTxtOrigin;
    @BindView(R.id.txtOrigin2) TextView mTxtOrigin2;
    @BindView(R.id.txtNew) TextView mTxtNew;
    @BindView(R.id.txtDamage) TextView mTxtDamage;

    private AppComponent mAppComponent;
    private ImageLoader mImageLoader;
    private String workType;

    public ItemInfoItemHolder(View itemView, String workType) {
        super(itemView);
        mAppComponent = ArmsUtils.obtainAppComponentFromContext(itemView.getContext());
        mImageLoader = mAppComponent.imageLoader();

        this.workType = workType;

        if (workType.equals("EGAS")) {
            ViewGroup.LayoutParams params = mTxtOrigin2.getLayoutParams();
            params.width = 0;
            params.height = 0;
            mTxtOrigin2.setLayoutParams(params);
        }
    }

    @Override
    public void setData(ItemInfo data, int position) {

        String itemInfo = data.getDrawNo() + "-" + data.getItemCode() + "\n" + data.getItemName();
        String standQty = String.valueOf(data.getStandQty());
        String egasCheckQty = String.valueOf(data.getEGASCheckQty());
        String egasDamageQty = String.valueOf(data.getEGASDamageQty());

        mTxtItemInfo.setText(itemInfo);
        mTxtOrigin.setText(standQty);
        mTxtOrigin2.setText(egasCheckQty);
        mTxtNew.setText(egasCheckQty);
        mTxtDamage.setText(egasDamageQty);

        if (workType.equals("EGAS")) {
            if (data.getEGASCheckQty() != data.getStandQty() || data.getEGASDamageQty() != data.getDamageQty()) {
                mTxtItemInfo.setTextColor(Color.RED);
                mTxtOrigin.setTextColor(Color.RED);
                mTxtOrigin2.setTextColor(Color.RED);
                mTxtNew.setTextColor(Color.RED);
                mTxtDamage.setTextColor(Color.RED);
            } else {
                mTxtItemInfo.setTextColor(Color.BLACK);
                mTxtOrigin.setTextColor(Color.BLACK);
                mTxtOrigin2.setTextColor(Color.BLACK);
                mTxtNew.setTextColor(Color.BLACK);
                mTxtDamage.setTextColor(Color.BLACK);
            }
        } else {
            if (data.getEVACheckQty() != data.getStandQty() || data.getEVACheckQty() != data.getDamageQty()) {
                mTxtItemInfo.setTextColor(Color.RED);
                mTxtOrigin.setTextColor(Color.RED);
                mTxtOrigin2.setTextColor(Color.RED);
                mTxtNew.setTextColor(Color.RED);
                mTxtDamage.setTextColor(Color.RED);
            } else {
                mTxtItemInfo.setTextColor(Color.BLACK);
                mTxtOrigin.setTextColor(Color.BLACK);
                mTxtOrigin2.setTextColor(Color.BLACK);
                mTxtNew.setTextColor(Color.BLACK);
                mTxtDamage.setTextColor(Color.BLACK);
            }
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

        setOnItemClickListener((view, position1) -> {
            Intent intent = new Intent();
            intent.setClass(mAppComponent.appManager().getTopActivity(), ItemDetailActivity.class);

            intent.putExtra("ItemInfo", data);
            intent.putExtra("position", position1);

            mAppComponent.appManager().getCurrentActivity().startActivityForResult(intent, 500);
        });
    }

    public void updateView(ItemInfo itemInfo) {
        if (workType.equals("EGAS")) {
            if (itemInfo.getEGASCheckQty() != itemInfo.getStandQty() || itemInfo.getEGASDamageQty() != itemInfo.getDamageQty()) {
                mTxtItemInfo.setTextColor(Color.RED);
                mTxtOrigin.setTextColor(Color.RED);
                mTxtNew.setTextColor(Color.RED);
                mTxtDamage.setTextColor(Color.RED);
            } else {
                mTxtItemInfo.setTextColor(Color.BLACK);
                mTxtOrigin.setTextColor(Color.BLACK);
                mTxtNew.setTextColor(Color.BLACK);
                mTxtDamage.setTextColor(Color.BLACK);
            }
        } else {
            if (itemInfo.getEVACheckQty() != itemInfo.getStandQty() || itemInfo.getEVADamageQty() != itemInfo.getDamageQty()) {
                mTxtItemInfo.setTextColor(Color.RED);
                mTxtOrigin.setTextColor(Color.RED);
                mTxtNew.setTextColor(Color.RED);
                mTxtDamage.setTextColor(Color.RED);
            } else {
                mTxtItemInfo.setTextColor(Color.BLACK);
                mTxtOrigin.setTextColor(Color.BLACK);
                mTxtNew.setTextColor(Color.BLACK);
                mTxtDamage.setTextColor(Color.BLACK);
            }
        }
    }

    @Override
    protected void onRelease() {
        Glide.with(mAppComponent.appManager().getTopActivity() == null
            ? mAppComponent.application() : mAppComponent.appManager().getTopActivity())
            .clear(mImgItem);
    }
}
