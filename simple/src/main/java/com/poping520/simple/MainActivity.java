package com.poping520.simple;

import android.os.Bundle;
import android.util.Log;

import com.tuojie.transport.android.WorkActivity;
import com.tuojie.transport.android.WorkListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

@SuppressWarnings("all")
public class MainActivity extends WorkActivity {

    private static final String TAG = "Simple";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected int getServerPort() {
        return 0;
    }

    @Override
    protected void doWork(String dataDir, String outputDir, WorkListener listener) {
        //outputDir = dataDir + "/output"

        Log.e(TAG, "dataDir=" + dataDir);
        Log.e(TAG, "outputDir=" + outputDir);

        listener.onProgress("开始任务");

        //do your own work
        copyDir(dataDir, outputDir, listener);
        //do your own work

        listener.onWorkComplete();
    }

    private void copyDir(String src, String dst, WorkListener listener) {
        File srcDir = new File(src);
        File dstDir = new File(dst);
        if (!dstDir.exists()) dstDir.mkdirs();

        File[] files = srcDir.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                copyDir(file.getAbsolutePath(), dst + File.separator + file.getName(), listener);
            } else {
                listener.onProgress("正在拷贝：" + file.getName());
                copyFile(file, new File(dst + File.separator + file.getName()));
            }
        }
    }

    private void copyFile(File src, File dst) {
        FileChannel srcChannel = null;
        FileChannel dstChannel = null;
        try {
            srcChannel = new FileInputStream(src).getChannel();
            dstChannel = new FileOutputStream(dst).getChannel();
            srcChannel.transferTo(0, srcChannel.size(), dstChannel);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (srcChannel != null) srcChannel.close();
                if (dstChannel != null) dstChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
