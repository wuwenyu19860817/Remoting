package org.salmon.remoting.wrap;

import org.salmon.remoting.netty.NettyRequestProcessor;
import org.salmon.remoting.protocol.RemotingCommand;

import io.netty.channel.ChannelHandlerContext;

public class AbstractRequestProcessor implements NettyRequestProcessor {

	@Override
	public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] process(byte[] request) {
		return null;
	}

}
