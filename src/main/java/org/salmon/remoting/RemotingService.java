package org.salmon.remoting;

/**
 * 统一接口
 *
 */
public interface RemotingService {
	public void start();

	public void shutdown();

	public void registerRPCHook(RPCHook rpcHook);
}
