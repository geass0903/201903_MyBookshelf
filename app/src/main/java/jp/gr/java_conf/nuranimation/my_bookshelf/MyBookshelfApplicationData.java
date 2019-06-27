package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

import java.io.File;
import java.util.List;


@SuppressWarnings({"unused"})
public class MyBookshelfApplicationData extends Application {
    private static final String TAG = MyBookshelfApplicationData.class.getSimpleName();
    private static final boolean D = true;

    private static final String PREFERENCE_NAME = "MyBookshelfPreference";
    private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
    private static final String KEY_SHELF_BOOKS_ORDER = "KEY_SHELF_BOOKS_ORDER";
    private static final String KEY_SEARCH_BOOKS_ORDER = "KEY_SEARCH_BOOKS_ORDER";

    private MyBookshelfDBOpenHelper mDatabaseHelper;
    private SharedPreferences mPreferences;
    private static final String[] Use_Permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private boolean isCheckedPermissions;





    @Override
    public void onCreate(){
        super.onCreate();
        if(D) Log.d(TAG,"onCreate");

//        File cacheDir = getCacheDir();
        String APPLICATION_DIRECTORY_PATH = "/Android/data/jp.gr.java_conf.nuranimation.my_bookshelf/";
        File dir = Environment.getExternalStorageDirectory();
        File cacheDir = new File(dir.getPath() + APPLICATION_DIRECTORY_PATH);

        DiskCacheConfig largeImageCacheConfig = DiskCacheConfig.newBuilder(this)
                .setBaseDirectoryName("largeImageCache")
                .setBaseDirectoryPath(cacheDir)
                .build();
        DiskCacheConfig smallImageCacheConfig = DiskCacheConfig.newBuilder(this)
                .setBaseDirectoryName("smallImageCache")
                .setBaseDirectoryPath(cacheDir)
                .build();
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setMainDiskCacheConfig(largeImageCacheConfig)
                .setSmallImageDiskCacheConfig(smallImageCacheConfig)
                .build();

        Fresco.initialize(this, config);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannelManager.create(getApplicationContext(),getString(R.string.notification_channel_id), R.string.notification_channel_title, R.string.notification_channel_description);
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


    public List<BookData> loadDatabaseBooks(){
        return mDatabaseHelper.loadShelfBooks(null, getString(R.string.code_shelf_books_order_registered_ascending));
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

    public List<BookData> loadShelfBooks(String keyword, String order){
        return mDatabaseHelper.loadShelfBooks(keyword, order);
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


    public String getShelfBooksOrder(){
        String order = mPreferences.getString(KEY_SHELF_BOOKS_ORDER,null);
        if(TextUtils.isEmpty(order)){
            order = getString(R.string.code_shelf_books_order_registered_ascending);
            mPreferences.edit().putString(KEY_SHELF_BOOKS_ORDER,getString(R.string.code_shelf_books_order_registered_ascending)).apply();
        }
        return order;
    }

    public void setShelfBooksOrder(String order){
        mPreferences.edit().putString(KEY_SHELF_BOOKS_ORDER, order).apply();
    }

    public String getSearchBooksOrder(){
        String order = mPreferences.getString(KEY_SEARCH_BOOKS_ORDER,null);
        if(TextUtils.isEmpty(order)){
            order = getString(R.string.code_search_books_order_sales_date_descending);
            mPreferences.edit().putString(KEY_SEARCH_BOOKS_ORDER,getString(R.string.code_search_books_order_sales_date_descending)).apply();
        }
        return order;
    }

    public void setSearchBooksOrder(String order){
        mPreferences.edit().putString(KEY_SEARCH_BOOKS_ORDER, order).apply();
    }

    public boolean containsKeyAccessToken(){
        return mPreferences.contains(KEY_ACCESS_TOKEN);
    }

    public String getAccessToken(){
        return mPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public void setAccessToken(String token){
        mPreferences.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public void deleteAccessToken(){
        mPreferences.edit().remove(KEY_ACCESS_TOKEN).apply();
    }

}
