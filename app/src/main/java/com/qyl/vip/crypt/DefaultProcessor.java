package com.qyl.vip.crypt;

import com.qyl.vip.download.IProcessor;
import com.qyl.vip.download.bean.ThreadInfo;

/**
 * Created by qiuyunlong on 17/6/2.
 */

public class DefaultProcessor implements IProcessor {
    private Crypt crypt;

    public DefaultProcessor(Crypt crypt) {
        this.crypt = crypt;
    }

    @Override
    public void onProcess(ThreadInfo mThreadInfo, long offset, int len, byte[] buffer) {
        //加密
        /*if (mThreadInfo.getStart() == 0 && mThreadInfo.getFinished() < 10) {
            Log.i("qqqq", "onProcess...offset=" + offset + ",length=" + len + "..." + Thread.currentThread().getName());
            for (int i = 0; i < 2; i++) {
                byte rawByte = buffer[i];
                byte tmp = (byte) (rawByte ^ i);
                buffer[i] = tmp;
            }
        }*/
        crypt.encrypt(mThreadInfo, buffer, offset, len);



    }
}
