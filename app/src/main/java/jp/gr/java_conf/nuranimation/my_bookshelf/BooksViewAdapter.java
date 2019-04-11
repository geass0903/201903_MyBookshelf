package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class BooksViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    public static final String TAG = BooksViewAdapter.class.getSimpleName();
    private static final boolean D = true;

    private static final int ITEM_VIEW_TYPE_NEXT_LOAD    = 0;
    private static final int ITEM_VIEW_TYPE_LOADING      = 1;
    private static final int ITEM_VIEW_TYPE_ITEM         = 2;

    private List<BookData> list;

    private Context mContext;
    private RecyclerView mRecyclerView;
    private OnBookClickListener mListener;

    private boolean dispLoadNext = false;
    private boolean isLoading = false;
    private boolean downloadFlg = false;
    private File downloadDir;


    BooksViewAdapter(List<BookData> list,boolean download) {
        this.list = list;
        this.downloadFlg = download;


//        downloadDir = mContext.getFilesDir();
        File dir = Environment.getExternalStorageDirectory();
        String dirPath = dir.getPath() + "/Android/data/jp.gr.java_conf.nuranimation.my_bookshelf/BookImage";
        downloadDir = new File(dirPath);
        if(!downloadDir.exists()){
            boolean success = downloadDir.mkdirs();
            if(D) Log.d(TAG,"mkdirs(): " + success);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView= recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    public void setContext(Context context){
        mContext = context;
    }

    @Override
    public int getItemCount() {
        if(dispLoadNext){
            return list.size() + 1;
        }else{
            return list.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(isFooter(position)){ // exist Footer
            if(isLoading){
                return ITEM_VIEW_TYPE_LOADING;
            }else {
                return ITEM_VIEW_TYPE_NEXT_LOAD;
            }
        }
        return ITEM_VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
        View inflate;
        if(viewType == ITEM_VIEW_TYPE_NEXT_LOAD){
            inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_button,viewGroup,false);
            inflate.setOnClickListener(this);
            inflate.setOnLongClickListener(this);
            return new ShelfLoadViewHolder(inflate);
        }
        if(viewType == ITEM_VIEW_TYPE_LOADING){
            inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_loading,viewGroup,false);
            inflate.setOnClickListener(this);
            inflate.setOnLongClickListener(this);
            return new ShelfLoadingViewHolder(inflate);
        }
        inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_shelf,viewGroup,false);
        inflate.setOnClickListener(this);
        inflate.setOnLongClickListener(this);
        return new BooksViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(position != list.size()) {
            if (holder.getItemViewType() == ITEM_VIEW_TYPE_ITEM && holder instanceof BooksViewHolder) {
                BooksViewHolder viewHolder = (BooksViewHolder) holder;
                viewHolder.draweeView.setImageURI(getUri(list.get(position)));
                viewHolder.titleView.setText(list.get(position).getTitle());
                viewHolder.authorView.setText(list.get(position).getAuthor());
                viewHolder.publisherView.setText(list.get(position).getPublisher());
            }
        }
    }

    void setClickListener(OnBookClickListener listener){
        this.mListener = listener;
    }

    void setLoadNext(){
        dispLoadNext = true;
        notifyItemInserted(list.size());
    }

    void startLoadNext(){
        dispLoadNext = true;
        isLoading = true;
        notifyItemChanged(list.size());
    }

    void finishLoadNext(){
        dispLoadNext = false;
        isLoading = false;
        notifyItemRemoved(list.size());
    }

    @Override
    public void onClick(View view) {
        if(mRecyclerView != null && mListener != null){
            BookData data = null;
            int position = mRecyclerView.getChildAdapterPosition(view);
            if(!isFooter(position)){
                data = list.get(position);
            }
            mListener.onBookClick(this, position, data);
        }
    }

    @Override
    public boolean onLongClick(View view){
        if(mRecyclerView != null && mListener != null){
            BookData data = null;
            int position = mRecyclerView.getChildAdapterPosition(view);
            if(!isFooter(position)){
                data = list.get(position);
            }
            mListener.onBookLongClick(this, position, data);
            return true;
        }
        return false;
    }

    private boolean isFooter(int position) {
        return position == list.size();
    }

    public class ShelfLoadViewHolder extends RecyclerView.ViewHolder {
        ShelfLoadViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class ShelfLoadingViewHolder extends RecyclerView.ViewHolder {
        ShelfLoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    interface OnBookClickListener{
        void onBookClick(BooksViewAdapter adapter, int position, BookData data);
        void onBookLongClick(BooksViewAdapter adapter, int position, BookData data);
    }

    private Uri getUri(BookData book){
        String isbn = book.getIsbn();
        String image = book.getImage();
        String filename = isbn + ".jpg";

        File file = new File(downloadDir.getPath() + "/" + filename);
        if(D) Log.d(TAG,"file: " + file.getPath());
        if(file.exists()){
            if(D) Log.d(TAG,"file exist: " + filename);
            return Uri.fromFile(file);
        }

        Uri uri = getImageUri(image);
        if(downloadFlg) {
            new AsyncDownload(this, uri, file).execute();
        }
        return uri;
    }



    private Uri getImageUri(String url){
        String REGEX_CSV_COMMA = ",";
        String REGEX_SURROUND_DOUBLE_QUOTATION = "^\"|\"$";
        String REGEX_SURROUND_BRACKET = "^\\(|\\)$";

        Pattern sdqPattern = Pattern.compile(REGEX_SURROUND_DOUBLE_QUOTATION);
        Matcher matcher = sdqPattern.matcher(url);
        url = matcher.replaceAll("");
        Pattern sbPattern = Pattern.compile(REGEX_SURROUND_BRACKET);
        matcher = sbPattern.matcher(url);
        url = matcher.replaceAll("");
        Pattern cPattern = Pattern.compile(REGEX_CSV_COMMA);
        String[] arr = cPattern.split(url, -1);
        return Uri.parse(arr[0]);
    }

    void addBooksData(List<BookData> books){
        int size = list.size();
        int add_size = books.size();
        list.addAll(books);
        notifyItemRangeInserted(size,add_size);
    }

    void clearBooksData(){
        list.clear();
        dispLoadNext = false;
        isLoading = false;
        notifyDataSetChanged();



    }




    private static class AsyncDownload extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<BooksViewAdapter> mReference;
        final Uri uri;
        final File file;

        private AsyncDownload(BooksViewAdapter adapter, Uri uri, File file){
            this.mReference = new WeakReference<>(adapter);
            this.uri = uri;
            this.file = file;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean isSuccess = false;
            HttpsURLConnection connection = null;
            try{
                if(D) Log.d(TAG,"uri.toString(): " + uri.toString());

                URL url = new URL(uri.toString());


//                URL url = new URL("");
                connection = (HttpsURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.setInstanceFollowRedirects(false);
                connection.connect();

                int status = connection.getResponseCode();
                if(status == HttpURLConnection.HTTP_OK){
                    final InputStream input = connection.getInputStream();
                    final DataInputStream dataInput = new DataInputStream(input);
                    // 書き込み用ストリーム
                    final FileOutputStream fileOutput = new FileOutputStream(file.getPath());
                    final DataOutputStream dataOut = new DataOutputStream(fileOutput);
                    // 読み込みデータ単位
                    final byte[] buffer = new byte[4096];
                    // 読み込んだデータを一時的に格納しておく変数
                    int readByte = 0;

                    // ファイルを読み込む
                    while((readByte = dataInput.read(buffer)) != -1) {
                        dataOut.write(buffer, 0, readByte);
                    }
                    // 各ストリームを閉じる
                    dataInput.close();
                    fileOutput.close();
                    dataInput.close();
                    input.close();
                    // 処理成功
                    isSuccess = true;
                }
            } catch (IOException e){
     //           if(D) Log.d(TAG,"Error");
                e.printStackTrace();
            } finally {
                if(connection != null){
                    connection.disconnect();
                }
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(D) Log.d(TAG,"download: " + result);

//            mReference.get().setLoadNext();
//            mFragmentReference.get().callback(result, json);
        }
    }




}
