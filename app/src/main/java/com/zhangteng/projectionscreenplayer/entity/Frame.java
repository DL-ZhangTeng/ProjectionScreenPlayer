package com.zhangteng.projectionscreenplayer.entity;

/**
 * Created by swing on 2018/8/22.
 */
public class Frame {
    public static final int SPSPPS = 2;
    public static final int KEY_FRAME = 4;
    public static final int NORMAL_FRAME = 5;
    public static final int AUDIO_FRAME = 6;
    private byte[] bytes;
    private int type;
    private byte[] sps;
    private byte[] pps;

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getSps() {
        return sps;
    }

    public void setSps(byte[] sps) {
        this.sps = sps;
    }

    public byte[] getPps() {
        return pps;
    }

    public void setPps(byte[] pps) {
        this.pps = pps;
    }
}
