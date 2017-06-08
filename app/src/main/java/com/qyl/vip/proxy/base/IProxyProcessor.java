package com.qyl.vip.proxy.base;


/**
 * Created by qiuyunlong on 17/6/3.
 */

public interface IProxyProcessor {

    void onProcess(byte[] buffer, long offset, int length);

}
