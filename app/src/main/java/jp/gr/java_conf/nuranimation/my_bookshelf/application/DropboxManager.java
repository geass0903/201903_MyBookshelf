package jp.gr.java_conf.nuranimation.my_bookshelf.application;

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

import jp.gr.java_conf.nuranimation.my_bookshelf.R;

public class DropboxManager {
    private static final boolean D = true;
    private static final String TAG = DropboxManager.class.getSimpleName();
    private static final String CLIENT_IDENTIFIER = "MyBookshelf/1.0";

    private Context mContext;
    private DbxClientV2 mClient;


    public DropboxManager(Context context){
        mContext = context;
    }


    public void setToken(String token){
        DbxRequestConfig config = new DbxRequestConfig(CLIENT_IDENTIFIER);
        mClient = new DbxClientV2(config,token);
    }

    public void startAuthenticate(){
        Auth.startOAuth2Authentication(mContext,mContext.getString(R.string.Dropbox_App_Key));
    }

    public String getAccessToken(){
        return Auth.getOAuth2Token();
    }


    public int backup() {
        int error = ErrorStatus.No_Error;
        try {
            File extDir = Environment.getExternalStorageDirectory();
            String dirPath = extDir.getPath() + FileManager.Application_DirectoryPath;
            String FILENAME_BOOKSHELF = FileManager.FileName_Bookshelf;
            String FILENAME_AUTHORS = FileManager.FileName_Authors;

            File file_books = new File(dirPath + FILENAME_BOOKSHELF);
            if (!file_books.exists()) {
                if (D) Log.e(TAG, "file_books not success");
                error = ErrorStatus.Error_File_Bookshelf_not_found;
                return error;
            }
            File file_authors = new File(dirPath + FILENAME_AUTHORS);
            if (!file_authors.exists()) {
                if (D) Log.e(TAG, "file_authors not success");
                error = ErrorStatus.Error_File_Authors_not_found;
                return error;
            }
            InputStream input_bookshelf = new FileInputStream(file_books);
            InputStream input_authors = new FileInputStream(file_authors);
            mClient.files().uploadBuilder("/MyBookshelf/" + FILENAME_BOOKSHELF).withMode(WriteMode.OVERWRITE).uploadAndFinish(input_bookshelf);
            mClient.files().uploadBuilder("/MyBookshelf/" + FILENAME_AUTHORS).withMode(WriteMode.OVERWRITE).uploadAndFinish(input_authors);
        } catch (Exception e) {
            if (D) Log.e(TAG, "Upload Error: " + e);
            error = ErrorStatus.Error_Upload_failed;
            return error;
        }
        return error;
    }

    public int restore(){
        int error = ErrorStatus.No_Error;
        try{
            String FILENAME_BOOKSHELF = FileManager.FileName_Bookshelf;
            String FILENAME_AUTHORS   = FileManager.FileName_Authors;
            SearchResult searchResult_bookshelf = mClient.files().search(FileManager.Dropbox_App_DirectoryPath,FILENAME_BOOKSHELF);
            List<SearchMatch> matches_bookshelf = searchResult_bookshelf.getMatches();
            Metadata metadataBookshelf = null;
            for(SearchMatch match : matches_bookshelf){
                metadataBookshelf = match.getMetadata();
                if(D) Log.d(TAG,"metadataBookshelf: " + metadataBookshelf.getPathLower());
                if(metadataBookshelf.getPathLower().equals(FileManager.Dropbox_App_DirectoryPath + FILENAME_BOOKSHELF)){
                    metadataBookshelf = match.getMetadata();
                    break;
                }
            }
            if(metadataBookshelf == null){
                if(D) Log.d(TAG,"metadataBookshelf not success");
                error = ErrorStatus.Error_File_Bookshelf_not_found;
                return error;
            }
            SearchResult searchResult_authors = mClient.files().search(FileManager.Dropbox_App_DirectoryPath,FILENAME_AUTHORS);
            List<SearchMatch> matches_authors = searchResult_authors.getMatches();
            Metadata metadataAuthors = null;
            for(SearchMatch match : matches_authors){
                metadataAuthors = match.getMetadata();
                if(D) Log.e(TAG,"metadataAuthors: " + metadataAuthors.getPathLower());
                if(metadataAuthors.getPathLower().equals(FileManager.Dropbox_App_DirectoryPath + FILENAME_AUTHORS)){
                    metadataAuthors = match.getMetadata();
                    break;
                }
            }
            if(metadataAuthors == null){
                if(D) Log.e(TAG,"metadataAuthors not success");
                error = ErrorStatus.Error_File_Authors_not_found;
                return error;
            }

            File extDir = Environment.getExternalStorageDirectory();
            String dirPath = extDir.getPath() + FileManager.Application_DirectoryPath;

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
            error = ErrorStatus.Error_Download_failed;
            return error;
        }
        return error;
    }

}
