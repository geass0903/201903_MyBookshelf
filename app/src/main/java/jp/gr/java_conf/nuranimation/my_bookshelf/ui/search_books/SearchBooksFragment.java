package jp.gr.java_conf.nuranimation.my_bookshelf.ui.search_books;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.database.MyBookshelfDBOpenHelper;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.Result;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.BookDataUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.CalendarUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.service.BookService;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.MyBookshelfEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.book_detail.BookDetailFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.NormalDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.ProgressDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.util.BooksRecyclerViewAdapter;


public class SearchBooksFragment extends BaseFragment implements BooksRecyclerViewAdapter.OnBookClickListener, NormalDialogFragment.OnNormalDialogListener, ProgressDialogFragment.OnProgressDialogListener{
    private static final String TAG = SearchBooksFragment.class.getSimpleName();
    private static final boolean D = true;

    public static final String KEY_PARAM_SEARCH_KEYWORD     = "SearchBooksFragment.KEY_PARAM_SEARCH_KEYWORD";
    public static final String KEY_PARAM_SEARCH_PAGE        = "SearchBooksFragment.KEY_PARAM_SEARCH_PAGE";

    private static final String KEY_LAYOUT_MANAGER          = "SearchBooksFragment.KEY_LAYOUT_MANAGER";
    private static final String KEY_TEMP_KEYWORD            = "SearchBooksFragment.KEY_TEMP_KEYWORD";
    private static final String KEY_HAS_RESULT_DATA         = "SearchBooksFragment.KEY_HAS_RESULT_DATA";
    private static final String KEY_HAS_BUTTON_LOAD_NEXT    = "SearchBooksFragment.KEY_HAS_BUTTON_LOAD_NEXT";
    private static final String KEY_POSITION                = "SearchBooksFragment.KEY_POSITION";
    private static final String KEY_BOOK_DATA               = "SearchBooksFragment.KEY_BOOK_DATA";

    private static final String TAG_REGISTER_BOOK           = "SearchBooksFragment.TAG_REGISTER_BOOKS";
    private static final String TAG_UNREGISTER_BOOK         = "SearchBooksFragment.TAG_UNREGISTER_BOOKS";
    private static final String TAG_SEARCH_PROGRESS_DIALOG  = "SearchBooksFragment.TAG_SEARCH_PROGRESS_DIALOG";

    private static final int REQUEST_CODE_REGISTER_BOOK             = 1;
    private static final int REQUEST_CODE_UNREGISTER_BOOK           = 2;
    private static final int REQUEST_CODE_SEARCH_PROGRESS_DIALOG    = 3;

    private MyBookshelfDBOpenHelper mDBOpenHelper;
    private BooksRecyclerViewAdapter mSearchBooksViewAdapter;
    private SearchView mSearchView;
    private LinearLayoutManager mLayoutManager;
    private List<BookData> mSearchBooks;
    private String mTempKeyword;
    private String mKeyword;
    private int mSearchPage = 1;
    private boolean hasResultData = false;
    private boolean hasButtonLoadNext = false;


    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mDBOpenHelper = MyBookshelfDBOpenHelper.getInstance(context.getApplicationContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_recyclerview, container, false);
    }


    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        if(D) Log.d(TAG, "onViewCreated");
        if(getActivity() != null) {
            getActivity().setTitle(R.string.navigation_item_search_books);
        }
        mLayoutManager = new LinearLayoutManager(view.getContext());
        if(savedInstanceState != null) {
            Parcelable mListState = savedInstanceState.getParcelable(KEY_LAYOUT_MANAGER);
            if(mListState != null){
                mLayoutManager.onRestoreInstanceState(mListState);
            }

            mKeyword = savedInstanceState.getString(KEY_PARAM_SEARCH_KEYWORD, null);
            mSearchPage = savedInstanceState.getInt(KEY_PARAM_SEARCH_PAGE, 1);
            mTempKeyword = savedInstanceState.getString(KEY_TEMP_KEYWORD, mKeyword);
            hasResultData = savedInstanceState.getBoolean(KEY_HAS_RESULT_DATA, false);
            hasButtonLoadNext = savedInstanceState.getBoolean(KEY_HAS_BUTTON_LOAD_NEXT, false);
            mSearchBooks = loadSearchBooks();
        }else{
            if(mSearchBooks == null) {
                mSearchBooks = loadSearchBooks();
            }
        }
        mSearchBooksViewAdapter = new BooksRecyclerViewAdapter(getContext(), mSearchBooks, BooksRecyclerViewAdapter.LIST_TYPE_SEARCH_BOOKS);
        mSearchBooksViewAdapter.setClickListener(this);
        RecyclerView mRecyclerView = view.findViewById(R.id.view_recyclerview);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        if(D) Log.d(TAG,"onPause()");
        getFragmentListener().onFragmentEvent(MyBookshelfEvent.CANCEL_SEARCH_BOOKS, null);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(D) Log.d(TAG,"onSaveInstanceState");
        outState.putString(KEY_PARAM_SEARCH_KEYWORD, mKeyword);
        outState.putInt(KEY_PARAM_SEARCH_PAGE, mSearchPage);
        outState.putString(KEY_TEMP_KEYWORD, mTempKeyword);
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
    public void onBookClick(BooksRecyclerViewAdapter adapter, int position, BookData data) {
        if(isClickable()) {
            waitClickable(500);
            int view_type = adapter.getItemViewType(position);
            if (view_type == BookData.TYPE_BOOK) {
                if (getFragmentListener() != null) {
                    Bundle bundle = new Bundle();
                    BookData book = mDBOpenHelper.loadBookDataFromShelfBooks(data);
                    if(book.getView_type() == BookData.TYPE_EMPTY){
                        book = new BookData(data);
                        book.setRating(BookDataUtils.convertRating(0.0f));
                        book.setReadStatus(BookData.STATUS_NONE);
                    }
                    bundle.putParcelable(BookDetailFragment.KEY_BOOK_DATA, book);
                    getFragmentListener().onFragmentEvent(MyBookshelfEvent.GO_TO_BOOK_DETAIL, bundle);
                }
            } else {
                if (view_type == BookData.TYPE_BUTTON_LOAD) {
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_PARAM_SEARCH_KEYWORD, mKeyword);
                    bundle.putInt(KEY_PARAM_SEARCH_PAGE, mSearchPage);
                    getFragmentListener().onFragmentEvent(MyBookshelfEvent.START_SEARCH_BOOKS,bundle);
                }
            }
        }
    }

    @Override
    public void onBookLongClick(BooksRecyclerViewAdapter adapter, int position, BookData data) {
        if (isClickable()) {
            waitClickable(500);
            int view_type = adapter.getItemViewType(position);
            String tag;
            if (view_type == BookData.TYPE_BOOK) {
                Bundle bundle = new Bundle();
                Bundle bundle_book = new Bundle();
                bundle_book.putInt(KEY_POSITION, position);
                BookData book = new BookData(data);
                bundle_book.putParcelable(KEY_BOOK_DATA, book);
                BookData registered = mDBOpenHelper.loadBookDataFromShelfBooks(data);
                if(registered.getView_type() == BookData.TYPE_BOOK){
                    // unregistered. register Dialog
                    bundle.putString(NormalDialogFragment.KEY_TITLE, getString(R.string.dialog_title_register_book));
                    bundle.putString(NormalDialogFragment.KEY_MESSAGE, getString(R.string.dialog_message_register_book));
                    bundle.putString(NormalDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
                    bundle.putString(NormalDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
                    bundle.putInt(NormalDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_REGISTER_BOOK);
                    bundle.putBundle(NormalDialogFragment.KEY_PARAMS, bundle_book);
                    bundle.putBoolean(NormalDialogFragment.KEY_CANCELABLE, true);
                    tag = TAG_REGISTER_BOOK;
                } else {
                    // registered. delete Dialog
                    bundle.putString(NormalDialogFragment.KEY_TITLE, getString(R.string.dialog_title_unregister_book));
                    bundle.putString(NormalDialogFragment.KEY_MESSAGE, getString(R.string.dialog_message_unregister_book));
                    bundle.putString(NormalDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
                    bundle.putString(NormalDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
                    bundle.putInt(NormalDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_UNREGISTER_BOOK);
                    bundle.putBundle(NormalDialogFragment.KEY_PARAMS, bundle_book);
                    bundle.putBoolean(NormalDialogFragment.KEY_CANCELABLE, true);
                    tag = TAG_UNREGISTER_BOOK;
                }
                NormalDialogFragment.showNormalDialog(this, bundle, tag);
            }
        }
    }

    @Override
    public void onNormalDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        if (resultCode == DialogInterface.BUTTON_POSITIVE && params != null) {
            switch (requestCode) {
                case REQUEST_CODE_REGISTER_BOOK:
                    int position_register = params.getInt(KEY_POSITION, -1);
                    BookData book_register = params.getParcelable(KEY_BOOK_DATA);
                    if (book_register != null) {
                        BookData book = new BookData(book_register);
                        Calendar calendar = Calendar.getInstance();
                        String registerDate = CalendarUtils.parseCalendar(calendar);
                        book.setRegisterDate(registerDate);
                        book.setRating(BookDataUtils.convertRating(0.0f));
                        book.setReadStatus(BookData.STATUS_NONE);
                        mDBOpenHelper.registerToShelfBooks(book);
                        mSearchBooksViewAdapter.refreshBook(position_register);
                        Toast.makeText(getContext(), getString(R.string.toast_success_register), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_CODE_UNREGISTER_BOOK:
                    int position_unregister = params.getInt(KEY_POSITION, -1);
                    BookData book_unregister = params.getParcelable(KEY_BOOK_DATA);
                    if (book_unregister != null) {
                        mDBOpenHelper.unregisterFromShelfBooks(book_unregister);
                        mSearchBooksViewAdapter.refreshBook(position_unregister);
                        Toast.makeText(getContext(), getString(R.string.toast_success_unregister), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    @Override
    public void onNormalDialogCancelled(int requestCode, Bundle params) {
    }

    @Override
    public void onProgressDialogCancelled(int requestCode, Bundle params) {
        if (requestCode == REQUEST_CODE_SEARCH_PROGRESS_DIALOG) {
            getFragmentListener().onFragmentEvent(MyBookshelfEvent.CANCEL_SEARCH_BOOKS, null);
        }
    }

    @Override
    public void onReceiveLocalBroadcast(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(BookService.FILTER_ACTION_UPDATE_SERVICE_STATE)) {
            int state = intent.getIntExtra(BookService.KEY_SERVICE_STATE, BookService.STATE_NONE);
            switch (state) {
                case BookService.STATE_NONE:
                    if (D) Log.d(TAG, "STATE_NONE");
                    break;
                case BookService.STATE_SEARCH_BOOKS_SEARCH_INCOMPLETE:
                    if (D) Log.d(TAG, "STATE_SEARCH_BOOKS_SEARCH_INCOMPLETE");
                    break;
                case BookService.STATE_SEARCH_BOOKS_SEARCH_COMPLETE:
                    if (D) Log.d(TAG, "STATE_SEARCH_BOOKS_SEARCH_COMPLETE");
                    getFragmentListener().onFragmentEvent(MyBookshelfEvent.FINISH_SEARCH_BOOKS, null);
                    break;
                default:
                    if (D) Log.d(TAG, "IllegalState: " + state);
                    break;
            }
        }
    }

    public void clearSearchView(){
        mTempKeyword = null;
        mSearchView.setQuery(null,false);
        mSearchView.setIconified(false);
    }

    public boolean startSearch(String keyword, int page){
        if (page < 1) {
            if (D) Log.d(TAG, "Illegal Parameter search page");
            return false;
        }
        try{
            if(!isSearchable(keyword)){
                Toast.makeText(getContext(), getString(R.string.toast_failed_search_keyword_error), Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (PatternSyntaxException e){
            if(D) Log.d(TAG,"PatternSyntaxException");
            Toast.makeText(getContext(), getString(R.string.toast_failed_search_keyword_error), Toast.LENGTH_SHORT).show();
            return false;
        }


        if(page == 1){
            mDBOpenHelper.clearSearchBooks();
            mSearchBooksViewAdapter.clearBooksData();
            hasResultData = false;
            hasButtonLoadNext = false;
            Bundle bundle = new Bundle();
            bundle.putInt(ProgressDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_SEARCH_PROGRESS_DIALOG);
            bundle.putString(ProgressDialogFragment.KEY_TITLE, getString(R.string.progress_title_search_books));
            bundle.putBoolean(ProgressDialogFragment.KEY_CANCELABLE, false);
            ProgressDialogFragment.showProgressDialog(this, bundle, TAG_SEARCH_PROGRESS_DIALOG);
        }else{ // page > 1
            BookData footer = new BookData();
            footer.setView_type(BookData.TYPE_VIEW_LOADING);
            mSearchBooksViewAdapter.setFooter(footer);
        }
        return true;
    }



    public void finishSearch(Result result){
        if (D) Log.d(TAG, "loadSearchResult");
        mSearchBooksViewAdapter.setFooter(null);
        if (result.isSuccess()) {
            hasButtonLoadNext = result.hasNext();
            List<BookData> books = result.getBooks();
            if (books != null && books.size() > 0) {
                if (hasButtonLoadNext) {
                    mSearchPage++;
                    BookData footer = new BookData();
                    footer.setView_type(BookData.TYPE_BUTTON_LOAD);
                    books.add(footer);
                }
                mSearchBooksViewAdapter.addBooksData(books);
                hasResultData = true;
            } else {
                Toast.makeText(getContext(), getString(R.string.toast_failed_search_book_not_found), Toast.LENGTH_SHORT).show();
            }
        } else {
            if(D) Log.d(TAG,"ErrorCode: " + result.getErrorCode());
            Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_SHORT).show();
            if(mSearchPage > 1){
                BookData footer = new BookData();
                footer.setView_type(BookData.TYPE_BUTTON_LOAD);
                mSearchBooksViewAdapter.setFooter(footer);
            }
        }
        ProgressDialogFragment.dismissProgressDialog(this, TAG_SEARCH_PROGRESS_DIALOG);
    }

    public void cancelSearch(){
        if(hasButtonLoadNext){
            BookData footer = new BookData();
            footer.setView_type(BookData.TYPE_BUTTON_LOAD);
            mSearchBooksViewAdapter.setFooter(footer);
        }
        ProgressDialogFragment.dismissProgressDialog(this, TAG_SEARCH_PROGRESS_DIALOG);
    }

    private List<BookData> loadSearchBooks(){
        List<BookData> books = new ArrayList<>();
        if(hasResultData){
            books = mDBOpenHelper.loadSearchBooks();
        }
        if(hasButtonLoadNext){
            BookData footer = new BookData();
            footer.setView_type(BookData.TYPE_BUTTON_LOAD);
            books.add(footer);
        }
        return books;
    }

    private void initSearchView(Menu menu){
        mSearchView = (SearchView)menu.findItem(R.id.menu_search_search_view).getActionView();
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setQueryHint(getString(R.string.input_hint_search_books));
        mSearchView.setQuery(mTempKeyword,false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchWord) {
                mSearchView.clearFocus();
                mKeyword = searchWord;
                mSearchPage = 1;
                Bundle bundle = new Bundle();
                bundle.putString(KEY_PARAM_SEARCH_KEYWORD, mKeyword);
                bundle.putInt(KEY_PARAM_SEARCH_PAGE, mSearchPage);
                getFragmentListener().onFragmentEvent(MyBookshelfEvent.START_SEARCH_BOOKS,bundle);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String word) {
                mTempKeyword = word;
                return false;
            }
        });
    }

    public static boolean isSearchable(String word) throws PatternSyntaxException {
        if (TextUtils.isEmpty(word)) {
            if (D) Log.d(TAG, "No word");
            return false;
        }
        if (word.length() >= 2) {
            return true;
        }

        int bytes = 0;
        char[] array = word.toCharArray();
        for (char c : array) {
            if (D) Log.d(TAG, "Unicode Block: " + Character.UnicodeBlock.of(c));
            if (String.valueOf(c).getBytes().length <= 1) {
                bytes += 1;
            } else {
                bytes += 2;
            }
        }
        if (bytes <= 1) {
            if (D) Log.d(TAG, "1 half width character. NG");
            return false;
        }
        String regex_InHIRAGANA = "\\p{InHIRAGANA}";
        String regex_InKATAKANA = "\\p{InKATAKANA}";
        String regex_InHALFWIDTH_AND_FULLWIDTH_FORMS = "\\p{InHALFWIDTH_AND_FULLWIDTH_FORMS}";
        String regex_InCJK_SYMBOLS_AND_PUNCTUATION = "\\p{InCJK_SYMBOLS_AND_PUNCTUATION}";


        if (word.matches(regex_InHIRAGANA)) {
            if (D) Log.d(TAG, "1 character in HIRAGANA");
            return false;
        }
        if (word.matches(regex_InKATAKANA)) {
            if (D) Log.d(TAG, "1 character in KATAKANA");
            return false;
        }
        if (word.matches(regex_InHALFWIDTH_AND_FULLWIDTH_FORMS)) {
            if (D) Log.d(TAG, "1 character in HALFWIDTH_AND_FULLWIDTH_FORMS");
            return false;
        }
        if (word.matches(regex_InCJK_SYMBOLS_AND_PUNCTUATION)) {
            if (D) Log.d(TAG, "1 character in CJK_SYMBOLS_AND_PUNCTUATION");
            return false;
        }
        return true;
    }


}
