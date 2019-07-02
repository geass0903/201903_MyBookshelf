package jp.gr.java_conf.nuranimation.my_bookshelf.service;

import android.app.Notification;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.model.database.MyBookshelfDBOpenHelper;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BooksOrder;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.Result;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.SearchParam;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.net.base.BaseThread;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.net.FileBackupThread;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.net.NewBooksThread;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.net.SearchBooksThread;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.prefs.MyBookshelfPreferences;
import jp.gr.java_conf.nuranimation.my_bookshelf.service.base.BaseService;


public class BookService extends BaseService implements BaseThread.ThreadFinishListener {
    public static final String TAG = BookService.class.getSimpleName();
    private static final boolean D = true;

    public static final int STATE_NONE = 0;
    public static final int STATE_SEARCH_BOOKS_SEARCH_INCOMPLETE = 1;
    public static final int STATE_SEARCH_BOOKS_SEARCH_COMPLETE = 2;
    public static final int STATE_NEW_BOOKS_RELOAD_INCOMPLETE = 3;
    public static final int STATE_NEW_BOOKS_RELOAD_COMPLETE = 4;
    public static final int STATE_EXPORT_INCOMPLETE = 5;
    public static final int STATE_EXPORT_COMPLETE = 6;
    public static final int STATE_IMPORT_INCOMPLETE = 7;
    public static final int STATE_IMPORT_COMPLETE = 8;
    public static final int STATE_BACKUP_INCOMPLETE = 9;
    public static final int STATE_BACKUP_COMPLETE = 10;
    public static final int STATE_RESTORE_INCOMPLETE = 11;
    public static final int STATE_RESTORE_COMPLETE = 12;
    public static final int STATE_DROPBOX_LOGIN = 13;

    private MyBookshelfDBOpenHelper mDBOpenHelper;
    private MyBookshelfPreferences mPreferences;
    private SearchBooksThread searchBooksThread;
    private NewBooksThread newBooksThread;
    private FileBackupThread fileBackupThread;

    private SearchParam mSearchParam;
    private Result mResult;

    public class MBinder extends Binder {
        public BookService getService() {
            return BookService.this;
        }
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
        super.onCreate();
        mDBOpenHelper = MyBookshelfDBOpenHelper.getInstance(this.getApplicationContext());
        mPreferences = new MyBookshelfPreferences(this.getApplicationContext());
    }


    @Override
    public void onDestroy() {
        if (D) Log.d(TAG, "onDestroy");
        switch (getServiceState()) {
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
    public void deliverResult(Result result) {
        mResult = result;
        switch (getServiceState()) {
            case STATE_SEARCH_BOOKS_SEARCH_INCOMPLETE:
                if (result.isSuccess()) {
                    mDBOpenHelper.registerToSearchBooks(result.getBooks());
                }
                setServiceState(STATE_SEARCH_BOOKS_SEARCH_COMPLETE);
                break;
            case STATE_NEW_BOOKS_RELOAD_INCOMPLETE:
                if (result.isSuccess()) {
                    mDBOpenHelper.dropTableNewBooks();
                    mDBOpenHelper.registerToNewBooks(result.getBooks());
                }
                setServiceState(STATE_NEW_BOOKS_RELOAD_COMPLETE);
                break;
            case STATE_EXPORT_INCOMPLETE:
                setServiceState(STATE_EXPORT_COMPLETE);
                break;
            case STATE_IMPORT_INCOMPLETE:
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
                            setServiceState(STATE_BACKUP_COMPLETE);
                            break;
                    }
                } else {
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
                            setServiceState(STATE_RESTORE_COMPLETE);
                            break;
                    }
                } else {
                    setServiceState(STATE_RESTORE_COMPLETE);
                }
                break;
            default:
                if (D) Log.d(TAG, "Illegal State");
                setServiceState(STATE_NONE);
                break;
        }
    }


    public SearchParam getSearchParam() {
        return mSearchParam;
    }

    public Result getResult() {
        if (mResult == null) {
            return Result.Error("get result failed");
        }
        return mResult;
    }

    private void clearResult(){
        mResult = null;
    }

    public void searchBooks(final SearchParam searchParam) {
        setServiceState(STATE_SEARCH_BOOKS_SEARCH_INCOMPLETE);
        clearResult();
        mSearchParam = searchParam;
        searchBooksThread = new SearchBooksThread(this, searchParam, BooksOrder.getSearchBooksOrder(mPreferences.getSearchBooksOrderCode()));
        searchBooksThread.start();
    }

    public void cancelSearch() {
        if (searchBooksThread != null) {
            searchBooksThread.cancel();
            setServiceState(STATE_NONE);
            searchBooksThread = null;
        }
    }

    public void reloadNewBooks(final List<String> authors) {
        setServiceState(STATE_NEW_BOOKS_RELOAD_INCOMPLETE);
        clearResult();
        newBooksThread = new NewBooksThread(this, authors);
        newBooksThread.start();
    }

    public void cancelReload() {
        if (newBooksThread != null) {
            newBooksThread.cancel();
            setServiceState(STATE_NONE);
            newBooksThread = null;
        }
    }

    public void fileBackup(int type) {
        clearResult();
        switch (type) {
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

    public void cancelBackup() {
        if (fileBackupThread != null) {
            fileBackupThread.cancel();
            setServiceState(STATE_NONE);
            fileBackupThread = null;
        }
    }

    protected Notification createNotification(int state) {
        Notification notification;
        NotificationParam notificationParam = NotificationParam.createNotificationParam(this, state);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(getApplicationContext(), notificationParam.getChannelId())
                    .setContentTitle(notificationParam.getTitle())
                    .setSmallIcon(notificationParam.getIconId())
                    .setContentText(notificationParam.getMessage())
                    .setContentIntent(notificationParam.getPendingIntent())
                    .build();
        } else {
            notification = new NotificationCompat.Builder(this, notificationParam.getChannelId())
                    .setContentTitle(notificationParam.getTitle())
                    .setSmallIcon(notificationParam.getIconId())
                    .setContentText(notificationParam.getMessage())
                    .setContentIntent(notificationParam.getPendingIntent())
                    .build();
        }
        return notification;
    }


}