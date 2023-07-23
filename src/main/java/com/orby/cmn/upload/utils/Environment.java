package com.orby.cmn.upload.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * 环境基类
 *
 */
public class Environment {

	private static Logger _LOG;

	static {
		_LOG = LoggerFactory.getLogger(Environment.class);
	}
	private static Properties properties = null;


	//静态方法访问时，直接访问不需要实例化
	public static synchronized Properties getProperties(){//synchronized表示同时只能一个线程进行实例化
		if(properties == null){//如果两个进程同时进入时，同时创建很多实例，不符合单例
			try {
				properties = Util.readProperties("config/file-upload.properties");
				_LOG.info("file-upload.properties 初始化完毕:"+properties);
			} catch (Exception e) {
				_LOG.error("初始化加载配置文件异常：", e);
			}
		}
		return properties;
	}

}

