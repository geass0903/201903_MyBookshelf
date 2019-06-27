package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.List;

public class ReadStatusSpinnerArrayAdapter extends ArrayAdapter<SpinnerItem> {
    private Drawable[] drawables;

    @SuppressWarnings("unused")
    public ReadStatusSpinnerArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        setDropDownViewResource(R.layout.item_read_status_spinner_drop_down);
    }

    public ReadStatusSpinnerArrayAdapter(Context context, int textViewResourceId, List<SpinnerItem> list) {
        super(context, textViewResourceId, list);
        drawables = new Drawable[list.size()];
        for(int i=0;i< list.size();i++){
            drawables[i] = MyBookshelfUtils.getReadStatusImage(context, list.get(i).getCode());
        }
        setDropDownViewResource(R.layout.item_read_status_spinner_drop_down);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        SpinnerItem item = getItem(position);
        if (item != null) {
            view.setCompoundDrawablesWithIntrinsicBounds(drawables[position],null,null,null);
            view.setText(item.getLabel());
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        CheckedTextView view = (CheckedTextView) super.getDropDownView(position, convertView, parent);
        SpinnerItem item = getItem(position);
        if (item != null) {
            view.setCompoundDrawablesWithIntrinsicBounds(drawables[position],null,null,null);
            view.setText(item.getLabel());
        }
        return view;
    }


    public int getPosition(String code){
        int position = -1;
        for (int i = 0; i < this.getCount(); i++) {
            SpinnerItem item = getItem(i);
            if(item != null){
                if(item.getCode().equals(code)) {
                    position = i;
                    break;
                }
            }
        }
        return position;
    }

}
