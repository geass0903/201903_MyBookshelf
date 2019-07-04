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

import com.dropbox.core.android.Auth;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.BooksOrder;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.Result;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.thread.base.BaseThread;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.thread.FileBackupThread;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.prefs.MyBookshelfPreferences;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.service.BookService;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.OrderSpinnerArrayAdapter;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.ProgressDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.SpinnerItem;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.MainActivity;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.NormalDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.permission.PermissionsFragment;


public class SettingsFragment extends BaseFragment implements View.OnClickListener, NormalDialogFragment.OnNormalDialogListener, ProgressDialogFragment.OnProgressDialogListener {
    public static final String TAG = SettingsFragment.class.getSimpleName();
    private static final boolean D = true;

    private static final String DROP_BOX_KEY = "fh2si4dchz272b1";

    public static final String KEY_SERVICE_STATE = "SettingsFragment.KEY_SERVICE_STATE";
    private static final String KEY_CURRENT_STATE = "KEY_CURRENT_STATE";
    private static final String KEY_IS_ALLOWED_PERMISSIONS = "KEY_IS_ALLOWED_PERMISSIONS";
    private static final String KEY_IS_LOGGED_IN = "KEY_IS_LOGGED_IN";

    private MyBookshelfPreferences mPreferences;

    private Button mButtonExport;
    private Button mButtonImport;
    private Button mButtonLogin;
    private Button mButtonLogout;
    private Button mButtonBackup;
    private Button mButtonRestore;

    private boolean isAllowedPermissions;
    private boolean isLogged_in;
    private int mSettingsState = BookService.STATE_NONE;

    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPreferences = new MyBookshelfPreferences(context.getApplicationContext());
        mContext = context;

        isAllowedPermissions = PermissionsFragment.isAllowedAllPermissions(context,PermissionsFragment.USE_PERMISSIONS);
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
            if (getArguments() != null){
                mSettingsState = getArguments().getInt(KEY_SERVICE_STATE, BookService.STATE_NONE);
            }
 //           if (isAllowedAllPermissions(USE_PERMISSIONS)) {
 //               isAllowedPermissions = true;
 //           }
            if(mPreferences.containsKeyAccessToken()){
                isLogged_in = true;
            }
        }




        initSpinner(view);
        initButton(view, isAllowedPermissions, isLogged_in);
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
    public void onNormalDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        if (requestCode == ProgressDialogFragment.REQUEST_CODE_DROPBOX_LOGOUT) {
            switch (resultCode) {
                case DialogInterface.BUTTON_POSITIVE:
                    if (D) Log.d(TAG, "Log out button pressed");
                    mPreferences.deleteAccessToken();
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
    public void onNormalDialogCancelled(int requestCode, Bundle params) {

    }

    @Override
    public void onProgressDialogCancelled(int requestCode, Bundle params) {
        if (getActivity() instanceof MainActivity) {
            BookService service = ((MainActivity) getActivity()).getService();
            if (service != null) {
                if (D) Log.d(TAG, "cancelBackup");
                service.cancelBackup();

            }
        }
    }



    public void onAllowAllPermissions() {
        isAllowedPermissions = true;
        enableButton(true);
    }


    public void onDenyPermissions() {
        if(D) Log.d(TAG, "Denied permission");
        isAllowedPermissions = false;
        enableButton(false);
    }


/*
    @Override
    protected void onAllowAllPermissions() {
        super.onAllowAllPermissions();
        isAllowedPermissions = true;
        enableButton(true);
    }

    @Override
    protected void onDenyPermissions() {
        super.onDenyPermissions();
        if(D) Log.d(TEMP_TAG, "Denied permission");
        isAllowedPermissions = false;
        enableButton(false);
    }
*/

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
                case BookService.FILTER_ACTION_UPDATE_SERVICE_STATE:
                    int state = intent.getIntExtra(BookService.KEY_SERVICE_STATE, 0);
                    switch (state) {
                        case BookService.STATE_NONE:
                            if (D) Log.d(TAG, "STATE_NONE");
                            mSettingsState = BookService.STATE_NONE;
                            ProgressDialogFragment.dismissProgressDialog(this);
                            break;
                        case BookService.STATE_EXPORT_COMPLETE:
                        case BookService.STATE_IMPORT_COMPLETE:
                        case BookService.STATE_BACKUP_COMPLETE:
                        case BookService.STATE_RESTORE_COMPLETE:
                            checkSettingsState();
                            break;
                    }
                    break;
                case BaseThread.FILTER_ACTION_UPDATE_PROGRESS:
                    String progress = intent.getStringExtra(BaseThread.KEY_PROGRESS_VALUE_TEXT);
                    if(progress == null){
                        progress = "";
                    }
                    String message = intent.getStringExtra(BaseThread.KEY_PROGRESS_MESSAGE_TEXT);
                    if(message == null){
                        message = "";
                    }
                    Bundle bundle = new Bundle();
//                    bundle.putString(ProgressDialogFragment.message, message);
//                    bundle.putString(ProgressDialogFragment.progress, progress);
                    bundle.putString(ProgressDialogFragment.KEY_MESSAGE, message);
                    bundle.putString(ProgressDialogFragment.KEY_PROGRESS, progress);
                    ProgressDialogFragment.updateProgress(this,bundle);
//                    getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_UPDATE, bundle).sendToTarget();
                    break;
            }
        }
    }






    private void initSpinner(View view) {
        Spinner mShelfBooksOrderSpinner = view.findViewById(R.id.shelf_books_order_spinner);
        OrderSpinnerArrayAdapter mShelfBooksOrderAdapter = new OrderSpinnerArrayAdapter(getContext(), R.layout.item_order_spinner, getShelfBooksOrderSpinnerList());
        mShelfBooksOrderSpinner.setAdapter(mShelfBooksOrderAdapter);
        String code = mPreferences.getShelfBooksOrderCode();
        if(code == null){
            code = BooksOrder.SHELF_BOOKS_ORDER_CODE_REGISTERED_ASC;
        }
        mShelfBooksOrderSpinner.setSelection(mShelfBooksOrderAdapter.getPosition(code));
        mShelfBooksOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter,
                                       View v, int position, long id) {
                SpinnerItem item = (SpinnerItem) adapter.getItemAtPosition(position);
                if (D) Log.d(TAG, "selected: " + item.getLabel());
                mPreferences.setShelfBooksOrderCode(item.getCode());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });

        Spinner mSearchBooksOrderSpinner = view.findViewById(R.id.search_books_order_spinner);
        OrderSpinnerArrayAdapter mSearchBooksOrderAdapter = new OrderSpinnerArrayAdapter(getContext(), R.layout.item_order_spinner, getSearchBooksOrderSpinnerList());
        mSearchBooksOrderSpinner.setAdapter(mSearchBooksOrderAdapter);
        code = mPreferences.getSearchBooksOrderCode();
        if(code == null){
            code = BooksOrder.SEARCH_BOOKS_ORDER_CODE_SALES_DATE_DESC;
        }
        mSearchBooksOrderSpinner.setSelection(mSearchBooksOrderAdapter.getPosition(code));
        mSearchBooksOrderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter,
                                       View v, int position, long id) {
                SpinnerItem item = (SpinnerItem) adapter.getItemAtPosition(position);
                if (D) Log.d(TAG, "selected: " + item.getLabel());
                mPreferences.setSearchBooksOrderCode(item.getCode());
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


    private List<SpinnerItem> getShelfBooksOrderSpinnerList(){
        List<SpinnerItem> list = new ArrayList<>();
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_TITLE_ASC,       getString(R.string.label_shelf_books_order_title_ascending)));
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_TITLE_DESC,      getString(R.string.label_shelf_books_order_title_descending)));
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_AUTHOR_ASC,      getString(R.string.label_shelf_books_order_author_ascending)));
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_AUTHOR_DESC,     getString(R.string.label_shelf_books_order_author_descending)));
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_SALES_DATE_ASC,  getString(R.string.label_shelf_books_order_sales_date_ascending)));
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_SALES_DATE_DESC, getString(R.string.label_shelf_books_order_sales_date_descending)));
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_REGISTERED_ASC,  getString(R.string.label_shelf_books_order_registered_ascending)));
        list.add(new SpinnerItem(BooksOrder.SHELF_BOOKS_ORDER_CODE_REGISTERED_DESC, getString(R.string.label_shelf_books_order_registered_descending)));
        return list;
    }

    private List<SpinnerItem> getSearchBooksOrderSpinnerList(){
        List<SpinnerItem> list = new ArrayList<>();
        list.add(new SpinnerItem(BooksOrder.SEARCH_BOOKS_ORDER_CODE_SALES_DATE_ASC,  getString(R.string.label_search_books_order_sales_date_ascending)));
        list.add(new SpinnerItem(BooksOrder.SEARCH_BOOKS_ORDER_CODE_SALES_DATE_DESC, getString(R.string.label_search_books_order_sales_date_descending)));
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
//        if (isAllowedAllPermissions(USE_PERMISSIONS)) {
            if (getActivity() instanceof MainActivity) {
                BookService service = ((MainActivity) getActivity()).getService();
                String title = null;
                if (service != null) {
                    switch(type){
                        case FileBackupThread.TYPE_EXPORT:
                            mSettingsState = BookService.STATE_EXPORT_INCOMPLETE;
                            title = getString(R.string.progress_title_export);
                            break;
                        case FileBackupThread.TYPE_IMPORT:
                            mSettingsState = BookService.STATE_IMPORT_INCOMPLETE;
                            title = getString(R.string.progress_title_import);
                            break;
                        case FileBackupThread.TYPE_BACKUP:
                            mSettingsState = BookService.STATE_BACKUP_INCOMPLETE;
                            title = getString(R.string.progress_title_backup);
                            break;
                        case FileBackupThread.TYPE_RESTORE:
                            mSettingsState = BookService.STATE_RESTORE_INCOMPLETE;
                            title = getString(R.string.progress_title_restore);
                            break;
                        default:
                            break;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(ProgressDialogFragment.KEY_TITLE, title);
                    ProgressDialogFragment.showProgressDialog(this, bundle);
                    service.fileBackup(type);
                }
            }
 //       } else {
 //           requestPermissions(USE_PERMISSIONS);
 //       }
    }

    private void onClickLogin(){
        if (getActivity() instanceof MainActivity) {
            BookService service = ((MainActivity) getActivity()).getService();
            if (service != null) {
                mSettingsState = BookService.STATE_DROPBOX_LOGIN;
                service.setServiceState(BookService.STATE_DROPBOX_LOGIN);
                startAuthenticate();
            }
        }
    }

    private void onClickLogout(){
        Bundle bundle = new Bundle();
        bundle.putString(NormalDialogFragment.KEY_TITLE, getString(R.string.dialog_title_logout_dropbox));
        bundle.putString(NormalDialogFragment.KEY_MESSAGE, getString(R.string.dialog_message_logout_dropbox));
        bundle.putString(NormalDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
        bundle.putString(NormalDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
        bundle.putInt(NormalDialogFragment.KEY_REQUEST_CODE, ProgressDialogFragment.REQUEST_CODE_DROPBOX_LOGOUT);
        if (getActivity() != null) {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            NormalDialogFragment dialog = NormalDialogFragment.newInstance(this, bundle);
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
                        String token = getAccessToken();
                        if (token != null) {
                            // Log-in Success
                            if(D) Log.d(TAG,"Log-in Success");
                            mPreferences.setAccessToken(token);
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
                Result result = service.getResult();
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
        ProgressDialogFragment.dismissProgressDialog(this);
    }




    public void startAuthenticate(){
        Auth.startOAuth2Authentication(mContext,DROP_BOX_KEY);
    }

    public String getAccessToken(){
        return Auth.getOAuth2Token();
    }



}

