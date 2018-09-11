package com.tuojie.transport.android;

/**
 * 简单包装{@link ServerSocketTread}
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/6/23 12:43
 */
public class Transport {

    private Responder mResponder;

    private ServerSocketTread mServerTread = new ServerSocketTread() {
        @Override
        public void onReceive(int code, String msg) {
            mResponder.onResponse(Events.FromPC.getEvent(code), msg);
        }
    };

    Transport(int port) {
        mServerTread.setPort(port);
    }

    public void registerResponder(Responder responder) {
        this.mResponder = responder;
    }

    public synchronized void sendMessage(Events.FromAndroid event, String msg) {
        mServerTread.sendMessage(event.getCode(), msg);
    }

    public void start() {
        mServerTread.start();
    }

    public void onDestroy() {
        mServerTread.closeServer();
    }
}
