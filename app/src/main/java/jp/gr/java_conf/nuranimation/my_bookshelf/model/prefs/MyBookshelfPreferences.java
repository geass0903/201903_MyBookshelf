package jp.gr.java_conf.nuranimation.my_bookshelf.model.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MyBookshelfPreferences {

    private static final String KEY_ACCESS_TOKEN = "MyBookshelfPreferences.KEY_ACCESS_TOKEN";
    private static final String KEY_SHELF_BOOKS_ORDER_CODE = "MyBookshelfPreferences.KEY_SHELF_BOOKS_ORDER_CODE";
    private static final String KEY_SEARCH_BOOKS_ORDER_CODE = "MyBookshelfPreferences.KEY_SEARCH_BOOKS_ORDER_CODE";
    private static final String KEY_IS_REQUEST_PERMISSION = "MyBookshelfPreferences.KEY_IS_REQUEST_PERMISSION";

    private static SharedPreferences mPreferences;

    public MyBookshelfPreferences(Context context) {
        if(mPreferences == null) {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        }
    }

    public String getShelfBooksOrderCode(){
        return mPreferences.getString(KEY_SHELF_BOOKS_ORDER_CODE,null);
    }

    public void setShelfBooksOrderCode(String code){
        mPreferences.edit().putString(KEY_SHELF_BOOKS_ORDER_CODE, code).apply();
    }

    public String getSearchBooksOrderCode(){
        return mPreferences.getString(KEY_SEARCH_BOOKS_ORDER_CODE,null);
    }

    public void setSearchBooksOrderCode(String code){
        mPreferences.edit().putString(KEY_SEARCH_BOOKS_ORDER_CODE, code).apply();
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

    public boolean isCheckedPermissions(){
        return mPreferences.getBoolean(KEY_IS_REQUEST_PERMISSION, false);
    }

    public void setCheckedPermissions(boolean flag){
        mPreferences.edit().putBoolean(KEY_IS_REQUEST_PERMISSION, flag).apply();
    }

}
