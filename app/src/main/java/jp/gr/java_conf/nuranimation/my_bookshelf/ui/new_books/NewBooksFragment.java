package jp.gr.java_conf.nuranimation.my_bookshelf.ui.new_books;

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

import java.util.Calendar;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.model.database.MyBookshelfDBOpenHelper;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.thread.base.BaseThread;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.BookDataUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.Result;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.CalendarUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.book_detail.BookDetailFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.service.BookService;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.util.BooksListViewAdapter;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.MyBookshelfEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.ProgressDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.MainActivity;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.NormalDialogFragment;


public class NewBooksFragment extends BaseFragment implements BooksListViewAdapter.OnBookClickListener, NormalDialogFragment.OnNormalDialogListener, ProgressDialogFragment.OnProgressDialogListener {
    public static final String TAG = NewBooksFragment.class.getSimpleName();
    private static final boolean D = true;

    public static final String KEY_SERVICE_STATE = "NewBooksFragment.KEY_SERVICE_STATE";

    private static final String KEY_LAYOUT_MANAGER = "KEY_LAYOUT_MANAGER";
    private static final String KEY_RELOAD_STATE = "KEY_RELOAD_STATE";
    private static final String KEY_POSITION = "KEY_POSITION";
    private static final String KEY_BOOK_DATA = "KEY_BOOK_DATA";

    private MyBookshelfDBOpenHelper mDBOpenHelper;
    private BooksListViewAdapter mNewBooksViewAdapter;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;

    private List<BookData> mNewBooks;
    private int mReloadState = 0;


    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mDBOpenHelper = MyBookshelfDBOpenHelper.getInstance(context.getApplicationContext());



    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_books, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(D) Log.d(TAG, "onViewCreated");
        if(getActivity() != null) {
            getActivity().setTitle(R.string.navigation_item_new_books);
        }
        mLayoutManager = new LinearLayoutManager(view.getContext());
        if (savedInstanceState == null){
            if(mNewBooks == null) {
                mNewBooks = mDBOpenHelper.loadNewBooks();
                if (getArguments() != null) {
                    mReloadState = getArguments().getInt(KEY_SERVICE_STATE, BookService.STATE_NONE);
                }
            }
        }else {
            if (D) Log.d(TAG, "savedInstanceState != null");
            mReloadState = savedInstanceState.getInt(KEY_RELOAD_STATE, BookService.STATE_NONE);
            mNewBooks = mDBOpenHelper.loadNewBooks();
            Parcelable mListState = savedInstanceState.getParcelable(KEY_LAYOUT_MANAGER);
            if (mListState != null) {
                mLayoutManager.onRestoreInstanceState(mListState);
            }
        }
        mNewBooksViewAdapter = new BooksListViewAdapter(getContext(), mNewBooks, BooksListViewAdapter.LIST_TYPE_NEW_BOOKS);
        mNewBooksViewAdapter.setClickListener(this);
        mRecyclerView = view.findViewById(R.id.fragment_new_books_recyclerview);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mNewBooksViewAdapter);
        switch (mReloadState) {
            case BookService.STATE_NONE:
                break;
            case BookService.STATE_NEW_BOOKS_RELOAD_INCOMPLETE:
            case BookService.STATE_NEW_BOOKS_RELOAD_COMPLETE:
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
        checkReloadState();
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
        if (item.getItemId() == R.id.menu_new_action_reload) {
            if (D) Log.d(TAG, "new action search");
            if (getActivity() instanceof MainActivity) {
                BookService service = ((MainActivity) getActivity()).getService();
                if (service != null) {
                    mReloadState = BookService.STATE_NEW_BOOKS_RELOAD_INCOMPLETE;

                    Bundle bundle = new Bundle();
                    bundle.putInt(ProgressDialogFragment.KEY_REQUEST_CODE, ProgressDialogFragment.REQUEST_CODE_ASK_FOR_PERMISSIONS);
                    bundle.putString(ProgressDialogFragment.KEY_TITLE, getString(R.string.progress_title_reload_new_books));
                    ProgressDialogFragment.showProgressDialog(this, bundle);

//                    Message msg = getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_SHOW);
//                    Bundle bundle = new Bundle();
//                    bundle.putString(ProgressDialogFragment.KEY_TITLE, getString(R.string.progress_title_reload_new_books));
//                    msg.setData(bundle);
//                    getPausedHandler().sendMessage(msg);
                    service.reloadNewBooks(mDBOpenHelper.loadAuthorsList());
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onBookClick(BooksListViewAdapter adapter, int position, BookData data) {
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
            }
        }
    }

    @Override
    public void onBookLongClick(BooksListViewAdapter adapter, int position, BookData data) {
        if (isClickable()) {
            waitClickable(500);
            int view_type = adapter.getItemViewType(position);
            if (view_type == BookData.TYPE_BOOK) {
                Bundle bundle = new Bundle();
                Bundle bundle_book = new Bundle();
                bundle_book.putInt(KEY_POSITION, position);
                BookData book = new BookData(data);
                bundle_book.putParcelable(KEY_BOOK_DATA, book);
                BookData registered = mDBOpenHelper.loadBookDataFromShelfBooks(data);
                if(registered.getView_type() == BookData.TYPE_BOOK){
                    // registered. delete Dialog
                    bundle.putString(NormalDialogFragment.KEY_TITLE, getString(R.string.dialog_title_unregister_book));
                    bundle.putString(NormalDialogFragment.KEY_MESSAGE, getString(R.string.dialog_message_unregister_book));
                    bundle.putString(NormalDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
                    bundle.putString(NormalDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
                    bundle.putInt(NormalDialogFragment.KEY_REQUEST_CODE, ProgressDialogFragment.REQUEST_CODE_UNREGISTER_BOOK);
                    bundle.putBundle(NormalDialogFragment.KEY_PARAMS, bundle_book);
                    bundle.putBoolean(NormalDialogFragment.KEY_CANCELABLE, true);
                }else {
                    // unregistered. register Dialog
                    bundle.putString(NormalDialogFragment.KEY_TITLE, getString(R.string.dialog_title_register_book));
                    bundle.putString(NormalDialogFragment.KEY_MESSAGE, getString(R.string.dialog_message_register_book));
                    bundle.putString(NormalDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
                    bundle.putString(NormalDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
                    bundle.putInt(NormalDialogFragment.KEY_REQUEST_CODE, ProgressDialogFragment.REQUEST_CODE_REGISTER_BOOK);
                    bundle.putBundle(NormalDialogFragment.KEY_PARAMS, bundle_book);
                    bundle.putBoolean(NormalDialogFragment.KEY_CANCELABLE, true);
                }
                if (getActivity() != null) {
                    FragmentManager manager = getActivity().getSupportFragmentManager();
 //                   NormalDialogFragment fragment = NormalDialogFragment.newInstance(this, bundle);
                    NormalDialogFragment fragment = NormalDialogFragment.newInstance(this, bundle);
                    fragment.show(manager, NormalDialogFragment.TEMP_TAG);
                }
            }
        }
    }

    @Override
    public void onNormalDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        if (resultCode == DialogInterface.BUTTON_POSITIVE && params != null) {
            switch (requestCode) {
                case ProgressDialogFragment.REQUEST_CODE_REGISTER_BOOK:
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
                        mNewBooksViewAdapter.refreshBook(position_register);
                        Toast.makeText(getContext(), getString(R.string.toast_success_register_book), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ProgressDialogFragment.REQUEST_CODE_UNREGISTER_BOOK:
                    int position_unregister = params.getInt(KEY_POSITION, -1);
                    BookData book_unregister = params.getParcelable(KEY_BOOK_DATA);
                    if (book_unregister != null) {
                        mDBOpenHelper.unregisterFromShelfBooks(book_unregister);
                        mNewBooksViewAdapter.refreshBook(position_unregister);
                        Toast.makeText(getContext(), getString(R.string.toast_success_unregister_book), Toast.LENGTH_SHORT).show();
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
        if (getActivity() instanceof MainActivity) {
            BookService service = ((MainActivity) getActivity()).getService();
            if (service != null) {
                if (D) Log.d(TAG, "cancelReload");
                service.cancelReload();

            }
        }
    }


    public void scrollToTop(){
        mRecyclerView.scrollToPosition(0);
    }

    public void checkReloadState(){
        if(getActivity() instanceof MainActivity){
            BookService service = ((MainActivity) getActivity()).getService();
            if(service != null && service.getServiceState() == BookService.STATE_NEW_BOOKS_RELOAD_COMPLETE){
                mReloadState = BookService.STATE_NEW_BOOKS_RELOAD_COMPLETE;
                loadNewBooksResult();
            }
        }
    }

    private void loadNewBooksResult() {
        if(D) Log.d(TAG, "loadNewBooksResult()");
        if (getActivity() instanceof MainActivity) {
            BookService service = ((MainActivity) getActivity()).getService();
            if (service != null) {
                Result result = service.getResult();
                if (result.isSuccess()) {
                    scrollToTop();
                    mNewBooksViewAdapter.replaceBooksData(mDBOpenHelper.loadNewBooks());
                    Toast.makeText(getContext(), getString(R.string.toast_success_reload), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
                service.setServiceState(BookService.STATE_NONE);
                service.stopForeground(false);
                service.stopSelf();
            }
        }

        mReloadState = BookService.STATE_NONE;
        ProgressDialogFragment.dismissProgressDialog(this);
    }


    @Override
    protected void onReceiveLocalBroadcast(Context context, Intent intent){
        String action = intent.getAction();
        if(action != null){
            switch (action){
                case BookService.FILTER_ACTION_UPDATE_SERVICE_STATE:
                    int state = intent.getIntExtra(BookService.KEY_SERVICE_STATE, 0);
                    switch (state) {
                        case BookService.STATE_NONE:
                            if (D) Log.d(TAG, "STATE_NONE");
                            mReloadState = BookService.STATE_NONE;
                            ProgressDialogFragment.dismissProgressDialog(this);
                            break;
                        case BookService.STATE_NEW_BOOKS_RELOAD_INCOMPLETE:
                            if (D) Log.d(TAG, "STATE_NEW_BOOKS_RELOAD_INCOMPLETE");
                            break;
                        case BookService.STATE_NEW_BOOKS_RELOAD_COMPLETE:
                            if (D) Log.d(TAG, "STATE_NEW_BOOKS_RELOAD_COMPLETE");
                            loadNewBooksResult();
                            break;
                    }
                    break;
                case BaseThread.FILTER_ACTION_UPDATE_PROGRESS:
                    String progress = intent.getStringExtra(BaseThread.KEY_PROGRESS_VALUE_TEXT);
                    if(progress == null){
                        progress = "";
                    }
                    String message = intent.getStringExtra(BaseThread.KEY_PROGRESS_MESSAGE_TEXT);
                    if(message == null){
                        message = "";
                    }
                    Bundle bundle = new Bundle();
//                    bundle.putString(ProgressDialogFragment.message, message);
//                    bundle.putString(ProgressDialogFragment.progress, progress);
                    bundle.putString(ProgressDialogFragment.KEY_MESSAGE, message);
                    bundle.putString(ProgressDialogFragment.KEY_PROGRESS, progress);

                    ProgressDialogFragment.updateProgress(this, bundle);

//                    Message msg = getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_UPDATE);
//                    msg.setData(bundle);
//                    getPausedHandler().sendMessage(msg);
//                    getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_UPDATE, bundle).sendToTarget();
                    break;
            }
        }
    }



























}
