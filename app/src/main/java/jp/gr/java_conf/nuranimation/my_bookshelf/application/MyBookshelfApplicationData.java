package jp.gr.java_conf.nuranimation.my_bookshelf.application;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;


@SuppressWarnings({"WeakerAccess","unused"})
public class MyBookshelfApplicationData extends Application {
    private static final String TAG = MyBookshelfApplicationData.class.getSimpleName();
    private static final boolean D = true;

    private static final String PREFERENCE_NAME = "MyBookshelfPreference";
    public static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
    public static final String KEY_SHELF_BOOKS_ORDER = "KEY_SHELF_BOOKS_ORDER";
    public static final String KEY_SEARCH_BOOKS_ORDER = "KEY_SEARCH_BOOKS_ORDER";


    private SharedPreferences mPreferences;
    private static final String[] Use_Permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private boolean isCheckedPermissions;
    private UpdateBookData mUpdateBookData;

    private MyBookshelfDBOpenHelper mDatabaseHelper;



    @Override
    public void onCreate(){
        super.onCreate();
        if(D) Log.d(TAG,"onCreate");
        Fresco.initialize(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannelManager.create(getApplicationContext(),getString(R.string.Notification_Channel_ID), R.string.Notification_Channel_Title, R.string.Notification_Channel_Description);
        }

        mPreferences = getSharedPreferences(PREFERENCE_NAME,Context.MODE_PRIVATE);
        mDatabaseHelper = new MyBookshelfDBOpenHelper(getApplicationContext());
        isCheckedPermissions = false;
    }

    @Override
    public void onTerminate(){
        super.onTerminate();
        if(D) Log.d(TAG,"onTerminate");
    }


    public SharedPreferences getSharedPreferences(){
        return mPreferences;
    }

    public String[] getUse_Permissions(){
        return Use_Permissions;
    }

    public boolean isCheckedPermissions(){
        return isCheckedPermissions;
    }

    public void setCheckedPermissions(boolean flag){
        isCheckedPermissions = flag;
    }

    public MyBookshelfDBOpenHelper getDatabaseHelper(){
        return mDatabaseHelper;
    }




    public void dropTableAuthorsList(){
        mDatabaseHelper.dropTableAuthorsList();
    }

    public void registerToAuthorsList(String author) {
        mDatabaseHelper.registerToAuthorsList(author);
    }

    public void registerToAuthorsList(List<String> authors){
        mDatabaseHelper.registerToAuthorsList(authors);
    }

    public List<String> loadAuthorsList(){
        return mDatabaseHelper.loadAuthorsList();
    }




    public void dropTableShelfBooks(){
        mDatabaseHelper.dropTableShelfBooks();
    }

    public void registerToShelfBooks(BookData book){
        mDatabaseHelper.registerToShelfBooks(book);
    }

    public void registerToShelfBooks(List<BookData> books){
        mDatabaseHelper.registerToShelfBooks(books);
    }

    public List<BookData> loadShelfBooks(String keyword){
        return mDatabaseHelper.loadShelfBooks(keyword);
    }

    public BookData loadBookDataFromShelfBooks(BookData book){
        return mDatabaseHelper.loadBookDataFromShelfBooks(book);
    }

    public void unregisterFromShelfBooks(BookData book){
        mDatabaseHelper.unregisterFromShelfBooks(book);
    }




    public void dropTableSearchBooks(){
        mDatabaseHelper.dropTableSearchBooks();
    }

    public void registerToSearchBooks(BookData book){
        mDatabaseHelper.registerToSearchBooks(book);
    }

    public void registerToSearchBooks(List<BookData> books){
        mDatabaseHelper.registerToSearchBooks(books);
    }

    public List<BookData> loadSearchBooks(){
        return mDatabaseHelper.loadSearchBooks();
    }

    public BookData loadBookDataFromSearchBooks(BookData book){
        return mDatabaseHelper.loadBookDataFromSearchBooks(book);
    }

    public void unregisterFromSearchBooks(BookData book){
        mDatabaseHelper.unregisterFromSearchBooks(book);
    }



    public void dropTableNewBooks(){
        mDatabaseHelper.dropTableNewBooks();
    }

    public void registerToNewBooks(BookData book){
        mDatabaseHelper.registerToNewBooks(book);
    }

    public void registerToNewBooks(List<BookData> books){
        mDatabaseHelper.registerToNewBooks(books);
    }

    public List<BookData> loadNewBooks(){
        return mDatabaseHelper.loadNewBooks();
    }

    public BookData loadBookDataFromNewBooks(BookData book){
        return mDatabaseHelper.loadBookDataFromNewBooks(book);
    }

    public void unregisterFromNewBooks(BookData book){
        mDatabaseHelper.unregisterFromNewBooks(book);
    }


    public String getShelfBooksSortSetting(){
        String sort = mPreferences.getString(KEY_SHELF_BOOKS_ORDER,null);
        if(TextUtils.isEmpty(sort)){
            sort = getString(R.string.ShelfBooks_SortSetting_Code_SalesDate_Descending);
            mPreferences.edit().putString(KEY_SHELF_BOOKS_ORDER,getString(R.string.ShelfBooks_SortSetting_Code_SalesDate_Descending)).apply();
        }
        return sort;
    }

    public String getSearchBooksSortSetting(){
        String sort = mPreferences.getString(KEY_SEARCH_BOOKS_ORDER,null);
        if(TextUtils.isEmpty(sort)){
            sort = getString(R.string.SearchBooks_SortSetting_Code_SalesDate_Descending);
            mPreferences.edit().putString(KEY_SEARCH_BOOKS_ORDER,getString(R.string.SearchBooks_SortSetting_Code_SalesDate_Descending)).apply();
        }
        return sort;
    }



    public static final class UpdateBookData {
        private final int position;
        private final BookData book;

        private UpdateBookData(int position, BookData book) {
            this.position = position;
            this.book = book;
        }

        public BookData getBookData(){
            return new BookData(book);
        }

        public int getPosition(){
            return position;
        }

        public static UpdateBookData set(int position, BookData book){
            return new UpdateBookData(position, book);
        }

    }







}
