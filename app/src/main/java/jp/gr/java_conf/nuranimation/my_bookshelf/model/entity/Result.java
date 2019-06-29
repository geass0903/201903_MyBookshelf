package jp.gr.java_conf.nuranimation.my_bookshelf.model.entity;

import java.util.ArrayList;
import java.util.List;

public class Result{
    public static final int ERROR_CODE_IO_EXCEPTION             =   1;
    public static final int ERROR_CODE_JSON_EXCEPTION           =   2;
    public static final int ERROR_CODE_DBX_EXCEPTION            =   3;
    public static final int ERROR_CODE_HTTP_ERROR               =   4;
    public static final int ERROR_CODE_INTERRUPTED_EXCEPTION    =   5;
    public static final int ERROR_CODE_FILE_NOT_FOUND           =   6;
    public static final int ERROR_CODE_EMPTY_KEYWORD            =   7;
    public static final int ERROR_CODE_EMPTY_AUTHORS_LIST       =   8;
    public static final int ERROR_CODE_EXPORT_DIR_NOT_FOUND     =   9;
    public static final int ERROR_CODE_EXPORT_CANCELED          =  10;
    public static final int ERROR_CODE_IMPORT_DIR_NOT_FOUND     =  11;
    public static final int ERROR_CODE_IMPORT_CANCELED          =  12;
    public static final int ERROR_CODE_UPLOAD_DIR_NOT_FOUND     =  13;
    public static final int ERROR_CODE_UPLOAD_CANCELED          =  14;
    public static final int ERROR_CODE_DOWNLOAD_DIR_NOT_FOUND   =  15;
    public static final int ERROR_CODE_DOWNLOAD_CANCELED        =  16;

    public static final int ERROR_CODE_UNKNOWN                  = 100;


    private final boolean isSuccess;
    private final int errorCode;
    private final String errorMessage;
    private final int type;
    private final List<BookData> books;
    private final boolean hasNext;

    private Result(boolean isSuccess, int errorCode, String errorMessage, int type,List<BookData> books, boolean hasNext) {
        this.isSuccess = isSuccess;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.type = type;
        this.books = books;
        this.hasNext = hasNext;
    }

    public boolean isSuccess() {
        return this.isSuccess;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public int getType() {
        return this.type;
    }

    public List<BookData> getBooks() {
        return new ArrayList<>(this.books);
    }

    public boolean hasNext() {
        return this.hasNext;
    }


    /*--- File Backup success ---*/
    public static Result BackupSuccess(int type) {
        return new Result(true, 0, "No Error", type, null, false);
    }
    /*--- File Backup error ---*/
    public static Result BackupError(int type, int errorCode, String errorMessage) {
        return new Result(false,errorCode, errorMessage ,type, null, false);
    }

    /*--- Search Books success ---*/
    public static Result SearchSuccess(List<BookData> books, boolean hasNext) {
        return new Result(true, 0, "No Error", 0, books, hasNext);
    }

    /*--- Search Books error ---*/
    public static Result SearchError(int errorCode, String errorMessage) {
        return new Result(false, errorCode, errorMessage, 0, null, false);
    }

    /*--- Reload NewBooks success ---*/
    public static Result ReloadSuccess(List<BookData> books) {
        return new Result(true, 0, "No Error", 0, books, false);
    }

    /*--- Reload NewBooks error ---*/
    public static Result ReloadError(int errorCode, String errorMessage) {
        return new Result(false, errorCode, errorMessage, 0, null, false);
    }

}