package org.salmon.remoting.remoting.function;

import org.salmon.remoting.RemotingServer;
import org.salmon.remoting.netty.NettyClientConfig;
import org.salmon.remoting.netty.NettyRemotingServer;
import org.salmon.remoting.netty.NettyServerConfig;
import org.salmon.remoting.wrap.BootStrapClient;

public class Main {
	public static BootStrapClient createRemotingClient() {
		NettyClientConfig config = new NettyClientConfig();
		BootStrapClient client = new BootStrapClient(config,500000);
		return client;
	}

	public static RemotingServer createRemotingServer(String scanPluginDir) throws InterruptedException {
		NettyServerConfig config = new NettyServerConfig();
		RemotingServer remotingServer = new NettyRemotingServer(config,scanPluginDir);
		return remotingServer;
	}
}
