package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShelfViewAdapter extends RecyclerView.Adapter<ShelfViewHolder>{

    private List<ShelfRowData> list;

    ShelfViewAdapter(List<ShelfRowData> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ShelfViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View inflate = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_shelf,viewGroup,false);
        return new ShelfViewHolder(inflate);
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
