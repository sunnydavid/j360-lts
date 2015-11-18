package me.j360.lts.remote.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.utils.NamedThreadFactory;
import me.j360.lts.remote.*;
import me.j360.lts.remote.common.RemotingHelper;
import me.j360.lts.remote.exception.RemotingException;
import me.j360.lts.remote.protocol.RemotingCommand;

import java.net.InetSocketAddress;

/**
 * @author Robert HG (254963746@qq.com) on 11/3/15.
 */
public class NettyRemotingServer extends AbstractRemotingServer {

    public static final Logger LOGGER = AbstractRemotingServer.LOGGER;

    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup eventLoopGroup;
    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    public NettyRemotingServer(RemotingServerConfig remotingServerConfig) {
        this(remotingServerConfig, null);
    }

    public NettyRemotingServer(RemotingServerConfig remotingServerConfig, final ChannelEventListener channelEventListener) {
        super(remotingServerConfig, channelEventListener);
        this.serverBootstrap = new ServerBootstrap();
        this.eventLoopGroup = new NioEventLoopGroup(remotingServerConfig.getServerSelectorThreads());
    }

    @Override
    protected void serverStart() throws RemotingException {

        NettyLogger.setNettyLoggerFactory();

        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                remotingServerConfig.getServerWorkerThreads(),
                new NamedThreadFactory("NettyServerWorkerThread_")
        );

        final NettyCodecFactory nettyCodecFactory = new NettyCodecFactory(getCodec());

        this.serverBootstrap.group(this.eventLoopGroup, new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 65536)
                .option(ChannelOption.SO_REUSEADDR, true)
                        //
                .childOption(ChannelOption.TCP_NODELAY, true)
                .localAddress(new InetSocketAddress(this.remotingServerConfig.getListenPort()))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                defaultEventExecutorGroup, //
                                nettyCodecFactory.getEncoder(), //
                                nettyCodecFactory.getDecoder(), //
                                new IdleStateHandler(remotingServerConfig.getReaderIdleTimeSeconds(),
                                        remotingServerConfig.getWriterIdleTimeSeconds(), remotingServerConfig.getServerChannelMaxIdleTimeSeconds()),//
                                new NettyConnectManageHandler(), //
                                new NettyServerHandler());
                    }
                });

        try {
            this.serverBootstrap.bind().sync();
        } catch (InterruptedException e) {
            throw new RemotingException("Start Netty server bootstrap error", e);
        }
    }

    @Override
    protected void serverShutdown() {

        this.eventLoopGroup.shutdownGracefully();

        if (this.defaultEventExecutorGroup != null) {
            this.defaultEventExecutorGroup.shutdownGracefully();
        }
    }

    class NettyServerHandler extends SimpleChannelInboundHandler<RemotingCommand> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
            processMessageReceived(new NettyChannel(ctx), msg);
        }
    }

    class NettyConnectManageHandler extends ChannelDuplexHandler {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(new NettyChannel(ctx));
            LOGGER.info("SERVER : channelRegistered {}", remoteAddress);
            super.channelRegistered(ctx);
        }


        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(new NettyChannel(ctx));
            LOGGER.info("SERVER : channelUnregistered, the channel[{}]", remoteAddress);
            super.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            me.j360.lts.remote.Channel channel = new NettyChannel(ctx);
            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
            LOGGER.info("SERVER: channelActive, the channel[{}]", remoteAddress);
            super.channelActive(ctx);

            if (channelEventListener != null) {
                putRemotingEvent(new RemotingEvent(RemotingEventType.CONNECT, remoteAddress, channel));
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            me.j360.lts.remote.Channel channel = new NettyChannel(ctx);

            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
            LOGGER.info("SERVER: channelInactive, the channel[{}]", remoteAddress);
            super.channelInactive(ctx);

            if (channelEventListener != null) {
                putRemotingEvent(new RemotingEvent(RemotingEventType.CLOSE, remoteAddress, channel));
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;

                me.j360.lts.remote.Channel channel = new NettyChannel(ctx);

                final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);

                if (event.state().equals(IdleState.ALL_IDLE)) {
                    LOGGER.warn("SERVER: IDLE [{}]", remoteAddress);
                    RemotingHelper.closeChannel(channel);
                }

                if (channelEventListener != null) {
                    RemotingEventType remotingEventType = RemotingEventType.valueOf(event.state().name());
                    putRemotingEvent(new RemotingEvent(remotingEventType,
                            remoteAddress, channel));
                }
            }

            ctx.fireUserEventTriggered(evt);
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

            me.j360.lts.remote.Channel channel = new NettyChannel(ctx);

            final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
            LOGGER.warn("SERVER: exceptionCaught {}", remoteAddress);
            LOGGER.warn("SERVER: exceptionCaught exception.", cause);

            if (channelEventListener != null) {
                putRemotingEvent(new RemotingEvent(RemotingEventType.EXCEPTION, remoteAddress, channel));
            }

            RemotingHelper.closeChannel(channel);
        }
    }

}
