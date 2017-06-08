package com.qyl.vip.proxy.base;

import com.qyl.vip.proxy.GetRequest;
import com.qyl.vip.proxy.exception.VideoProxyException;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by qiuyunlong on 17/6/3.
 */

public interface IProxy {

    void processRequest(GetRequest request, Socket socket) throws IOException, VideoProxyException;

    void shutdown();

}
