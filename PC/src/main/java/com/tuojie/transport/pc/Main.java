package com.tuojie.transport.pc;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;

import static com.tuojie.transport.pc.Utils.*;

/**
 * <b>SocketFramework PC (Client)</b><p>
 * 工作流程<p>
 * 发送连接请求 => 收到连接成功消息 => PUSH数据 => 发送PUSH完成消息 => 收到任务完成消息 => PULL结果 => 发送PULL完成消息 => 结束程序<p/>
 * <p>
 * 1.0.9<p>
 * 修改 "-ext" 参数，支持从文件传入
 * <p>
 * 1.0.8<p>
 * 修改 服务端 socket 发送消息逻辑<p/>
 * 优化 socket 连接成功率<p/>
 * 优化 整体代码结构<p/>
 * 增加 中文 usage
 * <p>
 * 1.0.7<p>
 * 优化 程序出现异常后重新开始任务的最大次数改为 5 次<p/>
 * <p>
 * 1.0.6<p>
 * 增加 捕获到 socket 通信发生的异常后重新开始任务 {@link ClientSocket#run()}<p>
 * 优化 整体代码结构<p/>
 * <p>
 * 1.0.5<p>
 * 增加 连接失败超时和服务端无响应超时处理 超时后会进行一定次数内的重试<p/>
 * <p>
 * 1.0.4<p>
 * 增加 "-log" 参数, 保存日志到文件中 (保存到输出文件夹 socket_framework.log 中)<p>
 * 优化 "-h" 改为必填参数<p>
 * 优化 当输出文件夹不存在时创建该文件夹<p>
 * 注意 无法从安卓设备 pull 一个文件夹到电脑某个磁盘的根目录 (adb原因)<p/>
 * <p>
 * 1.0.3<p>
 * 增加 "-adb" 参数, 自定义 adb.exe 的路径<p>
 * 修复 某些情况下无法连接到服务端 {@link ClientSocket#connect()}<p/>
 * <p>
 * 1.0.2<p>
 * 增加 "-ext" 参数 (附加信息)<p/>
 * <p>
 * 1.0.1<p>
 * 优化 使用 "adb -s xxx:xxx" 命令 (多设备连接的情况指定某个设备)<p>
 * 增加 任务完成回调 isSuccess 参数<p/>
 * <p>
 * 1.0.0<p>
 * 第一版<p>
 */
public class Main {

    private static final String VERSION_NAME = "1.0.9";
    private static final String VERSION_CODE = "20190412";

    // 是否显示执行的命令
    public static final boolean DEBUG = false;

    private static final String DEFAULT_OUTPUT_DIR_NAME = "socket_framework";
    private static final String LOG_FILE_NAME = "socket_framework.log";

    public static void main(String[] args) {
        //the must params
        if (args.length < 6) showUsage();

        Params params = new Params();
        int argstart = 0;

        while (argstart < args.length && args[argstart].startsWith("-")) {
            if ("-h".equals(args[argstart])) {
                params.device = args[++argstart];
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
                parseExtMsg(params, args[++argstart]);
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
        if (isEmpty(p.outputDir)) {
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

        // adb pull windows磁盘根目录问题
        if (isDisKRootDir(p.outputDir)) {
            String dir = p.outputDir;
            if (dir.contains("\\")) {
                p.outputDir = dir + DEFAULT_OUTPUT_DIR_NAME;
            } else {
                p.outputDir = dir + "\\" + DEFAULT_OUTPUT_DIR_NAME;
            }
        }

        System.out.println(p.outputDir);

        File outFile = new File(p.outputDir);
        if (!outFile.exists()) {
            if (!outFile.mkdirs()) {
                throw new ClientException("create output dir fail");
            }
        }

        //日志
        Logger.init(p.saveLog, new File(outFile, LOG_FILE_NAME));
    }

    // extMsg 可以为扩展参数文件路径 或者 扩展参数字符串
    private static void parseExtMsg(Params p, String extMsg) {
        String extMsg0 = extMsg;
        if (isExistFile(extMsg)) {
            extMsg0 = readTextFile(extMsg);
        }
        p.extMsg = extMsg0;
    }

    static void showUsage() {
        System.out.println("\nPC_Android_SocketFramework\n");
        if (Locale.getDefault() == Locale.CHINA) {
            usage_cn();
        } else {
            usage_en();
        }
        System.exit(0);
    }

    private static void usage_en() {
        System.out.println(String.format("version:\n" +
                "  v%s(%s)", VERSION_NAME, VERSION_CODE));
        System.out.println("usage:\n" +
                "  [must params]:\n" +
                "  [-h   <serial num or 'host:port'>]   connect to adb debug, USB or WIFI\n" +
                "  [-am  <am command's param>]          like com.xxx.xxx/.xxxActivity, launch server app\n" +
                "  [-in  <input dir>]                   save all data\n" +
                "  \n" +
                "  (optional params):\n" +
                "  (-out <output dir>)                  default=>input dir's parent dir\n" +
                "  (-adb <custom adb path>)             default=>use system env adb\n" +
                "  (-ext <extended message>)            it's string or file path, send to server before push data\n" +
                "  (-log)                               whether to save log, save to output dir\n" +
                "  (-cp  <client port>)                 default=>6789\n" +
                "  (-sp  <server port >)                default=>9876");
    }

    private static void usage_cn() {
        System.out.println(String.format("版本：\n" +
                "  v%s(%s)", VERSION_NAME, VERSION_CODE));
        System.out.println("用法：\n" +
                "  [必填参数]：\n" +
                "  [-h   <序列号或'host:port'>] 连接到 ADB 调试，USB 或 WIFI\n" +
                "  [-am  <am命令的参数>]        '包名/启动全类名'，启动服务端程序\n" +
                "  [-in  <输入文件夹>]          保存所有的待操作数据\n" +
                "  \n" +
                "  (可选参数)：\n" +
                "  (-out <输出文件夹>)      默认为：输入文件夹的父目录\n" +
                "  (-adb <自定义adb路径>)   默认为：系统环境变量的 adb\n" +
                "  (-ext <扩展信息>)        支持字符串和文件传入，push 数据前发送给服务端\n" +
                "  (-log)                   是否保存日志，保存到输出文件夹\n" +
                "  (-cp  <客户端端口号>)    默认为：6789\n" +
                "  (-sp  <服务端端口号>)    默认为：9876");
    }
}
