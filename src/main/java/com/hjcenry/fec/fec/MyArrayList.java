package com.hjcenry.fec.fec;

import java.util.ArrayList;

/**
 * Created by JinMiao
 * 2020/7/2.
 */
public class MyArrayList<E> extends ArrayList<E> {

    public MyArrayList() {
        super();
    }

    public MyArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
    }
}
