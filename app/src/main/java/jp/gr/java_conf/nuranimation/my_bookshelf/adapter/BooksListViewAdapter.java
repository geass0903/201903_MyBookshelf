package jp.gr.java_conf.nuranimation.my_bookshelf.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.BookData;

public class BooksListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = BooksListViewAdapter.class.getSimpleName();
    private static final boolean D = true;

    public static final int VIEW_TYPE_BOOK         = 1;
    public static final int VIEW_TYPE_BUTTON_LOAD  = 2;
    public static final int VIEW_TYPE_LOADING      = 3;


    private List<BookData> list;

    private Context mContext;
    private RecyclerView mRecyclerView;
    private OnBookClickListener mListener;

    private boolean downloadFlg;
    private File downloadDir;


    public BooksListViewAdapter(Context context, List<BookData> list, boolean download) {
        this.mContext = context;
        this.list = list;
        this.downloadFlg = download;
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

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).getView_type();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
        View inflate;
        if(viewType == VIEW_TYPE_BUTTON_LOAD){
            inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_load_next,viewGroup,false);
            inflate.setOnClickListener(this);
            inflate.setOnLongClickListener(this);
            return new ShelfLoadViewHolder(inflate);
        }
        if(viewType == VIEW_TYPE_LOADING){
            inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_loading,viewGroup,false);
            inflate.setOnClickListener(this);
            inflate.setOnLongClickListener(this);
            return new ShelfLoadingViewHolder(inflate);
        }
        inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_book,viewGroup,false);
        inflate.setOnClickListener(this);
        inflate.setOnLongClickListener(this);
        return new BooksListViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder.getItemViewType() == VIEW_TYPE_BOOK && holder instanceof BooksListViewHolder){
            BooksListViewHolder viewHolder = (BooksListViewHolder) holder;
            viewHolder.getImageView().setImageURI(getUri(list.get(position)));
            viewHolder.getTitleView().setText(list.get(position).getTitle());
            viewHolder.getAuthorView().setText(list.get(position).getAuthor());
            viewHolder.getPublisherView().setText(list.get(position).getPublisher());
            viewHolder.getSalesDateView().setText(list.get(position).getSalesDate());
            viewHolder.getItemPriceView().setText(list.get(position).getItemPrice());
            viewHolder.getRatingView().setRating(getRating(list.get(position).getRating()));
            viewHolder.getReadStatusView().setText(getReadStatus(list.get(position).getReadStatus()));
            viewHolder.getReadStatusImageView().setImageDrawable(getReadStatusIcon(list.get(position).getReadStatus()));
        }
    }

    public void setClickListener(OnBookClickListener listener){
        this.mListener = listener;
    }


    @Override
    public void onClick(View view) {
        if(mRecyclerView != null && mListener != null){
            int position = mRecyclerView.getChildAdapterPosition(view);
            BookData data = list.get(position);
            mListener.onBookClick(this, position, data);
        }
    }

    @Override
    public boolean onLongClick(View view){
        if(mRecyclerView != null && mListener != null){
            int position = mRecyclerView.getChildAdapterPosition(view);
            BookData data = list.get(position);
            mListener.onBookLongClick(this, position, data);
            return true;
        }
        return false;
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

    public interface OnBookClickListener{
        void onBookClick(BooksListViewAdapter adapter, int position, BookData data);
        void onBookLongClick(BooksListViewAdapter adapter, int position, BookData data);
    }

    private Uri getUri(BookData book){
        String isbn = book.getISBN();
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
            new AsyncDownload(uri, file).execute();
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

    private float getRating(String value){
        float rating = 0.0f;
        try {
            rating = Float.parseFloat(value);
        } catch (Exception e){
            e.printStackTrace();
        }
        return rating;
    }

    private String getReadStatus(String status){
        String readStatus = mContext.getString(R.string.Item_ReadStatus_0);
        if(status != null) {
            switch (status) {
                case "1":
                    readStatus = mContext.getString(R.string.Item_ReadStatus_1);
                    break;
                case "2":
                    readStatus = mContext.getString(R.string.Item_ReadStatus_2);
                    break;
                case "3":
                    readStatus = mContext.getString(R.string.Item_ReadStatus_2);
                    break;
                case "4":
                    readStatus = mContext.getString(R.string.Item_ReadStatus_4);
                    break;
                case "5":
                    readStatus = mContext.getString(R.string.Item_ReadStatus_5);
                    break;
                default:
                    readStatus = mContext.getString(R.string.Item_ReadStatus_0);
                    break;
            }
        }
        return readStatus;
    }

    private Drawable getReadStatusIcon(String status){
        Drawable icon = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_vector_read_status_0_24dp, null);
        if(status != null) {
            switch (status) {
                case BookData.STATUS_INTERESTED:
                    icon = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_vector_read_status_1_24dp, null);
                    break;
                case BookData.STATUS_UNREAD:
                    icon = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_vector_read_status_2_24dp, null);
                    break;
                case BookData.STATUS_READING:
                    icon = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_vector_read_status_2_24dp, null);
                    break;
                case BookData.STATUS_ALREADY_READ:
                    icon = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_vector_read_status_4_24dp, null);
                    break;
                case BookData.STATUS_NONE:
                    icon = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_vector_read_status_5_24dp, null);
                    break;
                default:
                    icon = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_vector_read_status_0_24dp, null);
                    break;
            }
        }
        return icon;
    }


    public void setBooksData(List<BookData> books){
        list.clear();
        list.addAll(books);
        notifyDataSetChanged();
    }

    public void addBooksData(List<BookData> books){
        int size = list.size();
        int add_size = books.size();
        list.addAll(books);
        notifyItemRangeInserted(size,add_size);
    }

    public void deleteBook(int position){
        if(position >= 0 && position < list.size()) {
            list.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void registerBook(int position){
        if(position >= 0 && position < list.size()) {
            BookData book = list.get(position);
            book.setReadStatus("5");
            list.set(position,book);
            notifyItemChanged(position);
        }
    }

    public void unregisterBook(int position){
        if(position >= 0 && position < list.size()) {
            BookData book = list.get(position);
            book.setReadStatus("0");
            list.set(position,book);
            notifyItemChanged(position);
        }
    }


    public void clearBooksData(){
        list.clear();
        notifyDataSetChanged();
    }


    public void setFooter(BookData footer){
        if(list.size() > 0) {
            int lastPosition = list.size() - 1;
            // delete old footer
            if (list.get(lastPosition).getView_type() != VIEW_TYPE_BOOK) {
                if (D) Log.d(TAG, "remove footer type: " + list.get(lastPosition).getView_type());
                list.remove(lastPosition);
                notifyItemRemoved(lastPosition);
            }
            // add new footer
            if (footer != null) {
                if (D) Log.d(TAG, "add footer type: " + footer.getView_type());
                list.add(footer);
                notifyItemInserted(lastPosition);
            }
        }
    }



    private static class AsyncDownload extends AsyncTask<Void, Void, Boolean> {
        final Uri uri;
        final File file;

        private AsyncDownload(Uri uri, File file){
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
                    int readByte;

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

        }
    }




}
