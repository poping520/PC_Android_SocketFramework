package com.tuojie.transport.pc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/10/24 15:38
 */
class Utils {

    static boolean isNullStr(String str) {
        return str == null || "".equals(str);
    }

    static String getCurrentTime() {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA).format(new Date());
    }

    static String getStackTraceMessage(Throwable th) {
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        th.printStackTrace(pw);
        return writer.toString();
    }
}
