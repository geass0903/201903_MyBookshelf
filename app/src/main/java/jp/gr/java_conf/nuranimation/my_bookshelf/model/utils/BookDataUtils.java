package jp.gr.java_conf.nuranimation.my_bookshelf.model.utils;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.gr.java_conf.nuranimation.my_bookshelf.model.entity.BookData;

public class BookDataUtils {
    public static final String JSON_KEY_ITEMS               = "Items";
    public static final String JSON_KEY_COUNT               = "count";
    public static final String JSON_KEY_LAST                = "last";
    public static final String JSON_KEY_ERROR               = "error";
    public static final String JSON_KEY_ERROR_DESCRIPTION   = "error_description";

    private static final String JSON_KEY_TITLE               = "title";
    private static final String JSON_KEY_AUTHOR              = "author";
    private static final String JSON_KEY_PUBLISHER_NAME      = "publisherName";
    private static final String JSON_KEY_ISBN                = "isbn";
    private static final String JSON_KEY_SALES_DATE          = "salesDate";
    private static final String JSON_KEY_ITEM_PRICE          = "itemPrice";
    private static final String JSON_KEY_ITEM_URL            = "itemUrl";
    private static final String JSON_KEY_IMAGE_URL           = "largeImageUrl";
    private static final String JSON_KEY_REVIEW_AVERAGE      = "reviewAverage";

    private static final String INDEX_IMAGE = "images";
    private static final String INDEX_ISBN = "isbn";
    private static final String INDEX_TITLE = "title";
    private static final String INDEX_AUTHOR = "author";
    private static final String INDEX_PUBLISHER = "publisherName";
    private static final String INDEX_RELEASE_DATE = "releaseDate";
    private static final String INDEX_PRICE = "price";
    private static final String INDEX_RAKUTEN_URL = "rakutenUrl";
    private static final String INDEX_RATING = "rating";
    private static final String INDEX_READ_STATUS = "readStatus";
    private static final String INDEX_TAGS = "tags";
    private static final String INDEX_READ_DATE = "finishReadDate";
    private static final String INDEX_REGISTER_DATE = "registerDate";

    private static final int IMAGE_TYPE_ORIGINAL    = 0;
    public static final int IMAGE_TYPE_LARGE        = 1;
    public static final int IMAGE_TYPE_SMALL        = 2;



    public static float convertRating(String rating){
        float value;
        try{
            value = Float.parseFloat(rating);
        }  catch (NumberFormatException e){
            value = 0.0f;
        }
        return value;
    }

    public static String convertRating(float rating){
        return String.format(Locale.JAPAN,"%.1f", rating);
    }

    public static BookData convertToBookData(JSONObject data) throws JSONException {
        BookData temp = new BookData();
        temp.setView_type(BookData.TYPE_BOOK);
        String title = getStringParam(data, JSON_KEY_TITLE);
        temp.setTitle(title);
        String author = getStringParam(data, JSON_KEY_AUTHOR);
        temp.setAuthor(author);
        String publisher = getStringParam(data, JSON_KEY_PUBLISHER_NAME);
        temp.setPublisher(publisher);
        String isbn = getStringParam(data, JSON_KEY_ISBN);
        temp.setISBN(isbn);
        String salesDate = getStringParam(data, JSON_KEY_SALES_DATE);
        temp.setSalesDate(salesDate);
        String itemPrice = getStringParam(data, JSON_KEY_ITEM_PRICE);
        temp.setItemPrice(itemPrice);
        String rakutenUrl = getStringParam(data, JSON_KEY_ITEM_URL);
        temp.setRakutenUrl(rakutenUrl);
        String imageUrl = getStringParam(data,JSON_KEY_IMAGE_URL);
        temp.setImage(imageUrl);
        String rating = getStringParam(data, JSON_KEY_REVIEW_AVERAGE);
        temp.setRating(rating);
        return new BookData(temp);
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
                    book.setImage(BookDataUtils.parseUrlString(split[i]));
                    break;
            }
        }
        if (TextUtils.isEmpty(book.getISBN())) {
            throw new IOException("illegal BookData");
        }
        book.setView_type(BookData.TYPE_BOOK);
        return book;
    }

    public static String convertBookDataToLine(String[] index, BookData book) {
        List<String> list = new ArrayList<>();

        for (String idx : index) {
            switch (idx) {
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

    public static String[] splitLineWithComma(String line) {
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

    public static String parseUrlString(String url){
        return parseUrlString(url, IMAGE_TYPE_ORIGINAL);
    }


    public static String parseUrlString(String url, int type) {
        if (TextUtils.isEmpty(url)) {
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
        if (index != -1) {
            url = url.substring(0, index + 4);
        } else {
            index = url.lastIndexOf(".gif");
            if (index != -1) {
                url = url.substring(0, index + 4);
            } else {
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


    private static String getStringParam(JSONObject json, String keyword) throws JSONException {
        if (json.has(keyword)) {
            return  json.getString(keyword);
        }
        return "";
    }


    private static String getDateString(String date) {
        Calendar calendar = CalendarUtils.parseDateString(date);
        return CalendarUtils.parseCalendar(calendar);
    }


}
