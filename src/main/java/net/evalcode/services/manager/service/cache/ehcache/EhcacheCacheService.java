package net.evalcode.services.manager.service.cache.ehcache;


import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Singleton;
import net.evalcode.services.manager.component.annotation.Component;
import net.evalcode.services.manager.internal.ComponentBundleManagerModule;
import net.evalcode.services.manager.service.cache.ehcache.internal.EhcacheCacheManagerFactory;
import net.evalcode.services.manager.service.cache.spi.Cache;
import net.evalcode.services.manager.service.cache.spi.CacheService;
import net.sf.ehcache.Ehcache;


/**
 * EhcacheCacheService
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
@Component(module=ComponentBundleManagerModule.class)
public class EhcacheCacheService implements CacheService<Ehcache>
{
  // MEMBERS
  final ConcurrentHashMap<String, Cache<Ehcache>> caches=new ConcurrentHashMap<>();


  // OVERRIDES/IMPLEMENTS
  @Override
  public boolean hasCache(final String name)
  {
    return EhcacheCacheManagerFactory.get().getCacheManager().cacheExists(name);
  }

  @Override
  public Cache<Ehcache> getCache(final String name)
  {
    final Cache<Ehcache> cache=caches.get(name);

    if(null==cache)
    {
      final Cache<Ehcache> cacheNew=new EhcacheCache(name);
      final Cache<Ehcache> cachePrevious=caches.putIfAbsent(name, cacheNew);

      if(null==cachePrevious)
        return cacheNew;

      return cachePrevious;
    }

    return cache;
  }
}
