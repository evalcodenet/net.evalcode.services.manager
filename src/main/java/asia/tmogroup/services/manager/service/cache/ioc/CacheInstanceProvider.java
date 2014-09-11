package net.evalcode.services.manager.service.cache.ioc;


import java.lang.reflect.Method;
import javax.inject.Provider;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import net.evalcode.services.manager.component.ComponentBundleInterface;
import net.evalcode.services.manager.service.cache.CacheServiceRegistry;
import net.evalcode.services.manager.service.cache.annotation.CacheInstance;
import net.evalcode.services.manager.service.cache.annotation.Region;
import com.google.inject.Injector;


/**
 * CacheInstanceProvider
 *
 * @author carsten.schipke@gmail.com
 */
public class CacheInstanceProvider implements MethodInterceptor
{
  // MEMBERS
  final Provider<Injector> providerInjector;
  volatile CacheServiceRegistry cacheServiceRegistry;


  // CONSTRUCTION
  public CacheInstanceProvider(final Provider<Injector> providerInjector)
  {
    super();

    this.providerInjector=providerInjector;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public Object invoke(final MethodInvocation methodInvocation)
    throws Throwable
  {
    if(null==cacheServiceRegistry)
      cacheServiceRegistry=providerInjector.get().getInstance(CacheServiceRegistry.class);

    final Method method=methodInvocation.getMethod();
    final Region region=method.getAnnotation(CacheInstance.class).region();

    final String regionName=resolveRegionName(methodInvocation, region);
    final String defaultConfig=resolveDefaultConfig(methodInvocation, region);

    return cacheServiceRegistry.cacheForRegion(regionName, defaultConfig);
  }


  // IMPLEMENTATION
  String resolveRegionName(final MethodInvocation methodInvocation, final Region region)
  {
    final String value=region.value();

    if(value.isEmpty())
      return methodInvocation.getMethod().getDeclaringClass().getPackage().getName();

    return value;
  }

  String resolveDefaultConfig(final MethodInvocation methodInvocation, final Region region)
  {
    final String defaultConfig=region.defaultConfig();

    if(defaultConfig.isEmpty())
    {
      return providerInjector.get()
        .getInstance(ComponentBundleInterface.class)
        .getBundle()
        .getSymbolicName();
    }

    return defaultConfig;
  }
}
