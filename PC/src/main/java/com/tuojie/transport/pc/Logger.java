package com.tuojie.transport.pc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/10/24 13:32
 */
public class Logger {

    private static boolean sSaveLog;
    private static File sLogFile;

    static void init(boolean saveLog, File logFile) {
        sSaveLog = saveLog;
        sLogFile = logFile;
    }

    static void log(String msg) {
        System.out.println(msg + "\n");

        if (sSaveLog) {
            writeToFile(msg + "\n\n");
        }
    }

    private static void writeToFile(String msg) {
        if (sLogFile == null) return;

        OutputStreamWriter osw = null;
        try {
            osw = new OutputStreamWriter(new FileOutputStream(sLogFile, true), "UTF-8");
            osw.write(msg);
            osw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (osw != null) osw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
