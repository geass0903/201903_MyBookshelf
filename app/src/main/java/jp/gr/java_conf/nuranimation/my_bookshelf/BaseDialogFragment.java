package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

public final class BaseDialogFragment extends DialogFragment {
    private static final boolean D = true;
    private static final String TAG = BaseDialogFragment.class.getSimpleName();

    public static final String title = "title";
    public static final String message = "message";
    public static final String items = "items";
    public static final String positiveLabel = "positive_label";
    public static final String negativeLabel = "negative_label";
    public static final String cancelable = "cancelable";
    public static final String params = "params";
    public static final String request_code = "request_code";


    public interface OnBaseDialogListener {
        void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params);
        void onBaseDialogCancelled(int requestCode, Bundle params);
    }
    private OnBaseDialogListener mListener;



    public static BaseDialogFragment newInstance(Fragment fragment, Bundle bundle){
        BaseDialogFragment instance = new BaseDialogFragment();
        instance.setArguments(bundle);
        int request_code = bundle.getInt(BaseDialogFragment.request_code);
        instance.setTargetFragment(fragment,request_code);
        return instance;
    }


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(D) Log.d(TAG,"onAttach");
        Fragment targetFragment = this.getTargetFragment();
        try{
            mListener = (OnBaseDialogListener) targetFragment;
        } catch (UnsupportedOperationException e){
            throw new UnsupportedOperationException("mListener is not Implementation.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
                if (getArguments() != null) {
                    mListener.onBaseDialogSucceeded(getRequestCode(), which, getArguments().getBundle(params));
                }
            }
        };

        final String title = bundle.getString(BaseDialogFragment.title);
        final String message = bundle.getString(BaseDialogFragment.message);
        final String[] items = bundle.getStringArray(BaseDialogFragment.items);
        final String positiveLabel = bundle.getString(BaseDialogFragment.positiveLabel);
        final String negativeLabel = bundle.getString(BaseDialogFragment.negativeLabel);
        setCancelable(bundle.getBoolean(cancelable));
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        if (!TextUtils.isEmpty(message)) {
            builder.setMessage(message);
        }
        if (items != null && items.length > 0) {
            builder.setItems(items, listener);
        }
        if (!TextUtils.isEmpty(positiveLabel)) {
            builder.setPositiveButton(positiveLabel, listener);
        }
        if (!TextUtils.isEmpty(negativeLabel)) {
            builder.setNegativeButton(negativeLabel, listener);
        }
        return builder.create();
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        Bundle bundle = getArguments();
        if(bundle != null){
            mListener.onBaseDialogCancelled(getRequestCode(), getArguments().getBundle(params));
        }
    }

    private int getRequestCode() {
        Bundle bundle = getArguments();
        if(bundle != null) {
            if(bundle.containsKey(request_code)){
                return bundle.getInt(request_code);
            }else{
                return getTargetRequestCode();
            }
        }
        return -1;
    }

}
