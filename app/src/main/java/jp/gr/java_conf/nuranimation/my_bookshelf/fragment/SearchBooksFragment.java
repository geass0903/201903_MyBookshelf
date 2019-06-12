package jp.gr.java_conf.nuranimation.my_bookshelf.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.PatternSyntaxException;

import jp.gr.java_conf.nuranimation.my_bookshelf.MainActivity;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.background.SearchBooksThread;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseProgressDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.adapter.BooksListViewAdapter;
import jp.gr.java_conf.nuranimation.my_bookshelf.background.BookService;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BundleBuilder;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfUtils;

public class SearchBooksFragment extends BaseFragment implements BooksListViewAdapter.OnBookClickListener{
    public static final String TAG = SearchBooksFragment.class.getSimpleName();
    private static final boolean D = true;

    private static final String KEY_LAYOUT_MANAGER = "KEY_LAYOUT_MANAGER";
    private static final String KEY_SEARCH_STATE = "KEY_SEARCH_STATE";
    private static final String KEY_TEMP_KEYWORD = "KEY_TEMP_KEYWORD";
    private static final String KEY_KEYWORD = "KEY_KEYWORD";
    private static final String KEY_PAGE = "KEY_PAGE";
    private static final String KEY_HAS_RESULT_DATA = "KEY_HAS_RESULT_DATA";
    private static final String KEY_HAS_BUTTON_LOAD_NEXT = "KEY_HAS_BUTTON_LOAD_NEXT";
    private static final String KEY_POSITION = "KEY_POSITION";
    private static final String KEY_BOOK_DATA = "KEY_BOOK_DATA";

    private MyBookshelfApplicationData mApplicationData;
    private BooksListViewAdapter mSearchBooksViewAdapter;
    private SearchView mSearchView;
    private LinearLayoutManager mLayoutManager;

    private List<BookData> mSearchBooks;
    private String mTempKeyword;
    private String mKeyword;
    private int mSearchPage = 1;
    private int mSearchState = 0;
    private boolean hasResultData = false;
    private boolean hasButtonLoadNext = false;


    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mApplicationData = (MyBookshelfApplicationData) context.getApplicationContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }


    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        if(D) Log.d(TAG, "onViewCreated");
        if(getActivity() != null) {
            getActivity().setTitle(R.string.Navigation_Item_SearchBooks);
        }
        mLayoutManager = new LinearLayoutManager(view.getContext());

        if(savedInstanceState != null) {
            if (D) Log.d(TAG, "savedInstanceState != null");
            Parcelable mListState = savedInstanceState.getParcelable(KEY_LAYOUT_MANAGER);
            if(mListState != null){
                mLayoutManager.onRestoreInstanceState(mListState);
            }
            mSearchBooks = loadSearchBooksData(savedInstanceState);
        }else{
            if(mSearchBooks == null) {
                mSearchBooks = loadSearchBooksData(null);
            }
        }
        mSearchBooksViewAdapter = new BooksListViewAdapter(getContext(), mSearchBooks, BooksListViewAdapter.LIST_TYPE_SEARCH_BOOKS);
        mSearchBooksViewAdapter.setClickListener(this);
        RecyclerView mRecyclerView = view.findViewById(R.id.fragment_search_recyclerview);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mSearchBooksViewAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (D) Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.d(TAG,"onResume()");
        checkSearchState();
    }


    @Override
    public void onPause() {
        super.onPause();
        if(D) Log.d(TAG,"onPause()");
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(D) Log.d(TAG,"onSaveInstanceState");
        outState.putInt(KEY_SEARCH_STATE, mSearchState);
        outState.putString(KEY_TEMP_KEYWORD, mTempKeyword);
        outState.putString(KEY_KEYWORD, mKeyword);
        outState.putInt(KEY_PAGE, mSearchPage);
        outState.putBoolean(KEY_HAS_RESULT_DATA, hasResultData);
        outState.putBoolean(KEY_HAS_BUTTON_LOAD_NEXT, hasButtonLoadNext);
        outState.putParcelable(KEY_LAYOUT_MANAGER,mLayoutManager.onSaveInstanceState());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search,menu);
        initSearchView(menu);
    }


    @Override
    public void onBookClick(BooksListViewAdapter adapter, int position, BookData data) {
        if(isClickable()) {
            setClickDisable();
            int view_type = adapter.getItemViewType(position);
            if (view_type == BooksListViewAdapter.VIEW_TYPE_BOOK) {
                if (getFragmentListener() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(BookDetailFragment.KEY_BUNDLE_POSITION, position);
                    BookData book = mApplicationData.loadBookDataFromShelfBooks(data);
                    if(book == null){
                        book = new BookData(data);
                        book.setRating("0.0");
                        book.setReadStatus("5");
                    }
                    bundle.putParcelable(BookDetailFragment.KEY_BUNDLE_BOOK, book);
                    getFragmentListener().onFragmentEvent(MyBookshelfEvent.GO_TO_BOOK_DETAIL, bundle);
                }
            } else {
                if (view_type == BooksListViewAdapter.VIEW_TYPE_BUTTON_LOAD) {
                    searchBooks(mKeyword, mSearchPage);
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
                Bundle bundle;
                Bundle bundle_book = new Bundle();
                bundle_book.putInt(KEY_POSITION, position);
                BookData book = new BookData(data);
                bundle_book.putParcelable(KEY_BOOK_DATA, book);

                if (mApplicationData.loadBookDataFromShelfBooks(data) == null){
                    // unregistered. register Dialog
                    bundle = new BundleBuilder()
                            .put(BaseDialogFragment.KEY_TITLE, getString(R.string.DialogTitle_Register_Book))
                            .put(BaseDialogFragment.KEY_MESSAGE, getString(R.string.DialogMessage_Register_Book))
                            .put(BaseDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.DialogButton_Label_Positive))
                            .put(BaseDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.DialogButton_Label_Negative))
                            .put(BaseDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_REGISTER_BOOK)
                            .put(BaseDialogFragment.KEY_PARAMS, bundle_book)
                            .put(BaseDialogFragment.KEY_CANCELABLE, true)
                            .build();
                } else {
                    // registered. delete Dialog
                    bundle = new BundleBuilder()
                            .put(BaseDialogFragment.KEY_TITLE, getString(R.string.DialogTitle_Unregister_Book))
                            .put(BaseDialogFragment.KEY_MESSAGE, getString(R.string.DialogMessage_Unregister_Book))
                            .put(BaseDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.DialogButton_Label_Positive))
                            .put(BaseDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.DialogButton_Label_Negative))
                            .put(BaseDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_UNREGISTER_BOOK)
                            .put(BaseDialogFragment.KEY_PARAMS, bundle_book)
                            .put(BaseDialogFragment.KEY_CANCELABLE, true)
                            .build();
                }
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
        if (resultCode == DialogInterface.BUTTON_POSITIVE && params != null) {
            switch (requestCode) {
                case REQUEST_CODE_REGISTER_BOOK:
                    int position_register = params.getInt(KEY_POSITION, -1);
                    BookData book_register = params.getParcelable(KEY_BOOK_DATA);
                    if (book_register != null) {
                        BookData book = new BookData(book_register);
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);
                        String registerDate = sdf.format(calendar.getTime());
                        book.setRegisterDate(registerDate);
                        book.setRating("0.0");
                        book.setReadStatus("5");
                        mApplicationData.registerToShelfBooks(book);
                        mSearchBooksViewAdapter.updateBook(position_register);
                        Toast.makeText(getContext(), getString(R.string.Toast_Register_Book), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_CODE_UNREGISTER_BOOK:
                    int position_unregister = params.getInt(KEY_POSITION, -1);
                    BookData book_unregister = params.getParcelable(KEY_BOOK_DATA);
                    if (book_unregister != null) {
                        mApplicationData.unregisterFromShelfBooks(book_unregister);
                        mSearchBooksViewAdapter.updateBook(position_unregister);
                        Toast.makeText(getContext(), getString(R.string.Toast_Unregister_Book), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {
        super.onBaseDialogCancelled(requestCode,params);
    }

    @Override
    public void onReceiveBroadcast(Context context, Intent intent){
        if (D) Log.d(TAG, "onReceive");
        String action = intent.getAction();
        if(action != null){
            switch (action){
                case FILTER_ACTION_UPDATE_SERVICE_STATE:
                    int state = intent.getIntExtra(KEY_UPDATE_SERVICE_STATE, 0);
                    switch (state) {
                        case BookService.STATE_NONE:
                            if (D) Log.d(TAG, "STATE_NONE");
                            mSearchState = BookService.STATE_NONE;
                            getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_DISMISS).sendToTarget();
                            break;
                        case BookService.STATE_SEARCH_BOOKS_SEARCH_START:
                            if (D) Log.d(TAG, "STATE_SEARCH_BOOKS_SEARCH_START");
                            break;
                        case BookService.STATE_SEARCH_BOOKS_SEARCH_FINISH:
                            if (D) Log.d(TAG, "STATE_SEARCH_BOOKS_SEARCH_FINISH");
                            loadSearchBooksResult();
                            break;
                    }
                    break;
            }
        }
    }


    private void initSearchView(Menu menu){
        mSearchView = (SearchView)menu.findItem(R.id.menu_search_search_view).getActionView();
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setQueryHint(getString(R.string.InputHint_Search));
        mSearchView.setQuery(mTempKeyword,false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchWord) {
                mSearchView.clearFocus();
                mKeyword = searchWord;
                mSearchPage = 1;
                searchBooks(mKeyword, mSearchPage);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String word) {
                mTempKeyword = word;
                return false;
            }
        });
    }

    private List<BookData> loadSearchBooksData(@Nullable Bundle savedInstanceState) {
        List<BookData> books = new ArrayList<>();
        if (savedInstanceState == null) {
            if (getArguments() != null) {
                mSearchState = getArguments().getInt(BookService.KEY_SERVICE_STATE, 0);
                mSearchPage = getArguments().getInt(BookService.KEY_PARAM_SEARCH_PAGE, 1);
                mTempKeyword = getArguments().getString(BookService.KEY_PARAM_SEARCH_KEYWORD, null);
                mKeyword = mTempKeyword;
            }
            switch (mSearchState) {
                case BookService.STATE_NONE:
                    break;
                case BookService.STATE_SEARCH_BOOKS_SEARCH_START:
                case BookService.STATE_SEARCH_BOOKS_SEARCH_FINISH:
                    if (mSearchPage > 1) {
                        books = mApplicationData.loadSearchBooks();
                        BookData footer = new BookData();
                        footer.setView_type(BooksListViewAdapter.VIEW_TYPE_LOADING);
                        books.add(footer);
                    } else if (mSearchPage == 1) {
                        Bundle progress = new BundleBuilder()
                                .put(BaseProgressDialogFragment.title, getString(R.string.ProgressTitle_Search))
                                .put(BaseProgressDialogFragment.message, "")
                                .build();
                        setProgressBundle(progress);
                    }
                    break;
            }
        } else {
            mSearchState = savedInstanceState.getInt(KEY_SEARCH_STATE, 0);
            mTempKeyword = savedInstanceState.getString(KEY_TEMP_KEYWORD, null);
            mKeyword = savedInstanceState.getString(KEY_KEYWORD, null);
            mSearchPage = savedInstanceState.getInt(KEY_PAGE, 1);
            hasResultData = savedInstanceState.getBoolean(KEY_HAS_RESULT_DATA, false);
            hasButtonLoadNext = savedInstanceState.getBoolean(KEY_HAS_BUTTON_LOAD_NEXT, false);

            if(hasResultData) {
                books = mApplicationData.loadSearchBooks();
            }
            switch (mSearchState) {
                case BookService.STATE_NONE:
                    if (hasResultData && hasButtonLoadNext) {
                        BookData footer = new BookData();
                        footer.setView_type(BooksListViewAdapter.VIEW_TYPE_BUTTON_LOAD);
                        books.add(footer);
                    }
                    break;
                case BookService.STATE_SEARCH_BOOKS_SEARCH_START:
                case BookService.STATE_SEARCH_BOOKS_SEARCH_FINISH:
                    if (hasResultData && mSearchPage > 1) {
                        BookData footer = new BookData();
                        footer.setView_type(BooksListViewAdapter.VIEW_TYPE_LOADING);
                        books.add(footer);
                    } else if (mSearchPage == 1) {
                        Bundle progress = new BundleBuilder()
                                .put(BaseProgressDialogFragment.title, getString(R.string.ProgressTitle_Search))
                                .put(BaseProgressDialogFragment.message, "")
                                .build();
                        setProgressBundle(progress);
                    }
                    break;
            }
        }
        return books;
    }

    public void checkSearchState(){
        if(getActivity() instanceof MainActivity){
            BookService service = ((MainActivity) getActivity()).getService();
            if(service != null && service.getServiceState() == BookService.STATE_SEARCH_BOOKS_SEARCH_FINISH){
                mSearchState = BookService.STATE_SEARCH_BOOKS_SEARCH_FINISH;
                loadSearchBooksResult();
            }
        }
    }

    public void prepareSearch(){
        mTempKeyword = null;
        mSearchView.setQuery(null,false);
        mSearchView.setIconified(false);
    }

    public void searchBooks(String keyword, int page) {
        try{
            if(!MyBookshelfUtils.isValid(keyword)){
                Toast.makeText(getContext(), getString(R.string.Toast_Search_Error_Keyword), Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (PatternSyntaxException e){
            if(D) Log.d(TAG,"PatternSyntaxException");
            Toast.makeText(getContext(), getString(R.string.Toast_Search_Error_Keyword), Toast.LENGTH_SHORT).show();
            return;
        }

        if (getActivity() instanceof MainActivity) {
            BookService service = ((MainActivity) getActivity()).getService();
            if (service == null) {
                return;
            }
            if (page == 1) {
                mApplicationData.dropTableSearchBooks();
                mSearchBooksViewAdapter.clearBooksData();
                hasResultData = false;
                hasButtonLoadNext = false;
                Bundle progress = new BundleBuilder()
                        .put(BaseProgressDialogFragment.title, getString(R.string.ProgressTitle_Search))
                        .put(BaseProgressDialogFragment.message, "")
                        .build();
                setProgressBundle(progress);
                showProgressDialog();
            } else {
                BookData footer = new BookData();
                footer.setView_type(BooksListViewAdapter.VIEW_TYPE_LOADING);
                mSearchBooksViewAdapter.setFooter(footer);
            }
            mSearchState = BookService.STATE_SEARCH_BOOKS_SEARCH_START;
            service.searchBooks(keyword, page);
        }
    }

    public void loadSearchBooksResult() {
        if (D) Log.d(TAG, "loadSearchBooksResult");
        mSearchBooksViewAdapter.setFooter(null);
        if (getActivity() instanceof MainActivity) {
            BookService service = ((MainActivity) getActivity()).getService();
            if (service != null) {
                SearchBooksThread.Result result = service.loadSearchBooksResult();
                if (result.isSuccess()) {
                    hasButtonLoadNext = result.hasNext();
                    List<BookData> books = result.getBooks();

                    if (books.size() > 0) {
                        if (hasButtonLoadNext) {
                            mSearchPage++;
                            BookData footer = new BookData();
                            footer.setView_type(BooksListViewAdapter.VIEW_TYPE_BUTTON_LOAD);
                            books.add(footer);
                        }
                        mSearchBooksViewAdapter.addBooksData(books);
                        hasResultData = true;
                    } else {
                        Toast.makeText(getContext(), getString(R.string.Toast_Search_Error_Book_not_found), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
                service.setServiceState(BookService.STATE_NONE);
                service.stopForeground(false);
                service.stopSelf();
            }
        }
        mSearchState = BookService.STATE_NONE;
        getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_DISMISS).sendToTarget();
    }


}
