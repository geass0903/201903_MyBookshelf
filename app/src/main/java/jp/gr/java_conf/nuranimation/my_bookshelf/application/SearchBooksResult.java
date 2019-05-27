package jp.gr.java_conf.nuranimation.my_bookshelf.application;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SearchBooksResult{
    private boolean isSuccess;
    private int errorStatus;
    private JSONObject jsonObject;

    public SearchBooksResult(){

    }


    public SearchBooksResult(SearchBooksResult result){
        isSuccess = result.isSuccess();
        errorStatus = result.getErrorStatus();
        jsonObject = result.getJSONObject();
    }


    public void setSuccess(boolean flag){
        isSuccess = flag;
    }

    public boolean isSuccess(){
        return isSuccess;
    }

    public void setErrorStatus(int status){
        errorStatus = status;
    }

    public int getErrorStatus(){
        return errorStatus;
    }

    public void setJSONObject(JSONObject object){
        jsonObject = object;
    }

    public JSONObject getJSONObject(){
        return jsonObject;
    }



}
