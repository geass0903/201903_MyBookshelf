package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements BaseFragment.FragmentListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean D = true; // Debug
    private static final boolean T = true; // Toast

    public static final String TOAST = "jp.gr.java_conf.nuranimation.my_bookshelf_TOAST";

    public static final int MESSAGE_TOAST         = 1;



    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 100;


    private MainActivityHandler mHandler;
    private RelativeLayout relativeLayout_mask;

    public boolean onResumeFlag = false;
    public boolean dispMaskFlag = false;
    private boolean process_MainActivityHandlerFlag = false;
    private LinkedList<Message> handlerMessageList;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.e(TAG, "+++ ON CREATE +++");
        setContentView(R.layout.activity_main);
        mHandler = new MainActivityHandler(this);
        relativeLayout_mask = findViewById(R.id.activity_main_mask);
        dispMask(false);
        handlerMessageList = new LinkedList<>();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.contents_container, new ShelfFragment(),ShelfFragment.TAG).commit();
        }

        checkPermission();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D) Log.e(TAG, "++ ON START ++");
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (D) Log.e(TAG, "+ ON RESUME +");
    }


    @Override
    public synchronized void onPause() {
        super.onPause();
        if (D) Log.e(TAG, "- ON PAUSE - ");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (D) Log.e(TAG, "--- ON DESTROY ---");
    }

    @Override
    public void onFragmentEvent(FragmentEvent event) {
        event.apply(this);
    }


    public void updateView() {
        if (D) Log.d(TAG, "updateView()");
        if(onResumeFlag) {
            if(dispMaskFlag) {
                relativeLayout_mask.setVisibility(View.VISIBLE);
            }else{
                relativeLayout_mask.setVisibility(View.GONE);
            }

            while (handlerMessageList.size() > 0) {
                Message msg = handlerMessageList.poll();
            }
        }
    }


    public void dispMask(boolean visible){
        dispMaskFlag = visible;
        updateView();
    }





    private static class MainActivityHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        MainActivityHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                if(msg.what == MESSAGE_TOAST){
                    if (T) Toast.makeText(activity, msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                }else {
                    Message message = new Message();
                    message.what = msg.what;
                    message.arg1 = msg.arg1;
                    message.arg2 = msg.arg2;
                    message.obj = msg.obj;
                    message.setData(msg.getData());
                    activity.handlerMessageList.add(message);
                    if (activity.onResumeFlag) {
                        activity.updateView();
                    }
                }
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_shelf:
                    fragment = getSupportFragmentManager().findFragmentByTag(ShelfFragment.TAG);
                    if(!(fragment instanceof ShelfFragment)){
                        mFragmentTransaction.replace(R.id.contents_container, new ShelfFragment(),ShelfFragment.TAG);
                        mFragmentTransaction.commit();
                    }
                    return true;
                case R.id.navigation_search:
                    fragment = getSupportFragmentManager().findFragmentByTag(BookDetailFragment.TAG);
                    if(fragment instanceof BookDetailFragment){
                        getSupportFragmentManager().popBackStack();
                        return true;
                    }
                    fragment = getSupportFragmentManager().findFragmentByTag(SearchFragment.TAG);
                    if(!(fragment instanceof SearchFragment)){
                        mFragmentTransaction.replace(R.id.contents_container, new SearchFragment(),SearchFragment.TAG);
                        mFragmentTransaction.commit();
                    }
                    return true;
                case R.id.navigation_new:
                    fragment = getSupportFragmentManager().findFragmentByTag(NewFragment.TAG);
                    if(!(fragment instanceof NewFragment)){
                        mFragmentTransaction.replace(R.id.contents_container, new NewFragment(),NewFragment.TAG);
                        mFragmentTransaction.commit();
                    }
                    return true;
                case R.id.navigation_settings:
                    fragment = getSupportFragmentManager().findFragmentByTag(SettingsFragment.TAG);
                    if(!(fragment instanceof SettingsFragment)){
                        mFragmentTransaction.replace(R.id.contents_container, new SettingsFragment(),SettingsFragment.TAG);
                        mFragmentTransaction.commit();
                    }

                    return true;
            }
            return false;
        }
    };



    private void checkPermission() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            if(D) Log.d(TAG,"Permission WRITE_EXTERNAL_STORAGE OK");
        }else {
            if (D) Log.d(TAG, "Permission WRITE_EXTERNAL_STORAGE NG");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void requestWriteExternalStoragePermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            } else {
                // Error
                Toast.makeText(this, getString(R.string.PERMISSION_ERROR), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermission();
                } else {
                    requestWriteExternalStoragePermission();
                }
                break;
            default:
                checkPermission();
                break;
        }
    }



 }
