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
import org.pentaho.csrf.ICsrfProtectionDefinitionListener;
import org.pentaho.csrf.pentaho.IPentahoCsrfProtection;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.StringUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings( "PackageAccessibility" )
public class PentahoCsrfProtection implements IPentahoCsrfProtection {

  private static final String CSRF_PROTECTION_ENABLED = "csrf-protection-enabled";

  private IPluginManager getPluginManager() {
    return PentahoSystem.get( IPluginManager.class );
  }

  private boolean isPluginEnabled( String pluginId ) {
    Object setting = getPluginManager().getPluginSetting( pluginId, CSRF_PROTECTION_ENABLED, "true" );

    return !( setting instanceof String ) || Boolean.parseBoolean( (String) setting );
  }

  @Override
  public boolean isEnabled() {
    return Boolean.parseBoolean( PentahoSystem.getSystemSetting( CSRF_PROTECTION_ENABLED, "false" ) );
  }

  @Override
  public boolean isEnabled( String pluginId ) {
    return isEnabled() && isPluginEnabled( pluginId );
  }

  @Override public boolean isCorsAllowed() {
    return Boolean.parseBoolean(
      PentahoSystem.getSystemSetting( PentahoSystem.CORS_REQUESTS_ALLOWED, "false" ) );
  }

  @Override
  public Collection<CsrfProtectionDefinition> getProtectionDefinitions() {
    // Should be empty, when globally disabled, but just in case...
    return isEnabled()
      ? PentahoSystem.getAll( CsrfProtectionDefinition.class )
      : Collections.emptyList();
  }

  @Override
  public void addListener( final ICsrfProtectionDefinitionListener listener ) {
    getPluginManager().addPluginManagerListener( listener::onDefinitionsChanged );
  }

  @Override
  public Set<String> getCorsAllowOrigins() {
    Set<String> allowOriginsSet = new HashSet<>();

    if ( isCorsAllowed() ) {
      String allowOrigins = PentahoSystem.getSystemSetting(
        PentahoSystem.CORS_REQUESTS_ALLOWED_DOMAINS, null );

      if ( !StringUtil.isEmpty( allowOrigins ) ) {
        allowOriginsSet.addAll( Arrays.asList( allowOrigins.split( "\\s*,\\s*" ) ) );
      }
    }

    return allowOriginsSet;
  }
}
