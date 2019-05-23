package jp.gr.java_conf.nuranimation.my_bookshelf.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;

public class BookSearchService extends Service {
    public static final String TAG = BookSearchService.class.getSimpleName();
    private static final boolean D = true;


    public class MBinder extends Binder {
        public BookSearchService getService() {
            return BookSearchService.this;
        }
    }

    public BookSearchService() {
        super();
    }


    @Override
    public IBinder onBind(Intent intent) {
        if (D) Log.d(TAG, "onBind");
        return new MBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        if (D) Log.d(TAG, "onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (D) Log.d(TAG, "onUnbind");
        super.onUnbind(intent);
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (D) Log.d(TAG, "onCreate");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (D) Log.d(TAG, "onDestroy");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (D) Log.d(TAG, "onStartCommand");
        String channelId = "service";
        String title = "TestService";
        // 通知設定
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Notification notification = new Notification.Builder(getApplicationContext(), channelId)
                        .setContentTitle(title)
                        .setSmallIcon(R.drawable.ic_vector_image_24dp)
                        .setContentText("service start")
                        .build();

                // フォアグラウンドで実行
                startForeground(1, notification);
            }
        }


        return START_NOT_STICKY;
    }


}