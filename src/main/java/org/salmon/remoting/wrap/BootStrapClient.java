package org.salmon.remoting.wrap;

import java.util.concurrent.ExecutorService;

import org.salmon.remoting.ChannelEventListener;
import org.salmon.remoting.InvokeCallback;
import org.salmon.remoting.RPCHook;
import org.salmon.remoting.RemotingClient;
import org.salmon.remoting.exception.RemotingConnectException;
import org.salmon.remoting.exception.RemotingException;
import org.salmon.remoting.exception.RemotingSendRequestException;
import org.salmon.remoting.exception.RemotingTimeoutException;
import org.salmon.remoting.exception.RemotingTooMuchRequestException;
import org.salmon.remoting.netty.NettyClientConfig;
import org.salmon.remoting.netty.NettyRemotingClient;
import org.salmon.remoting.netty.NettyRequestProcessor;
import org.salmon.remoting.netty.ResponseFuture;
import org.salmon.remoting.protocol.RemotingCommand;
import org.salmon.remoting.protocol.RemotingCommand.RemotingSysResponseCode;

/**
 * 通讯辅助类(不提对象供序列化反序列化)
 * 
 * @author wuwenyu
 *
 */
public final class BootStrapClient {

	private long timeoutMillis = 6000;

	private RemotingClient remotingClient = null;

	private RemotingException convertException(int code) {
		if (code == RemotingSysResponseCode.REQUEST_CODE_NOT_SUPPORTED) {
			return new RemotingException("request code is not supported.");
		} else if (code == RemotingSysResponseCode.SYSTEM_BUSY) {
			return new RemotingException("system busy  maybe thread pool is exhausted.");
		} else if (code == RemotingSysResponseCode.SYSTEM_ERROR) {
			return new RemotingException("system error.");
		} else {
			return new RemotingException("unknow error.");
		}
	}

	public BootStrapClient(final NettyClientConfig nettyClientConfig) {
		remotingClient = new NettyRemotingClient(nettyClientConfig);
	}

	public BootStrapClient(final NettyClientConfig nettyClientConfig, final ChannelEventListener channelEventListener) {
		remotingClient = new NettyRemotingClient(nettyClientConfig, channelEventListener);
	}

	public BootStrapClient(final NettyClientConfig nettyClientConfig, long timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
		remotingClient = new NettyRemotingClient(nettyClientConfig);
	}

	public BootStrapClient(final NettyClientConfig nettyClientConfig, final ChannelEventListener channelEventListener,
			long timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
		remotingClient = new NettyRemotingClient(nettyClientConfig, channelEventListener);
	}

	public void registerProcessor(final int code, final NettyRequestProcessor processor, final ExecutorService executor,
			long timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
		remotingClient.registerProcessor(code, processor, executor);
	}

	public boolean isChannelWriteable(final String addr) {
		return remotingClient.isChannelWriteable(addr);
	}

	public byte[] invokeSync(final String addr, final int code, final byte[] request) throws InterruptedException,
			RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, RemotingException {
		return invokeSync(addr, code, "0.0.0", request, timeoutMillis);
	}

	public byte[] invokeSync(final String addr, final int code, String version, final byte[] request)
			throws InterruptedException, RemotingConnectException, RemotingSendRequestException,
			RemotingTimeoutException, RemotingException {
		RemotingCommand responseCommand = remotingClient.invokeSync(addr,
				RemotingCommand.createRequestCommand(code, version, request), timeoutMillis);
		if (responseCommand.getCode() != RemotingSysResponseCode.SUCCESS) {
			throw convertException(responseCommand.getCode());
		}
		return responseCommand.getBody();
	}

	public byte[] invokeSync(final String addr, final int code, final byte[] request, final long timeoutMillis)
			throws InterruptedException, RemotingConnectException, RemotingSendRequestException,
			RemotingTimeoutException, RemotingException {
		return invokeSync(addr, code, "0.0.0", request, timeoutMillis);
	}

	public byte[] invokeSync(final String addr, final int code, String version, final byte[] request,
			final long timeoutMillis) throws InterruptedException, RemotingConnectException,
					RemotingSendRequestException, RemotingTimeoutException, RemotingException {
		RemotingCommand responseCommand = remotingClient.invokeSync(addr,
				RemotingCommand.createRequestCommand(code, version, request), timeoutMillis);
		if (responseCommand.getCode() != RemotingSysResponseCode.SUCCESS) {
			throw convertException(responseCommand.getCode());
		}
		return responseCommand.getBody();
	}

	public void invokeAsync(final String addr, final int code, final byte[] request, final Callback callback)
			throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
			RemotingTimeoutException, RemotingSendRequestException {
		invokeAsync(addr, code, "0.0.0", request, timeoutMillis, callback);
	}

	public void invokeAsync(final String addr, final int code, String version, final byte[] request,
			final Callback callback) throws InterruptedException, RemotingConnectException,
					RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException {
		remotingClient.invokeAsync(addr, RemotingCommand.createRequestCommand(code, version, request), timeoutMillis,
				new InvokeCallback() {
					@Override
					public void operationComplete(ResponseFuture responseFuture) {
						if (callback != null) {
							if (responseFuture.getCause() != null) {
								callback.operationComplete(responseFuture.getResponseCommand() == null ? null
										: responseFuture.getResponseCommand().getBody());
							} else {
								callback.exception(responseFuture.getCause());
							}
						}
					}
				});
	}

	public void invokeAsync(final String addr, final int code, final byte[] request, final long timeoutMillis,
			final Callback callback) throws InterruptedException, RemotingConnectException,
					RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException {
		invokeAsync(addr, code, "0.0.0", request, timeoutMillis, callback);
	}

	public void invokeAsync(final String addr, final int code, String version, final byte[] request,
			final long timeoutMillis, final Callback callback) throws InterruptedException, RemotingConnectException,
					RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException {
		remotingClient.invokeAsync(addr, RemotingCommand.createRequestCommand(code, version, request), timeoutMillis,
				new InvokeCallback() {
					@Override
					public void operationComplete(ResponseFuture responseFuture) {
						if (callback != null) {
							if (responseFuture.getCause() != null) {
								callback.operationComplete(responseFuture.getResponseCommand() == null ? null
										: responseFuture.getResponseCommand().getBody());
							} else {
								callback.exception(responseFuture.getCause());
							}
						}
					}
				});
	}

	public void invokeOneway(final String addr, final int code, final byte[] request)
			throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
			RemotingTimeoutException, RemotingSendRequestException {
		invokeOneway(addr, code, "0.0.0", request, timeoutMillis);
	}

	public void invokeOneway(final String addr, final int code, String version, final byte[] request)
			throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
			RemotingTimeoutException, RemotingSendRequestException {
		remotingClient.invokeOneway(addr, RemotingCommand.createRequestCommand(code, version, request), timeoutMillis);
	}

	public void invokeOneway(final String addr, final int code, final byte[] request, final long timeoutMillis)
			throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
			RemotingTimeoutException, RemotingSendRequestException {
		invokeOneway(addr, code, "0.0.0", request, timeoutMillis);
	}

	public void invokeOneway(final String addr, final int code, String version, final byte[] request,
			final long timeoutMillis) throws InterruptedException, RemotingConnectException,
					RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException {
		remotingClient.invokeOneway(addr, RemotingCommand.createRequestCommand(code, version, request), timeoutMillis);
	}

	public void start() {
		remotingClient.start();
	}

	public void stop() {
		remotingClient.shutdown();
	}

	public void registerRPCHook(RPCHook rpcHook) {
		remotingClient.registerRPCHook(rpcHook);
	}

}
