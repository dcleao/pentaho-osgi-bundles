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

package org.pentaho.platform.web.csrf.client;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * The `CsrfTokenServiceClient` class is a CSRF REST service JAX-RS client.
 *
 * The CSRF token retrieval service is necessarily an HTTP service
 * that works over a stateful HTTP connection.
 * The state of the HTTP connection is accomplished through the use of cookies and,
 * specifically, using a `CookieHandler`.
 */
public class CsrfTokenServiceClient {

  /**
   * The name of the query parameter on which to specify the URL of the protected service which is to be called.
   */
  static final String QUERY_PARAM_URL = "url";

  /**
   * The name of the response header whose value is the name of the request header on which the value of the CSRF token
   * should be sent on requests to the protected service.
   */
  static final String RESPONSE_HEADER_HEADER = "X-CSRF-HEADER";

  /**
   * The name of the response header whose value is the name of the request parameter on which the value of the CSRF
   * token should be sent on requests to the protected service.
   * <p>
   * It is preferable to use a request header to send the CSRF token, if possible.
   */
  static final String RESPONSE_HEADER_PARAM = "X-CSRF-PARAM";

  /**
   * The name of the response header whose value is the CSRF token.
   */
  static final String RESPONSE_HEADER_TOKEN = "X-CSRF-TOKEN";

  static final String COOKIE_HEADER = "Cookie";
  static final String SET_COOKIE_HEADER = "Set-Cookie";

  private final Client client;

  public CsrfTokenServiceClient() {
    client = ClientBuilder.newClient();
  }

  // Supports testing
  CsrfTokenServiceClient( Client client ) {
    this.client = Objects.requireNonNull( client );
  }

  /**
   * Gets a CSRF token to access a given protected service, if one needs to be used.
   *
   * To maintain the state of the HTTP connection the following is performed:
   * - Cookies which are applicable to {@code tokenServiceUrl} and are present in the cookie handler
   *   are added to the HTTP request.
   * - Cookies which the server sets in the HTTP response are added to the cookie handler.
   *
   * When CSRF protection is disabled, or if the specified {@code protectedServiceUrl} is not CSRF protected,
   * {@code null} may be returned.
   *
   * @param tokenServiceUri - The URI of the CSRF token retrieval service.
   * @param cookieHandler - The cookie handler user to maintain HTTP connection state.
   * @param protectedServiceUri - The URI of the protected service for which a CSRF token is requested.
   *
   * @return The CSRF token to use to access the specified protected service;
   * {@code null}, if a CSRF token need not be sent.
   */
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
      .queryParam( QUERY_PARAM_URL, protectedServiceUri )
      .request();

    // Write cookies to the request, read from the cookieHandler.
    writeCookiesToRequest( builder, tokenServiceUri, cookieHandler );

    Response response = builder.get();

    // Read cookies from the response, and write them to the cookieHandler.
    readCookiesFromResponse( response, tokenServiceUri, cookieHandler );

    // The response body should be empty, and return 204.
    // The relevant response is in the response headers.
    if ( response.getStatus() != 204 && response.getStatus() != 200 ) {
      return null;
    }

    // When CSRF protection is disabled, the token is not returned.
    String token = response.getHeaderString( RESPONSE_HEADER_TOKEN );
    if ( token == null || token.length() == 0 ) {
      return null;
    }

    String header = response.getHeaderString( RESPONSE_HEADER_HEADER );
    String parameter = response.getHeaderString( RESPONSE_HEADER_PARAM );

    return new CsrfToken( header, parameter, token );
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
