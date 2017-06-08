package com.qyl.vip.download.base;

import com.qyl.vip.download.exception.DownloadException;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public interface IDownloadTask extends Runnable{

    void pause();

    void cancel();

    boolean isDownloading();

    boolean isComplete();

    boolean isPaused();

    boolean isCanceled();

    boolean isFailed();

    interface OnDownloadListener {

        void onDownloadConnecting();

        void onDownloadProgress(long finished, long length);

        void onDownloadCompleted();

        void onDownloadPaused();

        void onDownloadCanceled();

        void onDownloadFailed(DownloadException de);
    }


}
