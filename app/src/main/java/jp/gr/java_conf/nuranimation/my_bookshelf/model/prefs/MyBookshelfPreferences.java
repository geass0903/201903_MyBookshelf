package jp.gr.java_conf.nuranimation.my_bookshelf.model.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BooksOrder;

public class MyBookshelfPreferences {

    private static final String PREFERENCE_NAME = "MyBookshelfPreference";
    private static final String KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN";
    private static final String KEY_SHELF_BOOKS_ORDER_CODE = "KEY_SHELF_BOOKS_ORDER_CODE";
    private static final String KEY_SEARCH_BOOKS_ORDER_CODE = "KEY_SEARCH_BOOKS_ORDER_CODE";
    private static final String KEY_IS_REQUEST_PERMISSION = "KEY_IS_REQUEST_PERMISSION";

    private SharedPreferences mPreferences;

    public MyBookshelfPreferences(Context context){
        mPreferences = context.getSharedPreferences(PREFERENCE_NAME,Context.MODE_PRIVATE);
    }

    public String getShelfBooksOrderCode(){
        String code = mPreferences.getString(KEY_SHELF_BOOKS_ORDER_CODE,null);
        if(TextUtils.isEmpty(code)){
            code = BooksOrder.SHELF_BOOKS_ORDER_CODE_REGISTERED_ASC;
            setShelfBooksOrderCode(code);
        }
        return code;
    }

    public void setShelfBooksOrderCode(String code){
        mPreferences.edit().putString(KEY_SHELF_BOOKS_ORDER_CODE, code).apply();
    }

    public String getSearchBooksOrderCode(){
        String code = mPreferences.getString(KEY_SEARCH_BOOKS_ORDER_CODE,null);
        if(TextUtils.isEmpty(code)){
            code = BooksOrder.SEARCH_BOOKS_ORDER_CODE_SALES_DATE_DESC;
            setSearchBooksOrderCode(code);
        }
        return code;
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
