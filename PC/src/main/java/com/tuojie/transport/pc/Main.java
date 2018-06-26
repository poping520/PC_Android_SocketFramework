package com.tuojie.transport.pc;

import java.io.File;

/**
 * SocketFramework PC (Client)
 * 工作流程
 * 发送连接请求 => 收到连接成功消息 => PUSH数据 => 发送PUSH完成消息 => 收到任务完成消息 => PULL结果 => 发送PULL完成消息 => 结束程序
 */
public class Main {

    public static final boolean DEBUG = false;

    public static void main(String[] args) {
        if (args.length < 4) showUsage();

        String hostPort = null;
        int clientPort = 0;
        int serverPort = 0;
        String amParam = null;
        String inputDir = null;
        String outputDir = null;

        int argstart = 0;

        while (argstart < args.length && args[argstart].startsWith("-")) {
            if ("-h".equals(args[argstart])) {
                hostPort = args[++argstart];
                ++argstart;
            } else if ("-cp".equals(args[argstart])) {
                clientPort = Integer.parseInt(args[++argstart]);
                ++argstart;
            } else if ("-sp".equals(args[argstart])) {
                serverPort = Integer.parseInt(args[++argstart]);
                ++argstart;
            } else if ("-am".equals(args[argstart])) {
                amParam = args[++argstart];
                ++argstart;
            } else if ("-in".equals(args[argstart])) {
                inputDir = args[++argstart];
                ++argstart;
            } else if ("-out".equals(args[argstart])) {
                outputDir = args[++argstart];
            } else {
                showUsage();
            }
        }

        if (amParam == null || "".equals(amParam)) showUsage();
        if (inputDir == null || "".equals(inputDir)) showUsage();
        work(hostPort, clientPort, serverPort, amParam, inputDir, outputDir);
    }

    private static void work(String hostPort, int clientPort, int serverPort, String amParam, String inputDir, String outputDir) {
        Transport transport = new Transport();
        transport.adbConnect(hostPort);
        transport.connectServer(clientPort, serverPort);
        transport.startServerApp(amParam);

        transport.registerResponder((event, msg) -> {
            switch (event) {
                case CONNECT_SUCCESS:
                    //msg = Android端workDir exp: /sdcard/xxx/xxx
                    transport.pushData(inputDir, msg);
                    transport.sendMessage(Events.FromPC.PUSH_DATA_FINISH, new File(inputDir).getName());
                    break;

                case WORK_PROGRESS:
                    //msg = 进度信息
                    println("server progress: " + msg);
                    break;

                case WORK_COMPLETE:
                    //msg = Android端outputDir exp: /sdcard/xxx/xxx/output
                    transport.pullResult(outputDir, msg);
                    transport.sendMessage(Events.FromPC.PULL_DATA_FINISH, null);
                    transport.close();
                    transport.deleteServerWorkDir();
                    println("finish");
                    break;

                case ERROR_OCCURED:
                    println("server error occured: " + msg);
                    transport.deleteServerWorkDir();
                    break;
            }
        });

        transport.sendMessage(Events.FromPC.CONNECT_REQUEST, null);
    }

    static void println(String str) {
        System.out.println(str + "\n");
    }

    private static void showUsage() {
        System.out.println("<optional params> [must params]");
        System.out.println("<-h>    adb connect host:port       default=>127.0.0.1:52001");
        System.out.println("<-cp>   socket client port          default=>6789");
        System.out.println("<-sp>   socket server port          default=>9876");
        System.out.println("[-am]   adb shell am start          com.xxx.xxx/.MainActivity");
        System.out.println("[-in]   input dir                   save all data");
        System.out.println("<-out>  output dir                  default=>input parent dir");
        System.exit(0);
    }
}
