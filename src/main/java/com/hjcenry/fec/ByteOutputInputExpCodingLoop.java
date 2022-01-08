/**
 * One specific ordering/nesting of the coding loops.
 * <p>
 * Copyright 2015, Backblaze, Inc.  All rights reserved.
 */

package com.hjcenry.fec;

public class ByteOutputInputExpCodingLoop extends CodingLoopBase {

    @Override
    public void codeSomeShards(
            byte[][] matrixRows,
            byte[][] inputs, int inputCount,
            byte[][] outputs, int outputCount,
            int offset, int byteCount) {

        for (int iByte = offset; iByte < offset + byteCount; iByte++) {
            for (int iOutput = 0; iOutput < outputCount; iOutput++) {
                byte[] matrixRow = matrixRows[iOutput];
                int value = 0;
                for (int iInput = 0; iInput < inputCount; iInput++) {
                    value ^= Galois.multiply(matrixRow[iInput], inputs[iInput][iByte]);
                }
                outputs[iOutput][iByte] = (byte) value;
            }
        }
    }

}
