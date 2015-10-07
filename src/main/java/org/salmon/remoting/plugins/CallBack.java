package org.salmon.remoting.plugins;

/**
 * 插件事件回调 
 * @author wuwenyu
 *
 */
public interface CallBack {
	/**
	 * 插件加载前回调
	 * @param plugin
	 */
	public void beforeLoadPlugin(Plugin plugin);
	
	/**
	 * 插件加载后回调
	 * @param plugin
	 */
	public void afterLoadPlugin(Plugin plugin);
	
	/**
	 * 插件卸载前回调
	 * @param plugin
	 */
	public void beforeUnloadPlugin(Plugin plugin);
	
	/**
	 * 插件卸载后回调
	 * @param plugin
	 */
	public void afterUnloadPlugin(Plugin plugin);
}
