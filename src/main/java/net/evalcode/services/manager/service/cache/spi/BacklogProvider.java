package net.evalcode.services.manager.service.cache.spi;


import java.util.Map;
import org.aopalliance.intercept.MethodInvocation;


/**
 * BacklogProvider
 *
 * @author evalcode.net
 */
public interface BacklogProvider<T>
{
  // ACCCESSORS
  Map<Integer, T> invoke(MethodInvocation methodInvocation, Map<Integer, T> backlog)
    throws Throwable;
}
