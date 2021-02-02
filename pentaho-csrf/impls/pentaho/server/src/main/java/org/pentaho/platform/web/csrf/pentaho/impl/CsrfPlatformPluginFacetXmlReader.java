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

import org.dom4j.Element;
import org.pentaho.web.security.csrf.CsrfConfiguration;
import org.pentaho.platform.api.engine.IPlatformPlugin;
import org.pentaho.platform.api.engine.IPlatformPluginFacet;
import org.pentaho.platform.plugin.services.pluginmgr.IPlatformPluginFacetXmlReader;

@SuppressWarnings( "PackageAccessibility" )
public class CsrfPlatformPluginFacetXmlReader implements IPlatformPluginFacetXmlReader {
  @Override
  public void read( IPlatformPlugin plugin, IPlatformPluginFacet facet, Element pluginDefinition ) {

    Element csrfProtectionElem = (Element) pluginDefinition.selectSingleNode( CsrfUtil.CSRF_PROTECTION_ELEMENT );
    if ( csrfProtectionElem != null ) {
      CsrfConfiguration protectionDefinition = CsrfUtil.parseXmlCsrfProtectionDefinition( csrfProtectionElem );

      plugin.setFacet( CsrfConfiguration.class, protectionDefinition );
    }
  }
}
