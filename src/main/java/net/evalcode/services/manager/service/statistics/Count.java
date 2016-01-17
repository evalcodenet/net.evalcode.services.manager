package net.evalcode.services.manager.service.statistics;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Count
 *
 * <p> Counts invocations of decorated methods for simple statistics.
 *
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Count
{
  // PROPERTIES
  String value() default "";
}
