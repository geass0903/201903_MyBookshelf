package jp.gr.java_conf.nuranimation.my_bookshelf.application;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

@SuppressWarnings("WeakerAccess")
public class NotificationChannelManager {
    @RequiresApi(Build.VERSION_CODES.O)
    public static void create(Context context, String channelId, int titleResId, int descriptionResId) {
        String title = context.getString(titleResId);
        String description = context.getString(descriptionResId);

        NotificationChannel channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

}
