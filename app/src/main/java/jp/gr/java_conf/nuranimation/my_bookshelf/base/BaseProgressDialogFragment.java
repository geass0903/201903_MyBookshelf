package jp.gr.java_conf.nuranimation.my_bookshelf.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;

public class BaseProgressDialogFragment extends DialogFragment{
    private static final boolean D = true;
    private static final String TAG = BaseProgressDialogFragment.class.getSimpleName();

    public static final String title = "KEY_TITLE";
    public static final String message = "KEY_MESSAGE";

    private TextView mTextView_Title;
    private TextView mTextView_Progress;
    private String mTitle;
    private String mProgress;


    public static BaseProgressDialogFragment newInstance(Bundle bundle){
        BaseProgressDialogFragment instance = new BaseProgressDialogFragment();
        instance.setArguments(bundle);
        return instance;
    }


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(D) Log.d(TAG,"onAttach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(getActivity() == null){
            throw new IllegalArgumentException("getActivity() == null");
        }
        if(getArguments() == null){
            throw new NullPointerException("getArguments() == null");
        }
        Bundle bundle = this.getArguments();
        setCancelable(false);
        mTitle = bundle.getString(BaseProgressDialogFragment.title);
        mProgress = bundle.getString(BaseProgressDialogFragment.message);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.fragment_progressdialog);
        return builder.create();
    }


    @Override
    public void onStart(){
        super.onStart();
        mTextView_Title = getDialog().findViewById(R.id.fragment_progress_dialog_text_title);
        mTextView_Progress = getDialog().findViewById(R.id.fragment_progress_dialog_text_progress);
        if(!TextUtils.isEmpty(mTitle)) {
            mTextView_Title.setText(mTitle);
        }
        if(!TextUtils.isEmpty(mProgress)) {
            mTextView_Progress.setText(mProgress);
        }
    }

    @SuppressWarnings("unused")
    public void setProgressTitle(String title){
        if(mTextView_Title == null){
            mTextView_Title = getDialog().findViewById(R.id.fragment_progress_dialog_text_title);
        }
        if(!TextUtils.isEmpty(title)) {
            mTextView_Title.setText(title);
        }
    }

    public void setProgressMessage(String message){
        if(mTextView_Progress == null){
            mTextView_Progress = getDialog().findViewById(R.id.fragment_progress_dialog_text_progress);
        }
        if(!TextUtils.isEmpty(message)) {
            mTextView_Progress.setText(message);
        }
    }

}



