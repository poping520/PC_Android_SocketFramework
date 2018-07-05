package com.tuojie.transport.android;

import android.os.Handler;
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
 * @version 1.0.0
 * create on 2018/6/23 13:06
 */
public abstract class ServerSocketTread extends Thread {

    private int mPort;
    private Socket mSocket;
    private ServerSocket mServerSocket;
    private DataOutputStream mDos;
    private DataInputStream mDis;

    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            onReceive(msg.what, (String) msg.obj);
        }
    };

    private Thread receiveThread = new Thread() {
        @Override
        public void run() {
            try {
                while (true) {
                    mDis = new DataInputStream(mSocket.getInputStream());
                    int code = mDis.readInt();
                    String str = mDis.readUTF();
                    Message msg = mHandler.obtainMessage();
                    msg.what = code;
                    msg.obj = str;
                    mHandler.sendMessage(msg);

                    //收到客户端pull完成消息 停止循环
                    if (code == Events.FromPC.PULL_DATA_FINISH.getCode()) {
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mSocket != null) {
                    try {
                        mSocket.close();
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
            }
        }
    };

    public void setPort(int port) {
        this.mPort = port;
    }

    @Override
    public void run() {
        try {
            mServerSocket = new ServerSocket(mPort);
            mSocket = mServerSocket.accept();
            mDos = new DataOutputStream(mSocket.getOutputStream());
            receiveThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeServer() {
        if (mServerSocket != null) {
            try {
                mServerSocket.close();
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
    }

    public void sendMessage(int code, String msg) {
        try {
            if (mSocket != null && mSocket.isConnected()) {
                mDos.writeInt(code);
                if (msg != null) mDos.writeUTF(msg);
                mDos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void onReceive(int code, String msg);
}
