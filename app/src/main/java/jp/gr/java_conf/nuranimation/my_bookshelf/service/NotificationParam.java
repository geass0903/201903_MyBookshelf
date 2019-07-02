package jp.gr.java_conf.nuranimation.my_bookshelf.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.MainActivity;

class NotificationParam {

    private final String channelId;
    private final String title;
    private final String message;
    private final int iconId;
    private final PendingIntent pendingIntent;


    private NotificationParam(String channelId, String title, String message, int iconId, PendingIntent pendingIntent){
        this.channelId = channelId;
        this.title = title;
        this.message = message;
        this.iconId = iconId;
        this.pendingIntent = pendingIntent;
    }

    String getChannelId(){
        return channelId;
    }

    String getTitle(){
        return title;
    }

    String getMessage(){
        return message;
    }

    int getIconId(){
        return iconId;
    }

    PendingIntent getPendingIntent(){
        return pendingIntent;
    }



    static NotificationParam createNotificationParam(Context context, int state){
        String channelId = context.getString(R.string.notification_channel_id);
        String title = context.getString(R.string.app_name);
        String message = context.getString(R.string.notification_channel_title);
        int iconId = 0;
        switch (state) {
            case BookService.STATE_NONE:
                break;
            case BookService.STATE_SEARCH_BOOKS_SEARCH_INCOMPLETE:
                message = context.getString(R.string.notification_message_search_incomplete);
                iconId = R.drawable.ic_search_24dp;
                break;
            case BookService.STATE_SEARCH_BOOKS_SEARCH_COMPLETE:
                message = context.getString(R.string.notification_message_search_complete);
                iconId = R.drawable.ic_search_24dp;
                break;
            case BookService.STATE_NEW_BOOKS_RELOAD_INCOMPLETE:
                message = context.getString(R.string.notification_message_reload_incomplete);
                iconId = R.drawable.ic_reload_24dp;
                break;
            case BookService.STATE_NEW_BOOKS_RELOAD_COMPLETE:
                message = context.getString(R.string.notification_message_reload_complete);
                iconId = R.drawable.ic_reload_24dp;
                break;
            case BookService.STATE_EXPORT_INCOMPLETE:
                message = context.getString(R.string.notification_message_export_incomplete);
                iconId = R.drawable.ic_export_24dp;
                break;
            case BookService.STATE_EXPORT_COMPLETE:
                message = context.getString(R.string.notification_message_export_complete);
                iconId = R.drawable.ic_export_24dp;
                break;
            case BookService.STATE_IMPORT_INCOMPLETE:
                message = context.getString(R.string.notification_message_import_incomplete);
                iconId = R.drawable.ic_import_24dp;
                break;
            case BookService.STATE_IMPORT_COMPLETE:
                message = context.getString(R.string.notification_message_import_complete);
                iconId = R.drawable.ic_import_24dp;
                break;
            case BookService.STATE_BACKUP_INCOMPLETE:
                message = context.getString(R.string.notification_message_backup_incomplete);
                iconId = R.drawable.ic_backup_24dp;
                break;
            case BookService.STATE_BACKUP_COMPLETE:
                message = context.getString(R.string.notification_message_backup_complete);
                iconId = R.drawable.ic_backup_24dp;
                break;
            case BookService.STATE_RESTORE_INCOMPLETE:
                message = context.getString(R.string.notification_message_restore_incomplete);
                iconId = R.drawable.ic_restore_24dp;
                break;
            case BookService.STATE_RESTORE_COMPLETE:
                message = context.getString(R.string.notification_message_restore_complete);
                iconId = R.drawable.ic_restore_24dp;
                break;
            case BookService.STATE_DROPBOX_LOGIN:
                message = context.getString(R.string.notification_message_login_dropbox);
                iconId = R.drawable.ic_link_up_24dp;
                break;

        }
        Intent ni = new Intent();
        ni.addCategory(Intent.CATEGORY_LAUNCHER);
        ni.setClassName(context.getApplicationContext().getPackageName(), MainActivity.class.getName());
        ni.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, ni, PendingIntent.FLAG_CANCEL_CURRENT);

        return new NotificationParam(channelId,title,message,iconId,pendingIntent);
    }

}
