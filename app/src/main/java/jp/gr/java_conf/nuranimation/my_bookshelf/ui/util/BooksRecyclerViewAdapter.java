package jp.gr.java_conf.nuranimation.my_bookshelf.ui.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.database.MyBookshelfDBOpenHelper;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.BookDataUtils;


public class BooksRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    public static final int LIST_TYPE_SHELF_BOOKS   = 1;
    public static final int LIST_TYPE_SEARCH_BOOKS  = 2;
    public static final int LIST_TYPE_NEW_BOOKS     = 3;

    private List<BookData> list;

    private Context mContext;
    private MyBookshelfDBOpenHelper mDBOpenHelper;
    private RecyclerView mRecyclerView;
    private OnBookClickListener mListener;

    private int list_type;


    public interface OnBookClickListener{
        void onBookClick(BooksRecyclerViewAdapter adapter, int position, BookData data);
        void onBookLongClick(BooksRecyclerViewAdapter adapter, int position, BookData data);
    }


    public BooksRecyclerViewAdapter(Context context, List<BookData> list, int list_type) {
        this.mContext = context;
        this.list = list;
        this.list_type = list_type;
        mDBOpenHelper = MyBookshelfDBOpenHelper.getInstance(context);
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
                inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_book, viewGroup, false);
                inflate.setOnClickListener(this);
                inflate.setOnLongClickListener(this);
                return new BooksViewHolder(inflate);
            case BookData.TYPE_BUTTON_LOAD:
                inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_load_next,viewGroup,false);
                inflate.setOnClickListener(this);
                inflate.setOnLongClickListener(this);
                return new LoadViewHolder(inflate);
            case BookData.TYPE_VIEW_LOADING:
                inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_loading,viewGroup,false);
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
                list.remove(lastPosition);
                notifyItemRemoved(lastPosition);
            }
            // add new footer
            if (footer != null) {
                list.add(footer);
                notifyItemInserted(lastPosition);
            }
        }
    }

    private void bindViewHolder(BooksViewHolder holder, BookData book){
        Uri uri = Uri.parse(BookDataUtils.parseUrlString(book.getImage(), BookDataUtils.IMAGE_TYPE_SMALL));
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setCacheChoice(ImageRequest.CacheChoice.SMALL)
                .build();
        holder.mBookImageView.setController(
                Fresco.newDraweeControllerBuilder()
                        .setOldController(holder.mBookImageView.getController())
                        .setImageRequest(request)
                        .build());
        holder.mTitleView.setText(book.getTitle());
        holder.mAuthorView.setText(book.getAuthor());
        holder.mPublisherView.setText(book.getPublisher());
        holder.mSalesDateView.setText(book.getSalesDate());
        holder.mPriceView.setText(book.getItemPrice());
        holder.mRatingBar.setRating(BookDataUtils.convertRating(book.getRating()));

        Drawable read_status_image = getReadStatusImage(mContext, book.getReadStatus());
        holder.mReadStatusImageView.setImageDrawable(read_status_image);
        String read_status_text = getReadStatusText(mContext, book.getReadStatus());
        holder.mReadStatusTextView.setText(read_status_text);
    }


    private static Drawable getReadStatusImage(Context context, String status) {
        Resources res = context.getResources();
        Drawable read_status_image;
        if (status == null) {
            read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
            if (read_status_image != null) {
                read_status_image.mutate().setColorFilter(Color.parseColor("#00000000"), PorterDuff.Mode.CLEAR);
            }
        } else switch (status) {
            case BookData.STATUS_INTERESTED:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_favorites, null);
                if (read_status_image != null) {
                    read_status_image.mutate().setColorFilter(Color.parseColor("#FFDD0000"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case BookData.STATUS_UNREAD:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.mutate().setColorFilter(Color.parseColor("#FFDDDD00"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case BookData.STATUS_READING:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.mutate().setColorFilter(Color.parseColor("#FF00DD00"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case BookData.STATUS_ALREADY_READ:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.mutate().setColorFilter(Color.parseColor("#FF0000DD"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case BookData.STATUS_NONE:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.mutate().setColorFilter(Color.parseColor("#FF808080"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            default:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.mutate().setColorFilter(Color.parseColor("#00000000"), PorterDuff.Mode.SRC_ATOP);
                }
        }
        return read_status_image;
    }

   private static String getReadStatusText(Context context, String status) {
        String read_status_text;
        if (status == null) {
            read_status_text = context.getString(R.string.read_status_label_0);
        } else switch (status) {
            case BookData.STATUS_INTERESTED:
                read_status_text = context.getString(R.string.read_status_label_1);
                break;
            case BookData.STATUS_UNREAD:
                read_status_text = context.getString(R.string.read_status_label_2);
                break;
            case BookData.STATUS_READING:
                read_status_text = context.getString(R.string.read_status_label_3);
                break;
            case BookData.STATUS_ALREADY_READ:
                read_status_text = context.getString(R.string.read_status_label_4);
                break;
            case BookData.STATUS_NONE:
                read_status_text = context.getString(R.string.read_status_label_5);
                break;
            default:
                read_status_text = context.getString(R.string.read_status_label_0);
        }
        return read_status_text;
    }



    public static class LoadViewHolder extends RecyclerView.ViewHolder {
        LoadViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class BooksViewHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView mBookImageView;
        private TextView mTitleView;
        private TextView mAuthorView;
        private TextView mPublisherView;
        private TextView mSalesDateView;
        private TextView mPriceView;
        private TextView mReadStatusTextView;
        private ImageView mReadStatusImageView;
        private RatingBar mRatingBar;

        BooksViewHolder(@NonNull View itemView) {
            super(itemView);
            mBookImageView = itemView.findViewById(R.id.item_book_image);
            mTitleView = itemView.findViewById(R.id.item_book_title);
            mAuthorView = itemView.findViewById(R.id.item_book_author);
            mPublisherView = itemView.findViewById(R.id.item_book_publisher);
            mSalesDateView = itemView.findViewById(R.id.item_book_sales_date);
            mPriceView = itemView.findViewById(R.id.item_book_price);
            mReadStatusTextView = itemView.findViewById(R.id.item_book_read_status_text);
            mReadStatusImageView = itemView.findViewById(R.id.item_book_read_status_image);
            mRatingBar = itemView.findViewById(R.id.item_book_rating);
        }
    }

}
