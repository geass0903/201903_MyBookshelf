package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class Spinner_KeyValuePairArrayAdapter extends ArrayAdapter<Spinner_KeyValuePair> {

    public Spinner_KeyValuePairArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public Spinner_KeyValuePairArrayAdapter(Context context, int textViewResourceId, List<Spinner_KeyValuePair> list) {
        super(context, textViewResourceId, list);
        setDropDownViewResource(R.layout.item_spinner_drop_down);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        Spinner_KeyValuePair pair = getItem(position);
        if(pair != null){
            String value = pair.getValue();
            view.setText(value);
        }
//        view.setText(getItem(position).getValue());
        return view;

    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        CheckedTextView view = (CheckedTextView) super.getDropDownView(position, convertView, parent);
        Spinner_KeyValuePair pair = getItem(position);
        if(pair != null){
            String value = pair.getValue();
            view.setText(value);
        }
        return view;
    }

    public int getPosition(int key) {
        int position = -1;
        for (int i = 0; i < this.getCount(); i++) {
            Spinner_KeyValuePair pair = getItem(i);
            if(pair != null) {
                if (pair.getKey() == key) {
                    position = i;
                    break;
                }
            }
        }
        return position;
    }

    public int getPosition(String value) {
        int position = -1;
        for (int i = 0; i < this.getCount(); i++) {
            Spinner_KeyValuePair pair = getItem(i);
            if(pair != null) {
                if (pair.getValue().equals(value)) {
                    position = i;
                    break;
                }
            }
        }
        return position;
    }


}
