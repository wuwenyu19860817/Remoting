package org.salmon.remoting;
 
import java.util.concurrent.ExecutorService;

import org.salmon.remoting.exception.RemotingConnectException;
import org.salmon.remoting.exception.RemotingSendRequestException;
import org.salmon.remoting.exception.RemotingTimeoutException;
import org.salmon.remoting.exception.RemotingTooMuchRequestException;
import org.salmon.remoting.netty.NettyRequestProcessor;
import org.salmon.remoting.protocol.RemotingCommand;

/**
 * 远程通信，Client接口
 */
public interface RemotingClient extends RemotingService { 

	public RemotingCommand invokeSync(final String addr, final RemotingCommand request, final long timeoutMillis)
			throws InterruptedException, RemotingConnectException, RemotingSendRequestException,
			RemotingTimeoutException;

	public void invokeAsync(final String addr, final RemotingCommand request, final long timeoutMillis,
			final InvokeCallback invokeCallback) throws InterruptedException, RemotingConnectException,
					RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

	public void invokeOneway(final String addr, final RemotingCommand request, final long timeoutMillis)
			throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
			RemotingTimeoutException, RemotingSendRequestException;

	public void registerProcessor(final int code, final NettyRequestProcessor processor,
			final ExecutorService executor);

	public boolean isChannelWriteable(final String addr);
}
