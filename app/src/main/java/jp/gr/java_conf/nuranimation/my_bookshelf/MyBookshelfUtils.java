package jp.gr.java_conf.nuranimation.my_bookshelf;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


@SuppressWarnings({"WeakerAccess"})
public class MyBookshelfUtils {
    public static final String TAG = MyBookshelfUtils.class.getSimpleName();
    private static final boolean D = false;

    private static final String INDEX_IMAGE             = "images";
    private static final String INDEX_ISBN              = "isbn";
    private static final String INDEX_TITLE             = "title";
    private static final String INDEX_AUTHOR            = "author";
    private static final String INDEX_PUBLISHER         = "publisherName";
    private static final String INDEX_RELEASE_DATE      = "releaseDate";
    private static final String INDEX_PRICE             = "price";
    private static final String INDEX_RAKUTEN_URL       = "rakutenUrl";
    private static final String INDEX_RATING            = "rating";
    private static final String INDEX_READ_STATUS       = "readStatus";
    private static final String INDEX_TAGS              = "tags";
    private static final String INDEX_READ_DATE         = "finishReadDate";
    private static final String INDEX_REGISTER_DATE     = "registerDate";


    public static final int IMAGE_TYPE_ORIGINAL = 0;
    public static final int IMAGE_TYPE_LARGE    = 1;
    public static final int IMAGE_TYPE_SMALL    = 2;


    public static String[] getShelfBooksIndex() {
        List<String> list = new ArrayList<>();
        list.add(INDEX_ISBN);
        list.add(INDEX_TITLE);
        list.add(INDEX_AUTHOR);
        list.add(INDEX_PUBLISHER);
        list.add(INDEX_IMAGE);
        list.add(INDEX_RELEASE_DATE);
        list.add(INDEX_PRICE);
        list.add(INDEX_RAKUTEN_URL);
        list.add(INDEX_RATING);
        list.add(INDEX_READ_STATUS);
        list.add(INDEX_READ_DATE);
        list.add(INDEX_TAGS);
        list.add(INDEX_REGISTER_DATE);
        return list.toArray(new String[0]);
    }

    public static String convertBookDataToLine(String[] index, BookData book){
        List<String> list = new ArrayList<>();
        for(String idx: index){
            switch(idx){
                case INDEX_ISBN:
                    list.add(book.getISBN());
                    break;
                case INDEX_TITLE:
                    list.add(book.getTitle());
                    break;
                case INDEX_AUTHOR:
                    list.add(book.getAuthor());
                    break;
                case INDEX_PUBLISHER:
                    list.add(book.getPublisher());
                    break;
                case INDEX_IMAGE:
                    list.add(book.getImage());
                    break;
                case INDEX_RELEASE_DATE:
                    list.add(book.getSalesDate());
                    break;
                case INDEX_PRICE:
                    list.add(book.getItemPrice());
                    break;
                case INDEX_RAKUTEN_URL:
                    list.add(book.getRakutenUrl());
                    break;
                case INDEX_RATING:
                    list.add(book.getRating());
                    break;
                case INDEX_READ_STATUS:
                    list.add(book.getReadStatus());
                    break;
                case INDEX_READ_DATE:
                    list.add(book.getFinishReadDate());
                    break;
                case INDEX_TAGS:
                    list.add(book.getTags());
                    break;
                case INDEX_REGISTER_DATE:
                    list.add(book.getRegisterDate());
                    break;
                default:
                    list.add("");
                    break;
            }
        }
        return TextUtils.join(",", list.toArray(new String[0]));
    }

    public static BookData convertToBookData(final String[] index, final String line) throws IOException {
        BookData book = new BookData();
        String[] split = splitLineWithComma(line);
        if (split.length != index.length) {
            throw new IOException("can not convertToBookData");
        }

        for (int i = 0; i < index.length; i++) {
            switch (index[i]) {
                case INDEX_ISBN:
                    book.setISBN(split[i]);
                    break;
                case INDEX_TITLE:
                    book.setTitle(split[i]);
                    break;
                case INDEX_AUTHOR:
                    book.setAuthor(split[i]);
                    break;
                case INDEX_PUBLISHER:
                    book.setPublisher(split[i]);
                    break;
                case INDEX_RELEASE_DATE:
                    book.setSalesDate(getDateString(split[i]));
                    break;
                case INDEX_PRICE:
                    book.setItemPrice(split[i]);
                    break;
                case INDEX_RAKUTEN_URL:
                    book.setRakutenUrl(split[i]);
                    break;
                case INDEX_RATING:
                    book.setRating(split[i]);
                    break;
                case INDEX_READ_STATUS:
                    book.setReadStatus(split[i]);
                    break;
                case INDEX_TAGS:
                    book.setTags(split[i]);
                    break;
                case INDEX_READ_DATE:
                    book.setFinishReadDate(getDateString(split[i]));
                    break;
                case INDEX_REGISTER_DATE:
                    book.setRegisterDate(getDateString(split[i]));
                    break;
                case INDEX_IMAGE:
                    book.setImage(parseUrlString(split[i], IMAGE_TYPE_ORIGINAL));
                    break;
            }
        }
        if (TextUtils.isEmpty(book.getISBN())) {
            throw new IOException("illegal BookData");
        }
        return book;
    }

    public static BookData convertToBookData(JSONObject data) throws JSONException {
        BookData temp = new BookData();
        temp.setView_type(BooksListViewAdapter.VIEW_TYPE_BOOK);
        String title = getStringParam(data, BookData.JSON_KEY_TITLE);
        temp.setTitle(title);
        String author = getStringParam(data, BookData.JSON_KEY_AUTHOR);
        temp.setAuthor(author);
        String publisher = getStringParam(data, BookData.JSON_KEY_PUBLISHER_NAME);
        temp.setPublisher(publisher);
        String isbn = getStringParam(data, BookData.JSON_KEY_ISBN);
        temp.setISBN(isbn);
        String salesDate = getStringParam(data, BookData.JSON_KEY_SALES_DATE);
        temp.setSalesDate(salesDate);
        String itemPrice = getStringParam(data, BookData.JSON_KEY_ITEM_PRICE);
        temp.setItemPrice(itemPrice);
        String rakutenUrl = getStringParam(data, BookData.JSON_KEY_ITEM_URL);
        temp.setRakutenUrl(rakutenUrl);
        String imageUrl = getStringParam(data, BookData.JSON_KEY_IMAGE_URL);
        temp.setImage(imageUrl);
        String rating = getStringParam(data, BookData.JSON_KEY_REVIEW_AVERAGE);
        temp.setRating(rating);
        String readStatus = BookData.STATUS_UNREGISTERED;
        temp.setReadStatus(readStatus);
        return new BookData(temp);
    }





    public static boolean isSearchable(String word) throws PatternSyntaxException {
        if (TextUtils.isEmpty(word)) {
            if (D) Log.d(TAG, "No word");
            return false;
        }
        if (word.length() >= 2) {
            if (D) Log.d(TAG, "over 2characters. OK");
            return true;
        }

        int bytes = 0;
        char[] array = word.toCharArray();
        for (char c : array) {
            if (D) Log.d(TAG, "Unicode Block: " + Character.UnicodeBlock.of(c));
            if (String.valueOf(c).getBytes().length <= 1) {
                bytes += 1;
            } else {
                bytes += 2;
            }
        }
        if (bytes <= 1) {
            if (D) Log.d(TAG, "1 half width character. NG");
            return false;
        }
        String regex_InHIRAGANA = "\\p{InHIRAGANA}";
        String regex_InKATAKANA = "\\p{InKATAKANA}";
        String regex_InHALFWIDTH_AND_FULLWIDTH_FORMS = "\\p{InHALFWIDTH_AND_FULLWIDTH_FORMS}";
        String regex_InCJK_SYMBOLS_AND_PUNCTUATION = "\\p{InCJK_SYMBOLS_AND_PUNCTUATION}";


        if (word.matches(regex_InHIRAGANA)) {
            if (D) Log.d(TAG, "1 character in HIRAGANA");
            return false;
        }
        if (word.matches(regex_InKATAKANA)) {
            if (D) Log.d(TAG, "1 character in KATAKANA");
            return false;
        }
        if (word.matches(regex_InHALFWIDTH_AND_FULLWIDTH_FORMS)) {
            if (D) Log.d(TAG, "1 character in HALFWIDTH_AND_FULLWIDTH_FORMS");
            return false;
        }
        if (word.matches(regex_InCJK_SYMBOLS_AND_PUNCTUATION)) {
            if (D) Log.d(TAG, "1 character in CJK_SYMBOLS_AND_PUNCTUATION");
            return false;
        }
        if (D) Log.d(TAG, "OK");
        return true;
    }

    public static Calendar parseDate(String source) {
        Calendar calendar = Calendar.getInstance();

        String format1 = "yyyy/MM/dd";
        String format2 = "yyyy年MM月dd日";
        String format3 = "yyyy年MM月";
        String format4 = "yyyy年";

        SimpleDateFormat sdf = new SimpleDateFormat(format1, Locale.JAPAN);
        sdf.setLenient(false);

        if (TextUtils.isEmpty(source)) {
            return null;
        }

        try {
            Date date = sdf.parse(source);
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e1) {
            sdf.applyPattern(format2);
            try {
                Date date = sdf.parse(source);
                calendar.setTime(date);
                return calendar;
            } catch (ParseException e2) {
                sdf.applyPattern(format3);
                try {
                    Date date = sdf.parse(source);
                    calendar.setTime(date);
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    return calendar;
                } catch (ParseException e3) {
                    sdf.applyPattern(format4);
                    try {
                        Date date = sdf.parse(source);
                        calendar.setTime(date);
                        calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                        return calendar;
                    } catch (ParseException e4) {
                        e4.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static String parseUrlString(String url, int type){
        if(TextUtils.isEmpty(url)){
            return "";
        }

        String REGEX_SURROUND_DOUBLE_QUOTATION = "^\"|\"$";
        String REGEX_SURROUND_BRACKET = "^\\(|\\)$";
        Pattern sdqPattern = Pattern.compile(REGEX_SURROUND_DOUBLE_QUOTATION);
        Matcher matcher = sdqPattern.matcher(url);
        url = matcher.replaceAll("");
        Pattern sbPattern = Pattern.compile(REGEX_SURROUND_BRACKET);
        matcher = sbPattern.matcher(url);
        url = matcher.replaceAll("");

        int index = url.lastIndexOf(".jpg");
        if(index != -1) {
            url = url.substring(0, index+4);
        }else{
            index = url.lastIndexOf(".gif");
            if(index != -1){
                url = url.substring(0, index+4);
            }else{
                return "";
            }
        }

        switch (type) {
            case IMAGE_TYPE_ORIGINAL:
                break;
            case IMAGE_TYPE_LARGE:
                url = url + "?_200x200";
                break;
            case IMAGE_TYPE_SMALL:
                url = url + "?_100x100";
                break;
        }

        return url;
    }

    private static String getDateString(String date){
        if(TextUtils.isEmpty(date)){
            return "";
        }
        Calendar calendar = getCalendar(date);
//        Calendar calendar = parseDate(date);
        if(calendar == null){
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.JAPAN);
        return sdf.format(calendar.getTime());
    }

    private static String[] splitLineWithComma(String line) throws PatternSyntaxException{
        String REGEX_CSV_COMMA = ",(?=(([^\"]*\"){2})*[^\"]*$)";
        String REGEX_SURROUND_DOUBLE_QUOTATION = "^\"|\"$";
        String REGEX_DOUBLE_DOUBLE_QUOTATION = "\"\"";

        Pattern cPattern = Pattern.compile(REGEX_CSV_COMMA);
        String[] cols = cPattern.split(line, -1);
        String[] arr = new String[cols.length];
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
        return arr;
    }

    private static String getStringParam(JSONObject json, String keyword) throws JSONException {
        if (json.has(keyword)) {
            String param = json.getString(keyword);
            if (D) Log.d(TAG, keyword + ": " + param);
            return param;
        }
        return "";
    }

    public static Calendar getCalendar(String source){
        String[] formats = {"yyyy/MM/dd","yyyy年MM月dd日","yyyy年MM月","yyyy年"};
        for(String format : formats){
            Calendar calendar = parseDate(format, source);
            if(calendar != null){
                return calendar;
            }
        }
        return null;
    }

    private  static Calendar parseDate(String format, String source) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.JAPAN);
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(source);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            switch (format) {
                case "yyyy年MM月":
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    break;
                case "yyyy年":
                    calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    break;
            }
            return calendar;
        } catch (ParseException e) {
            if(D) e.printStackTrace();
        }
        return null;
    }


    public static Drawable getReadStatusImage(Context context, String status) {
        Resources res = context.getResources();
        Drawable read_status_image;
        switch (status) {
            case BookData.STATUS_UNREGISTERED:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.setColorFilter(Color.parseColor("#00000000"), PorterDuff.Mode.CLEAR);
                }
                break;
            case BookData.STATUS_INTERESTED:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_favorites, null);
                if (read_status_image != null) {
                    read_status_image.setColorFilter(Color.parseColor("#FFDD0000"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case BookData.STATUS_UNREAD:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.setColorFilter(Color.parseColor("#FFDDDD00"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case BookData.STATUS_READING:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.setColorFilter(Color.parseColor("#FF00DD00"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case BookData.STATUS_ALREADY_READ:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.setColorFilter(Color.parseColor("#FF0000DD"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            case BookData.STATUS_NONE:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.setColorFilter(Color.parseColor("#FF808080"), PorterDuff.Mode.SRC_ATOP);
                }
                break;
            default:
                read_status_image = ResourcesCompat.getDrawable(res, R.drawable.ic_circle, null);
                if (read_status_image != null) {
                    read_status_image.setColorFilter(Color.parseColor("#00000000"), PorterDuff.Mode.SRC_ATOP);
                }
        }
        return read_status_image;
    }


    public static String getReadStatusText(Context context, String status){
        String read_status_text;
        switch (status){
            case BookData.STATUS_UNREGISTERED:
                read_status_text = context.getString(R.string.read_status_label_0);
                break;
            case BookData.STATUS_INTERESTED:
                read_status_text = context.getString(R.string.read_status_label_1);
                break;
            case BookData.STATUS_UNREAD:
                read_status_text = context.getString(R.string.read_status_label_2);
                break;
            case BookData.STATUS_READING:
                read_status_text = context.getString(R.string.read_status_label_3);
                break;
            case BookData.STATUS_ALREADY_READ:
                read_status_text = context.getString(R.string.read_status_label_4);
                break;
            case BookData.STATUS_NONE:
                read_status_text = context.getString(R.string.read_status_label_5);
                break;
            default:
                read_status_text = context.getString(R.string.read_status_label_0);
        }
        return read_status_text;
    }


}
