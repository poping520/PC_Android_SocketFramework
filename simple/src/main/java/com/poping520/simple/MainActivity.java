package com.poping520.simple;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.tuojie.transport.android.Events;
import com.tuojie.transport.android.UiListener;
import com.tuojie.transport.android.WorkActivity;
import com.tuojie.transport.android.Sender;

import java.io.File;
import java.io.FileInputStream;
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

        final TextView tv = findViewById(R.id.tv);
        registerUiListener(new UiListener() {
            @Override
            public void onUiProgress(String msg) {
                tv.setText(msg);
            }
        });
    }

    @Override
    protected int getServerPort() {
        return 0;
    }

    // java -jar socket_framework_pc.jar -log -h 8fbfcc4f -am com.poping520.simple/.MainActivity -in J:\test_data\test
    // java -jar socket_framework_pc.jar -log -h 127.0.0.1:62001 -am com.poping520.simple/.MainActivity -in J:\test_data\test
    @Override
    protected void doWork(String dataDir, String outputDir, String extMsg, Sender sender) {
        //outputDir = dataDir + "/output"

        Log.e(TAG, "dataDir = " + dataDir);
        Log.e(TAG, "outputDir = " + outputDir);

        sender.sendProgress("开始任务");

        //do your own work
        copyDir(dataDir, outputDir, sender);
        //do your own work

        sender.sendFinish(true);
    }

    private void copyDir(String src, String dst, Sender sender) {
        File srcDir = new File(src);
        File dstDir = new File(dst);
        if (!dstDir.exists()) dstDir.mkdirs();

        File[] files = srcDir.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                copyDir(file.getAbsolutePath(), dst + File.separator + file.getName(), sender);
            } else {
                sender.sendProgress(true, "正在拷贝：%s", file.getName());
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
