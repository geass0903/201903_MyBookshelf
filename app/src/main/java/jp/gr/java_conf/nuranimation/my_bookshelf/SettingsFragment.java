package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class SettingsFragment extends BaseFragment implements BaseDialogFragment.OnBaseDialogListener{
    public static final String TAG = SettingsFragment.class.getSimpleName();
    private static final boolean D = true;
    private static final String ACCESS_TOKEN = "Key_Access_Token";
    private static final int REQUEST_CODE_LOG_OUT = 100;

    public static final int MESSAGE_PROGRESS = 1;

    private enum ButtonAction {
        NONE,
        LOG_IN,
        LOG_OUT,
        EXPORT_CSV,
        IMPORT_CSV,
        BACKUP_DROPBOX,
        RESTORE_DROPBOX,
    }
    private ButtonAction mCurrentAction;

    private Context mContext;
    private MyBookshelfApplicationData mData;
    private DropboxManager mDropboxManager;
    private FileManager mFileManager;

    private LinearLayout mLinearLayout_Progress;
    private TextView mTextView_Title;
    private TextView mTextView_Progress;


    private Button mButton_Login;
    private Button mButton_Logout;
    private Button mButton_Backup;
    private Button mButton_Restore;

    private ProgressHandler mHandler = new ProgressHandler(this);

    @Override
    public void onAttach (Context context){
        super.onAttach(context);
        mContext = context;
        mData = (MyBookshelfApplicationData) context.getApplicationContext();
        mDropboxManager = new DropboxManager(mContext);
        mFileManager = new FileManager(mContext);
        mFileManager.setHandler(mHandler);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mCurrentAction = ButtonAction.NONE;
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = view.findViewById(R.id.fragment_settings_toolbar);
        toolbar.setTitle(R.string.Navigation_Item_Settings);
        toolbar.setNavigationOnClickListener(toolbarClickListener);

        view.findViewById(R.id.settings_button_export).setOnClickListener(button_exportListener);
        view.findViewById(R.id.settings_button_import).setOnClickListener(button_importListener);

        mButton_Login = view.findViewById(R.id.settings_button_log_in_dropbox);
        mButton_Logout = view.findViewById(R.id.settings_button_log_out_dropbox);
        mButton_Backup = view.findViewById(R.id.settings_button_backup);
        mButton_Restore = view.findViewById(R.id.settings_button_restore);
        mButton_Login.setOnClickListener(button_loginListener);
        mButton_Logout.setOnClickListener(button_logoutListener);
        mButton_Backup.setOnClickListener(button_backupListener);
        mButton_Restore.setOnClickListener(button_restoreListener);

        mTextView_Title = view.findViewById(R.id.fragment_settings_progress_text_title);
        mTextView_Progress = view.findViewById(R.id.fragment_settings_progress_text_progress);
        mLinearLayout_Progress = view.findViewById(R.id.fragment_settings_progress_view);

        initSpinner_SortSetting_Bookshelf(view);
        initSpinner_SortSetting_SearchResult(view);

    }

    @Override
    public void onResume(){
        super.onResume();
        if (D) Log.i(TAG, "onResume");
        checkDropbox();
    }


    private void checkDropbox(){
        switch (mCurrentAction){
            case NONE:
                if(mData.containsKey(MyBookshelfPreferenceManager.Key_Access_Token)){
                    Login();
                }else{
                    Logout();
                }
                break;
            case LOG_IN:
                mCurrentAction = ButtonAction.NONE;
                try{
                    String token = mDropboxManager.getAccessToken();
                    if(token != null){
                        // Log-in Success
                        mData.putStringPreference(MyBookshelfPreferenceManager.Key_Access_Token,token);
                        Login();
                    }
                }catch (IllegalStateException e) {
                    // IllegalStateException
                }
                break;
            case LOG_OUT:
                mCurrentAction = ButtonAction.NONE;
                mData.removeKey(MyBookshelfPreferenceManager.Key_Access_Token);
                Logout();
                break;
            case BACKUP_DROPBOX:
                break;
            case RESTORE_DROPBOX:
                break;
        }
    }


    public void callback(ButtonAction action, boolean result) {
        mTextView_Progress.setText("");
        mLinearLayout_Progress.setVisibility(View.GONE);
        if(mFragmentListener != null) {
            mFragmentListener.onFragmentEvent(FragmentEvent.REMOVE_MASK);
        }
        if(result){
            switch (action) {
                case EXPORT_CSV:
                    Toast.makeText(mContext, R.string.Toast_Success_Export, Toast.LENGTH_SHORT).show();
                    break;
                case IMPORT_CSV:
                    Toast.makeText(mContext, R.string.Toast_Success_Import, Toast.LENGTH_SHORT).show();
                    break;
                case BACKUP_DROPBOX:
                    Toast.makeText(mContext, R.string.Toast_Success_Backup, Toast.LENGTH_SHORT).show();
                    break;
                case RESTORE_DROPBOX:
                    Toast.makeText(mContext, R.string.Toast_Success_Restore, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }else {
            Toast.makeText(mContext, R.string.Toast_Failed, Toast.LENGTH_SHORT).show();
        }

    }

    private void Login(){
        mButton_Login.setVisibility(View.GONE);
        mButton_Logout.setVisibility(View.VISIBLE);
        mButton_Backup.setVisibility(View.VISIBLE);
        mButton_Restore.setVisibility(View.VISIBLE);
    }
    private void Logout(){
        mButton_Login.setVisibility(View.VISIBLE);
        mButton_Logout.setVisibility(View.GONE);
        mButton_Backup.setVisibility(View.GONE);
        mButton_Restore.setVisibility(View.GONE);
    }

    public void AsyncTaskCSV(ButtonAction action){
        new AsyncCSV(this, action).execute();
    }


    View.OnClickListener toolbarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Do Nothing
        }
    };

    View.OnClickListener button_exportListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AsyncTaskCSV(ButtonAction.EXPORT_CSV);
        }
    };

    View.OnClickListener button_importListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AsyncTaskCSV(ButtonAction.IMPORT_CSV);
        }
    };

    View.OnClickListener button_backupListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mData.containsKey(MyBookshelfPreferenceManager.Key_Access_Token)){
                String token = mData.getStringPreference(MyBookshelfPreferenceManager.Key_Access_Token,null);
                mDropboxManager.setToken(token);
                AsyncTaskCSV(ButtonAction.BACKUP_DROPBOX);
            }else{
                callback(ButtonAction.BACKUP_DROPBOX,false);
            }
        }
    };

    View.OnClickListener button_restoreListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mData.containsKey(MyBookshelfPreferenceManager.Key_Access_Token)){
                String token = mData.getStringPreference(MyBookshelfPreferenceManager.Key_Access_Token,null);
                mDropboxManager.setToken(token);
                AsyncTaskCSV(ButtonAction.RESTORE_DROPBOX);
            }else{
                callback(ButtonAction.RESTORE_DROPBOX,false);
            }
        }
    };

    View.OnClickListener button_loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCurrentAction = ButtonAction.LOG_IN;
            mDropboxManager.startAuthenticate();
        }
    };

    View.OnClickListener button_logoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showLogoutDialog();
        }
    };

    private void showLogoutDialog(){
        Bundle bundle = new Bundle();
        bundle.putString(BaseDialogFragment.title,getString(R.string.Dialog_Label_Logout));
        bundle.putString(BaseDialogFragment.message,getString(R.string.Dialog_Message_Logout));
        bundle.putString(BaseDialogFragment.positiveLabel,getString(R.string.Dialog_Button_Positive));
        bundle.putString(BaseDialogFragment.negativeLabel,getString(R.string.Dialog_Button_Negative));
        bundle.putInt(BaseDialogFragment.request_code,REQUEST_CODE_LOG_OUT);
        if(getActivity() != null) {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            BaseDialogFragment dialog = BaseDialogFragment.newInstance(this,bundle);
            dialog.show(manager,SettingsFragment.TAG);
        }
    }

    @Override
    public void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        switch (resultCode){
            case DialogInterface.BUTTON_POSITIVE:
                if(D) Log.d(TAG,"Log out button pressed");
                mCurrentAction = ButtonAction.LOG_OUT;
                checkDropbox();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                if(D) Log.d(TAG,"Log out button cancel");
                break;
        }
    }

    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {

    }



    private static class AsyncCSV extends AsyncTask<Void, Void, Boolean>{
        private final WeakReference<SettingsFragment> mFragmentReference;
        private ButtonAction action;
        private int error;

        private AsyncCSV(SettingsFragment fragment, ButtonAction buttonAction){
            this.mFragmentReference = new WeakReference<>(fragment);
            this.action = buttonAction;
            this.error = ErrorStatus.No_Error;
        }

        @Override
        protected void onPreExecute(){
            SettingsFragment fragment = mFragmentReference.get();
            if(fragment.mFragmentListener != null){
                fragment.mFragmentListener.onFragmentEvent(FragmentEvent.DISP_MASK);
            }

            switch (action){
                case EXPORT_CSV:
                    fragment.mTextView_Title.setText(R.string.Progress_Export);
                    break;
                case IMPORT_CSV:
                    fragment.mTextView_Title.setText(R.string.Progress_Import);
                    break;
                case BACKUP_DROPBOX:
                    fragment.mTextView_Title.setText(R.string.Progress_Backup);
                    break;
                case RESTORE_DROPBOX:
                    fragment.mTextView_Title.setText(R.string.Progress_Restore);
                    break;
            }
            fragment.mLinearLayout_Progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            SettingsFragment fragment = mFragmentReference.get();

            switch (action) {
                case EXPORT_CSV:
                    error = fragment.mFileManager.export_csv();
                    break;
                case IMPORT_CSV:
                    error = fragment.mFileManager.import_csv();
                    break;
                case BACKUP_DROPBOX:
                    error= fragment.mFileManager.export_csv();
                    if(error == ErrorStatus.No_Error) {
                        error = fragment.mDropboxManager.backup();
                    }
                    break;
                case RESTORE_DROPBOX:
                    error = fragment.mDropboxManager.restore();
                    if(error == ErrorStatus.No_Error){
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
            mFragmentReference.get().callback(action,result);
        }
    }


    private static class ProgressHandler extends Handler {
        private final WeakReference<SettingsFragment> mFragment;

        ProgressHandler(SettingsFragment fragment){
            mFragment = new WeakReference<>(fragment);
        }
        @Override
        public void handleMessage(Message msg){
            SettingsFragment fragment = mFragment.get();
            if(fragment != null){
                switch (msg.what){
                    case MESSAGE_PROGRESS:
                        String progress = (String)msg.obj;
                        fragment.mTextView_Progress.setText(progress);
                        break;
                }

            }
        }

    }




    private void initSpinner_SortSetting_Bookshelf(View view) {
        Spinner spinner_SortSetting_Bookshelf = view.findViewById(R.id.SettingsFragment_Spinner_SortSetting_Bookshelf);
        SpinnerArrayAdapter arrayAdapter_SortSetting_Bookshelf = new SpinnerArrayAdapter(this.mContext, R.layout.item_spinner, getList_Spinner_Sort_Shelf());
        spinner_SortSetting_Bookshelf.setAdapter(arrayAdapter_SortSetting_Bookshelf);
        String code = mData.getStringPreference(MyBookshelfPreferenceManager.Key_SortSetting_Bookshelf, getString(R.string.Code_SortSetting_Registered_Ascending));
        spinner_SortSetting_Bookshelf.setSelection(arrayAdapter_SortSetting_Bookshelf.getPosition(code));
        spinner_SortSetting_Bookshelf.setOnItemSelectedListener(listener_SortSetting_Bookshelf);
    }


    private void initSpinner_SortSetting_SearchResult(View view) {
        Spinner spinner_SortSetting_SearchResult = view.findViewById(R.id.SettingsFragment_Spinner_SortSetting_SearchResult);
        SpinnerArrayAdapter arrayAdapter_SortSetting_SearchResult = new SpinnerArrayAdapter(this.mContext, R.layout.item_spinner, getList_Spinner_Sort_SearchResult());
        spinner_SortSetting_SearchResult.setAdapter(arrayAdapter_SortSetting_SearchResult);
        String code = mData.getStringPreference(MyBookshelfPreferenceManager.Key_SortSetting_SearchResult, getString(R.string.Code_SortSetting_SalesDate_Descending));
        spinner_SortSetting_SearchResult.setSelection(arrayAdapter_SortSetting_SearchResult.getPosition(code));
        spinner_SortSetting_SearchResult.setOnItemSelectedListener(listener_SortSetting_SearchResult);
    }


    private List<SpinnerItem> getList_Spinner_Sort_Shelf(){
        List<SpinnerItem> list = new ArrayList<>();
        Resources res = getResources();
        TypedArray array = res.obtainTypedArray(R.array.Spinner_Bookshelf_SortSetting);
        for (int i = 0; i < array.length(); ++i) {
            int id = array.getResourceId(i, -1);
            if (id > -1) {
                String[] item = res.getStringArray(id);
                list.add(new SpinnerItem(item[0],item[1]));
            }
        }
        array.recycle();
        return list;
    }

    private List<SpinnerItem> getList_Spinner_Sort_SearchResult(){
        List<SpinnerItem> list = new ArrayList<>();
        Resources res = getResources();
        TypedArray array = res.obtainTypedArray(R.array.Spinner_SearchResult_SortSetting);
        for (int i = 0; i < array.length(); ++i) {
            int id = array.getResourceId(i, -1);
            if (id > -1) {
                String[] item = res.getStringArray(id);
                list.add(new SpinnerItem(item[0],item[1]));
            }
        }
        array.recycle();
        return list;
    }


    AdapterView.OnItemSelectedListener listener_SortSetting_Bookshelf = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter,
                View v, int position, long id) {
            SpinnerItem item = (SpinnerItem) adapter.getItemAtPosition(position);
            if(D) Log.d(TAG,"selected: " + item.mLabel);
            mData.putStringPreference(MyBookshelfPreferenceManager.Key_SortSetting_Bookshelf,item.mCode);
            mData.updateList_MyBookshelf();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapter) {
        }
    };

    AdapterView.OnItemSelectedListener listener_SortSetting_SearchResult = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter,
                                   View v, int position, long id) {
            SpinnerItem item = (SpinnerItem) adapter.getItemAtPosition(position);
            if(D) Log.d(TAG,"selected: " + item.mLabel);
            mData.putStringPreference(MyBookshelfPreferenceManager.Key_SortSetting_SearchResult,item.mCode);
        }
        @Override
        public void onNothingSelected(AdapterView<?> adapter) {
        }
    };

}

