package com.orby.cmn.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * description:
 *
 * @author: huangJian
 * @create: 2020-11-25
 */
public class TradeUploadStartApplication extends SpringBootServletInitializer {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        logger.info("文件系统启动成功：：：：：：：");
        return builder.sources(TradeUploadApplication.class);
    }
}
