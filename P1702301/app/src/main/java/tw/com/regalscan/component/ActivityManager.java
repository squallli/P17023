package tw.com.regalscan.component;

import android.app.Activity;
import android.app.Application;

import java.util.HashMap;


//用來管理所有Activity的單例類別

public class ActivityManager extends Application{
    private static ActivityManager instance;

    //用來儲存Activity的LinkedList
    private static HashMap<String, Activity> activityHashMap = new HashMap<String, Activity>();

    //此類別無法使用普通的方法實例化
    private ActivityManager(){
        super();
    }

    //用來取得唯一實例物件的方法
    public static ActivityManager getInstance(){
        if (instance == null){
            instance = new ActivityManager();
        }
        return instance;
    }

    //用來加入要管理的Activity的方法
    public void addActivity(String name, Activity activity){
        if(!activityHashMap.containsKey(name)){
            activityHashMap.put(name, activity);
        }
    }

    public static boolean isActivityInStock(String name){
        return activityHashMap.containsKey(name);
    }

    //用來關掉所有被管理的Activity的方法
    public static void closeAllActivity(){

        for (Object key : activityHashMap.keySet()) {
            //finish()將Activity推向後台，移出了Activity推疊，資源並沒有被
            //釋放，不會觸發onDestory()，但按Back鍵也會回到原Activity
            activityHashMap.get(key).finish();
        }

        activityHashMap.clear();
    }

    public static void removeActivity(String key){

        if(activityHashMap.get(key)!=null){
            activityHashMap.get(key).finish();
            activityHashMap.remove(key);
        }
    }

}