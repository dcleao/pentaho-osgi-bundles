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
 * Copyright (c) 2019-2021 Hitachi Vantara. All rights reserved.
 */

package org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util.internal;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.CsrfToken;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.net.CookieHandler;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsrfUtil {

  /**
   * The name of the response header whose value is the name of the request header on which the value of the CSRF token
   * should be sent on requests to the protected service.
   */
  public static final String RESPONSE_HEADER_HEADER = "X-CSRF-HEADER";

  /**
   * The name of the response header whose value is the name of the request parameter on which the value of the CSRF
   * token should be sent on requests to the protected service.
   * <p>
   * It is preferable to use a request header to send the CSRF token, if possible.
   */
  public static final String RESPONSE_HEADER_PARAM = "X-CSRF-PARAM";

  /**
   * The name of the response header whose value is the CSRF token.
   */
  public static final String RESPONSE_HEADER_TOKEN = "X-CSRF-TOKEN";

  // The response body should be empty, and return 204.
  // The relevant response is in the response headers.
  public static boolean isTokenResponseSuccessful( @Nonnull ClientResponse response ) {
    return response.getStatus() == 204;
  }

  public static CsrfToken readResponseToken( @Nonnull ClientResponse response ) {
    // When CSRF protection is disabled, the token is not returned.
    String token = getResponseHeaderString( response, RESPONSE_HEADER_TOKEN );
    if ( token == null || token.length() == 0 ) {
      return null;
    }

    String header = getResponseHeaderString( response, RESPONSE_HEADER_HEADER );
    String parameter = getResponseHeaderString( response, RESPONSE_HEADER_PARAM );

    return new CsrfToken( header, parameter, token );
  }

  private static String getResponseHeaderString( ClientResponse response, String name ) {
    List<String> values = response.getHeaders().get( name );
    if ( values == null ) {
      return null;
    }

    if ( values.isEmpty() ) {
      return "";
    }

    return String.join( ",", values );
  }

  public static boolean hasClientFilter( @Nonnull Client client,
                                         @Nonnull Class<? extends ClientFilter> clientFilterClass ) {

    ClientHandler handler = client.getHeadHandler();
    do {
      if ( !( handler instanceof ClientFilter ) ) {
        return false;
      }

      ClientFilter filter = (ClientFilter) handler;
      if ( clientFilterClass.isAssignableFrom( filter.getClass() ) ) {
        return true;
      }

      handler = filter.getNext();

    } while ( handler != null );

    return false;
  }

  public static Map<String, List<String>> serializeHeaders( MultivaluedMap<String, Object> headers ) {

    if ( headers == null ) {
      return Collections.emptyMap();
    }

    Map<String, List<String>> stringHeaders = new LinkedHashMap<>( headers.size() );

    for ( Map.Entry<String, List<Object>> entry : headers.entrySet() ) {
      stringHeaders.put(
        entry.getKey(),
        entry.getValue()
          .stream()
          .map( ClientRequest::getHeaderValue )
          .collect( Collectors.toList() ) );
    }

    return stringHeaders;
  }

  public static Stream<Cookie> getCookieStreamForRequest( CookieHandler cookieHandler, ClientRequest request )
    throws IOException {

    Map<String, List<String>> cookiesHeadersMap = cookieHandler.get(
      request.getURI(),
      serializeHeaders( request.getHeaders() ) );

    List<String> cookiesText = cookiesHeadersMap.get( HttpHeaders.COOKIE );
    if ( cookiesText == null ) {
      cookiesText = Collections.emptyList();
    }

    return cookiesText.stream()
      // Strangely, this is added by CookieManager#get, to account for some disambiguation
      // of cookie spec versions... We clearly don't need that here.
      .filter( cookieText -> !"$Version=\"1\"".equals( cookieText ) )
      .map( Cookie::valueOf );
  }
}
