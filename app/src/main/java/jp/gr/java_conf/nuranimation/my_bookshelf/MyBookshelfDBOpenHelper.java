package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyBookshelfDBOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "jp.gr.java_conf.nuranimation.MyBookshelf.db";
    private static final int DB_VERSION = 1;
    private static final String DROP_TABLE_SHELF = "drop table MY_BOOKSHELF;";

    public MyBookshelfDBOpenHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table MY_BOOKSHELF ("
                + "_id integer primary key" // id
                + ", " + "isbn text"         // ISBN
                + ", " + "title text"           // タイトル
                + ", " + "subTitle text"        // サブタイトル
                + ", " + "seriesName text"      // シリーズ名
                + ", " + "contents text"        // 多巻物収録内容
                + ", " + "genreId integer"      // ジャンルID
                + ", " + "author text"          // 著者
                + ", " + "publisherName text"   // 出版社
                + ", " + "size text"            // 書籍のサイズ
                + ", " + "releaseDate text"     // 発売日
                + ", " + "price integer"        // 定価
                + ", " + "rakutenUrl text"      // URL
                + ", " + "images text"          // 商品画像URL
                + ", " + "rating text"       // レーティング
                + ", " + "readStatus text"   // ステータス
                + ", " + "tags text"            // タグ
                + ", " + "memo text"            // メモ
                + ", " + "finishReadDate text"  // 読了日
                + ", " + "itemCaption text"     // 商品説明文
                + ", " + "titleKana text"       // タイトルカナ
                + ", " + "authorkana text"      // 著者カナ
                + ", " + "registerDate text"   // 登録日
                + ");"
        );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_SHELF);
        onCreate(db);
    }

    public boolean deleteDB(SQLiteDatabase db){
        db.execSQL(DROP_TABLE_SHELF);
        onCreate(db);
        return true;
    }

    public void insertData(SQLiteDatabase db, ContentValues values){
        db.insert("MY_BOOKSHELF",null,values);
    }

}



