package jp.gr.java_conf.nuranimation.my_bookshelf.ui.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.database.MyBookshelfDBOpenHelper;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookData;


public class BooksListViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private static final String TAG = BooksListViewAdapter.class.getSimpleName();
    private static final boolean D = true;

    public static final int LIST_TYPE_SHELF_BOOKS   = 1;
    public static final int LIST_TYPE_SEARCH_BOOKS  = 2;
    public static final int LIST_TYPE_NEW_BOOKS     = 3;


    private List<BookData> list;

    private Context mContext;
    private MyBookshelfDBOpenHelper mDBOpenHelper;
    private RecyclerView mRecyclerView;
    private OnBookClickListener mListener;

    private int list_type;

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


    public BooksListViewAdapter(Context context, List<BookData> list, int list_type) {
        this.mContext = context;
        this.list = list;
        this.list_type = list_type;
        mDBOpenHelper = new MyBookshelfDBOpenHelper(context.getApplicationContext());
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
        switch (viewType){
            case BookData.TYPE_BOOK:
                inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_books_book, viewGroup, false);
                inflate.setOnClickListener(this);
                inflate.setOnLongClickListener(this);
                return new BooksViewHolder(inflate);
            case BookData.TYPE_BUTTON_LOAD:
                inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_books_load_next,viewGroup,false);
                inflate.setOnClickListener(this);
                inflate.setOnLongClickListener(this);
                return new LoadViewHolder(inflate);
            case BookData.TYPE_VIEW_LOADING:
                inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_books_loading,viewGroup,false);
                inflate.setOnClickListener(this);
                inflate.setOnLongClickListener(this);
                return new LoadingViewHolder(inflate);
        }
        // Error
        inflate = LayoutInflater.from(viewGroup.getContext()).inflate(null,viewGroup,false);
        return new BooksViewHolder(inflate);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == BookData.TYPE_BOOK && holder instanceof BooksViewHolder) {
            BooksViewHolder viewHolder = (BooksViewHolder) holder;
            BookData registered_book = mDBOpenHelper.loadBookDataFromShelfBooks(list.get(position));
            switch(list_type) {
                case LIST_TYPE_SHELF_BOOKS:
                    if (registered_book.getView_type() == BookData.TYPE_BOOK) {
                        bindViewHolder(viewHolder, registered_book);
                    } else {
                        bindViewHolder(viewHolder, list.get(position));
                    }
                    break;
                case LIST_TYPE_SEARCH_BOOKS:
                case LIST_TYPE_NEW_BOOKS:
                    BookData book = new BookData(list.get(position));
                    if (registered_book.getView_type() == BookData.TYPE_BOOK) {
                        book.setRating(registered_book.getRating());
                        book.setReadStatus(registered_book.getReadStatus());
                    }
                    bindViewHolder(viewHolder, book);
                    break;
            }
        }
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

    public void setClickListener(OnBookClickListener listener){
        this.mListener = listener;
    }

    public void replaceBooksData(List<BookData> books){
        list.clear();
        if(books != null) {
            list.addAll(books);
        }
        notifyDataSetChanged();
    }

    public void addBooksData(List<BookData> books){
        int start = list.size();
        if(books != null) {
            int count = books.size();
            list.addAll(books);
            notifyItemRangeInserted(start, count);
        }
    }

    public void refreshBook(int position){
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
            if (list.get(lastPosition).getView_type() != BookData.TYPE_BOOK) {
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

    private void bindViewHolder(BooksViewHolder holder, BookData book){
        Uri uri = Uri.parse(MyBookshelfUtils.parseUrlString(book.getImage(), MyBookshelfUtils.IMAGE_TYPE_SMALL));
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setCacheChoice(ImageRequest.CacheChoice.SMALL)
                .build();
        holder.getBookImageView().setController(
                Fresco.newDraweeControllerBuilder()
                        .setOldController(holder.getBookImageView().getController())
                        .setImageRequest(request)
                        .build());
        holder.getTitleView().setText(book.getTitle());
        holder.getAuthorView().setText(book.getAuthor());
        holder.getPublisherView().setText(book.getPublisher());
        holder.getSalesDateView().setText(book.getSalesDate());
        holder.getItemPriceView().setText(book.getItemPrice());
        holder.getRatingView().setRating(book.getFloatRating());

        Drawable read_status_image = MyBookshelfUtils.getReadStatusImage(mContext, book.getReadStatus());
        holder.getReadStatusImageView().setImageDrawable(read_status_image);
        String read_status_text = MyBookshelfUtils.getReadStatusText(mContext, book.getReadStatus());
        holder.getReadStatusTextView().setText(read_status_text);
    }



}
