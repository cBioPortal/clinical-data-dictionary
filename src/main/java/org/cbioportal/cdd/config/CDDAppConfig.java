/*
 * Copyright (c) 2019 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
*/

package org.cbioportal.cdd.config;

import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.jcache.JCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

/**
 *
 * @author ochoaa
 */
@Configuration
@EnableCaching
public class CDDAppConfig {

    @Value("${ehcache.persistence.path}")
    private String ehcachePersistencePath;

    @Bean
    public JCacheManagerFactoryBean jCacheManagerFactory() throws Exception {
        File file = ResourceUtils.getFile("classpath:ehcache.xml");
        JCacheManagerFactoryBean factory = new JCacheManagerFactoryBean();
        factory.setCacheManagerUri(file.getAbsoluteFile().toURI());
        return factory;
    }

    @Bean
    public JCacheCacheManager jCacheCacheManager() throws Exception {
        JCacheCacheManager jCacheCacheManager = new JCacheCacheManager();
        jCacheCacheManager.setCacheManager(jCacheManagerFactory().getObject());
        return jCacheCacheManager;
    }
}
