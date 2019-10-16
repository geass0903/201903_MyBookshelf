package jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog;

import android.app.Dialog;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.utils.BookDataUtils;


public class BookImageDialogFragment extends DialogFragment {
    private static final String TAG = BookImageDialogFragment.class.getSimpleName();
    private static final boolean D = true;

    public static final String KEY_IMAGE_URL    = "BookImageDialogFragment.KEY_IMAGE_URL";

    private SimpleDraweeView bookImageView;
    private String url;

    public static BookImageDialogFragment newInstance(Bundle bundle) {
        BookImageDialogFragment instance = new BookImageDialogFragment();
        instance.setArguments(bundle);
        return instance;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() == null) {
            throw new IllegalArgumentException("getActivity() == null");
        }
        if (getArguments() == null) {
            throw new NullPointerException("getArguments() == null");
        }

        if (savedInstanceState != null) {
            url = savedInstanceState.getString(KEY_IMAGE_URL);
        } else {
            Bundle bundle = this.getArguments();
            url = bundle.getString(KEY_IMAGE_URL);
        }
        setCancelable(true);


        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.view_simpledraweeview);

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_IMAGE_URL, url);
    }


    @Override
    public void onStart() {
        super.onStart();

        bookImageView = getDialog().findViewById(R.id.book_image);
        bookImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        String urlString = BookDataUtils.parseUrlString(url, BookDataUtils.IMAGE_TYPE_ORIGINAL);
        Uri uri = Uri.parse(urlString);

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(mControllerListener)
                .setUri(uri)
                .build();
        bookImageView.setController(controller);

    }


    ControllerListener<ImageInfo> mControllerListener = new ControllerListener<ImageInfo>() {
        @Override
        public void onSubmit(String id, Object callerContext) {

        }

        @Override
        public void onFinalImageSet(
                String id,
                @Nullable ImageInfo imageInfo,
                @Nullable Animatable animatable) {
            if (imageInfo != null) {
                float width = (float) imageInfo.getWidth();
                float height = (float) imageInfo.getHeight();
                if (D) Log.d(TAG, "width: " + width);
                if (D) Log.d(TAG, "height: " + height);
                float ratio = width / height;
                if (D) Log.d(TAG, "ratio: " + ratio);
                bookImageView.setAspectRatio(ratio);
            }
        }

        @Override
        public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {

        }

        @Override
        public void onIntermediateImageFailed(String id, Throwable throwable) {

        }

        @Override
        public void onFailure(String id, Throwable throwable) {

        }

        @Override
        public void onRelease(String id) {

        }
    };

    public static void showBookImageDialog(Fragment fragment, Bundle bundle, String tag) {
        if (D) Log.d(TAG, "showBookImageDialog TAG: " + tag);
        if (fragment != null && fragment.getActivity() != null && bundle != null) {
            FragmentManager manager = fragment.getActivity().getSupportFragmentManager();
            BookImageDialogFragment dialog = BookImageDialogFragment.newInstance(bundle);
            dialog.show(manager, tag);
        }
    }





}
