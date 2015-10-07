package org.salmon.remoting;

import org.salmon.remoting.protocol.RemotingCommand;

/**
 * 钩子过程 请求执行前后拦截
 */
public interface RPCHook {
	public void doBeforeRequest(final String remoteAddr, final RemotingCommand request);

	public void doAfterResponse(final String remoteAddr, final RemotingCommand request, final RemotingCommand response);
}
