package net.evalcode.services.manager.util.security;


import org.apache.commons.lang.RandomStringUtils;
import net.evalcode.services.manager.internal.util.SystemProperty;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;


/**
 * Hash
 *
 * @author evalcode.net
 */
public class Hash
{
  // STATIC ACCESSORS
  public static String crc32(final String string)
  {
    final HashFunction function=Hashing.crc32();
    final HashCode hash=function.hashString(string, SystemProperty.getCharset());

    return hash.toString();
  }

  public static String md5(final String string)
  {
    final HashFunction function=Hashing.md5();
    final HashCode hash=function.hashString(string, SystemProperty.getCharset());

    return hash.toString();
  }

  public static String sha1(final String string)
  {
    final HashFunction function=Hashing.sha1();
    final HashCode hash=function.hashString(string, SystemProperty.getCharset());

    return hash.toString();
  }

  public static String sha256(final String string)
  {
    final HashFunction function=Hashing.sha256();
    final HashCode hash=function.hashString(string, SystemProperty.getCharset());

    return hash.toString();
  }

  public static String sha512(final String string)
  {
    final HashFunction function=Hashing.sha512();
    final HashCode hash=function.hashString(string, SystemProperty.getCharset());

    return hash.toString();
  }

  public static String random()
  {
    return sha1(RandomStringUtils.randomAscii(32));
  }
}
