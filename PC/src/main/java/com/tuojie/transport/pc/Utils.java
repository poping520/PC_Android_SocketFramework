package com.tuojie.transport.pc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/10/24 15:38
 */
public class Utils {

    /**
     * 判断 路径 是否为磁盘根目录
     */
    public static boolean isDisKRootDir(String path) {
        // 路径有冒号
        boolean isWindows = path.contains(":");

        // 非 windows 根目录为 '/'
        if (!isWindows) {
            return path.equals("/");
        }
        // C:  C:\
        return !path.contains("\\") || path.endsWith(":") || path.endsWith(":\\");
    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.size() == 0;
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
