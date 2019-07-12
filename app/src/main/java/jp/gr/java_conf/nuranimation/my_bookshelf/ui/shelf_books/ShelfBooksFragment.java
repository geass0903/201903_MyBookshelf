package jp.gr.java_conf.nuranimation.my_bookshelf.ui.shelf_books;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.database.MyBookshelfDBOpenHelper;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.prefs.MyBookshelfPreferences;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.BooksOrder;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.MyBookshelfEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.book_detail.BookDetailFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.NormalDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.util.BooksRecyclerViewAdapter;


public class ShelfBooksFragment extends BaseFragment implements BooksRecyclerViewAdapter.OnBookClickListener, NormalDialogFragment.OnNormalDialogListener {
    private static final String TAG = ShelfBooksFragment.class.getSimpleName();
    private static final boolean D = true;

    private static final String TAG_UNREGISTER_BOOK = "ShelfBooksFragment.TAG_UNREGISTER_BOOKS";
    private static final int REQUEST_CODE_UNREGISTER_BOOK = 1;

    private static final String KEY_LAYOUT_MANAGER = "ShelfBooksFragment.KEY_LAYOUT_MANAGER";
    private static final String KEY_KEYWORD = "ShelfBooksFragment.KEY_KEYWORD";
    private static final String KEY_POSITION = "ShelfBooksFragment.KEY_POSITION";
    private static final String KEY_BOOK_DATA = "ShelfBooksFragment.KEY_BOOK_DATA";

    private MyBookshelfDBOpenHelper mDBOpenHelper;
    private MyBookshelfPreferences mPreferences;
    private BooksRecyclerViewAdapter mShelfBooksViewAdapter;
    private SearchView mSearchView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private List<BookData> mShelfBooks;
    private String mKeyword;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mDBOpenHelper = MyBookshelfDBOpenHelper.getInstance(context.getApplicationContext());
        mPreferences = new MyBookshelfPreferences(context.getApplicationContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (D) Log.d(TAG, "onViewCreated");
        if (getActivity() != null) {
            getActivity().setTitle(R.string.navigation_item_shelf_books);
        }
        mLayoutManager = new LinearLayoutManager(view.getContext());
        if (savedInstanceState != null) {
            if (D) Log.d(TAG, "savedInstanceState != null");
            mKeyword = savedInstanceState.getString(KEY_KEYWORD, null);
            Parcelable mListState = savedInstanceState.getParcelable(KEY_LAYOUT_MANAGER);
            if (mListState != null) {
                mLayoutManager.onRestoreInstanceState(mListState);
            }
        }
        if (mShelfBooks == null) {
            mShelfBooks = mDBOpenHelper.loadShelfBooks(mKeyword, BooksOrder.getShelfBooksOrder(mPreferences.getShelfBooksOrderCode()));
        }
        mShelfBooksViewAdapter = new BooksRecyclerViewAdapter(getContext(), mShelfBooks, BooksRecyclerViewAdapter.LIST_TYPE_SHELF_BOOKS);
        mShelfBooksViewAdapter.setClickListener(this);
        mRecyclerView = view.findViewById(R.id.view_recyclerview);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mShelfBooksViewAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (D) Log.d(TAG, "onResume()");
    }


    @Override
    public void onPause() {
        super.onPause();
        if (D) Log.d(TAG, "onPause()");
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_KEYWORD, mKeyword);
        outState.putParcelable(KEY_LAYOUT_MANAGER, mLayoutManager.onSaveInstanceState());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_shelf, menu);
        initSearchView(menu);
    }


    @Override
    public void onBookClick(BooksRecyclerViewAdapter adapter, int position, BookData data) {
        if (isClickable()) {
            waitClickable(500);
            int view_type = adapter.getItemViewType(position);
            if (view_type == BookData.TYPE_BOOK) {
                if (getFragmentListener() != null) {
                    Bundle bundle = new Bundle();
                    BookData book = mDBOpenHelper.loadBookDataFromShelfBooks(data);
                    if (book.getView_type() == BookData.TYPE_EMPTY) {
                        book = new BookData(data);
                    }
                    bundle.putParcelable(BookDetailFragment.KEY_BOOK_DATA, book);
                    getFragmentListener().onFragmentEvent(MyBookshelfEvent.GO_TO_BOOK_DETAIL, bundle);
                }
            }
        }
    }

    @Override
    public void onBookLongClick(BooksRecyclerViewAdapter adapter, int position, BookData data) {
        if (isClickable()) {
            waitClickable(500);
            int view_type = adapter.getItemViewType(position);
            if (view_type == BookData.TYPE_BOOK) {
                Bundle bundle_book = new Bundle();
                bundle_book.putInt(KEY_POSITION, position);
                BookData book = new BookData(data);
                bundle_book.putParcelable(KEY_BOOK_DATA, book);
                Bundle bundle = new Bundle();
                bundle.putString(NormalDialogFragment.KEY_TITLE, getString(R.string.dialog_title_unregister_book));
                bundle.putString(NormalDialogFragment.KEY_MESSAGE, getString(R.string.dialog_message_unregister_book));
                bundle.putString(NormalDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
                bundle.putString(NormalDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
                bundle.putInt(NormalDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_UNREGISTER_BOOK);
                bundle.putBundle(NormalDialogFragment.KEY_PARAMS, bundle_book);
                bundle.putBoolean(NormalDialogFragment.KEY_CANCELABLE, true);
                NormalDialogFragment.showNormalDialog(this, bundle, TAG_UNREGISTER_BOOK);
            }
        }
    }

    @Override
    public void onNormalDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        if (requestCode == REQUEST_CODE_UNREGISTER_BOOK && resultCode == DialogInterface.BUTTON_POSITIVE && params != null) {
            int position = params.getInt(KEY_POSITION, -1);
            BookData book = params.getParcelable(KEY_BOOK_DATA);
            if (book != null) {
                mDBOpenHelper.unregisterFromShelfBooks(book);
                mShelfBooksViewAdapter.deleteBook(position);
                Toast.makeText(getContext(), getString(R.string.toast_success_unregister), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onNormalDialogCancelled(int requestCode, Bundle params) {
    }

    private void initSearchView(Menu menu) {
        mSearchView = (SearchView) menu.findItem(R.id.menu_shelf_search_view).getActionView();
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setQueryHint(getString(R.string.input_hint_search_books));
        mSearchView.setQuery(mKeyword, false);
        if (!TextUtils.isEmpty(mKeyword)) {
            mSearchView.setIconified(false);
            mSearchView.clearFocus();
        }
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchWord) {
                mSearchView.clearFocus();
                searchBooksInShelf(searchWord);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String word) {
                if (D) Log.d(TAG, "QueryTextChange: " + word);
                mKeyword = word;
                searchBooksInShelf(word);
                return false;
            }
        });
    }

    public void scrollToTop() {
        mRecyclerView.scrollToPosition(0);
    }

    private void searchBooksInShelf(String keyword) {
        scrollToTop();
        List<BookData> books = mDBOpenHelper.loadShelfBooks(keyword, BooksOrder.getShelfBooksOrder(mPreferences.getShelfBooksOrderCode()));
        mShelfBooksViewAdapter.replaceBooksData(books);
    }

}
