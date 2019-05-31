package jp.gr.java_conf.nuranimation.my_bookshelf.base;

public final class BaseSpinnerItem {
    private final String mCode;
    private final String mLabel;

    public BaseSpinnerItem(String code, String label){
        mCode = code;
        mLabel = label;
    }

    public String getCode(){
        return mCode;
    }

    public String getLabel(){
        return mLabel;
    }

}
