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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;

public class RegisterAuthorDialogFragment extends DialogFragment {
    private static final String TAG = RegisterAuthorDialogFragment.class.getSimpleName();
    private static final boolean D = true;

    public static final String KEY_REQUEST_CODE     = "RegisterAuthorDialogFragment.KEY_REQUEST_CODE";
    public static final String KEY_TITLE            = "RegisterAuthorDialogFragment.KEY_TITLE";
    public static final String KEY_POSITIVE_LABEL   = "RegisterAuthorDialogFragment.KEY_POSITIVE_LABEL";
    public static final String KEY_NEGATIVE_LABEL   = "RegisterAuthorDialogFragment.KEY_NEGATIVE_LABEL";
    public static final String KEY_AUTHOR           = "RegisterAuthorDialogFragment.KEY_AUTHOR";
    public static final String KEY_AUTHOR_LIST      = "RegisterAuthorDialogFragment.KEY_AUTHOR_LIST";
    public static final String KEY_PARAMS           = "RegisterAuthorDialogFragment.KEY_PARAMS";

    private static final String KEY_TEMP_AUTHOR = "EditTextDialogFragment.KEY_TEMP_AUTHOR";
    private static final String KEY_ERROR_MESSAGE = "EditTextDialogFragment.KEY_ERROR_MESSAGE";

    private TextView tv_ErrorMessage;
    private Button bt_Positive;

    private List<String> authorsList;
    private String editAuthor;
    private String errorMessage;



    public interface OnRegisterAuthorDialogListener {
        void onRegister(int requestCode, int resultCode, String author, Bundle params);
        void onCancelled(int requestCode, Bundle params);
    }
    private OnRegisterAuthorDialogListener mListener;


    public static RegisterAuthorDialogFragment newInstance(Fragment fragment, Bundle bundle){
        RegisterAuthorDialogFragment instance = new RegisterAuthorDialogFragment();
        instance.setArguments(bundle);
        int request_code = bundle.getInt(KEY_REQUEST_CODE);
        instance.setTargetFragment(fragment,request_code);
        return instance;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment targetFragment = this.getTargetFragment();
        if (targetFragment instanceof OnRegisterAuthorDialogListener) {
            mListener = (OnRegisterAuthorDialogListener) targetFragment;
        } else {
            Fragment parentFragment = this.getParentFragment();
            if (parentFragment instanceof OnRegisterAuthorDialogListener) {
                mListener = (OnRegisterAuthorDialogListener) parentFragment;
            } else {
                if (context instanceof OnRegisterAuthorDialogListener) {
                    mListener = (OnRegisterAuthorDialogListener) context;
                }
            }
        }
        if (mListener == null) {
            throw new UnsupportedOperationException("Listener is not Implementation.");
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
        if(D) Log.d(TAG, "onSaveInstanceState");
        outState.putString(KEY_TEMP_AUTHOR, editAuthor);
        outState.putString(KEY_ERROR_MESSAGE, errorMessage);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(D) Log.d(TAG, "onCreateDialog");
        if (getActivity() == null) {
            throw new IllegalArgumentException("getActivity() == null");
        }
        if (getArguments() == null) {
            throw new NullPointerException("getArguments() == null");
        }

        Bundle bundle = this.getArguments();
        if (savedInstanceState != null) {
            if(D) Log.d(TAG, "savedInstanceState != null");
            editAuthor = savedInstanceState.getString(KEY_TEMP_AUTHOR);
            errorMessage = savedInstanceState.getString(KEY_ERROR_MESSAGE);
        }else{
            editAuthor = bundle.getString(KEY_AUTHOR);
            errorMessage = null;
        }

        authorsList = bundle.getStringArrayList(KEY_AUTHOR_LIST);

        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if( mListener != null && getArguments() != null) {
                    mListener.onRegister(getRequestCode(), which, editAuthor, getArguments().getBundle(KEY_PARAMS));
                }
            }
        };
        final String title = bundle.getString(KEY_TITLE);
        final String positiveLabel = bundle.getString(KEY_POSITIVE_LABEL);
        final String negativeLabel = bundle.getString(KEY_NEGATIVE_LABEL);
        setCancelable(true);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        if (!TextUtils.isEmpty(positiveLabel)) {
            builder.setPositiveButton(positiveLabel, listener);
        }
        if (!TextUtils.isEmpty(negativeLabel)) {
            builder.setNegativeButton(negativeLabel, listener);
        }
        builder.setView(R.layout.fragment_edit_author_dialog);

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Bundle bundle = getArguments();
        if(bundle != null && mListener != null){
            mListener.onCancelled(getRequestCode(), getArguments().getBundle(KEY_PARAMS));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() instanceof AlertDialog) {
            AlertDialog dialog = (AlertDialog) getDialog();
            bt_Positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            EditText et_Author = dialog.findViewById(R.id.fragment_edit_author_edit_text);
            if (et_Author != null) {
                et_Author.addTextChangedListener(new GenericTextWatcher(et_Author));
                if(editAuthor != null) {
                    et_Author.setText(editAuthor);
                }
            }
            tv_ErrorMessage = dialog.findViewById(R.id.fragment_edit_author_error_message);
            if (errorMessage != null && tv_ErrorMessage != null) {
                tv_ErrorMessage.setText(errorMessage);
            }

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


    public static void showRegisterAuthorDialog(Fragment fragment, Bundle bundle, String tag) {
        if (D) Log.d(TAG, "showRegisterAuthorDialog TAG: " + tag);
        if (fragment != null && fragment.getActivity() != null && bundle != null) {
            FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
            RegisterAuthorDialogFragment dialog = RegisterAuthorDialogFragment.newInstance(fragment, bundle);
            dialog.show(manager, tag);
        }
    }


    private class GenericTextWatcher implements TextWatcher {
        private View view;
        int currentLength = 0;

        GenericTextWatcher(View view){
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if(D) Log.d(TAG, "beforeTextChanged");
            editAuthor = s.toString();
            checkAuthor(s.toString());
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            currentLength = s.toString().length();
            if(D) Log.d(TAG, "onTextChanged");
            editAuthor = s.toString();
            checkAuthor(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().length() < currentLength) {
                return;
            }
            boolean unfixed = false;
            Object[] spanned = s.getSpans(0, s.length(), Object.class);
            if (spanned != null) {
                for (Object obj : spanned) {
                    if (obj instanceof android.text.style.UnderlineSpan) {
                        unfixed = true;
                    }
                }
            }
            if (!unfixed) {
                confirmString(view, s.toString());
            }

        }

        private void confirmString(View view, String author){
            if (view.getId() == R.id.fragment_edit_author_edit_text) {
                editAuthor = author;
                checkAuthor(editAuthor);
            }
        }
    }


    private void checkAuthor(String author){
        if(bt_Positive != null && tv_ErrorMessage != null){
            if(TextUtils.isEmpty(author)){
                tv_ErrorMessage.setText(R.string.dialog_message_error_empty);
                bt_Positive.setEnabled(false);
                return;
            }

            //            if(mDBOpenHelper.containsAuthor(author)){
            if(authorsList != null && authorsList.contains(author)){

                if(D) Log.d(TAG, "already exists : " + author);

                if(getArguments() != null){
                    String edit = getArguments().getString(KEY_AUTHOR);
                    if(edit != null && edit.equals(author)){
                        tv_ErrorMessage.setText("");
                        bt_Positive.setEnabled(true);
                        return;
                    }
                }
                tv_ErrorMessage.setText(R.string.dialog_message_error_exists);
                bt_Positive.setEnabled(false);

            }else{
                tv_ErrorMessage.setText("");
                bt_Positive.setEnabled(true);
            }
        }
    }



}
