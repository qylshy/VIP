package com.qyl.vip.download.util;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by qiuyunlong on 17/6/2.
 */
public class IOCloseUtils {

    public static final void close(Closeable closeable) throws IOException {
        if (closeable != null) {
            synchronized (IOCloseUtils.class) {
                closeable.close();
            }
        }
    }
}
