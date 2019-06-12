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

import javax.net.ssl.HttpsURLConnection;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfApplicationData;
import jp.gr.java_conf.nuranimation.my_bookshelf.application.MyBookshelfUtils;


public class BooksListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = BooksListViewAdapter.class.getSimpleName();
    private static final boolean D = false;

    public static final int LIST_TYPE_SHELF_BOOKS   = 1;
    public static final int LIST_TYPE_SEARCH_BOOKS  = 2;
    public static final int LIST_TYPE_NEW_BOOKS     = 3;

    public static final int VIEW_TYPE_BOOK         = 1;
    public static final int VIEW_TYPE_BUTTON_LOAD  = 2;
    public static final int VIEW_TYPE_LOADING      = 3;

    private List<BookData> list;

    private Context mContext;
    private MyBookshelfApplicationData mApplicationData;
    private RecyclerView mRecyclerView;
    private OnBookClickListener mListener;

    private boolean doDownload;
    private File downloadDir;
    private int list_type;


    public BooksListViewAdapter(Context context, List<BookData> list, int list_type, boolean download) {
        this.mContext = context;
        this.list = list;
        this.list_type = list_type;
        this.doDownload = download;
        mApplicationData = (MyBookshelfApplicationData) mContext.getApplicationContext();
        File dir = Environment.getExternalStorageDirectory();
        String dirPath = dir.getPath() + "/Android/data/jp.gr.java_conf.nuranimation.my_bookshelf/BookImage";
        downloadDir = new File(dirPath);
        if(!downloadDir.exists()){
            boolean isSuccess = downloadDir.mkdirs();
            if(D) Log.d(TAG,"mkdirs(): " + isSuccess);
            if(!isSuccess){
                doDownload = false;
            }
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
            return new LoadViewHolder(inflate);
        }
        if(viewType == VIEW_TYPE_LOADING){
            inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_loading,viewGroup,false);
            inflate.setOnClickListener(this);
            inflate.setOnLongClickListener(this);
            return new LoadingViewHolder(inflate);
        }
        inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_book,viewGroup,false);
        inflate.setOnClickListener(this);
        inflate.setOnLongClickListener(this);
        return new BooksViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_BOOK && holder instanceof BooksViewHolder) {
            BooksViewHolder viewHolder = (BooksViewHolder) holder;

            BookData book = mApplicationData.loadBookDataFromShelfBooks(list.get(position));

            switch(list_type) {
                case LIST_TYPE_SHELF_BOOKS:
                    if (book != null) {
                        viewHolder.getImageView().setImageURI(getUri(book));
                        viewHolder.getTitleView().setText(book.getTitle());
                        viewHolder.getAuthorView().setText(book.getAuthor());
                        viewHolder.getPublisherView().setText(book.getPublisher());
                        viewHolder.getSalesDateView().setText(book.getSalesDate());
                        viewHolder.getItemPriceView().setText(book.getItemPrice());
                        viewHolder.getRatingView().setRating(Float.parseFloat(book.getRating()));
                        viewHolder.getReadStatusView().setText(getReadStatusText(book.getReadStatus()));
                        viewHolder.getReadStatusImageView().setImageDrawable(getReadStatusIcon(book.getReadStatus()));
                    } else {
                        viewHolder.getImageView().setImageURI(getUri(list.get(position)));
                        viewHolder.getTitleView().setText(list.get(position).getTitle());
                        viewHolder.getAuthorView().setText(list.get(position).getAuthor());
                        viewHolder.getPublisherView().setText(list.get(position).getPublisher());
                        viewHolder.getSalesDateView().setText(list.get(position).getSalesDate());
                        viewHolder.getItemPriceView().setText(list.get(position).getItemPrice());
                        viewHolder.getRatingView().setRating(Float.parseFloat(list.get(position).getRating()));
                        viewHolder.getReadStatusView().setText(getReadStatusText(list.get(position).getReadStatus()));
                        viewHolder.getReadStatusImageView().setImageDrawable(getReadStatusIcon(list.get(position).getReadStatus()));
                    }
                    break;
                case LIST_TYPE_SEARCH_BOOKS:
                case LIST_TYPE_NEW_BOOKS:
                    viewHolder.getImageView().setImageURI(getUri(list.get(position)));
                    viewHolder.getTitleView().setText(list.get(position).getTitle());
                    viewHolder.getAuthorView().setText(list.get(position).getAuthor());
                    viewHolder.getPublisherView().setText(list.get(position).getPublisher());
                    viewHolder.getSalesDateView().setText(list.get(position).getSalesDate());
                    viewHolder.getItemPriceView().setText(list.get(position).getItemPrice());
                    if (book != null) {
                        viewHolder.getRatingView().setRating(Float.parseFloat(book.getRating()));
                        viewHolder.getReadStatusView().setText(getReadStatusText(book.getReadStatus()));
                        viewHolder.getReadStatusImageView().setImageDrawable(getReadStatusIcon(book.getReadStatus()));
                    } else {
                        viewHolder.getRatingView().setRating(Float.parseFloat(list.get(position).getRating()));
                        viewHolder.getReadStatusView().setText(getReadStatusText(list.get(position).getReadStatus()));
                        viewHolder.getReadStatusImageView().setImageDrawable(getReadStatusIcon(list.get(position).getReadStatus()));
                    }
                    break;
            }
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

    public class LoadViewHolder extends RecyclerView.ViewHolder {
        LoadViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface OnBookClickListener{
        void onBookClick(BooksListViewAdapter adapter, int position, BookData data);
        void onBookLongClick(BooksListViewAdapter adapter, int position, BookData data);
    }

    public void replaceBooksData(List<BookData> books){
        list.clear();
        list.addAll(books);
        notifyDataSetChanged();
    }

    public void addBooksData(List<BookData> books){
        int start = list.size();
        int count = books.size();
        list.addAll(books);
        notifyItemRangeInserted(start,count);
    }

    public void updateBook(int position){
        if(position >= 0 && position < list.size()){
            notifyItemChanged(position);
        }
    }

    public void deleteBook(int position){
        if(position >= 0 && position < list.size()) {
            list.remove(position);
            notifyItemRemoved(position);
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

        Uri uri = MyBookshelfUtils.getImageUri(image);
        if(doDownload) {
            new AsyncDownload(uri, file).execute();
        }
        return uri;
    }


    private String getReadStatusText(String status) {
        String readStatus = mContext.getString(R.string.ReadStatus_Label_STATUS_0);
        if(status != null) {
            switch (status) {
                case BookData.STATUS_UNREGISTERED:
                    readStatus = mContext.getString(R.string.ReadStatus_Label_STATUS_0);
                    break;
                case BookData.STATUS_INTERESTED:
                    readStatus = mContext.getString(R.string.ReadStatus_Label_STATUS_1);
                    break;
                case BookData.STATUS_UNREAD:
                    readStatus = mContext.getString(R.string.ReadStatus_Label_STATUS_2);
                    break;
                case BookData.STATUS_READING:
                    readStatus = mContext.getString(R.string.ReadStatus_Label_STATUS_2);
                    break;
                case BookData.STATUS_ALREADY_READ:
                    readStatus = mContext.getString(R.string.ReadStatus_Label_STATUS_4);
                    break;
                case BookData.STATUS_NONE:
                    readStatus = mContext.getString(R.string.ReadStatus_Label_STATUS_5);
                    break;
                default:
                    readStatus = mContext.getString(R.string.ReadStatus_Label_STATUS_0);
                    break;
            }
        }
        return readStatus;
    }

    private Drawable getReadStatusIcon(String status){
        Drawable icon = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_vector_read_status_0_24dp, null);
        if(status != null) {
            switch (status) {
                case BookData.STATUS_UNREGISTERED:
                    icon = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_vector_read_status_0_24dp, null);
                    break;
                case BookData.STATUS_INTERESTED:
                    icon = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_vector_read_status_1_24dp, null);
                    break;
                case BookData.STATUS_UNREAD:
                    icon = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_vector_read_status_2_24dp, null);
                    break;
                case BookData.STATUS_READING:
                    icon = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_vector_read_status_3_24dp, null);
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
                URL url = new URL(uri.toString());
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
