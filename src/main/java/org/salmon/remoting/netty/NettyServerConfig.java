package org.salmon.remoting.netty;

 
public class NettyServerConfig {
	/**
	 * 默认监听端口
	 */
    private int listenPort = 8888;
    
    /**
     * 事件派发线程
     */
    private int serverWorkerThreads = 8;
    
    /**
     * 业务相关线程数配置
     */
    private int serverCallbackExecutorThreads = 0;
    
    /**
     * io读写线程数
     */
    private int serverSelectorThreads = 3;
    
    /**
     * 控制异步并发数
     */
    private int serverOnewaySemaphoreValue = 256;
    private int serverAsyncSemaphoreValue = 64;
    
    /**
     * 控制连接闲置自动断开
     */
    private int serverChannelMaxIdleTimeSeconds = 0;

    private int serverSocketSndBufSize = NettySystemConfig.SocketSndbufSize;
    private int serverSocketRcvBufSize = NettySystemConfig.SocketRcvbufSize;
    private boolean serverPooledByteBufAllocatorEnable = false;


    public int getListenPort() {
        return listenPort;
    }


    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }


    public int getServerWorkerThreads() {
        return serverWorkerThreads;
    }


    public void setServerWorkerThreads(int serverWorkerThreads) {
        this.serverWorkerThreads = serverWorkerThreads;
    }


    public int getServerSelectorThreads() {
        return serverSelectorThreads;
    }


    public void setServerSelectorThreads(int serverSelectorThreads) {
        this.serverSelectorThreads = serverSelectorThreads;
    }


    public int getServerOnewaySemaphoreValue() {
        return serverOnewaySemaphoreValue;
    }


    public void setServerOnewaySemaphoreValue(int serverOnewaySemaphoreValue) {
        this.serverOnewaySemaphoreValue = serverOnewaySemaphoreValue;
    }


    public int getServerCallbackExecutorThreads() {
        return serverCallbackExecutorThreads;
    }


    public void setServerCallbackExecutorThreads(int serverCallbackExecutorThreads) {
        this.serverCallbackExecutorThreads = serverCallbackExecutorThreads;
    }


    public int getServerAsyncSemaphoreValue() {
        return serverAsyncSemaphoreValue;
    }


    public void setServerAsyncSemaphoreValue(int serverAsyncSemaphoreValue) {
        this.serverAsyncSemaphoreValue = serverAsyncSemaphoreValue;
    }


    public int getServerChannelMaxIdleTimeSeconds() {
        return serverChannelMaxIdleTimeSeconds;
    }


    public void setServerChannelMaxIdleTimeSeconds(int serverChannelMaxIdleTimeSeconds) {
        this.serverChannelMaxIdleTimeSeconds = serverChannelMaxIdleTimeSeconds;
    }


    public int getServerSocketSndBufSize() {
        return serverSocketSndBufSize;
    }


    public void setServerSocketSndBufSize(int serverSocketSndBufSize) {
        this.serverSocketSndBufSize = serverSocketSndBufSize;
    }


    public int getServerSocketRcvBufSize() {
        return serverSocketRcvBufSize;
    }


    public void setServerSocketRcvBufSize(int serverSocketRcvBufSize) {
        this.serverSocketRcvBufSize = serverSocketRcvBufSize;
    }


    public boolean isServerPooledByteBufAllocatorEnable() {
        return serverPooledByteBufAllocatorEnable;
    }


    public void setServerPooledByteBufAllocatorEnable(boolean serverPooledByteBufAllocatorEnable) {
        this.serverPooledByteBufAllocatorEnable = serverPooledByteBufAllocatorEnable;
    }
}
