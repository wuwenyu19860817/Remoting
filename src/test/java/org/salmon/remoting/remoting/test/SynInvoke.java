package org.salmon.remoting.remoting.test;

import java.util.concurrent.Executors;

import org.junit.Test;
import org.salmon.remoting.RemotingServer;
import org.salmon.remoting.netty.NettyRequestProcessor;
import org.salmon.remoting.protocol.RemotingCommand;
import org.salmon.remoting.protocol.RemotingCommand.RemotingSysResponseCode;
import org.salmon.remoting.protocol.RemotingSerializable;
import org.salmon.remoting.remoting.function.Main;
import org.salmon.remoting.wrap.BootStrapClient;

import io.netty.channel.ChannelHandlerContext;
import junit.framework.Assert;

public class SynInvoke {
	@Test
	public void testSynInvoke() throws Exception {
		// 方式一 注册RPC处理器(插件加载) 单个服务多版本
		final RemotingServer remotingServer = Main.createRemotingServer("classpath:loader");
		final BootStrapClient remotingClient = Main.createRemotingClient(); 

		// 方式二 注册RPC处理器(代码加载) 单个服务单个版本 版本固定为(0.0.0)
		remotingServer.registerProcessor(0, new NettyRequestProcessor() { 
			@Override
			public RemotingCommand processRequest(ChannelHandlerContext ctx, RemotingCommand request) throws Exception {
				RemotingCommand res = RemotingCommand.createResponseCommand(RemotingSysResponseCode.SUCCESS, "ok");
				res.setBody(
						RemotingSerializable.encode(RemotingSerializable.decode(request.getBody(), Response.class)));
				return res;
			}
		}, Executors.newCachedThreadPool());

		remotingServer.start();
		remotingClient.start();

		Request req = new Request();
		req.setSid("sidasdf");
		req.setBody("bodyasdf");
		try { 
			System.out.println(RemotingSerializable.decode(
					remotingClient.invokeSync("127.0.0.1:8888", -1, "0.0.0", RemotingSerializable.encode(req)),
					Response.class));
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(e.getMessage(), "request code is not supported.");
		}
		req.setSid("0.0.0");
		req.setBody("0-0.0.0");
		
		Response res = RemotingSerializable.decode(
				remotingClient.invokeSync("127.0.0.1:8888", 0, "0.0.0", RemotingSerializable.encode(req)),
				Response.class); 
		Assert.assertEquals("resId", res.getResId());
		Assert.assertEquals("0-0.0.0", res.getBody());
		
		req.setSid("1.0.0");
		req.setBody("1-1.0.0");
		res = (RemotingSerializable.decode(
				remotingClient.invokeSync("127.0.0.1:8888", 1, "1.0.0", RemotingSerializable.encode(req)),
				Response.class));
		Assert.assertEquals("1.0.0", res.getResId());
		Assert.assertEquals("1-1.0.0@1-1.0.0", res.getBody());
		
		req.setSid("1.0.1");
		req.setBody("1-1.0.1");
		res = (RemotingSerializable.decode(
				remotingClient.invokeSync("127.0.0.1:8888", 1, "1.0.1", RemotingSerializable.encode(req)),
				Response.class));
		Assert.assertEquals("1.0.1", res.getResId());
		Assert.assertEquals("1-1.0.1@1-1.0.1", res.getBody());
		
		req.setSid("1.0.2");
		req.setBody("1-1.0.2");
		res = (RemotingSerializable.decode(
				remotingClient.invokeSync("127.0.0.1:8888", 1, "1.0.2", RemotingSerializable.encode(req)),
				Response.class));
		Assert.assertEquals("1.0.2", res.getResId());
		Assert.assertEquals("1-1.0.2@1-1.0.2", res.getBody());
		
		req.setSid("1.0.3");
		req.setBody("1-1.0.3");
		res = (RemotingSerializable.decode(
				remotingClient.invokeSync("127.0.0.1:8888", 1, "1.0.3", RemotingSerializable.encode(req)),
				Response.class));
		Assert.assertEquals("1.0.3", res.getResId());
		Assert.assertEquals("1-1.0.3@1-1.0.3", res.getBody());
		
		req.setSid("1.0.4");
		req.setBody("1-1.0.4");
		res = (RemotingSerializable.decode(
				remotingClient.invokeSync("127.0.0.1:8888", 1, "1.0.4", RemotingSerializable.encode(req)),
				Response.class));
		Assert.assertEquals("1.0.4", res.getResId());
		Assert.assertEquals("1-1.0.4@1-1.0.4", res.getBody());
		
		req.setSid("1.0.5");
		req.setBody("1-1.0.5");
		res = (RemotingSerializable.decode(
				remotingClient.invokeSync("127.0.0.1:8888", 1, "1.0.5", RemotingSerializable.encode(req)),
				Response.class));
		Assert.assertEquals("1.0.5", res.getResId());
		Assert.assertEquals("1-1.0.5@1-1.0.5", res.getBody());
		
		try{
			res = (RemotingSerializable.decode(
					remotingClient.invokeSync("127.0.0.1:8888", 1, "1.0.6", RemotingSerializable.encode(req)),
					Response.class));
			Assert.fail();
		} catch (Exception e) {
			Assert.assertEquals(e.getMessage(), "request code is not supported.");
		}
		

	}
}
