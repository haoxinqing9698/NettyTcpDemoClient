package net.itfeng.nettytcpdemoclient.handler;

import net.itfeng.nettytcpdemoclient.protocol.DataTransPackageOuterClass;

/**
 * 消息处理接口
 *
 * @author fengxubo
 * @since 2024/1/26 17:41
 * 
 */
public interface MyMessageHandler {
    void handle(byte[] messageBytes);

    DataTransPackageOuterClass.DataType getDataType();

    /**
     * 判断是否支持该消息类型，mqtt中即判断正则topic与消息的实际topic是否匹配
     * @param messageType
     * @return
     */
    boolean isSupport(DataTransPackageOuterClass.DataType messageType);
}
