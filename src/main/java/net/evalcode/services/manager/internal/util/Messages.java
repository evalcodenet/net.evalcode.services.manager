package net.evalcode.services.manager.internal.util;


import java.util.MissingResourceException;
import java.util.ResourceBundle;


/**
 * Messages
 *
 * @author carsten.schipke@gmail.com
 */
public enum Messages
{
  // net.evalcode.services.manager
  STARTING("manager.starting"),
  STOPPING("manager.stopping"),


  // net.evalcode.services.manager.internal
  REGISTERED_SERVICE("manager.internal.registered_service"),
  SERVICE_REGISTRY_UPDATE("manager.internal.notifying_observers_for_service_registry_update"),
  UNEGISTER_SERVICE("manager.internal.unregister_service"),
  UNABLE_TO_ACCESS_COMPONENT_METHOD("manager.internal.unable_to_access_component_method"),
  UNABLE_TO_ACCESS_COMPONENT_MODULE("manager.internal.unable_to_access_component_module"),
  UNABLE_TO_INSTANTIATE_COMPONENT_MODULE("manager.internal.unable_to_instantiate_component_module"),
  UNABLE_TO_INVOKE_COMPONENT_METHOD("manager.internal.unable_to_invoke_component_method"),
  UNABLE_TO_LOAD_EXPORTED_CLASS("manager.internal.unable_to_load_exported_class"),


  // net.evalcode.services.manager.management.osgi
  COMPONENT_BUNDLE_MANAGER_CONSOLE_DESCRIPTION(
    "manager.management.component_bundle_manager_console_description"
  ),
  MAINTENANCE_MANAGEMENT_CONSOLE_COMMAND_PROVIDER_HELP_HEADER(
    "manager.management.console_command_provider_help_header"
  ),
  MAINTENANCE_MANAGEMENT_CONSOLE_COMMAND_PROVIDER_HELP_SERVICE_HEADER(
    "manager.management.console_command_provider_help_service_header"
  ),
  MAINTENANCE_MANAGEMENT_CONSOLE_COMMAND_PROVIDER_HELP_SERVICE_COMMAND(
    "manager.management.console_command_provider_help_service_command"
  ),


  // net.evalcode.services.manager.management.jmx
  MAINTENANCE_MANAGEMENT_MBEAN_REGISTRY_BIND_MANAGEMENT_BEAN(
    "manager.management.mbean_registry_bind_management_bean"
  ),
  MAINTENANCE_MANAGEMENT_MBEAN_REGISTRY_UNBIND_MANAGEMENT_BEAN(
    "manager.management.mbean_registry_unbind_management_bean"
  );


  // PREDEFINED PROPERTIES
  final ResourceBundle resourceBundle=ResourceBundle.getBundle(
    "net.evalcode.services.manager.messages", SystemProperty.getLocale()
  );


  // MEMBERS
  final String key;


  // CONSTRUCTION
  Messages(final String key)
  {
    this.key=key;
  }


  // ACCESSORS/MUTATORS
  public String get()
  {
    try
    {
      return resourceBundle.getString(key);
    }
    catch(final MissingResourceException e)
    {
      return '!'+key+'!';
    }
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public String toString()
  {
    return get();
  }
}
