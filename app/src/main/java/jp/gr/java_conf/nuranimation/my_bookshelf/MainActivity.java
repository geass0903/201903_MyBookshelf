package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.event.ActivityEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.event.FragmentEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.fragment.ShelfBooksFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.service.BookSearchService;
import jp.gr.java_conf.nuranimation.my_bookshelf.utils.MyBookshelfApplicationData;

public class MainActivity extends AppCompatActivity implements BaseFragment.FragmentListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean D = true;

    private static final String KEY_NAVIGATION_STATE = "KEY_NAVIGATION_STATE";

    private MyBookshelfApplicationData mApplicationData;
    private int state = 0;




    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (D) Log.e(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (D) Log.e(TAG, "onServiceDisconnected");

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.e(TAG, "+++ ON CREATE +++");
        setContentView(R.layout.activity_main);

        mApplicationData = (MyBookshelfApplicationData)getApplicationContext();
        Toolbar toolbar = findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        if (savedInstanceState == null) {
            setTitle(R.string.Navigation_Item_Shelf);
            getSupportFragmentManager().beginTransaction().replace(R.id.contents_container, new ShelfBooksFragment(), ShelfBooksFragment.TAG).commit();
        }
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
        mApplicationData.setCheckedPermissions(false);
 //       unbindService(connection);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_NAVIGATION_STATE,state);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        state = savedInstanceState.getInt(KEY_NAVIGATION_STATE,0);
    }


    @Override
    public void onFragmentEvent(FragmentEvent event) {
        event.apply(this);
    }


    private void onEvent(ActivityEvent event){
        event.apply(this);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_shelf:
                    switch (state) {
                        case 0:
                        case R.id.navigation_shelf:

//                            Intent intent =  new Intent(getApplicationContext(), BookSearchService.class);

                            Intent intent = new Intent(getApplicationContext(),BookSearchService.class);

 //                           bindService(intent,connection, Service.BIND_AUTO_CREATE);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
   //                             startForegroundService(intent);
                            }else{
    //                            startService(intent);
                            }


//                            onEvent(ActivityEvent.NAVIGATION_SHELF);
                            break;
                        default:
                            onEvent(ActivityEvent.NAVIGATION_OTHER_TO_SHELF);
                            break;
                    }
                    state = R.id.navigation_shelf;
                    break;
                case R.id.navigation_search:
                    switch (state) {
                        case R.id.navigation_search:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Intent intent = new Intent(getApplicationContext(),BookSearchService.class);

   //                             startForegroundService(intent);
                            }else{
                                Intent intent = new Intent(getApplicationContext(),BookSearchService.class);

    //                            startService(intent);
                            }
//                            onEvent(ActivityEvent.NAVIGATION_SEARCH);
                            break;
                        default:
                            onEvent(ActivityEvent.NAVIGATION_OTHER_TO_SEARCH);
                            break;
                    }
                    state = R.id.navigation_search;
                    break;
                case R.id.navigation_new:
                    switch (state) {
                        case R.id.navigation_new:
                            Intent intent = new Intent(getApplicationContext(),BookSearchService.class);

                            stopService(intent);
//                            onEvent(ActivityEvent.NAVIGATION_NEW);
                            break;
                        default:
                            onEvent(ActivityEvent.NAVIGATION_OTHER_TO_NEW);
                            break;
                    }
                    state = R.id.navigation_new;
                    break;
                case R.id.navigation_settings:
                    switch (state) {
                        case R.id.navigation_settings:
                            onEvent(ActivityEvent.NAVIGATION_SETTINGS);
                            break;
                        default:
                            onEvent(ActivityEvent.NAVIGATION_OTHER_TO_SETTINGS);
                            break;
                    }
                    state = R.id.navigation_settings;
                    break;
            }
            return true;
        }
    };

}
