package jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;

public class ProgressDialogFragment extends DialogFragment{
    public static final String TAG = ProgressDialogFragment.class.getSimpleName();

    private static final boolean D = true;

    public static final String KEY_REQUEST_CODE     = "ProgressDialogFragment.KEY_REQUEST_CODE";
    public static final String KEY_TITLE            = "ProgressDialogFragment.KEY_TITLE";
    public static final String KEY_MESSAGE          = "ProgressDialogFragment.KEY_MESSAGE";
    public static final String KEY_PROGRESS         = "ProgressDialogFragment.KEY_PROGRESS";
    public static final String KEY_PARAMS           = "ProgressDialogFragment.KEY_PARAMS";

    private TextView mTextView_Title;
    private TextView mTextView_Message;
    private TextView mTextView_Progress;
    private String mTitle;
    private String mMessage;
    private String mProgress;





    public static final String title = "KEY_TITLE";
    public static final String message = "KEY_MESSAGE";
    public static final String progress = "KEY_PROGRESS_VALUE_TEXT";




    public interface OnProgressDialogListener {
        void onProgressDialogCancelled(int requestCode, Bundle params);
    }
    private OnProgressDialogListener mListener;


    @SuppressWarnings("unused")
    public static ProgressDialogFragment newInstance(Bundle bundle){
        ProgressDialogFragment instance = new ProgressDialogFragment();
        instance.setArguments(bundle);
        return instance;
    }


    public static ProgressDialogFragment newInstance(Fragment fragment, Bundle bundle){
        ProgressDialogFragment instance = new ProgressDialogFragment();
        instance.setArguments(bundle);
        int request_code = bundle.getInt(KEY_REQUEST_CODE);
        instance.setTargetFragment(fragment,request_code);
        return instance;
    }




    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(D) Log.d(TAG,"onAttach");
        Fragment targetFragment = this.getTargetFragment();
        try{
            if(targetFragment != null){
                mListener = (OnProgressDialogListener) targetFragment;
            }else{
                Fragment parent = this.getParentFragment();
                if(parent != null){
                    mListener = (OnProgressDialogListener) parent;
                }else {
                    mListener = (OnProgressDialogListener) context;
                }
            }
        } catch (UnsupportedOperationException e){
            throw new UnsupportedOperationException("mListener is not Implementation.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_TITLE, mTitle);
        outState.putString(KEY_MESSAGE, mMessage);
        outState.putString(KEY_PROGRESS, mProgress);
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

        if(savedInstanceState != null){
            mTitle = savedInstanceState.getString(KEY_TITLE);
            mMessage = savedInstanceState.getString(KEY_MESSAGE);
            mProgress = savedInstanceState.getString(KEY_PROGRESS);
        }else{
            Bundle bundle = this.getArguments();
            mTitle = bundle.getString(KEY_TITLE);
            mMessage = bundle.getString(KEY_MESSAGE);
            mProgress = bundle.getString(KEY_PROGRESS);
        }
        setCancelable(false);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.fragment_progress_dialog);
        return builder.create();
    }


    @Override
    public void onStart(){
        super.onStart();
        mTextView_Title = getDialog().findViewById(R.id.fragment_progress_dialog_title);
        mTextView_Message = getDialog().findViewById(R.id.fragment_progress_dialog_message);
        mTextView_Progress = getDialog().findViewById(R.id.fragment_progress_dialog_progress);
        Button mButton_Cancel = getDialog().findViewById(R.id.fragment_progress_dialog_button_cancel);
        mButton_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (getArguments() != null && mListener != null) {
                    mListener.onProgressDialogCancelled(getRequestCode(), getArguments().getBundle(KEY_PARAMS));
                }
            }
        });
        setDialogTitle(mTitle);
        setDialogProgress(mMessage,mProgress);
    }

    public void setDialogTitle(String title){
        if(mTextView_Title == null){
            mTextView_Title = getDialog().findViewById(R.id.fragment_progress_dialog_title);
        }
        if(title != null) {
            mTextView_Title.setText(title);
            mTitle = title;
        }
    }

    public void setDialogProgress(String message, String progress) {
        if (mTextView_Message == null) {
            mTextView_Message = getDialog().findViewById(R.id.fragment_progress_dialog_message);
        }
        if (mTextView_Progress == null) {
            mTextView_Progress = getDialog().findViewById(R.id.fragment_progress_dialog_progress);
        }
        if (message != null) {
            mTextView_Message.setText(message);
            mMessage = message;
        }
        if (progress != null) {
            mTextView_Progress.setText(progress);
            mProgress = progress;
        }
    }


    private int getRequestCode() {
        Bundle bundle = getArguments();
        if(bundle != null) {
            if(bundle.containsKey(KEY_REQUEST_CODE)){
                return bundle.getInt(KEY_REQUEST_CODE);
            }else{
                return getTargetRequestCode();
            }
        }
        return -1;
    }


    public static void showProgressDialog(Fragment fragment, Bundle bundle){
        if (fragment.getActivity() != null && bundle != null) {
            FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
            ProgressDialogFragment dialog = ProgressDialogFragment.newInstance(fragment, bundle);
            dialog.show(manager, TAG);
        }
    }

    @SuppressWarnings("unused")
    public static void dismissProgressDialog(Fragment fragment){
        if(fragment.getActivity() != null){
            FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
            Fragment findFragment = manager.findFragmentByTag(TAG);
            if(findFragment instanceof ProgressDialogFragment){
                ((ProgressDialogFragment) findFragment).dismiss();
            }
        }
    }


}



