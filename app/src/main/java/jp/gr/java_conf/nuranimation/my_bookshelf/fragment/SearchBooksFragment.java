package jp.gr.java_conf.nuranimation.my_bookshelf.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.Slide;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import jp.gr.java_conf.nuranimation.my_bookshelf.book.AsyncSearchBook;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseProgressDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.book.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.book.BooksListViewAdapter;
import jp.gr.java_conf.nuranimation.my_bookshelf.utils.BundleBuilder;
import jp.gr.java_conf.nuranimation.my_bookshelf.utils.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.utils.MyBookshelfUtils;

public class SearchBooksFragment extends BaseFragment implements BooksListViewAdapter.OnBookClickListener{
    public static final String TAG = SearchBooksFragment.class.getSimpleName();
    private static final boolean D = true;

    private static final int LoaderID = 1;

    private static final String KEY_LAYOUT_MANAGER = "KEY_LAYOUT_MANAGER";
    private static final String KEY_TEMP_KEYWORD = "KEY_TEMP_KEYWORD";
    private static final String KEY_KEYWORD = "KEY_KEYWORD";
    private static final String KEY_PAGE = "KEY_PAGE";
    private static final String KEY_HAS_RESULT_DATA = "KEY_HAS_RESULT_DATA";
    private static final String KEY_HAS_BUTTON_LOAD_NEXT = "KEY_HAS_BUTTON_LOAD_NEXT";
    private static final String KEY_POSITION = "KEY_POSITION";
    private static final String KEY_BOOK_DATA = "KEY_BOOK_DATA";

    private MyBookshelfApplicationData mApplicationData;
    private BooksListViewAdapter mSearchBooksViewAdapter;
    private LoaderManager mLoaderManager;
    private SearchView mSearchView;
    private LinearLayoutManager mLayoutManager;
    private Parcelable mListState;

    private List<BookData> mSearchBooks = new ArrayList<>();
    private String mTempKeyword = null;
    private String mKeyword = null;
    private int mSearchPage = 1;
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
        if(D) Log.d(TAG,"onViewCreated");
        if(savedInstanceState != null){
            if(D) Log.d(TAG,"savedInstanceState != null");
            mListState = savedInstanceState.getParcelable(KEY_LAYOUT_MANAGER);
            mTempKeyword = savedInstanceState.getString(KEY_TEMP_KEYWORD,null);
            mKeyword = savedInstanceState.getString(KEY_KEYWORD,null);
            mSearchPage = savedInstanceState.getInt(KEY_PAGE,1);
            hasResultData = savedInstanceState.getBoolean(KEY_HAS_RESULT_DATA,false);
            hasButtonLoadNext = savedInstanceState.getBoolean(KEY_HAS_BUTTON_LOAD_NEXT);


            if(D) Log.d(TAG,"mKeyword : " + mKeyword);
            if(D) Log.d(TAG,"mSearchPage : " + mSearchPage);
            if(D) Log.d(TAG,"hasResultData : " + hasResultData);
            if(D) Log.d(TAG,"hasButtonLoad : " + hasButtonLoadNext);

            if(hasResultData) {
                mSearchBooks = mApplicationData.getSearchBooks();
                if(D) Log.d(TAG,"mSearchBooks.size() : " + mSearchBooks.size());
                if(hasButtonLoadNext){
                    BookData footer = new BookData();
                    footer.setView_type(BooksListViewAdapter.VIEW_TYPE_BUTTON_LOAD);
                    mSearchBooks.add(footer);

                }
            }
        }
        mSearchBooksViewAdapter = new BooksListViewAdapter(getContext(), mSearchBooks,false);
        mSearchBooksViewAdapter.setClickListener(this);
        RecyclerView mRecyclerView = view.findViewById(R.id.fragment_search_recyclerview);
        mLayoutManager = new LinearLayoutManager(view.getContext());
        if(mListState != null){
            mLayoutManager.onRestoreInstanceState(mListState);
        }
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mSearchBooksViewAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (D) Log.d(TAG, "onActivityCreated");
        mLoaderManager = LoaderManager.getInstance(this);
        if (mLoaderManager.getLoader(LoaderID) != null) {
            if (D) Log.d(TAG, "initLoader");
            mLoaderManager.initLoader(LoaderID, null, mCallback);
        }else{
            getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DISMISS).sendToTarget();
        }
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
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(D) Log.d(TAG,"onSaveInstanceState");
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
        if(isClickable()){
            setClickDisable();
            int view_type = adapter.getItemViewType(position);
            if (view_type == BooksListViewAdapter.VIEW_TYPE_BOOK) {
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    BookDetailFragment fragment = new BookDetailFragment();
                    Bundle bundle = new Bundle();
                    BookData registered = mApplicationData.searchInShelfBooks(data.getISBN());
                    if(registered != null){
                        bundle.putParcelable(BookDetailFragment.KEY_BUNDLE_BOOK, new BookData(registered));
                    }else{
                        bundle.putParcelable(BookDetailFragment.KEY_BUNDLE_BOOK, new BookData(data));
                    }
                    fragment.setArguments(bundle);
                    Slide slide = new Slide();
                    slide.setSlideEdge(Gravity.BOTTOM);
                    fragment.setEnterTransition(slide);
                    fragmentTransaction.replace(R.id.contents_container, fragment, BookDetailFragment.TAG);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            } else {
                if (view_type == BooksListViewAdapter.VIEW_TYPE_BUTTON_LOAD) {
                    BookData footer = new BookData();
                    footer.setView_type(BooksListViewAdapter.VIEW_TYPE_LOADING);
                    adapter.setFooter(footer);
                    AsyncSearchTask(mKeyword, mSearchPage);
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

                if (data.getReadStatus().equals("0")) {
                    // unregistered. register Dialog
                    bundle = new BundleBuilder()
                            .put(BaseDialogFragment.KEY_TITLE, getString(R.string.Dialog_Register_Book_Title))
                            .put(BaseDialogFragment.KEY_MESSAGE, getString(R.string.Dialog_Register_Book_Message))
                            .put(BaseDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.Dialog_Button_Positive))
                            .put(BaseDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.Dialog_Button_Negative))
                            .put(BaseDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_REGISTER_BOOK)
                            .put(BaseDialogFragment.KEY_PARAMS, bundle_book)
                            .put(BaseDialogFragment.KEY_CANCELABLE, true)
                            .build();
                } else {
                    // registered. delete Dialog
                    bundle = new BundleBuilder()
                            .put(BaseDialogFragment.KEY_TITLE, getString(R.string.Dialog_Delete_Book_Title))
                            .put(BaseDialogFragment.KEY_MESSAGE, getString(R.string.Dialog_Delete_Book_Message))
                            .put(BaseDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.Dialog_Button_Positive))
                            .put(BaseDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.Dialog_Button_Negative))
                            .put(BaseDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_DELETE_BOOK)
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
                        boolean isSuccess = mApplicationData.registerToShelfBooks(book);
                        if (isSuccess) {
                            mSearchBooksViewAdapter.registerBook(position_register);
                            Toast.makeText(getContext(), getString(R.string.Toast_Register_Book), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), getString(R.string.Toast_Failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
                case REQUEST_CODE_DELETE_BOOK:
                    int position_unregister = params.getInt(KEY_POSITION, -1);
                    BookData book_unregister = params.getParcelable(KEY_BOOK_DATA);
                    if (book_unregister != null) {
                        boolean isSuccess = mApplicationData.deleteFromShelfBooks(book_unregister.getISBN());
                        if (isSuccess) {
                            mSearchBooksViewAdapter.unregisterBook(position_unregister);
                            Toast.makeText(getContext(), getString(R.string.Toast_Delete_Book), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), getString(R.string.Toast_Failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {
        super.onBaseDialogCancelled(requestCode,params);
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
                AsyncSearchTask(mKeyword, mSearchPage);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String word) {
                mTempKeyword = word;
                return false;
            }
        });
    }


    public void prepareSearch(){
        mTempKeyword = null;
        mSearchView.setQuery(null,false);
        mSearchView.setIconified(false);
    }

    public void AsyncSearchTask(String search, int page) {
        if (MyBookshelfUtils.checkInputWord(search)) {
            if (page == 1) {
                Bundle progress = new BundleBuilder()
                        .put(BaseProgressDialogFragment.title, getString(R.string.Progress_Search))
                        .put(BaseProgressDialogFragment.message, "")
                        .build();
                setProgressDialog(progress);
            }
            Bundle bundle = new BundleBuilder()
                    .put(KEY_KEYWORD, search)
                    .put(KEY_PAGE, page)
                    .build();
            mLoaderManager.restartLoader(LoaderID, bundle, mCallback);
        } else {
            Toast.makeText(getContext(), getString(R.string.Toast_Search_Error_Keyword), Toast.LENGTH_SHORT).show();
        }
    }


    private LoaderManager.LoaderCallbacks<AsyncSearchBook.Result> mCallback = new LoaderManager.LoaderCallbacks<AsyncSearchBook.Result>() {
        @NonNull
        @Override
        public Loader<AsyncSearchBook.Result> onCreateLoader(int i, @Nullable Bundle bundle) {
            String sort = mApplicationData.getSearchBooksSortSetting();
            String keyword = null;
            int page = 1;
            if(bundle != null){
                keyword = bundle.getString(KEY_KEYWORD,null);
                page = bundle.getInt(KEY_PAGE,1);
            }
            return new AsyncSearchBook(getContext(),sort,keyword,page);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<AsyncSearchBook.Result> loader, AsyncSearchBook.Result result) {
            if(D) Log.d(TAG," onLoadFinished");
            mLoaderManager.destroyLoader(loader.getId());
            callback(result);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<AsyncSearchBook.Result> loader) {
        }
    };

    void callback(AsyncSearchBook.Result result){
        if(mSearchPage == 1){
            mApplicationData.deleteTABLE_SEARCH_BOOKS();
            mSearchBooksViewAdapter.clearBooksData();
            hasResultData = false;
            hasButtonLoadNext = false;
        }else{
            BookData footer = new BookData();
            footer.setView_type(BooksListViewAdapter.VIEW_TYPE_BUTTON_LOAD);
            mSearchBooksViewAdapter.setFooter(footer);
        }

        if(!result.isSuccess){
            Toast.makeText(getContext(), getString(R.string.Toast_Search_Error_Unknown), Toast.LENGTH_SHORT).show();
        }else{
            switch (result.errorStatus){
                case HttpURLConnection.HTTP_BAD_REQUEST:     // 400 wrong parameter
                    Toast.makeText(getContext(), getString(R.string.Toast_Search_Error_HTTP_400), Toast.LENGTH_SHORT).show();
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:      // 404 not found
                    Toast.makeText(getContext(), getString(R.string.Toast_Search_Error_HTTP_404), Toast.LENGTH_SHORT).show();
                    break;
                case 429: // 429 too many requests
                    Toast.makeText(getContext(), getString(R.string.Toast_Search_Error_HTTP_429), Toast.LENGTH_SHORT).show();
                    break;
                case HttpURLConnection.HTTP_INTERNAL_ERROR: // 500 system error
                    Toast.makeText(getContext(), getString(R.string.Toast_Search_Error_HTTP_500), Toast.LENGTH_SHORT).show();
                    break;
                case HttpURLConnection.HTTP_UNAVAILABLE:    // 503 service unavailable
                    Toast.makeText(getContext(), getString(R.string.Toast_Search_Error_HTTP_503), Toast.LENGTH_SHORT).show();
                    break;
                case HttpURLConnection.HTTP_OK:
                    convertJSON(result.json);
                    break;
            }
        }
        getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DISMISS).sendToTarget();
    }

    private void convertJSON(JSONObject json){
        List<BookData> books = new ArrayList<>();
        int count = 0;
        int last = 0;
        try {
            if (json.has("Items")) {
                List<BookData> tmp = new ArrayList<>();
                JSONArray jsonArray = json.getJSONArray("Items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject data = jsonArray.getJSONObject(i);
                    if (D) Log.d(TAG, "sb: " + data.toString());
                    BookData book = MyBookshelfUtils.getBook(data);
                    BookData registered = mApplicationData.searchInShelfBooks(book.getISBN());
                    if(registered != null){
                        book.setRegisterDate(registered.getRegisterDate());
                        book.setReadStatus(registered.getReadStatus());
                    }
                    tmp.add(book);
                }

                if (json.has("count")) {
                    count = json.getInt("count");
                    if (D) Log.d(TAG, "count: " + count);
                }

                if (json.has("last")) {
                    last = json.getInt("last");
                    if (D) Log.d(TAG, "last: " + last);
                }

                hasButtonLoadNext = count - last > 0;
                books = new ArrayList<>(tmp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (books.size() > 0) {
            boolean isSuccess = mApplicationData.registerToSearchBooks(books);
            if (D) Log.d(TAG, "isSuccess : " + isSuccess);
            if (isSuccess) {
                mSearchBooksViewAdapter.setFooter(null);
                if (hasButtonLoadNext) {
                    mSearchPage++;
                    BookData footer = new BookData();
                    footer.setView_type(BooksListViewAdapter.VIEW_TYPE_BUTTON_LOAD);
                    books.add(footer);
                }
                mSearchBooksViewAdapter.addBooksData(books);
                hasResultData = true;
            }
        }else {
            Toast.makeText(getContext(), getString(R.string.Toast_Search_Error_Book_not_found), Toast.LENGTH_SHORT).show();
        }
    }


}
