package jp.gr.java_conf.nuranimation.my_bookshelf.model.base;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.Result;


public class BaseThread extends Thread{
    public static final int TYPE_UNKNOWN        = 0;
    public static final int TYPE_SEARCH_BOOKS   = 1;
    public static final int TYPE_NEW_BOOKS      = 2;
    public static final int TYPE_EXPORT         = 3;
    public static final int TYPE_IMPORT         = 4;
    public static final int TYPE_BACKUP         = 5;
    public static final int TYPE_RESTORE        = 6;

    public static final String FILTER_ACTION_UPDATE_PROGRESS = "BaseThread.FILTER_ACTION_UPDATE_PROGRESS";
    public static final String KEY_PROGRESS_MESSAGE_TEXT = "BaseThread.KEY_PROGRESS_MESSAGE_TEXT";
    public static final String KEY_PROGRESS_VALUE_TEXT = "BaseThread.KEY_PROGRESS_VALUE_TEXT";

    private ThreadFinishListener mListener;
    private LocalBroadcastManager mLocalBroadcastManager;

    private boolean isCanceled;

    public interface ThreadFinishListener {
        void deliverResult(int type, Result result);
    }



    protected BaseThread(Context context){
        this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        if (context instanceof ThreadFinishListener) {
            mListener = (ThreadFinishListener) context;
        } else {
            throw new UnsupportedOperationException("Listener is not Implementation.");
        }
        isCanceled = false;
    }

    public void cancel() {
        isCanceled = true;
    }


    protected ThreadFinishListener getThreadFinishListener(){
        return mListener;
    }

    protected boolean isCanceled(){
        return isCanceled;
    }


    protected void updateProgress(String message, String progress) {
        Intent intent = new Intent();
        intent.setAction(FILTER_ACTION_UPDATE_PROGRESS);
        intent.putExtra(KEY_PROGRESS_MESSAGE_TEXT, message);
        intent.putExtra(KEY_PROGRESS_VALUE_TEXT, progress);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

}
