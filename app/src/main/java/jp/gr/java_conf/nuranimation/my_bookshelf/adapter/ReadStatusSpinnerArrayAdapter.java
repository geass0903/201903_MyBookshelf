package jp.gr.java_conf.nuranimation.my_bookshelf.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseSpinnerItem;

public class ReadStatusSpinnerArrayAdapter extends ArrayAdapter<BaseSpinnerItem> {

    private LayoutInflater inflater;
    private int layoutId;
    private String[] labels;
    private int[] imageIDs;

    static class ViewHolder {
        ImageView imageView;
        TextView textView;
    }




    @SuppressWarnings("unused")
    public ReadStatusSpinnerArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        inflater = LayoutInflater.from(context);
        layoutId = textViewResourceId;
        setDropDownViewResource(R.layout.litem_spinner_read_status);
    }

    public ReadStatusSpinnerArrayAdapter(Context context, int textViewResourceId, List<BaseSpinnerItem> list) {
        super(context, textViewResourceId, list);
        inflater = LayoutInflater.from(context);
        layoutId = textViewResourceId;
        imageIDs = new int[list.size()];
        labels = new String[list.size()];
        Resources res = context.getResources();

        for(int i=0;i< list.size();i++){
            labels[i] = list.get(i).getLabel();
            String id = "ic_vector_read_status_" + list.get(i).getCode() + "_24dp";
            imageIDs[i] = res.getIdentifier(id,"drawable",context.getPackageName());
        }

        setDropDownViewResource(R.layout.litem_spinner_read_status);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = inflater.inflate(layoutId,null);
            holder = new ViewHolder();

            holder.imageView = convertView.findViewById(R.id.item_spinner_read_status_image);
            holder.textView = convertView.findViewById(R.id.item_spinner_read_status_text);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageResource(imageIDs[position]);
        holder.textView.setText(labels[position]);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = inflater.inflate(layoutId,null);
            holder = new ViewHolder();

            holder.imageView = convertView.findViewById(R.id.item_spinner_read_status_image);
            holder.textView = convertView.findViewById(R.id.item_spinner_read_status_text);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setImageResource(imageIDs[position]);
        holder.textView.setText(labels[position]);

        return convertView;
    }


    public int getPosition(String label){
        int position = -1;
        for (int i = 0; i < this.getCount(); i++) {
            BaseSpinnerItem item = getItem(i);
            if(item != null){
                if(item.getCode().equals(label)) {
                    position = i;
                    break;
                }
            }
        }
        return position;
    }


}
