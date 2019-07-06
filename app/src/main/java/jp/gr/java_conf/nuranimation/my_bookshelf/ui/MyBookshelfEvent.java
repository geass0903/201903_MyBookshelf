package jp.gr.java_conf.nuranimation.my_bookshelf.ui;


import android.os.Bundle;
import android.support.transition.Slide;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.service.BookService;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.book_detail.BookDetailFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.new_books.NewBooksFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.permission.PermissionsFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.search_books.SearchBooksFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.settings.SettingsFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.shelf_books.ShelfBooksFragment;

public enum MyBookshelfEvent {
    SELECT_SHELF_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService service = activity.getService();
            if(service != null){
                service.cancelSearch();
            }
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ShelfBooksFragment shelfBooksFragment = new ShelfBooksFragment();
            shelfBooksFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.contents_container,shelfBooksFragment, ShelfBooksFragment.TAG);
            PermissionsFragment permissionsFragment = new PermissionsFragment();
            fragmentTransaction.add(R.id.contents_container, permissionsFragment, PermissionsFragment.TAG);
            fragmentTransaction.commit();
        }
    },
    RESELECT_SHELF_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            fragment = fragmentManager.findFragmentByTag(ShelfBooksFragment.TAG);
            if (fragment instanceof ShelfBooksFragment) {
                ((ShelfBooksFragment) fragment).scrollToTop();
            }
        }
    },
    SELECT_SEARCH_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            SearchBooksFragment searchBooksFragment = new SearchBooksFragment();
            searchBooksFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.contents_container,searchBooksFragment, SearchBooksFragment.TAG);
            PermissionsFragment permissionsFragment = new PermissionsFragment();
            fragmentTransaction.add(R.id.contents_container, permissionsFragment, PermissionsFragment.TAG);
            fragmentTransaction.commit();
        }
    },
    RESELECT_SEARCH_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            fragment = fragmentManager.findFragmentByTag(SearchBooksFragment.TAG);
            if (fragment instanceof SearchBooksFragment) {
                ((SearchBooksFragment) fragment).prepareSearch();
            }
        }
    },
    SELECT_NEW_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService service = activity.getService();
            if(service != null){
                service.cancelSearch();
            }
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            NewBooksFragment newBooksFragment = new NewBooksFragment();
            newBooksFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.contents_container,newBooksFragment, NewBooksFragment.TAG);
            PermissionsFragment permissionsFragment = new PermissionsFragment();
            fragmentTransaction.add(R.id.contents_container, permissionsFragment, PermissionsFragment.TAG);
            fragmentTransaction.commit();
        }
    },
    RESELECT_NEW_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }

            fragment = fragmentManager.findFragmentByTag(NewBooksFragment.TAG);
            if (fragment instanceof NewBooksFragment) {
                ((NewBooksFragment) fragment).scrollToTop();
            }

        }
    },
    SELECT_SETTINGS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService service = activity.getService();
            if(service != null){
                service.cancelSearch();
            }
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            SettingsFragment settingsFragment = new SettingsFragment();
            settingsFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.contents_container,settingsFragment, SettingsFragment.TAG);
            PermissionsFragment permissionsFragment = new PermissionsFragment();
            fragmentTransaction.add(R.id.contents_container, permissionsFragment, PermissionsFragment.TAG);
            fragmentTransaction.commit();
        }
    },
    RESELECT_SETTINGS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
        }
    },
    GO_TO_BOOK_DETAIL {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            BookDetailFragment bookDetailFragment = new BookDetailFragment();
            bookDetailFragment.setArguments(bundle);
//            Slide slide = new Slide();
//            slide.setSlideEdge(Gravity.BOTTOM);
//            bookDetailFragment.setEnterTransition(slide);

            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right);

            fragmentTransaction.replace(R.id.contents_container, bookDetailFragment, BookDetailFragment.TAG);

            PermissionsFragment permissionsFragment = new PermissionsFragment();
            fragmentTransaction.add(R.id.contents_container, permissionsFragment, PermissionsFragment.TAG);


            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        }
    },
    POP_BACK_STACK_BOOK_DETAIL {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(BookDetailFragment.TAG);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
        }
    },

    POP_BACK_STACK {
      @Override
      public void apply(MainActivity activity, Bundle bundle){
          FragmentManager fragmentManager = activity.getSupportFragmentManager();
          if(fragmentManager.getBackStackEntryCount() > 0){
              fragmentManager.popBackStack();
          }
      }
    },

    CHECK_SEARCH_STATE {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(SearchBooksFragment.TAG);
            if (fragment instanceof SearchBooksFragment) {
                ((SearchBooksFragment) fragment).checkSearchState();
            }
        }
    },
    CHECK_RELOAD_STATE {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(NewBooksFragment.TAG);
            if (fragment instanceof NewBooksFragment) {
                ((NewBooksFragment) fragment).checkReloadState();
            }
        }
    },
    CHECK_SETTINGS_STATE {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(SettingsFragment.TAG);
            if (fragment instanceof SettingsFragment) {
                ((SettingsFragment) fragment).checkSettingsState();
            }
        }
    },
    ALLOWED_ALL_PERMISSIONS {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(SettingsFragment.TAG);
            if(fragment instanceof SettingsFragment) {
                ((SettingsFragment) fragment).onAllowAllPermissions();
            }
        }
    },
    DENY_PERMISSIONS {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(SettingsFragment.TAG);
            if(fragment instanceof SettingsFragment) {
                ((SettingsFragment) fragment).onDenyPermissions();
            }
        }
    },


    @SuppressWarnings("unused")
    DEFAULT {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
        }
    };

    abstract public void apply(MainActivity activity, Bundle bundle);

}
