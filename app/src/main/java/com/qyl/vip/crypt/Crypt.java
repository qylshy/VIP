package com.qyl.vip.crypt;

import com.qyl.vip.download.bean.ThreadInfo;

/**
 * Created by qiuyunlong on 17/6/3.
 */

public interface Crypt {

    void encrypt(ThreadInfo mThreadInfo, byte[]buffer, long offset, int length);

    void decrypt(byte[] buffer, long offset, int length);

}
