package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.List;

public class MyBookshelfApplicationData extends MultiDexApplication {
    private static final String TAG = MyBookshelfApplicationData.class.getSimpleName();
    private static final boolean D = true;

    private MyBookshelfDBOpenHelper mDatabaseHelper;
    List<BookData> mBooksListShelf;
    List<BookData> mBooksListSearch;
    List<BookData> mBooksListNew;
    String ARG_SEARCH_KEYWORD;
    String ARG_TMP_KEYWORD;
    int ARG_SEARCH_PAGE;

    @Override
    public void onCreate(){
        super.onCreate();
        if(D) Log.d(TAG,"onCreate");
        Fresco.initialize(this);
        registerActivityLifecycleCallbacks(new LifecycleHandler());
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
        mBooksListShelf = mDatabaseHelper.getMyShelf();
        mBooksListNew = mDatabaseHelper.getNewBooks();
        mBooksListSearch = new ArrayList<>();
        ARG_SEARCH_KEYWORD = "";
        ARG_TMP_KEYWORD = "";
        ARG_SEARCH_PAGE = 1;
    }

    public void clearData(){
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        if(db != null && db.isOpen()){
            db.close();
        }
        mDatabaseHelper = null;
        mBooksListShelf.clear();
        mBooksListNew.clear();
        mBooksListSearch.clear();
    }


    public MyBookshelfDBOpenHelper getDatabaseHelper(){
        return mDatabaseHelper;
    }


    public List<BookData> getBooksListShelf(){
        return mBooksListShelf;
    }

    public void updateBooksListShelf(){
        mBooksListShelf = mDatabaseHelper.getMyShelf();
    }

    public List<BookData> getBooksListNew(){
        return mBooksListNew;
    }

    public void updateBooksListNew(){
        mBooksListNew = mDatabaseHelper.getNewBooks();
    }

    public List<BookData> getBooksListSearch(){
        return mBooksListSearch;
    }

    public void saveSearchKeyword(String keyword){
        ARG_SEARCH_KEYWORD = keyword;
    }

    public String loadSearchKeyword(){
        return ARG_SEARCH_KEYWORD;
    }

    public void saveTmpKeyword(String keyword){
        ARG_TMP_KEYWORD = keyword;
    }

    public String loadTmpKeyword(){
        return ARG_TMP_KEYWORD;
    }

    public void saveSearchPage(int page){
        ARG_SEARCH_PAGE = page;
    }

    public int loadSearchPage(){
        return ARG_SEARCH_PAGE;
    }

}
