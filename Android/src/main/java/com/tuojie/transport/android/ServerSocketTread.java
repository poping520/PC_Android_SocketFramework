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
abstract class ServerSocketTread extends Thread {

    private int mPort;
    private Socket mSocket;

    private AtomicBoolean isLooper = new AtomicBoolean(true);

    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            onReceive(msg.what, (String) msg.obj);
        }
    };

    public void setPort(int port) {
        this.mPort = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(mPort);

            while (isLooper.get()) {
                mSocket = serverSocket.accept();

                DataInputStream dis = new DataInputStream(mSocket.getInputStream());
                int code = dis.readInt();
                String str = dis.readUTF();

                Message msg = mHandler.obtainMessage();
                msg.what = code;
                msg.obj = str;
                mHandler.sendMessage(msg);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    public void close() {
        if (mSocket != null) {
            try {
                interrupt();
                isLooper.set(false);
                mSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(int code, String msg) {
        try {
            if (mSocket != null && mSocket.isConnected()) {
                DataOutputStream dos = new DataOutputStream(mSocket.getOutputStream());
                dos.writeInt(code);
                if (msg != null) dos.writeUTF(msg);
                dos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void onReceive(int code, String msg);
}
