package net.evalcode.services.manager.service.logging;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Log
 *
 * <p> Logs invocations of decorated methods for simple auditing.
 *
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log
{
  // PROPERTIES
  /**
   * %1: Type Instance <br>
   * %2: Method Name <br>
   * %3: Method Arguments <br>
   *
   * @return Log Pattern
   */
  String pattern() default "Invoke {}#{}({}).";

  /**
   * @return Log Level
   */
  Level level() default Level.DEBUG;


  /**
   * Level
   *
   * @author carsten.schipke@gmail.com
   */
  public static enum Level
  {
    // PREDEFINED LOG LEVELS
    DEBUG,
    INFO;
  }
}
