package jp.gr.java_conf.nuranimation.my_bookshelf;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;


//@SuppressWarnings({"WeakerAccess"})
public class BaseUtils {
    public static BufferedReader getBufferedReaderSkipBOM(InputStream is, Charset charSet) throws IOException {
        InputStreamReader isr;
        BufferedReader br;

        if (!(charSet == Charset.forName("UTF-8"))) {
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            return br;
        }

        if (!is.markSupported()) {
            is = new BufferedInputStream(is);
        }
        is.mark(3);
        if (is.available() >= 3) {
            byte[] b = {0, 0, 0};
            int bytes = is.read(b, 0, 3);
            if (bytes == 3 && b[0] != (byte) 0xEF || b[1] != (byte) 0xBB || b[2] != (byte) 0xBF) {
                is.reset();
            }
        }
        isr = new InputStreamReader(is, charSet);
        br = new BufferedReader(isr);
        return br;
    }

    public static BufferedWriter getBufferedWriter(OutputStream os, Charset charSet){
        OutputStreamWriter osr = new OutputStreamWriter(os, charSet);
        return new BufferedWriter(osr);
    }


}
