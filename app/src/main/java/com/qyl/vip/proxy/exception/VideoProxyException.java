package com.qyl.vip.proxy.exception;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public class VideoProxyException extends Exception {

    public VideoProxyException(String message) {
        super(message);
    }

    public VideoProxyException(String message, Throwable cause) {
        super(message, cause);
    }

    public VideoProxyException(Throwable cause) {
        super(cause);
    }
}