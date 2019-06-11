package jp.gr.java_conf.nuranimation.my_bookshelf.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import jp.gr.java_conf.nuranimation.my_bookshelf.MainActivity;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.DropboxManager;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.FileManager;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.adapter.SettingsSpinnerArrayAdapter;
import jp.gr.java_conf.nuranimation.my_bookshelf.background.BookService;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseProgressDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseSpinnerItem;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.ErrorStatus;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BundleBuilder;


public class SettingsFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = SettingsFragment.class.getSimpleName();
    private static final boolean D = true;

    private static final String KEY_CURRENT_STATE = "KEY_CURRENT_STATE";
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

    private MyBookshelfApplicationData mApplicationData;
    private DropboxManager mDropboxManager;
    private FileManager mFileManager;

    private Button mButtonExport;
    private Button mButtonImport;
    private Button mButtonLogin;
    private Button mButtonLogout;
    private Button mButtonBackup;
    private Button mButtonRestore;

    private boolean isAllowedPermissions;
    private boolean isLogged_in;
    private int mSettingsState = BookService.STATE_NONE;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mApplicationData = (MyBookshelfApplicationData) context.getApplicationContext();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (D) Log.d(TAG, "onViewCreated");
        if (getActivity() != null) {
            getActivity().setTitle(R.string.Navigation_Item_Settings);
        }
        if (savedInstanceState != null) {
            isAllowedPermissions = savedInstanceState.getBoolean(KEY_IS_ALLOWED_PERMISSIONS);
            isLogged_in = savedInstanceState.getBoolean(KEY_IS_LOGGED_IN);
            mSettingsState = savedInstanceState.getInt(KEY_CURRENT_STATE, 0);
        } else {
            if (isAllowedAllPermissions(mApplicationData.getUse_Permissions())) {
                isAllowedPermissions = true;
            }
            if (mApplicationData.getSharedPreferences().contains(MyBookshelfApplicationData.KEY_ACCESS_TOKEN)) {
                isLogged_in = true;
            }
        }
        initSpinner(view);
        initButton(view);
        setProgress(mSettingsState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (D) Log.i(TAG, "onResume");
        checkSettingsState();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (D) Log.i(TAG, "onPause");
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_STATE, mSettingsState);
        outState.putBoolean(KEY_IS_ALLOWED_PERMISSIONS, isAllowedPermissions);
        outState.putBoolean(KEY_IS_LOGGED_IN, isLogged_in);
    }


    @Override
    public void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        super.onBaseDialogSucceeded(requestCode, resultCode, params);
        switch (requestCode) {
            case REQUEST_CODE_DROPBOX_LOGOUT:
                switch (resultCode) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (D) Log.d(TAG, "Log out button pressed");
                        mApplicationData.getSharedPreferences().edit().remove(MyBookshelfApplicationData.KEY_ACCESS_TOKEN).apply();
                        isLogged_in = false;
                        enableDropboxFunction(false);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        if (D) Log.d(TAG, "Log out button cancel");
                        break;
                }
                break;
        }
    }

    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {
        super.onBaseDialogCancelled(requestCode, params);
    }

    @Override
    protected void onAllowPermission(String permission) {
        super.onAllowPermission(permission);
        isAllowedPermissions = true;
        enableButton(true);
    }

    @Override
    protected void onDenyPermission(String permission) {
        super.onDenyPermission(permission);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.settings_button_export:
                onClickExport();
                break;
            case R.id.settings_button_import:
                onClickImport();
                break;
            case R.id.settings_button_backup:
                onClickBackup();
                break;
            case R.id.settings_button_restore:
                onClickRestore();
                break;
            case R.id.settings_button_login_dropbox:
                onClickLogin();
                break;
            case R.id.settings_button_logout_dropbox:
                onClickLogout();
                break;
        }
    }



    private void initSpinner(View view) {
        Spinner spinner_SortSetting_Bookshelf = view.findViewById(R.id.SettingsFragment_Spinner_SortSetting_Bookshelf);
        SettingsSpinnerArrayAdapter arrayAdapter_SortSetting_Bookshelf = new SettingsSpinnerArrayAdapter(this.getContext(), R.layout.item_spinner, getList_Spinner_Sort_Shelf());
        spinner_SortSetting_Bookshelf.setAdapter(arrayAdapter_SortSetting_Bookshelf);
        String code = mApplicationData.getSharedPreferences().getString(MyBookshelfApplicationData.KEY_SHELF_BOOKS_ORDER, getString(R.string.ShelfBooks_SortSetting_Code_Registered_Ascending));
        spinner_SortSetting_Bookshelf.setSelection(arrayAdapter_SortSetting_Bookshelf.getPosition(code), false);
        spinner_SortSetting_Bookshelf.setOnItemSelectedListener(listener_SortSetting_Bookshelf);

        Spinner spinner_SortSetting_SearchResult = view.findViewById(R.id.SettingsFragment_Spinner_SortSetting_SearchResult);
        SettingsSpinnerArrayAdapter arrayAdapter_SortSetting_SearchResult = new SettingsSpinnerArrayAdapter(getContext(), R.layout.item_spinner, getList_Spinner_Sort_SearchResult());
        spinner_SortSetting_SearchResult.setAdapter(arrayAdapter_SortSetting_SearchResult);
        String code_search = mApplicationData.getSharedPreferences().getString(MyBookshelfApplicationData.KEY_SEARCH_BOOKS_ORDER, getString(R.string.ShelfBooks_SortSetting_Code_SalesDate_Descending));
        spinner_SortSetting_SearchResult.setSelection(arrayAdapter_SortSetting_SearchResult.getPosition(code_search), false);
        spinner_SortSetting_SearchResult.setOnItemSelectedListener(listener_SortSetting_SearchResult);
    }

    private void initButton(View view){
        mButtonExport = view.findViewById(R.id.settings_button_export);
        mButtonExport.setOnClickListener(this);
        mButtonImport = view.findViewById(R.id.settings_button_import);
        mButtonImport.setOnClickListener(this);
        mButtonBackup = view.findViewById(R.id.settings_button_backup);
        mButtonBackup.setOnClickListener(this);
        mButtonRestore = view.findViewById(R.id.settings_button_restore);
        mButtonRestore.setOnClickListener(this);
        mButtonLogin = view.findViewById(R.id.settings_button_login_dropbox);
        mButtonLogin.setOnClickListener(this);
        mButtonLogout = view.findViewById(R.id.settings_button_logout_dropbox);
        mButtonLogout.setOnClickListener(this);
        enableButton(isAllowedPermissions);
        enableDropboxFunction(isLogged_in);
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
            mApplicationData.getSharedPreferences().edit().putString(MyBookshelfApplicationData.KEY_SHELF_BOOKS_ORDER, item.getCode()).apply();
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
            mApplicationData.getSharedPreferences().edit().putString(MyBookshelfApplicationData.KEY_SEARCH_BOOKS_ORDER, item.getCode()).apply();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapter) {
        }
    };


    private void enableDropboxFunction(boolean enable){
        if(enable){
            mButtonLogin.setVisibility(View.GONE);
            mButtonLogout.setVisibility(View.VISIBLE);
            mButtonBackup.setVisibility(View.VISIBLE);
            mButtonRestore.setVisibility(View.VISIBLE);
        }else{
            mButtonLogin.setVisibility(View.VISIBLE);
            mButtonLogout.setVisibility(View.GONE);
            mButtonBackup.setVisibility(View.GONE);
            mButtonRestore.setVisibility(View.GONE);
        }
    }

    private void enableButton(boolean enable){
        if(enable){
            mButtonExport.setBackgroundResource(R.drawable.selector_button);
            mButtonImport.setBackgroundResource(R.drawable.selector_button);
            mButtonBackup.setBackgroundResource(R.drawable.selector_button);
            mButtonRestore.setBackgroundResource(R.drawable.selector_button);
        }else{
            mButtonExport.setBackgroundResource(R.drawable.selector_button_disable);
            mButtonImport.setBackgroundResource(R.drawable.selector_button_disable);
            mButtonBackup.setBackgroundResource(R.drawable.selector_button_disable);
            mButtonRestore.setBackgroundResource(R.drawable.selector_button_disable);
        }
    }


    private void onClickExport() {
        if (isAllowedAllPermissions(mApplicationData.getUse_Permissions())) {
            if (getActivity() instanceof MainActivity) {
                BookService service = ((MainActivity) getActivity()).getService();
                if (service != null) {
                    mSettingsState = BookService.STATE_EXPORT_START;
                    service.exportCSV();
                }
            }
        } else {
            requestPermissions(mApplicationData.getUse_Permissions());
        }
    }

    private void onClickImport(){
        if (isAllowedAllPermissions(mApplicationData.getUse_Permissions())) {
            if (getActivity() instanceof MainActivity) {
                BookService service = ((MainActivity) getActivity()).getService();
                if (service != null) {
                    mSettingsState = BookService.STATE_IMPORT_START;
                    service.importCSV();
                }
            }
        } else {
            requestPermissions(mApplicationData.getUse_Permissions());
        }
    }

    private void onClickBackup(){
        if (isAllowedAllPermissions(mApplicationData.getUse_Permissions())) {
            if (getActivity() instanceof MainActivity) {
                BookService service = ((MainActivity) getActivity()).getService();
                if (service != null) {
                    mSettingsState = BookService.STATE_BACKUP_START;
                    service.backupCSV();
                }
            }
        } else {
            requestPermissions(mApplicationData.getUse_Permissions());
        }
    }

    private void onClickRestore(){
        if (isAllowedAllPermissions(mApplicationData.getUse_Permissions())) {
            if (getActivity() instanceof MainActivity) {
                BookService service = ((MainActivity) getActivity()).getService();
                if (service != null) {
                    mSettingsState = BookService.STATE_RESTORE_START;
                    setProgress(BookService.STATE_RESTORE_START);
                    showProgressDialog();
                    service.restoreCSV();
                }
            }
        } else {
            requestPermissions(mApplicationData.getUse_Permissions());
        }
    }


    private void onClickLogin(){
        if (getActivity() instanceof MainActivity) {
            BookService service = ((MainActivity) getActivity()).getService();
            if (service != null) {
                mSettingsState = BookService.STATE_DROPBOX_LOGIN;
                service.setServiceState(BookService.STATE_DROPBOX_LOGIN);
                service.startAuthenticate();


            }
        }
    }



    private void onClickLogout(){
        Bundle bundle = new Bundle();
        bundle.putString(BaseDialogFragment.KEY_TITLE, getString(R.string.Dialog_Logout_Title));
        bundle.putString(BaseDialogFragment.KEY_MESSAGE, getString(R.string.Dialog_Logout_Message));
        bundle.putString(BaseDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.Dialog_Button_Positive));
        bundle.putString(BaseDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.Dialog_Button_Negative));
        bundle.putInt(BaseDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_DROPBOX_LOGOUT);
        if (getActivity() != null) {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            BaseDialogFragment dialog = BaseDialogFragment.newInstance(this, bundle);
            dialog.show(manager, SettingsFragment.TAG);
        }
    }














    private void setProgress(int state){
        Bundle progress;
        switch (state) {
            case BookService.STATE_EXPORT_START:
            case BookService.STATE_EXPORT_FINISH:
                progress = new BundleBuilder()
                        .put(BaseProgressDialogFragment.title, getString(R.string.Progress_Export))
                        .put(BaseProgressDialogFragment.message, "")
                        .build();
                setProgressBundle(progress);
                break;
            case BookService.STATE_IMPORT_START:
            case BookService.STATE_IMPORT_FINISH:
                progress = new BundleBuilder()
                        .put(BaseProgressDialogFragment.title, getString(R.string.Progress_Import))
                        .put(BaseProgressDialogFragment.message, "")
                        .build();
                setProgressBundle(progress);
                break;
            case BookService.STATE_BACKUP_START:
            case BookService.STATE_BACKUP_FINISH:
                progress = new BundleBuilder()
                        .put(BaseProgressDialogFragment.title, getString(R.string.Progress_Backup))
                        .put(BaseProgressDialogFragment.message, "")
                        .build();
                setProgressBundle(progress);
                break;
            case BookService.STATE_RESTORE_START:
            case BookService.STATE_RESTORE_FINISH:
                progress = new BundleBuilder()
                        .put(BaseProgressDialogFragment.title, getString(R.string.Progress_Restore))
                        .put(BaseProgressDialogFragment.message, "")
                        .build();
                setProgressBundle(progress);
                break;
        }
    }


    public void checkSettingsState(){
        if(D) Log.d(TAG,"checkSettingsState");
        if(getActivity() instanceof MainActivity){
            BookService service = ((MainActivity) getActivity()).getService();
            if(service != null) {
                switch (service.getServiceState()) {
                    case BookService.STATE_EXPORT_START:
                        mSettingsState = BookService.STATE_EXPORT_START;
                        break;
                    case BookService.STATE_EXPORT_FINISH:
                        mSettingsState = BookService.STATE_EXPORT_FINISH;
                        break;
                    case BookService.STATE_IMPORT_START:
                        mSettingsState = BookService.STATE_IMPORT_START;
                        break;
                    case BookService.STATE_IMPORT_FINISH:
                        mSettingsState = BookService.STATE_IMPORT_FINISH;
                        break;
                    case BookService.STATE_BACKUP_START:
                        mSettingsState = BookService.STATE_BACKUP_START;
                        break;
                    case BookService.STATE_BACKUP_FINISH:
                        mSettingsState = BookService.STATE_BACKUP_FINISH;
                        break;
                    case BookService.STATE_RESTORE_START:
                        mSettingsState = BookService.STATE_RESTORE_START;
                        break;
                    case BookService.STATE_RESTORE_FINISH:
                        mSettingsState = BookService.STATE_RESTORE_FINISH;
                        break;
                    case BookService.STATE_DROPBOX_LOGIN:
                        String token = service.getAccessToken();
                        if (token != null) {
                            // Log-in Success
                            if(D) Log.d(TAG,"Log-in Success");
                            mApplicationData.getSharedPreferences().edit().putString(MyBookshelfApplicationData.KEY_ACCESS_TOKEN, token).apply();
                            isLogged_in = true;
                            enableDropboxFunction(true);
                        }
                        service.setServiceState(BookService.STATE_NONE);
                        service.stopForeground(false);
                        service.stopSelf();
                        mSettingsState = BookService.STATE_NONE;
                        break;
                }
            }
        }
    }



    @Override
    public void onReceiveBroadcast(Context context, Intent intent){
        String action = intent.getAction();
        if(action != null){
            switch (action){
                case FILTER_ACTION_UPDATE_SERVICE_STATE:
                    int state = intent.getIntExtra(KEY_UPDATE_SERVICE_STATE, 0);
                    switch (state) {
                        case BookService.STATE_NONE:
                            if (D) Log.d(TAG, "STATE_NONE");
                            mSettingsState = BookService.STATE_NONE;
                            getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_DISMISS).sendToTarget();
                            break;
                        case BookService.STATE_EXPORT_FINISH:
                            mSettingsState = BookService.STATE_NONE;
                            getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_DISMISS).sendToTarget();
                            break;
                        case BookService.STATE_IMPORT_FINISH:
                            mSettingsState = BookService.STATE_NONE;
                            getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_DISMISS).sendToTarget();
                            break;
                        case BookService.STATE_BACKUP_FINISH:
                            mSettingsState = BookService.STATE_NONE;
                            getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_DISMISS).sendToTarget();
                            break;
                        case BookService.STATE_RESTORE_FINISH:
                            mSettingsState = BookService.STATE_NONE;
                            getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_DISMISS).sendToTarget();
                            break;
                    }
                    break;
                case FILTER_ACTION_UPDATE_PROGRESS:
                    String progress = intent.getStringExtra(KEY_UPDATE_PROGRESS);
                    getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_UPDATE, progress).sendToTarget();
                    break;
            }
        }
    }













































    public void callback(ButtonAction action, boolean result) {
        mCurrentAction = ButtonAction.None;
        getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_DISMISS).sendToTarget();
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



    public void AsyncTaskCSV(ButtonAction action) {
        new AsyncCSV(this, action).execute();
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
            Message msg = mHandler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_SHOW);
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






}

