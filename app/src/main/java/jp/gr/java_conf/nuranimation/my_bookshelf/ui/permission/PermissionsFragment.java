package jp.gr.java_conf.nuranimation.my_bookshelf.ui.permission;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.prefs.MyBookshelfPreferences;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.MyBookshelfEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.NormalDialogFragment;


public class PermissionsFragment extends BaseFragment implements NormalDialogFragment.OnNormalDialogListener {
    private static final String TAG = PermissionsFragment.class.getSimpleName();
    private static final boolean D = true;

    public static final String[] USE_PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final String KEY_USE_PERMISSIONS = "PermissionsFragment.KEY_USE_PERMISSIONS";

    private static final String KEY_SAVED_REQUEST_PERMISSIONS = "PermissionsFragment.KEY_SAVED_REQUEST_PERMISSIONS";
    private static final String TAG_RATIONALE_DIALOG = "PermissionsFragment.TAG_RATIONALE_DIALOG";
    private static final int REQUEST_CODE_ASK_FOR_PERMISSIONS = 1;


    private String[] mRequestPermissions;
    private MyBookshelfPreferences mPreferences;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (D) Log.d(TAG, "onAttach");
        mPreferences = new MyBookshelfPreferences(context.getApplicationContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (D) Log.d(TAG, "onViewCreated");
        if (savedInstanceState != null) {
            setRequestPermissions(savedInstanceState.getStringArray(KEY_SAVED_REQUEST_PERMISSIONS));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isAllowedAllPermissions(getContext(), USE_PERMISSIONS)) {
            if (!mPreferences.isCheckedPermissions()) {
                mPreferences.setCheckedPermissions(true);
                if (D) Log.d(TAG, "requestPermissions");
                requestPermissions(USE_PERMISSIONS);
            }
        } else {
            if (D) Log.d(TAG, "AllowedAllPermissions");
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray(KEY_SAVED_REQUEST_PERMISSIONS, mRequestPermissions);
    }

    @Override
    public void onNormalDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        if (requestCode == REQUEST_CODE_ASK_FOR_PERMISSIONS) {
            if (resultCode == DialogInterface.BUTTON_POSITIVE) {
                onNextRequestPermissions(getRequestPermissions());
            } else {
                onSkipRequestPermissions(getRequestPermissions());
            }
        }
    }

    @Override
    public void onNormalDialogCancelled(int requestCode, Bundle params) {
        if (requestCode == REQUEST_CODE_ASK_FOR_PERMISSIONS) {
            onSkipRequestPermissions(getRequestPermissions());
        }
    }

    public static boolean isAllowedAllPermissions(Context context, final String... permissions) {
        String[] denyPermissions = getDenyPermissions(context, permissions);
        return (denyPermissions == null);
    }

    public static String[] getDenyPermissions(Context context, final String... permissions) {
        final List<String> denyPermissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (context != null) {
                if (permission != null && ContextCompat.checkSelfPermission(context, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    denyPermissionList.add(permission);
                }
            }
        }
        if (denyPermissionList.size() == 0) {
            return null;
        }
        return denyPermissionList.toArray(new String[0]);
    }

    private void setRequestPermissions(String[] permissions) {
        mRequestPermissions = permissions;
    }

    private String[] getRequestPermissions() {
        return mRequestPermissions;
    }

    public final void requestPermissions(final String... permissions) {
        String[] denyPermissions = getDenyPermissions(getContext(), permissions);
        if (denyPermissions != null) {
            String[] shouldShowRationalePermissions = getShouldShowRationalePermissions(
                    denyPermissions);
            if (shouldShowRationalePermissions == null) {
                setRequestPermissions(denyPermissions);
                requestPermissions(getRequestPermissions(), REQUEST_CODE_ASK_FOR_PERMISSIONS);
            } else {
                setRequestPermissions(shouldShowRationalePermissions);
                showRationaleFragment();
            }
        }
    }

    protected final String[] getShouldShowRationalePermissions(final String... permissions) {
        final List<String> shouldRequestPermissionList = new ArrayList<>();
        for (String denyPermission : permissions) {
            if (denyPermission != null && shouldShowRequestPermissionRationale(denyPermission)) {
                shouldRequestPermissionList.add(denyPermission);
            }
        }
        if (shouldRequestPermissionList.size() == 0) {
            return null;
        }
        return shouldRequestPermissionList.toArray(new String[0]);
    }

    protected void showRationaleFragment() {
        String message = getString(R.string.permission_dialog_message) + "\n"
                + getString(R.string.permission_dialog_message1) + "\n"
                + getString(R.string.permission_dialog_message2);
        Bundle bundle = new Bundle();
        bundle.putString(NormalDialogFragment.KEY_TITLE, getString(R.string.permission_dialog_title));
        bundle.putString(NormalDialogFragment.KEY_MESSAGE, message);
        bundle.putString(NormalDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
        bundle.putString(NormalDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
        bundle.putBoolean(NormalDialogFragment.KEY_CANCELABLE, true);
        bundle.putInt(NormalDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_ASK_FOR_PERMISSIONS);
        if (!NormalDialogFragment.isShowingNormalDialog(this, TAG_RATIONALE_DIALOG)) {
            NormalDialogFragment.showNormalDialog(this, bundle, TAG_RATIONALE_DIALOG);
        }
    }

    protected void onNextRequestPermissions(final String[] requestPermissions) {
        requestPermissions(requestPermissions, REQUEST_CODE_ASK_FOR_PERMISSIONS);
    }

    protected void onSkipRequestPermissions(final String[] requestPermissions) {
        for (String permission : requestPermissions) {
            if (D) Log.d(TAG, "Skip request permission : " + permission);
        }
        onRequestPermissionsFinally();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_CODE_ASK_FOR_PERMISSIONS) {
            return;
        }
        for (int i = 0; i < permissions.length; i++) {
            final String permission = permissions[i];
            final int grantResult = grantResults[i];
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                onAllowPermission(permission);
            } else {
                onDenyPermission(permission);
            }
        }
        onRequestPermissionsFinally();
    }

    protected void onAllowPermission(final String permission) {
        if (D) Log.d(TAG, "Allowed permission = " + permission);
    }

    protected void onDenyPermission(final String permission) {
        if (D) Log.d(TAG, "Denied permission = " + permission);
    }

    protected void onRequestPermissionsFinally() {
        setRequestPermissions(null);
        if (D) Log.d(TAG, "onRequestPermissionsFinally");
        if (isAllowedAllPermissions(getContext(), USE_PERMISSIONS)) {
            onAllowAllPermissions();
        } else {
            onDenyPermissions();
        }
    }

    protected void onAllowAllPermissions() {
        Toast.makeText(getContext(), R.string.toast_success_get_permissions, Toast.LENGTH_SHORT).show();
        getFragmentListener().onFragmentEvent(MyBookshelfEvent.ALLOWED_ALL_PERMISSIONS, null);
    }

    protected void onDenyPermissions() {
        Toast.makeText(getContext(), R.string.toast_failed_get_permissions, Toast.LENGTH_SHORT).show();
        getFragmentListener().onFragmentEvent(MyBookshelfEvent.DENY_PERMISSIONS, null);
    }


}
