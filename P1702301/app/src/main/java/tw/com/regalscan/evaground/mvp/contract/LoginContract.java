package tw.com.regalscan.evaground.mvp.contract;

import java.io.File;

import com.jess.arms.mvp.IModel;
import com.jess.arms.mvp.IView;
import io.reactivex.Observable;
import tw.com.regalscan.app.entity.UserInfo;


public interface LoginContract {
    //对于经常使用的关于UI的方法可以定义到IView中,如显示隐藏进度条,和显示文字消息
    interface View extends IView {
        void setUserID(String userID);

        void openRFID();

        void closeRFID();

        void hideSoftKeyboard(android.view.View view);

    }

    //Model层定义接口,外部只需关心Model返回的数据,无需关心内部细节,即是否使用缓存
    interface Model extends IModel {
        Observable<UserInfo> login(String userId, String password, String rfidMark, String company);

        Observable<File> downloadApk(String fileUrl);
    }
}
