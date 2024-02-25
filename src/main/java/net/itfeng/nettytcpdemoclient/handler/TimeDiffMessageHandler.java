package net.itfeng.nettytcpdemoclient.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import net.itfeng.nettytcpdemoclient.protocol.DataTransPackageOuterClass;
import net.itfeng.nettytcpdemoclient.protocol.TestDataTransOuterClass;
import net.itfeng.nettytcpdemoclient.service.MessageAsyncPublishService;
import net.itfeng.nettytcpdemoclient.util.ClientIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 时间校准消息处理器
 *
 * @author fengxubo
 * @since 2024/2/25 15:46
 * 
 */
@Slf4j
@Service
public class TimeDiffMessageHandler implements MyMessageHandler{
    private static final DataTransPackageOuterClass.DataType DATA_TYPE = DataTransPackageOuterClass.DataType.CLIENT_TIME_DIFF;

    @Autowired
    private MessageAsyncPublishService messageAsyncPublishService;

    @Async("messageHandlerPool")
    public void handle(byte[] messageBytes)  {
        long now = System.currentTimeMillis();
        String clientId = ClientIdUtil.getClientId();
        TestDataTransOuterClass.ClientTimeDiff clientTimeDiff;
        try {
            clientTimeDiff = TestDataTransOuterClass.ClientTimeDiff.parseFrom(messageBytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("ClientTimeDiff 反序列化异常", e);
            throw new RuntimeException(e);
        }
        TestDataTransOuterClass.ClientTimeDiff clientTimeDiffNew = TestDataTransOuterClass.ClientTimeDiff.newBuilder()
                .setClientId(clientId)
                .setMsgId(clientTimeDiff.getMsgId())
                .setStartTimeMillis(clientTimeDiff.getStartTimeMillis())
                .setTimeMillis(now)
                .build();
        // 客户端收到时间校准消息，发送给云服务
        messageAsyncPublishService.publish(DataTransPackageOuterClass.DataType.CLIENT_TIME_DIFF, clientTimeDiffNew.toByteArray(),clientId);
    }


    @Override
    public DataTransPackageOuterClass.DataType getDataType() {
        return DATA_TYPE;
    }


    @Override
    public boolean isSupport(DataTransPackageOuterClass.DataType dataType) {
        return dataType == DATA_TYPE;
    }
}
