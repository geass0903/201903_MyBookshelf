package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class SearchFragment extends BaseFragment implements BooksViewAdapter.OnBookClickListener{
    private static final boolean D = true;
    private static final String TAG = SearchFragment.class.getSimpleName();

    private MyBookshelfApplicationData mData;
    private Context mContext;
    private BooksViewAdapter adapter;

    private Spinner mSpinner;
    private EditText mEditText;

    private RecyclerView mRecyclerView;


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
                        String search;
                        switch (position){
                            case 0:
                                search = "&title=";
                                break;
                            case 1:
                                search = "&author=";
                                break;
                            default:
                                search = "&title=";
                                break;
                        }
                        if(D) Log.d(TAG,"Search: " + search + " = " + keyword);
                        closeKeyBoard();
                        AsyncSearchTask(search,keyword);
                        break;
                }
                return false;
            }
        });

        mRecyclerView = view.findViewById(R.id.fragment_search_recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
//        SetShelfRowData(recyclerView);

    }



//        SearchView searchView= (SearchView) toolbar.getMenu().findItem(R.id.menu_search_action_search).getActionView();
//        searchView.setMaxWidth(Integer.MAX_VALUE);
/*
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(D) Log.d(TAG, "submit : " + s);
                new AsyncSearch().execute();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(D) Log.d(TAG, "change : " + s);
                return false;
            }
        });
*/


    public void AsyncSearchTask(String search, String keyword) {
        new AsyncSearch(this,search,keyword).execute();
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


    private void SetShelfRowData(RecyclerView recyclerView) {
        List<BookData> dataset = new ArrayList<>();


//        MyBookshelfDBOpenHelper helper = new MyBookshelfDBOpenHelper(mContext.getApplicationContext());
//        SQLiteDatabase db = helper.getReadableDatabase();
        SQLiteDatabase db = mData.getDataBase();

        long recodeCount = DatabaseUtils.queryNumEntries(db, "MY_BOOKSHELF");
        if(D) Log.d(TAG, "recodeCount : " + recodeCount);

        String sql = "SELECT * FROM MY_BOOKSHELF";


        Cursor c = db.rawQuery(sql, null);
        boolean mov = c.moveToFirst();

        while (mov) {
            BookData data = new BookData();
            data.setImage(c.getString(c.getColumnIndex("images")));
            data.setTitle(c.getString(c.getColumnIndex("title")));
            data.setAuthor(c.getString(c.getColumnIndex("author")));
            data.setPublisher(c.getString(c.getColumnIndex("publisherName")));
            dataset.add(data);
            mov = c.moveToNext();
        }
        c.close();

        BooksViewAdapter adapter = new BooksViewAdapter(dataset);
        recyclerView.setAdapter(adapter);
    }


    public void callback(boolean result, JSONObject jsonObject){

        JSONObject json = jsonObject;

        int count = 0;

        if(result) {
            try {

                if(json.has("count")){
                    if(D) Log.d(TAG,"count: " + json.getInt("count"));
                    count = json.getInt("count");
                }


                if(json.has("Items")) {
                    List<BookData> dataset = new ArrayList<>();
                    JSONArray jsonArray = json.getJSONArray("Items");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        // 名前を取得
                        if(D) Log.d(TAG,"sb: " + data.toString());
                        String title = data.getString("title");
                        if(D) Log.d(TAG,"title: " + title);
                        // 年齢を取得
                        String author = data.getString("titleKana");
                        if(D) Log.d(TAG,"author: " + author);

                        String imageurl = data.getString("largeImageUrl");
                        if(D) Log.d(TAG,"author: " + imageurl);

                        BookData book = new BookData();
                        book.setImage(imageurl);
                        book.setTitle(title);
                        book.setAuthor(author);
                        book.setPublisher("publisher");
                        dataset.add(book);


                    }

                    adapter = new BooksViewAdapter(dataset);
                    adapter.setClickListener(this);
                    mRecyclerView.setAdapter(adapter);


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

    }

    @Override
    public void onBookLongClick(BooksViewAdapter adapter, int position, BookData data) {

    }


    private static class AsyncSearch extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<SearchFragment> mFragmentReference;
        final String search;
        final String keyword;
        JSONObject json;

        private AsyncSearch(SearchFragment fragment, String spinner, String edit){
            this.mFragmentReference = new WeakReference<>(fragment);
            this.search = spinner;
            this.keyword = edit;
        }


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean isSuccess = false;
            HttpsURLConnection connection = null;
            JSONArray jsonArray = null;
            try{
                String urlBase = "https://app.rakuten.co.jp/services/api/BooksBook/Search/20170404?applicationId=1028251347039610250";
                String urlSort = "&sort=" + URLEncoder.encode("-releaseDate","UTF-8");
                String urlFormat = "&formatVersion=" + "2";
                String urlString = urlBase + search + URLEncoder.encode(keyword) + urlSort + urlFormat;
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

//                    JSONObject json = new JSONObject(sb.toString());

                    if(json.has("count")){
                        if(D) Log.d(TAG,"count: " + json.getString("count"));
                    }



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
