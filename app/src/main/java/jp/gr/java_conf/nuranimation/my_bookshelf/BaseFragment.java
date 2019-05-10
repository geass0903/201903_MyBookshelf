package jp.gr.java_conf.nuranimation.my_bookshelf;

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
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Kamada on 2019/03/11.
 */

public class BaseFragment extends Fragment implements BaseDialogFragment.OnBaseDialogListener{
    private static final String TAG = BaseFragment.class.getSimpleName();

    public static final int MESSAGE_Progress_Show       = 1;
    public static final int MESSAGE_Progress_Message    = 2;
    public static final int MESSAGE_Progress_Dismiss    = 3;

    static final int REQUEST_CODE_Logout = 100;
    static final int REQUEST_CODE_Delete_Book = 110;
    static final int REQUEST_CODE_Register_Book = 111;
    static final int REQUEST_CODE_Ask_for_Permissions = 999;
    private static final String KEY_Saved_RequestPermissions = "KEY_Saved_RequestPermissions";
    private static final String KEY_isShowingDialog = "KEY_isShowingDialog";
    private static final String KEY_bundleProgressDialog = "KEY_bundleProgressDialog";

    private String[] mRequestPermissions;
    boolean isShowingDialog;

    AppCompatActivity mActivity = null;
    FragmentListener mFragmentListener = null;

    BaseProgressDialogFragment progressDialogFragment;
    ProgressDialogHandler handler = new ProgressDialogHandler(this);
    Handler waitHandler = new Handler();
    boolean isClickEnabled = true;
    Bundle bundleProgressDialog;


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
            isShowingDialog = savedInstanceState.getBoolean(KEY_isShowingDialog,false);
            bundleProgressDialog = new BundleBuilder(savedInstanceState.getBundle(KEY_bundleProgressDialog)).build();
        }
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            // 保存していたパーミッションを再設定
            setRequestPermissions(savedInstanceState.getStringArray(KEY_Saved_RequestPermissions));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.resume();
        if(isShowingDialog){
            showProgress();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(progressDialogFragment != null){
            progressDialogFragment.dismiss();
            progressDialogFragment = null;
        }
        handler.pause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putStringArray(KEY_Saved_RequestPermissions, mRequestPermissions);
        outState.putBoolean(KEY_isShowingDialog,isShowingDialog);
        outState.putBundle(KEY_bundleProgressDialog,bundleProgressDialog);
    }


    public void setProgressDialog(Bundle bundle){
        isShowingDialog = true;
        bundleProgressDialog = new BundleBuilder(bundle).build();
        showProgress();
    }


    private void showProgress(){
        Message msg = handler.obtainMessage(BaseFragment.MESSAGE_Progress_Show);
        msg.setData(bundleProgressDialog);
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
                requestPermissions(getRequestPermissions(), REQUEST_CODE_Ask_for_Permissions);
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
        bundle.putString(BaseDialogFragment.title, getString(R.string.Permission_Title));
        bundle.putString(BaseDialogFragment.message,getString(R.string.Permission_Message));
        bundle.putString(BaseDialogFragment.positiveLabel, getString(R.string.Permission_PositiveLabel));
        bundle.putString(BaseDialogFragment.negativeLabel, getString(R.string.Permission_NegativeLabel));
        bundle.putBoolean(BaseDialogFragment.cancelable,true);
        bundle.putInt(BaseDialogFragment.request_code, REQUEST_CODE_Ask_for_Permissions);
        BaseDialogFragment dialog = BaseDialogFragment.newInstance(this, bundle);
        if(getActivity() != null) {
            dialog.show(getActivity().getSupportFragmentManager(), BaseFragment.TAG);
        }
    }

    @SuppressWarnings({"unused","SameParameterValue"})
    protected void onNextRequestPermissions(final String[] requestPermissions) {
        // パーミッション要求ダイアログを表示
        requestPermissions(requestPermissions, REQUEST_CODE_Ask_for_Permissions);
    }
    @SuppressWarnings({"unused","SameParameterValue"})
    protected void onSkipRequestPermissions(final String[] requestPermissions) {
        // 後処理
        onRequestPermissionsFinally();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CODE_Ask_for_Permissions) {
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

    @SuppressWarnings({"unused","SameParameterValue"})
    protected void onAllowPermission(final String permission) {

    }
    @SuppressWarnings({"unused","SameParameterValue"})
    protected void onDenyPermission(final String permission) {

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
        if(requestCode == REQUEST_CODE_Ask_for_Permissions) {
            if(resultCode == DialogInterface.BUTTON_POSITIVE) {
                onNextRequestPermissions(getRequestPermissions());
            }else{
                onSkipRequestPermissions(getRequestPermissions());
            }
        }
    }

    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {
        if(requestCode == REQUEST_CODE_Ask_for_Permissions) {
            onSkipRequestPermissions(getRequestPermissions());
        }
    }

    @SuppressWarnings({"SameParameterValue"})
    void setWait_ClickEnable(long wait){
        waitHandler.removeCallbacks(enableClick);
        waitHandler.postDelayed(enableClick,wait);
    }

    Runnable enableClick = new Runnable() {
        @Override
        public void run() {
            isClickEnabled = true;
        }
    };


    static class ProgressDialogHandler extends Handler {
        private final WeakReference<BaseFragment> mWeakReference;

        private Queue<Message> mQueue = new LinkedList<>();
//        private List<Message> messageList = new ArrayList<>();
        private boolean isPaused;

        ProgressDialogHandler(BaseFragment fragment) {
            mWeakReference = new WeakReference<>(fragment);
        }

        void resume() {
            isPaused = false;
            while(!mQueue.isEmpty()){
                Message msg = mQueue.poll();
                if(msg != null){
                    sendMessage(msg);
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
                    case MESSAGE_Progress_Show:
                        Bundle bundle = msg.getData();
                        if (fragment.getActivity() != null) {
                            FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
                            fragment.progressDialogFragment = BaseProgressDialogFragment.newInstance(bundle);
                            fragment.progressDialogFragment.show(manager, BaseFragment.TAG);
                        }
                        break;
                    case MESSAGE_Progress_Message:
                        if (fragment.progressDialogFragment != null) {
                            String progress = (String) msg.obj;
                            fragment.progressDialogFragment.setProgressMessage(progress);
                        }
                        break;
                    case MESSAGE_Progress_Dismiss:
                        fragment.isShowingDialog = false;
                        if(fragment.progressDialogFragment != null){
                            fragment.progressDialogFragment.dismiss();
                            fragment.progressDialogFragment = null;
                        }
                        break;
                }
            }
        }


        @Override
        public void dispatchMessage(Message msg){
            if(isPaused){
                Message msgCopy = new Message();
                msgCopy.copyFrom(msg);
                mQueue.offer(msgCopy);
            }else{
                super.dispatchMessage(msg);
            }
        }


        @Override
        public void handleMessage(Message msg) {
            processMessage(msg);
        }
    }

}
