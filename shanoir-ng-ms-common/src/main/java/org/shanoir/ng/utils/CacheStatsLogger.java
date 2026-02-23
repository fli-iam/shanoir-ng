/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */


package org.shanoir.ng.utils;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.stats.CacheStats;

@Component
@ConditionalOnProperty(
        name = "cache.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class CacheStatsLogger {

    private static final Logger LOG = LoggerFactory.getLogger(CacheStatsLogger.class);

    @Autowired
    private CacheManager cacheManager;

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void logCacheStats() {
        if (!(cacheManager instanceof CaffeineCacheManager caffeineCacheManager)) {
            return;
        }
        caffeineCacheManager.getCacheNames().forEach(cacheName -> {
            Cache springCache = cacheManager.getCache(cacheName);
            if (springCache instanceof CaffeineCache caffeineCache) {
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                CacheStats stats = nativeCache.stats();
                LOG.info(
                        "[Cache Stats] name='{}' | size={} | hitRate={}% | hits={} | misses={} | loads={} | evictions={} | avgLoadPenalty={}ms",
                        cacheName,
                        nativeCache.estimatedSize(),
                        String.format("%.2f", stats.hitRate() * 100),
                        stats.hitCount(),
                        stats.missCount(),
                        stats.loadCount(),
                        stats.evictionCount(),
                        String.format("%.2f", stats.averageLoadPenalty() / 1_000_000.0));
            }
        });
    }

}
