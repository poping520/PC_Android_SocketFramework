package com.tuojie.transport.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import java.io.File;

/**
 * SocketFramework Android (Server)
 * 工作流程
 * 收到连接请求 => 发送连接成功消息 => 收到PUSH完成消息 => 开始任务 => 发送任务完成消息 => 收到PULL完成消息 => 结束程序
 * <p>
 * 自定义Activity继承此类，将任务逻辑写到{@link #doWork(String, String, WorkListener)}中
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/6/25 10:43
 */
public abstract class WorkActivity extends Activity {

    private Transport mTransport;

    private int mPort;

    private String mWorkDir;

    private String mOutputDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        mTransport = new Transport(mPort);

        mTransport.registerResponder((event, msg) -> {
            switch (event) {
                case CONNECT_REQUEST:
                    mTransport.sendMessage(Events.FromAndroid.CONNECT_SUCCESS, mWorkDir);
                    break;

                case PUSH_DATA_FINISH:
                    //msg = 存放数据的文件夹名字 exp: shared_prefs
                    doWork(mWorkDir + "/" + msg, mOutputDir, new WorkListener() {
                        @Override
                        public void onProgress(String progressMsg) {
                            mTransport.sendMessage(Events.FromAndroid.WORK_PROGRESS, progressMsg);
                        }

                        @Override
                        public void onWorkComplete(boolean isSuccess) {
                            if (isSuccess)
                                mTransport.sendMessage(Events.FromAndroid.WORK_COMPLETE_SUCC, mOutputDir);
                            else
                                mTransport.sendMessage(Events.FromAndroid.WORK_COMPLETE_FAIL, null);
                        }

                        @Override
                        public void onWorkError(String errorMsg) {
                            mTransport.sendMessage(Events.FromAndroid.ERROR_OCCURED, errorMsg);
                        }
                    });

                    break;

                case CLOSE_SERVER_APP:
                    finish();
                    break;
            }
        });

        mTransport.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTransport.onDestroy();
    }

    @SuppressLint("SdCardPath")
    private void init() {
        String[] split = getPackageName().split("\\.");

        if (split.length >= 2)
            mWorkDir = "/sdcard/" + split[split.length - 2] + File.separator + split[split.length - 1];
        else
            mWorkDir = "/sdcard/" + split[0];

        mPort = getServerPort() == 0 ? 9876 : getServerPort();
        mOutputDir = mWorkDir + "/output";
    }

    /**
     * @return 服务端端口号，返回0则端口号默认为：9876
     */
    protected abstract int getServerPort();


    /**
     * @param dataDir   目标数据的存放目录
     * @param outputDir 输出目录
     * @param listener  任务进度监听
     */
    protected abstract void doWork(String dataDir, String outputDir, WorkListener listener);

}
