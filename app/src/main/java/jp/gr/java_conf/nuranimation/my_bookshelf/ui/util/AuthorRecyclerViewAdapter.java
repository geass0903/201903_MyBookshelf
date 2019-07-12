package jp.gr.java_conf.nuranimation.my_bookshelf.ui.util;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;

public class AuthorRecyclerViewAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private List<String> authors;
    private RecyclerView mRecyclerView;
    private OnAuthorClickListener mListener;



    public interface OnAuthorClickListener {
        void onClick(AuthorRecyclerViewAdapter adapter, int position, String author);
        void onLongClick(AuthorRecyclerViewAdapter adapter, int position, String author);
    }


    public AuthorRecyclerViewAdapter(List<String> authors){
        this.authors = authors;
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
        return authors.size();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_author, viewGroup, false);
        inflate.setOnClickListener(this);
        inflate.setOnLongClickListener(this);
        return new AuthorViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if(viewHolder instanceof AuthorViewHolder){
            AuthorViewHolder holder = (AuthorViewHolder) viewHolder;
            holder.item_author.setText(authors.get(position));
        }
    }

    public void setClickListener(AuthorRecyclerViewAdapter.OnAuthorClickListener listener){
        this.mListener = listener;
    }

    @Override
    public void onClick(View view) {
        if(mRecyclerView != null && mListener != null){
            int position = mRecyclerView.getChildAdapterPosition(view);
            String author = authors.get(position);
            mListener.onClick(this, position, author);
        }
    }

    @Override
    public boolean onLongClick(View view) {
        if(mRecyclerView != null && mListener != null){
            int position = mRecyclerView.getChildAdapterPosition(view);
            String author = authors.get(position);
            mListener.onLongClick(this, position, author);
            return true;
        }
        return false;
    }




    public void replaceAuthor(int position, String author){
        if(position >= 0 && position < authors.size()){
            authors.set(position, author);
            notifyItemChanged(position);
        }
    }

    public void deleteAuthor(int position){
        if(position >= 0 && position < authors.size()) {
            authors.remove(position);
            notifyItemRemoved(position);
        }
    }








    public static class AuthorViewHolder extends RecyclerView.ViewHolder{
        private TextView item_author;

        AuthorViewHolder(@NonNull View itemView) {
            super(itemView);
            item_author = itemView.findViewById(R.id.item_author);
        }
    }


}
