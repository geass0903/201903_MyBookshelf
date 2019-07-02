package jp.gr.java_conf.nuranimation.my_bookshelf.service.base;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public abstract class BaseService extends Service {
    public static final String TAG = BaseService.class.getSimpleName();
    private static final boolean D = true;

    public static final String FILTER_ACTION_UPDATE_SERVICE_STATE   = "BaseService.FILTER_ACTION_UPDATE_SERVICE_STATE";
    public static final String KEY_SERVICE_STATE                    = "BaseService.KEY_SERVICE_STATE";

    private static final int notifyId = 1;
    private NotificationManager mNotificationManager;
    private LocalBroadcastManager mLocalBroadcastManager;

    private int mState;
    private boolean isForeground;

    abstract protected Notification createNotification(int state);


    @Override
    public void onCreate() {
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this.getApplicationContext());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (D) Log.d(TAG, "startForeground");
        Notification notification = createNotification(mState);
        isForeground = true;
        startForeground(notifyId, notification);
        return START_NOT_STICKY;
    }

    public void cancelForeground(){
        if (D) Log.d(TAG, "stopForeground");
        isForeground = false;
        stopForeground(true);
    }

    public int getServiceState(){
        return mState;
    }

    public void setServiceState(int state){
        if (D) Log.d(TAG, "update ServiceState : " + mState + " > " + state);
        mState = state;
        Intent intent = new Intent();
        intent.setAction(FILTER_ACTION_UPDATE_SERVICE_STATE);
        intent.putExtra(KEY_SERVICE_STATE, state);
        mLocalBroadcastManager.sendBroadcast(intent);
        updateNotification(mState);
    }

    private void updateNotification(int state) {
        if(mNotificationManager != null && isForeground) {
            Notification notification = createNotification(state);
            mNotificationManager.notify(notifyId,notification);
        }
    }

}
