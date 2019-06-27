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

public class MainActivity extends AppCompatActivity implements BaseFragment.FragmentListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean D = true;

    private MyBookshelfApplicationData mApplicationData;
    private BookService mBookService;
    private BottomNavigationView mBottomNavigationView;


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            if (D) Log.d(TAG, "onServiceConnected");
            mBookService = ((BookService.MBinder)binder).getService();
            mBookService.cancelForeground();
            applyFragment(mBookService);
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
        mBottomNavigationView.setOnNavigationItemReselectedListener(mOnNavigationItemReselectedListener);
        if (savedInstanceState == null) {
            onFragmentEvent(MyBookshelfEvent.SELECT_SHELF_BOOKS, null);
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
            mBookService.cancelForeground();
            applyFragment(mBookService);
        }
    }


    @Override
    public synchronized void onPause() {
        super.onPause();
        if (D) Log.e(TAG, "- ON PAUSE - ");
        if (mBookService != null && mBookService.getServiceState() != BookService.STATE_NONE) {
            Intent intent = new Intent(this, BookService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
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
    public void onFragmentEvent(MyBookshelfEvent event, Bundle bundle) {
        event.apply(this,bundle);
    }



    private void applyFragment(BookService bookService) {
        Bundle bundle;
        int navigation_state = mBottomNavigationView.getSelectedItemId();
        int serviceState = bookService.getServiceState();

        switch(serviceState) {
            case BookService.STATE_NONE:
                break;
            case BookService.STATE_SEARCH_BOOKS_SEARCH_INCOMPLETE:
            case BookService.STATE_SEARCH_BOOKS_SEARCH_COMPLETE:
                if (navigation_state != R.id.navigation_search_books) {
                    mBottomNavigationView.getMenu().findItem(R.id.navigation_search_books).setChecked(true);
                    bundle = new BundleBuilder()
                            .put(BookService.KEY_SERVICE_STATE, serviceState)
                            .put(BookService.KEY_PARAM_SEARCH_KEYWORD, bookService.getSearchKeyword())
                            .put(BookService.KEY_PARAM_SEARCH_PAGE, bookService.getSearchPage())
                            .build();
                    onFragmentEvent(MyBookshelfEvent.SELECT_SEARCH_BOOKS, bundle);
                }else {
                    onFragmentEvent(MyBookshelfEvent.CHECK_SEARCH_STATE, null);
                }
                break;
            case BookService.STATE_NEW_BOOKS_RELOAD_INCOMPLETE:
            case BookService.STATE_NEW_BOOKS_RELOAD_COMPLETE:
                if (navigation_state != R.id.navigation_new_books) {
                    mBottomNavigationView.getMenu().findItem(R.id.navigation_new_books).setChecked(true);
                    bundle = new BundleBuilder()
                            .put(BookService.KEY_SERVICE_STATE, serviceState)
                            .build();
                    onFragmentEvent(MyBookshelfEvent.SELECT_NEW_BOOKS, bundle);
                }else {
                    onFragmentEvent(MyBookshelfEvent.CHECK_RELOAD_STATE, null);
                }
                break;
            case BookService.STATE_EXPORT_INCOMPLETE:
            case BookService.STATE_EXPORT_COMPLETE:
            case BookService.STATE_IMPORT_INCOMPLETE:
            case BookService.STATE_IMPORT_COMPLETE:
            case BookService.STATE_BACKUP_INCOMPLETE:
            case BookService.STATE_BACKUP_COMPLETE:
            case BookService.STATE_RESTORE_INCOMPLETE:
            case BookService.STATE_RESTORE_COMPLETE:
            case BookService.STATE_DROPBOX_LOGIN:
                if (navigation_state != R.id.navigation_settings) {
                    mBottomNavigationView.getMenu().findItem(R.id.navigation_settings).setChecked(true);
                    bundle = new BundleBuilder()
                            .put(BookService.KEY_SERVICE_STATE, serviceState)
                            .build();
                    onFragmentEvent(MyBookshelfEvent.SELECT_SETTINGS, bundle);
                }else {
                    onFragmentEvent(MyBookshelfEvent.CHECK_SETTINGS_STATE, null);
                }
                break;
        }
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.navigation_shelf_books:
                    onFragmentEvent(MyBookshelfEvent.SELECT_SHELF_BOOKS, null);
                    return true;
                case R.id.navigation_search_books:
                    onFragmentEvent(MyBookshelfEvent.SELECT_SEARCH_BOOKS, null);
                    return true;
                case R.id.navigation_new_books:
                    onFragmentEvent(MyBookshelfEvent.SELECT_NEW_BOOKS, null);
                    return true;
                case R.id.navigation_settings:
                    onFragmentEvent(MyBookshelfEvent.SELECT_SETTINGS, null);
                    return true;
            }
            return false;
        }
    };

    private BottomNavigationView.OnNavigationItemReselectedListener mOnNavigationItemReselectedListener = new BottomNavigationView.OnNavigationItemReselectedListener() {
        @Override
        public void onNavigationItemReselected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.navigation_shelf_books:
                    onFragmentEvent(MyBookshelfEvent.RESELECT_SHELF_BOOKS, null);
                    break;
                case R.id.navigation_search_books:
                    onFragmentEvent(MyBookshelfEvent.RESELECT_SEARCH_BOOKS, null);
                    break;
                case R.id.navigation_new_books:
                    onFragmentEvent(MyBookshelfEvent.RESELECT_NEW_BOOKS, null);
                    break;
                case R.id.navigation_settings:
                    onFragmentEvent(MyBookshelfEvent.RESELECT_SETTINGS, null);
                    break;
            }
        }
    };

}
