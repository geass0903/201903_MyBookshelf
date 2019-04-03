package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;

public class MyBookshelfApplicationData extends Application {
    private static final String TAG = MyBookshelfApplicationData.class.getSimpleName();
    private static final boolean D = true;

    SQLiteDatabase mDatabase;

    @Override
    public void onCreate(){
        super.onCreate();
        if(D) Log.d(TAG,"onCreate");
        Fresco.initialize(this);
        registerActivityLifecycleCallbacks(new LifecycleHandler());
        MyBookshelfDBOpenHelper helper = new MyBookshelfDBOpenHelper(getApplicationContext());
        mDatabase = helper.getReadableDatabase();
    }

    @Override
    public void onTerminate(){
        super.onTerminate();
        if(D) Log.d(TAG,"onTerminate");
        mDatabase.close();
    }


    public SQLiteDatabase getDataBase(){
        return mDatabase;
    }


}
