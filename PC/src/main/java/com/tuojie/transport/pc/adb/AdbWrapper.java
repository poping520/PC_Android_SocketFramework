package com.tuojie.transport.pc.adb;

import com.tuojie.transport.pc.adb.Command.Result;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * <pre>
 * desc: ADB 命令包装
 * time: 2019/4/2
 * </pre>
 */
public class AdbWrapper {

    private Result mLastCmdRet;

    // 不带 -s 参数
    private String mBaseAdb;

    // 可以带 -s 参数
    private String mAdb;

    /**
     * @param adbEnv adb.exe 路径
     */
    public AdbWrapper(String adbEnv) {
        this.mBaseAdb = adbEnv;
        this.mAdb = adbEnv;
    }

    /**
     * adb devices
     */
    public Map<String, DeviceState> devices() {
        Map<String, DeviceState> ret = new HashMap<>();
        String devices = execAdb("devices").succMsg;
        String[] arr = devices.split("\n");

        for (String str : arr) {
            // 跳过第一行 'List of devices attached'
            if (str.startsWith("List")) continue;

            // 8fbfcc4f unauthorized
            // 127.0.0.1:62001  device
            if (str.contains("\t")) {
                String[] split = str.split("\t");
                String device = split[0];
                DeviceState state = DeviceState.getDeviceState(split[1]);
                ret.put(device, state);
            } else {
                ret.put(str, DeviceState.OTHER);
            }
        }
        return ret;
    }

    /**
     * adb connect
     *
     * @param hostPort host:port
     */
    public boolean connect(String hostPort) {
        Result connect = execAdb("connect " + hostPort);
        String msg = connect.succMsg;
        return msg.contains("already connected to") || msg.contains("connected to");
    }

    /**
     * adb forward
     *
     * @param clientPort 客户端端口号
     * @param serverPort 服务端端口号
     */
    public boolean forward(int clientPort, int serverPort) {
        return execAdb_s(format("forward tcp:%s tcp:%s", clientPort, serverPort)).isSucc;
    }

    /**
     * adb push
     *
     * @param from pc
     * @param to   android
     */
    public boolean push(String from, String to) {
        return execAdb_s(format("push %s %s", from, to)).isSucc;
    }

    /**
     * adb pull
     *
     * @param from android
     * @param to   pc
     */
    public boolean pull(String from, String to) {
        return execAdb_s(format("pull %s %s", from, to)).isSucc;
    }

    /**
     * shell 关闭应用
     *
     * @param pkgName 应用包名
     */
    public boolean shell_closeApp(String pkgName) {
        return execShell("am force-stop " + pkgName).isSucc;
    }

    /**
     * shell 启动应用
     *
     * @param amParam 包名/启动Activity
     */
    public boolean shell_launchApp(String amParam) {
        Result result = execShell("am start -n " + amParam);
        return result.isSucc && !result.succMsg.contains("does not exist");
    }

    /**
     * shell 创建文件夹
     *
     * @param dir 路径
     */
    public boolean shell_forceCreateDir(String dir) {
        return execShell("mkdir -p " + dir).isSucc;
    }

    /**
     * shell 删除文件/夹
     *
     * @param path 路径
     */
    public boolean shell_forceRemove(String path) {
        return execShell("rm -rf " + path).isSucc;
    }

    // 执行 shell 命令
    private Result execShell(String shellCmd) {
        return execAdb_s("shell " + shellCmd);
    }

    // 执行 adb -s xxx 命令
    private Result execAdb_s(String cmd) {
        return exec(mAdb + " " + cmd);
    }

    // 执行 adb 命令
    private Result execAdb(String cmd) {
        return exec(mBaseAdb + " " + cmd);
    }

    /**
     * 指定设备
     * adb -s 参数
     */
    public void setTargetDevice(String device) {
        mAdb = format("%s -s %s", mBaseAdb, device);
    }

    /**
     * 获取最近一次执行命令的结果
     */
    public Result getLastCmdRet() {
        return mLastCmdRet;
    }

    private Result exec(String cmd) {
        Result exec = Command.exec(cmd);
        mLastCmdRet = exec;
        return exec;
    }
}
