//package org.salmon.remoting.remoting.function;
//
//import java.util.concurrent.TimeUnit;
//
//import org.junit.Test;
//import org.salmon.remoting.CommandCustomHeader;
//import org.salmon.remoting.RemotingClient;
//import org.salmon.remoting.RemotingServer;
//import org.salmon.remoting.netty.NettyClientConfig;
//import org.salmon.remoting.netty.NettyRemotingClient;
//import org.salmon.remoting.netty.NettyRemotingServer;
//import org.salmon.remoting.netty.NettyRequestProcessor;
//import org.salmon.remoting.netty.NettyServerConfig;
//import org.salmon.remoting.protocol.RemotingCommand;
//import org.salmon.remoting.protocol.RemotingCommand.RemotingSysResponseCode;
//import org.salmon.remoting.protocol.RemotingSerializable;
//
//import io.netty.channel.ChannelHandlerContext;
//
///**
// * 功能测试
// */
//public class FunctionTest {
//	public static RemotingClient createRemotingClient() {
//		NettyClientConfig config = new NettyClientConfig();
//		RemotingClient client = new NettyRemotingClient(config);
//		return client;
//	}
//
//	public static RemotingServer createRemotingServer() throws InterruptedException {
//		NettyServerConfig config = new NettyServerConfig();
//		RemotingServer remotingServer = new NettyRemotingServer(config);
//		return remotingServer;
//	}
//
//	@Test
//	public void testFunction() throws Exception {
//		final RemotingServer remotingServer = createRemotingServer();
//		final RemotingClient remotingClient = createRemotingClient();
//		try {
//			remotingServer.registerDefaultProcessor(new NettyRequestProcessor() {
//				@Override
//				public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
//						throws Exception {
//					System.out.println("服务端默认RPC处理器  header=>" + request.getExtFields());
//					Student stu = RemotingSerializable.decode(request.getBody(), Student.class);
//					System.out.println("服务端默认RPC处理器   body=>" + stu);
//					return RemotingCommand.createResponseCommand(RemotingSysResponseCode.SUCCESS, "服务端默认RPC处理器，处理成功。");
//				}
//			}, null);
//
//			remotingServer.registerProcessor(0, new NettyRequestProcessor() {
//				@Override
//				public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
//						throws Exception {
//					System.out.println("服务端RPC处理器0 header=>" + request.getExtFields());
//					Student stu = RemotingSerializable.decode(request.getBody(), Student.class);
//					System.out.println("服务端RPC处理器0 body=>" + stu);
//					return RemotingCommand.createResponseCommand(RemotingSysResponseCode.SUCCESS, "服务端RPC处理器0，处理成功。");
//				}
//			}, null);
//
//			remotingServer.registerProcessor(1, new NettyRequestProcessor() {
//				@Override
//				public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
//						throws Exception {
//					System.out.println("服务端RPC处理器1 header=>" + request.getExtFields());
//					Student stu = RemotingSerializable.decode(request.getBody(), Student.class);
//					System.out.println("服务端RPC处理器1 body=>" + stu);
// 
//					RemotingCommand cmd = RemotingCommand.createRequestCommand(3, null);
//					cmd.setBody(request.getBody());
//					System.out.println("服务端RPC处理器1 call 客户端响应=>" + remotingServer.invokeSync(ctx.channel(), cmd, 3000));
//					return RemotingCommand.createResponseCommand(RemotingSysResponseCode.SUCCESS, "服务端RPC处理器1，处理成功。");
//				}
//			}, null);
//
//			remotingClient.registerProcessor(3, new NettyRequestProcessor() {
//				@Override
//				public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request)
//						throws Exception {
//					System.out.println("客户端RPC处理器3 header=>" + request.getExtFields());
//					Student stu = RemotingSerializable.decode(request.getBody(), Student.class);
//					System.out.println("客户端RPC处理器3 body=>" + stu);
//					return RemotingCommand.createResponseCommand(RemotingSysResponseCode.SUCCESS, "服务端RPC处理器1，处理成功。");
//				}
//			}, null);
//
//			remotingServer.start();
//			remotingClient.start();
//
//			// 测试同步调用
//			// MyCustomHeader header = new MyCustomHeader();
//			// header.setFirst("first");
//			// header.setSecond("second");
//			// RemotingCommand request = RemotingCommand.createRequestCommand(0,
//			// header);
//			//
//			// Student stu = new Student();
//			// stu.setAge(30);
//			// stu.setName("wuwenyu");
//			// request.setBody(RemotingSerializable.encode(stu));
//			// RemotingCommand response =
//			// remotingClient.invokeSync("localhost:8888", request, 600000);
//			// System.out.println(response);
//
//			// 测试异步调用
//			// MyCustomHeader header = new MyCustomHeader();
//			// header.setFirst("first");
//			// header.setSecond("second");
//			// RemotingCommand request = RemotingCommand.createRequestCommand(0,
//			// header);
//			//
//			// Student stu = new Student();
//			// stu.setAge(30);
//			// stu.setName("wuwenyu");
//			// request.setBody(RemotingSerializable.encode(stu));
//			// remotingClient.invokeAsync("localhost:8888", request, 3000, new
//			// InvokeCallback(){
//			// @Override
//			// public void operationComplete(ResponseFuture responseFuture) {
//			// System.out.println(responseFuture);
//			// }
//			// });
//
//			// 测试oneway
//			// MyCustomHeader header = new MyCustomHeader();
//			// header.setFirst("first");
//			// header.setSecond("second");
//			// RemotingCommand request = RemotingCommand.createRequestCommand(0,
//			// header);
//			//
//			// Student stu = new Student();
//			// stu.setAge(30);
//			// stu.setName("wuwenyu");
//			// request.setBody(RemotingSerializable.encode(stu));
//			// remotingClient.invokeOneway("localhost:8888", request, 600000);
//
//			// 服务端回调
//			Student stu = new Student();
//			stu.setAge(30);
//			stu.setName("wuwenyu");
//			RemotingCommand request = RemotingCommand.createRequestCommand(1, RemotingSerializable.encode(stu));
//
//			RemotingCommand response = remotingClient.invokeSync("localhost:8888", request, 600000);
//			System.out.println(response);
//			TimeUnit.SECONDS.sleep(3);
//		} finally {
//			remotingClient.shutdown();
//			remotingServer.shutdown();
//		}
//	}
//
//}
