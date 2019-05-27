package jp.gr.java_conf.nuranimation.my_bookshelf.application;

@SuppressWarnings({"unused","WeakerAccess"})
public class ErrorStatus {
    public static final int No_Error                        =   0;
    public static final int Error_File_Bookshelf_not_found  =   1;
    public static final int Error_File_Authors_not_found    =   2;
    public static final int Error_Upload_failed             =   3;
    public static final int Error_Download_failed           =   4;
    public static final int Error_IO_Error                  =  50;
    public static final int Error_File_not_found            =  51;

    public static final int Error_Empty_Word                = 100;
    public static final int Error_InterruptedException      = 101;
    public static final int Error_IOException               = 102;
    public static final int Error_JSONException             = 103;


    public static final int Unknown                         = 999;
}
