package org.salmon.remoting.wrap;

/**
 * 回调接口
 * 
 * @author wuwenyu
 *
 */
public interface Callback {
	public void operationComplete(final byte[] response);

	public void exception(final Throwable ex);
}
