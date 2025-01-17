package com.kevin.rpc.common;

import java.util.concurrent.atomic.AtomicLong;

public class ChannelFuturePollingRef {

    private final AtomicLong referenceTimes = new AtomicLong(0);


    public ChannelFutureWrapper getChannelFutureWrapper(ChannelFutureWrapper[] arr) {
        long i = referenceTimes.getAndIncrement();
        int index = (int) (i % arr.length);
        return arr[index];
    }

}