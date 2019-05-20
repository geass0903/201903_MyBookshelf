package jp.gr.java_conf.nuranimation.my_bookshelf.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.utils.DropboxManager;
import jp.gr.java_conf.nuranimation.my_bookshelf.utils.FileManager;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.adapter.SettingsSpinnerArrayAdapter;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseProgressDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseSpinnerItem;
import jp.gr.java_conf.nuranimation.my_bookshelf.utils.ErrorStatus;
import jp.gr.java_conf.nuranimation.my_bookshelf.utils.MyBookshelfApplicationData;


public class SettingsFragment extends BaseFragment {
    public static final String TAG = SettingsFragment.class.getSimpleName();
    private static final boolean D = true;
    private static final String KEY_CURRENT_ACTION = "KEY_CURRENT_ACTION";
    private static final String KEY_IS_ALLOWED_PERMISSIONS = "KEY_IS_ALLOWED_PERMISSIONS";
    private static final String KEY_IS_LOGGED_IN = "KEY_IS_LOGGED_IN";

    private enum ButtonAction {
        None,
        Log_in,
        Log_out,
        Export_CSV,
        Import_CSV,
        Backup_Dropbox,
        Restore_Dropbox,
    }
    private ButtonAction mCurrentAction;

    private MyBookshelfApplicationData mData;
    private DropboxManager mDropboxManager;
    private FileManager mFileManager;

    private Button mButton_Export;
    private Button mButton_Import;
    private Button mButton_Login;
    private Button mButton_Logout;
    private Button mButton_Backup;
    private Button mButton_Restore;

    private boolean isAllowedPermissions;
    private boolean isLogged_in;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mData = (MyBookshelfApplicationData) context.getApplicationContext();
        mDropboxManager = new DropboxManager(context);
        mFileManager = new FileManager(context);
        mFileManager.setHandler(getPausedHandler());
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState != null){
            isAllowedPermissions = savedInstanceState.getBoolean(KEY_IS_ALLOWED_PERMISSIONS);
            isLogged_in = savedInstanceState.getBoolean(KEY_IS_LOGGED_IN);
            mCurrentAction = (ButtonAction)savedInstanceState.getSerializable(KEY_CURRENT_ACTION);
            if(mCurrentAction == null) {
                mCurrentAction = ButtonAction.None;
            }
        }else{
            mCurrentAction = ButtonAction.None;
        }
        if (D) Log.d(TAG, "mCurrentAction: " + mCurrentAction);
        mButton_Export = view.findViewById(R.id.settings_button_export);
        mButton_Import = view.findViewById(R.id.settings_button_import);
        mButton_Login = view.findViewById(R.id.settings_button_log_in_dropbox);
        mButton_Logout = view.findViewById(R.id.settings_button_log_out_dropbox);
        mButton_Backup = view.findViewById(R.id.settings_button_backup);
        mButton_Restore = view.findViewById(R.id.settings_button_restore);
        mButton_Export.setOnClickListener(button_exportListener);
        mButton_Import.setOnClickListener(button_importListener);
        mButton_Login.setOnClickListener(button_loginListener);
        mButton_Logout.setOnClickListener(button_logoutListener);
        mButton_Backup.setOnClickListener(button_backupListener);
        mButton_Restore.setOnClickListener(button_restoreListener);
        enableButton(isAllowedPermissions);
        enableDropboxFunction(isLogged_in);

        initSpinner_SortSetting_Bookshelf(view);
        initSpinner_SortSetting_SearchResult(view);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (D) Log.i(TAG, "onResume");
        checkAction();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (D) Log.i(TAG, "onPause");
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_CURRENT_ACTION, mCurrentAction);
        outState.putBoolean(KEY_IS_ALLOWED_PERMISSIONS, isAllowedPermissions);
        outState.putBoolean(KEY_IS_LOGGED_IN, isLogged_in);
    }


    private void checkPermission() {
        if (isAllowedAllPermissions(mData.getUse_Permissions())) {
            if (D) Log.i(TAG, "isAllowedAllPermissions");
        } else {
            if (D) Log.i(TAG, "requestPermissions");
            requestPermissions(mData.getUse_Permissions());
        }
    }


    @Override
    protected void onAllowPermission(String permission) {
        super.onAllowPermission(permission);
        isAllowedPermissions = true;
        enableButton(true);
        mCurrentAction = ButtonAction.None;
    }

    @Override
    protected void onDenyPermission(String permission) {
        super.onDenyPermission(permission);
        isAllowedPermissions = false;
        enableButton(false);
    }


    private void checkAction() {
        switch (mCurrentAction) {
            case None:
                if(D) Log.d(TAG,"checkAction None");
                if (mData.getSharedPreferences().contains(MyBookshelfApplicationData.KEY_ACCESS_TOKEN)) {
                    isLogged_in = true;
                    enableDropboxFunction(true);
                } else {
                    isLogged_in = false;
                    enableDropboxFunction(false);
                }
                if (isAllowedAllPermissions(mData.getUse_Permissions())) {
                    isAllowedPermissions = true;
                    enableButton(true);
                } else {
                    isAllowedPermissions = false;
                    enableButton(false);
                }
                break;
            case Log_in:
                if(D) Log.d(TAG,"checkAction Log in");
                mCurrentAction = ButtonAction.None;
                try {
                    String token = mDropboxManager.getAccessToken();
                    if (token != null) {
                        // Log-in Success
                        mData.getSharedPreferences().edit().putString(MyBookshelfApplicationData.KEY_ACCESS_TOKEN, token).apply();
                        isLogged_in = true;
                        enableDropboxFunction(true);
                    }
                } catch (IllegalStateException e) {
                    // IllegalStateException
                    if(D) Log.d(TAG,"IllegalStateException");
                }
                break;
            case Log_out:
                if(D) Log.d(TAG,"checkAction Log out");
                mCurrentAction = ButtonAction.None;
                mData.getSharedPreferences().edit().remove(MyBookshelfApplicationData.KEY_ACCESS_TOKEN).apply();
                isLogged_in = false;
                enableDropboxFunction(false);
                break;
        }
    }


    public void callback(ButtonAction action, boolean result) {
        mCurrentAction = ButtonAction.None;
        getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DISMISS).sendToTarget();
        if (result) {
            switch (action) {
                case Export_CSV:
                    Toast.makeText(getContext(), R.string.Toast_Success_Export, Toast.LENGTH_SHORT).show();
                    break;
                case Import_CSV:
                    Toast.makeText(getContext(), R.string.Toast_Success_Import, Toast.LENGTH_SHORT).show();
                    break;
                case Backup_Dropbox:
                    Toast.makeText(getContext(), R.string.Toast_Success_Backup, Toast.LENGTH_SHORT).show();
                    break;
                case Restore_Dropbox:
                    Toast.makeText(getContext(), R.string.Toast_Success_Restore, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        } else {
            Toast.makeText(getContext(), R.string.Toast_Failed, Toast.LENGTH_SHORT).show();
        }

    }

    private void enableDropboxFunction(boolean enable){
        if(enable){
            mButton_Login.setVisibility(View.GONE);
            mButton_Logout.setVisibility(View.VISIBLE);
            mButton_Backup.setVisibility(View.VISIBLE);
            mButton_Restore.setVisibility(View.VISIBLE);
        }else{
            mButton_Login.setVisibility(View.VISIBLE);
            mButton_Logout.setVisibility(View.GONE);
            mButton_Backup.setVisibility(View.GONE);
            mButton_Restore.setVisibility(View.GONE);
        }
    }

    private void enableButton(boolean enable){
        if(enable){
            mButton_Export.setBackgroundResource(R.drawable.selector_button);
            mButton_Import.setBackgroundResource(R.drawable.selector_button);
            mButton_Backup.setBackgroundResource(R.drawable.selector_button);
            mButton_Restore.setBackgroundResource(R.drawable.selector_button);
        }else{
            mButton_Export.setBackgroundResource(R.drawable.selector_button_disable);
            mButton_Import.setBackgroundResource(R.drawable.selector_button_disable);
            mButton_Backup.setBackgroundResource(R.drawable.selector_button_disable);
            mButton_Restore.setBackgroundResource(R.drawable.selector_button_disable);
        }
    }


    public void AsyncTaskCSV(ButtonAction action) {
        new AsyncCSV(this, action).execute();
    }


    View.OnClickListener button_exportListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isAllowedAllPermissions(mData.getUse_Permissions())) {
                AsyncTaskCSV(ButtonAction.Export_CSV);
            } else {
                mCurrentAction = ButtonAction.Export_CSV;
                checkPermission();
            }
        }
    };

    View.OnClickListener button_importListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isAllowedAllPermissions(mData.getUse_Permissions())) {
                AsyncTaskCSV(ButtonAction.Import_CSV);
            } else {
                mCurrentAction = ButtonAction.Import_CSV;
                checkPermission();
            }

        }
    };

    View.OnClickListener button_backupListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isAllowedAllPermissions(mData.getUse_Permissions())) {
                if (mData.getSharedPreferences().contains(MyBookshelfApplicationData.KEY_ACCESS_TOKEN)) {
                    String token = mData.getSharedPreferences().getString(MyBookshelfApplicationData.KEY_ACCESS_TOKEN, null);
                    mDropboxManager.setToken(token);
                    AsyncTaskCSV(ButtonAction.Backup_Dropbox);
                } else {
                    callback(ButtonAction.Backup_Dropbox, false);
                }
            } else {
                mCurrentAction = ButtonAction.Backup_Dropbox;
                checkPermission();
            }


        }
    };

    View.OnClickListener button_restoreListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (isAllowedAllPermissions(mData.getUse_Permissions())) {
                if (mData.getSharedPreferences().contains(MyBookshelfApplicationData.KEY_ACCESS_TOKEN)) {
                    String token = mData.getSharedPreferences().getString(MyBookshelfApplicationData.KEY_ACCESS_TOKEN, null);
                    mDropboxManager.setToken(token);
                    AsyncTaskCSV(ButtonAction.Restore_Dropbox);
                } else {
                    callback(ButtonAction.Restore_Dropbox, false);
                }
            } else {
                mCurrentAction = ButtonAction.Restore_Dropbox;
                checkPermission();
            }
        }
    };

    View.OnClickListener button_loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCurrentAction = ButtonAction.Log_in;
            mDropboxManager.startAuthenticate();
        }
    };

    View.OnClickListener button_logoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showLogoutDialog();
        }
    };

    private void showLogoutDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(BaseDialogFragment.KEY_TITLE, getString(R.string.Dialog_Logout_Title));
        bundle.putString(BaseDialogFragment.KEY_MESSAGE, getString(R.string.Dialog_Logout_Message));
        bundle.putString(BaseDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.Dialog_Button_Positive));
        bundle.putString(BaseDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.Dialog_Button_Negative));
        bundle.putInt(BaseDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_LOGOUT);
        if (getActivity() != null) {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            BaseDialogFragment dialog = BaseDialogFragment.newInstance(this, bundle);
            dialog.show(manager, SettingsFragment.TAG);
        }
    }

    @Override
    public void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        super.onBaseDialogSucceeded(requestCode, resultCode, params);
        switch (requestCode) {
            case REQUEST_CODE_LOGOUT:
                switch (resultCode) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (D) Log.d(TAG, "Log out button pressed");
                        mCurrentAction = ButtonAction.Log_out;
                        checkAction();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        if (D) Log.d(TAG, "Log out button cancel");
                        break;
                }
                break;
            case REQUEST_CODE_ASK_FOR_PERMISSIONS:
                switch (resultCode) {
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        if (D) Log.d(TAG, "Permission cancel");
                        mCurrentAction = ButtonAction.None;
                        break;
                }
                break;

        }


    }

    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {
        super.onBaseDialogCancelled(requestCode, params);
        if (requestCode == REQUEST_CODE_ASK_FOR_PERMISSIONS) {
            if (D) Log.d(TAG, "Permission cancel");
            mCurrentAction = ButtonAction.None;
        }
    }


    private static class AsyncCSV extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<SettingsFragment> mFragmentReference;
        private ButtonAction action;
        private int error;

        private AsyncCSV(SettingsFragment fragment, ButtonAction buttonAction) {
            this.mFragmentReference = new WeakReference<>(fragment);
            this.action = buttonAction;
            this.error = ErrorStatus.No_Error;
        }

        @Override
        protected void onPreExecute() {
            SettingsFragment fragment = mFragmentReference.get();

            Bundle bundle = new Bundle();

            switch (action) {
                case Export_CSV:
                    bundle.putString(BaseProgressDialogFragment.title, fragment.getString(R.string.Progress_Export));
                    break;
                case Import_CSV:
                    bundle.putString(BaseProgressDialogFragment.title, fragment.getString(R.string.Progress_Import));
                    break;
                case Backup_Dropbox:
                    bundle.putString(BaseProgressDialogFragment.title, fragment.getString(R.string.Progress_Backup));
                    break;
                case Restore_Dropbox:
                    bundle.putString(BaseProgressDialogFragment.title, fragment.getString(R.string.Progress_Restore));
                    break;
            }

            bundle.putString(BaseProgressDialogFragment.message, "");
/*
            if (fragment.getActivity() != null) {
                FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
                fragment.mProgressFragment = BaseProgressDialogFragment.newInstance(bundle);
                fragment.mProgressFragment.show(manager, SettingsFragment.TAG);
            }
*/


            PausedHandler mHandler = fragment.getPausedHandler();
            Message msg = mHandler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_SHOW);
            msg.setData(bundle);
            mHandler.sendMessage(msg);

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            SettingsFragment fragment = mFragmentReference.get();

            switch (action) {
                case Export_CSV:
                    error = fragment.mFileManager.export_csv();
                    break;
                case Import_CSV:
                    error = fragment.mFileManager.import_csv();
                    break;
                case Backup_Dropbox:
                    error = fragment.mFileManager.export_csv();
                    if (error == ErrorStatus.No_Error) {
                        error = fragment.mDropboxManager.backup();
                    }
                    break;
                case Restore_Dropbox:
                    error = fragment.mDropboxManager.restore();
                    if (error == ErrorStatus.No_Error) {
                        error = fragment.mFileManager.import_csv();
                    }
                    break;
                default:
                    break;
            }
            return error == ErrorStatus.No_Error;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mFragmentReference.get().callback(action, result);
        }
    }


    private void initSpinner_SortSetting_Bookshelf(View view) {
        Spinner spinner_SortSetting_Bookshelf = view.findViewById(R.id.SettingsFragment_Spinner_SortSetting_Bookshelf);
        SettingsSpinnerArrayAdapter arrayAdapter_SortSetting_Bookshelf = new SettingsSpinnerArrayAdapter(this.getContext(), R.layout.item_spinner, getList_Spinner_Sort_Shelf());
        spinner_SortSetting_Bookshelf.setAdapter(arrayAdapter_SortSetting_Bookshelf);
        String code = mData.getSharedPreferences().getString(MyBookshelfApplicationData.KEY_SHELF_BOOKS_ORDER, getString(R.string.ShelfBooks_SortSetting_Code_Registered_Ascending));
        spinner_SortSetting_Bookshelf.setSelection(arrayAdapter_SortSetting_Bookshelf.getPosition(code), false);
        spinner_SortSetting_Bookshelf.setOnItemSelectedListener(listener_SortSetting_Bookshelf);
    }


    private void initSpinner_SortSetting_SearchResult(View view) {
        Spinner spinner_SortSetting_SearchResult = view.findViewById(R.id.SettingsFragment_Spinner_SortSetting_SearchResult);
        SettingsSpinnerArrayAdapter arrayAdapter_SortSetting_SearchResult = new SettingsSpinnerArrayAdapter(getContext(), R.layout.item_spinner, getList_Spinner_Sort_SearchResult());
        spinner_SortSetting_SearchResult.setAdapter(arrayAdapter_SortSetting_SearchResult);
        String code = mData.getSharedPreferences().getString(MyBookshelfApplicationData.KEY_SEARCH_BOOKS_ORDER, getString(R.string.ShelfBooks_SortSetting_Code_SalesDate_Descending));
        spinner_SortSetting_SearchResult.setSelection(arrayAdapter_SortSetting_SearchResult.getPosition(code), false);
        spinner_SortSetting_SearchResult.setOnItemSelectedListener(listener_SortSetting_SearchResult);
    }


    private List<BaseSpinnerItem> getList_Spinner_Sort_Shelf() {
        List<BaseSpinnerItem> list = new ArrayList<>();
        Resources res = getResources();
        TypedArray array = res.obtainTypedArray(R.array.Spinner_ShelfBooks_SortSetting);
        for (int i = 0; i < array.length(); ++i) {
            int id = array.getResourceId(i, -1);
            if (id > -1) {
                String[] item = res.getStringArray(id);
                list.add(new BaseSpinnerItem(item[0], item[1]));
            }
        }
        array.recycle();
        return list;
    }

    private List<BaseSpinnerItem> getList_Spinner_Sort_SearchResult() {
        List<BaseSpinnerItem> list = new ArrayList<>();
        Resources res = getResources();
        TypedArray array = res.obtainTypedArray(R.array.Spinner_SearchBooks_SortSetting);
        for (int i = 0; i < array.length(); ++i) {
            int id = array.getResourceId(i, -1);
            if (id > -1) {
                String[] item = res.getStringArray(id);
                list.add(new BaseSpinnerItem(item[0], item[1]));
            }
        }
        array.recycle();
        return list;
    }


    AdapterView.OnItemSelectedListener listener_SortSetting_Bookshelf = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter,
                                   View v, int position, long id) {
            BaseSpinnerItem item = (BaseSpinnerItem) adapter.getItemAtPosition(position);
            if (D) Log.d(TAG, "selected: " + item.getLabel());
            mData.getSharedPreferences().edit().putString(MyBookshelfApplicationData.KEY_SHELF_BOOKS_ORDER, item.getCode()).apply();
 //           mData.updateList_MyBookshelf();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapter) {
        }
    };

    AdapterView.OnItemSelectedListener listener_SortSetting_SearchResult = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter,
                                   View v, int position, long id) {
            BaseSpinnerItem item = (BaseSpinnerItem) adapter.getItemAtPosition(position);
            if (D) Log.d(TAG, "selected: " + item.getLabel());
            mData.getSharedPreferences().edit().putString(MyBookshelfApplicationData.KEY_SEARCH_BOOKS_ORDER, item.getCode()).apply();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapter) {
        }
    };


}

