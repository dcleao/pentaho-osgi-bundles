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
import org.dom4j.Element;
import org.pentaho.web.security.csrf.CsrfConfiguration;
import org.pentaho.platform.web.csrf.pentaho.PentahoCsrfProtection;
import org.pentaho.platform.api.engine.IPentahoObjectRegistration;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.references.SingletonPentahoObjectReference;
import org.pentaho.platform.web.csrf.pentaho.impl.messages.Messages;

import java.util.List;

@SuppressWarnings( "PackageAccessibility" )
public class CsrfProtectionSystemListener implements IPentahoSystemListener {

  private static final Log logger = LogFactory.getLog( CsrfProtectionSystemListener.class );

  private final PentahoCsrfProtection pentahoCsrfProtection;

  private IPentahoObjectRegistration protectionDefinitionRegistration = null;

  public CsrfProtectionSystemListener( PentahoCsrfProtection pentahoCsrfProtection ) {
    this.pentahoCsrfProtection = pentahoCsrfProtection;
  }

  @Override
  public boolean startup( IPentahoSession session ) {
    // Register pentaho.xml's CsrfProtectionDefinition.

    if ( !pentahoCsrfProtection.isEnabled() ) {
      return true;
    }

    List csrProtectionNodes = PentahoSystem.getSystemSettings().getSystemSettings( CsrfUtil.CSRF_PROTECTION_ELEMENT );
    if ( csrProtectionNodes.size() > 0 ) {
      Element csrfProtectionElem = (Element) csrProtectionNodes.get( 0 );

      CsrfConfiguration csrfConfiguration;
      try {
        csrfConfiguration = CsrfUtil.parseXmlCsrfProtectionDefinition( csrfProtectionElem );
      } catch ( IllegalArgumentException parseError ) {
        logger.warn(
          "CsrfProtectionSystemListener:" +
            Messages.getInstance().getString(
              "CsrfProtectionSystemListener.WARN_CSRF_SYSTEM_NOT_REGISTERED",
              parseError.getMessage() ) );
        return true;
      }

      protectionDefinitionRegistration = PentahoSystem.registerReference(
        new SingletonPentahoObjectReference.Builder<>( CsrfConfiguration.class )
          .object( csrfConfiguration )
          .build(),
        CsrfConfiguration.class );
    }

    return true;
  }

  @Override
  public void shutdown() {
    if ( protectionDefinitionRegistration != null ) {
      protectionDefinitionRegistration.remove();
      protectionDefinitionRegistration = null;
    }
  }
}
