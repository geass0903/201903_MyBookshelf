package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.ArrayList;
import java.util.List;

public class MyBookshelfApplicationData extends Application {
    private static final String TAG = MyBookshelfApplicationData.class.getSimpleName();
    private static final boolean D = true;

    SQLiteDatabase mDatabase;
    List<BookData> mBooksListShelf;
    List<BookData> mBooksListSearch;
    String ARG_SEARCH_WORD;
    int ARG_SEARCH_PAGE;

    @Override
    public void onCreate(){
        super.onCreate();
        if(D) Log.d(TAG,"onCreate");
        Fresco.initialize(this);
        registerActivityLifecycleCallbacks(new LifecycleHandler());
        MyBookshelfDBOpenHelper helper = new MyBookshelfDBOpenHelper(getApplicationContext());
        mDatabase = helper.getReadableDatabase();

        mBooksListShelf = new ArrayList<>();
        mBooksListSearch = new ArrayList<>();
        ARG_SEARCH_WORD = "";
        ARG_SEARCH_PAGE = 1;
    }

    @Override
    public void onTerminate(){
        super.onTerminate();
        if(D) Log.d(TAG,"onTerminate");
        mDatabase.close();
        mBooksListShelf = null;
        mBooksListSearch = null;
    }


    public SQLiteDatabase getDataBase(){
        return mDatabase;
    }

    public List<BookData> getmBooksListShelf(){
        return mBooksListShelf;
    }

    public List<BookData> getmBooksListSearch(){
        return mBooksListSearch;
    }

    public void setSearchWord(String word){
        ARG_SEARCH_WORD = word;
    }

    public String getSearchWord(){
        return ARG_SEARCH_WORD;
    }

    public void setSearchPage(int page){
        ARG_SEARCH_PAGE = page;
    }

    public int getSearchPage(){
        return ARG_SEARCH_PAGE;
    }

}
