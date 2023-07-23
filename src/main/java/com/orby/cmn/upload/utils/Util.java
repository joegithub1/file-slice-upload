package com.orby.cmn.upload.utils;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * description: 工具类
 *
 * @author: huangJian
 * @create: 2020-08-26
 */
public class Util {
    /**
    * description:  读取配置文件
    * @author HuangJian
    * @param jsonFileName
    * @return java.util.Properties
    * @create 2020-08-26
    */
    public static Properties readProperties(String jsonFileName) throws Exception{
        String path = Util.class.getClassLoader().getResource(jsonFileName).getPath();
        Properties properties = new Properties();
        properties.load(new InputStreamReader(new FileInputStream(path),"utf-8"));
        return properties;
    }

}
