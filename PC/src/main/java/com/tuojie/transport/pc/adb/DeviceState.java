package com.tuojie.transport.pc.adb;

/**
 * <pre>
 * desc: 设备 ADB 连接状态
 * time: 2019/4/3
 * </pre>
 */
public enum DeviceState {

    /**
     * 表示 设备已连接
     */
    DEVICE("device"),

    /**
     * 表示 PC未获得设备的授权
     */
    UNAUTHORIZED("unauthorized"),

    /**
     * 表示 设备离线 未连接
     */
    OFFLINE("offline"),

    /**
     * 其他状态
     */
    OTHER("unknown");

    private String mState;

    DeviceState(String state) {
        this.mState = state;
    }

    @Override
    public String toString() {
        return mState;
    }

    static DeviceState getDeviceState(String state) {
        DeviceState[] dss = DeviceState.values();
        for (DeviceState ds : dss) {
            if (ds.mState.equalsIgnoreCase(state)) {
                return ds;
            }
        }
        return OTHER;
    }
}