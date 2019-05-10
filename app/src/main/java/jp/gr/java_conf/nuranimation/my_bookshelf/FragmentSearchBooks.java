package jp.gr.java_conf.nuranimation.my_bookshelf;

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

public class FragmentSearchBooks extends BaseFragment implements BooksListViewAdapter.OnBookClickListener{
    private static final boolean D = true;
    public static final String TAG = FragmentSearchBooks.class.getSimpleName();
    private static final int LoaderID = 1;
    private static final String KEY_TmpKeyword = "KEY_TmpKeyword";
    private static final String KEY_Books_SearchResult = "KEY_Books_SearchResult";
    private static final String KEY_SearchKeyword = "KEY_SearchKeyword";
    private static final String KEY_SearchPage = "KEY_SearchPage";
    private static final String KEY_position = "KEY_position";
    private static final String KEY_Book = "KEY_Book";

    private MyBookshelfApplicationData mData;
    private BooksListViewAdapter mBooksViewAdapter;
    private LoaderManager mLoaderManager;
    private SearchView mSearchView;


    private List<BookData> mBooks_SearchResult = new ArrayList<>();
    private String mSearchKeyword;
    private int mSearchPage;
    private String mTmpKeyword;


    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mData = (MyBookshelfApplicationData) context.getApplicationContext();
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
            mTmpKeyword = savedInstanceState.getString(KEY_TmpKeyword,null);
            mSearchKeyword = savedInstanceState.getString(KEY_SearchKeyword,null);
            mSearchPage = savedInstanceState.getInt(KEY_SearchPage,1);
            mBooks_SearchResult = savedInstanceState.getParcelableArrayList(KEY_Books_SearchResult);
        }
        mBooksViewAdapter = new BooksListViewAdapter(getContext(), mBooks_SearchResult,false);
        mBooksViewAdapter.setClickListener(this);
        RecyclerView mRecyclerView = view.findViewById(R.id.fragment_search_recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mBooksViewAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (D) Log.d(TAG, "onActivityCreated");
        mLoaderManager = LoaderManager.getInstance(this);
        if (mLoaderManager.getLoader(LoaderID) != null) {
            mLoaderManager.initLoader(LoaderID, null, mCallback);
        }else{
            isShowingDialog = false;
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
        outState.putString(KEY_TmpKeyword,mTmpKeyword);
        outState.putString(KEY_SearchKeyword,mSearchKeyword);
        outState.putInt(KEY_SearchPage,mSearchPage);
        outState.putParcelableArrayList(KEY_Books_SearchResult, (ArrayList<BookData>) mBooks_SearchResult);
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
        mSearchView.setQuery(mTmpKeyword,false);
        mSearchView.setQueryHint(getString(R.string.search_input_hint));
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchWord) {
                mSearchView.clearFocus();
                mBooksViewAdapter.clearBooksData();
                mSearchKeyword = searchWord;
                mSearchPage = 1;
                AsyncSearchTask(mSearchKeyword, mSearchPage);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String word) {
                mTmpKeyword = word;
                return false;
            }
        });
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
                    .put(KEY_SearchKeyword, search)
                    .put(KEY_SearchPage, page)
                    .build();
            mLoaderManager.restartLoader(LoaderID, bundle, mCallback);
        } else {
            Toast.makeText(getContext(), getString(R.string.search_error_input_word), Toast.LENGTH_SHORT).show();
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
                keyword = bundle.getString(KEY_SearchKeyword,null);
                page = bundle.getInt(KEY_SearchPage,1);
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
        mBooksViewAdapter.setFooter(null);
        if(result.isSuccess){
            if(result.status == HttpURLConnection.HTTP_OK) {
                if (result.books.size() > 0) {
                    mBooksViewAdapter.addBooksData(result.books);
                } else {
                    Toast.makeText(getContext(), getString(R.string.search_book_not_found), Toast.LENGTH_SHORT).show();
                }
            }else{
                switch(result.status){
                    case HttpURLConnection.HTTP_BAD_REQUEST:     // 400 wrong parameter
                        Toast.makeText(getContext(), getString(R.string.search_error_http_400), Toast.LENGTH_SHORT).show();
                        break;
                    case HttpURLConnection.HTTP_NOT_FOUND:      // 404 not found
                        Toast.makeText(getContext(), getString(R.string.search_error_http_404), Toast.LENGTH_SHORT).show();
                        break;
                    case 429: // 429 too many requests
                        Toast.makeText(getContext(), getString(R.string.search_error_http_429), Toast.LENGTH_SHORT).show();
                        break;
                    case HttpURLConnection.HTTP_INTERNAL_ERROR: // 500 system error
                        Toast.makeText(getContext(), getString(R.string.search_error_http_500), Toast.LENGTH_SHORT).show();
                        break;
                    case HttpURLConnection.HTTP_UNAVAILABLE:    // 503 service unavailable
                        Toast.makeText(getContext(), getString(R.string.search_error_http_503), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }else{
            Toast.makeText(getContext(), getString(R.string.search_error_unknown), Toast.LENGTH_SHORT).show();
        }
        handler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_DISMISS).sendToTarget();
    }


    private String getSortType(){
        String sort = "standard";
        String code = mData.getSharedPreferences().getString(MyBookshelfApplicationData.Key_SortSetting_SearchResult, getString(R.string.Code_SortSetting_SalesDate_Descending));
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
        if(isClickEnabled) {
            isClickEnabled = false;
            setWait_ClickEnable(500);
            int view_type = adapter.getItemViewType(position);
            if (view_type == BooksListViewAdapter.VIEW_TYPE_BOOK) {
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    FragmentBookDetail fragment = new FragmentBookDetail();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(FragmentBookDetail.KEY_bundle_book, data);

                    fragment.setArguments(bundle);
                    Slide slide = new Slide();
                    slide.setSlideEdge(Gravity.END);
                    fragment.setEnterTransition(slide);
                fragmentTransaction.replace(R.id.contents_container, fragment,FragmentBookDetail.TAG);
//                    fragmentTransaction.add(R.id.contents_container, fragment, FragmentBookDetail.TAG);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            } else {
                if (view_type == BooksListViewAdapter.VIEW_TYPE_BUTTON_LOAD) {
                    BookData footer = new BookData();
                    footer.setView_type(BooksListViewAdapter.VIEW_TYPE_LOADING);
                    adapter.setFooter(footer);
                    mSearchPage++;
                    AsyncSearchTask(mSearchKeyword, mSearchPage);
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
                bundle.putString(BaseDialogFragment.title,getString(R.string.Dialog_Label_Register_Book));
                bundle.putString(BaseDialogFragment.message,getString(R.string.Dialog_Message_Register_Book));
                bundle.putString(BaseDialogFragment.positiveLabel,getString(R.string.Dialog_Button_Positive));
                bundle.putString(BaseDialogFragment.negativeLabel,getString(R.string.Dialog_Button_Negative));
                bundle.putInt(BaseDialogFragment.request_code, REQUEST_CODE_REGISTER_BOOK);
            }else{
                // registered. delete Dialog
                bundle.putString(BaseDialogFragment.title,getString(R.string.Dialog_Label_Delete_Book));
                bundle.putString(BaseDialogFragment.message,getString(R.string.Dialog_Message_Delete_Book));
                bundle.putString(BaseDialogFragment.positiveLabel,getString(R.string.Dialog_Button_Positive));
                bundle.putString(BaseDialogFragment.negativeLabel,getString(R.string.Dialog_Button_Negative));
                bundle.putInt(BaseDialogFragment.request_code, REQUEST_CODE_DELETE_BOOK);
            }

            Bundle bundle_book = new Bundle();
            bundle_book.putInt(KEY_position,position);
            bundle_book.putParcelable(KEY_Book,data);
            bundle.putBundle(BaseDialogFragment.params,bundle_book);
            if(getActivity() != null) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                BaseDialogFragment dialog = BaseDialogFragment.newInstance(this,bundle);
                dialog.show(manager, FragmentSettings.TAG);
            }
        }

    }


    @Override
    public void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        super.onBaseDialogSucceeded(requestCode, resultCode, params);
        if(resultCode == DialogInterface.BUTTON_POSITIVE && params != null){
            switch (requestCode){
                case REQUEST_CODE_REGISTER_BOOK:
                    int position_register = params.getInt(KEY_position, -1);
                    BookData book_register = params.getParcelable(KEY_Book);
                    if(book_register != null) {
                        BookData book = new BookData(book_register);
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);
                        String registerDate = sdf.format(calendar.getTime());
                        book.setRegisterDate(registerDate);
                        book.setRating("0.0");
                        book.setReadStatus("5");
                        MyBookshelfDBOpenHelper helper = mData.getDatabaseHelper();
                        helper.registerBook(book);
                        mBooksViewAdapter.registerBook(position_register);
                        Toast.makeText(getContext(), getString(R.string.Toast_Register_Book), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_CODE_DELETE_BOOK:
                    int position_unregister = params.getInt(KEY_position, -1);
                    BookData book_unregister = params.getParcelable(KEY_Book);
                    if (book_unregister != null) {
                        MyBookshelfDBOpenHelper helper = mData.getDatabaseHelper();
                        helper.deleteBook(book_unregister.getIsbn());
                        mBooksViewAdapter.unregisterBook(position_unregister);
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
