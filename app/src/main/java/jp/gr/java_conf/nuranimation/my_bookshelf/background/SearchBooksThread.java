package jp.gr.java_conf.nuranimation.my_bookshelf.background;

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

import jp.gr.java_conf.nuranimation.my_bookshelf.adapter.BooksListViewAdapter;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.BookData;

@SuppressWarnings({"WeakerAccess","unused"})
public class SearchBooksThread extends Thread {
    private static final String TAG = SearchBooksThread.class.getSimpleName();
    private static final boolean D = false;

    public static final int NO_ERROR                    = 0;
    public static final int ERROR_EMPTY_KEYWORD         = 1;
    public static final int ERROR_HTTP_ERROR            = 2;
    public static final int ERROR_INTERRUPTED_EXCEPTION = 3;
    public static final int ERROR_IO_EXCEPTION          = 4;
    public static final int ERROR_JSON_EXCEPTION        = 5;
    public static final int ERROR_UNKNOWN               = 6;

    private static final String JSON_KEY_ITEMS = "Items";
    private static final String JSON_KEY_COUNT = "count";
    private static final String JSON_KEY_LAST = "last";
    private static final String JSON_KEY_ERROR = "error";
    private static final String JSON_KEY_ERROR_DESCRIPTION = "error_description";
    private static final String JSON_KEY_TITLE = "title";
    private static final String JSON_KEY_AUTHOR = "author";
    private static final String JSON_KEY_PUBLISHER_NAME = "publisherName";
    private static final String JSON_KEY_ISBN = "isbn";
    private static final String JSON_KEY_SALES_DATE = "salesDate";
    private static final String JSON_KEY_ITEM_PRICE = "itemPrice";
    private static final String JSON_KEY_ITEM_URL = "itemUrl";
    private static final String JSON_KEY_IMAGE_URL = "largeImageUrl";
    private static final String JSON_KEY_REVIEW_AVERAGE = "reviewAverage";


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
    private boolean isCanceled;
    private Result mResult;
    private ThreadFinishListener mListener;

    public static final class Result {
        private final boolean isSuccess;
        private final int errorCode;
        private final String errorMessage;
        private final boolean hasNext;
        private final List<BookData> books;

        private Result(boolean isSuccess, int errorCode, String errorMessage, boolean hasNext, List<BookData> books) {
            this.isSuccess = isSuccess;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.hasNext = hasNext;
            this.books = books;
        }

        public boolean isSuccess() {
            return this.isSuccess;
        }

        public int getErrorCode() {
            return this.errorCode;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }

        public boolean hasNext() {
            return this.hasNext;
        }

        public List<BookData> getBooks() {
            return new ArrayList<>(this.books);
        }

        public static Result success(boolean hasNext, List<BookData> books) {
            return new Result(true, NO_ERROR, "no error", hasNext, books);
        }

        public static Result error(int errorCode, String errorMessage) {
            return new Result(false, errorCode, errorMessage, false, null);
        }

    }

    public interface ThreadFinishListener {
        void deliverSearchBooksResult(Result result);
    }


    public SearchBooksThread(Context context, String keyword, int page, String sort) {
        this.sort = sort;
        this.keyword = keyword;
        this.page = page;
        isCanceled = false;
        if (context instanceof ThreadFinishListener) {
            mListener = (ThreadFinishListener) context;
        } else {
            throw new UnsupportedOperationException("Listener is not Implementation.");
        }
    }


    @Override
    public void run() {
        if (D) Log.d(TAG, "thread start");
        int count = 0;
        int last = 0;

        int retried = 0;
        while (retried < 3) {
            if (isCanceled) {
                break;
            }

            HttpsURLConnection connection = null;
            try {
                if (retried > 0) {
                    Thread.sleep(retried * 200);
                }
                Thread.sleep(1000);
                if (TextUtils.isEmpty(keyword)) {
                    mResult = Result.error(ERROR_EMPTY_KEYWORD, "empty keyword");
                    break;
                }

                String urlSort = "&sort=" + URLEncoder.encode(sort, "UTF-8");
                String urlPage = "&page=" + String.valueOf(page);
                String urlKeyword = "&keyword=" + URLEncoder.encode(keyword, "UTF-8");
                String urlString = urlBase + urlFormat + urlFormatVersion + urlGenre + urlHits + urlStockFlag + urlField
                        + urlSort + urlPage + urlKeyword;
                URL url = new URL(urlString);
                if (isCanceled) {
                    break;
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
                        if (json.has(JSON_KEY_ITEMS)) {
                            List<BookData> tmp = new ArrayList<>();
                            JSONArray jsonArray = json.getJSONArray(JSON_KEY_ITEMS);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject data = jsonArray.getJSONObject(i);
                                if (D) Log.d(TAG, "data: " + data.toString());
                                BookData book = convertBookData(data);
                                tmp.add(book);
                            }
                            if (json.has(JSON_KEY_COUNT)) {
                                count = json.getInt(JSON_KEY_COUNT);
                                if (D) Log.d(TAG, "count: " + count);
                            }
                            if (json.has(JSON_KEY_LAST)) {
                                last = json.getInt(JSON_KEY_LAST);
                                if (D) Log.d(TAG, "last: " + last);
                            }
                            boolean hasNext = count - last > 0;
                            List<BookData> books = new ArrayList<>(tmp);
                            mResult = Result.success(hasNext, books);
                        }else{
                            mResult = Result.error(ERROR_IO_EXCEPTION, "JSONException");
                        }
                        break;
                    case HttpURLConnection.HTTP_BAD_REQUEST:    // 400 wrong parameter
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
                        if (errorJSON.has(JSON_KEY_ERROR_DESCRIPTION)) {
                            String errorMessage = errorJSON.getString(JSON_KEY_ERROR_DESCRIPTION);
                            mResult = Result.error(ERROR_HTTP_ERROR, errorMessage);
                        }else{
                            mResult = Result.error(ERROR_IO_EXCEPTION, "JSONException");
                        }
                        break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "InterruptedException");
                mResult = Result.error(ERROR_INTERRUPTED_EXCEPTION, "InterruptedException");
            } catch (IOException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "IOException");
                mResult = Result.error(ERROR_IO_EXCEPTION, "IOException");
            } catch (JSONException e) {
                e.printStackTrace();
                if (D) Log.d(TAG, "JSONException");
                mResult = Result.error(ERROR_JSON_EXCEPTION, "JSONException");
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            if (mResult != null && mResult.isSuccess()) {
                break;
            }
            retried++;
        }
        if (D) Log.d(TAG, "thread finish");
        if (mListener != null && !isCanceled && mResult != null) {
            mListener.deliverSearchBooksResult(mResult);
        }
    }

    public void cancel() {
        if (D) Log.d(TAG, "thread cancel");
        isCanceled = true;
    }

    private static BookData convertBookData(JSONObject data) throws JSONException {
        BookData temp = new BookData();
        temp.setView_type(BooksListViewAdapter.VIEW_TYPE_BOOK);
        String title = getParam(data, JSON_KEY_TITLE);
        temp.setTitle(title);
        String author = getParam(data, JSON_KEY_AUTHOR);
        temp.setAuthor(author);
        String publisher = getParam(data, JSON_KEY_PUBLISHER_NAME);
        temp.setPublisher(publisher);
        String isbn = getParam(data, JSON_KEY_ISBN);
        temp.setISBN(isbn);
        String salesDate = getParam(data, JSON_KEY_SALES_DATE);
        temp.setSalesDate(salesDate);
        String itemPrice = getParam(data, JSON_KEY_ITEM_PRICE);
        temp.setItemPrice(itemPrice);
        String rakutenUrl = getParam(data, JSON_KEY_ITEM_URL);
        temp.setRakutenUrl(rakutenUrl);
        String imageUrl = getParam(data, JSON_KEY_IMAGE_URL);
        temp.setImage(imageUrl);
        String rating = getParam(data, JSON_KEY_REVIEW_AVERAGE);
        temp.setRating(rating);
        String readStatus = BookData.STATUS_UNREGISTERED;
        temp.setReadStatus(readStatus);
        return new BookData(temp);
    }

    private static String getParam(JSONObject json, String keyword) throws JSONException {
        if (json.has(keyword)) {
            String param = json.getString(keyword);
            if (D) Log.d(TAG, keyword + ": " + param);
            return param;
        }
        return "";
    }

}
