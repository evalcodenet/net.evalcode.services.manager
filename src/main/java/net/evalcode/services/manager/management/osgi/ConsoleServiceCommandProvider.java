package net.evalcode.services.manager.management.osgi;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Singleton;
import net.evalcode.services.manager.annotation.Bind;
import net.evalcode.services.manager.annotation.Component;
import net.evalcode.services.manager.annotation.Unbind;
import net.evalcode.services.manager.internal.ComponentBundleManagerModule;
import net.evalcode.services.manager.internal.util.Messages;
import net.evalcode.services.manager.management.logging.Log;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;


/**
 * ConsoleServiceCommandProvider
 *
 * @author carsten.schipke@gmail.com
 */
@Singleton
@Component(module=ComponentBundleManagerModule.class, providedServices={CommandProvider.class})
public class ConsoleServiceCommandProvider implements CommandProvider
{
  // MEMBERS
  private final ConcurrentMap<String, ConsoleService> services=
    new ConcurrentHashMap<String, ConsoleService>();


  // ACCESSORS/MUTATORS
  @Log
  @Bind
  public void bindConsoleService(final ConsoleService service)
  {
    services.put(service.getCommand(), service);
  }

  @Log
  @Unbind
  public void unbindConsoleService(final ConsoleService service)
  {
    services.remove(service.getCommand());
  }


  // OVERRIDES/IMPLEMENTS
  public void _eval(final CommandInterpreter commandInterpreter)
  {
    final String service=commandInterpreter.nextArgument();

    if(null!=service && services.containsKey(service))
      invokeServiceCommand(services.get(service), commandInterpreter);
    else
      commandInterpreter.print(getHelp());
  }

  @Override
  public String getHelp()
  {
    final StringBuffer stringBuffer=new StringBuffer(
      Messages.MAINTENANCE_MANAGEMENT_CONSOLE_COMMAND_PROVIDER_HELP_HEADER.get()
    );

    for(final ConsoleService service : services.values())
      stringBuffer.append(getHelpForService(service));

    return stringBuffer.toString();
  }


  // IMPLEMENTATION
  static void invokeServiceCommand(final ConsoleService consoleService,
    final CommandInterpreter commandInterpreter)
  {
    final Map<String, Method> serviceMethods=getServiceMethods(consoleService);
    final String command=commandInterpreter.nextArgument();

    if(null==command || !serviceMethods.containsKey(command))
    {
      commandInterpreter.print(getHelpForService(consoleService));
    }
    else
    {
      try
      {
        serviceMethods.get(command).invoke(consoleService, new Object[] {commandInterpreter});
      }
      catch(final Exception e)
      {
        commandInterpreter.printStackTrace(e);
      }
    }
  }

  static String getHelpForService(final ConsoleService consoleService)
  {
    final StringBuffer help=new StringBuffer(String.format(
      Messages.MAINTENANCE_MANAGEMENT_CONSOLE_COMMAND_PROVIDER_HELP_SERVICE_HEADER.get(),
      consoleService.getCommand(),
      consoleService.getDescription()
    ));

    for(final Method method : getServiceMethods(consoleService).values())
    {
      help.append(String.format(
        Messages.MAINTENANCE_MANAGEMENT_CONSOLE_COMMAND_PROVIDER_HELP_SERVICE_COMMAND.get(),
        method.getAnnotation(ConsoleService.Method.class).command(),
        method.getAnnotation(ConsoleService.Method.class).description()
      ));
    }

    return help.toString();
  }

  static Map<String, Method> getServiceMethods(final ConsoleService consoleService)
  {
    final Map<String, Method> serviceMethods=new HashMap<String, Method>();

    final Class<?> clazz=consoleService.getClass();
    final Class<?> superClazz=consoleService.getClass().getSuperclass();
    final Method[] methods=(Method[])ArrayUtils.addAll(clazz.getMethods(), superClazz.getMethods());

    for(final Method method : methods)
    {
      final ConsoleService.Method annotation=method.getAnnotation(ConsoleService.Method.class);

      if(null!=annotation)
        serviceMethods.put(annotation.command(), method);
    }

    return serviceMethods;
  }
}
