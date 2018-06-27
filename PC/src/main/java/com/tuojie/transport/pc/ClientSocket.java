package com.tuojie.transport.pc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class ClientSocket implements Runnable {

    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";

    private String mHost;
    private int mPort;
    private Socket mSocket;
    private DataOutputStream mDos;

    @Override
    public void run() {
        try {
            while (!mSocket.isClosed()) {
                DataInputStream dis = new DataInputStream(mSocket.getInputStream());
                onReceive(dis.readInt(), dis.readUTF());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPort(int port) {
        mPort = port;
    }

    public void setHost(String host) {
        mHost = host;
    }

    public void connect() {
        mHost = mHost == null ? DEFAULT_SERVER_HOST : mHost;
        try {
            mSocket = new Socket(mHost, mPort);
            mDos = new DataOutputStream(mSocket.getOutputStream());
            new Thread(this).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(int code, String msg) {
        if (mDos == null) return;
        try {
            mDos.writeInt(code);
            mDos.writeUTF(msg == null ? "" : msg);
            mDos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (mSocket != null && !mSocket.isClosed()) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void onReceive(int code, String msg);
}
