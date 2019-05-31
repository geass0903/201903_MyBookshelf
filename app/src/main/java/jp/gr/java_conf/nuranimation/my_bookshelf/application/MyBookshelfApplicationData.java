package jp.gr.java_conf.nuranimation.my_bookshelf.application;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;


@SuppressWarnings({"WeakerAccess","unused","UnusedReturnValue"})
public class MyBookshelfApplicationData extends MultiDexApplication {
    private static final String TAG = MyBookshelfApplicationData.class.getSimpleName();
    private static final boolean D = true;

    private static final String PREFERENCE_NAME = "MyBookshelfPreference";
    public static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
    public static final String KEY_SHELF_BOOKS_ORDER = "KEY_SHELF_BOOKS_ORDER";
    public static final String KEY_SEARCH_BOOKS_ORDER = "KEY_SEARCH_BOOKS_ORDER";


    private SharedPreferences mPreferences;

    private static final String[] Use_Permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private boolean isCheckedPermissions;

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



    public void deleteTABLE_AUTHORS(){
        mDatabaseHelper.deleteTABLE_AUTHORS();
    }

    public List<String> getAuthors(){
        return mDatabaseHelper.getAuthors();
    }

    public void registerToAuthors(String author) {
        mDatabaseHelper.registerToAuthors(author);
    }

    public void registerToAuthors(List<String> authors){
        mDatabaseHelper.registerToAuthors(authors);
    }



    public void deleteTABLE_SHELF_BOOKS(){
        mDatabaseHelper.deleteTABLE_SHELF_BOOKS();
    }

    public List<BookData> getShelfBooks(String word){
        return mDatabaseHelper.getShelfBooks(word);
    }

    public BookData searchInShelfBooks(BookData book){
        return mDatabaseHelper.searchInShelfBooks(book);
    }

    public void registerToShelfBooks(BookData book){
        mDatabaseHelper.registerToShelfBooks(book);
    }

    public void registerToShelfBooks(List<BookData> books){
        mDatabaseHelper.registerToShelfBooks(books);
    }

    public void deleteFromShelfBooks(String ISBN){
        mDatabaseHelper.deleteFromShelfBooks(ISBN);
    }



    public void deleteTABLE_SEARCH_BOOKS(){
        mDatabaseHelper.deleteTABLE_SEARCH_BOOKS();
    }

    public List<BookData> getSearchBooks(){
        return mDatabaseHelper.getSearchBooks();
    }

    public boolean registerToSearchBooks(BookData book){
        return mDatabaseHelper.registerToSearchBooks(book);
    }

    public boolean registerToSearchBooks(List<BookData> books){
        return mDatabaseHelper.registerToSearchBooks(books);
    }

    public void deleteTABLE_NEW_BOOKS(){
        mDatabaseHelper.deleteTABLE_NEW_BOOKS();
    }

    public List<BookData> getNewBooks(){
        return mDatabaseHelper.getNewBooks();
    }


    public boolean registerToNewBooks(List<BookData> books){
        return mDatabaseHelper.registerToNewBooks(books);
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


}
