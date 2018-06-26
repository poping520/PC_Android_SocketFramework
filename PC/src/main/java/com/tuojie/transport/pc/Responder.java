package com.tuojie.transport.pc;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/6/23 15:26
 */
public interface Responder {

    void onResponse(Events.FromAndroid event, String msg);
}
