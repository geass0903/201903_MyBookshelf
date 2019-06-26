package jp.gr.java_conf.nuranimation.my_bookshelf.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;

@SuppressWarnings("WeakerAccess")
public class BooksViewHolder extends RecyclerView.ViewHolder {

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

    public SimpleDraweeView getBookImageView(){
        return mBookImageView;
    }

    public TextView getTitleView(){
        return mTitleView;
    }

    public TextView getAuthorView(){
        return mAuthorView;
    }

    public TextView getPublisherView(){
        return mPublisherView;
    }

    public TextView getSalesDateView(){
        return mSalesDateView;
    }

    public TextView getItemPriceView(){
        return mPriceView;
    }

    public TextView getReadStatusTextView(){
        return mReadStatusTextView;
    }

    public ImageView getReadStatusImageView() {
        return mReadStatusImageView;
    }

    public RatingBar getRatingView(){
        return mRatingBar;
    }

}
