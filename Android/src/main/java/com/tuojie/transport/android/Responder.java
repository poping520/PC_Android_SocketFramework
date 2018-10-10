package com.tuojie.transport.android;

/**
 * @author WangKZ
 * create on 2018/6/23 12:30
 */
public interface Responder {

    /**
     * 接收来自PC端的响应信息
     *
     * @param event 事件类型
     * @param msg   携带信息
     */
    void onResponse(Events.FromPC event, String msg);
}
