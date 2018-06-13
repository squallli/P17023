package tw.com.regalscan.component;

import android.app.Application;

/**
 * Created by Heidi on 2017/8/29.
 */

public class SingletonSettingClass extends Application {

  // Singleton instance
  private static SingletonSettingClass sInstance = null;

  @Override
  public void onCreate() {
    super.onCreate();
    // Setup singleton instance
    sInstance = this;
  }

  // Getter to access Singleton instance
  public static SingletonSettingClass getInstance() {
    if (sInstance == null)
      sInstance = new SingletonSettingClass();
    // Return the instance
    return sInstance;
  }
}
