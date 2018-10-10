package com.tuojie.transport.pc;

import java.io.File;
import java.io.IOException;

import static com.tuojie.transport.pc.Main.log;

/**
 * @author WangKZ
 * create on 2018/6/23 14:39
 */
public class Transport {

    private static final int DEFAULT_CLIENT_PORT = 6789;
    private static final int DEFAULT_SERVER_PORT = 9876;

    private static final String DEFAULT_ADB_ENV = "adb";

    private Responder mResponder;

    private String mAdb;

    private String mClientSrcDir;

    private String mServerWorkDir;

    private ClientSocket mClientSocket = new ClientSocket() {
        @Override
        public void onReceive(int code, String msg) {
            mResponder.onResponse(Events.FromAndroid.getEvent(code), msg);
        }
    };

    public void adbConnect(String adbEnv, String hostPort) {
        log("adb connect...");

        if (adbEnv != null)
            mAdb = adbEnv;
        else
            mAdb = DEFAULT_ADB_ENV;

        Command.Result connect = Command.exec(String.format("%s connect %s", mAdb, hostPort));
        mAdb = String.format("%s -s %s ", mAdb, hostPort);

        if (!connect.isSucc || connect.succMsg.contains("unable to connect to " + hostPort) || connect.succMsg.contains("cannot connect to " + hostPort))
            throw new ClientException("adb connect fail", connect);

        log("adb connect success");
    }

    public void connectServer(int clientPort, int serverPort) {
        clientPort = clientPort == 0 ? DEFAULT_CLIENT_PORT : clientPort;
        serverPort = serverPort == 0 ? DEFAULT_SERVER_PORT : serverPort;

        Command.Result forward = Command.exec(String.format("%s forward tcp:%s tcp:%s", mAdb, clientPort, serverPort));
        mClientSocket.setPort(clientPort);

        if (!forward.isSucc) throw new ClientException("connect to server fail", forward);
        log("connect to server success");
    }

    public void startServerApp(String amParam) {
        String pkgName = amParam.substring(0, amParam.indexOf("/"));

        Command.exec(mAdb + "shell am force-stop " + pkgName);
        Command.Result startAct = Command.exec(String.format("%s shell am start -n %s", mAdb, amParam));
        mClientSocket.connect();

        if (!startAct.isSucc || startAct.succMsg.contains("does not exist"))
            throw new ClientException("server app [" + pkgName + "] launch fail", startAct);

        log("server app [" + pkgName + "] launch success");
    }

    public void pushData(String clientSrcDir, String serverWorkDir) {
        log("push " + clientSrcDir + " to " + serverWorkDir);
        this.mClientSrcDir = clientSrcDir;
        this.mServerWorkDir = serverWorkDir;
        Command.exec(mAdb + "shell mkdir -p " + serverWorkDir);
        Command.Result push = Command.exec(String.format("%s push %s %s", mAdb, mClientSrcDir, mServerWorkDir));
        if (!push.isSucc) throw new ClientException("push data fail", push);
        log("push data success");
    }

    public void pullResult(String clientOutputDir, String serverOutputDir) {
        log("server work completed, start pull result data");
        Command.Result pull = Command.exec(String.format("%s pull %s %s", mAdb, serverOutputDir, clientOutputDir));
        if (!pull.isSucc) throw new ClientException("pull fail", pull);
        log("pull completed, output to " + clientOutputDir);
    }

    public void deleteServerWorkDir() {
        if (mServerWorkDir != null) {
            Command.exec(mAdb + "shell rm -rf " + mServerWorkDir);
        }
    }

    public void registerResponder(Responder responder) {
        this.mResponder = responder;
    }

    public void sendMessage(Events.FromPC event, String msg) {
        mClientSocket.sendMessage(event.getCode(), msg);
    }

    public void closeSocket() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientSocket.close();
    }
}
