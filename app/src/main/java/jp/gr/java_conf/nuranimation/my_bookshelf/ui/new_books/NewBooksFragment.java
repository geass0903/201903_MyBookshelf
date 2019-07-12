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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.database.MyBookshelfDBOpenHelper;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.Result;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.thread.base.BaseThread;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.BookDataUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.CalendarUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.service.BookService;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.MyBookshelfEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.book_detail.BookDetailFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.NormalDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.ProgressDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.util.BooksRecyclerViewAdapter;


public class NewBooksFragment extends BaseFragment implements BooksRecyclerViewAdapter.OnBookClickListener, NormalDialogFragment.OnNormalDialogListener, ProgressDialogFragment.OnProgressDialogListener {
    private static final String TAG = NewBooksFragment.class.getSimpleName();
    private static final boolean D = true;

    public static final String KEY_AUTHORS_LIST = "NewBooksFragment.KEY_AUTHORS_LIST";

    private static final String KEY_LAYOUT_MANAGER = "NewBooksFragment.KEY_LAYOUT_MANAGER";
    private static final String KEY_POSITION = "NewBooksFragment.KEY_POSITION";
    private static final String KEY_BOOK_DATA = "NewBooksFragment.KEY_BOOK_DATA";

    private static final String TAG_REGISTER_BOOK_DIALOG = "NewBooksFragment.TAG_REGISTER_BOOK_DIALOG";
    private static final String TAG_UNREGISTER_BOOK_DIALOG = "NewBooksFragment.TAG_UNREGISTER_BOOK_DIALOG";
    private static final String TAG_RELOAD_PROGRESS_DIALOG = "NewBooksFragment.TAG_RELOAD_PROGRESS_DIALOG";

    private static final int REQUEST_CODE_UNREGISTER_BOOK = 101;
    private static final int REQUEST_CODE_REGISTER_BOOK = 102;
    private static final int REQUEST_CODE_RELOAD_PROGRESS_DIALOG = 103;

    private MyBookshelfDBOpenHelper mDBOpenHelper;
    private BooksRecyclerViewAdapter mNewBooksViewAdapter;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private List<BookData> mNewBooks;


    @Override
    public void onAttach(Context context) {
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (D) Log.d(TAG, "onViewCreated");
        if (getActivity() != null) {
            getActivity().setTitle(R.string.navigation_item_new_books);
        }
        mLayoutManager = new LinearLayoutManager(view.getContext());
        if (savedInstanceState != null) {
            Parcelable mListState = savedInstanceState.getParcelable(KEY_LAYOUT_MANAGER);
            if (mListState != null) {
                mLayoutManager.onRestoreInstanceState(mListState);
            }
        }
        if (mNewBooks == null) {
            mNewBooks = mDBOpenHelper.loadNewBooks();
        }
        mNewBooksViewAdapter = new BooksRecyclerViewAdapter(getContext(), mNewBooks, BooksRecyclerViewAdapter.LIST_TYPE_NEW_BOOKS);
        mNewBooksViewAdapter.setClickListener(this);
        mRecyclerView = view.findViewById(R.id.view_recyclerview);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mNewBooksViewAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (D) Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (D) Log.d(TAG, "onResume()");
        getFragmentListener().onFragmentEvent(MyBookshelfEvent.CHECK_RELOAD_STATE, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (D) Log.d(TAG, "onPause()");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_LAYOUT_MANAGER, mLayoutManager.onSaveInstanceState());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_new, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_new_action_reload).getIcon().setColorFilter(Color.argb(255, 255, 255, 255), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new_action_reload) {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(KEY_AUTHORS_LIST, new ArrayList<>(mDBOpenHelper.loadAuthorsList()));
            getFragmentListener().onFragmentEvent(MyBookshelfEvent.START_RELOAD_NEW_BOOKS, bundle);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBookClick(BooksRecyclerViewAdapter adapter, int position, BookData data) {
        if (isClickable()) {
            waitClickable(500);
            int view_type = adapter.getItemViewType(position);
            if (view_type == BookData.TYPE_BOOK) {
                Bundle bundle = new Bundle();
                BookData book = mDBOpenHelper.loadBookDataFromShelfBooks(data);
                if (book.getView_type() == BookData.TYPE_EMPTY) {
                    book = new BookData(data);
                    book.setRating(BookDataUtils.convertRating(0.0f));
                    book.setReadStatus(BookData.STATUS_NONE);
                }
                bundle.putParcelable(BookDetailFragment.KEY_BOOK_DATA, book);
                getFragmentListener().onFragmentEvent(MyBookshelfEvent.GO_TO_BOOK_DETAIL, bundle);
            }
        }
    }

    @Override
    public void onBookLongClick(BooksRecyclerViewAdapter adapter, int position, BookData data) {
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
                if (registered.getView_type() == BookData.TYPE_BOOK) {
                    // registered. delete Dialog
                    bundle.putString(NormalDialogFragment.KEY_TITLE, getString(R.string.dialog_title_unregister_book));
                    bundle.putString(NormalDialogFragment.KEY_MESSAGE, getString(R.string.dialog_message_unregister_book));
                    bundle.putString(NormalDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
                    bundle.putString(NormalDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
                    bundle.putInt(NormalDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_UNREGISTER_BOOK);
                    bundle.putBundle(NormalDialogFragment.KEY_PARAMS, bundle_book);
                    bundle.putBoolean(NormalDialogFragment.KEY_CANCELABLE, true);
                    NormalDialogFragment.showNormalDialog(this, bundle, TAG_UNREGISTER_BOOK_DIALOG);
                } else {
                    // unregistered. register Dialog
                    bundle.putString(NormalDialogFragment.KEY_TITLE, getString(R.string.dialog_title_register_book));
                    bundle.putString(NormalDialogFragment.KEY_MESSAGE, getString(R.string.dialog_message_register_book));
                    bundle.putString(NormalDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
                    bundle.putString(NormalDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
                    bundle.putInt(NormalDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_REGISTER_BOOK);
                    bundle.putBundle(NormalDialogFragment.KEY_PARAMS, bundle_book);
                    bundle.putBoolean(NormalDialogFragment.KEY_CANCELABLE, true);
                    NormalDialogFragment.showNormalDialog(this, bundle, TAG_REGISTER_BOOK_DIALOG);
                }

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
                        mNewBooksViewAdapter.refreshBook(position_register);
                        Toast.makeText(getContext(), getString(R.string.toast_success_register), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_CODE_UNREGISTER_BOOK:
                    int position_unregister = params.getInt(KEY_POSITION, -1);
                    BookData book_unregister = params.getParcelable(KEY_BOOK_DATA);
                    if (book_unregister != null) {
                        mDBOpenHelper.unregisterFromShelfBooks(book_unregister);
                        mNewBooksViewAdapter.refreshBook(position_unregister);
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
        if (requestCode == REQUEST_CODE_RELOAD_PROGRESS_DIALOG) {
            getFragmentListener().onFragmentEvent(MyBookshelfEvent.CANCEL_RELOAD_NEW_BOOKS, null);
        }
    }

    @Override
    protected void onReceiveLocalBroadcast(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case BookService.FILTER_ACTION_UPDATE_SERVICE_STATE:
                    int state = intent.getIntExtra(BookService.KEY_SERVICE_STATE, BookService.STATE_NONE);
                    switch (state) {
                        case BookService.STATE_NONE:
                            if (D) Log.d(TAG, "STATE_NONE");
                            break;
                        case BookService.STATE_NEW_BOOKS_RELOAD_INCOMPLETE:
                            if (D) Log.d(TAG, "STATE_NEW_BOOKS_RELOAD_INCOMPLETE");
                            break;
                        case BookService.STATE_NEW_BOOKS_RELOAD_COMPLETE:
                            if (D) Log.d(TAG, "STATE_NEW_BOOKS_RELOAD_COMPLETE");
                            getFragmentListener().onFragmentEvent(MyBookshelfEvent.FINISH_RELOAD_NEW_BOOKS, null);
                            break;
                    }
                    break;
                case BaseThread.FILTER_ACTION_UPDATE_PROGRESS:
                    String progress = intent.getStringExtra(BaseThread.KEY_PROGRESS_VALUE_TEXT);
                    if (progress == null) {
                        progress = "";
                    }
                    String message = intent.getStringExtra(BaseThread.KEY_PROGRESS_MESSAGE_TEXT);
                    if (message == null) {
                        message = "";
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(ProgressDialogFragment.KEY_MESSAGE, message);
                    bundle.putString(ProgressDialogFragment.KEY_PROGRESS, progress);
                    ProgressDialogFragment.updateProgress(this, bundle, TAG_RELOAD_PROGRESS_DIALOG);
                    break;
            }
        }
    }

    public void scrollToTop() {
        mRecyclerView.scrollToPosition(0);
    }

    public void startReloadNewBooks(){
        Bundle bundle = new Bundle();
        bundle.putInt(ProgressDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_RELOAD_PROGRESS_DIALOG);
        bundle.putString(ProgressDialogFragment.KEY_TITLE, getString(R.string.progress_title_reload_new_books));
        bundle.putBoolean(ProgressDialogFragment.KEY_CANCELABLE, true);
        ProgressDialogFragment.showProgressDialog(this, bundle, TAG_RELOAD_PROGRESS_DIALOG);
    }

    public void checkReloadState(int state) {
        if (state == BookService.STATE_NEW_BOOKS_RELOAD_COMPLETE) {
            getFragmentListener().onFragmentEvent(MyBookshelfEvent.FINISH_RELOAD_NEW_BOOKS, null);
        }
    }

    public void finishReloadNewBooks(Result result){
        if (result.isSuccess()) {
            scrollToTop();
            mNewBooksViewAdapter.replaceBooksData(mDBOpenHelper.loadNewBooks());
            Toast.makeText(getContext(), getString(R.string.toast_success_reload), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), result.getErrorMessage(), Toast.LENGTH_SHORT).show();
        }
        ProgressDialogFragment.dismissProgressDialog(this, TAG_RELOAD_PROGRESS_DIALOG);
    }

}
