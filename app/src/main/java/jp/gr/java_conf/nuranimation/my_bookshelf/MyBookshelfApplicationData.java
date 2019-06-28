package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

import java.io.File;

//@SuppressWarnings({"unused"})
public class MyBookshelfApplicationData extends Application {
    private static final String TAG = MyBookshelfApplicationData.class.getSimpleName();
    private static final boolean D = true;

    @Override
    public void onCreate(){
        super.onCreate();
        if(D) Log.d(TAG,"onCreate");

//        File cacheDir = getCacheDir();
        File cacheDir = getExternalFilesDir(null);

        DiskCacheConfig largeImageCacheConfig = DiskCacheConfig.newBuilder(this)
                .setBaseDirectoryName("largeImageCache")
                .setBaseDirectoryPath(cacheDir)
                .build();
        DiskCacheConfig smallImageCacheConfig = DiskCacheConfig.newBuilder(this)
                .setBaseDirectoryName("smallImageCache")
                .setBaseDirectoryPath(cacheDir)
                .build();
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setMainDiskCacheConfig(largeImageCacheConfig)
                .setSmallImageDiskCacheConfig(smallImageCacheConfig)
                .build();
        Fresco.initialize(this, config);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(getApplicationContext(),getString(R.string.notification_channel_id), R.string.notification_channel_title, R.string.notification_channel_description);
        }

    }

    @Override
    public void onTerminate(){
        super.onTerminate();
        if(D) Log.d(TAG,"onTerminate");
    }


    @SuppressWarnings({"SameParameterValue"})
    @RequiresApi(Build.VERSION_CODES.O)
    private static void createNotificationChannel(Context context, String channelId, int titleResId, int descriptionResId) {
        String title = context.getString(titleResId);
        String description = context.getString(descriptionResId);

        NotificationChannel channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
    }

}
