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

package org.pentaho.csrf.filter;

import org.pentaho.csrf.CsrfProtectionDefinition;
import org.pentaho.csrf.ICsrfService;
import org.pentaho.csrf.RequestMatcherDefinition;

import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SuppressWarnings( "PackageAccessibility" )
class CsrfUtil {
  // region CORS
  private static final String ORIGIN_HEADER = "origin";
  private static final String CORS_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
  private static final String CORS_ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";
  private static final String CORS_EXPOSE_HEADERS_HEADER = "Access-Control-Expose-Headers";
  private static final String CORS_EXPOSED_HEADERS = String.join( ",", Arrays.asList(
    ICsrfService.RESPONSE_HEADER_HEADER,
    ICsrfService.RESPONSE_HEADER_PARAM,
    ICsrfService.RESPONSE_HEADER_TOKEN ));

  static void setCorsResponseHeaders( HttpServletRequest request, HttpServletResponse response  ) {
    final String origin = request.getHeader( ORIGIN_HEADER );
    if ( origin != null && origin.length() > 0 ) {
      response.setHeader( CORS_ALLOW_ORIGIN_HEADER, origin );
      response.setHeader( CORS_ALLOW_CREDENTIALS_HEADER, "true" );
      response.setHeader( CsrfUtil.CORS_EXPOSE_HEADERS_HEADER, CORS_EXPOSED_HEADERS );
    }
  }
  // endregion

  static RequestMatcher buildCsrfRequestMatcher( Collection<CsrfProtectionDefinition> csrfProtectionDefinitions ) {

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
