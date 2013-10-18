package net.evalcode.services.manager.service.cache.ehcache.internal;


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
public class EhcacheCacheManagerFactory
{
  // PREDEFINED PROPERTIES
  static final Logger LOG=LoggerFactory.getLogger(EhcacheCacheManagerFactory.class);

  static final String NET_SF_EHCACHE_CONFIGURATION_RESOURCE_NAME=
    "net.sf.ehcache.configurationResourceName";
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
      final String configurationResourceName=System.getProperty(
        NET_SF_EHCACHE_CONFIGURATION_RESOURCE_NAME
      );

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
