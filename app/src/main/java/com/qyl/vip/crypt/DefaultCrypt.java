package com.qyl.vip.crypt;

import android.util.Log;

import com.qyl.vip.download.bean.ThreadInfo;

/**
 * Created by qiuyunlong on 17/6/3.
 * 对视频前两个字节加密
 */

public class DefaultCrypt implements Crypt {
    private final static String TAG = DefaultCrypt.class.getSimpleName();

    public DefaultCrypt() {

    }

    @Override
    public void encrypt(ThreadInfo mThreadInfo, byte[] buffer, long offset, int length) {
        //加密
        if (mThreadInfo.getStart() == 0 && mThreadInfo.getFinished() < 10) {
            Log.i(TAG, "加密:onProcess...offset=" + offset + ",length=" + length + "..." + Thread.currentThread().getName());
            for (int i = 0; i < 2; i++) {
                byte rawByte = buffer[i];
                byte tmp = (byte) (rawByte ^ i);
                buffer[i] = tmp;
            }
        }
    }

    @Override
    public void decrypt(byte[] buffer, long offset, int length) {
        if (offset == 0) {
            Log.v(TAG, "解密...");
            for (int i = 0; i < 2; i++) {
                byte rawByte = buffer[i];
                byte tmp = (byte) (rawByte ^ i);
                buffer[i] = tmp;
            }

        }

    }



}
