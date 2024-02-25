package net.itfeng.nettytcpdemoclient.handler;

import com.google.protobuf.InvalidProtocolBufferException;
import net.itfeng.nettytcpdemoclient.protocol.DataTransPackageOuterClass;
import net.itfeng.nettytcpdemoclient.protocol.TestDataTransOuterClass;
import net.itfeng.nettytcpdemoclient.service.MessageAsyncPublishService;
import net.itfeng.nettytcpdemoclient.util.ClientIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 测试消息处理类
 *
 * @author fengxubo
 * @since 2024/1/27 10:13
 * 
 */
@Service
public class TestDataMessageHandler implements MyMessageHandler{

    private static final DataTransPackageOuterClass.DataType DATA_TYPE = DataTransPackageOuterClass.DataType.TEST_DATA_TRANS;

    @Autowired
    private MessageAsyncPublishService messageAsyncPublishService;
    @Async("messageHandlerPool")
    @Override
    public void handle(byte[] messageBytes) {
        try {
            TestDataTransOuterClass.TestDataTrans testData = TestDataTransOuterClass.TestDataTrans.parseFrom(messageBytes);
            if(testData != null) {
                // 发送一个result，然后开始处理
                messageAsyncPublishService.publish(DataTransPackageOuterClass.DataType.TEST_DATA_TRANS_RESULT,buildSuccessResult(testData).toByteArray(),testData.getClientId());
            }else{
                // 发送一个消息为空的result
                messageAsyncPublishService.publish(DataTransPackageOuterClass.DataType.TEST_DATA_TRANS_RESULT,buildResult("TEST_DATA_NULL").toByteArray(),testData.getClientId());
            }
        } catch (InvalidProtocolBufferException e) {
            // 发送一个解析失败的result
            messageAsyncPublishService.publish(DataTransPackageOuterClass.DataType.TEST_DATA_TRANS_RESULT,buildResult("PB_ERROR").toByteArray(),ClientIdUtil.getClientId());
            throw new RuntimeException(e);
        }
    }

    private TestDataTransOuterClass.TestDataTransResult buildSuccessResult(TestDataTransOuterClass.TestDataTrans testData) {
        return TestDataTransOuterClass.TestDataTransResult.newBuilder()
                .setStartTimeMillis(testData.getStartTimeMillis())
                .setClientId(testData.getClientId())
                .setMsgId(testData.getMsgId())
                .setResult("SUCCESS")
                .setReceivedTimeMillis(System.currentTimeMillis())
                .build();
    }

    private TestDataTransOuterClass.TestDataTransResult buildResult(String info) {
        return TestDataTransOuterClass.TestDataTransResult.newBuilder()
        .setResult(info).setReceivedTimeMillis(System.currentTimeMillis()).build();
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
