package jp.gr.java_conf.nuranimation.my_bookshelf.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.SpinnerItem;

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
            drawables[i] = getReadStatusImage(context, list.get(i).getCode());
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



    public static Drawable getReadStatusImage(Context context, String status) {
        Resources res = context.getResources();
        Drawable read_status_image;
        if (status == null) {
            read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
            if (read_status_image != null) {
                read_status_image.setColorFilter(Color.parseColor("#00000000"), PorterDuff.Mode.CLEAR);
            }
        } else switch (status) {
            case BookData.STATUS_INTERESTED:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_favorites, null);
                if (read_status_image != null) {
                    read_status_image.setColorFilter(Color.parseColor("#FFDD0000"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case BookData.STATUS_UNREAD:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.setColorFilter(Color.parseColor("#FFDDDD00"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case BookData.STATUS_READING:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.setColorFilter(Color.parseColor("#FF00DD00"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case BookData.STATUS_ALREADY_READ:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.setColorFilter(Color.parseColor("#FF0000DD"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case BookData.STATUS_NONE:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.setColorFilter(Color.parseColor("#FF808080"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            default:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.setColorFilter(Color.parseColor("#00000000"), PorterDuff.Mode.SRC_ATOP);
                }
        }
        return read_status_image;
    }


}
