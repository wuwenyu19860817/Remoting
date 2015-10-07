package org.salmon.remoting.netty;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.salmon.remoting.ChannelEventListener;
import org.salmon.remoting.InvokeCallback;
import org.salmon.remoting.RPCHook;
import org.salmon.remoting.common.Pair;
import org.salmon.remoting.common.RemotingUtil;
import org.salmon.remoting.common.SemaphoreReleaseOnlyOnce;
import org.salmon.remoting.common.ServiceThread;
import org.salmon.remoting.exception.RemotingSendRequestException;
import org.salmon.remoting.exception.RemotingTimeoutException;
import org.salmon.remoting.exception.RemotingTooMuchRequestException;
import org.salmon.remoting.protocol.RemotingCommand;
import org.salmon.remoting.protocol.RemotingCommand.RemotingSysResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

/**
 * Server与Client公用抽象类
 *
 */
public abstract class NettyRemotingAbstract {
	private static final Logger plog = LoggerFactory.getLogger(NettyRemotingAbstract.class);

	// 信号量，Oneway情况会使用，防止本地Netty缓存请求过多(控制oneway调用并发数)
	protected final Semaphore semaphoreOneway;

	// 信号量，异步调用情况会使用，防止本地Netty缓存请求过多(控制异步调用并发数)
	protected final Semaphore semaphoreAsync;

	// 缓存所有对外请求
	protected final ConcurrentHashMap<Integer /* requestId */, ResponseFuture> responseTable = new ConcurrentHashMap<Integer, ResponseFuture>(
			256); 

	// 注册的各个RPC处理器
	protected final HashMap<String/* sid */, Pair<NettyRequestProcessor, ExecutorService>> processorTable = new HashMap<String, Pair<NettyRequestProcessor, ExecutorService>>(
			64);

	// 事件执行器
	protected final NettyEventExecuter nettyEventExecuter = new NettyEventExecuter();

	// 事件触发自定义监听回调对象
	public abstract ChannelEventListener getChannelEventListener();

	// 自定义的钩子过程
	public abstract RPCHook getRPCHook();

	public void putNettyEvent(final NettyEvent event) {
		this.nettyEventExecuter.putNettyEvent(event);
	}

	class NettyEventExecuter extends ServiceThread {
		private final LinkedBlockingQueue<NettyEvent> eventQueue = new LinkedBlockingQueue<NettyEvent>();
		private final int MaxSize = 10000;

		public void putNettyEvent(final NettyEvent event) {
			if (this.eventQueue.size() <= MaxSize) {
				this.eventQueue.add(event);
			} else {
				plog.warn("event queue size[{}] enough, so drop this event {}", this.eventQueue.size(),
						event.toString());
			}
		}

		@Override
		public void run() {
			plog.info(this.getServiceName() + " service started");

			final ChannelEventListener listener = NettyRemotingAbstract.this.getChannelEventListener();

			while (!this.isStoped()) {
				try {
					NettyEvent event = this.eventQueue.poll(3000, TimeUnit.MILLISECONDS);
					if (event != null && listener != null) {
						switch (event.getType()) {
						case IDLE:
							listener.onChannelIdle(event.getRemoteAddr(), event.getChannel());
							break;
						case CLOSE:
							listener.onChannelClose(event.getRemoteAddr(), event.getChannel());
							break;
						case CONNECT:
							listener.onChannelConnect(event.getRemoteAddr(), event.getChannel());
							break;
						case EXCEPTION:
							listener.onChannelException(event.getRemoteAddr(), event.getChannel());
							break;
						default:
							break;

						}
					}
				} catch (Exception e) {
					plog.warn(this.getServiceName() + " service has exception. ", e);
				}
			}

			plog.info(this.getServiceName() + " service end");
		}

		@Override
		public String getServiceName() {
			return NettyEventExecuter.class.getSimpleName();
		}
	}

	public NettyRemotingAbstract(final int permitsOneway, final int permitsAsync) {
		this.semaphoreOneway = new Semaphore(permitsOneway, true);
		this.semaphoreAsync = new Semaphore(permitsAsync, true);
	}

	/**
	 * 处理发送来的请求
	 * 
	 * @param ctx
	 * @param cmd
	 */
	public void processRequestCommand(final ChannelHandlerContext ctx, final RemotingCommand cmd) {
		String sid = cmd.getCode() + RemotingCommand.splitVersion + cmd.getVersion();
		final Pair<NettyRequestProcessor, ExecutorService> pair = this.processorTable.get(sid); 

		if (pair != null) {
			Runnable run = new Runnable() {
				@Override
				public void run() {
					try {
						RPCHook rpcHook = NettyRemotingAbstract.this.getRPCHook();
						if (rpcHook != null) {
							rpcHook.doBeforeRequest(RemotingUtil.parseChannelRemoteAddr(ctx.channel()), cmd);
						}

						final RemotingCommand response = pair.getObject1().processRequest(ctx, cmd);
						if (rpcHook != null) {
							rpcHook.doAfterResponse(RemotingUtil.parseChannelRemoteAddr(ctx.channel()), cmd, response);
						}

						if (!cmd.isOnewayRPC()) {
							if (response != null) {
								response.setOpaque(cmd.getOpaque());
								response.markResponseType();
								try {
									ctx.writeAndFlush(response);
								} catch (Throwable e) {
									plog.error("process request over, but response failed", e);
									plog.error(cmd.toString());
									plog.error(response.toString());
								}
							} else { 
								// 收到请求，但是没有返回应答，可能是processRequest中进行了应答，忽略这种情况
								plog.warn("ignore response:"+cmd.toString());
							}
						}
					} catch (Throwable e) {
						plog.error("process request exception", e);
						plog.error(cmd.toString());

						if (!cmd.isOnewayRPC()) {
							final RemotingCommand response = RemotingCommand.createResponseCommand(
									RemotingSysResponseCode.SYSTEM_ERROR, //
									RemotingUtil.exceptionSimpleDesc(e));
							response.setOpaque(cmd.getOpaque());
							ctx.writeAndFlush(response);
						}
					}
				}
			};

			try {
				// 这里需要做流控，要求线程池对应的队列必须是有大小限制的
				pair.getObject2().submit(run);
			} catch (RejectedExecutionException e) {
				// 每个线程10s打印一次
				if ((System.currentTimeMillis() % 10000) == 0) {
					plog.warn(RemotingUtil.parseChannelRemoteAddr(ctx.channel()) //
							+ ", too many requests and system thread pool busy, RejectedExecutionException " //
							+ pair.getObject2().toString() //
							+ " request code: " + sid);
				}

				if (!cmd.isOnewayRPC()) {
					final RemotingCommand response = RemotingCommand.createResponseCommand(
							RemotingSysResponseCode.SYSTEM_BUSY,
							"too many requests and system thread pool busy, please try another server");
					response.setOpaque(cmd.getOpaque());
					ctx.writeAndFlush(response);
				}
			}
		} else {
			String error = " request service [" + sid + "] not supported";
			if (!cmd.isOnewayRPC()) {
				final RemotingCommand response = RemotingCommand
						.createResponseCommand(RemotingSysResponseCode.REQUEST_CODE_NOT_SUPPORTED, error);
				response.setOpaque(cmd.getOpaque());
				ctx.writeAndFlush(response);
			}
			plog.error(RemotingUtil.parseChannelRemoteAddr(ctx.channel()) + error);
		}
	}

	/**
	 * 处理回来的响应
	 * 
	 * @param ctx
	 * @param cmd
	 */
	public void processResponseCommand(ChannelHandlerContext ctx, RemotingCommand cmd) {
		final ResponseFuture responseFuture = responseTable.get(cmd.getOpaque());
		if (responseFuture != null) {
			responseFuture.setResponseCommand(cmd);
			responseFuture.release();
			responseTable.remove(cmd.getOpaque());

			if (responseFuture.getInvokeCallback() != null) {
				boolean runInThisThread = false;
				ExecutorService executor = this.getCallbackExecutor();
				if (executor != null) {
					try {
						executor.submit(new Runnable() {
							@Override
							public void run() {
								try {
									responseFuture.executeInvokeCallback();
								} catch (Throwable e) {
									plog.warn("excute callback in executor exception, and callback throw", e);
								}
							}
						});
					} catch (Exception e) {
						runInThisThread = true;
						plog.warn("excute callback in executor exception, maybe executor busy", e);
					}
				} else {
					runInThisThread = true;
				}

				if (runInThisThread) {
					try {
						responseFuture.executeInvokeCallback();
					} catch (Throwable e) {
						plog.warn("executeInvokeCallback Exception", e);
					}
				}
			} else {
				responseFuture.putResponse(cmd);
			}
		} else {
			plog.warn("receive response, but not matched any request, "
					+ RemotingUtil.parseChannelRemoteAddr(ctx.channel()));
			plog.warn(cmd.toString());
		}

	}

	/**
	 * 接收请求或者响应(两端对等)
	 * 
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
	public void processMessageReceived(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
		final RemotingCommand cmd = msg;
		if (cmd != null) {
			switch (cmd.getType()) {
			case REQUEST_COMMAND:
				processRequestCommand(ctx, cmd);
				break;
			case RESPONSE_COMMAND:
				processResponseCommand(ctx, cmd);
				break;
			default:
				break;
			}
		}
	}

	abstract public ExecutorService getCallbackExecutor();

	/**
	 * 处理超时的请求
	 */
	protected void scanResponseTable() {
		Iterator<Entry<Integer, ResponseFuture>> it = this.responseTable.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, ResponseFuture> next = it.next();
			ResponseFuture rep = next.getValue();

			if ((rep.getBeginTimestamp() + rep.getTimeoutMillis() + 1000) <= System.currentTimeMillis()) {
				it.remove();
				try {
					rep.setCause(new RemotingTimeoutException(null, rep.getTimeoutMillis()));
					rep.executeInvokeCallback();
				} catch (Throwable e) {
					plog.warn("scanResponseTable, operationComplete Exception", e);
				} finally {
					rep.release();
				}

				plog.warn("remove timeout request, " + rep);
			}
		}
	}

	public RemotingCommand invokeSyncImpl(final Channel channel, final RemotingCommand request,
			final long timeoutMillis)
					throws InterruptedException, RemotingSendRequestException, RemotingTimeoutException {
		try {
			final ResponseFuture responseFuture = new ResponseFuture(request.getOpaque(), timeoutMillis, null, null);
			this.responseTable.put(request.getOpaque(), responseFuture);
			channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture f) throws Exception {
					if (f.isSuccess()) {
						responseFuture.setSendRequestOK(true);
						return;
					} else {
						responseFuture.setSendRequestOK(false);
					}

					responseTable.remove(request.getOpaque());
					responseFuture.setCause(f.cause());
					responseFuture.putResponse(null);
					plog.warn("invokeSyncImpl send a request command to channel <" + channel.remoteAddress()
							+ "> failed.");
					plog.warn(request.toString());
				}
			});

			RemotingCommand responseCommand = responseFuture.waitResponse(timeoutMillis);
			if (null == responseCommand) {
				if (responseFuture.isSendRequestOK()) {
					throw new RemotingTimeoutException(RemotingUtil.parseChannelRemoteAddr(channel), timeoutMillis,
							responseFuture.getCause());
				} else {
					throw new RemotingSendRequestException(RemotingUtil.parseChannelRemoteAddr(channel),
							responseFuture.getCause());
				}
			}

			return responseCommand;
		} finally {
			this.responseTable.remove(request.getOpaque());
		}
	}

	public void invokeAsyncImpl(final Channel channel, final RemotingCommand request, final long timeoutMillis,
			final InvokeCallback invokeCallback) throws InterruptedException, RemotingTooMuchRequestException,
					RemotingTimeoutException, RemotingSendRequestException {
		boolean acquired = this.semaphoreAsync.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
		if (acquired) {
			final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreAsync);

			final ResponseFuture responseFuture = new ResponseFuture(request.getOpaque(), timeoutMillis, invokeCallback,
					once);
			this.responseTable.put(request.getOpaque(), responseFuture);
			try {
				channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture f) throws Exception {
						if (f.isSuccess()) {
							responseFuture.setSendRequestOK(true);
							return;
						} else {
							responseFuture.setSendRequestOK(false);
						}

						responseFuture.putResponse(null);
						responseFuture.setCause(new RemotingSendRequestException(
								RemotingUtil.parseChannelRemoteAddr(channel), f.cause()));
						responseTable.remove(request.getOpaque());
						try {
							responseFuture.executeInvokeCallback();
						} catch (Throwable e) {
							plog.warn("excute callback in writeAndFlush addListener, and callback throw", e);
						} finally {
							responseFuture.release();
						}

						plog.warn("invokeAsyncImpl send a request command to channel <{}> failed.",
								RemotingUtil.parseChannelRemoteAddr(channel));
						plog.warn(request.toString());
					}
				});
			} catch (Exception e) {
				responseFuture.release();
				plog.warn("send a request command to channel <" + RemotingUtil.parseChannelRemoteAddr(channel)
						+ "> Exception", e);
				throw new RemotingSendRequestException(RemotingUtil.parseChannelRemoteAddr(channel), e);
			}
		} else {
			if (timeoutMillis <= 0) {
				throw new RemotingTooMuchRequestException("invokeAsyncImpl invoke too fast");
			} else {
				String info = String.format(
						"invokeAsyncImpl tryAcquire semaphore timeout, %dms, waiting thread nums: %d semaphoreAsyncValue: %d", //
						timeoutMillis, //
						this.semaphoreAsync.getQueueLength(), //
						this.semaphoreAsync.availablePermits()//
				);
				plog.warn(info);
				plog.warn(request.toString());
				throw new RemotingTimeoutException(info);
			}
		}
	}

	public void invokeOnewayImpl(final Channel channel, final RemotingCommand request, final long timeoutMillis)
			throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException,
			RemotingSendRequestException {
		request.markOnewayRPC();
		boolean acquired = this.semaphoreOneway.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
		if (acquired) {
			final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreOneway);
			try {
				channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture f) throws Exception {
						once.release();
						if (!f.isSuccess()) {
							plog.warn("invokeOnewayImpl send a request command to channel <" + channel.remoteAddress()
									+ "> failed.");
							plog.warn(request.toString());
						}
					}
				});
			} catch (Exception e) {
				once.release();
				plog.warn("write send a request command to channel <" + channel.remoteAddress() + "> failed.");
				throw new RemotingSendRequestException(RemotingUtil.parseChannelRemoteAddr(channel), e);
			}
		} else {
			if (timeoutMillis <= 0) {
				throw new RemotingTooMuchRequestException("invokeOnewayImpl invoke too fast");
			} else {
				String info = String.format(
						"invokeOnewayImpl tryAcquire semaphore timeout, %dms, waiting thread nums: %d semaphoreAsyncValue: %d", //
						timeoutMillis, //
						this.semaphoreAsync.getQueueLength(), //
						this.semaphoreAsync.availablePermits()//
				);
				plog.warn(info);
				plog.warn(request.toString());
				throw new RemotingTimeoutException(info);
			}
		}
	}
}
