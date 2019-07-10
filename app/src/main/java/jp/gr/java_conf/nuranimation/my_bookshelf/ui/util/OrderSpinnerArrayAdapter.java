package jp.gr.java_conf.nuranimation.my_bookshelf.ui.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.SpinnerItem;

public class OrderSpinnerArrayAdapter extends ArrayAdapter<SpinnerItem> {

    @SuppressWarnings("unused")
    public OrderSpinnerArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        setDropDownViewResource(R.layout.item_order_spinner_drop_down);
    }

    public OrderSpinnerArrayAdapter(Context context, int textViewResourceId, List<SpinnerItem> list) {
        super(context, textViewResourceId, list);
        setDropDownViewResource(R.layout.item_order_spinner_drop_down);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        SpinnerItem item = getItem(position);
        if (item != null) {
            Drawable[] drawables = view.getCompoundDrawables();
            for(Drawable drawable : drawables){
                if(drawable != null){
                    drawable.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
                }
            }
            view.setCompoundDrawablesWithIntrinsicBounds(drawables[0],drawables[1],drawables[2],drawables[3]);
            view.setText(item.getLabel());
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        CheckedTextView view = (CheckedTextView) super.getDropDownView(position, convertView, parent);
        SpinnerItem item = getItem(position);
        if (item != null) {
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
