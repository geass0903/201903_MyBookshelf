package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class AsyncSearchBook extends BaseAsyncTaskLoader<AsyncSearchBook.Result> {
    public static final String TAG = AsyncSearchBook.class.getSimpleName();
    private static final boolean D = true;

    private MyBookshelfApplicationData mData;

    class Result {
        boolean isSuccess;
        int status;
        List<BookData> books;
    }
    private Result mResult;

    private static final String urlBase = "https://app.rakuten.co.jp/services/api/BooksTotal/Search/20170404?applicationId=1028251347039610250";
    private static final String urlFormat = "&format=" + "json";
    private static final String urlFormatVersion = "&formatVersion=" + "2";
    private static final String urlGenre = "&booksGenreId=" + "001"; // Books
    private static final String urlHits = "&hits=20";
    private static final String urlStockFlag = "&outOfStockFlag=" + "1";
    private static final String urlField = "&field=" + "0";
    private final String sort;
    private final String keyword;
    private final int page;

    AsyncSearchBook(Context context, String sort, String keyword, int page) {
        super(context);
        mData = (MyBookshelfApplicationData) context.getApplicationContext();
        mResult = null;
        this.sort = sort;
        this.keyword = keyword;
        this.page = page;
    }


    @Nullable
    @Override
    public Result loadInBackground() {
        int retried = 0;
        while (retried < 3) {
            HttpsURLConnection connection = null;
            try {
                Thread.sleep(1200);

                mResult = new Result();
                mResult.isSuccess = false;
                mResult.books = new ArrayList<>();
                if (TextUtils.isEmpty(keyword)) {
                    mResult.status = ErrorStatus.Error_Empty_Word;
                    return mResult;
                }

                String urlSort = "&sort=" + URLEncoder.encode(sort, "UTF-8");
                String urlPage = "&page=" + String.valueOf(page);
                String urlKeyword = "&keyword=" + URLEncoder.encode(keyword, "UTF-8");

                String urlString = urlBase + urlFormat + urlFormatVersion + urlGenre + urlHits + urlStockFlag + urlField
                        + urlSort + urlPage + urlKeyword;
                URL url = new URL(urlString);

                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(false);
                connection.connect();
                BufferedReader reader = null;
                mResult.status = connection.getResponseCode();
                switch (mResult.status){
                    case HttpURLConnection.HTTP_OK:             // 200
                        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:    // 400 wrong parameter
                    case HttpURLConnection.HTTP_NOT_FOUND:      // 404 not found
                    case 429:                                   // 429 too many requests
                    case HttpURLConnection.HTTP_INTERNAL_ERROR: // 500 system error
                    case HttpURLConnection.HTTP_UNAVAILABLE:    // 503 service unavailable
                        reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                        break;
                }
                if(reader != null) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                    JSONObject json = new JSONObject(sb.toString());
                    mResult.books = getBooks(json);
                    mResult.isSuccess = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "InterruptedException");
                mResult.status = ErrorStatus.Error_InterruptedException;
            } catch (IOException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "IOException");
                mResult.status = ErrorStatus.Error_IOException;
            } catch (JSONException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "JSONException");
                mResult.status = ErrorStatus.Error_JSONException;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            if(mResult.isSuccess){
                return mResult;
            }
            retried++;
        }
        return mResult;
    }


    private List<BookData> getBooks(JSONObject json) {
        List<BookData> books = new ArrayList<>();

        int count = 0;
        int last = 0;

        try {
            if (json.has("Items")) {
                JSONArray jsonArray = json.getJSONArray("Items");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject data = jsonArray.getJSONObject(i);
                    if (D) Log.d(TAG, "sb: " + data.toString());

                    String isbn = MyBookshelfUtils.getParam(data, "isbn");
                    BookData registered = mData.getDatabaseHelper().searchRegistered(isbn);
                    BookData book = MyBookshelfUtils.getBook(data);
                    if(registered != null){
                        book.setRegisterDate(registered.getRegisterDate());
                        book.setReadStatus(registered.getReadStatus());
                    }
                    books.add(book);
                }

                if (json.has("count")) {
                    count = json.getInt("count");
                    if (D) Log.d(TAG, "count: " + count);
                }

                if (json.has("last")) {
                    last = json.getInt("last");
                    if (D) Log.d(TAG, "last: " + last);
                }

                if (count - last > 0) {
                    BookData footer = new BookData();
                    footer.setView_type(BooksListViewAdapter.VIEW_TYPE_BUTTON_LOAD);
                    books.add(footer);
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return books;

    }


}