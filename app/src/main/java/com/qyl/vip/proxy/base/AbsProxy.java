package com.qyl.vip.proxy.base;

import android.util.Log;

import com.qyl.vip.proxy.GetRequest;
import com.qyl.vip.proxy.exception.VideoProxyException;
import com.qyl.vip.proxy.utils.VideoProxyUtil;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static com.qyl.vip.proxy.utils.VideoProxyUtil.DEFAULT_BUFFER_SIZE;
import static com.qyl.vip.proxy.utils.VideoProxyUtil.LOG_TAG;

/**
 * Created by qiuyunlong on 17/6/3.
 */

public abstract class AbsProxy implements IProxy{

    protected Source source;
    private IProxyProcessor proxyProcessor;


    public AbsProxy(Source source) {
        this.source = source;
    }

    @Override
    public void processRequest(GetRequest request, Socket socket) throws IOException, VideoProxyException {
        OutputStream out = new BufferedOutputStream(socket.getOutputStream());
        String responseHeaders = newResponseHeaders(request);
        Log.v(LOG_TAG, "processRequest responseHeaders="+responseHeaders);
        out.write(responseHeaders.getBytes("UTF-8"));

        long offset = request.rangeOffset;
        responseWithProxy(out, offset);
    }

    @Override
    public void shutdown() {
        Log.d(LOG_TAG, "Shutdown proxy for " + source);
        try {
            source.close();
        } catch (VideoProxyException e) {
            onError(e);
        }
    }

    protected void onError(final Throwable e) {
        Log.e(LOG_TAG, "Proxy error", e);
    }

    private void responseWithProxy(OutputStream out, long offset) throws VideoProxyException, IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int readBytes;
        while ((readBytes = read(buffer, offset, buffer.length)) != -1) {
            out.write(buffer, 0, readBytes);
            offset += readBytes;
        }
        out.flush();
    }

    public int read(byte[] buffer, long offset, int length) throws VideoProxyException {
        VideoProxyUtil.assertBuffer(buffer, offset, length);
        //Log.v(LOG_TAG, "read ===offset" + offset + ", length="+length);

        int read = source.read(buffer, offset, length);

        if (proxyProcessor != null) {
            proxyProcessor.onProcess(buffer, offset, length);
        }

        return read;
    }

    public void setProxyProcessor(IProxyProcessor proxyProcessor) {
        this.proxyProcessor = proxyProcessor;
    }

    protected abstract String newResponseHeaders(GetRequest request) throws IOException, VideoProxyException;


}
