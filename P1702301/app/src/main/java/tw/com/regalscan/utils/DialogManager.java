package tw.com.regalscan.utils;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by tp00175 on 2017/12/26.
 */

public class DialogManager {
    private static final DialogManager ourInstance = new DialogManager();

    public static DialogManager getInstance() {
        return ourInstance;
    }

    private DialogManager() {
    }

    public MaterialDialog displayDialog(Context context, String content) {
        return new MaterialDialog.Builder(context)
            .content(content)
            .cancelable(false)
            .progress(true, 0)
            .show();
    }

    public void dismissDialog(MaterialDialog materialDialog) {
        materialDialog.dismiss();
    }
}
