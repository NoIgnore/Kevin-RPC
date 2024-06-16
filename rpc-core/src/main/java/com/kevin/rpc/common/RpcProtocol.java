package com.kevin.rpc.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

@Data
public class RpcProtocol implements Serializable {

    private static final long serialVersionUID = 2669293150219020249L;

    /**
     * 魔法数,在做服务通讯的时候定义的一个安全检测，确认当前请求的协议是否合法。
     */
    private short magicNumber = 123;
    /**
     * 协议传输核心数据的长度
     */
    private int contentLength;
    /**
     * 传输的数据
     */
    private byte[] content;

    public RpcProtocol(byte[] content) {
        this.contentLength = content.length;
        this.content = content;
    }

    @Override
    public String toString() {
        return "RpcProtocol{" + "contentLength=" + contentLength + ", content=" + Arrays.toString(content) + '}';
    }
}