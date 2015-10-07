package org.salmon.remoting.netty;

 
public class NettyClientConfig {
	
    /**
     * io读写线程数
     */
    private int clientSelectorThreads = 3; 

	/**
     * 事件派发线程
     */
    private int clientWorkerThreads = 4;
    
    /**
     * 业务相关线程数配置
     */
    private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();
    
    
    private int clientOnewaySemaphoreValue = NettySystemConfig.ClientOnewaySemaphoreValue;
    private int clientAsyncSemaphoreValue = NettySystemConfig.ClientAsyncSemaphoreValue;
    private long connectTimeoutMillis = 3000;
    private long channelNotActiveInterval = 1000 * 60;

    /**
     * IdleStateEvent will be triggered when neither read nor write was performed for
     * the specified period of this time. Specify {@code 0} to disable
     */
    private int clientChannelMaxIdleTimeSeconds = 0;

    private int clientSocketSndBufSize = NettySystemConfig.SocketSndbufSize;
    private int clientSocketRcvBufSize = NettySystemConfig.SocketRcvbufSize;
    private boolean clientPooledByteBufAllocatorEnable = false;


    public int getClientWorkerThreads() {
        return clientWorkerThreads;
    }


    public void setClientWorkerThreads(int clientWorkerThreads) {
        this.clientWorkerThreads = clientWorkerThreads;
    }


    public int getClientOnewaySemaphoreValue() {
        return clientOnewaySemaphoreValue;
    }


    public void setClientOnewaySemaphoreValue(int clientOnewaySemaphoreValue) {
        this.clientOnewaySemaphoreValue = clientOnewaySemaphoreValue;
    }


    public long getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }


    public void setConnectTimeoutMillis(long connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }


    public int getClientCallbackExecutorThreads() {
        return clientCallbackExecutorThreads;
    }


    public void setClientCallbackExecutorThreads(int clientCallbackExecutorThreads) {
        this.clientCallbackExecutorThreads = clientCallbackExecutorThreads;
    }


    public long getChannelNotActiveInterval() {
        return channelNotActiveInterval;
    }


    public void setChannelNotActiveInterval(long channelNotActiveInterval) {
        this.channelNotActiveInterval = channelNotActiveInterval;
    }


    public int getClientAsyncSemaphoreValue() {
        return clientAsyncSemaphoreValue;
    }


    public void setClientAsyncSemaphoreValue(int clientAsyncSemaphoreValue) {
        this.clientAsyncSemaphoreValue = clientAsyncSemaphoreValue;
    }


    public int getClientChannelMaxIdleTimeSeconds() {
        return clientChannelMaxIdleTimeSeconds;
    }


    public void setClientChannelMaxIdleTimeSeconds(int clientChannelMaxIdleTimeSeconds) {
        this.clientChannelMaxIdleTimeSeconds = clientChannelMaxIdleTimeSeconds;
    }


    public int getClientSocketSndBufSize() {
        return clientSocketSndBufSize;
    }


    public void setClientSocketSndBufSize(int clientSocketSndBufSize) {
        this.clientSocketSndBufSize = clientSocketSndBufSize;
    }


    public int getClientSocketRcvBufSize() {
        return clientSocketRcvBufSize;
    }


    public void setClientSocketRcvBufSize(int clientSocketRcvBufSize) {
        this.clientSocketRcvBufSize = clientSocketRcvBufSize;
    }


    public boolean isClientPooledByteBufAllocatorEnable() {
        return clientPooledByteBufAllocatorEnable;
    }


    public void setClientPooledByteBufAllocatorEnable(boolean clientPooledByteBufAllocatorEnable) {
        this.clientPooledByteBufAllocatorEnable = clientPooledByteBufAllocatorEnable;
    }
    
    public int getClientSelectorThreads() {
		return clientSelectorThreads;
	}


	public void setClientSelectorThreads(int clientSelectorThreads) {
		this.clientSelectorThreads = clientSelectorThreads;
	}
}
