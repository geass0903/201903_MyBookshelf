package jp.gr.java_conf.nuranimation.my_bookshelf.application;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseAsyncTaskLoader;

public class AsyncSearchBook extends BaseAsyncTaskLoader<AsyncSearchBook.Result> {
    private static final String TAG = AsyncSearchBook.class.getSimpleName();
    private static final boolean D = true;


    public class Result {
        public boolean isSuccess;
        public int errorStatus;
        public JSONObject json;
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

    public AsyncSearchBook(Context context, String sort, String keyword, int page) {
        super(context);
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
                if (TextUtils.isEmpty(keyword)) {
                    mResult.errorStatus = ErrorStatus.Error_Empty_Word;
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
                mResult.errorStatus = connection.getResponseCode();
                switch (mResult.errorStatus){
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
                    mResult.json = new JSONObject(sb.toString());
                    mResult.isSuccess = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "InterruptedException");
                mResult.errorStatus = ErrorStatus.Error_InterruptedException;
            } catch (IOException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "IOException");
                mResult.errorStatus = ErrorStatus.Error_IOException;
            } catch (JSONException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "JSONException");
                mResult.errorStatus = ErrorStatus.Error_JSONException;
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

}