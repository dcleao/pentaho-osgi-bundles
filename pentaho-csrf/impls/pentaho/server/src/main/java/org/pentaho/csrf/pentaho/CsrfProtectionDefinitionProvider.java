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
package org.pentaho.csrf.pentaho;

import org.pentaho.csrf.CsrfProtectionDefinition;
import org.pentaho.csrf.ICsrfProtectionDefinitionListener;
import org.pentaho.csrf.ICsrfProtectionDefinitionProvider;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.Collection;

public class CsrfProtectionDefinitionProvider implements ICsrfProtectionDefinitionProvider {
  @Override
  public boolean isEnabled() {
    return PentahoSystem.isCsrfProtectionEnabled();
  }

  @Override
  public Collection<CsrfProtectionDefinition> getProtectionDefinitions() {
    return PentahoSystem.getAll( CsrfProtectionDefinition.class );
  }

  @Override
  public void addListener( final ICsrfProtectionDefinitionListener listener ) {
    PentahoSystem.get( IPluginManager.class ).addPluginManagerListener( listener::onDefinitionsChanged );
  }
}
