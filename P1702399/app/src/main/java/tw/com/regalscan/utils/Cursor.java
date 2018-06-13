package tw.com.regalscan.utils;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by rgsystem on 16/4/14.
 */
public final class Cursor {

    private static ProgressDialog dialog;

    public static void Busy(String msg, Context context) {
        dialog = new ProgressDialog(context);
        dialog.setMessage(msg);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
    }

    public static void Normal() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
