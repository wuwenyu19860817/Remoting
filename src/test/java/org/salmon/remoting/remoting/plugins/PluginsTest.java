package org.salmon.remoting.remoting.plugins;
import java.io.IOException;

import org.junit.Test;
import org.salmon.remoting.plugins.CallBack;
import org.salmon.remoting.plugins.Loader;
import org.salmon.remoting.plugins.LoaderException;
import org.salmon.remoting.plugins.Plugin;
public class PluginsTest {
	@Test
	public void testPlugins(){
		Loader loader = null;
		try {
			loader = Loader.load("D:\\loader", new CallBack(){ 
				@Override
				public void beforeLoadPlugin(Plugin plugin) { 
					System.out.println("----------------------beforeLoadPlugin-----------------------"+plugin.getPluginName());
				}

				@Override
				public void afterLoadPlugin(Plugin plugin) {
					System.out.println("----------------------afterLoadPlugin-----------------------"+plugin.getPluginName());
				}

				@Override
				public void beforeUnloadPlugin(Plugin plugin) {
					System.out.println("----------------------beforeUnloadPlugin-----------------------"+plugin.getPluginName());			
				}

				@Override
				public void afterUnloadPlugin(Plugin plugin) {
					System.out.println("----------------------afterUnloadPlugin-----------------------"+plugin.getPluginName());
				} 
			});
		} catch (LoaderException e) { 
			e.printStackTrace();
		}finally{
//			if(loader!=null){
//				loader.unload();
//			}
		}
		
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
