package com.hjcenry.time;

/**
 * 系统时间服务
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/12 16:09
 **/
public class SystemTimeServiceImpl implements IKtucpTimeService {

    @Override
    public long now() {
        return System.currentTimeMillis();
    }
}
