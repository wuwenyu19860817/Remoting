package org.salmon.remoting.wrap;

import java.util.HashMap;

import org.salmon.remoting.netty.NettyRequestProcessor;
import org.salmon.remoting.protocol.RemotingCommand;
import org.salmon.remoting.protocol.RemotingCommand.RemotingSysResponseCode;

import io.netty.channel.ChannelHandlerContext;

/**
 * 抽象实现
 * 
 * @author wuwenyu
 *
 */
public abstract class AbstractRequestProcessor implements NettyRequestProcessor {

	@Override
	public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
		RemotingCommand ret = RemotingCommand.createResponseCommand(RemotingSysResponseCode.SUCCESS, "success.");
		ret.setBody(process(request.getExtFields(), request.getBody()));
		return ret;
	}

	/**
	 * 插件需要实现处理方法
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public abstract byte[] process(HashMap<String, String> header, byte[] body) throws Exception;

}
