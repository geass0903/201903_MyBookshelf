package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.util.Log;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.util.IOUtil;
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


   boolean backup(String dirPath, String filename){
        try{
            File file = new File(dirPath + filename);
            if(!file.exists()){
                if(D) Log.e(TAG,"File not found");
                return false;
            }

            InputStream input = new FileInputStream(file);

            IOUtil.ProgressListener listener = new IOUtil.ProgressListener() {
                @Override
                public void onProgress(long bytesWritten) {
                    if(D) Log.d(TAG,"Upload : " + bytesWritten);
                }
            };

            mClient.files().uploadBuilder("/MyBookshelf/" + filename).withMode(WriteMode.OVERWRITE).uploadAndFinish(input,listener);

        }catch (Exception e){
            if(D) Log.e(TAG,"Upload Error: " + e);
            return false;
        }
        return true;
    }

    boolean restore(String dirPath, String filename){
        try{
            SearchResult result = mClient.files().search("",filename);
            List<SearchMatch> matches = result.getMatches();
            Metadata filedata = null;
            Metadata metadata = null;
            for(SearchMatch match : matches){
                metadata = match.getMetadata();
                if(D) Log.d(TAG,"name: " + metadata.getName());
                if(D) Log.d(TAG,"pathLower: " + metadata.getPathLower());
                if(metadata.getPathLower().equals("/mybookshelf/" + filename)){
                    filedata = metadata;
                    break;
                }

            }
            if(filedata == null){
                if(D) Log.e(TAG,"File not found");
                return false;
            }

            File dir = new File(dirPath);
            if(!dir.exists()){
                boolean success = dir.mkdirs();
                if(D) Log.d(TAG,"mkdirs(): " + success);
            }

            File file = new File(dirPath + filename);
            OutputStream output = new FileOutputStream(file);

            IOUtil.ProgressListener listener = new IOUtil.ProgressListener() {
                @Override
                public void onProgress(long bytesWritten) {
                    if(D) Log.d(TAG,"Download : " + bytesWritten);
                }
            };
            mClient.files().download(filedata.getPathLower()).download(output,listener);
        } catch (Exception e){
            if(D) Log.e(TAG,"Download Error: " + e);
            return false;
        }
        return true;
    }

}
