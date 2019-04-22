package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.util.Pair;

public class Spinner_KeyValuePair extends Pair<Integer,String> {

    public Spinner_KeyValuePair(Integer key, String value) {
        super(key, value);
    }

    Integer getKey(){
        return super.first;
    }

    String getValue(){
        return super.second;
    }
}