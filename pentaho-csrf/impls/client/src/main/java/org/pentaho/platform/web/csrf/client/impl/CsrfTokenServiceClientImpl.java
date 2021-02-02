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

package org.pentaho.platform.web.csrf.client.impl;

import org.pentaho.web.security.csrf.CsrfTokenService;
import org.pentaho.platform.web.csrf.client.CsrfToken;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * The `CsrfClient` class implements the `ICsrfClient` interface.
 */
public class CsrfTokenServiceClientImpl implements org.pentaho.platform.web.csrf.client.CsrfTokenServiceClient {

  private static final String COOKIE_HEADER = "Cookie";
  private static final String SET_COOKIE_HEADER = "Set-Cookie";

  private final Client client;

  CsrfTokenServiceClientImpl() {
    client = ClientBuilder.newClient();
  }

  // Supports testing
  CsrfTokenServiceClientImpl( Client client ) {
    if ( client == null ) {
      throw new IllegalArgumentException( "Argument 'client' is required." );
    }

    this.client = client;
  }

  /** @inheritDoc */
  public CsrfToken getToken( URI tokenServiceUri, CookieHandler cookieHandler, URI protectedServiceUri ) {

    if ( tokenServiceUri == null ) {
      throw new IllegalArgumentException( "Argument 'tokenServiceUri' is required.");
    }

    if ( cookieHandler == null ) {
      throw new IllegalArgumentException( "Argument 'cookieHandler' is required.");
    }

    if ( protectedServiceUri == null ) {
      throw new IllegalArgumentException( "Argument 'protectedServiceUri' is required.");
    }

    Invocation.Builder builder = client.target( tokenServiceUri )
      .queryParam( CsrfTokenService.QUERY_PARAM_URL, protectedServiceUri )
      .request();

    // Write cookies to the request, read from the cookieHandler.
    writeCookiesToRequest( builder, tokenServiceUri, cookieHandler );

    Response response = builder.get();

    // Read cookies from the response, and write them to the cookieHandler.
    readCookiesFromResponse( response, tokenServiceUri, cookieHandler );

    // The response body should be empty, and return 204.
    // The relevant response is in the response headers.
    if ( response.getStatus() != 204 && response.getStatus() != 200) {
      return null;
    }

    // When CSRF protection is disabled, the token is not returned.
    String token = response.getHeaderString( CsrfTokenService.RESPONSE_HEADER_TOKEN );
    if ( token == null || token.length() == 0 ) {
      return null;
    }

    String header = response.getHeaderString( CsrfTokenService.RESPONSE_HEADER_HEADER );
    String parameter = response.getHeaderString( CsrfTokenService.RESPONSE_HEADER_PARAM );

    return new CsrfTokenImpl( header, parameter, token );
  }

  private void writeCookiesToRequest( Invocation.Builder builder, URI tokenServiceUri, CookieHandler cookieHandler ) {

    Map<String, List<String>> requestHeadersMap;
    Map<String, List<String>> currentRequestHeadersMap = new java.util.HashMap<>();
    try {
      requestHeadersMap = cookieHandler.get( tokenServiceUri, currentRequestHeadersMap );
    } catch ( IOException e ) {
      e.printStackTrace();
      return;
    }

    // Each cookie text is something like: "name=value"
    List<String> cookiesText = requestHeadersMap.get( COOKIE_HEADER );
    if ( cookiesText != null ) {
      for ( String cookieText : cookiesText ) {
        builder.cookie( Cookie.valueOf( cookieText ) );
      }
    }
  }

  private void readCookiesFromResponse( Response response, URI tokenServiceUri, CookieHandler cookieHandler ) {

    Map<String, NewCookie> newCookieMap = response.getCookies();
    if ( newCookieMap.size() > 0 ) {
      List<String> newCookiesText = newCookieMap.values().stream()
        .map( NewCookie::toString ).collect( Collectors.toList() );

      Map<String, List<String>> responseHeadersMap = new HashMap<>();
      responseHeadersMap.put( SET_COOKIE_HEADER, newCookiesText );

      try {
        cookieHandler.put( tokenServiceUri, responseHeadersMap );
      } catch ( IOException e ) {
        e.printStackTrace();
      }
    }
  }
}
