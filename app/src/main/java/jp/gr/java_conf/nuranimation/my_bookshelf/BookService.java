package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dropbox.core.android.Auth;

import java.util.List;


public class BookService extends Service implements SearchBooksThread.ThreadFinishListener, NewBooksThread.ThreadFinishListener, FileBackupThread.ThreadFinishListener {
    public static final String TAG = BookService.class.getSimpleName();
    private static final boolean D = true;

    private static final String DROP_BOX_KEY = "fh2si4dchz272b1";

    public static final String KEY_SERVICE_STATE = "KEY_SERVICE_STATE";
    public static final String KEY_PARAM_SEARCH_KEYWORD = "KEY_PARAM_SEARCH_KEYWORD";
    public static final String KEY_PARAM_SEARCH_PAGE    = "KEY_PARAM_SEARCH_PAGE";

    public static final int STATE_NONE                              =  0;
    public static final int STATE_SEARCH_BOOKS_SEARCH_INCOMPLETE    =  1;
    public static final int STATE_SEARCH_BOOKS_SEARCH_COMPLETE      =  2;
    public static final int STATE_NEW_BOOKS_RELOAD_INCOMPLETE       =  3;
    public static final int STATE_NEW_BOOKS_RELOAD_COMPLETE         =  4;
    public static final int STATE_EXPORT_INCOMPLETE                 =  5;
    public static final int STATE_EXPORT_COMPLETE                   =  6;
    public static final int STATE_IMPORT_INCOMPLETE                 =  7;
    public static final int STATE_IMPORT_COMPLETE                   =  8;
    public static final int STATE_BACKUP_INCOMPLETE                 =  9;
    public static final int STATE_BACKUP_COMPLETE                   = 10;
    public static final int STATE_RESTORE_INCOMPLETE                = 11;
    public static final int STATE_RESTORE_COMPLETE                  = 12;
    public static final int STATE_DROPBOX_LOGIN                     = 13;

    private static final int notifyId = 1;
    private NotificationManager mNotificationManager;
    private LocalBroadcastManager mLocalBroadcastManager;
    private MyBookshelfApplicationData mApplicationData;
    private SearchBooksThread searchBooksThread;
    private SearchBooksThread.Result mSearchBooksResult;
    private NewBooksThread newBooksThread;
    private NewBooksThread.Result mNewBooksResult;
    private FileBackupThread fileBackupThread;
    private FileBackupThread.Result mFileBackupResult;

    private String mParamSEARCH_KEYWORD;
    private int mParamSEARCH_PAGE;
    private int mState;
    private boolean isForeground;


    public class MBinder extends Binder {
        public BookService getService() {
            return BookService.this;
        }
    }

    public BookService() {
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
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (D) Log.d(TAG, "onUnbind");
        return true;
    }

    @Override
    public void onCreate() {
        if (D) Log.d(TAG, "onCreate");
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mApplicationData = (MyBookshelfApplicationData) this.getApplicationContext();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


    @Override
    public void onDestroy() {
        if (D) Log.d(TAG, "onDestroy");
        switch(mState) {
            case STATE_SEARCH_BOOKS_SEARCH_INCOMPLETE:
                cancelSearch();
                break;
            case STATE_NEW_BOOKS_RELOAD_INCOMPLETE:
                cancelReload();
                break;
            case STATE_EXPORT_INCOMPLETE:
            case STATE_IMPORT_INCOMPLETE:
            case STATE_BACKUP_INCOMPLETE:
            case STATE_RESTORE_INCOMPLETE:
                cancelBackup();
                break;
        }
     }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (D) Log.d(TAG, "onStartCommand");
        Notification notification = createNotification(mState);
        isForeground = true;
        startForeground(notifyId, notification);
        return START_NOT_STICKY;
    }


    @Override
    public void deliverSearchBooksResult(SearchBooksThread.Result result) {
        if(mState == STATE_SEARCH_BOOKS_SEARCH_INCOMPLETE){
            mSearchBooksResult = result;
            if (result.isSuccess()) {
                mApplicationData.registerToSearchBooks(result.getBooks());
            }
            setServiceState(STATE_SEARCH_BOOKS_SEARCH_COMPLETE);
        }else{
            if (D) Log.d(TAG, "Illegal State");
            setServiceState(STATE_NONE);
        }
    }

    @Override
    public void deliverNewBooksResult(NewBooksThread.Result result) {
        if(mState == STATE_NEW_BOOKS_RELOAD_INCOMPLETE) {
            mNewBooksResult = result;
            if (result.isSuccess()) {
                mApplicationData.registerToNewBooks(result.getBooks());
            }
            setServiceState(STATE_NEW_BOOKS_RELOAD_COMPLETE);
        }else{
            if (D) Log.d(TAG, "Illegal State");
            setServiceState(STATE_NONE);
        }
    }

    @Override
    public void deliverBackupResult(FileBackupThread.Result result) {
        switch (mState) {
            case STATE_EXPORT_INCOMPLETE:
                mFileBackupResult = result;
                setServiceState(STATE_EXPORT_COMPLETE);
                break;
            case STATE_IMPORT_INCOMPLETE:
                mFileBackupResult = result;
                setServiceState(STATE_IMPORT_COMPLETE);
                break;
            case STATE_BACKUP_INCOMPLETE:
                if (result.isSuccess()) {
                    switch (result.getType()) {
                        case FileBackupThread.TYPE_EXPORT:
                            fileBackupThread = new FileBackupThread(this, FileBackupThread.TYPE_BACKUP);
                            fileBackupThread.start();
                            break;
                        case FileBackupThread.TYPE_BACKUP:
                            mFileBackupResult = result;
                            setServiceState(STATE_BACKUP_COMPLETE);
                            break;
                    }
                } else {
                    mFileBackupResult = result;
                    setServiceState(STATE_BACKUP_COMPLETE);
                }
                break;
            case STATE_RESTORE_INCOMPLETE:
                if (result.isSuccess()) {
                    switch (result.getType()) {
                        case FileBackupThread.TYPE_RESTORE:
                            fileBackupThread = new FileBackupThread(this, FileBackupThread.TYPE_IMPORT);
                            fileBackupThread.start();
                            break;
                        case FileBackupThread.TYPE_IMPORT:
                            mFileBackupResult = result;
                            setServiceState(STATE_RESTORE_COMPLETE);
                            break;
                    }
                } else {
                    mFileBackupResult = result;
                    setServiceState(STATE_RESTORE_COMPLETE);
                }
                break;
            default:
                if (D) Log.d(TAG, "Illegal State");
                setServiceState(STATE_NONE);
                break;
        }
    }

    public void startAuthenticate(){
        Auth.startOAuth2Authentication(this,DROP_BOX_KEY);
    }

    public String getAccessToken(){
        return Auth.getOAuth2Token();
    }


    public void setServiceState(int state){
        this.mState = state;
        Intent intent = new Intent();
        intent.putExtra(BaseFragment.KEY_UPDATE_SERVICE_STATE, state);
        intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_SERVICE_STATE);
        mLocalBroadcastManager.sendBroadcast(intent);
        updateNotification(mState);
    }

    public int getServiceState(){
        return mState;
    }

    private void setSearchParam(String keyword, int page){
        this.mParamSEARCH_KEYWORD = keyword;
        this.mParamSEARCH_PAGE = page;
    }

    public String getSearchKeyword(){
        return this.mParamSEARCH_KEYWORD;
    }

    public int getSearchPage(){
        return this.mParamSEARCH_PAGE;
    }

    public void cancelForeground(){
        isForeground = false;
        stopForeground(true);
    }

    public void cancelSearch(){
        if(searchBooksThread != null) {
            searchBooksThread.cancel();
            setServiceState(STATE_NONE);
            searchBooksThread = null;
        }
    }

    public void cancelReload(){
        if(newBooksThread != null){
            newBooksThread.cancel();
            setServiceState(STATE_NONE);
            newBooksThread = null;
        }
    }

    public void cancelBackup(){
        if(fileBackupThread != null){
            fileBackupThread.cancel();
            setServiceState(STATE_NONE);
            fileBackupThread = null;
        }
    }

    public void searchBooks(final String keyword, final int page){
        setServiceState(STATE_SEARCH_BOOKS_SEARCH_INCOMPLETE);
        setSearchParam(keyword, page);
        searchBooksThread = new SearchBooksThread(this, keyword, page, mApplicationData.getSearchBooksOrder());
        searchBooksThread.start();
    }

    public SearchBooksThread.Result getSearchBooksResult(){
        if(mSearchBooksResult == null){
            return SearchBooksThread.Result.error(SearchBooksThread.ERROR_UNKNOWN, "get result failed");
        }
        return mSearchBooksResult;
    }

    public void reloadNewBooks(final List<String> authors){
        setServiceState(STATE_NEW_BOOKS_RELOAD_INCOMPLETE);
        newBooksThread = new NewBooksThread(this, authors);
        newBooksThread.start();
    }

    public NewBooksThread.Result getNewBooksResult(){
        if(mNewBooksResult == null){
            return NewBooksThread.Result.error(NewBooksThread.ERROR_UNKNOWN, "get result failed");
        }
        return mNewBooksResult;
    }

    public void fileBackup(int type){
        switch(type){
            case FileBackupThread.TYPE_EXPORT:
                setServiceState(STATE_EXPORT_INCOMPLETE);
                fileBackupThread = new FileBackupThread(this, FileBackupThread.TYPE_EXPORT);
                break;
            case FileBackupThread.TYPE_IMPORT:
                setServiceState(STATE_IMPORT_INCOMPLETE);
                fileBackupThread = new FileBackupThread(this, FileBackupThread.TYPE_IMPORT);
                break;
            case FileBackupThread.TYPE_BACKUP:
                setServiceState(STATE_BACKUP_INCOMPLETE);
                fileBackupThread = new FileBackupThread(this, FileBackupThread.TYPE_EXPORT);
                break;
            case FileBackupThread.TYPE_RESTORE:
                setServiceState(STATE_RESTORE_INCOMPLETE);
                fileBackupThread = new FileBackupThread(this, FileBackupThread.TYPE_RESTORE);
                break;
            default:
                if (D) Log.d(TAG, "Illegal Type");
                setServiceState(STATE_NONE);
                break;
        }

        fileBackupThread.start();
    }

    public FileBackupThread.Result getFileBackupResult(){
        if(mFileBackupResult == null){
            return FileBackupThread.Result.error(FileBackupThread.TYPE_UNKNOWN,FileBackupThread.ERROR_UNKNOWN,"get result failed");
        }
        return mFileBackupResult;
    }

    private void updateNotification(int state) {
        if(mNotificationManager != null && isForeground) {
            Notification notification = createNotification(state);
            mNotificationManager.notify(notifyId,notification);
        }
    }

    private Notification createNotification(int state){
        Notification notification;
        String title = getString(R.string.app_name);
        String message = getString(R.string.notification_channel_title);
        int iconId  = R.drawable.ic_shelf_24dp;
        switch (state) {
            case STATE_NONE:
                break;
            case STATE_SEARCH_BOOKS_SEARCH_INCOMPLETE:
                message = getString(R.string.notification_message_search_incomplete);
                iconId  = R.drawable.ic_search_24dp;
                break;
            case STATE_SEARCH_BOOKS_SEARCH_COMPLETE:
                message = getString(R.string.notification_message_search_complete);
                iconId  = R.drawable.ic_search_24dp;
                break;
            case STATE_NEW_BOOKS_RELOAD_INCOMPLETE:
                message = getString(R.string.notification_message_reload_incomplete);
                iconId  = R.drawable.ic_reload_24dp;
                break;
            case STATE_NEW_BOOKS_RELOAD_COMPLETE:
                message = getString(R.string.notification_message_reload_complete);
                iconId  = R.drawable.ic_reload_24dp;
                break;
            case STATE_EXPORT_INCOMPLETE:
                message = getString(R.string.notification_message_export_incomplete);
                iconId  = R.drawable.ic_file_download_24dp;
                break;
            case STATE_EXPORT_COMPLETE:
                message = getString(R.string.notification_message_export_complete);
                iconId  = R.drawable.ic_file_download_24dp;
                break;
            case STATE_IMPORT_INCOMPLETE:
                message = getString(R.string.notification_message_import_incomplete);
                iconId  = R.drawable.ic_file_download_24dp;
                break;
            case STATE_IMPORT_COMPLETE:
                message = getString(R.string.notification_message_import_complete);
                iconId  = R.drawable.ic_file_download_24dp;
                break;
            case STATE_BACKUP_INCOMPLETE:
                message = getString(R.string.notification_message_backup_incomplete);
                iconId  = R.drawable.ic_file_download_24dp;
                break;
            case STATE_BACKUP_COMPLETE:
                message = getString(R.string.notification_message_backup_complete);
                iconId  = R.drawable.ic_file_download_24dp;
                break;
            case STATE_RESTORE_INCOMPLETE:
                message = getString(R.string.notification_message_reload_incomplete);
                iconId  = R.drawable.ic_file_download_24dp;
                break;
            case STATE_RESTORE_COMPLETE:
                message = getString(R.string.notification_message_reload_complete);
                iconId  = R.drawable.ic_file_download_24dp;
                break;
            case STATE_DROPBOX_LOGIN:
                message = getString(R.string.notification_message_login_dropbox);
                iconId  = R.drawable.ic_file_download_24dp;
                break;

        }

        Intent ni = new Intent();
        ni.addCategory(Intent.CATEGORY_LAUNCHER);
        ni.setClassName(getApplicationContext().getPackageName(), MainActivity.class.getName());
        ni.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, ni, PendingIntent.FLAG_CANCEL_CURRENT);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(getApplicationContext(), getString(R.string.notification_channel_id))
                    .setContentTitle(title)
                    .setSmallIcon(iconId)
                    .setContentText(message)
                    .setContentIntent(pi)
                    .build();
        } else {
            notification = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                    .setContentTitle(title)
                    .setSmallIcon(iconId)
                    .setContentText(message)
                    .setContentIntent(pi)
                    .build();
        }
        return notification;
    }


}