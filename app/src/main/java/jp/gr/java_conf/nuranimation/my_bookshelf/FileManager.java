package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FileManager {
    private static final boolean D = true;
    private static final String TAG = FileManager.class.getSimpleName();

    static final String Dropbox_App_DirectoryPath = "/MyBookshelf/";
    static final String Application_DirectoryPath = "/Android/data/jp.gr.java_conf.nuranimation.my_bookshelf/";
    static final String FileName_Bookshelf = "backup_bookshelf.csv";
    static final String FileName_Authors = "backup_authors.csv";

    private MyBookshelfApplicationData mData;
    private Handler mHandler;

    FileManager(Context context){
        mData = (MyBookshelfApplicationData) context.getApplicationContext();
    }

    void setHandler(Handler handler){
        mHandler = handler;
    }

    int export_csv(){
        int error = ErrorStatus.No_Error;
        int count = 0;
        MyBookshelfDBOpenHelper helper = mData.getDatabaseHelper();

        File extDir = Environment.getExternalStorageDirectory();
        String dirPath = extDir.getPath() + Application_DirectoryPath;
        File dir = new File(dirPath);
        if(!dir.exists()){
            boolean success = dir.mkdirs();
            if(D) Log.d(TAG,"mkdirs(): " + success);
        }

        try {
            List<BookData> books = helper.getMyBookshelf();
            int recodeCount = books.size();
            if(D) Log.d(TAG,"recodeCount : " + recodeCount);

            File file_bookshelf = new File(dirPath + "alt_" + FileName_Bookshelf);
            OutputStream os_bookshelf = new FileOutputStream(file_bookshelf);
            OutputStreamWriter osr_bookshelf = new OutputStreamWriter(os_bookshelf, Charset.forName("UTF-8"));
            BufferedWriter bw_bookshelf = new BufferedWriter(osr_bookshelf);

            String idx = "isbn,title,author\r\n";
            bw_bookshelf.write(idx);
            for(BookData book : books){
                String isbn = book.getIsbn();
                String title = book.getTitle();
                String author = book.getAuthor();
                String str_book = isbn + "," + title + "," + author + "\r\n";
                bw_bookshelf.write(str_book);
                count++;
                String progress = count + "/" + recodeCount;
                mHandler.obtainMessage(BaseFragment.MESSAGE_PROGRESS, -1, -1, progress).sendToTarget();
            }
            bw_bookshelf.close();

            List<String> authors = helper.getAuthors();
            recodeCount = authors.size();
            if(D) Log.d(TAG,"recodeCount : " + recodeCount);
            count = 0;

            File file_authors = new File(dirPath + FileName_Authors);
            OutputStream os_authors = new FileOutputStream(file_authors);
            OutputStreamWriter osr_authors = new OutputStreamWriter(os_authors, Charset.forName("UTF-8"));
            BufferedWriter bw_authors = new BufferedWriter(osr_authors);

            for(String author : authors) {
                bw_authors.write(author+ "\r\n");
                count++;
                String progress = count + "/" + recodeCount;
                mHandler.obtainMessage(FragmentSettings.MESSAGE_PROGRESS, -1, -1, progress).sendToTarget();
            }
            bw_authors.close();
        } catch (IOException e){
            if (D) Log.d(TAG, "IO Exception");
            error = ErrorStatus.Error_IO_Error;
        }
        return error;
    }

    int import_csv() {
        int size = 0;
        int count = 0;
        int error = ErrorStatus.No_Error;

        File extDir = Environment.getExternalStorageDirectory();
        String dirPath = extDir.getPath() + Application_DirectoryPath;
        File file_bookshelf = new File(dirPath + FileName_Bookshelf);
        if (!file_bookshelf.exists()){
            if (D) Log.d(TAG, "file_bookshelf not found");
            error = ErrorStatus.Error_File_Bookshelf_not_found;
            return error;
        }

        File file_authors = new File(dirPath + FileName_Authors);
        if (!file_authors.exists()){
            if (D) Log.d(TAG, "file_authors not found");
            error = ErrorStatus.Error_File_Authors_not_found;
            return error;
        }

        MyBookshelfDBOpenHelper helper = mData.getDatabaseHelper();
        helper.getWritableDatabase().beginTransaction();

        try {
            // insert BookData from CSV
            helper.deleteTABLE_SHELF();
            // count line
            InputStream pre_is_bookshelf = getStreamSkipBOM(new FileInputStream(file_bookshelf),Charset.forName("UTF-8"));
            InputStreamReader pre_isr_bookshelf = new InputStreamReader(pre_is_bookshelf, Charset.forName("UTF-8"));
            BufferedReader pre_br_bookshelf = new BufferedReader(pre_isr_bookshelf);
            pre_br_bookshelf.readLine(); // skip first line
            while ((pre_br_bookshelf.readLine()) != null) {
                size++;
            }
            pre_br_bookshelf.close();
            if (D) Log.d(TAG, "size: " + size);
            // import csv
            InputStream is_bookshelf = getStreamSkipBOM(new FileInputStream(file_bookshelf),Charset.forName("UTF-8"));
            InputStreamReader isr_bookshelf = new InputStreamReader(is_bookshelf, Charset.forName("UTF-8"));
            BufferedReader br_bookshelf = new BufferedReader(isr_bookshelf);
            String str_line_bookshelf = br_bookshelf.readLine();
            String[] idx_bookshelf = str_line_bookshelf.split(",");

            if(idx_bookshelf.length == 40){
                while ((str_line_bookshelf = br_bookshelf.readLine()) != null) {
                    if (D) Log.d(TAG, "str: " + str_line_bookshelf);
                    String[] split = splitLineWithComma(str_line_bookshelf);
                    BookData book = convertReadeeToBookData(split);
                    helper.registerBook(book);
                    count++;
                    String progress = count + "/" + size;
                    mHandler.obtainMessage(FragmentSettings.MESSAGE_PROGRESS, -1, -1, progress).sendToTarget();
                }
            }
            if(idx_bookshelf.length == 20){
                while ((str_line_bookshelf = br_bookshelf.readLine()) != null) {
                    if (D) Log.d(TAG, "str: " + str_line_bookshelf);
                    String[] split = splitLineWithComma(str_line_bookshelf);
                    import_MYBOOKSHELF_CSV(split);
                    count++;
                    String progress = count + "/" + size;
                    mHandler.obtainMessage(FragmentSettings.MESSAGE_PROGRESS, -1, -1, progress).sendToTarget();
                }
            }
            br_bookshelf.close();

            size = 0;
            count = 0;
            // insert Author from CSV file
            helper.deleteTABLE_AUTHOR();
            // count line
            InputStream pre_is_authors = new FileInputStream(file_authors);
            InputStreamReader pre_isr_authors = new InputStreamReader(pre_is_authors);
            BufferedReader pre_br_authors = new BufferedReader(pre_isr_authors);
            while ((pre_br_authors.readLine()) != null) {
                size++;
            }
            pre_br_authors.close();
            if (D) Log.d(TAG, "size: " + size);
            // import csv
            InputStream is_authors = new FileInputStream(file_authors);
            InputStreamReader isr_authors = new InputStreamReader(is_authors);
            BufferedReader br_authors = new BufferedReader(isr_authors);
            String str_line_authors;
            while ((str_line_authors = br_authors.readLine()) != null) {
                helper.registerAuthor(str_line_authors);
                count++;
                String progress = count + "/" + size;
                mHandler.obtainMessage(FragmentSettings.MESSAGE_PROGRESS, -1, -1, progress).sendToTarget();
            }
            br_authors.close();

            helper.getWritableDatabase().setTransactionSuccessful();
            mData.updateList_MyBookshelf();
        } catch (FileNotFoundException e) {
            if(D) Log.e(TAG,"Error");
            error = ErrorStatus.Error_File_not_found;
        } catch (IOException e) {
            if(D) Log.e(TAG,"Error");
            error = ErrorStatus.Error_IO_Error;
        } finally {
            helper.getWritableDatabase().endTransaction();
        }
        return error;
    }


    private void import_MYBOOKSHELF_CSV(String[] split){
        if(D) Log.d(TAG,"isbn: " + split[0]);
    }


    private BookData convertReadeeToBookData (String[] split){
        BookData bookData = new BookData();
        bookData.setIsbn(split[1]);
        bookData.setTitle(split[3]);
        bookData.setAuthor(split[8]);
        bookData.setPublisher(split[9]);
        bookData.setSalesDate(convertSalesDate(split[11]));
        bookData.setItemPrice(split[12]);
        bookData.setRakutenUrl(split[13]);
        bookData.setImage(split[17]);
        bookData.setRating(split[18]);
        bookData.setReadStatus(split[19]);
        bookData.setTags(split[23]);
        bookData.setFinishReadDate(split[26]);
        bookData.setRegisterDate(split[39]);
        return bookData;
    }



    private String convertSalesDate(String date) {
        String[] split = date.split("/");
        if(split.length == 3) {
            return String.format(Locale.JAPAN, "%s年%s月%s日", split[0], split[1], split[2]);
        }
        return date;
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



    private InputStream getStreamSkipBOM(InputStream is, Charset charSet) throws IOException{
        if( !(charSet == Charset.forName("UTF-8")) ){
            return is;
        }
        if( !is.markSupported() ){
            is = new BufferedInputStream(is);
        }
        is.mark(3);
        if( is.available() >= 3 ){
            byte b[] = {0,0,0};
            int bytes = is.read(b,0,3);
            if(bytes == 3 &&  b[0]!=(byte)0xEF  || b[1]!=(byte)0xBB || b[2]!= (byte)0xBF ){
                is.reset();
            }
        }
        return is;
    }

}
