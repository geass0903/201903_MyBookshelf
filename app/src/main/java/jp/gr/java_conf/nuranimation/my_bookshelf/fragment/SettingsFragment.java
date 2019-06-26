package jp.gr.java_conf.nuranimation.my_bookshelf.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.MainActivity;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.adapter.SortSpinnerArrayAdapter;
import jp.gr.java_conf.nuranimation.my_bookshelf.background.BookService;
import jp.gr.java_conf.nuranimation.my_bookshelf.background.FileBackupThread;
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
            getActivity().setTitle(R.string.navigation_item_settings);
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
    protected void onAllowAllPermissions() {
        super.onAllowAllPermissions();
        isAllowedPermissions = true;
        enableButton(true);
    }

    @Override
    protected void onDenyPermissions() {
        super.onDenyPermissions();
        if(D) Log.d(TAG, "Denied permission");
        isAllowedPermissions = false;
        enableButton(false);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.settings_button_export:
                onClickBackup(FileBackupThread.TYPE_EXPORT);
                break;
            case R.id.settings_button_import:
                onClickBackup(FileBackupThread.TYPE_IMPORT);
                break;
            case R.id.settings_button_backup:
                onClickBackup(FileBackupThread.TYPE_BACKUP);
                break;
            case R.id.settings_button_restore:
                onClickBackup(FileBackupThread.TYPE_RESTORE);
                break;
            case R.id.settings_button_login_dropbox:
                onClickLogin();
                break;
            case R.id.settings_button_logout_dropbox:
                onClickLogout();
                break;
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
                        case BookService.STATE_EXPORT_COMPLETE:
                        case BookService.STATE_IMPORT_COMPLETE:
                        case BookService.STATE_BACKUP_COMPLETE:
                        case BookService.STATE_RESTORE_COMPLETE:
                            checkSettingsState();
                            break;
                    }
                    break;
                case FILTER_ACTION_UPDATE_PROGRESS:
                    int type = intent.getIntExtra(KEY_PROGRESS_TYPE,FileBackupThread.TYPE_UNKNOWN);
                    String progress = intent.getStringExtra(KEY_PROGRESS);
                    if(progress == null){
                        progress = "";
                    }
                    String message = null;
                    switch (type){
                        case FileBackupThread.PROGRESS_TYPE_EXPORT_BOOKS:
                            message = getString(R.string.progress_message_export_books);
                            progress = progress + getString(R.string.progress_unit_book);
                            break;
                        case FileBackupThread.PROGRESS_TYPE_EXPORT_AUTHORS:
                            message = getString(R.string.progress_message_export_authors);
                            break;
                        case FileBackupThread.PROGRESS_TYPE_IMPORT_BOOKS:
                            message = getString(R.string.progress_message_import_books);
                            progress = progress + getString(R.string.progress_unit_book);
                            break;
                        case FileBackupThread.PROGRESS_TYPE_IMPORT_AUTHORS:
                            message = getString(R.string.progress_message_import_authors);
                            break;
                        case FileBackupThread.PROGRESS_TYPE_UPLOAD_BOOKS:
                            message = getString(R.string.progress_message_upload_books);
                            progress = progress + getString(R.string.progress_unit_byte);
                            break;
                        case FileBackupThread.PROGRESS_TYPE_UPLOAD_AUTHORS:
                            message = getString(R.string.progress_message_upload_authors);
                            progress = progress + getString(R.string.progress_unit_byte);
                            break;
                        case FileBackupThread.PROGRESS_TYPE_DOWNLOAD_BOOKS:
                            message = getString(R.string.progress_message_download_books);
                            progress = progress + getString(R.string.progress_unit_byte);
                            break;
                        case FileBackupThread.PROGRESS_TYPE_DOWNLOAD_AUTHORS:
                            message = getString(R.string.progress_message_download_authors);
                            progress = progress + getString(R.string.progress_unit_byte);
                            break;
                        case FileBackupThread.PROGRESS_TYPE_REGISTER:
                            message = getString(R.string.progress_message_register);
                            break;
                    }
                    Bundle bundle = new BundleBuilder()
                            .put(BaseProgressDialogFragment.message, message)
                            .put(BaseProgressDialogFragment.progress, progress)
                            .build();
                    getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_UPDATE, bundle).sendToTarget();
                    break;
            }
        }
    }






    private void initSpinner(View view) {
        Spinner mShelfBooksSortSpinner = view.findViewById(R.id.shelf_books_sort_spinner);
        SortSpinnerArrayAdapter mShelfBooksSortAdapter = new SortSpinnerArrayAdapter(getContext(), R.layout.item_sort_spinner, getSpinnerItemList(R.array.shelf_books_sort_spinner));
        mShelfBooksSortSpinner.setAdapter(mShelfBooksSortAdapter);
        String sort = mApplicationData.getShelfBooksSortSetting();
        mShelfBooksSortSpinner.setSelection(mShelfBooksSortAdapter.getPosition(sort));
        mShelfBooksSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        });

        Spinner mSearchBooksSortSpinner = view.findViewById(R.id.search_books_sort_spinner);
        SortSpinnerArrayAdapter mSearchBooksSortAdapter = new SortSpinnerArrayAdapter(getContext(), R.layout.item_sort_spinner, getSpinnerItemList(R.array.search_books_sort_spinner));
        mSearchBooksSortSpinner.setAdapter(mSearchBooksSortAdapter);
        sort = mApplicationData.getSearchBooksSortSetting();
        mSearchBooksSortSpinner.setSelection(mSearchBooksSortAdapter.getPosition(sort));
        mSearchBooksSortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        });
    }


    private void initButton(View view, boolean isAllowedPermissions, boolean isLogged_in){
        mButtonExport = view.findViewById(R.id.settings_button_export);
        mButtonExport.setOnClickListener(this);
        setColorFilterButtonDrawables(mButtonExport, Color.parseColor("#FFFFFF"));
        mButtonImport = view.findViewById(R.id.settings_button_import);
        mButtonImport.setOnClickListener(this);
        setColorFilterButtonDrawables(mButtonImport, Color.parseColor("#FFFFFF"));
        mButtonBackup = view.findViewById(R.id.settings_button_backup);
        mButtonBackup.setOnClickListener(this);
        setColorFilterButtonDrawables(mButtonBackup, Color.parseColor("#FFFFFF"));
        mButtonRestore = view.findViewById(R.id.settings_button_restore);
        mButtonRestore.setOnClickListener(this);
        setColorFilterButtonDrawables(mButtonRestore, Color.parseColor("#FFFFFF"));
        mButtonLogin = view.findViewById(R.id.settings_button_login_dropbox);
        mButtonLogin.setOnClickListener(this);
        setColorFilterButtonDrawables(mButtonLogin, Color.parseColor("#FFFFFF"));
        mButtonLogout = view.findViewById(R.id.settings_button_logout_dropbox);
        mButtonLogout.setOnClickListener(this);
        setColorFilterButtonDrawables(mButtonLogout, Color.parseColor("#FFFFFF"));
        enableButton(isAllowedPermissions);
        enableDropboxFunction(isLogged_in);
    }

    private void setColorFilterButtonDrawables(Button button, int color){
        Drawable[] drawables = button.getCompoundDrawables();
        for(Drawable drawable : drawables){
            if(drawable != null){
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        }
        button.setCompoundDrawablesWithIntrinsicBounds(drawables[0],drawables[1],drawables[2],drawables[3]);
    }

    private List<BaseSpinnerItem> getSpinnerItemList(int array_id){
        List<BaseSpinnerItem> list = new ArrayList<>();
        Resources res = getResources();
        TypedArray array = res.obtainTypedArray(array_id);
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
            mButtonExport.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_primary);
            mButtonImport.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_primary);
            mButtonBackup.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_primary);
            mButtonRestore.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_primary);
        }else{
            mButtonExport.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_gray);
            mButtonImport.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_gray);
            mButtonBackup.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_gray);
            mButtonRestore.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_gray);
        }
    }


    private void onClickBackup(int type){
        if (isAllowedAllPermissions(mApplicationData.getUse_Permissions())) {
            if (getActivity() instanceof MainActivity) {
                BookService service = ((MainActivity) getActivity()).getService();
                if (service != null) {
                    switch(type){
                        case FileBackupThread.TYPE_EXPORT:
                            mSettingsState = BookService.STATE_EXPORT_INCOMPLETE;
                            break;
                        case FileBackupThread.TYPE_IMPORT:
                            mSettingsState = BookService.STATE_IMPORT_INCOMPLETE;
                            break;
                        case FileBackupThread.TYPE_BACKUP:
                            mSettingsState = BookService.STATE_BACKUP_INCOMPLETE;
                            break;
                        case FileBackupThread.TYPE_RESTORE:
                            mSettingsState = BookService.STATE_RESTORE_INCOMPLETE;
                            break;
                        default:
                            break;
                    }
                    setProgress(mSettingsState);
                    showProgressDialog();
                    service.fileBackup(type);
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
        bundle.putString(BaseDialogFragment.KEY_TITLE, getString(R.string.dialog_title_logout_dropbox));
        bundle.putString(BaseDialogFragment.KEY_MESSAGE, getString(R.string.dialog_message_logout_dropbox));
        bundle.putString(BaseDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
        bundle.putString(BaseDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
        bundle.putInt(BaseDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_DROPBOX_LOGOUT);
        if (getActivity() != null) {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            BaseDialogFragment dialog = BaseDialogFragment.newInstance(this, bundle);
            dialog.show(manager, SettingsFragment.TAG);
        }
    }


    public void checkSettingsState(){
        if(D) Log.d(TAG,"checkSettingsState");
        if(getActivity() instanceof MainActivity){
            BookService service = ((MainActivity) getActivity()).getService();
            if(service != null) {
                switch (service.getServiceState()) {
                    case BookService.STATE_EXPORT_COMPLETE:
                    case BookService.STATE_IMPORT_COMPLETE:
                    case BookService.STATE_BACKUP_COMPLETE:
                    case BookService.STATE_RESTORE_COMPLETE:
                        mSettingsState = service.getServiceState();
                        loadFileBackupResult();
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


    private void loadFileBackupResult() {
        if (D) Log.d(TAG, "loadFileBackupResult");
        String message = null;
        if (getActivity() instanceof MainActivity) {
            BookService service = ((MainActivity) getActivity()).getService();
            if (service != null) {
                FileBackupThread.Result result = service.getFileBackupResult();
                if(result.isSuccess()) {
                    switch (service.getServiceState()){
                        case BookService.STATE_EXPORT_COMPLETE:
                            message = getString(R.string.toast_success_export);
                            break;
                        case BookService.STATE_IMPORT_COMPLETE:
                            message = getString(R.string.toast_success_import);
                            break;
                        case BookService.STATE_BACKUP_COMPLETE:
                            message = getString(R.string.toast_success_backup);
                            break;
                        case BookService.STATE_RESTORE_COMPLETE:
                            message = getString(R.string.toast_success_restore);
                            break;
                    }
                }else{
                    message = result.getErrorMessage();
                }
                if(!TextUtils.isEmpty(message)){
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
                service.setServiceState(BookService.STATE_NONE);
                service.stopForeground(false);
                service.stopSelf();
            }
        }
        mSettingsState = BookService.STATE_NONE;
        getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_DISMISS).sendToTarget();
    }


}

