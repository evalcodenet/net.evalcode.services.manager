package net.evalcode.services.manager.cache;


import java.io.File;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import net.evalcode.services.manager.internal.util.SystemProperty;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
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
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CollectionRegion;
import org.hibernate.cache.EntityRegion;
import org.hibernate.cache.QueryResultsRegion;
import org.hibernate.cache.RegionFactory;
import org.hibernate.cache.TimestampsRegion;
import org.hibernate.cache.access.AccessType;
import org.hibernate.cfg.Settings;


/**
 * EhCacheFactory
 *
 * @author carsten.schipke@gmail.com
 */
public class EhCacheFactory implements RegionFactory
{
  // PREDEFINED PROPERTIES
  private static final AtomicInteger COUNT_REFERENCES=new AtomicInteger();
  private static final String NET_SF_EHCACHE_CONFIGURATION_RESOURCE_NAME=
    "net.sf.ehcache.configurationResourceName";


  // MEMBERS
  private final EhcacheAccessStrategyFactory accessStrategyFactory=
    new NonstopAccessStrategyFactory(new EhcacheAccessStrategyFactoryImpl());
  private final ProviderMBeanRegistrationHelper mbeanRegistrationHelper=
    new ProviderMBeanRegistrationHelper();

  @SuppressWarnings("unused")
  private final Properties properties;
  private Settings settings;
  private volatile CacheManager manager;


  // CONSTRUCTION
  public EhCacheFactory(final Properties properties)
  {
    super();

    this.properties=properties;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public void start(final Settings settings, final Properties properties)
  {
    try
    {
      String configurationResourceName=null;

      if(properties!=null)
      {
        configurationResourceName=(String)properties.get(
          NET_SF_EHCACHE_CONFIGURATION_RESOURCE_NAME
        );
      }

      if(null==configurationResourceName || 1>configurationResourceName.length())
      {
        manager=CacheManager.create();

        COUNT_REFERENCES.incrementAndGet();
      }
      else
      {
        final File configurationFile=loadResource(configurationResourceName);

        final Configuration configuration=ConfigurationFactory.parseConfiguration(
          configurationFile
        );

        manager=CacheManager.create(configuration);

        COUNT_REFERENCES.incrementAndGet();
      }

      mbeanRegistrationHelper.registerMBean(manager, properties);
    }
    catch(final net.sf.ehcache.CacheException e)
    {
      throw new CacheException(e);
    }
  }

  @Override
  public void stop()
  {
    try
    {
      if(null!=manager)
      {
        if(1>COUNT_REFERENCES.decrementAndGet())
          manager.shutdown();
      }
    }
    catch(final net.sf.ehcache.CacheException e)
    {
      throw new CacheException(e);
    }
  }

  @Override
  public EntityRegion buildEntityRegion(
    final String regionName, final Properties properties, final CacheDataDescription metadata)
  {
    return new EhcacheEntityRegion(
      accessStrategyFactory, getCache(regionName), settings, metadata, properties
    );
  }

  @Override
  public CollectionRegion buildCollectionRegion(
    final String regionName, final Properties properties, final CacheDataDescription metadata)
  {
    return new EhcacheCollectionRegion(
      accessStrategyFactory, getCache(regionName), settings, metadata, properties
    );
  }

  @Override
  public QueryResultsRegion buildQueryResultsRegion(
    final String regionName, final Properties properties)
  {
    return new EhcacheQueryResultsRegion(accessStrategyFactory, getCache(regionName), properties);
  }

  @Override
  public TimestampsRegion buildTimestampsRegion(
    final String regionName, final Properties properties)
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
  Ehcache getCache(final String name)
  {
    final Ehcache cache=manager.getEhcache(name);

    if(null==cache)
    {
      manager.addCache(name);

      return manager.getEhcache(name);
    }

    return cache;
  }

  File loadResource(final String configurationResourceName)
  {
    return SystemProperty.getConfigurationFile(configurationResourceName);
  }
}
