package tw.com.regalscan.component;

import android.content.Context;
import android.widget.RelativeLayout;

public abstract class MenuPageView extends RelativeLayout {

    public MenuPageView(Context context) {
        super(context);
    }

    public abstract void refreshView();
}