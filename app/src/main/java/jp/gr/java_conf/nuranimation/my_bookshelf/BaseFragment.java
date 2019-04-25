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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kamada on 2019/03/11.
 */

public class BaseFragment extends Fragment implements BaseDialogFragment.OnBaseDialogListener{
    private static final String TAG = BaseFragment.class.getSimpleName();

    public static final int MESSAGE_PROGRESS_SHOW       = 1;
    public static final int MESSAGE_PROGRESS            = 2;
    public static final int MESSAGE_PROGRESS_DISMISS = 3;

    static final int RequestCode_Logout = 100;
    static final int RequestCode_ask_for_Permissions = 999;
    private static final String Key_saved_RequestPermissions = "Key_saved_RequestPermissions";

    private String[] mRequestPermissions;

    AppCompatActivity mActivity = null;
    FragmentListener mFragmentListener = null;

    BaseProgressDialogFragment progressDialogFragment;
    ProgressDialogHandler handler = new ProgressDialogHandler(this);


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
    public void onResume() {
        super.onResume();
        handler.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.pause();
    }




    private void setRequestPermissions(String[] permissions) {
        mRequestPermissions = permissions;
    }

    private String[] getRequestPermissions() {
        return mRequestPermissions;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putStringArray(Key_saved_RequestPermissions, mRequestPermissions);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            // 保存していたパーミッションを再設定
            setRequestPermissions(savedInstanceState.getStringArray(Key_saved_RequestPermissions));
        }
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
                requestPermissions(getRequestPermissions(), RequestCode_ask_for_Permissions);
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
        bundle.putInt(BaseDialogFragment.request_code, RequestCode_ask_for_Permissions);
        BaseDialogFragment dialog = BaseDialogFragment.newInstance(this, bundle);
        if(getActivity() != null) {
            dialog.show(getActivity().getSupportFragmentManager(), BaseFragment.TAG);
        }
    }

    @SuppressWarnings({"unused","SameParameterValue"})
    protected void onNextRequestPermissions(final String[] requestPermissions) {
        // パーミッション要求ダイアログを表示
        requestPermissions(requestPermissions, RequestCode_ask_for_Permissions);
    }
    @SuppressWarnings({"unused","SameParameterValue"})
    protected void onSkipRequestPermissions(final String[] requestPermissions) {
        // 後処理
        onRequestPermissionsFinally();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RequestCode_ask_for_Permissions) {
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
        if(requestCode == RequestCode_ask_for_Permissions) {
            if(resultCode == DialogInterface.BUTTON_POSITIVE) {
                onNextRequestPermissions(getRequestPermissions());
            }else{
                onSkipRequestPermissions(getRequestPermissions());
            }
        }
    }

    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {
        if(requestCode == RequestCode_ask_for_Permissions) {
            onSkipRequestPermissions(getRequestPermissions());
        }
    }



    static class ProgressDialogHandler extends Handler {
        private final WeakReference<BaseFragment> mWeakReference;

        private List<Message> messageList = new ArrayList<>();
        private boolean isPaused;

        ProgressDialogHandler(BaseFragment fragment) {
            mWeakReference = new WeakReference<>(fragment);
        }

        void resume() {
            isPaused = false;
            while (messageList.size() > 0) {
                Message msg = messageList.get(0);
                messageList.remove(0);
                sendMessage(msg);
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
                        Bundle bundle = msg.getData();
                        if (fragment.getActivity() != null) {
                            FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
                            fragment.progressDialogFragment = BaseProgressDialogFragment.newInstance(bundle);
                            fragment.progressDialogFragment.show(manager, BaseFragment.TAG);
                        }
                        break;
                    case MESSAGE_PROGRESS:
                        if (fragment.progressDialogFragment != null) {
                            String progress = (String) msg.obj;
                            fragment.progressDialogFragment.setProgressMessage(progress);
                        }
                        break;
                    case MESSAGE_PROGRESS_DISMISS:
                        if(fragment.progressDialogFragment != null){
                            fragment.progressDialogFragment.dismiss();
                            fragment.progressDialogFragment = null;
                        }
                        break;
                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            if (isPaused) {
                Message msgCopy = new Message();
                msgCopy.copyFrom(msg);
                messageList.add(msgCopy);
            } else {
                processMessage(msg);
            }
        }

    }

}
