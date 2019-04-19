package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MyBookshelfDBOpenHelper extends SQLiteOpenHelper {
    public static final String TAG = MyBookshelfDBOpenHelper.class.getSimpleName();
    private static final boolean D = true;

    private static final String DB_NAME = "jp.gr.java_conf.nuranimation.MyBookshelf.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_MY_BOOKSHELF = "my_bookshelf";
    private static final String TABLE_AUTHOR = "authors";
    private static final String TABLE_NEW_BOOKS_LIST = "new_books_list";

    private static final String BOOKSHELF_KEY_ISBN               = "isbn";
    private static final String BOOKSHELF_KEY_TITLE              = "title";
    private static final String BOOKSHELF_KEY_AUTHOR             = "author";
    private static final String BOOKSHELF_KEY_PUBLISHER          = "publisher_name";
    private static final String BOOKSHELF_KEY_RELEASE_DATE       = "release_date";
    private static final String BOOKSHELF_KEY_PRICE              = "price";
    private static final String BOOKSHELF_KEY_RAKUTEN_URL        = "rakuten_url";
    private static final String BOOKSHELF_KEY_IMAGES             = "images";
    private static final String BOOKSHELF_KEY_RATING             = "rating";
    private static final String BOOKSHELF_KEY_READ_STATUS        = "read_status";
    private static final String BOOKSHELF_KEY_TAGS               = "tags";
    private static final String BOOKSHELF_KEY_FINISH_READ_DATE   = "finish_read_date";
    private static final String BOOKSHELF_KEY_REGISTER_DATE      = "register_date";

    private static final String AUTHOR_KEY_AUTHOR = "author";

    private static final String DROP_TABLE_SHELF  = "drop table " + TABLE_MY_BOOKSHELF + ";";
    private static final String DROP_TABLE_AUTHOR = "drop table " + TABLE_AUTHOR + ";";
    private static final String DROP_TABLE_NEW_BOOKS = "drop table " + TABLE_NEW_BOOKS_LIST + ";";

    private static final String CREATE_TABLE_SHELF = "create table " + TABLE_MY_BOOKSHELF + " ("
            + "_id integer primary key" // id
            + ", " + BOOKSHELF_KEY_ISBN             + " text"  // ISBN
            + ", " + BOOKSHELF_KEY_TITLE            + " text"  // タイトル
            + ", " + BOOKSHELF_KEY_AUTHOR           + " text"  // 著者
            + ", " + BOOKSHELF_KEY_PUBLISHER        + " text"  // 出版社
            + ", " + BOOKSHELF_KEY_RELEASE_DATE     + " text"  // 発売日
            + ", " + BOOKSHELF_KEY_PRICE            + " text"  // 定価
            + ", " + BOOKSHELF_KEY_RAKUTEN_URL      + " text"  // URL
            + ", " + BOOKSHELF_KEY_IMAGES           + " text"  // 商品画像URL
            + ", " + BOOKSHELF_KEY_RATING           + " text"  // レーティング
            + ", " + BOOKSHELF_KEY_READ_STATUS      + " text"  // ステータス
            + ", " + BOOKSHELF_KEY_TAGS             + " text"  // タグ
            + ", " + BOOKSHELF_KEY_FINISH_READ_DATE + " text"  // 読了日
            + ", " + BOOKSHELF_KEY_REGISTER_DATE    + " text"  // 登録日
            + ");";
    private static final String CREATE_TABLE_AUTHOR = "create table " + TABLE_AUTHOR + " ("
            + "_id integer primary key" // id
            + ", " + AUTHOR_KEY_AUTHOR + " text"  // 著者
            + ");";
    private static final String CREATE_TABLE_NEW_BOOKS = "create table " + TABLE_NEW_BOOKS_LIST + " ("
            + "_id integer primary key" // id
            + ", " + BOOKSHELF_KEY_ISBN             + " text"  // ISBN
            + ", " + BOOKSHELF_KEY_TITLE            + " text"  // タイトル
            + ", " + BOOKSHELF_KEY_AUTHOR           + " text"  // 著者
            + ", " + BOOKSHELF_KEY_PUBLISHER        + " text"  // 出版社
            + ", " + BOOKSHELF_KEY_RELEASE_DATE     + " text"  // 発売日
            + ", " + BOOKSHELF_KEY_PRICE            + " text"  // 定価
            + ", " + BOOKSHELF_KEY_RAKUTEN_URL      + " text"  // URL
            + ", " + BOOKSHELF_KEY_IMAGES           + " text"  // 商品画像URL
            + ", " + BOOKSHELF_KEY_RATING           + " text"  // レーティング
            + ", " + BOOKSHELF_KEY_READ_STATUS      + " text"  // ステータス
            + ", " + BOOKSHELF_KEY_TAGS             + " text"  // タグ
            + ", " + BOOKSHELF_KEY_FINISH_READ_DATE + " text"  // 読了日
            + ", " + BOOKSHELF_KEY_REGISTER_DATE    + " text"  // 登録日
            + ");";


    MyBookshelfDBOpenHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (D) Log.d(TAG, "onCreate");
        db.execSQL(CREATE_TABLE_SHELF);
        db.execSQL(CREATE_TABLE_AUTHOR);
        db.execSQL(CREATE_TABLE_NEW_BOOKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_SHELF);
        db.execSQL(DROP_TABLE_AUTHOR);
        db.execSQL(DROP_TABLE_NEW_BOOKS);
        onCreate(db);
    }

    void deleteTABLE_SHELF(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DROP_TABLE_SHELF);
        db.execSQL(CREATE_TABLE_SHELF);
    }

    void deleteTABLE_AUTHOR(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DROP_TABLE_AUTHOR);
        db.execSQL(CREATE_TABLE_AUTHOR);
    }

    void deleteTABLE_NEW_BOOKS(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DROP_TABLE_NEW_BOOKS);
        db.execSQL(CREATE_TABLE_NEW_BOOKS);
    }


    List<BookData> getMyShelf(){
        List<BookData> shelf = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + TABLE_MY_BOOKSHELF + ";";

        Cursor c = db.rawQuery(sql, null);
        boolean mov = c.moveToFirst();
        while (mov) {
            BookData data = new BookData();
            data.setView_type(ShelfBooksViewAdapter.VIEW_TYPE_BOOK);
            data.setIsbn(c.getString(c.getColumnIndex(BOOKSHELF_KEY_ISBN)));
            data.setImage(c.getString(c.getColumnIndex(BOOKSHELF_KEY_IMAGES)));
            data.setTitle(c.getString(c.getColumnIndex(BOOKSHELF_KEY_TITLE)));
            data.setAuthor(c.getString(c.getColumnIndex(BOOKSHELF_KEY_AUTHOR)));
            data.setPublisher(c.getString(c.getColumnIndex(BOOKSHELF_KEY_PUBLISHER)));
            data.setSalesDate(c.getString(c.getColumnIndex(BOOKSHELF_KEY_RELEASE_DATE)));
            data.setItemPrice(c.getString(c.getColumnIndex(BOOKSHELF_KEY_PRICE)));
            data.setRakutenUrl(c.getString(c.getColumnIndex(BOOKSHELF_KEY_RAKUTEN_URL)));
            data.setRating(c.getString(c.getColumnIndex(BOOKSHELF_KEY_RATING)));
            data.setReadStatus(c.getString(c.getColumnIndex(BOOKSHELF_KEY_READ_STATUS)));
            data.setTags(c.getString(c.getColumnIndex(BOOKSHELF_KEY_TAGS)));
            data.setFinishReadDate(c.getString(c.getColumnIndex(BOOKSHELF_KEY_FINISH_READ_DATE)));
            data.setRegisterDate(c.getString(c.getColumnIndex(BOOKSHELF_KEY_REGISTER_DATE)));
            shelf.add(data);
            mov = c.moveToNext();
        }
        c.close();
        return shelf;
    }

    void registerBook(BookData book){
        ContentValues insertValues = new ContentValues();
        insertValues.put(BOOKSHELF_KEY_ISBN, book.getIsbn());// ISBN
        insertValues.put(BOOKSHELF_KEY_TITLE, book.getTitle());// タイトル
        insertValues.put(BOOKSHELF_KEY_AUTHOR,book.getAuthor()); // 著者
        insertValues.put(BOOKSHELF_KEY_PUBLISHER, book.getPublisher());// 出版社
        insertValues.put(BOOKSHELF_KEY_RELEASE_DATE, book.getSalesDate());// 発売日
        insertValues.put(BOOKSHELF_KEY_PRICE, book.getItemPrice());// 定価
        insertValues.put(BOOKSHELF_KEY_RAKUTEN_URL, book.getRakutenUrl());// URL
        insertValues.put(BOOKSHELF_KEY_IMAGES, book.getImage());// 商品画像URL
        insertValues.put(BOOKSHELF_KEY_RATING, book.getRating()); // レーティング
        insertValues.put(BOOKSHELF_KEY_READ_STATUS, book.getReadStatus());// ステータス
        insertValues.put(BOOKSHELF_KEY_TAGS, book.getTags());// タグ
        insertValues.put(BOOKSHELF_KEY_FINISH_READ_DATE, book.getFinishReadDate()); // 読了日
        insertValues.put(BOOKSHELF_KEY_REGISTER_DATE, book.getRegisterDate());// 登録日

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_MY_BOOKSHELF,"",insertValues);
    }


    List<String> getAuthors(){
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + TABLE_AUTHOR + ";";
        Cursor c = db.rawQuery(sql, null);
        boolean mov = c.moveToFirst();
        while (mov) {
            list.add(c.getString(c.getColumnIndex(AUTHOR_KEY_AUTHOR)));
            mov = c.moveToNext();
        }
        c.close();
        return list;
    }


    void registerAuthor(String author){
        if(!author.equals("")) {
            author = author.replaceAll("[\\x20\\u3000]","");
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "select * from " + TABLE_AUTHOR + " where " + AUTHOR_KEY_AUTHOR + " = ?;";
            Cursor c = db.rawQuery(sql, new String[]{author});
            boolean mov = c.moveToFirst();
            if (mov) {
                if (D) Log.d(TAG, "already registered: " + author);
            } else {
                // register author
                if (D) Log.d(TAG, "register: " + author);
                ContentValues insertValues = new ContentValues();
                insertValues.put(AUTHOR_KEY_AUTHOR, author);
                db.insert(TABLE_AUTHOR, "", insertValues);
            }
            c.close();
        }
    }


    List<BookData> getNewBooks(){
        List<BookData> new_books = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + TABLE_NEW_BOOKS_LIST + " order by " + BOOKSHELF_KEY_RELEASE_DATE + " desc" + ";";

        Cursor c = db.rawQuery(sql, null);
        boolean mov = c.moveToFirst();
        while (mov) {
            BookData data = new BookData();
            data.setView_type(ShelfBooksViewAdapter.VIEW_TYPE_BOOK);
            data.setIsbn(c.getString(c.getColumnIndex(BOOKSHELF_KEY_ISBN)));
            data.setImage(c.getString(c.getColumnIndex(BOOKSHELF_KEY_IMAGES)));
            data.setTitle(c.getString(c.getColumnIndex(BOOKSHELF_KEY_TITLE)));
            data.setAuthor(c.getString(c.getColumnIndex(BOOKSHELF_KEY_AUTHOR)));
            data.setPublisher(c.getString(c.getColumnIndex(BOOKSHELF_KEY_PUBLISHER)));
            data.setSalesDate(c.getString(c.getColumnIndex(BOOKSHELF_KEY_RELEASE_DATE)));
            data.setItemPrice(c.getString(c.getColumnIndex(BOOKSHELF_KEY_PRICE)));
            data.setRakutenUrl(c.getString(c.getColumnIndex(BOOKSHELF_KEY_RAKUTEN_URL)));
            data.setRating(c.getString(c.getColumnIndex(BOOKSHELF_KEY_RATING)));
            data.setReadStatus(c.getString(c.getColumnIndex(BOOKSHELF_KEY_READ_STATUS)));
            data.setTags(c.getString(c.getColumnIndex(BOOKSHELF_KEY_TAGS)));
            data.setFinishReadDate(c.getString(c.getColumnIndex(BOOKSHELF_KEY_FINISH_READ_DATE)));
            data.setRegisterDate(c.getString(c.getColumnIndex(BOOKSHELF_KEY_REGISTER_DATE)));
            new_books.add(data);
            mov = c.moveToNext();
        }
        c.close();
        return new_books;
    }


    void addNewBook(BookData book){
        String isbn = book.getIsbn();
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "select * from " + TABLE_NEW_BOOKS_LIST + " where " + BOOKSHELF_KEY_ISBN + " = ?;";
        Cursor c = db.rawQuery(sql, new String[]{isbn});
        boolean mov = c.moveToFirst();
        if (mov) {
            if (D) Log.d(TAG, "already registered");
        } else {
            // register author
            if (D) Log.d(TAG, "book register");
            ContentValues insertValues = new ContentValues();
            insertValues.put(BOOKSHELF_KEY_ISBN, book.getIsbn());// ISBN
            insertValues.put(BOOKSHELF_KEY_TITLE, book.getTitle());// タイトル
            insertValues.put(BOOKSHELF_KEY_AUTHOR,book.getAuthor()); // 著者
            insertValues.put(BOOKSHELF_KEY_PUBLISHER, book.getPublisher());// 出版社
            insertValues.put(BOOKSHELF_KEY_RELEASE_DATE, book.getSalesDate());// 発売日
            insertValues.put(BOOKSHELF_KEY_PRICE, book.getItemPrice());// 定価
            insertValues.put(BOOKSHELF_KEY_RAKUTEN_URL, book.getRakutenUrl());// URL
            insertValues.put(BOOKSHELF_KEY_IMAGES, book.getImage());// 商品画像URL
            insertValues.put(BOOKSHELF_KEY_RATING, book.getRating()); // レーティング
            insertValues.put(BOOKSHELF_KEY_READ_STATUS, book.getReadStatus());// ステータス
            insertValues.put(BOOKSHELF_KEY_TAGS, book.getTags());// タグ
            insertValues.put(BOOKSHELF_KEY_FINISH_READ_DATE, book.getFinishReadDate()); // 読了日
            insertValues.put(BOOKSHELF_KEY_REGISTER_DATE, book.getRegisterDate());// 登録日
            db.insert(TABLE_NEW_BOOKS_LIST, "", insertValues);
        }
        c.close();
    }

}



