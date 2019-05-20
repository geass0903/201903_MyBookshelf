package jp.gr.java_conf.nuranimation.my_bookshelf.base;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import jp.gr.java_conf.nuranimation.my_bookshelf.event.FragmentEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.utils.MyBookshelfApplicationData;

/**
 * Created by Kamada on 2019/03/11.
 */



public class BaseFragment extends Fragment implements BaseDialogFragment.OnBaseDialogListener{
    private static final String TAG = BaseFragment.class.getSimpleName();
    private static final boolean D = true;

    public static final int MESSAGE_PROGRESS_SHOW       = 1;
    public static final int MESSAGE_PROGRESS_UPDATE     = 2;
    public static final int MESSAGE_PROGRESS_DISMISS    = 3;

    public static final int REQUEST_CODE_LOGOUT                = 100;
    public static final int REQUEST_CODE_REGISTER_BOOK         = 110;
    public static final int REQUEST_CODE_DELETE_BOOK           = 111;
    public static final int REQUEST_CODE_ASK_FOR_PERMISSIONS   = 999;

    private static final String KEY_SAVED_REQUEST_PERMISSIONS   = "KEY_SAVED_REQUEST_PERMISSIONS";
    private static final String KEY_IS_SHOW_PROGRESS = "KEY_IS_SHOW_PROGRESS";
    private static final String KEY_BUNDLE_PROGRESS_DIALOG      = "KEY_BUNDLE_PROGRESS_DIALOG";

    private PausedHandler handler = new PausedHandler(this);
    private AppCompatActivity mActivity = null;
    private FragmentListener mFragmentListener = null;
    private boolean isShowingProgress;
    private Bundle mBundleProgress;
    private String[] mRequestPermissions;
    private BaseProgressDialogFragment mProgressFragment;
    private boolean isClickEnabled = true;
    private MyBookshelfApplicationData mApplicationData;

    @SuppressWarnings("unused")
    public interface FragmentListener {
        void onFragmentEvent(FragmentEvent event);
    }

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mApplicationData = (MyBookshelfApplicationData) context.getApplicationContext();
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
            isShowingProgress = savedInstanceState.getBoolean(KEY_IS_SHOW_PROGRESS,false);
            mBundleProgress = savedInstanceState.getBundle(KEY_BUNDLE_PROGRESS_DIALOG);
            // 保存していたパーミッションを再設定
            setRequestPermissions(savedInstanceState.getStringArray(KEY_SAVED_REQUEST_PERMISSIONS));
        }
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.resume();

        if(!mApplicationData.isCheckedPermissions()) {
            mApplicationData.setCheckedPermissions(true);
            if (!isAllowedAllPermissions(mApplicationData.getUse_Permissions())) {
                if(D) Log.d(TAG,"requestPermissions");
                requestPermissions(mApplicationData.getUse_Permissions());
            }else{
                if(D) Log.d(TAG,"AllowedAllPermissions");
            }
        }
        if(isShowingProgress){
            showProgress();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.pause();
        if(mProgressFragment != null){
            mProgressFragment.dismiss();
            mProgressFragment = null;
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray(KEY_SAVED_REQUEST_PERMISSIONS, mRequestPermissions);
        outState.putBoolean(KEY_IS_SHOW_PROGRESS, isShowingProgress);
        outState.putBundle(KEY_BUNDLE_PROGRESS_DIALOG, mBundleProgress);
    }


    public void setProgressDialog(Bundle bundle){
        isShowingProgress = true;
        mBundleProgress = new Bundle(bundle);
        showProgress();
    }

    private void showProgress(){
        Message msg = handler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_SHOW);
        msg.setData(mBundleProgress);
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
        Bundle bundle = new Bundle();
        bundle.putString(BaseDialogFragment.KEY_TITLE, getString(R.string.Permission_Title));
        String message = getString(R.string.Permission_Message) + "\n"
                + getString(R.string.Permission_Message1) + "\n"
                + getString(R.string.Permission_Message2) + "\n"
                + getString(R.string.Permission_Message3);
        bundle.putString(BaseDialogFragment.KEY_MESSAGE,message);
        bundle.putString(BaseDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.Permission_PositiveLabel));
        bundle.putString(BaseDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.Permission_NegativeLabel));
        bundle.putBoolean(BaseDialogFragment.KEY_CANCELABLE,true);
        bundle.putInt(BaseDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_ASK_FOR_PERMISSIONS);
        BaseDialogFragment dialog = BaseDialogFragment.newInstance(this, bundle);
        if(getActivity() != null) {
            dialog.show(getActivity().getSupportFragmentManager(), BaseFragment.TAG);
        }
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
        Toast.makeText(getContext(), R.string.Toast_Success_Get_Permissions, Toast.LENGTH_SHORT).show();
    }

    protected void onDenyPermission(final String permission) {
        if(D) Log.d(TAG, "Denied permission = " + permission);
        Toast.makeText(getContext(), R.string.Toast_Failed_Get_Permissions, Toast.LENGTH_SHORT).show();
    }

    protected void onRequestPermissionsFinally() {
        // 保持していたパーミッションを破棄
        setRequestPermissions(null);
    }

    private String[] toStringArray(final List<String> stringList) {
        return stringList.toArray(new String[0]);
    }



    @Override
    public void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        if(requestCode == REQUEST_CODE_ASK_FOR_PERMISSIONS) {
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
                    case MESSAGE_PROGRESS_SHOW:
                        Bundle bundle_progress = msg.getData();
                        if (fragment.getActivity() != null && bundle_progress != null) {
                            FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
                            fragment.mProgressFragment = BaseProgressDialogFragment.newInstance(bundle_progress);
                            fragment.mProgressFragment.show(manager, BaseFragment.TAG);
                        }
                        break;
                    case MESSAGE_PROGRESS_UPDATE:
                        if (fragment.mProgressFragment != null) {
                            String progress = (String) msg.obj;
                            fragment.mProgressFragment.setProgressMessage(progress);
                        }
                        break;
                    case MESSAGE_PROGRESS_DISMISS:
                        fragment.isShowingProgress = false;
                        if(fragment.mProgressFragment != null){
                            fragment.mProgressFragment.dismiss();
                            fragment.mProgressFragment = null;
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

}
