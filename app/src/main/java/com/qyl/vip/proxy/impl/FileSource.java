package com.qyl.vip.proxy.impl;

import android.util.Log;

import com.qyl.vip.proxy.exception.VideoProxyException;
import com.qyl.vip.proxy.base.Source;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.qyl.vip.proxy.utils.VideoProxyUtil.LOG_TAG;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public class FileSource implements Source {

    public File file;

    public FileSource(File file) {
        this.file = file;
    }


    @Override
    public void open(int offset) throws VideoProxyException {

    }

    @Override
    public void open(int startOffset, long endOffset) throws VideoProxyException {

    }

    @Override
    public int length() throws VideoProxyException {
        return (int) file.length();
    }

    @Override
    public int read(byte[] buffer) throws VideoProxyException {
        return 0;
    }

    @Override
    public int read(byte[] buffer, long offset, int length) throws VideoProxyException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(this.file, "rw");
            raf.seek(offset);
            //todo 解密

            return raf.read(buffer, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(LOG_TAG, "File read() " + e.toString());
            String format = "Error reading %d bytes with offset %d from file[%d bytes] to buffer[%d bytes]";
            throw new VideoProxyException(String.format(format, length, offset, length(), buffer.length), e);
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws VideoProxyException {

    }

    @Override
    public String getMime() throws VideoProxyException {
        return "video/mpeg4";
    }
}
