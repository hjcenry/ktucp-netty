package com.hjcenry.time;

/**
 * KCP时间服务
 * <p>
 * 抽象出时间接口服务的目的:<br/>
 * 为了支持不使用{@link System#currentTimeMillis()}来获取时间，而是使用上层调用者提供的获取时间（比如有缓存的时间系统）
 * </p>
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/1/12 16:08
 */
public interface IKcpTimeService {

    /**
     * 获取当前时间
     *
     * @return 当前时间
     */
    public long now();
}
