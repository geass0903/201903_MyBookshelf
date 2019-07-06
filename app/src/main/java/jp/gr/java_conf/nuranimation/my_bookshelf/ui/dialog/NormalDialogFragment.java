package jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

public class NormalDialogFragment extends DialogFragment {
    private static final String TAG = NormalDialogFragment.class.getSimpleName();
    public static final String TEMP_TAG = NormalDialogFragment.class.getSimpleName();
    private static final boolean D = true;

    public static final String KEY_REQUEST_CODE     = "NormalDialogFragment.KEY_REQUEST_CODE";
    public static final String KEY_TITLE            = "NormalDialogFragment.KEY_TITLE";
    public static final String KEY_MESSAGE          = "NormalDialogFragment.KEY_MESSAGE";
    public static final String KEY_ITEMS            = "NormalDialogFragment.KEY_ITEMS";
    public static final String KEY_POSITIVE_LABEL   = "NormalDialogFragment.KEY_POSITIVE_LABEL";
    public static final String KEY_NEGATIVE_LABEL   = "NormalDialogFragment.KEY_NEGATIVE_LABEL";
    public static final String KEY_CANCELABLE       = "NormalDialogFragment.KEY_CANCELABLE";
    public static final String KEY_PARAMS           = "NormalDialogFragment.KEY_PARAMS";

    public interface OnNormalDialogListener {
        void onNormalDialogSucceeded(int requestCode, int resultCode, Bundle params);
        void onNormalDialogCancelled(int requestCode, Bundle params);
    }
    private OnNormalDialogListener mListener;


    @SuppressWarnings("unused")
    public static NormalDialogFragment newInstance(Bundle bundle){
        NormalDialogFragment instance = new NormalDialogFragment();
        instance.setArguments(bundle);
        return instance;
    }

    public static NormalDialogFragment newInstance(Fragment fragment, Bundle bundle){
        NormalDialogFragment instance = new NormalDialogFragment();
        instance.setArguments(bundle);
        int request_code = bundle.getInt(KEY_REQUEST_CODE);
        instance.setTargetFragment(fragment,request_code);
        return instance;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Fragment targetFragment = this.getTargetFragment();
        try{
            if(targetFragment != null){
                mListener = (OnNormalDialogListener) targetFragment;
            }else{
                Fragment parent = this.getParentFragment();
                if(parent != null){
                    mListener = (OnNormalDialogListener) parent;
                }else {
                    mListener = (OnNormalDialogListener) context;
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
                if (getArguments() != null && mListener != null) {
                    mListener.onNormalDialogSucceeded(getRequestCode(), which, getArguments().getBundle(KEY_PARAMS));
                }
            }
        };

        final String title = bundle.getString(KEY_TITLE);
        final String message = bundle.getString(KEY_MESSAGE);
        final String[] items = bundle.getStringArray(KEY_ITEMS);
        final String positiveLabel = bundle.getString(KEY_POSITIVE_LABEL);
        final String negativeLabel = bundle.getString(KEY_NEGATIVE_LABEL);
        setCancelable(bundle.getBoolean(KEY_CANCELABLE,true));
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
        if(bundle != null && mListener != null){
            mListener.onNormalDialogCancelled(getRequestCode(), getArguments().getBundle(KEY_PARAMS));
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




    public static void showNormalDialog(Fragment fragment, Bundle bundle, String tag){
        if(D) Log.d(TAG, "showNormalDialog TAG: " + tag);
        if (fragment.getActivity() != null && bundle != null) {
            FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
            NormalDialogFragment dialog = NormalDialogFragment.newInstance(fragment, bundle);
            dialog.show(manager, tag);
        }
    }

    @SuppressWarnings("unused")
    public static void dismissNormalDialog(Fragment fragment, String tag){
        if(D) Log.d(TAG, "dismissNormalDialog TAG: " + tag);
        if(fragment.getActivity() != null){
            FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
            Fragment findFragment = manager.findFragmentByTag(tag);
            if(findFragment instanceof NormalDialogFragment){
                ((NormalDialogFragment) findFragment).dismiss();
            }
        }
    }

    public static boolean isShowingNormalDialog(Fragment fragment, String tag) {
        if (fragment.getActivity() != null) {
            FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
            Fragment findFragment = manager.findFragmentByTag(tag);
            return findFragment instanceof NormalDialogFragment;
        }
        return false;
    }

}
