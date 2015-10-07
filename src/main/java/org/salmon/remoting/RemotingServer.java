package org.salmon.remoting;

import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;

import org.salmon.remoting.common.Pair;
import org.salmon.remoting.exception.RemotingSendRequestException;
import org.salmon.remoting.exception.RemotingTimeoutException;
import org.salmon.remoting.exception.RemotingTooMuchRequestException;
import org.salmon.remoting.netty.NettyRequestProcessor;
import org.salmon.remoting.protocol.RemotingCommand;

/**
 * 远程通信，Server接口
 *
 */
public interface RemotingServer extends RemotingService {

	/**
	 * 注册请求处理器，ExecutorService必须要对应一个队列大小有限制的阻塞队列，防止OOM
	 * 
	 * @param sid
	 * @param processor
	 * @param executor
	 */
	public void registerProcessor(final int code, final NettyRequestProcessor processor,
			final ExecutorService executor); 

	public Pair<NettyRequestProcessor, ExecutorService> getProcessorPair(final int code);

	/**
	 * 服务器绑定的本地端口
	 * 
	 * @return PORT
	 */
	public int localListenPort();

	public RemotingCommand invokeSync(final Channel channel, final RemotingCommand request, final long timeoutMillis)
			throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException;

	public void invokeAsync(final Channel channel, final RemotingCommand request, final long timeoutMillis,
			final InvokeCallback invokeCallback) throws InterruptedException, RemotingTooMuchRequestException,
					RemotingTimeoutException, RemotingSendRequestException;

	public void invokeOneway(final Channel channel, final RemotingCommand request, final long timeoutMillis)
			throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException,
			RemotingSendRequestException;

}
