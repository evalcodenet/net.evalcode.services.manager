package net.evalcode.services.manager.service.cache;


import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.inject.Singleton;
import net.evalcode.services.manager.component.annotation.Bind;
import net.evalcode.services.manager.component.annotation.Component;
import net.evalcode.services.manager.component.annotation.Property;
import net.evalcode.services.manager.component.annotation.Unbind;
import net.evalcode.services.manager.internal.ComponentBundleManagerModule;
import net.evalcode.services.manager.service.cache.impl.ehcache.internal.EhcacheCacheManagerFactory;
import net.evalcode.services.manager.service.cache.spi.Cache;
import net.evalcode.services.manager.service.cache.spi.CacheService;
import net.evalcode.services.manager.service.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * CacheServiceRegistry
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
@Component(module=ComponentBundleManagerModule.class, properties={
  @Property(name="net.evalcode.services.cache.config", defaultValue="cache.xml")
})
public class CacheServiceRegistry
{
  // PREDEFINED PROPERTIES
  static final Logger LOG=LoggerFactory.getLogger(CacheServiceRegistry.class);
  static final CacheServiceRegistry INSTANCE=new CacheServiceRegistry();


  // MEMBERS
  final Queue<CacheService<?>> cacheServices=new ConcurrentLinkedQueue<>();


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
    LOG.debug("Binding cache service: {}", cacheService);

    cacheServices.offer(cacheService);
  }

  @Log
  @Unbind
  public void unbind(final CacheService<?> cacheService)
  {
    LOG.debug("Unbinding cache service: {}", cacheService);

    cacheServices.remove(cacheService);
  }


  @Log
  public Cache<?> cacheForRegion(final String regionName)
  {
    return cacheServiceForRegion(regionName).cache(regionName);
  }

  @Log
  public Cache<?> cacheForRegion(final String regionName, final String defaultConfig)
  {
    return cacheServiceForRegion(regionName).cache(regionName, defaultConfig);
  }


  @Log
  public CacheService<?> cacheServiceForRegion(final String regionName)
  {
    if(regionName.isEmpty())
      return cacheServices.peek();

    /**
     * TODO Count votes for responsibility.
     *
     * for(final CacheService<?> cacheService : cacheServices)
     *   cacheService.vote(regionName)
     */

    return cacheServices.peek();
  }
}
