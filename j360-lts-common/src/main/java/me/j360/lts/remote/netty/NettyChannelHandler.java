package me.j360.lts.remote.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import me.j360.lts.remote.ChannelHandler;
import me.j360.lts.remote.ChannelHandlerListener;
import me.j360.lts.remote.Future;

/**
 * @author Robert HG (254963746@qq.com) on 11/3/15.
 */
public class NettyChannelHandler implements ChannelHandler {

    private ChannelFuture channelFuture;

    public NettyChannelHandler(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    @Override
    public ChannelHandler addListener(final ChannelHandlerListener listener) {

        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {
                listener.operationComplete(new Future() {
                    @Override
                    public boolean isSuccess() {
                        return future.isSuccess();
                    }

                    @Override
                    public Throwable cause() {
                        return future.cause();
                    }
                });
            }
        });

        return this;
    }
}
