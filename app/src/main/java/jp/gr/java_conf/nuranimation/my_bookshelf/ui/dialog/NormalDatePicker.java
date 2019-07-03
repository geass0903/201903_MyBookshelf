package jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import java.util.Calendar;

public class NormalDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener{
    public static final String TAG = NormalDatePicker.class.getSimpleName();

    public static final String KEY_REQUEST_CODE     = "NormalDatePicker.KEY_REQUEST_CODE";
    public static final String KEY_YEAR             = "NormalDatePicker.KEY_YEAR";
    public static final String KEY_MONTH            = "NormalDatePicker.KEY_MONTH";
    public static final String KEY_DAY_OF_MONTH     = "NormalDatePicker.KEY_DAY_OF_MONTH";

    public interface OnBaseDateSetListener {
        void onDataSet(int requestCode, Calendar calendar);
    }
    private OnBaseDateSetListener mListener;


    @SuppressWarnings("unused")
    public static NormalDatePicker newInstance(Bundle bundle){
        NormalDatePicker instance = new NormalDatePicker();
        instance.setArguments(bundle);
        return instance;
    }

    public static NormalDatePicker newInstance(Fragment fragment, Bundle bundle){
        NormalDatePicker instance = new NormalDatePicker();
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
                mListener = (OnBaseDateSetListener) targetFragment;
            }else{
                Fragment parent = this.getParentFragment();
                if(parent != null){
                    mListener = (OnBaseDateSetListener) parent;
                }else {
                    mListener = (OnBaseDateSetListener) context;
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

        Calendar calendar = Calendar.getInstance();
        if(bundle.containsKey(KEY_YEAR) && bundle.containsKey(KEY_MONTH) && bundle.containsKey(KEY_DAY_OF_MONTH)){
            calendar.set(bundle.getInt(KEY_YEAR), bundle.getInt(KEY_MONTH), bundle.getInt(KEY_DAY_OF_MONTH));
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
            mListener.onDataSet(getRequestCode(), calendar);
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

}
