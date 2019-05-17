package jp.gr.java_conf.nuranimation.my_bookshelf.base;


@SuppressWarnings("unused")
public class BaseSpinnerItem {
    private String mCode;
    private String mLabel;

    public BaseSpinnerItem(String code, String label){
        mCode = code;
        mLabel = label;
    }

    public void setCode(String code){
        mCode = code;
    }
    public String getCode(){
        return mCode;
    }

    public void setLabel(String label){
        mLabel = label;
    }
    public String getLabel(){
        return mLabel;
    }

}
