package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.Slide;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    public static final String TAG = SearchFragment.class.getSimpleName();

    private MyBookshelfApplicationData mData;
    private BooksViewAdapter mBooksViewAdapter;

    private SearchView mSearchView;
    private RecyclerView mRecyclerView;
    private List<BookData> mBooksListSearch = new ArrayList<>();

    String ARG_SEARCH_WORD;
    int ARG_SEARCH_PAGE;


    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        mData = (MyBookshelfApplicationData) context.getApplicationContext();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(D) Log.d(TAG,"onPause()");
//        mData.setSearchWord(ARG_SEARCH_WORD);
    }
    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.d(TAG,"onResume()");
//        ARG_SEARCH_WORD = mData.getSearchWord();
//        mSearchView.setQuery(mData.getSearchWord(),false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        if(D) Log.d(TAG,"onViewCreated");
        initSearchView(view);
        initRecyclerView(view);
        if(D) Log.d(TAG,"dataset size" + mData.getmBooksListSearch().size());
    }

    private void initRecyclerView(View view){
        if(mBooksViewAdapter == null) {
            mBooksViewAdapter = new BooksViewAdapter(mBooksListSearch,false);
            mBooksViewAdapter.setContext(getContext());
        }
        mBooksViewAdapter.setClickListener(this);

        mRecyclerView = view.findViewById(R.id.fragment_search_recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mBooksViewAdapter);
    }

    private void initSearchView(View view){
        mSearchView = view.findViewById(R.id.searchView);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setQueryHint(getString(R.string.search_input_hint));
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setQuery(mData.getSearchWord(),false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchWord) {
                mSearchView.clearFocus();
                if(!searchWord.equals("")){
                    if(D) Log.d(TAG,"Search: " + searchWord);
                    mBooksViewAdapter.clearBooksData();
                    ARG_SEARCH_WORD = searchWord;
                    ARG_SEARCH_PAGE = 1;
                    //                   new AsyncSearch(,ARG_SEARCH_WORD,ARG_SEARCH_PAGE).onPreExecute();
                    AsyncSearchTask(ARG_SEARCH_WORD, ARG_SEARCH_PAGE);
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String word) {
                ARG_SEARCH_WORD = word;
                return false;
            }
        });
    }


    public void callback(boolean result, JSONObject json){
        List<BookData> dataset = new ArrayList<>();


        int count = 0;
        int last = 0;


        if(result) {
            try {
                if(json.has("Items")) {
                    JSONArray jsonArray = json.getJSONArray("Items");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        if(D) Log.d(TAG,"sb: " + data.toString());
                        BookData book = new BookData();

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

                        book.setRegisteredFlag(false);
//                        boolean registeredFlag;

                        dataset.add(book);
                    }


                    if(json.has("count")) {
                        count = json.getInt("count");
                        if (D) Log.d(TAG, "count: " + count);
                    }

                    if(json.has("last")) {
                        last = json.getInt("last");
                        if (D) Log.d(TAG, "last: " + last);
                    }

                    if(ARG_SEARCH_PAGE > 1){
                        mBooksViewAdapter.finishLoadNext();
                    }

                    mBooksViewAdapter.addBooksData(dataset);
                    if(count - last > 0){
                        mBooksViewAdapter.setLoadNext();
                    }

                }


            }catch (JSONException e){
                e.printStackTrace();
            }
        }

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



    @Override
    public void onBookClick(BooksViewAdapter adapter, int position, BookData data) {
        if(position != adapter.getItemCount() && data != null){
            FragmentManager fragmentManager = getFragmentManager();
            if(fragmentManager != null){
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                BookDetailFragment fragment = new BookDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString(BookDetailFragment.ARG_KEY_IMAGE,data.getImage());
                bundle.putString(BookDetailFragment.ARG_KEY_TITLE,data.getTitle());
                bundle.putString(BookDetailFragment.ARG_KEY_AUTHOR,data.getAuthor());
                fragment.setArguments(bundle);
                Slide slide = new Slide();
                slide.setSlideEdge(Gravity.END);
                fragment.setEnterTransition(slide);
                fragmentTransaction.replace(R.id.contents_container, fragment,BookDetailFragment.TAG);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }else{
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



    public void AsyncSearchTask(String search, int page) {
        new AsyncSearch(this,search,page).execute();
    }


    private static class AsyncSearch extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<SearchFragment> mFragmentReference;
        final String keyword;
        final int page;
        JSONObject json;

        private AsyncSearch(SearchFragment fragment, String keyword, int page){
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
            try{
                String urlBase = "https://app.rakuten.co.jp/services/api/BooksTotal/Search/20170404?applicationId=1028251347039610250";
                String urlFormat = "&format=" + "json";
                String urlFormatVersion = "&formatVersion=" + "2";
                String urlGenre = "&booksGenreId=" + "001"; // Books
                String urlHits = "&hits=20";
                String urlPage = "&page=" + String.valueOf(page);
                String urlStockFlag = "&outOfStockFlag=" + "1";
                String urlField = "&field=" + "0";
                String urlSort = "&sort=" + URLEncoder.encode("-releaseDate","UTF-8");
                String urlKeyword = "&keyword=" + URLEncoder.encode(keyword);

                String urlString = urlBase + urlFormat + urlFormatVersion + urlGenre + urlHits
                        + urlPage + urlStockFlag + urlField + urlSort + urlKeyword;
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
