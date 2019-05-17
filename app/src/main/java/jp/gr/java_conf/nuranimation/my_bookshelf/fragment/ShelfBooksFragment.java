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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.utils.BundleBuilder;
import jp.gr.java_conf.nuranimation.my_bookshelf.utils.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.book.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.book.BooksListViewAdapter;


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
    private Parcelable mListState;
    private String mKeyword;




    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mApplicationData = (MyBookshelfApplicationData) context.getApplicationContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookshelf, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(D) Log.d(TAG, "onViewCreated");
        if(savedInstanceState != null){
            if(D) Log.d(TAG,"savedInstanceState != null");
            mKeyword = savedInstanceState.getString(KEY_KEYWORD,null);
            mListState = savedInstanceState.getParcelable(KEY_LAYOUT_MANAGER);
        }
        List<BookData> mShelfBooks = new ArrayList<>(mApplicationData.getShelfBooks(mKeyword));
        mShelfBooksViewAdapter = new BooksListViewAdapter(getContext(),mShelfBooks,true);
        mShelfBooksViewAdapter.setClickListener(this);
        mRecyclerView = view.findViewById(R.id.fragment_shelf_recyclerview);
        mLayoutManager = new LinearLayoutManager(view.getContext());
        if(mListState != null){
            mLayoutManager.onRestoreInstanceState(mListState);
        }
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
                searchShelf(searchWord);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String word) {
                if(D) Log.d(TAG,"QueryTextChange: " + word);
                mKeyword = word;
                searchShelf(word);
                return false;
            }
        });
    }






    public void scrollTop(){
        mRecyclerView.scrollToPosition(0);
    }

    private void searchShelf(String word){
        List<BookData> books = mApplicationData.getShelfBooks(word);
        mShelfBooksViewAdapter.setBooksData(books);
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
                    bundle.putParcelable(BookDetailFragment.KEY_BUNDLE_BOOK, data);
                    fragment.setArguments(bundle);
                    Slide slide = new Slide();
                    slide.setSlideEdge(Gravity.BOTTOM);
                    fragment.setEnterTransition(slide);
                    fragmentTransaction.replace(R.id.contents_container, fragment, BookDetailFragment.TAG);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }
        }

    }

    @Override
    public void onBookLongClick(BooksListViewAdapter adapter, int position, BookData data) {
        if(isClickable()) {
            setClickDisable();
            int view_type = adapter.getItemViewType(position);
            if (view_type == BooksListViewAdapter.VIEW_TYPE_BOOK) {
                Bundle bundle_book = new Bundle();
                bundle_book.putInt(KEY_POSITION, position);
                BookData book = new BookData(data);
                bundle_book.putParcelable(KEY_BOOK_DATA, book);

                if (getActivity() != null) {
                    Bundle bundle = new BundleBuilder()
                            .put(BaseDialogFragment.KEY_TITLE, getString(R.string.Dialog_Delete_Book_Title))
                            .put(BaseDialogFragment.KEY_MESSAGE, getString(R.string.Dialog_Delete_Book_Message))
                            .put(BaseDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.Dialog_Button_Positive))
                            .put(BaseDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.Dialog_Button_Negative))
                            .put(BaseDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_DELETE_BOOK)
                            .put(BaseDialogFragment.KEY_PARAMS, bundle_book)
                            .put(BaseDialogFragment.KEY_CANCELABLE, true)
                            .build();
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    BaseDialogFragment fragment = BaseDialogFragment.newInstance(this, bundle);
                    fragment.show(manager, ShelfBooksFragment.TAG);
                }
            }
        }
    }


    @Override
    public void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        super.onBaseDialogSucceeded(requestCode, resultCode, params);
        if (requestCode == REQUEST_CODE_DELETE_BOOK && resultCode == DialogInterface.BUTTON_POSITIVE && params != null) {
            int position = params.getInt(KEY_POSITION, -1);
            BookData book = params.getParcelable(KEY_BOOK_DATA);
            if (book != null) {
                boolean isSuccess = mApplicationData.deleteFromShelfBooks(book.getISBN());
                if(isSuccess) {
                    mShelfBooksViewAdapter.deleteBook(position);
                    Toast.makeText(getContext(), getString(R.string.Toast_Delete_Book), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getContext(), getString(R.string.Toast_Failed), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {
        super.onBaseDialogCancelled(requestCode,params);
    }

}
