package net.evalcode.services.manager.service.cache.ioc;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.inject.Provider;
import net.evalcode.services.manager.component.ComponentBundleInterface;
import net.evalcode.services.manager.service.cache.CacheServiceRegistry;
import net.evalcode.services.manager.service.cache.annotation.Cache;
import net.evalcode.services.manager.service.cache.annotation.CollectionBacklog;
import net.evalcode.services.manager.service.cache.annotation.Key;
import net.evalcode.services.manager.service.cache.annotation.KeySegment;
import net.evalcode.services.manager.service.cache.annotation.Region;
import net.evalcode.services.manager.service.cache.impl.MethodCacheKeyGenerator;
import net.evalcode.services.manager.service.cache.spi.BacklogProvider;
import net.evalcode.services.manager.service.cache.spi.internal.CacheKeyGenerator;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import com.google.inject.Injector;


/**
 * MethodInvocationCache
 *
 * @author carsten.schipke@gmail.com
 */
public class MethodInvocationCache implements MethodInterceptor
{
  // MEMBERS
  final Provider<Injector> providerInjector;
  volatile CacheServiceRegistry cacheServiceRegistry;


  // CONSTRUCTION
  public MethodInvocationCache(final Provider<Injector> providerInjector)
  {
    super();

    this.providerInjector=providerInjector;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public Object invoke(final MethodInvocation methodInvocation)
    throws Throwable
  {
    if(null==cacheServiceRegistry)
      cacheServiceRegistry=providerInjector.get().getInstance(CacheServiceRegistry.class);

    final Method method=methodInvocation.getMethod();

    if(method.isAnnotationPresent(CollectionBacklog.class))
      return invoke(methodInvocation, method.getAnnotation(CollectionBacklog.class));

    if(method.isAnnotationPresent(CollectionBacklog.Asynchronous.class))
      return invoke(methodInvocation, method.getAnnotation(CollectionBacklog.Asynchronous.class));

    return invoke(methodInvocation, method.getAnnotation(Cache.class));
  }


  // IMPLEMENTATION
  Object invoke(final MethodInvocation methodInvocation, final Cache annotation)
    throws Throwable
  {
    final Region region=annotation.region();

    final String regionName=resolveRegionName(methodInvocation, region);
    final String defaultConfig=resolveDefaultConfig(methodInvocation, region);
    final Object cacheKey=resolveCacheKey(methodInvocation, annotation.key());

    final net.evalcode.services.manager.service.cache.spi.Cache<?> cache=
      cacheServiceRegistry.cacheForRegion(regionName, defaultConfig);

    if(null==cache)
      return methodInvocation.proceed();

    // TODO Respect method arguments.
    final Object value=cache.get(cacheKey);

    if(null==value)
      return cache.put(cacheKey, methodInvocation.proceed());

    return value;
  }

  Object invoke(final MethodInvocation methodInvocation, final CollectionBacklog annotation)
    throws Throwable
  {
    return invoke(methodInvocation, annotation.provider());
  }

  Object invoke(final MethodInvocation methodInvocation,
    final CollectionBacklog.Asynchronous annotation)
      throws Throwable
  {
    return invoke(methodInvocation, annotation.provider());
  }

  Object invoke(final MethodInvocation methodInvocation,
    final Class<? extends BacklogProvider> typeBacklogProvider) throws Throwable
  {
    final BacklogProvider backlogProvider=providerInjector.get()
      .getInstance(typeBacklogProvider);

    return backlogProvider.invoke(methodInvocation);
  }

  Object resolveCacheKey(final MethodInvocation methodInvocation, final Key key)
  {
    final CacheKeyGenerator cacheKeyGenerator=providerInjector.get()
      .getInstance(key.generator());

    final StringBuilder cacheKey=new StringBuilder(32);

    if(cacheKeyGenerator instanceof MethodCacheKeyGenerator)
    {
      cacheKey.append(
        ((MethodCacheKeyGenerator)cacheKeyGenerator).createKey(key, methodInvocation)
      );
    }
    else
    {
      cacheKey.append(cacheKeyGenerator.createKey(key));
    }

    int idx=0;

    for(final Annotation[] annotations : methodInvocation.getMethod().getParameterAnnotations())
    {
      for(final Annotation annotation : annotations)
      {
        if(KeySegment.class.equals(annotation.annotationType()))
          cacheKey.append(methodInvocation.getArguments()[idx].hashCode());
      }

      idx++;
    }

    return cacheKey.toString();
  }

  String resolveRegionName(final MethodInvocation methodInvocation, final Region region)
  {
    final String value=region.value();

    if(value.isEmpty())
      return methodInvocation.getMethod().getDeclaringClass().getPackage().getName();

    return value;
  }

  String resolveDefaultConfig(final MethodInvocation methodInvocation, final Region region)
  {
    final String defaultConfig=region.defaultConfig();

    if(defaultConfig.isEmpty())
    {
      return providerInjector.get()
        .getInstance(ComponentBundleInterface.class)
        .getBundle()
        .getSymbolicName();
    }

    return defaultConfig;
  }
}
