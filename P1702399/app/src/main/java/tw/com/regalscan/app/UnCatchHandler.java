package tw.com.regalscan.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;


import com.regalscan.sqlitelibrary.TSQL;

import tw.com.regalscan.BuildConfig;
import tw.com.regalscan.db.FlightData;

/**
 * Created by tp00175 on 2018/3/28.
 */

public class UnCatchHandler implements Thread.UncaughtExceptionHandler {

    // activity对象列表,用于activity统一管理
    private static List<Activity> activityList;

    private Context mContext;
    private Application mApplication;
    private PendingIntent restartIntent;
    private String pakageName;

    public UnCatchHandler(Application application) {
//        mContext = context;
        mApplication = application;
        pakageName = application.getPackageName();

        activityList = new ArrayList<>();

        Intent intent = new Intent();
        intent.setClassName(pakageName, pakageName + ".MainActivity");
        restartIntent = PendingIntent.getActivity(mApplication.getApplicationContext(), -1, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        saveCatchInfo(e);

        AlarmManager alarmManager = (AlarmManager)mApplication.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
        e.printStackTrace();
        if(!BuildConfig.DEBUG){

        }
        finishAllActivity();
        finishProgram();//閃退
    }

    private void saveCatchInfo(Throwable e) {
        TSQL tsql = TSQL.getINSTANCE(mApplication.getApplicationContext(), FlightData.SecSeq, "P17023");
        tsql.WriteLog(FlightData.SecSeq, "Crash", "", "", e.getMessage());
    }

    // activity管理：从列表中移除activity
    public static void removeActivity(Activity activity) {
        activityList.remove(activity);
    }

    // activity管理：添加activity到列表
    public static void addActivity(Activity activity) {
        activityList.add(activity);
    }

    // activity管理：结束所有activity
    public static void finishAllActivity() {
        for (Activity activity : activityList) {
            if (null != activity) {
                activity.finish();
            }
        }
    }

    // 结束线程,一般与finishAllActivity()一起使用
    // 例如: finishAllActivity;finishProgram();
    public static void finishProgram() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
