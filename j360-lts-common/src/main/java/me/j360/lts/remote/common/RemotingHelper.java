package me.j360.lts.remote.common;


import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import me.j360.lts.remote.Channel;
import me.j360.lts.remote.ChannelHandlerListener;
import me.j360.lts.remote.Future;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * ͨ�Ų�һЩ��������
 */
public class RemotingHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingHelper.RemotingLogName);

    public static final String RemotingLogName = "LtsRemoting";

    /**
     * IP:PORT
     */
    public static SocketAddress string2SocketAddress(final String addr) {
        String[] s = addr.split(":");
        return new InetSocketAddress(s[0], Integer.valueOf(s[1]));
    }

    public static String parseChannelRemoteAddr(final Channel channel) {
        if (null == channel) {
            return "";
        }
        final SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";

        if (addr.length() > 0) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }

            return addr;
        }

        return "";
    }

    public static void closeChannel(Channel channel) {
        final String addrRemote = RemotingHelper.parseChannelRemoteAddr(channel);
        channel.close().addListener(new ChannelHandlerListener() {
            @Override
            public void operationComplete(Future future) throws Exception {
                LOGGER.info("closeChannel: close the connection to remote address[{}] result: {}", addrRemote,
                        future.isSuccess());
            }
        });
    }

}
