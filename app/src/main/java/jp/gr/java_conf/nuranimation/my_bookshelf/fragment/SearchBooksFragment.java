package jp.gr.java_conf.nuranimation.my_bookshelf.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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
import jp.gr.java_conf.nuranimation.my_bookshelf.utils.MyBookshelfDBOpenHelper;
import jp.gr.java_conf.nuranimation.my_bookshelf.utils.MyBookshelfUtils;

public class SearchBooksFragment extends BaseFragment implements BooksListViewAdapter.OnBookClickListener{
    private static final boolean D = true;
    public static final String TAG = SearchBooksFragment.class.getSimpleName();
    private static final int LoaderID = 1;
    private static final String KEY_TEMP_KEYWORD = "KEY_TEMP_KEYWORD";
    private static final String KEY_KEYWORD = "KEY_KEYWORD";
    private static final String KEY_PAGE = "KEY_PAGE";
    private static final String KEY_POSITION = "KEY_POSITION";
    private static final String KEY_BOOK_DATA = "KEY_BOOK_DATA";

    private MyBookshelfApplicationData mApplicationData;
    private BooksListViewAdapter mSearchBooksViewAdapter;
    private LoaderManager mLoaderManager;
    private SearchView mSearchView;

    private List<BookData> mSearchBooks = new ArrayList<>();
    private String mTempKeyword;
    private String mKeyword;
    private int mSearchPage;


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
        if(savedInstanceState == null){
            if(D) Log.d(TAG,"savedInstanceState == null");
            mSearchBooks = new ArrayList<>();
        }else{
            if(D) Log.d(TAG,"savedInstanceState != null");
            mSearchBooks = mApplicationData.getSearchBooks();
            mTempKeyword = savedInstanceState.getString(KEY_TEMP_KEYWORD,null);
        }

        mSearchBooksViewAdapter = new BooksListViewAdapter(getContext(), mSearchBooks,false);
        mSearchBooksViewAdapter.setClickListener(this);
        RecyclerView mRecyclerView = view.findViewById(R.id.fragment_search_recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mSearchBooksViewAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (D) Log.d(TAG, "onActivityCreated");
        mLoaderManager = LoaderManager.getInstance(this);
        if (mLoaderManager.getLoader(LoaderID) != null) {
            mLoaderManager.initLoader(LoaderID, null, mCallback);
        }else{
            dismissProgress();
//            isShowingProgress = false;
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
        outState.putString(KEY_TEMP_KEYWORD, mTempKeyword);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search,menu);
        initSearchView(menu);
    }



    private void initSearchView(Menu menu){
        mSearchView = (SearchView)menu.findItem(R.id.menu_search_search_view).getActionView();
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setQuery(mTempKeyword,false);
        mSearchView.setQueryHint(getString(R.string.InputHint_Search));
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchWord) {
                mSearchView.clearFocus();
                mApplicationData.getDatabaseHelper().deleteTABLE_SEARCH_BOOKS();
                mSearchBooksViewAdapter.clearBooksData();
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
            String sort = getSortType();
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
            mLoaderManager.destroyLoader(LoaderID);
            callback(result);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<AsyncSearchBook.Result> loader) {
        }
    };


    void callback(AsyncSearchBook.Result result){
        mSearchBooksViewAdapter.setFooter(null);
        if(result.isSuccess){
            if(result.status == HttpURLConnection.HTTP_OK) {
                if (result.books.size() > 0) {
                    mSearchBooksViewAdapter.addBooksData(result.books);
                } else {
                    Toast.makeText(getContext(), getString(R.string.Toast_Search_Error_Book_not_found), Toast.LENGTH_SHORT).show();
                }
            }else{
                switch(result.status){
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
                }
            }
        }else{
            Toast.makeText(getContext(), getString(R.string.Toast_Search_Error_Unknown), Toast.LENGTH_SHORT).show();
        }
        getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DISMISS).sendToTarget();
    }


    private String getSortType(){
        String sort = "standard";
        String code = mApplicationData.getSharedPreferences().getString(MyBookshelfApplicationData.KEY_SEARCH_BOOKS_ORDER, getString(R.string.Code_SortSetting_SalesDate_Descending));
        if(code != null) {
            if (code.equals(getString(R.string.Code_SortSetting_SalesDate_Ascending))) {
                sort = "+releaseDate";
            }
            if (code.equals(getString(R.string.Code_SortSetting_SalesDate_Descending))) {
                sort = "-releaseDate";
            }
        }
        return sort;
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
                    BookData book = new BookData(data);
                    bundle.putParcelable(BookDetailFragment.KEY_BUNDLE_BOOK, book);

                    fragment.setArguments(bundle);
                    Slide slide = new Slide();
                    slide.setSlideEdge(Gravity.END);
                    fragment.setEnterTransition(slide);
                    fragmentTransaction.replace(R.id.contents_container, fragment, BookDetailFragment.TAG);
//                    fragmentTransaction.add(R.id.contents_container, fragment, BookDetailFragment.TAG);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            } else {
                if (view_type == BooksListViewAdapter.VIEW_TYPE_BUTTON_LOAD) {
                    BookData footer = new BookData();
                    footer.setView_type(BooksListViewAdapter.VIEW_TYPE_LOADING);
                    adapter.setFooter(footer);
                    mSearchPage++;
                    AsyncSearchTask(mKeyword, mSearchPage);
                }
            }

        }
    }

    @Override
    public void onBookLongClick(BooksListViewAdapter adapter, int position, BookData data) {
        int view_type = adapter.getItemViewType(position);
        if(view_type == BooksListViewAdapter.VIEW_TYPE_BOOK) {
            String title = data.getTitle();
            if(D) Log.d(TAG,"LongClick: " + title);
            Bundle bundle = new Bundle();
            if(data.getReadStatus().equals("0")){
                // unregistered. register Dialog
                bundle.putString(BaseDialogFragment.KEY_TITLE,getString(R.string.Dialog_Register_Book_Title));
                bundle.putString(BaseDialogFragment.KEY_MESSAGE,getString(R.string.Dialog_Register_Book_Message));
                bundle.putString(BaseDialogFragment.KEY_POSITIVE_LABEL,getString(R.string.Dialog_Button_Positive));
                bundle.putString(BaseDialogFragment.KEY_NEGATIVE_LABEL,getString(R.string.Dialog_Button_Negative));
                bundle.putInt(BaseDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_REGISTER_BOOK);
            }else{
                // registered. delete Dialog
                bundle.putString(BaseDialogFragment.KEY_TITLE,getString(R.string.Dialog_Delete_Book_Title));
                bundle.putString(BaseDialogFragment.KEY_MESSAGE,getString(R.string.Dialog_Delete_Book_Message));
                bundle.putString(BaseDialogFragment.KEY_POSITIVE_LABEL,getString(R.string.Dialog_Button_Positive));
                bundle.putString(BaseDialogFragment.KEY_NEGATIVE_LABEL,getString(R.string.Dialog_Button_Negative));
                bundle.putInt(BaseDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_DELETE_BOOK);
            }

            Bundle bundle_book = new Bundle();
            bundle_book.putInt(KEY_POSITION,position);
            bundle_book.putParcelable(KEY_BOOK_DATA,data);
            bundle.putBundle(BaseDialogFragment.KEY_PARAMS,bundle_book);
            if(getActivity() != null) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                BaseDialogFragment dialog = BaseDialogFragment.newInstance(this,bundle);
                dialog.show(manager, SettingsFragment.TAG);
            }
        }

    }


    @Override
    public void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        super.onBaseDialogSucceeded(requestCode, resultCode, params);
        if(resultCode == DialogInterface.BUTTON_POSITIVE && params != null){
            switch (requestCode){
                case REQUEST_CODE_REGISTER_BOOK:
                    int position_register = params.getInt(KEY_POSITION, -1);
                    BookData book_register = params.getParcelable(KEY_BOOK_DATA);
                    if(book_register != null) {
                        BookData book = new BookData(book_register);
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);
                        String registerDate = sdf.format(calendar.getTime());
                        book.setRegisterDate(registerDate);
                        book.setRating("0.0");
                        book.setReadStatus("5");
                        MyBookshelfDBOpenHelper helper = mApplicationData.getDatabaseHelper();
                        boolean isSuccess = helper.registerToShelfBooks(book);
                        mSearchBooksViewAdapter.registerBook(position_register);
                        Toast.makeText(getContext(), getString(R.string.Toast_Register_Book), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_CODE_DELETE_BOOK:
                    int position_unregister = params.getInt(KEY_POSITION, -1);
                    BookData book_unregister = params.getParcelable(KEY_BOOK_DATA);
                    if (book_unregister != null) {
                        MyBookshelfDBOpenHelper helper = mApplicationData.getDatabaseHelper();
                        helper.deleteFromShelfBooks(book_unregister.getISBN());
                        mSearchBooksViewAdapter.unregisterBook(position_unregister);
                        Toast.makeText(getContext(),getString(R.string.Toast_Delete_Book),Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

    }


    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {
        super.onBaseDialogCancelled(requestCode,params);
    }


}
