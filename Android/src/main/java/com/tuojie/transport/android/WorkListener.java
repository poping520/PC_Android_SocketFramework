package com.tuojie.transport.android;

/**
 * {@link WorkActivity#doWork(String, String, WorkListener)}
 * 使用本接口向客户端发送任务的相关信息
 *
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/6/25 10:58
 */
public interface WorkListener {

    /**
     * 调用此方法，向PC发送任务进度
     */
    void onProgress(String progressMsg);

    /**
     * 调用此方法，向PC发送任务完成消息
     *
     * @param isSuccess 任务是否执行成功
     */
    void onWorkComplete(boolean isSuccess);

    /**
     * 调用此方法，向PC发送程序异常消息
     */
    void onWorkError(String errorMsg);
}
