package net.evalcode.services.manager.service.cache.ioc;


import javax.inject.Provider;
import net.evalcode.services.manager.service.cache.Cache;
import net.evalcode.services.manager.service.cache.Cache.Key;
import net.evalcode.services.manager.service.cache.Cache.Region;
import net.evalcode.services.manager.service.cache.CacheServiceRegistry;
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
  public Object invoke(final MethodInvocation methodInvocation) throws Throwable
  {
    if(null==cacheServiceRegistry)
      cacheServiceRegistry=providerInjector.get().getInstance(CacheServiceRegistry.class);

    final Cache annotation=methodInvocation.getMethod().getAnnotation(Cache.class);
    final Region region=annotation.region();

    final String regionName=resolveRegionName(methodInvocation, region);
    final String cacheKey=resolveCacheKey(methodInvocation, annotation.key());

    final net.evalcode.services.manager.service.cache.spi.Cache<?> cache=
      cacheServiceRegistry.cacheForRegion(regionName, region.defaultConfig());

    if(null==cache)
      return methodInvocation.proceed();

    final Object returnValue=cache.get(cacheKey);

    if(null==returnValue)
      return cache.put(cacheKey, methodInvocation.proceed());

    return returnValue;
  }


  // IMPLEMENTATION
  String resolveCacheKey(final MethodInvocation methodInvocation, final Key key)
  {
    if(Key.Type.VALUE.equals(key.type()))
      return key.value();

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
}
