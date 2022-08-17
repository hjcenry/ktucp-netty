package com.hjcenry.test;

import com.hjcenry.kcp.Uktucp;
import kcp.Ukcp;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetSocketAddress;

/**
 * @ClassName KcpChannel
 * @Description
 * @Author hejincheng
 * @Date 2021/12/22 16:24
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
public class KcpChannel {

    private InetSocketAddress address;

    private Ukcp ukcp;
}
