package net.evalcode.services.manager.service.cache.impl;


import javax.inject.Singleton;
import net.evalcode.services.manager.service.cache.annotation.Key;
import org.aopalliance.intercept.MethodInvocation;


/**
 * NoCacheKeyGenerator
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
public final class NoCacheKeyGenerator extends MethodCacheKeyGenerator
{
  // ACCESSORS/MUTATORS
  @Override
  public Object createKey(final Key key, final MethodInvocation methodInvocation)
  {
    final String value=key.value();

    if(value.isEmpty())
      return super.createKey(key, methodInvocation);

    if(Key.Type.PLAIN.equals(key.type()))
      return value;

    return Integer.valueOf(value.hashCode());
  }
}
