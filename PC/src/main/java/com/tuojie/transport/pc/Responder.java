package com.tuojie.transport.pc;

/**
 * @author WangKZ
 * create on 2018/6/23 15:26
 */
public interface Responder {

    /**
     * 接收来自Android端的响应信息
     *
     * @param event 事件类型
     * @param msg   携带信息
     */
    void onResponse(Events.FromAndroid event, String msg);
}
