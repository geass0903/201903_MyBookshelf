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
import java.util.Calendar;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.Result;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.SearchParam;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.net.base.BaseThread;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.CalendarUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.BookDataUtils;


public class NewBooksThread extends BaseThread {
    private static final String TAG = NewBooksThread.class.getSimpleName();
    private static final boolean D = false;

    private static final String urlBase = "https://app.rakuten.co.jp/services/api/BooksTotal/Search/20170404?applicationId=1028251347039610250";
    private static final String urlFormat = "&format=" + "json";
    private static final String urlFormatVersion = "&formatVersion=" + "2";
    private static final String urlGenre = "&booksGenreId=" + "001"; // Books
    private static final String urlHits = "&hits=20";
    private static final String urlStockFlag = "&outOfStockFlag=" + "1";
    private static final String urlField = "&field=" + "0";
    private static final String urlSort = "&sort=" + "-releaseDate";

    private final Context mContext;
    private final List<String> authors;

    public NewBooksThread(Context context, List<String> authors) {
        super(context);
        this.mContext = context;
        this.authors = authors;
    }


    @Override
    public void run() {
        Result mResult;

        if(authors.size() == 0){
            mResult = Result.ReloadError(Result.ERROR_CODE_EMPTY_AUTHORS_LIST, "empty authors");
        }else{
            mResult = reloadNewBooks(authors);
        }
        if (getThreadFinishListener() != null) {
            getThreadFinishListener().deliverResult(mResult);
        }
    }


    @Override
    public void cancel() {
        super.cancel();
        if (D) Log.d(TAG, "thread cancel");
    }


    private Result reloadNewBooks(List<String> authors){
        int size = authors.size();
        int count = 0;
        String message;
        String progress;

        List<BookData> books = new ArrayList<>();
        for (String author : authors) {
            if (isCanceled()) {
                return Result.ReloadError(Result.ERROR_CODE_RELOAD_CANCELED, "reload canceled");
            }
            count++;
            message = mContext.getString(R.string.progress_message_author) + author;
            progress = count + "/" + size;
            updateProgress(message, progress);
            List<BookData> newBooks = getNewBooks(author);
            books.addAll(newBooks);
        }
        if(books.size() > 0) {
            return Result.ReloadSuccess(books);
        }else{
            return Result.ReloadError(Result.ERROR_CODE_IO_EXCEPTION, "no books");
        }
    }

    private List<BookData> getNewBooks(String author) {
        List<BookData> books = new ArrayList<>();
        int page = 1;
        boolean hasNext = true;
        while (hasNext) {
            SearchParam searchParam = SearchParam.setSearchParam(author, page);
            Result result = search(searchParam);
            if (result.isSuccess()) {
                hasNext = result.hasNext();
                List<BookData> check = result.getBooks();
                for (BookData book : check) {
                    if (isNewBook(book)) {
                        String book_author = book.getAuthor();
                        book_author = book_author.replaceAll("[\\x20\\u3000]", "");
                        if (book_author.contains(author)) {
                            if (D) Log.d(TAG, "author: " + author + " add: " + book.getTitle());
                            books.add(book);
                        }
                    } else {
                        hasNext = false;
                        break;
                    }
                }
                page++;
            } else {
                hasNext = false;
            }
        }
        return books;
    }

    private Result search(final SearchParam searchParam) {
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
                if (TextUtils.isEmpty(searchParam.getKeyword())) {
                    return Result.SearchError(Result.ERROR_CODE_EMPTY_KEYWORD, "empty keyword");
                }
                String urlKeyword = "&keyword=" + URLEncoder.encode(searchParam.getKeyword(), "UTF-8");
                String urlPage = "&page=" + searchParam.getPage();
                String urlString = urlBase + urlFormat + urlFormatVersion + urlGenre + urlHits + urlStockFlag + urlField + urlSort
                        + urlKeyword + urlPage;
                URL url = new URL(urlString);
                if (isCanceled()) {
                    return Result.SearchError(Result.ERROR_CODE_IO_EXCEPTION, "search canceled");
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
                        // retry
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


    private boolean isNewBook(BookData book){
        Calendar baseDate = Calendar.getInstance();
        baseDate.add(Calendar.DAY_OF_MONTH, -14);
        Calendar salesDate = CalendarUtils.parseDateString(book.getSalesDate());
        if(salesDate != null){
            return salesDate.compareTo(baseDate) >= 0;
        }else{
            return false;
        }
    }

}
