package net.evalcode.services.manager.service.cache.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.evalcode.services.manager.service.cache.impl.NoCacheKeyGenerator;
import net.evalcode.services.manager.service.cache.spi.internal.CacheKeyGenerator;


/**
 * Key
 *
 * @author carsten.schipke@gmail.com
 */
@Documented
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Key
{
  // PROPERTIES
  Key.Type type() default Type.PLAIN;

  String value() default "";

  Class<? extends CacheKeyGenerator> generator() default NoCacheKeyGenerator.class;


  /**
   * Type
   *
   * @author carsten.schipke@gmail.com
   */
  public enum Type
  {
    // PREDEFINED TYPES
    HASHCODE,
    PLAIN;
  }
}
