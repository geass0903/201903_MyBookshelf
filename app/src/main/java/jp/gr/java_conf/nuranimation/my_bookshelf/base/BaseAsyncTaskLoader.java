package jp.gr.java_conf.nuranimation.my_bookshelf.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;


public abstract class  BaseAsyncTaskLoader<D> extends AsyncTaskLoader<D> {
    private D mResult;
    private boolean mIsStarted = false;

    public BaseAsyncTaskLoader(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mResult != null) {
            deliverResult(mResult);
            return;
        }
        if (!mIsStarted || takeContentChanged()) {
            forceLoad();
        }
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        mIsStarted = true;
    }

    @Override
    public void deliverResult(D data) {
        mResult = data;
        super.deliverResult(data);
    }

}
