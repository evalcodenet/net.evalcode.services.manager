package net.evalcode.services.manager.component;


import com.google.inject.Injector;
import com.google.inject.Module;


/**
 * ServiceComponentInterface
 *
 * @author carsten.schipke@gmail.com
 */
public interface ServiceComponentInterface
{
  // ACCESSORS/MUTATORS
  String getName();
  <T> Class<T> getType();
  <T> T getInstance();

  Injector getInjector();
  Module getModule();

  ComponentBundleInterface getComponentBundle();
}
