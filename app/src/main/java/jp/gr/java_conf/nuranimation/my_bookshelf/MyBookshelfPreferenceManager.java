package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.content.SharedPreferences;

class MyBookshelfPreferenceManager {
    private static final String PreferenceName = "MyBookshelfPreference";

    static final String Key_Access_Token = "Key_Access_Token";
    static final String Key_SortSetting_Bookshelf = "Key_SortSetting_Bookshelf";
    static final String Key_SortSetting_SearchResult = "Key_SortSetting_SearchResult";

    private SharedPreferences mPreference;

    MyBookshelfPreferenceManager(Context context){
        mPreference = context.getSharedPreferences(PreferenceName,Context.MODE_PRIVATE);
    }



    @SuppressWarnings("unused")
    boolean containsKey(String key){
        return mPreference.contains(key);
    }

    @SuppressWarnings("unused")
    void removeKey(String key){
        mPreference.edit().remove(key).apply();
    }

    @SuppressWarnings("unused")
    void putInt(String key, int value){
        mPreference.edit().putInt(key,value).apply();
    }

    @SuppressWarnings("unused")
    void putString(String key, String  value){
        mPreference.edit().putString(key,value).apply();
    }

    @SuppressWarnings("unused")
    int getInt(String key,int defValue){
        return mPreference.getInt(key,defValue);
    }

    @SuppressWarnings("unused")
    String getString(String key,String defValue){
        return mPreference.getString(key,defValue);
    }


}
