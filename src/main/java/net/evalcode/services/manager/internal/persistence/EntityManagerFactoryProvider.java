package net.evalcode.services.manager.internal.persistence;


import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import net.evalcode.services.manager.component.ComponentBundleInterface;
import com.google.inject.Provider;


/**
 * EntityManagerFactoryProvider
 *
 * @author carsten.schipke@gmail.com
 */
public class EntityManagerFactoryProvider implements Provider<EntityManagerFactory>
{
  // MEMBERS
  final ComponentBundleInterface componentBundleInterface;


  // CONSTRUCTION
  @Inject
  EntityManagerFactoryProvider(final ComponentBundleInterface componentBundleInterface)
  {
    this.componentBundleInterface=componentBundleInterface;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public EntityManagerFactory get()
  {
    return componentBundleInterface.getEntityManagerFactory();
  }
}
