package com.tuojie.transport.android;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private DataOutputStream mDos;
    private DataInputStream mDis;

    private AtomicBoolean isLooper = new AtomicBoolean(true);

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
                while (isLooper.get()) {
                    mDis = new DataInputStream(mSocket.getInputStream());
                    int code = mDis.readInt();
                    String str = mDis.readUTF();
                    Message msg = mHandler.obtainMessage();
                    msg.what = code;
                    msg.obj = str;
                    mHandler.sendMessage(msg);
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
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(mPort);
            while (isLooper.get()) {
                mSocket = serverSocket.accept();
                mDos = new DataOutputStream(mSocket.getOutputStream());
                receiveThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
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
    }

    public void close() {
        if (mSocket != null) {
            try {
                isLooper.set(false);
            } catch (Exception e) {
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
