package jp.gr.java_conf.nuranimation.my_bookshelf.model.net;


import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.v2.files.SearchResult;
import com.dropbox.core.v2.files.WriteMode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.database.MyBookshelfDBOpenHelper;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookDataUtils;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BooksOrder;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.Result;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.prefs.MyBookshelfPreferences;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.BaseFragment;


//@SuppressWarnings({"WeakerAccess","unused"})
public class FileBackupThread extends Thread {
    private static final String TAG = FileBackupThread.class.getSimpleName();
    private static final boolean D = true;

    public static final int TYPE_UNKNOWN    = 0;
    public static final int TYPE_EXPORT     = 1;
    public static final int TYPE_IMPORT     = 2;
    public static final int TYPE_BACKUP     = 3;
    public static final int TYPE_RESTORE    = 4;

    private static final String CLIENT_IDENTIFIER = "MyBookshelf/1.0";

    private static final String DROPBOX_APP_DIRECTORY_PATH = "/MyBookshelf/";
    private static final String FILE_NAME_BOOKS = "backup_books.csv";
    private static final String FILE_NAME_AUTHORS = "backup_authors.csv";

    private final MyBookshelfPreferences mPreferences;
    private final MyBookshelfDBOpenHelper mDBOpenHelper;
    private final LocalBroadcastManager mLocalBroadcastManager;
    private final ThreadFinishListener mListener;
    private final Context mContext;
    private final int type;

    private boolean isCanceled;

    public interface ThreadFinishListener {
        void deliverBackupResult(Result result);
    }

    public FileBackupThread(Context context, int type) {
        this.mPreferences = new MyBookshelfPreferences(context.getApplicationContext());
        this.mDBOpenHelper = MyBookshelfDBOpenHelper.getInstance(context.getApplicationContext());
        this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
        this.mContext = context;
        this.type = type;
        isCanceled = false;
        if (context instanceof ThreadFinishListener) {
            mListener = (ThreadFinishListener) context;
        } else {
            throw new UnsupportedOperationException("Listener is not Implementation.");
        }
    }

    @Override
    public void run() {
        Result mResult;
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
            default:
                mResult = Result.BackupError(TYPE_UNKNOWN, Result.ERROR_CODE_UNKNOWN, "Unknown BackupType");
                break;
        }
        if (mListener != null) {
            mListener.deliverBackupResult(mResult);
        }
    }

    public void cancel() {
        if (D) Log.d(TAG, "thread cancel");
        isCanceled = true;
    }


    private Result exportFile() {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return Result.BackupError(TYPE_EXPORT, Result.ERROR_CODE_EXPORT_DIR_NOT_FOUND, "MEDIA NOT MOUNTED");
        }
        File exportDir = mContext.getExternalFilesDir(null);
        if (exportDir == null) {
            return Result.BackupError(TYPE_EXPORT, Result.ERROR_CODE_EXPORT_DIR_NOT_FOUND, "getExternalFilesDir == null");
        }
        if (!exportDir.exists()) {
            boolean isSuccess = exportDir.mkdirs();
            if (D) Log.d(TAG, "mkdirs(): " + isSuccess);
            return Result.BackupError(TYPE_EXPORT, Result.ERROR_CODE_EXPORT_DIR_NOT_FOUND, "mkdirs() failed");
        }

        File file_books = new File(exportDir, FILE_NAME_BOOKS);
        File file_authors = new File(exportDir, FILE_NAME_AUTHORS);

        try {
            // export books
            List<BookData> books = mDBOpenHelper.loadShelfBooks(null, BooksOrder.getShelfBooksOrder(BooksOrder.SHELF_BOOKS_ORDER_CODE_REGISTERED_ASC));
            int recodeCount = books.size();
            int exportCount = 0;
            String message = mContext.getString(R.string.progress_message_export_books);
            String unit = mContext.getString(R.string.progress_unit_book);
            String progress = exportCount + "/" + recodeCount + unit;
            sendProgressMessage(message, progress);
            BufferedWriter bw_books = getBufferedWriter(new FileOutputStream(file_books), Charset.forName("UTF-8"));
            String[] index = BookDataUtils.getShelfBooksIndex();
            String line = TextUtils.join(",", index);
            bw_books.write(line + "\r\n");
            for (BookData book : books) {
                if(isCanceled){
                    bw_books.flush();
                    bw_books.close();
                    return Result.BackupError(TYPE_EXPORT,Result.ERROR_CODE_EXPORT_CANCELED, "export canceled");
                }
                line = BookDataUtils.convertBookDataToLine(index, book) + "\r\n";
                bw_books.write(line);
                exportCount++;
                progress = exportCount + "/" + recodeCount + unit;
                sendProgressMessage(message, progress);
            }
            bw_books.flush();
            bw_books.close();

            // export authors
            List<String> authors = mDBOpenHelper.loadAuthorsList();
            recodeCount = authors.size();
            exportCount = 0;
            message = mContext.getString(R.string.progress_message_export_authors);
            progress = exportCount + "/" + recodeCount;
            sendProgressMessage(message, progress);
            BufferedWriter bw_authors = getBufferedWriter(new FileOutputStream(file_authors), Charset.forName("UTF-8"));
            for (String author : authors) {
                if(isCanceled){
                    bw_authors.flush();
                    bw_authors.close();
                    return Result.BackupError(TYPE_EXPORT,Result.ERROR_CODE_EXPORT_CANCELED, "export canceled");
                }
                bw_authors.write(author + "\r\n");
                exportCount++;
                progress = exportCount + "/" + recodeCount;
                sendProgressMessage(message, progress);
            }
            bw_authors.flush();
            bw_authors.close();
        } catch (IOException e) {
            e.printStackTrace();
            return Result.BackupError(TYPE_EXPORT, Result.ERROR_CODE_IO_EXCEPTION, "IOException");
        }
        return Result.BackupSuccess(TYPE_EXPORT);
    }

    private Result importFile() {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return Result.BackupError(TYPE_IMPORT, Result.ERROR_CODE_IMPORT_DIR_NOT_FOUND, "MEDIA NOT MOUNTED");
        }
        File importDir = mContext.getExternalFilesDir(null);
        if (importDir == null) {
            return Result.BackupError(TYPE_IMPORT, Result.ERROR_CODE_IMPORT_DIR_NOT_FOUND, "getExternalFilesDir == null");
        }
        File file_books = new File(importDir, FILE_NAME_BOOKS);
        if (!file_books.exists()) {
            if (D) Log.e(TAG, "file_books not found");
            return Result.BackupError(TYPE_IMPORT, Result.ERROR_CODE_FILE_NOT_FOUND, "file not found");
        }
        File file_authors = new File(importDir, FILE_NAME_AUTHORS);
        if (!file_authors.exists()) {
            if (D) Log.e(TAG, "file_authors not found");
            return Result.BackupError(TYPE_IMPORT, Result.ERROR_CODE_FILE_NOT_FOUND, "file not found");
        }

        try {
            // import books
            int recodeCount = getLineCount(new FileInputStream(file_books), Charset.forName("UTF-8")) - 1; // remove count index line
            int importCount = 0;
            String message = mContext.getString(R.string.progress_message_import_books);
            String unit = mContext.getString(R.string.progress_unit_book);
            String progress = importCount + "/" + recodeCount + unit;
            sendProgressMessage(message, progress);

            List<BookData> books = new ArrayList<>(1000);
            BufferedReader br_books = getBufferedReaderSkipBOM(new FileInputStream(file_books), Charset.forName("UTF-8"));
            String line = br_books.readLine();
            String[] index = BookDataUtils.splitLineWithComma(line);
            while ((line = br_books.readLine()) != null) {
                if (isCanceled) {
                    br_books.close();
                    return Result.BackupError(TYPE_IMPORT, Result.ERROR_CODE_IMPORT_CANCELED, "import canceled");
                }
                BookData book = BookDataUtils.convertToBookData(index, line);
                books.add(book);
                importCount++;
                progress = importCount + "/" + recodeCount + unit;
                sendProgressMessage(message, progress);
            }
            br_books.close();

            // import authors
            recodeCount = getLineCount(new FileInputStream(file_authors), Charset.forName("UTF-8"));
            importCount = 0;
            message = mContext.getString(R.string.progress_message_import_authors);
            progress = importCount + "/" + recodeCount;
            sendProgressMessage(message, progress);

            List<String> authors = new ArrayList<>();
            BufferedReader br_authors = getBufferedReaderSkipBOM(new FileInputStream(file_authors), Charset.forName("UTF-8"));
            while ((line = br_authors.readLine()) != null) {
                if (isCanceled) {
                    br_authors.close();
                    return Result.BackupError(TYPE_IMPORT, Result.ERROR_CODE_IMPORT_CANCELED, "import canceled");
                }
                authors.add(line);
                importCount++;
                progress = importCount + "/" + recodeCount;
                sendProgressMessage(message, progress);
            }
            br_authors.close();

            // register database
            message = mContext.getString(R.string.progress_message_register);
            sendProgressMessage(message, "");
            if (isCanceled) {
                return Result.BackupError(TYPE_IMPORT, Result.ERROR_CODE_IMPORT_CANCELED, "import canceled");
            }
            mDBOpenHelper.dropTableShelfBooks();
            mDBOpenHelper.registerToShelfBooks(books);
            mDBOpenHelper.dropTableAuthorsList();
            mDBOpenHelper.registerToAuthorsList(authors);
            return Result.BackupSuccess(TYPE_IMPORT);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.BackupError(TYPE_IMPORT, Result.ERROR_CODE_IO_EXCEPTION, "IOException");
        }
    }

    private Result backupFile() {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return Result.BackupError(TYPE_BACKUP, Result.ERROR_CODE_IMPORT_DIR_NOT_FOUND, "MEDIA NOT MOUNTED");
        }
        File importDir = mContext.getExternalFilesDir(null);
        if (importDir == null) {
            return Result.BackupError(TYPE_BACKUP, Result.ERROR_CODE_IMPORT_DIR_NOT_FOUND, "getExternalFilesDir == null");
        }
        File file_books = new File(importDir, FILE_NAME_BOOKS);
        if (!file_books.exists()) {
            if (D) Log.e(TAG, "file_books not found");
            return Result.BackupError(TYPE_BACKUP, Result.ERROR_CODE_FILE_NOT_FOUND, "file not found");
        }
        File file_authors = new File(importDir, FILE_NAME_AUTHORS);
        if (!file_authors.exists()) {
            if (D) Log.e(TAG, "file_authors not found");
            return Result.BackupError(TYPE_BACKUP, Result.ERROR_CODE_FILE_NOT_FOUND, "file not found");
        }

        String token = mPreferences.getAccessToken();
        if (token == null) {
            return Result.BackupError(TYPE_BACKUP, Result.ERROR_CODE_DBX_EXCEPTION, "No token");
        }
        DbxRequestConfig config = new DbxRequestConfig(CLIENT_IDENTIFIER);
        DbxClientV2 mClient = new DbxClientV2(config, token);

        try {
            // Upload books
            if(isCanceled){
                return Result.BackupError(TYPE_BACKUP, Result.ERROR_CODE_UPLOAD_CANCELED, "Upload canceled");
            }
            String message = mContext.getString(R.string.progress_message_upload_books);
            sendProgressMessage(message, "");
            InputStream input_books = new FileInputStream(file_books);
            mClient.files().uploadBuilder(DROPBOX_APP_DIRECTORY_PATH + FILE_NAME_BOOKS).withMode(WriteMode.OVERWRITE).uploadAndFinish(input_books);

            // Upload authors
            if(isCanceled){
                return Result.BackupError(TYPE_BACKUP, Result.ERROR_CODE_UPLOAD_CANCELED, "Upload canceled");
            }
            message = mContext.getString(R.string.progress_message_upload_authors);
            sendProgressMessage(message, "");
            InputStream input_authors = new FileInputStream(file_authors);
            mClient.files().uploadBuilder(DROPBOX_APP_DIRECTORY_PATH + FILE_NAME_AUTHORS).withMode(WriteMode.OVERWRITE).uploadAndFinish(input_authors);
            return Result.BackupSuccess(TYPE_BACKUP);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.BackupError(TYPE_BACKUP, Result.ERROR_CODE_IO_EXCEPTION, "IOException");
        } catch (DbxException e) {
            e.printStackTrace();
            return Result.BackupError(TYPE_BACKUP, Result.ERROR_CODE_DBX_EXCEPTION, "DbxException");
        }
    }

    private Result restoreFile() {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return Result.BackupError(TYPE_RESTORE, Result.ERROR_CODE_EXPORT_DIR_NOT_FOUND, "MEDIA NOT MOUNTED");
        }
        File exportDir = mContext.getExternalFilesDir(null);
        if (exportDir == null) {
            return Result.BackupError(TYPE_RESTORE, Result.ERROR_CODE_EXPORT_DIR_NOT_FOUND, "getExternalFilesDir == null");
        }
        if (!exportDir.exists()) {
            boolean isSuccess = exportDir.mkdirs();
            if (D) Log.d(TAG, "mkdirs(): " + isSuccess);
            return Result.BackupError(TYPE_RESTORE, Result.ERROR_CODE_EXPORT_DIR_NOT_FOUND, "mkdirs() failed");
        }
        File file_books = new File(exportDir, FILE_NAME_BOOKS);
        File file_authors = new File(exportDir, FILE_NAME_AUTHORS);

        String token = mPreferences.getAccessToken();
        if (token == null) {
            return Result.BackupError(TYPE_RESTORE, Result.ERROR_CODE_DBX_EXCEPTION, "No token");
        }
        DbxRequestConfig config = new DbxRequestConfig(CLIENT_IDENTIFIER);
        DbxClientV2 mClient = new DbxClientV2(config, token);

        try {
            // Download books
            Metadata metadata_books = getMetadata(mClient, FILE_NAME_BOOKS);
            if (metadata_books == null) {
                return Result.BackupError(TYPE_RESTORE, Result.ERROR_CODE_FILE_NOT_FOUND, "metadata_books not found");
            }
            if (isCanceled) {
                return Result.BackupError(TYPE_RESTORE, Result.ERROR_CODE_DOWNLOAD_CANCELED, "Download canceled");
            }
            String message = mContext.getString(R.string.progress_message_download_books);
            sendProgressMessage(message, "");
            OutputStream output_books = new FileOutputStream(file_books);
            mClient.files().download(metadata_books.getPathLower()).download(output_books);

            // Download authors
            Metadata metadata_authors = getMetadata(mClient, FILE_NAME_AUTHORS);
            if (metadata_authors == null) {
                return Result.BackupError(TYPE_RESTORE, Result.ERROR_CODE_FILE_NOT_FOUND, "metadata_authors not found");
            }
            if (isCanceled) {
                return Result.BackupError(TYPE_RESTORE, Result.ERROR_CODE_DOWNLOAD_CANCELED, "Download canceled");
            }
            message = mContext.getString(R.string.progress_message_download_authors);
            sendProgressMessage(message, "");
            OutputStream output_authors = new FileOutputStream(file_authors);
            mClient.files().download(metadata_authors.getPathLower()).download(output_authors);
            if (isCanceled) {
                return Result.BackupError(TYPE_RESTORE, Result.ERROR_CODE_DOWNLOAD_CANCELED, "Download canceled");
            }
            return Result.BackupSuccess(TYPE_RESTORE);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.BackupError(TYPE_RESTORE, Result.ERROR_CODE_IO_EXCEPTION, "IOException");
        } catch (DbxException e) {
            e.printStackTrace();
            return Result.BackupError(TYPE_RESTORE, Result.ERROR_CODE_DBX_EXCEPTION, "DbxException");
        }
    }

    private void sendProgressMessage(String message, String progress) {
        Intent intent = new Intent();
        intent.putExtra(BaseFragment.KEY_PROGRESS_MESSAGE, message);
        intent.putExtra(BaseFragment.KEY_PROGRESS, progress);
        intent.setAction(BaseFragment.FILTER_ACTION_UPDATE_PROGRESS);
        mLocalBroadcastManager.sendBroadcast(intent);
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

    private int getLineCount(InputStream is, Charset charSet) throws IOException {
        int count = 0;
        BufferedReader br = getBufferedReaderSkipBOM(is, charSet);
        while (br.readLine() != null) {
            count++;
        }
        br.close();
        return count;
    }

    private BufferedReader getBufferedReaderSkipBOM(InputStream is, Charset charSet) throws IOException {
        InputStreamReader isr;
        BufferedReader br;

        if (!(charSet == Charset.forName("UTF-8"))) {
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            return br;
        }

        if (!is.markSupported()) {
            is = new BufferedInputStream(is);
        }
        is.mark(3);
        if (is.available() >= 3) {
            byte[] b = {0, 0, 0};
            int bytes = is.read(b, 0, 3);
            if (bytes == 3 && b[0] != (byte) 0xEF || b[1] != (byte) 0xBB || b[2] != (byte) 0xBF) {
                is.reset();
            }
        }
        isr = new InputStreamReader(is, charSet);
        br = new BufferedReader(isr);
        return br;
    }

    private BufferedWriter getBufferedWriter(OutputStream os, Charset charSet) {
        OutputStreamWriter osr = new OutputStreamWriter(os, charSet);
        return new BufferedWriter(osr);
    }


















}
