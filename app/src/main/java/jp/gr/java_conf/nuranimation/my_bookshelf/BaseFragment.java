package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Kamada on 2019/03/11.
 */

public class BaseFragment extends Fragment {
    AppCompatActivity mActivity = null;
    FragmentListener mFragmentListener = null;

    public interface FragmentListener {
        void onFragmentEvent(FragmentEvent event);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        if (context instanceof FragmentListener){
            mFragmentListener = (FragmentListener) context;
        }else {
            throw new UnsupportedOperationException("Listener is not Implementation.");
        }

        if (context instanceof AppCompatActivity){
            mActivity = (MainActivity) context;
        }else{
            throw new UnsupportedOperationException("Activity is not Implementation.");
        }
    }
}
