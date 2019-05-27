package jp.gr.java_conf.nuranimation.my_bookshelf.application;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jp.gr.java_conf.nuranimation.my_bookshelf.adapter.BooksListViewAdapter;

@SuppressWarnings({"unused","WeakerAccess"})
public class MyBookshelfUtils {
    public static final String TAG = MyBookshelfUtils.class.getSimpleName();
    private static final boolean D = true;


    public static String getParam(JSONObject json, String keyword){
        try {
            if (json.has(keyword)) {
                String param = json.getString(keyword);
                if(D) Log.d(TAG,keyword + ": " + param);
                return param;
            }
        }catch (JSONException e){
            if(D) Log.d(TAG,"JSONException");
            return "";
        }
        return "";
    }

    public static boolean isValid(String word){
        if(TextUtils.isEmpty(word)){
            if(D) Log.d(TAG,"No word");
            return false;
        }
        if(word.length() >= 2){
            if(D) Log.d(TAG,"over 2characters. OK");
            return true;
        }

        int bytes = 0;
        char[] array = word.toCharArray();
        for(char c: array){
            if(D) Log.d(TAG,"Unicode Block: " + Character.UnicodeBlock.of(c));
            if(String.valueOf(c).getBytes().length <= 1){
                bytes += 1;
            }else{
                bytes += 2;
            }
        }
        if(bytes <= 1){
            if(D) Log.d(TAG,"1 half width character. NG");
            return false;
        }
        String regex_InHIRAGANA = "\\p{InHIRAGANA}";
        String regex_InKATAKANA = "\\p{InKATAKANA}";
        String regex_InHALFWIDTH_AND_FULLWIDTH_FORMS = "\\p{InHALFWIDTH_AND_FULLWIDTH_FORMS}";
        String regex_InCJK_SYMBOLS_AND_PUNCTUATION = "\\p{InCJK_SYMBOLS_AND_PUNCTUATION}";

        try {
            if(word.matches(regex_InHIRAGANA)){
                if(D) Log.d(TAG,"1 character in HIRAGANA");
                return false;
            }
            if(word.matches(regex_InKATAKANA)){
                if(D) Log.d(TAG,"1 character in KATAKANA");
                return false;
            }
            if(word.matches(regex_InHALFWIDTH_AND_FULLWIDTH_FORMS)){
                if(D) Log.d(TAG,"1 character in HALFWIDTH_AND_FULLWIDTH_FORMS");
                return false;
            }
            if(word.matches(regex_InCJK_SYMBOLS_AND_PUNCTUATION)){
                if(D) Log.d(TAG,"1 character in CJK_SYMBOLS_AND_PUNCTUATION");
                return false;
            }
            if(D) Log.d(TAG,"OK");
            return true;
        }catch (PatternSyntaxException e) {
            if(D) Log.d(TAG,"PatternSyntaxException");
        }
        return false;
    }

    public static BookData getBook(JSONObject data){
        BookData book = new BookData();
        book.setView_type(BooksListViewAdapter.VIEW_TYPE_BOOK);
        String isbn = MyBookshelfUtils.getParam(data, "isbn");
        book.setISBN(isbn);
        String imageUrl = MyBookshelfUtils.getParam(data, "largeImageUrl");
        book.setImage(imageUrl);
        String title = MyBookshelfUtils.getParam(data, "title");
        book.setTitle(title);
        String author = MyBookshelfUtils.getParam(data, "author");
        book.setAuthor(author);
        String publisher = MyBookshelfUtils.getParam(data, "publisherName");
        book.setPublisher(publisher);
        String salesDate = MyBookshelfUtils.getParam(data, "salesDate");
        book.setSalesDate(salesDate);
        String itemPrice = MyBookshelfUtils.getParam(data, "itemPrice");
        book.setItemPrice(itemPrice);
        String rating = MyBookshelfUtils.getParam(data, "reviewAverage");
        book.setRating(rating);
        String rakutenUrl = MyBookshelfUtils.getParam(data, "itemUrl");
        book.setRakutenUrl(rakutenUrl);
        String readStatus = "0"; // Unregistered
        book.setReadStatus(readStatus);
        return book;
    }



    public static Calendar parseDate(String source){
        Calendar calendar = Calendar.getInstance();

        String format1 = "yyyy年MM月dd日";
        String format2 = "yyyy年MM月";
        String format3 = "yyyy年";

        SimpleDateFormat sdf = new SimpleDateFormat(format1, Locale.JAPAN);
        sdf.setLenient(false);

        if(TextUtils.isEmpty(source)){
            return null;
        }

        try {
            Date date = sdf.parse(source);
            if(sdf.format(date).equalsIgnoreCase(source)){
                calendar.setTime(date);
                return calendar;
            }else{
                return null;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        sdf.applyPattern(format2);
        try{
            Date date = sdf.parse(source);
            calendar.setTime(date);
            calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            return calendar;
        } catch (ParseException e){
            e.printStackTrace();
        }

        sdf.applyPattern(format3);
        try{
            Date date = sdf.parse(source);
            calendar.setTime(date);
            calendar.set(Calendar.MONTH,calendar.getActualMinimum(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
            return calendar;
        } catch (ParseException e){
            e.printStackTrace();
        }

        return null;
    }


    public static InputStream getStreamSkipBOM(InputStream is, Charset charSet) throws IOException {
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

    public static String[] splitLineWithComma(String line) {
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
