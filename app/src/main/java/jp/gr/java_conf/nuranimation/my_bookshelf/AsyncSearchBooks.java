package jp.gr.java_conf.nuranimation.my_bookshelf;


import android.os.AsyncTask;
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
class AsyncSearchBooks extends AsyncTask<Void, Void, Boolean> {
    public static final String TAG = AsyncSearchBooks.class.getSimpleName();
    private static final boolean D = true;
    private CallBackTask callBackTask;

    private static final String urlBase             = "https://app.rakuten.co.jp/services/api/BooksTotal/Search/20170404?applicationId=1028251347039610250";
    private static final String urlFormat           = "&format=" + "json";
    private static final String urlFormatVersion    = "&formatVersion=" + "2";
    private static final String urlGenre            = "&booksGenreId=" + "001"; // Books
    private static final String urlHits             = "&hits=20";
    private static final String urlStockFlag        = "&outOfStockFlag=" + "1";
    private static final String urlField            = "&field=" + "0";
    private final String sort;
    private final String keyword;
    private final int page;
    private JSONObject json;
    private int status;

    AsyncSearchBooks(String sort, String keyword, int page) {
        this.sort = sort;
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
            String urlSort = "&sort=" + URLEncoder.encode(sort, "UTF-8");
            String urlPage = "&page=" + String.valueOf(page);
            String urlKeyword = "&keyword=" + URLEncoder.encode(keyword, "UTF-8");

            String urlString = urlBase + urlFormat + urlFormatVersion + urlGenre + urlHits + urlStockFlag + urlField
                    + urlSort + urlPage   + urlKeyword;
            URL url = new URL(urlString);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(false);
            connection.connect();

            status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK                     // 200
                    || status == HttpURLConnection.HTTP_BAD_REQUEST     // 400 wrong parameter
                    || status == HttpURLConnection.HTTP_NOT_FOUND       // 404 not found
                    || status == 429                                    // 429 too many requests
                    || status == HttpURLConnection.HTTP_INTERNAL_ERROR  // 500 system error
                    || status == HttpURLConnection.HTTP_UNAVAILABLE     // 503 service unavailable
            ) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
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
        if(callBackTask != null) {
            callBackTask.CallBack(result,status, json);
        }
    }


    void setOnCallBack(CallBackTask task){
        callBackTask = task;
    }

    static class CallBackTask {
        public void CallBack(final boolean result,final int status, final JSONObject json){
        }
    }

}
