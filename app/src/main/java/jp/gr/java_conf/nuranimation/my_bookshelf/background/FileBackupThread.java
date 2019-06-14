package jp.gr.java_conf.nuranimation.my_bookshelf.background;


import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;


import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.util.IOUtil;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.SearchErrorException;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.v2.files.SearchResult;
import com.dropbox.core.v2.files.WriteMode;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jp.gr.java_conf.nuranimation.my_bookshelf.application.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseFragment;

@SuppressWarnings({"WeakerAccess","unused"})
public class FileBackupThread extends Thread {
    private static final String TAG = FileBackupThread.class.getSimpleName();
    private static final boolean D = true;

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_EXPORT = 1;
    public static final int TYPE_IMPORT = 2;
    public static final int TYPE_BACKUP = 3;
    public static final int TYPE_RESTORE = 4;

    public static final int PROGRESS_TYPE_EXPORT_BOOKS      = 1;
    public static final int PROGRESS_TYPE_EXPORT_AUTHORS    = 2;
    public static final int PROGRESS_TYPE_IMPORT_BOOKS      = 3;
    public static final int PROGRESS_TYPE_IMPORT_AUTHORS    = 4;
    public static final int PROGRESS_TYPE_UPLOAD_BOOKS      = 5;
    public static final int PROGRESS_TYPE_UPLOAD_AUTHORS    = 6;
    public static final int PROGRESS_TYPE_DOWNLOAD_BOOKS    = 7;
    public static final int PROGRESS_TYPE_DOWNLOAD_AUTHORS  = 8;
    public static final int PROGRESS_TYPE_REGISTER          = 9;


    private static final String CLIENT_IDENTIFIER = "MyBookshelf/1.0";

    private static final String DROPBOX_APP_DIRECTORY_PATH = "/MyBookshelf/";
    private static final String APPLICATION_DIRECTORY_PATH = "/Android/data/jp.gr.java_conf.nuranimation.my_bookshelf/";
    private static final String FILE_NAME_SHELF_BOOKS = "backup_bookshelf.csv";
    private static final String FILE_NAME_AUTHORS = "backup_authors.csv";


    public static final int NO_ERROR                        = 0;
    public static final int ERROR_FILE_NOT_FOUND            = 1;
    public static final int ERROR_IO_EXCEPTION              = 2;
    public static final int ERROR_DBX_EXCEPTION             = 3;
    public static final int ERROR_PATTERN_SYNTAX_EXCEPTION  = 4;
    public static final int ERROR_UNKNOWN                   = 5;
    public static final int ERROR_INTERRUPTED_EXCEPTION     = 6;

    private final int type;

    private Result mResult;
    private ThreadFinishListener mListener;
    private LocalBroadcastManager mLocalBroadcastManager;
    private MyBookshelfApplicationData mApplicationData;


    public static final class Result {
        private final boolean isSuccess;
        private final int type;
        private final int errorCode;
        private final String errorMessage;

        private Result(boolean isSuccess, int type, int errorCode, String errorMessage) {
            this.isSuccess = isSuccess;
            this.type = type;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() {
            return this.isSuccess;
        }

        public int getType() {
            return this.type;
        }

        public int getErrorCode() {
            return this.errorCode;
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }

        public static Result success(int type) {
            return new Result(true, type, NO_ERROR, "no error");
        }

        public static Result error(int type, int errorCode, String errorMessage) {
            return new Result(false, type, errorCode, errorMessage);
        }

    }


    public interface ThreadFinishListener {
        void deliverBackupResult(Result result);
    }


    public FileBackupThread(Context context, int type) {
        this.type = type;
        this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        this.mApplicationData = (MyBookshelfApplicationData) context.getApplicationContext();
        if (context instanceof ThreadFinishListener) {
            mListener = (ThreadFinishListener) context;
        } else {
            throw new UnsupportedOperationException("Listener is not Implementation.");
        }
    }


    @Override
    public void run() {
        switch (type) {
            case TYPE_EXPORT:
                mResult = exportFile();
                break;
            case TYPE_IMPORT:
                mResult = importFile();
                break;
            case TYPE_BACKUP:
                mResult = backupFile();
                break;
            case TYPE_RESTORE:
                mResult = restoreFile();
                break;
        }
        if (mListener != null && mResult != null) {
            mListener.deliverBackupResult(mResult);
        }
    }


    private Result exportFile() {
        File extDir = Environment.getExternalStorageDirectory();
        String dirPath = extDir.getPath() + APPLICATION_DIRECTORY_PATH;
        File dir = new File(dirPath);

        if (!dir.exists()) {
            boolean isSuccess = dir.mkdirs();
            if (D) Log.d(TAG, "mkdirs(): " + isSuccess);
            return Result.error(TYPE_EXPORT, ERROR_IO_EXCEPTION, "dir.mkdirs() failed");
        }

        File file_books = new File(dirPath + FILE_NAME_SHELF_BOOKS);
        File file_authors = new File(dirPath + FILE_NAME_AUTHORS);

        try {
            List<BookData> books = mApplicationData.loadShelfBooks(null);
            int recodeCount = books.size();
            if (D) Log.d(TAG, "recodeCount : " + recodeCount);
            int count = 0;

            File file_bookshelf = new File(dirPath + "alt_" + FILE_NAME_SHELF_BOOKS);
            OutputStream os_bookshelf = new FileOutputStream(file_bookshelf);
            OutputStreamWriter osr_bookshelf = new OutputStreamWriter(os_bookshelf, Charset.forName("UTF-8"));
            BufferedWriter bw_bookshelf = new BufferedWriter(osr_bookshelf);

            String idx = "isbn,title,author\r\n";
            bw_bookshelf.write(idx);
            for (BookData book : books) {
                String isbn = book.getISBN();
                String title = book.getTitle();
                String author = book.getAuthor();
                String str_book = isbn + "," + title + "," + author + "\r\n";
                bw_bookshelf.write(str_book);
                count++;
                String progress = count + "/" + recodeCount;
                Intent intent = new Intent();
                intent.putExtra(BaseFragment.KEY_PROGRESS_TYPE, PROGRESS_TYPE_EXPORT_BOOKS);
                intent.putExtra(BaseFragment.KEY_PROGRESS, progress);
                intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
                mLocalBroadcastManager.sendBroadcast(intent);
                Thread.sleep(2);
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
                Intent intent = new Intent();
                intent.putExtra(BaseFragment.KEY_PROGRESS_TYPE, PROGRESS_TYPE_EXPORT_AUTHORS);
                intent.putExtra(BaseFragment.KEY_PROGRESS, progress);
                intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
                mLocalBroadcastManager.sendBroadcast(intent);
                Thread.sleep(2);
            }
            bw_authors.close();

            return Result.success(TYPE_EXPORT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Result.error(TYPE_EXPORT, ERROR_FILE_NOT_FOUND, "FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(TYPE_EXPORT, ERROR_IO_EXCEPTION, "IOException");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.error(TYPE_EXPORT, ERROR_INTERRUPTED_EXCEPTION, "InterruptedException");
        }
    }

    private Result importFile() {
        File extDir = Environment.getExternalStorageDirectory();
        String dirPath = extDir.getPath() + APPLICATION_DIRECTORY_PATH;
        File dir = new File(dirPath);

        if (!dir.exists()) {
            boolean isSuccess = dir.mkdirs();
            if (D) Log.d(TAG, "mkdirs(): " + isSuccess);
            return Result.error(TYPE_IMPORT, ERROR_IO_EXCEPTION, "dir.mkdirs() failed");
        }

        File file_books = new File(dirPath + FILE_NAME_SHELF_BOOKS);
        if (!file_books.exists()) {
            if (D) Log.e(TAG, "file_books not found");
            return Result.error(TYPE_IMPORT, ERROR_FILE_NOT_FOUND, "file not found");
        }
        File file_authors = new File(dirPath + FILE_NAME_AUTHORS);
        if (!file_authors.exists()) {
            if (D) Log.e(TAG, "file_authors not found");
            return Result.error(TYPE_IMPORT, ERROR_FILE_NOT_FOUND, "file not found");
        }


        try {
            // insert BookData from CSV
            // count line
            InputStream pre_is_bookshelf = MyBookshelfUtils.getStreamSkipBOM(new FileInputStream(file_books), Charset.forName("UTF-8"));
            InputStreamReader pre_isr_bookshelf = new InputStreamReader(pre_is_bookshelf, Charset.forName("UTF-8"));
            BufferedReader pre_br_bookshelf = new BufferedReader(pre_isr_bookshelf);
            pre_br_bookshelf.readLine(); // skip first line
            int size = 0;
            while ((pre_br_bookshelf.readLine()) != null) {
                size++;
            }
            pre_br_bookshelf.close();
            if (D) Log.d(TAG, "size: " + size);
            // import csv
            InputStream is_bookshelf = MyBookshelfUtils.getStreamSkipBOM(new FileInputStream(file_books), Charset.forName("UTF-8"));
            InputStreamReader isr_bookshelf = new InputStreamReader(is_bookshelf, Charset.forName("UTF-8"));
            BufferedReader br_bookshelf = new BufferedReader(isr_bookshelf);
            String str_line_bookshelf = br_bookshelf.readLine();
            String[] idx_bookshelf = str_line_bookshelf.split(",");
            List<BookData> books = new ArrayList<>();
            int count = 0;
            if (idx_bookshelf.length == 40) {
                while ((str_line_bookshelf = br_bookshelf.readLine()) != null) {
                    if (D) Log.d(TAG, "str: " + str_line_bookshelf);
                    String[] split = MyBookshelfUtils.splitLineWithComma(str_line_bookshelf);
                    BookData book = convertReadeeToBookData(split);
                    books.add(book);
                    count++;
                    String progress = count + "/" + size;
                    Intent intent = new Intent();
                    intent.putExtra(BaseFragment.KEY_PROGRESS_TYPE, PROGRESS_TYPE_IMPORT_BOOKS);
                    intent.putExtra(BaseFragment.KEY_PROGRESS, progress);
                    intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
                    mLocalBroadcastManager.sendBroadcast(intent);
                    Thread.sleep(2);
                }
            }
            if (idx_bookshelf.length == 20) {
                while ((str_line_bookshelf = br_bookshelf.readLine()) != null) {
                    if (D) Log.d(TAG, "str: " + str_line_bookshelf);
                    String[] split = MyBookshelfUtils.splitLineWithComma(str_line_bookshelf);
                    import_MYBOOKSHELF_CSV(split);
                    count++;
                    String progress = count + "/" + size;
                    Intent intent = new Intent();
                    intent.putExtra(BaseFragment.KEY_PROGRESS_TYPE, PROGRESS_TYPE_IMPORT_BOOKS);
                    intent.putExtra(BaseFragment.KEY_PROGRESS, progress);
                    intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
                    mLocalBroadcastManager.sendBroadcast(intent);
                    Thread.sleep(2);
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
            List<String> authors = new ArrayList<>();
            String str_line_authors;
            while ((str_line_authors = br_authors.readLine()) != null) {
                authors.add(str_line_authors);
                count++;
                String progress = count + "/" + size;
                Intent intent = new Intent();
                intent.putExtra(BaseFragment.KEY_PROGRESS_TYPE, PROGRESS_TYPE_IMPORT_AUTHORS);
                intent.putExtra(BaseFragment.KEY_PROGRESS, progress);
                intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
                mLocalBroadcastManager.sendBroadcast(intent);
                Thread.sleep(2);
            }
            br_authors.close();

            if (D) Log.d(TAG, "register start");
            Intent intent = new Intent();
            intent.putExtra(BaseFragment.KEY_PROGRESS_TYPE, PROGRESS_TYPE_REGISTER);
            intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
            mLocalBroadcastManager.sendBroadcast(intent);
            mApplicationData.registerToShelfBooks(books);
            mApplicationData.registerToAuthorsList(authors);
            if (D) Log.d(TAG, "register end");

            return Result.success(TYPE_IMPORT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Result.error(TYPE_IMPORT, ERROR_FILE_NOT_FOUND, "FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(TYPE_IMPORT, ERROR_IO_EXCEPTION, "IOException");
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
            return Result.error(TYPE_IMPORT, ERROR_PATTERN_SYNTAX_EXCEPTION, "PatternSyntaxException");
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.error(TYPE_IMPORT, ERROR_INTERRUPTED_EXCEPTION, "InterruptedException");
        }
    }


    private Result backupFile() {
        String token = mApplicationData.getSharedPreferences().getString(MyBookshelfApplicationData.KEY_ACCESS_TOKEN, null);
        if (token == null) {
            return Result.error(TYPE_BACKUP, ERROR_DBX_EXCEPTION, "No token");
        }
        DbxRequestConfig config = new DbxRequestConfig(CLIENT_IDENTIFIER);
        DbxClientV2 mClient = new DbxClientV2(config, token);
        File extDir = Environment.getExternalStorageDirectory();
        String dirPath = extDir.getPath() + APPLICATION_DIRECTORY_PATH;

        try {
            File file_books = new File(dirPath + FILE_NAME_SHELF_BOOKS);
            if (!file_books.exists()) {
                return Result.error(TYPE_BACKUP, ERROR_FILE_NOT_FOUND, "file_books not found");
            }
            File file_authors = new File(dirPath + FILE_NAME_AUTHORS);
            if (!file_authors.exists()) {
                if (D) Log.e(TAG, "file_authors not found");
                return Result.error(TYPE_BACKUP, ERROR_FILE_NOT_FOUND, "file_authors not found");
            }

            InputStream input_bookshelf = new FileInputStream(file_books);
            mClient.files().uploadBuilder("/MyBookshelf/" + FILE_NAME_SHELF_BOOKS).withMode(WriteMode.OVERWRITE).uploadAndFinish(input_bookshelf, new IOUtil.ProgressListener() {
                @Override
                public void onProgress(long bytesWritten) {
                    String progress = String.format(Locale.JAPAN,"%d",bytesWritten);
                    Intent intent = new Intent();
                    intent.putExtra(BaseFragment.KEY_PROGRESS_TYPE, PROGRESS_TYPE_UPLOAD_BOOKS);
                    intent.putExtra(BaseFragment.KEY_PROGRESS, progress);
                    intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
                    mLocalBroadcastManager.sendBroadcast(intent);
                }
            });


            InputStream input_authors = new FileInputStream(file_authors);
            mClient.files().uploadBuilder("/MyBookshelf/" + FILE_NAME_AUTHORS).withMode(WriteMode.OVERWRITE).uploadAndFinish(input_authors, new IOUtil.ProgressListener() {
                @Override
                public void onProgress(long bytesWritten) {
                    String progress = String.format(Locale.JAPAN,"%d",bytesWritten);
                    Intent intent = new Intent();
                    intent.putExtra(BaseFragment.KEY_PROGRESS_TYPE, PROGRESS_TYPE_UPLOAD_AUTHORS);
                    intent.putExtra(BaseFragment.KEY_PROGRESS, progress);
                    intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
                    mLocalBroadcastManager.sendBroadcast(intent);
                }
            });

            return Result.success(TYPE_BACKUP);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Result.error(TYPE_BACKUP, ERROR_FILE_NOT_FOUND, "FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(TYPE_BACKUP, ERROR_IO_EXCEPTION, "IOException");
        } catch (DbxException e) {
            e.printStackTrace();
            return Result.error(TYPE_BACKUP, ERROR_DBX_EXCEPTION, "DbxException");
        }
    }

    private Result restoreFile() {
        String token = mApplicationData.getSharedPreferences().getString(MyBookshelfApplicationData.KEY_ACCESS_TOKEN, null);
        if (token == null) {
            return Result.error(TYPE_BACKUP, ERROR_DBX_EXCEPTION, "No token");
        }
        DbxRequestConfig config = new DbxRequestConfig(CLIENT_IDENTIFIER);
        DbxClientV2 mClient = new DbxClientV2(config, token);
        File extDir = Environment.getExternalStorageDirectory();
        String dirPath = extDir.getPath() + APPLICATION_DIRECTORY_PATH;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean isSuccess = dir.mkdirs();
            if (D) Log.d(TAG, "mkdirs(): " + isSuccess);
            return Result.error(TYPE_IMPORT, ERROR_IO_EXCEPTION, "dir.mkdirs() failed");
        }

        try {
            SearchResult searchResult_bookshelf = mClient.files().search(DROPBOX_APP_DIRECTORY_PATH, FILE_NAME_SHELF_BOOKS);
            List<SearchMatch> matches_bookshelf = searchResult_bookshelf.getMatches();
            Metadata metadataBookshelf = null;
            for (SearchMatch match : matches_bookshelf) {
                metadataBookshelf = match.getMetadata();
                if (D)
                    Log.d(TAG, "metadataBookshelf: " + metadataBookshelf.getPathLower());
                if (metadataBookshelf.getPathLower().equals(DROPBOX_APP_DIRECTORY_PATH + FILE_NAME_SHELF_BOOKS)) {
                    metadataBookshelf = match.getMetadata();
                    break;
                }
            }
            if (metadataBookshelf == null) {
                return Result.error(TYPE_RESTORE, ERROR_FILE_NOT_FOUND, "metadataBookshelf not found");
            }
            SearchResult searchResult_authors = mClient.files().search(DROPBOX_APP_DIRECTORY_PATH, FILE_NAME_AUTHORS);
            List<SearchMatch> matches_authors = searchResult_authors.getMatches();
            Metadata metadataAuthors = null;
            for (SearchMatch match : matches_authors) {
                metadataAuthors = match.getMetadata();
                if (D)
                    Log.d(TAG, "metadataAuthors: " + metadataAuthors.getPathLower());
                if (metadataAuthors.getPathLower().equals(DROPBOX_APP_DIRECTORY_PATH + FILE_NAME_AUTHORS)) {
                    metadataAuthors = match.getMetadata();
                    break;
                }
            }
            if (metadataAuthors == null) {
                return Result.error(TYPE_RESTORE, ERROR_FILE_NOT_FOUND, "metadataAuthors not found");
            }

            File file_books = new File(dirPath + FILE_NAME_SHELF_BOOKS);
            OutputStream output_bookshelf = new FileOutputStream(file_books);
            mClient.files().download(metadataBookshelf.getPathLower()).download(output_bookshelf, new IOUtil.ProgressListener() {
                @Override
                public void onProgress(long bytesWritten) {
                    String progress = String.format(Locale.JAPAN,"%d",bytesWritten);
                    Intent intent = new Intent();
                    intent.putExtra(BaseFragment.KEY_PROGRESS_TYPE, PROGRESS_TYPE_DOWNLOAD_BOOKS);
                    intent.putExtra(BaseFragment.KEY_PROGRESS, progress);
                    intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
                    mLocalBroadcastManager.sendBroadcast(intent);
                }
            });

            File file_authors = new File(dirPath + FILE_NAME_AUTHORS);
            OutputStream output_authors = new FileOutputStream(file_authors);
            mClient.files().download(metadataAuthors.getPathLower()).download(output_authors, new IOUtil.ProgressListener() {
                @Override
                public void onProgress(long bytesWritten) {
                    String progress = String.format(Locale.JAPAN,"%d",bytesWritten);
                    Intent intent = new Intent();
                    intent.putExtra(BaseFragment.KEY_PROGRESS_TYPE, PROGRESS_TYPE_DOWNLOAD_AUTHORS);
                    intent.putExtra(BaseFragment.KEY_PROGRESS, progress);
                    intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
                    mLocalBroadcastManager.sendBroadcast(intent);
                }
            });

            return Result.success(TYPE_RESTORE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Result.error(TYPE_RESTORE, ERROR_FILE_NOT_FOUND, "FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(TYPE_RESTORE, ERROR_IO_EXCEPTION, "IOException");
        } catch (DownloadErrorException e) {
            e.printStackTrace();
            return Result.error(TYPE_RESTORE, ERROR_DBX_EXCEPTION, "DownloadErrorException");
        } catch (SearchErrorException e) {
            e.printStackTrace();
            return Result.error(TYPE_RESTORE, ERROR_DBX_EXCEPTION, "SearchErrorException");
        } catch (DbxException e) {
            e.printStackTrace();
            return Result.error(TYPE_RESTORE, ERROR_DBX_EXCEPTION, "DbxException");
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


        String url = split[17];
//        String REGEX_CSV_COMMA = ",";
        String REGEX_SURROUND_DOUBLE_QUOTATION = "^\"|\"$";
        String REGEX_SURROUND_BRACKET = "^\\(|\\)$";

        Pattern sdqPattern = Pattern.compile(REGEX_SURROUND_DOUBLE_QUOTATION);
        Matcher matcher = sdqPattern.matcher(url);
        url = matcher.replaceAll("");
        Pattern sbPattern = Pattern.compile(REGEX_SURROUND_BRACKET);
        matcher = sbPattern.matcher(url);
        url = matcher.replaceAll("");

        int index = url.lastIndexOf(".jpg");
        if(index != -1) {
            url = url.substring(0, index+4);
        }else{
            index = url.lastIndexOf(".gif");
            if(index != -1){
                url = url.substring(0, index+4);
            }
        }

        if(D) Log.d(TAG,"url: " + url);


        bookData.setImage(url);
//        bookData.setImage(split[17]);
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
