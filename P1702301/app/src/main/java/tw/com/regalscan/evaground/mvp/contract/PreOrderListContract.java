package tw.com.regalscan.evaground.mvp.contract;

import java.util.List;

import android.widget.ArrayAdapter;

import com.jess.arms.mvp.IModel;
import com.jess.arms.mvp.IView;
import tw.com.regalscan.app.entity.PreOrderInfo;


public interface PreOrderListContract {
    //对于经常使用的关于UI的方法可以定义到IView中,如显示隐藏进度条,和显示文字消息
    interface View extends IView {
        void initSpinner(ArrayAdapter arrayAdapter);
        void setTotal(String total);
        String getWorkType();
        void setSelectButton(boolean isSale);
    }

    //Model层定义接口,外部只需关心Model返回的数据,无需关心内部细节,即是否使用缓存
    interface Model extends IModel {
        List<PreOrderInfo> getPreOrderList();
    }
}
