package com.tuojie.transport.pc;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/10/24 14:24
 */
class Params {

    /**
     * usb连接：设备序列号
     * 无线连接：host:port
     */
    @MustParam
    String device;

    /**
     * am 启动参数
     */
    @MustParam
    String amParam;

    /**
     * 输入数据文件夹路径
     */
    @MustParam
    String inputDir;

    /**
     * 输出数据文件夹路径
     */
    String outputDir;

    /**
     * adb.exe 路径
     */
    String adbEnv;

    /**
     * 拓展数据
     */
    String extMsg;

    /**
     * 是否保存日志
     */
    boolean saveLog;

    /**
     * 客户端端口号
     */
    int clientPort;

    /**
     * 服务端端口号
     */
    int serverPort;

    /**
     * 服务端 工作文件夹
     */
    String serverWorkDir;
}
