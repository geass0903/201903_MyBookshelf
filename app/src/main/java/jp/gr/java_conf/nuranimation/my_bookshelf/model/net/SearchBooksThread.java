package jp.gr.java_conf.nuranimation.my_bookshelf.model.net;

import android.content.Context;
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

import jp.gr.java_conf.nuranimation.my_bookshelf.model.base.BaseThread;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.Result;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.BookDataUtils;


public class SearchBooksThread extends BaseThread {
    private static final String TAG = SearchBooksThread.class.getSimpleName();
    private static final boolean D = false;

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



    public SearchBooksThread(Context context, String keyword, int page, String sort) {
        super(context);
        this.sort = sort;
        this.keyword = keyword;
        this.page = page;
    }


    @Override
    public void run() {
        Result mResult = search(keyword, page, sort);
        if(getThreadFinishListener() != null){
            getThreadFinishListener().deliverResult(mResult);
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        if (D) Log.d(TAG, "thread cancel");
    }


    private Result search(final String keyword, final int page, final String sort) {
        Result mResult = Result.SearchError(Result.ERROR_CODE_UNKNOWN, "search failed");
        int count = 0;
        int last = 0;

        int retried = 0;
        while (retried < 3) {
            if (isCanceled()) {
                return Result.SearchError(Result.ERROR_CODE_IO_EXCEPTION, "search canceled");
            }

            HttpsURLConnection connection = null;
            try {
                if (retried > 0) {
                    Thread.sleep(retried * 200);
                }
                Thread.sleep(1000);
                if (TextUtils.isEmpty(keyword)) {
                    return Result.SearchError(Result.ERROR_CODE_EMPTY_KEYWORD, "empty keyword");
                }
                String urlSort = "&sort=" + URLEncoder.encode(sort, "UTF-8");
                String urlPage = "&page=" + page;
                String urlKeyword = "&keyword=" + URLEncoder.encode(keyword, "UTF-8");
                String urlString = urlBase + urlFormat + urlFormatVersion + urlGenre + urlHits + urlStockFlag + urlField
                        + urlSort + urlPage + urlKeyword;
                URL url = new URL(urlString);
                if (isCanceled()) {
                    return Result.SearchError(Result.ERROR_CODE_SEARCH_CANCELED, "search canceled");
                }
                connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(false);
                connection.connect();
                BufferedReader reader;
                StringBuilder sb = new StringBuilder();
                String line;
                switch (connection.getResponseCode()) {
                    case HttpURLConnection.HTTP_OK:             // 200
                        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        reader.close();
                        JSONObject json = new JSONObject(sb.toString());
                        if (json.has(BookDataUtils.JSON_KEY_ITEMS)) {
                            List<BookData> tmp = new ArrayList<>();
                            JSONArray jsonArray = json.getJSONArray(BookDataUtils.JSON_KEY_ITEMS);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject data = jsonArray.getJSONObject(i);
                                if (D) Log.d(TAG, "data: " + data.toString());
                                BookData book = BookDataUtils.convertToBookData(data);
                                tmp.add(book);
                            }
                            if (json.has(BookDataUtils.JSON_KEY_COUNT)) {
                                count = json.getInt(BookDataUtils.JSON_KEY_COUNT);
                                if (D) Log.d(TAG, "count: " + count);
                            }
                            if (json.has(BookDataUtils.JSON_KEY_LAST)) {
                                last = json.getInt(BookDataUtils.JSON_KEY_LAST);
                                if (D) Log.d(TAG, "last: " + last);
                            }
                            boolean hasNext = count - last > 0;
                            List<BookData> books = new ArrayList<>(tmp);
                            return Result.SearchSuccess(books, hasNext);
                        } else {
                            return Result.SearchError(Result.ERROR_CODE_IO_EXCEPTION, "No json item");
                        }
                    case HttpURLConnection.HTTP_BAD_REQUEST:    // 400 wrong parameter
                        return Result.SearchError(Result.ERROR_CODE_IO_EXCEPTION, "wrong parameter");
                    case HttpURLConnection.HTTP_NOT_FOUND:      // 404 not success
                    case 429:                                   // 429 too many requests
                    case HttpURLConnection.HTTP_INTERNAL_ERROR: // 500 system error
                    case HttpURLConnection.HTTP_UNAVAILABLE:    // 503 service unavailable
                        reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        reader.close();
                        JSONObject errorJSON = new JSONObject(sb.toString());
                        if (errorJSON.has(BookDataUtils.JSON_KEY_ERROR) && errorJSON.has(BookDataUtils.JSON_KEY_ERROR_DESCRIPTION)) {
                            String errorMessage = errorJSON.getString(BookDataUtils.JSON_KEY_ERROR_DESCRIPTION);
                            mResult = Result.SearchError(Result.ERROR_CODE_HTTP_ERROR, errorMessage);
                            // retry
                        } else {
                            mResult = Result.SearchError(Result.ERROR_CODE_IO_EXCEPTION, "JSONException");
                            // retry
                        }
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "InterruptedException");
                mResult = Result.SearchError(Result.ERROR_CODE_INTERRUPTED_EXCEPTION, "InterruptedException");
                // retry
            } catch (IOException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "IOException");
                mResult = Result.SearchError(Result.ERROR_CODE_IO_EXCEPTION, "IOException");
                // retry
            } catch (JSONException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "JSONException");
                mResult = Result.SearchError(Result.ERROR_CODE_JSON_EXCEPTION, "JSONException");
                // retry
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            retried++;
        }
        return mResult;
    }



}
