package jp.gr.java_conf.nuranimation.my_bookshelf.background;


import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.PatternSyntaxException;

import jp.gr.java_conf.nuranimation.my_bookshelf.application.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;

//@SuppressWarnings({"WeakerAccess","unused"})
public class FileIOThread extends Thread {
    private static final String TAG = FileIOThread.class.getSimpleName();
    private static final boolean D = true;

    public static final int TYPE_EXPORT  = 1;
    public static final int TYPE_IMPORT  = 2;

    private static final String APPLICATION_DIRECTORY_PATH = "/Android/data/jp.gr.java_conf.nuranimation.my_bookshelf/";
    private static final String FILE_NAME_SHELF_BOOKS = "backup_bookshelf.csv";
    private static final String FILE_NAME_AUTHORS = "backup_authors.csv";


    public static final int NO_ERROR = 0;
    public static final int ERROR_FILE_NOT_FOUND = 1;
    public static final int ERROR_IO_EXCEPTION = 2;
    public static final int ERROR_DBX_EXCEPTION = 3;


    private final int type;

    private Result mResult;
    private ThreadFinishListener mListener;
    private LocalBroadcastManager mLocalBroadcastManager;
    private MyBookshelfApplicationData mApplicationData;


    public static final class Result {
        private final boolean isSuccess;
        private final int errorCode;
        private final String errorMessage;

        private Result(boolean isSuccess, int errorCode, String errorMessage) {
            this.isSuccess = isSuccess;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
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

        public static Result success() {
            return new Result(true, NO_ERROR, "no error");
        }

        public static Result error(int errorCode, String errorMessage) {
            return new Result(false, errorCode, errorMessage);
        }

    }


    public interface ThreadFinishListener {
        void deliverExportResult(Result result);
        void deliverImportResult(Result result);
    }


    public FileIOThread(Context context, int type){
        this.type = type;
        this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        mApplicationData = (MyBookshelfApplicationData) context.getApplicationContext();
        if (context instanceof ThreadFinishListener) {
            mListener = (ThreadFinishListener) context;
        } else {
            throw new UnsupportedOperationException("Listener is not Implementation.");
        }
    }



    @Override
    public void run(){
        File extDir = Environment.getExternalStorageDirectory();
        String dirPath = extDir.getPath() + APPLICATION_DIRECTORY_PATH;
        File dir = new File(dirPath);
        int retried = 0;

        switch (type){
            case TYPE_EXPORT:
                while (retried < 3) {
                    if(!dir.exists()){
                        boolean isSuccess = dir.mkdirs();
                        if(D) Log.d(TAG,"mkdirs(): " + isSuccess);
                        mResult = Result.error(ERROR_IO_EXCEPTION, "dir.mkdirs() failed");
                        return;
                    }

                    int count = 0;

                    File file_books = new File(dirPath + FILE_NAME_SHELF_BOOKS);
                    File file_authors = new File(dirPath + FILE_NAME_AUTHORS);

                    try {
                        List<BookData> books = mApplicationData.loadShelfBooks(null);
                        int recodeCount = books.size();
                        if (D) Log.d(TAG, "recodeCount : " + recodeCount);

                        File file_bookshelf = new File(dirPath + "alt_" + FILE_NAME_SHELF_BOOKS);
                        OutputStream os_bookshelf = new FileOutputStream(file_bookshelf);
                        OutputStreamWriter osr_bookshelf = new OutputStreamWriter(os_bookshelf, Charset.forName("UTF-8"));
                        BufferedWriter bw_bookshelf = new BufferedWriter(osr_bookshelf);

                        String idx = "isbn,KEY_TITLE,author\r\n";
                        bw_bookshelf.write(idx);
                        for (BookData book : books) {
                            String isbn = book.getISBN();
                            String title = book.getTitle();
                            String author = book.getAuthor();
                            String str_book = isbn + "," + title + "," + author + "\r\n";
                            bw_bookshelf.write(str_book);
                            count++;
                            String progress = count + "/" + recodeCount;
//                        mHandler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_UPDATE, -1, -1, progress).sendToTarget();
                            Intent intent = new Intent();
                            intent.putExtra(BaseFragment.KEY_UPDATE_PROGRESS, progress);
                            intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
                            mLocalBroadcastManager.sendBroadcast(intent);
                        }
                        bw_bookshelf.close();


                        List<String> authors = mApplicationData.loadAuthorsList();
                        recodeCount = authors.size();
                        if (D) Log.d(TAG, "recodeCount : " + recodeCount);
                        count = 0;

                        OutputStream os_authors = new FileOutputStream(file_authors);
                        OutputStreamWriter osr_authors = new OutputStreamWriter(os_authors, Charset.forName("UTF-8"));
                        BufferedWriter bw_authors = new BufferedWriter(osr_authors);

                        for (String author : authors) {
                            bw_authors.write(author + "\r\n");
                            count++;
                            String progress = count + "/" + recodeCount;
//                        mHandler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_UPDATE, -1, -1, progress).sendToTarget();
                            Intent intent = new Intent();
                            intent.putExtra(BaseFragment.KEY_UPDATE_PROGRESS, progress);
                            intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
                            mLocalBroadcastManager.sendBroadcast(intent);
                        }
                        bw_authors.close();
                        mResult = Result.success();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (mResult.isSuccess()) {
                        break;
                    }
                    retried++;
                }
                break;
            case TYPE_IMPORT:
                while (retried < 3) {

                    int size = 0;
                    int count = 0;


                    File file_books = new File(dirPath + FILE_NAME_SHELF_BOOKS);
                    if (!file_books.exists()) {
                        if (D) Log.e(TAG, "file_books not found");
                        mResult = Result.error(ERROR_FILE_NOT_FOUND, "file not found");
                        break;
                    }
                    File file_authors = new File(dirPath + FILE_NAME_AUTHORS);
                    if (!file_authors.exists()) {
                        if (D) Log.e(TAG, "file_authors not found");
                        mResult = Result.error(ERROR_FILE_NOT_FOUND, "file not found");
                        break;
                    }

                    List<BookData> books = new ArrayList<>();
                    List<String> authors = new ArrayList<>();

                    try {
                        // insert BookData from CSV
                        // count line
                        InputStream pre_is_bookshelf = MyBookshelfUtils.getStreamSkipBOM(new FileInputStream(file_books),Charset.forName("UTF-8"));
                        InputStreamReader pre_isr_bookshelf = new InputStreamReader(pre_is_bookshelf, Charset.forName("UTF-8"));
                        BufferedReader pre_br_bookshelf = new BufferedReader(pre_isr_bookshelf);
                        pre_br_bookshelf.readLine(); // skip first line
                        while ((pre_br_bookshelf.readLine()) != null) {
                            size++;
                        }
                        pre_br_bookshelf.close();
                        if (D) Log.d(TAG, "size: " + size);
                        // import csv
                        InputStream is_bookshelf = MyBookshelfUtils.getStreamSkipBOM(new FileInputStream(file_books),Charset.forName("UTF-8"));
                        InputStreamReader isr_bookshelf = new InputStreamReader(is_bookshelf, Charset.forName("UTF-8"));
                        BufferedReader br_bookshelf = new BufferedReader(isr_bookshelf);
                        String str_line_bookshelf = br_bookshelf.readLine();
                        String[] idx_bookshelf = str_line_bookshelf.split(",");

                        if(idx_bookshelf.length == 40){
                            while ((str_line_bookshelf = br_bookshelf.readLine()) != null) {
                                if (D) Log.d(TAG, "str: " + str_line_bookshelf);
                                String[] split = MyBookshelfUtils.splitLineWithComma(str_line_bookshelf);
                                BookData book = convertReadeeToBookData(split);
                                books.add(book);
                                count++;
                                String progress = count + "/" + size;
//                                mHandler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_UPDATE, -1, -1, progress).sendToTarget();
                                Intent intent = new Intent();
                                intent.putExtra(BaseFragment.KEY_UPDATE_PROGRESS, progress);
                                intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
                                mLocalBroadcastManager.sendBroadcast(intent);
                            }
                        }
                        if(idx_bookshelf.length == 20){
                            while ((str_line_bookshelf = br_bookshelf.readLine()) != null) {
                                if (D) Log.d(TAG, "str: " + str_line_bookshelf);
                                String[] split = MyBookshelfUtils.splitLineWithComma(str_line_bookshelf);
                                import_MYBOOKSHELF_CSV(split);
                                count++;
                                String progress = count + "/" + size;
//                                mHandler.obtainMessage(BaseFragment.MESSAGE_PROGRESS_DIALOG_UPDATE, -1, -1, progress).sendToTarget();
                                Intent intent = new Intent();
                                intent.putExtra(BaseFragment.KEY_UPDATE_PROGRESS, progress);
                                intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
                                mLocalBroadcastManager.sendBroadcast(intent);
                            }
                        }
                        br_bookshelf.close();

                        size = 0;
                        count = 0;
                        // insert Author from CSV file

                        // count line
                        InputStream pre_is_authors = new FileInputStream(file_authors);
                        InputStreamReader pre_isr_authors = new InputStreamReader(pre_is_authors);
                        BufferedReader pre_br_authors = new BufferedReader(pre_isr_authors);
                        while ((pre_br_authors.readLine()) != null) {
                            size++;
                        }
                        pre_br_authors.close();
                        if (D) Log.d(TAG, "size: " + size);
                        // import csv
                        InputStream is_authors = new FileInputStream(file_authors);
                        InputStreamReader isr_authors = new InputStreamReader(is_authors);
                        BufferedReader br_authors = new BufferedReader(isr_authors);
                        String str_line_authors;
                        while ((str_line_authors = br_authors.readLine()) != null) {
                            authors.add(str_line_authors);
                            count++;
                            String progress = count + "/" + size;
//                            mHandler.obtainMessage(SettingsFragment.MESSAGE_PROGRESS_DIALOG_UPDATE, -1, -1, progress).sendToTarget();
                            Intent intent = new Intent();
                            intent.putExtra(BaseFragment.KEY_UPDATE_PROGRESS, progress);
                            intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_SERVICE_STATE);
                            mLocalBroadcastManager.sendBroadcast(intent);
                        }
                        br_authors.close();


                        mApplicationData.registerToShelfBooks(books);
                        mApplicationData.registerToAuthorsList(authors);
                        mResult = Result.success();
//            mData.updateList_MyBookshelf();
                    } catch (FileNotFoundException e) {
                        if(D) Log.e(TAG,"Error");

                    } catch (IOException e) {
                        if (D) Log.e(TAG, "Error");
                    } catch (PatternSyntaxException e){
                        if (D) Log.e(TAG, "Error");
                    }


                    if (mResult.isSuccess()) {
                        break;
                    }
                    retried++;
                }
                break;
        }

        if (D) Log.d(TAG, "thread finish");
        if (mListener != null && mResult != null) {
            switch (type) {
                case TYPE_EXPORT:
                    mListener.deliverExportResult(mResult);
                    break;
                case TYPE_IMPORT:
                    mListener.deliverImportResult(mResult);
                    break;
            }
        }

    }


    private void import_MYBOOKSHELF_CSV(String[] split){
        if(D) Log.d(TAG,"isbn: " + split[0]);
    }


    private BookData convertReadeeToBookData (String[] split){
        BookData bookData = new BookData();
        bookData.setISBN(split[1]);
        bookData.setTitle(split[3]);
        bookData.setAuthor(split[8]);
        bookData.setPublisher(split[9]);
        bookData.setSalesDate(convertSalesDate(split[11]));
        bookData.setItemPrice(split[12]);
        bookData.setRakutenUrl(split[13]);
        bookData.setImage(split[17]);
        bookData.setRating(split[18]);
        bookData.setReadStatus(split[19]);
        bookData.setTags(split[23]);
        bookData.setFinishReadDate(convertSalesDate(split[26]));
        bookData.setRegisterDate(split[39]);
        return bookData;
    }



    private String convertSalesDate(String date) {
        if(!TextUtils.isEmpty(date)) {
            String[] split = date.split("/");
            if(split.length == 3) {
                return String.format(Locale.JAPAN, "%s年%s月%s日", split[0], split[1], split[2]);
            }
        }
        return date;
    }



}
