package com.qyl.vip.proxy;

import android.util.Log;

import com.qyl.vip.proxy.base.AbsProxy;
import com.qyl.vip.proxy.base.IProxyProcessor;
import com.qyl.vip.proxy.base.Source;
import com.qyl.vip.proxy.exception.VideoProxyException;
import com.qyl.vip.proxy.impl.FileSource;
import com.qyl.vip.proxy.impl.PingProxy;
import com.qyl.vip.proxy.impl.PingSource;
import com.qyl.vip.proxy.impl.VideoProxy;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import static com.qyl.vip.proxy.VideoProxyServer.PING_REQUEST;
import static com.qyl.vip.proxy.utils.Preconditions.checkNotNull;
import static com.qyl.vip.proxy.utils.VideoProxyUtil.LOG_TAG;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public class VideoProxyClient {

    private static final String TAG = VideoProxyClient.class.getSimpleName();

    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final String url;
    private volatile AbsProxy proxy;
    private IProxyProcessor proxyProcessor;

    public VideoProxyClient(String url) {
        this.url = checkNotNull(url);
    }

    public void processRequest(GetRequest request, Socket socket) throws VideoProxyException, IOException {
        startProcessRequest();
        try {
            requestCount.incrementAndGet();
            proxy.processRequest(request, socket);
        }catch (Exception e){
            Log.e(LOG_TAG, "processRequest==="+e.getMessage());
        } finally {
            finishProcessRequest();
        }
    }

    private synchronized void startProcessRequest() throws VideoProxyException {
        proxy = proxy == null ? newVideoProxy() : proxy;
    }

    private synchronized void finishProcessRequest() {
        Log.v(LOG_TAG, "finishProcessRequest==requestCount=" + requestCount.get());
        if (requestCount.decrementAndGet() <= 0) {
            proxy.shutdown();
            proxy = null;
        }
    }

    public void shutdown() {
        if (proxy != null) {
            proxy.shutdown();
            proxy = null;
        }
        if (proxyProcessor != null) {
            proxyProcessor = null;
        }
        requestCount.set(0);
    }

    public int getClientsCount() {
        return requestCount.get();
    }

    private AbsProxy newVideoProxy() throws VideoProxyException {
        Log.e(TAG, "newVideoProxy url " + url);
        Source source;
        if (PING_REQUEST.equals(url)) {
            source = new PingSource(url);
            proxy = new PingProxy(source);
            return proxy;
        }else {
            source = new FileSource(new File(url));
            proxy = new VideoProxy(source);
            if (proxyProcessor != null) {
                proxy.setProxyProcessor(proxyProcessor);
            }
            return proxy;
        }

    }

    public void registerProcessor(IProxyProcessor processor) {
        this.proxyProcessor = processor;
    }
}
