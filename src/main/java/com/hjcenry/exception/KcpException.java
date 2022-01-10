package com.hjcenry.exception;

/**
 * KCP异常
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/10 16:30
 **/
public class KcpException extends Exception {

    public KcpException(String msg) {
        super(msg);
    }

    public KcpException(Exception e) {
        super(e);
    }
}
