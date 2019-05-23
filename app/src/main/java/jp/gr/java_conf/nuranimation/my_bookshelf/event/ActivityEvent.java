package jp.gr.java_conf.nuranimation.my_bookshelf.event;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import jp.gr.java_conf.nuranimation.my_bookshelf.fragment.BookDetailFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.fragment.ShelfBooksFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.MainActivity;
import jp.gr.java_conf.nuranimation.my_bookshelf.fragment.NewBooksFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.fragment.SearchBooksFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.fragment.SettingsFragment;

/**
 * Created by Kamada on 2019/03/11.
 */


public enum ActivityEvent {
    NAVIGATION_SHELF {
        @Override
        public void apply(MainActivity activity) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            if (fragmentManager != null) {
                fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
                if (fragment instanceof BookDetailFragment) {
                    fragmentManager.popBackStack();
                }
                fragment = fragmentManager.findFragmentByTag(ShelfBooksFragment.TAG);
                if (fragment instanceof ShelfBooksFragment) {
                    ((ShelfBooksFragment) fragment).scrollTop();
                }
            }
        }
    },
    NAVIGATION_OTHER_TO_SHELF {
        @Override
        public void apply(MainActivity activity) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            activity.setTitle(R.string.Navigation_Item_Shelf);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contents_container, new ShelfBooksFragment(), ShelfBooksFragment.TAG);
            fragmentTransaction.commit();
        }
    },
    NAVIGATION_SEARCH {
        @Override
        public void apply(MainActivity activity) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            if (fragmentManager != null) {
                fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
                if (fragment instanceof BookDetailFragment) {
                    fragmentManager.popBackStack();
                }
                fragment = fragmentManager.findFragmentByTag(SearchBooksFragment.TAG);
                if (fragment instanceof SearchBooksFragment) {
                    ((SearchBooksFragment) fragment).prepareSearch();
                }
            }
        }
    },
    NAVIGATION_OTHER_TO_SEARCH {
        @Override
        public void apply(MainActivity activity) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            activity.setTitle(R.string.Navigation_Item_Search);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contents_container, new SearchBooksFragment(), SearchBooksFragment.TAG);
            fragmentTransaction.commit();
        }
    },
    NAVIGATION_NEW {
        @Override
        public void apply(MainActivity activity) {
        }
    },
    NAVIGATION_OTHER_TO_NEW {
        @Override
        public void apply(MainActivity activity) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            activity.setTitle(R.string.Navigation_Item_NewBooks);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contents_container, new NewBooksFragment(), NewBooksFragment.TAG);
            fragmentTransaction.commit();
        }
    },
    NAVIGATION_SETTINGS {
        @Override
        public void apply(MainActivity activity) {
        }
    },
    NAVIGATION_OTHER_TO_SETTINGS {
        @Override
        public void apply(MainActivity activity) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            activity.setTitle(R.string.Navigation_Item_Settings);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.contents_container, new SettingsFragment(), SettingsFragment.TAG);
            fragmentTransaction.commit();
        }
    },






    @SuppressWarnings("unused")
    DEFAULT {
        @Override
        public void apply(MainActivity activity) {
        }
    };

    abstract public void apply(MainActivity activity);

}
