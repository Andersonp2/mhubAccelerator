package anderson.com.mhubaccelerometer;

import android.app.Application;

import anderson.com.mhubaccelerometer.controller.AppController;

/**
 * Created by lcmuniz on 29/12/16.
 */

public class MainApplication extends Application {

    private AppController appController;

    @Override
    public void onCreate() {
        super.onCreate();
        appController = new AppController(getApplicationContext());
    }

    public AppController getAppController() {
        return appController;
    }

}
