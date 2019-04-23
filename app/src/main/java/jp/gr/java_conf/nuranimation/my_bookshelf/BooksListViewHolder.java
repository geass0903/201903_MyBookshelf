package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

class BooksListViewHolder extends RecyclerView.ViewHolder {

    TextView textView_Title;
    TextView textView_Author;
    TextView textView_Publisher;
    TextView textView_SalesDate;
    TextView textView_ItemPrice;
    TextView textView_ReadStatus;
    SimpleDraweeView draweeView_Image;
    RatingBar ratingBar;
    ImageView image_ReadStatus;

    BooksListViewHolder(@NonNull View itemView) {
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

}
