package jp.gr.java_conf.nuranimation.my_bookshelf;

@SuppressWarnings({"unused","WeakerAccess"})
public class ErrorStatus {
    static final int No_Error                        =   0;
    static final int Error_File_Bookshelf_not_found  =   1;
    static final int Error_File_Authors_not_found    =   2;
    static final int Error_Upload_failed             =   3;
    static final int Error_Download_failed           =   4;
    static final int Error_IO_Error                  =  50;
    static final int Error_File_not_found            =  51;

    static final int Error_Empty_Word                = 100;
    static final int Error_InterruptedException      = 101;
    static final int Error_IOException               = 102;
    static final int Error_JSONException             = 103;


    static final int Unknown                         = 999;
}
