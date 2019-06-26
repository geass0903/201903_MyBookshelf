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
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
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
    private static final String FILE_NAME_BOOKS = "backup_books.csv";
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
    private boolean isCanceled;

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
        isCanceled = false;
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
        if (mListener != null && mResult != null && !isCanceled) {
            mListener.deliverBackupResult(mResult);
        }
    }

    public void cancel() {
        if (D) Log.d(TAG, "thread cancel");
        isCanceled = true;
    }

    private Result exportFile() {
        int recodeCount;
        int count;
        String line;
        String progress;

        File extDir = Environment.getExternalStorageDirectory();
        String dirPath = extDir.getPath() + APPLICATION_DIRECTORY_PATH;
        File dir = new File(dirPath);

        if (!dir.exists()) {
            boolean isSuccess = dir.mkdirs();
            if (D) Log.d(TAG, "mkdirs(): " + isSuccess);
            return Result.error(TYPE_EXPORT, ERROR_IO_EXCEPTION, "dir.mkdirs() failed");
        }

        File file_books = new File(dirPath + FILE_NAME_BOOKS);
        File file_authors = new File(dirPath + FILE_NAME_AUTHORS);

        try {
            // export books
            List<BookData> books = mApplicationData.loadDatabaseBooks();
            count = 0;
            recodeCount = books.size();
            progress = count + "/" + recodeCount;
            sendProgressMessage(PROGRESS_TYPE_EXPORT_BOOKS, progress);
            BufferedWriter bw_books = MyBookshelfUtils.getBufferedWriter(new FileOutputStream(file_books), Charset.forName("UTF-8"));
            String[] index = MyBookshelfUtils.getShelfBooksIndex();
            line = TextUtils.join(",", index);
            bw_books.write(line + "\r\n");
            for (BookData book : books) {
                line = MyBookshelfUtils.convertBookDataToLine(index, book) + "\r\n";
                bw_books.write(line);
                count++;
                progress = count + "/" + recodeCount;
                sendProgressMessage(PROGRESS_TYPE_EXPORT_BOOKS, progress);
                Thread.sleep(2);
            }
            bw_books.flush();
            bw_books.close();


            // export authors
            List<String> authors = mApplicationData.loadAuthorsList();
            count = 0;
            recodeCount = authors.size();
            progress = count + "/" + recodeCount;
            sendProgressMessage(PROGRESS_TYPE_EXPORT_AUTHORS, progress);
            BufferedWriter bw_authors = MyBookshelfUtils.getBufferedWriter(new FileOutputStream(file_authors), Charset.forName("UTF-8"));
            for (String author : authors) {
                bw_authors.write(author + "\r\n");
                count++;
                progress = count + "/" + recodeCount;
                sendProgressMessage(PROGRESS_TYPE_EXPORT_AUTHORS, progress);
                Thread.sleep(2);
            }
            bw_authors.flush();
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
        int count;
        int size;
        String progress;
        String line;
        List<BookData> books = new ArrayList<>();
        List<String> authors = new ArrayList<>();

        File extDir = Environment.getExternalStorageDirectory();
        String dirPath = extDir.getPath() + APPLICATION_DIRECTORY_PATH;

        File file_books = new File(dirPath + FILE_NAME_BOOKS);
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
            // import books
            count = 0;
            size = getLineCount(new FileInputStream(file_books), Charset.forName("UTF-8")) - 1; // remove count index line
            progress = count + "/" + size;
            sendProgressMessage(PROGRESS_TYPE_IMPORT_BOOKS, progress);
            BufferedReader br_books = MyBookshelfUtils.getBufferedReaderSkipBOM(new FileInputStream(file_books), Charset.forName("UTF-8"));
            String[] index =  br_books.readLine().split(",");
            while( (line = br_books.readLine()) != null ) {
                BookData book = MyBookshelfUtils.convertToBookData(index, line);
                books.add(book);
                count++;
                progress = count + "/" + size;
                sendProgressMessage(PROGRESS_TYPE_IMPORT_BOOKS, progress);
                Thread.sleep(2);
            }
            br_books.close();

            // import authors
            count = 0;
            size = getLineCount(new FileInputStream(file_authors), Charset.forName("UTF-8"));
            progress = count + "/" + size;
            sendProgressMessage(PROGRESS_TYPE_IMPORT_AUTHORS, progress);
            BufferedReader br_authors = MyBookshelfUtils.getBufferedReaderSkipBOM(new FileInputStream(file_authors), Charset.forName("UTF-8"));
            while ((line = br_authors.readLine()) != null) {
                authors.add(line);
                count++;
                progress = count + "/" + size;
                sendProgressMessage(PROGRESS_TYPE_IMPORT_AUTHORS, progress);
                Thread.sleep(2);
            }
            br_authors.close();

            // register database
            sendProgressMessage(PROGRESS_TYPE_REGISTER, "");
            mApplicationData.registerToShelfBooks(books);
            mApplicationData.registerToAuthorsList(authors);

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
            File file_books = new File(dirPath + FILE_NAME_BOOKS);
            if (!file_books.exists()) {
                return Result.error(TYPE_BACKUP, ERROR_FILE_NOT_FOUND, "file_books not found");
            }
            File file_authors = new File(dirPath + FILE_NAME_AUTHORS);
            if (!file_authors.exists()) {
                if (D) Log.e(TAG, "file_authors not found");
                return Result.error(TYPE_BACKUP, ERROR_FILE_NOT_FOUND, "file_authors not found");
            }

            sendProgressMessage(PROGRESS_TYPE_UPLOAD_BOOKS, "0");
            InputStream input_books = new FileInputStream(file_books);
            mClient.files().uploadBuilder(DROPBOX_APP_DIRECTORY_PATH + FILE_NAME_BOOKS).withMode(WriteMode.OVERWRITE).uploadAndFinish(input_books, new IOUtil.ProgressListener() {
                @Override
                public void onProgress(long bytesWritten) {
                    sendProgressMessage(PROGRESS_TYPE_UPLOAD_BOOKS, String.valueOf(bytesWritten));
                }
            });

            sendProgressMessage(PROGRESS_TYPE_UPLOAD_AUTHORS, "0");
            InputStream input_authors = new FileInputStream(file_authors);
            mClient.files().uploadBuilder(DROPBOX_APP_DIRECTORY_PATH + FILE_NAME_AUTHORS).withMode(WriteMode.OVERWRITE).uploadAndFinish(input_authors, new IOUtil.ProgressListener() {
                @Override
                public void onProgress(long bytesWritten) {
                    sendProgressMessage(PROGRESS_TYPE_UPLOAD_AUTHORS, String.valueOf(bytesWritten));
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
            Metadata metadata_books = getMetadata(mClient, FILE_NAME_BOOKS);
            if(metadata_books == null){
                return Result.error(TYPE_RESTORE, ERROR_FILE_NOT_FOUND, "metadata_books not found");
            }
            Metadata metadata_authors = getMetadata(mClient, FILE_NAME_AUTHORS);
            if(metadata_authors == null){
                return Result.error(TYPE_RESTORE, ERROR_FILE_NOT_FOUND, "metadata_authors not found");
            }

            sendProgressMessage(PROGRESS_TYPE_DOWNLOAD_BOOKS, "0");
            File file_books = new File(dirPath + FILE_NAME_BOOKS);
            OutputStream output_books = new FileOutputStream(file_books);
            mClient.files().download(metadata_books.getPathLower()).download(output_books, new IOUtil.ProgressListener() {
                @Override
                public void onProgress(long bytesWritten) {
                    sendProgressMessage(PROGRESS_TYPE_DOWNLOAD_BOOKS, String.valueOf(bytesWritten));
                }
            });

            sendProgressMessage(PROGRESS_TYPE_DOWNLOAD_AUTHORS, "0");
            File file_authors = new File(dirPath + FILE_NAME_AUTHORS);
            OutputStream output_authors = new FileOutputStream(file_authors);
            mClient.files().download(metadata_authors.getPathLower()).download(output_authors, new IOUtil.ProgressListener() {
                @Override
                public void onProgress(long bytesWritten) {
                    sendProgressMessage(PROGRESS_TYPE_DOWNLOAD_AUTHORS, String.valueOf(bytesWritten));
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



    private void sendProgressMessage(int progress_type, String progress) {
        Intent intent = new Intent();
        intent.putExtra(BaseFragment.KEY_PROGRESS_TYPE, progress_type);
        intent.putExtra(BaseFragment.KEY_PROGRESS, progress);
        intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    private int getLineCount(InputStream is, Charset charSet) throws IOException{
        int count = 0;
        BufferedReader br = MyBookshelfUtils.getBufferedReaderSkipBOM(is, charSet);
        while(br.readLine() != null) {
            count++;
        }
        br.close();
        return count;
    }

    private Metadata getMetadata(DbxClientV2 client, final String file_name) throws DbxException {
        SearchResult searchResult = client.files().search(DROPBOX_APP_DIRECTORY_PATH, file_name);
        List<SearchMatch> matches = searchResult.getMatches();
        for (SearchMatch match : matches) {
            Metadata metadata = match.getMetadata();
            if (D) Log.d(TAG, "metadata.getPathLower() : " + metadata.getPathLower());
            if (metadata.getPathLower().equalsIgnoreCase(DROPBOX_APP_DIRECTORY_PATH + file_name)) {
                return metadata;
            }
        }
        return null;
    }




}
