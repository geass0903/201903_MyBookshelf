package jp.gr.java_conf.nuranimation.my_bookshelf.ui.book_detail;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import jp.gr.java_conf.nuranimation.my_bookshelf.model.database.MyBookshelfDBOpenHelper;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookDataUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.MyBookshelfEvent;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.ReadStatusSpinnerArrayAdapter;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.SpinnerItem;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.BaseDatePickerFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.BaseDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.BaseFragment;


public class BookDetailFragment extends BaseFragment implements BaseDatePickerFragment.OnBaseDateSetListener, BaseDialogFragment.OnBaseDialogListener{
    public static final String TAG = BookDetailFragment.class.getSimpleName();
    private static final boolean D = true;

    private static final int REQUEST_CODE_SALES_DATE = 100;
    private static final int REQUEST_CODE_READ_DATE  = 101;

    public static final String KEY_BUNDLE_BOOK = "KEY_BUNDLE_BOOK";
    public static final String KEY_BUNDLE_POSITION = "KEY_BUNDLE_POSITION";
    public static final String KEY_SAVED_BOOK = "KEY_SAVED_BOOK";
    public static final String KEY_SAVED_POSITION = "KEY_SAVED_POSITION";

    private MyBookshelfDBOpenHelper mDBOpenHelper;

    private SimpleDraweeView mBookImageView;
    private ReadStatusSpinnerArrayAdapter mArrayAdapter;
    private Spinner mSpinnerReadStatus;
    private RatingBar mRatingBar;
    private EditText titleView;
    private EditText authorView;
    private EditText publisherView;
    private TextView itemPriceView;
    private TextView isbnView;
    private TextView salesDateView;
    private TextView readDateView;




    private BookData detailBook = new BookData();
    private int position;


/*
    private String rakutenUrl;
    private String tags;
*/

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
            if(savedInstanceState.getParcelable(KEY_SAVED_BOOK) != null) {
                detailBook = savedInstanceState.getParcelable(KEY_SAVED_BOOK);
            }
            position = savedInstanceState.getInt(KEY_SAVED_POSITION, -1);
            initView(view,detailBook);
        }else{
            Bundle bundle = getArguments();
            if(bundle != null){
                if(bundle.getParcelable(KEY_BUNDLE_BOOK) != null) {
                    detailBook = bundle.getParcelable(KEY_BUNDLE_BOOK);
                }
                position = bundle.getInt(KEY_BUNDLE_POSITION, -1);
            }
            initView(view,detailBook);
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
    }


    @Override
    public void onPause() {
        super.onPause();
        detailBook = getBookData();
        if(D) Log.d(TAG,"onPause()");
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SAVED_BOOK,detailBook);
        outState.putInt(KEY_SAVED_POSITION, position);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail,menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_detail_action_register){
            if(D) Log.d(TAG,"detail action register");
            BookData book = getBookData();
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.JAPAN);
            String registerDate = sdf.format(calendar.getTime());
            book.setRegisterDate(registerDate);
            mDBOpenHelper.registerToShelfBooks(book);
            Toast.makeText(getContext(), getString(R.string.toast_success_register_book), Toast.LENGTH_SHORT).show();
            if(getFragmentListener() != null){
                getFragmentListener().onFragmentEvent(MyBookshelfEvent.POP_BACK_STACK_BOOK_DETAIL, null);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataSet(int requestCode, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.JAPAN);
        switch (requestCode) {
            case REQUEST_CODE_SALES_DATE:
                String sales_date = sdf.format(calendar.getTime());
                if (D) Log.d(TAG, "sales_date: " + sales_date);
                detailBook.setSalesDate(sales_date);
                salesDateView.setText(sales_date);
                break;
            case REQUEST_CODE_READ_DATE:
                String read_date = sdf.format(calendar.getTime());
                if (D) Log.d(TAG, "read_date: " + read_date);
                detailBook.setFinishReadDate(read_date);
                readDateView.setText(read_date);
                break;
        }
    }

    @Override
    public void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        super.onBaseDialogSucceeded(requestCode, resultCode, params);
        if(resultCode == DialogInterface.BUTTON_POSITIVE){
            switch (requestCode){
                case REQUEST_CODE_SALES_DATE:
                    salesDateView.setText(getString(R.string.label_no_data));
                    break;
                case REQUEST_CODE_READ_DATE:
                    readDateView.setText(getString(R.string.label_no_data));
                    break;
            }
        }
    }

    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {
        super.onBaseDialogCancelled(requestCode,params);
    }



    private void initView(View view,BookData book) {


        mBookImageView = view.findViewById(R.id.book_detail_image);
        titleView = view.findViewById(R.id.book_detail_title);
        authorView = view.findViewById(R.id.book_detail_author);
        publisherView = view.findViewById(R.id.book_detail_publisher);
        salesDateView = view.findViewById(R.id.book_detail_sales_date);
        itemPriceView = view.findViewById(R.id.book_detail_price);
        isbnView = view.findViewById(R.id.book_detail_isbn);
        readDateView = view.findViewById(R.id.book_detail_finish_read_date);
        readDateView.setOnClickListener(dateButtonOnClickListener);
        readDateView.setOnLongClickListener(dateButtonOnLongClickListener);
        mSpinnerReadStatus = view.findViewById(R.id.book_detail_spinner_read_status);
        mArrayAdapter = new ReadStatusSpinnerArrayAdapter(this.getContext(), R.layout.item_read_status_spinner, getSpinnerItem_ReadStatus());
        mSpinnerReadStatus.setAdapter(mArrayAdapter);
        mSpinnerReadStatus.setOnItemSelectedListener(listener_ReadStatus);
        mRatingBar = view.findViewById(R.id.book_detail_rating_bar);
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                detailBook.setRating(BookDataUtils.convertRating(rating));
            }
        });
        setBookData(book);
    }


    private void setBookData(BookData book){

        if(book != null) {
            mBookImageView.setImageURI(Uri.parse(BookDataUtils.parseUrlString(book.getImage(),BookDataUtils.IMAGE_TYPE_LARGE)));

            mBookImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(D) Log.d(TAG,"onClick");
                }
            });

            mBookImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(D) Log.d(TAG,"onLongClick");
                    return true;
                }
            });


            titleView.setText(book.getTitle());





            authorView.setText(book.getAuthor());
            publisherView.setText(book.getPublisher());
            if(!TextUtils.isEmpty(book.getSalesDate())) {
                salesDateView.setText(book.getSalesDate());
            }
            itemPriceView.setText(book.getItemPrice());
            isbnView.setText(book.getISBN());
            if(!TextUtils.isEmpty(book.getFinishReadDate())) {
                readDateView.setText(book.getFinishReadDate());
            }
            mSpinnerReadStatus.setSelection(mArrayAdapter.getPosition(book.getReadStatus()));
            mRatingBar.setRating(BookDataUtils.convertRating(book.getRating()));
        }
    }


    private BookData getBookData(){
        BookData book = new BookData(detailBook);
//        book.setImage(detailBook.getImage());
        book.setTitle(titleView.getText().toString());
        book.setAuthor(authorView.getText().toString());
        book.setPublisher(publisherView.getText().toString());
        book.setSalesDate(salesDateView.getText().toString());
        book.setItemPrice(itemPriceView.getText().toString());
        book.setISBN(isbnView.getText().toString());
        book.setFinishReadDate(readDateView.getText().toString());
        SpinnerItem item = (SpinnerItem)mSpinnerReadStatus.getSelectedItem();
        book.setReadStatus(item.getCode());
        book.setRating(BookDataUtils.convertRating(mRatingBar.getRating()));
        return book;
    }





    private View.OnClickListener dateButtonOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.book_detail_sales_date:
                    if (D) Log.d(TAG, "SalesDate on Click");
                    String salesDate = salesDateView.getText().toString();
                    showDatePicker(REQUEST_CODE_SALES_DATE,salesDate);
                    break;
                case R.id.book_detail_finish_read_date:
                    if (D) Log.d(TAG, "ReadDate on Click");
                    String readDate = readDateView.getText().toString();
                    showDatePicker(REQUEST_CODE_READ_DATE,readDate);
                    break;
            }
        }
    };


    private View.OnLongClickListener dateButtonOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.book_detail_sales_date:
                    if (D) Log.d(TAG, "SalesDate on LongClick");
                    showDeleteDateDialog(REQUEST_CODE_SALES_DATE);
                    return true;
                case R.id.book_detail_finish_read_date:
                    if (D) Log.d(TAG, "ReadDate on LongClick");
                    showDeleteDateDialog(REQUEST_CODE_READ_DATE);
                    return true;
            }
            return false;
        }
    };

    private AdapterView.OnItemSelectedListener listener_ReadStatus = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter,
                                   View v, int position, long id) {
            SpinnerItem item = (SpinnerItem) adapter.getItemAtPosition(position);
            if (D) Log.d(TAG, "selected: " + item.getLabel());
            detailBook.setReadStatus(item.getCode());
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapter) {
        }
    };

    private void showDatePicker(int requestCode,String date){
        if(getActivity() != null){
            Bundle mBundle_DatePicker = new Bundle();
            mBundle_DatePicker.putString(BaseDatePickerFragment.KEY_DATE, date);
            mBundle_DatePicker.putInt(BaseDatePickerFragment.KEY_REQUEST_CODE, requestCode);


            FragmentManager manager = getActivity().getSupportFragmentManager();
            BaseDatePickerFragment mDatePicker = BaseDatePickerFragment.newInstance(this,mBundle_DatePicker);
            mDatePicker.show(manager, BaseDatePickerFragment.TAG);
        }
    }

    private void showDeleteDateDialog(int requestCode) {
        if (getActivity() != null) {
            Bundle bundle = new Bundle();
            bundle.putString(BaseDialogFragment.KEY_TITLE, getString(R.string.dialog_title_clear_date));
            bundle.putString(BaseDialogFragment.KEY_MESSAGE, getString(R.string.dialog_message_clear_date));
            bundle.putString(BaseDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
            bundle.putString(BaseDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
            bundle.putInt(BaseDialogFragment.KEY_REQUEST_CODE, requestCode);
            bundle.putBoolean(BaseDialogFragment.KEY_CANCELABLE, true);
            FragmentManager manager = getActivity().getSupportFragmentManager();
            BaseDialogFragment fragment = BaseDialogFragment.newInstance(this, bundle);
            fragment.show(manager, BaseDialogFragment.TAG);
        }
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

}