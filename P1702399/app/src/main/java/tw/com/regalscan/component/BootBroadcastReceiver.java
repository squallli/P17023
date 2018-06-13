package tw.com.regalscan.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import tw.com.regalscan.MainActivity;

/**
 * Created by Heidi on 2017/6/27.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {

  static final String ACTION = "android.intent.action.BOOT_COMPLETED";

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals(ACTION)) {
      Intent bootIntent = new Intent(context, MainActivity.class);
      bootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(bootIntent);
    }
  }
}
