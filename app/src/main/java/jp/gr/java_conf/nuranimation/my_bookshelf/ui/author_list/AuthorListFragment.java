package jp.gr.java_conf.nuranimation.my_bookshelf.ui.author_list;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.R;
import jp.gr.java_conf.nuranimation.my_bookshelf.model.database.MyBookshelfDBOpenHelper;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.base.BaseFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.RegisterAuthorDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.dialog.NormalDialogFragment;
import jp.gr.java_conf.nuranimation.my_bookshelf.ui.util.AuthorRecyclerViewAdapter;

public class AuthorListFragment extends BaseFragment implements AuthorRecyclerViewAdapter.OnAuthorClickListener, NormalDialogFragment.OnNormalDialogListener, RegisterAuthorDialogFragment.OnRegisterAuthorDialogListener {
    private static final String TAG = AuthorListFragment.class.getSimpleName();
    private static final boolean D = true;

    private static final String TAG_UNREGISTER_AUTHOR = "AuthorListFragment.TAG_UNREGISTER_AUTHOR";
    private static final String TAG_EDIT_AUTHOR = "AuthorListFragment.TAG_EDIT_AUTHOR";

    private static final int REQUEST_CODE_UNREGISTER_AUTHOR = 1;
    private static final int REQUEST_CODE_EDIT_AUTHOR = 2;

    private static final String KEY_LAYOUT_MANAGER = "AuthorListFragment.KEY_LAYOUT_MANAGER";
    private static final String KEY_POSITION = "AuthorListFragment.KEY_POSITION";
    private static final String KEY_AUTHOR = "AuthorListFragment.KEY_AUTHOR";


    private MyBookshelfDBOpenHelper mDBOpenHelper;
    private LinearLayoutManager mLayoutManager;
    private AuthorRecyclerViewAdapter mAuthorsViewAdapter;
    private List<String> mAuthorList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setHasOptionsMenu(true);
        mDBOpenHelper = MyBookshelfDBOpenHelper.getInstance(context.getApplicationContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (D) Log.d(TAG, "onViewCreated");
        if (getActivity() != null) {
            getActivity().setTitle(R.string.label_author_list);
        }
        mLayoutManager = new LinearLayoutManager(view.getContext());
        if (savedInstanceState != null) {
            if (D) Log.d(TAG, "savedInstanceState != null");
            Parcelable mListState = savedInstanceState.getParcelable(KEY_LAYOUT_MANAGER);
            if (mListState != null) {
                mLayoutManager.onRestoreInstanceState(mListState);
            }
        }

        if (mAuthorList == null) {
            mAuthorList = mDBOpenHelper.loadAuthorsList();
        }

        mAuthorsViewAdapter = new AuthorRecyclerViewAdapter(mAuthorList);
        mAuthorsViewAdapter.setClickListener(this);
        RecyclerView mRecyclerView = view.findViewById(R.id.view_recyclerview);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAuthorsViewAdapter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (D) Log.d(TAG, "onResume()");
    }


    @Override
    public void onPause() {
        super.onPause();
        if (D) Log.d(TAG, "onPause()");
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_LAYOUT_MANAGER, mLayoutManager.onSaveInstanceState());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_author_list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_author_list_save).getIcon().setColorFilter(Color.argb(255, 255, 255, 255), PorterDuff.Mode.SRC_ATOP);
        menu.findItem(R.id.menu_author_list_add).getIcon().setColorFilter(Color.argb(255, 255, 255, 255), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_author_list_save:
                break;
            case R.id.menu_author_list_add:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(AuthorRecyclerViewAdapter adapter, int position, String author) {
        if (D) Log.d(TAG, "onClick() : " + author);
        if (isClickable()) {
            waitClickable(500);
            Bundle param = new Bundle();
            param.putInt(KEY_POSITION, position);
            param.putString(KEY_AUTHOR, author);
            Bundle bundle = new Bundle();
            bundle.putString(RegisterAuthorDialogFragment.KEY_TITLE, getString(R.string.dialog_title_edit_author));
            bundle.putString(RegisterAuthorDialogFragment.KEY_AUTHOR, author);
            bundle.putStringArrayList(RegisterAuthorDialogFragment.KEY_AUTHOR_LIST, (ArrayList<String>) mAuthorList);
            bundle.putString(RegisterAuthorDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
            bundle.putString(RegisterAuthorDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
            bundle.putInt(RegisterAuthorDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_EDIT_AUTHOR);
            bundle.putBundle(RegisterAuthorDialogFragment.KEY_PARAMS, param);
            RegisterAuthorDialogFragment.showRegisterAuthorDialog(this, bundle, TAG_EDIT_AUTHOR);
        }
    }

    @Override
    public void onLongClick(AuthorRecyclerViewAdapter adapter, int position, String author) {
        if (D) Log.d(TAG, "onLongClick() : " + author);
        if (isClickable()) {
            waitClickable(500);
            Bundle param = new Bundle();
            param.putInt(KEY_POSITION, position);
            param.putString(KEY_AUTHOR, author);
            Bundle bundle = new Bundle();
            bundle.putString(NormalDialogFragment.KEY_TITLE, getString(R.string.dialog_title_unregister_author));
            bundle.putString(NormalDialogFragment.KEY_MESSAGE, getString(R.string.dialog_message_unregister_author));
            bundle.putString(NormalDialogFragment.KEY_POSITIVE_LABEL, getString(R.string.dialog_button_label_positive));
            bundle.putString(NormalDialogFragment.KEY_NEGATIVE_LABEL, getString(R.string.dialog_button_label_negative));
            bundle.putInt(NormalDialogFragment.KEY_REQUEST_CODE, REQUEST_CODE_UNREGISTER_AUTHOR);
            bundle.putBundle(NormalDialogFragment.KEY_PARAMS, param);
            bundle.putBoolean(NormalDialogFragment.KEY_CANCELABLE, true);
            NormalDialogFragment.showNormalDialog(this, bundle, TAG_UNREGISTER_AUTHOR);
        }
    }

    @Override
    public void onNormalDialogSucceeded(int requestCode, int resultCode, Bundle params) {
        if (requestCode == REQUEST_CODE_UNREGISTER_AUTHOR && resultCode == DialogInterface.BUTTON_POSITIVE && params != null) {
            int position = params.getInt(KEY_POSITION);
            String author = params.getString(KEY_AUTHOR);
            if(!TextUtils.isEmpty(author)) {
  //              mDBOpenHelper.unregisterAuthor(author);
                mAuthorsViewAdapter.deleteAuthor(position);
//                Toast.makeText(getContext(), getString(R.string.toast_success_unregister), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onNormalDialogCancelled(int requestCode, Bundle params) {
    }

    @Override
    public void onRegister(int requestCode, int resultCode, String author, Bundle params) {
        if (requestCode == REQUEST_CODE_EDIT_AUTHOR && resultCode == DialogInterface.BUTTON_POSITIVE && params != null) {
            int position = params.getInt(KEY_POSITION);
            String deleteAuthor = params.getString(KEY_AUTHOR);
            if(!TextUtils.isEmpty(deleteAuthor) && !TextUtils.isEmpty(author)) {
//                mDBOpenHelper.unregisterAuthor(deleteAuthor);
//                mDBOpenHelper.registerToAuthorsList(author);
                mAuthorsViewAdapter.replaceAuthor(position, author);
//                Toast.makeText(getContext(), getString(R.string.toast_success_register), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCancelled(int requestCode, Bundle params) {

    }



}
