package net.evalcode.services.manager.internal.persistence;


import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import com.google.inject.Provider;


/**
 * EntityManagerProvider
 *
 * @author carsten.schipke@gmail.com
 */
public class EntityManagerProvider implements Provider<EntityManager>
{
  // MEMBERS
  private final Provider<EntityManagerFactory> entityManagerFactoryProvider;


  // CONSTRUCTION
  @Inject
  EntityManagerProvider(final Provider<EntityManagerFactory> entityManagerFactoryProvider)
  {
    this.entityManagerFactoryProvider=entityManagerFactoryProvider;
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public EntityManager get()
  {
    return entityManagerFactoryProvider.get().createEntityManager();
  }
}
