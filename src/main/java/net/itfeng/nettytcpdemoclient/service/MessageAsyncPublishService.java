package net.itfeng.nettytcpdemoclient.service;

import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import net.itfeng.nettytcpdemoclient.context.ClientChannelContext;
import net.itfeng.nettytcpdemoclient.protocol.DataTransPackageOuterClass;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 异步代理处理
 *
 * @author itfeng
 * @since 2024/1/9 20:16
 * 
 */
@Slf4j
@Service
public class MessageAsyncPublishService {

    @Async("messagePublisherPool")
    public void publish(DataTransPackageOuterClass.DataType dataType, byte[] message,String clientId) {
        Channel channel = ClientChannelContext.getChannel();
        if(channel==null){
            log.error("消息发送异常，没有获取到 channel ,clientId: {}",clientId);
            return;
        }
        DataTransPackageOuterClass.DataTransPackage dataTransPackage = DataTransPackageOuterClass.DataTransPackage.newBuilder()
                .setDataType(dataType)
                .setClientId(clientId)
                .setPbData(ByteString.copyFrom(message)).build();
        if(channel.isActive()){
            // 这里只返回了ChannelFuture，实际做业务时需要考虑传输结果
            ChannelFuture channelFuture = channel.writeAndFlush(dataTransPackage);
            long writableBytes = channel.bytesBeforeUnwritable();
            if (channel.isWritable()) {
                if (writableBytes< 1024 ) {
                    log.info("NettyWrite Ok id:{}  writableBytes:{}", channel.id(), writableBytes);
                }
            }else {
                log.warn("NettyWrite Fail id:{}  writableBytes:{}", channel.id(), writableBytes);
            }
        }else{
            log.warn("Netty channel inactive id:{} ", channel.id());
        }
    }
}
