package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FileManager {
    private static final boolean D = true;
    private static final String TAG = FileManager.class.getSimpleName();

    private Context mContext;
    private Handler mHandler;

    FileManager(Context context){
        mContext = context;
    }

    void setHandler(Handler handler){
        mHandler = handler;
    }

    boolean export_csv(String dirPath, String filename){
        MyBookshelfDBOpenHelper helper = new MyBookshelfDBOpenHelper(mContext.getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();

        long recodeCount = DatabaseUtils.queryNumEntries(db, "MY_BOOKSHELF");
        Log.d(TAG, "recodeCount : " + recodeCount);

        long count = 0;

        File dir = new File(dirPath);
        if(!dir.exists()){
            boolean success = dir.mkdirs();
            if(D) Log.d(TAG,"mkdirs(): " + success);
        }
        try {
            FileWriter fileWriter = new FileWriter(dirPath+"2"+filename,false);

            String sql = "SELECT * FROM MY_BOOKSHELF";

            Cursor c = db.rawQuery(sql,null);
            boolean mov = c.moveToFirst();

            while(mov){
                String isbn  = c.getString(c.getColumnIndex("isbn"));
                String title = c.getString(c.getColumnIndex("title"));
                String str = isbn + "," + title + "\r\n";
                fileWriter.write(str);

                count++;
                String progress = count + "/" + recodeCount;
                mHandler.obtainMessage(SettingsFragment.MESSAGE_PROGRESS, -1, -1, progress).sendToTarget();

                mov = c.moveToNext();
            }
            c.close();

            fileWriter.close();
        } catch (IOException e){
            return false;
        }
        return true;
    }

    boolean import_csv(String dirPath, String filename) {
        boolean isSuccess = false;

        File file = new File(dirPath + filename);
        if (!file.exists()) {
            if (D) Log.d(TAG, "File not found");
            return false;
        }

        MyBookshelfDBOpenHelper helper = new MyBookshelfDBOpenHelper(mContext.getApplicationContext());
        SQLiteDatabase db = helper.getWritableDatabase();
        helper.deleteDB(db);
        db.beginTransaction();
        try {
            int size = 0;
            int count = 0;

            // count line
            InputStream input = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(input);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            bufferedReader.readLine(); // skip first line
            while ((bufferedReader.readLine()) != null) {
                size++;
            }
            bufferedReader.close();
            if (D) Log.d(TAG, "size: " + size);

            // import csv
            InputStream input2 = new FileInputStream(file);
            InputStreamReader inputStreamReader2 = new InputStreamReader(input2);
            BufferedReader bufferedReader2 = new BufferedReader(inputStreamReader2);
            String str = bufferedReader2.readLine();
            String[] index = str.split(",");
            if(index.length == 40){
                while ((str = bufferedReader2.readLine()) != null) {
                    if (D) Log.d(TAG, "str: " + str);
                    String[] split = splitLineWithComma(str);

                    ContentValues insert = import_Readee_CSV(split);
                    helper.insertData(db,insert);
                    count++;
                    String progress = count + "/" + size;
                    mHandler.obtainMessage(SettingsFragment.MESSAGE_PROGRESS, -1, -1, progress).sendToTarget();
                }
            }
            if(index.length == 20){
                while ((str = bufferedReader2.readLine()) != null) {
                    if (D) Log.d(TAG, "str: " + str);
                    String[] split = splitLineWithComma(str);
                    import_MYBOOKSHELF_CSV(split);
                    count++;
                    String progress = count + "/" + size;
                    mHandler.obtainMessage(SettingsFragment.MESSAGE_PROGRESS, -1, -1, progress).sendToTarget();
                }
            }
            bufferedReader2.close();
            db.setTransactionSuccessful();
            isSuccess = true;
        } catch (FileNotFoundException e) {
            if(D) Log.e(TAG,"Error");
        } catch (IOException e) {
            if(D) Log.e(TAG,"Error");
        } catch (Exception e) {
            if (D) Log.e(TAG, "Error");
        } finally {
            db.endTransaction();
        }
        return isSuccess;
    }


    private void import_MYBOOKSHELF_CSV(String[] split){
        if(D) Log.d(TAG,"isbn: " + split[0]);
    }


    private ContentValues import_Readee_CSV(String[] split) {
        ContentValues insertValues = new ContentValues();
        insertValues.put("isbn", split[1]);// ISBN
        insertValues.put("title", split[3]);// タイトル
        insertValues.put("subTitle", split[4]);// サブタイトル
        insertValues.put("seriesName", split[5]);// シリーズ名
        insertValues.put("contents", split[6]);// 多巻物収録内容
        insertValues.put("genreId", split[7]);// ジャンルID
        insertValues.put("author", split[8]); // 著者
        insertValues.put("publisherName", split[9]);// 出版社
        insertValues.put("size", split[10]); // 書籍のサイズ
        insertValues.put("releaseDate", split[11]);// 発売日
        insertValues.put("price", split[12]);// 定価
        insertValues.put("rakutenUrl", split[13]);// URL
        insertValues.put("images", split[17]);// 商品画像URL
        insertValues.put("rating", split[18]); // レーティング
        insertValues.put("readStatus", split[19]);// ステータス
        insertValues.put("tags", split[23]);// タグ
        insertValues.put("memo", split[24]);// メモ
        insertValues.put("finishReadDate", split[26]); // 読了日
        insertValues.put("itemCaption", split[29]);// 商品説明文
        insertValues.put("titleKana", split[30]); // タイトルカナ
        insertValues.put("authorkana", split[31]);// 著者カナ
        insertValues.put("registerDate", split[39]);// 登録日

        return insertValues;
    }



    private static String[] splitLineWithComma(String line) {
        String REGEX_CSV_COMMA = ",(?=(([^\"]*\"){2})*[^\"]*$)";
        String REGEX_SURROUND_DOUBLE_QUOTATION = "^\"|\"$";
        String REGEX_DOUBLE_DOUBLE_QUOTATION = "\"\"";
        String[] arr = null;
        try {
            Pattern cPattern = Pattern.compile(REGEX_CSV_COMMA);
            String[] cols = cPattern.split(line, -1);
            arr = new String[cols.length];

            for (int i = 0, len = cols.length; i < len; i++) {
                String col = cols[i].trim();
                Pattern sdqPattern = Pattern.compile(REGEX_SURROUND_DOUBLE_QUOTATION);
                Matcher matcher = sdqPattern.matcher(col);
                col = matcher.replaceAll("");
                Pattern dqPattern = Pattern.compile(REGEX_DOUBLE_DOUBLE_QUOTATION);
                matcher = dqPattern.matcher(col);
                col = matcher.replaceAll("\"");
                arr[i] = col;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arr;
    }

}
