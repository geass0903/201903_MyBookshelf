package jp.gr.java_conf.nuranimation.my_bookshelf.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import jp.gr.java_conf.nuranimation.my_bookshelf.MainActivity;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.background.BookService;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseProgressDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.adapter.BooksListViewAdapter;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BundleBuilder;


public class NewBooksFragment extends BaseFragment implements BooksListViewAdapter.OnBookClickListener{
    public static final String TAG = NewBooksFragment.class.getSimpleName();
    private static final boolean D = true;

    private static final String KEY_LAYOUT_MANAGER = "KEY_LAYOUT_MANAGER";
    private static final String KEY_RELOAD_STATE = "KEY_RELOAD_STATE";
    private static final String KEY_POSITION = "KEY_POSITION";
    private static final String KEY_BOOK_DATA = "KEY_BOOK_DATA";

    private MyBookshelfApplicationData mApplicationData;
    private BooksListViewAdapter mNewBooksViewAdapter;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;

    private List<BookData> mNewBooks;
    private int mReloadState = 0;


    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mApplicationData = (MyBookshelfApplicationData) context.getApplicationContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(D) Log.d(TAG, "onViewCreated");
        if(getActivity() != null) {
            getActivity().setTitle(R.string.Navigation_Item_NewBooks);
        }
        mLayoutManager = new LinearLayoutManager(view.getContext());
        if (savedInstanceState == null){
            if(mNewBooks == null) {
                mNewBooks = mApplicationData.loadNewBooks();
                if (getArguments() != null) {
                    mReloadState = getArguments().getInt(BookService.KEY_SERVICE_STATE, 0);
                }
            }
        }else {
            if (D) Log.d(TAG, "savedInstanceState != null");
            mReloadState = savedInstanceState.getInt(KEY_RELOAD_STATE, 0);
            mNewBooks = mApplicationData.loadNewBooks();
            Parcelable mListState = savedInstanceState.getParcelable(KEY_LAYOUT_MANAGER);
            if (mListState != null) {
                mLayoutManager.onRestoreInstanceState(mListState);
            }
        }
        mNewBooksViewAdapter = new BooksListViewAdapter(getContext(), mNewBooks,true);
        mNewBooksViewAdapter.setClickListener(this);
        mRecyclerView = view.findViewById(R.id.fragment_new_recyclerview);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mNewBooksViewAdapter);
        switch (mReloadState) {
            case BookService.STATE_NONE:
                break;
            case BookService.STATE_NEW_BOOKS_RELOAD_START:
            case BookService.STATE_NEW_BOOKS_RELOAD_FINISH:
                Bundle progress = new BundleBuilder()
                        .put(BaseProgressDialogFragment.title, getString(R.string.Progress_Reload))
                        .put(BaseProgressDialogFragment.message, "")
                        .build();
                setProgressBundle(progress);
                break;
        }
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
        if(getActivity() instanceof MainActivity){
            BookService service = ((MainActivity) getActivity()).getService();
            if(service != null && service.getServiceState() == BookService.STATE_NEW_BOOKS_RELOAD_FINISH){
                mReloadState = BookService.STATE_NEW_BOOKS_RELOAD_FINISH;
                loadNewBooksResult();
            }
        }
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
        outState.putInt(KEY_RELOAD_STATE, mReloadState);
        outState.putParcelable(KEY_LAYOUT_MANAGER,mLayoutManager.onSaveInstanceState());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_new,menu);
        if(D) Log.d(TAG,"onCreateOptionsMenu");
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_new_action_reload).getIcon().setColorFilter(Color.argb(255,255,255,255), PorterDuff.Mode.SRC_ATOP);
        if(D) Log.d(TAG,"onPrepareOptionsMenu()");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_new_action_reload:
                if(D) Log.d(TAG,"new action search");

                if (getActivity() instanceof MainActivity) {
                    BookService service = ((MainActivity) getActivity()).getService();
                    if (service != null) {
                        Bundle progress = new BundleBuilder()
                                .put(BaseProgressDialogFragment.title, getString(R.string.Progress_Reload))
                                .put(BaseProgressDialogFragment.message, "")
                                .build();
                        setProgressBundle(progress);
                        showProgressDialog();
                        mReloadState = BookService.STATE_NEW_BOOKS_RELOAD_START;

                        List<String> authorsList = new ArrayList<>();
                        authorsList.add("金田一蓮十郎");
                        authorsList.add("田丸雅智");

                        service.reloadNewBooks(authorsList);


//                        service.reloadNewBooks(mApplicationData.loadAuthorsList());
                    }
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
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
                        mApplicationData.registerToShelfBooks(book);
                        mNewBooksViewAdapter.updateBook(position_register);
                        Toast.makeText(getContext(), getString(R.string.Toast_Register_Book), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_CODE_DELETE_BOOK:
                    int position_unregister = params.getInt(KEY_POSITION, -1);
                    BookData book_unregister = params.getParcelable(KEY_BOOK_DATA);
                    if (book_unregister != null) {
                        mApplicationData.unregisterFromShelfBooks(book_unregister);
                        mNewBooksViewAdapter.updateBook(position_unregister);
                        Toast.makeText(getContext(), getString(R.string.Toast_Delete_Book), Toast.LENGTH_SHORT).show();
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
        String action = intent.getAction();
        if(action != null){
            switch (action){
                case FILTER_ACTION_UPDATE_SERVICE_STATE:
                    int state = intent.getIntExtra(KEY_UPDATE_SERVICE_STATE, 0);
                    switch (state) {
                        case BookService.STATE_NONE:
                            if (D) Log.d(TAG, "STATE_NONE");
                            mReloadState = BookService.STATE_NONE;
                            getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DISMISS).sendToTarget();
                            break;
                        case BookService.STATE_NEW_BOOKS_RELOAD_START:
                            if (D) Log.d(TAG, "STATE_NEW_BOOKS_RELOAD_START");
                            break;
                        case BookService.STATE_NEW_BOOKS_RELOAD_FINISH:
                            if (D) Log.d(TAG, "STATE_NEW_BOOKS_RELOAD_FINISH");
                            loadNewBooksResult();
                            break;
                    }
                    break;
                case FILTER_ACTION_UPDATE_PROGRESS:
                    String progress = intent.getStringExtra(KEY_UPDATE_PROGRESS);
                    getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_UPDATE, progress).sendToTarget();
                    break;
            }
        }
    }


    public void checkReloadState(){
        if(getActivity() instanceof MainActivity){
            BookService service = ((MainActivity) getActivity()).getService();
            if(service != null && service.getServiceState() == BookService.STATE_NEW_BOOKS_RELOAD_FINISH){
                mReloadState = BookService.STATE_NEW_BOOKS_RELOAD_FINISH;
                loadNewBooksResult();
            }
        }
    }

    private void loadNewBooksResult() {
        if(D) Log.d(TAG, "loadNewBooksResult()");
        scrollToTop();
        mNewBooksViewAdapter.replaceBooksData(mApplicationData.loadNewBooks());
        getPausedHandler().removeCallbacks(delayLoadNewBooksResult);
        getPausedHandler().postDelayed(delayLoadNewBooksResult,1000);
    }

    public void scrollToTop(){
        mRecyclerView.scrollToPosition(0);
    }



    Runnable delayLoadNewBooksResult = new Runnable() {
        @Override
        public void run() {
            if(D) Log.d(TAG, "delayLoadNewBooksResult()");
            if (getActivity() instanceof MainActivity) {
                BookService service = ((MainActivity) getActivity()).getService();
                if (service != null) {
                    service.setServiceState(BookService.STATE_NONE);
                    service.stopForeground(false);
                    service.stopSelf();
                }
            }
            mReloadState = BookService.STATE_NONE;
            getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DISMISS).sendToTarget();
        }
    };

























}
