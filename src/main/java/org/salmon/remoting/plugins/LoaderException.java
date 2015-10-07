package org.salmon.remoting.plugins;

/**
 * 插件加载异常
 * @author wuwenyu
 *
 */
public class LoaderException extends Exception {
    private static final long serialVersionUID = -5690687334570505110L;


    public LoaderException(String message) {
        super(message);
    }


    public LoaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
