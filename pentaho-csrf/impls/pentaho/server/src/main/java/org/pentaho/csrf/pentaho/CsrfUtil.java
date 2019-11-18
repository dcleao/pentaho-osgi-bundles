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

import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.csrf.CsrfProtectionDefinition;
import org.pentaho.csrf.RequestMatcherDefinition;

import org.pentaho.csrf.pentaho.messages.Messages;

import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CsrfUtil {

  public static CsrfProtectionDefinition parseXmlCsrfProtectionDefinition( Element csrfProtectionElem ) {
    /* Example XML
     * <csrf-protection>
     *   <request-matcher type="regex" methods="GET,POST" pattern="" />
     *   ...
     * </csrf-protection>
     */
    List<RequestMatcherDefinition> requestMatchers = new ArrayList<>();

    for ( Node csrfRequestMatcherNode : csrfProtectionElem.selectNodes( "request-matcher" ) ) {
      requestMatchers.add( getCsrfRequestMatcher( (Element) csrfRequestMatcherNode ) );
    }

    if ( requestMatchers.size() == 0 ) {
      return null;
    }

    CsrfProtectionDefinition protectionDefinition = new CsrfProtectionDefinition();
    protectionDefinition.setProtectedRequestMatchers( requestMatchers );
    return protectionDefinition;
  }

  private static RequestMatcherDefinition getCsrfRequestMatcher( Element csrfRequestMatcherElem ) {

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

    List<String> methodsCol = new ArrayList<>(  );

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

    return new RequestMatcherDefinition( type, pattern, methodsCol );
  }

  public static RequestMatcher buildCsrfRequestMatcher( Collection<CsrfProtectionDefinition> csrfProtectionDefinitions ) {

    List<RequestMatcher> requestMatchers = new ArrayList<>();

    for ( CsrfProtectionDefinition csrfProtectionDefinition : csrfProtectionDefinitions ) {
      collectRequestMatchers( requestMatchers, csrfProtectionDefinition );
    }

    return requestMatchers.size() > 0 ? new OrRequestMatcher( requestMatchers ) : null;
  }

  private static void collectRequestMatchers( Collection<RequestMatcher> requestMatchers,
                                              CsrfProtectionDefinition csrfProtectionDefinition ) {

    Collection<RequestMatcherDefinition> requestMatcherDefinitions =
      csrfProtectionDefinition.getProtectedRequestMatchers();
    if ( requestMatcherDefinitions != null ) {
      for ( RequestMatcherDefinition requestMatcherDefinition : requestMatcherDefinitions ) {

        Collection<String> httpMethods = requestMatcherDefinition.getMethods();
        if ( httpMethods == null ) {
          requestMatchers.add(
            new RegexRequestMatcher( requestMatcherDefinition.getPattern(), null, false ) );
        } else {
          for ( String httpMethod : requestMatcherDefinition.getMethods() ) {
            requestMatchers.add(
              new RegexRequestMatcher( requestMatcherDefinition.getPattern(), httpMethod, false ) );
          }
        }
      }
    }
  }
}
