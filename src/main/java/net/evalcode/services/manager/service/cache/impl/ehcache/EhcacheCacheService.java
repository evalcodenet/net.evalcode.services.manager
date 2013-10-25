package net.evalcode.services.manager.service.cache.impl.ehcache;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Singleton;
import net.evalcode.services.manager.component.annotation.Component;
import net.evalcode.services.manager.internal.ComponentBundleManagerModule;
import net.evalcode.services.manager.service.cache.impl.ehcache.internal.EhcacheCache;
import net.evalcode.services.manager.service.cache.impl.ehcache.internal.EhcacheCacheManagerFactory;
import net.evalcode.services.manager.service.cache.spi.Cache;
import net.evalcode.services.manager.service.cache.spi.CacheService;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;


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
  final ConcurrentMap<String, Cache<Ehcache>> caches=new ConcurrentHashMap<>();
  final CacheManager cacheManager=EhcacheCacheManagerFactory.get().getCacheManager();


  // OVERRIDES/IMPLEMENTS
  @Override
  public Cache<Ehcache> cache(final String regionName)
  {
    return cache(regionName, CacheManager.DEFAULT_NAME);
  }

  // FIXME Thread safety ...
  @Override
  public Cache<Ehcache> cache(final String regionName, final String defaultConfig)
  {
    final Cache<Ehcache> cache=caches.get(regionName);

    if(null==cache)
    {
      if(cacheManager.cacheExists(regionName))
      {
        final Ehcache ehcache=cacheManager.getEhcache(regionName);

        final Cache<Ehcache> cacheNew=new EhcacheCache(ehcache);
        final Cache<Ehcache> cachePrev=caches.putIfAbsent(regionName, cacheNew);

        if(null==cachePrev)
          return cacheNew;

        return cachePrev;
      }

      CacheConfiguration cacheConfiguration=null;

      if(cacheManager.cacheExists(defaultConfig))
        cacheConfiguration=cacheManager.getCache(defaultConfig).getCacheConfiguration();
      else if(cacheManager.cacheExists(CacheManager.DEFAULT_NAME))
        cacheConfiguration=cacheManager.getConfiguration().getDefaultCacheConfiguration();

      cacheConfiguration.setName(regionName);

      final Ehcache ehcache=new net.sf.ehcache.Cache(cacheConfiguration);

      final Cache<Ehcache> cacheNew=new EhcacheCache(ehcache);
      final Cache<Ehcache> cachePrev=caches.putIfAbsent(regionName, cacheNew);

      if(null==cachePrev)
        return cacheNew;

      return cachePrev;
    }

    return cache;
  }

  @Override
  public boolean cacheExists(final String regionName)
  {
    return cacheManager.cacheExists(regionName);
  }

  @Override
  public int vote(final String regionName)
  {
    return 10;
  }
}
