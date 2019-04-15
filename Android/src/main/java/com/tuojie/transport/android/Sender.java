package com.tuojie.transport.android;

/**
 * {@link WorkActivity#doWork(String, String, String, Sender)}
 * 使用本接口向客户端发送任务的相关信息
 *
 * @author WangKZ
 * create on 2018/6/25 10:58
 */
public interface Sender {

    /**
     * 向 PC 发送任务进度消息
     * <p>
     * {@link WorkActivity#registerUiListener(UiListener)}
     *
     * @param callbackToUi 是否回调给 UI 进度
     * @param progressMsg  消息
     */
    void sendProgress(boolean callbackToUi, String progressMsg);

    /**
     * 向 PC 发送任务进度消息
     * callback 默认为 false
     *
     * @param progressMsg 消息
     */
    default void sendProgress(String progressMsg) {
        sendProgress(false, progressMsg);
    }

    /**
     * 向 PC 发送任务进度消息
     * 格式化字符串
     */
    default void sendProgress(String fmt, Object... args) {
        sendProgress(false, fmt, args);
    }

    /**
     * 向 PC 发送任务进度消息
     * 格式化字符串
     */
    default void sendProgress(boolean callbackToUi, String fmt, Object... args) {
        sendProgress(callbackToUi, String.format(fmt, args));
    }

    /**
     * 向 PC 发送任务完成消息
     *
     * @param isSuccess 任务是否执行成功
     */
    void sendFinish(boolean isSuccess);

    /**
     * 向 PC 发送程序异常消息
     *
     * @param errorMsg 异常消息
     */
    void sendError(String errorMsg);


    /**
     * 向 PC 发送程序异常消息
     * 格式化字符串
     */
    default void sendError(String fmt, Object... args) {
        sendError(String.format(fmt, args));
    }
}
