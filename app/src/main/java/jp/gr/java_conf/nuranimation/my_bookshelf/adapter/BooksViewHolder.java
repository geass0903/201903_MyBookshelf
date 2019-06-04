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

    private TextView textView_Title;
    private TextView textView_Author;
    private TextView textView_Publisher;
    private TextView textView_SalesDate;
    private TextView textView_ItemPrice;
    private TextView textView_ReadStatus;
    private SimpleDraweeView draweeView_Image;
    private RatingBar ratingBar;
    private ImageView image_ReadStatus;

    BooksViewHolder(@NonNull View itemView) {
        super(itemView);
        draweeView_Image = itemView.findViewById(R.id.Item_Book_Image);
        textView_Title = itemView.findViewById(R.id.Item_Book_Title);
        textView_Author = itemView.findViewById(R.id.Item_Book_Author);
        textView_Publisher = itemView.findViewById(R.id.Item_Book_Publisher);
        textView_SalesDate = itemView.findViewById(R.id.Item_Book_SalesDate);
        textView_ItemPrice = itemView.findViewById(R.id.Item_Book_ItemPrice);
        ratingBar = itemView.findViewById(R.id.Item_Book_Rating);
        image_ReadStatus = itemView.findViewById(R.id.Item_Book_Icon_ReadStatus);
        textView_ReadStatus = itemView.findViewById(R.id.Item_Book_ReadStatus);
    }

    public SimpleDraweeView getImageView(){
        return draweeView_Image;
    }

    public TextView getTitleView(){
        return textView_Title;
    }

    public TextView getAuthorView(){
        return textView_Author;
    }

    public TextView getPublisherView(){
        return textView_Publisher;
    }

    public TextView getSalesDateView(){
        return textView_SalesDate;
    }

    public TextView getItemPriceView(){
        return textView_ItemPrice;
    }

    public RatingBar getRatingView(){
        return ratingBar;
    }

    public ImageView getReadStatusImageView(){
        return image_ReadStatus;
    }

    public TextView getReadStatusView(){
        return textView_ReadStatus;
    }
}
