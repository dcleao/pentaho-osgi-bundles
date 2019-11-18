/*!
 *
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
 *
 * Copyright (c) 2019 Hitachi Vantara. All rights reserved.
 *
 */
package org.pentaho.csrf.pentaho;

import com.google.common.annotations.VisibleForTesting;
import org.apache.http.HttpStatus;
import org.pentaho.csrf.ICsrfService;
import org.pentaho.platform.web.WebUtil;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Binds a {@link org.springframework.security.web.csrf.CsrfToken} to the {@link HttpServletResponse} headers if the
 * Spring {@link org.springframework.security.web.csrf.CsrfFilter} has placed one in the {@link HttpServletRequest}.
 *
 * <p>
 *   Based on the work found in:
 *   http://stackoverflow.com/questions/20862299/with-spring-security-3-2-0-release-how-can-i-get-the-csrf-token-in-a-page-that
 * <p>
 *
 * Code from
 * https://github.com/aditzel/spring-security-csrf-filter/blob/master/src/main/java/com/allanditzel/springframework/security/web/csrf/CsrfTokenResponseHeaderBindingFilter.java
 */
public class CsrfTokenResponseHeaderFilter extends OncePerRequestFilter {

  @VisibleForTesting
  static final String REQUEST_ATTRIBUTE_NAME = "_csrf";

  @Override
  protected void doFilterInternal( HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain ) throws ServletException {

    final CsrfToken token = (CsrfToken) request.getAttribute( REQUEST_ATTRIBUTE_NAME );
    if ( token != null ) {
      final String tokenHeaderName = token.getHeaderName();
      response.setHeader( ICsrfService.RESPONSE_HEADER_HEADER, tokenHeaderName );

      final String tokenParameterName = token.getParameterName();
      response.setHeader( ICsrfService.RESPONSE_HEADER_PARAM, tokenParameterName );

      final String tokenValue = token.getToken();
      response.setHeader( ICsrfService.RESPONSE_HEADER_TOKEN, tokenValue );
    }

    // Add CORS headers, if CORS is enabled.
    WebUtil.setCorsResponseHeaders( request, response, getCorsHeadersConfiguration() );

    response.setStatus( HttpStatus.SC_NO_CONTENT );
  }

  @VisibleForTesting
  Map<String, List<String>> getCorsHeadersConfiguration() {
    Map<String, List<String>> corsConfiguration = new HashMap<>( 1 );

    final List<String> exposedHeaders = Arrays.asList(
      ICsrfService.RESPONSE_HEADER_HEADER,
      ICsrfService.RESPONSE_HEADER_PARAM,
      ICsrfService.RESPONSE_HEADER_TOKEN );

    corsConfiguration.put( WebUtil.CORS_EXPOSE_HEADERS_HEADER, exposedHeaders );

    return corsConfiguration;
  }
}
