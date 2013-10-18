package net.evalcode.services.manager.service.cache.ehcache;


import net.evalcode.services.manager.service.cache.ehcache.internal.EhcacheCacheManagerFactory;
import net.evalcode.services.manager.service.cache.spi.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;


/**
 * EhcacheCache
 *
 * @author carsten.schipke@gmail.com
 */
public class EhcacheCache implements Cache<Ehcache>
{
  // PREDEFINED PROPERTIES
  static final String NAME_DEFAULT="default";


  // MEMBERS
  final String name;
  volatile Ehcache impl=null;


  // CONSTRUCTION
  public EhcacheCache()
  {
    this(NAME_DEFAULT);
  }

  public EhcacheCache(final String name)
  {
    this.name=name;
  }

  public EhcacheCache(final String name, final Ehcache impl)
  {
    this.name=name;
    this.impl=impl;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public boolean contains(final Object key)
  {
    final Element value=getImpl().get(key);

    return null!=value;
  }

  @Override
  public Object get(final Object key)
  {
    final Element value=getImpl().get(key);

    if(null==value)
      return null;

    return value.getObjectValue();
  }

  @Override
  public Object put(final Object key, final Object value)
  {
    final Element element=new Element(key, value);

    getImpl().put(element);

    return value;
  }

  @Override
  public void remove(final Object key)
  {
    getImpl().remove(key);
  }

  @Override
  public void removeAll()
  {
    getImpl().removeAll();
  }

  @Override
  public Ehcache getImpl()
  {
    if(null==impl)
    {
      final CacheManager cacheManager=EhcacheCacheManagerFactory.get().getCacheManager();

      Ehcache cacheImpl=cacheManager.getCache(name);

      if(null==cacheImpl)
      {
        cacheManager.addCache(name);
        cacheImpl=cacheManager.getCache(name);
      }

      synchronized(this)
      {
        if(null==impl)
          impl=cacheImpl;

        return impl;
      }
    }

    return impl;
  }
}
