package net.evalcode.services.manager.service.console;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.evalcode.services.manager.component.annotation.Service;


/**
 * ConsoleService
 *
 * @author carsten.schipke@gmail.com
 */
@Service
public interface ConsoleService
{
  /**
   * Method
   *
   * @author carsten.schipke@gmail.com
   */
  @Documented
  @Target({ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Method
  {
    // PROPERTIES
    String command();
    String description();
  }


  // ACCESSORS/MUTATORS
  String getCommand();
  String getDescription();
}
