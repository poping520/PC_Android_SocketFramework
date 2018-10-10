package com.tuojie.transport.pc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * SocketFramework PC (Client)
 * 工作流程
 * 发送连接请求 => 收到连接成功消息 => PUSH数据 => 发送PUSH完成消息 => 收到任务完成消息 => PULL结果 => 发送PULL完成消息 => 结束程序
 *
 * 1.0.4
 * 增加 "-log" 参数, 保存日志到文件中 (保存到输出文件夹 socket_framework.log 中)
 * 优化 "-h" 改为必填参数
 * 优化 当输出文件夹不存在时创建改文件夹
 * 注意 无法从安卓设备 pull 一个文件夹到电脑某个磁盘的根目录 (adb原因)
 *
 * 1.0.3
 * 增加 自定义 adb.exe 的路径
 * 修复 某些情况下无法连接到服务端 {@link ClientSocket#connect()}
 *
 * 1.0.2
 * 增加 "-ext" 参数 (附加信息)
 * 增加 任务完成回调 isSuccess 参数
 *
 * 1.0.1
 * 优化 使用 "adb -s xxx:xxx" 命令 (多设备连接的情况指定某个设备)
 *
 * 1.0.0
 * 第一版
 */
public class Main {

    private static final String VERSION = "1.0.4";

    public static final boolean DEBUG = false;

    private static boolean isSaveLog;

    private static File sLogFile;

    public static void main(String[] args) {
        //the must params
        if (args.length < 6) showUsage();

        String hostPort = null;
        String amParam = null;
        String inputDir = null;
        String outputDir = null;
        String adbEnv = null;
        String extMsg = null;
        int clientPort = 0;
        int serverPort = 0;

        int argstart = 0;

        while (argstart < args.length && args[argstart].startsWith("-")) {
            if ("-h".equals(args[argstart])) {
                hostPort = args[++argstart];
                ++argstart;
            } else if ("-am".equals(args[argstart])) {
                amParam = args[++argstart];
                ++argstart;
            } else if ("-in".equals(args[argstart])) {
                inputDir = args[++argstart];
                ++argstart;
            } else if ("-out".equals(args[argstart])) {
                outputDir = args[++argstart];
                ++argstart;
            } else if ("-adb".equals(args[argstart])) {
                adbEnv = args[++argstart];
                ++argstart;
            } else if ("-ext".equals(args[argstart])) {
                extMsg = args[++argstart];
                ++argstart;
            } else if ("-log".equals(args[argstart])) {
                isSaveLog = true;
                ++argstart;
            } else if ("-cp".equals(args[argstart])) {
                clientPort = Integer.parseInt(args[++argstart]);
                ++argstart;
            } else if ("-sp".equals(args[argstart])) {
                serverPort = Integer.parseInt(args[++argstart]);
                ++argstart;
            } else {
                showUsage();
            }
        }

        if (isNullStr(hostPort) || isNullStr(amParam) || isNullStr(inputDir))
            showUsage();


        // 处理输出文件夹
        if (outputDir == null) {
            File file = new File(inputDir);
            if (!file.isAbsolute()) {
                try {
                    outputDir = file.getCanonicalFile().getParent();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                outputDir = file.getParent();
            }
        }

        File outFile = new File(outputDir);
        if (!outFile.exists()) {
            if (!outFile.mkdirs()) {
                throw new ClientException("create output dir fail");
            }
        }
        sLogFile = new File(outFile, "socket_framework.log");

        log("\n==========> this work start at " + getCurrentTime() + " <==========");

        work(hostPort, adbEnv, clientPort, serverPort, amParam, inputDir, outputDir, extMsg);
    }

    private static void work(String hostPort, String adbEnv, int clientPort,
                             int serverPort, String amParam, String inputDir,
                             String outputDir, String extMsg) {

        Transport transport = new Transport();
        transport.adbConnect(adbEnv, hostPort);
        transport.connectServer(clientPort, serverPort);
        transport.startServerApp(amParam);

        transport.registerResponder((event, msg) -> {
            switch (event) {
                case CONNECT_SUCCESS:
                    if (extMsg != null && !"".equals(extMsg)) {
                        transport.sendMessage(Events.FromPC.EXTENDED_MESSAGE, extMsg);
                    }

                    //msg = Android端workDir exp: /sdcard/xxx/xxx
                    transport.pushData(inputDir, msg);
                    transport.sendMessage(Events.FromPC.PUSH_DATA_FINISH, new File(inputDir).getName());
                    break;

                case WORK_PROGRESS:
                    //msg = 进度信息
                    log("server progress: " + msg);
                    break;

                case WORK_COMPLETE_SUCC:
                    //msg = Android端outputDir exp: /sdcard/xxx/xxx/output
                    transport.pullResult(outputDir, msg);
                    workComplete(transport);
                    log("work complete; result => success");
                    break;

                case WORK_COMPLETE_FAIL:
                    workComplete(transport);
                    log("work complete; result => fail");
                    break;

                case ERROR_OCCURED:
                    log("server error occured: " + msg);
                    break;
            }
        });

        transport.sendMessage(Events.FromPC.CONNECT_REQUEST, null);
    }

    private static void workComplete(Transport transport) {
        transport.sendMessage(Events.FromPC.CLOSE_SERVER_APP, null);
        transport.closeSocket();
        transport.deleteServerWorkDir();
    }

    private static boolean isNullStr(String str) {
        return str == null || "".equals(str);
    }

    static void log(String msg) {
        System.out.println(msg + "\n");

        if (isSaveLog) {
            writeToFile(msg + "\n\n");
        }
    }

    private static void writeToFile(String msg) {
        OutputStreamWriter osw = null;

        try {
            osw = new OutputStreamWriter(new FileOutputStream(sLogFile, true), "UTF-8");
            osw.write(msg);
            osw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (osw != null) osw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getCurrentTime() {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA).format(new Date());
    }

    private static void showUsage() {
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
