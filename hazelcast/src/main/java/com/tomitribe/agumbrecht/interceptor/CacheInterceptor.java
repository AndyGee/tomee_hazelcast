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
package com.tomitribe.agumbrecht.interceptor;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CacheInterceptor {

    private static final AtomicReference<Cache<String, Object>> cache = new AtomicReference<Cache<String, Object>>();

    static {
        final CachingProvider cachingProvider = Caching.getCachingProvider();

        final CacheManager cacheManager = cachingProvider.getCacheManager();

        final CompleteConfiguration<String, Object> config = new MutableConfiguration<String, Object>()
                .setTypes(String.class, Object.class);

        cache.set(cacheManager.createCache("example", config));
    }

    @AroundInvoke
    public Object cache(final InvocationContext ctx) throws Exception {

        final String name = ctx.getTarget().getClass().getName();
        final Method method = ctx.getMethod();
        final Object[] parameters = ctx.getParameters();

        final String s = Arrays.toString(parameters);

        final String key = name + ":" + method.getName() + ":" + s;

        Object o = cache.get().get(key);

        if (null == o) {
            o = ctx.proceed();
            cache.get().put(key, o);
        }

        return o;
    }
}
