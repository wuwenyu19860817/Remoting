package org.salmon.remoting.plugins;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实现定时插件根目录扫描 加载插件卸载插件
 * 
 * @author wuwenyu
 *
 */
public class Loader {
	private static final Logger log = LoggerFactory.getLogger(Loader.class);

	private ConcurrentMap<String, Plugin> loadedPlugins = new ConcurrentHashMap<String, Plugin>();

	private volatile static Loader loader = null;

	private final String rootDir;

	private final CallBack pluginEventCallback;

	private final ExecutorService scanExecutor = Executors.newFixedThreadPool(1, new ThreadFactory() {
		private AtomicInteger seq = new AtomicInteger(0);

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName("ScanPluginsThread[" + seq.getAndIncrement() + "]");
			return thread;
		}
	}); 

	private Loader(String rootDir, CallBack pluginEventCallback) throws LoaderException {
		String dir = rootDir;
		dir = dir.toLowerCase();
		if (dir != null && dir.startsWith("classpath:")) {
			dir = dir.replace("classpath:", "");
			this.rootDir = Loader.class.getClassLoader().getResource(".").getPath() + dir;
		} else {
			this.rootDir = dir;
		}
		this.pluginEventCallback = pluginEventCallback;
		log.info("开始从根目录:" + rootDir + "加载插件列表.");
		this.loadPluginsFromRootDir();
		this.startTimer();
	}

	private void startTimer() {
		scanExecutor.execute(new Runnable() {
			@SuppressWarnings("static-access")
			@Override
			public void run() {
				while (!scanExecutor.isShutdown()) {
					try {
						TimeUnit.SECONDS.sleep(5);
					} catch (InterruptedException i) {
					}

					try {
						Loader.this.loadPluginsFromRootDir();
					} catch (Throwable t) {
						Loader.this.log.error("ScanPluginsThread load plugins fail.", t);
					}
				}
			}
		});
	}

	public static Loader load(String rootDir, CallBack pluginEventCallback) throws LoaderException {
		if (rootDir == null) {
			throw new LoaderException("插件根目录为空.");
		}

		if (loader == null) {
			synchronized (Loader.class) {
				if (loader == null) {
					loader = new Loader(rootDir, pluginEventCallback);
				}
			}
		}
		return loader;
	}

	public void unload() {
		synchronized (Loader.class) {
			scanExecutor.shutdown();
			Iterator<String> iter = loadedPlugins.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				Plugin plugin = loadedPlugins.get(key);
				plugin.unload();
			}
		}
	}

	private synchronized void loadPluginsFromRootDir() throws LoaderException {
		// 获取插件根目录路径
		File rootDirF = new File(rootDir);
		if (!rootDirF.exists()) {
			throw new LoaderException("插件根目录:" + rootDir + "不存在.");
		}

		final Set<String> allPlugins = new HashSet<String>();

		// 扫描并新增插件
		// 列出根目录下面文件命名规则 code-version的目录 且没有加载过
		File[] pluginDirs = rootDirF.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				allPlugins.add(name);
				if (loadedPlugins.containsKey(name)) {
					return false;
				}
				boolean parsedOk = true;
				String[] codeVersionArr = null;
				try {
					codeVersionArr = name.split("-");
				} catch (Exception ignore) {
				}

				if (codeVersionArr == null || codeVersionArr.length != 2) {
					parsedOk = false;
				}

				if (parsedOk) {
					try {
						Integer.parseInt(codeVersionArr[0]);
					} catch (Exception e) {
						parsedOk = false;
					}
				}

				if (parsedOk) {
					return true;
				} else {
					allPlugins.remove(name);
					log.error("不能识别插件目录[" + name + "] 校验格式'sid-version'.");
					return false;
				}
			}
		});

		if (pluginDirs == null || pluginDirs.length == 0) {
			return;
		}

		for (File pluginDir : pluginDirs) {
			Plugin plugin = new Plugin(pluginDir, this.pluginEventCallback);
			try {
				log.info("开始加载插件:" + pluginDir.getName());
				plugin.load();
				loadedPlugins.putIfAbsent(plugin.getPluginName(), plugin);
				log.info("加载插件:" + pluginDir.getName() + "成功.");
			} catch (Exception e) {
				log.error("加载插件:" + pluginDir.getName() + "失败.", e);
				log.info("开始卸载插件:" + pluginDir.getName());
				plugin.unload();
				log.info("卸载插件:" + pluginDir.getName() + "成功.");
			}

		}

		Set<String> needUnLoadSet = new HashSet<String>();

		// 卸载多余的插件
		Iterator<String> iter = this.loadedPlugins.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			if (!allPlugins.contains(key)) {
				needUnLoadSet.add(key);
			}
		}

		for (String pluginName : needUnLoadSet) {
			Plugin plugin = loadedPlugins.remove(pluginName);
			plugin.unload();
		}
	}
}
