package com.tuojie.transport.pc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public abstract class ClientSocket {

    private static final String SERVER_HOST = "127.0.0.1";

    private String mHost;
    private int mPort;
    private boolean isLooper;

    private Socket socket;

    public void setPort(int port) {
        mPort = port;
    }

    public void setHost(String host) {
        mHost = host;
    }

    public void sendMessage(int code, String msg) {
        try {
            mHost = mHost == null ? SERVER_HOST : mHost;
            socket = new Socket(SERVER_HOST, mPort);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(code);
            msg = msg == null ? "" : msg;
            dos.writeUTF(msg);

            if (isLooper) return;

            //等待服务端回复
            while (!socket.isClosed()) {
                isLooper = true;
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                onReceive(dis.readInt(), dis.readUTF());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendLastMessage(int code, String msg) {
        try {
            socket = new Socket(SERVER_HOST, mPort);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(code);
            msg = msg == null ? "" : msg;
            dos.writeUTF(msg);
            dos.flush();

            Thread.sleep(100);
            close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void onReceive(int code, String msg);
}
