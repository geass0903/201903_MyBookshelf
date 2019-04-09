package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;

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

public class SearchFragment extends BaseFragment implements BooksViewAdapter.OnBookClickListener{
    private static final boolean D = true;
    private static final String TAG = SearchFragment.class.getSimpleName();

    private MyBookshelfApplicationData mData;
    private Context mContext;
    private BooksViewAdapter adapter;
    private List<BookData> dataset = new ArrayList<>();

    private Spinner mSpinner;
    private EditText mEditText;

    private RecyclerView mRecyclerView;

    String ARG_SEARCH_WORD;
    int ARG_SEARCH_PAGE = 1;


    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        mContext = context;
        mData = (MyBookshelfApplicationData) context.getApplicationContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = view.findViewById(R.id.fragment_search_toolbar);
        toolbar.setTitle(R.string.navigation_title_search);
        toolbar.inflateMenu(R.menu.menu_search);

        mSpinner = view.findViewById(R.id.fragment_search_spinner_item);
        mEditText = view.findViewById(R.id.fragment_search_input_item);
        mEditText.setOnKeyListener(onKeyListener);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.menu_search_action_search:
                        int position = mSpinner.getSelectedItemPosition();
                        String keyword = mEditText.getText().toString();
                        String search_type;
                        switch (position){
                            case 0:
                                search_type = "&title=";
                                break;
                            case 1:
                                search_type = "&author=";
                                break;
                            default:
                                search_type = "&title=";
                                break;
                        }
                        if(D) Log.d(TAG,"Search: " + search_type + " = " + keyword);
                        closeKeyBoard();
                        adapter.clearBooksData();
                        ARG_SEARCH_WORD = search_type + URLEncoder.encode(keyword);
                        ARG_SEARCH_PAGE = 1;
                        AsyncSearchTask(ARG_SEARCH_WORD, ARG_SEARCH_PAGE);
                        break;
                }
                return false;
            }
        });

        mRecyclerView = view.findViewById(R.id.fragment_search_recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        adapter = new BooksViewAdapter(dataset);
        adapter.setClickListener(this);
        mRecyclerView.setAdapter(adapter);


//        adapter.setLoadNext();




//        SetShelfRowData(recyclerView);

    }


    public void AsyncSearchTask(String search, int page) {
        new AsyncSearch(this,search,page).execute();
    }

    View.OnKeyListener onKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_ENTER) {
                //ソフトキーボードを閉じる
                closeKeyBoard();
                return true;
            }
            return false;
        }
    };

    private void closeKeyBoard(){
        if(getActivity() != null && getView() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }



    public void callback(boolean result, JSONObject json){
        List<BookData> dataset = new ArrayList<>();

        String title = null;
        String author = null;
        String imageUrl = null;

        int count = 0;
        int first = 0;
        int last = 0;

        int size = dataset.size();
        int add_size;


        if(result) {
            try {
                if(json.has("Items")) {
                    JSONArray jsonArray = json.getJSONArray("Items");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        if(D) Log.d(TAG,"sb: " + data.toString());

                        if(data.has("title")) {
                            title = data.getString("title");
                            if (D) Log.d(TAG, "title: " + title);
                        }
                        if(data.has("author")) {
                            author = data.getString("author");
                            if (D) Log.d(TAG, "author: " + author);
                        }
                        if(data.has("largeImageUrl")){
                            imageUrl = data.getString("largeImageUrl");
                            if(D) Log.d(TAG,"author: " + imageUrl);
                        }

                        BookData book = new BookData();
                        book.setImage(imageUrl);
                        book.setTitle(title);
                        book.setAuthor(author);
                        book.setPublisher("publisher");
                        dataset.add(book);
                    }


                    if(json.has("count")) {
                        count = json.getInt("count");
                        if (D) Log.d(TAG, "count: " + count);
                    }

                    if(json.has("first")) {
                        first = json.getInt("first");
                        if (D) Log.d(TAG, "first: " + first);
                    }

                    if(json.has("last")) {
                        last = json.getInt("last");
                        if (D) Log.d(TAG, "last: " + last);
                    }

                    add_size = last - first + 1;
                    if (D) Log.d(TAG, "add_size: " + add_size);



                    if(ARG_SEARCH_PAGE > 1){
                        adapter.finishLoadNext();
                    }

                    adapter.addBooksData(dataset);
                    if(count - last > 0){
                        adapter.setLoadNext();
                        add_size++;
                    }
//                    adapter.notifyItemRemoved(1);

//                    adapter = new BooksViewAdapter(dataset);
//                    adapter.setLoadNext();
//                    adapter.addButtonLoad();
//                    adapter.setClickListener(this);
//                    mRecyclerView.setAdapter(adapter);


                }







                /*
                "count":40,"page":1,"first":1,"last":30,"hits":30,"carrier":0,"pageCount":2,

                 */


            }catch (JSONException e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onBookClick(BooksViewAdapter adapter, int position, BookData data) {
        if(position != adapter.getItemCount() && data != null){
            String title = data.getTitle();
            if(D) Log.d(TAG,"Click: " + title);
        }else{
            // Footer
            adapter.startLoadNext();
            ARG_SEARCH_PAGE++;
            AsyncSearchTask(ARG_SEARCH_WORD, ARG_SEARCH_PAGE);
        }

    }

    @Override
    public void onBookLongClick(BooksViewAdapter adapter, int position, BookData data) {
        if(position != adapter.getItemCount() && data != null){
            String title = data.getTitle();
            if(D) Log.d(TAG,"LongClick: " + title);
        }
    }


    private static class AsyncSearch extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<SearchFragment> mFragmentReference;
        final String search;
        final int page;
        JSONObject json;

        private AsyncSearch(SearchFragment fragment, String search,int page){
            this.mFragmentReference = new WeakReference<>(fragment);
            this.search = search;
            this.page = page;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean isSuccess = false;
            HttpsURLConnection connection = null;
            try{
                String urlBase = "https://app.rakuten.co.jp/services/api/BooksBook/Search/20170404?applicationId=1028251347039610250";
                String urlFormatVersion = "&formatVersion=" + "2";
                String urlHits = "&hits=20";
                String urlSort = "&sort=" + URLEncoder.encode("-releaseDate","UTF-8");
                String urlPage = "&page=" + page;
                String urlStockFlag = "&outOfStockFlag=" + 1;
                String urlString = urlBase + urlFormatVersion + urlHits + urlSort + urlPage + urlStockFlag + search;
                URL url = new URL(urlString);
                connection = (HttpsURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(false);
                connection.connect();

                int status = connection.getResponseCode();
                if(status == HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while( (line = reader.readLine()) != null){
                        sb.append(line);
                    }
                    reader.close();

                    if(D) Log.d(TAG,"sb: " + sb.toString());
                    json = new JSONObject(sb.toString());
                    isSuccess = true;
                }
            } catch (IOException | JSONException e){
                if(D) Log.d(TAG,"Error");
                e.printStackTrace();
            } finally {
                if(connection != null){
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
