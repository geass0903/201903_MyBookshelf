package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FragmentBookDetail extends BaseFragment{
    public static final String TAG = FragmentBookDetail.class.getSimpleName();
    private static final boolean D = true;

    public static final String KEY_bundle_book = "Key_bundle_book";

    private MyBookshelfApplicationData mData;
    TextView titleView;
    TextView authorView;
    SimpleDraweeView draweeView;



    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mData = (MyBookshelfApplicationData) context.getApplicationContext();
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
        draweeView = view.findViewById(R.id.book_detail_image);
        titleView = view.findViewById(R.id.book_detail_title);
        authorView = view.findViewById(R.id.book_detail_author);

        Bundle bundle = getArguments();
        if(bundle != null) {
            BookData book = bundle.getParcelable(KEY_bundle_book);
            if(book != null) {
                draweeView.setImageURI(getImageUri(book.getImage()));
                titleView.setText(book.getTitle());
                authorView.setText(book.getAuthor());
            }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(D) Log.d(TAG,"onCreateOptionsMenu()");
        inflater.inflate(R.menu.menu_new,menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(D) Log.d(TAG,"onPrepareOptionsMenu()");
    }

    private Uri getImageUri(String url){
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


}