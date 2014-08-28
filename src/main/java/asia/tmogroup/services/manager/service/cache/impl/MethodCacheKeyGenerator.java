package net.evalcode.services.manager.service.cache.impl;


import javax.inject.Singleton;
import net.evalcode.services.manager.service.cache.annotation.Key;
import net.evalcode.services.manager.service.cache.spi.internal.CacheKeyGenerator;
import org.aopalliance.intercept.MethodInvocation;


/**
 * MethodCacheKeyGenerator
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
public class MethodCacheKeyGenerator implements CacheKeyGenerator
{
  // ACCESSORS/MUTATORS
  public Object createKey(final Key key, final MethodInvocation methodInvocation)
  {
    if(Key.Type.PLAIN.equals(key.type()))
      return methodInvocation.getMethod().toGenericString();

    return Integer.valueOf(methodInvocation.hashCode());
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public Object createKey(final Key key)
  {
    throw new UnsupportedOperationException();
  }
}
