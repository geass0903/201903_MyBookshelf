package jp.gr.java_conf.nuranimation.my_bookshelf.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.base.BaseSpinnerItem;

public class SettingsSpinnerArrayAdapter extends ArrayAdapter<BaseSpinnerItem> {

    @SuppressWarnings("unused")
    public SettingsSpinnerArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        setDropDownViewResource(R.layout.item_spinner_drop_down);
    }

    public SettingsSpinnerArrayAdapter(Context context, int textViewResourceId, List<BaseSpinnerItem> list) {
        super(context, textViewResourceId, list);
        setDropDownViewResource(R.layout.item_spinner_drop_down);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        TextView view = (TextView) super.getView(position, convertView, parent);
        BaseSpinnerItem item = getItem(position);
        if (item != null) {
            view.setText(item.getLabel());
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        CheckedTextView view = (CheckedTextView) super.getDropDownView(position, convertView, parent);
        BaseSpinnerItem item = getItem(position);
        if (item != null) {
            view.setText(item.getLabel());
        }
        return view;
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
