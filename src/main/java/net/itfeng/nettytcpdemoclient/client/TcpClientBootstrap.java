package net.itfeng.nettytcpdemoclient.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import net.itfeng.nettytcpdemoclient.context.ClientChannelContext;
import net.itfeng.nettytcpdemoclient.handler.MyMessageHandler;
import net.itfeng.nettytcpdemoclient.protocol.DataTransPackageOuterClass;
import net.itfeng.nettytcpdemoclient.protocol.TestDataTransOuterClass;
import net.itfeng.nettytcpdemoclient.service.MessageAsyncPublishService;
import net.itfeng.nettytcpdemoclient.util.ClientIdUtil;
import net.itfeng.nettytcpdemoclient.util.MessageIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TCP连接netty客户端引导类
 *
 * @author itfeng
 * @since 2024/2/25 16:10
 */
@Slf4j
@Component
public class TcpClientBootstrap {
    private static final AtomicBoolean connected = new AtomicBoolean(false);

    @Value("${netty.tcp.serverHost}")
    private String serverHost;
    @Value("${netty.tcp.serverPort}")
    private int serverPort;

    @Autowired
    private List<MyMessageHandler> myMessageHandlers;

    @Autowired
    private MessageAsyncPublishService messageAsyncPublishService;
    private EventLoopGroup eventLoopGroup;

    private ExecutorService executorService;

    @PostConstruct
    public void start(){
        this.eventLoopGroup = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        channel.pipeline()
                                .addLast(new ProtobufVarint32FrameDecoder())
                                .addLast(new ProtobufDecoder(DataTransPackageOuterClass.DataTransPackage.getDefaultInstance()))
                                .addLast(new ProtobufVarint32LengthFieldPrepender())
                                .addLast(new ProtobufEncoder())
                                .addLast(new SimpleChannelInboundHandler<DataTransPackageOuterClass.DataTransPackage>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DataTransPackageOuterClass.DataTransPackage dataTransPackage)  {
                                        if(dataTransPackage.getPbData().isEmpty()){
                                            log.warn("接收到的消息没有消息体 ，clientId: {} , dataType: {} ",dataTransPackage.getClientId(), dataTransPackage.getDataType());
                                            return;
                                        }
                                        ClientChannelContext.updateLastInboundTime();
                                        myMessageHandlers.stream()
                                                .filter(myMessageHandler -> myMessageHandler.isSupport(dataTransPackage.getDataType()))
                                                .forEach(myMessageHandler->myMessageHandler.handle(dataTransPackage.getPbData().toByteArray()));
                                    }
                                    @Override
                                    public void channelInactive(ChannelHandlerContext ctx){
                                        // 客户端断开连接
                                        log.info("客户端连接断开 channel: {}",ctx.channel().id());
                                        connected.set(false);
                                        ctx.fireChannelInactive();
                                    }
                                });
                    }
                });
        try {
            ChannelFuture f = bootstrap.connect(serverHost, serverPort).sync();
            ClientChannelContext.put(f.channel());
            connected.set(true);
            publishOnlineEvent();
            startHeartbeat();
        }catch (Exception e){
            log.error("出现异常 --------------------- 5秒后重连");
            eventLoopGroup.shutdownGracefully().syncUninterruptibly();
            if(connected.get()) {
                executorService.shutdownNow();
            }
            connected.set(false);
            // 5秒后重连
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            start();
        }

    }

    @PreDestroy
    public void stop(){
        if(connected.get()) {
            log.info("开始停止 ---------------------------");
            executorService.shutdownNow();
            try {
                ClientChannelContext.getChannel().close().sync();
            }catch (Exception e){
                log.error("关闭Channel异常",e);
            }
            eventLoopGroup.shutdownGracefully().syncUninterruptibly();
            connected.set(false);
            log.info("停止完成 ---------------------------");
        }
    }

    private void restart(){
        stop();
        start();
    }


    private void startHeartbeat() {
        // Start a thread to send heartbeat messages
        Thread heartbeatThreat = new Thread(() -> {
            while (connected.get()) {
                try {
                    if (connected.get()) {
                        TestDataTransOuterClass.HeartBeatPing ping = buildHeartBeatPing();

                        // Send a heartbeat message
                        messageAsyncPublishService.publish(DataTransPackageOuterClass.DataType.HEARTBEAT_PING, ping.toByteArray(), ping.getClientId());
                        log.info("发送心跳 msg_id:{}, timestamp:{}",ping.getMsgId(), System.currentTimeMillis());
                    }
                    Thread.sleep(5000); // Send a heartbeat every 5 seconds
                } catch (InterruptedException e) {
                    log.error("发送心跳异常", e);
                }
                if(System.currentTimeMillis() - ClientChannelContext.getLastInboundTime() >15000){
                    log.error("接收服务端数据超时，执行重连 --------------------------");
                    restart();
                    break;
                }
            }
        },"heartbeat-thread");
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(heartbeatThreat);
    }
    private void publishOnlineEvent() {
        if (connected.get()) {
            // 构建上线事件
            TestDataTransOuterClass.OnlineEvent onlineEvent = buildOnlineEvent();
            messageAsyncPublishService.publish(DataTransPackageOuterClass.DataType.ONLINE_EVENT, onlineEvent.toByteArray(),onlineEvent.getClientId());
            log.info("发送上线事件");
        }
    }
    private TestDataTransOuterClass.OnlineEvent buildOnlineEvent() {
        TestDataTransOuterClass.OnlineEvent.Builder builder = TestDataTransOuterClass.OnlineEvent.newBuilder();
        builder.setClientId(ClientIdUtil.getClientId())
                .setStartTimeMillis(System.currentTimeMillis())
                .setMsgId(MessageIdUtil.getMessageId());
        return builder.build();
    }
    private TestDataTransOuterClass.HeartBeatPing buildHeartBeatPing() {
        TestDataTransOuterClass.HeartBeatPing.Builder builder = TestDataTransOuterClass.HeartBeatPing.newBuilder();
        builder.setClientId(ClientIdUtil.getClientId())
                .setStartTimeMillis(System.currentTimeMillis())
                .setMsgId(MessageIdUtil.getMessageId());
        return builder.build();
    }
}
