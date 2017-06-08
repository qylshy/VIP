package com.qyl.vip.proxy.impl;

import android.text.TextUtils;
import android.util.Log;

import com.qyl.vip.proxy.exception.VideoProxyException;
import com.qyl.vip.proxy.base.Source;
import com.qyl.vip.proxy.utils.Preconditions;
import com.qyl.vip.proxy.utils.VideoProxyUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.qyl.vip.proxy.utils.VideoProxyUtil.DEFAULT_BUFFER_SIZE;
import static com.qyl.vip.proxy.utils.VideoProxyUtil.LOG_TAG;
import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

/**
 * Created by qiuyunlong on 17/6/3.
 */

public class PingSource implements Source {

    public static final String PING_RESPONSE = "ping ok";

    private static final int MAX_REDIRECTS = 5;
    public final String url;
    private HttpURLConnection connection;
    private InputStream inputStream;
    private volatile int length = Integer.MIN_VALUE;
    private volatile String mime;

    public PingSource(String url) {
        this(url, VideoProxyUtil.getSupposablyMime(url));
    }

    public PingSource(String url, String mime) {
        this.url = Preconditions.checkNotNull(url);
        this.mime = mime;
    }


    @Override
    public synchronized int length() throws VideoProxyException {
        if (length == Integer.MIN_VALUE) {
            fetchContentInfo();
        }
        return length;
    }

    @Override
    public void open(int offset) throws VideoProxyException {
        try {
            connection = openConnection(offset, -1);
            mime = connection.getContentType();
            inputStream = new BufferedInputStream(connection.getInputStream(), DEFAULT_BUFFER_SIZE);
            length = readSourceAvailableBytes(connection, offset, connection.getResponseCode());
        } catch (IOException e) {
            throw new VideoProxyException("Error opening connection for " + url + " with offset " + offset, e);
        }
    }

    @Override
    public void open(int startOffset, long endOffset) throws VideoProxyException {

    }

    @Override
    public int read(byte[] buffer, long offset, int length) throws VideoProxyException {
        for (int i =0; i < PING_RESPONSE.getBytes().length; i++) {
            buffer[i] = PING_RESPONSE.getBytes()[i];
        }
        return PING_RESPONSE.getBytes().length;
    }

    private int readSourceAvailableBytes(HttpURLConnection connection, int offset, int responseCode) throws IOException {
        int contentLength = connection.getContentLength();
        Log.v(LOG_TAG, "readSourceAvailableBytes==contentLength="+contentLength+",offset="+offset+",responseCode="+responseCode);
        return responseCode == HTTP_OK ? contentLength
                : responseCode == HTTP_PARTIAL ? contentLength + offset : length;
    }

    @Override
    public void close() throws VideoProxyException {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (NullPointerException e) {
                throw new VideoProxyException("Error disconnecting HttpUrlConnection", e);
            }
        }
    }

    @Override
    public int read(byte[] buffer) throws VideoProxyException {
        if (inputStream == null) {
            throw new VideoProxyException("Error reading data from " + url + ": connection is absent!");
        }
        try {
            return inputStream.read(buffer, 0, buffer.length);
        } catch (InterruptedIOException e) {
            throw new VideoProxyException("Reading source " + url + " is interrupted", e);
        } catch (IOException e) {
            throw new VideoProxyException("Error reading data from " + url, e);
        }
    }

    @Override
    public synchronized String getMime() throws VideoProxyException {
        if (TextUtils.isEmpty(mime)) {
            fetchContentInfo();
        }
        return mime;
    }

    private void fetchContentInfo() throws VideoProxyException {
        Log.d(LOG_TAG, "Read content info from " + url);
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = openConnection(0, 10000);
            length = urlConnection.getContentLength();
            mime = urlConnection.getContentType();
            inputStream = urlConnection.getInputStream();
            Log.i(LOG_TAG, "Content info for `" + url + "`: mime: " + mime + ", content-length: " + length);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error fetching info from " + url, e);
        } finally {
            VideoProxyUtil.close(inputStream);
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private HttpURLConnection openConnection(int offset, int timeout) throws IOException, VideoProxyException {
        HttpURLConnection connection;
        boolean redirected;
        int redirectCount = 0;
        String url = this.url;
        do {
            Log.d(LOG_TAG, "Open connection " + (offset > 0 ? " with offset " + offset : "") + " to " + url);
            connection = (HttpURLConnection) new URL(url).openConnection();
            if (offset > 0) {
                connection.setRequestProperty("Range", "bytes=" + offset + "-");
            }
            if (timeout > 0) {
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
            }
            int code = connection.getResponseCode();
            redirected = code == HTTP_MOVED_PERM || code == HTTP_MOVED_TEMP || code == HTTP_SEE_OTHER;
            if (redirected) {
                url = connection.getHeaderField("Location");
                redirectCount++;
                connection.disconnect();
            }
            if (redirectCount > MAX_REDIRECTS) {
                throw new VideoProxyException("Too many redirects: " + redirectCount);
            }
        } while (redirected);
        return connection;
    }



}
