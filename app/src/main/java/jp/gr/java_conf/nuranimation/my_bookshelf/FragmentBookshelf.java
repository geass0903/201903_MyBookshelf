package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.Slide;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

public class FragmentBookshelf extends BaseFragment implements BooksListViewAdapter.OnBookClickListener {
    public static final String TAG = FragmentBookshelf.class.getSimpleName();
    private static final boolean D = true;

    private MyBookshelfApplicationData mData;
    private BooksListViewAdapter booksListViewAdapter;

    private static final String KEY_position = "KEY_position";
    private static final String KEY_Book = "KEY_Book";

    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mData = (MyBookshelfApplicationData) context.getApplicationContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bookshelf, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if(D) Log.d(TAG, "onViewCreated");
        RecyclerView recyclerView = view.findViewById(R.id.fragment_shelf_recyclerview);
        LinearLayoutManager manager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(manager);
        SetShelfRowData(recyclerView);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_shelf,menu);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_shelf_action_search).getIcon().setColorFilter(Color.argb(255,255,255,255), PorterDuff.Mode.SRC_ATOP);
        if(D) Log.d(TAG,"onPrepareOptionsMenu()");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_shelf_action_search:
                if(D) Log.d(TAG,"shelf action search");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void SetShelfRowData(RecyclerView recyclerView) {
        mData.updateList_MyBookshelf();
        List<BookData> books = mData.getList_MyBookshelf();
        int recodeCount = books.size();
        if(D) Log.d(TAG, "recodeCount : " + recodeCount);
        booksListViewAdapter = new BooksListViewAdapter(this.getContext(),books,true);
        booksListViewAdapter.setClickListener(this);
        recyclerView.setAdapter(booksListViewAdapter);
    }




    @Override
    public void onBookClick(BooksListViewAdapter adapter, int position, BookData data) {
        if(isClickEnabled) {
            isClickEnabled = false;
            setWait_ClickEnable(500);
            int view_type = adapter.getItemViewType(position);
            if (view_type == BooksListViewAdapter.VIEW_TYPE_BOOK) {
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    FragmentBookDetail fragment = new FragmentBookDetail();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(FragmentBookDetail.KEY_bundle_book, data);

                    fragment.setArguments(bundle);
                    Slide slide = new Slide();
                    slide.setSlideEdge(Gravity.BOTTOM);
                    fragment.setEnterTransition(slide);
                fragmentTransaction.replace(R.id.contents_container, fragment,FragmentBookDetail.TAG);
//                    fragmentTransaction.add(R.id.contents_container, fragment, FragmentBookDetail.TAG);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            }
        }





    }

    @Override
    public void onBookLongClick(BooksListViewAdapter adapter, int position, BookData data) {
        String title = data.getTitle();
        if(D) Log.d(TAG,"LongClick: " + title);
        Bundle bundle = new Bundle();
        bundle.putString(BaseDialogFragment.title,getString(R.string.Dialog_Label_Delete_Book));
        bundle.putString(BaseDialogFragment.message,getString(R.string.Dialog_Message_Delete_Book));
        bundle.putString(BaseDialogFragment.positiveLabel,getString(R.string.Dialog_Button_Positive));
        bundle.putString(BaseDialogFragment.negativeLabel,getString(R.string.Dialog_Button_Negative));
        bundle.putInt(BaseDialogFragment.request_code, REQUEST_CODE_Delete_Book);

        Bundle bundle_book = new Bundle();
        bundle_book.putInt(KEY_position,position);
        bundle_book.putParcelable(KEY_Book,data);
        bundle.putBundle(BaseDialogFragment.params,bundle_book);
        if(getActivity() != null) {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            BaseDialogFragment dialog = BaseDialogFragment.newInstance(this,bundle);
            dialog.show(manager, FragmentSettings.TAG);
        }
    }


    @Override
    public void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        super.onBaseDialogSucceeded(requestCode, resultCode, params);
        if (requestCode == REQUEST_CODE_Delete_Book && resultCode == DialogInterface.BUTTON_POSITIVE && params != null) {
            int position = params.getInt(KEY_position, -1);
            BookData book = params.getParcelable(KEY_Book);
            if (book != null) {
                MyBookshelfDBOpenHelper helper = mData.getDatabaseHelper();
                helper.deleteBook(book.getIsbn());
                booksListViewAdapter.deleteBook(position);
                Toast.makeText(getContext(),getString(R.string.Toast_Delete_Book),Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {
        super.onBaseDialogCancelled(requestCode,params);
    }


}
