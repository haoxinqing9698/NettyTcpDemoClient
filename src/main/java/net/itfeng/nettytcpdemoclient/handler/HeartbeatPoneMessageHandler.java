package net.itfeng.nettytcpdemoclient.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import net.itfeng.nettytcpdemoclient.protocol.DataTransPackageOuterClass;
import net.itfeng.nettytcpdemoclient.protocol.TestDataTransOuterClass;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 时间校准消息处理器
 *
 * @author itfeng
 * @since 2024/1/8 15:46
 * 
 */
@Slf4j
@Service
public class HeartbeatPoneMessageHandler implements MyMessageHandler{

    private static final DataTransPackageOuterClass.DataType DATA_TYPE = DataTransPackageOuterClass.DataType.HEARTBEAT_PONG;



    @Async("messageHandlerPool")
    public void handle(byte[] messageBytes)  {
        TestDataTransOuterClass.HeartBeatPong heartBeatPong;
        try {
            heartBeatPong = TestDataTransOuterClass.HeartBeatPong.parseFrom(messageBytes);
        } catch (InvalidProtocolBufferException e) {
            log.error("收到心跳响应消息, 解析心跳响应消息失败", e);
            return;
        }
        log.info("收到心跳响应消息, msg_id:{}, 服务器时间:{}", heartBeatPong.getMsgId(), heartBeatPong.getReceivedTimeMillis());
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
