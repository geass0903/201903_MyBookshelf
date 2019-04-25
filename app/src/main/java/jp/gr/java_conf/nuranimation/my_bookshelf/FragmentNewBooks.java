package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class FragmentNewBooks extends BaseFragment implements BooksListViewAdapter.OnBookClickListener{
    public static final String TAG = FragmentNewBooks.class.getSimpleName();
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        authors_list.clear();
        authors_list.addAll(mData.getDatabaseHelper().getAuthors());
        size = authors_list.size();
        initBooksViewAdapter();
        initRecyclerView(view);

        Toolbar toolbar = view.findViewById(R.id.fragment_new_toolbar);
        toolbar.setTitle(R.string.Navigation_Item_NewBooks);

        toolbar.inflateMenu(R.menu.menu_new);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_new_action_renew:
                        if(authors_list.size() > 0){
                            mDBOpenHelper.deleteTABLE_NEW_BOOKS();
                            cal_baseDate = Calendar.getInstance();
                            cal_baseDate.add(Calendar.DAY_OF_MONTH,-14);
                            cal_salesDate = Calendar.getInstance();

                            String text = "0" + "/" + size;

                            Bundle bundle = new Bundle();
                            bundle.putString(BaseProgressDialogFragment.title, getString(R.string.Progress_Reload));
                            bundle.putString(BaseProgressDialogFragment.message, text);
                            Message msg = handler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_SHOW);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                            search_author = authors_list.get(0);
                            authors_list.remove(0);
                            AsyncSearchTask(search_author,1);
                        }
                        break;
                }
                return false;
            }
        });
    }


    private void initBooksViewAdapter(){
        mBooksViewAdapter = new BooksListViewAdapter(getContext(),mData.getList_NewBooks(),false);
        mBooksViewAdapter.setClickListener(this);
    }


    private void initRecyclerView(View view){
        RecyclerView mRecyclerView = view.findViewById(R.id.fragment_new_recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mBooksViewAdapter);
    }


    public void callback(final boolean result, final JSONObject json){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

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
                                book.setIsbn(isbn);
                                String imageUrl = getParam(data,"largeImageUrl");
                                book.setImage(imageUrl);
                                String title = getParam(data,"title");
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

                                    if(D) Log.d(TAG,"Add Book title: " + book.getTitle());
                                    if(D) Log.d(TAG,"Add Book author: " + book.getAuthor());
                                    if(D) Log.d(TAG,"Add Book search: " + search_author);
                                    mDBOpenHelper.addNewBook(book);
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
                    handler.obtainMessage(BaseFragment.MESSAGE_PROGRESS, -1, -1, text).sendToTarget();

                    search_author = authors_list.get(0);
                    authors_list.remove(0);
                    AsyncSearchTask(search_author,1);
                }else{
                    mData.updateList_NewBooks();
                    handler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_DISMISS).sendToTarget();
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
        new FragmentNewBooks.AsyncSearch(this,search,page).execute();
    }

    @Override
    public void onBookClick(BooksListViewAdapter adapter, int position, BookData data) {

    }

    @Override
    public void onBookLongClick(BooksListViewAdapter adapter, int position, BookData data) {

    }


    private static class AsyncSearch extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<FragmentNewBooks> mFragmentReference;
        final String keyword;
        final int page;
        JSONObject json;

        private AsyncSearch(FragmentNewBooks fragment, String keyword, int page) {
            this.mFragmentReference = new WeakReference<>(fragment);
            this.keyword = keyword;
            this.page = page;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean isSuccess = false;
            HttpsURLConnection connection = null;
            try {
                String urlBase = "https://app.rakuten.co.jp/services/api/BooksTotal/Search/20170404?applicationId=1028251347039610250";
                String urlFormat = "&format=" + "json";
                String urlFormatVersion = "&formatVersion=" + "2";
                String urlGenre = "&booksGenreId=" + "001"; // Books
                String urlHits = "&hits=20";
                String urlPage = "&page=" + String.valueOf(page);
                String urlStockFlag = "&outOfStockFlag=" + "1";
                String urlField = "&field=" + "0";
                String urlSort = "&sort=" + URLEncoder.encode("-releaseDate", "UTF-8");
                String urlKeyword = "&keyword=" + URLEncoder.encode(keyword);

                String urlString = urlBase + urlFormat + urlFormatVersion + urlGenre + urlHits
                        + urlPage + urlStockFlag + urlField + urlSort + urlKeyword;
                URL url = new URL(urlString);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(false);
                connection.connect();

                int status = connection.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    if (D) Log.d(TAG, "sb: " + sb.toString());
                    json = new JSONObject(sb.toString());
                    isSuccess = true;
                }
            } catch (IOException | JSONException e) {
                if (D) Log.d(TAG, "Error");
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            mFragmentReference.get().callback(result, json);
        }
    }







}
