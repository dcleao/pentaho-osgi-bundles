/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 */
package org.pentaho.csrf.pentaho.impl;

import org.pentaho.csrf.CsrfProtectionDefinition;
import org.pentaho.csrf.pentaho.IPentahoCsrfProtection;
import org.pentaho.platform.api.engine.IPentahoObjectRegistration;
import org.pentaho.platform.api.engine.IPentahoRegistrableObjectFactory;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPlatformPluginFacet;
import org.pentaho.platform.api.engine.IPlatformPluginFacetLoader;
import org.pentaho.platform.engine.core.system.objfac.references.SingletonPentahoObjectReference;

import java.io.Closeable;
import java.util.Collections;

import static org.pentaho.platform.plugin.services.pluginmgr.PentahoSystemPluginManager.PLUGIN_ID;

@SuppressWarnings( "PackageAccessibility" )
public class CsrfPlatformPluginFacetLoader implements IPlatformPluginFacetLoader {

  private final IPentahoCsrfProtection pentahoCsrfProtection;

  public CsrfPlatformPluginFacetLoader( IPentahoCsrfProtection pentahoCsrfProtection ) {
    this.pentahoCsrfProtection = pentahoCsrfProtection;
  }

  @Override
  public Closeable load(
    IPlatformPlugin plugin,
    IPlatformPluginFacet facet,
    IPentahoRegistrableObjectFactory pentahoSystemObjectFactory ) {

    if ( !pentahoCsrfProtection.isEnabled( plugin.getId() ) ) {
      return null;
    }

    CsrfProtectionDefinition protectionDefinition = plugin.getFacet( CsrfProtectionDefinition.class );
    if ( protectionDefinition == null ) {
      return null;
    }

    IPentahoObjectRegistration handle = pentahoSystemObjectFactory.registerReference(
      new SingletonPentahoObjectReference.Builder<>( CsrfProtectionDefinition.class )
        .object( protectionDefinition )
        .attributes( Collections.singletonMap( PLUGIN_ID, plugin.getId() ) )
        .build(),
      CsrfProtectionDefinition.class );

    return handle::remove;
  }
}
