package jp.gr.java_conf.nuranimation.my_bookshelf.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.book.AsyncSearchBook;
import jp.gr.java_conf.nuranimation.my_bookshelf.utils.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.utils.MyBookshelfDBOpenHelper;
import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseProgressDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.book.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.book.BooksListViewAdapter;

public class NewBooksFragment extends BaseFragment implements BooksListViewAdapter.OnBookClickListener{
    public static final String TAG = NewBooksFragment.class.getSimpleName();
    private static final boolean D = true;

    private MyBookshelfApplicationData mData;
    private Context mContext;

    List<String> authors_list = new ArrayList<>();
    private BooksListViewAdapter mBooksViewAdapter;
    private Handler mHandler = new Handler();
    int size = 0;

    private Calendar cal_baseDate;
    private Calendar cal_salesDate;
    private MyBookshelfDBOpenHelper mDBOpenHelper;

    private String search_author;

    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mContext = context;
        mData = (MyBookshelfApplicationData) context.getApplicationContext();
        mDBOpenHelper = mData.getDatabaseHelper();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_new,menu);
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
                if(authors_list.size() > 0){
                    boolean isSuccess = mDBOpenHelper.deleteTABLE_NEW_BOOKS();
                    cal_baseDate = Calendar.getInstance();
                    cal_baseDate.add(Calendar.DAY_OF_MONTH,-14);
                    cal_salesDate = Calendar.getInstance();

                    String text = "0" + "/" + size;

                    Bundle bundle = new Bundle();
                    bundle.putString(BaseProgressDialogFragment.title, getString(R.string.Progress_Reload));
                    bundle.putString(BaseProgressDialogFragment.message, text);

                    PausedHandler mHandler = getPausedHandler();

                    Message msg = mHandler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_SHOW);
                    msg.setData(bundle);
                    mHandler.sendMessage(msg);
                    search_author = authors_list.get(0);
                    authors_list.remove(0);
                    AsyncSearchTask(search_author,1);
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authors_list.clear();
        authors_list.addAll(mData.getDatabaseHelper().getAuthors());
        size = authors_list.size();
        initBooksViewAdapter();
        initRecyclerView(view);
    }


    private void initBooksViewAdapter(){
        mBooksViewAdapter = new BooksListViewAdapter(getContext(),mData.getNewBooks(),false);
        mBooksViewAdapter.setClickListener(this);
    }


    private void initRecyclerView(View view){
        RecyclerView mRecyclerView = view.findViewById(R.id.fragment_new_recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mBooksViewAdapter);
    }


    public void callback(final boolean result, final JSONObject json){
        LoaderManager manager = LoaderManager.getInstance(this);
        manager.destroyLoader(0);



        getPausedHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DISMISS).sendToTarget();
                List<BookData> dataset = new ArrayList<>();


                int count = 0;
                int last = 0;

                mBooksViewAdapter.setFooter(null);
                mDBOpenHelper.getWritableDatabase().beginTransaction();
                if(result) {
                    try {
                        if(json.has("Items")) {
                            JSONArray jsonArray = json.getJSONArray("Items");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject data = jsonArray.getJSONObject(i);
                                if(D) Log.d(TAG,"sb: " + data.toString());
                                BookData book = new BookData();
                                book.setView_type(BooksListViewAdapter.VIEW_TYPE_BOOK);

                                String isbn = getParam(data,"isbn");
                                book.setISBN(isbn);
                                String imageUrl = getParam(data,"largeImageUrl");
                                book.setImage(imageUrl);
                                String title = getParam(data,"KEY_TITLE");
                                book.setTitle(title);
                                String author = getParam(data,"author");
                                book.setAuthor(author);
                                String publisher = getParam(data,"publisherName");
                                book.setPublisher(publisher);
                                String salesDate = getParam(data,"salesDate");
                                book.setSalesDate(salesDate);
                                String itemPrice = getParam(data,"itemPrice");
                                book.setItemPrice(itemPrice);
                                String rating = getParam(data,"reviewAverage");
                                book.setRating(rating);
                                String readStatus = "0"; // Unregistered
                                book.setReadStatus(readStatus);

                                if(checkNewBook(book)){

                                    if(D) Log.d(TAG,"Add Book KEY_TITLE: " + book.getTitle());
                                    if(D) Log.d(TAG,"Add Book author: " + book.getAuthor());
                                    if(D) Log.d(TAG,"Add Book search: " + search_author);
//                                    mDBOpenHelper.addNewBook(book);
                                    mDBOpenHelper.registerToNewBooks(book);
                                }

//                                dataset.add(book);
                            }



/*
                            if(json.has("count")) {
                                count = json.getInt("count");
                                if (D) Log.d(TAG, "count: " + count);
                            }

                            if(json.has("last")) {
                                last = json.getInt("last");
                                if (D) Log.d(TAG, "last: " + last);
                            }

                            mBooksViewAdapter.addBooksData(dataset);

                            if(count - last > 0){
                                BookData footer = new BookData();
                                footer.setView_type(BooksListViewAdapter.VIEW_TYPE_BUTTON_LOAD);
                                mBooksViewAdapter.setFooter(footer);
//                        mBooksViewAdapter.setLoadNext();
                            }else{
                                mBooksViewAdapter.setFooter(null);
                            }
*/
                        }




                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                mDBOpenHelper.getWritableDatabase().setTransactionSuccessful();
                mDBOpenHelper.getWritableDatabase().endTransaction();
                if(authors_list.size() > 0){
                    int now = size - authors_list.size();
                    String text = now + "/" + size;
                    getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_UPDATE, -1, -1, text).sendToTarget();

                    search_author = authors_list.get(0);
                    authors_list.remove(0);
                    AsyncSearchTask(search_author,1);
                }else{
//                    mData.updateList_NewBooks();
                    getPausedHandler().obtainMessage(BaseFragment.MESSAGE_PROGRESS_DISMISS).sendToTarget();

                }
            }
        },1200);
    }

    boolean checkNewBook(BookData book){
        if (checkAuthor(book)) {
            return checkSalesDate(book);
        }
        return false;
    }


    boolean checkAuthor(BookData book){
        String book_author = book.getAuthor();
        book_author = book_author.replaceAll("[\\x20\\u3000]","");
        return book_author.contains(search_author);
    }

    boolean checkSalesDate(BookData book){
        String salesDate = book.getSalesDate();
        int year;
        int month;
        int day;
        int startIdx;
        int endIdx;

        startIdx = salesDate.indexOf("年");
        if(startIdx != -1){
            year = Integer.parseInt(salesDate.substring(0,startIdx));
            if(D) Log.d(TAG,"year: " + year);
            endIdx = startIdx+1;
            startIdx = salesDate.indexOf("月",endIdx);
            if(startIdx != -1){
                month = Integer.parseInt(salesDate.substring(endIdx,startIdx));
                if(D) Log.d(TAG,"month: " + month);
                endIdx = startIdx+1;
                startIdx = salesDate.indexOf("日",endIdx);
                if(startIdx != -1){
                    day = Integer.parseInt(salesDate.substring(endIdx,startIdx));
                    if(D) Log.d(TAG,"day: " + day);
                    cal_salesDate.set(year,month-1,day);
                }else{
                    cal_salesDate.set(year,month-1,cal_salesDate.getActualMaximum(Calendar.DAY_OF_MONTH));
                }
                if(D) Log.d(TAG,"nowDate: " + cal_baseDate.getTime());
                if(D) Log.d(TAG,"textView_SalesDate: " + cal_salesDate.getTime());
                return cal_salesDate.compareTo(cal_baseDate) >= 0;
            }
        }
        return false;
    }




    String getParam(JSONObject json, String keyword){
        try {
            if (json.has(keyword)) {
                String param = json.getString(keyword);
                if(D) Log.d(TAG,keyword + ": " + param);
                return param;
            }
        }catch (JSONException e){
            if(D) Log.d(TAG,"JSONException");
            return "";
        }
        return "";
    }






    public void AsyncSearchTask(String search, int page) {
//        new NewBooksFragment.AsyncSearch(this,search,page).execute();

        Bundle bundle = new Bundle();
        bundle.putString("search",search);
        bundle.putInt("page",page);

        LoaderManager manager = LoaderManager.getInstance(this);


        manager.initLoader(0,bundle,mCallback);

    }

    @Override
    public void onBookClick(BooksListViewAdapter adapter, int position, BookData data) {

    }

    @Override
    public void onBookLongClick(BooksListViewAdapter adapter, int position, BookData data) {
    }


    private LoaderManager.LoaderCallbacks<AsyncSearchBook.Result> mCallback = new LoaderManager.LoaderCallbacks<AsyncSearchBook.Result>() {
        @NonNull
        @Override
        public Loader<AsyncSearchBook.Result> onCreateLoader(int i, @Nullable Bundle bundle) {
            String sort = "standard";
            String code = mData.getSharedPreferences().getString(MyBookshelfApplicationData.KEY_SEARCH_BOOKS_ORDER, getString(R.string.ShelfBooks_SortSetting_Code_SalesDate_Descending));
            if(code != null) {
                if (code.equals(getString(R.string.ShelfBooks_SortSetting_Code_SalesDate_Ascending))) {
                    sort = "+releaseDate";
                }
                if (code.equals(getString(R.string.ShelfBooks_SortSetting_Code_SalesDate_Descending))) {
                    sort = "-releaseDate";
                }
            }
            String keyword = "";
            int page = 1;
            if(bundle != null){
                keyword = bundle.getString("search","error");
                page = bundle.getInt("page",1);
            }
            return new AsyncSearchBook(getContext(),sort,keyword,page);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<AsyncSearchBook.Result> loader, AsyncSearchBook.Result result) {



//            callback(isSuccess.isSuccess,isSuccess.json);
        }

        @Override
        public void onLoaderReset(@NonNull Loader<AsyncSearchBook.Result> loader) {

        }
    };


}
