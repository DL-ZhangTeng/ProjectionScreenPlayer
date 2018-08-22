package com.zhangteng.projectionscreenplayer.server;

import android.util.Log;

import com.zhangteng.projectionscreenplayer.constant.Constant;
import com.zhangteng.projectionscreenplayer.listener.OnAcceptBuffListener;
import com.zhangteng.projectionscreenplayer.listener.OnAcceptTcpStateChangeListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {
    private ServerSocket serverSocket;
    private int tcpPort = Constant.port;
    private boolean isAccept = true;
    private OnAcceptBuffListener mListener;
    private OnAcceptTcpStateChangeListener mConnectListener;
    private AcceptH264MsgThread acceptH264MsgThread;

    public void startServer() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    serverSocket = new ServerSocket();
                    serverSocket.setReuseAddress(true);
                    InetSocketAddress socketAddress = new InetSocketAddress(tcpPort);
                    serverSocket.bind(socketAddress);
                    while (isAccept) {
                        Socket socket = serverSocket.accept();
                        acceptH264MsgThread = new AcceptH264MsgThread(socket.getInputStream(), socket.getOutputStream(), mListener, mConnectListener);
                        acceptH264MsgThread.start();
                    }
                } catch (Exception e) {
                    Log.e("TcpServer", "" + e.toString());
                }

            }
        }.start();
    }

    public void setOnAccepttBuffListener(OnAcceptBuffListener listener) {
        this.mListener = listener;
    }

    public void setOnTcpConnectListener(OnAcceptTcpStateChangeListener listener) {
        this.mConnectListener = listener;
    }

    public void stopServer() {
        this.mListener = null;
        isAccept = false;
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    if (acceptH264MsgThread != null) acceptH264MsgThread.shutdown();
                    if (serverSocket != null) serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
