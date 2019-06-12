package jp.gr.java_conf.nuranimation.my_bookshelf.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.MainActivity;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.adapter.SpinnerArrayAdapter;
import jp.gr.java_conf.nuranimation.my_bookshelf.background.BookService;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseProgressDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseSpinnerItem;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BundleBuilder;


public class SettingsFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = SettingsFragment.class.getSimpleName();
    private static final boolean D = true;

    private static final String KEY_CURRENT_STATE = "KEY_CURRENT_STATE";
    private static final String KEY_IS_ALLOWED_PERMISSIONS = "KEY_IS_ALLOWED_PERMISSIONS";
    private static final String KEY_IS_LOGGED_IN = "KEY_IS_LOGGED_IN";

    private MyBookshelfApplicationData mApplicationData;

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
            isAllowedPermissions = savedInstanceState.getBoolean(KEY_IS_ALLOWED_PERMISSIONS, false);
            isLogged_in = savedInstanceState.getBoolean(KEY_IS_LOGGED_IN, false);
            mSettingsState = savedInstanceState.getInt(KEY_CURRENT_STATE, BookService.STATE_NONE);
        } else {
            if (isAllowedAllPermissions(mApplicationData.getUse_Permissions())) {
                isAllowedPermissions = true;
            }
            if (mApplicationData.getSharedPreferences().contains(MyBookshelfApplicationData.KEY_ACCESS_TOKEN)) {
                isLogged_in = true;
            }
        }
        initSpinner(view);
        initButton(view, isAllowedPermissions, isLogged_in);
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
        if (requestCode == REQUEST_CODE_DROPBOX_LOGOUT) {
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
        Spinner mShelfBooksSortSpinner = view.findViewById(R.id.ShelfBooksSortSpinner);
        SpinnerArrayAdapter mShelfBooksSortAdapter = new SpinnerArrayAdapter(getContext(), R.layout.item_spinner, getShelfBooksSortSpinnerList());
        mShelfBooksSortSpinner.setAdapter(mShelfBooksSortAdapter);
        String code = mApplicationData.getSharedPreferences().getString(MyBookshelfApplicationData.KEY_SHELF_BOOKS_ORDER, getString(R.string.ShelfBooksSort_Code_REGISTERED_ASCENDING));
        mShelfBooksSortSpinner.setSelection(mShelfBooksSortAdapter.getPosition(code), false);
        mShelfBooksSortSpinner.setOnItemSelectedListener(mShelfBooksSortSpinnerListener);

        Spinner mSearchBooksSortSpinner = view.findViewById(R.id.SearchBooksSortSpinner);
        SpinnerArrayAdapter mSearchBooksSortAdapter = new SpinnerArrayAdapter(getContext(), R.layout.item_spinner, getSearchBooksSortSpinnerList());
        mSearchBooksSortSpinner.setAdapter(mSearchBooksSortAdapter);
        code = mApplicationData.getSharedPreferences().getString(MyBookshelfApplicationData.KEY_SEARCH_BOOKS_ORDER, getString(R.string.SearchBooksSort_Code_SALES_DATE_DESCENDING));
        mSearchBooksSortSpinner.setSelection(mSearchBooksSortAdapter.getPosition(code), false);
        mSearchBooksSortSpinner.setOnItemSelectedListener(mSearchBooksSortSpinnerListener);
    }

    private void initButton(View view, boolean isAllowedPermissions, boolean isLogged_in){
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



    private List<BaseSpinnerItem> getShelfBooksSortSpinnerList() {
        List<BaseSpinnerItem> list = new ArrayList<>();
        Resources res = getResources();
        TypedArray array = res.obtainTypedArray(R.array.ShelfBooksSortSpinner);
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

    private List<BaseSpinnerItem> getSearchBooksSortSpinnerList() {
        List<BaseSpinnerItem> list = new ArrayList<>();
        Resources res = getResources();
        TypedArray array = res.obtainTypedArray(R.array.SearchBooksSortSpinner);
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


    AdapterView.OnItemSelectedListener mShelfBooksSortSpinnerListener = new AdapterView.OnItemSelectedListener() {
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

    AdapterView.OnItemSelectedListener mSearchBooksSortSpinnerListener = new AdapterView.OnItemSelectedListener() {
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
        bundle.putString(BaseDialogFragment.KEY_TITLE, getString(R.string.DialogTitle_Logout_Dropbox));
        bundle.putString(BaseDialogFragment.KEY_MESSAGE, getString(R.string.DialogMessage_Logout_Dropbox));
        bundle.putString(BaseDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.DialogButton_Label_Positive));
        bundle.putString(BaseDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.DialogButton_Label_Negative));
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
                        .put(BaseProgressDialogFragment.title, getString(R.string.ProgressTitle_Export))
                        .put(BaseProgressDialogFragment.message, "")
                        .build();
                setProgressBundle(progress);
                break;
            case BookService.STATE_IMPORT_START:
            case BookService.STATE_IMPORT_FINISH:
                progress = new BundleBuilder()
                        .put(BaseProgressDialogFragment.title, getString(R.string.ProgressTitle_Import))
                        .put(BaseProgressDialogFragment.message, "")
                        .build();
                setProgressBundle(progress);
                break;
            case BookService.STATE_BACKUP_START:
            case BookService.STATE_BACKUP_FINISH:
                progress = new BundleBuilder()
                        .put(BaseProgressDialogFragment.title, getString(R.string.ProgressTitle_Backup))
                        .put(BaseProgressDialogFragment.message, "")
                        .build();
                setProgressBundle(progress);
                break;
            case BookService.STATE_RESTORE_START:
            case BookService.STATE_RESTORE_FINISH:
                progress = new BundleBuilder()
                        .put(BaseProgressDialogFragment.title, getString(R.string.ProgressTitle_Restore))
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




































}

