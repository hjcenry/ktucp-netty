package com.hjcenry.exception;

/**
 * KCP初始化异常
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/10 16:30
 **/
public class KcpInitException extends KcpException {

    public KcpInitException(Exception e) {
        super(e);
    }

    public KcpInitException(String msg) {
        super(msg);
    }
}
