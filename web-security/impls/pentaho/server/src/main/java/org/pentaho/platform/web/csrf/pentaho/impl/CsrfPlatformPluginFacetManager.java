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
package org.pentaho.platform.web.csrf.pentaho.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Node;
import org.pentaho.platform.api.engine.IPentahoObjectRegistration;
import org.pentaho.platform.api.engine.IPentahoRegistrableObjectFactory;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPlatformPluginFacetManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.references.SingletonPentahoObjectReference;
import org.pentaho.platform.web.csrf.pentaho.impl.messages.Messages;
import org.pentaho.web.security.csrf.CsrfConfiguration;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;

import static org.pentaho.platform.plugin.services.pluginmgr.PentahoSystemPluginManager.PLUGIN_ID;

public class CsrfPlatformPluginFacetManager implements IPlatformPluginFacetManager {

  private static final Log logger = LogFactory.getLog( CsrfPlatformPluginFacetManager.class );

  // region init Global Csrf Configuration
  /**
   * Registers the global {@link CsrfConfiguration}, <b>even if disabled</b>, in the given Pentaho object factory,
   * making it visible to the Pentaho system.
   * <p>
   * The returned {@link Closeable} instance, if closed, unloads the global {@code CsrfConfiguration}.
   *
   * @param pentahoSystemObjectFactory - The object factory of the Pentaho system.
   * @return A closeable instance.
   */
  @Override
  public Closeable init( IPentahoRegistrableObjectFactory pentahoSystemObjectFactory ) {

    CsrfConfiguration csrfConfiguration = getSettingCsrfConfiguration();

    IPentahoObjectRegistration registration = pentahoSystemObjectFactory.registerReference(
      new SingletonPentahoObjectReference.Builder<>( CsrfConfiguration.class )
        .object( csrfConfiguration )
        // This allows filtering specifically for the global entry.
        .attributes( Collections.singletonMap( PLUGIN_ID, "" ) )
        .build(),
      CsrfConfiguration.class );

    return registration::remove;
  }

  private boolean getSettingIsCsrfEnabled() {
    return Boolean.parseBoolean( PentahoSystem.getSystemSetting( CsrfUtil.CSRF_PROTECTION_ENABLED, "false" ) );
  }

  @SuppressWarnings( "unchecked" )
  private List<Node> getSettingCsrfProtectionNodes() {
    return (List<Node>) PentahoSystem.getSystemSettings().getSystemSettings( CsrfUtil.CSRF_PROTECTION_ELEMENT );
  }

  private CsrfConfiguration getSettingCsrfConfiguration() {
    CsrfConfiguration csrfConfiguration = null;

    if ( getSettingIsCsrfEnabled() ) {
      List<Node> csrfProtectionNodes = getSettingCsrfProtectionNodes();

      try {
        csrfConfiguration = CsrfUtil.parseXmlCsrfProtectionDefinition( csrfProtectionNodes );
      } catch ( IllegalArgumentException parseError ) {
        logger.warn(
          "CsrfPlatformPluginFacetManager:" +
            Messages.getInstance().getString(
              "CsrfPlatformPluginFacetManager.WARN_CSRF_SYSTEM_NOT_REGISTERED",
              parseError.getMessage() ) );
      }
    }

    if ( csrfConfiguration == null ) {
      // Create a disabled global configuration, anyway.
      csrfConfiguration = new CsrfConfiguration();
    }

    return csrfConfiguration;
  }
  // endregion

  /**
   * Register's the given plugin's {@link CsrfConfiguration} facet, <b>if enabled</b>, in the given Pentaho object
   * factory, making it visible to the Pentaho system.
   * <p>
   * The returned {@link Closeable} instance, if closed, unloads the plugin.
   */
  @Override
  public Closeable load( IPlatformPlugin plugin, IPentahoRegistrableObjectFactory pentahoSystemObjectFactory ) {

    CsrfConfiguration csrfConfiguration = plugin.getFacet( CsrfConfiguration.class );
    if ( csrfConfiguration == null || !csrfConfiguration.isEnabled() ) {
      return null;
    }

    IPentahoObjectRegistration registration = pentahoSystemObjectFactory.registerReference(
      new SingletonPentahoObjectReference.Builder<>( CsrfConfiguration.class )
        .object( csrfConfiguration )
        .attributes( Collections.singletonMap( PLUGIN_ID, plugin.getId() ) )
        .build(),
      CsrfConfiguration.class );

    return registration::remove;
  }
}
