package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

public class LifecycleHandler implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = LifecycleHandler.class.getSimpleName();
    private static final boolean D = true;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if(activity instanceof MainActivity){
            if (D) Log.e(TAG, "onActivityResumed");
            ((MainActivity) activity).onResumeFlag = true;
            ((MainActivity) activity).updateView();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if(activity instanceof MainActivity){
            if (D) Log.e(TAG, "onActivityPaused");
            ((MainActivity) activity).onResumeFlag = false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

}