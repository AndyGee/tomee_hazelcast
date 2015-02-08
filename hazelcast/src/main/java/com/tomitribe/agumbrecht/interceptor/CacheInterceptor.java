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

import com.tomitribe.agumbrecht.qualifiers.ObjectCache;

import javax.cache.Cache;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.Arrays;

public class CacheInterceptor {

    @Inject
    @ObjectCache
    private Cache<String, ObjectWrapper> cache;

    @AroundInvoke
    public Object cache(final InvocationContext ctx) throws Exception {

        final String name = ctx.getTarget().getClass().getName();
        final Method method = ctx.getMethod();
        final Object[] parameters = ctx.getParameters();

        final String s = Arrays.toString(parameters);

        final String key = name + ":" + method.getName() + ":" + s;

        ObjectWrapper o = cache.get(key);

        if (null == o) {
            o = new ObjectWrapper(ctx.proceed());
            cache.put(key, o);
        }

        return o.getObject();
    }

    public static class ObjectWrapper {

        private final Object object;

        public ObjectWrapper(final Object object) {
            this.object = object;
        }

        public Object getObject() {
            return object;
        }
    }
}
