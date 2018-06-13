package tw.com.regalscan.evaground.mvp.contract;

import android.app.Activity;

import com.jess.arms.mvp.IModel;
import com.jess.arms.mvp.IView;


public interface MenuContract {
    //对于经常使用的关于UI的方法可以定义到IView中,如显示隐藏进度条,和显示文字消息
    interface View extends IView {
        Activity getActivity();
        void setFlightNo(String flightNo);
        void setFlightDate(String flightDate);
        void setCartNo(String cartNo);
        void setVipOrder(String vipOrder);
        void setPreOrder(String preOrder);
        void setFlightInfo(String flightInfo);
        void setNeedUpload(Boolean isUpload);
    }

    //Model层定义接口,外部只需关心Model返回的数据,无需关心内部细节,即是否使用缓存
    interface Model extends IModel {

    }
}
