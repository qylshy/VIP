package com.qyl.vip.download.impl;

import android.text.TextUtils;
import android.util.Log;

import com.qyl.vip.download.exception.DownloadException;
import com.qyl.vip.download.bean.DownloadStatus;
import com.qyl.vip.download.base.IConnectTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public class ConnectTaskImpl implements IConnectTask {

    public static final int CONNECT_TIME_OUT = 5 * 1000;
    public static final int READ_TIME_OUT = 5 * 1000;
    public static final String GET = "GET";

    private String mUri;
    private OnConnectListener mOnConnectListener;

    private volatile int mStatus;

    private volatile long mStartTime;

    public ConnectTaskImpl(String uri, OnConnectListener listener) {
        this.mUri = uri;
        this.mOnConnectListener = listener;
    }


    @Override
    public void pause() {
        mStatus = DownloadStatus.STATUS_PAUSED;
    }

    @Override
    public void cancel() {
        mStatus = DownloadStatus.STATUS_CANCELED;
    }

    @Override
    public boolean isConnecting() {
        return mStatus == DownloadStatus.STATUS_CONNECTING;
    }

    @Override
    public boolean isConnected() {
        return mStatus == DownloadStatus.STATUS_CONNECTED;
    }

    @Override
    public boolean isPaused() {
        return mStatus == DownloadStatus.STATUS_PAUSED;
    }

    @Override
    public boolean isCanceled() {
        return mStatus == DownloadStatus.STATUS_CANCELED;
    }

    @Override
    public boolean isFailed() {
        return mStatus == DownloadStatus.STATUS_FAILED;
    }

    @Override
    public void run() {
        mStatus = DownloadStatus.STATUS_CONNECTING;
        if (mOnConnectListener != null) {
            mOnConnectListener.onConnecting();
        }
        try {
            executeConnection();
        } catch (DownloadException e) {
            handleDownloadException(e);
        }

    }

    private void executeConnection() throws DownloadException {
        mStartTime = System.currentTimeMillis();
        HttpURLConnection httpConnection = null;
        final URL url;
        try {
            url = new URL(mUri);
        } catch (MalformedURLException e) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED, "Bad url.", e);
        }
        try {
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setConnectTimeout(CONNECT_TIME_OUT);
            httpConnection.setReadTimeout(READ_TIME_OUT);
            httpConnection.setRequestMethod(GET);
            httpConnection.setRequestProperty("Range", "bytes=" + 0 + "-");
            final int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                parseResponse(httpConnection, false);
            } else if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                parseResponse(httpConnection, true);
            } else {
                throw new DownloadException(DownloadStatus.STATUS_FAILED, "UnSupported response code:" + responseCode);
            }
        } catch (ProtocolException e) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED, "Protocol error", e);
        } catch (IOException e) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED, "IO error", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    private void parseResponse(HttpURLConnection httpConnection, boolean isAcceptRanges) throws DownloadException {

        final long length;
        String contentLength = httpConnection.getHeaderField("Content-Length");
        if (TextUtils.isEmpty(contentLength) || contentLength.equals("0") || contentLength.equals("-1")) {
            length = httpConnection.getContentLength();
        } else {
            length = Long.parseLong(contentLength);
        }

        if (length <= 0) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED, "length <= 0");
        }

        checkCanceledOrPaused();

        mStatus = DownloadStatus.STATUS_CONNECTED;
        if (mOnConnectListener != null) {
            final long timeDelta = System.currentTimeMillis() - mStartTime;
            mOnConnectListener.onConnected(timeDelta, length, isAcceptRanges);
        }
    }

    private void checkCanceledOrPaused() throws DownloadException {
        if (isCanceled()) {
            // cancel
            throw new DownloadException(DownloadStatus.STATUS_CANCELED, "Connection Canceled!");
        } else if (isPaused()) {
            // paused
            throw new DownloadException(DownloadStatus.STATUS_PAUSED, "Connection Paused!");
        }
    }

    private void handleDownloadException(DownloadException e) {
        Log.e("qqqqqq", "handleDownloadException" + e.getErrorCode());
        switch (e.getErrorCode()) {
            case DownloadStatus.STATUS_FAILED:
                synchronized (mOnConnectListener) {
                    mStatus = DownloadStatus.STATUS_FAILED;
                    mOnConnectListener.onConnectFailed(e);
                }
                break;
            case DownloadStatus.STATUS_PAUSED:
                synchronized (mOnConnectListener) {
                    mStatus = DownloadStatus.STATUS_PAUSED;
                    mOnConnectListener.onConnectPaused();
                }
                break;
            case DownloadStatus.STATUS_CANCELED:
                synchronized (mOnConnectListener) {
                    mStatus = DownloadStatus.STATUS_CANCELED;
                    mOnConnectListener.onConnectCanceled();
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown state");
        }
    }
}
