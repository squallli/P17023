package tw.com.regalscan.evaground.mvp.ui.holder;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import com.jess.arms.base.BaseHolder;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.utils.ArmsUtils;
import tw.com.regalscan.R;
import tw.com.regalscan.app.entity.PreOrderInfo;
import tw.com.regalscan.evaground.RecieptDetailActivity;

/**
 * Created by tp00175 on 2018/2/8.
 */

public class PreOrderInfoHolder extends BaseHolder<PreOrderInfo> {

    @BindView(R.id.txtreceipt) TextView mTxtReceipt;
    @BindView(R.id.txtSaleState) TextView mTxtSaleState;

    private AppComponent mAppComponent;
    private String workType;

    public PreOrderInfoHolder(View itemView, String workType) {
        super(itemView);

        mAppComponent = ArmsUtils.obtainAppComponentFromContext(itemView.getContext());

        this.workType = workType;
    }

    @Override
    public void setData(PreOrderInfo data, int position) {
        mTxtReceipt.setText(data.getPreorderNO() + "\n" + "Preorder Receipt");
        mTxtSaleState.setText(data.getSaleFlag().equals("N") ? "UnSale" : "Sale");

        mTxtReceipt.setTextColor(Color.RED);
        mTxtSaleState.setTextColor(Color.RED);

        setOnItemClickListener((view, position1) -> {
            Intent intentReceipt = new Intent();
            intentReceipt.setClass(mAppComponent.appManager().getTopActivity(), RecieptDetailActivity.class);

            if (workType.equals("EGAS")) {
                if (data.getEGASSaleFlag().equals("S")) {
                    intentReceipt.putExtra("saleState", "Sale");
                } else {
                    intentReceipt.putExtra("saleState", "UnSale");
                }
            } else {
                if (data.getEVASaleFlag().equals("S")) {
                    intentReceipt.putExtra("saleState", "Sale");
                } else {
                    intentReceipt.putExtra("saleState", "UnSale");
                }
            }
            intentReceipt.putExtra("receiptInfo", data.getPreorderNO());

            mAppComponent.appManager().getCurrentActivity().startActivityForResult(intentReceipt, 400);
        });
    }
}
