package com.tuojie.transport.pc;

import java.io.File;
import java.util.Locale;

import static com.tuojie.transport.pc.Main.*;
import static com.tuojie.transport.pc.Logger.log;
import static com.tuojie.transport.pc.Utils.getStackTraceMessage;
import static com.tuojie.transport.pc.Utils.isNullStr;

/**
 * @author WangKZ
 * create on 2018/6/23 14:39
 */
class Transport {

    private static final int DEFAULT_CLIENT_PORT = 6789;
    private static final int DEFAULT_SERVER_PORT = 9876;

    private static final String DEFAULT_ADB_ENV = "adb";

    // 连接超时
    private static final int CONNECT_TIMEOUT = 5000;

    // 最大连接次数
    private static final int ATTEMPT_MAX_TIME = 10;

    // 参数
    private Params mParams;

    // adb 环境
    private String mAdb;

    private ClientSocket mClientSocket = new ClientSocket() {

        @Override
        public void onResponse(Events.FromAndroid event, String msg) {
            switch (event) {
                case CONNECT_SUCCESS:
                    //msg = Android端workDir exp: /sdcard/xxx/xxx
                    log("received connect success response from server");
                    onConnectSuccess(msg);
                    break;

                case WORK_PROGRESS:
                    //msg = 进度信息
                    log("server progress: " + msg);
                    break;

                case WORK_COMPLETE_SUCC:
                case WORK_COMPLETE_FAIL:
                    //msg = Android端outputDir exp: /sdcard/xxx/xxx/output

                    String result;
                    if (event == Events.FromAndroid.WORK_COMPLETE_SUCC) {
                        pullResult(msg);
                        result = "success";
                    } else {
                        result = "fail";
                    }
                    log("work complete; result => " + result);
                    onWorkComplete();
                    break;

                case ERROR_OCCURED:
                    //msg = Android端错误信息
                    log("server error occured: " + msg);
                    break;
            }
        }

        @Override
        public void onExceptionOccured(Throwable th) {
            log("socket exception occured, try restart work; cause => " + getStackTraceMessage(th));
            new Thread(Transport.this::start).start();
        }
    };

    Transport(Params params) {
        if (params == null) {
            showUsage();
            return;
        }
        mParams = params;

        String adb = mParams.adbEnv;
        mAdb = isNullStr(adb) ? DEFAULT_ADB_ENV : adb;
    }

    public synchronized void start() {
        adbConnect();
        closeSocket();
        connectServer();
        sendMessage(Events.FromPC.CONNECT_REQUEST, null);
    }

    private void onConnectSuccess(String serverWorkDir) {
        mParams.serverWorkDir = serverWorkDir;
        String extMsg = mParams.extMsg;

        if (!isNullStr(extMsg)) {
            sendMessage(Events.FromPC.EXTENDED_MESSAGE, extMsg);
        }

        //msg = Android端workDir exp: /sdcard/xxx/xxx
        pushData();
        sendMessage(Events.FromPC.PUSH_DATA_FINISH, new File(mParams.inputDir).getName());
    }

    private void onWorkComplete() {
        sendMessage(Events.FromPC.CLOSE_SERVER_APP, null);
        closeSocket();
        deleteServerWorkDir();
    }

    private void adbConnect() {
        log("adb connect...");

        String hostPort = mParams.hostPort;
        Command.Result connect = Command.exec(String.format("%s connect %s", mAdb, hostPort));
        mAdb = String.format("%s -s %s ", mAdb, hostPort);

        if (!connect.isSucc
                || connect.succMsg.contains("unable to connect to " + hostPort)
                || connect.succMsg.contains("cannot connect to " + hostPort))
            throw new ClientException("adb connect fail", connect);

        log("adb connect success");
    }

    private void connectServer() {

        // 连接失败超时处理
        for (int time = 0; time <= ATTEMPT_MAX_TIME; time++) {
            if (connectServerInternal(mParams.clientPort, mParams.serverPort, mParams.amParam)) {
                log("connect to server success");
                return;
            } else {
                if (time == ATTEMPT_MAX_TIME) {
                    throw new ClientException("connect to server timeout");
                }

                log(String.format(Locale.ENGLISH,
                        "connect to server fail, wait 5s to try next connect(%d/%d)",
                        time + 1, ATTEMPT_MAX_TIME));
                try {
                    Thread.sleep(CONNECT_TIMEOUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean connectServerInternal(int clientPort, int serverPort, String amParam) {
        clientPort = clientPort == 0 ? DEFAULT_CLIENT_PORT : clientPort;
        serverPort = serverPort == 0 ? DEFAULT_SERVER_PORT : serverPort;

        Command.Result forward = Command.exec(String.format("%s forward tcp:%s tcp:%s", mAdb, clientPort, serverPort));
        if (!forward.isSucc) {
            log("adb forward fail");
            return false;
        }
        mClientSocket.setPort(clientPort);

        String pkgName = amParam.substring(0, amParam.indexOf("/"));
        Command.exec(mAdb + "shell am force-stop " + pkgName);
        Command.Result startAct = Command.exec(String.format("%s shell am start -n %s", mAdb, amParam));
        if (!startAct.isSucc || startAct.succMsg.contains("does not exist")) {
            log("server app [" + pkgName + "] start fail, reason => " + startAct.toString());
            return false;
        }
        log("server app [" + pkgName + "] start success");

        return mClientSocket.connect();
    }

    private void pushData() {
        String clientSrcDir = mParams.inputDir;
        String serverWorkDir = mParams.serverWorkDir;

        log("start push " + clientSrcDir + " to " + serverWorkDir);

        Command.exec(mAdb + "shell mkdir -p " + serverWorkDir);
        Command.Result push = Command.exec(String.format("%s push %s %s", mAdb, clientSrcDir, serverWorkDir));
        if (!push.isSucc) throw new ClientException("push data fail", push);
        log("push completed");
    }

    private void pullResult(String serverOutputDir) {
        log("server work completed, start pull result data");

        Command.Result pull = Command.exec(String.format("%s pull %s %s", mAdb, serverOutputDir, mParams.outputDir));
        if (!pull.isSucc) throw new ClientException("pull fail", pull);
        log("pull completed, output to " + mParams.outputDir);
    }

    private void deleteServerWorkDir() {
        if (!isNullStr(mParams.serverWorkDir)) {
            Command.exec(mAdb + "shell rm -rf " + mParams.serverWorkDir);
        }
    }

    private synchronized void sendMessage(Events.FromPC event, String msg) {
        log(String.format("client send msg: event => %s; msg => %s", event.name(), msg));
        mClientSocket.sendMessage(event.getCode(), msg);
    }

    private void closeSocket() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientSocket.close();
    }
}
