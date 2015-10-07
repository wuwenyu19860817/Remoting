package org.salmon.remoting.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.salmon.remoting.netty.NettyRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 插件类定义
 * 
 * @author wuwenyu
 *
 */
public class Plugin {
	private static final Logger log = LoggerFactory.getLogger(Plugin.class);

	/**
	 * 保证同一个插件只加载一次
	 */
	AtomicBoolean loadOnlyOnce = new AtomicBoolean(false);

	/**
	 * 插件目录
	 */
	private File pluginDir;

	/**
	 * 插件lib目录
	 */
	private File pluginLibDir;

	/**
	 * 插件resources目录
	 */
	private File pluginResourcesDir;

	/**
	 * 插件元数据描述文件
	 */
	private File pluginMenifestFile;

	/**
	 * 插件入口类必须实现org.salmon.remoting.netty.NettyRequestProcessor
	 */
	private String entryPoint;

	/**
	 * 对外服务编号
	 */
	private int sid;

	/**
	 * 对外服务版本
	 */
	private String version;

	/**
	 * 线程池配置
	 */
	private boolean threadPoolEnable = false;
	private int threadPoolCorePoolSize = 50;
	private int threadPoolMaximumPoolSize = 50;
	private int threadPoolKeepAliveTimeInSecond = 60;
	private int threadPoolQueqeSize = 50;

	/**
	 * 插件执行线程池
	 */
	private ExecutorService executorService;

	/**
	 * 插件RPC处理器
	 */
	private NettyRequestProcessor processor;

	/**
	 * 当前插件类加载器
	 */
	private URLClassLoader classLoader;

	/**
	 * 入口class类对象
	 */
	private Class<?> entryClazz;

	/**
	 * 插件事件回调
	 */
	private CallBack pluginEventCallback;

	public Plugin(File pluginDir, CallBack pluginEventCallback) {
		this.pluginEventCallback = pluginEventCallback;
		this.pluginDir = pluginDir;
		this.pluginLibDir = new File(pluginDir.getPath() + File.separator + "lib");
		this.pluginResourcesDir = new File(pluginDir.getPath() + File.separator + "resources");
		this.pluginMenifestFile = new File(pluginDir.getPath() + File.separator + "menifest.properties");
	}

	public Plugin load() throws LoaderException {
		if (loadOnlyOnce.compareAndSet(false, true)) {
			if (pluginEventCallback != null) {
				try {
					pluginEventCallback.beforeLoadPlugin(this);
				} catch (Throwable e) {
					log.error("插件[" + this.getPluginName() + "] " + "回调事件[beforeLoadPlugin] 发生异常.", e);
				}
			}

			checkDirExists();

			parseMenifestFile();

			createClassLoader();

			validateEntryPointAndSidVersion();

			createProcessor();

			if (pluginEventCallback != null) {
				try {
					pluginEventCallback.afterLoadPlugin(this);
				} catch (Throwable e) {  
					log.error("插件[" + this.getPluginName() + "] " + "回调事件[afterLoadPlugin] 发生异常.", e);
				}
			}
		}
		return this;
	}

	public String getPluginName() {
		return this.pluginDir.getName();
	}

	private void checkDirExists() throws LoaderException {
		if (!pluginDir.exists() || !pluginLibDir.exists() || !pluginResourcesDir.exists()
				|| !pluginMenifestFile.exists()) {
			throw new LoaderException("插件[" + this.getPluginName() + "] " + "pluginDir:" + pluginDir.getPath() + " \r\n pluginLibDir:"
					+ pluginLibDir.getPath() + " \r\n pluginResourcesDir:" + pluginResourcesDir.getPath()
					+ " \r\n pluginMenifestFile:" + pluginMenifestFile.getPath() + " 有不存在的目录或者文件.");
		}
		log.info("检测目录ok.");
	}

	private void parseMenifestFile() throws LoaderException {
		Properties p = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(pluginMenifestFile);
			p.load(fis);
			this.entryPoint = p.getProperty("Entrypoint") == null ? p.getProperty("Entrypoint")
					: p.getProperty("Entrypoint").trim();
			this.sid = Integer
					.parseInt(p.getProperty("Sid") == null ? p.getProperty("Sid") : p.getProperty("Sid").trim());
			this.version = p.getProperty("Version") == null ? p.getProperty("Version")
					: p.getProperty("Version").trim();
			String sidVersion = this.sid + "-" + this.version;
			try {
				this.threadPoolEnable = Boolean.parseBoolean(p.getProperty("Threadpool.enable") == null
						? p.getProperty("Threadpool.enable") : p.getProperty("Threadpool.enable").trim());
			} catch (Exception e) {
				this.threadPoolEnable = false;
				log.error("加载插件[" + sidVersion + "],解析menifest属性" + p.getProperty("Threadpool.enable") + "失败");
			}
			try {
				this.threadPoolCorePoolSize = Integer.parseInt(p.getProperty("Threadpool.corePoolSize") == null
						? p.getProperty("Threadpool.corePoolSize") : p.getProperty("Threadpool.corePoolSize").trim());
			} catch (Exception e) {
				this.threadPoolCorePoolSize = 50;
				log.error("加载插件[" + sidVersion + "],解析menifest属性" + p.getProperty("Threadpool.corePoolSize") + "失败");
			}

			try {
				this.threadPoolMaximumPoolSize = Integer.parseInt(p.getProperty("Threadpool.maximumPoolSize") == null
						? p.getProperty("Threadpool.maximumPoolSize")
						: p.getProperty("Threadpool.maximumPoolSize").trim());
			} catch (Exception e) {
				this.threadPoolMaximumPoolSize = 50;
				log.error("加载插件[" + sidVersion + "],解析menifest属性" + p.getProperty("Threadpool.maximumPoolSize") + "失败");
			}
			try {
				this.threadPoolKeepAliveTimeInSecond = Integer
						.parseInt(p.getProperty("Threadpool.keepAliveTimeInSecond") == null
								? p.getProperty("Threadpool.keepAliveTimeInSecond")
								: p.getProperty("Threadpool.keepAliveTimeInSecond").trim());
			} catch (Exception e) {
				this.threadPoolKeepAliveTimeInSecond = 60;
				log.error("加载插件[" + sidVersion + "],解析menifest属性" + p.getProperty("Threadpool.keepAliveTimeInSecond")
						+ "失败");
			}
			try {
				this.threadPoolQueqeSize = Integer.parseInt(p.getProperty("Threadpool.queqeSize") == null
						? p.getProperty("Threadpool.queqeSize") : p.getProperty("Threadpool.queqeSize").trim());
			} catch (Exception e) {
				this.threadPoolQueqeSize = 50;
				log.error("加载插件[" + sidVersion + "],解析menifest属性" + p.getProperty("Threadpool.queqeSize") + "失败");
			}

			log.info("解析插件配置文件ok " + this);

		} catch (Exception e) {
			throw new LoaderException("load pluginMenifestFile:" + pluginMenifestFile.getPath() + " fail.", e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ignore) {
				}
			}
		}

	}

	private void validateEntryPointAndSidVersion() throws LoaderException {
		String[] strArry = splitPluginDir();
		if (this.sid != Integer.parseInt(strArry[0]) || !this.version.equals(strArry[1])) {
			throw new LoaderException("目录sid-verson:" + this.pluginDir + "和menifest.properties读取到sid(" + this.sid
					+ ")和version(" + this.version + ")不一样.");
		}
		try {
			entryClazz = this.classLoader.loadClass(this.entryPoint);
			if (!NettyRequestProcessor.class.isAssignableFrom(entryClazz)) {
				throw new LoaderException(
						"entryPoint(" + entryPoint + ") 没有实现接口org.salmon.remoting.netty.NettyRequestProcessor.");
			}
		} catch (Throwable e) {
			throw new LoaderException("加载entryPoint(" + entryPoint + ")失败", e);
		}

		log.info("校验加载类服务编号版本ok " + this);
	}

	private void createProcessor() throws LoaderException {
		try {
			this.processor = (NettyRequestProcessor) entryClazz.newInstance();
			if (this.threadPoolEnable) {
				this.executorService = new ThreadPoolExecutor(this.threadPoolCorePoolSize,
						this.threadPoolMaximumPoolSize, this.threadPoolKeepAliveTimeInSecond, TimeUnit.SECONDS,
						new LinkedBlockingQueue<Runnable>(this.threadPoolQueqeSize));
			} else {
				this.executorService = null;
			}

			log.info("创建业务线程池和业务处理器ok " + this);
		} catch (Exception e) {
			throw new LoaderException(e.getMessage(), e);
		}
	}

	private void createClassLoader() throws LoaderException {
		File[] jars = pluginLibDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if (name != null && name.endsWith(".jar")) {
					return true;
				}
				return false;
			}
		});
		List<URL> urls = new ArrayList<URL>();
		for (File f : jars) {
			if ((f.isFile()) && (f.canRead()) && (f.getName().endsWith(".jar"))) {
				try {
					urls.add(f.toURI().toURL());
				} catch (MalformedURLException e) {
					throw new LoaderException("加载jar:" + f.getAbsolutePath() + "失败," + e.getMessage(), e);
				}
			}
		}

		try {
			urls.add(this.pluginResourcesDir.toURI().toURL());
		} catch (MalformedURLException e) {
			throw new LoaderException("加载jar:" + pluginResourcesDir.getAbsolutePath() + "失败," + e.getMessage(), e);
		}

		classLoader = new URLClassLoader((URL[]) urls.toArray(new URL[urls.size()]), Loader.class.getClassLoader());

		log.info("创建类加载器ok " + this);
	}

	public void unload() {
		if (pluginEventCallback != null) {
			try {
				pluginEventCallback.beforeUnloadPlugin(this);
			} catch (Throwable e) {
				log.error("插件[" + this.getPluginName() + "] " + "回调事件[beforeUnloadPlugin] 发生异常.", e); 
			}
		}
		this.classLoader = null;
		this.processor = null;
		entryClazz = null;
		if (executorService != null) {
			executorService.shutdown();
		}
		if (pluginEventCallback != null) {
			try {
				pluginEventCallback.afterUnloadPlugin(this);
			} catch (Throwable e) {
				log.error("插件[" + this.getPluginName() + "] " + "回调事件[afterUnloadPlugin] 发生异常.", e);  
			}
		}
	}

	private String[] splitPluginDir() {
		return pluginDir.getName().split("-");
	}

	public NettyRequestProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(NettyRequestProcessor processor) {
		this.processor = processor;
	}

	public URLClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(URLClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public String getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}

	public boolean isThreadPoolEnable() {
		return threadPoolEnable;
	}

	public void setThreadPoolEnable(boolean threadPoolEnable) {
		this.threadPoolEnable = threadPoolEnable;
	}

	public int getThreadPoolCorePoolSize() {
		return threadPoolCorePoolSize;
	}

	public void setThreadPoolCorePoolSize(int threadPoolCorePoolSize) {
		this.threadPoolCorePoolSize = threadPoolCorePoolSize;
	}

	public int getThreadPoolMaximumPoolSize() {
		return threadPoolMaximumPoolSize;
	}

	public void setThreadPoolMaximumPoolSize(int threadPoolMaximumPoolSize) {
		this.threadPoolMaximumPoolSize = threadPoolMaximumPoolSize;
	}

	public int getThreadPoolKeepAliveTimeInSecond() {
		return threadPoolKeepAliveTimeInSecond;
	}

	public void setThreadPoolKeepAliveTimeInSecond(int threadPoolKeepAliveTimeInSecond) {
		this.threadPoolKeepAliveTimeInSecond = threadPoolKeepAliveTimeInSecond;
	}

	public int getThreadPoolQueqeSize() {
		return threadPoolQueqeSize;
	}

	public void setThreadPoolQueqeSize(int threadPoolQueqeSize) {
		this.threadPoolQueqeSize = threadPoolQueqeSize;
	}

	public File getPluginDir() {
		return pluginDir;
	}

	public void setPluginDir(File pluginDir) {
		this.pluginDir = pluginDir;
	}

	@Override
	public String toString() {
		return "插件名[" + this.getPluginName() + "] [pluginDir=" + pluginDir + ", pluginLibDir=" + pluginLibDir
				+ ", pluginResourcesDir=" + pluginResourcesDir + ", pluginMenifestFile=" + pluginMenifestFile
				+ ", entryPoint=" + entryPoint + ", sid=" + sid + ", version=" + version + ", threadPoolEnable="
				+ threadPoolEnable + ", threadPoolCorePoolSize=" + threadPoolCorePoolSize
				+ ", threadPoolMaximumPoolSize=" + threadPoolMaximumPoolSize + ", threadPoolKeepAliveTimeInSecond="
				+ threadPoolKeepAliveTimeInSecond + ", threadPoolQueqeSize=" + threadPoolQueqeSize
				+ ", executorService=" + executorService + ", processor=" + processor + ", classLoader=" + classLoader
				+ ", entryClazz=" + entryClazz + "]";
	}
}
