package net.evalcode.services.manager.service.cache.spi;


import net.evalcode.services.manager.component.annotation.Service;
import org.aopalliance.intercept.MethodInvocation;


/**
 * CacheKey
 *
 * @author evalcode.net
 */
@Service
public interface CacheKeyGenerator
{
  // ACCESSORS/MUTATORS
  String createKey(MethodInvocation methodInvocation);
}
