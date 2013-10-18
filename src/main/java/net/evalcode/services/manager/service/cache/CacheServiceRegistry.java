package net.evalcode.services.manager.service.cache;


import javax.inject.Singleton;
import net.evalcode.services.manager.component.annotation.Bind;
import net.evalcode.services.manager.component.annotation.Component;
import net.evalcode.services.manager.component.annotation.Unbind;
import net.evalcode.services.manager.internal.ComponentBundleManagerModule;
import net.evalcode.services.manager.service.cache.ehcache.internal.EhcacheCacheManagerFactory;
import net.evalcode.services.manager.service.cache.spi.CacheService;
import net.evalcode.services.manager.service.logging.Log;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * CacheServiceRegistry
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
@Component(module=ComponentBundleManagerModule.class)
public class CacheServiceRegistry
{
  // PREDEFINED PROPERTIES
  static final Logger LOG=LoggerFactory.getLogger(CacheServiceRegistry.class);
  static final CacheServiceRegistry INSTANCE=new CacheServiceRegistry();


  // MEMBERS
  final ConcurrentHashSet<CacheService<?>> cacheServices=new ConcurrentHashSet<>();


  // CONSTRUCTION
  CacheServiceRegistry()
  {
    super();
  }


  // STATIC ACCESSORS
  /**
   * @internal
   *
   * FIXME Refactor / decorated & exchangable (component service) caches/cache
   * (region) factories for persistence and component service caching.
   *
   * @see EhcacheCacheManagerFactory
   */
  public static final CacheServiceRegistry get()
  {
    return INSTANCE;
  }


  // ACCESSORS/MUTATORS
  @Log
  @Bind
  public void bind(final CacheService<?> cacheService)
  {
    LOG.info("Binding cache service: {}", cacheService);
    cacheServices.add(cacheService);
  }

  @Log
  @Unbind
  public void unbind(final CacheService<?> cacheService)
  {
    LOG.info("Unbinding cache service: {}", cacheService);
    cacheServices.remove(cacheService);
  }

  @Log
  public CacheService<?> lookup(final String name)
  {
    for(final CacheService<?> cacheService : cacheServices)
    {
      if(cacheService.hasCache(name))
        return cacheService;
    }

    return null;
  }
}
