package jp.gr.java_conf.nuranimation.my_bookshelf.application;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.adapter.BooksListViewAdapter;

@SuppressWarnings({"WeakerAccess","unused"})
public class MyBookshelfDBOpenHelper extends SQLiteOpenHelper {
    public static final String TAG = MyBookshelfDBOpenHelper.class.getSimpleName();
    private static final boolean D = true;

    private static final String DB_NAME = "jp.gr.java_conf.nuranimation.MyBookshelf.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_SHELF_BOOKS   = "shelf_books";
    private static final String TABLE_AUTHORS       = "authors";
    private static final String TABLE_SEARCH_BOOKS  = "search_books";
    private static final String TABLE_NEW_BOOKS     = "new_books";

    private static final String KEY_ISBN                = "isbn";
    private static final String KEY_TITLE               = "title";
    private static final String KEY_AUTHOR              = "author";
    private static final String KEY_PUBLISHER           = "publisher_name";
    private static final String KEY_RELEASE_DATE        = "release_date";
    private static final String KEY_PRICE               = "price";
    private static final String KEY_RAKUTEN_URL         = "rakuten_url";
    private static final String KEY_IMAGES              = "images";
    private static final String KEY_RATING              = "rating";
    private static final String KEY_READ_STATUS         = "read_status";
    private static final String KEY_TAGS                = "tags";
    private static final String KEY_FINISH_READ_DATE    = "finish_read_date";
    private static final String KEY_REGISTER_DATE       = "register_date";

    private static final String CREATE_TABLE_AUTHOR = "create table " + TABLE_AUTHORS + " ("
            + "_id integer primary key" // id
            + ", " + KEY_AUTHOR + " text"  // 著者
            + ");";
    private static final String CREATE_TABLE_SHELF_BOOKS = "create table " + TABLE_SHELF_BOOKS + " ("
            + "_id integer primary key" // id
            + ", " + KEY_ISBN + " text"  // ISBN
            + ", " + KEY_TITLE + " text"  // タイトル
            + ", " + KEY_AUTHOR + " text"  // 著者
            + ", " + KEY_PUBLISHER + " text"  // 出版社
            + ", " + KEY_RELEASE_DATE + " text"  // 発売日
            + ", " + KEY_PRICE + " text"  // 定価
            + ", " + KEY_RAKUTEN_URL + " text"  // URL
            + ", " + KEY_IMAGES + " text"  // 商品画像URL
            + ", " + KEY_RATING + " text"  // レーティング
            + ", " + KEY_READ_STATUS + " text"  // ステータス
            + ", " + KEY_TAGS + " text"  // タグ
            + ", " + KEY_FINISH_READ_DATE + " text"  // 読了日
            + ", " + KEY_REGISTER_DATE + " text"  // 登録日
            + ");";
    private static final String CREATE_TABLE_SEARCH_BOOKS = "create table " + TABLE_SEARCH_BOOKS + " ("
            + "_id integer primary key" // id
            + ", " + KEY_ISBN + " text"  // ISBN
            + ", " + KEY_TITLE + " text"  // タイトル
            + ", " + KEY_AUTHOR + " text"  // 著者
            + ", " + KEY_PUBLISHER + " text"  // 出版社
            + ", " + KEY_RELEASE_DATE + " text"  // 発売日
            + ", " + KEY_PRICE + " text"  // 定価
            + ", " + KEY_RAKUTEN_URL + " text"  // URL
            + ", " + KEY_IMAGES + " text"  // 商品画像URL
            + ", " + KEY_RATING + " text"  // レーティング
            + ", " + KEY_READ_STATUS + " text"  // ステータス
            + ", " + KEY_TAGS + " text"  // タグ
            + ", " + KEY_FINISH_READ_DATE + " text"  // 読了日
            + ", " + KEY_REGISTER_DATE + " text"  // 登録日
            + ");";

    private static final String CREATE_TABLE_NEW_BOOKS = "create table " + TABLE_NEW_BOOKS + " ("
            + "_id integer primary key" // id
            + ", " + KEY_ISBN + " text"  // ISBN
            + ", " + KEY_TITLE + " text"  // タイトル
            + ", " + KEY_AUTHOR + " text"  // 著者
            + ", " + KEY_PUBLISHER + " text"  // 出版社
            + ", " + KEY_RELEASE_DATE + " text"  // 発売日
            + ", " + KEY_PRICE + " text"  // 定価
            + ", " + KEY_RAKUTEN_URL + " text"  // URL
            + ", " + KEY_IMAGES + " text"  // 商品画像URL
            + ", " + KEY_RATING + " text"  // レーティング
            + ", " + KEY_READ_STATUS + " text"  // ステータス
            + ", " + KEY_TAGS + " text"  // タグ
            + ", " + KEY_FINISH_READ_DATE + " text"  // 読了日
            + ", " + KEY_REGISTER_DATE + " text"  // 登録日
            + ");";

    private MyBookshelfApplicationData mApplicationData;


    public MyBookshelfDBOpenHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
        mApplicationData = (MyBookshelfApplicationData) context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (D) Log.d(TAG, "onCreate");
        db.execSQL(CREATE_TABLE_AUTHOR);
        db.execSQL(CREATE_TABLE_SHELF_BOOKS);
        db.execSQL(CREATE_TABLE_SEARCH_BOOKS);
        db.execSQL(CREATE_TABLE_NEW_BOOKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTable(TABLE_AUTHORS);
        dropTable(TABLE_SHELF_BOOKS);
        dropTable(TABLE_SEARCH_BOOKS);
        dropTable(TABLE_NEW_BOOKS);
    }

    private void dropTable(final String table){
        SQLiteDatabase db = this.getWritableDatabase();
        String drop = "drop table " + table + ";";
        db.execSQL(drop);
        switch(table){
            case TABLE_AUTHORS:
                db.execSQL(CREATE_TABLE_AUTHOR);
                break;
            case TABLE_SHELF_BOOKS:
                db.execSQL(CREATE_TABLE_SHELF_BOOKS);
                break;
            case TABLE_SEARCH_BOOKS:
                db.execSQL(CREATE_TABLE_SEARCH_BOOKS);
                break;
            case TABLE_NEW_BOOKS:
                db.execSQL(CREATE_TABLE_NEW_BOOKS);
                break;
        }
    }

    private void registerBook(final String table, final BookData book) {
        if (book != null && !TextUtils.isEmpty(book.getISBN())) {
            ContentValues insertValues = new ContentValues();
            insertValues.put(KEY_ISBN, book.getISBN());// ISBN
            insertValues.put(KEY_TITLE, book.getTitle());// タイトル
            insertValues.put(KEY_AUTHOR, book.getAuthor()); // 著者
            insertValues.put(KEY_PUBLISHER, book.getPublisher());// 出版社
            insertValues.put(KEY_RELEASE_DATE, book.getSalesDate());// 発売日
            insertValues.put(KEY_PRICE, book.getItemPrice());// 定価
            insertValues.put(KEY_RAKUTEN_URL, book.getRakutenUrl());// URL
            insertValues.put(KEY_IMAGES, book.getImage());// 商品画像URL
            insertValues.put(KEY_RATING, book.getRating()); // レーティング
            insertValues.put(KEY_READ_STATUS, book.getReadStatus());// ステータス
            insertValues.put(KEY_TAGS, book.getTags());// タグ
            insertValues.put(KEY_FINISH_READ_DATE, book.getFinishReadDate()); // 読了日
            insertValues.put(KEY_REGISTER_DATE, book.getRegisterDate());// 登録日

            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "select * from " + table + " where " + KEY_ISBN + " = ?;";
            Cursor c = db.rawQuery(sql, new String[]{book.getISBN()});
            boolean mov = c.moveToFirst();
            if (mov) {
                if (D) Log.d(TAG, "update Books: " + book.getTitle());
                String sql_ISBN = KEY_ISBN + " = ?";
                db.update(table, insertValues, sql_ISBN, new String[]{book.getISBN()});
            } else {
                if (D) Log.d(TAG, "register Books: " + book.getTitle());
                db.insert(table, "", insertValues);
            }
            c.close();
        }
    }

    private void registerBooks(final String table, final List<BookData> books){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        switch(table){
            case TABLE_NEW_BOOKS:
                dropTable(TABLE_NEW_BOOKS);
                break;
        }



        for (BookData book : books) {
            if (book != null && !TextUtils.isEmpty(book.getISBN())) {
                ContentValues insertValues = new ContentValues();
                insertValues.put(KEY_ISBN, book.getISBN());// ISBN
                insertValues.put(KEY_TITLE, book.getTitle());// タイトル
                insertValues.put(KEY_AUTHOR, book.getAuthor()); // 著者
                insertValues.put(KEY_PUBLISHER, book.getPublisher());// 出版社
                insertValues.put(KEY_RELEASE_DATE, book.getSalesDate());// 発売日
                insertValues.put(KEY_PRICE, book.getItemPrice());// 定価
                insertValues.put(KEY_RAKUTEN_URL, book.getRakutenUrl());// URL
                insertValues.put(KEY_IMAGES, book.getImage());// 商品画像URL
                insertValues.put(KEY_RATING, book.getRating()); // レーティング
                insertValues.put(KEY_READ_STATUS, book.getReadStatus());// ステータス
                insertValues.put(KEY_TAGS, book.getTags());// タグ
                insertValues.put(KEY_FINISH_READ_DATE, book.getFinishReadDate()); // 読了日
                insertValues.put(KEY_REGISTER_DATE, book.getRegisterDate());// 登録日

                String sql = "select * from " + table + " where " + KEY_ISBN + " = ?;";
                Cursor c = db.rawQuery(sql, new String[]{book.getISBN()});
                boolean mov = c.moveToFirst();
                if (mov) {
                    if (D) Log.d(TAG, "update Books: " + book.getTitle());
                    String sql_ISBN = KEY_ISBN + " = ?";
                    db.update(table, insertValues, sql_ISBN, new String[]{book.getISBN()});
                } else {
                    if (D) Log.d(TAG, "register Books: " + book.getTitle());
                    db.insert(table, "", insertValues);
                }
                c.close();
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void unregisterBook(final String table, final BookData book){
        if (book != null && !TextUtils.isEmpty(book.getISBN())) {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "select * from " + table + " where " + KEY_ISBN + " = ?;";
            String ISBN = book.getISBN();
            Cursor c = db.rawQuery(sql, new String[]{ISBN});
            boolean mov = c.moveToFirst();
            if (mov) {
                if (D) Log.d(TAG, "delete Books ISBN: " + ISBN);
                String sql_ISBN = KEY_ISBN + " = ?";
                db.delete(TABLE_SHELF_BOOKS, sql_ISBN, new String[]{ISBN});
            }
            c.close();
        }
    }

    private List<BookData> loadBooks(final String table, final String keyword) {
        List<BookData> books = new ArrayList<>(1000);
        String where = "";
        String order = "";

        if (!TextUtils.isEmpty(keyword)) {
            where = " where "
                    + KEY_TITLE + " like " + "'%" + keyword + "%'" + " or "
                    + KEY_AUTHOR + " like " + "'%" + keyword + "%'" + " or "
                    + KEY_ISBN + " = " + "'" + keyword + "'";
        }
        switch(table){
            case TABLE_SHELF_BOOKS:
                order = mApplicationData.getShelfBooksSortSetting();
                break;
            case TABLE_SEARCH_BOOKS:
                order = "";
                break;
            case TABLE_NEW_BOOKS:
                order = " order by " + KEY_RELEASE_DATE + " desc";
                break;
        }
        String sql = "select * from " + table + where + order + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);
        boolean mov = c.moveToFirst();
        while (mov) {
            BookData book = new BookData();
            book.setView_type(BooksListViewAdapter.VIEW_TYPE_BOOK);
            book.setISBN(c.getString(c.getColumnIndex(KEY_ISBN)));
            book.setImage(c.getString(c.getColumnIndex(KEY_IMAGES)));
            book.setTitle(c.getString(c.getColumnIndex(KEY_TITLE)));
            book.setAuthor(c.getString(c.getColumnIndex(KEY_AUTHOR)));
            book.setPublisher(c.getString(c.getColumnIndex(KEY_PUBLISHER)));
            book.setSalesDate(c.getString(c.getColumnIndex(KEY_RELEASE_DATE)));
            book.setItemPrice(c.getString(c.getColumnIndex(KEY_PRICE)));
            book.setRakutenUrl(c.getString(c.getColumnIndex(KEY_RAKUTEN_URL)));
            book.setRating(c.getString(c.getColumnIndex(KEY_RATING)));
            book.setReadStatus(c.getString(c.getColumnIndex(KEY_READ_STATUS)));
            book.setTags(c.getString(c.getColumnIndex(KEY_TAGS)));
            book.setFinishReadDate(c.getString(c.getColumnIndex(KEY_FINISH_READ_DATE)));
            book.setRegisterDate(c.getString(c.getColumnIndex(KEY_REGISTER_DATE)));
            books.add(book);
            mov = c.moveToNext();
        }
        c.close();
        return books;
    }

    private BookData loadBookData(String table, BookData book){
        if(book == null){
            return null;
        }
        if(TextUtils.isEmpty(book.getISBN())){
            return null;
        }
        String ISBN = book.getISBN();
        BookData result = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + table + " where " + KEY_ISBN + " = ?;";
        Cursor c = db.rawQuery(sql, new String[]{ISBN});
        boolean mov = c.moveToFirst();
        if (mov) {
            result = new BookData();
            result.setView_type(BooksListViewAdapter.VIEW_TYPE_BOOK);
            result.setISBN(c.getString(c.getColumnIndex(KEY_ISBN)));
            result.setImage(c.getString(c.getColumnIndex(KEY_IMAGES)));
            result.setTitle(c.getString(c.getColumnIndex(KEY_TITLE)));
            result.setAuthor(c.getString(c.getColumnIndex(KEY_AUTHOR)));
            result.setPublisher(c.getString(c.getColumnIndex(KEY_PUBLISHER)));
            result.setSalesDate(c.getString(c.getColumnIndex(KEY_RELEASE_DATE)));
            result.setItemPrice(c.getString(c.getColumnIndex(KEY_PRICE)));
            result.setRakutenUrl(c.getString(c.getColumnIndex(KEY_RAKUTEN_URL)));
            result.setRating(c.getString(c.getColumnIndex(KEY_RATING)));
            result.setReadStatus(c.getString(c.getColumnIndex(KEY_READ_STATUS)));
            result.setTags(c.getString(c.getColumnIndex(KEY_TAGS)));
            result.setFinishReadDate(c.getString(c.getColumnIndex(KEY_FINISH_READ_DATE)));
            result.setRegisterDate(c.getString(c.getColumnIndex(KEY_REGISTER_DATE)));
        }
        c.close();
        return result;
    }









    /* --- Authors List --- */
    public void dropTableAuthorsList() {
        dropTable(TABLE_AUTHORS);
    }

    public void registerToAuthorsList(String author) {
        if (!TextUtils.isEmpty(author)) {
            author = author.replaceAll("[\\x20\\u3000]", ""); // replace Half and Full width space
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "select * from " + TABLE_AUTHORS + " where " + KEY_AUTHOR + " = ?;";
            Cursor c = db.rawQuery(sql, new String[]{author});
            boolean mov = c.moveToFirst();
            if (mov) {
                if (D) Log.d(TAG, "already registered: " + author);
            } else {
                // register author
                if (D) Log.d(TAG, "register: " + author);
                ContentValues insertValues = new ContentValues();
                insertValues.put(KEY_AUTHOR, author);
                db.insert(TABLE_AUTHORS, "", insertValues);
            }
            c.close();
        }
    }

    public void registerToAuthorsList(List<String> authors) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        for (String author : authors) {
            if (!TextUtils.isEmpty(author)) {
                author = author.replaceAll("[\\x20\\u3000]", ""); // replace Half and Full width space
                String sql = "select * from " + TABLE_AUTHORS + " where " + KEY_AUTHOR + " = ?;";
                Cursor c = db.rawQuery(sql, new String[]{author});
                boolean mov = c.moveToFirst();
                if (mov) {
                    if (D) Log.d(TAG, "already registered: " + author);
                } else {
                    // register author
                    if (D) Log.d(TAG, "register: " + author);
                    ContentValues insertValues = new ContentValues();
                    insertValues.put(KEY_AUTHOR, author);
                    db.insert(TABLE_AUTHORS, "", insertValues);
                }
                c.close();
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public List<String> loadAuthorsList() {
        List<String> authors = new ArrayList<>(100);
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + TABLE_AUTHORS + " order by " + KEY_AUTHOR + " asc" + ";";
        Cursor c = db.rawQuery(sql, null);
        boolean mov = c.moveToFirst();
        while (mov) {
            authors.add(c.getString(c.getColumnIndex(KEY_AUTHOR)));
            mov = c.moveToNext();
        }
        c.close();
        return authors;
    }


    /* --- ShelfBooks --- */
    public void dropTableShelfBooks(){
        dropTable(TABLE_SHELF_BOOKS);
    }

    public void registerToShelfBooks(BookData book) {
        registerBook(TABLE_SHELF_BOOKS, book);
    }

    public void registerToShelfBooks(List<BookData> books) {
        registerBooks(TABLE_SHELF_BOOKS, books);
    }

    public List<BookData> loadShelfBooks(String keyword) {
        return loadBooks(TABLE_SHELF_BOOKS, keyword);
    }

    public BookData loadBookDataFromShelfBooks(BookData book){
        return loadBookData(TABLE_SHELF_BOOKS, book);
    }

    public void unregisterFromShelfBooks(BookData book) {
        unregisterBook(TABLE_SHELF_BOOKS, book);
    }



    /* --- SearchBooks --- */
    public void dropTableSearchBooks() {
        dropTable(TABLE_SEARCH_BOOKS);
    }

    public void registerToSearchBooks(BookData book) {
        registerBook(TABLE_SEARCH_BOOKS, book);
    }

    public void registerToSearchBooks(List<BookData> books) {
        registerBooks(TABLE_SEARCH_BOOKS, books);
    }

    public List<BookData> loadSearchBooks(){
        return loadBooks(TABLE_SEARCH_BOOKS, null);
    }

    public BookData loadBookDataFromSearchBooks(BookData book){
        return loadBookData(TABLE_SEARCH_BOOKS, book);
    }

    public void unregisterFromSearchBooks(BookData book) {
        unregisterBook(TABLE_SEARCH_BOOKS, book);
    }


    /* --- NewBooks --- */
    public void dropTableNewBooks(){
        dropTable(TABLE_NEW_BOOKS);
    }

    public void registerToNewBooks(BookData book) {
        registerBook(TABLE_NEW_BOOKS, book);
    }

    public void registerToNewBooks(List<BookData> books) {
        registerBooks(TABLE_NEW_BOOKS, books);
    }

    public List<BookData> loadNewBooks(){
        return loadBooks(TABLE_NEW_BOOKS, null);
    }

    public BookData loadBookDataFromNewBooks(BookData book){
        return loadBookData(TABLE_NEW_BOOKS, book);
    }

    public void unregisterFromNewBooks(BookData book) {
        unregisterBook(TABLE_NEW_BOOKS, book);
    }












































}
