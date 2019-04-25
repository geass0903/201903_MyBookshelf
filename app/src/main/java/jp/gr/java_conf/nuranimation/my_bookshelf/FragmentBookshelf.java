package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class FragmentBookshelf extends BaseFragment implements BooksListViewAdapter.OnBookClickListener {
    public static final String TAG = FragmentBookshelf.class.getSimpleName();
    private static final boolean D = true;

    private MyBookshelfApplicationData mData;
    private BooksListViewAdapter booksListViewAdapter;

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
        Toolbar toolbar = view.findViewById(R.id.fragment_bookshelf_toolbar);
        toolbar.setTitle(R.string.Navigation_Item_Shelf);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do Nothing
            }
        });

        toolbar.inflateMenu(R.menu.menu_shelf);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.menu_shelf_action_search:
                        if(D) Log.d(TAG,"menu shelf action keyword");
                        break;
                }
                return false;
            }
        });

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

    private void SetShelfRowData(RecyclerView recyclerView) {
        List<BookData> books = mData.getList_MyBookshelf();
        int recodeCount = books.size();
        if(D) Log.d(TAG, "recodeCount : " + recodeCount);
        booksListViewAdapter = new BooksListViewAdapter(this.getContext(),books,true);
        booksListViewAdapter.setClickListener(this);
        recyclerView.setAdapter(booksListViewAdapter);
    }




    @Override
    public void onBookClick(BooksListViewAdapter adapter, int position, BookData data) {
        String title = data.getTitle();
        if(D) Log.d(TAG,"Click: " + title);
    }

    @Override
    public void onBookLongClick(BooksListViewAdapter adapter, int position, BookData data) {
        String title = data.getTitle();
        if(D) Log.d(TAG,"LongClick: " + title);
        showLogoutDialog();
    }





    private void showLogoutDialog(){
        Bundle bundle = new Bundle();
        bundle.putString(BaseDialogFragment.title,getString(R.string.Dialog_Label_Logout));
        bundle.putString(BaseDialogFragment.message,getString(R.string.Dialog_Message_Logout));
        bundle.putString(BaseDialogFragment.positiveLabel,getString(R.string.Dialog_Button_Positive));
        bundle.putString(BaseDialogFragment.negativeLabel,getString(R.string.Dialog_Button_Negative));
        bundle.putInt(BaseDialogFragment.request_code,RequestCode_Logout);
        if(getActivity() != null) {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            BaseDialogFragment dialog = BaseDialogFragment.newInstance(this,bundle);
            dialog.show(manager, FragmentSettings.TAG);
        }
    }


    @Override
    public void onBaseDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        super.onBaseDialogSucceeded(requestCode, resultCode, params);

 //       String isbn = data.getIsbn();

        MyBookshelfDBOpenHelper helper = mData.getDatabaseHelper();
   //     helper.deleteBook(isbn);


    }


    @Override
    public void onBaseDialogCancelled(int requestCode, Bundle params) {
        super.onBaseDialogCancelled(requestCode,params);
    }


}
