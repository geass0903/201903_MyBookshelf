package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

class ShelfBooksViewHolder extends RecyclerView.ViewHolder {

    TextView titleView;
    TextView authorView;
    TextView publisherView;
    SimpleDraweeView draweeView;

    ShelfBooksViewHolder(@NonNull View itemView) {
        super(itemView);

        draweeView = itemView.findViewById(R.id.shelf_row_book_image);
        titleView = itemView.findViewById(R.id.shelf_row_title);
        authorView = itemView.findViewById(R.id.shelf_row_author);
        publisherView = itemView.findViewById(R.id.shelf_row_publisher);
    }

}
