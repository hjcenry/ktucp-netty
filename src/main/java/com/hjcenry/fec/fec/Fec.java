package com.hjcenry.fec.fec;

/**
 * Created by JinMiao
 * 2018/6/6.
 */
public class Fec {
    public static int
            fecHeaderSize = 6,
            fecDataSize = 2,
    // plus 2B data size
    fecHeaderSizePlus2 = fecHeaderSize + fecDataSize,
            typeData = 0xf1,
            typeParity = 0xf2;

}
