package jp.gr.java_conf.nuranimation.my_bookshelf.background;

import android.content.Context;
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

import jp.gr.java_conf.nuranimation.my_bookshelf.application.ErrorStatus;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.SearchBooksResult;

public class SearchBooksThread extends Thread{
    private static final String TAG = SearchBooksThread.class.getSimpleName();
    private static final boolean D = true;

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
    private SearchBooksResult mResult;
    private boolean isCanceled;


    @SuppressWarnings("unused")
    public interface ThreadFinishListener {
        void deliverResult(SearchBooksResult result);
    }
    private ThreadFinishListener mListener;

    @SuppressWarnings("WeakerAccess")
    public SearchBooksThread(Context context, String keyword, int page, String sort) {
            this.sort = sort;
            this.keyword = keyword;
            this.page = page;
            isCanceled = false;
            if(context instanceof ThreadFinishListener){
                mListener =(ThreadFinishListener) context;
            } else {
                throw new UnsupportedOperationException("Listener is not Implementation.");
            }
    }


    @Override
    public void run(){
        if (D) Log.d(TAG, "thread start");
        int retried = 0;
        while (retried < 3) {
            if(isCanceled){
                break;
            }
            HttpsURLConnection connection = null;
            try {
                Thread.sleep(12000);
                mResult = new SearchBooksResult();
                if (TextUtils.isEmpty(keyword)) {
                    mResult.setErrorStatus(ErrorStatus.Error_Empty_Word);
                    isCanceled = true;
                    break;
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
                mResult.setErrorStatus(connection.getResponseCode());
                switch (connection.getResponseCode()) {
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
                if (reader != null) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                    mResult.setJSONObject(new JSONObject(sb.toString()));
                    mResult.setSuccess(true);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "InterruptedException");
                mResult.setErrorStatus(ErrorStatus.Error_InterruptedException);
            } catch (IOException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "IOException");
                mResult.setErrorStatus(ErrorStatus.Error_IOException);
            } catch (JSONException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "JSONException");
                mResult.setErrorStatus(ErrorStatus.Error_JSONException);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            if(mResult.isSuccess()){
                break;
            }
            retried++;
        }
        if (D) Log.d(TAG, "thread finish");
        if(mListener != null){
            mListener.deliverResult(mResult);
        }
    }


    @SuppressWarnings("unused")
    public void cancel(){
        isCanceled = true;
    }





}
