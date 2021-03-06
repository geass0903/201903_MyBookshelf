package jp.gr.java_conf.nuranimation.my_bookshelf.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import jp.gr.java_conf.nuranimation.my_bookshelf.MainActivity;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.background.BookService;

/**
 * Created by Kamada on 2019/03/11.
 */



public class BaseFragment extends Fragment implements BaseDialogFragment.OnBaseDialogListener{
    private static final String TAG = BaseFragment.class.getSimpleName();
    private static final boolean D = true;

    public static final int MESSAGE_PROGRESS_DIALOG_SHOW        = 1;
    public static final int MESSAGE_PROGRESS_DIALOG_UPDATE      = 2;
    public static final int MESSAGE_PROGRESS_DIALOG_DISMISS     = 3;
    public static final int MESSAGE_PERMISSION_DIALOG_SHOW      = 4;
    public static final int MESSAGE_PERMISSION_DIALOG_DISMISS   = 5;

    public static final int REQUEST_CODE_ASK_FOR_PERMISSIONS    =   1;
    public static final int REQUEST_CODE_DROPBOX_LOGOUT         =   2;
    public static final int REQUEST_CODE_REGISTER_BOOK          =   3;
    public static final int REQUEST_CODE_UNREGISTER_BOOK        =   4;

    public static final String FILTER_ACTION_UPDATE_SERVICE_STATE = "FILTER_ACTION_UPDATE_SERVICE_STATE";
    public static final String KEY_UPDATE_SERVICE_STATE = "KEY_UPDATE_SERVICE_STATE";
    public static final String FILTER_ACTION_UPDATE_PROGRESS = "FILTER_ACTION_UPDATE_PROGRESS";
    public static final String KEY_PROGRESS_TYPE = "KEY_PROGRESS_TYPE";
    public static final String KEY_PROGRESS = "KEY_PROGRESS";

    private static final String KEY_SAVED_REQUEST_PERMISSIONS   = "KEY_SAVED_REQUEST_PERMISSIONS";
    private static final String KEY_IS_SHOW_PROGRESS_DIALOG     = "KEY_IS_SHOW_PROGRESS_DIALOG";
    private static final String KEY_BUNDLE_PROGRESS_DIALOG      = "KEY_BUNDLE_PROGRESS_DIALOG";
    private static final String KEY_IS_SHOW_PERMISSION_DIALOG   = "KEY_IS_SHOW_PERMISSION_DIALOG";
    private static final String KEY_BUNDLE_PERMISSION_DIALOG    = "KEY_BUNDLE_PERMISSION_DIALOG";
    private static final String KEY_IS_SHOW_REQUEST_PERMISSION  = "KEY_IS_SHOW_REQUEST_PERMISSION";


    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private PausedHandler handler = new PausedHandler(this);
    private AppCompatActivity mActivity = null;
    private FragmentListener mFragmentListener = null;
    private boolean isShowingProgressDialog;
    private boolean isShowingPermissionDialog;
    private boolean isShowingRequestPermission;
    private Bundle mBundleProgressDialog;
    private Bundle mBundlePermissionDialog;
    private String[] mRequestPermissions;
    private BaseProgressDialogFragment mProgressDialogFragment;
    private BaseDialogFragment mPermissionDialogFragment;
    private boolean isClickEnabled = true;
    private MyBookshelfApplicationData mApplicationData;

    @SuppressWarnings("unused")
    public interface FragmentListener {
        void onFragmentEvent(MyBookshelfEvent event, Bundle bundle);
    }

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mApplicationData = (MyBookshelfApplicationData) context.getApplicationContext();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context.getApplicationContext());
        mReceiver = new LocalReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(FILTER_ACTION_UPDATE_SERVICE_STATE);
        mIntentFilter.addAction(FILTER_ACTION_UPDATE_PROGRESS);

        if (context instanceof FragmentListener) {
            mFragmentListener = (FragmentListener) context;
        } else {
            throw new UnsupportedOperationException("Listener is not Implementation.");
        }

        if (context instanceof AppCompatActivity) {
            mActivity = (MainActivity) context;
        } else {
            throw new UnsupportedOperationException("Activity is not Implementation.");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState != null){
            // 保存していたパーミッションを再設定
            setRequestPermissions(savedInstanceState.getStringArray(KEY_SAVED_REQUEST_PERMISSIONS));
            isShowingProgressDialog = savedInstanceState.getBoolean(KEY_IS_SHOW_PROGRESS_DIALOG, false);
            mBundleProgressDialog = savedInstanceState.getBundle(KEY_BUNDLE_PROGRESS_DIALOG);
            isShowingPermissionDialog = savedInstanceState.getBoolean(KEY_IS_SHOW_PERMISSION_DIALOG, false);
            mBundlePermissionDialog = savedInstanceState.getBundle(KEY_BUNDLE_PERMISSION_DIALOG);
            isShowingRequestPermission = savedInstanceState.getBoolean(KEY_IS_SHOW_REQUEST_PERMISSION, false);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        handler.resume();
        mLocalBroadcastManager.registerReceiver(mReceiver, mIntentFilter);
        if (isShowingProgressDialog) {
            showProgressDialog();
        }
        if (!isShowingRequestPermission) {
            if (isShowingPermissionDialog) {
                showPermissionDialog();
            } else {
                if (!mApplicationData.isCheckedPermissions()) {
                    mApplicationData.setCheckedPermissions(true);
                    if (!isAllowedAllPermissions(mApplicationData.getUse_Permissions())) {
                        if (D) Log.d(TAG, "requestPermissions");
                        requestPermissions(mApplicationData.getUse_Permissions());
                    } else {
                        if (D) Log.d(TAG, "AllowedAllPermissions");
                    }
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.pause();
        mLocalBroadcastManager.unregisterReceiver(mReceiver);
        if(mPermissionDialogFragment != null){
            mPermissionDialogFragment.dismiss();
            mPermissionDialogFragment = null;
        }
        if(mProgressDialogFragment != null){
            mProgressDialogFragment.dismiss();
            mProgressDialogFragment = null;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray(KEY_SAVED_REQUEST_PERMISSIONS, mRequestPermissions);
        outState.putBoolean(KEY_IS_SHOW_PROGRESS_DIALOG, isShowingProgressDialog);
        outState.putBundle(KEY_BUNDLE_PROGRESS_DIALOG, mBundleProgressDialog);
        outState.putBoolean(KEY_IS_SHOW_PERMISSION_DIALOG, isShowingPermissionDialog);
        outState.putBundle(KEY_BUNDLE_PERMISSION_DIALOG, mBundlePermissionDialog);
        outState.putBoolean(KEY_IS_SHOW_REQUEST_PERMISSION, isShowingRequestPermission);
    }




    public void setProgress(int state) {
        String title = null;
        switch (state) {
            case BookService.STATE_NONE:
            case BookService.STATE_DROPBOX_LOGIN:
                break;
            case BookService.STATE_SEARCH_BOOKS_SEARCH_INCOMPLETE:
            case BookService.STATE_SEARCH_BOOKS_SEARCH_COMPLETE:
                title = getString(R.string.ProgressTitle_Search);
                break;
            case BookService.STATE_NEW_BOOKS_RELOAD_INCOMPLETE:
            case BookService.STATE_NEW_BOOKS_RELOAD_COMPLETE:
                title = getString(R.string.ProgressTitle_Reload);
                break;
            case BookService.STATE_EXPORT_INCOMPLETE:
            case BookService.STATE_EXPORT_COMPLETE:
                title = getString(R.string.ProgressTitle_Export);
                break;
            case BookService.STATE_IMPORT_INCOMPLETE:
            case BookService.STATE_IMPORT_COMPLETE:
                title = getString(R.string.ProgressTitle_Import);
                break;
            case BookService.STATE_BACKUP_INCOMPLETE:
            case BookService.STATE_BACKUP_COMPLETE:
                title = getString(R.string.ProgressTitle_Backup);
                break;
            case BookService.STATE_RESTORE_INCOMPLETE:
            case BookService.STATE_RESTORE_COMPLETE:
                title = getString(R.string.ProgressTitle_Restore);
                break;
        }
        if (!TextUtils.isEmpty(title)) {
            isShowingProgressDialog = true;
            mBundleProgressDialog = new BundleBuilder()
                    .put(BaseProgressDialogFragment.title, title)
                    .put(BaseProgressDialogFragment.message, "")
                    .build();
        }
    }

    public void showProgressDialog(){
        if(mBundleProgressDialog != null) {
            Message msg = handler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_SHOW);
            msg.setData(mBundleProgressDialog);
            handler.sendMessage(msg);
        }
    }

    public void showPermissionDialog(){
        Message msg = handler.obtainMessage(BaseFragment.MESSAGE_PERMISSION_DIALOG_SHOW);
        msg.setData(mBundlePermissionDialog);
        handler.sendMessage(msg);
    }


    private void setRequestPermissions(String[] permissions) {
        mRequestPermissions = permissions;
    }

    private String[] getRequestPermissions() {
        return mRequestPermissions;
    }


    protected boolean isAllowedAllPermissions(final String... permissions) {
        String[] denyPermissions = getDenyPermissions(permissions);
        return (denyPermissions == null);
    }

    protected final void requestPermissions(final String... permissions) {
        // 許可されていないパーミッションを取得
        String[] denyPermissions = getDenyPermissions(permissions);
        if (denyPermissions != null) {
            // 説明画面を表示すべきパーミッションを取得
            String[] shouldShowRationalePermissions = getShouldShowRationalePermissions(
                    denyPermissions);
            if (shouldShowRationalePermissions == null) {
                // 要求するパーミッションを設定
                setRequestPermissions(denyPermissions);

                // パーミッションの要求ダイアログを表示
                isShowingRequestPermission = true;
                requestPermissions(getRequestPermissions(), REQUEST_CODE_ASK_FOR_PERMISSIONS);
            } else {
                // 要求するパーミッションを設定
                setRequestPermissions(shouldShowRationalePermissions);

                // 説明画面を表示
                showRationaleFragment();
            }
        }
    }

    protected final String[] getDenyPermissions(final String... permissions) {
        // パーミッションが許可されているか
        final List<String> denyPermissionList = new ArrayList<>();
        for (String permission : permissions) {
            if(getContext() != null) {
                if (permission != null && ContextCompat.checkSelfPermission(getContext(), permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    denyPermissionList.add(permission);
                }
            }
        }
        // すべて許可されている場合は、nullを返却
        if (denyPermissionList.size() == 0) {
            return null;
        }
        return toStringArray(denyPermissionList);
    }

    protected final String[] getShouldShowRationalePermissions(final String... permissions) {
        // パーミッションに対する説明画面が必要か
        final List<String> shouldRequestPermissionList = new ArrayList<>();
        for (String denyPermission : permissions) {
            if (denyPermission != null && shouldShowRequestPermissionRationale(denyPermission)) {
                shouldRequestPermissionList.add(denyPermission);
            }
        }

        // 説明が必要なパーミッションがない場合は、nullを返却
        if (shouldRequestPermissionList.size() == 0) {
            return null;
        }

        return toStringArray(shouldRequestPermissionList);
    }

    private void showRationaleFragment() {
        String message = getString(R.string.Permission_Message) + "\n"
                + getString(R.string.Permission_Message1) + "\n"
                + getString(R.string.Permission_Message2) + "\n"
                + getString(R.string.Permission_Message3);
        isShowingPermissionDialog = true;
        mBundlePermissionDialog = new BundleBuilder()
                .put(BaseDialogFragment.KEY_TITLE, getString(R.string.Permission_Title))
                .put(BaseDialogFragment.KEY_MESSAGE, message)
                .put(BaseDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.DialogButton_Label_Positive))
                .put(BaseDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.DialogButton_Label_Negative))
                .put(BaseDialogFragment.KEY_CANCELABLE, true)
                .put(BaseDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_ASK_FOR_PERMISSIONS)
                .build();
        showPermissionDialog();
    }

    protected void onNextRequestPermissions(final String[] requestPermissions) {
        // パーミッション要求ダイアログを表示
        isShowingRequestPermission = true;
        requestPermissions(requestPermissions, REQUEST_CODE_ASK_FOR_PERMISSIONS);
    }

    protected void onSkipRequestPermissions(final String[] requestPermissions) {
        for(String permission : requestPermissions){
            if(D) Log.d(TAG, "Skip request permission : " + permission);
        }
        // 後処理
        onRequestPermissionsFinally();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode != REQUEST_CODE_ASK_FOR_PERMISSIONS) {
            return;
        }
        // 許可されたかチェック
        for (int i = 0; i < permissions.length; i++) {
            final String permission = permissions[i];
            final int grantResult = grantResults[i];

            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                onAllowPermission(permission);
            } else {
                onDenyPermission(permission);
            }
        }
        // 後処理
        onRequestPermissionsFinally();
    }


    protected void onAllowPermission(final String permission) {
        if(D) Log.d(TAG, "Allowed permission = " + permission);
        Toast.makeText(getContext(), R.string.Toast_Success_Get_Permissions, Toast.LENGTH_SHORT).show();
    }

    protected void onDenyPermission(final String permission) {
        if(D) Log.d(TAG, "Denied permission = " + permission);
        Toast.makeText(getContext(), R.string.Toast_Failed_Get_Permissions, Toast.LENGTH_SHORT).show();
    }

    protected void onRequestPermissionsFinally() {
        // 保持していたパーミッションを破棄
        setRequestPermissions(null);
        isShowingRequestPermission = false;
        mApplicationData.setCheckedPermissions(true);
        if (D) Log.d(TAG, "onRequestPermissionsFinally");
    }

    private String[] toStringArray(final List<String> stringList) {
        return stringList.toArray(new String[0]);
    }

    @Override
    public void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        if(requestCode == REQUEST_CODE_ASK_FOR_PERMISSIONS) {
            getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PERMISSION_DIALOG_DISMISS).sendToTarget();
            if(resultCode == DialogInterface.BUTTON_POSITIVE) {
                onNextRequestPermissions(getRequestPermissions());
            }else{
                onSkipRequestPermissions(getRequestPermissions());
            }
        }
    }

    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {
        if(requestCode == REQUEST_CODE_ASK_FOR_PERMISSIONS) {
            getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PERMISSION_DIALOG_DISMISS).sendToTarget();
            onSkipRequestPermissions(getRequestPermissions());
        }
    }

    @SuppressWarnings({"unused"})
    protected AppCompatActivity getAppCompatActivity(){
        return mActivity;
    }

    @SuppressWarnings({"unused"})
    protected FragmentListener getFragmentListener(){
        return mFragmentListener;
    }


    protected PausedHandler getPausedHandler(){
        return handler;
    }

    protected void setClickDisable(){
        isClickEnabled = false;
        handler.removeCallbacks(enableClick);
        handler.postDelayed(enableClick,500);
    }

    protected boolean isClickable(){
        return isClickEnabled;
    }

    Runnable enableClick = new Runnable() {
        @Override
        public void run() {
            if(D) Log.d(TAG,"click enable");
            isClickEnabled = true;
        }
    };



    public static class PausedHandler extends Handler {
        private final WeakReference<BaseFragment> mWeakReference;

        private Queue<Message> mQueue = new LinkedList<>();
        private boolean isPaused;

        PausedHandler(BaseFragment fragment) {
            mWeakReference = new WeakReference<>(fragment);
        }

        void resume() {
            isPaused = false;
            while(!mQueue.isEmpty()){
                Message msg = mQueue.poll();
                if(msg != null){
                    dispatchMessage(msg);
                }
            }
        }

        void pause() {
            isPaused = true;
        }

        private void processMessage(Message msg) {
            BaseFragment fragment = mWeakReference.get();
            if (fragment != null) {
                switch (msg.what) {
                    case MESSAGE_PROGRESS_DIALOG_SHOW:
                        if (D) Log.d(TAG, "MESSAGE_PROGRESS_SHOW");
                        Bundle bundle_progress = msg.getData();
                        if (fragment.getActivity() != null && bundle_progress != null) {
                            if (fragment.mProgressDialogFragment == null) {
                                FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
                                fragment.mProgressDialogFragment = BaseProgressDialogFragment.newInstance(bundle_progress);
                                fragment.mProgressDialogFragment.show(manager, BaseProgressDialogFragment.TAG);
                            }
                        }
                        break;
                    case MESSAGE_PROGRESS_DIALOG_UPDATE:
                        if (fragment.mProgressDialogFragment != null) {
                            if (msg.obj instanceof Bundle){
                                Bundle bundle = (Bundle) msg.obj;
                                String message = bundle.getString(BaseProgressDialogFragment.message);
                                String progress = bundle.getString(BaseProgressDialogFragment.progress);
                                fragment.mProgressDialogFragment.setDialogProgress(message, progress);
                            }
                        }
                        break;
                    case MESSAGE_PROGRESS_DIALOG_DISMISS:
                        if (D) Log.d(TAG, "MESSAGE_PROGRESS_DIALOG_DISMISS");
                        fragment.isShowingProgressDialog = false;
                        if (fragment.mProgressDialogFragment != null) {
                            fragment.mProgressDialogFragment.dismiss();
                            fragment.mProgressDialogFragment = null;
                        }
                        break;
                    case MESSAGE_PERMISSION_DIALOG_SHOW:
                        if (D) Log.d(TAG, "MESSAGE_PERMISSION_SHOW");
                        Bundle bundle_permission = msg.getData();
                        if (fragment.getActivity() != null && bundle_permission != null) {
                            if (fragment.mPermissionDialogFragment == null) {
                                FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
                                fragment.mPermissionDialogFragment = BaseDialogFragment.newInstance(fragment, bundle_permission);
                                fragment.mPermissionDialogFragment.show(manager, BaseDialogFragment.TAG);
                            }
                        }
                        break;
                    case MESSAGE_PERMISSION_DIALOG_DISMISS:
                        if (D) Log.d(TAG, "MESSAGE_PERMISSION_DIALOG_DISMISS");
                        fragment.isShowingPermissionDialog = false;
                        if (fragment.mPermissionDialogFragment != null) {
                            fragment.mPermissionDialogFragment.dismiss();
                            fragment.mPermissionDialogFragment = null;
                            fragment.mBundleProgressDialog = null;
                        }
                        break;
                }
            }
        }

        @Override
        public void dispatchMessage(Message msg){
            if(isPaused){
                if(D) Log.d(TAG,"isPaused offer msg");
                final Message copied = Message.obtain(msg);
                mQueue.offer(copied);
            }else{
                super.dispatchMessage(msg);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(D) Log.d(TAG,"handleMessage");
            processMessage(msg);
        }
    }


    public void onReceiveBroadcast(Context context, Intent intent){

    }



    private class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onReceiveBroadcast(context, intent);
        }
    }


}
