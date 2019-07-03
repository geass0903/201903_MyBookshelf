package jp.gr.java_conf.nuranimation.my_bookshelf.ui.base;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.model.net.base.BaseThread;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.prefs.MyBookshelfPreferences;
import jp.gr.java_conf.nuranimation.my_bookshelf.service.base.BaseService;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.MyBookshelfEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.ProgressDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.service.BookService;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.handler.PausedHandler;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.NormalDialogFragment;


public class BaseFragment extends Fragment implements NormalDialogFragment.OnNormalDialogListener, ProgressDialogFragment.OnProgressDialogListener{
    private static final String TAG = BaseFragment.class.getSimpleName();
    private static final boolean D =true;

    public static final int MESSAGE_PROGRESS_DIALOG_SHOW        = 1;
    public static final int MESSAGE_PROGRESS_DIALOG_UPDATE      = 2;
    public static final int MESSAGE_PROGRESS_DIALOG_DISMISS     = 3;
    public static final int MESSAGE_PERMISSION_DIALOG_SHOW      = 4;
    public static final int MESSAGE_PERMISSION_DIALOG_DISMISS   = 5;

    public static final int REQUEST_CODE_ASK_FOR_PERMISSIONS    =   1;
    public static final int REQUEST_CODE_DROPBOX_LOGOUT         =   2;
    public static final int REQUEST_CODE_REGISTER_BOOK          =   3;
    public static final int REQUEST_CODE_UNREGISTER_BOOK        =   4;

    private static final String KEY_SAVED_REQUEST_PERMISSIONS   = "KEY_SAVED_REQUEST_PERMISSIONS";
    private static final String KEY_IS_SHOW_PROGRESS_DIALOG     = "KEY_IS_SHOW_PROGRESS_DIALOG";
    private static final String KEY_BUNDLE_PROGRESS_DIALOG      = "KEY_BUNDLE_PROGRESS_DIALOG";

    protected static final String[] USE_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private DialogHandler handler = new DialogHandler(this);
    //    private PausedHandler handler = new PausedHandler(this);
    private FragmentListener mFragmentListener = null;
    private boolean isShowingProgressDialog;
    private Bundle mBundleProgressDialog;
    private String[] mRequestPermissions;
    private ProgressDialogFragment mProgressDialogFragment;
    private boolean isClickEnabled = true;

    private MyBookshelfPreferences mPreferences;

    @Override
    public void onProgressDialogCancelled(int requestCode, Bundle params) {

    }


    public interface FragmentListener {
        void onFragmentEvent(MyBookshelfEvent event, Bundle bundle);
    }

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPreferences = new MyBookshelfPreferences(context.getApplicationContext());
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(context.getApplicationContext());
        mReceiver = new LocalReceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(BaseService.FILTER_ACTION_UPDATE_SERVICE_STATE);
        mIntentFilter.addAction(BaseThread.FILTER_ACTION_UPDATE_PROGRESS);
        if (context instanceof FragmentListener) {
            mFragmentListener = (FragmentListener) context;
        } else {
            throw new UnsupportedOperationException("Listener is not Implementation.");
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
 //       if (!isShowingRequestPermission) {
 //           if (isShowingPermissionDialog) {
 //               showPermissionDialog();
 //           } else {
                if (!mPreferences.isCheckedPermissions()) {
                    mPreferences.setCheckedPermissions(true);
                    if (!isAllowedAllPermissions(USE_PERMISSIONS)) {
                        if (D) Log.d(TAG, "requestPermissions");
                        requestPermissions(USE_PERMISSIONS);
                    } else {
                        if (D) Log.d(TAG, "AllowedAllPermissions");
                    }
                }
 //           }
 //       }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.pause();
        mLocalBroadcastManager.unregisterReceiver(mReceiver);
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
    }




    public void setProgress(int state) {
        String title = null;
        switch (state) {
            case BookService.STATE_NONE:
            case BookService.STATE_DROPBOX_LOGIN:
                break;
            case BookService.STATE_SEARCH_BOOKS_SEARCH_INCOMPLETE:
            case BookService.STATE_SEARCH_BOOKS_SEARCH_COMPLETE:
                title = getString(R.string.progress_title_search_books);
                break;
            case BookService.STATE_NEW_BOOKS_RELOAD_INCOMPLETE:
            case BookService.STATE_NEW_BOOKS_RELOAD_COMPLETE:
                title = getString(R.string.progress_title_reload_new_books);
                break;
            case BookService.STATE_EXPORT_INCOMPLETE:
            case BookService.STATE_EXPORT_COMPLETE:
                title = getString(R.string.progress_title_export);
                break;
            case BookService.STATE_IMPORT_INCOMPLETE:
            case BookService.STATE_IMPORT_COMPLETE:
                title = getString(R.string.progress_title_import);
                break;
            case BookService.STATE_BACKUP_INCOMPLETE:
            case BookService.STATE_BACKUP_COMPLETE:
                title = getString(R.string.progress_title_backup);
                break;
            case BookService.STATE_RESTORE_INCOMPLETE:
            case BookService.STATE_RESTORE_COMPLETE:
                title = getString(R.string.progress_title_restore);
                break;
        }
        if (!TextUtils.isEmpty(title)) {
            isShowingProgressDialog = true;
            mBundleProgressDialog = new Bundle();
            mBundleProgressDialog.putString(ProgressDialogFragment.title, title);
            mBundleProgressDialog.putString(ProgressDialogFragment.message, "");
        }
    }

    public void showProgressDialog(){
        if(mBundleProgressDialog != null) {
            Message msg = handler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_SHOW);
            msg.setData(mBundleProgressDialog);
            handler.sendMessage(msg);

//            handler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_SHOW, msg).sendToTarget();

        }
    }

    public void showPermissionDialog(){

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
        String message = getString(R.string.permission_dialog_message) + "\n"
                + getString(R.string.permission_dialog_message1) + "\n"
                + getString(R.string.permission_dialog_message2);

        Bundle mBundlePermissionDialog = new Bundle();
        mBundlePermissionDialog.putString(NormalDialogFragment.KEY_TITLE, getString(R.string.permission_dialog_title));
        mBundlePermissionDialog.putString(NormalDialogFragment.KEY_MESSAGE, message);
        mBundlePermissionDialog.putString(NormalDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
        mBundlePermissionDialog.putString(NormalDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
        mBundlePermissionDialog.putBoolean(NormalDialogFragment.KEY_CANCELABLE, true);
        mBundlePermissionDialog.putInt(NormalDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_ASK_FOR_PERMISSIONS);
        Message msg = handler.obtainMessage(BaseFragment.MESSAGE_PERMISSION_DIALOG_SHOW);
        msg.setData(mBundlePermissionDialog);
        handler.sendMessage(msg);

        showPermissionDialog();
    }

    protected void onNextRequestPermissions(final String[] requestPermissions) {
        // パーミッション要求ダイアログを表示
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

    }

    protected void onDenyPermission(final String permission) {
        if(D) Log.d(TAG, "Denied permission = " + permission);

    }

    protected void onAllowAllPermissions(){
        Toast.makeText(getContext(), R.string.toast_success_get_permissions, Toast.LENGTH_SHORT).show();
    }

    protected void onDenyPermissions(){
        Toast.makeText(getContext(), R.string.toast_failed_get_permissions, Toast.LENGTH_SHORT).show();
    }



    protected void onRequestPermissionsFinally() {
        // 保持していたパーミッションを破棄
        setRequestPermissions(null);

        if (isAllowedAllPermissions(USE_PERMISSIONS)) {
            onAllowAllPermissions();
        }else{
            onDenyPermissions();
        }

        if (D) Log.d(TAG, "onRequestPermissionsFinally");
    }

    private String[] toStringArray(final List<String> stringList) {
        return stringList.toArray(new String[0]);
    }

    @Override
    public void onNormalDialogSucceeded(int requestCode, int resultCode, Bundle params) {
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
    public void onNormalDialogCancelled(int requestCode, Bundle params) {
        if(requestCode == REQUEST_CODE_ASK_FOR_PERMISSIONS) {
            getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PERMISSION_DIALOG_DISMISS).sendToTarget();
            onSkipRequestPermissions(getRequestPermissions());
        }
    }

    protected FragmentListener getFragmentListener(){
        return mFragmentListener;
    }


    protected DialogHandler getPausedHandler(){
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

    private Runnable enableClick = new Runnable() {
        @Override
        public void run() {
            if(D) Log.d(TAG,"click enable");
            isClickEnabled = true;
        }
    };





    protected static class DialogHandler extends PausedHandler {
        private final WeakReference<BaseFragment> mWeakReference;


        DialogHandler(BaseFragment fragment){
            super();
            mWeakReference = new WeakReference<>(fragment);
        }


        @Override
        protected void processMessage(Message msg) {
            BaseFragment fragment = mWeakReference.get();
            if (fragment != null) {
                switch (msg.what) {
                    case MESSAGE_PROGRESS_DIALOG_SHOW:
                        if (D) Log.d(TAG, "MESSAGE_PROGRESS_SHOW");
                        Bundle bundle_progress = msg.getData();
                        ProgressDialogFragment.showProgressDialog(fragment, bundle_progress);

//                        if (fragment.getActivity() != null && bundle_progress != null) {
 //                           if (fragment.mProgressDialogFragment == null) {
 //                               FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
  //                              fragment.mProgressDialogFragment = ProgressDialogFragment.newInstance(bundle_progress);
  //                              fragment.mProgressDialogFragment.show(manager, ProgressDialogFragment.TAG);
  //                          }
  //                      }
                        break;
                    case MESSAGE_PROGRESS_DIALOG_UPDATE:
                        if (fragment.getActivity() != null) {
                            FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
                            Fragment findFragment = manager.findFragmentByTag(ProgressDialogFragment.TAG);
                            if(findFragment instanceof ProgressDialogFragment){
                                Bundle bundle = (Bundle) msg.obj;
                                String message = bundle.getString(ProgressDialogFragment.KEY_MESSAGE);
                                String progress = bundle.getString(ProgressDialogFragment.KEY_PROGRESS);
                                ((ProgressDialogFragment) findFragment).setDialogProgress(message, progress);
                            }

                        }

//                        if (fragment.mProgressDialogFragment != null) {
//                            if (msg.obj instanceof Bundle){
//                                Bundle bundle = (Bundle) msg.obj;
//                                String message = bundle.getString(ProgressDialogFragment.message);
//                                String progress = bundle.getString(ProgressDialogFragment.progress);
//                                fragment.mProgressDialogFragment.setDialogProgress(message, progress);
//                            }
//                        }
                        break;
                    case MESSAGE_PROGRESS_DIALOG_DISMISS:
                        if (D) Log.d(TAG, "MESSAGE_PROGRESS_DIALOG_DISMISS");
                        ProgressDialogFragment.dismissProgressDialog(fragment);
//                        fragment.isShowingProgressDialog = false;
//                        if (fragment.mProgressDialogFragment != null) {
//                            fragment.mProgressDialogFragment.dismiss();
//                            fragment.mProgressDialogFragment = null;
//                        }
                        break;
                    case MESSAGE_PERMISSION_DIALOG_SHOW:
                        if (D) Log.d(TAG, "MESSAGE_PERMISSION_SHOW");
                        Bundle bundle_permission = msg.getData();
                        NormalDialogFragment.showNormalDialog(fragment, bundle_permission);
                        break;
                }
            }

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
