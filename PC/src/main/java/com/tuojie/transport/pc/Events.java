package com.tuojie.transport.pc;

import java.util.HashMap;
import java.util.Map;

/**
 * 事件类型
 *
 * @author WangKZ
 * create on 2018/6/23 12:52
 */
public class Events {

    /**
     * 来自PC端
     */
    public enum FromPC {
        /**
         * 连接请求
         */
        CONNECT_REQUEST(0x0),

        /**
         * push数据完成
         */
        PUSH_DATA_FINISH(0x1),

        /**
         * 关闭服务端程序
         */
        CLOSE_SERVER_APP(0x2),

        /**
         * 扩展消息
         */
        EXTENDED_MESSAGE(0x3);


        private int mCode;

        FromPC(int code) {
            this.mCode = code;
        }

        public int getCode() {
            return mCode;
        }
    }

    /**
     * 来自Android端
     */
    public enum FromAndroid {

        /**
         * 连接成功
         */
        CONNECT_SUCCESS(0x10),

        /**
         * 进度
         */
        WORK_PROGRESS(0x11),

        /**
         * 完成工作 成功
         */
        WORK_COMPLETE_SUCC(0x12),

        /**
         * 完成工作 失败
         */
        WORK_COMPLETE_FAIL(0x13),

        /**
         * 发生错误
         */
        ERROR_OCCURED(0x14);


        private int mCode;

        FromAndroid(int code) {
            this.mCode = code;
        }

        public int getCode() {
            return mCode;
        }

        public static FromAndroid getEvent(int code) {
            Map<Integer, FromAndroid> map = new HashMap<>();
            FromAndroid[] events = FromAndroid.values();

            for (FromAndroid event : events) {
                map.put(event.getCode(), event);
            }
            return map.get(code);
        }
    }
}
