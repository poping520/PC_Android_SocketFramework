package com.tuojie.transport.pc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import static com.tuojie.transport.pc.Logger.*;
import static com.tuojie.transport.pc.Utils.*;

/**
 * <b>SocketFramework PC (Client)</b><p>
 * 工作流程<p>
 * 发送连接请求 => 收到连接成功消息 => PUSH数据 => 发送PUSH完成消息 => 收到任务完成消息 => PULL结果 => 发送PULL完成消息 => 结束程序<p>
 * <p>
 * 1.0.7<p>
 * 优化 程序出现异常后重新开始任务的最大次数改为 5 次<p>
 * <p>
 * 1.0.6<p>
 * 增加 捕获到 socket 通信发生的异常后重新开始任务 {@link ClientSocket#run()}
 * 优化 整体代码结构<p>
 * <p>
 * 1.0.5<p>
 * 增加 连接失败超时和服务端无响应超时处理 超时后会进行一定次数内的重试<p>
 * <p>
 * 1.0.4<p>
 * 增加 "-log" 参数, 保存日志到文件中 (保存到输出文件夹 socket_framework.log 中)<p>
 * 优化 "-h" 改为必填参数<p>
 * 优化 当输出文件夹不存在时创建该文件夹<p>
 * 注意 无法从安卓设备 pull 一个文件夹到电脑某个磁盘的根目录 (adb原因)<p>
 * <p>
 * 1.0.3<p>
 * 增加 "-adb" 参数, 自定义 adb.exe 的路径<p>
 * 修复 某些情况下无法连接到服务端 {@link ClientSocket#connect()}<p>
 * <p>
 * 1.0.2<p>
 * 增加 "-ext" 参数 (附加信息)<p>
 * <p>
 * 1.0.1<p>
 * 优化 使用 "adb -s xxx:xxx" 命令 (多设备连接的情况指定某个设备)<p>
 * 增加 任务完成回调 isSuccess 参数<p>
 * <p>
 * 1.0.0<p>
 * 第一版<p>
 */
public class Main {

    private static final String VERSION = "1.0.7";

    static final boolean DEBUG = false;

    private static final String LOG_FILE_NAME = "socket_framework.log";

    public static void main(String[] args) {
        //the must params
        if (args.length < 6) showUsage();

        Params params = new Params();
        int argstart = 0;

        while (argstart < args.length && args[argstart].startsWith("-")) {
            if ("-h".equals(args[argstart])) {
                params.hostPort = args[++argstart];
                ++argstart;
            } else if ("-am".equals(args[argstart])) {
                params.amParam = args[++argstart];
                ++argstart;
            } else if ("-in".equals(args[argstart])) {
                params.inputDir = args[++argstart];
                ++argstart;
            } else if ("-out".equals(args[argstart])) {
                params.outputDir = args[++argstart];
                ++argstart;
            } else if ("-adb".equals(args[argstart])) {
                params.adbEnv = args[++argstart];
                ++argstart;
            } else if ("-ext".equals(args[argstart])) {
                params.extMsg = args[++argstart];
                ++argstart;
            } else if ("-log".equals(args[argstart])) {
                params.saveLog = true;
                ++argstart;
            } else if ("-cp".equals(args[argstart])) {
                params.clientPort = Integer.parseInt(args[++argstart]);
                ++argstart;
            } else if ("-sp".equals(args[argstart])) {
                params.serverPort = Integer.parseInt(args[++argstart]);
                ++argstart;
            } else {
                showUsage();
            }
        }

        init(params);
        log("\n==========> this work start at " + getCurrentTime() + " <==========");
        new Transport(params).start();
    }

    private static void init(Params p) {

        // 必须的参数
        final Field[] fields = p.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(MustParam.class)) {
                try {
                    final Object obj = field.get(p);
                    if (obj == null) showUsage();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        // 处理输出文件夹
        if (isNullStr(p.outputDir)) {
            File file = new File(p.inputDir);
            if (!file.isAbsolute()) {
                try {
                    p.outputDir = file.getCanonicalFile().getParent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                p.outputDir = file.getParent();
            }
        }

        File outFile = new File(p.outputDir);
        if (!outFile.exists()) {
            if (!outFile.mkdirs()) {
                throw new ClientException("create output dir fail");
            }
        }

        //日志
        Logger.init(p.saveLog, new File(outFile, LOG_FILE_NAME));
    }

    static void showUsage() {
        System.out.println(String.format("PC_Android_SocketFramework v%s\n", VERSION));
        System.out.println("<optional params> [must params]");
        System.out.println("[-h]    adb connect host:port       this param this must now");
        System.out.println("[-am]   am start param              like com.xxx.xxx/.xxxActivity");
        System.out.println("[-in]   input dir                   save all input data dir");
        System.out.println("<-out>  output dir                  default=>input parent dir");
        System.out.println("<-adb>  custom adb.exe path         default=>use system env adb.exe");
        System.out.println("<-ext>  extended message            send before push data");
        System.out.println("<-log>                              save log to file");
        System.out.println("<-cp>   socket client port          default=>6789");
        System.out.println("<-sp>   socket server port          default=>9876");
        System.exit(0);
    }
}
