package jp.gr.java_conf.nuranimation.my_bookshelf.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.book.BookData;


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



    public boolean deleteTABLE_AUTHORS(){
        return mDatabaseHelper.deleteTABLE_AUTHORS();
    }

    public List<String> getAuthors(){
        return mDatabaseHelper.getAuthors();
    }

    public boolean registerToAuthors(String author){
        return mDatabaseHelper.registerToAuthors(author);
    }

    public boolean registerToAuthors(List<String> authors){
        return mDatabaseHelper.registerToAuthors(authors);
    }



    public boolean deleteTABLE_SHELF_BOOKS(){
        return mDatabaseHelper.deleteTABLE_SHELF_BOOKS();
    }

    public List<BookData> getShelfBooks(String word){
        return mDatabaseHelper.getShelfBooks(word);
    }

    public BookData searchInShelfBooks(String ISBN){
        return mDatabaseHelper.searchInShelfBooks(ISBN);
    }

    public boolean registerToShelfBooks(BookData book){
        return mDatabaseHelper.registerToShelfBooks(book);
    }

    public boolean registerToShelfBooks(List<BookData> books){
        return mDatabaseHelper.registerToShelfBooks(books);
    }

    public boolean deleteFromShelfBooks(String ISBN){
        return mDatabaseHelper.deleteFromShelfBooks(ISBN);
    }



    public boolean deleteTABLE_SEARCH_BOOKS(){
        return mDatabaseHelper.deleteTABLE_SEARCH_BOOKS();
    }

    public List<BookData> getSearchBooks(){
        return mDatabaseHelper.getSearchBooks();
    }

    public boolean registerToSearchBooks(BookData book){
        return mDatabaseHelper.registerToSearchBooks(book);
    }



    public boolean deleteTABLE_NEW_BOOKS(){
        return mDatabaseHelper.deleteTABLE_NEW_BOOKS();
    }

    public List<BookData> getNewBooks(){
        return mDatabaseHelper.getNewBooks();
    }

    public boolean registerToNewBooks(BookData book){
        return mDatabaseHelper.registerToNewBooks(book);
    }



}
