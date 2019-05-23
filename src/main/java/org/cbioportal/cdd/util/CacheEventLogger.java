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

package org.cbioportal.cdd.util;

import org.apache.log4j.Logger;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
/**
 *
 * @author ochoaa
 */
public class CacheEventLogger implements CacheEventListener<Object, Object> {

    private static Logger LOG = Logger.getLogger(CacheEventLogger.class);

    @Override
    public void onEvent(CacheEvent cacheEvent) {
        if (LOG.isInfoEnabled()) {
            LOG.info("CACHE_EVENT:\n" +
                     "\tTYPE: " + cacheEvent.getType() + "\n" +
                     "\tKEY: " + cacheEvent.getKey() + "\n" +
                     "\tVALUE: " + cacheEvent.getNewValue() + "\n" +
                     "CACHE_EVENT<>\n");
        }
    }

}
