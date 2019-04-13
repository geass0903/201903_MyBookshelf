package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class ShelfFragment extends BaseFragment implements ShelfBooksViewAdapter.OnBookClickListener {
    public static final String TAG = ShelfFragment.class.getSimpleName();
    private static final boolean D = true;

    private ShelfBooksViewAdapter adapter;

    private MyBookshelfApplicationData mData;
    private Context mContext;


    @Override
    public void onAttach (Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mContext = context;
        mData = (MyBookshelfApplicationData) context.getApplicationContext();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_shelf, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = view.findViewById(R.id.fragment_shelf_toolbar);
        toolbar.setTitle(R.string.navigation_title_shelf);
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
        List<BookData> books = mData.getBooksListShelf();
        int recodeCount = books.size();
        if(D) Log.d(TAG, "recodeCount : " + recodeCount);
/*
        for(BookData book : books){
            if(D) Log.d(TAG,"author: " + book.getAuthor());
            helper.registerAuthor(book.getAuthor());
        }
*/
        adapter = new ShelfBooksViewAdapter(books,true);
        adapter.setContext(mContext);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }




    @Override
    public void onBookClick(ShelfBooksViewAdapter adapter, int position, BookData data) {
        String title = data.getTitle();
        if(D) Log.d(TAG,"Click: " + title);
    }

    @Override
    public void onBookLongClick(ShelfBooksViewAdapter adapter, int position, BookData data) {
        String title = data.getTitle();
        if(D) Log.d(TAG,"LongClick: " + title);
    }
}
