package net.evalcode.services.manager.service.cache.ioc;


import javax.inject.Provider;
import javax.inject.Singleton;
import net.evalcode.services.manager.service.cache.Cache;
import net.evalcode.services.manager.service.cache.CacheServiceRegistry;
import net.evalcode.services.manager.service.cache.spi.CacheService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import com.google.inject.Injector;


/**
 * MethodInvocationCache
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
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

    final Cache annotationCache=methodInvocation.getMethod().getAnnotation(Cache.class);
    final CacheService<?> cacheService=cacheServiceRegistry.lookup(annotationCache.region());
    final net.evalcode.services.manager.service.cache.spi.Cache<?> cache=
      cacheService.getCache(annotationCache.region());

    String key=annotationCache.key();

    if(key.isEmpty())
      key=String.valueOf(methodInvocation.getMethod().getName().hashCode());

    final Object cachedReturnValue=cache.get(key);

    if(null==cachedReturnValue)
      return cache.put(key, methodInvocation.proceed());

    return cachedReturnValue;
  }
}
