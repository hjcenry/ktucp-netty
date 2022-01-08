package com.hjcenry.fec;

import com.hjcenry.fec.fecNative.FecDecode;
import com.hjcenry.fec.fecNative.FecEncode;
import com.hjcenry.fec.fecNative.ReedSolomonNative;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * Created by JinMiao
 * 2021/2/2.
 */
public class FecAdapt {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(FecAdapt.class);
    private ReedSolomonNative reedSolomonNative;
    private ReedSolomon reedSolomon;

    public FecAdapt(int dataShards, int parityShards) {
        if (ReedSolomonNative.isNativeSupport()) {
            reedSolomonNative = new ReedSolomonNative(dataShards, parityShards);
            log.info("fec use C native reedSolomon dataShards {} parityShards {}", dataShards, parityShards);
        } else {
            reedSolomon = ReedSolomon.create(dataShards, parityShards);
            log.info("fec use jvm reedSolomon dataShards {} parityShards {}", dataShards, parityShards);
        }
    }

    public IFecEncode fecEncode(int headerOffset, int mtu) {
        IFecEncode iFecEncode;
        if (reedSolomonNative != null) {
            iFecEncode = new FecEncode(headerOffset, this.reedSolomonNative, mtu);
        } else {
            iFecEncode = new com.hjcenry.fec.fec.FecEncode(headerOffset, this.reedSolomon, mtu);
        }
        return iFecEncode;
    }


    public IFecDecode fecDecode(int mtu) {
        IFecDecode iFecDecode;
        if (reedSolomonNative != null) {
            iFecDecode = new FecDecode(3 * reedSolomonNative.getTotalShardCount(), this.reedSolomonNative, mtu);
        } else {
            iFecDecode = new com.hjcenry.fec.fec.FecDecode(3 * this.reedSolomon.getTotalShardCount(), this.reedSolomon, mtu);
        }
        return iFecDecode;
    }
}
