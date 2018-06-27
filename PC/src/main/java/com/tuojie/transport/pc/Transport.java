package com.tuojie.transport.pc;

import java.io.File;

import static com.tuojie.transport.pc.Main.println;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/6/23 14:39
 */
public class Transport {

    private static final int DEFAULT_CLIENT_PORT = 6789;
    private static final int DEFAULT_SERVER_PORT = 9876;

    private static final String DEFAULT_HOST_PORT = "127.0.0.1:52001";

    private Responder mResponder;

    private String adb;

    private String mClientSrcDir;

    private String mServerWorkDir;

    private ClientSocket mClientSocket = new ClientSocket() {
        @Override
        public void onReceive(int code, String msg) {
            mResponder.onResponse(Events.FromAndroid.getEvent(code), msg);
        }
    };

    public void adbConnect(String hostPort) {
        println("adb connect...");
        if (hostPort != null) {
            mClientSocket.setHost(hostPort.substring(0, hostPort.indexOf(":")));
        } else {
            hostPort = DEFAULT_HOST_PORT;
        }
        Command.Result connect = Command.exec("adb connect " + hostPort);
        this.adb = String.format("adb -s %s ", hostPort);

        if (!connect.isSucc || connect.succMsg.contains("unable to connect to " + hostPort) || connect.succMsg.contains("cannot connect to " + hostPort))
            throw new ClientException("adb connect fail", connect);

        println("adb connect success");
    }

    public void connectServer(int clientPort, int serverPort) {
        clientPort = clientPort == 0 ? DEFAULT_CLIENT_PORT : clientPort;
        serverPort = serverPort == 0 ? DEFAULT_SERVER_PORT : serverPort;

        Command.Result forward = Command.exec(String.format("%s forward tcp:%s tcp:%s", adb, clientPort, serverPort));
        mClientSocket.setPort(clientPort);

        if (!forward.isSucc) throw new ClientException("connect to server fail", forward);
        println("connect to server success");
    }

    public void startServerApp(String amParam) {
        String pkgName = amParam.substring(0, amParam.indexOf("/"));

        Command.exec(adb + "shell am force-stop " + pkgName);
        Command.Result startAct = Command.exec(String.format("%s shell am start -n %s", adb, amParam));
        mClientSocket.connect();

        if (!startAct.isSucc || startAct.succMsg.contains("does not exist"))
            throw new ClientException("server app [" + pkgName + "] launch fail", startAct);

        println("server app [" + pkgName + "] launch success");
    }

    public void pushData(String clientSrcDir, String serverWorkDir) {
        println("push " + clientSrcDir + " to " + serverWorkDir);
        Command.exec(adb + "shell mkdir -p " + serverWorkDir);
        Command.Result push = Command.exec(String.format("%s push %s %s", adb, clientSrcDir, serverWorkDir));
        if (!push.isSucc) throw new ClientException("push data fail", push);
        this.mClientSrcDir = clientSrcDir;
        this.mServerWorkDir = serverWorkDir;
        println("push data success");
    }

    public void pullResult(String clientOutputDir, String serverOutputDir) {
        println("server work completed, start pull result data");
        clientOutputDir = clientOutputDir == null ? new File(mClientSrcDir).getParent() : clientOutputDir;
        Command.Result pull = Command.exec(String.format("%s pull %s %s", adb, serverOutputDir, clientOutputDir));
        if (!pull.isSucc) throw new ClientException("pull fail", pull);
        println("pull completed, output to " + clientOutputDir);
    }

    public void deleteServerWorkDir() {
        if (mServerWorkDir != null) {
            Command.exec(adb + "shell rm -rf " + mServerWorkDir);
        }
    }

    public void registerResponder(Responder responder) {
        this.mResponder = responder;
    }

    public void sendMessage(Events.FromPC event, String msg) {
        mClientSocket.sendMessage(event.getCode(), msg);
    }

    public void close() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientSocket.close();
    }
}
