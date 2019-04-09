package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BooksViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    public static final int ITEM_VIEW_TYPE_NEXT_LOAD    = 0;
    public static final int ITEM_VIEW_TYPE_LOADING      = 1;
    public static final int ITEM_VIEW_TYPE_ITEM         = 2;


    private List<BookData> list;

    private RecyclerView mRecyclerView;
    private OnBookClickListener mListener;





    void setClickListener(OnBookClickListener listener){
        this.mListener = listener;
    }


    @Override
    public int getItemViewType(int position) {

        if(position == list.size() + 1){
            return ITEM_VIEW_TYPE_NEXT_LOAD;
        }

        return ITEM_VIEW_TYPE_ITEM;

    }


    BooksViewAdapter(List<BookData> list) {
        this.list = list;
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


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType){
        View inflate;
        if(viewType == ITEM_VIEW_TYPE_NEXT_LOAD){
            inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_button,viewGroup,false);
            inflate.setOnClickListener(this);
            inflate.setOnLongClickListener(this);
            return new ShelfLoadViewHolder(inflate);
        }
        if(viewType == ITEM_VIEW_TYPE_LOADING){
            inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_shelf,viewGroup,false);
            inflate.setOnClickListener(this);
            inflate.setOnLongClickListener(this);
            return new ShelfLoadingViewHolder(inflate);
        }
        inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_shelf,viewGroup,false);
        inflate.setOnClickListener(this);
        inflate.setOnLongClickListener(this);
        return new BooksViewHolder(inflate);

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder.getItemViewType() == ITEM_VIEW_TYPE_ITEM && holder instanceof BooksViewHolder){
            BooksViewHolder viewHolder = (BooksViewHolder)holder;
            viewHolder.draweeView.setImageURI(getImageUri(list.get(position).getImage()));
            viewHolder.titleView.setText(list.get(position).getTitle());
            viewHolder.authorView.setText(list.get(position).getAuthor());
            viewHolder.publisherView.setText(list.get(position).getPublisher());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    BookData get(int position){
        return this.list.get(position);
    }

    private Uri getImageUri(String url){
        String REGEX_CSV_COMMA = ",";
        String REGEX_SURROUND_DOUBLE_QUOTATION = "^\"|\"$";
        String REGEX_SURROUND_BRACKET = "^\\(|\\)$";

        Pattern sdqPattern = Pattern.compile(REGEX_SURROUND_DOUBLE_QUOTATION);
        Matcher matcher = sdqPattern.matcher(url);
        url = matcher.replaceAll("");
        Pattern sbPattern = Pattern.compile(REGEX_SURROUND_BRACKET);
        matcher = sbPattern.matcher(url);
        url = matcher.replaceAll("");
        Pattern cPattern = Pattern.compile(REGEX_CSV_COMMA);
        String[] arr = cPattern.split(url, -1);
        return Uri.parse(arr[0]);
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

    interface OnBookClickListener{
        void onBookClick(BooksViewAdapter adapter, int position, BookData data);
        void onBookLongClick(BooksViewAdapter adapter, int position, BookData data);
    }


    public class ShelfLoadViewHolder extends RecyclerView.ViewHolder {
        ShelfLoadViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class ShelfLoadingViewHolder extends RecyclerView.ViewHolder {
        ShelfLoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }


}
