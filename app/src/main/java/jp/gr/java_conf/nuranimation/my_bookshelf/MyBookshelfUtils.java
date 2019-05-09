package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

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

    public static boolean checkInputWord(String word){
        if(D) Log.d(TAG,"Input: " + word);

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
        book.setIsbn(isbn);
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
        String readStatus = "0"; // Unregistered
        book.setReadStatus(readStatus);
        return book;
    }




}
