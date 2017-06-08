package com.qyl.vip.proxy.base;

import com.qyl.vip.proxy.exception.VideoProxyException;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public interface Source {

    void open(int offset) throws VideoProxyException;

    void open(int startOffset, long endOffset) throws VideoProxyException;

    int length() throws VideoProxyException;

    int read(byte[] buffer) throws VideoProxyException;

    int read(byte[] buffer, long offset, int length) throws VideoProxyException;

    void close() throws VideoProxyException;

    String getMime() throws VideoProxyException;
}

