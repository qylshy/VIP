package com.qyl.vip.download.impl;

import android.util.Log;

import com.qyl.vip.download.DownloadCallBack;
import com.qyl.vip.download.DownloadConfig;
import com.qyl.vip.download.exception.DownloadException;
import com.qyl.vip.download.DownloadRequest;
import com.qyl.vip.download.bean.DownloadStatus;
import com.qyl.vip.download.base.IConnectTask;
import com.qyl.vip.download.base.IDownloadTask;
import com.qyl.vip.download.base.IDownloader;
import com.qyl.vip.download.IProcessor;
import com.qyl.vip.download.bean.DownloadInfo;
import com.qyl.vip.download.bean.ThreadInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public class DownloaderImpl implements IDownloader,
        IConnectTask.OnConnectListener,
        IDownloadTask.OnDownloadListener {

    private static final String TAG = DownloaderImpl.class.getSimpleName();

    private DownloadRequest mRequest;


    private Executor mExecutor;

    private DownloadConfig mConfig;

    private int mStatus;

    private DownloadInfo mDownloadInfo;

    private IConnectTask mConnectTask;

    private List<IDownloadTask> mDownloadTasks;

    private DownloadCallBack mDownloadCallBack;

    private IProcessor mProcessor;

    public DownloaderImpl(DownloadRequest request, Executor executor, DownloadConfig config, DownloadCallBack callBack) {
        mRequest = request;
        mExecutor = executor;
        mConfig = config;
        this.mDownloadCallBack= callBack;
        init();
    }

    public DownloaderImpl(DownloadRequest request, Executor executor, DownloadConfig config, DownloadCallBack callBack, IProcessor processor) {
        mRequest = request;
        mExecutor = executor;
        mConfig = config;
        this.mDownloadCallBack= callBack;
        this.mProcessor = processor;
        init();
    }

    private void init() {
        mDownloadInfo = new DownloadInfo(mRequest.getName().toString(), mRequest.getUri(), mRequest.getFolder());
        mDownloadTasks = new LinkedList<>();
    }

    @Override
    public boolean isRunning() {
        return mStatus == DownloadStatus.STATUS_STARTED
                || mStatus == DownloadStatus.STATUS_CONNECTING
                || mStatus == DownloadStatus.STATUS_CONNECTED
                || mStatus == DownloadStatus.STATUS_PROGRESS;
    }

    @Override
    public void start() {
        mStatus = DownloadStatus.STATUS_STARTED;
        if (mDownloadCallBack != null) {
            mDownloadCallBack.onStarted();
        }
        connect();
    }

    @Override
    public void pause() {
        if (mConnectTask != null) {
            mConnectTask.pause();
        }
        for (IDownloadTask task : mDownloadTasks) {
            task.pause();
        }
        if (mStatus != DownloadStatus.STATUS_PROGRESS) {
            onDownloadPaused();
        }
    }

    @Override
    public void cancel() {
        if (mConnectTask != null) {
            mConnectTask.cancel();
        }
        for (IDownloadTask task : mDownloadTasks) {
            task.cancel();
        }
        if (mStatus != DownloadStatus.STATUS_PROGRESS) {
            onDownloadCanceled();
        }
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "onDestroy");
    }

    @Override
    public void onConnecting() {
        mStatus = DownloadStatus.STATUS_CONNECTING;
        Log.i(TAG, "onConnecting...");
        if (mDownloadCallBack != null) {
            mDownloadCallBack.onConnecting();
        }
    }

    @Override
    public void onConnected(long time, long length, boolean isAcceptRanges) {
        Log.i(TAG, "onConnected...");
        if (mConnectTask.isCanceled()) {
            onConnectCanceled();
        } else {
            mStatus = DownloadStatus.STATUS_CONNECTED;
            if (mDownloadCallBack != null) {
                mDownloadCallBack.onConnected(length, isAcceptRanges);
            }

            mDownloadInfo.setAcceptRanges(isAcceptRanges);
            mDownloadInfo.setLength(length);
            download(length, isAcceptRanges);
        }
    }

    @Override
    public void onConnectPaused() {
        Log.i(TAG, "onConnectPaused...");
        onDownloadPaused();

    }

    @Override
    public void onConnectCanceled() {
        Log.i(TAG, "onConnectCanceled...");
        deleteFile();
        mStatus = DownloadStatus.STATUS_CANCELED;
        onDestroy();
    }

    @Override
    public void onConnectFailed(DownloadException de) {
        Log.i(TAG, "onConnectFailed..." + de.toString());
        if (mConnectTask.isCanceled()) {
            onConnectCanceled();
        } else if (mConnectTask.isPaused()) {
            onDownloadPaused();
        } else {
            mStatus = DownloadStatus.STATUS_FAILED;
            onDestroy();
        }
        if (mDownloadCallBack != null) {
            mDownloadCallBack.onFailed(de);
        }
    }

    @Override
    public void onDownloadConnecting() {
        Log.i(TAG, "onDownloadConnecting...");

    }

    @Override
    public void onDownloadProgress(long finished, long length) {
        Log.i(TAG, "onDownloadProgress...finished = " + finished + ",length=" + length);
        if (mDownloadCallBack != null) {
            final int percent = (int) (finished * 100 / length);
            mDownloadCallBack.onProgress(finished, length, percent);
        }
    }

    @Override
    public void onDownloadCompleted() {
        Log.i(TAG, "onDownloadCompleted...");
        if (isAllComplete()) {
            mStatus = DownloadStatus.STATUS_COMPLETED;
            onDestroy();
            if (mDownloadCallBack != null) {
                String path = mRequest.getFolder().getAbsolutePath() + "/" + mRequest.getName();
                mDownloadCallBack.onCompleted(path);
            }
        }

    }

    @Override
    public void onDownloadPaused() {
        Log.i(TAG, "onDownloadPaused..." + Thread.currentThread().getName());
        if (isAllPaused()) {
            mStatus = DownloadStatus.STATUS_PAUSED;
            //mResponse.onDownloadPaused();
            onDestroy();
            if (mDownloadCallBack != null) {
                mDownloadCallBack.onDownloadPaused();
            }
        }
    }

    @Override
    public void onDownloadCanceled() {
        Log.i(TAG, "onDownloadCanceled...");
        if (mDownloadCallBack != null) {
            mDownloadCallBack.onDownloadCanceled();
        }
    }

    @Override
    public void onDownloadFailed(DownloadException de) {
        Log.i(TAG, "onDownloadFailed..." + de.toString());
        if (isAllCanceled()) {
            deleteFile();
            mStatus = DownloadStatus.STATUS_CANCELED;
           // mResponse.onDownloadCanceled();
            onDestroy();
            if (mDownloadCallBack != null) {
                mDownloadCallBack.onFailed(de);
            }
        }

    }

    private void connect() {
        mConnectTask = new ConnectTaskImpl(mRequest.getUri(), this);
        mExecutor.execute(mConnectTask);
    }

    private void download(long length, boolean acceptRanges) {
        mStatus = DownloadStatus.STATUS_PROGRESS;
        initDownloadTasks(length, acceptRanges);
        // start tasks
        for (IDownloadTask downloadTask : mDownloadTasks) {
            mExecutor.execute(downloadTask);
        }
    }

    //TODO
    private void initDownloadTasks(long length, boolean acceptRanges) {
        mDownloadTasks.clear();
        if (acceptRanges) {
            List<ThreadInfo> threadInfos = getMultiThreadInfos(length);
            // init finished
            int finished = 0;
            for (ThreadInfo threadInfo : threadInfos) {
                finished += threadInfo.getFinished();
            }
            mDownloadInfo.setFinished(finished);
            for (ThreadInfo info : threadInfos) {
                mDownloadTasks.add(new MultiDownloadTask(mDownloadInfo, info, this, mProcessor));
            }
        } else {
            ThreadInfo info = getSingleThreadInfo();
            mDownloadTasks.add(new SingleDownloadTask(mDownloadInfo, info, this));
        }
    }

    //TODO
    private List<ThreadInfo> getMultiThreadInfos(long length) {
        // init threadInfo from db
        final List<ThreadInfo> threadInfos = new ArrayList<>();
//        if (threadInfos.isEmpty()) {
//
//        }

        final int threadNum = mConfig.getThreadNum();
        for (int i = 0; i < threadNum; i++) {
            // calculate average
            final long average = length / threadNum;
            final long start = average * i;
            final long end;
            if (i == threadNum - 1) {
                end = length;
            } else {
                end = start + average - 1;
            }
            ThreadInfo threadInfo = new ThreadInfo(i, mRequest.getUri(), start, end, 0);
            threadInfos.add(threadInfo);
        }

        return threadInfos;
    }

    private ThreadInfo getSingleThreadInfo() {
        ThreadInfo threadInfo = new ThreadInfo(0, mRequest.getUri(), 0);
        return threadInfo;
    }

    private boolean isAllComplete() {
        boolean allFinished = true;
        for (IDownloadTask task : mDownloadTasks) {
            if (!task.isComplete()) {
                allFinished = false;
                break;
            }
        }
        return allFinished;
    }

    private boolean isAllFailed() {
        boolean allFailed = true;
        for (IDownloadTask task : mDownloadTasks) {
            if (task.isDownloading()) {
                allFailed = false;
                break;
            }
        }
        return allFailed;
    }

    private boolean isAllPaused() {
        boolean allPaused = true;
        for (IDownloadTask task : mDownloadTasks) {
            if (task.isDownloading()) {
                allPaused = false;
                break;
            }
        }
        return allPaused;
    }

    private boolean isAllCanceled() {
        boolean allCanceled = true;
        for (IDownloadTask task : mDownloadTasks) {
            if (task.isDownloading()) {
                allCanceled = false;
                break;
            }
        }
        return allCanceled;
    }

    private void deleteFile() {
        File file = new File(mDownloadInfo.getDir(), mDownloadInfo.getName());
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }
}
