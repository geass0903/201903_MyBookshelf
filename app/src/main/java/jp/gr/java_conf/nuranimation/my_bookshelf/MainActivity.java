package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements BaseFragment.FragmentListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean D = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.e(TAG, "+++ ON CREATE +++");
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.contents_container, new FragmentBookshelf(), FragmentBookshelf.TAG).commit();
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
    public void onFragmentEvent(FragmentEvent event) {
        event.apply(this);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();

            // remove FragmentBookDetail
            fragment = getSupportFragmentManager().findFragmentByTag(FragmentBookDetail.TAG);
            if(fragment instanceof FragmentBookDetail){
                mFragmentTransaction.remove(fragment);
            }
            switch (item.getItemId()) {
                case R.id.navigation_shelf:
                    fragment = getSupportFragmentManager().findFragmentByTag(FragmentBookshelf.TAG);
                    if(!(fragment instanceof FragmentBookshelf)){
                        mFragmentTransaction.replace(R.id.contents_container, new FragmentBookshelf(), FragmentBookshelf.TAG);
                    }
                    break;
                case R.id.navigation_search:
                    fragment = getSupportFragmentManager().findFragmentByTag(FragmentSearchBooks.TAG);
                    if(!(fragment instanceof FragmentSearchBooks)){
                        mFragmentTransaction.replace(R.id.contents_container, new FragmentSearchBooks(), FragmentSearchBooks.TAG);
                    }
                    break;
                case R.id.navigation_new:
                    fragment = getSupportFragmentManager().findFragmentByTag(FragmentNewBooks.TAG);
                    if(!(fragment instanceof FragmentNewBooks)){
                        mFragmentTransaction.replace(R.id.contents_container, new FragmentNewBooks(), FragmentNewBooks.TAG);
                    }
                    break;
                case R.id.navigation_settings:
                    fragment = getSupportFragmentManager().findFragmentByTag(FragmentSettings.TAG);
                    if(!(fragment instanceof FragmentSettings)){
                        mFragmentTransaction.replace(R.id.contents_container, new FragmentSettings(), FragmentSettings.TAG);
                    }
                    break;
            }
            mFragmentTransaction.commit();
            return true;
        }
    };



 }
