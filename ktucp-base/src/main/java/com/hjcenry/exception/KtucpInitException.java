package com.hjcenry.exception;

/**
 * KCP初始化异常
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/10 16:30
 **/
public class KtucpInitException extends KtucpException {

    public KtucpInitException(Exception e) {
        super(e);
    }

    public KtucpInitException(String msg) {
        super(msg);
    }
}
