package net.evalcode.services.manager.management.osgi;


import javax.inject.Inject;
import javax.inject.Singleton;
import net.evalcode.services.manager.annotation.Component;
import net.evalcode.services.manager.internal.ComponentBundleManager;
import net.evalcode.services.manager.internal.ComponentBundleManagerModule;
import net.evalcode.services.manager.internal.util.Messages;
import net.evalcode.services.manager.management.logging.Log;
import org.eclipse.osgi.framework.console.CommandInterpreter;


/**
 * ComponentBundleManagerConsoleService
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
@Component(module=ComponentBundleManagerModule.class)
public class ComponentBundleManagerConsoleService implements ConsoleService
{
  // PREDEFINED PROEPRTIES
  private static final String COMMAND="components";


  // MEMBERS
  @Inject
  private ComponentBundleManager componentBundleManager;


  // ACCESSORS/MUTATORS
  @Log
  @ConsoleService.Method(command="list", description="List Installed Components")
  public void list(final CommandInterpreter commandInterpreter)
  {
    for(final String componentBundleName : componentBundleManager.getComponentBundleNames())
      commandInterpreter.println(componentBundleName);
  }


  // OVERRIDES/IMPLEMENTS
  @Override
  public String getCommand()
  {
    return COMMAND;
  }

  @Override
  public String getDescription()
  {
    return Messages.COMPONENT_BUNDLE_MANAGER_CONSOLE_DESCRIPTION.get();
  }
}
