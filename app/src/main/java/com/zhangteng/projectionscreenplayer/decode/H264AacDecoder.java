package com.zhangteng.projectionscreenplayer.decode;

import android.util.Log;

import com.zhangteng.projectionscreenplayer.entity.Frame;


/**
 * @Desc 解析H264和AAC的Decoder
 */

public class H264AacDecoder {
    private static final String TAG = "H264AacDecoder";

    // Coded slice of a non-IDR picture slice_layer_without_partitioning_rbsp( )
    public final static int NonIDR = 1;
    // Coded slice of an IDR picture slice_layer_without_partitioning_rbsp( )
    public final static int IDR = 5;
    // Supplemental enhancement information (SEI) sei_rbsp( )
    public final static int SEI = 6;
    // Sequence parameter set seq_parameter_set_rbsp( )
    public final static int SPS = 7;
    // Picture parameter set pic_parameter_set_rbsp( )
    public final static int PPS = 8;
    // Access unit delimiter access_unit_delimiter_rbsp( )
    public final static int AccessUnitDelimiter = 9;

    //
    public final static int AUDIO = -2;

    private byte[] mPps;
    private byte[] mSps;
    private byte[] mKf;

    public OnVideoListener mListener;

    public void setOnVideoListener(OnVideoListener listener) {
        this.mListener = listener;
    }

    public void decodeH264(byte[] frame) {
        //todo h264帧解码
        boolean isKeyFrame = false;
        if (frame == null) {
            Log.e(TAG, "annexb not match.");
            return;
        }
        // ignore the nalu type aud(9)
        if (isAccessUnitDelimiter(frame)) {
            return;
        }
        //for pps and sps and keyframe
        if (isPpsAndSpsAndKeyFrame(frame)) {
            if (mPps != null && mSps != null && mKf != null) {
                mListener.onSpsPps(mSps, mPps);
                mListener.onVideo(mKf, Frame.KEY_FRAME);
            }
            return;
        }
        //for pps and sps
        if (isPpsAndSps(frame)) {
            if (mPps != null && mSps != null) {
                mListener.onSpsPps(mSps, mPps);
            }
            return;
        }
        // for pps
        if (isPps(frame)) {
            mPps = frame;
            if (mPps != null && mSps != null) {
                mListener.onSpsPps(mSps, mPps);
            }
            return;
        }
        // for sps
        if (isSps(frame)) {
            mSps = frame;
            if (mPps != null && mSps != null) {
                mListener.onSpsPps(mSps, mPps);
            }
            return;
        }
        if (isAudio(frame)) {
            byte[] temp = new byte[frame.length - 4];
            System.arraycopy(frame, 4, temp, 0, frame.length - 4);
            mListener.onVideo(temp, Frame.AUDIO_FRAME);
            return;
        }
        // for IDR frame
        if (isKeyFrame(frame)) {
            isKeyFrame = true;
        } else {
            isKeyFrame = false;
        }
        mListener.onVideo(frame, isKeyFrame ? Frame.KEY_FRAME : Frame.NORMAL_FRAME);
    }

    private boolean isAudio(byte[] frame) {
        if (frame.length < 5) {
            return false;
        }
        return frame[4] == ((byte) 0xFF) && frame[5] == ((byte) 0xF9);
    }

    private boolean isSps(byte[] frame) {
        if (frame.length < 5) {
            return false;
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        int nal_unit_type = (frame[4] & 0x1f);
        return nal_unit_type == SPS;
    }

    private boolean isPps(byte[] frame) {
        if (frame.length < 5) {
            return false;
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        int nal_unit_type = (frame[4] & 0x1f);
        return nal_unit_type == PPS;
    }

    private boolean isKeyFrame(byte[] frame) {
        if (frame.length < 5) {
            return false;
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        int nal_unit_type = (frame[4] & 0x1f);
        return nal_unit_type == IDR;
    }

    private boolean isPpsAndSpsAndKeyFrame(byte[] frame) {
        if (frame.length <= 4 + 7 + 19) {
            return false;
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        int pps_type = (frame[4] & 0x1f);
        int sps_type = (frame[4 + 7] & 0x1f);
        int key_type = (frame[4 + 7 + 19] & 0x1f);
        if ((pps_type == PPS && sps_type == SPS && (key_type == IDR || key_type == NonIDR || key_type == SEI))) {
            mPps = new byte[7];
            mSps = new byte[19];
            mKf = new byte[frame.length - 7 - 19];
            System.arraycopy(frame, 0, mPps, 0, mPps.length);
            System.arraycopy(frame, mPps.length, mSps, 0, mSps.length);
            System.arraycopy(frame, 7 + 19, mKf, 0, mKf.length);
            return true;
        }
        pps_type = (frame[4 + 19] & 0x1f);
        sps_type = (frame[4] & 0x1f);
        key_type = (frame[4 + 7 + 19] & 0x1f);
        if ((pps_type == PPS && sps_type == SPS && (key_type == IDR || key_type == NonIDR || key_type == SEI))) {
            mPps = new byte[7];
            mSps = new byte[19];
            mKf = new byte[frame.length - 7 - 19];
            System.arraycopy(frame, 0, mSps, 0, mSps.length);
            System.arraycopy(frame, mSps.length, mPps, 0, mPps.length);
            System.arraycopy(frame, 7 + 19, mKf, 0, mKf.length);
            return true;
        }
        return false;
    }

    private boolean isPpsAndSps(byte[] frame) {
        if (frame.length <= 7 + 19) {
            return false;
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        int pps_type = (frame[4] & 0x1f);
        int sps_type = (frame[4 + 7] & 0x1f);
        if ((pps_type == PPS && sps_type == SPS)) {
            mPps = new byte[7];
            mSps = new byte[19];
            System.arraycopy(frame, 0, mPps, 0, mPps.length);
            System.arraycopy(frame, mPps.length, mSps, 0, mSps.length);
            return true;
        }
        pps_type = (frame[4 + 19] & 0x1f);
        sps_type = (frame[4] & 0x1f);
        if ((pps_type == PPS && sps_type == SPS)) {
            mPps = new byte[7];
            mSps = new byte[19];
            System.arraycopy(frame, 0, mSps, 0, mSps.length);
            System.arraycopy(frame, mSps.length, mPps, 0, mPps.length);
            return true;
        }
        return false;
    }

    private static boolean isAccessUnitDelimiter(byte[] frame) {
        if (frame.length < 5) {
            return false;
        }
        // 5bits, 7.3.1 NAL unit syntax,
        // H.264-AVC-ISO_IEC_14496-10.pdf, page 44.
        //  7: SPS, 8: PPS, 5: I Frame, 1: P Frame
        int nal_unit_type = (frame[4] & 0x1f);
        return nal_unit_type == AccessUnitDelimiter;
    }

    public interface OnVideoListener {
        void onSpsPps(byte[] sps, byte[] pps);

        void onVideo(byte[] video, int type);
    }
}
