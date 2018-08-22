package com.zhangteng.projectionscreenplayer.MediaCodec;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.nio.ByteBuffer;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class VideoMediaCodec {
    private MediaCodec mCodec;
    private String mime_type = "video/avc";
    //屏幕相关
    private int screen_width = 720;
    private int screen_height = 1280;
    private boolean useSpsPPs = false;
    private SurfaceHolder mHolder;
    byte[] header_sps = {0, 0, 0, 1, 103, 66, 0, 42, (byte) 149, (byte) 168, 30, 0, (byte) 137, (byte) 249, 102, (byte) 224, 32, 32, 32, 64};
    byte[] header_pps = {0, 0, 0, 1, 104, (byte) 206, 60, (byte) 128, 0, 0, 0, 1, 6, (byte) 229, 1, (byte) 151, (byte) 128};
    private MediaFormat mediaformat;

    public VideoMediaCodec(SurfaceHolder holder, byte[] sps, byte[] pps) {
        this.mHolder = holder;
        if (sps != null && pps != null) {
            useSpsPPs = true;
        }
        if (sps != null) {
            header_sps = sps;
        }
        if (pps != null) {
            header_pps = pps;
        }
    }

    private int bit_rate = 2000000;
    private int frame_rate = 20;
    private int key_frame_interval = 1;
    private int video_fps = 30;

    private void initialCodec() {
        try {
            //通过多媒体格式名创建一个可用的解码器
            mCodec = MediaCodec.createDecoderByType(mime_type);
            //初始化编码器
            mediaformat = MediaFormat.createVideoFormat(mime_type, screen_width, screen_height);
            //获取h264中的pps及sps数据
            if (useSpsPPs) {
                mediaformat.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
                mediaformat.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
            }
            mediaformat.setInteger(MediaFormat.KEY_BIT_RATE, bit_rate);
            mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, frame_rate);
            mediaformat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, key_frame_interval);
            mediaformat.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR);
            mCodec.configure(mediaformat, mHolder.getSurface(), null, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaCodec getCodec() {
        return mCodec;
    }

    public void start() {
        mCodec.start();
    }

    public void release() {
        mCodec.stop();
        mCodec.release();
    }

    /**
     * 设置视频FPS
     *
     * @param fps
     */
    public VideoMediaCodec setVideoFPS(int fps) {
        this.video_fps = fps;
        return this;
    }

    /**
     * 设置视屏编码采样率
     *
     * @param bit
     */
    public VideoMediaCodec setVideoBit(int bit) {
        this.bit_rate = bit;
        return this;
    }

    public VideoMediaCodec setFrameRate(int frameRate) {
        this.frame_rate = frameRate;
        return this;
    }

    public VideoMediaCodec setFrameInternal(int frameInternal) {
        this.key_frame_interval = frameInternal;
        return this;
    }

    public VideoMediaCodec setScreenWidth(int screen_width) {
        this.screen_width = screen_width;
        return this;
    }

    public VideoMediaCodec setScreenHeight(int screen_height) {
        this.screen_height = screen_height;
        return this;
    }

    public VideoMediaCodec setMimeType(String mime_type) {
        this.mime_type = mime_type;
        return this;
    }

    public VideoMediaCodec build() {
        initialCodec();
        return this;
    }
}
