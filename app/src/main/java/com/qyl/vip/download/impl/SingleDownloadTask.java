package com.qyl.vip.download.impl;

import com.qyl.vip.download.IProcessor;
import com.qyl.vip.download.bean.DownloadInfo;
import com.qyl.vip.download.bean.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public class SingleDownloadTask extends AbsDownloadTask {

    public SingleDownloadTask(DownloadInfo mDownloadInfo, ThreadInfo mThreadInfo, OnDownloadListener mOnDownloadListener) {
        super(mDownloadInfo, mThreadInfo, mOnDownloadListener);
    }

    public SingleDownloadTask(DownloadInfo mDownloadInfo, ThreadInfo mThreadInfo, OnDownloadListener mOnDownloadListener, IProcessor processor) {
        super(mDownloadInfo, mThreadInfo, mOnDownloadListener, processor);
    }

    @Override
    protected void saveThreadInfo(ThreadInfo info) {

    }

    @Override
    protected int getResponseCode() {
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    protected void updateThreadInfo(ThreadInfo info) {

    }

    @Override
    protected Map<String, String> getHttpHeaders(ThreadInfo info) {
        return null;
    }

    @Override
    protected RandomAccessFile getFile(File dir, String name, long offset) throws IOException {
        File file = new File(dir, name);
        RandomAccessFile raf = new RandomAccessFile(file, "rwd");
        raf.seek(0);
        return raf;
    }
}
