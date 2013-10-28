package net.evalcode.services.manager.service.cache.ioc;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Provider;
import net.evalcode.services.manager.component.ComponentBundleInterface;
import net.evalcode.services.manager.service.cache.CacheServiceRegistry;
import net.evalcode.services.manager.service.cache.annotation.Cache;
import net.evalcode.services.manager.service.cache.annotation.CollectionBacklog;
import net.evalcode.services.manager.service.cache.annotation.Key;
import net.evalcode.services.manager.service.cache.annotation.Region;
import net.evalcode.services.manager.service.cache.impl.CollectionBacklogProvider;
import net.evalcode.services.manager.service.cache.spi.CacheKeyGenerator;
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
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Object invoke(final MethodInvocation methodInvocation) throws Throwable
  {
    if(null==cacheServiceRegistry)
      cacheServiceRegistry=providerInjector.get().getInstance(CacheServiceRegistry.class);

    final Method method=methodInvocation.getMethod();
    final Cache annotation=method.getAnnotation(Cache.class);
    final Region region=annotation.region();

    final String regionName=resolveRegionName(methodInvocation, region);
    final String defaultConfig=resolveDefaultConfig(methodInvocation, region);
    final String cacheKey=resolveCacheKey(methodInvocation, annotation.key());

    final net.evalcode.services.manager.service.cache.spi.Cache<?> cache=
      cacheServiceRegistry.cacheForRegion(regionName, defaultConfig);

    if(null==cache)
      return methodInvocation.proceed();

    final Object value=cache.get(cacheKey);

    /**
     * FIXME
     * - Fork & return futures.
     * - Resolve, merge & re-integrate values in async.
     */
    if(method.isAnnotationPresent(CollectionBacklog.class))
    {
      final CollectionBacklog collectionBacklog=method.getAnnotation(CollectionBacklog.class);
      final Class<? extends CollectionBacklogProvider> collectionBacklogProviderType=
        collectionBacklog.provider();
      final CollectionBacklogProvider collectionBacklogProvider=
        providerInjector.get().getInstance(collectionBacklogProviderType);

      final Class<? extends java.util.Collection> type=collectionBacklog.type();
      final java.util.Collection<Object> valueReturn=type.newInstance();

      if(value instanceof Map)
      {
        final Map<Integer, Object> mapReturn=collectionBacklogProvider
          .invoke(methodInvocation, (Map)value);

        cache.put(cacheKey, value);

        valueReturn.addAll(mapReturn.values());
      }
      else
      {
        final Map<Integer, Object> mapBacklog=new HashMap<>();
        final Map<Integer, Object> mapReturn=collectionBacklogProvider
          .invoke(methodInvocation, mapBacklog);

        // FIXME Async merge map & store synchronized
        cache.put(cacheKey, mapBacklog);

        valueReturn.addAll(mapReturn.values());
      }

      return valueReturn;
    }

    if(null==value)
      return cache.put(cacheKey, methodInvocation.proceed());

    return value;
  }


  // IMPLEMENTATION
  String resolveCacheKey(final MethodInvocation methodInvocation, final Key key)
  {
    final String value=key.value();

    if(!value.isEmpty())
      return value;

    if(Key.Type.HASHCODE.equals(key.type()))
      return String.valueOf(methodInvocation.getMethod().hashCode());

    if(Key.Type.GENERATOR.equals(key.type()))
    {
      final CacheKeyGenerator cacheKeyGenerator=providerInjector.get()
        .getInstance(key.generator());

      return cacheKeyGenerator.createKey(methodInvocation);
    }

    return methodInvocation.getMethod().toString();
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
