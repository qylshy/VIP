package com.qyl.vip.proxy.impl;

import android.text.TextUtils;

import com.qyl.vip.proxy.GetRequest;
import com.qyl.vip.proxy.base.AbsProxy;
import com.qyl.vip.proxy.base.Source;
import com.qyl.vip.proxy.exception.VideoProxyException;

import java.io.IOException;

/**
 * Created by qiuyunlong on 17/6/3.
 */

public class VideoProxy extends AbsProxy {

    public VideoProxy(Source source) {
        super(source);
    }

    @Override
    protected String newResponseHeaders(GetRequest request) throws IOException, VideoProxyException {
        String mime = source.getMime();
        boolean mimeKnown = !TextUtils.isEmpty(mime);
        int length = source.length();
        boolean lengthKnown = length >= 0;
        long contentLength = request.partial ? length - request.rangeOffset : length;
        boolean addRange = lengthKnown && request.partial;
        return new StringBuilder()
                .append(request.partial ? "HTTP/1.1 206 PARTIAL CONTENT\n" : "HTTP/1.1 200 OK\n")
                .append("Accept-Ranges: bytes\n")
                .append(lengthKnown ? String.format("Content-Length: %d\n", contentLength) : "")
                .append(addRange ? String.format("Content-Range: bytes %d-%d/%d\n", request.rangeOffset, length - 1, length) : "")
                .append(mimeKnown ? String.format("Content-Type: %s\n", mime) : "")
                .append("\n") // headers end
                .toString();
    }


}
