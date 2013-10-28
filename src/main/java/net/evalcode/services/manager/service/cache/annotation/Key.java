package net.evalcode.services.manager.service.cache.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.evalcode.services.manager.service.cache.impl.NoCacheKeyGenerator;
import net.evalcode.services.manager.service.cache.spi.CacheKeyGenerator;


/**
 * Key
 *
 * @author evalcode.net
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Key
{
  // PROPERTIES
  Key.Type type() default Type.HASHCODE;

  String value() default "";

  Class<? extends CacheKeyGenerator> generator() default NoCacheKeyGenerator.class;


  /**
   * Type
   *
   * @author evalcode.net
   */
  public enum Type
  {
    // PREDEFINED TYPES
    ALL,
    GENERATOR,
    HASHCODE,
    SIGNATURE;
  }
}
