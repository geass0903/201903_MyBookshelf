package jp.gr.java_conf.nuranimation.my_bookshelf.model.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CalendarUtils {
    private static final boolean D = false;

    private static final String DATE_FORMAT_1 = "yyyy年MM月dd日";
    private static final String DATE_FORMAT_2 = "yyyy年MM月";
    private static final String DATE_FORMAT_3 = "yyyy年";
    private static final String DATE_FORMAT_4 = "yyyy/MM/dd";


    public static Calendar parseDateString(String source) {
        String[] enableFormats = new String[]{DATE_FORMAT_1, DATE_FORMAT_4, DATE_FORMAT_2, DATE_FORMAT_3};
        for (String format : enableFormats) {

            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.JAPAN);
            sdf.setLenient(false);
            try {
                Date date = sdf.parse(source);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                switch (format) {
                    case DATE_FORMAT_2:
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                        break;
                    case DATE_FORMAT_3:
                        calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                        break;
                }
                return calendar;
            } catch (ParseException e) {
                if (D) e.printStackTrace();
            }
        }
        return null;
    }

    public static String parseCalendar(Calendar calendar){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_1, Locale.JAPAN);
        if(calendar != null) {
            return sdf.format(calendar.getTime());
        }
        return "";
    }


}
