package net.evalcode.services.manager.service.cache.ioc;


import java.lang.reflect.Method;
import javax.inject.Provider;
import net.evalcode.services.manager.component.ComponentBundleInterface;
import net.evalcode.services.manager.service.cache.CacheServiceRegistry;
import net.evalcode.services.manager.service.cache.annotation.Cache;
import net.evalcode.services.manager.service.cache.annotation.CollectionBacklog;
import net.evalcode.services.manager.service.cache.annotation.Key;
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
  public Object invoke(final MethodInvocation methodInvocation) throws Throwable
  {
    if(null==cacheServiceRegistry)
      cacheServiceRegistry=providerInjector.get().getInstance(CacheServiceRegistry.class);

    final Method method=methodInvocation.getMethod();
    final Cache annotation=method.getAnnotation(Cache.class);
    final Region region=annotation.region();

    final String regionName=resolveRegionName(methodInvocation, region);
    final String defaultConfig=resolveDefaultConfig(methodInvocation, region);
    final Object cacheKey=resolveCacheKey(methodInvocation, annotation.key());

    final net.evalcode.services.manager.service.cache.spi.Cache<?> cache=
      cacheServiceRegistry.cacheForRegion(regionName, defaultConfig);

    if(null==cache)
      return methodInvocation.proceed();

    if(method.isAnnotationPresent(CollectionBacklog.class))
    {
      final CollectionBacklog backlog=method.getAnnotation(CollectionBacklog.class);
      final Class<? extends BacklogProvider> backlogProviderType=backlog.provider();
      final BacklogProvider backlogProvider=providerInjector.get()
        .getInstance(backlogProviderType);

      return backlogProvider.invoke(cache, cacheKey, methodInvocation);
    }

    // TODO Respect method arguments.
    final Object value=cache.get(cacheKey);

    if(null==value)
      return cache.put(cacheKey, methodInvocation.proceed());

    return value;
  }


  // IMPLEMENTATION
  Object resolveCacheKey(final MethodInvocation methodInvocation, final Key key)
  {
    final CacheKeyGenerator cacheKeyGenerator=providerInjector.get()
      .getInstance(key.generator());

    if(cacheKeyGenerator instanceof MethodCacheKeyGenerator)
      return ((MethodCacheKeyGenerator)cacheKeyGenerator).createKey(key, methodInvocation);

    return cacheKeyGenerator.createKey(key);
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
