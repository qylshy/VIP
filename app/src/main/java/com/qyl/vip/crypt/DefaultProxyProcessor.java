package com.qyl.vip.crypt;

import com.qyl.vip.proxy.base.IProxyProcessor;

/**
 * Created by qiuyunlong on 17/6/3.
 */

public class DefaultProxyProcessor implements IProxyProcessor {

    private Crypt crypt;

    public DefaultProxyProcessor(Crypt crypt) {
        this.crypt = crypt;
    }

    @Override
    public void onProcess(byte[] buffer, long offset, int length) {
        if (crypt != null) {
            crypt.decrypt(buffer, offset, length);
        }
    }
}
