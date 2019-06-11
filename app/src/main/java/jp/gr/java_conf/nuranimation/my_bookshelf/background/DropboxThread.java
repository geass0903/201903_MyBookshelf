package jp.gr.java_conf.nuranimation.my_bookshelf.background;

import android.content.Context;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.SearchErrorException;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.v2.files.SearchResult;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class DropboxThread extends Thread {
    private static final String TAG = DropboxThread.class.getSimpleName();
    private static final boolean D = true;

    private static final String CLIENT_IDENTIFIER = "MyBookshelf/1.0";

    private static final String DROPBOX_APP_DIRECTORY_PATH = "/MyBookshelf/";
    private static final String APPLICATION_DIRECTORY_PATH = "/Android/data/jp.gr.java_conf.nuranimation.my_bookshelf/";
    private static final String FILE_NAME_SHELF_BOOKS = "backup_bookshelf.csv";
    private static final String FILE_NAME_AUTHORS = "backup_authors.csv";


    public static final int NO_ERROR = 0;
    public static final int ERROR_FILE_NOT_FOUND = 1;
    public static final int ERROR_IO_EXCEPTION = 2;
    public static final int ERROR_DBX_EXCEPTION = 3;


    public static final int TYPE_BACKUP = 1;
    public static final int TYPE_RESTORE = 2;


    private final String token;
    private final int type;

    private Result mResult;
    private ThreadFinishListener mListener;
    private LocalBroadcastManager mLocalBroadcastManager;

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
        void deliverBackupResult(Result result);
        void deliverRestoreResult(Result result);
    }


    public DropboxThread(Context context, int type, String token) {
        this.type = type;
        this.token = token;
        this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        if (context instanceof ThreadFinishListener) {
            mListener = (ThreadFinishListener) context;
        } else {
            throw new UnsupportedOperationException("Listener is not Implementation.");
        }
    }


    @Override
    public void run() {
        DbxRequestConfig config = new DbxRequestConfig(CLIENT_IDENTIFIER);
        DbxClientV2 mClient = new DbxClientV2(config, token);
        File extDir = Environment.getExternalStorageDirectory();
        String dirPath = extDir.getPath() + APPLICATION_DIRECTORY_PATH;

        int retried = 0;

        switch (type) {
            case TYPE_BACKUP:
                while (retried < 3) {
                    try {
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

                        InputStream input_bookshelf = new FileInputStream(file_books);
                        InputStream input_authors = new FileInputStream(file_authors);
                        mClient.files().uploadBuilder("/MyBookshelf/" + FILE_NAME_SHELF_BOOKS).withMode(WriteMode.OVERWRITE).uploadAndFinish(input_bookshelf);
                        mClient.files().uploadBuilder("/MyBookshelf/" + FILE_NAME_AUTHORS).withMode(WriteMode.OVERWRITE).uploadAndFinish(input_authors);
                        mResult = Result.success();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        mResult = Result.error(ERROR_FILE_NOT_FOUND, "FileNotFoundException");
                    } catch (IOException e) {
                        e.printStackTrace();
                        mResult = Result.error(ERROR_IO_EXCEPTION, "IOException");
                    } catch (DbxException e) {
                        e.printStackTrace();
                        mResult = Result.error(ERROR_DBX_EXCEPTION, "DbxException");
                    }
                    if (mResult.isSuccess()) {
                        break;
                    }
                    retried++;
                }
                break;
            case TYPE_RESTORE:
                while (retried < 3) {
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
                            if (D) Log.d(TAG, "metadataBookshelf not found");
                            mResult = Result.error(ERROR_FILE_NOT_FOUND, "file not found");
                            break;
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
                            if (D) Log.e(TAG, "metadataAuthors not found");
                            mResult = Result.error(ERROR_FILE_NOT_FOUND, "file not found");
                            break;
                        }
                        File dir = new File(dirPath);
                        if (!dir.exists()) {
                            boolean isSuccess = dir.mkdirs();
                            if (!isSuccess) {
                                mResult = Result.error(ERROR_IO_EXCEPTION, "dir.mkdirs() failed");
                                break;
                            }
                        }
                        File file_bookshelf = new File(dirPath + FILE_NAME_SHELF_BOOKS);
                        OutputStream output_bookshelf = new FileOutputStream(file_bookshelf);
                        mClient.files().download(metadataBookshelf.getPathLower()).download(output_bookshelf);

                        File file_authors = new File(dirPath + FILE_NAME_AUTHORS);
                        OutputStream output_authors = new FileOutputStream(file_authors);
                        mClient.files().download(metadataAuthors.getPathLower()).download(output_authors);
                        mResult = Result.success();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        mResult = Result.error(ERROR_FILE_NOT_FOUND, "file not found");
                    } catch (IOException e) {
                        e.printStackTrace();
                        mResult = Result.error(ERROR_IO_EXCEPTION, "IOException");
                    } catch (DownloadErrorException e) {
                        e.printStackTrace();
                        mResult = Result.error(ERROR_DBX_EXCEPTION, "DownloadErrorException");
                    } catch (SearchErrorException e) {
                        e.printStackTrace();
                        mResult = Result.error(ERROR_DBX_EXCEPTION, "SearchErrorException");
                    } catch (DbxException e) {
                        e.printStackTrace();
                        mResult = Result.error(ERROR_DBX_EXCEPTION, "DbxException");
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
                case TYPE_BACKUP:
                    mListener.deliverBackupResult(mResult);
                    break;
                case TYPE_RESTORE:
                    mListener.deliverRestoreResult(mResult);
                    break;
            }
        }
    }
}
