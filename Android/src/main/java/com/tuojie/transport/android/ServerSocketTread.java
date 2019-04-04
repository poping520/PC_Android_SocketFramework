package com.tuojie.transport.android;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Socket通信核心类
 *
 * @author WangKZ
 * create on 2018/6/23 13:06
 */
public abstract class ServerSocketTread extends Thread {

    private int mPort;
    private Socket mSocket;
    private ServerSocket mServerSocket;
    private DataOutputStream mDos;
    private DataInputStream mDis;

    // 发消息线程 向客户端发送消息
    private Handler mWriteHandler;
    private HandlerThread mWriteHandlerThread;

    // 读消息线程 读取客户端消息
    private Thread mReadThread;
    private Handler mReadHandler;

    private static final String CLASS_NAME = "ServerSocketTread";

    public void setPort(int port) {
        this.mPort = port;
    }

    @Override
    public void run() {
        try {
            mServerSocket = new ServerSocket(mPort);

            mSocket = mServerSocket.accept();
            mDos = new DataOutputStream(mSocket.getOutputStream());
            mDis = new DataInputStream(mSocket.getInputStream());

            startWriteThread();
            startReadThread();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 初始化 写线程
    private void startWriteThread() {
        mWriteHandlerThread = new HandlerThread(CLASS_NAME + "-write");
        mWriteHandlerThread.start();

        // 工作线程 Handler 发送消息
        mWriteHandler = new Handler(mWriteHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message message) {
                super.handleMessage(message);

                if (mSocket == null || mSocket.isClosed()) {
                    return;
                }

                int code = message.what;
                String msg = (String) message.obj;
                try {
                    mDos.writeInt(code);
                    mDos.writeUTF(msg == null ? "" : msg);
                    mDos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    // 初始化 读线程
    private void startReadThread() {
        // 主线程 handler 接收消息
        mReadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                onReceive(msg.what, (String) msg.obj);
            }
        };

        mReadThread = new Thread(CLASS_NAME + "-read") {
            @Override
            public void run() {
                try {
                    while (true) {
                        int code = mDis.readInt(); /* 会阻塞线程 */
                        String str = mDis.readUTF();
                        Message msg = mReadHandler.obtainMessage();
                        msg.what = code;
                        msg.obj = str;
                        mReadHandler.sendMessage(msg);

                        //收到客户端pull完成消息 停止循环
                        if (code == Events.FromPC.CLOSE_SERVER_APP.getCode()) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        mReadThread.start();
    }

    public void sendMessage(int code, String msg) {
        Message message = mWriteHandler.obtainMessage(code, msg);
        mWriteHandler.sendMessage(message);
    }

    public void closeServer() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mServerSocket != null) {
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mDis != null) {
            try {
                mDis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mDos != null) {
            try {
                mDos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mWriteHandlerThread != null && mWriteHandlerThread.isAlive()) {
            mWriteHandlerThread.quitSafely();
        }

        if (mReadThread != null && mReadThread.isAlive()) {
            mReadThread.interrupt();
        }
    }

    public abstract void onReceive(int code, String msg);
}
