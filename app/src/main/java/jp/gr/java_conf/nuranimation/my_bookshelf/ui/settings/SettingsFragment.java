package jp.gr.java_conf.nuranimation.my_bookshelf.ui.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.dropbox.core.android.Auth;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.Result;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.SpinnerItem;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.prefs.MyBookshelfPreferences;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.thread.FileBackupThread;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.thread.base.BaseThread;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.BooksOrder;
import jp.gr.java_conf.nuranimation.my_bookshelf.service.BookService;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.MyBookshelfEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.util.OrderSpinnerArrayAdapter;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.NormalDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.ProgressDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.permission.PermissionsFragment;


public class SettingsFragment extends BaseFragment implements NormalDialogFragment.OnNormalDialogListener, ProgressDialogFragment.OnProgressDialogListener {
    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static final boolean D = true;

    private static final String DROP_BOX_KEY = "fh2si4dchz272b1";

    public static final String KEY_BACKUP_TYPE = "SettingsFragment.KEY_BACKUP_TYPE";

    private static final String TAG_DROPBOX_LOGOUT = "SettingsFragment.TAG_DROPBOX_LOGOUT";
    private static final String TAG_BACKUP_PROGRESS = "SettingsFragment.TAG_BACKUP_PROGRESS";

    private static final int REQUEST_CODE_DROPBOX_LOGOUT = 1;
    private static final int REQUEST_CODE_BACKUP_PROGRESS_DIALOG = 2;

    private MyBookshelfPreferences mPreferences;
    private Context mContext;

    private Button mButtonExport;
    private Button mButtonImport;
    private Button mButtonLogin;
    private Button mButtonLogout;
    private Button mButtonBackup;
    private Button mButtonRestore;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPreferences = new MyBookshelfPreferences(context.getApplicationContext());
        mContext = context;
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
        initSpinner(view);
        initButton(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (D) Log.i(TAG, "onResume");
        boolean isAllowedPermissions = PermissionsFragment.isAllowedAllPermissions(mContext, PermissionsFragment.USE_PERMISSIONS);
        enableBackupFunction(isAllowedPermissions);
        boolean hasToken = mPreferences.containsKeyAccessToken();
        enableDropboxFunction(hasToken);
        getFragmentListener().onFragmentEvent(MyBookshelfEvent.CHECK_SETTINGS_STATE, null);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (D) Log.i(TAG, "onPause");
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onNormalDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        if (requestCode == REQUEST_CODE_DROPBOX_LOGOUT) {
            switch (resultCode) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (D) Log.d(TAG, "Log out button pressed");
                    mPreferences.deleteAccessToken();
                    enableDropboxFunction(false);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    if (D) Log.d(TAG, "Log out button cancel");
                    break;
            }
        }
    }

    @Override
    public void onNormalDialogCancelled(int requestCode, Bundle params) {

    }

    @Override
    public void onProgressDialogCancelled(int requestCode, Bundle params) {
        if (requestCode == REQUEST_CODE_BACKUP_PROGRESS_DIALOG) {
            getFragmentListener().onFragmentEvent(MyBookshelfEvent.BACKUP_CANCEL, null);
        }
    }

    @Override
    protected void onReceiveLocalBroadcast(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case BookService.FILTER_ACTION_UPDATE_SERVICE_STATE:
                    int state = intent.getIntExtra(BookService.KEY_SERVICE_STATE, BookService.STATE_NONE);
                    switch (state) {
                        case BookService.STATE_NONE:
                            if (D) Log.d(TAG, "STATE_NONE");
                            break;
                        case BookService.STATE_EXPORT_COMPLETE:
                        case BookService.STATE_IMPORT_COMPLETE:
                        case BookService.STATE_BACKUP_COMPLETE:
                        case BookService.STATE_RESTORE_COMPLETE:
                            getFragmentListener().onFragmentEvent(MyBookshelfEvent.CHECK_SETTINGS_STATE, null);
                            break;
                    }
                    break;
                case BaseThread.FILTER_ACTION_UPDATE_PROGRESS:
                    String progress = intent.getStringExtra(BaseThread.KEY_PROGRESS_VALUE_TEXT);
                    if (progress == null) {
                        progress = "";
                    }
                    String message = intent.getStringExtra(BaseThread.KEY_PROGRESS_MESSAGE_TEXT);
                    if (message == null) {
                        message = "";
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(ProgressDialogFragment.KEY_MESSAGE, message);
                    bundle.putString(ProgressDialogFragment.KEY_PROGRESS, progress);
                    ProgressDialogFragment.updateProgress(this, bundle, TAG_BACKUP_PROGRESS);
                    break;
            }
        }
    }

    public void startBackup(int type) {
        String title = null;
        switch (type) {
            case FileBackupThread.TYPE_EXPORT:
                title = getString(R.string.progress_title_export);
                break;
            case FileBackupThread.TYPE_IMPORT:
                title = getString(R.string.progress_title_import);
                break;
            case FileBackupThread.TYPE_BACKUP:
                title = getString(R.string.progress_title_backup);
                break;
            case FileBackupThread.TYPE_RESTORE:
                title = getString(R.string.progress_title_restore);
                break;
            default:
                break;
        }
        Bundle bundle = new Bundle();
        bundle.putString(ProgressDialogFragment.KEY_TITLE, title);
        bundle.putBoolean(ProgressDialogFragment.KEY_CANCELABLE, true);
        ProgressDialogFragment.showProgressDialog(this, bundle, TAG_BACKUP_PROGRESS);
    }

    public void checkSettingsState(int state) {
        switch (state) {
            case BookService.STATE_EXPORT_COMPLETE:
            case BookService.STATE_IMPORT_COMPLETE:
            case BookService.STATE_BACKUP_COMPLETE:
            case BookService.STATE_RESTORE_COMPLETE:
                getFragmentListener().onFragmentEvent(MyBookshelfEvent.FINISH_BACKUP, null);
                break;
            case BookService.STATE_DROPBOX_AUTH:
                getFragmentListener().onFragmentEvent(MyBookshelfEvent.FINISH_DROPBOX_AUTH, null);
                break;
        }
    }

    public void finishBackup(Result result) {
        String message;
        if (result.isSuccess()) {
            switch (result.getType()) {
                case FileBackupThread.TYPE_EXPORT:
                    message = getString(R.string.toast_success_export);
                    break;
                case FileBackupThread.TYPE_IMPORT:
                    message = getString(R.string.toast_success_import);
                    break;
                case FileBackupThread.TYPE_BACKUP:
                    message = getString(R.string.toast_success_backup);
                    break;
                case FileBackupThread.TYPE_RESTORE:
                    message = getString(R.string.toast_success_restore);
                    break;
                default:
                    message = getString(R.string.toast_failed);
            }
        } else {
            message = result.getErrorMessage();
        }
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        ProgressDialogFragment.dismissProgressDialog(this, TAG_BACKUP_PROGRESS);
    }

    public void startDropboxAuth() {
        Auth.startOAuth2Authentication(mContext, DROP_BOX_KEY);
    }

    public void finishDropboxAuth() {
        String token = getAccessToken();
        if (token != null) {
            if (D) Log.d(TAG, "Log-in Success");
            mPreferences.setAccessToken(token);
            enableDropboxFunction(true);
        }
    }

    public void onAllowAllPermissions() {
        enableBackupFunction(true);
    }

    public void onDenyPermissions() {
        if (D) Log.d(TAG, "Denied permission");
        enableBackupFunction(false);
    }

    public boolean isAllowedPermissions() {
        return PermissionsFragment.isAllowedAllPermissions(mContext, PermissionsFragment.USE_PERMISSIONS);
    }

    public void requestPermissions() {
        Bundle bundle = new Bundle();
        bundle.putStringArray(PermissionsFragment.KEY_USE_PERMISSIONS, PermissionsFragment.USE_PERMISSIONS);
        getFragmentListener().onFragmentEvent(MyBookshelfEvent.REQUEST_PERMISSIONS, bundle);
    }

    private void enableDropboxFunction(boolean enable) {
        if (enable) {
            mButtonLogin.setVisibility(View.GONE);
            mButtonLogout.setVisibility(View.VISIBLE);
            mButtonBackup.setVisibility(View.VISIBLE);
            mButtonRestore.setVisibility(View.VISIBLE);
        } else {
            mButtonLogin.setVisibility(View.VISIBLE);
            mButtonLogout.setVisibility(View.GONE);
            mButtonBackup.setVisibility(View.GONE);
            mButtonRestore.setVisibility(View.GONE);
        }
    }

    private void enableBackupFunction(boolean enable) {
        if (enable) {
            mButtonExport.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_primary);
            mButtonImport.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_primary);
            mButtonBackup.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_primary);
            mButtonRestore.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_primary);
        } else {
            mButtonExport.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_gray);
            mButtonImport.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_gray);
            mButtonBackup.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_gray);
            mButtonRestore.setBackgroundResource(R.drawable.selector_rounded_rectangle_color_gray);
        }
    }

    private void showLogoutDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(NormalDialogFragment.KEY_TITLE, getString(R.string.dialog_title_logout_dropbox));
        bundle.putString(NormalDialogFragment.KEY_MESSAGE, getString(R.string.dialog_message_logout_dropbox));
        bundle.putString(NormalDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
        bundle.putString(NormalDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
        bundle.putInt(NormalDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_DROPBOX_LOGOUT);
        NormalDialogFragment.showNormalDialog(this, bundle, TAG_DROPBOX_LOGOUT);
    }

    private String getAccessToken() {
        return Auth.getOAuth2Token();
    }

    private void initSpinner(View view) {
        Spinner mShelfBooksOrderSpinner = view.findViewById(R.id.shelf_books_order_spinner);
        OrderSpinnerArrayAdapter mShelfBooksOrderAdapter = new OrderSpinnerArrayAdapter(getContext(), R.layout.item_order_spinner, getShelfBooksOrderSpinnerList());
        mShelfBooksOrderSpinner.setAdapter(mShelfBooksOrderAdapter);
        String code = mPreferences.getShelfBooksOrderCode();
        if (code == null) {
            code = BooksOrder.SHELF_BOOKS_ORDER_CODE_REGISTERED_ASC;
        }
        mShelfBooksOrderSpinner.setSelection(mShelfBooksOrderAdapter.getPosition(code));
        mShelfBooksOrderSpinner.setOnItemSelectedListener(mOnItemSelectedListener);

        Spinner mSearchBooksOrderSpinner = view.findViewById(R.id.search_books_order_spinner);
        OrderSpinnerArrayAdapter mSearchBooksOrderAdapter = new OrderSpinnerArrayAdapter(getContext(), R.layout.item_order_spinner, getSearchBooksOrderSpinnerList());
        mSearchBooksOrderSpinner.setAdapter(mSearchBooksOrderAdapter);
        code = mPreferences.getSearchBooksOrderCode();
        if (code == null) {
            code = BooksOrder.SEARCH_BOOKS_ORDER_CODE_SALES_DATE_DESC;
        }
        mSearchBooksOrderSpinner.setSelection(mSearchBooksOrderAdapter.getPosition(code));
        mSearchBooksOrderSpinner.setOnItemSelectedListener(mOnItemSelectedListener);
    }

    private void initButton(View view) {
        mButtonExport = view.findViewById(R.id.settings_button_export);
        mButtonExport.setOnClickListener(mOnClickListener);
        setColorFilterButtonDrawables(mButtonExport, Color.parseColor("#FFFFFF"));
        mButtonImport = view.findViewById(R.id.settings_button_import);
        mButtonImport.setOnClickListener(mOnClickListener);
        setColorFilterButtonDrawables(mButtonImport, Color.parseColor("#FFFFFF"));
        mButtonBackup = view.findViewById(R.id.settings_button_backup);
        mButtonBackup.setOnClickListener(mOnClickListener);
        setColorFilterButtonDrawables(mButtonBackup, Color.parseColor("#FFFFFF"));
        mButtonRestore = view.findViewById(R.id.settings_button_restore);
        mButtonRestore.setOnClickListener(mOnClickListener);
        setColorFilterButtonDrawables(mButtonRestore, Color.parseColor("#FFFFFF"));
        mButtonLogin = view.findViewById(R.id.settings_button_login_dropbox);
        mButtonLogin.setOnClickListener(mOnClickListener);
        setColorFilterButtonDrawables(mButtonLogin, Color.parseColor("#FFFFFF"));
        mButtonLogout = view.findViewById(R.id.settings_button_logout_dropbox);
        mButtonLogout.setOnClickListener(mOnClickListener);
        setColorFilterButtonDrawables(mButtonLogout, Color.parseColor("#FFFFFF"));
    }

    private void setColorFilterButtonDrawables(Button button, int color) {
        Drawable[] drawables = button.getCompoundDrawables();
        for (Drawable drawable : drawables) {
            if (drawable != null) {
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        }
        button.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
    }

    private List<SpinnerItem> getShelfBooksOrderSpinnerList() {
        List<SpinnerItem> list = new ArrayList<>();
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_TITLE_ASC, getString(R.string.label_shelf_books_order_title_ascending)));
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_TITLE_DESC, getString(R.string.label_shelf_books_order_title_descending)));
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_AUTHOR_ASC, getString(R.string.label_shelf_books_order_author_ascending)));
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_AUTHOR_DESC, getString(R.string.label_shelf_books_order_author_descending)));
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_SALES_DATE_ASC, getString(R.string.label_shelf_books_order_sales_date_ascending)));
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_SALES_DATE_DESC, getString(R.string.label_shelf_books_order_sales_date_descending)));
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_REGISTERED_ASC, getString(R.string.label_shelf_books_order_registered_ascending)));
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_REGISTERED_DESC, getString(R.string.label_shelf_books_order_registered_descending)));
        return list;
    }

    private List<SpinnerItem> getSearchBooksOrderSpinnerList() {
        List<SpinnerItem> list = new ArrayList<>();
        list.add(new SpinnerItem(BooksOrder.SEARCH_BOOKS_ORDER_CODE_SALES_DATE_ASC, getString(R.string.label_search_books_order_sales_date_ascending)));
        list.add(new SpinnerItem(BooksOrder.SEARCH_BOOKS_ORDER_CODE_SALES_DATE_DESC, getString(R.string.label_search_books_order_sales_date_descending)));
        return list;
    }

    private AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
            if (adapter.getItemAtPosition(position) instanceof SpinnerItem) {
                SpinnerItem item = (SpinnerItem) adapter.getItemAtPosition(position);
                if (D) Log.d(TAG, "selected: " + item.getLabel());
                Spinner spinner = (Spinner) adapter;
                switch (spinner.getId()) {
                    case R.id.shelf_books_order_spinner:
                        mPreferences.setShelfBooksOrderCode(item.getCode());
                        break;
                    case R.id.search_books_order_spinner:
                        mPreferences.setSearchBooksOrderCode(item.getCode());
                        break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.settings_button_export:
                    Bundle type_export = new Bundle();
                    type_export.putInt(KEY_BACKUP_TYPE, FileBackupThread.TYPE_EXPORT);
                    getFragmentListener().onFragmentEvent(MyBookshelfEvent.START_BACKUP, type_export);
                    break;
                case R.id.settings_button_import:
                    Bundle type_import = new Bundle();
                    type_import.putInt(KEY_BACKUP_TYPE, FileBackupThread.TYPE_IMPORT);
                    getFragmentListener().onFragmentEvent(MyBookshelfEvent.START_BACKUP, type_import);
                    break;
                case R.id.settings_button_backup:
                    Bundle type_backup = new Bundle();
                    type_backup.putInt(KEY_BACKUP_TYPE, FileBackupThread.TYPE_BACKUP);
                    getFragmentListener().onFragmentEvent(MyBookshelfEvent.START_BACKUP, type_backup);
                    break;
                case R.id.settings_button_restore:
                    Bundle type_restore = new Bundle();
                    type_restore.putInt(KEY_BACKUP_TYPE, FileBackupThread.TYPE_RESTORE);
                    getFragmentListener().onFragmentEvent(MyBookshelfEvent.START_BACKUP, type_restore);
                    break;
                case R.id.settings_button_login_dropbox:
                    getFragmentListener().onFragmentEvent(MyBookshelfEvent.START_DROPBOX_AUTH, null);
                    break;
                case R.id.settings_button_logout_dropbox:
                    showLogoutDialog();
                    break;
            }
        }
    };

}
