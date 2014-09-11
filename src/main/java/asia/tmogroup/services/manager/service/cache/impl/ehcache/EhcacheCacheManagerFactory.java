package net.evalcode.services.manager.service.cache.impl.ehcache;


import java.net.MalformedURLException;
import java.net.URL;
import net.evalcode.services.manager.internal.util.SystemProperty;
import net.sf.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * EhcacheCacheManagerFactory
 *
 * @author carsten.schipke@gmail.com
 */
// FIXME Hide implementation.
public class EhcacheCacheManagerFactory
{
  // PREDEFINED PROPERTIES
  static final Logger LOG=LoggerFactory.getLogger(EhcacheCacheManagerFactory.class);
  /**
   * FIXME Inject component properties.
   */
  static final String CONFIG="net.evalcode.services.cache.config";
  /**
   * @internal
   */
  static final EhcacheCacheManagerFactory INSTANCE=new EhcacheCacheManagerFactory();


  // MEMBERS
  volatile CacheManager cacheManager=null;


  // STATIC ACCESSORS
  /**
   * @internal
   *
   * FIXME Refactor / decorated & exchangable (component service) caches/cache
   * (region) factories for persistence and component service caching.
   */
  public static final EhcacheCacheManagerFactory get()
  {
    return INSTANCE;
  }


  // ACCESSORS/MUTATORS
  public CacheManager getCacheManager()
  {
    if(null==cacheManager)
    {
      // FIXME Go through component configuration.
      final String configurationResourceName=SystemProperty.get(CONFIG);

      CacheManager newCacheManager;

      try
      {
        final URL configurationResource=SystemProperty
          .getConfigurationFilePath(configurationResourceName)
          .toUri()
          .toURL();

        newCacheManager=CacheManager.create(configurationResource);
      }
      catch(final MalformedURLException e)
      {
        LOG.error(e.getMessage(), e);

        newCacheManager=CacheManager.create();
      }

      synchronized(this)
      {
        if(null==cacheManager)
          cacheManager=newCacheManager;

        return cacheManager;
      }
    }

    return cacheManager;
  }
}
