package jp.gr.java_conf.nuranimation.my_bookshelf.base;

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
    public static final String TAG = BaseDialogFragment.class.getSimpleName();

    public static final String KEY_TITLE = "KEY_TITLE";
    public static final String KEY_MESSAGE = "KEY_MESSAGE";
    public static final String KEY_ITEMS = "KEY_ITEMS";
    public static final String KEY_POSITIVE_LABEL = "KEY_POSITIVE_LABEL";
    public static final String KEY_NEGATIVE_LABEL = "KEY_NEGATIVE_LABEL";
    public static final String KEY_CANCELABLE = "KEY_CANCELABLE";
    public static final String KEY_PARAMS = "KEY_PARAMS";
    public static final String KEY_REQUEST_CODE = "KEY_REQUEST_CODE";


    public interface OnBaseDialogListener {
        void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params);
        void onBaseDialogCancelled(int requestCode, Bundle params);
    }
    private OnBaseDialogListener mListener;


    @SuppressWarnings("unused")
    public static BaseDialogFragment newInstance(Bundle bundle){
        BaseDialogFragment instance = new BaseDialogFragment();
        instance.setArguments(bundle);
        return instance;
    }


    public static BaseDialogFragment newInstance(Fragment fragment, Bundle bundle){
        BaseDialogFragment instance = new BaseDialogFragment();
        instance.setArguments(bundle);
        int request_code = bundle.getInt(BaseDialogFragment.KEY_REQUEST_CODE);
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
                mListener = (OnBaseDialogListener) targetFragment;
            }else{
                mListener = (OnBaseDialogListener) context;
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
                    mListener.onBaseDialogSucceeded(getRequestCode(), which, getArguments().getBundle(KEY_PARAMS));
                }
            }
        };

        final String title = bundle.getString(BaseDialogFragment.KEY_TITLE);
        final String message = bundle.getString(BaseDialogFragment.KEY_MESSAGE);
        final String[] items = bundle.getStringArray(BaseDialogFragment.KEY_ITEMS);
        final String positiveLabel = bundle.getString(BaseDialogFragment.KEY_POSITIVE_LABEL);
        final String negativeLabel = bundle.getString(BaseDialogFragment.KEY_NEGATIVE_LABEL);
        setCancelable(bundle.getBoolean(KEY_CANCELABLE));
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
            mListener.onBaseDialogCancelled(getRequestCode(), getArguments().getBundle(KEY_PARAMS));
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

}
