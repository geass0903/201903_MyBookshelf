package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentSearchBooks extends BaseFragment implements BooksListViewAdapter.OnBookClickListener{
    private static final boolean D = true;
    public static final String TAG = FragmentSearchBooks.class.getSimpleName();

    private MyBookshelfApplicationData mData;
    private Handler mHandler = new Handler();
    private BooksListViewAdapter mBooksViewAdapter;
    private SearchView mSearchView;
    private List<BookData> mList_SearchResult = new ArrayList<>();
    private String searchKeyword;
    private int searchPage;

    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        mData = (MyBookshelfApplicationData) context.getApplicationContext();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(D) Log.d(TAG,"onPause()");
    }
    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.d(TAG,"onResume()");
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
        initBooksViewAdapter();
        initSearchView(view);
        initRecyclerView(view);
    }

    private void initBooksViewAdapter(){
        mBooksViewAdapter = new BooksListViewAdapter(getContext(),mList_SearchResult,false);
//        mBooksViewAdapter.setContext(getContext());
        mBooksViewAdapter.setClickListener(this);
    }

    private void initRecyclerView(View view){
        RecyclerView mRecyclerView = view.findViewById(R.id.fragment_search_recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mBooksViewAdapter);
    }

    private void initSearchView(View view){
        mSearchView = view.findViewById(R.id.searchView);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setQueryHint(getString(R.string.search_input_hint));
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String searchWord) {
                mSearchView.clearFocus();
                if(!searchWord.equals("")) {
                    if (D) Log.d(TAG, "Search: " + searchWord);
                    mBooksViewAdapter.clearBooksData();
                    searchKeyword = searchWord;
                    searchPage = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString(BaseProgressDialogFragment.title, getString(R.string.Progress_Search));
                    bundle.putString(BaseProgressDialogFragment.message, "");
                    Message msg = handler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_SHOW);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                    AsyncSearchTask(searchKeyword, searchPage);
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String word) {
                return false;
            }
        });
    }

/*
    public void callback(final boolean result, final JSONObject json){
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLinearLayout_Progress.setVisibility(View.GONE);
                if(mFragmentListener != null) {
                    mFragmentListener.onFragmentEvent(FragmentEvent.REMOVE_MASK);
                }

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
                                book.setView_type(BooksListViewAdapter.VIEW_TYPE_BOOK);

                                String isbn = getParam(data,"isbn");
                                book.setIsbn(isbn);
                                String title = getParam(data,"title");
                                book.setProgressTitle(title);
                                String author = getParam(data,"author");
                                book.setAuthor(author);
                                String imageUrl = getParam(data,"largeImageUrl");
                                book.setImage(imageUrl);
                                String publisher = getParam(data,"publisherName");
                                book.setPublisher(publisher);
                                String salesDate = getParam(data,"textView_SalesDate");
                                book.setSalesDate(salesDate);
                                String itemPrice = getParam(data,"itemPrice");
                                book.setItemPrice(itemPrice);
                                String rakutenUrl = getParam(data,"rakutenUrl");
                                book.setRakutenUrl(rakutenUrl);
                                String rating = getParam(data,"reviewAverage");
                                book.setRating(rating);
                                String readStatus = "5"; // Unregistered
                                book.setReadStatus(readStatus);

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

                            mBooksViewAdapter.addBooksData(dataset);

                            if(count - last > 0){
                                BookData footer = new BookData();
                                footer.setView_type(BooksListViewAdapter.VIEW_TYPE_BUTTON_LOAD);
                                mBooksViewAdapter.setFooter(footer);
//                        mBooksViewAdapter.setLoadNext();
                            }else{
                                mBooksViewAdapter.setFooter(null);
                            }

                        }


                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        },1500);
    }

*/

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
    public void onBookClick(BooksListViewAdapter adapter, int position, BookData data) {
        int view_type = adapter.getItemViewType(position);
        if(view_type == BooksListViewAdapter.VIEW_TYPE_BOOK){
            FragmentManager fragmentManager = getFragmentManager();
            if(fragmentManager != null){
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                FragmentBookDetail fragment = new FragmentBookDetail();
                Bundle bundle = new Bundle();
                bundle.putString(FragmentBookDetail.ARG_KEY_IMAGE,data.getImage());
                bundle.putString(FragmentBookDetail.ARG_KEY_TITLE,data.getTitle());
                bundle.putString(FragmentBookDetail.ARG_KEY_AUTHOR,data.getAuthor());


                fragment.setArguments(bundle);
                Slide slide = new Slide();
                slide.setSlideEdge(Gravity.END);
                fragment.setEnterTransition(slide);
//                fragmentTransaction.replace(R.id.contents_container, fragment,FragmentBookDetail.TAG);
                fragmentTransaction.add(R.id.contents_container, fragment, FragmentBookDetail.TAG);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }else{
            if(view_type == BooksListViewAdapter.VIEW_TYPE_BUTTON_LOAD){
                BookData footer = new BookData();
                footer.setView_type(BooksListViewAdapter.VIEW_TYPE_LOADING);
                adapter.setFooter(footer);
                searchPage++;
                AsyncSearchTask(searchKeyword,searchPage);
            }
        }


    }

    @Override
    public void onBookLongClick(BooksListViewAdapter adapter, int position, BookData data) {
        if(position != adapter.getItemCount() && data != null){
            String title = data.getTitle();
            if(D) Log.d(TAG,"LongClick: " + title);
        }
    }



    public void AsyncSearchTask(String search, int page) {
        String sort = "standard";
        String code = mData.getSharedPreferences().getString(MyBookshelfApplicationData.Key_SortSetting_SearchResult, getString(R.string.Code_SortSetting_SalesDate_Descending));
        if(code != null) {
            if (code.equals(getString(R.string.Code_SortSetting_SalesDate_Ascending))) {
                sort = "+releaseDate";
            }
            if (code.equals(getString(R.string.Code_SortSetting_SalesDate_Descending))) {
                sort = "-releaseDate";
            }
        }
        AsyncSearchBooks asyncSearchBooks = new AsyncSearchBooks(sort,search,page);
        asyncSearchBooks.setOnCallBack(callbackAsyncSearchBooks);
        asyncSearchBooks.execute();
    }


    AsyncSearchBooks.CallBackTask callbackAsyncSearchBooks = new AsyncSearchBooks.CallBackTask(){
        @Override
        public void CallBack(final boolean result,final int status, final JSONObject json){
            super.CallBack(result,status,json);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_DISMISS).sendToTarget();

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

                                mBooksViewAdapter.addBooksData(dataset);

                                if(count - last > 0){
                                    BookData footer = new BookData();
                                    footer.setView_type(BooksListViewAdapter.VIEW_TYPE_BUTTON_LOAD);
                                    mBooksViewAdapter.setFooter(footer);
//                        mBooksViewAdapter.setLoadNext();
                                }else{
                                    mBooksViewAdapter.setFooter(null);
                                }

                            }


                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }
            },1500);
        }
    };


/*
    private static class AsyncSearch extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<FragmentSearchBooks> mFragmentReference;
        final String keyword;
        final int page;
        JSONObject json;

        private AsyncSearch(FragmentSearchBooks fragment, String keyword, int page) {
            this.mFragmentReference = new WeakReference<>(fragment);
            this.keyword = keyword;
            this.page = page;
        }

        @Override
        protected void onPreExecute() {
            if(page == 1) {
                FragmentSearchBooks fragment = mFragmentReference.get();
                if (fragment.mFragmentListener != null) {
                    fragment.mFragmentListener.onFragmentEvent(FragmentEvent.DISP_MASK);
                }
                fragment.mLinearLayout_Progress.setVisibility(View.VISIBLE);
            }
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
*/
}
