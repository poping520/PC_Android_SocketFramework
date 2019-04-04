package com.tuojie.transport.pc;

import com.tuojie.transport.pc.adb.AdbWrapper;
import com.tuojie.transport.pc.adb.DeviceState;

import java.io.File;
import java.util.Map;

import static com.tuojie.transport.pc.Utils.getCurrentTime;
import static java.lang.String.format;
import static com.tuojie.transport.pc.Main.*;
import static com.tuojie.transport.pc.Logger.log;
import static com.tuojie.transport.pc.Utils.getStackTraceMessage;
import static com.tuojie.transport.pc.Utils.isEmpty;

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

    private AdbWrapper mAdb;

    private int mExceptionTime;

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
                    onWorkComplete();
                    log("==> work completed at %s result => %s", getCurrentTime(), result);
                    break;

                case ERROR_OCCURED:
                    //msg = Android端错误信息
                    log("server error occured: " + msg);
                    break;
            }
        }

        @Override
        public void onExceptionOccured(Throwable th) {
            if (mExceptionTime >= ATTEMPT_MAX_TIME / 2) {
                log("socket exception occured, try max times; cause => " + getStackTraceMessage(th));
                return;
            }

            ++mExceptionTime;
            log("socket exception occured, try restart work(%s/%s); cause => %s",
                    mExceptionTime, ATTEMPT_MAX_TIME / 2, getStackTraceMessage(th));
            new Thread(Transport.this::start).start();
        }
    };

    Transport(Params params) {
        if (params == null) {
            showUsage();
            return;
        }

        log("\n==> work start at %s", getCurrentTime());

        mParams = params;

        String adbEnv = mParams.adbEnv;
        if (isEmpty(adbEnv)) {
            adbEnv = DEFAULT_ADB_ENV;
        }
        mAdb = new AdbWrapper(adbEnv);
    }

    public synchronized void start() {
        connectDevice();
        closeSocket();
        connectServer();
        sendMessage(Events.FromPC.CONNECT_REQUEST, null);
    }

    private void onConnectSuccess(String serverWorkDir) {
        mParams.serverWorkDir = serverWorkDir;
        String extMsg = mParams.extMsg;

        if (!isEmpty(extMsg)) {
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

    private void connectDevice() {
        Map<String, DeviceState> devices = mAdb.devices();

        // 找不到任何设备
        if (isEmpty(devices)) {
            throw new ClientException("no device has been found anymore");
        }

        // 8fbfcc4f or 127.0.0.1:62001
        String device = mParams.device;
        boolean isWifiConn = device.contains(".") || device.contains(":");
        DeviceState state = devices.get(device);

        // 设备已连接
        if (state != null && state == DeviceState.DEVICE) {
            log(isWifiConn ?
                    format("device '%s' is connected already", device) :
                    format("find device '%s'", device)
            );
        } else { // 未找到设备 或者 设备是未连接状态
            if (!isWifiConn) {
                throw new ClientException(
                        state == null ?
                                format("device '%s' is not found", device) :
                                format("device '%s' is %s", device, state)
                );
            } else {
                log("adb connect...");
                boolean connect = mAdb.connect(device);
                if (!connect) {
                    throw new ClientException("adb connect fail", mAdb.getLastCmdRet());
                } else {
                    log("adb connect success");
                }
            }
        }
        mAdb.setTargetDevice(mParams.device);
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

                log("connect to server fail, wait 5s to try next connect(%s/%s)",
                        time + 1, ATTEMPT_MAX_TIME);
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

        if (!mAdb.forward(clientPort, serverPort)) {
            log("adb forward fail");
            return false;
        }
        mClientSocket.setPort(clientPort);

        String pkgName = amParam.substring(0, amParam.indexOf("/"));
        mAdb.shell_closeApp(pkgName);
        boolean launchApp = mAdb.shell_launchApp(amParam);
        if (!launchApp) {
            log("server app [%s] launch fail, reason => %s", pkgName, mAdb.getLastCmdRet());
            return false;
        }

        log("server app [%s] launch success", pkgName);

        // 等待 服务端 ServerSocket 初始化
        try {
            log("wait server socket initialize");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return mClientSocket.connect();
    }

    private void pushData() {
        String clientSrcDir = mParams.inputDir;
        String serverWorkDir = mParams.serverWorkDir;

        mAdb.shell_forceCreateDir(serverWorkDir);

        log("start push PC:'%s' to Android:'%s'", clientSrcDir, serverWorkDir);
        boolean push = mAdb.push(clientSrcDir, serverWorkDir);
        if (!push) throw new ClientException("push data fail", mAdb.getLastCmdRet());
        log("push completed");
    }

    private void pullResult(String serverOutputDir) {
        log("server work completed, start pull result data");

        boolean pull = mAdb.pull(serverOutputDir, mParams.outputDir);
        if (!pull)
            log("pull fail " + mAdb.getLastCmdRet());
        log("pull completed, output to " + mParams.outputDir);
    }

    private void deleteServerWorkDir() {
        if (!isEmpty(mParams.serverWorkDir)) {
            mAdb.shell_forceRemove(mParams.serverWorkDir);
        }
    }

    private synchronized void sendMessage(Events.FromPC event, String msg) {
        log("client send msg: event => %s; msg => %s", event.name(), msg);
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
