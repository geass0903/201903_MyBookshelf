package jp.gr.java_conf.nuranimation.my_bookshelf.application;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

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

@SuppressWarnings({"WeakerAccess"})
public class MyBookshelfUtils {
    public static final String TAG = MyBookshelfUtils.class.getSimpleName();
    private static final boolean D = true;

    public static final int IMAGE_TYPE_LARGE = 1;
    public static final int IMAGE_TYPE_SMALL = 2;


    public static boolean isValid(String word) throws PatternSyntaxException {
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

        String format1 = "yyyy年MM月dd日";
        String format2 = "yyyy年MM月";
        String format3 = "yyyy年";

        SimpleDateFormat sdf = new SimpleDateFormat(format1, Locale.JAPAN);
        sdf.setLenient(false);

        if (TextUtils.isEmpty(source)) {
            return null;
        }

        try {
            Date date = sdf.parse(source);
            calendar.setTime(date);
            return calendar;
        } catch (ParseException e) {
            sdf.applyPattern(format2);
            try {
                Date date = sdf.parse(source);
                calendar.setTime(date);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                return calendar;
            } catch (ParseException e2) {
                sdf.applyPattern(format3);
                try {
                    Date date = sdf.parse(source);
                    calendar.setTime(date);
                    calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                    return calendar;
                } catch (ParseException e3) {
                    e3.printStackTrace();
                }
            }
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
            byte[] b = {0, 0, 0};
            int bytes = is.read(b,0,3);
            if(bytes == 3 &&  b[0]!=(byte)0xEF  || b[1]!=(byte)0xBB || b[2]!= (byte)0xBF ){
                is.reset();
            }
        }
        return is;
    }

    public static String[] splitLineWithComma(String line) throws PatternSyntaxException{
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



    public static Uri getImageUri(String url, int type){
        if(TextUtils.isEmpty(url)){
            return null;
        }
//        String REGEX_CSV_COMMA = ",";
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
            }
        }

        if(D) Log.d(TAG,"url: " + url);

        switch (type){
            case IMAGE_TYPE_LARGE:
                url = url + "?_200x200";
                break;
            case IMAGE_TYPE_SMALL:
                url = url + "?_100x100";
                break;
        }



//        Pattern cPattern = Pattern.compile(REGEX_CSV_COMMA);
//        String[] arr = cPattern.split(url, -1);
        return Uri.parse(url);
//        return Uri.parse(arr[0]);
    }







}
