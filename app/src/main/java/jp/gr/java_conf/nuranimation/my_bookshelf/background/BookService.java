package jp.gr.java_conf.nuranimation.my_bookshelf.background;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import jp.gr.java_conf.nuranimation.my_bookshelf.MainActivity;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.SearchBooksResult;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;

public class BookService extends Service implements SearchBooksThread.ThreadFinishListener {
    public static final String TAG = BookService.class.getSimpleName();
    private static final boolean D = true;

    public static final String KEY_SERVICE_STATE = "KEY_SERVICE_STATE";
    public static final String KEY_PARAM_SEARCH_KEYWORD = "KEY_PARAM_SEARCH_KEYWORD";
    public static final String KEY_PARAM_SEARCH_PAGE    = "KEY_PARAM_SEARCH_PAGE";



    public static final int STATE_NONE                          = 0;
    public static final int STATE_SEARCH_BOOKS_SEARCH_START     = 1;
    public static final int STATE_SEARCH_BOOKS_SEARCH_FINISH    = 2;
    public static final int STATE_NEW_BOOKS_RELOAD_START        = 3;
    public static final int STATE_NEW_BOOKS_RELOAD_FINISH       = 4;
    private int mState;

    private String mParamSEARCH_KEYWORD;
    private int mParamSEARCH_PAGE;

    private LocalBroadcastManager mLocalBroadcastManager;
    private MyBookshelfApplicationData mApplicationData;


    private SearchBooksResult mSearchBooksResult = new SearchBooksResult();



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
        stopForeground(true);
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
    }


    @Override
    public void onDestroy() {
        if (D) Log.d(TAG, "onDestroy");
     }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (D) Log.d(TAG, "onStartCommand");
        String channelId = getString(R.string.Notification_Channel_ID);
        String title = getString(R.string.Notification_Title_Search);
        // 通知設定

        Intent ni = new Intent();
        ni.addCategory(Intent.CATEGORY_LAUNCHER);
        ni.setClassName(getApplicationContext().getPackageName(),MainActivity.class.getName());
        ni.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),0,ni,PendingIntent.FLAG_CANCEL_CURRENT);




        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {


        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel =
                null;

            channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
                Notification notification = new Notification.Builder(getApplicationContext(), channelId)
                        .setContentTitle(title)
                        .setSmallIcon(R.drawable.ic_vector_reload_24dp)
                        .setContentText(getString(R.string.Notification_Message_Search_Start))
                        .setContentIntent(pi)
                        .build();

                // フォアグラウンドで実行
                startForeground(1, notification);
            }
        }else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
            builder.setContentTitle(title)
                    .setSmallIcon(R.drawable.ic_vector_reload_24dp)
                    .setContentText(getString(R.string.Notification_Message_Search_Start))
                    .setContentIntent(pi);
            startForeground(1, builder.build());


        }


        return START_NOT_STICKY;
    }


    @Override
    public void deliverResult(SearchBooksResult result) {
        if (D) Log.d(TAG, "deliverResult");
        setSearchBooksResult(result);
        setServiceState(STATE_SEARCH_BOOKS_SEARCH_FINISH);
    }


    public void searchBooks(final String keyword, final int page, final String sort){
        setServiceState(STATE_SEARCH_BOOKS_SEARCH_START);
        setSearchKeyword(keyword);
        setSearchPage(page);
        SearchBooksThread thread = new SearchBooksThread(this,keyword,page,sort);
        thread.start();
    }










    public void setServiceState(int state){
        this.mState = state;
        Intent intent = new Intent();
        intent.putExtra(BaseFragment.KEY_UPDATE_SERVICE_STATE, state);
        intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_SERVICE_STATE);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    public int getServiceState(){
        return mState;
    }

    public void setSearchKeyword(String keyword){
        this.mParamSEARCH_KEYWORD = keyword;
    }

    public String getSearchKeyword(){
        return this.mParamSEARCH_KEYWORD;
    }

    public void setSearchPage(int page){
        this.mParamSEARCH_PAGE = page;
    }

    public int getSearchPage(){
        return this.mParamSEARCH_PAGE;
    }













    public void setSearchBooksResult(SearchBooksResult result){
        mSearchBooksResult = new SearchBooksResult(result);
    }

    public SearchBooksResult getSearchBooksResult(){
        return mSearchBooksResult;
    }



}