package net.evalcode.services.manager.internal;


import org.osgi.framework.Bundle;
import com.google.inject.Injector;
import com.google.inject.Module;


/**
 * ManagerBundleInstance
 *
 * @author carsten.schipke@gmail.com
 */
public class ManagerBundleInstance extends ComponentBundleInstance
{
  // MEMBERS
  final Module module=new ComponentBundleManagerModule();
  final Injector injector;


  // CONSTRUCTION
  ManagerBundleInstance(final ComponentBundleManager componentBundleManager,
      final Bundle bundle, final Injector injector)
  {
    super(componentBundleManager, bundle);

    this.injector=injector;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  Module getComponentModule(final ServiceComponentInstance serviceComponent)
  {
    return module;
  }

  @Override
  Injector getComponentInjector(final ServiceComponentInstance serviceComponent)
  {
    return injector;
  }
}
