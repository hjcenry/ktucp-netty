package com.hjcenry.test;

import com.hjcenry.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @ClassName NetStatistic
 * @Description
 * @Author hejincheng
 * @Date 2021/12/24 15:58
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
public class NetStatistic {
    // 服务器
    private TestServerEnum serverEnum;
    private int testPacketCount;
    private long totalReceiveBytes;
    private long totalSendBytes;
    private long totalDelay;
    private int testPackageContentSize;
    private String custom;

    private float highestDelay;
    private float lowestDelay;

    public float getAvgDelay() {
        return this.totalDelay * 1f / this.testPacketCount;
    }

    public String getStatisticLog() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(printLine());
        stringBuilder.append(String.format("%s.服务器.HOST[%s].端口[%d]\n", serverEnum.name(), serverEnum.getHost(), serverEnum.getPort()));
        if (!StringUtil.isEmpty(custom)) {
            stringBuilder.append(custom);
        }
        stringBuilder.append(printLine());
        float size = testPacketCount;
        stringBuilder.append(String.format("测试消息总数\t\t : %d \t 发送字符串长度:%d \t 平均单条发送数据:%fKB \t 总发送数据:%fMB 平均接收发送数据:%fKB \t 总接收数据:%fMB \t 平均单程总延迟 : %fms \t 平均总延迟 : %fms  \t 最高总延迟 : %fms  \t 最低总延迟 : %fms \n",
                        (int) size,
                        testPackageContentSize,
                        size == 0 ? 0 : totalReceiveBytes / size,
                        size == 0 ? 0 : totalReceiveBytes / 1024f / 1024f,
                        size == 0 ? 0 : totalSendBytes / size,
                        size == 0 ? 0 : totalSendBytes / 1024f / 1024f,
                        size == 0 ? 0 : totalDelay / size / 2,
                        size == 0 ? 0 : totalDelay / size,
                        size == 0 ? 0 : highestDelay,
                        size == 0 ? 0 : lowestDelay
                )
        );
        stringBuilder.append(printLine());
        return stringBuilder.toString();
    }

    private static String printLine() {
        return "================================================================================================================================================================================================================================\n";
    }
}
