package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements BaseFragment.FragmentListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean D = true;


    private static final String KEY_STATE = "KEY_STATE";

    private int state = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.e(TAG, "+++ ON CREATE +++");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        if (savedInstanceState == null) {
            setTitle(R.string.Navigation_Item_Shelf);
            getSupportFragmentManager().beginTransaction().replace(R.id.contents_container, new FragmentShelfBooks(), FragmentShelfBooks.TAG).commit();
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
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_STATE,state);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        state = savedInstanceState.getInt(KEY_STATE,0);
    }




    @Override
    public void onFragmentEvent(FragmentEvent event) {
        event.apply(this);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_shelf:
                    switch (state) {
                        case 0:
                        case R.id.navigation_shelf:
                            fragment = getSupportFragmentManager().findFragmentByTag(FragmentBookDetail.TAG);
                            if (fragment instanceof FragmentBookDetail) {
                                getSupportFragmentManager().popBackStack();
                            } else {
                                fragment = getSupportFragmentManager().findFragmentByTag(FragmentShelfBooks.TAG);
                                if (fragment instanceof FragmentShelfBooks) {
                                    ((FragmentShelfBooks) fragment).scrollTop();
                                }
                            }
                            break;
                        default:
                            setTitle(R.string.Navigation_Item_Shelf);
                            mFragmentTransaction.replace(R.id.contents_container, new FragmentShelfBooks(), FragmentShelfBooks.TAG);
                            mFragmentTransaction.commit();
                            break;
                    }
                    state = R.id.navigation_shelf;
                    break;
                case R.id.navigation_search:
                    switch (state) {
                        case R.id.navigation_search:
                            fragment = getSupportFragmentManager().findFragmentByTag(FragmentBookDetail.TAG);
                            if (fragment instanceof FragmentBookDetail) {
                                getSupportFragmentManager().popBackStack();
                            } else {
                                fragment = getSupportFragmentManager().findFragmentByTag(FragmentSearchBooks.TAG);
                                if (fragment instanceof FragmentSearchBooks) {
                                }
                            }
                            break;
                        default:
                            setTitle(R.string.Navigation_Item_Search);
                            mFragmentTransaction.replace(R.id.contents_container, new FragmentSearchBooks(), FragmentSearchBooks.TAG);
                            mFragmentTransaction.commit();
                            break;
                    }
                    state = R.id.navigation_search;
                    break;
                case R.id.navigation_new:
                    switch (state) {
                        case R.id.navigation_new:
                            break;
                        default:
                            setTitle(R.string.Navigation_Item_NewBooks);
                            mFragmentTransaction.replace(R.id.contents_container, new FragmentNewBooks(), FragmentNewBooks.TAG);
                            mFragmentTransaction.commit();
                            break;
                    }
                    state = R.id.navigation_new;
                    break;
                case R.id.navigation_settings:
                    switch (state) {
                        case R.id.navigation_settings:
                            break;
                        default:
                            setTitle(R.string.Navigation_Item_Settings);
                            mFragmentTransaction.replace(R.id.contents_container, new FragmentSettings(), FragmentSettings.TAG);
                            mFragmentTransaction.commit();
                            break;
                    }
                    state = R.id.navigation_settings;
                    break;
            }
            return true;
        }
    };



 }
