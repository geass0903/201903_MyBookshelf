package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.Slide;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import java.util.ArrayList;
import java.util.List;

public class FragmentShelfBooks extends BaseFragment implements BooksListViewAdapter.OnBookClickListener {
    public static final String TAG = FragmentShelfBooks.class.getSimpleName();
    private static final boolean D = true;

    private static final String KEY_SEARCH_WORD = "KEY_SEARCH_WORD";
    private static final String KEY_SHELF_BOOKS = "KEY_SHELF_BOOKS";
    private static final String KEY_POSITION = "KEY_POSITION";
    private static final String KEY_BOOK = "KEY_BOOK";

    private MyBookshelfApplicationData mApplicationData;
    private BooksListViewAdapter mShelfBooksViewAdapter;
    private SearchView mSearchView;
    private RecyclerView mRecyclerView;

    private List<BookData> mShelfBooks = new ArrayList<>();
    private String mSearchWord;


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
        if(D) Log.d(TAG, "onViewCreated");
        if(savedInstanceState != null){
            mSearchWord = savedInstanceState.getString(KEY_SEARCH_WORD,null);
            mShelfBooks = savedInstanceState.getParcelableArrayList(KEY_SHELF_BOOKS);
        }else {
            mShelfBooks = mApplicationData.getShelfBooks(null);
        }
        mShelfBooksViewAdapter = new BooksListViewAdapter(getContext(),mShelfBooks,true);
        mShelfBooksViewAdapter.setClickListener(this);
        mRecyclerView = view.findViewById(R.id.fragment_shelf_recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mShelfBooksViewAdapter);


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
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_SEARCH_WORD,mSearchWord);
        outState.putParcelableArrayList(KEY_SHELF_BOOKS, (ArrayList<BookData>) mShelfBooks);
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
        mSearchView.setQuery(mSearchWord,false);
        mSearchView.setQueryHint(getString(R.string.search_input_hint));
        mSearchView.setSubmitButtonEnabled(false);
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
                mSearchWord = word;
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
                    fragmentTransaction.replace(R.id.contents_container, fragment, FragmentBookDetail.TAG);
//                    fragmentTransaction.add(R.id.contents_container, fragment, FragmentBookDetail.TAG);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }
        }
    }

    @Override
    public void onBookLongClick(BooksListViewAdapter adapter, int position, BookData data) {
        String title = data.getTitle();
        if(D) Log.d(TAG,"LongClick: " + title);
        Bundle bundle = new Bundle();
        bundle.putString(BaseDialogFragment.title,getString(R.string.Dialog_Label_Delete_Book));
        bundle.putString(BaseDialogFragment.message,getString(R.string.Dialog_Message_Delete_Book));
        bundle.putString(BaseDialogFragment.positiveLabel,getString(R.string.Dialog_Button_Positive));
        bundle.putString(BaseDialogFragment.negativeLabel,getString(R.string.Dialog_Button_Negative));
        bundle.putInt(BaseDialogFragment.request_code, REQUEST_CODE_DELETE_BOOK);

        Bundle bundle_book = new Bundle();
        bundle_book.putInt(KEY_POSITION,position);
        bundle_book.putParcelable(KEY_BOOK,data);
        bundle.putBundle(BaseDialogFragment.params,bundle_book);
        if(getActivity() != null) {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            BaseDialogFragment dialog = BaseDialogFragment.newInstance(this,bundle);
            dialog.show(manager, FragmentSettings.TAG);
        }
    }


    @Override
    public void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        super.onBaseDialogSucceeded(requestCode, resultCode, params);
        if (requestCode == REQUEST_CODE_DELETE_BOOK && resultCode == DialogInterface.BUTTON_POSITIVE && params != null) {
            int position = params.getInt(KEY_POSITION, -1);
            BookData book = params.getParcelable(KEY_BOOK);
            if (book != null) {
                mApplicationData.deleteBook(book.getIsbn());
                mShelfBooksViewAdapter.deleteBook(position);
                Toast.makeText(getContext(),getString(R.string.Toast_Delete_Book),Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {
        super.onBaseDialogCancelled(requestCode,params);
    }

}
