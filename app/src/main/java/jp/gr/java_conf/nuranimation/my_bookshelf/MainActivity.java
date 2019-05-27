package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.app.Service;
import android.content.ComponentName;
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
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BundleBuilder;
import jp.gr.java_conf.nuranimation.my_bookshelf.fragment.ShelfBooksFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.background.BookService;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfApplicationData;

public class MainActivity extends AppCompatActivity implements BaseFragment.FragmentListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean D = true;

    private static final String KEY_NAVIGATION_STATE = "KEY_NAVIGATION_STATE";

    private MyBookshelfApplicationData mApplicationData;
    private BookService mBookService;
    private BottomNavigationView mBottomNavigationView;
    private int navigation_state = 0;



    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            if (D) Log.d(TAG, "onServiceConnected");
            mBookService = ((BookService.MBinder)binder).getService();
            checkServiceState();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (D) Log.e(TAG, "onServiceDisconnected");
        }
    };

    public BookService getService(){
        return mBookService;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.e(TAG, "+++ ON CREATE +++");
        setContentView(R.layout.activity_main);
        mApplicationData = (MyBookshelfApplicationData)getApplicationContext();

        Toolbar toolbar = findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        mBottomNavigationView = findViewById(R.id.navigation);
        mBottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        if (savedInstanceState == null) {
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
    public synchronized void onResumeFragments(){
        super.onResumeFragments();
        if (D) Log.e(TAG, "+ ON RESUME FRAGMENTS +");
        if(mBookService == null){
            Intent intent = new Intent(this, BookService.class);
            bindService(intent,connection, Service.BIND_AUTO_CREATE);
        }else{
            mBookService.stopForeground(true);
            checkServiceState();
        }
    }


    @Override
    public synchronized void onPause() {
        super.onPause();
        if (D) Log.e(TAG, "- ON PAUSE - ");
        if(mBookService != null){
            if(mBookService.getServiceState() != BookService.STATE_NONE){
                Intent intent = new Intent(this, BookService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                }else{
                    startService(intent);
                }
            }
        }
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
        if(mBookService != null) {
            unbindService(connection);
            mBookService = null;
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_NAVIGATION_STATE, navigation_state);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        navigation_state = savedInstanceState.getInt(KEY_NAVIGATION_STATE, 0);
    }


    @Override
    public void onFragmentEvent(MyBookshelfEvent event, Bundle bundle) {
        event.apply(this,bundle);
    }


    private void onActivityEvent(MyBookshelfEvent event, Bundle bundle){
        event.apply(this,bundle);
    }


    private void checkServiceState() {
        switch(navigation_state){
            case 0:
                Bundle bundle;
                switch (mBookService.getServiceState()) {
                    case BookService.STATE_NONE:
                        mBottomNavigationView.getMenu().findItem(R.id.navigation_shelf).setChecked(true);
                        navigation_state = R.id.navigation_shelf;
                        break;
                    case BookService.STATE_SEARCH_BOOKS_SEARCH_START:
                    case BookService.STATE_SEARCH_BOOKS_SEARCH_FINISH:
                        mBottomNavigationView.getMenu().findItem(R.id.navigation_search).setChecked(true);
                        navigation_state = R.id.navigation_search;
                        bundle = new BundleBuilder()
                                .put(BookService.KEY_SERVICE_STATE, mBookService.getServiceState())
                                .put(BookService.KEY_PARAM_SEARCH_KEYWORD, mBookService.getSearchKeyword())
                                .put(BookService.KEY_PARAM_SEARCH_PAGE, mBookService.getSearchPage())
                                .build();
                        if (D) Log.d(TAG, "MOVE_OTHER_TO_SEARCH_BOOKS bundle: " + bundle);
                        onActivityEvent(MyBookshelfEvent.MOVE_OTHER_TO_SEARCH_BOOKS, bundle);
                        break;
                    case BookService.STATE_NEW_BOOKS_RELOAD_START:
                    case BookService.STATE_NEW_BOOKS_RELOAD_FINISH:
                        mBottomNavigationView.getMenu().findItem(R.id.navigation_new).setChecked(true);
                        navigation_state = R.id.navigation_new;
                        bundle = new BundleBuilder()
                                .put(BookService.KEY_SERVICE_STATE,mBookService.getServiceState())
                                .put(BookService.KEY_PARAM_SEARCH_KEYWORD, 1)
                                .put(BookService.KEY_PARAM_SEARCH_PAGE, 1)
                                .build();
                        if (D) Log.d(TAG, "MOVE_OTHER_TO_SEARCH_BOOKS bundle: " + bundle);
                        onActivityEvent(MyBookshelfEvent.MOVE_OTHER_TO_NEW_BOOKS, bundle);
                        break;
                }
                break;
            case R.id.navigation_shelf:
                break;
            case R.id.navigation_search:
                switch(mBookService.getServiceState()){
                    case BookService.STATE_SEARCH_BOOKS_SEARCH_START:
                    case BookService.STATE_SEARCH_BOOKS_SEARCH_FINISH:
                        if (D) Log.d(TAG, "ACTION_CHECK_SEARCH_STATE");
                        onActivityEvent(MyBookshelfEvent.ACTION_CHECK_SEARCH_STATE,null);
                        break;
                }
                break;
            case R.id.navigation_new:
                switch(mBookService.getServiceState()){
                    case BookService.STATE_NEW_BOOKS_RELOAD_START:
                    case BookService.STATE_NEW_BOOKS_RELOAD_FINISH:
                        if (D) Log.d(TAG, "ACTION_CHECK_RELOAD_STATE");
                        onActivityEvent(MyBookshelfEvent.ACTION_CHECK_RELOAD_STATE,null);
                        break;
                }
                break;
            case R.id.navigation_settings:
                break;
        }

    }



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_shelf:
                    switch (navigation_state) {
                        case R.id.navigation_shelf:
                            if (D) Log.d(TAG, "GO_TO_SHELF_BOOKS");
                            onActivityEvent(MyBookshelfEvent.GO_TO_SHELF_BOOKS, null);
                            break;
                        default:
                            if (D) Log.d(TAG, "MOVE_OTHER_TO_SHELF_BOOKS");
                            onActivityEvent(MyBookshelfEvent.MOVE_OTHER_TO_SHELF_BOOKS, null);
                            break;
                    }
                    navigation_state = R.id.navigation_shelf;
                    break;
                case R.id.navigation_search:
                    switch (navigation_state) {
                        case R.id.navigation_search:
                            if (D) Log.d(TAG, "GO_TO_SEARCH_BOOKS");
                            onActivityEvent(MyBookshelfEvent.GO_TO_SEARCH_BOOKS, null);
                            break;
                        default:
                            if (D) Log.d(TAG, "MOVE_OTHER_TO_SEARCH_BOOKS");
                            onActivityEvent(MyBookshelfEvent.MOVE_OTHER_TO_SEARCH_BOOKS, null);
                            break;
                    }
                    navigation_state = R.id.navigation_search;
                    break;
                case R.id.navigation_new:
                    switch (navigation_state) {
                        case R.id.navigation_new:
                            if (D) Log.d(TAG, "GO_TO_NEW_BOOKS");
                            onActivityEvent(MyBookshelfEvent.GO_TO_NEW_BOOKS, null);
                            break;
                        default:
                            if (D) Log.d(TAG, "MOVE_OTHER_TO_NEW_BOOKS");
                            onActivityEvent(MyBookshelfEvent.MOVE_OTHER_TO_NEW_BOOKS, null);
                            break;
                    }
                    navigation_state = R.id.navigation_new;
                    break;
                case R.id.navigation_settings:
                    switch (navigation_state) {
                        case R.id.navigation_settings:
                            if (D) Log.d(TAG, "GO_TO_SETTINGS");
                            onActivityEvent(MyBookshelfEvent.GO_TO_SETTINGS, null);
                            break;
                        default:
                            if (D) Log.d(TAG, "MOVE_OTHER_TO_SETTINGS");
                            onActivityEvent(MyBookshelfEvent.MOVE_OTHER_TO_SETTINGS, null);
                            break;
                    }
                    navigation_state = R.id.navigation_settings;
                    break;
            }
            return true;
        }
    };

}
