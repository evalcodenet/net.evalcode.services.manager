package net.evalcode.services.manager.service.cache.impl.ehcache.internal;


import net.evalcode.services.manager.service.cache.spi.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;


/**
 * EhcacheCache
 *
 * @author carsten.schipke@gmail.com
 */
public class EhcacheCache implements Cache<Ehcache>
{
  // MEMBERS
  final Ehcache impl;


  // CONSTRUCTION
  public EhcacheCache(final Ehcache impl)
  {
    this.impl=impl;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public String getName()
  {
    return impl.getName();
  }

  @Override
  public boolean contains(final Object key)
  {
    final Element value=impl.get(key);

    return null!=value;
  }

  @Override
  public Object get(final Object key)
  {
    final Element value=impl.get(key);

    if(null==value)
      return null;

    return value.getObjectValue();
  }

  @Override
  public Object put(final Object key, final Object value)
  {
    final Element element=new Element(key, value);

    impl.put(element);

    return value;
  }

  @Override
  public void remove(final Object key)
  {
    impl.remove(key);
  }

  @Override
  public void removeAll()
  {
    impl.removeAll();
  }
}
