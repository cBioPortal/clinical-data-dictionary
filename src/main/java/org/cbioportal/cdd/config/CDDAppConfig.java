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
import java.io.IOException;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.jcache.JCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 *
 * @author ochoaa
 */
@Configuration
@EnableCaching
public class CDDAppConfig {

    @Bean
    public JCacheCacheManager cacheManager() throws IOException {
        JCacheCacheManager cacheManager = new JCacheCacheManager();
        cacheManager.setCacheManager(ehCacheCacheManager().getObject());
        return cacheManager;
    }
    @Bean
    public JCacheManagerFactoryBean ehCacheCacheManager() throws IOException {
        JCacheManagerFactoryBean factory = new JCacheManagerFactoryBean();
        factory.setCacheManagerUri(new File("/Users/ochoaa/cbio-projects/clinical-data-dictionary/src/main/resources/ehcache.xml").toURI());
        return factory;
    }
}
