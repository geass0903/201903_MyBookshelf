package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

class ShelfViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
    private ShelfViewAdapter.Listener listener;

    TextView titleView;
    TextView authorView;
    TextView publisherView;
    SimpleDraweeView draweeView;

    ShelfViewHolder(@NonNull View itemView, ShelfViewAdapter.Listener listener) {
        super(itemView);
        this.listener = listener;
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);


        draweeView = itemView.findViewById(R.id.shelf_row_book_image);
        titleView = itemView.findViewById(R.id.shelf_row_title);
        authorView = itemView.findViewById(R.id.shelf_row_author);
        publisherView = itemView.findViewById(R.id.shelf_row_publisher);
    }


    @Override
    public void onClick(View view){
        if(this.listener != null) {
            this.listener.onItemClick(getAdapterPosition());
        }
    }

    @Override
    public boolean onLongClick(View view){
        if(this.listener != null) {
            return this.listener.onItemLongClick(getAdapterPosition());
        }
        return false;
    }

}
