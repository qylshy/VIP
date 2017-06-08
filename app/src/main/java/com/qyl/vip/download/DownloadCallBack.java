package com.qyl.vip.download;


import com.qyl.vip.download.exception.DownloadException;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public interface DownloadCallBack {

    void onStarted();

    void onConnecting();

    void onConnected(long total, boolean isRangeSupport);

    void onProgress(long finished, long total, int progress);

    void onCompleted(String path);

    void onDownloadPaused();

    void onDownloadCanceled();

    void onFailed(DownloadException e);

}
