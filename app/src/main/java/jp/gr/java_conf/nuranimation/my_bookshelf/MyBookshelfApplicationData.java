package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.List;

public class MyBookshelfApplicationData extends MultiDexApplication {
    private static final String TAG = MyBookshelfApplicationData.class.getSimpleName();
    private static final boolean D = true;

    private static final String PreferenceName = "MyBookshelfPreference";
    static final String Key_Access_Token = "Key_Access_Token";
    static final String Key_SortSetting_Bookshelf = "Key_SortSetting_Bookshelf";
    static final String Key_SortSetting_SearchResult = "Key_SortSetting_SearchResult";
    static final String Key_isCheckedPermissions = "Key_isCheckedPermissions";

    private SharedPreferences mPreferences;

    private static final String[] Use_Permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private boolean isCheckedPermissions = false;

    private MyBookshelfDBOpenHelper mDatabaseHelper;
    private List<BookData> mList_Bookshelf;
    private List<BookData> mList_NewBooks;


    @Override
    public void onCreate(){
        super.onCreate();
        if(D) Log.d(TAG,"onCreate");
        Fresco.initialize(this);
        mPreferences = getSharedPreferences(PreferenceName,Context.MODE_PRIVATE);
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

    public SharedPreferences getSharedPreferences(){
        return mPreferences;
    }

    public String[] getUse_Permissions(){
        return Use_Permissions;
    }

    public void checkedPermissions(boolean flag){
        mPreferences.edit().putBoolean(Key_isCheckedPermissions,flag).apply();
    }

    public boolean isCheckedPermissions(){
        return mPreferences.getBoolean(Key_isCheckedPermissions,false);
    }

}
