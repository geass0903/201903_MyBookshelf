package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class NewFragment extends BaseFragment implements ShelfBooksViewAdapter.OnBookClickListener{
    public static final String TAG = NewFragment.class.getSimpleName();
    private static final boolean D = true;

    private MyBookshelfApplicationData mData;
    private Context mContext;

    List<String> authors_list = new ArrayList<>();
    private ShelfBooksViewAdapter mBooksViewAdapter;
    private LinearLayout mLinearLayout_Progress;
    private TextView mTextView_Progress;
    private Handler mHandler = new Handler();
    int size = 0;

    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mContext = context;
        mData = (MyBookshelfApplicationData) context.getApplicationContext();
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
        mLinearLayout_Progress = view.findViewById(R.id.fragment_new_progress_view);
        mTextView_Progress = view.findViewById(R.id.fragment_new_progress_text_progress);

        Toolbar toolbar = view.findViewById(R.id.fragment_new_toolbar);
        toolbar.setTitle(R.string.navigation_title_new);

        toolbar.inflateMenu(R.menu.menu_new);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_new_action_renew:
                        if (D) Log.d(TAG, "menu new action renew");
                        if(authors_list.size() > 0){
                            mLinearLayout_Progress.setVisibility(View.VISIBLE);
                            if(mFragmentListener != null) {
                                mFragmentListener.onFragmentEvent(FragmentEvent.DISP_MASK);
                            }
                            String text = "0" + "/" + size;
                            mTextView_Progress.setText(text);
                            String search = authors_list.get(0);
                            authors_list.remove(0);
                            MyBookshelfDBOpenHelper helper = mData.getDatabaseHelper();
                            helper.getWritableDatabase().beginTransaction();
                            AsyncSearchTask(search,1);
                        }
                        break;
                }
                return false;
            }
        });
    }


    private void initBooksViewAdapter(){
        mBooksViewAdapter = new ShelfBooksViewAdapter(mData.getBooksListNew(),false);
        mBooksViewAdapter.setContext(getContext());
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

                if(result) {
                    try {
                        if(json.has("Items")) {
                            JSONArray jsonArray = json.getJSONArray("Items");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject data = jsonArray.getJSONObject(i);
                                if(D) Log.d(TAG,"sb: " + data.toString());
                                BookData book = new BookData();
                                book.setView_type(ShelfBooksViewAdapter.VIEW_TYPE_BOOK);

                                String isbn = getParam(data,"isbn");
                                book.setIsbn(isbn);
                                String title = getParam(data,"title");
                                book.setTitle(title);
                                String author = getParam(data,"author");
                                book.setAuthor(author);
                                String imageUrl = getParam(data,"largeImageUrl");
                                book.setImage(imageUrl);
                                String publisher = getParam(data,"publisherName");
                                book.setPublisher(publisher);
                                String salesDate = getParam(data,"salesDate");
                                book.setSalesDate(salesDate);
                                String itemPrice = getParam(data,"itemPrice");
                                book.setItemPrice(itemPrice);
                                String rakutenUrl = getParam(data,"rakutenUrl");
                                book.setRakutenUrl(rakutenUrl);

                                MyBookshelfDBOpenHelper helper = mData.getDatabaseHelper();
                                helper.addNewBook(book);

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
                                footer.setView_type(ShelfBooksViewAdapter.VIEW_TYPE_BUTTON_LOAD);
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
                if(authors_list.size() > 0){
                    int now = size - authors_list.size();
                    String text = now + "/" + size;
                    mTextView_Progress.setText(text);
                    String search = authors_list.get(0);
                    authors_list.remove(0);
                    AsyncSearchTask(search,1);
                }else{
                    MyBookshelfDBOpenHelper helper = mData.getDatabaseHelper();
                    helper.getWritableDatabase().setTransactionSuccessful();
                    helper.getWritableDatabase().endTransaction();
                    mLinearLayout_Progress.setVisibility(View.GONE);
                    if(mFragmentListener != null) {
                        mFragmentListener.onFragmentEvent(FragmentEvent.REMOVE_MASK);
                    }
                }
            }
        },1500);
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
        new NewFragment.AsyncSearch(this,search,page).execute();
    }

    @Override
    public void onBookClick(ShelfBooksViewAdapter adapter, int position, BookData data) {

    }

    @Override
    public void onBookLongClick(ShelfBooksViewAdapter adapter, int position, BookData data) {

    }


    private static class AsyncSearch extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<NewFragment> mFragmentReference;
        final String keyword;
        final int page;
        JSONObject json;

        private AsyncSearch(NewFragment fragment, String keyword, int page) {
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
