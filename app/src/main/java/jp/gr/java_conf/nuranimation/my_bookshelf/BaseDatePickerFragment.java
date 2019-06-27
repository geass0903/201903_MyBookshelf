package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class BaseDatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    public static final String TAG = BaseDatePickerFragment.class.getSimpleName();
    private static final boolean D = false;
    public static final String KEY_DATE = "KEY_DATE";
    public static final String KEY_REQUEST_CODE = "KEY_REQUEST_CODE";


    public interface OnBaseDateSetListener {
        void onDataSet(int requestCode, Calendar calendar);
    }
    private OnBaseDateSetListener mListener;

    @SuppressWarnings("unused")
    public static BaseDatePickerFragment newInstance(Bundle bundle){
        BaseDatePickerFragment instance = new BaseDatePickerFragment();
        instance.setArguments(bundle);
        return instance;
    }

    public static BaseDatePickerFragment newInstance(Fragment fragment, Bundle bundle){
        BaseDatePickerFragment instance = new BaseDatePickerFragment();
        instance.setArguments(bundle);
        int request_code = bundle.getInt(KEY_REQUEST_CODE);
        instance.setTargetFragment(fragment,request_code);
        return instance;
    }


    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Fragment targetFragment = this.getTargetFragment();
        try{
            if(targetFragment != null){
                mListener = (BaseDatePickerFragment.OnBaseDateSetListener) targetFragment;
            }else{
                Fragment parent = this.getParentFragment();
                if(parent != null){
                    mListener = (BaseDatePickerFragment.OnBaseDateSetListener) parent;
                }else {
                    mListener = (BaseDatePickerFragment.OnBaseDateSetListener) context;
                }
            }
        } catch (UnsupportedOperationException e){
            throw new UnsupportedOperationException("mListener is not Implementation.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(getActivity() == null){
            throw new IllegalArgumentException("getActivity() == null");
        }
        if(getArguments() == null){
            throw new NullPointerException("getArguments() == null");
        }
        Bundle bundle = this.getArguments();

        String date = bundle.getString(KEY_DATE);
        Calendar calendar = getCalendar(date);
        if(calendar == null){
            calendar = Calendar.getInstance();
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(android.widget.DatePicker view, int year,
                          int monthOfYear, int dayOfMonth) {
        if(mListener != null){
            Calendar calendar = Calendar.getInstance();
            calendar.set(year,monthOfYear,dayOfMonth);
            mListener.onDataSet(getRequestCode(),calendar);
        }
    }


    private int getRequestCode() {
        Bundle bundle = getArguments();
        if(bundle != null) {
            if(bundle.containsKey(KEY_REQUEST_CODE)){
                return bundle.getInt(KEY_REQUEST_CODE);
            }else{
                return getTargetRequestCode();
            }
        }
        return -1;
    }

    private Calendar getCalendar(String source){
        String[] formats = {"yyyy/MM/dd","yyyy年MM月dd日","yyyy年MM月","yyyy年"};
        for(String format : formats){
            Calendar calendar = parseDate(format, source);
            if(calendar != null){
                return calendar;
            }
        }
        return null;
    }

    private Calendar parseDate(String format, String source) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.JAPAN);
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(source);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            switch (format) {
                case "yyyy年MM月":
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    break;
                case "yyyy年":
                    calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    break;
            }
            return calendar;
        } catch (ParseException e) {
            if(D) e.printStackTrace();
        }
        return null;
    }

}
