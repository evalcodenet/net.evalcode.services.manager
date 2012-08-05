package net.evalcode.services.manager.management.osgi;


import static org.junit.Assert.assertEquals;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import net.evalcode.services.manager.management.osgi.ConsoleService;
import net.evalcode.services.manager.management.osgi.ConsoleServiceCommandProvider;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * Test {@link ConsoleServiceCommandProvider}
 *
 * @author carsten.schipke@gmail.com
 */
public class ConsoleServiceCommandProviderTest
{
  // PREDEFINED PROPERTIES
  static ConsoleServiceCommandProvider consoleCommandProvider;


  // SETUP
  @BeforeClass
  public static void setUp()
  {
    consoleCommandProvider=new ConsoleServiceCommandProvider();
  }


  // TESTS
  @Test
  public void testGetServiceMethods() throws IllegalAccessException, InvocationTargetException
  {
    final ConsoleService consoleService=new ConsoleServiceImpl();
    final CommandInterpreter commandInterpreter=Mockito.mock(CommandInterpreter.class);

    final Map<String, Method> serviceMethods=
      ConsoleServiceCommandProvider.getServiceMethods(consoleService);

    assertEquals(1, serviceMethods.size());
    assertEquals("bar", serviceMethods.keySet().toArray()[0]);

    serviceMethods.get("bar").invoke(consoleService, new Object[] {commandInterpreter});

    Mockito.verify(commandInterpreter, Mockito.atLeastOnce()).println("bar");
  }


  /**
   * ConsoleServiceImpl
   *
   * @author carsten.schipke@gmail.com
   */
  @Ignore
  static class ConsoleServiceImpl implements ConsoleService
  {
    // ACCESSORS/MUTATORS
    @ConsoleService.Method(command="bar", description="Bar Console Command")
    public void invokeBar(final CommandInterpreter commandInterpreter)
    {
      commandInterpreter.println("bar");
    }


    // OVERRIDES/IMPLEMENTS
    @Override
    public String getCommand()
    {
      return "foo";
    }

    @Override
    public String getDescription()
    {
      return "Foo Console Service";
    }
  }
}
