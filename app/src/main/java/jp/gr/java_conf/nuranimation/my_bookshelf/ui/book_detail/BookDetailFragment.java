package jp.gr.java_conf.nuranimation.my_bookshelf.ui.book_detail;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.database.MyBookshelfDBOpenHelper;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.SpinnerItem;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.BookDataUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.CalendarUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.MyBookshelfEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.ReadStatusSpinnerArrayAdapter;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.NormalDatePicker;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.NormalDialogFragment;


public class BookDetailFragment extends BaseFragment implements NormalDatePicker.OnBaseDateSetListener, NormalDialogFragment.OnNormalDialogListener {
    private static final String TAG = BookDetailFragment.class.getSimpleName();
    private static final boolean D = true;

    private static final String TAG_DATE_PICKER = "BookDetailFragment.TAG_DATE_PICKER";
    private static final String TAG_CLEAR_DATE_DIALOG = "BookDetailFragment.TAG_CLEAR_DATE_DIALOG";

    private static final int REQUEST_CODE_FINISH_READ_DATE = 101;

    public static final String KEY_BOOK_DATA = "BookDetailFragment.KEY_BOOK_DATA";

    private MyBookshelfDBOpenHelper mDBOpenHelper;

    private BookData bookData;
    private ReadStatusSpinnerArrayAdapter mArrayAdapter;
    private SimpleDraweeView sdv_bookImage;
    private EditText et_title;
    private EditText et_author;
    private EditText et_publisher;
    private TextView tv_isbn;
    private TextView tv_salesDate;
    private TextView tv_itemPrice;
    private TextView tv_finishReadDate;
    private RatingBar rb_rating;
    private Spinner sp_readStatus;

    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mDBOpenHelper = MyBookshelfDBOpenHelper.getInstance(context.getApplicationContext());
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_book_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        if(D) Log.d(TAG,"onViewCreated");
        if(savedInstanceState != null){
            bookData = savedInstanceState.getParcelable(KEY_BOOK_DATA);
        }else{
            if(getArguments() != null) {
                bookData = getArguments().getParcelable(KEY_BOOK_DATA);
            }
        }
        initView(view);
        setBookDataToView(bookData);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null && bookData != null){
            et_title.setText(bookData.getTitle());
            et_author.setText(bookData.getAuthor());
            et_publisher.setText(bookData.getPublisher());
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
        outState.putParcelable(KEY_BOOK_DATA, bookData);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail,menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_detail_action_register).getIcon().setColorFilter(Color.argb(255,255,255,255), PorterDuff.Mode.SRC_ATOP);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_detail_action_register) {
            if (D) Log.d(TAG, "detail action register");
            BookData book = new BookData(bookData);
            String registerDate = CalendarUtils.parseCalendar(Calendar.getInstance());
            book.setRegisterDate(registerDate);
            mDBOpenHelper.registerToShelfBooks(book);
            Toast.makeText(getContext(), getString(R.string.toast_success_register_book), Toast.LENGTH_SHORT).show();
            getFragmentListener().onFragmentEvent(MyBookshelfEvent.POP_BACK_STACK, null);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataSet(int requestCode, Calendar calendar) {
        if (requestCode == REQUEST_CODE_FINISH_READ_DATE) {
            String read_date = CalendarUtils.parseCalendar(calendar);
            if (D) Log.d(TAG, "read_date: " + read_date);
            bookData.setFinishReadDate(read_date);
            tv_finishReadDate.setText(read_date);
        }
    }

    @Override
    public void onNormalDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        if(resultCode == DialogInterface.BUTTON_POSITIVE){
            if (requestCode == REQUEST_CODE_FINISH_READ_DATE) {
                bookData.setFinishReadDate("");
                tv_finishReadDate.setText(getString(R.string.label_no_data));
            }
        }
    }

    @Override
    public void onNormalDialogCancelled(int requestCode, Bundle params) {
        // Cancel
    }





    private void initView(View view){
        sdv_bookImage = view.findViewById(R.id.book_detail_image);
        et_title = view.findViewById(R.id.book_detail_title);
        et_author = view.findViewById(R.id.book_detail_author);
        et_publisher = view.findViewById(R.id.book_detail_publisher);
        tv_salesDate = view.findViewById(R.id.book_detail_sales_date);
        tv_itemPrice = view.findViewById(R.id.book_detail_price);
        tv_isbn = view.findViewById(R.id.book_detail_isbn);
        tv_finishReadDate = view.findViewById(R.id.book_detail_finish_read_date);
        sp_readStatus = view.findViewById(R.id.book_detail_spinner_read_status);
        rb_rating = view.findViewById(R.id.book_detail_rating_bar);
        Button bt_downloadBookData = view.findViewById(R.id.book_detail_button_download_book_data);
        Button bt_link_rakutenBooks = view.findViewById(R.id.book_detail_button_rakutenUrl);

        sdv_bookImage.setOnClickListener(mOnClickListener);
        sdv_bookImage.setOnLongClickListener(mOnLongClickListener);
        et_title.addTextChangedListener(new GenericTextWatcher(et_title));
        et_author.addTextChangedListener(new GenericTextWatcher(et_author));
        et_publisher.addTextChangedListener(new GenericTextWatcher(et_publisher));
        tv_finishReadDate.setOnClickListener(mOnClickListener);
        tv_finishReadDate.setOnLongClickListener(mOnLongClickListener);
        mArrayAdapter = new ReadStatusSpinnerArrayAdapter(this.getContext(), R.layout.item_read_status_spinner, getSpinnerItem_ReadStatus());
        sp_readStatus.setAdapter(mArrayAdapter);
        sp_readStatus.setOnItemSelectedListener(mOnItemSelectedListener);
        rb_rating.setOnRatingBarChangeListener(mOnRatingBarChangeListener);
        bt_downloadBookData.setOnClickListener(mOnClickListener);
        bt_link_rakutenBooks.setOnClickListener(mOnClickListener);
    }

    private void setBookDataToView(BookData bookData) {
        if (bookData != null) {
            if (sdv_bookImage != null) {
                String urlString = BookDataUtils.parseUrlString(bookData.getImage(), BookDataUtils.IMAGE_TYPE_LARGE);
                if (!TextUtils.isEmpty(urlString) && sdv_bookImage != null) {
                    sdv_bookImage.setImageURI(Uri.parse(urlString));
                }
            }
            if (tv_isbn != null) {
                if (!TextUtils.isEmpty(bookData.getISBN())) {
                    tv_isbn.setText(bookData.getISBN());
                }
            }
            if (tv_salesDate != null) {
                if (!TextUtils.isEmpty(bookData.getSalesDate())) {
                    tv_salesDate.setText(bookData.getSalesDate());
                } else {
                    tv_salesDate.setText(R.string.label_unknown);
                }
            }
            if (tv_itemPrice != null) {
                if (!TextUtils.isEmpty(bookData.getItemPrice())) {
                    String itemPrice = getString(R.string.label_price_yen_mark) + bookData.getItemPrice();
                    tv_itemPrice.setText(itemPrice);
                } else {
                    tv_itemPrice.setText(R.string.label_unknown);
                }
            }
            if (et_title != null) {
                if (!TextUtils.isEmpty(bookData.getTitle())) {
                    et_title.setText(bookData.getTitle());
                }
            }
            if (et_author != null) {
                if (!TextUtils.isEmpty(bookData.getAuthor())) {
                    et_author.setText(bookData.getAuthor());
                }
            }
            if (et_publisher != null) {
                if (!TextUtils.isEmpty(bookData.getPublisher())) {
                    et_publisher.setText(bookData.getPublisher());
                }
            }
            if (tv_finishReadDate != null) {
                if (!TextUtils.isEmpty(bookData.getFinishReadDate())) {
                    tv_finishReadDate.setText(bookData.getFinishReadDate());
                } else {
                    tv_finishReadDate.setText(R.string.label_no_data);
                }
            }
            if (sp_readStatus != null && mArrayAdapter != null) {
                if (!TextUtils.isEmpty(bookData.getReadStatus())) {
                    int position = mArrayAdapter.getPosition(bookData.getReadStatus());
                    sp_readStatus.setSelection(position);
                }
            }
            if (rb_rating != null) {
                if (!TextUtils.isEmpty(bookData.getRating())) {
                    float rating = BookDataUtils.convertRating(bookData.getRating());
                    rb_rating.setRating(rating);
                }
            }
        }
    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.book_detail_image:
                    if(D) Log.d(TAG,"onClick");
                    break;
                case R.id.book_detail_finish_read_date:
                    String readDate = tv_finishReadDate.getText().toString();
                    showDatePicker(REQUEST_CODE_FINISH_READ_DATE, readDate);
                    break;
                case R.id.book_detail_button_download_book_data:
                    if(D) Log.d(TAG,"onClick");
                    break;
                case R.id.book_detail_button_rakutenUrl:
                    if(D) Log.d(TAG,"onClick");
                    break;
            }
        }
    };

    private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.book_detail_image:
                    if(D) Log.d(TAG,"onLongClick");
                    return true;
                case R.id.book_detail_finish_read_date:
                    showClearDateDialog(REQUEST_CODE_FINISH_READ_DATE);
                    return true;
            }
            return false;
        }
    };

    private AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter,
                                   View v, int position, long id) {
            if(adapter.getItemAtPosition(position) instanceof SpinnerItem){
                SpinnerItem item = (SpinnerItem) adapter.getItemAtPosition(position);
                if (D) Log.d(TAG, "selected: " + item.getLabel());
                bookData.setReadStatus(item.getCode());
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> adapter) {
        }
    };

    private RatingBar.OnRatingBarChangeListener mOnRatingBarChangeListener = new RatingBar.OnRatingBarChangeListener() {
        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            int id = ratingBar.getId();
            if(id == R.id.book_detail_rating_bar) {
                bookData.setRating(BookDataUtils.convertRating(rating));
            }
        }
    };

    @SuppressWarnings("SameParameterValue")
    private void showDatePicker(int requestCode,String date){
        Calendar calendar = CalendarUtils.parseDateString(date);
        if(calendar == null){
            calendar = Calendar.getInstance();
        }
        Bundle bundle = new Bundle();
        bundle.putInt(NormalDatePicker.KEY_YEAR, calendar.get(Calendar.YEAR));
        bundle.putInt(NormalDatePicker.KEY_MONTH, calendar.get(Calendar.MONTH));
        bundle.putInt(NormalDatePicker.KEY_DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        bundle.putInt(NormalDatePicker.KEY_REQUEST_CODE, requestCode);
        NormalDatePicker.showNormalDatePicker(this, bundle, TAG_DATE_PICKER);
    }

    @SuppressWarnings("SameParameterValue")
    private void showClearDateDialog(int requestCode) {
        Bundle bundle = new Bundle();
        bundle.putString(NormalDialogFragment.KEY_TITLE, getString(R.string.dialog_title_clear_date));
        bundle.putString(NormalDialogFragment.KEY_MESSAGE, getString(R.string.dialog_message_clear_date));
        bundle.putString(NormalDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
        bundle.putString(NormalDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
        bundle.putInt(NormalDialogFragment.KEY_REQUEST_CODE, requestCode);
        bundle.putBoolean(NormalDialogFragment.KEY_CANCELABLE, true);
        NormalDialogFragment.showNormalDialog(this,bundle, TAG_CLEAR_DATE_DIALOG);
    }

    private List<SpinnerItem> getSpinnerItem_ReadStatus() {
        List<SpinnerItem> list = new ArrayList<>();
        list.add(new SpinnerItem(BookData.STATUS_INTERESTED,    getString(R.string.read_status_label_1)));
        list.add(new SpinnerItem(BookData.STATUS_UNREAD,        getString(R.string.read_status_label_2)));
        list.add(new SpinnerItem(BookData.STATUS_READING,       getString(R.string.read_status_label_3)));
        list.add(new SpinnerItem(BookData.STATUS_ALREADY_READ,  getString(R.string.read_status_label_4)));
        list.add(new SpinnerItem(BookData.STATUS_NONE,          getString(R.string.read_status_label_5)));
        return list;
    }

    private class GenericTextWatcher implements TextWatcher {
        private View view;
        int currentLength = 0;

        GenericTextWatcher(View view){
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            currentLength = s.toString().length();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().length() < currentLength) {
                return;
            }
            boolean unfixed = false;
            Object[] spanned = s.getSpans(0, s.length(), Object.class);
            if (spanned != null) {
                for (Object obj : spanned) {
                    if (obj instanceof android.text.style.UnderlineSpan) {
                        unfixed = true;
                    }
                }
            }
            if (!unfixed) {
                confirmString(view, s.toString());
            }

        }

        private void confirmString(View view, String text){
            if(D) Log.d(TAG, "Confirm: " + text);
            switch(view.getId()){
                case R.id.book_detail_title:
                    bookData.setTitle(text);
                    break;
                case R.id.book_detail_author:
                    bookData.setAuthor(text);
                    break;
                case R.id.book_detail_publisher:
                    bookData.setPublisher(text);
                    break;
            }
        }
    }


}