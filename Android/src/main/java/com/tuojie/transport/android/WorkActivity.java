package com.tuojie.transport.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import static com.tuojie.transport.android.Events.FromAndroid.*;


import java.io.File;

/**
 * SocketFramework Android (Server)
 * 工作流程
 * 收到连接请求 => 发送连接成功消息 => 收到PUSH完成消息 => 开始任务 => 发送任务完成消息 => 收到PULL完成消息 => 结束程序
 * <p>
 * 自定义Activity继承此类，将任务逻辑写到{@link #doWork(String, String, String, Sender)}中
 *
 * @author WangKZ
 * create on 2018/6/25 10:43
 */
public abstract class WorkActivity extends Activity {

    private Transport mTransport;

    private int mPort;

    private String mWorkDir;

    private String mOutputDir;

    private String mExtMsg;

    private UiListener mUiListener;

    private Handler mUiHandler;

    private Sender mSender = new Sender() {

        @Override
        public void sendProgress(boolean callback, String progressMsg) {

            mTransport.sendMessage(WORK_PROGRESS, progressMsg);

            if (callback) {
                notifyUi(progressMsg);
            }
        }

        @Override
        public void sendFinish(boolean isSuccess) {
            if (isSuccess) {
                mTransport.sendMessage(WORK_COMPLETE_SUCC, mOutputDir);
            } else {
                mTransport.sendMessage(WORK_COMPLETE_FAIL, "");
            }
        }

        @Override
        public void sendError(String errorMsg) {
            mTransport.sendMessage(ERROR_OCCURED, errorMsg);
        }
    };

    /**
     * 注册 UI 进度回调
     */
    public void registerUiListener(UiListener listener) {
        if (listener == null) {
            return;
        }

        this.mUiListener = listener;

        mUiHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mUiListener.onUiProgress((String) msg.obj);
            }
        };
    }

    private void notifyUi(String msg) {
        if (mUiListener == null || mUiHandler == null) {
            return;
        }
        mUiHandler.sendMessage(mUiHandler.obtainMessage(0, msg));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        mTransport = new Transport(mPort);

        mTransport.registerResponder((event, msg) -> {
            switch (event) {
                case CONNECT_REQUEST:
                    mTransport.sendMessage(CONNECT_SUCCESS, mWorkDir);
                    break;

                case EXTENDED_MESSAGE:
                    mExtMsg = msg;
                    break;

                case PUSH_DATA_FINISH:
                    //msg = 存放数据的文件夹名字 exp: shared_prefs
                    doWork(mWorkDir + File.separator + msg,
                            mOutputDir, mExtMsg, mSender);
                    break;

                case CLOSE_SERVER_APP:
                    mTransport.onDestroy();
                    finish();
                    break;
            }
        });

        mTransport.start();
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
     * @param extMsg    扩展消息
     * @param sender    任务进度回调
     */
    protected abstract void doWork(String dataDir, String outputDir, String extMsg, Sender sender);
}
