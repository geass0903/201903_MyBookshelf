package jp.gr.java_conf.nuranimation.my_bookshelf.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.SearchParam;
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
            fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_BOOK_DETAIL_FRAGMENT);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            ShelfBooksFragment shelfBooksFragment = new ShelfBooksFragment();
            shelfBooksFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.contents_container,shelfBooksFragment, MainActivity.TAG_SHELF_BOOKS_FRAGMENT);
            fragmentTransaction.commit();
        }
    },
    RESELECT_SHELF_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_BOOK_DETAIL_FRAGMENT);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_SHELF_BOOKS_FRAGMENT);
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
            fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_BOOK_DETAIL_FRAGMENT);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            SearchBooksFragment searchBooksFragment = new SearchBooksFragment();
            searchBooksFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.contents_container,searchBooksFragment, MainActivity.TAG_SEARCH_BOOKS_FRAGMENT);
            fragmentTransaction.commit();
        }
    },
    RESELECT_SEARCH_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_BOOK_DETAIL_FRAGMENT);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_SEARCH_BOOKS_FRAGMENT);
            if (fragment instanceof SearchBooksFragment) {
                ((SearchBooksFragment) fragment).clearSearchView();
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
            fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_BOOK_DETAIL_FRAGMENT);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            NewBooksFragment newBooksFragment = new NewBooksFragment();
            newBooksFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.contents_container,newBooksFragment, MainActivity.TAG_NEW_BOOKS_FRAGMENT);
            fragmentTransaction.commit();
        }
    },
    RESELECT_NEW_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            Fragment fragment;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_BOOK_DETAIL_FRAGMENT);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }

            fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_NEW_BOOKS_FRAGMENT);
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
            fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_BOOK_DETAIL_FRAGMENT);
            if (fragment instanceof BookDetailFragment) {
                fragmentManager.popBackStack();
            }
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            SettingsFragment settingsFragment = new SettingsFragment();
            settingsFragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.contents_container,settingsFragment, MainActivity.TAG_SETTINGS_FRAGMENT);
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

            fragmentTransaction.replace(R.id.contents_container, bookDetailFragment, MainActivity.TAG_BOOK_DETAIL_FRAGMENT);

            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();




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







    START_SEARCH_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService bookService = activity.getService();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_SEARCH_BOOKS_FRAGMENT);
            if (fragment instanceof SearchBooksFragment && bookService != null && bundle != null) {
                String keyword = bundle.getString(SearchBooksFragment.KEY_PARAM_SEARCH_KEYWORD);
                int page = bundle.getInt(SearchBooksFragment.KEY_PARAM_SEARCH_PAGE);
                boolean can_start = ((SearchBooksFragment) fragment).startSearch(keyword, page);
                if(can_start) {
                    SearchParam searchParam = SearchParam.setSearchParam(keyword, page);
                    bookService.searchBooks(searchParam);
                }
            }
        }
    },
    FINISH_SEARCH_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService bookService = activity.getService();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_SEARCH_BOOKS_FRAGMENT);
            if (fragment instanceof SearchBooksFragment && bookService != null) {
                ((SearchBooksFragment) fragment).finishSearch(bookService.getResult());
                bookService.setServiceState(BookService.STATE_NONE);
                bookService.stopForeground(false);
                bookService.stopSelf();
            }
        }
    },
    SEARCH_CANCEL {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService bookService = activity.getService();
            if(bookService != null){
                bookService.cancelSearch();
            }
        }
    },
    CHECK_SEARCH_STATE {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            BookService bookService = activity.getService();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_SEARCH_BOOKS_FRAGMENT);
            if (fragment instanceof SearchBooksFragment && bookService != null) {
                ((SearchBooksFragment) fragment).checkSearchState(bookService.getServiceState());
            }
        }
    },


    START_RELOAD_NEW_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService bookService = activity.getService();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_NEW_BOOKS_FRAGMENT);
            if (fragment instanceof NewBooksFragment && bookService != null && bundle != null) {
                List<String> authors = bundle.getStringArrayList(NewBooksFragment.KEY_AUTHORS_LIST);
                ((NewBooksFragment) fragment).startReloadNewBooks();
                bookService.reloadNewBooks(authors);
            }
        }
    },
    FINISH_RELOAD_NEW_BOOKS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService bookService = activity.getService();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_NEW_BOOKS_FRAGMENT);
            if (fragment instanceof NewBooksFragment && bookService != null) {
                ((NewBooksFragment) fragment).finishReloadNewBooks(bookService.getResult());
                bookService.setServiceState(BookService.STATE_NONE);
                bookService.stopForeground(false);
                bookService.stopSelf();
            }
        }
    },
    RELOAD_CANCEL {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService bookService = activity.getService();
            if(bookService != null){
                bookService.cancelReload();
            }
        }
    },
    CHECK_RELOAD_STATE {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            BookService bookService = activity.getService();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_NEW_BOOKS_FRAGMENT);
            if (fragment instanceof NewBooksFragment && bookService != null) {
                ((NewBooksFragment) fragment).checkReloadState(bookService.getServiceState());
            }
        }
    },

    START_BACKUP {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService bookService = activity.getService();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_SETTINGS_FRAGMENT);
            if (fragment instanceof SettingsFragment && bookService != null && bundle != null) {
                boolean isAllowedPermissions = ((SettingsFragment) fragment).isAllowedPermissions();
                if(isAllowedPermissions){
                    int type = bundle.getInt(SettingsFragment.KEY_BACKUP_TYPE);
                    ((SettingsFragment) fragment).startBackup(type);
                    bookService.fileBackup(type);
                }else{
                    ((SettingsFragment) fragment).requestPermissions();
                }
            }
        }
    },

    FINISH_BACKUP {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService bookService = activity.getService();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_SETTINGS_FRAGMENT);
            if (fragment instanceof SettingsFragment && bookService != null) {
                ((SettingsFragment) fragment).finishBackup(bookService.getResult());
                bookService.setServiceState(BookService.STATE_NONE);
                bookService.stopForeground(false);
                bookService.stopSelf();
            }
        }
    },
    BACKUP_CANCEL {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService bookService = activity.getService();
            if(bookService != null){
                bookService.cancelBackup();
            }
        }
    },
    CHECK_SETTINGS_STATE {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            BookService bookService = activity.getService();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_SETTINGS_FRAGMENT);
            if (fragment instanceof SettingsFragment && bookService != null) {
                ((SettingsFragment) fragment).checkSettingsState(bookService.getServiceState());
            }
        }
    },







    ALLOWED_ALL_PERMISSIONS {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_SETTINGS_FRAGMENT);
            if(fragment instanceof SettingsFragment) {
                ((SettingsFragment) fragment).onAllowAllPermissions();
            }
        }
    },
    DENY_PERMISSIONS {
        @Override
        public void apply(MainActivity activity, Bundle bundle){
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_SETTINGS_FRAGMENT);
            if(fragment instanceof SettingsFragment) {
                ((SettingsFragment) fragment).onDenyPermissions();
            }
        }
    },




    START_DROPBOX_AUTH {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService bookService = activity.getService();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_SETTINGS_FRAGMENT);
            if (fragment instanceof SettingsFragment && bookService != null) {
                bookService.setServiceState(BookService.STATE_DROPBOX_AUTH);
                ((SettingsFragment) fragment).startDropboxAuth();
            }
        }
    },

    FINISH_DROPBOX_AUTH {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            BookService bookService = activity.getService();
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_SETTINGS_FRAGMENT);
            if (fragment instanceof SettingsFragment && bookService != null) {
                ((SettingsFragment) fragment).finishDropboxAuth();
                bookService.setServiceState(BookService.STATE_NONE);
                bookService.stopForeground(false);
                bookService.stopSelf();
            }
        }
    },









    ADD_PERMISSIONS_FRAGMENT {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            PermissionsFragment permissionsFragment = new PermissionsFragment();
            permissionsFragment.setArguments(bundle);
            fragmentTransaction.add(R.id.permissions_fragment_container, permissionsFragment, MainActivity.TAG_PERMISSIONS_FRAGMENT);
            fragmentTransaction.commit();
        }
    },

    REQUEST_PERMISSIONS {
        @Override
        public void apply(MainActivity activity, Bundle bundle) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(MainActivity.TAG_PERMISSIONS_FRAGMENT);
            if(fragment instanceof PermissionsFragment && bundle != null){
                String[] permissions = bundle.getStringArray(PermissionsFragment.KEY_USE_PERMISSIONS);
                ((PermissionsFragment) fragment).requestPermissions(permissions);
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
