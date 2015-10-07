//package org.salmon.remoting.remoting.function;
//
//import org.salmon.remoting.RemotingClient;
//import org.salmon.remoting.RemotingServer;
//import org.salmon.remoting.netty.NettyClientConfig;
//import org.salmon.remoting.netty.NettyRemotingClient;
//import org.salmon.remoting.netty.NettyRemotingServer;
//import org.salmon.remoting.netty.NettyRequestProcessor;
//import org.salmon.remoting.netty.NettyServerConfig;
//import org.salmon.remoting.protocol.RemotingCommand; 
//import org.salmon.remoting.protocol.RemotingCommand.RemotingSysResponseCode;
//
//import io.netty.channel.ChannelHandlerContext;
//
//public class RPCTest {
//	public static RemotingClient createRemotingClient() {
//		NettyClientConfig config = new NettyClientConfig();
//		RemotingClient client = new NettyRemotingClient(config);
//		return client;
//	}
//
//	public static RemotingServer createRemotingServer() throws InterruptedException {
//		NettyServerConfig config = new NettyServerConfig();
//		RemotingServer remotingServer = new NettyRemotingServer(config,"D:\\loader");
//		return remotingServer;
//	}
//	
//	public static void main(String[] args) throws Exception {
//		final RemotingServer remotingServer = createRemotingServer();
//		final RemotingClient remotingClient = createRemotingClient();
//		remotingServer.registerDefaultProcessor(new NettyRequestProcessor() {
//			@Override
//			public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
//					throws Exception { 
//				return RemotingCommand.createResponseCommand(RemotingSysResponseCode.REQUEST_CODE_NOT_SUPPORTED, "没有合适的rpc处理器。");
//			}
//		}, null); 
//		remotingServer.start();
//		remotingClient.start();
//		RemotingCommand ret = remotingClient.invokeSync("localhost:8888", RemotingCommand.createRequestCommand(1, "1.0.4", null), 3000);
//		System.out.println(ret);
//	}
//	
//	
//}
