package com.tuojie.transport.pc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
class Utils {

    /**
     * 判断 路径 是否为磁盘根目录
     */
    static boolean isDisKRootDir(String path) {
        // 路径有冒号
        boolean isWindows = path.contains(":");

        // 非 windows 根目录为 '/'
        if (!isWindows) {
            return path.equals("/");
        }
        // C:  C:\
        return !path.contains("\\") || path.endsWith(":") || path.endsWith(":\\");
    }

    static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }

    static <K, V> boolean isEmpty(Map<K, V> map) {
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

    /**
     * 读取文本文件
     *
     * @param path 源文件路径
     * @return 文本字符串
     */
    static String readTextFile(String path) {
        return readTextFile(new File(path));
    }

    /**
     * 读取文本文件
     *
     * @param src 源文件
     * @return 文本字符串
     */
    static String readTextFile(File src) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(src));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 判断字符串是否是已存在文件的全路径
     *
     * @param str 字符串
     * @return 是否是存在的文件
     */
    static boolean isExistFile(String str) {
        if (isEmpty(str)) return false;
        File file = new File(str);
        return file.exists() && file.isFile();
    }
}
