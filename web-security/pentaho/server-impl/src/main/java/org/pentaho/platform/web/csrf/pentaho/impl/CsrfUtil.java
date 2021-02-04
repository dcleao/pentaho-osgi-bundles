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
import org.dom4j.Node;
import org.pentaho.platform.web.csrf.pentaho.impl.messages.Messages;
import org.hitachivantara.web.security.RequestMatcherConfiguration;
import org.hitachivantara.web.security.api.csrf.CsrfConfiguration;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings( "PackageAccessibility" )
class CsrfUtil {

  public static final String CSRF_PROTECTION_ENABLED = "csrf-protection-enabled";
  public static final String CSRF_PROTECTION_ELEMENT = "csrf-protection";
  private static final String CSRF_PROTECTION_REQUEST_MATCHER_ELEMENT = "request-matcher";

  public static CsrfConfiguration parseXmlCsrfProtectionDefinition( List<Node> csrfProtectionElems ) {
    /* Example XML
     * <csrf-protection>
     *   <request-matcher type="regex" methods="GET,POST" pattern="" />
     *   ...
     * </csrf-protection>
     * ...
     */
    List<RequestMatcherConfiguration> requestMatchers = new ArrayList<>();
    for ( Node csrfProtectionElem : csrfProtectionElems ) {
      for ( Node csrfRequestMatcherNode : csrfProtectionElem.selectNodes( CSRF_PROTECTION_REQUEST_MATCHER_ELEMENT ) ) {
        requestMatchers.add( getCsrfRequestMatcher( (Element) csrfRequestMatcherNode ) );
      }
    }

    return new CsrfConfiguration( requestMatchers );
  }

  private static RequestMatcherConfiguration getCsrfRequestMatcher( Element csrfRequestMatcherElem ) {

    String type = csrfRequestMatcherElem.attributeValue( "type", "regex" );
    String pattern = csrfRequestMatcherElem.attributeValue( "pattern", "" );
    String methods = csrfRequestMatcherElem.attributeValue( "methods", "GET,POST" );

    if ( !"regex".equals( type ) ) {
      throw new IllegalArgumentException(
        Messages.getInstance().getString(
          "CsrfProtection.REQUEST_MATCHER_INVALID_TYPE",
          type ) );
    }

    if ( pattern == null || pattern.length() == 0 ) {
      throw new IllegalArgumentException(
        Messages.getInstance().getString( "CsrfProtection.REQUEST_MATCHER_NO_PATTERN" ) );
    }

    List<String> methodsCol = new ArrayList<>();

    for ( String method : methods.split( "\\s*,\\s*" ) ) {
      try {
        RequestMethod.valueOf( method );
      } catch ( IllegalArgumentException invalidEnumError ) {
        throw new IllegalArgumentException(
          Messages.getInstance().getString(
            "CsrfProtection.REQUEST_MATCHER_INVALID_METHOD",
            method ) );
      }

      methodsCol.add( method );
    }

    if ( methodsCol.size() == 0 ) {
      methodsCol.add( "POST" );
      methodsCol.add( "GET" );
    }

    return new RequestMatcherConfiguration( type, pattern, methodsCol );
  }
}
