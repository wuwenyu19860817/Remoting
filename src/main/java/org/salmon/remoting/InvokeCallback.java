package org.salmon.remoting;

import org.salmon.remoting.netty.ResponseFuture;

/**
 * 异步回调接口
 *
 */
public interface InvokeCallback {
	public void operationComplete(final ResponseFuture responseFuture);
}
