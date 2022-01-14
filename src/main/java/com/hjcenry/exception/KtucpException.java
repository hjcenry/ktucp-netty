package com.hjcenry.exception;

/**
 * KCP异常
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/10 16:30
 **/
public class KtucpException extends Exception {

    public KtucpException(String msg) {
        super(msg);
    }

    public KtucpException(Exception e) {
        super(e);
    }
}
