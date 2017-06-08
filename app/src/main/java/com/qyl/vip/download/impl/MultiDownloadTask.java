package com.qyl.vip.download.impl;

import android.util.Log;

import com.qyl.vip.download.IProcessor;
import com.qyl.vip.download.bean.DownloadInfo;
import com.qyl.vip.download.bean.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public class MultiDownloadTask extends AbsDownloadTask {

    public MultiDownloadTask(DownloadInfo downloadInfo, ThreadInfo threadInfo, OnDownloadListener listener) {
        super(downloadInfo, threadInfo, listener);
    }

    public MultiDownloadTask(DownloadInfo downloadInfo, ThreadInfo threadInfo, OnDownloadListener listener, IProcessor processor) {
        super(downloadInfo, threadInfo, listener, processor);
    }

    @Override
    protected void saveThreadInfo(ThreadInfo info) {

    }

    @Override
    protected int getResponseCode() {
        return HttpURLConnection.HTTP_PARTIAL;
    }

    @Override
    protected void updateThreadInfo(ThreadInfo info) {

    }

    @Override
    protected Map<String, String> getHttpHeaders(ThreadInfo info) {
        Map<String, String> headers = new HashMap<String, String>();
        long start = info.getStart() + info.getFinished();
        long end = info.getEnd();
        headers.put("Range", "bytes=" + start + "-" + end);
        return headers;
    }

    @Override
    protected RandomAccessFile getFile(File dir, String name, long offset) throws IOException {
        File file = new File(dir, name);
        RandomAccessFile raf = new RandomAccessFile(file, "rwd");
        raf.seek(offset);
        Log.e("qqqq", "getFile offset=" + offset + "," + raf.getFilePointer());
        return raf;
    }
}
