package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.v2.files.SearchResult;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

class DropboxManager {
    private static final boolean D = true;
    private static final String TAG = DropboxManager.class.getSimpleName();
    private static final String CLIENT_IDENTIFIER = "MyBookshelf/1.0";

    private Context mContext;
    private DbxClientV2 mClient;


    DropboxManager(Context context){
        mContext = context;
    }


    void setToken(String token){
        DbxRequestConfig config = new DbxRequestConfig(CLIENT_IDENTIFIER);
        mClient = new DbxClientV2(config,token);
    }

    void startAuthenticate(){
        Auth.startOAuth2Authentication(mContext,mContext.getString(R.string.DROPBOX_APP_KEY));
    }

    String getAccessToken(){
        return Auth.getOAuth2Token();
    }


    int backup() {
        int error = ErrorStatus.Error_NO_ERROR;
        try {
            File extDir = Environment.getExternalStorageDirectory();
            String dirPath = extDir.getPath() + FileManager.APP_DIR_PATH;
            String FILENAME_BOOKSHELF = FileManager.FILENAME_BOOKSHELF;
            String FILENAME_AUTHORS = FileManager.FILENAME_AUTHORS;

            File file_books = new File(dirPath + FILENAME_BOOKSHELF);
            if (!file_books.exists()) {
                if (D) Log.e(TAG, "file_books not found");
                error = ErrorStatus.Error_FILE_BOOKSHLF_NOT_FOUND;
                return error;
            }
            File file_authors = new File(dirPath + FILENAME_AUTHORS);
            if (!file_authors.exists()) {
                if (D) Log.e(TAG, "file_authors not found");
                error = ErrorStatus.Error_FILE_AUTHORS_NOT_FOUND;
                return error;
            }
            InputStream input_bookshelf = new FileInputStream(file_books);
            InputStream input_authors = new FileInputStream(file_authors);
            mClient.files().uploadBuilder("/MyBookshelf/" + FILENAME_BOOKSHELF).withMode(WriteMode.OVERWRITE).uploadAndFinish(input_bookshelf);
            mClient.files().uploadBuilder("/MyBookshelf/" + FILENAME_AUTHORS).withMode(WriteMode.OVERWRITE).uploadAndFinish(input_authors);
        } catch (Exception e) {
            if (D) Log.e(TAG, "Upload Error: " + e);
            error = ErrorStatus.Error_UPLOAD_ERROR;
            return error;
        }
        return error;
    }

    int restore(){
        int error = ErrorStatus.Error_NO_ERROR;
        try{
            String FILENAME_BOOKSHELF = FileManager.FILENAME_BOOKSHELF;
            String FILENAME_AUTHORS   = FileManager.FILENAME_AUTHORS;
            SearchResult searchResult_bookshelf = mClient.files().search(FileManager.DROPBOX_APP_DIR_PATH,FILENAME_BOOKSHELF);
            List<SearchMatch> matches_bookshelf = searchResult_bookshelf.getMatches();
            Metadata metadataBookshelf = null;
            for(SearchMatch match : matches_bookshelf){
                metadataBookshelf = match.getMetadata();
                if(D) Log.d(TAG,"metadataBookshelf: " + metadataBookshelf.getPathLower());
                if(metadataBookshelf.getPathLower().equals(FileManager.DROPBOX_APP_DIR_PATH + FILENAME_BOOKSHELF)){
                    metadataBookshelf = match.getMetadata();
                    break;
                }
            }
            if(metadataBookshelf == null){
                if(D) Log.d(TAG,"metadataBookshelf not found");
                error = ErrorStatus.Error_FILE_BOOKSHLF_NOT_FOUND;
                return error;
            }
            SearchResult searchResult_authors = mClient.files().search(FileManager.DROPBOX_APP_DIR_PATH,FILENAME_AUTHORS);
            List<SearchMatch> matches_authors = searchResult_authors.getMatches();
            Metadata metadataAuthors = null;
            for(SearchMatch match : matches_authors){
                metadataAuthors = match.getMetadata();
                if(D) Log.e(TAG,"metadataAuthors: " + metadataAuthors.getPathLower());
                if(metadataAuthors.getPathLower().equals(FileManager.DROPBOX_APP_DIR_PATH + FILENAME_AUTHORS)){
                    metadataAuthors = match.getMetadata();
                    break;
                }
            }
            if(metadataAuthors == null){
                if(D) Log.e(TAG,"metadataAuthors not found");
                error = ErrorStatus.Error_FILE_AUTHORS_NOT_FOUND;
                return error;
            }

            File extDir = Environment.getExternalStorageDirectory();
            String dirPath = extDir.getPath() + FileManager.APP_DIR_PATH;

            File dir = new File(dirPath);
            if(!dir.exists()){
                boolean success = dir.mkdirs();
                if(D) Log.d(TAG,"mkdirs(): " + success);
            }
            File file_bookshelf = new File(dirPath + FILENAME_BOOKSHELF);
            OutputStream output_bookshelf = new FileOutputStream(file_bookshelf);
            mClient.files().download(metadataBookshelf.getPathLower()).download(output_bookshelf);

            File file_authors = new File(dirPath + FILENAME_AUTHORS);
            OutputStream output_authors = new FileOutputStream(file_authors);
            mClient.files().download(metadataAuthors.getPathLower()).download(output_authors);

        } catch (Exception e){
            if(D) Log.e(TAG,"Download Error: " + e);
            error = ErrorStatus.Error_DOWNLOAD_ERROR;
            return error;
        }
        return error;
    }

}
