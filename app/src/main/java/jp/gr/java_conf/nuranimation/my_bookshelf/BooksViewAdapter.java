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
    private static final int ITEM_VIEW_TYPE_NEXT_LOAD    = 0;
    private static final int ITEM_VIEW_TYPE_LOADING      = 1;
    private static final int ITEM_VIEW_TYPE_ITEM         = 2;

    private List<BookData> list;

    private RecyclerView mRecyclerView;
    private OnBookClickListener mListener;

    private boolean dispLoadNext = false;
    private boolean isLoading = false;


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

    @Override
    public int getItemCount() {
        if(dispLoadNext){
            return list.size() + 1;
        }else{
            return list.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(isFooter(position)){ // exist Footer
            if(isLoading){
                return ITEM_VIEW_TYPE_LOADING;
            }else {
                return ITEM_VIEW_TYPE_NEXT_LOAD;
            }
        }
        return ITEM_VIEW_TYPE_ITEM;
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
            inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_loading,viewGroup,false);
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
        if(position != list.size()) {
            if (holder.getItemViewType() == ITEM_VIEW_TYPE_ITEM && holder instanceof BooksViewHolder) {
                BooksViewHolder viewHolder = (BooksViewHolder) holder;
                viewHolder.draweeView.setImageURI(getImageUri(list.get(position).getImage()));
                viewHolder.titleView.setText(list.get(position).getTitle());
                viewHolder.authorView.setText(list.get(position).getAuthor());
                viewHolder.publisherView.setText(list.get(position).getPublisher());
            }
        }
    }

    void setClickListener(OnBookClickListener listener){
        this.mListener = listener;
    }

    void setLoadNext(){
        dispLoadNext = true;
        notifyItemInserted(list.size());
    }

    void startLoadNext(){
        dispLoadNext = true;
        isLoading = true;
        notifyItemChanged(list.size());
    }

    void finishLoadNext(){
        dispLoadNext = false;
        isLoading = false;
        notifyItemRemoved(list.size());
    }

    @Override
    public void onClick(View view) {
        if(mRecyclerView != null && mListener != null){
            BookData data = null;
            int position = mRecyclerView.getChildAdapterPosition(view);
            if(!isFooter(position)){
                data = list.get(position);
            }
            mListener.onBookClick(this, position, data);
        }
    }

    @Override
    public boolean onLongClick(View view){
        if(mRecyclerView != null && mListener != null){
            BookData data = null;
            int position = mRecyclerView.getChildAdapterPosition(view);
            if(!isFooter(position)){
                data = list.get(position);
            }
            mListener.onBookLongClick(this, position, data);
            return true;
        }
        return false;
    }

    private boolean isFooter(int position) {
        return position == list.size();
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

    interface OnBookClickListener{
        void onBookClick(BooksViewAdapter adapter, int position, BookData data);
        void onBookLongClick(BooksViewAdapter adapter, int position, BookData data);
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

    void addBooksData(List<BookData> books){
        int size = list.size();
        int add_size = books.size();
        list.addAll(books);
        notifyItemRangeInserted(size,add_size);
    }

    void clearBooksData(){
        list.clear();
        dispLoadNext = false;
        isLoading = false;
        notifyDataSetChanged();

    }


}
