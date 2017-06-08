package com.qyl.vip.download.base;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public interface IDownloader {

    boolean isRunning();

    void start();

    void pause();

    void cancel();

    void onDestroy();


}
