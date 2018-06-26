package com.tuojie.transport.android;

import android.util.SparseArray;

/**
 * 事件类型
 *
 * @author WangKZ
 * @version 1.0.0
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
         * pull数据完成
         */
        PULL_DATA_FINISH(0x2);


        private int mCode;

        FromPC(int code) {
            this.mCode = code;
        }

        public int getCode() {
            return mCode;
        }

        public static FromPC getEvent(int code) {
            SparseArray<FromPC> array = new SparseArray<>();
            FromPC[] events = FromPC.values();
            for (FromPC e : events) {
                array.put(e.mCode, e);
            }
            return array.get(code);
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
         * 完成工作
         */
        WORK_COMPLETE(0x12),

        /**
         * 发生错误
         */
        ERROR_OCCURED(0x13);


        private int mCode;

        FromAndroid(int code) {
            this.mCode = code;
        }

        public int getCode() {
            return mCode;
        }
    }
}
