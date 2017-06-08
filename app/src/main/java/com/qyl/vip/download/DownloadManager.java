package com.qyl.vip.download;

import android.support.annotation.NonNull;

import com.qyl.vip.download.base.IDownloader;
import com.qyl.vip.download.impl.DownloaderImpl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public class DownloadManager {

    private static final String TAG = DownloadManager.class.getSimpleName();

    private Map<String, IDownloader> mDownloadMap;

    private DownloadConfig mDownloadConfig;

    private ExecutorService mExecutorService;

    private static class SingletonHolder {
        private static final DownloadManager INSTANCE = new DownloadManager();
    }

    private DownloadManager (){
        mDownloadMap = new LinkedHashMap<>();
    }

    public static final DownloadManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(@NonNull DownloadConfig config) {
        if (config.getThreadNum() > config.getMaxThreadNum()) {
            throw new IllegalArgumentException("thread num must < max thread num");
        }
        mDownloadConfig = config;
        mExecutorService = Executors.newFixedThreadPool(mDownloadConfig.getMaxThreadNum());
    }


    public void download(DownloadRequest request, DownloadCallBack callBack) {
        if (mDownloadMap.containsKey(request.getUri())) {
            IDownloader downloader = mDownloadMap.get(request.getUri());
            if (downloader.isRunning()){
                downloader.cancel();
                mDownloadMap.remove(request.getUri());
            }else {
                downloader.start();
            }

        }else {
            IDownloader downloader = new DownloaderImpl(request, mExecutorService,
                    mDownloadConfig, callBack, request.getProcessor());
            mDownloadMap.put(request.getUri(), downloader);
            downloader.start();
        }

    }

}
