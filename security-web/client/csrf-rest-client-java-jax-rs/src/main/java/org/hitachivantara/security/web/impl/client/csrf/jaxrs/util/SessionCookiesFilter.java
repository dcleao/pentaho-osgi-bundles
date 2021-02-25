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

package org.hitachivantara.security.web.impl.client.csrf.jaxrs.util;

import javax.annotation.Nonnull;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import java.io.IOException;
import java.net.CookieHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A JAX-RS request and response filter which maintains cookies across requests made by a JAX-RS {@link
 * javax.ws.rs.client.Client}.
 */
public class SessionCookiesFilter implements ClientRequestFilter, ClientResponseFilter {

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

  // Request Filter implementation
  @Override
  public void filter( ClientRequestContext requestContext ) throws IOException {
    // Add cookies in the cookie handler to the request.
    getAddRequestCookies( requestContext )
      .forEach( cookie -> requestContext.getHeaders().add( HttpHeaders.COOKIE, cookie ) );
  }

  // Response Filter implementation
  @Override
  public void filter( ClientRequestContext requestContext, ClientResponseContext responseContext )
    throws IOException {

    // Add response cookies to the cookie handler.
    Map<String, NewCookie> newCookieMap = responseContext.getCookies();
    if ( newCookieMap.size() > 0 ) {
      List<String> newCookiesText = newCookieMap.values().stream()
        .map( NewCookie::toString ).collect( Collectors.toList() );

      Map<String, List<String>> responseHeadersMap = new HashMap<>();
      responseHeadersMap.put( HttpHeaders.SET_COOKIE, newCookiesText );

      cookieHandler.put( requestContext.getUri(), responseHeadersMap );
    }
  }

  private Stream<Cookie> getAddRequestCookies( ClientRequestContext requestContext ) throws IOException {

    Map<String, List<String>> cookiesRequestHeadersMap = cookieHandler.get(
      requestContext.getUri(),
      requestContext.getStringHeaders() );

    List<String> addCookiesText = cookiesRequestHeadersMap.get( HttpHeaders.COOKIE );
    if ( addCookiesText == null ) {
      addCookiesText = Collections.emptyList();
    }

    return addCookiesText.stream().map( Cookie::valueOf );
  }
}
