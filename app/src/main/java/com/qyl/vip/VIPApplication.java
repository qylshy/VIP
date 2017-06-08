package com.qyl.vip;

import android.app.Application;
import android.content.Context;

import com.qyl.vip.download.DownloadConfig;
import com.qyl.vip.download.DownloadManager;
import com.qyl.vip.proxy.VideoProxyServer;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public class VIPApplication extends Application {

    private VideoProxyServer proxy;


    @Override
    public void onCreate() {
        super.onCreate();
        initDownloader();
    }

    private void initDownloader() {
        DownloadConfig config = new DownloadConfig();
        config.setMaxThreadNum(10);
        config.setThreadNum(5);
        DownloadManager.getInstance().init(config);
    }

    public static VideoProxyServer getProxy(Context context) {
        VIPApplication app = (VIPApplication) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private VideoProxyServer newProxy() {
        return new VideoProxyServer();
    }

}
