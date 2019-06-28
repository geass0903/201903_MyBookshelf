package jp.gr.java_conf.nuranimation.my_bookshelf.model.entity;

public class BooksOrder {
    public static final String SHELF_BOOKS_ORDER_CODE_TITLE_ASC          = "1";
    public static final String SHELF_BOOKS_ORDER_CODE_TITLE_DESC         = "2";
    public static final String SHELF_BOOKS_ORDER_CODE_AUTHOR_ASC         = "3";
    public static final String SHELF_BOOKS_ORDER_CODE_AUTHOR_DESC        = "4";
    public static final String SHELF_BOOKS_ORDER_CODE_SALES_DATE_ASC     = "5";
    public static final String SHELF_BOOKS_ORDER_CODE_SALES_DATE_DESC    = "6";
    public static final String SHELF_BOOKS_ORDER_CODE_REGISTERED_ASC     = "7";
    public static final String SHELF_BOOKS_ORDER_CODE_REGISTERED_DESC    = "8";

    private static final String SHELF_BOOKS_ORDER_TITLE_ASC          = " order by title asc";
    private static final String SHELF_BOOKS_ORDER_TITLE_DESC         = " order by title desc";
    private static final String SHELF_BOOKS_ORDER_AUTHOR_ASC         = " order by author asc";
    private static final String SHELF_BOOKS_ORDER_AUTHOR_DESC        = " order by author desc";
    private static final String SHELF_BOOKS_ORDER_SALES_DATE_ASC     = " order by release_date asc";
    private static final String SHELF_BOOKS_ORDER_SALES_DATE_DESC    = " order by release_date desc";
    private static final String SHELF_BOOKS_ORDER_REGISTERED_ASC     = " order by register_date asc";
    private static final String SHELF_BOOKS_ORDER_REGISTERED_DESC    = " order by register_date desc";

    public static final String SEARCH_BOOKS_ORDER_CODE_SALES_DATE_ASC     = "1";
    public static final String SEARCH_BOOKS_ORDER_CODE_SALES_DATE_DESC    = "2";

    private static final String SEARCH_BOOKS_ORDER_SALES_DATE_ASC     = "+releaseDate";
    private static final String SEARCH_BOOKS_ORDER_SALES_DATE_DESC    = "-releaseDate";



    public static String getShelfBooksOrder(String code) {
        if (code == null) {
            return SHELF_BOOKS_ORDER_REGISTERED_ASC;
        } else switch (code) {
            case SHELF_BOOKS_ORDER_CODE_TITLE_ASC:
                return SHELF_BOOKS_ORDER_TITLE_ASC;
            case SHELF_BOOKS_ORDER_CODE_TITLE_DESC:
                return SHELF_BOOKS_ORDER_TITLE_DESC;
            case SHELF_BOOKS_ORDER_CODE_AUTHOR_ASC:
                return SHELF_BOOKS_ORDER_AUTHOR_ASC;
            case SHELF_BOOKS_ORDER_CODE_AUTHOR_DESC:
                return SHELF_BOOKS_ORDER_AUTHOR_DESC;
            case SHELF_BOOKS_ORDER_CODE_SALES_DATE_ASC:
                return SHELF_BOOKS_ORDER_SALES_DATE_ASC;
            case SHELF_BOOKS_ORDER_CODE_SALES_DATE_DESC:
                return SHELF_BOOKS_ORDER_SALES_DATE_DESC;
            case SHELF_BOOKS_ORDER_CODE_REGISTERED_ASC:
                return SHELF_BOOKS_ORDER_REGISTERED_ASC;
            case SHELF_BOOKS_ORDER_CODE_REGISTERED_DESC:
                return SHELF_BOOKS_ORDER_REGISTERED_DESC;
            default:
                return SHELF_BOOKS_ORDER_REGISTERED_ASC;
        }
    }

    public static String getSearchBooksOrder(String code) {
        if (code == null) {
            return SEARCH_BOOKS_ORDER_SALES_DATE_DESC;
        } else switch (code) {
            case SEARCH_BOOKS_ORDER_CODE_SALES_DATE_ASC:
                return SEARCH_BOOKS_ORDER_SALES_DATE_ASC;
            case SEARCH_BOOKS_ORDER_CODE_SALES_DATE_DESC:
                return SEARCH_BOOKS_ORDER_SALES_DATE_DESC;
            default:
                return SEARCH_BOOKS_ORDER_SALES_DATE_ASC;
        }
    }

}
