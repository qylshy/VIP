package com.qyl.vip.download.base;

import com.qyl.vip.download.exception.DownloadException;

/**
 * Created by qiuyunlong on 17/6/2.
 */
public interface IConnectTask extends Runnable{

    void pause();

    void cancel();

    boolean isConnecting();

    boolean isConnected();

    boolean isPaused();

    boolean isCanceled();

    boolean isFailed();


    interface OnConnectListener {
        void onConnecting();

        void onConnected(long time, long length, boolean isAcceptRanges);

        void onConnectPaused();

        void onConnectCanceled();

        void onConnectFailed(DownloadException de);
    }



}
