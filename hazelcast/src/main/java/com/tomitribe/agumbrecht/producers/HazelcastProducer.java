/**
 * Tomitribe Confidential
 * <p/>
 * Copyright(c) Tomitribe Corporation. 2014
 * <p/>
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 * <p/>
 */
package com.tomitribe.agumbrecht.producers;

import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.tomitribe.agumbrecht.interceptor.CacheInterceptor;
import com.tomitribe.agumbrecht.qualifiers.Hazelcast;
import com.tomitribe.agumbrecht.qualifiers.LocalCacheProvider;
import com.tomitribe.agumbrecht.qualifiers.ObjectCache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class HazelcastProducer {

    private static final String CACHE_NAME = System.getProperty("jcache.cache.name", "object_cache");
    public static final int DURATION = Integer.parseInt(System.getProperty("jcache.cache.duration", "60"));

    @Produces
    @Singleton
    @Hazelcast
    public HazelcastInstance createHazelcastInstance() {

        final String configFile = "META-INF/hazelcast.xml";
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final URL location = loader.getResource(configFile);
        final Config config = new Config();

        config.setConfigurationUrl(location);
        config.setInstanceName("ExampleInstance");

        return com.hazelcast.core.Hazelcast.newHazelcastInstance(config);
    }

    @Produces
    @Singleton
    @LocalCacheProvider
    public CacheManager createCacheManager(@Hazelcast final HazelcastInstance instance) {
        return HazelcastServerCachingProvider.createCachingProvider(instance).getCacheManager();
    }


    @Produces
    @Singleton
    @ObjectCache
    public Cache<String, CacheInterceptor.ObjectWrapper> createUserCache(@LocalCacheProvider final CacheManager cacheManager) {

        final MutableConfiguration<String, CacheInterceptor.ObjectWrapper> config = new MutableConfiguration<String, CacheInterceptor.ObjectWrapper>();
        config.setStoreByValue(true)
                .setTypes(String.class, CacheInterceptor.ObjectWrapper.class)
                .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, DURATION)))
                .setStatisticsEnabled(false);

        return cacheManager.createCache(CACHE_NAME, config);
    }

    public void close(@Disposes @Hazelcast final HazelcastInstance instance) {
        instance.shutdown();
    }
}
