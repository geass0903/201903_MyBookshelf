package jp.gr.java_conf.nuranimation.my_bookshelf.model.entity;

public final class SpinnerItem {
    private final String mCode;
    private final String mLabel;

    public SpinnerItem(String code, String label){
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
