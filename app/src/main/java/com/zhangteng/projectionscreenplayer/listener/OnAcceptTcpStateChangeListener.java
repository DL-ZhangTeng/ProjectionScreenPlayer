package com.zhangteng.projectionscreenplayer.listener;

public interface OnAcceptTcpStateChangeListener {
    void acceptTcpConnect();    //接收到客户端的Tcp连接

    void acceptTcpDisConnect(Exception e); //接收到客户端的Tcp断开连接
}
