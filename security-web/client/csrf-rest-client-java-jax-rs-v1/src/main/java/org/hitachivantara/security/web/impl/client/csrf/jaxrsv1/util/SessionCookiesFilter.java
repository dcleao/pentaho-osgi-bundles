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

package org.hitachivantara.security.web.impl.client.csrf.jaxrsv1.util;

import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;

import javax.annotation.Nonnull;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import java.io.IOException;
import java.net.CookieHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A JAX-RS 1.1 (request and response) client filter which maintains cookies across requests.
 */
public class SessionCookiesFilter extends ClientFilter {

  @Nonnull
  private final CookieHandler cookieHandler;

  /**
   * Creates a filter which uses the system default cookie handler, {@link CookieHandler#getDefault()}.
   */
  public SessionCookiesFilter() {
    this( CookieHandler.getDefault() );
  }

  /**
   * Creates a filter which uses the given cookie handler.
   *
   * @param cookieHandler The cookie handler where to maintain the exchanged cookies.
   */
  public SessionCookiesFilter( @Nonnull CookieHandler cookieHandler ) {

    Objects.requireNonNull( cookieHandler );

    this.cookieHandler = cookieHandler;
  }

  @Override
  public ClientResponse handle( ClientRequest request ) throws ClientHandlerException {

    // Add cookies in the cookie handler to the request.
    try {
      getAddRequestCookies( request )
        .forEach( cookie -> request.getHeaders().add( HttpHeaders.COOKIE, cookie ) );
    } catch ( IOException e ) {
      throw new ClientHandlerException( "Could not add cookies to request.", e );
    }

    ClientResponse response = getNextNotFinal().handle( request );

    // Add response cookies to the cookie handler.
    List<NewCookie> newCookies = response.getCookies();
    if ( newCookies.size() > 0 ) {
      List<String> newCookiesText = newCookies
        .stream()
        .map( NewCookie::toString )
        .filter( cookieText -> cookieText != null && !cookieText.isEmpty() )
        .collect( Collectors.toList() );

      if ( !newCookiesText.isEmpty() ) {
        Map<String, List<String>> responseHeadersMap = new HashMap<>();
        responseHeadersMap.put( HttpHeaders.SET_COOKIE, newCookiesText );

        try {
          cookieHandler.put( request.getURI(), responseHeadersMap );
        } catch ( IOException e ) {
          throw new ClientHandlerException( "Could not save response cookies.", e );
        }
      }
    }

    return response;
  }

  // getNext is final and cannot be stubbed.
  // this wrapper allows unit testing.
  ClientHandler getNextNotFinal() {
    return getNext();
  }

  private Stream<Cookie> getAddRequestCookies( ClientRequest request ) throws IOException {

    Map<String, List<String>> cookiesRequestHeadersMap = cookieHandler.get(
      request.getURI(),
      serializeHeaders( request.getHeaders() ) );

    List<String> addCookiesText = cookiesRequestHeadersMap.get( HttpHeaders.COOKIE );
    if ( addCookiesText == null ) {
      addCookiesText = Collections.emptyList();
    }

    return addCookiesText.stream().map( Cookie::valueOf );
  }

  private Map<String, List<String>> serializeHeaders( MultivaluedMap<String, Object> headers ) {
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
}
