package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

class ShelfBooksViewHolder extends RecyclerView.ViewHolder {

    TextView textView_Title;
    TextView textView_Author;
    TextView textView_Publisher;
    TextView textView_SalesDate;
    SimpleDraweeView draweeView_Image;

    ShelfBooksViewHolder(@NonNull View itemView) {
        super(itemView);

        draweeView_Image = itemView.findViewById(R.id.item_Shelf_Book_Image);
        textView_Title = itemView.findViewById(R.id.item_Shelf_Book_Title);
        textView_Author = itemView.findViewById(R.id.item_Shelf_Book_Author);
        textView_Publisher = itemView.findViewById(R.id.item_Shelf_Book_Publisher);
        textView_SalesDate = itemView.findViewById(R.id.item_Shelf_Book_SalesDate);
    }

}
