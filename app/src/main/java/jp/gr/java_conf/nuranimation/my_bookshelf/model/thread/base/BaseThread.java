package jp.gr.java_conf.nuranimation.my_bookshelf.model.thread.base;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.Result;


public class BaseThread extends Thread{
    public static final String FILTER_ACTION_UPDATE_PROGRESS = "BaseThread.FILTER_ACTION_UPDATE_PROGRESS";
    public static final String KEY_PROGRESS_MESSAGE_TEXT     = "BaseThread.KEY_PROGRESS_MESSAGE_TEXT";
    public static final String KEY_PROGRESS_VALUE_TEXT       = "BaseThread.KEY_PROGRESS_VALUE_TEXT";

    private ThreadListener mListener;
    private LocalBroadcastManager mLocalBroadcastManager;
    private boolean isCanceled;

    public interface ThreadListener {
        void deliverResult(Result result);
    }

    protected BaseThread(Context context){
        isCanceled = false;
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        if (context instanceof ThreadListener) {
            mListener = (ThreadListener) context;
        } else {
            throw new UnsupportedOperationException("Listener is not Implementation.");
        }
    }

    public void cancel() {
        isCanceled = true;
    }


    protected ThreadListener getThreadListener(){
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
