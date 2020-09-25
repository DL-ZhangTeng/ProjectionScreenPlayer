package com.zhangteng.projectionscreenplayer;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.zhangteng.projectionscreenplayer.MediaCodec.VideoMediaCodec;
import com.zhangteng.projectionscreenplayer.constant.Constant;
import com.zhangteng.projectionscreenplayer.decode.DecodeThread;
import com.zhangteng.projectionscreenplayer.entity.Frame;
import com.zhangteng.projectionscreenplayer.listener.OnAcceptBuffListener;
import com.zhangteng.projectionscreenplayer.listener.OnAcceptTcpStateChangeListener;
import com.zhangteng.projectionscreenplayer.server.NormalPlayQueue;
import com.zhangteng.projectionscreenplayer.server.TcpServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by swing on 2018/8/22.
 */
public class PlayerActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private SurfaceView mSurface = null;
    private SurfaceHolder mSurfaceHolder;
    private DecodeThread mDecodeThread;

    private NormalPlayQueue mPlayqueue;
    private TcpServer tcpServer;
    private VideoMediaCodec videoMediaCodec;
    private FileOutputStream fos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_player);
        mSurface = findViewById(R.id.surfaceview);
        initialFIle();
        startServer();
        mSurfaceHolder = mSurface.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initialMediaCodec(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (videoMediaCodec != null) {
                    videoMediaCodec.release();
                }
            }
        });
    }

    private void initialMediaCodec(SurfaceHolder holder) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        videoMediaCodec = new VideoMediaCodec(holder, Constant.header_sps, Constant.header_pps)
                .setMimeType(Constant.MIME_TYPE)
                .setFrameInternal(Constant.KEY_I_FRAME_INTERVAL)
                .setFrameRate(Constant.KEY_FRAME_RATE)
                .setVideoBit(Constant.KEY_BIT_RATE)
                .setVideoFPS(Constant.VIDEO_FPS)
                .setScreenHeight(dm.heightPixels)
                .setScreenWidth(dm.widthPixels)
                .build();
        videoMediaCodec.start();
        mDecodeThread = new DecodeThread(videoMediaCodec.getCodec(), mPlayqueue);
        mDecodeThread.start();
    }

    private void startServer() {
        mPlayqueue = new NormalPlayQueue();
        tcpServer = new TcpServer();
        tcpServer.setOnAccepttBuffListener(new MyAcceptH264Listener());
        tcpServer.setOnTcpConnectListener(new MyAcceptTcpStateListener());
        tcpServer.startServer();
    }

    //接收到H264buff的回调
    class MyAcceptH264Listener implements OnAcceptBuffListener {

        @Override
        public void acceptBuff(Frame frame) {
            mPlayqueue.putByte(frame);
        }
    }

    //客户端Tcp连接状态的回调...
    class MyAcceptTcpStateListener implements OnAcceptTcpStateChangeListener {

        @Override
        public void acceptTcpConnect() {    //接收到客户端的连接...
            Log.e(TAG, "accept a tcp connect...");
        }

        @Override
        public void acceptTcpDisConnect(Exception e) {  //客户端的连接断开...
            Log.e(TAG, "acceptTcpConnect exception = " + e.toString());
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (mPlayqueue != null) {
            mPlayqueue.stop();
        }
        if (videoMediaCodec != null) {
            videoMediaCodec.release();
        }
        if (mDecodeThread != null) {
            mDecodeThread.shutdown();
        }
        if (tcpServer != null) {
            tcpServer.stopServer();
        }
    }

    private void initialFIle() {
        File file = new File(Environment.getExternalStorageDirectory(), "test.aac");
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            fos = new FileOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
