package net.evalcode.services.manager.service.cache.impl.ehcache;


import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Singleton;
import net.evalcode.services.manager.component.annotation.Component;
import net.evalcode.services.manager.internal.ComponentBundleManagerModule;
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
        final Cache<Ehcache> cacheExisting=caches.putIfAbsent(regionName, cacheNew);

        if(null==cacheExisting)
          return cacheNew;

        return cacheExisting;
      }

      return cacheCreate(regionName, defaultConfig);
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

  @Override
  public Cache<Ehcache> cacheCreate(final String regionName, final String defaultConfig)
  {
    final net.sf.ehcache.Cache defaultCache=cacheManager.getCache(defaultConfig);

    if(null==defaultCache)
    {
      throw new IllegalArgumentException(String.format(
        "Unable to resolve default cache configuration for region [region: %s, default: %s].",
          regionName, defaultConfig
      ));
    }

    final CacheConfiguration configuration=defaultCache.getCacheConfiguration();
    configuration.setName(regionName);

    final net.sf.ehcache.Cache ehcache=new net.sf.ehcache.Cache(configuration);
    final Ehcache ehcacheConcrete=cacheManager.addCacheIfAbsent(ehcache);

    final Cache<Ehcache> cacheNew=new EhcacheCache(ehcacheConcrete);
    final Cache<Ehcache> cacheExisting=caches.putIfAbsent(regionName, cacheNew);

    if(null==cacheExisting)
      return cacheNew;

    return cacheExisting;
  }
}
