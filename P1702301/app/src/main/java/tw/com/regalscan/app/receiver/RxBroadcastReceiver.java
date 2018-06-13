package tw.com.regalscan.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import io.reactivex.Observable;


/**
 * Created by tp00175 on 2017/11/9.
 */

public final class RxBroadcastReceiver {

    public static Observable<Intent> fromBroadCast(final Context context, final IntentFilter intentFilter) {
        return Observable.create(e -> {
            final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context1, Intent intent) {
                    e.onNext(intent);
                }
            };

            context.registerReceiver(broadcastReceiver, intentFilter);
            e.setCancellable(() -> context.unregisterReceiver(broadcastReceiver));
        });
    }
}
