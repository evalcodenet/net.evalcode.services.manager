package net.evalcode.services.manager.internal;


import java.util.HashSet;
import java.util.Set;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;


/**
 * ComponentBundleManifest
 *
 * @author carsten.schipke@gmail.com
 */
public class ComponentBundleManifest
{
  /**
   * Header
   *
   * @author carsten.schipke@gmail.com
   */
  static enum Header
  {
    EXPORT_PACKAGE(Constants.EXPORT_PACKAGE),
    REQUIRE_BUNDLE(Constants.REQUIRE_BUNDLE);


    final String headerName;


    Header(final String headerName)
    {
      this.headerName=headerName;
    }
  }


  // MEMBERS
  final Bundle bundle;


  // CONSTRUCTION
  ComponentBundleManifest(final Bundle bundle)
  {
    this.bundle=bundle;
  }


  // ACCESSORS/MUTATORS
  public Set<String> getEntry(final Header header)
  {
    final Set<String> entries=new HashSet<String>();
    final Object headerObject=bundle.getHeaders().get(header.headerName);

    if(null==headerObject)
      return entries;

    final String[] headerValues=headerObject.toString().split(",");

    for(final String value : headerValues)
      entries.add(value.trim());

    return entries;
  }
}
