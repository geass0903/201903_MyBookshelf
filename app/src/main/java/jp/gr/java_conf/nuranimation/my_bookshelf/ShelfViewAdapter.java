package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShelfViewAdapter extends RecyclerView.Adapter<ShelfViewHolder>{
    public static final int ITEM_VIEW_TYPE_ITEM         = 0;
    public static final int ITEM_VIEW_TYPE_BUTTON_LOAD  = 1;
    public static final int ITEM_VIEW_TYPE_LOADING      = 2;


    private List<ShelfRowData> list;

    private RecyclerView mRecyclerView;
    private Listener mListener;


    public interface Listener {
        void onItemClick(int position);
        boolean onItemLongClick(int position);
    }

    public void setClickListener(Listener listener){
        this.mListener = listener;
    }


    @Override
    public int getItemViewType(int position) {

        return ITEM_VIEW_TYPE_ITEM;

    }


    ShelfViewAdapter(List<ShelfRowData> list) {
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
    public ShelfViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch(viewType) {
            case ITEM_VIEW_TYPE_ITEM:
                break;
        }
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_shelf,viewGroup,false);
        return new ShelfViewHolder(inflate,this.mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ShelfViewHolder shelfViewHolder, int position) {

        shelfViewHolder.draweeView.setImageURI(getImageUri(list.get(position).getImage()));
        shelfViewHolder.titleView.setText(list.get(position).getTitle());
        shelfViewHolder.authorView.setText(list.get(position).getAuthor());
        shelfViewHolder.publisherView.setText(list.get(position).getPublisher());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    ShelfRowData get(int position){
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



}
