package com.qyl.vip.proxy.impl;

import com.qyl.vip.proxy.GetRequest;
import com.qyl.vip.proxy.base.AbsProxy;
import com.qyl.vip.proxy.base.Source;
import com.qyl.vip.proxy.exception.VideoProxyException;

import java.io.IOException;

/**
 * Created by qiuyunlong on 17/6/3.
 */

public class PingProxy extends AbsProxy {

    public PingProxy(Source source) {
        super(source);
    }

    @Override
    protected String newResponseHeaders(GetRequest request) throws IOException, VideoProxyException{
        return "HTTP/1.1 200 OK\n\n";
    }

}
