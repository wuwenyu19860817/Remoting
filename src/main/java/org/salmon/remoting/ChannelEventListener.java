package org.salmon.remoting;

import io.netty.channel.Channel;

/**
 * channel事件监听器
 *
 */
public interface ChannelEventListener {
	void onChannelConnect(final String remoteAddr, final Channel channel);

	void onChannelClose(final String remoteAddr, final Channel channel);

	void onChannelException(final String remoteAddr, final Channel channel);

	void onChannelIdle(final String remoteAddr, final Channel channel);
}
