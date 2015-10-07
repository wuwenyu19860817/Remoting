package org.salmon.remoting.netty;

import org.salmon.remoting.protocol.RemotingCommand;

import io.netty.channel.ChannelHandlerContext;

/**
 * rpc处理器
 *
 */
public interface NettyRequestProcessor {
    RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
            throws Exception;
}
