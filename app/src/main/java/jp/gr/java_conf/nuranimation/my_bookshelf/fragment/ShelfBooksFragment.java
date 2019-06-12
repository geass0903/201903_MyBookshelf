package jp.gr.java_conf.nuranimation.my_bookshelf.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
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

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BundleBuilder;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.adapter.BooksListViewAdapter;


public class ShelfBooksFragment extends BaseFragment implements BooksListViewAdapter.OnBookClickListener {
    public static final String TAG = ShelfBooksFragment.class.getSimpleName();
    private static final boolean D = true;

    private static final String KEY_LAYOUT_MANAGER = "KEY_LAYOUT_MANAGER";
    private static final String KEY_KEYWORD = "KEY_KEYWORD";
    private static final String KEY_POSITION = "KEY_POSITION";
    private static final String KEY_BOOK_DATA = "KEY_BOOK_DATA";

    private MyBookshelfApplicationData mApplicationData;
    private BooksListViewAdapter mShelfBooksViewAdapter;
    private SearchView mSearchView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private String mKeyword;
    private List<BookData> mShelfBooks;


    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mApplicationData = (MyBookshelfApplicationData) context.getApplicationContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (D) Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_bookshelf, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (D) Log.d(TAG, "onViewCreated");
        if (getActivity() != null) {
            getActivity().setTitle(R.string.Navigation_Item_ShelfBooks);
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
        if(mShelfBooks == null) {
            mShelfBooks = mApplicationData.loadShelfBooks(mKeyword);
        }
        mShelfBooksViewAdapter = new BooksListViewAdapter(getContext(), mShelfBooks, BooksListViewAdapter.LIST_TYPE_SHELF_BOOKS, true);
        mShelfBooksViewAdapter.setClickListener(this);
        mRecyclerView = view.findViewById(R.id.fragment_shelf_recyclerview);
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
        if(D) Log.d(TAG,"onPause()");
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_KEYWORD, mKeyword);
        outState.putParcelable(KEY_LAYOUT_MANAGER,mLayoutManager.onSaveInstanceState());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_shelf,menu);
        initSearchView(menu);
    }


    @Override
    public void onBookClick(BooksListViewAdapter adapter, int position, BookData data) {
        if(isClickable()){
            setClickDisable();
            int view_type = adapter.getItemViewType(position);
            if (view_type == BooksListViewAdapter.VIEW_TYPE_BOOK) {
                if(getFragmentListener() != null){
                    Bundle bundle = new Bundle();
                    bundle.putInt(BookDetailFragment.KEY_BUNDLE_POSITION, position);
                    BookData book = mApplicationData.loadBookDataFromShelfBooks(data);
                    if(book == null){
                        book = new BookData(data);
                    }
                    bundle.putParcelable(BookDetailFragment.KEY_BUNDLE_BOOK, book);
                    getFragmentListener().onFragmentEvent(MyBookshelfEvent.GO_TO_BOOK_DETAIL,bundle);
                }
           }
        }
    }

    @Override
    public void onBookLongClick(BooksListViewAdapter adapter, int position, BookData data) {
        if (isClickable()) {
            setClickDisable();
            int view_type = adapter.getItemViewType(position);
            if (view_type == BooksListViewAdapter.VIEW_TYPE_BOOK) {
                Bundle bundle_book = new Bundle();
                bundle_book.putInt(KEY_POSITION, position);
                BookData book = new BookData(data);
                bundle_book.putParcelable(KEY_BOOK_DATA, book);
                Bundle bundle = new BundleBuilder()
                        .put(BaseDialogFragment.KEY_TITLE, getString(R.string.DialogTitle_Unregister_Book))
                        .put(BaseDialogFragment.KEY_MESSAGE, getString(R.string.DialogMessage_Unregister_Book))
                        .put(BaseDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.DialogButton_Label_Positive))
                        .put(BaseDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.DialogButton_Label_Negative))
                        .put(BaseDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_UNREGISTER_BOOK)
                        .put(BaseDialogFragment.KEY_PARAMS, bundle_book)
                        .put(BaseDialogFragment.KEY_CANCELABLE, true)
                        .build();
                if (getActivity() != null) {
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    BaseDialogFragment fragment = BaseDialogFragment.newInstance(this, bundle);
                    fragment.show(manager, BaseDialogFragment.TAG);
                }
            }
        }
    }

    @Override
    public void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        super.onBaseDialogSucceeded(requestCode, resultCode, params);
        if (requestCode == REQUEST_CODE_UNREGISTER_BOOK && resultCode == DialogInterface.BUTTON_POSITIVE && params != null) {
            int position = params.getInt(KEY_POSITION, -1);
            BookData book = params.getParcelable(KEY_BOOK_DATA);
            if (book != null) {
                mApplicationData.unregisterFromShelfBooks(book);
                mShelfBooksViewAdapter.deleteBook(position);
                Toast.makeText(getContext(), getString(R.string.Toast_Unregister_Book), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {
        super.onBaseDialogCancelled(requestCode,params);
    }

    private void initSearchView(Menu menu){
        mSearchView = (SearchView)menu.findItem(R.id.menu_shelf_search_view).getActionView();
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setQueryHint(getString(R.string.InputHint_Search));
        mSearchView.setQuery(mKeyword,false);
        if(!TextUtils.isEmpty(mKeyword)){
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
                if(D) Log.d(TAG,"QueryTextChange: " + word);
                mKeyword = word;
                searchBooksInShelf(word);
                return false;
            }
        });
    }

    public void scrollToTop(){
        mRecyclerView.scrollToPosition(0);
    }

    private void searchBooksInShelf(String keyword){
        scrollToTop();
        List<BookData> books = mApplicationData.loadShelfBooks(keyword);
        mShelfBooksViewAdapter.replaceBooksData(books);
    }

}
