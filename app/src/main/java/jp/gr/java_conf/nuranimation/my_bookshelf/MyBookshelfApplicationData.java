package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.database.sqlite.SQLiteDatabase;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.List;

public class MyBookshelfApplicationData extends MultiDexApplication {
    private static final String TAG = MyBookshelfApplicationData.class.getSimpleName();
    private static final boolean D = true;

    private MyBookshelfDBOpenHelper mDatabaseHelper;
    private MyBookshelfPreferenceManager mPreferenceManager;
    private List<BookData> mList_Bookshelf;
    private List<BookData> mList_NewBooks;

    @Override
    public void onCreate(){
        super.onCreate();
        if(D) Log.d(TAG,"onCreate");
        Fresco.initialize(this);
        registerActivityLifecycleCallbacks(new LifecycleHandler());
        mPreferenceManager = new MyBookshelfPreferenceManager(getApplicationContext());
        mDatabaseHelper = new MyBookshelfDBOpenHelper(getApplicationContext());
        initData();
    }

    @Override
    public void onTerminate(){
        super.onTerminate();
        if(D) Log.d(TAG,"onTerminate");
        clearData();
    }


    public void initData(){
        updateList_MyBookshelf();
        updateList_NewBooks();
    }

    public void clearData(){
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        if(db != null && db.isOpen()){
            db.close();
        }
        mDatabaseHelper = null;
        mList_Bookshelf.clear();
        mList_NewBooks.clear();
    }


    public MyBookshelfDBOpenHelper getDatabaseHelper(){
        return mDatabaseHelper;
    }


    public List<BookData> getList_MyBookshelf(){
        return mList_Bookshelf;
    }

    public void updateList_MyBookshelf(){
        mList_Bookshelf = mDatabaseHelper.getMyBookshelf();
    }

    public List<BookData> getList_NewBooks(){
        return mList_NewBooks;
    }

    public void updateList_NewBooks(){
        mList_NewBooks = mDatabaseHelper.getNewBooks();
    }


    @SuppressWarnings({"unused","SameParameterValue"})
    boolean containsKey(String key){
        return mPreferenceManager.containsKey(key);
    }
    @SuppressWarnings({"unused","SameParameterValue"})
    void removeKey(String key){
        mPreferenceManager.removeKey(key);
    }

    @SuppressWarnings("unused")
    public void putIntPreference(String key, int value){
        mPreferenceManager.putInt(key,value);
    }
    @SuppressWarnings("unused")
    public void putStringPreference(String key, String value){
        mPreferenceManager.putString(key,value);
    }

    @SuppressWarnings("unused")
    public int getIntPreference(String key,int defValue){
        return mPreferenceManager.getInt(key,defValue);
    }

    @SuppressWarnings("unused")
    public String getStringPreference(String key,String defValue){
        return mPreferenceManager.getString(key,defValue);
    }

}
