package com.tuojie.transport.pc;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/10/24 14:24
 */
class Params {

    @MustParam
    String hostPort;

    @MustParam
    String amParam;

    @MustParam
    String inputDir;

    String outputDir;

    String adbEnv;

    String extMsg;

    boolean saveLog;

    int clientPort;

    int serverPort;

    String serverWorkDir;
}
