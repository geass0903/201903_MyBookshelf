package jp.gr.java_conf.nuranimation.my_bookshelf.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.book.BookData;
import jp.gr.java_conf.nuranimation.my_bookshelf.book.BooksListViewAdapter;

@SuppressWarnings({"WeakerAccess","unused","UnusedReturnValue"})
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


    private static final String DROP_TABLE_SHELF        = "drop table " + TABLE_SHELF_BOOKS + ";";
    private static final String DROP_TABLE_AUTHOR       = "drop table " + TABLE_AUTHORS + ";";
    private static final String DROP_TABLE_SEARCH_BOOKS = "drop table " + TABLE_SEARCH_BOOKS + ";";
    private static final String DROP_TABLE_NEW_BOOKS    = "drop table " + TABLE_NEW_BOOKS + ";";


    private static final String CREATE_TABLE_SHELF = "create table " + TABLE_SHELF_BOOKS + " ("
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
    private static final String CREATE_TABLE_AUTHOR = "create table " + TABLE_AUTHORS + " ("
            + "_id integer primary key" // id
            + ", " + KEY_AUTHOR + " text"  // 著者
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
        db.execSQL(CREATE_TABLE_SHELF);
        db.execSQL(CREATE_TABLE_AUTHOR);
        db.execSQL(CREATE_TABLE_SEARCH_BOOKS);
        db.execSQL(CREATE_TABLE_NEW_BOOKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_SHELF);
        db.execSQL(DROP_TABLE_AUTHOR);
        db.execSQL(DROP_TABLE_SEARCH_BOOKS);
        db.execSQL(DROP_TABLE_NEW_BOOKS);
        onCreate(db);
    }

    public boolean deleteTABLE_AUTHORS() {
        boolean isSuccess = false;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(DROP_TABLE_AUTHOR);
            db.execSQL(CREATE_TABLE_AUTHOR);
            isSuccess = true;
        } catch (SQLException e) {
            if (D) Log.d(TAG, "SQLException" + e);
        }
        return isSuccess;
    }

    public List<String> getAuthors() {
        List<String> authors = new ArrayList<>();
        Cursor c = null;
        try {
            List<String> tmp = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            String sql = "select * from " + TABLE_AUTHORS + " order by " + KEY_AUTHOR + " asc" + ";";
            c = db.rawQuery(sql, null);
            boolean mov = c.moveToFirst();
            while (mov) {
                tmp.add(c.getString(c.getColumnIndex(KEY_AUTHOR)));
                mov = c.moveToNext();
            }
            authors = new ArrayList<>(tmp);
        } catch (SQLException e) {
            if (D) Log.d(TAG, "SQLException");
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return authors;
    }

    public boolean registerToAuthors(String author){
        boolean isSuccess = false;
        if(TextUtils.isEmpty(author)){
            if (D) Log.d(TAG, "empty author");
            return false;
        }
        Cursor c = null;
        try{
            author = author.replaceAll("[\\x20\\u3000]",""); // replace Half and Full width space
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "select * from " + TABLE_AUTHORS + " where " + KEY_AUTHOR + " = ?;";
            c = db.rawQuery(sql, new String[]{author});
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
            isSuccess = true;
        } catch (SQLException e) {
            if (D) Log.d(TAG, "SQLException");
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return isSuccess;
    }

    public boolean registerToAuthors(List<String> authors) {
        boolean isSuccess = false;
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        for (String author : authors) {
            isSuccess = registerToAuthors(author);
            if(!isSuccess){
                break;
            }
        }
        if (isSuccess) {
            db.setTransactionSuccessful();
        }
        db.endTransaction();
        return isSuccess;
    }




    public boolean deleteTABLE_SHELF_BOOKS(){
        boolean isSuccess = false;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(DROP_TABLE_SHELF);
            db.execSQL(CREATE_TABLE_SHELF);
            isSuccess = true;
        }catch (SQLException e){
            if(D) Log.d(TAG,"SQLException" + e);
        }
        return isSuccess;
    }

    public List<BookData> getShelfBooks(String word) {
        List<BookData> books = new ArrayList<>();
        String where = "";
        if (!TextUtils.isEmpty(word)) {
            where = " where "
                    + KEY_TITLE + " like " + "'%" + word + "%'" + " or "
                    + KEY_AUTHOR + " like " + "'%" + word + "%'" + " or "
                    + KEY_ISBN + " = " + "'" + word + "'";
        }
        String order = mApplicationData.getShelfBooksSortSetting();
        Cursor c = null;
        try {
            List<BookData> tmp = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            String sql = "select * from " + TABLE_SHELF_BOOKS + where + order + ";";
            c = db.rawQuery(sql, null);
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
                tmp.add(book);
                mov = c.moveToNext();
            }
            books = new ArrayList<>(tmp);
        } catch (SQLException e) {
            if (D) Log.d(TAG, "SQLException");
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return books;
    }

    public BookData searchInShelfBooks(String ISBN){
        BookData book = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + TABLE_SHELF_BOOKS + " where " + KEY_ISBN + " = ?;";
        Cursor c = db.rawQuery(sql, new String[]{ISBN});
        boolean mov = c.moveToFirst();
        if (mov) {
            book = new BookData();
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
        }
        c.close();
        return book;
    }

    public boolean registerToShelfBooks(BookData book) {
        boolean isSuccess = false;
        if (TextUtils.isEmpty(book.getISBN())) {
            if (D) Log.d(TAG, "No ISBN");
            return false;
        }
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
        Cursor c = null;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "select * from " + TABLE_SHELF_BOOKS + " where " + KEY_ISBN + " = ?;";
            c = db.rawQuery(sql, new String[]{book.getISBN()});
            boolean mov = c.moveToFirst();
            if (mov) {
                if (D) Log.d(TAG, "update Books: " + book.getTitle());
                String sql_ISBN = KEY_ISBN + " = ?";
                db.update(TABLE_SHELF_BOOKS, insertValues, sql_ISBN, new String[]{book.getISBN()});
            } else {
                if (D) Log.d(TAG, "register Books: " + book.getTitle());
                db.insert(TABLE_SHELF_BOOKS, "", insertValues);
            }
            isSuccess = true;
        } catch (SQLException e) {
            if (D) Log.d(TAG, "SQLException");
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return isSuccess;
    }

    public boolean registerToShelfBooks(List<BookData> books) {
        boolean isSuccess = false;
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        for (BookData book : books) {
            isSuccess = registerToShelfBooks(book);
            if (!isSuccess) {
                break;
            }
        }
        if (isSuccess) {
            db.setTransactionSuccessful();
        }
        db.endTransaction();
        return isSuccess;
    }

    public boolean deleteFromShelfBooks(String ISBN) {
        boolean isSuccess = false;
        if (TextUtils.isEmpty(ISBN)) {
            if (D) Log.d(TAG, "No ISBN");
            return false;
        }
        Cursor c = null;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "select * from " + TABLE_SHELF_BOOKS + " where " + KEY_ISBN + " = ?;";
            c = db.rawQuery(sql, new String[]{ISBN});
            boolean mov = c.moveToFirst();
            if (mov) {
                if (D) Log.d(TAG, "delete Books ISBN: " + ISBN);
                String sql_ISBN = KEY_ISBN + " = ?";
                db.delete(TABLE_SHELF_BOOKS, sql_ISBN, new String[]{ISBN});
            } else {
                if (D) Log.d(TAG, "not found Books ISBN: " + ISBN);
            }
            isSuccess = true;
        } catch (SQLException e) {
            if (D) Log.d(TAG, "SQLException");
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return isSuccess;
    }




    public boolean deleteTABLE_SEARCH_BOOKS(){
        boolean isSuccess = false;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(DROP_TABLE_SEARCH_BOOKS);
            db.execSQL(CREATE_TABLE_SEARCH_BOOKS);
            isSuccess = true;
        } catch (SQLException e) {
            if (D) Log.d(TAG, "SQLException" + e);
        }
        return isSuccess;
    }

    public List<BookData> getSearchBooks(){
        List<BookData> books = new ArrayList<>();
        Cursor c = null;
        try {
            List<BookData> tmp = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            String sql = "select * from " + TABLE_SEARCH_BOOKS + ";";
            c = db.rawQuery(sql, null);
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
                tmp.add(book);
                mov = c.moveToNext();
            }
            books = new ArrayList<>(tmp);
        } catch (SQLException e) {
            if (D) Log.d(TAG, "SQLException");
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return books;
    }

    public boolean registerToSearchBooks(BookData book) {
        boolean isSuccess = false;
        if (TextUtils.isEmpty(book.getISBN())) {
            if (D) Log.d(TAG, "No ISBN");
            return false;
        }

        Cursor c = null;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "select * from " + TABLE_SEARCH_BOOKS + " where " + KEY_ISBN + " = ?;";
            c = db.rawQuery(sql, new String[]{book.getISBN()});
            boolean mov = c.moveToFirst();
            if (mov) {
                if (D) Log.d(TAG, "already registered : " + book.getTitle());
            } else {
                if (D) Log.d(TAG, "register Books: " + book.getTitle());
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
                db.insert(TABLE_SEARCH_BOOKS, "", insertValues);
            }
            isSuccess = true;
        } catch (SQLException e) {
            if (D) Log.d(TAG, "SQLException");
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return isSuccess;
    }

    public boolean registerToSearchBooks(List<BookData> books) {
        boolean isSuccess = false;
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        for (BookData book : books) {
            isSuccess = registerToSearchBooks(book);
            if (!isSuccess) {
                break;
            }
        }
        if (isSuccess) {
            db.setTransactionSuccessful();
        }
        db.endTransaction();
        return isSuccess;
    }




    public boolean deleteTABLE_NEW_BOOKS(){
        boolean isSuccess = false;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(DROP_TABLE_NEW_BOOKS);
            db.execSQL(CREATE_TABLE_NEW_BOOKS);
            isSuccess = true;
        } catch (SQLException e) {
            if (D) Log.d(TAG, "SQLException");
            e.printStackTrace();
        }
        return isSuccess;
    }

    public List<BookData> getNewBooks(){
        List<BookData> books = new ArrayList<>();
        Cursor c = null;
        try {
            List<BookData> tmp = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();
            String sql = "select * from " + TABLE_NEW_BOOKS + " order by " + KEY_RELEASE_DATE + " desc" + ";";
            c = db.rawQuery(sql, null);
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
                tmp.add(book);
                mov = c.moveToNext();
            }
            books = new ArrayList<>(tmp);
        } catch (SQLException e) {
            if (D) Log.d(TAG, "SQLException");
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return books;
    }

    public boolean registerToNewBooks(BookData book) {
        boolean isSuccess = false;
        if (TextUtils.isEmpty(book.getISBN())) {
            if (D) Log.d(TAG, "No ISBN");
            return false;
        }
        Cursor c = null;
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "select * from " + TABLE_NEW_BOOKS + " where " + KEY_ISBN + " = ?;";
            c = db.rawQuery(sql, new String[]{book.getISBN()});
            boolean mov = c.moveToFirst();
            if (mov) {
                if (D) Log.d(TAG, "already registered : " + book.getTitle());
            } else {
                if (D) Log.d(TAG, "register Books: " + book.getTitle());
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
                db.insert(TABLE_SHELF_BOOKS, "", insertValues);
            }
            isSuccess = true;
        } catch (SQLException e) {
            if (D) Log.d(TAG, "SQLException");
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return isSuccess;
    }

    public boolean registerToNewBooks(List<BookData> books) {
        boolean isSuccess = false;
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        for (BookData book : books) {
            isSuccess = registerToNewBooks(book);
            if (!isSuccess) {
                break;
            }
        }
        if (isSuccess) {
            db.setTransactionSuccessful();
        }
        db.endTransaction();
        return isSuccess;
    }





}
