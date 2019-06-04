package jp.gr.java_conf.nuranimation.my_bookshelf.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.gr.java_conf.nuranimation.my_bookshelf.base.BundleBuilder;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.adapter.ReadStatusSpinnerArrayAdapter;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseDatePickerFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseSpinnerItem;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.BookData;


public class BookDetailFragment extends BaseFragment implements BaseDatePickerFragment.OnBaseDateSetListener {
    public static final String TAG = BookDetailFragment.class.getSimpleName();
    private static final boolean D = true;

    private static final int REQUEST_CODE_SALES_DATE = 100;
    private static final int REQUEST_CODE_READ_DATE  = 101;

    public static final String KEY_BUNDLE_BOOK = "KEY_BUNDLE_BOOK";
    public static final String KEY_BUNDLE_POSITION = "KEY_BUNDLE_POSITION";
    public static final String KEY_SAVED_BOOK = "KEY_SAVED_BOOK";
    public static final String KEY_SAVED_POSITION = "KEY_SAVED_POSITION";

    private MyBookshelfApplicationData mApplicationData;


    private TextView salesDateView;
    private TextView readDateView;
    private TextView mRatingText;


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
        mApplicationData = (MyBookshelfApplicationData) context.getApplicationContext();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
        if(D) Log.d(TAG,"onCreateOptionsMenu()");
        inflater.inflate(R.menu.menu_detail,menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_detail_action_register).getIcon().setColorFilter(Color.argb(255,255,255,255), PorterDuff.Mode.SRC_ATOP);
        if(D) Log.d(TAG,"onPrepareOptionsMenu()");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_detail_action_register:
                if(D) Log.d(TAG,"detail action register");
                BookData book = new BookData(detailBook);
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN);
                String registerDate = sdf.format(calendar.getTime());
                book.setRegisterDate(registerDate);
                mApplicationData.registerToShelfBooks(book);
                Toast.makeText(getContext(), getString(R.string.Toast_Register_Book), Toast.LENGTH_SHORT).show();
                if(D) Log.d(TAG,"detail action register");
                Fragment fragment;
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null) {
                    fragment = getFragmentManager().findFragmentByTag(BookDetailFragment.TAG);
                    if (fragment instanceof BookDetailFragment) {
                        getFragmentManager().popBackStack();
                    }
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView(View view,BookData book) {
        SimpleDraweeView mBookImageView = view.findViewById(R.id.book_detail_image);
        EditText titleView = view.findViewById(R.id.book_detail_title);
        EditText authorView = view.findViewById(R.id.book_detail_author);
        EditText publisherView = view.findViewById(R.id.book_detail_publisher);
        salesDateView = view.findViewById(R.id.book_detail_sales_date);
        salesDateView.setOnClickListener(listener_DateButton);
        EditText itemPriceView = view.findViewById(R.id.book_detail_price);
        EditText isbnView = view.findViewById(R.id.book_detail_isbn);
        readDateView = view.findViewById(R.id.book_detail_read_date);
        readDateView.setOnClickListener(listener_DateButton);
        Spinner mSpinnerReadStatus = view.findViewById(R.id.book_detail_spinner_read_status);
        ReadStatusSpinnerArrayAdapter mArrayAdapter = new ReadStatusSpinnerArrayAdapter(this.getContext(), R.layout.litem_spinner_read_status, getSpinnerItem_ReadStatus());
        mSpinnerReadStatus.setAdapter(mArrayAdapter);
        mSpinnerReadStatus.setOnItemSelectedListener(listener_ReadStatus);
        mRatingText = view.findViewById(R.id.book_detail_rating_text);
        RatingBar mRatingBar = view.findViewById(R.id.book_detail_rating);
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                String rate = String.format(Locale.JAPAN,"%.1f",rating);
                String ratingText = String.format(Locale.JAPAN, "%.1f / 5.0", rating);
                detailBook.setRating(rate);
                mRatingText.setText(ratingText);
            }
        });


        if(book != null) {
            mBookImageView.setImageURI(getImageUri(book.getImage()));
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
            mSpinnerReadStatus.setSelection(mArrayAdapter.getPosition(book.getReadStatus()), false);
            mRatingBar.setRating(getRating(book.getRating()));
            String ratingText = String.format(Locale.JAPAN, "%.1f / 5.0", getRating(book.getRating()));
            mRatingText.setText(ratingText);
        }
    }


    private float getRating(String value){
        float rating = 0.0f;
        try {
            rating = Float.parseFloat(value);
        } catch (Exception e){
            e.printStackTrace();
        }
        return rating;
    }


    private Uri getImageUri(String url){
        if(TextUtils.isEmpty(url)){
            return null;
        }
        String REGEX_CSV_COMMA = ",";
        String REGEX_SURROUND_DOUBLE_QUOTATION = "^\"|\"$";
        String REGEX_SURROUND_BRACKET = "^\\(|\\)$";

        Pattern sdqPattern = Pattern.compile(REGEX_SURROUND_DOUBLE_QUOTATION);
        Matcher matcher = sdqPattern.matcher(url);
        url = matcher.replaceAll("");
        Pattern sbPattern = Pattern.compile(REGEX_SURROUND_BRACKET);
        matcher = sbPattern.matcher(url);
        url = matcher.replaceAll("");
        Pattern cPattern = Pattern.compile(REGEX_CSV_COMMA);
        String[] arr = cPattern.split(url, -1);
        return Uri.parse(arr[0]);
    }




    void showDatePicker(int requestCode,String date){
        if(getActivity() != null){
            Bundle mBundle_SalesDateDialog = new BundleBuilder()
                    .put(BaseDatePickerFragment.KEY_DATE,date)
                    .put(BaseDatePickerFragment.KEY_REQUEST_CODE,requestCode)
                    .build();
            FragmentManager manager = getActivity().getSupportFragmentManager();
            BaseDatePickerFragment mSalesDateDialog = BaseDatePickerFragment.newInstance(this,mBundle_SalesDateDialog);
            mSalesDateDialog.show(manager, BookDetailFragment.TAG);
        }
    }




    View.OnClickListener listener_DateButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.book_detail_sales_date:
                    if (D) Log.d(TAG, "SalesDate on Click");
                    String salesDate = salesDateView.getText().toString();
                    showDatePicker(REQUEST_CODE_SALES_DATE,salesDate);
                    break;
                case R.id.book_detail_read_date:
                    if (D) Log.d(TAG, "ReadDate on Click");
                    String readDate = readDateView.getText().toString();
                    showDatePicker(REQUEST_CODE_READ_DATE,readDate);
                    break;
            }
        }
    };

    AdapterView.OnItemSelectedListener listener_ReadStatus = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter,
                                   View v, int position, long id) {
            BaseSpinnerItem item = (BaseSpinnerItem) adapter.getItemAtPosition(position);
            if (D) Log.d(TAG, "selected: " + item.getLabel());
            detailBook.setReadStatus(item.getCode());
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapter) {
        }
    };


    private List<BaseSpinnerItem> getSpinnerItem_ReadStatus() {
        List<BaseSpinnerItem> list = new ArrayList<>();
        Resources res = getResources();
        TypedArray array = res.obtainTypedArray(R.array.BookDetail_ReadStatus_SpinnerItem);
        for (int i = 0; i < array.length(); ++i) {
            int id = array.getResourceId(i, -1);
            if (id > -1) {
                String[] item = res.getStringArray(id);
                list.add(new BaseSpinnerItem(item[0], item[1]));
            }
        }
        array.recycle();
        return list;
    }

    @Override
    public void onDataSet(int requestCode, Calendar calendar)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY年MM月dd日",Locale.JAPAN);
        switch (requestCode){
            case REQUEST_CODE_SALES_DATE:
                try {
                    String date = sdf.format(calendar.getTime());
                    if(D) Log.d(TAG,"date: " + date);
                    detailBook.setSalesDate(date);
                    salesDateView.setText(date);
                }catch (IllegalArgumentException e){
                    e.printStackTrace();
                }
                break;
            case REQUEST_CODE_READ_DATE:
                try {
                    String date = sdf.format(calendar.getTime());
                    if(D) Log.d(TAG,"date: " + date);
                    detailBook.setFinishReadDate(date);
                    readDateView.setText(date);
                }catch (IllegalArgumentException e){
                    e.printStackTrace();
                }
                break;
        }

    }
}