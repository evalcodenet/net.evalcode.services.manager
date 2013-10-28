package net.evalcode.services.manager.service.cache.impl.ehcache;


import java.util.Properties;
import net.evalcode.services.manager.service.cache.impl.ehcache.internal.EhcacheCacheManagerFactory;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.hibernate.management.impl.ProviderMBeanRegistrationHelper;
import net.sf.ehcache.hibernate.nonstop.NonstopAccessStrategyFactory;
import net.sf.ehcache.hibernate.regions.EhcacheCollectionRegion;
import net.sf.ehcache.hibernate.regions.EhcacheEntityRegion;
import net.sf.ehcache.hibernate.regions.EhcacheQueryResultsRegion;
import net.sf.ehcache.hibernate.regions.EhcacheTimestampsRegion;
import net.sf.ehcache.hibernate.strategy.EhcacheAccessStrategyFactory;
import net.sf.ehcache.hibernate.strategy.EhcacheAccessStrategyFactoryImpl;
import net.sf.ehcache.util.Timestamper;
import org.hibernate.cache.CacheDataDescription;
import org.hibernate.cache.CollectionRegion;
import org.hibernate.cache.EntityRegion;
import org.hibernate.cache.QueryResultsRegion;
import org.hibernate.cache.RegionFactory;
import org.hibernate.cache.TimestampsRegion;
import org.hibernate.cache.access.AccessType;
import org.hibernate.cfg.Settings;


/**
 * EhcacheHibernateRegionCacheFactory
 *
 * @author carsten.schipke@gmail.com
 */
public class EhcacheHibernateRegionCacheFactory implements RegionFactory
{
  // MEMBERS
  private final EhcacheAccessStrategyFactory accessStrategyFactory=
    new NonstopAccessStrategyFactory(new EhcacheAccessStrategyFactoryImpl());
  private final ProviderMBeanRegistrationHelper mbeanRegistrationHelper=
    new ProviderMBeanRegistrationHelper();

  private volatile CacheManager cacheManager;
  private Settings settings;


  // CONSTRUCTION
  public EhcacheHibernateRegionCacheFactory(final Properties properties)
  {
    super();
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public void start(final Settings settings, final Properties properties)
  {
    cacheManager=EhcacheCacheManagerFactory.get().getCacheManager();
    mbeanRegistrationHelper.registerMBean(cacheManager, properties);
  }

  @Override
  public void stop()
  {
    // FIXME Global cache reference counting & lifecycle management.
    mbeanRegistrationHelper.unregisterMBean();
  }

  @Override
  public EntityRegion buildEntityRegion(final String regionName,
    final Properties properties, final CacheDataDescription metadata)
  {
    return new EhcacheEntityRegion(
      accessStrategyFactory, getCache(regionName), settings, metadata, properties
    );
  }

  @Override
  public CollectionRegion buildCollectionRegion(final String regionName,
    final Properties properties, final CacheDataDescription metadata)
  {
    return new EhcacheCollectionRegion(
      accessStrategyFactory, getCache(regionName), settings, metadata, properties
    );
  }

  @Override
  public QueryResultsRegion buildQueryResultsRegion(final String regionName,
    final Properties properties)
  {
    return new EhcacheQueryResultsRegion(accessStrategyFactory, getCache(regionName), properties);
  }

  @Override
  public TimestampsRegion buildTimestampsRegion(final String regionName,
    final Properties properties)
  {
    return new EhcacheTimestampsRegion(accessStrategyFactory, getCache(regionName), properties);
  }

  @Override
  public AccessType getDefaultAccessType()
  {
    return AccessType.READ_WRITE;
  }

  @Override
  public boolean isMinimalPutsEnabledByDefault()
  {
    return true;
  }

  @Override
  public long nextTimestamp()
  {
    return Timestamper.next();
  }


  // IMPLEMENTATION
  Cache getCache(final String name)
  {
    final Cache cache=cacheManager.getCache(name);

    if(null==cache)
    {
      cacheManager.addCache(name);

      return cacheManager.getCache(name);
    }

    return cache;
  }
}
