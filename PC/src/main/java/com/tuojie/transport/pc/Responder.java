package com.tuojie.transport.pc;

/**
 * @author WangKZ
 * create on 2018/6/23 15:26
 */
public interface Responder {

    void onResponse(Events.FromAndroid event, String msg);
}
