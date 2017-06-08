package com.qyl.vip.download;

import com.qyl.vip.download.bean.ThreadInfo;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public interface IProcessor {
    void onProcess(ThreadInfo mThreadInfo, long offset, int len, byte[] buffer);
}
