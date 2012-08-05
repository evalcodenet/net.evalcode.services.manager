package net.evalcode.services.manager.component;


import java.util.Set;
import javax.persistence.EntityManagerFactory;
import org.hibernate.ejb.Ejb3Configuration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;


/**
 * ComponentBundleInterface
 *
 * @author carsten.schipke@gmail.com
 */
public interface ComponentBundleInterface
{
  // ACCESSORS/MUTATORS
  Bundle getBundle();
  BundleContext getBundleContext();

  Configuration getConfiguration();
  Set<Class<?>> getConfigurationEntities();

  EntityManagerFactory getEntityManagerFactory();
  Ejb3Configuration getEntityManagerConfiguration();

  ComponentBundleInspector getInspector();
  Set<ServiceComponentInterface> getServiceComponents();
}
