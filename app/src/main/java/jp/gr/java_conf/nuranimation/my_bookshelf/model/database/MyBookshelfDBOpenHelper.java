package jp.gr.java_conf.nuranimation.my_bookshelf.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookData;


@SuppressWarnings({"unused"})
public class MyBookshelfDBOpenHelper extends SQLiteOpenHelper {
    public static final String TAG = MyBookshelfDBOpenHelper.class.getSimpleName();
    private static final boolean D = true;

    private static final String DB_NAME = "jp.gr.java_conf.nuranimation.MyBookshelf.db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_SHELF_BOOKS = "shelf_books";
    private static final String TABLE_AUTHORS = "authors";
    private static final String TABLE_SEARCH_BOOKS = "search_books";
    private static final String TABLE_NEW_BOOKS = "new_books";

    private static final String KEY_ISBN = "isbn";
    private static final String KEY_TITLE = "title";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_PUBLISHER = "publisher_name";
    private static final String KEY_RELEASE_DATE = "release_date";
    private static final String KEY_PRICE = "price";
    private static final String KEY_RAKUTEN_URL = "rakuten_url";
    private static final String KEY_IMAGES = "images";
    private static final String KEY_RATING = "rating";
    private static final String KEY_READ_STATUS = "read_status";
    private static final String KEY_TAGS = "tags";
    private static final String KEY_FINISH_READ_DATE = "finish_read_date";
    private static final String KEY_REGISTER_DATE = "register_date";

    private static final String CREATE_TABLE_AUTHOR = "create table " + TABLE_AUTHORS + " ("
            + "_id integer primary key" // id
            + ", " + KEY_AUTHOR + " text"  // author
            + ");";
    private static final String CREATE_TABLE_SHELF_BOOKS = "create table " + TABLE_SHELF_BOOKS + " ("
            + "_id integer primary key"               // id
            + ", " + KEY_ISBN + " text"                 // ISBN
            + ", " + KEY_TITLE + " text"                // title
            + ", " + KEY_AUTHOR + " text"               // author
            + ", " + KEY_PUBLISHER + " text"            // publisher
            + ", " + KEY_RELEASE_DATE + " text"         // sales date
            + ", " + KEY_PRICE + " text"                // price
            + ", " + KEY_RAKUTEN_URL + " text"          // rakuten url
            + ", " + KEY_IMAGES + " text"               // image url
            + ", " + KEY_RATING + " text"               // rating
            + ", " + KEY_READ_STATUS + " text"          // read status
            + ", " + KEY_TAGS + " text"                 // tag
            + ", " + KEY_FINISH_READ_DATE + " text"     // finish read date
            + ", " + KEY_REGISTER_DATE + " text"        // register date
            + ");";

    private static final String CREATE_TABLE_SEARCH_BOOKS = "create table " + TABLE_SEARCH_BOOKS + " ("
            + "_id integer primary key"               // id
            + ", " + KEY_ISBN + " text"                 // ISBN
            + ", " + KEY_TITLE + " text"                // title
            + ", " + KEY_AUTHOR + " text"               // author
            + ", " + KEY_PUBLISHER + " text"            // publisher
            + ", " + KEY_RELEASE_DATE + " text"         // sales date
            + ", " + KEY_PRICE + " text"                // price
            + ", " + KEY_RAKUTEN_URL + " text"          // rakuten url
            + ", " + KEY_IMAGES + " text"               // image url
            + ", " + KEY_RATING + " text"               // rating
            + ", " + KEY_READ_STATUS + " text"          // read status
            + ", " + KEY_TAGS + " text"                 // tag
            + ", " + KEY_FINISH_READ_DATE + " text"     // finish read date
            + ", " + KEY_REGISTER_DATE + " text"        // register date
            + ");";

    private static final String CREATE_TABLE_NEW_BOOKS = "create table " + TABLE_NEW_BOOKS + " ("
            + "_id integer primary key"               // id
            + ", " + KEY_ISBN + " text"                 // ISBN
            + ", " + KEY_TITLE + " text"                // title
            + ", " + KEY_AUTHOR + " text"               // author
            + ", " + KEY_PUBLISHER + " text"            // publisher
            + ", " + KEY_RELEASE_DATE + " text"         // sales date
            + ", " + KEY_PRICE + " text"                // price
            + ", " + KEY_RAKUTEN_URL + " text"          // rakuten url
            + ", " + KEY_IMAGES + " text"               // image url
            + ", " + KEY_RATING + " text"               // rating
            + ", " + KEY_READ_STATUS + " text"          // read status
            + ", " + KEY_TAGS + " text"                 // tag
            + ", " + KEY_FINISH_READ_DATE + " text"     // finish read date
            + ", " + KEY_REGISTER_DATE + " text"        // register date
            + ");";

    private static final String DROP_TABLE_TABLE_AUTHOR = "drop table if exists " + TABLE_AUTHORS + ";";
    private static final String DROP_TABLE_SHELF_BOOKS = "drop table if exists " + TABLE_SHELF_BOOKS + ";";
    private static final String DROP_TABLE_SEARCH_BOOKS = "drop table if exists " + TABLE_SEARCH_BOOKS + ";";
    private static final String DROP_TABLE_NEW_BOOKS = "drop table if exists " + TABLE_NEW_BOOKS + ";";

    private static final Object mLock = new Object();
    private static MyBookshelfDBOpenHelper mInstance;

    @NonNull
    public static MyBookshelfDBOpenHelper getInstance(@NonNull Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new MyBookshelfDBOpenHelper(context.getApplicationContext());
            }
            return mInstance;
        }
    }

    private MyBookshelfDBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
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
        // UPDATE
    }


    private void deleteAllData(final String table) {
        SQLiteDatabase db = this.getWritableDatabase();
        switch (table) {
            case TABLE_AUTHORS:
                db.delete(TABLE_AUTHORS, null, null);
                break;
            case TABLE_SHELF_BOOKS:
                db.delete(TABLE_SHELF_BOOKS, null, null);
                break;
            case TABLE_SEARCH_BOOKS:
                db.delete(TABLE_SEARCH_BOOKS, null, null);
                break;
            case TABLE_NEW_BOOKS:
                db.delete(TABLE_NEW_BOOKS, null, null);
                break;
        }
        db.close();
    }


    private void dropTable(final String table) {
        SQLiteDatabase db = this.getWritableDatabase();
        switch (table) {
            case TABLE_AUTHORS:
                db.execSQL(DROP_TABLE_TABLE_AUTHOR);
                db.execSQL(CREATE_TABLE_AUTHOR);
                break;
            case TABLE_SHELF_BOOKS:
                db.execSQL(DROP_TABLE_SHELF_BOOKS);
                db.execSQL(CREATE_TABLE_SHELF_BOOKS);
                break;
            case TABLE_SEARCH_BOOKS:
                db.execSQL(DROP_TABLE_SEARCH_BOOKS);
                db.execSQL(CREATE_TABLE_SEARCH_BOOKS);
                break;
            case TABLE_NEW_BOOKS:
                db.execSQL(DROP_TABLE_NEW_BOOKS);
                db.execSQL(CREATE_TABLE_NEW_BOOKS);
                break;
        }
        db.close();
    }

    private void registerBook(final String table, final BookData book) {
        if (book != null && !TextUtils.isEmpty(book.getISBN())) {
            ContentValues insertValues = new ContentValues();
            insertValues.put(KEY_ISBN, book.getISBN());                         // ISBN
            insertValues.put(KEY_TITLE, book.getTitle());                       // title
            insertValues.put(KEY_AUTHOR, book.getAuthor());                     // author
            insertValues.put(KEY_PUBLISHER, book.getPublisher());               // publisher
            insertValues.put(KEY_RELEASE_DATE, book.getSalesDate());            // sales date
            insertValues.put(KEY_PRICE, book.getItemPrice());                   // price
            insertValues.put(KEY_RAKUTEN_URL, book.getRakutenUrl());            // rakuten url
            insertValues.put(KEY_IMAGES, book.getImage());                      // image url
            insertValues.put(KEY_RATING, book.getRating());                     // rating
            insertValues.put(KEY_READ_STATUS, book.getReadStatus());            // read status
            insertValues.put(KEY_TAGS, book.getTags());                         // tag
            insertValues.put(KEY_FINISH_READ_DATE, book.getFinishReadDate());   // finish read date
            insertValues.put(KEY_REGISTER_DATE, book.getRegisterDate());        // register date

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
            db.close();
        }
    }

    private void registerBooks(final String table, final List<BookData> books) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();

        for (BookData book : books) {
            if (book != null && !TextUtils.isEmpty(book.getISBN())) {
                ContentValues insertValues = new ContentValues();
                insertValues.put(KEY_ISBN, book.getISBN());                         // ISBN
                insertValues.put(KEY_TITLE, book.getTitle());                       // title
                insertValues.put(KEY_AUTHOR, book.getAuthor());                     // author
                insertValues.put(KEY_PUBLISHER, book.getPublisher());               // publisher
                insertValues.put(KEY_RELEASE_DATE, book.getSalesDate());            // sales date
                insertValues.put(KEY_PRICE, book.getItemPrice());                   // price
                insertValues.put(KEY_RAKUTEN_URL, book.getRakutenUrl());            // rakuten url
                insertValues.put(KEY_IMAGES, book.getImage());                      // image url
                insertValues.put(KEY_RATING, book.getRating());                     // rating
                insertValues.put(KEY_READ_STATUS, book.getReadStatus());            // read status
                insertValues.put(KEY_TAGS, book.getTags());                         // tag
                insertValues.put(KEY_FINISH_READ_DATE, book.getFinishReadDate());   // finish read date
                insertValues.put(KEY_REGISTER_DATE, book.getRegisterDate());        // register date


//                db.replace(table, "", insertValues);

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
        db.close();
    }

    private void unregisterBook(final String table, final BookData book) {
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
            db.close();
        }
    }

    private BookData loadBookData(String table, BookData book) {
        BookData result = new BookData();
        result.setView_type(BookData.TYPE_EMPTY);
        if (book == null || TextUtils.isEmpty(book.getISBN())) {
            return result;
        }
        String ISBN = book.getISBN();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + table + " where " + KEY_ISBN + " = ?;";
        Cursor c = db.rawQuery(sql, new String[]{ISBN});
        boolean mov = c.moveToFirst();
        if (mov) {
            result.setView_type(BookData.TYPE_BOOK);
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
        db.close();
        return result;
    }

    private List<BookData> loadBooks(final String table, final String keyword, final String order) {
        List<BookData> books = new ArrayList<>(1000);
        String sql_where = "";
        if (!TextUtils.isEmpty(keyword)) {
            sql_where = " where "
                    + KEY_TITLE + " like " + "'%" + keyword + "%'" + " or "
                    + KEY_AUTHOR + " like " + "'%" + keyword + "%'" + " or "
                    + KEY_ISBN + " = " + "'" + keyword + "'";
        }
        String sql_order = "";
        if (!TextUtils.isEmpty(order)) {
            sql_order = order;
        }

        String sql = "select * from " + table + sql_where + sql_order + ";";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);
        boolean mov = c.moveToFirst();
        while (mov) {
            BookData book = new BookData();
            book.setView_type(BookData.TYPE_BOOK);
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
        db.close();
        return books;
    }


    /* --- Authors List --- */
    public void clearAuthorsList() {
        deleteAllData(TABLE_AUTHORS);
    }

    public boolean containsAuthor(String author) {
        boolean contains = false;
        if (!TextUtils.isEmpty(author)) {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "select * from " + TABLE_AUTHORS + " where " + KEY_AUTHOR + " = ?;";
            Cursor c = db.rawQuery(sql, new String[]{author});
            boolean mov = c.moveToFirst();
            if (mov) {
                if (D) Log.d(TAG, "already registered: " + author);
                contains = true;
            }
            c.close();
            db.close();
        }
        return contains;
    }


    public void unregisterAuthor(String author) {
        if (!TextUtils.isEmpty(author)) {
            SQLiteDatabase db = this.getWritableDatabase();
            String sql = "select * from " + TABLE_AUTHORS + " where " + KEY_AUTHOR + " = ?;";
            Cursor c = db.rawQuery(sql, new String[]{author});
            boolean mov = c.moveToFirst();
            if (mov) {
                if (D) Log.d(TAG, "delete Author: " + author);
                String sql_Author = KEY_AUTHOR + " = ?";
                db.delete(TABLE_AUTHORS, sql_Author, new String[]{author});
            }
            c.close();
            db.close();
        }
    }

    public void registerToAuthorsList(String author) {
        if (!TextUtils.isEmpty(author)) {
//            author = author.replaceAll("[\\x20\\u3000]", ""); // replace Half and Full width space
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
            db.close();
        }
    }

    public void registerToAuthorsList(List<String> authors) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        for (String author : authors) {
            if (!TextUtils.isEmpty(author)) {
//                author = author.replaceAll("[\\x20\\u3000]", ""); // replace Half and Full width space
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
        db.close();
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
        db.close();
        return authors;
    }


    /* --- ShelfBooks --- */
    public void clearShelfBooks(){
        deleteAllData(TABLE_SHELF_BOOKS);
    }

    public void registerToShelfBooks(BookData book) {
        registerBook(TABLE_SHELF_BOOKS, book);
    }

    public void registerToShelfBooks(List<BookData> books) {
        registerBooks(TABLE_SHELF_BOOKS, books);
    }

    public List<BookData> loadShelfBooks(String keyword, String order) {
        return loadBooks(TABLE_SHELF_BOOKS, keyword, order);
    }

    public BookData loadBookDataFromShelfBooks(BookData book){
        return loadBookData(TABLE_SHELF_BOOKS, book);
    }

    public void unregisterFromShelfBooks(BookData book) {
        unregisterBook(TABLE_SHELF_BOOKS, book);
    }


    /* --- SearchBooks --- */
    public void clearSearchBooks() {
        deleteAllData(TABLE_SEARCH_BOOKS);
    }

    public void registerToSearchBooks(BookData book) {
        registerBook(TABLE_SEARCH_BOOKS, book);
    }

    public void registerToSearchBooks(List<BookData> books) {
        registerBooks(TABLE_SEARCH_BOOKS, books);
    }

    public List<BookData> loadSearchBooks(){
        return loadBooks(TABLE_SEARCH_BOOKS, null, null);
    }

    public BookData loadBookDataFromSearchBooks(BookData book){
        return loadBookData(TABLE_SEARCH_BOOKS, book);
    }

    public void unregisterFromSearchBooks(BookData book) {
        unregisterBook(TABLE_SEARCH_BOOKS, book);
    }


    /* --- NewBooks --- */
    public void clearNewBooks(){
        deleteAllData(TABLE_NEW_BOOKS);
    }

    public void registerToNewBooks(BookData book) {
        registerBook(TABLE_NEW_BOOKS, book);
    }

    public void registerToNewBooks(List<BookData> books) {
        registerBooks(TABLE_NEW_BOOKS, books);
    }

    public List<BookData> loadNewBooks(){
        String order = " order by " + KEY_RELEASE_DATE + " desc";
        return loadBooks(TABLE_NEW_BOOKS, null, order);
    }

    public BookData loadBookDataFromNewBooks(BookData book){
        return loadBookData(TABLE_NEW_BOOKS, book);
    }

    public void unregisterFromNewBooks(BookData book) {
        unregisterBook(TABLE_NEW_BOOKS, book);
    }


}
